package com.msb.mall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.msb.common.constant.OrderConstant;
import com.msb.common.dto.SeckillOrderDto;
import com.msb.common.exception.NoStockExecption;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.order.Interceptor.AuthInterceptor;
import com.msb.mall.order.client.ProductClient;
import com.msb.mall.order.dto.OrderCreateTO;
import com.msb.mall.order.entity.OrderItemEntity;
import com.msb.mall.order.fegin.CartFeginService;
import com.msb.mall.order.fegin.MemberFeignService;
import com.msb.mall.order.fegin.WareFeignService;
import com.msb.mall.order.service.OrderItemService;
import com.msb.mall.order.utils.OrderMsgProducer;
import com.msb.mall.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.order.dao.OrderDao;
import com.msb.mall.order.entity.OrderEntity;
import com.msb.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private CartFeginService cartFeginService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductClient productClient;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    OrderMsgProducer orderMsgProducer;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo vo=new OrderConfirmVo();
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            Long id = memberVO.getId();
            System.out.println("id = " + id);
            List<MemberAddressVo> addressVos = memberFeignService.getAddress(id);
            vo.setAddress(addressVos);
        }, executor);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> cartItems=cartFeginService.getUserCartItems();
            vo.setItems(cartItems);
        }, executor);

        try {
            CompletableFuture.allOf(future1,future2).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("订单服务开始计算价格,{}",vo);

        String token = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_PREFIX+":" +memberVO.getId(),token);
        vo.setOrderToken(token);

        return vo;
    }

    private Lock lock=new ReentrantLock();

   // @GlobalTransactional
    @Transactional
    @Override
    public OrderResponseVO submitOrder(OrderSubmitVO vo) throws NoStockExecption{
        OrderResponseVO responseVO = new OrderResponseVO();
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        log.info("订单服务开始提交订单,{},{}",vo,memberVO);
        //保证原子性操作
        String script="if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.ORDER_TOKEN_PREFIX + ":" + memberVO.getId()), vo.getOrderToken());
        if (execute==0){

            return responseVO;
        }
        /*try {
            lock.lock();
            String token = redisTemplate.opsForValue().get(OrderConstant.ORDER_TOKEN_PREFIX+":" +memberVO.getId());
            if (token!=null && token.equals(vo.getOrderToken())){
                //第一次提交
                //删除token
                redisTemplate.delete(OrderConstant.ORDER_TOKEN_PREFIX+":" +memberVO.getId());
            }else {
                return responseVO;
            }
        }finally {
            lock.unlock();
        }*/
        OrderCreateTO orderCreateTO=createOrder(vo);
        responseVO.setOrderEntity(orderCreateTO.getOrderEntity());

        saveOrder(orderCreateTO);

        lockWareSkuStock(orderCreateTO, responseVO);

        //同步更新用户的会员
       // int i=1/0;


        //订单成功后需要给消息中间件发送延迟消息
        orderMsgProducer.sendOrderMsg(orderCreateTO.getOrderEntity().getOrderSn());
        return responseVO;
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo=new PayVo();
        OrderEntity orderEntity = this.getBaseMapper().getOrderByOrderSn(orderSn);
        payVo.setOut_trader_no(orderEntity.getOrderSn());
        payVo.setTotal_amount(orderEntity.getTotalAmount().setScale(2, RoundingMode.UP).toString());
        payVo.setSubject(orderEntity.getOrderSn());
        payVo.setBody(orderEntity.getOrderSn());

        log.info("订单服务开始生成支付信息,{}",payVo);

        return payVo;
    }

    @Override
    public void updateOrderStatus(String orderSn,Integer status) {
        this.getBaseMapper().updateOrderStatus(orderSn,status);

    }

    @Override
    public void handleOrderComplete(String orderSn) {
        this.updateOrderStatus(orderSn, OrderConstant.OrderStateEnum.TO_SEND_GOODS.getCode());
        //更新库存数量
        log.info("开始处理更新库存状态，{}",orderSn);
        //1.根据订单号获取订单中的商品信息
        List<OrderItemEntity> orderItemEntitys = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        //2.扣减对应仓库中的库存
        WareSkuLockVO wareSkuLockVO=new WareSkuLockVO();
        wareSkuLockVO.setOrderSN(orderSn);
        wareSkuLockVO.setItems(orderItemEntitys.stream().map(item->{
            OrderItemVo itemVo=new OrderItemVo();
            itemVo.setSkuId(item.getSkuId());
            itemVo.setCount(item.getSkuQuantity());
            itemVo.setTitle(item.getSkuName());
            return itemVo;
        }).collect(Collectors.toList()));
        R r = wareFeignService.orderDeductStock(wareSkuLockVO);
        if (r.getCode()==0){
            log.info("扣减库存成功");
        }

        //清空购物车中的商品
        log.info("开始处理购物车中的商品，{}",orderSn);
        boolean b = cartFeginService.deleteCart();
        if (b){
            log.info("清空购物车成功");
        }

        //更新会员积分
        log.info("开始处理会员积分，{}",orderSn);
        log.info("处理会员积分成功");



    }

    @Transactional
    @Override
    public void quickCreateOrder(SeckillOrderDto orderDto) {
        log.info("开始处理秒杀订单，{}",orderDto);
        //快速完成秒杀活动的订单处理
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderDto.getOrderSN());
        orderEntity.setStatus(OrderConstant.OrderStateEnum.FOR_THE_PAYMENT.getCode());
        orderEntity.setMemberId(orderDto.getMemberId());
        orderEntity.setCreateTime(new Date());
        orderEntity.setTotalAmount(orderDto.getSeckillPrice().multiply(new BigDecimal(orderDto.getNum())));
        this.save(orderEntity);
        OrderItemEntity itemEntity = new OrderItemEntity();

        itemEntity.setOrderSn(orderDto.getOrderSN());
        itemEntity.setSkuPrice(orderDto.getSeckillPrice());
        itemEntity.setSkuId(orderDto.getSkuId());
        itemEntity.setRealAmount(orderDto.getSeckillPrice().multiply(new BigDecimal(orderDto.getNum())));
        itemEntity.setSkuQuantity(orderDto.getNum());


        boolean save = orderItemService.save(itemEntity);
        if (save){
            log.info("保存订单成功");
        }
    }

    private void lockWareSkuStock(OrderCreateTO orderCreateTO, OrderResponseVO responseVO)  throws NoStockExecption{
        WareSkuLockVO wareSkuLockVO=new WareSkuLockVO();
        wareSkuLockVO.setOrderSN(orderCreateTO.getOrderEntity().getOrderSn());
        wareSkuLockVO.setItems(orderCreateTO.getOrderItemEntitys().stream().map(item->{
            OrderItemVo itemVo=new OrderItemVo();
            itemVo.setSkuId(item.getSkuId());
            itemVo.setCount(item.getSkuQuantity());
            itemVo.setTitle(item.getSkuName());
            return itemVo;
        }).collect(Collectors.toList()));
        R r = wareFeignService.orderLockStock(wareSkuLockVO);
        if (r.getCode()==0){
            log.info("锁库成功");
            responseVO.setCode(0);
        }
        else {
            log.info("锁库失败");
            responseVO.setCode(2);
            throw new NoStockExecption(10000L);
        }
    }

    private void saveOrder(OrderCreateTO orderCreateTO) {
        OrderEntity orderEntity = orderCreateTO.getOrderEntity();
        orderService.save(orderEntity);

        List<OrderItemEntity> orderItemEntitys = orderCreateTO.getOrderItemEntitys();
        orderItemService.saveBatch(orderItemEntitys);

    }

    private OrderCreateTO createOrder(OrderSubmitVO vo) {
        OrderCreateTO createTO=new OrderCreateTO();
        OrderEntity orderEntity = buildOrder(vo);
        createTO.setOrderEntity(orderEntity);

        List<OrderItemEntity> orderItemEntitys=buildOrderItems(orderEntity.getOrderSn());
        BigDecimal total_amount=new BigDecimal(0);
        for (OrderItemEntity orderItemEntity : orderItemEntitys) {
            System.out.println(orderItemEntity.getSkuQuantity()+"  "+orderItemEntity.getSkuPrice());
            BigDecimal total = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
            total_amount = total_amount.add(total);
        }
        System.out.println(total_amount);
        orderEntity.setTotalAmount(total_amount);


        createTO.setOrderItemEntitys(orderItemEntitys);

        return createTO;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {

        List<OrderItemEntity> orderItemEntitys=new ArrayList<>();
        List<OrderItemVo> userCartItems = cartFeginService.getUserCartItems();
        if (userCartItems!=null && userCartItems.size()>0){
            List<Long> spuIds = new ArrayList<>();
            for (OrderItemVo orderItemVo : userCartItems) {
                if(!spuIds.contains(orderItemVo.getSpuId())){
                    log.info("【商品上架】开始查询spu信息,{}",orderItemVo.getSpuId());
                    spuIds.add(orderItemVo.getSpuId());
                }
            }
            System.out.println(spuIds);
            Long[] spuIdsArray = new Long[spuIds.size()];
            spuIdsArray = spuIds.toArray(spuIdsArray);
            System.out.println("---->" + spuIdsArray.length);
            // 远程调用商品服务获取到对应的SPU信息
            List<OrderItemSpuInfoVO> spuInfos = productClient.getOrderItemSpuInfoBySpuId(spuIdsArray);
            Map<Long, OrderItemSpuInfoVO> map = spuInfos.stream().collect(Collectors.toMap(OrderItemSpuInfoVO::getId, item -> item));

            for (OrderItemVo userCartItem : userCartItems){
                OrderItemSpuInfoVO spuInfo=map.get(userCartItem.getSpuId());
                OrderItemEntity orderItemEntity= buildOrderItem(userCartItem,spuInfo);
                orderItemEntity.setOrderSn(orderSn);
                orderItemEntitys.add(orderItemEntity);
            }
        }


        return orderItemEntitys;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo userCartItem, OrderItemSpuInfoVO spuInfo) {
        OrderItemEntity entity=new OrderItemEntity();

        entity.setSkuId(userCartItem.getSkuId());
        entity.setSkuName(userCartItem.getTitle());
        entity.setSkuPic(userCartItem.getImage());
        entity.setSkuQuantity(userCartItem.getCount());
        List<String> skuAttr = userCartItem.getSkuAttr();
        String skuAttrStr = StringUtils.collectionToDelimitedString(skuAttr, ";");
        entity.setSkuAttrsVals(skuAttrStr);

        entity.setSpuId(spuInfo.getId());
        entity.setSpuName(spuInfo.getSpuName());
        entity.setCategoryId(spuInfo.getCatalogId());
        entity.setSpuPic(spuInfo.getImg());



        entity.setGiftGrowth(userCartItem.getPrice().intValue());
        entity.setGiftIntegration(userCartItem.getPrice().intValue());
        entity.setSkuPrice(userCartItem.getPrice());
        System.out.println("skuPrice"+userCartItem.getPrice());

        return entity;

    }

    private OrderEntity buildOrder(OrderSubmitVO vo) {
        OrderEntity orderEntity=new OrderEntity();
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        orderEntity.setMemberId(memberVO.getId());
        orderEntity.setMemberUsername(memberVO.getUsername());

        MemberAddressVo memberAddressVo = memberFeignService.getAddressById(vo.getAddrId());
        orderEntity.setReceiverName(memberAddressVo.getName());
        orderEntity.setReceiverPhone(memberAddressVo.getPhone());
        orderEntity.setReceiverPostCode(memberAddressVo.getPostCode());
        orderEntity.setReceiverProvince(memberAddressVo.getProvince());
        orderEntity.setReceiverCity(memberAddressVo.getCity());
        orderEntity.setReceiverRegion(memberAddressVo.getRegion());
        orderEntity.setReceiverDetailAddress(memberAddressVo.getDetailAddress());
        orderEntity.setStatus(OrderConstant.OrderStateEnum.FOR_THE_PAYMENT.getCode());


        return orderEntity;
    }

}
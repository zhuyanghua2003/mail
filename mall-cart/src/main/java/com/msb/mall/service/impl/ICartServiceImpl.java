package com.msb.mall.service.impl;

import com.alibaba.fastjson.JSON;
import com.msb.common.constant.CartConstant;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.Interceptor.AuthInterceptor;
import com.msb.mall.feign.ProductFeignService;
import com.msb.mall.service.ICartService;
import com.msb.mall.vo.Cart;
import com.msb.mall.vo.CartItem;
import com.msb.mall.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ICartServiceImpl implements ICartService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Override
    public Cart getCartList() {
         BoundHashOperations<String, Object, Object> hashOperations = getCartKeyOperation();
         Set<Object> keys = hashOperations.keys();
         Cart cart = new Cart();
         List<CartItem> list=new ArrayList<>();
         for (Object k : keys){
             String key=(String) k;
             Object o=hashOperations.get(key);
             String json = (String) o;
             CartItem item = JSON.parseObject(json, CartItem.class);
             list.add(item);
         }
         cart.setItems(list);


        return cart;
    }

    @Override
    public CartItem addCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> hashOperations = getCartKeyOperation();
        Object o=hashOperations.get(skuId.toString());
        if(o != null){
            // 说明已经存在了这个商品那么修改商品的数量即可
            String json = (String) o;
            CartItem item = JSON.parseObject(json, CartItem.class);
            item.setCount(item.getCount()+num);
            hashOperations.put(skuId.toString(),JSON.toJSONString(item));
            return item;
        }
        CartItem item = new CartItem();

        CompletableFuture future1= CompletableFuture.runAsync(() -> {
            R r = productFeignService.info(skuId);
            String skuInfoJSON = (String) r.get("skuInfoJSON");
            SkuInfoVo skuInfo = JSON.parseObject(skuInfoJSON,SkuInfoVo.class);
            item.setSkuId(skuInfo.getSkuId());
            item.setSpuId(skuInfo.getSpuId());
            System.out.println(skuInfo);
            item.setImage(skuInfo.getSkuDefaultImg());
            item.setTitle(skuInfo.getSkuTitle());
            item.setCount(num);
            item.setPrice(skuInfo.getPrice());
            item.setCheck(true);
        }, threadPoolExecutor);
        CompletableFuture future2= CompletableFuture.runAsync(() -> {
            List<String> skuSaleAttrs = productFeignService.getSkuSaleAttrs(skuId);
            item.setSkuAttr(skuSaleAttrs);
        }, threadPoolExecutor);

        CompletableFuture.allOf(future1,future2).get();
        String json = JSON.toJSONString(item);
        hashOperations.put(skuId.toString(),json);
        log.info("添加购物车成功,{}",item);

        return item;
    }

    @Override
    public List<CartItem> getUserCartItems() {
        BoundHashOperations<String, Object, Object> operations = getCartKeyOperation();
        List<Object> values = operations.values();
        List<CartItem> list = values.stream().map(item -> {
            String json = (String) item;
            CartItem cartItem = JSON.parseObject(json, CartItem.class);
            return cartItem;
        }).filter(item -> {
            return item.isCheck();
        }).collect(Collectors.toList());
        return list;
    }

    //清空当前用户购物车中的上哦
    @Override
    public boolean deleteCart() {
        BoundHashOperations<String, Object, Object> operations = getCartKeyOperation();
        // 先获取Hash中的所有字段，判断是否为空
        Set<Object> keys = operations.keys();
        if (CollectionUtils.isEmpty(keys)) {
            log.info("购物车已为空，无需清空");
            return true;
        }
        // 传入所有字段进行删除（避免无参delete()触发异常）
        Long delete = operations.delete(keys.toArray());
        if (delete > 0) {
            log.info("清空购物车成功，删除{}个商品", delete);
            return true;
        }
        log.info("清空购物车失败");
        return false;
    }

    private BoundHashOperations<String, Object, Object> getCartKeyOperation() {
        // hash key: cart:1   skuId:cartItem
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        String cartKey = CartConstant.CART_PERFIX + memberVO.getId();
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cartKey);
        return hashOperations;
    }
}

package com.msb.mall.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.msb.common.constant.OrderConstant;
import com.msb.common.constant.SeckillConstant;
import com.msb.common.dto.SeckillOrderDto;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.Interceptor.AuthInterceptor;
import com.msb.mall.dto.SeckillSkuRedisDto;
import com.msb.mall.feign.CouponFeignService;
import com.msb.mall.feign.ProductFeignService;
import com.msb.mall.service.SeckillService;
import com.msb.mall.vo.SeckillSessionEntity;
import com.msb.mall.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RocketMQTemplate rocketMQTemplate;


    @Trace
    @Override
    public void uploadSeckillSku3Days() {
        R r = couponFeignService.getLatest3DaysSession();
        if (r.getCode() == 0){
            String json =(String) r.get("data");
            List<SeckillSessionEntity> seckillSessionEntities= JSON.parseArray(json, SeckillSessionEntity.class);
            //缓存SKU基本信息
            saveSessionInfos(seckillSessionEntities);
            //缓存每日秒杀信息
            saveSessionSkuInfos(seckillSessionEntities);
        }

    }

    public List<SeckillSkuRedisDto> blockHandler(BlockException blockException){
        log.error("限流执行的blockHandler方法---》》》》》{}",blockException.getMessage());
        return null;
    }

    @SentinelResource(value = "currentSeckillSkusResources",blockHandler = "blockHandler")
    @Override
    public List<SeckillSkuRedisDto> getCurrentSeckillSkus() {
        long time = new Date().getTime();

        try (Entry entry= SphU.entry("getCurrentSeckillSkusResources")){
            Set<String> keys = redisTemplate.keys(SeckillConstant.SESSION_CHACE_PREFIX + "*");
            for (String key : keys){
                String replace=key.replace(SeckillConstant.SESSION_CHACE_PREFIX,"");
                String[] s = replace.split("_");
                long start=Long.parseLong(s[0]);
                long end=Long.parseLong(s[1]);
                if (time>=start && time<=end){
                    System.out.println(key);
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    System.out.println(range);
                    BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKU_CHACE_PREFIX);
                    List<String> list = ops.multiGet(range);
                    System.out.println(list);
                    if (list!=null && list.size()>0){
                        List<SeckillSkuRedisDto> collect = list.stream().map(item -> {
                            SeckillSkuRedisDto seckillSkuRedisDto = JSON.parseObject(item, SeckillSkuRedisDto.class);
                            System.out.println(seckillSkuRedisDto);
                            return seckillSkuRedisDto;
                        }).collect(Collectors.toList());
                        log.info("成功查询了{}条数据",collect.size());
                        System.out.println(collect);
                        return collect;
                    }
                    log.info("未查询的数据，请稍后再试");
                }
            }
        }catch (BlockException ex){

            log.error("getCurrentSeckillSkusResources--->>>被限制访问了");
        }






        return null;
    }

    @Override
    public SeckillSkuRedisDto getSeckillSessionBySkuId(Long skuId) {


        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKU_CHACE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys!=null && keys.size()>0){
            String regx="\\d_"+skuId;
            for (String key : keys){
                boolean matches = Pattern.matches(regx, key);
                if (matches){
                    String json = ops.get(key);
                    SeckillSkuRedisDto dto = JSON.parseObject(json, SeckillSkuRedisDto.class);


                    return dto;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String code, Integer num) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SeckillConstant.SKU_CHACE_PREFIX);
        String json=ops.get(killId);
        if (StringUtils.isNotBlank( json)){
            SeckillSkuRedisDto dto = JSON.parseObject(json, SeckillSkuRedisDto.class);
            //校验时效性
            Long startTime = dto.getStartTime();
            Long endTime = dto.getEndTime();
            long now = new Date().getTime();
            if (now>=startTime && now<=endTime){
                //校验随机码
                String randomCode = dto.getRandCode();
                Long skuId = dto.getSkuId();
                String redisKillId=dto.getPromotionSessionId()+"_"+skuId;
                if (randomCode.equals(code) && killId.equals(redisKillId)){
                    if (num<=dto.getSeckillLimit().intValue()){
                        MemberVO memberVO =(MemberVO) AuthInterceptor.threadLocal.get();
                        Long id=memberVO.getId();
                        String redisKey=id+"_"+redisKillId;
                        Boolean aBoolean = redisTemplate.opsForValue()
                                .setIfAbsent(redisKey, num.toString(), (endTime - now), TimeUnit.MILLISECONDS);
                        if (aBoolean){
                            RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE+randomCode);
                            System.out.println(semaphore);
                            try {
                                boolean b1 = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                if (b1){

                                    String orderSN=UUID.randomUUID().toString().replace("-", "");
                                    log.info("秒杀成功,{}",orderSN);
                                    SeckillOrderDto orderDto=new SeckillOrderDto();
                                    orderDto.setOrderSN(orderSN);
                                    orderDto.setSkuId(skuId);
                                    orderDto.setNum(num);
                                    orderDto.setPromotionSessionId(dto.getPromotionSessionId());
                                    orderDto.setSeckillPrice(dto.getSeckillPrice());
                                    orderDto.setMemberId(id);

                                    //继续完成快速下订单的操作 ----》RocketMQ
                                    rocketMQTemplate.sendOneWay(OrderConstant.ROCKETMQ_SECKILL_ORDER_TOPIC,
                                            JSON.toJSONString(orderDto));
                                    return orderSN;
                                }else {

                                    log.info("秒杀失败,服务器繁忙");
                                }
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }else {
                            log.info("超出秒杀商品次数限制,不可重复秒杀");
                        }
                    }else {
                        log.info("超出秒杀商品数量限制,{},{}",num,dto.getSeckillLimit());
                    }
                }else {
                    log.info("随机码校验失败");
                }
            }else {
                log.info("秒杀活动已结束");
            }
        }else {
            log.info("未查到数据");
        }


        return null;
    }

    private void saveSessionSkuInfos(List<SeckillSessionEntity> seckillSessionEntities) {
        log.info("准备缓存秒杀活动信息,{}",seckillSessionEntities);

        for (SeckillSessionEntity seckillSessionEntity : seckillSessionEntities){
            //循环缓存每一个获得
            long start=seckillSessionEntity.getStartTime().getTime();
            long end=seckillSessionEntity.getEndTime().getTime();
            String key= SeckillConstant.SESSION_CHACE_PREFIX+start+"_"+end;
            Boolean flag = redisTemplate.hasKey(key);
            if (!flag){
                List<String> collect = seckillSessionEntity.getRelationEntities().stream().map(item -> {
                    return item.getPromotionSessionId()+"_"+ item.getSkuId().toString();
                }).collect(Collectors.toList());
                Long l = redisTemplate.opsForList().leftPushAll(key, collect);
                if (l>0){
                    log.info("成功缓存了"+l+"条数据");
                }
            }

        }


    }

    private void saveSessionInfos(List<SeckillSessionEntity> seckillSessionEntities) {
        log.info("准备缓存秒杀活动信息,{}",seckillSessionEntities);
        seckillSessionEntities.stream().forEach(session -> {
            //循环取出每个Session，取出对应的SkuID,封装相关信息
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SeckillConstant.SKU_CHACE_PREFIX);
            session.getRelationEntities().stream().forEach(item -> {
                String skuKey =item.getPromotionSessionId()+"_"+ item.getSkuId();
                Boolean flag = redisTemplate.hasKey(skuKey);
                if(!flag){
                    SeckillSkuRedisDto dto=new SeckillSkuRedisDto();
                    //获取Sku的基本信息
                    R info=productFeignService.info(item.getSkuId());
                    if (info.getCode()==0){
                        //保存Sku基本信息

                        String json = (String) info.get("skuInfoJSON");
                        dto.setSkuInfoVo(JSON.parseObject(json,SkuInfoVo.class));
                    }
                /*dto.setSkuId(item.getSkuId());
                dto.setSeckillCount(item.getSeckillCount());
                dto.setSeckillLimit(item.getSeckillLimit());
                dto.setSeckillPrice(item.getSeckillPrice());
                dto.setSeckillSort(item.getSeckillSort());*/
                    BeanUtils.copyProperties(item,dto);
                    dto.setStartTime(session.getStartTime().getTime());
                    dto.setEndTime(session.getEndTime().getTime());
                    dto.setPromotionSessionId(item.getPromotionSessionId());

                    String token= UUID.randomUUID().toString().replace("_","");
                    dto.setRandCode(token);
                    //获取Sku的秒杀信息
                    RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(item.getSeckillCount().intValue());


                    hashOps.put(skuKey,JSON.toJSONString(dto));
                    log.info("成功缓存了"+session.getId()+"秒杀活动");
                }
            });
        });


    }
}

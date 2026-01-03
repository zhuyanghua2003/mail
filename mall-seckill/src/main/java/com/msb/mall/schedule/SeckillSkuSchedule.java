package com.msb.mall.schedule;

import com.msb.mall.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SeckillSkuSchedule {
    @Autowired
    private SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;

    @Async
    @Scheduled(cron = "0 * * * * *")
    public void uploadSeckillSku3Days() {
        //定时调用上架商品的方法

        RLock lock = redissonClient.getLock("seckill:upload:lock");
        lock.lock(10, TimeUnit.SECONDS);
        log.info("定时上架秒杀商品执行了...." + new Date());
        try {
            seckillService.uploadSeckillSku3Days();
        }catch (Exception e){

        }finally {
            lock.unlock();
        }
    }


}

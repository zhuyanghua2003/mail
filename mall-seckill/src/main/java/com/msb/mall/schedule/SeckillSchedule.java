package com.msb.mall.schedule;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

//@Slf4j
//@Component
public class SeckillSchedule {

    //@Autowired
    RedissonClient redissonClient;

//    @Async
//    @Scheduled(cron = "0 0 2 * * *")
//    public void schedule() {
//
//
//        log.info("开始执行定时任务");
//        RLock lock = redissonClient.getLock("seckill:upload:lock");
//        lock.lock(10, TimeUnit.SECONDS);
//
//
//        try {
//            Thread.sleep(5000);
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            lock.unlock();
//        }
//    }


}

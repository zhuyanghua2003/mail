package com.msb.mall.search.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedisConfig {

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://redis.sangomall.svc.cluster.local.:6379");
        config.useSingleServer().setPassword("123456");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}

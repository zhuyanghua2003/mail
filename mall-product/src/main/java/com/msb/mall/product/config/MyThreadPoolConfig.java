package com.msb.mall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class MyThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        return new  ThreadPoolExecutor(20
                , 200
                , 10
                , TimeUnit.SECONDS
                , new LinkedBlockingDeque<>(10000)
                , Executors.defaultThreadFactory()
                , new ThreadPoolExecutor.AbortPolicy()
                );
    }
}

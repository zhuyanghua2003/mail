package com.msb.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.msb.mall.order.dao")
@EnableDiscoveryClient
@EnableFeignClients
public class OrderStarterApp {

    public static void main(String[] args) {
        SpringApplication.run(OrderStarterApp.class, args);
    }
}

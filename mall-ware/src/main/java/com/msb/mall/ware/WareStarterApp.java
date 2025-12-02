package com.msb.mall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.msb.mall.ware.dao")
@EnableDiscoveryClient
@EnableFeignClients("com.msb.mall.ware.feign")
public class WareStarterApp {
    public static void main(String[] args) {
        SpringApplication.run(WareStarterApp.class, args);
    }
}

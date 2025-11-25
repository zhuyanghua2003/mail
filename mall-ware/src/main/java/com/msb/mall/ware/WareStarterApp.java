package com.msb.mall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.msb.mall.ware.dao")
@EnableDiscoveryClient
public class WareStarterApp {
    public static void main(String[] args) {
        SpringApplication.run(WareStarterApp.class, args);
    }
}

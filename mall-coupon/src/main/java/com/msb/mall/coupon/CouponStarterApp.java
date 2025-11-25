package com.msb.mall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.msb.mall.coupon.dao")
@EnableDiscoveryClient
public class CouponStarterApp {

    public static void main(String[] args) {
        SpringApplication.run(CouponStarterApp.class, args);
    }
}

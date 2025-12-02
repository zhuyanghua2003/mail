package com.msb.mall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MallSearchStarterApp {
    public static void main(String[] args) {
        SpringApplication.run(MallSearchStarterApp.class, args);
    }
}

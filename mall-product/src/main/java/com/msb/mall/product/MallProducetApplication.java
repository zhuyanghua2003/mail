package com.msb.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@MapperScan("com.msb.mall.product.dao")
@EnableDiscoveryClient
@EnableFeignClients
public class MallProducetApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallProducetApplication.class, args);
    }

}

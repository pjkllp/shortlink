package com.nageoffer.shortlink.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient  // 注册到 Nacos
public class ShortLinkGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortLinkGatewayApplication.class, args);
    }
}
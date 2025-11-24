package com.neusoft.neu23;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 * 端口: 8080
 */
@SpringBootApplication
@EnableDiscoveryClient // 开启 Nacos 服务发现
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("========================================");
        System.out.println("🚀 API Gateway Started on Port 8080");
        System.out.println("========================================");
    }
}
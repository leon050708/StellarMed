package com.neusoft.neu23;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 医生最终确认服务启动类
 * 支持服务发现和Feign远程调用（按照23456顺序调用各个AI服务）
 */
@SpringBootApplication(scanBasePackages = "com.assist, com.neusoft.neu23")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.neusoft.neu23.tc.client")
@MapperScan("com.neusoft.neu23.mapper")
public class DoctorConfirmServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DoctorConfirmServiceApplication.class, args);
    }
}
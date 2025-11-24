package com.assist.diagnosis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(
        scanBasePackages = {
                "com.assist"
        }
)
@MapperScan("com.assist.diagnosis.mapper")
@EnableDiscoveryClient  // 启用 Nacos 服务发现
public class DiagnosisAiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiagnosisAiServiceApplication.class, args);
    }
}
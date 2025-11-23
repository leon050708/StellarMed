package com.assist.diagnosis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
// import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        scanBasePackages = {
                "com.assist"
        }
)
@MapperScan("com.assist.diagnosis.mapper")
// @EnableDiscoveryClient  // TODO: 开启 Nacos 时再放开
// @EnableFeignClients(basePackages = "com.assist.diagnosis.client") // 目前不需要 Feign，可先不建 client 包
public class DiagnosisAiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiagnosisAiServiceApplication.class, args);
    }
}
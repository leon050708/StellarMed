package com.neusoft.neu23;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Test Suggestion AI Service 主启动类
 * AI 检查建议服务
 * 
 * @author StellarMed Team
 */
@SpringBootApplication(scanBasePackages = {
    "com.neusoft.neu23",
    "com.assist.common"
})
// @EnableDiscoveryClient 已禁用，用于测试（不连接 Nacos）
@EnableFeignClients  // 启用 Feign 客户端
@MapperScan("com.neusoft.neu23.mapper")
public class TestSuggestionAiServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestSuggestionAiServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("✅ Test Suggestion AI Service Started Successfully!");
        System.out.println("========================================");
    }
}

package com.neusoft.neu23;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI问诊总结服务启动类
 * 负责启动服务并注册到服务发现中心
 */
@SpringBootApplication(scanBasePackages = "com.assist, com.neusoft.neu23") // 扫描公共模块和当前服务包
@EnableDiscoveryClient // 启用Nacos服务发现
@EnableFeignClients(basePackages = "com.neusoft.neu23.tc.client") // 启用Feign客户端
@MapperScan("com.neusoft.neu23.mapper") // 扫描MyBatis Mapper接口
public class App {
    public static void main(String[] args) {
        // 禁用 Nacos Config 检查
        System.setProperty("spring.cloud.nacos.config.import-check.enabled", "false");
        System.setProperty("spring.cloud.nacos.config.enabled", "false");
        // 优化启动速度
        System.setProperty("spring.cloud.nacos.discovery.watch.enabled", "false");
        System.setProperty("spring.cloud.nacos.discovery.fail-fast", "false");
        
        SpringApplication app = new SpringApplication(App.class);
        app.setLogStartupInfo(true); // 启用启动日志
        app.run(args);
    }
}
package com.neusoft.neu23;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 处方AI服务主应用类
 * 
 * @author StellarMed
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.neusoft.neu23.mapper")
public class PrescriptionApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrescriptionApplication.class, args);
    }
}

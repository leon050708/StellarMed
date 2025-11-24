package com.assist.patient;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 患者与会话服务启动类
 * 核心编排服务，负责调用所有AI服务
 */
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.assist.patient.mapper")
public class PatientServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PatientServiceApplication.class, args);
    }
}


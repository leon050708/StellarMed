package com.assist.symptom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 症状AI服务启动类
 * 负责将自然语言症状转为结构化医学字段
 */
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.assist.symptom.mapper")
public class SymptomAiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SymptomAiServiceApplication.class, args);
    }
}


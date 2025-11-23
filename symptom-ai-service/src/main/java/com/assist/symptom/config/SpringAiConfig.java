package com.assist.symptom.config;

import org.springframework.context.annotation.Configuration;

/**
 * SpringAI配置类
 * 通义千问的配置通过application.yml中的spring.ai.dashscope配置
 */
@Configuration
public class SpringAiConfig {
    // SpringAI会自动配置DashScopeChatModel，无需手动配置
}


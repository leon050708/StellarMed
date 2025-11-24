package com.assist.diagnosis.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI + 通义千问配置
 */
@Configuration
public class SpringAiConfig {

    /**
     * 使用 Spring AI Alibaba Starter 自动注入的 ChatModel，
     * 在这里包装成 ChatClient，后面业务层直接注入 ChatClient 用就行。
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
package com.assist.diagnosis.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI + 通义千问配置（使用OpenAI兼容模式）
 */
@Configuration
public class SpringAiConfig {

    /**
     * 使用 Spring AI OpenAI 兼容模式（通义千问）
     * 在这里包装成 ChatClient，后面业务层直接注入 ChatClient 用就行。
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
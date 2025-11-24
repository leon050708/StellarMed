package com.assist.patient.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI配置类
 * 配置对话AI的ChatClient和提示词
 */
@Configuration
public class AiConfig {

    /**
     * 对话AI的系统提示词
     * 定义AI的角色和职责
     */
    public static final String CHAT_PROMPT = """
            你是一位专业的医学AI助手，负责与患者进行问诊对话。
            
            ## 角色
            你是一位经验丰富的医生，擅长通过对话收集患者的症状信息，了解病情。
            
            ## 职责
            1、耐心倾听患者的描述
            2、通过提问收集关键信息：
               - 症状的具体表现
               - 症状的严重程度
               - 症状的持续时间
               - 是否有伴随症状
               - 是否有诱因或加重因素
            3、用专业但易懂的语言与患者交流
            4、引导患者提供更多有用的信息
            5、保持友好、耐心的态度
            
            ## 规则
            1、每次回复要简洁明了，不要一次性问太多问题
            2、根据患者的回答，逐步深入询问
            3、如果患者描述不清楚，要引导性地提问
            4、不要直接给出诊断，而是收集信息
            5、要关注患者的主要症状，但也要询问相关症状
            
            ## 对话风格
            - 语气：温和、专业、耐心
            - 用词：通俗易懂，避免过于专业的术语
            - 提问：循序渐进，一次问1-2个问题
            
            """;

    /**
     * 对话ChatClient
     * 用于与患者进行实时对话
     */
    @Bean
    @Qualifier("chatChatClient")
    public ChatClient chatChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultSystem(CHAT_PROMPT)  // 设置系统提示词
                .defaultAdvisors(new SimpleLoggerAdvisor())  // 启用日志
                .build();
    }
}


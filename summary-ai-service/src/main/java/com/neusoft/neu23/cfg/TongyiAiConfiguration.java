package com.neusoft.neu23.cfg;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通义千问AI配置
 * 为 summary-ai-service 提供AI模型调用能力
 * 用于生成医疗问诊总结
 */
@Configuration
public class TongyiAiConfiguration {

    /**
     * 默认系统提示词
     * 定义AI助手的基本角色和行为准则
     */
    private static final String DEFAULT_SYSTEM_PROMPT = """
        你是一位资深的医疗AI助手，专门为医生生成医疗问诊总结。
        
        你的职责：
        1. 基于所有AI输出生成结构化的医疗总结
        2. 生成可直接给医生阅读的专业医疗文档
        3. 确保总结内容准确、专业、完整
        
        重要原则：
        - 使用专业、规范的医疗术语
        - 确保信息的准确性和完整性
        - 按照医疗总结模板组织内容
        - 突出关键信息和风险点
        """;

    /**
     * 创建 ChatClient Bean
     * 只有当 OpenAiChatModel Bean 存在时才创建
     */
    @Bean
    @Qualifier("chatClientQwen")
    @ConditionalOnBean(OpenAiChatModel.class)
    public ChatClient chatClientQwen(OpenAiChatModel model) {
        return ChatClient.builder(model)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}


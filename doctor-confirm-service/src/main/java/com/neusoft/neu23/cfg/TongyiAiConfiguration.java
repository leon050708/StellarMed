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
 * 为 doctor-confirm-service 提供AI模型调用能力
 * 用于生成医生确认建议、诊断对比分析、合理性评估等场景
 */
@Configuration
public class TongyiAiConfiguration {

    /**
     * 默认系统提示词
     * 定义AI助手的基本角色和行为准则
     */
    private static final String DEFAULT_SYSTEM_PROMPT = """
        你是一位资深的医疗AI助手，专门为医生提供诊断确认辅助服务。
        
        你的职责：
        1. 基于助诊报告提供专业的确认建议
        2. 对比分析AI诊断与医生诊断的差异
        3. 评估诊断的合理性和风险点
        
        重要原则：
        - 所有建议仅供医生参考，不能替代医生的专业判断
        - 使用专业、谨慎、客观的语言
        - 不虚构结论，基于提供的数据进行分析
        - 明确指出不确定性和需要进一步检查的情况
        - 优先关注患者安全和风险提示
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

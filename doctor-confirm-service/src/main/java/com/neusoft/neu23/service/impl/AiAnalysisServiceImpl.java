package com.neusoft.neu23.service.impl;

import com.assist.common.dto.response.AiAggregatedReport;
import com.assist.common.entity.DoctorFinalDiagnosis;
import com.neusoft.neu23.service.AiAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * AI分析服务实现
 * 提供特定场景的AI模型调用方法
 */
@Service
@Slf4j
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final ChatClient chatClient;

    @Autowired(required = false)
    public AiAnalysisServiceImpl(@Qualifier("chatClientQwen") @Nullable ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    // 医生确认建议的提示词
    private static final String CONFIRMATION_SUGGESTION_PROMPT = """
        你是一位资深的医疗AI助手。请基于以下助诊报告，为医生提供确认建议。
        
        报告内容：
        - 患者信息：{patientInfo}
        - 症状：{symptoms}
        - AI诊断：{diagnoses}
        - 风险评估：{riskAssessment}
        - 检查建议：{testSuggestions}
        - 就诊总结：{sessionSummary}
        - AI处方：{prescriptions}
        
        请从以下角度提供建议：
        1. 诊断的合理性分析
        2. 需要重点关注的风险点
        3. 处方用药的注意事项
        4. 建议的检查项目优先级
        5. 可能的鉴别诊断
        
        请用专业、简洁的语言输出，每条建议不超过100字。
        """;

    // 诊断对比分析的提示词
    private static final String COMPARISON_PROMPT = """
        你是一位医疗AI分析专家。请对比分析AI诊断和医生最终诊断的差异。
        
        AI诊断结果：
        {aiDiagnoses}
        
        医生最终诊断：
        {doctorDiagnosis}
        
        请分析：
        1. 诊断差异点
        2. 差异的可能原因
        3. 是否需要进一步检查
        4. 对患者的影响评估
        
        请用专业、客观的语言输出分析结果。
        """;

    // 诊断合理性评估的提示词
    private static final String REASONABLENESS_PROMPT = """
        你是一位医疗质量评估专家。请评估以下AI诊断的合理性。
        
        患者信息：{patientInfo}
        症状：{symptoms}
        AI诊断：{diagnoses}
        风险评估：{riskAssessment}
        
        请从以下维度评估：
        1. 诊断与症状的匹配度（1-10分）
        2. 诊断的临床合理性（1-10分）
        3. 风险评估的准确性（1-10分）
        4. 需要重点关注的风险点
        5. 建议医生复核的要点
        
        请用结构化格式输出评估结果。
        """;

    @Override
    public String generateConfirmationSuggestion(AiAggregatedReport report) {
        if (chatClient == null) {
            log.warn("ChatClient 未配置，AI分析服务不可用");
            return "AI分析服务未配置，请医生根据报告自行判断。";
        }
        
        try {
            log.info("开始生成医生确认建议，patientId={}, sessionId={}", 
                    report.getPatient() != null ? report.getPatient().getId() : null,
                    report.getSession() != null ? report.getSession().getSessionId() : null);

            String prompt = buildConfirmationPrompt(report);
            
            String suggestion = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("医生确认建议生成成功");
            return suggestion;
        } catch (Exception e) {
            log.error("生成医生确认建议失败", e);
            return "AI分析服务暂时不可用，请医生根据报告自行判断。";
        }
    }

    @Override
    public String compareDiagnosis(AiAggregatedReport report, DoctorFinalDiagnosis finalDiagnosis) {
        if (chatClient == null) {
            log.warn("ChatClient 未配置，AI分析服务不可用");
            return "AI分析服务未配置。";
        }
        
        try {
            log.info("开始对比分析诊断，sessionId={}", finalDiagnosis.getSessionId());

            String prompt = buildComparisonPrompt(report, finalDiagnosis);
            
            String comparison = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("诊断对比分析完成");
            return comparison;
        } catch (Exception e) {
            log.error("诊断对比分析失败", e);
            return "AI对比分析服务暂时不可用。";
        }
    }

    @Override
    public String evaluateDiagnosisReasonableness(AiAggregatedReport report) {
        if (chatClient == null) {
            log.warn("ChatClient 未配置，AI分析服务不可用");
            return "AI分析服务未配置。";
        }
        
        try {
            log.info("开始评估诊断合理性");

            String prompt = buildReasonablenessPrompt(report);
            
            String evaluation = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("诊断合理性评估完成");
            return evaluation;
        } catch (Exception e) {
            log.error("诊断合理性评估失败", e);
            return "AI评估服务暂时不可用。";
        }
    }

    /**
     * 构建确认建议的提示词
     */
    private String buildConfirmationPrompt(AiAggregatedReport report) {
        return CONFIRMATION_SUGGESTION_PROMPT
                .replace("{patientInfo}", formatPatientInfo(report))
                .replace("{symptoms}", formatSymptoms(report))
                .replace("{diagnoses}", formatDiagnoses(report))
                .replace("{riskAssessment}", formatRiskAssessment(report))
                .replace("{testSuggestions}", formatTestSuggestions(report))
                .replace("{sessionSummary}", formatSessionSummary(report))
                .replace("{prescriptions}", formatPrescriptions(report));
    }

    /**
     * 构建对比分析的提示词
     */
    private String buildComparisonPrompt(AiAggregatedReport report, DoctorFinalDiagnosis finalDiagnosis) {
        return COMPARISON_PROMPT
                .replace("{aiDiagnoses}", formatDiagnoses(report))
                .replace("{doctorDiagnosis}", finalDiagnosis.getFinalDiagnosis() != null 
                        ? finalDiagnosis.getFinalDiagnosis() : "未填写");
    }

    /**
     * 构建合理性评估的提示词
     */
    private String buildReasonablenessPrompt(AiAggregatedReport report) {
        return REASONABLENESS_PROMPT
                .replace("{patientInfo}", formatPatientInfo(report))
                .replace("{symptoms}", formatSymptoms(report))
                .replace("{diagnoses}", formatDiagnoses(report))
                .replace("{riskAssessment}", formatRiskAssessment(report));
    }

    // 格式化方法
    private String formatPatientInfo(AiAggregatedReport report) {
        if (report.getPatient() == null) return "无";
        return String.format("姓名：%s，年龄：%s，性别：%s", 
                report.getPatient().getName(),
                report.getPatient().getAge(),
                report.getPatient().getGender());
    }

    private String formatSymptoms(AiAggregatedReport report) {
        if (report.getSymptoms() == null || report.getSymptoms().isEmpty()) {
            return "无";
        }
        return report.getSymptoms().stream()
                .map(s -> s.toString())
                .reduce((a, b) -> a + "；" + b)
                .orElse("无");
    }

    private String formatDiagnoses(AiAggregatedReport report) {
        if (report.getDiagnoses() == null || report.getDiagnoses().isEmpty()) {
            return "无";
        }
        return report.getDiagnoses().stream()
                .map(d -> d.toString())
                .reduce((a, b) -> a + "；" + b)
                .orElse("无");
    }

    private String formatRiskAssessment(AiAggregatedReport report) {
        return report.getRiskAssessment() != null 
                ? report.getRiskAssessment().toString() 
                : "无";
    }

    private String formatTestSuggestions(AiAggregatedReport report) {
        if (report.getTestSuggestions() == null || report.getTestSuggestions().isEmpty()) {
            return "无";
        }
        return report.getTestSuggestions().stream()
                .map(t -> t.toString())
                .reduce((a, b) -> a + "；" + b)
                .orElse("无");
    }

    private String formatSessionSummary(AiAggregatedReport report) {
        return report.getSessionSummary() != null 
                ? report.getSessionSummary().toString() 
                : "无";
    }

    private String formatPrescriptions(AiAggregatedReport report) {
        if (report.getPrescriptions() == null || report.getPrescriptions().isEmpty()) {
            return "无";
        }
        return report.getPrescriptions().stream()
                .map(p -> p.toString())
                .reduce((a, b) -> a + "；" + b)
                .orElse("无");
    }
}


package com.neusoft.neu23.service.impl;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.response.SummaryWithDataResponse;
import com.assist.common.entity.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.neusoft.neu23.mapper.*;
import com.neusoft.neu23.service.SessionSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 会话总结服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionSummaryServiceImpl implements SessionSummaryService {

    private final AiSessionSummaryMapper summaryMapper;
    private final AiSymptomStructuredMapper symptomMapper;
    private final AiPreDiagnosisMapper diagnosisMapper;
    private final AiRiskAssessmentMapper riskAssessmentMapper;
    private final AiTestSuggestionMapper testSuggestionMapper;

    private ChatClient chatClient;

    @Autowired(required = false)
    public void setChatClient(@Qualifier("chatClientQwen") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 医疗总结生成提示词模板
     */
    private static final String SUMMARY_PROMPT_TEMPLATE = """
        请基于以下AI问诊数据，生成一份可直接给医生阅读的医疗总结。
        
        请按照以下医疗总结模板组织内容：
        
        【主诉】
        {chiefComplaint}
        
        【现病史】
        {presentIllness}
        
        【伴随症状】
        {accompanyingSymptoms}
        
        【可能诊断】
        {possibleDiagnosis}
        
        【风险评估】
        {riskAssessment}
        
        【检查建议】
        {testSuggestions}
        
        请确保：
        1. 使用专业、规范的医疗术语
        2. 内容准确、完整、条理清晰
        3. 突出关键信息和风险点
        4. 便于医生快速理解和决策
        """;

    @Override
    public ApiResponse<AiSessionSummary> generateSummary(Integer patientId, Integer sessionId) {
        if (patientId == null || sessionId == null) {
            return ApiResponse.error("患者ID和会话ID不能为空");
        }

        try {
            log.info("开始生成会话总结，patientId={}, sessionId={}", patientId, sessionId);

            // 1. 从数据库读取所有AI输出数据
            List<AiSymptomStructured> symptoms = safeList(symptomMapper.selectBySessionId(sessionId));
            log.debug("从数据库读取到 {} 条结构化症状", symptoms.size());
            
            List<AiPreDiagnosis> diagnoses = safeList(diagnosisMapper.selectBySessionId(sessionId));
            log.debug("从数据库读取到 {} 条初步诊断", diagnoses.size());
            
            AiRiskAssessment riskAssessment = riskAssessmentMapper.selectBySessionId(sessionId);
            log.debug("从数据库读取风险评估: {}", riskAssessment != null ? "有数据" : "无数据");
            
            List<AiTestSuggestion> testSuggestions = safeList(testSuggestionMapper.selectBySessionId(sessionId));
            log.debug("从数据库读取到 {} 条检查建议", testSuggestions.size());

            // 2. 构建提示词
            String prompt = buildSummaryPrompt(symptoms, diagnoses, riskAssessment, testSuggestions);

            // 3. 调用AI生成总结
            String summaryText;
            if (chatClient == null) {
                log.warn("ChatClient 未配置，使用默认总结模板");
                // 如果 AI 服务不可用，生成一个基于现有数据的简单总结
                summaryText = generateDefaultSummary(symptoms, diagnoses, riskAssessment, testSuggestions);
            } else {
                try {
                    summaryText = chatClient.prompt()
                            .user(prompt)
                            .call()
                            .content();
                } catch (Exception e) {
                    log.error("AI生成总结失败，使用默认模板", e);
                    summaryText = generateDefaultSummary(symptoms, diagnoses, riskAssessment, testSuggestions);
                }
            }

            // 4. 可选：生成推理链（简化处理，可以留空或生成简要说明）
            String reasoningChain = generateReasoningChain(symptoms, diagnoses, riskAssessment);

            // 5. 保存到数据库（如果数据库可用）
            AiSessionSummary summary = new AiSessionSummary();
            summary.setPatientId(patientId);
            summary.setSessionId(sessionId);
            summary.setSummaryText(summaryText);
            summary.setReasoningChain(reasoningChain);
            summary.setCreatedTime(new Date());

            try {
                // 检查是否已存在，如果存在则更新，否则插入
                AiSessionSummary existing = summaryMapper.selectOne(
                    Wrappers.lambdaQuery(AiSessionSummary.class)
                        .eq(AiSessionSummary::getPatientId, patientId)
                        .eq(AiSessionSummary::getSessionId, sessionId)
                );

                if (existing != null) {
                    summary.setSummaryId(existing.getSummaryId());
                    summaryMapper.updateById(summary);
                    log.info("更新已存在的总结记录，summaryId={}", existing.getSummaryId());
                } else {
                    summaryMapper.insert(summary);
                    log.info("插入新的总结记录，summaryId={}", summary.getSummaryId());
                }
            } catch (Exception e) {
                log.warn("保存总结到数据库失败，但继续返回结果: {}", e.getMessage());
                // 数据库不可用时，仍然返回生成的总结，但不保存
            }

            log.info("会话总结生成成功");
            return ApiResponse.success(summary);

        } catch (Exception e) {
            log.error("生成会话总结失败，patientId={}, sessionId={}", patientId, sessionId, e);
            return ApiResponse.error("生成总结失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<SummaryWithDataResponse> generateSummaryWithData(Integer patientId, Integer sessionId) {
        if (patientId == null || sessionId == null) {
            return ApiResponse.error("患者ID和会话ID不能为空");
        }

        try {
            log.info("开始生成会话总结（含原始数据），patientId={}, sessionId={}", patientId, sessionId);

            // 1. 从数据库读取所有AI输出数据
            List<AiSymptomStructured> symptoms = safeList(symptomMapper.selectBySessionId(sessionId));
            log.debug("从数据库读取到 {} 条结构化症状", symptoms.size());
            
            List<AiPreDiagnosis> diagnoses = safeList(diagnosisMapper.selectBySessionId(sessionId));
            log.debug("从数据库读取到 {} 条初步诊断", diagnoses.size());
            
            AiRiskAssessment riskAssessment = riskAssessmentMapper.selectBySessionId(sessionId);
            log.debug("从数据库读取风险评估: {}", riskAssessment != null ? "有数据" : "无数据");
            
            List<AiTestSuggestion> testSuggestions = safeList(testSuggestionMapper.selectBySessionId(sessionId));
            log.debug("从数据库读取到 {} 条检查建议", testSuggestions.size());

            // 2. 构建提示词并生成总结
            String prompt = buildSummaryPrompt(symptoms, diagnoses, riskAssessment, testSuggestions);
            String summaryText;
            if (chatClient == null) {
                log.warn("ChatClient 未配置，使用默认总结模板");
                summaryText = generateDefaultSummary(symptoms, diagnoses, riskAssessment, testSuggestions);
            } else {
                try {
                    summaryText = chatClient.prompt()
                            .user(prompt)
                            .call()
                            .content();
                } catch (Exception e) {
                    log.error("AI生成总结失败，使用默认模板", e);
                    summaryText = generateDefaultSummary(symptoms, diagnoses, riskAssessment, testSuggestions);
                }
            }

            // 3. 生成推理链
            String reasoningChain = generateReasoningChain(symptoms, diagnoses, riskAssessment);

            // 4. 构建总结对象
            AiSessionSummary sessionSummary = new AiSessionSummary();
            sessionSummary.setPatientId(patientId);
            sessionSummary.setSessionId(sessionId);
            sessionSummary.setSummaryText(summaryText);
            sessionSummary.setReasoningChain(reasoningChain);
            sessionSummary.setCreatedTime(new Date());

            // 5. 保存到数据库（可选）
            try {
                AiSessionSummary existing = summaryMapper.selectOne(
                    Wrappers.lambdaQuery(AiSessionSummary.class)
                        .eq(AiSessionSummary::getPatientId, patientId)
                        .eq(AiSessionSummary::getSessionId, sessionId)
                );
                if (existing != null) {
                    sessionSummary.setSummaryId(existing.getSummaryId());
                    summaryMapper.updateById(sessionSummary);
                } else {
                    summaryMapper.insert(sessionSummary);
                }
            } catch (Exception e) {
                log.warn("保存总结到数据库失败，但继续返回结果: {}", e.getMessage());
            }

            // 6. 构建扩展响应
            SummaryWithDataResponse response = new SummaryWithDataResponse();
            response.setSymptoms(symptoms);
            response.setDiagnoses(diagnoses);
            response.setRiskAssessment(riskAssessment);
            response.setTestSuggestions(testSuggestions);
            response.setSessionSummary(sessionSummary);

            log.info("会话总结（含原始数据）生成成功");
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("生成会话总结（含原始数据）失败，patientId={}, sessionId={}", patientId, sessionId, e);
            return ApiResponse.error("生成总结失败: " + e.getMessage());
        }
    }

    /**
     * 构建总结提示词
     */
    private String buildSummaryPrompt(List<AiSymptomStructured> symptoms,
                                     List<AiPreDiagnosis> diagnoses,
                                     AiRiskAssessment riskAssessment,
                                     List<AiTestSuggestion> testSuggestions) {
        
        // 主诉（从症状中提取主要症状）
        String chiefComplaint = formatChiefComplaint(symptoms);

        // 现病史（从症状中提取详细信息）
        String presentIllness = formatPresentIllness(symptoms);

        // 伴随症状
        String accompanyingSymptoms = formatAccompanyingSymptoms(symptoms);

        // 可能诊断
        String possibleDiagnosis = formatDiagnoses(diagnoses);

        // 风险评估
        String riskAssessmentText = formatRiskAssessment(riskAssessment);

        // 检查建议
        String testSuggestionsText = formatTestSuggestions(testSuggestions);

        return SUMMARY_PROMPT_TEMPLATE
                .replace("{basicInfo}", "（基本信息需从其他服务获取）")
                .replace("{chiefComplaint}", chiefComplaint)
                .replace("{presentIllness}", presentIllness)
                .replace("{accompanyingSymptoms}", accompanyingSymptoms)
                .replace("{possibleDiagnosis}", possibleDiagnosis)
                .replace("{riskAssessment}", riskAssessmentText)
                .replace("{testSuggestions}", testSuggestionsText)
                .replace("{treatmentSuggestions}", "（治疗建议需从其他服务获取）");
    }

    private String formatChiefComplaint(List<AiSymptomStructured> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) {
            return "无明确主诉";
        }
        // 取第一个症状作为主诉
        AiSymptomStructured mainSymptom = symptoms.get(0);
        return String.format("%s，严重程度：%s，持续时间：%s",
            mainSymptom.getSymptomName(),
            mainSymptom.getSeverity() != null ? mainSymptom.getSeverity() : "未知",
            mainSymptom.getDuration() != null ? mainSymptom.getDuration() : "未知");
    }

    private String formatPresentIllness(List<AiSymptomStructured> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) {
            return "无现病史信息";
        }
        StringBuilder sb = new StringBuilder();
        for (AiSymptomStructured symptom : symptoms) {
            sb.append(symptom.getSymptomName());
            if (symptom.getDuration() != null) {
                sb.append("，持续").append(symptom.getDuration());
            }
            if (symptom.getSeverity() != null) {
                sb.append("，严重程度：").append(symptom.getSeverity());
            }
            if (symptom.getExtraInfo() != null && !symptom.getExtraInfo().isEmpty()) {
                sb.append("，").append(symptom.getExtraInfo());
            }
            sb.append("；");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "无现病史信息";
    }

    private String formatAccompanyingSymptoms(List<AiSymptomStructured> symptoms) {
        if (symptoms == null || symptoms.size() <= 1) {
            return "无伴随症状";
        }
        // 除第一个症状外的其他症状
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < symptoms.size(); i++) {
            AiSymptomStructured symptom = symptoms.get(i);
            sb.append(symptom.getSymptomName());
            if (symptom.getExtraInfo() != null && !symptom.getExtraInfo().isEmpty()) {
                sb.append("（").append(symptom.getExtraInfo()).append("）");
            }
            sb.append("；");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "无伴随症状";
    }

    private String formatDiagnoses(List<AiPreDiagnosis> diagnoses) {
        if (diagnoses == null || diagnoses.isEmpty()) {
            return "暂无诊断";
        }
        StringBuilder sb = new StringBuilder();
        for (AiPreDiagnosis diagnosis : diagnoses) {
            sb.append(diagnosis.getDiagnosis());
            if (diagnosis.getProbability() != null) {
                sb.append("（可能性：").append(diagnosis.getProbability().multiply(java.math.BigDecimal.valueOf(100)))
                  .append("%）");
            }
            if (diagnosis.getReasoning() != null && !diagnosis.getReasoning().isEmpty()) {
                sb.append("，依据：").append(diagnosis.getReasoning());
            }
            sb.append("；");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "暂无诊断";
    }

    private String formatRiskAssessment(AiRiskAssessment riskAssessment) {
        if (riskAssessment == null) {
            return "暂无风险评估";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("风险等级：").append(riskAssessment.getRiskLevel() != null ? riskAssessment.getRiskLevel() : "未知");
        if (riskAssessment.getReason() != null && !riskAssessment.getReason().isEmpty()) {
            sb.append("；原因：").append(riskAssessment.getReason());
        }
        return sb.toString();
    }

    private String formatTestSuggestions(List<AiTestSuggestion> testSuggestions) {
        if (testSuggestions == null || testSuggestions.isEmpty()) {
            return "暂无检查建议";
        }
        StringBuilder sb = new StringBuilder();
        for (AiTestSuggestion test : testSuggestions) {
            sb.append(test.getTestName());
            if (test.getReason() != null && !test.getReason().isEmpty()) {
                sb.append("（").append(test.getReason()).append("）");
            }
            sb.append("；");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "暂无检查建议";
    }


    /**
     * 生成推理链（可选）
     */
    private String generateReasoningChain(List<AiSymptomStructured> symptoms,
                                         List<AiPreDiagnosis> diagnoses,
                                         AiRiskAssessment riskAssessment) {
        StringBuilder chain = new StringBuilder();
        chain.append("基于以下信息生成总结：");
        if (symptoms != null && !symptoms.isEmpty()) {
            chain.append("症状数据（").append(symptoms.size()).append("项）；");
        }
        if (diagnoses != null && !diagnoses.isEmpty()) {
            chain.append("诊断数据（").append(diagnoses.size()).append("项）；");
        }
        if (riskAssessment != null) {
            chain.append("风险评估数据；");
        }
        return chain.toString();
    }

    /**
     * 安全处理列表
     */
    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : java.util.Collections.emptyList();
    }

    /**
     * 生成默认总结（当AI服务不可用时使用）
     */
    private String generateDefaultSummary(List<AiSymptomStructured> symptoms,
                                         List<AiPreDiagnosis> diagnoses,
                                         AiRiskAssessment riskAssessment,
                                         List<AiTestSuggestion> testSuggestions) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("【主诉】\n");
        summary.append(formatChiefComplaint(symptoms));
        summary.append("\n\n");

        summary.append("【现病史】\n");
        summary.append(formatPresentIllness(symptoms));
        summary.append("\n\n");

        summary.append("【伴随症状】\n");
        summary.append(formatAccompanyingSymptoms(symptoms));
        summary.append("\n\n");

        summary.append("【可能诊断】\n");
        summary.append(formatDiagnoses(diagnoses));
        summary.append("\n\n");

        summary.append("【风险评估】\n");
        summary.append(formatRiskAssessment(riskAssessment));
        summary.append("\n\n");

        summary.append("【检查建议】\n");
        summary.append(formatTestSuggestions(testSuggestions));

        return summary.toString();
    }
}


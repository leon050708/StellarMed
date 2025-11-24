package com.neusoft.neu23.service.impl;

import com.assist.common.common.BusinessException;
import com.assist.common.common.ErrorCode;
import com.assist.common.dto.request.PrescriptionGenerateRequest;
import com.assist.common.dto.response.PrescriptionGenerateResponse;
import com.assist.common.entity.AiPreDiagnosis;
import com.assist.common.entity.AiPrescription;
import com.assist.common.entity.AiRiskAssessment;
import com.assist.common.entity.AiSymptomStructured;
import com.assist.common.entity.AiTestSuggestion;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neusoft.neu23.mapper.AiPreDiagnosisMapper;
import com.neusoft.neu23.mapper.AiPrescriptionMapper;
import com.neusoft.neu23.mapper.AiRiskAssessmentMapper;
import com.neusoft.neu23.mapper.AiSymptomStructuredMapper;
import com.neusoft.neu23.mapper.AiTestSuggestionMapper;
import com.neusoft.neu23.service.IPrescriptionService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 处方生成服务实现类：
 * - 直接从本地数据库查询结构化症状 / 初步诊断 / 风险评估 / 检查建议
 * - 构建 Prompt 调用大模型生成处方 JSON
 * - 解析并写入 ai_prescription 表
 */
@Slf4j
@Service
public class PrescriptionServiceImpl
        extends ServiceImpl<AiPrescriptionMapper, AiPrescription>
        implements IPrescriptionService {

    @Autowired
    private AiSymptomStructuredMapper aiSymptomStructuredMapper;

    @Autowired
    private AiPreDiagnosisMapper aiPreDiagnosisMapper;

    @Autowired
    private AiRiskAssessmentMapper aiRiskAssessmentMapper;

    @Autowired
    private AiTestSuggestionMapper aiTestSuggestionMapper;

    @Autowired(required = false)
    private ChatModel chatModel;

    /**
     * 生成处方建议：从本地数据库查询上下文数据 -> 构建 Prompt -> 调用大模型 -> 解析结果 -> 入库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionGenerateResponse generatePrescription(PrescriptionGenerateRequest request) {
        Integer patientId = request.getPatientId();
        Integer sessionId = request.getSessionId();

        log.info("开始生成处方建议，patientId: {}, sessionId: {}", patientId, sessionId);

        // 1. 从本地数据库查询上下文数据
        log.info("从数据库查询症状/诊断/风险/检查建议数据...");

        // 结构化症状列表
        LambdaQueryWrapper<AiSymptomStructured> symptomWrapper = new LambdaQueryWrapper<>();
        symptomWrapper.eq(AiSymptomStructured::getPatientId, patientId)
                .eq(AiSymptomStructured::getSessionId, sessionId)
                .orderByDesc(AiSymptomStructured::getCreateTime);
        List<AiSymptomStructured> symptoms = aiSymptomStructuredMapper.selectList(symptomWrapper);

        // 初步诊断列表
        LambdaQueryWrapper<AiPreDiagnosis> diagnosisWrapper = new LambdaQueryWrapper<>();
        diagnosisWrapper.eq(AiPreDiagnosis::getPatientId, patientId)
                .eq(AiPreDiagnosis::getSessionId, sessionId)
                .orderByDesc(AiPreDiagnosis::getCreateTime);
        List<AiPreDiagnosis> diagnoses = aiPreDiagnosisMapper.selectList(diagnosisWrapper);

        // 最新一条风险评估（可选）
        LambdaQueryWrapper<AiRiskAssessment> riskWrapper = new LambdaQueryWrapper<>();
        riskWrapper.eq(AiRiskAssessment::getPatientId, patientId)
                .eq(AiRiskAssessment::getSessionId, sessionId)
                .orderByDesc(AiRiskAssessment::getCreatedTime)
                .last("LIMIT 1");
        AiRiskAssessment riskAssessment = aiRiskAssessmentMapper.selectOne(riskWrapper);

        // 检查建议列表（可选）
        LambdaQueryWrapper<AiTestSuggestion> testWrapper = new LambdaQueryWrapper<>();
        testWrapper.eq(AiTestSuggestion::getPatientId, patientId)
                .eq(AiTestSuggestion::getSessionId, sessionId)
                .orderByDesc(AiTestSuggestion::getCreatedTime);
        List<AiTestSuggestion> testSuggestions = aiTestSuggestionMapper.selectList(testWrapper);

        log.info("数据库查询完成: 症状{}条, 诊断{}条, 风险评估{}, 检查建议{}条",
                symptoms != null ? symptoms.size() : 0,
                diagnoses != null ? diagnoses.size() : 0,
                riskAssessment != null ? "存在" : "不存在",
                testSuggestions != null ? testSuggestions.size() : 0);

        // 2. 数据完整性校验（症状和诊断是必需的）
        if (symptoms == null || symptoms.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "未找到结构化症状数据");
        }
        if (diagnoses == null || diagnoses.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "未找到初步诊断数据");
        }

        // 3. 构建 Prompt
        String prompt = buildPrescriptionPrompt(symptoms, diagnoses, riskAssessment, testSuggestions);
        log.debug("生成的 Prompt: {}", prompt);

        try {
            if (chatModel == null) {
                throw new BusinessException(ErrorCode.AI_ERROR, "ChatModel未配置，请检查SpringAI配置");
            }

            // 调用大模型
            ChatClient chatClient = ChatClient.builder(chatModel).build();
            String aiResponse = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("AI 原始返回: {}", aiResponse);

            // 解析 JSON 为处方条目列表
            List<PrescriptionItem> items = parsePrescriptionItems(aiResponse);
            log.info("AI 解析后的处方条数: {}", items.size());

            // 4. 删除旧处方（如果存在）
            LambdaQueryWrapper<AiPrescription> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(AiPrescription::getPatientId, patientId)
                    .eq(AiPrescription::getSessionId, sessionId);
            this.remove(deleteWrapper);

            // 5. 保存新处方到数据库
            List<AiPrescription> prescriptions = new ArrayList<>();
            for (PrescriptionItem item : items) {
                AiPrescription prescription = new AiPrescription();
                prescription.setPatientId(patientId);
                prescription.setSessionId(sessionId);
                prescription.setDrugName(item.getDrugName());
                prescription.setDosage(item.getDosage());
                prescription.setDuration(item.getDuration());
                prescription.setUsageInstruction(item.getUsageInstruction() != null ? item.getUsageInstruction() : "");
                prescription.setReason(item.getReason() != null ? item.getReason() : "");
                prescription.setCreateTime(new Date());
                prescriptions.add(prescription);
            }

            boolean saved = this.saveBatch(prescriptions);
            if (!saved) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "保存处方数据失败");
            }

            log.info("成功生成 {} 条处方建议", prescriptions.size());

            PrescriptionGenerateResponse response = new PrescriptionGenerateResponse();
            response.setPrescriptions(prescriptions);
            response.setMessage("处方建议生成成功");
            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成处方建议失败", e);
            throw new BusinessException(ErrorCode.AI_ERROR, "AI生成处方建议失败: " + e.getMessage());
        }
    }

    /**
     * 构建处方生成 Prompt，将结构化症状 / 诊断 / 风险 / 检查建议整理成文本，并约束输出为指定 JSON 格式。
     */
    private String buildPrescriptionPrompt(List<AiSymptomStructured> symptoms,
                                           List<AiPreDiagnosis> diagnoses,
                                           AiRiskAssessment riskAssessment,
                                           List<AiTestSuggestion> testSuggestions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的临床药师，请根据以下患者信息，生成合理的处方建议。\n\n");

        // 结构化症状
        prompt.append("【结构化症状】\n");
        for (AiSymptomStructured symptom : symptoms) {
            prompt.append(String.format("- %s：严重程度：%s，持续时间：%s",
                    symptom.getSymptomName(),
                    symptom.getSeverity(),
                    symptom.getDuration()));
            if (symptom.getExtraInfo() != null && !symptom.getExtraInfo().isEmpty()) {
                prompt.append("，补充信息：").append(symptom.getExtraInfo());
            }
            prompt.append("\n");
        }

        // 初步诊断
        prompt.append("\n【初步诊断】\n");
        for (AiPreDiagnosis diagnosis : diagnoses) {
            BigDecimal probabilityPercent = null;
            if (diagnosis.getProbability() != null) {
                probabilityPercent = diagnosis.getProbability().multiply(BigDecimal.valueOf(100));
            }
            String probabilityText = probabilityPercent != null ? probabilityPercent.toPlainString() : "未知";
            String reasonText = diagnosis.getReasoning() != null ? diagnosis.getReasoning() : "无";

            prompt.append(String.format("- %s（概率：%s%%，理由：%s）\n",
                    diagnosis.getDiagnosis(),
                    probabilityText,
                    reasonText));
        }

        // 风险等级（可选）
        if (riskAssessment != null) {
            prompt.append("\n【风险等级】\n");
            prompt.append(String.format("- 风险等级：%s\n", riskAssessment.getRiskLevel()));
            if (riskAssessment.getReason() != null && !riskAssessment.getReason().isEmpty()) {
                prompt.append(String.format("- 风险说明：%s\n", riskAssessment.getReason()));
            }
        }

        // 检查建议（可选）
        if (testSuggestions != null && !testSuggestions.isEmpty()) {
            prompt.append("\n【检查建议】\n");
            for (AiTestSuggestion test : testSuggestions) {
                prompt.append(String.format("- %s", test.getTestName()));
                if (test.getReason() != null && !test.getReason().isEmpty()) {
                    prompt.append(String.format("（原因：%s）", test.getReason()));
                }
                prompt.append("\n");
            }
        }

        // 用药要求
        prompt.append("\n【要求】\n");
        prompt.append("请根据以上信息，生成推荐用药方案。要求：\n");
        prompt.append("1. 药品选择要针对主要诊断和症状；\n");
        prompt.append("2. 考虑风险等级，高风险患者用药需谨慎；\n");
        prompt.append("3. 剂量要合理，符合临床常规；\n");
        prompt.append("4. 疗程要适当，不能过长或过短；\n");
        prompt.append("5. 用法说明要清晰明确；\n");
        prompt.append("6. 每种药品都要提供推荐理由。\n\n");

        // 输出格式约束（JSON）
        prompt.append("【输出格式】\n");
        prompt.append("请严格按照以下JSON数组格式输出，不要包含任何其他文字说明：\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"drug_name\": \"药品名称\",\n");
        prompt.append("    \"dosage\": \"剂量（如75mg）\",\n");
        prompt.append("    \"duration\": \"疗程（如5天）\",\n");
        prompt.append("    \"usage_instruction\": \"用法说明（如口服，每日两次）\",\n");
        prompt.append("    \"reason\": \"推荐理由\"\n");
        prompt.append("  }\n");
        prompt.append("]\n\n");
        prompt.append("请直接输出JSON数组，不要添加任何前缀或后缀文字。");

        return prompt.toString();
    }

    /**
     * 解析大模型返回的 JSON 文本为处方条目列表
     */
    private List<PrescriptionItem> parsePrescriptionItems(String aiResponse) {
        if (aiResponse == null) {
            throw new BusinessException(ErrorCode.AI_ERROR, "AI返回结果为空");
        }

        try {
            String jsonStr = aiResponse.trim();

            // 去掉可能存在的 Markdown 代码块包装
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            } else if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.type.CollectionType type =
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PrescriptionItem.class);

            List<PrescriptionItem> items = objectMapper.readValue(jsonStr, type);
            if (items == null || items.isEmpty()) {
                throw new BusinessException(ErrorCode.AI_ERROR, "AI返回的处方数据为空");
            }
            return items;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析AI返回的处方JSON失败: {}", aiResponse, e);
            throw new BusinessException(ErrorCode.AI_ERROR, "解析AI返回的处方数据失败: " + e.getMessage());
        }
    }

    /**
     * Spring AI 结构化输出映射对象，对应大模型返回的单条处方 JSON。
     */
    @Data
    private static class PrescriptionItem {
        @JsonProperty("drug_name")
        private String drugName;
        @JsonProperty("dosage")
        private String dosage;
        @JsonProperty("duration")
        private String duration;
        @JsonProperty("usage_instruction")
        private String usageInstruction;
        @JsonProperty("reason")
        private String reason;
    }

    /**
     * 根据患者与会话查询处方列表
     */
    @Override
    public PrescriptionGenerateResponse getPrescriptionBySession(Integer patientId, Integer sessionId) {
        LambdaQueryWrapper<AiPrescription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiPrescription::getPatientId, patientId)
                .eq(AiPrescription::getSessionId, sessionId)
                .orderByDesc(AiPrescription::getCreateTime);
        List<AiPrescription> prescriptions = this.list(queryWrapper);

        PrescriptionGenerateResponse response = new PrescriptionGenerateResponse();
        response.setPrescriptions(prescriptions);
        if (prescriptions == null || prescriptions.isEmpty()) {
            response.setMessage("未找到处方建议");
        } else {
            response.setMessage("查询成功");
        }

        return response;
    }
}


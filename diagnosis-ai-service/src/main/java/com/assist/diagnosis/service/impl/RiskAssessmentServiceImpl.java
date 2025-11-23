package com.assist.diagnosis.service.impl;

import com.assist.common.common.BusinessException;
import com.assist.common.common.ErrorCode;
import com.assist.common.dto.request.RiskEvaluateRequest;
import com.assist.common.dto.response.RiskEvaluateResponse;
import com.assist.common.entity.AiPreDiagnosis;
import com.assist.common.entity.AiRiskAssessment;
import com.assist.common.entity.AiSymptomStructured;
import com.assist.diagnosis.mapper.AiPreDiagnosisMapper;
import com.assist.diagnosis.mapper.AiRiskAssessmentMapper;
import com.assist.diagnosis.mapper.AiSymptomStructuredMapper;
import com.assist.diagnosis.service.RiskAssessmentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    @Resource
    private AiSymptomStructuredMapper aiSymptomStructuredMapper;
    @Resource
    private AiPreDiagnosisMapper aiPreDiagnosisMapper;
    @Resource
    private AiRiskAssessmentMapper aiRiskAssessmentMapper;
    @Resource
    private ChatClient chatClient;

    // 用来解析 AI 返回的 JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RiskEvaluateResponse evaluateRisk(RiskEvaluateRequest request) {

        Integer patientId = request.getPatientId();
        Integer sessionId = request.getSessionId();
        if (patientId == null || sessionId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "patientId 或 sessionId 不能为空");
        }

        // 1. 查询结构化症状
        QueryWrapper<AiSymptomStructured> symptomWrapper = new QueryWrapper<>();
        symptomWrapper.eq("patient_id", patientId)
                .eq("session_id", sessionId);
        List<AiSymptomStructured> symptomList = aiSymptomStructuredMapper.selectList(symptomWrapper);
        if (symptomList == null || symptomList.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到该会话的结构化症状数据");
        }

        // 2. 查询已有的 AI 初步诊断（可选）
        QueryWrapper<AiPreDiagnosis> diagnosisWrapper = new QueryWrapper<>();
        diagnosisWrapper.eq("patient_id", patientId)
                .eq("session_id", sessionId);
        List<AiPreDiagnosis> diagnosisList = aiPreDiagnosisMapper.selectList(diagnosisWrapper);

        // 3. 构造 Prompt
        String prompt = buildRiskPrompt(symptomList, diagnosisList);

        // 4. 调用通义千问（写法和 DiagnosisServiceImpl 一样）
        String aiText = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
        System.out.println("AI 风险评估返回内容：" + aiText);

        // 5. 解析 AI 返回文本为实体
        AiRiskAssessment risk = parseRiskFromAi(aiText, patientId, sessionId);

        // 6. 写入 ai_risk_assessment 表
        aiRiskAssessmentMapper.insert(risk);

        // 7. 组装返回值（注意：RiskEvaluateResponse 只有 riskAssessment + message 两个字段）
        RiskEvaluateResponse resp = new RiskEvaluateResponse();
        resp.setRiskAssessment(risk);
        resp.setMessage("AI 风险评估完成");
        return resp;
    }

    /**
     * 构造风险评估 Prompt：结构化症状 + 初步诊断
     */
    private String buildRiskPrompt(List<AiSymptomStructured> symptoms,
                                   List<AiPreDiagnosis> diagnoses) {

        StringBuilder sb = new StringBuilder();
        sb.append("你是一名资深临床医生，请根据下面的结构化症状和 AI 初步诊断结果，给出一次整体的风险评估。\n")
                .append("只需要判断当前病例的整体风险等级，并给出简要原因。\n\n");

        sb.append("【结构化症状】:\n");
        for (AiSymptomStructured s : symptoms) {
            sb.append("- 症状名称：").append(s.getSymptomName()).append("\n")
                    .append("  严重程度：").append(s.getSeverity()).append("\n")
                    .append("  持续时间：").append(s.getDuration()).append("\n");
            if (s.getExtraInfo() != null) {
                sb.append("  其他信息：").append(s.getExtraInfo()).append("\n");
            }
            sb.append("\n");
        }

        if (diagnoses != null && !diagnoses.isEmpty()) {
            sb.append("【AI 初步诊断结果】:\n");
            for (AiPreDiagnosis d : diagnoses) {
                sb.append("- 疾病：").append(d.getDiagnosis())
                        .append("，概率：").append(d.getProbability())
                        .append("，理由：").append(d.getReasoning())
                        .append("\n");
            }
            sb.append("\n");
        }

        sb.append("请你综合以上信息，从 LOW / MEDIUM / HIGH / CRITICAL 中选择一个最合适的风险等级。")
                .append("注意：必须严格按照下面的 JSON 格式返回，不要多余文字：\n")
                .append("{\n")
                .append("  \"risk_level\": \"LOW 或 MEDIUM 或 HIGH 或 CRITICAL\",\n")
                .append("  \"reason\": \"给出不超过 100 字的风险判断理由\"\n")
                .append("}\n");

        return sb.toString();
    }

    /**
     * 解析 AI 返回的 JSON 文本，组装成 AiRiskAssessment 实体
     */
    private AiRiskAssessment parseRiskFromAi(String aiText,
                                             Integer patientId,
                                             Integer sessionId) {
        try {
            // 防止模型前后加解释文字，只取第一个 { 到最后一个 } 之间的内容
            int start = aiText.indexOf('{');
            int end = aiText.lastIndexOf('}');
            String json = (start >= 0 && end > start) ? aiText.substring(start, end + 1) : aiText;

            JsonNode root = objectMapper.readTree(json);
            String level = root.path("risk_level").asText(null);
            String reason = root.path("reason").asText(null);

            if (level == null || level.isEmpty()) {
                throw new BusinessException(ErrorCode.AI_ERROR, "AI 未返回风险等级");
            }

            AiRiskAssessment entity = new AiRiskAssessment();
            entity.setPatientId(patientId);
            entity.setSessionId(sessionId);
            // 统一转成大写，方便和 RiskLevelEnum 对齐：LOW / MEDIUM / HIGH / CRITICAL
            entity.setRiskLevel(level.trim().toUpperCase());
            entity.setReason(reason);
            // createdTime 字段让数据库自己填（或为空）即可

            return entity;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_ERROR, "解析 AI 风险评估结果失败");
        }
    }
}
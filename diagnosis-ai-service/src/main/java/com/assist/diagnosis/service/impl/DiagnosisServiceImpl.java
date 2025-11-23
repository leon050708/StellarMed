package com.assist.diagnosis.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import com.assist.common.common.ErrorCode;
import com.assist.common.common.ApiResponse;
import com.assist.common.common.BusinessException;
import com.assist.common.dto.request.DiagnosisEvaluateRequest;
import com.assist.common.dto.response.DiagnosisEvaluateResponse;
import com.assist.common.entity.AiPreDiagnosis;
import com.assist.common.entity.AiSymptomStructured;
import com.assist.diagnosis.mapper.AiPreDiagnosisMapper;
import com.assist.diagnosis.mapper.AiSymptomStructuredMapper;
import com.assist.diagnosis.service.DiagnosisService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosisServiceImpl implements DiagnosisService {

    private final AiPreDiagnosisMapper aiPreDiagnosisMapper;
    private final AiSymptomStructuredMapper aiSymptomStructuredMapper;
    private final ChatClient chatClient;

    // TODO: 注入 Spring AI 的 ChatClient / ChatModel（在 SpringAiConfig 里配置好后）
    // private final ChatClient chatClient;

    @Override
    public DiagnosisEvaluateResponse evaluateDiagnosis(DiagnosisEvaluateRequest request) {
        Integer patientId = request.getPatientId();
        Integer sessionId = request.getSessionId();

        if (patientId == null || sessionId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "patientId/sessionId 不能为空");
        }

        // 1. 读取该 session 的结构化症状
        //    SELECT * FROM ai_symptom_structured WHERE patient_id=? AND session_id=?;
        QueryWrapper<AiSymptomStructured> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", patientId);
        wrapper.eq("session_id", sessionId);

        List<AiSymptomStructured> symptomList = aiSymptomStructuredMapper.selectList(wrapper);

        if (symptomList == null || symptomList.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到该会话的结构化症状数据");
        }

        // 2. 构造 Prompt
        String prompt = buildDiagnosisPrompt(symptomList);

        // 3. 调用通义千问模型
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        System.out.println("AI 返回内容：" + aiResponse);

        // 4. 解析 JSON，生成诊断列表
        List<AiPreDiagnosis> aiResultList = parseDiagnosisFromJson(aiResponse, patientId, sessionId);

        // 5. 将结果写入 ai_pre_diagnosis 表
        for (AiPreDiagnosis item : aiResultList) {
            aiPreDiagnosisMapper.insert(item);
        }

        // 6. 组装响应
        DiagnosisEvaluateResponse resp = new DiagnosisEvaluateResponse();
        resp.setDiagnoses(aiResultList);
        resp.setMessage("AI 初步诊断完成");
        return resp;
    }

    // private String buildDiagnosisPrompt(List<AiSymptomStructured> symptoms) { ... }
    // private List<AiPreDiagnosis> parseDiagnosisFromAi(ChatResponse response) { ... }
    /**
     * 构造通义千问的 Prompt，用于生成初步诊断结果
     */
    private String buildDiagnosisPrompt(List<AiSymptomStructured> symptoms) {

        StringBuilder sb = new StringBuilder();
        sb.append("你是一名专业的临床医生，请根据给定的结构化症状信息，生成初步诊断列表。")
                .append("输出格式必须是 JSON，不要包含任何额外解释，仅输出 JSON。\n\n");

        sb.append("【患者症状】：\n");

        for (AiSymptomStructured s : symptoms) {
            sb.append("- 症状名称：").append(s.getSymptomName()).append("\n");
            sb.append("  严重程度：").append(s.getSeverity()).append("\n");
            sb.append("  持续时间：").append(s.getDuration()).append("\n");
            sb.append("  其他补充信息：").append(s.getExtraInfo()).append("\n");
        }

        sb.append("\n请严格按照以下 JSON 格式返回：\n")
                .append("{\n")
                .append("  \"diagnosis\": [\n")
                .append("    {\n")
                .append("      \"diseaseName\": \"疾病名称\",\n")
                .append("      \"probability\": 0.85,\n")
                .append("      \"reason\": \"结合症状的理由\"\n")
                .append("    }\n")
                .append("  ]\n")
                .append("}");

        return sb.toString();
    }
    /**
     * 解析通义千问返回的 JSON，转换为 AiPreDiagnosis 列表
     */
    private List<AiPreDiagnosis> parseDiagnosisFromJson(String aiResponse,
                                                        Integer patientId,
                                                        Integer sessionId) {
        List<AiPreDiagnosis> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(aiResponse);
            JsonNode diagnosisArray = root.get("diagnosis");
            if (diagnosisArray != null && diagnosisArray.isArray()) {
                for (JsonNode node : diagnosisArray) {
                    String diseaseName = node.path("diseaseName").asText(null);
                    double probabilityDouble = node.path("probability").asDouble(0.0);
                    String reason = node.path("reason").asText(null);

                    // 没有疾病名就跳过
                    if (diseaseName == null || diseaseName.isEmpty()) {
                        continue;
                    }

                    AiPreDiagnosis item = new AiPreDiagnosis();
                    item.setPatientId(patientId);
                    item.setSessionId(sessionId);
                    item.setDiagnosis(diseaseName);
                    item.setProbability(BigDecimal.valueOf(probabilityDouble));
                    item.setReasoning(reason);

                    result.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 先用 RuntimeException，后面你愿意的话可以改成 BusinessException
            throw new RuntimeException("解析 AI 诊断结果失败", e);
        }
        return result;
    }
}
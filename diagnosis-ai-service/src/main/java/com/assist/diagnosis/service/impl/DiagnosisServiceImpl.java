package com.assist.diagnosis.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import com.assist.common.common.ErrorCode;
import com.assist.common.common.BusinessException;
import com.assist.common.dto.request.DiagnosisEvaluateRequest;
import com.assist.common.dto.request.RiskEvaluateRequest;
import com.assist.common.dto.response.DiagnosisEvaluateResponse;
import com.assist.common.dto.response.DiagnosisAndRiskResponse;
import com.assist.common.entity.AiPreDiagnosis;
import com.assist.common.entity.AiSymptomStructured;
import com.assist.diagnosis.mapper.AiPreDiagnosisMapper;
import com.assist.diagnosis.mapper.AiSymptomStructuredMapper;
import com.assist.diagnosis.service.DiagnosisService;
import com.assist.diagnosis.service.RiskAssessmentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisServiceImpl implements DiagnosisService {

    private final AiPreDiagnosisMapper aiPreDiagnosisMapper;
    private final AiSymptomStructuredMapper aiSymptomStructuredMapper;
    private final ChatClient chatClient;
    private final RiskAssessmentService riskAssessmentService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public DiagnosisEvaluateResponse evaluateDiagnosis(DiagnosisEvaluateRequest request) {
        Integer patientId = request.getPatientId();
        Integer sessionId = request.getSessionId();

        if (patientId == null || sessionId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "patientId/sessionId 不能为空");
        }

        // Redis 缓存键：diagnosis:患者ID:会话ID
        String cacheKey = "diagnosis:" + patientId + ":" + sessionId;
        
        // 1. 先查 Redis 缓存
        if (redisTemplate != null) {
            try {
                DiagnosisEvaluateResponse cached = (DiagnosisEvaluateResponse) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.info("从 Redis 缓存获取诊断结果，patientId: {}, sessionId: {}", patientId, sessionId);
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Redis 缓存读取失败，继续查询数据库: {}", e.getMessage());
            }
        }
        
        // 2. 检查数据库中是否已有诊断结果
        QueryWrapper<AiPreDiagnosis> diagnosisWrapper = new QueryWrapper<>();
        diagnosisWrapper.eq("patient_id", patientId)
                .eq("session_id", sessionId)
                .orderByDesc("create_time");
        List<AiPreDiagnosis> existingDiagnoses = aiPreDiagnosisMapper.selectList(diagnosisWrapper);
        
        if (existingDiagnoses != null && !existingDiagnoses.isEmpty()) {
            // 数据库中有诊断结果，直接返回（避免重复调用AI）
            log.info("从数据库获取已有诊断结果，patientId: {}, sessionId: {}, 诊断数量: {}", 
                    patientId, sessionId, existingDiagnoses.size());
            DiagnosisEvaluateResponse resp = new DiagnosisEvaluateResponse();
            resp.setDiagnoses(existingDiagnoses);
            resp.setMessage("AI 初步诊断完成（从数据库读取）");
            
            // 存入 Redis 缓存（2 小时过期）
            if (redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set(cacheKey, resp, 2, TimeUnit.HOURS);
                    log.info("诊断结果已存入 Redis 缓存，patientId: {}, sessionId: {}", patientId, sessionId);
                } catch (Exception e) {
                    log.warn("Redis 缓存写入失败: {}", e.getMessage());
                }
            }
            return resp;
        }

        // 3. 数据库中没有，需要调用AI生成
        log.info("数据库中无诊断结果，开始调用AI生成，patientId: {}, sessionId: {}", patientId, sessionId);
        
        // 读取该 session 的结构化症状
        QueryWrapper<AiSymptomStructured> wrapper = new QueryWrapper<>();
        wrapper.eq("patient_id", patientId);
        wrapper.eq("session_id", sessionId);

        List<AiSymptomStructured> symptomList = aiSymptomStructuredMapper.selectList(wrapper);

        if (symptomList == null || symptomList.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到该会话的结构化症状数据");
        }

        // 构造 Prompt
        String prompt = buildDiagnosisPrompt(symptomList);

        // 调用通义千问模型
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        log.info("AI 返回内容：{}", aiResponse);

        // 解析 JSON，生成诊断列表
        List<AiPreDiagnosis> aiResultList = parseDiagnosisFromJson(aiResponse, patientId, sessionId);

        // 将结果写入 ai_pre_diagnosis 表
        for (AiPreDiagnosis item : aiResultList) {
            aiPreDiagnosisMapper.insert(item);
        }

        // 组装响应
        DiagnosisEvaluateResponse resp = new DiagnosisEvaluateResponse();
        resp.setDiagnoses(aiResultList);
        resp.setMessage("AI 初步诊断完成");
        
        // 存入 Redis 缓存（2 小时过期）
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, resp, 2, TimeUnit.HOURS);
                log.info("诊断结果已存入 Redis 缓存，patientId: {}, sessionId: {}", patientId, sessionId);
            } catch (Exception e) {
                log.warn("Redis 缓存写入失败: {}", e.getMessage());
            }
        }
        
        return resp;
    }

    @Override
    public DiagnosisAndRiskResponse evaluateDiagnosisAndRisk(DiagnosisEvaluateRequest request) {
        // 1. 先执行初步诊断
        DiagnosisEvaluateResponse diagnosisResponse = evaluateDiagnosis(request);
        
        // 2. 再执行风险评估（风险评估会使用已生成的诊断结果）
        RiskEvaluateRequest riskRequest = new RiskEvaluateRequest();
        riskRequest.setPatientId(request.getPatientId());
        riskRequest.setSessionId(request.getSessionId());
        com.assist.common.dto.response.RiskEvaluateResponse riskResponse = 
                riskAssessmentService.evaluateRisk(riskRequest);
        
        // 3. 合并结果
        DiagnosisAndRiskResponse combinedResponse = new DiagnosisAndRiskResponse();
        combinedResponse.setDiagnoses(diagnosisResponse.getDiagnoses());
        combinedResponse.setRiskAssessment(riskResponse.getRiskAssessment());
        combinedResponse.setMessage("AI 初步诊断和风险评估完成");
        
        return combinedResponse;
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
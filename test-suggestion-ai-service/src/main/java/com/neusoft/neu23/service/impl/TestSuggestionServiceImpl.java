package com.neusoft.neu23.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.assist.common.dto.request.TestSuggestionRequest;
import com.assist.common.dto.response.TestSuggestionResponse;
import com.assist.common.entity.AiPreDiagnosis;
import com.assist.common.entity.AiRiskAssessment;
import com.assist.common.entity.AiSymptomStructured;
import com.assist.common.entity.AiTestSuggestion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.neu23.mapper.AiPreDiagnosisMapper;
import com.neusoft.neu23.mapper.AiRiskAssessmentMapper;
import com.neusoft.neu23.mapper.AiSymptomStructuredMapper;
import com.neusoft.neu23.mapper.AiTestSuggestionMapper;
import com.neusoft.neu23.service.TestSuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AI 检查建议服务实现类
 * 
 * @author StellarMed Team
 */
@Slf4j
@Service
public class TestSuggestionServiceImpl implements TestSuggestionService {
    
    @Autowired
    private DashScopeChatModel chatModel;
    
    @Autowired
    private AiTestSuggestionMapper testSuggestionMapper;
    
    @Autowired
    private AiSymptomStructuredMapper symptomMapper;
    
    @Autowired
    private AiPreDiagnosisMapper diagnosisMapper;
    
    @Autowired
    private AiRiskAssessmentMapper riskMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    @Transactional
    public TestSuggestionResponse generateTestSuggestions(TestSuggestionRequest request) {
        log.info("🔬 开始生成检查建议，patientId: {}, sessionId: {}", 
                request.getPatientId(), request.getSessionId());
        
        TestSuggestionResponse response = new TestSuggestionResponse();
        
        try {
            // 1. 从数据库获取相关信息（先获取，用于判断数据是否有更新）
            List<AiSymptomStructured> symptoms = symptomMapper.selectBySessionId(request.getSessionId());
            List<AiPreDiagnosis> diagnoses = diagnosisMapper.selectBySessionId(request.getSessionId());
            List<AiRiskAssessment> risks = riskMapper.selectBySessionId(request.getSessionId());
            
            log.info("📊 已获取数据 - 症状数: {}, 诊断数: {}, 风险评估数: {}", 
                    symptoms.size(), diagnoses.size(), risks.size());
            
            // 2. 检查数据是否充足
            if (symptoms.isEmpty()) {
                log.warn("⚠️ 结构化症状为空，无法生成检查建议");
                response.setMessage("结构化症状数据不足，无法生成检查建议");
                response.setTestSuggestions(new ArrayList<>());
                return response;
            }
            
            // 3. 检查该 session 是否已有检查建议，并判断数据是否有更新
            List<AiTestSuggestion> existingSuggestions = testSuggestionMapper.selectBySessionId(request.getSessionId());
            if (!existingSuggestions.isEmpty()) {
                // 获取检查建议的最新创建时间
                Date suggestionLatestTime = testSuggestionMapper.getLatestCreateTime(request.getSessionId());
                
                // 获取症状/诊断/风险评估的最新更新时间
                Date symptomLatestTime = symptomMapper.getLatestUpdateTime(request.getSessionId());
                Date diagnosisLatestTime = diagnosisMapper.getLatestUpdateTime(request.getSessionId());
                Date riskLatestTime = riskMapper.getLatestUpdateTime(request.getSessionId());
                
                // 找出数据的最新更新时间
                Date dataLatestTime = null;
                if (symptomLatestTime != null) {
                    dataLatestTime = symptomLatestTime;
                }
                if (diagnosisLatestTime != null && 
                    (dataLatestTime == null || diagnosisLatestTime.after(dataLatestTime))) {
                    dataLatestTime = diagnosisLatestTime;
                }
                if (riskLatestTime != null && 
                    (dataLatestTime == null || riskLatestTime.after(dataLatestTime))) {
                    dataLatestTime = riskLatestTime;
                }
                
                // 如果检查建议的时间 >= 数据的最新更新时间，说明数据没有更新，返回已有数据
                if (suggestionLatestTime != null && dataLatestTime != null && 
                    !suggestionLatestTime.before(dataLatestTime)) {
                    log.info("ℹ️ 该 session 已存在 {} 条检查建议，且数据未更新，返回已有数据", existingSuggestions.size());
                    response.setTestSuggestions(existingSuggestions);
                    response.setMessage("已存在检查建议，返回已有数据");
                    return response;
                } else {
                    // 数据有更新，删除旧的检查建议，重新生成
                    log.info("🔄 检测到症状/诊断数据有更新，删除旧的检查建议，重新生成");
                    int deletedCount = testSuggestionMapper.deleteBySessionId(request.getSessionId());
                    log.info("🗑️ 已删除 {} 条旧的检查建议", deletedCount);
                }
            }
            
            // 4. 构造 AI Prompt
            String prompt = buildPrompt(symptoms, diagnoses, risks);
            log.debug("📝 构造的 Prompt: \n{}", prompt);
            
            // 5. 调用 AI 模型
            String aiResponse = callAiModel(prompt);
            log.info("🤖 AI 模型返回结果: {}", aiResponse);
            
            // 6. 解析 AI 返回结果
            List<TestSuggestionDto> aiSuggestions = parseAiResponse(aiResponse);
            log.info("✅ 解析成功，获得 {} 条检查建议", aiSuggestions.size());
            
            // 7. 保存到数据库
            List<AiTestSuggestion> entities = new ArrayList<>();
            for (TestSuggestionDto dto : aiSuggestions) {
                AiTestSuggestion entity = new AiTestSuggestion();
                entity.setPatientId(request.getPatientId());
                entity.setSessionId(request.getSessionId());
                entity.setTestName(dto.getTestName());
                entity.setReason(dto.getReason());
                entity.setCreatedTime(new Date());
                entities.add(entity);
            }
            
            if (!entities.isEmpty()) {
                testSuggestionMapper.batchInsert(entities);
                log.info("💾 已保存 {} 条检查建议到数据库", entities.size());
            }
            
            // 8. 构造响应
            response.setTestSuggestions(entities);
            response.setMessage("检查建议生成成功");
            
        } catch (Exception e) {
            log.error("❌ 生成检查建议失败", e);
            response.setMessage("生成检查建议失败: " + e.getMessage());
            response.setTestSuggestions(new ArrayList<>());
        }
        
        return response;
    }
    
    @Override
    public TestSuggestionResponse getTestSuggestionsBySessionId(Integer sessionId) {
        log.info("🔍 查询检查建议，sessionId: {}", sessionId);
        
        TestSuggestionResponse response = new TestSuggestionResponse();
        
        try {
            List<AiTestSuggestion> suggestions = testSuggestionMapper.selectBySessionId(sessionId);
            response.setTestSuggestions(suggestions);
            response.setMessage("查询成功");
            log.info("✅ 查询到 {} 条检查建议", suggestions.size());
        } catch (Exception e) {
            log.error("❌ 查询检查建议失败", e);
            response.setMessage("查询失败: " + e.getMessage());
            response.setTestSuggestions(new ArrayList<>());
        }
        
        return response;
    }
    
    @Override
    @Transactional
    public TestSuggestionResponse regenerateTestSuggestions(TestSuggestionRequest request) {
        log.info("🔄 重新生成检查建议，patientId: {}, sessionId: {}", 
                request.getPatientId(), request.getSessionId());
        
        TestSuggestionResponse response = new TestSuggestionResponse();
        
        try {
            // 1. 删除该 session 的旧检查建议
            int deletedCount = testSuggestionMapper.deleteBySessionId(request.getSessionId());
            log.info("🗑️ 已删除 {} 条旧的检查建议", deletedCount);
            
            // 2. 调用生成方法（此时不会有旧数据，会直接生成新的）
            response = generateTestSuggestions(request);
            response.setMessage("检查建议已重新生成");
            
        } catch (Exception e) {
            log.error("❌ 重新生成检查建议失败", e);
            response.setMessage("重新生成检查建议失败: " + e.getMessage());
            response.setTestSuggestions(new ArrayList<>());
        }
        
        return response;
    }
    
    /**
     * 构造 AI Prompt
     */
    private String buildPrompt(List<AiSymptomStructured> symptoms,
                                List<AiPreDiagnosis> diagnoses,
                                List<AiRiskAssessment> risks) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("# 患者信息分析\n\n");
        
        // 结构化症状
        sb.append("## 结构化症状：\n");
        for (AiSymptomStructured symptom : symptoms) {
            sb.append(String.format("- 症状名称: %s\n", symptom.getSymptomName()));
            sb.append(String.format("  严重程度: %s\n", symptom.getSeverity()));
            sb.append(String.format("  持续时间: %s\n", symptom.getDuration()));
            if (symptom.getExtraInfo() != null && !symptom.getExtraInfo().isEmpty()) {
                sb.append(String.format("  额外信息: %s\n", symptom.getExtraInfo()));
            }
        }
        sb.append("\n");
        
        // 初步诊断
        if (!diagnoses.isEmpty()) {
            sb.append("## 初步诊断：\n");
            for (AiPreDiagnosis diagnosis : diagnoses) {
                sb.append(String.format("- 诊断: %s (概率: %.2f%%)\n", 
                        diagnosis.getDiagnosis(), 
                        diagnosis.getProbability().doubleValue() * 100));
                sb.append(String.format("  理由: %s\n", diagnosis.getReasoning()));
            }
            sb.append("\n");
        }
        
        // 风险评估
        if (!risks.isEmpty()) {
            sb.append("## 风险评估：\n");
            for (AiRiskAssessment risk : risks) {
                sb.append(String.format("- 风险等级: %s\n", risk.getRiskLevel()));
                sb.append(String.format("  原因: %s\n", risk.getReason()));
            }
            sb.append("\n");
        }
        
        sb.append("## 任务要求：\n");
        sb.append("请根据以上信息，给出需要进行的医学检查建议。\n");
        sb.append("每个检查项目应该说明：\n");
        sb.append("1. 检查名称（如：血常规、CRP、胸片X光、心电图等）\n");
        sb.append("2. 检查理由（为什么需要做这个检查）\n\n");
        sb.append("请以 JSON 数组格式返回，格式如下：\n");
        sb.append("[\n");
        sb.append("  {\n");
        sb.append("    \"testName\": \"血常规\",\n");
        sb.append("    \"reason\": \"检查白细胞计数，判断是否存在细菌感染\"\n");
        sb.append("  },\n");
        sb.append("  {\n");
        sb.append("    \"testName\": \"CRP（C反应蛋白）\",\n");
        sb.append("    \"reason\": \"评估炎症程度，辅助判断感染类型\"\n");
        sb.append("  }\n");
        sb.append("]\n\n");
        sb.append("注意：只返回 JSON 数组，不要包含其他解释性文字。");
        
        return sb.toString();
    }
    
    /**
     * 调用 AI 模型
     */
    private String callAiModel(String promptText) {
        try {
            SystemMessage systemMessage = new SystemMessage(
                "你是一个专业的医学助手，擅长根据患者症状、诊断和风险评估，推荐合适的医学检查项目。" +
                "你的建议应该基于循证医学和临床指南。请以 JSON 格式返回结果。"
            );
            
            UserMessage userMessage = new UserMessage(promptText);
            
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
            
            ChatResponse response = chatModel.call(prompt);
            
            // 获取 AI 响应内容
            // SpringAI 1.0.3 中，getResult().getOutput() 返回 AssistantMessage
            // 使用 getText() 方法获取文本内容（这是 SpringAI 的标准方法）
            return response.getResult().getOutput().getText();
            
        } catch (Exception e) {
            log.error("❌ 调用 AI 模型失败", e);
            throw new RuntimeException("AI 模型调用失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析 AI 返回结果
     */
    private List<TestSuggestionDto> parseAiResponse(String aiResponse) {
        try {
            // 清理响应文本，提取 JSON 部分
            String jsonStr = extractJson(aiResponse);
            
            // 解析 JSON
            return objectMapper.readValue(jsonStr, 
                    new TypeReference<List<TestSuggestionDto>>() {});
            
        } catch (Exception e) {
            log.error("❌ 解析 AI 响应失败: {}", aiResponse, e);
            
            // 返回默认建议
            List<TestSuggestionDto> defaults = new ArrayList<>();
            TestSuggestionDto dto = new TestSuggestionDto();
            dto.setTestName("血常规");
            dto.setReason("基础检查，评估患者整体健康状况");
            defaults.add(dto);
            
            return defaults;
        }
    }
    
    /**
     * 从 AI 响应中提取 JSON 字符串
     */
    private String extractJson(String response) {
        // 移除 markdown 代码块标记
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        
        response = response.trim();
        
        // 如果没有找到 JSON 数组，尝试提取
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        
        return response;
    }
    
    /**
     * 内部 DTO 类，用于解析 AI 返回结果
     */
    @lombok.Data
    private static class TestSuggestionDto {
        private String testName;
        private String reason;
    }
}


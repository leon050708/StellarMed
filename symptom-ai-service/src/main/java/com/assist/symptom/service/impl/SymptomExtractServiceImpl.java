package com.assist.symptom.service.impl;

import com.assist.common.dto.request.SymptomExtractRequest;
import com.assist.common.dto.response.SymptomExtractResponse;
import com.assist.common.entity.AiSymptomStructured;
import com.assist.common.entity.ChatRecord;
import com.assist.common.entity.SymptomRecord;
import com.assist.symptom.mapper.AiSymptomStructuredMapper;
import com.assist.symptom.mapper.ChatRecordMapper;
import com.assist.symptom.mapper.SymptomRecordMapper;
import com.assist.symptom.service.SymptomExtractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 症状提取服务实现
 * 使用SpringAI调用通义千问进行症状结构化
 */
@Slf4j
@Service
public class SymptomExtractServiceImpl implements SymptomExtractService {

    private final ChatClient symptomExtractChatClient;
    private final ChatRecordMapper chatRecordMapper;
    private final SymptomRecordMapper symptomRecordMapper;
    private final AiSymptomStructuredMapper aiSymptomStructuredMapper;

    public SymptomExtractServiceImpl(
            @Qualifier("symptomExtractChatClient") ChatClient symptomExtractChatClient,
            ChatRecordMapper chatRecordMapper,
            SymptomRecordMapper symptomRecordMapper,
            AiSymptomStructuredMapper aiSymptomStructuredMapper) {
        this.symptomExtractChatClient = symptomExtractChatClient;
        this.chatRecordMapper = chatRecordMapper;
        this.symptomRecordMapper = symptomRecordMapper;
        this.aiSymptomStructuredMapper = aiSymptomStructuredMapper;
    }

    @Override
    @Transactional
    public SymptomExtractResponse extractStructuredSymptoms(SymptomExtractRequest request) {
        log.info("开始提取结构化症状，patientId: {}, sessionId: {}", 
                request.getPatientId(), request.getSessionId());

        // 1. 从数据库读取聊天记录和原始症状
        List<ChatRecord> chatRecords = chatRecordMapper.selectBySessionId(request.getSessionId());
        List<SymptomRecord> symptomRecords = symptomRecordMapper.selectBySessionId(request.getSessionId());

        // 2. 构建用户输入（包含对话记录和原始症状）
        String userInput = buildUserInput(chatRecords, symptomRecords);
        log.debug("构建的用户输入: {}", userInput);

        // 3. 使用ChatClient调用AI，获取JSON格式的结构化症状
        String conversationId = "symptom-extract-" + request.getSessionId();
        String aiResponse = symptomExtractChatClient.prompt()
                .user(userInput)
                .advisors(a -> a.param("conversationId", conversationId))
                .call()
                .content();

        log.info("AI返回结果: {}", aiResponse);

        // 4. 解析AI返回的JSON并保存到数据库
        List<AiSymptomStructured> structuredSymptoms = parseAndSaveSymptoms(aiResponse, request);

        // 5. 构建响应
        SymptomExtractResponse result = new SymptomExtractResponse();
        result.setStructuredSymptoms(structuredSymptoms);
        result.setMessage("症状结构化完成：" + aiResponse);

        return result;
    }

    /**
     * 构建用户输入（包含对话记录和原始症状信息）
     */
    private String buildUserInput(List<ChatRecord> chatRecords, List<SymptomRecord> symptomRecords) {
        StringBuilder input = new StringBuilder();
        input.append("请根据以下患者对话记录和症状描述，提取并结构化症状信息。\n\n");
        
        input.append("【对话记录】\n");
        if (chatRecords.isEmpty()) {
            input.append("暂无对话记录\n");
        } else {
            for (ChatRecord chat : chatRecords) {
                input.append("患者: ").append(chat.getQuestion()).append("\n");
                if (chat.getAiReply() != null) {
                    input.append("医生: ").append(chat.getAiReply()).append("\n");
                }
            }
        }
        
        input.append("\n【原始症状】\n");
        if (symptomRecords.isEmpty()) {
            input.append("暂无原始症状记录\n");
        } else {
            for (SymptomRecord symptom : symptomRecords) {
                input.append("- ").append(symptom.getSymptomText());
                if (symptom.getSeverity() != null) {
                    input.append(" (严重程度: ").append(symptom.getSeverity()).append(")");
                }
                if (symptom.getDuration() != null) {
                    input.append(" (持续时间: ").append(symptom.getDuration()).append(")");
                }
                input.append("\n");
            }
        }
        
        input.append("\n请提取所有症状信息，并以JSON数组格式返回。");

        return input.toString();
    }

    /**
     * 解析AI返回的JSON并保存到数据库
     */
    private List<AiSymptomStructured> parseAndSaveSymptoms(String aiResponse, SymptomExtractRequest request) {
        List<AiSymptomStructured> symptoms = new java.util.ArrayList<>();
        
        try {
            // 清理AI返回的文本（可能包含markdown代码块标记）
            String jsonText = aiResponse.trim();
            if (jsonText.startsWith("```json")) {
                jsonText = jsonText.substring(7);
            }
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.substring(3);
            }
            if (jsonText.endsWith("```")) {
                jsonText = jsonText.substring(0, jsonText.length() - 3);
            }
            jsonText = jsonText.trim();

            // 使用Jackson解析JSON数组
            // Spring Boot已包含Jackson依赖，可以直接使用
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>> typeRef = 
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {};
            java.util.List<java.util.Map<String, Object>> symptomList = mapper.readValue(jsonText, typeRef);
            
            // 保存每个症状
            for (java.util.Map<String, Object> symptomMap : symptomList) {
                AiSymptomStructured symptom = new AiSymptomStructured();
                // 确保 ID 为 null，让数据库自动生成
                symptom.setId(null);
                symptom.setPatientId(request.getPatientId());
                symptom.setSessionId(request.getSessionId());
                symptom.setSymptomName((String) symptomMap.get("symptomName"));
                symptom.setSeverity((String) symptomMap.get("severity"));
                symptom.setDuration((String) symptomMap.get("duration"));
                symptom.setExtraInfo(symptomMap.get("extraInfo") != null ? 
                        symptomMap.get("extraInfo").toString() : "");
                symptom.setCreateTime(new java.util.Date());
                
                // 插入后，MyBatis-Plus 会自动回填生成的 ID
                aiSymptomStructuredMapper.insert(symptom);
                log.debug("保存症状成功，生成的ID: {}", symptom.getId());
                symptoms.add(symptom);
            }
            
            log.info("成功解析并保存 {} 个结构化症状", symptoms.size());
            
        } catch (Exception e) {
            log.error("解析AI返回结果失败", e);
            // 如果解析失败，返回空列表
        }
        
        return symptoms;
    }
}


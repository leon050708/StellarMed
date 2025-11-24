package com.assist.symptom.tools;

import com.assist.common.entity.AiSymptomStructured;
import com.assist.symptom.mapper.AiSymptomStructuredMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 症状工具类
 * 提供AI可以调用的工具函数，用于保存结构化症状数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SymptomTools {

    private final AiSymptomStructuredMapper aiSymptomStructuredMapper;

    /**
     * 保存结构化症状数据
     * AI会调用此方法将提取的症状信息保存到数据库
     * 
     * @param patientId 患者ID
     * @param sessionId 会话ID
     * @param symptomName 症状名称
     * @param severity 严重程度（mild/moderate/severe）
     * @param duration 持续时间
     * @param extraInfo 额外信息
     * @return 保存结果信息
     */
    public String saveStructuredSymptoms(
            Integer patientId,
            Integer sessionId,
            String symptomName,
            String severity,
            String duration,
            String extraInfo) {
        
        try {
            log.info("保存结构化症状：patientId={}, sessionId={}, symptomName={}, severity={}, duration={}, extraInfo={}",
                    patientId, sessionId, symptomName, severity, duration, extraInfo);
            
            AiSymptomStructured symptom = new AiSymptomStructured();
            symptom.setPatientId(patientId);
            symptom.setSessionId(sessionId);
            symptom.setSymptomName(symptomName);
            symptom.setSeverity(severity);
            symptom.setDuration(duration);
            symptom.setExtraInfo(extraInfo != null ? extraInfo : "");
            symptom.setCreateTime(new Date());
            
            aiSymptomStructuredMapper.insert(symptom);
            
            return String.format("症状已成功保存：%s（严重程度：%s，持续时间：%s）", 
                    symptomName, severity, duration);
        } catch (Exception e) {
            log.error("保存结构化症状失败", e);
            return "保存症状失败：" + e.getMessage();
        }
    }

    /**
     * 批量保存结构化症状数据
     * 当AI提取到多个症状时，可以调用此方法批量保存
     * 
     * @param patientId 患者ID
     * @param sessionId 会话ID
     * @param symptoms JSON格式的症状列表字符串
     * @return 保存结果信息
     */
    public String saveStructuredSymptomsBatch(
            Integer patientId,
            Integer sessionId,
            String symptoms) {
        
        try {
            log.info("批量保存结构化症状：patientId={}, sessionId={}, symptoms={}",
                    patientId, sessionId, symptoms);
            
            // TODO: 解析JSON字符串并批量保存
            // 这里简化处理，实际应该使用Jackson解析JSON数组
            // 示例：List<Map<String, Object>> symptomList = mapper.readValue(symptoms, new TypeReference<>() {});
            
            return "批量保存症状功能待实现，当前请使用单个保存方法";
        } catch (Exception e) {
            log.error("批量保存结构化症状失败", e);
            return "批量保存症状失败：" + e.getMessage();
        }
    }
}


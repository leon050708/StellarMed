package com.assist.common.dto.response;

import com.assist.common.entity.AiPreDiagnosis;
import com.assist.common.entity.AiRiskAssessment;
import lombok.Data;
import java.util.List;

/**
 * 诊断和风险评估合并响应
 * 包含AI初步诊断列表和风险评估结果
 */
@Data
public class DiagnosisAndRiskResponse {
    /**
     * AI初步诊断列表
     */
    private List<AiPreDiagnosis> diagnoses;
    
    /**
     * 风险评估结果
     */
    private AiRiskAssessment riskAssessment;
    
    /**
     * 响应消息
     */
    private String message;
}


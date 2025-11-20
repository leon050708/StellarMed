package com.assist.common.dto.response;

import com.assist.common.entity.AiRiskAssessment;
import lombok.Data;

/**
 * 风险评估响应
 */
@Data
public class RiskEvaluateResponse {
    private AiRiskAssessment riskAssessment;
    private String message;
}


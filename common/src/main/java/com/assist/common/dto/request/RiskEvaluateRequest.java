package com.assist.common.dto.request;

import lombok.Data;

/**
 * 风险评估请求
 */
@Data
public class RiskEvaluateRequest {
    private Integer patientId;
    private Integer sessionId;
}


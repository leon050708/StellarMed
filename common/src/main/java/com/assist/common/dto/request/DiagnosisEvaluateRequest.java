package com.assist.common.dto.request;

import lombok.Data;

/**
 * 诊断评估请求
 */
@Data
public class DiagnosisEvaluateRequest {
    private Integer patientId;
    private Integer sessionId;
}


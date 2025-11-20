package com.assist.common.dto.request;

import lombok.Data;

/**
 * 症状提取请求
 */
@Data
public class SymptomExtractRequest {
    private Integer patientId;
    private Integer sessionId;
    private String symptomText;
}


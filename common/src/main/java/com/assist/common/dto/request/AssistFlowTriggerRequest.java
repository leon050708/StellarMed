package com.assist.common.dto.request;

import lombok.Data;

/**
 * 助诊流程触发请求
 */
@Data
public class AssistFlowTriggerRequest {
    private Integer patientId;
    private Integer sessionId;
    private String initialSymptom;
}


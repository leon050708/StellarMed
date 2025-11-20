package com.assist.common.dto.request;

import lombok.Data;

/**
 * 处方生成请求
 */
@Data
public class PrescriptionGenerateRequest {
    private Integer patientId;
    private Integer sessionId;
}


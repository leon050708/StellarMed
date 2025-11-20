package com.assist.common.dto.request;

import lombok.Data;

/**
 * 总结生成请求
 */
@Data
public class SummaryGenerateRequest {
    private Integer patientId;
    private Integer sessionId;
}


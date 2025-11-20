package com.assist.common.dto.request;

import lombok.Data;

/**
 * 检查建议请求
 */
@Data
public class TestSuggestionRequest {
    private Integer patientId;
    private Integer sessionId;
}


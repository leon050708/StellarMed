package com.assist.common.dto.response;

import com.assist.common.entity.AiSessionSummary;
import lombok.Data;

/**
 * 总结生成响应
 */
@Data
public class SummaryGenerateResponse {
    private AiSessionSummary summary;
    private String message;
}


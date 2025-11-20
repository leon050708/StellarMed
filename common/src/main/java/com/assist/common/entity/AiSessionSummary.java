package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * AI 会话总结实体类
 */
@Data
public class AiSessionSummary {
    private Integer summaryId;
    private Integer patientId;
    private Integer sessionId;
    private String summaryText;
    private String reasoningChain;
    private Date createdTime;
}


package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * AI 检查建议实体类
 */
@Data
public class AiTestSuggestion {
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private String testName;
    private String reason;
    private Date createdTime;
}


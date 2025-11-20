package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * AI 风险评估实体类
 */
@Data
public class AiRiskAssessment {
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private String riskLevel;
    private String reason;
    private Date createdTime;
}


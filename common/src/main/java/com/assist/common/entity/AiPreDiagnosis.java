package com.assist.common.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * AI 初步诊断实体类
 */
@Data
public class AiPreDiagnosis {
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private String diagnosis;
    private BigDecimal probability;
    private String reasoning;
    private Date createTime;
}


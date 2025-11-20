package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * AI 处方建议实体类
 */
@Data
public class AiPrescription {
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private String drugName;
    private String dosage;
    private String duration;
    private String usageInstruction;
    private String reason;
    private Date createTime;
}


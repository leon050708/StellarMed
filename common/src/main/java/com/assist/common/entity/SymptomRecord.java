package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * 原始症状记录实体类（用户输入）
 */
@Data
public class SymptomRecord {
    private Integer symptomId;
    private Integer patientId;
    private Integer sessionId;
    private String symptomText;
    private String severity;
    private String duration;
    private Date extractedTime;
}


package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * AI 结构化症状实体类
 */
@Data
public class AiSymptomStructured {
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private String symptomName;
    private String severity;
    private String duration;
    private String extraInfo;
    private Date createTime;
}


package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * 医生人工总结实体类
 */
@Data
public class PatientSummary {
    private Integer summaryId;
    private Integer patientId;
    private Integer appointmentId;
    private String summaryText;
    private Date createdTime;
}


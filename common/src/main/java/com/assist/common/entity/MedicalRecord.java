package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * 医疗记录实体类（医生手写诊断）
 */
@Data
public class MedicalRecord {
    private Integer recordId;
    private Integer appointmentId;
    private Integer patientId;
    private Integer doctorId;
    private String diagnosis;
    private String prescription;
    private String suggestions;
    private Date createdTime;
}


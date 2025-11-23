package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * 医生最终确认诊断实体类
 * 对应数据库表：doctor_final_diagnosis
 */
@Data
public class DoctorFinalDiagnosis {
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private Integer doctorId;
    private String finalDiagnosis;
    private String finalPrescription;
    private String comment;
    private Date createdTime;
}

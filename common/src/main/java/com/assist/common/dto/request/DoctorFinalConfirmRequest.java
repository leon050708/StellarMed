package com.assist.common.dto.request;

import lombok.Data;

/**
 * 医生最终确认请求
 */
@Data
public class DoctorFinalConfirmRequest {
    private Integer patientId;
    private Integer sessionId;
    private Integer doctorId;
    private String finalDiagnosis;
    private String finalPrescription;
    private String comment;
}


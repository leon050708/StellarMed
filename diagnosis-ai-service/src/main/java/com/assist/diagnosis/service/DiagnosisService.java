package com.assist.diagnosis.service;

import com.assist.common.dto.request.DiagnosisEvaluateRequest;
import com.assist.common.dto.response.DiagnosisEvaluateResponse;

public interface DiagnosisService {

    /**
     * 根据 patientId + sessionId 生成 AI 初步诊断列表，并入库
     */
    DiagnosisEvaluateResponse evaluateDiagnosis(DiagnosisEvaluateRequest request);
}
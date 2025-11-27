package com.assist.diagnosis.service;

import com.assist.common.dto.request.DiagnosisEvaluateRequest;
import com.assist.common.dto.response.DiagnosisEvaluateResponse;
import com.assist.common.dto.response.DiagnosisAndRiskResponse;

public interface DiagnosisService {

    /**
     * 根据 patientId + sessionId 生成 AI 初步诊断列表，并入库
     */
    DiagnosisEvaluateResponse evaluateDiagnosis(DiagnosisEvaluateRequest request);

    /**
     * 合并评估：同时生成 AI 初步诊断和风险评估
     * 
     * @param request 诊断评估请求，包含 patientId 和 sessionId
     * @return 包含诊断列表和风险评估的合并响应
     */
    DiagnosisAndRiskResponse evaluateDiagnosisAndRisk(DiagnosisEvaluateRequest request);
}
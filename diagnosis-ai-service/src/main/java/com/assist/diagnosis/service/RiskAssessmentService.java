package com.assist.diagnosis.service;

import com.assist.common.dto.request.RiskEvaluateRequest;
import com.assist.common.dto.response.RiskEvaluateResponse;

public interface RiskAssessmentService {

    /**
     * 根据 patientId + sessionId 生成 AI 风险评估，并入库
     */
    RiskEvaluateResponse evaluateRisk(RiskEvaluateRequest request);
}
package com.assist.diagnosis.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.DiagnosisEvaluateRequest;
import com.assist.common.dto.request.RiskEvaluateRequest;
import com.assist.common.dto.response.DiagnosisEvaluateResponse;
import com.assist.common.dto.response.RiskEvaluateResponse;
import com.assist.diagnosis.service.DiagnosisService;
import com.assist.diagnosis.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class DiagnosisAiController {

    private final DiagnosisService diagnosisService;
    private final RiskAssessmentService riskAssessmentService;

    /**
     * AI 初步诊断
     * POST /api/ai/diagnosis/evaluate
     */
    @PostMapping("/diagnosis/evaluate")
    public ApiResponse<DiagnosisEvaluateResponse> evaluateDiagnosis(
            @RequestBody DiagnosisEvaluateRequest request) {

        DiagnosisEvaluateResponse resp = diagnosisService.evaluateDiagnosis(request);
        return ApiResponse.success(resp);
    }

    /**
     * AI 风险评估
     * POST /api/ai/risk/evaluate
     */
    @PostMapping("/risk/evaluate")
    public ApiResponse<RiskEvaluateResponse> evaluateRisk(
            @RequestBody RiskEvaluateRequest request) {

        RiskEvaluateResponse resp = riskAssessmentService.evaluateRisk(request);
        return ApiResponse.success(resp);
    }
}
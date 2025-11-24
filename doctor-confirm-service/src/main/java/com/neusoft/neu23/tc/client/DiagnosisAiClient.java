package com.neusoft.neu23.tc.client;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.DiagnosisEvaluateRequest;
import com.assist.common.dto.request.RiskEvaluateRequest;
import com.assist.common.dto.response.DiagnosisEvaluateResponse;
import com.assist.common.dto.response.RiskEvaluateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 诊断AI服务 (服务3)
 * 接口文档：
 * - POST /api/ai/diagnosis/evaluate (AI 初步诊断)
 * - POST /api/ai/risk/evaluate (AI 风险评估)
 */
@FeignClient(name = "diagnosis-ai-service")
public interface DiagnosisAiClient {

    /**
     * AI 初步诊断
     * POST /api/ai/diagnosis/evaluate
     */
    @PostMapping("/api/ai/diagnosis/evaluate")
    ApiResponse<DiagnosisEvaluateResponse> evaluateDiagnosis(@RequestBody DiagnosisEvaluateRequest request);

    /**
     * AI 风险评估
     * POST /api/ai/risk/evaluate
     */
    @PostMapping("/api/ai/risk/evaluate")
    ApiResponse<RiskEvaluateResponse> evaluateRisk(@RequestBody RiskEvaluateRequest request);
}


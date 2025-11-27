package com.neusoft.neu23.tc.client;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.DiagnosisEvaluateRequest;
import com.assist.common.dto.request.RiskEvaluateRequest;
import com.assist.common.dto.response.DiagnosisAndRiskResponse;
import com.assist.common.dto.response.DiagnosisEvaluateResponse;
import com.assist.common.dto.response.RiskEvaluateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 诊断AI服务 (服务3)
 * 接口文档：
 * - POST /api/ai/diagnosis/evaluate (AI 初步诊断和风险评估 - 合并接口) ⭐ 推荐使用
 * - POST /api/ai/risk/evaluate (AI 风险评估 - 已废弃，建议使用合并接口)
 */
@FeignClient(name = "diagnosis-ai-service")
public interface DiagnosisAiClient {

    /**
     * AI 初步诊断和风险评估（合并接口）⭐ 推荐使用
     * POST /api/ai/diagnosis/evaluate
     * 同时返回诊断列表和风险评估结果
     */
    @PostMapping("/api/ai/diagnosis/evaluate")
    ApiResponse<DiagnosisAndRiskResponse> evaluateDiagnosisAndRisk(@RequestBody DiagnosisEvaluateRequest request);

    /**
     * AI 初步诊断（单独接口，已废弃）
     * POST /api/ai/diagnosis/evaluate
     * @deprecated 建议使用 evaluateDiagnosisAndRisk 合并接口
     */
    @Deprecated
    @PostMapping("/api/ai/diagnosis/evaluate")
    ApiResponse<DiagnosisEvaluateResponse> evaluateDiagnosis(@RequestBody DiagnosisEvaluateRequest request);

    /**
     * AI 风险评估（单独接口，已废弃）
     * POST /api/ai/risk/evaluate
     * @deprecated 建议使用 evaluateDiagnosisAndRisk 合并接口
     */
    @Deprecated
    @PostMapping("/api/ai/risk/evaluate")
    ApiResponse<RiskEvaluateResponse> evaluateRisk(@RequestBody RiskEvaluateRequest request);
}


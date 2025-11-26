package com.assist.diagnosis.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.common.BusinessException;
import com.assist.common.common.ErrorCode;
import com.assist.common.dto.request.DiagnosisEvaluateRequest;
import com.assist.common.dto.request.RiskEvaluateRequest;
import com.assist.common.dto.response.DiagnosisEvaluateResponse;
import com.assist.common.dto.response.RiskEvaluateResponse;
import com.assist.diagnosis.service.DiagnosisService;
import com.assist.diagnosis.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * AI诊断控制器
 * 提供AI初步诊断和风险评估接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class DiagnosisAiController {

    private final DiagnosisService diagnosisService;
    private final RiskAssessmentService riskAssessmentService;

    /**
     * AI 初步诊断
     * POST /api/ai/diagnosis/evaluate
     * 
     * @param request 诊断评估请求，包含 patientId 和 sessionId
     * @return 诊断评估响应，包含诊断列表
     */
    @PostMapping("/diagnosis/evaluate")
    public ApiResponse<DiagnosisEvaluateResponse> evaluateDiagnosis(
            @RequestBody @Validated DiagnosisEvaluateRequest request) {

        log.info("🔍 收到AI初步诊断请求: patientId={}, sessionId={}", 
                request.getPatientId(), request.getSessionId());

        try {
            // 参数验证
            if (request.getPatientId() == null || request.getSessionId() == null) {
                log.warn("⚠️ 参数验证失败: patientId={}, sessionId={}", 
                        request.getPatientId(), request.getSessionId());
                return ApiResponse.error(ErrorCode.PARAM_ERROR.ordinal(), "患者ID和会话ID不能为空");
            }

            DiagnosisEvaluateResponse resp = diagnosisService.evaluateDiagnosis(request);
            
            if (resp != null && resp.getDiagnoses() != null) {
                log.info("✅ AI初步诊断完成: patientId={}, sessionId={}, 诊断数量={}", 
                        request.getPatientId(), request.getSessionId(), resp.getDiagnoses().size());
                return ApiResponse.success(resp);
            } else {
                log.warn("⚠️ 未生成任何诊断结果");
                return ApiResponse.error(resp != null ? resp.getMessage() : "诊断生成失败");
            }

        } catch (BusinessException e) {
            log.error("❌ AI初步诊断业务异常: patientId={}, sessionId={}, error={}", 
                    request.getPatientId(), request.getSessionId(), e.getMessage(), e);
            return ApiResponse.error(e.getErrorCode().ordinal(), e.getMessage());
        } catch (Exception e) {
            log.error("❌ AI初步诊断系统异常: patientId={}, sessionId={}", 
                    request.getPatientId(), request.getSessionId(), e);
            return ApiResponse.error(ErrorCode.SERVICE_ERROR.ordinal(), 
                    "AI初步诊断失败: " + e.getMessage());
        }
    }

    /**
     * AI 风险评估
     * POST /api/ai/risk/evaluate
     * 
     * @param request 风险评估请求，包含 patientId 和 sessionId
     * @return 风险评估响应，包含风险等级和原因
     */
    @PostMapping("/risk/evaluate")
    public ApiResponse<RiskEvaluateResponse> evaluateRisk(
            @RequestBody @Validated RiskEvaluateRequest request) {

        log.info("⚠️ 收到AI风险评估请求: patientId={}, sessionId={}", 
                request.getPatientId(), request.getSessionId());

        try {
            // 参数验证
            if (request.getPatientId() == null || request.getSessionId() == null) {
                log.warn("⚠️ 参数验证失败: patientId={}, sessionId={}", 
                        request.getPatientId(), request.getSessionId());
                return ApiResponse.error(ErrorCode.PARAM_ERROR.ordinal(), "患者ID和会话ID不能为空");
            }

            RiskEvaluateResponse resp = riskAssessmentService.evaluateRisk(request);
            
            if (resp != null && resp.getRiskAssessment() != null) {
                log.info("✅ AI风险评估完成: patientId={}, sessionId={}, 风险等级={}", 
                        request.getPatientId(), request.getSessionId(), 
                        resp.getRiskAssessment().getRiskLevel());
                return ApiResponse.success(resp);
            } else {
                log.warn("⚠️ 未生成风险评估结果");
                return ApiResponse.error(resp != null ? resp.getMessage() : "风险评估生成失败");
            }

        } catch (BusinessException e) {
            log.error("❌ AI风险评估业务异常: patientId={}, sessionId={}, error={}", 
                    request.getPatientId(), request.getSessionId(), e.getMessage(), e);
            return ApiResponse.error(e.getErrorCode().ordinal(), e.getMessage());
        } catch (Exception e) {
            log.error("❌ AI风险评估系统异常: patientId={}, sessionId={}", 
                    request.getPatientId(), request.getSessionId(), e);
            return ApiResponse.error(ErrorCode.SERVICE_ERROR.ordinal(), 
                    "AI风险评估失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Diagnosis AI Service is running!");
    }
}
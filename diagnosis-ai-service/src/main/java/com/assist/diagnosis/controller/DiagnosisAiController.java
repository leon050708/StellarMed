package com.assist.diagnosis.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.common.BusinessException;
import com.assist.common.common.ErrorCode;
import com.assist.common.dto.request.DiagnosisEvaluateRequest;
import com.assist.common.dto.response.DiagnosisAndRiskResponse;
import com.assist.diagnosis.service.DiagnosisService;
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

    /**
     * AI 诊断和风险评估（合并接口）
     * POST /api/ai/diagnosis/evaluate
     * 同时执行AI初步诊断和风险评估，返回合并结果
     * 
     * @param request 诊断评估请求，包含 patientId 和 sessionId
     * @return 包含诊断列表和风险评估的合并响应
     */
    @PostMapping("/diagnosis/evaluate")
    public ApiResponse<DiagnosisAndRiskResponse> evaluateDiagnosisAndRisk(
            @RequestBody @Validated DiagnosisEvaluateRequest request) {

        log.info("🔍 收到AI诊断和风险评估请求: patientId={}, sessionId={}", 
                request.getPatientId(), request.getSessionId());

        try {
            // 参数验证
            if (request.getPatientId() == null || request.getSessionId() == null) {
                log.warn("⚠️ 参数验证失败: patientId={}, sessionId={}", 
                        request.getPatientId(), request.getSessionId());
                return ApiResponse.error(ErrorCode.PARAM_ERROR.ordinal(), "患者ID和会话ID不能为空");
            }

            DiagnosisAndRiskResponse resp = diagnosisService.evaluateDiagnosisAndRisk(request);
            
            if (resp != null && resp.getDiagnoses() != null && resp.getRiskAssessment() != null) {
                log.info("✅ AI诊断和风险评估完成: patientId={}, sessionId={}, 诊断数量={}, 风险等级={}", 
                        request.getPatientId(), request.getSessionId(), 
                        resp.getDiagnoses().size(), 
                        resp.getRiskAssessment().getRiskLevel());
                return ApiResponse.success(resp);
            } else {
                log.warn("⚠️ 诊断或风险评估结果不完整");
                return ApiResponse.error(resp != null ? resp.getMessage() : "诊断和风险评估生成失败");
            }

        } catch (BusinessException e) {
            log.error("❌ AI诊断和风险评估业务异常: patientId={}, sessionId={}, error={}", 
                    request.getPatientId(), request.getSessionId(), e.getMessage(), e);
            return ApiResponse.error(e.getErrorCode().ordinal(), e.getMessage());
        } catch (Exception e) {
            log.error("❌ AI诊断和风险评估系统异常: patientId={}, sessionId={}", 
                    request.getPatientId(), request.getSessionId(), e);
            return ApiResponse.error(ErrorCode.SERVICE_ERROR.ordinal(), 
                    "AI诊断和风险评估失败: " + e.getMessage());
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
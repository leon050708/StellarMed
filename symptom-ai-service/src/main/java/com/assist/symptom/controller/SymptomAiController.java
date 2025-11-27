package com.assist.symptom.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.common.BusinessException;
import com.assist.common.common.ErrorCode;
import com.assist.common.dto.request.SymptomExtractRequest;
import com.assist.common.dto.response.SymptomExtractResponse;
import com.assist.symptom.service.SymptomExtractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 症状AI控制器
 * 提供症状结构化提取接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class SymptomAiController {

    private final SymptomExtractService symptomExtractService;

    /**
     * AI抽取结构化症状
     * POST /api/ai/symptoms/extract
     * 
     * @param request 症状提取请求，包含 patientId、sessionId 和 symptomText
     * @return 结构化症状响应，包含结构化症状列表
     */
    @PostMapping("/symptoms/extract")
    public ApiResponse<SymptomExtractResponse> extractSymptoms(
            @RequestBody @Validated SymptomExtractRequest request) {
        
        log.info("🔍 收到症状结构化提取请求: patientId={}, sessionId={}", 
                request.getPatientId(), request.getSessionId());

        try {
            // 参数验证
            if (request.getPatientId() == null || request.getSessionId() == null) {
                log.warn("⚠️ 参数验证失败: patientId={}, sessionId={}", 
                        request.getPatientId(), request.getSessionId());
                return ApiResponse.error(ErrorCode.PARAM_ERROR.ordinal(), "患者ID和会话ID不能为空");
            }
            
            if (request.getSymptomText() == null || request.getSymptomText().trim().isEmpty()) {
                log.warn("⚠️ 症状文本为空: patientId={}, sessionId={}", 
                        request.getPatientId(), request.getSessionId());
                return ApiResponse.error(ErrorCode.PARAM_ERROR.ordinal(), "症状文本不能为空");
            }

            SymptomExtractResponse response = symptomExtractService.extractStructuredSymptoms(request);
            
            if (response != null && response.getStructuredSymptoms() != null) {
                log.info("✅ 症状结构化提取完成: patientId={}, sessionId={}, 结构化症状数量={}", 
                        request.getPatientId(), request.getSessionId(), 
                        response.getStructuredSymptoms().size());
                return ApiResponse.success(response);
            } else {
                log.warn("⚠️ 未提取到任何结构化症状");
                return ApiResponse.error(response != null ? response.getMessage() : "症状结构化提取失败");
            }

        } catch (BusinessException e) {
            log.error("❌ 症状结构化提取业务异常: patientId={}, sessionId={}, error={}", 
                    request.getPatientId(), request.getSessionId(), e.getMessage(), e);
            return ApiResponse.error(e.getErrorCode().ordinal(), e.getMessage());
        } catch (Exception e) {
            log.error("❌ 症状结构化提取系统异常: patientId={}, sessionId={}", 
                    request.getPatientId(), request.getSessionId(), e);
            return ApiResponse.error(ErrorCode.SERVICE_ERROR.ordinal(), 
                    "症状结构化提取失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Symptom AI Service is running!");
    }
}


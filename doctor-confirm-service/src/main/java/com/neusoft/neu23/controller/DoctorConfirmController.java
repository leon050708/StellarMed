package com.neusoft.neu23.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.DoctorFinalConfirmRequest;
import com.assist.common.dto.response.AiAggregatedReport;
import com.assist.common.entity.DoctorFinalDiagnosis;
import com.neusoft.neu23.mapper.DoctorFinalDiagnosisMapper;
import com.neusoft.neu23.service.AiAnalysisService;
import com.neusoft.neu23.service.DoctorConfirmService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 医生最终确认控制器
 * 接收医生输入的最终诊断、处方等信息并保存
 */
@RestController
@RequestMapping("/api/doctor-confirm")
@RequiredArgsConstructor
public class DoctorConfirmController {

    private final DoctorConfirmService confirmService;
    private final AiAnalysisService aiAnalysisService;
    private final DoctorFinalDiagnosisMapper diagnosisMapper;

    /**
     * 7.1 聚合查询助诊报告
     * GET /api/doctor-confirm/ai-report?patientId=1&sessionId=1001
     * 返回所有 AI 数据汇总
     */
    @GetMapping("/ai-report")
    public ApiResponse<AiAggregatedReport> getAssistReport(@RequestParam Integer patientId,
                                                           @RequestParam Integer sessionId) {
        AiAggregatedReport report = confirmService.aggregateAssistReport(patientId, sessionId);
        return ApiResponse.success(report);
    }

    /**
     * 7.2 医生最终确认诊断
     * POST /api/doctor-confirm/final-diagnosis
     */
    @PostMapping("/final-diagnosis")
    public ApiResponse<Void> saveFinalConfirm(@RequestBody DoctorFinalConfirmRequest request) {
        return confirmService.saveFinalDiagnosis(request);
    }

    /**
     * 获取AI生成的确认建议
     * 基于聚合的助诊报告，生成AI建议供医生参考
     */
    @GetMapping("/ai/suggestion")
    public ApiResponse<String> getAiSuggestion(@RequestParam Integer patientId,
                                               @RequestParam Integer sessionId) {
        AiAggregatedReport report = confirmService.aggregateAssistReport(patientId, sessionId);
        String suggestion = aiAnalysisService.generateConfirmationSuggestion(report);
        return new ApiResponse<>(0, "success", suggestion);
    }

    /**
     * 获取诊断对比分析
     * 对比AI诊断和医生最终诊断的差异
     */
    @GetMapping("/ai/compare")
    public ApiResponse<String> compareDiagnosis(@RequestParam Integer sessionId) {
        LambdaQueryWrapper<DoctorFinalDiagnosis> wrapper = Wrappers.lambdaQuery(DoctorFinalDiagnosis.class)
                .eq(DoctorFinalDiagnosis::getSessionId, sessionId);
        DoctorFinalDiagnosis finalDiagnosis = diagnosisMapper.selectOne(wrapper);
        
        if (finalDiagnosis == null) {
            return ApiResponse.error("未找到该会话的最终诊断记录");
        }

        AiAggregatedReport report = confirmService.aggregateAssistReport(
                finalDiagnosis.getPatientId(), sessionId);
        String comparison = aiAnalysisService.compareDiagnosis(report, finalDiagnosis);
        return new ApiResponse<>(0, "success", comparison);
    }

    /**
     * 获取诊断合理性评估
     * 评估AI诊断的合理性，提供风险提示
     */
    @GetMapping("/ai/evaluate")
    public ApiResponse<String> evaluateDiagnosis(@RequestParam Integer patientId,
                                                  @RequestParam Integer sessionId) {
        AiAggregatedReport report = confirmService.aggregateAssistReport(patientId, sessionId);
        String evaluation = aiAnalysisService.evaluateDiagnosisReasonableness(report);
        return new ApiResponse<>(0, "success", evaluation);
    }
}
package com.neusoft.neu23.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.entity.AiSessionSummary;
import com.neusoft.neu23.service.SessionSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 会话总结控制器
 * 提供AI问诊总结生成接口
 */
@RestController
@RequestMapping("/api/ai/session-summary")
@RequiredArgsConstructor
public class SessionSummaryController {

    private final SessionSummaryService summaryService;

    /**
     * 生成会话总结
     * 支持 POST 和 GET 请求
     * POST /api/ai/session-summary/generate?patientId=1&sessionId=1
     * GET  /api/ai/session-summary/generate?patientId=1&sessionId=1
     *
     * @param patientId 患者ID（必需）
     * @param sessionId 会话ID（必需）
     * @return 生成的总结，包含summaryText和reasoningChain
     */
    @PostMapping("/generate")
    @GetMapping("/generate")
    public ApiResponse<AiSessionSummary> generateSummary(
            @RequestParam(value = "patientId", required = true) Integer patientId,
            @RequestParam(value = "sessionId", required = true) Integer sessionId) {
        
        // 参数验证
        if (patientId == null) {
            return ApiResponse.error("参数 patientId 不能为空");
        }
        if (sessionId == null) {
            return ApiResponse.error("参数 sessionId 不能为空");
        }
        
        return summaryService.generateSummary(patientId, sessionId);
    }

    /**
     * 生成会话总结并返回原始数据（供 doctor-confirm-service 使用）
     * POST /api/ai/session-summary/generate-with-data?patientId=1&sessionId=1
     *
     * @param patientId 患者ID（必需）
     * @param sessionId 会话ID（必需）
     * @return 包含原始数据（症状、诊断、风险、检查建议）和总结的扩展响应
     */
    @PostMapping("/generate-with-data")
    public com.assist.common.common.ApiResponse<com.assist.common.dto.response.SummaryWithDataResponse> generateSummaryWithData(
            @RequestParam(value = "patientId", required = true) Integer patientId,
            @RequestParam(value = "sessionId", required = true) Integer sessionId) {
        
        // 参数验证
        if (patientId == null) {
            return com.assist.common.common.ApiResponse.error("参数 patientId 不能为空");
        }
        if (sessionId == null) {
            return com.assist.common.common.ApiResponse.error("参数 sessionId 不能为空");
        }
        
        return summaryService.generateSummaryWithData(patientId, sessionId);
    }
}


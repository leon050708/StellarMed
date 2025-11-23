package com.neusoft.neu23.tc.client;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.response.SummaryWithDataResponse;
import com.assist.common.entity.AiSessionSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 总结AI服务
 * 接口文档：POST /api/ai/session-summary/generate
 * 注意：实际 Controller 使用 @RequestParam，所以这里也使用 @RequestParam
 */
@FeignClient(name = "summary-ai-service", path = "/api/ai/session-summary")
public interface SummaryAiClient {

    /**
     * AI 问诊总结生成（仅返回总结文本）
     * POST /api/ai/session-summary/generate?patientId=1&sessionId=1001
     */
    @PostMapping("/generate")
    ApiResponse<AiSessionSummary> generate(
            @RequestParam("patientId") Integer patientId,
            @RequestParam("sessionId") Integer sessionId);

    /**
     * AI 问诊总结生成并返回原始数据（包含症状、诊断、风险、检查建议、总结）
     * POST /api/ai/session-summary/generate-with-data?patientId=1&sessionId=1001
     * 用于 doctor-confirm-service，避免重复调用 2、3、4 服务
     */
    @PostMapping("/generate-with-data")
    ApiResponse<SummaryWithDataResponse> generateWithData(
            @RequestParam("patientId") Integer patientId,
            @RequestParam("sessionId") Integer sessionId);
}


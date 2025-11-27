package com.neusoft.neu23.tc.client;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.PrescriptionGenerateRequest;
import com.assist.common.dto.response.PrescriptionGenerateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 处方AI服务
 * 接口文档：
 * - POST /api/ai/prescriptions/generate (生成处方)
 * - GET /api/ai/prescriptions/query (查询处方，带Redis缓存)
 */
@FeignClient(name = "prescription-ai-service", path = "/api/ai")
public interface PrescriptionAiClient {

    /**
     * AI 处方建议（生成）
     * POST /api/ai/prescriptions/generate
     */
    @PostMapping("/prescriptions/generate")
    ApiResponse<PrescriptionGenerateResponse> generate(@RequestBody PrescriptionGenerateRequest request);

    /**
     * 查询处方建议（带Redis缓存）
     * GET /api/ai/prescriptions/query
     * 优先使用此方法，可以利用Redis缓存提升性能
     */
    @GetMapping("/prescriptions/query")
    ApiResponse<PrescriptionGenerateResponse> query(@RequestParam Integer patientId, @RequestParam Integer sessionId);
}


package com.neusoft.neu23.tc.client;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.PrescriptionGenerateRequest;
import com.assist.common.dto.response.PrescriptionGenerateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 处方AI服务
 * 接口文档：POST /api/ai/prescriptions/generate
 */
@FeignClient(name = "prescription-ai-service", path = "/api/ai/prescriptions")
public interface PrescriptionAiClient {

    /**
     * AI 处方建议
     * POST /api/ai/prescriptions/generate
     */
    @PostMapping("/generate")
    ApiResponse<PrescriptionGenerateResponse> generate(@RequestBody PrescriptionGenerateRequest request);
}


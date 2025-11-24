package com.neusoft.neu23.tc.client;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.SymptomExtractRequest;
import com.assist.common.dto.response.SymptomExtractResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 症状结构化AI服务 (服务2)
 * 接口文档：POST /api/ai/symptoms/extract
 */
@FeignClient(name = "symptom-ai-service", path = "/api/ai")
public interface SymptomAiClient {

    /**
     * AI 抽取结构化症状
     * POST /api/ai/symptoms/extract
     */
    @PostMapping("/symptoms/extract")
    ApiResponse<SymptomExtractResponse> extract(@RequestBody SymptomExtractRequest request);
}


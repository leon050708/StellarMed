package com.neusoft.neu23.tc.client;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.TestSuggestionRequest;
import com.assist.common.dto.response.TestSuggestionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 检查建议AI服务 (服务4)
 * 接口文档：POST /api/ai/test-suggestions/generate
 */
@FeignClient(name = "test-suggestion-ai-service", path = "/api/ai/test-suggestions")
public interface TestSuggestionAiClient {

    /**
     * AI 给出检查建议
     * POST /api/ai/test-suggestions/generate
     */
    @PostMapping("/generate")
    ApiResponse<TestSuggestionResponse> generate(@RequestBody TestSuggestionRequest request);
}


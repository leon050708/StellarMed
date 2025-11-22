package com.neusoft.neu23.feign;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.TestSuggestionRequest;
import com.assist.common.dto.response.TestSuggestionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Test Suggestion AI Service Feign 客户端接口
 * 供其他服务调用本服务的接口
 * 
 * @author StellarMed Team
 */
@FeignClient(name = "test-suggestion-ai-service", path = "/api/ai/test-suggestions")
public interface TestSuggestionFeignClient {
    
    /**
     * 生成检查建议
     * 
     * @param request 请求参数
     * @return 检查建议响应
     */
    @PostMapping("/generate")
    ApiResponse<TestSuggestionResponse> generateTestSuggestions(@RequestBody TestSuggestionRequest request);
    
    /**
     * 根据 sessionId 查询检查建议
     * 
     * @param sessionId 会话ID
     * @return 检查建议响应
     */
    @GetMapping("/session/{sessionId}")
    ApiResponse<TestSuggestionResponse> getTestSuggestionsBySessionId(@PathVariable Integer sessionId);
}


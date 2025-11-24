package com.assist.patient.client;

import com.assist.common.dto.request.AssistFlowTriggerRequest;
import com.assist.common.dto.response.TestSuggestionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 检查建议AI服务Feign客户端
 */
@FeignClient(name = "test-suggestion-ai-service", path = "/api/ai")
public interface TestSuggestionAiClient {
    
    @PostMapping("/tests/suggest")
    TestSuggestionResponse suggest(@RequestBody AssistFlowTriggerRequest request);
}


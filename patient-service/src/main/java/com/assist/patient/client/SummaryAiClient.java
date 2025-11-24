package com.assist.patient.client;

import com.assist.common.dto.request.AssistFlowTriggerRequest;
import com.assist.common.dto.response.SummaryGenerateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 总结AI服务Feign客户端
 */
@FeignClient(name = "summary-ai-service", path = "/api/ai")
public interface SummaryAiClient {
    
    @PostMapping("/session-summary/generate")
    SummaryGenerateResponse generate(@RequestBody AssistFlowTriggerRequest request);
}


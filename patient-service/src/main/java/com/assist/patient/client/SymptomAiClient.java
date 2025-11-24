package com.assist.patient.client;

import com.assist.common.dto.request.SymptomExtractRequest;
import com.assist.common.dto.response.SymptomExtractResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 症状AI服务Feign客户端
 */
@FeignClient(name = "symptom-ai-service", path = "/api/ai")
public interface SymptomAiClient {
    
    @PostMapping("/symptoms/extract")
    SymptomExtractResponse extract(@RequestBody SymptomExtractRequest request);
}


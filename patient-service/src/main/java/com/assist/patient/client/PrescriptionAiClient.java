package com.assist.patient.client;

import com.assist.common.dto.request.AssistFlowTriggerRequest;
import com.assist.common.dto.response.PrescriptionGenerateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 处方AI服务Feign客户端
 */
@FeignClient(name = "prescription-ai-service", path = "/api/ai")
public interface PrescriptionAiClient {
    
    @PostMapping("/prescriptions/generate")
    PrescriptionGenerateResponse generate(@RequestBody AssistFlowTriggerRequest request);
}


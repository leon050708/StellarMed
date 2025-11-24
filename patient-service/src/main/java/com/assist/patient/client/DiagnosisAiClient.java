package com.assist.patient.client;

import com.assist.common.dto.request.AssistFlowTriggerRequest;
import com.assist.common.dto.response.DiagnosisEvaluateResponse;
import com.assist.common.dto.response.RiskEvaluateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 诊断AI服务Feign客户端
 */
@FeignClient(name = "diagnosis-ai-service", path = "/api/ai")
public interface DiagnosisAiClient {
    
    @PostMapping("/diagnosis/evaluate")
    DiagnosisEvaluateResponse evaluate(@RequestBody AssistFlowTriggerRequest request);
    
    @PostMapping("/risk/evaluate")
    RiskEvaluateResponse evaluateRisk(@RequestBody AssistFlowTriggerRequest request);
}


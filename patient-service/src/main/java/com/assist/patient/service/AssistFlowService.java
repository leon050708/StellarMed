package com.assist.patient.service;

import com.assist.common.dto.request.AssistFlowTriggerRequest;
import com.assist.common.dto.response.AiAggregatedReport;

/**
 * AI助诊流程编排服务接口
 */
public interface AssistFlowService {
    /**
     * 一键触发完整AI助诊流程
     */
    AiAggregatedReport triggerFullFlow(AssistFlowTriggerRequest request);
}


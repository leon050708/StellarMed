package com.assist.symptom.service;

import com.assist.common.dto.request.SymptomExtractRequest;
import com.assist.common.dto.response.SymptomExtractResponse;

/**
 * 症状提取服务接口
 */
public interface SymptomExtractService {
    /**
     * 提取结构化症状
     */
    SymptomExtractResponse extractStructuredSymptoms(SymptomExtractRequest request);
}


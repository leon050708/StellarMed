package com.neusoft.neu23.service;

import com.assist.common.dto.request.TestSuggestionRequest;
import com.assist.common.dto.response.TestSuggestionResponse;

/**
 * AI 检查建议服务接口
 * 
 * @author StellarMed Team
 */
public interface TestSuggestionService {
    
    /**
     * 生成检查建议
     * 根据结构化症状、初步诊断和风险等级，通过 AI 生成检查建议
     * 
     * @param request 请求参数（patientId, sessionId）
     * @return 检查建议响应
     */
    TestSuggestionResponse generateTestSuggestions(TestSuggestionRequest request);
    
    /**
     * 根据 sessionId 查询检查建议
     * 
     * @param sessionId 会话ID
     * @return 检查建议响应
     */
    TestSuggestionResponse getTestSuggestionsBySessionId(Integer sessionId);
}


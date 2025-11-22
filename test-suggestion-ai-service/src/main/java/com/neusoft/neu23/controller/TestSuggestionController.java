package com.neusoft.neu23.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.TestSuggestionRequest;
import com.assist.common.dto.response.TestSuggestionResponse;
import com.neusoft.neu23.service.TestSuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * AI 检查建议 Controller
 * 提供检查建议生成和查询接口
 * 
 * @author StellarMed Team
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/test-suggestions")
public class TestSuggestionController {
    
    @Autowired
    private TestSuggestionService testSuggestionService;
    
    /**
     * 生成检查建议
     * 
     * @param request 请求参数
     * @return 检查建议响应
     */
    @PostMapping("/generate")
    public ApiResponse<TestSuggestionResponse> generateTestSuggestions(
            @RequestBody @Validated TestSuggestionRequest request) {
        
        log.info("🔬 收到生成检查建议请求: patientId={}, sessionId={}", 
                request.getPatientId(), request.getSessionId());
        
        try {
            TestSuggestionResponse response = testSuggestionService.generateTestSuggestions(request);
            
            if (response.getTestSuggestions() != null && !response.getTestSuggestions().isEmpty()) {
                log.info("✅ 检查建议生成成功，共 {} 条", response.getTestSuggestions().size());
                return ApiResponse.success(response);
            } else {
                log.warn("⚠️ 未生成任何检查建议");
                return ApiResponse.error(response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("❌ 生成检查建议失败", e);
            return ApiResponse.error("生成检查建议失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据 sessionId 查询检查建议
     * 
     * @param sessionId 会话ID
     * @return 检查建议响应
     */
    @GetMapping("/session/{sessionId}")
    public ApiResponse<TestSuggestionResponse> getTestSuggestionsBySessionId(
            @PathVariable Integer sessionId) {
        
        log.info("🔍 查询检查建议: sessionId={}", sessionId);
        
        try {
            TestSuggestionResponse response = testSuggestionService.getTestSuggestionsBySessionId(sessionId);
            log.info("✅ 查询成功，共 {} 条检查建议", 
                    response.getTestSuggestions() != null ? response.getTestSuggestions().size() : 0);
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("❌ 查询检查建议失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Test Suggestion AI Service is running!");
    }
}


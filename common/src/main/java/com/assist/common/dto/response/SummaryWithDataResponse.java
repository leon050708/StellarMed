package com.assist.common.dto.response;

import com.assist.common.entity.*;
import lombok.Data;
import java.util.List;

/**
 * 总结服务扩展响应
 * 包含原始数据（症状、诊断、风险、检查建议）和生成的总结
 * 供 doctor-confirm-service 使用，避免重复调用 2、3、4 服务
 */
@Data
public class SummaryWithDataResponse {
    /**
     * 结构化症状列表（来自服务 2：symptom-ai-service）
     */
    private List<AiSymptomStructured> symptoms;
    
    /**
     * 诊断列表（来自服务 3：diagnosis-ai-service）
     */
    private List<AiPreDiagnosis> diagnoses;
    
    /**
     * 风险评估（来自服务 3：diagnosis-ai-service）
     */
    private AiRiskAssessment riskAssessment;
    
    /**
     * 检查建议列表（来自服务 4：test-suggestion-ai-service）
     */
    private List<AiTestSuggestion> testSuggestions;
    
    /**
     * 生成的总结（包含 summaryText 和 reasoningChain）
     */
    private AiSessionSummary sessionSummary;
}


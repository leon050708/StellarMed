package com.neusoft.neu23.service;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.response.SummaryWithDataResponse;
import com.assist.common.entity.AiSessionSummary;

/**
 * 会话总结服务接口
 */
public interface SessionSummaryService {

    /**
     * 生成会话总结（仅返回总结文本）
     *
     * @param patientId 患者ID
     * @param sessionId 会话ID
     * @return 生成的总结
     */
    ApiResponse<AiSessionSummary> generateSummary(Integer patientId, Integer sessionId);

    /**
     * 生成会话总结并返回原始数据（供 doctor-confirm-service 使用）
     * 包含：症状、诊断、风险评估、检查建议、总结
     *
     * @param patientId 患者ID
     * @param sessionId 会话ID
     * @return 包含原始数据和总结的扩展响应
     */
    ApiResponse<SummaryWithDataResponse> generateSummaryWithData(Integer patientId, Integer sessionId);
}


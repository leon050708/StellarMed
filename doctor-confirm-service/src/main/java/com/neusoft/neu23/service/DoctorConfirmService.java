package com.neusoft.neu23.service;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.DoctorFinalConfirmRequest;
import com.assist.common.dto.response.AiAggregatedReport;

/**
 * 医生最终确认服务接口
 */
public interface DoctorConfirmService {

    /**
     * 聚合AI子服务产出的完整助诊报告
     *
     * @param patientId 患者ID
     * @param sessionId 会话ID
     * @return 合并后的AI报告
     */
    AiAggregatedReport aggregateAssistReport(Integer patientId, Integer sessionId);

    /**
     * 保存医生最终确认结果
     *
     * @param request 最终确认请求
     * @return 执行结果
     */
    ApiResponse<Void> saveFinalDiagnosis(DoctorFinalConfirmRequest request);
}


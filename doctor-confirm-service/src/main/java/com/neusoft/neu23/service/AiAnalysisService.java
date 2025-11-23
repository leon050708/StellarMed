package com.neusoft.neu23.service;

import com.assist.common.dto.response.AiAggregatedReport;
import com.assist.common.entity.DoctorFinalDiagnosis;

/**
 * AI分析服务接口
 * 提供特定场景的AI模型调用方法
 */
public interface AiAnalysisService {

    /**
     * 生成医生确认建议
     * 基于聚合的助诊报告，生成AI建议供医生参考
     *
     * @param report 聚合的助诊报告
     * @return AI生成的确认建议
     */
    String generateConfirmationSuggestion(AiAggregatedReport report);

    /**
     * 对比分析AI诊断和医生最终诊断
     * 分析差异点，提供参考意见
     *
     * @param report 聚合的助诊报告
     * @param finalDiagnosis 医生最终诊断
     * @return 对比分析结果
     */
    String compareDiagnosis(AiAggregatedReport report, DoctorFinalDiagnosis finalDiagnosis);

    /**
     * 生成诊断合理性评估
     * 评估AI诊断的合理性，提供风险提示
     *
     * @param report 聚合的助诊报告
     * @return 合理性评估结果
     */
    String evaluateDiagnosisReasonableness(AiAggregatedReport report);
}


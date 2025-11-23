package com.neusoft.neu23.service.impl;

import com.assist.common.common.ApiResponse;
import com.assist.common.common.BusinessException;
import com.assist.common.common.ErrorCode;
import com.assist.common.dto.request.DoctorFinalConfirmRequest;
import com.assist.common.dto.response.AiAggregatedReport;
import com.assist.common.entity.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.neusoft.neu23.mapper.*;
import com.neusoft.neu23.service.DoctorConfirmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 医生最终确认服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorConfirmServiceImpl implements DoctorConfirmService {

    private final DoctorFinalDiagnosisMapper diagnosisMapper;
    private final AiSymptomStructuredMapper symptomMapper;
    private final AiPreDiagnosisMapper preDiagnosisMapper;
    private final AiRiskAssessmentMapper riskAssessmentMapper;
    private final AiTestSuggestionMapper testSuggestionMapper;
    private final AiSessionSummaryMapper sessionSummaryMapper;
    private final AiPrescriptionMapper prescriptionMapper;

    @Override
    public AiAggregatedReport aggregateAssistReport(Integer patientId, Integer sessionId) {
        if (patientId == null || sessionId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "患者ID和会话ID不能为空");
        }

        AiAggregatedReport report = new AiAggregatedReport();
        
        // 注意：patient 和 session 信息暂不获取，因为 patient-service 尚未提供根据ID查询的接口
        // 如果后续需要，可以在 patient-service 中添加 GET /api/patients/{patientId} 和 GET /api/sessions/{sessionId} 接口
        report.setPatient(null);
        report.setSession(null);
        
        // 从数据库读取所有AI输出数据
        log.info("从数据库读取AI助诊数据，patientId={}, sessionId={}", patientId, sessionId);
        
        List<AiSymptomStructured> symptoms = safeList(symptomMapper.selectBySessionId(sessionId));
        log.debug("从数据库读取到 {} 条结构化症状", symptoms.size());
        report.setSymptoms(symptoms);
        
        List<AiPreDiagnosis> diagnoses = safeList(preDiagnosisMapper.selectBySessionId(sessionId));
        log.debug("从数据库读取到 {} 条初步诊断", diagnoses.size());
        report.setDiagnoses(diagnoses);
        
        AiRiskAssessment riskAssessment = riskAssessmentMapper.selectBySessionId(sessionId);
        log.debug("从数据库读取风险评估: {}", riskAssessment != null ? "有数据" : "无数据");
        report.setRiskAssessment(riskAssessment);
        
        List<AiTestSuggestion> testSuggestions = safeList(testSuggestionMapper.selectBySessionId(sessionId));
        log.debug("从数据库读取到 {} 条检查建议", testSuggestions.size());
        report.setTestSuggestions(testSuggestions);
        
        AiSessionSummary sessionSummary = sessionSummaryMapper.selectBySessionId(sessionId);
        log.debug("从数据库读取会话总结: {}", sessionSummary != null ? "有数据" : "无数据");
        report.setSessionSummary(sessionSummary);
        
        List<AiPrescription> prescriptions = safeList(prescriptionMapper.selectBySessionId(sessionId));
        log.debug("从数据库读取到 {} 条处方建议", prescriptions.size());
        report.setPrescriptions(prescriptions);
        
        log.info("AI助诊数据聚合完成，sessionId={}", sessionId);
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> saveFinalDiagnosis(DoctorFinalConfirmRequest request) {
        if (request == null
                || request.getPatientId() == null
                || request.getSessionId() == null
                || request.getDoctorId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "医生确认请求参数不完整");
        }

        DoctorFinalDiagnosis entity = new DoctorFinalDiagnosis();
        entity.setPatientId(request.getPatientId());
        entity.setSessionId(request.getSessionId());
        entity.setDoctorId(request.getDoctorId());
        entity.setFinalDiagnosis(request.getFinalDiagnosis());
        entity.setFinalPrescription(request.getFinalPrescription());
        entity.setComment(request.getComment());
        entity.setCreatedTime(new Date());

        LambdaQueryWrapper<DoctorFinalDiagnosis> wrapper = Wrappers.lambdaQuery(DoctorFinalDiagnosis.class)
                .eq(DoctorFinalDiagnosis::getSessionId, request.getSessionId());
        DoctorFinalDiagnosis existing = diagnosisMapper.selectOne(wrapper);
        if (existing == null) {
            diagnosisMapper.insert(entity);
            log.info("医生最终确认结果已写入 sessionId={}", request.getSessionId());
        } else {
            entity.setId(existing.getId());
            diagnosisMapper.updateById(entity);
            log.info("医生最终确认结果已更新 sessionId={}", request.getSessionId());
        }
        return ApiResponse.successMsg("医生最终确认保存成功");
    }

    private <T> List<T> safeList(List<T> data) {
        return data == null ? List.of() : data;
    }
}


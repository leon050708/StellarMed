package com.neusoft.neu23.service.impl;

import com.assist.common.common.ApiResponse;
import com.assist.common.common.BusinessException;
import com.assist.common.common.ErrorCode;
import com.assist.common.dto.request.*;
import com.assist.common.dto.response.*;
import com.assist.common.entity.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.neusoft.neu23.mapper.*;
import com.neusoft.neu23.service.DoctorConfirmService;
import com.neusoft.neu23.tc.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * 医生最终确认服务实现
 * 按照23456的顺序调用对应的服务，然后汇总发给医生确认
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorConfirmServiceImpl implements DoctorConfirmService {

    private final DoctorFinalDiagnosisMapper diagnosisMapper;

    // Feign客户端 - 按照23456顺序调用服务
    private final SymptomAiClient symptomAiClient;           // 服务2
    private final DiagnosisAiClient diagnosisAiClient;      // 服务3
    private final TestSuggestionAiClient testSuggestionAiClient; // 服务4
    private final SummaryAiClient summaryAiClient;            // 服务5
    private final PrescriptionAiClient prescriptionAiClient; // 服务6

    @Override
    public AiAggregatedReport aggregateAssistReport(Integer patientId, Integer sessionId) {
        if (patientId == null || sessionId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "患者ID和会话ID不能为空");
        }

        log.info("开始聚合AI助诊报告，按照23456顺序调用服务，patientId={}, sessionId={}", patientId, sessionId);
        
        AiAggregatedReport report = new AiAggregatedReport();
        report.setPatient(null);
        report.setSession(null);

        try {
            // 步骤2: 调用 symptom-ai-service - 结构化症状
            log.info("步骤2: 调用症状结构化服务");
            SymptomExtractRequest symptomRequest = new SymptomExtractRequest();
            symptomRequest.setPatientId(patientId);
            symptomRequest.setSessionId(sessionId);
            ApiResponse<SymptomExtractResponse> symptomResponse = safeFeignCall("symptom-ai-service",
                    () -> symptomAiClient.extract(symptomRequest));
            if (isSuccess(symptomResponse)) {
                List<AiSymptomStructured> structuredSymptoms = symptomResponse.getData().getStructuredSymptoms();
                report.setSymptoms(structuredSymptoms != null ? structuredSymptoms : List.of());
                log.info("症状结构化完成，共 {} 条", report.getSymptoms().size());
            } else {
                log.warn("症状结构化服务调用失败: {}", errorMsg(symptomResponse));
                report.setSymptoms(List.of());
            }

            // 步骤3: 调用 diagnosis-ai-service - 初步诊断
            log.info("步骤3: 调用初步诊断服务");
            DiagnosisEvaluateRequest diagnosisRequest = new DiagnosisEvaluateRequest();
            diagnosisRequest.setPatientId(patientId);
            diagnosisRequest.setSessionId(sessionId);
            ApiResponse<DiagnosisEvaluateResponse> diagnosisResponse = safeFeignCall("diagnosis-ai-service#evaluate",
                    () -> diagnosisAiClient.evaluateDiagnosis(diagnosisRequest));
            if (isSuccess(diagnosisResponse)) {
                List<AiPreDiagnosis> diagnoses = diagnosisResponse.getData().getDiagnoses();
                report.setDiagnoses(diagnoses != null ? diagnoses : List.of());
                log.info("初步诊断完成，共 {} 条", report.getDiagnoses().size());
            } else {
                log.warn("初步诊断服务调用失败: {}", errorMsg(diagnosisResponse));
                report.setDiagnoses(List.of());
            }

            // 步骤3: 调用 diagnosis-ai-service - 风险评估
            log.info("步骤3: 调用风险评估服务");
            RiskEvaluateRequest riskRequest = new RiskEvaluateRequest();
            riskRequest.setPatientId(patientId);
            riskRequest.setSessionId(sessionId);
            ApiResponse<RiskEvaluateResponse> riskResponse = safeFeignCall("diagnosis-ai-service#risk",
                    () -> diagnosisAiClient.evaluateRisk(riskRequest));
            if (isSuccess(riskResponse)) {
                report.setRiskAssessment(riskResponse.getData().getRiskAssessment());
                log.info("风险评估完成: {}", report.getRiskAssessment() != null ? "有数据" : "无数据");
            } else {
                log.warn("风险评估服务调用失败: {}", errorMsg(riskResponse));
                report.setRiskAssessment(null);
            }

            // 步骤4: 调用 test-suggestion-ai-service - 检查建议
            log.info("步骤4: 调用检查建议服务");
            TestSuggestionRequest testRequest = new TestSuggestionRequest();
            testRequest.setPatientId(patientId);
            testRequest.setSessionId(sessionId);
            ApiResponse<TestSuggestionResponse> testResponse = safeFeignCall("test-suggestion-ai-service",
                    () -> testSuggestionAiClient.generate(testRequest));
            if (isSuccess(testResponse)) {
                List<AiTestSuggestion> testSuggestions = testResponse.getData().getTestSuggestions();
                report.setTestSuggestions(testSuggestions != null ? testSuggestions : List.of());
                log.info("检查建议完成，共 {} 条", report.getTestSuggestions().size());
            } else {
                log.warn("检查建议服务调用失败: {}", errorMsg(testResponse));
                report.setTestSuggestions(List.of());
            }

            // 步骤5: 调用 summary-ai-service - 问诊总结
            log.info("步骤5: 调用问诊总结服务");
            ApiResponse<SummaryWithDataResponse> summaryResponse = safeFeignCall("summary-ai-service",
                    () -> summaryAiClient.generateWithData(patientId, sessionId));
            if (isSuccess(summaryResponse)) {
                report.setSessionSummary(summaryResponse.getData().getSessionSummary());
                log.info("问诊总结完成: {}", report.getSessionSummary() != null ? "有数据" : "无数据");
            } else {
                log.warn("问诊总结服务调用失败: {}", errorMsg(summaryResponse));
                report.setSessionSummary(null);
            }

            // 步骤6: 调用 prescription-ai-service - 处方建议
            log.info("步骤6: 调用处方建议服务");
            PrescriptionGenerateRequest prescriptionRequest = new PrescriptionGenerateRequest();
            prescriptionRequest.setPatientId(patientId);
            prescriptionRequest.setSessionId(sessionId);
            ApiResponse<PrescriptionGenerateResponse> prescriptionResponse = safeFeignCall("prescription-ai-service",
                    () -> prescriptionAiClient.generate(prescriptionRequest));
            if (isSuccess(prescriptionResponse)) {
                List<AiPrescription> prescriptions = prescriptionResponse.getData().getPrescriptions();
                report.setPrescriptions(prescriptions != null ? prescriptions : List.of());
                log.info("处方建议完成，共 {} 条", report.getPrescriptions().size());
            } else {
                log.warn("处方建议服务调用失败: {}", errorMsg(prescriptionResponse));
                report.setPrescriptions(List.of());
            }

            log.info("AI助诊报告聚合完成，sessionId={}", sessionId);
            return report;

        } catch (Exception e) {
            log.error("聚合AI助诊报告失败，patientId={}, sessionId={}", patientId, sessionId, e);
            throw new BusinessException(ErrorCode.SERVICE_ERROR, "聚合AI助诊报告失败: " + e.getMessage());
        }
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

    private <T> ApiResponse<T> safeFeignCall(String serviceName, Supplier<ApiResponse<T>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("{} 调用异常: {}", serviceName, e.getMessage());
            return null;
        }
    }

    private boolean isSuccess(ApiResponse<?> response) {
        return response != null && response.getCode() == 0 && response.getData() != null;
    }

    private String errorMsg(ApiResponse<?> response) {
        if (response == null) {
            return "请求失败（无响应，可能是服务未启动或网关未转发）";
        }
        return response.getMsg() != null ? response.getMsg() : "服务返回空数据";
    }
}


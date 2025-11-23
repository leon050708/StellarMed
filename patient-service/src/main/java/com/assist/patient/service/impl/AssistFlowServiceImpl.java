package com.assist.patient.service.impl;

import com.assist.common.dto.request.AssistFlowTriggerRequest;
import com.assist.common.dto.request.SymptomExtractRequest;
import com.assist.common.dto.response.AiAggregatedReport;
import com.assist.common.entity.*;
import com.assist.patient.client.*;
import com.assist.patient.mapper.PatientMapper;
import com.assist.patient.mapper.SessionMapper;
import com.assist.patient.service.AssistFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI助诊流程编排服务实现
 * 核心编排逻辑：顺序调用所有AI服务并聚合结果
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssistFlowServiceImpl implements AssistFlowService {

    private final PatientMapper patientMapper;
    private final SessionMapper sessionMapper;
    
    // Feign客户端 - 调用各个AI服务
    private final SymptomAiClient symptomAiClient;
    private final DiagnosisAiClient diagnosisAiClient;
    private final TestSuggestionAiClient testSuggestionAiClient;
    private final SummaryAiClient summaryAiClient;
    private final PrescriptionAiClient prescriptionAiClient;

    @Override
    public AiAggregatedReport triggerFullFlow(AssistFlowTriggerRequest request) {
        log.info("开始执行完整AI助诊流程，patientId: {}, sessionId: {}", 
                request.getPatientId(), request.getSessionId());

        // 1. 获取基础信息
        Patient patient = patientMapper.selectById(request.getPatientId());
        Session session = sessionMapper.selectById(request.getSessionId());

        // 2. 调用 symptom-ai-service - 结构化症状
        log.info("步骤1: 调用症状结构化服务");
        SymptomExtractRequest symptomRequest = new SymptomExtractRequest();
        symptomRequest.setPatientId(request.getPatientId());
        symptomRequest.setSessionId(request.getSessionId());
        var symptomResponse = symptomAiClient.extract(symptomRequest);
        List<AiSymptomStructured> structuredSymptoms = symptomResponse.getStructuredSymptoms();

        // 3. 调用 diagnosis-ai-service - 初步诊断
        log.info("步骤2: 调用初步诊断服务");
        var diagnosisResponse = diagnosisAiClient.evaluate(request);
        List<AiPreDiagnosis> diagnoses = diagnosisResponse.getDiagnoses();

        // 4. 调用 diagnosis-ai-service - 风险评估
        log.info("步骤3: 调用风险评估服务");
        var riskResponse = diagnosisAiClient.evaluateRisk(request);
        AiRiskAssessment riskAssessment = riskResponse.getRiskAssessment();

        // 5. 调用 test-suggestion-ai-service - 检查建议
        log.info("步骤4: 调用检查建议服务");
        var testResponse = testSuggestionAiClient.suggest(request);
        List<AiTestSuggestion> testSuggestions = testResponse.getTestSuggestions();

        // 6. 调用 summary-ai-service - 问诊总结
        log.info("步骤5: 调用问诊总结服务");
        var summaryResponse = summaryAiClient.generate(request);
        AiSessionSummary sessionSummary = summaryResponse.getSummary();

        // 7. 调用 prescription-ai-service - 处方建议
        log.info("步骤6: 调用处方建议服务");
        var prescriptionResponse = prescriptionAiClient.generate(request);
        List<AiPrescription> prescriptions = prescriptionResponse.getPrescriptions();

        // 8. 聚合所有结果
        AiAggregatedReport report = new AiAggregatedReport();
        report.setPatient(patient);
        report.setSession(session);
        report.setSymptoms(structuredSymptoms);
        report.setDiagnoses(diagnoses);
        report.setRiskAssessment(riskAssessment);
        report.setTestSuggestions(testSuggestions);
        report.setSessionSummary(sessionSummary);
        report.setPrescriptions(prescriptions);

        log.info("AI助诊流程完成，sessionId: {}", request.getSessionId());
        return report;
    }
}


package com.assist.patient.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.AssistFlowTriggerRequest;
import com.assist.common.dto.response.AiAggregatedReport;
import com.assist.common.entity.Patient;
import com.assist.patient.service.PatientService;
import com.assist.patient.service.SessionService;
import com.assist.patient.service.ChatService;
import com.assist.patient.service.SymptomService;
import com.assist.patient.service.AssistFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 患者与会话控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final SessionService sessionService;
    private final ChatService chatService;
    private final SymptomService symptomService;
    private final AssistFlowService assistFlowService;

    /**
     * 创建或更新患者
     */
    @PostMapping("/patients")
    public ApiResponse<Integer> createOrUpdatePatient(@RequestBody Patient patient) {
        Integer patientId = patientService.createOrUpdate(patient);
        return ApiResponse.success(patientId);
    }

    /**
     * 根据病例号查询患者
     */
    @GetMapping("/patients/by-case-number")
    public ApiResponse<Patient> getPatientByCaseNumber(@RequestParam String caseNumber) {
        Patient patient = patientService.getByCaseNumber(caseNumber);
        return ApiResponse.success(patient);
    }

    /**
     * 创建会话
     */
    @PostMapping("/sessions")
    public ApiResponse<com.assist.common.entity.Session> createSession(@RequestParam Integer patientId) {
        com.assist.common.entity.Session session = sessionService.createSession(patientId);
        return ApiResponse.success(session);
    }

    /**
     * 关闭会话
     */
    @PatchMapping("/sessions/{sessionId}/close")
    public ApiResponse<Void> closeSession(@PathVariable Integer sessionId) {
        sessionService.closeSession(sessionId);
        return ApiResponse.success(null);
    }

    /**
     * 保存聊天记录
     */
    @PostMapping("/chats")
    public ApiResponse<Integer> saveChat(@RequestBody com.assist.common.entity.ChatRecord chatRecord) {
        Integer chatId = chatService.saveChat(chatRecord);
        return ApiResponse.success(chatId);
    }

    /**
     * AI对话：与患者进行实时对话
     * @param sessionId 会话ID
     * @param patientId 患者ID
     * @param question 患者问题
     * @return AI回复
     */
    @PostMapping("/chat")
    public ApiResponse<String> chatWithAi(
            @RequestParam Integer sessionId,
            @RequestParam Integer patientId,
            @RequestParam String question) {
        String reply = chatService.chatWithAi(sessionId, patientId, question);
        return ApiResponse.success(reply);
    }

    /**
     * 获取对话历史
     * @param sessionId 会话ID
     * @return 对话记录列表
     */
    @GetMapping("/chat/history/{sessionId}")
    public ApiResponse<java.util.List<com.assist.common.entity.ChatRecord>> getChatHistory(
            @PathVariable Integer sessionId) {
        java.util.List<com.assist.common.entity.ChatRecord> history = chatService.getChatHistory(sessionId);
        return ApiResponse.success(history);
    }

    /**
     * 记录原始症状
     */
    @PostMapping("/symptoms")
    public ApiResponse<Integer> recordSymptom(@RequestBody com.assist.common.entity.SymptomRecord symptomRecord) {
        Integer symptomId = symptomService.saveSymptom(symptomRecord);
        return ApiResponse.success(symptomId);
    }

    /**
     * 一键触发整体AI助诊流程（核心接口）
     */
    @PostMapping("/assist/full")
    public ApiResponse<AiAggregatedReport> triggerFullAssistFlow(@RequestBody AssistFlowTriggerRequest request) {
        AiAggregatedReport report = assistFlowService.triggerFullFlow(request);
        return ApiResponse.success(report);
    }
}


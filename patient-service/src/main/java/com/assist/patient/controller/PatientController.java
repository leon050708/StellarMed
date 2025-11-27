package com.assist.patient.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.entity.Patient;
import com.assist.patient.service.PatientService;
import com.assist.patient.service.SessionService;
import com.assist.patient.service.ChatService;
import com.assist.patient.service.SymptomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建或更新患者
     */
    @PostMapping("/patients")
    public ApiResponse<Integer> createOrUpdatePatient(@RequestBody Patient patient) {
        Integer patientId = patientService.createOrUpdate(patient);
        return ApiResponse.success(patientId);
    }

    /**
     * 获取所有患者列表
     */
    @GetMapping("/patients")
    public ApiResponse<java.util.List<Patient>> getAllPatients() {
        java.util.List<Patient> patients = patientService.getAllPatients();
        return ApiResponse.success(patients);
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
     * 获取会话列表
     * 支持按患者ID查询，如果不传patientId则返回所有会话
     */
    @GetMapping("/sessions")
    public ApiResponse<java.util.List<com.assist.common.entity.Session>> getSessions(
            @RequestParam(required = false) Integer patientId) {
        if (patientId != null) {
            java.util.List<com.assist.common.entity.Session> sessions = sessionService.getSessionsByPatientId(patientId);
            return ApiResponse.success(sessions);
        } else {
            java.util.List<com.assist.common.entity.Session> sessions = sessionService.getAllSessions();
            return ApiResponse.success(sessions);
        }
    }

    /**
     * 根据会话ID获取会话详情
     */
    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<com.assist.common.entity.Session> getSessionById(@PathVariable Integer sessionId) {
        com.assist.common.entity.Session session = sessionService.getSessionById(sessionId);
        if (session == null) {
            return ApiResponse.error("会话不存在");
        }
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
     * Redis 测试接口 - 存储数据
     * 支持两种方式传参：
     * 1. Query Parameters: ?key=xxx&value=xxx
     * 2. Request Body (JSON): {"key":"xxx","value":"xxx"}
     */
    @PostMapping("/redis/test/set")
    public ApiResponse<String> redisTestSet(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String value,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        // 优先使用 Request Body，如果没有则使用 Query Parameters
        String finalKey = (body != null && body.containsKey("key")) ? body.get("key") : key;
        String finalValue = (body != null && body.containsKey("value")) ? body.get("value") : value;
        
        if (finalKey == null || finalValue == null) {
            return ApiResponse.error("参数错误：请提供 key 和 value 参数（可通过 Query Parameters 或 Request Body 传递）");
        }
        
        redisTemplate.opsForValue().set(finalKey, finalValue);
        return ApiResponse.success("数据已存储到 Redis: " + finalKey + " = " + finalValue);
    }

    /**
     * Redis 测试接口 - 获取数据
     */
    @GetMapping("/redis/test/get")
    public ApiResponse<Object> redisTestGet(@RequestParam String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return ApiResponse.error("Key 不存在: " + key);
        }
        return ApiResponse.success(value);
    }

    /**
     * Redis 测试接口 - 查看所有键
     */
    @GetMapping("/redis/test/keys")
    public ApiResponse<java.util.Set<String>> redisTestKeys(@RequestParam(required = false, defaultValue = "*") String pattern) {
        java.util.Set<String> keys = redisTemplate.keys(pattern);
        return ApiResponse.success(keys);
    }

}


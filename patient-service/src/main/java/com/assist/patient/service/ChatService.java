package com.assist.patient.service;

import com.assist.common.entity.ChatRecord;

/**
 * 聊天记录服务接口
 */
public interface ChatService {
    /**
     * 保存聊天记录
     */
    Integer saveChat(ChatRecord chatRecord);
    
    /**
     * AI对话：接收患者问题，返回AI回复，并保存对话记录
     * @param sessionId 会话ID
     * @param patientId 患者ID
     * @param question 患者问题
     * @return AI回复
     */
    String chatWithAi(Integer sessionId, Integer patientId, String question);
    
    /**
     * 获取会话的对话历史
     * @param sessionId 会话ID
     * @return 对话记录列表
     */
    java.util.List<ChatRecord> getChatHistory(Integer sessionId);
}


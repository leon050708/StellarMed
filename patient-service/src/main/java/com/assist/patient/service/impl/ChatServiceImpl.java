package com.assist.patient.service.impl;

import com.assist.common.entity.ChatRecord;
import com.assist.patient.mapper.ChatRecordMapper;
import com.assist.patient.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 聊天记录服务实现
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRecordMapper chatRecordMapper;
    private final ChatClient chatChatClient;

    public ChatServiceImpl(
            ChatRecordMapper chatRecordMapper,
            @Qualifier("chatChatClient") ChatClient chatChatClient) {
        this.chatRecordMapper = chatRecordMapper;
        this.chatChatClient = chatChatClient;
    }

    @Override
    public Integer saveChat(ChatRecord chatRecord) {
        chatRecord.setTimestamp(new Date());
        chatRecordMapper.insert(chatRecord);
        return chatRecord.getChatId();
    }

    @Override
    public String chatWithAi(Integer sessionId, Integer patientId, String question) {
        log.info("开始AI对话，sessionId: {}, patientId: {}, question: {}", sessionId, patientId, question);
        
        // 1. 获取对话历史
        List<ChatRecord> chatHistory = chatRecordMapper.selectBySessionId(sessionId);
        
        // 2. 构建上下文（包含历史对话）
        StringBuilder context = new StringBuilder();
        if (!chatHistory.isEmpty()) {
            context.append("【对话历史】\n");
            for (ChatRecord chat : chatHistory) {
                context.append("患者: ").append(chat.getQuestion()).append("\n");
                if (chat.getAiReply() != null) {
                    context.append("医生: ").append(chat.getAiReply()).append("\n");
                }
            }
            context.append("\n");
        }
        context.append("患者: ").append(question).append("\n");
        context.append("医生: ");
        
        // 3. 调用AI获取回复
        String conversationId = "chat-" + sessionId;
        String aiReply = chatChatClient.prompt()
                .user(context.toString())
                .advisors(a -> a.param("conversationId", conversationId))
                .call()
                .content();
        
        log.info("AI回复: {}", aiReply);
        
        // 4. 保存对话记录
        ChatRecord chatRecord = new ChatRecord();
        chatRecord.setSessionId(sessionId);
        chatRecord.setPatientId(patientId);
        chatRecord.setQuestion(question);
        chatRecord.setAiReply(aiReply);
        chatRecord.setTimestamp(new Date());
        chatRecordMapper.insert(chatRecord);
        
        return aiReply;
    }

    @Override
    public List<ChatRecord> getChatHistory(Integer sessionId) {
        return chatRecordMapper.selectBySessionId(sessionId);
    }
}


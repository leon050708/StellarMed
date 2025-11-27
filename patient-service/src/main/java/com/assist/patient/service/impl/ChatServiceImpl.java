package com.assist.patient.service.impl;

import com.assist.common.entity.ChatRecord;
import com.assist.common.entity.Session;
import com.assist.patient.mapper.ChatRecordMapper;
import com.assist.patient.mapper.SessionMapper;
import com.assist.patient.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 聊天记录服务实现
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRecordMapper chatRecordMapper;
    private final SessionMapper sessionMapper;
    private final ChatClient chatChatClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public ChatServiceImpl(
            ChatRecordMapper chatRecordMapper,
            SessionMapper sessionMapper,
            @Qualifier("chatChatClient") ChatClient chatChatClient,
            RedisTemplate<String, Object> redisTemplate) {
        this.chatRecordMapper = chatRecordMapper;
        this.sessionMapper = sessionMapper;
        this.chatChatClient = chatChatClient;
        this.redisTemplate = redisTemplate;
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
        
        // 0. 验证 sessionId 是否存在
        if (sessionId == null) {
            log.error("sessionId 不能为空");
            throw new IllegalArgumentException("sessionId 不能为空");
        }
        
        // 验证 sessionId 是否存在于数据库中
        Session session = sessionMapper.selectById(sessionId);
        if (session == null) {
            log.error("sessionId {} 不存在于数据库中，无法创建聊天记录", sessionId);
            throw new IllegalArgumentException("会话不存在，请先创建会话");
        }
        log.debug("验证通过：sessionId {} 存在，patientId: {}", sessionId, session.getPatientId());
        
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
        
        // 5. 清除对话历史缓存（因为新增了对话记录）
        if (redisTemplate != null) {
            try {
                String cacheKey = "chat:history:" + sessionId;
                redisTemplate.delete(cacheKey);
                log.info("已清除对话历史缓存，sessionId: {}", sessionId);
            } catch (Exception e) {
                log.warn("清除缓存失败: {}", e.getMessage());
            }
        }
        
        return aiReply;
    }

    @Override
    public List<ChatRecord> getChatHistory(Integer sessionId) {
        // Redis 缓存键：chat:history:会话ID
        String cacheKey = "chat:history:" + sessionId;
        
        // 1. 先查 Redis 缓存
        if (redisTemplate != null) {
            try {
                @SuppressWarnings("unchecked")
                List<ChatRecord> cached = (List<ChatRecord>) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.info("从 Redis 缓存获取对话历史，sessionId: {}, 记录数量: {}", sessionId, cached.size());
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Redis 缓存读取失败，继续查询数据库: {}", e.getMessage());
            }
        }
        
        // 2. 缓存没有，查询数据库
        log.info("从数据库查询对话历史，sessionId: {}", sessionId);
        List<ChatRecord> history = chatRecordMapper.selectBySessionId(sessionId);
        
        // 3. 将查询结果存入 Redis 缓存（30 分钟过期）
        if (redisTemplate != null && history != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, history, 30, TimeUnit.MINUTES);
                log.info("对话历史已存入 Redis 缓存，sessionId: {}, 记录数量: {}", sessionId, history.size());
            } catch (Exception e) {
                log.warn("Redis 缓存写入失败: {}", e.getMessage());
            }
        }
        
        return history;
    }
}


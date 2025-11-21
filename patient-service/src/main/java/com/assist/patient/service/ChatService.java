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
}


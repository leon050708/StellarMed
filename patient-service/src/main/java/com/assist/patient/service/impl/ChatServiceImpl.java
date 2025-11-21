package com.assist.patient.service.impl;

import com.assist.common.entity.ChatRecord;
import com.assist.patient.mapper.ChatRecordMapper;
import com.assist.patient.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 聊天记录服务实现
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRecordMapper chatRecordMapper;

    @Override
    public Integer saveChat(ChatRecord chatRecord) {
        chatRecord.setTimestamp(new Date());
        chatRecordMapper.insert(chatRecord);
        return chatRecord.getChatId();
    }
}


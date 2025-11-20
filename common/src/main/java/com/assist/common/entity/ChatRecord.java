package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * 聊天记录实体类
 */
@Data
public class ChatRecord {
    private Integer chatId;
    private Integer patientId;
    private Integer sessionId;
    private Date timestamp;
    private String question;
    private String aiReply;
}


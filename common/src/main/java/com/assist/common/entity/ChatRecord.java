package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 聊天记录实体类
 */
@Data
@TableName("chat_record")
public class ChatRecord {
    @TableId(value = "chat_id", type = IdType.AUTO)
    private Integer chatId;
    private Integer patientId;
    private Integer sessionId;
    private Date timestamp;
    private String question;
    private String aiReply;
}


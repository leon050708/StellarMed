package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * 消息队列日志实体类
 */
@Data
public class MessageQueueLog {
    private Long messageId;
    private String eventType;
    private String payload;
    private String status;
    private Date createdTime;
}


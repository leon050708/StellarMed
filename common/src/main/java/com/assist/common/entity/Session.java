package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * 会话实体类（助诊 session）
 */
@Data
public class Session {
    private Integer sessionId;
    private Integer patientId;
    private Date createdTime;
    private String status;
}


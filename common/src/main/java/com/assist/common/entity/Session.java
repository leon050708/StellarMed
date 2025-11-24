package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 会话实体类（助诊 session）
 */
@Data
@TableName("session")
public class Session {
    @TableId(value = "session_id", type = IdType.AUTO)
    private Integer sessionId;
    private Integer patientId;
    private Date createdTime;
    private String status;
}


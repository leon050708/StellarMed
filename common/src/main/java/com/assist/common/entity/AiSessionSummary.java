package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * AI 会话总结实体类
 */
@Data
@TableName("ai_session_summary")
public class AiSessionSummary {
    @TableId(value = "summary_id", type = IdType.AUTO)
    private Integer summaryId;
    private Integer patientId;
    private Integer sessionId;
    private String summaryText;
    private String reasoningChain;
    private Date createdTime;
}


package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * AI 检查建议实体类
 */
@Data
@TableName("ai_test_suggestion")
public class AiTestSuggestion {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private String testName;
    private String reason;
    private Date createdTime;
}


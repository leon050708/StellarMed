package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * AI 风险评估实体类
 */
@Data
@TableName("ai_risk_assessment")
public class AiRiskAssessment {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private String riskLevel;
    private String reason;
    private Date createdTime;
}


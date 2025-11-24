package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * AI 处方建议实体类
 */
@Data
@TableName("ai_prescription")
public class AiPrescription {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private String drugName;
    private String dosage;
    private String duration;
    private String usageInstruction;
    private String reason;
    private Date createTime;
}


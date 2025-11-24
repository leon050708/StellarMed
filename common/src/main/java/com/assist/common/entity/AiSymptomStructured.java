package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * AI 结构化症状实体类
 */
@Data
@TableName("ai_symptom_structured")
public class AiSymptomStructured {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private String symptomName;
    private String severity;
    private String duration;
    private String extraInfo;
    private Date createTime;
}


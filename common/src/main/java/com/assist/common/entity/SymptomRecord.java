package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 原始症状记录实体类（用户输入）
 */
@Data
@TableName("symptom_record")
public class SymptomRecord {
    @TableId(value = "symptom_id", type = IdType.AUTO)
    private Integer symptomId;
    private Integer patientId;
    private Integer sessionId;
    private String symptomText;
    private String severity;
    private String duration;
    private Date extractedTime;
}


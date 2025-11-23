package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * AI 初步诊断实体类
 */
@Data
public class AiPreDiagnosis {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private String diagnosis;
    private BigDecimal probability;
    private String reasoning;
    private Date createTime;
}


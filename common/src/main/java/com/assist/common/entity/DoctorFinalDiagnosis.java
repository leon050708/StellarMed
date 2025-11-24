package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 医生最终确认诊断实体类
 * 对应数据库表：doctor_final_diagnosis
 */
@Data
@TableName("doctor_final_diagnosis")
public class DoctorFinalDiagnosis {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer patientId;
    private Integer sessionId;
    private Integer doctorId;
    private String finalDiagnosis;
    private String finalPrescription;
    private String comment;
    private Date createdTime;
}

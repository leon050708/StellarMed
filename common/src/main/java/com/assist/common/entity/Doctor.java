package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 医生实体类
 */
@Data
@TableName("doctor")
public class Doctor {
    private Integer doctorId;
    private String name;
    private String department;
    private String title;
    private String phone;
}


package com.assist.common.entity;

import lombok.Data;

/**
 * 医生实体类
 */
@Data
public class Doctor {
    private Integer doctorId;
    private String name;
    private String department;
    private String title;
    private String phone;
}


package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * 患者实体类
 */
@Data
public class Patient {
    private Integer id;
    private String name;
    private String age;
    private String gender;
    private String phoneNumber;
    private String idCard;
    private String height;
    private String weight;
    private String caseNumber;
    private Date createTime;
}


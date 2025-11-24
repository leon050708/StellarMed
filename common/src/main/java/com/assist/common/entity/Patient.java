package com.assist.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 患者实体类
 */
@Data
@TableName("patient")
public class Patient {
    @TableId(value = "id", type = IdType.AUTO)
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


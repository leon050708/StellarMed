package com.assist.common.entity;

import lombok.Data;
import java.util.Date;

/**
 * 挂号实体类
 */
@Data
public class Appointment {
    private Integer appointmentId;
    private Integer patientId;
    private Integer doctorId;
    private Date appointmentTime;
    private String status;
}


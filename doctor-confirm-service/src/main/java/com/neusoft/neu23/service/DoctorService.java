package com.neusoft.neu23.service;

import com.assist.common.entity.Doctor;
import com.assist.common.entity.Patient;

import java.util.List;

/**
 * 医生服务接口
 */
public interface DoctorService {
    /**
     * 医生登录：根据姓名和电话验证医生身份
     */
    Doctor login(String name, String phone);
    
    /**
     * 根据医生ID查询该医生的患者列表
     */
    List<Patient> getPatientsByDoctorId(Integer doctorId);
}





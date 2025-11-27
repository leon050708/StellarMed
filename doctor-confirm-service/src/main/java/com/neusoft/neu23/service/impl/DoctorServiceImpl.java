package com.neusoft.neu23.service.impl;

import com.assist.common.common.BusinessException;
import com.assist.common.common.ErrorCode;
import com.assist.common.entity.Doctor;
import com.assist.common.entity.Patient;
import com.neusoft.neu23.mapper.DoctorMapper;
import com.neusoft.neu23.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医生服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {
    
    private final DoctorMapper doctorMapper;
    
    @Override
    public Doctor login(String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "医生姓名不能为空");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "医生电话不能为空");
        }
        
        Doctor doctor = doctorMapper.selectByNameAndPhone(name.trim(), phone.trim());
        if (doctor == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "姓名或电话错误，登录失败");
        }
        
        log.info("医生登录成功: doctorId={}, name={}", doctor.getDoctorId(), doctor.getName());
        return doctor;
    }
    
    @Override
    public List<Patient> getPatientsByDoctorId(Integer doctorId) {
        if (doctorId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "医生ID不能为空");
        }
        
        List<Patient> patients = doctorMapper.selectPatientsByDoctorId(doctorId);
        log.info("查询医生患者列表: doctorId={}, 患者数量={}", doctorId, patients.size());
        return patients;
    }
}





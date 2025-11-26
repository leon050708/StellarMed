package com.assist.patient.service;

import com.assist.common.entity.Patient;

import java.util.List;

/**
 * 患者服务接口
 */
public interface PatientService {
    /**
     * 创建或更新患者
     */
    Integer createOrUpdate(Patient patient);

    /**
     * 根据病例号查询患者
     */
    Patient getByCaseNumber(String caseNumber);

    /**
     * 获取所有患者列表
     */
    List<Patient> getAllPatients();
}


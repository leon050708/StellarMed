package com.assist.patient.service.impl;

import com.assist.common.entity.Patient;
import com.assist.patient.mapper.PatientMapper;
import com.assist.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 患者服务实现
 */
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientMapper patientMapper;

    @Override
    public Integer createOrUpdate(Patient patient) {
        Patient existing = patientMapper.selectByCaseNumber(patient.getCaseNumber());
        if (existing != null) {
            patient.setId(existing.getId());
            patient.setCreateTime(existing.getCreateTime());
            patientMapper.updateById(patient);
            return existing.getId();
        } else {
            patient.setCreateTime(new Date());
            patientMapper.insert(patient);
            return patient.getId();
        }
    }

    @Override
    public Patient getByCaseNumber(String caseNumber) {
        return patientMapper.selectByCaseNumber(caseNumber);
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientMapper.selectList(null);
    }
}


package com.assist.patient.service;

import com.assist.common.entity.SymptomRecord;

/**
 * 症状记录服务接口
 */
public interface SymptomService {
    /**
     * 保存原始症状
     */
    Integer saveSymptom(SymptomRecord symptomRecord);
}


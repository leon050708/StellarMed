package com.assist.patient.service.impl;

import com.assist.common.entity.SymptomRecord;
import com.assist.patient.mapper.SymptomRecordMapper;
import com.assist.patient.service.SymptomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 症状记录服务实现
 */
@Service
@RequiredArgsConstructor
public class SymptomServiceImpl implements SymptomService {

    private final SymptomRecordMapper symptomRecordMapper;

    @Override
    public Integer saveSymptom(SymptomRecord symptomRecord) {
        symptomRecord.setExtractedTime(new Date());
        symptomRecordMapper.insert(symptomRecord);
        return symptomRecord.getSymptomId();
    }
}


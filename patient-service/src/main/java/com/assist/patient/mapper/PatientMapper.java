package com.assist.patient.mapper;

import com.assist.common.entity.Patient;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 患者Mapper
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
    
    @Select("SELECT * FROM patient WHERE case_number = #{caseNumber}")
    Patient selectByCaseNumber(String caseNumber);
}


package com.assist.symptom.mapper;

import com.assist.common.entity.SymptomRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 症状记录Mapper
 */
@Mapper
public interface SymptomRecordMapper extends BaseMapper<SymptomRecord> {
    
    @Select("SELECT * FROM symptom_record WHERE session_id = #{sessionId} ORDER BY extracted_time")
    List<SymptomRecord> selectBySessionId(Integer sessionId);
}


package com.assist.symptom.mapper;

import com.assist.common.entity.AiSymptomStructured;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI结构化症状Mapper
 */
@Mapper
public interface AiSymptomStructuredMapper extends BaseMapper<AiSymptomStructured> {
    
    @Select("SELECT * FROM ai_symptom_structured WHERE session_id = #{sessionId} ORDER BY create_time")
    List<AiSymptomStructured> selectBySessionId(Integer sessionId);
}


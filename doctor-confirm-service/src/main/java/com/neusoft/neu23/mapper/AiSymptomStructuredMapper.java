package com.neusoft.neu23.mapper;

import com.assist.common.entity.AiSymptomStructured;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 结构化症状 Mapper 接口
 */
public interface AiSymptomStructuredMapper extends BaseMapper<AiSymptomStructured> {
    
    /**
     * 根据 sessionId 查询结构化症状
     * 
     * @param sessionId 会话ID
     * @return 结构化症状列表
     */
    @Select("SELECT * FROM ai_symptom_structured WHERE session_id = #{sessionId} ORDER BY create_time DESC")
    List<AiSymptomStructured> selectBySessionId(@Param("sessionId") Integer sessionId);
}


package com.neusoft.neu23.mapper;

import com.assist.common.entity.AiPreDiagnosis;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 初步诊断 Mapper 接口
 * 
 * @author StellarMed Team
 */
@Mapper
public interface AiPreDiagnosisMapper extends BaseMapper<AiPreDiagnosis> {
    
    /**
     * 根据 sessionId 查询初步诊断
     * 
     * @param sessionId 会话ID
     * @return 初步诊断列表
     */
    @Select("SELECT * FROM ai_pre_diagnosis WHERE session_id = #{sessionId} ORDER BY create_time DESC")
    List<AiPreDiagnosis> selectBySessionId(@Param("sessionId") Integer sessionId);
}


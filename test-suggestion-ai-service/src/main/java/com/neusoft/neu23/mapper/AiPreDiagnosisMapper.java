package com.neusoft.neu23.mapper;

import com.assist.common.entity.AiPreDiagnosis;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
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
    
    /**
     * 获取该 session 最新诊断的更新时间
     * 
     * @param sessionId 会话ID
     * @return 最新更新时间，如果没有则返回 null
     */
    @Select("SELECT MAX(create_time) FROM ai_pre_diagnosis WHERE session_id = #{sessionId}")
    Date getLatestUpdateTime(@Param("sessionId") Integer sessionId);
}


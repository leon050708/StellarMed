package com.neusoft.neu23.mapper;

import com.assist.common.entity.AiRiskAssessment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 风险评估 Mapper 接口
 * 
 * @author StellarMed Team
 */
@Mapper
public interface AiRiskAssessmentMapper extends BaseMapper<AiRiskAssessment> {
    
    /**
     * 根据 sessionId 查询风险评估
     * 
     * @param sessionId 会话ID
     * @return 风险评估列表
     */
    @Select("SELECT * FROM ai_risk_assessment WHERE session_id = #{sessionId} ORDER BY created_time DESC")
    List<AiRiskAssessment> selectBySessionId(@Param("sessionId") Integer sessionId);
}


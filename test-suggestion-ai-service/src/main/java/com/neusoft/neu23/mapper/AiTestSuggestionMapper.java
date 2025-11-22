package com.neusoft.neu23.mapper;

import com.assist.common.entity.AiTestSuggestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * AI 检查建议 Mapper 接口
 * 
 * @author StellarMed Team
 */
@Mapper
public interface AiTestSuggestionMapper extends BaseMapper<AiTestSuggestion> {
    
    /**
     * 根据 sessionId 查询所有检查建议
     * 
     * @param sessionId 会话ID
     * @return 检查建议列表
     */
    List<AiTestSuggestion> selectBySessionId(@Param("sessionId") Integer sessionId);
    
    /**
     * 根据 patientId 查询所有检查建议
     * 
     * @param patientId 患者ID
     * @return 检查建议列表
     */
    List<AiTestSuggestion> selectByPatientId(@Param("patientId") Integer patientId);
    
    /**
     * 批量插入检查建议
     * 
     * @param suggestions 检查建议列表
     * @return 插入数量
     */
    int batchInsert(@Param("list") List<AiTestSuggestion> suggestions);
    
    /**
     * 根据 sessionId 删除检查建议
     * 
     * @param sessionId 会话ID
     * @return 删除数量
     */
    int deleteBySessionId(@Param("sessionId") Integer sessionId);
    
    /**
     * 获取该 session 最新检查建议的创建时间
     * 
     * @param sessionId 会话ID
     * @return 最新创建时间，如果没有则返回 null
     */
    @Select("SELECT MAX(created_time) FROM ai_test_suggestion WHERE session_id = #{sessionId}")
    Date getLatestCreateTime(@Param("sessionId") Integer sessionId);
}


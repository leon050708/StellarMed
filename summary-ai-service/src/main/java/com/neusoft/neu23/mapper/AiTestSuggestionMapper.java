package com.neusoft.neu23.mapper;

import com.assist.common.entity.AiTestSuggestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 检查建议 Mapper 接口
 */
public interface AiTestSuggestionMapper extends BaseMapper<AiTestSuggestion> {
    
    /**
     * 根据 sessionId 查询检查建议
     * 
     * @param sessionId 会话ID
     * @return 检查建议列表
     */
    @Select("SELECT * FROM ai_test_suggestion WHERE session_id = #{sessionId} ORDER BY created_time DESC")
    List<AiTestSuggestion> selectBySessionId(@Param("sessionId") Integer sessionId);
}


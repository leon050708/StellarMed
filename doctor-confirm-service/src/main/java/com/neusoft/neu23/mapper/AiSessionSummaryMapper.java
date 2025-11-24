package com.neusoft.neu23.mapper;

import com.assist.common.entity.AiSessionSummary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AI 会话总结 Mapper 接口
 */
public interface AiSessionSummaryMapper extends BaseMapper<AiSessionSummary> {
    
    /**
     * 根据 sessionId 查询会话总结
     * 
     * @param sessionId 会话ID
     * @return 会话总结，如果没有则返回 null
     */
    @Select("SELECT * FROM ai_session_summary WHERE session_id = #{sessionId} ORDER BY created_time DESC LIMIT 1")
    AiSessionSummary selectBySessionId(@Param("sessionId") Integer sessionId);
}


package com.assist.patient.mapper;

import com.assist.common.entity.ChatRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天记录Mapper
 */
@Mapper
public interface ChatRecordMapper extends BaseMapper<ChatRecord> {
    
    /**
     * 根据会话ID查询聊天记录，按时间排序
     */
    @Select("SELECT * FROM chat_record WHERE session_id = #{sessionId} ORDER BY timestamp")
    List<ChatRecord> selectBySessionId(Integer sessionId);
}


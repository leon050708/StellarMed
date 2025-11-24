package com.neusoft.neu23.mapper;

import com.assist.common.entity.AiPrescription;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 处方建议 Mapper 接口
 */
public interface AiPrescriptionMapper extends BaseMapper<AiPrescription> {
    
    /**
     * 根据 sessionId 查询处方建议
     * 
     * @param sessionId 会话ID
     * @return 处方建议列表
     */
    @Select("SELECT * FROM ai_prescription WHERE session_id = #{sessionId} ORDER BY create_time DESC")
    List<AiPrescription> selectBySessionId(@Param("sessionId") Integer sessionId);
}


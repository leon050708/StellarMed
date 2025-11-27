package com.assist.patient.service;

import com.assist.common.entity.Session;
import java.util.List;

/**
 * 会话服务接口
 */
public interface SessionService {
    /**
     * 创建会话
     */
    Session createSession(Integer patientId);

    /**
     * 关闭会话
     */
    void closeSession(Integer sessionId);

    /**
     * 根据患者ID查询会话列表
     */
    List<Session> getSessionsByPatientId(Integer patientId);

    /**
     * 查询所有会话列表
     */
    List<Session> getAllSessions();

    /**
     * 根据会话ID查询会话详情
     */
    Session getSessionById(Integer sessionId);
}


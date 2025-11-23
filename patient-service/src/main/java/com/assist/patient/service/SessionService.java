package com.assist.patient.service;

import com.assist.common.entity.Session;

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
}


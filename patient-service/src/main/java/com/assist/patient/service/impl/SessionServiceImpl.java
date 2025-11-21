package com.assist.patient.service.impl;

import com.assist.common.entity.Session;
import com.assist.common.enums.SessionStatusEnum;
import com.assist.patient.mapper.SessionMapper;
import com.assist.patient.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 会话服务实现
 */
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionMapper sessionMapper;

    @Override
    public Session createSession(Integer patientId) {
        Session session = new Session();
        session.setPatientId(patientId);
        session.setCreatedTime(new Date());
        session.setStatus(SessionStatusEnum.ACTIVE.name());
        sessionMapper.insert(session);
        return session;
    }

    @Override
    public void closeSession(Integer sessionId) {
        Session session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setStatus(SessionStatusEnum.COMPLETED.name());
            sessionMapper.updateById(session);
        }
    }
}


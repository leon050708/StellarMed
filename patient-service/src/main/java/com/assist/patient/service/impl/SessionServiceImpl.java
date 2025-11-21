package com.assist.patient.service.impl;

import com.assist.common.entity.Session;
import com.assist.common.enums.SessionStatusEnum;
import com.assist.patient.mapper.SessionMapper;
import com.assist.patient.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 会话服务实现
 */
@Slf4j
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
        log.info("创建会话前，sessionId: {}", session.getSessionId());
        sessionMapper.insert(session);
        log.info("创建会话后，sessionId: {}", session.getSessionId());
        // 使用 @TableId 注解后，MyBatis-Plus 会自动回填 sessionId
        // 如果没有回填，需要手动查询
        if (session.getSessionId() == null) {
            log.warn("sessionId 未自动回填，尝试手动查询");
            Session createdSession = sessionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Session>()
                    .eq(Session::getPatientId, patientId)
                    .eq(Session::getStatus, SessionStatusEnum.ACTIVE.name())
                    .orderByDesc(Session::getCreatedTime)
                    .last("LIMIT 1")
            );
            if (createdSession != null) {
                log.info("手动查询到 sessionId: {}", createdSession.getSessionId());
                return createdSession;
            }
        }
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


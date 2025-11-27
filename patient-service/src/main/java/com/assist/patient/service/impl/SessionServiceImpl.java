package com.assist.patient.service.impl;

import com.assist.common.entity.Session;
import com.assist.common.enums.SessionStatusEnum;
import com.assist.patient.mapper.SessionMapper;
import com.assist.patient.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 会话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionMapper sessionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Session createSession(Integer patientId) {
        Session session = new Session();
        session.setPatientId(patientId);
        session.setCreatedTime(new Date());
        session.setStatus(SessionStatusEnum.ACTIVE.name());
        log.info("创建会话前，sessionId: {}", session.getSessionId());
        
        int insertResult = sessionMapper.insert(session);
        log.info("插入会话结果: {}, 插入后 sessionId: {}", insertResult, session.getSessionId());
        
        // 使用 @TableId 注解后，MyBatis-Plus 会自动回填 sessionId
        // 如果没有回填，需要手动查询
        if (session.getSessionId() == null) {
            log.warn("sessionId 未自动回填，尝试手动查询最新创建的会话");
            Session createdSession = sessionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Session>()
                    .eq(Session::getPatientId, patientId)
                    .eq(Session::getStatus, SessionStatusEnum.ACTIVE.name())
                    .orderByDesc(Session::getCreatedTime)
                    .last("LIMIT 1")
            );
            if (createdSession != null && createdSession.getSessionId() != null) {
                log.info("手动查询到 sessionId: {}", createdSession.getSessionId());
                return createdSession;
            } else {
                log.error("创建会话失败：无法获取 sessionId，patientId: {}", patientId);
                throw new RuntimeException("创建会话失败：无法获取 sessionId");
            }
        }
        
        log.info("会话创建成功，sessionId: {}, patientId: {}", session.getSessionId(), patientId);
        
        // 清除相关缓存（因为新增了会话，列表会变化）
        if (redisTemplate != null) {
            try {
                // 清除该患者的会话列表缓存
                String patientSessionsKey = "sessions:patient:" + patientId;
                redisTemplate.delete(patientSessionsKey);
                
                // 清除所有会话列表缓存
                redisTemplate.delete("sessions:all");
                
                log.info("已清除会话列表缓存，patientId: {}", patientId);
            } catch (Exception e) {
                log.warn("清除缓存失败: {}", e.getMessage());
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
            
            // 清除相关缓存
            if (redisTemplate != null) {
                try {
                    // 清除会话详情缓存
                    String sessionKey = "session:" + sessionId;
                    redisTemplate.delete(sessionKey);
                    
                    // 清除该患者的会话列表缓存
                    String patientSessionsKey = "sessions:patient:" + session.getPatientId();
                    redisTemplate.delete(patientSessionsKey);
                    
                    // 清除所有会话列表缓存
                    redisTemplate.delete("sessions:all");
                    
                    log.info("已清除会话相关缓存，sessionId: {}", sessionId);
                } catch (Exception e) {
                    log.warn("清除缓存失败: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public List<Session> getSessionsByPatientId(Integer patientId) {
        log.info("查询患者会话列表: patientId={}", patientId);
        
        // Redis 缓存键：sessions:患者ID
        String cacheKey = "sessions:patient:" + patientId;
        
        // 1. 先查 Redis 缓存
        if (redisTemplate != null) {
            try {
                @SuppressWarnings("unchecked")
                List<Session> cached = (List<Session>) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.info("从 Redis 缓存获取会话列表，patientId: {}, 会话数量: {}", patientId, cached.size());
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Redis 缓存读取失败，继续查询数据库: {}", e.getMessage());
            }
        }
        
        // 2. 缓存没有，查询数据库
        log.info("从数据库查询会话列表，patientId: {}", patientId);
        LambdaQueryWrapper<Session> wrapper = new LambdaQueryWrapper<Session>()
                .eq(Session::getPatientId, patientId)
                .orderByDesc(Session::getCreatedTime);
        List<Session> sessions = sessionMapper.selectList(wrapper);
        
        // 3. 将查询结果存入 Redis 缓存（30 分钟过期）
        if (redisTemplate != null && sessions != null && !sessions.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, sessions, 30, TimeUnit.MINUTES);
                log.info("会话列表已存入 Redis 缓存，patientId: {}, 会话数量: {}", patientId, sessions.size());
            } catch (Exception e) {
                log.warn("Redis 缓存写入失败: {}", e.getMessage());
            }
        }
        
        return sessions;
    }

    @Override
    public List<Session> getAllSessions() {
        log.info("查询所有会话列表");
        
        // Redis 缓存键：sessions:all
        String cacheKey = "sessions:all";
        
        // 1. 先查 Redis 缓存
        if (redisTemplate != null) {
            try {
                @SuppressWarnings("unchecked")
                List<Session> cached = (List<Session>) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.info("从 Redis 缓存获取所有会话列表，会话数量: {}", cached.size());
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Redis 缓存读取失败，继续查询数据库: {}", e.getMessage());
            }
        }
        
        // 2. 缓存没有，查询数据库
        log.info("从数据库查询所有会话列表");
        LambdaQueryWrapper<Session> wrapper = new LambdaQueryWrapper<Session>()
                .orderByDesc(Session::getCreatedTime);
        List<Session> sessions = sessionMapper.selectList(wrapper);
        
        // 3. 将查询结果存入 Redis 缓存（30 分钟过期）
        if (redisTemplate != null && sessions != null && !sessions.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, sessions, 30, TimeUnit.MINUTES);
                log.info("所有会话列表已存入 Redis 缓存，会话数量: {}", sessions.size());
            } catch (Exception e) {
                log.warn("Redis 缓存写入失败: {}", e.getMessage());
            }
        }
        
        return sessions;
    }

    @Override
    public Session getSessionById(Integer sessionId) {
        log.info("查询会话详情: sessionId={}", sessionId);
        
        // Redis 缓存键：session:会话ID
        String cacheKey = "session:" + sessionId;
        
        // 1. 先查 Redis 缓存
        if (redisTemplate != null) {
            try {
                Session cached = (Session) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.info("从 Redis 缓存获取会话详情，sessionId: {}", sessionId);
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Redis 缓存读取失败，继续查询数据库: {}", e.getMessage());
            }
        }
        
        // 2. 缓存没有，查询数据库
        log.info("从数据库查询会话详情，sessionId: {}", sessionId);
        Session session = sessionMapper.selectById(sessionId);
        
        // 3. 将查询结果存入 Redis 缓存（30 分钟过期）
        if (redisTemplate != null && session != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, session, 30, TimeUnit.MINUTES);
                log.info("会话详情已存入 Redis 缓存，sessionId: {}", sessionId);
            } catch (Exception e) {
                log.warn("Redis 缓存写入失败: {}", e.getMessage());
            }
        }
        
        return session;
    }
}


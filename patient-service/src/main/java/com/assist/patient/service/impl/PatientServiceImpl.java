package com.assist.patient.service.impl;

import com.assist.common.entity.Patient;
import com.assist.patient.mapper.PatientMapper;
import com.assist.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 患者服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientMapper patientMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Integer createOrUpdate(Patient patient) {
        Patient existing = patientMapper.selectByCaseNumber(patient.getCaseNumber());
        if (existing != null) {
            patient.setId(existing.getId());
            patient.setCreateTime(existing.getCreateTime());
            patientMapper.updateById(patient);
            
            // 清除相关缓存
            if (redisTemplate != null) {
                try {
                    String cacheKey = "patient:case:" + patient.getCaseNumber();
                    redisTemplate.delete(cacheKey);
                    // 清除患者列表缓存
                    redisTemplate.delete("patients:all");
                    log.info("已清除患者相关缓存，caseNumber: {}", patient.getCaseNumber());
                } catch (Exception e) {
                    log.warn("清除缓存失败: {}", e.getMessage());
                }
            }
            
            return existing.getId();
        } else {
            patient.setCreateTime(new Date());
            patientMapper.insert(patient);
            
            // 清除患者列表缓存
            if (redisTemplate != null) {
                try {
                    redisTemplate.delete("patients:all");
                    log.info("已清除患者列表缓存");
                } catch (Exception e) {
                    log.warn("清除缓存失败: {}", e.getMessage());
                }
            }
            
            return patient.getId();
        }
    }

    @Override
    public Patient getByCaseNumber(String caseNumber) {
        // Redis 缓存键：patient:case:病例号
        String cacheKey = "patient:case:" + caseNumber;
        
        // 1. 先查 Redis 缓存
        if (redisTemplate != null) {
            try {
                Patient cached = (Patient) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.info("从 Redis 缓存获取患者信息，caseNumber: {}", caseNumber);
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Redis 缓存读取失败，继续查询数据库: {}", e.getMessage());
            }
        }
        
        // 2. 缓存没有，查询数据库
        log.info("从数据库查询患者信息，caseNumber: {}", caseNumber);
        Patient patient = patientMapper.selectByCaseNumber(caseNumber);
        
        // 3. 将查询结果存入 Redis 缓存（1 小时过期）
        if (redisTemplate != null && patient != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, patient, 1, TimeUnit.HOURS);
                log.info("患者信息已存入 Redis 缓存，caseNumber: {}", caseNumber);
            } catch (Exception e) {
                log.warn("Redis 缓存写入失败: {}", e.getMessage());
            }
        }
        
        return patient;
    }

    @Override
    public List<Patient> getAllPatients() {
        // Redis 缓存键：patients:all
        String cacheKey = "patients:all";
        
        // 1. 先查 Redis 缓存
        if (redisTemplate != null) {
            try {
                @SuppressWarnings("unchecked")
                List<Patient> cached = (List<Patient>) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.info("从 Redis 缓存获取患者列表，患者数量: {}", cached.size());
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Redis 缓存读取失败，继续查询数据库: {}", e.getMessage());
            }
        }
        
        // 2. 缓存没有，查询数据库
        log.info("从数据库查询患者列表");
        List<Patient> patients = patientMapper.selectList(null);
        
        // 3. 将查询结果存入 Redis 缓存（30 分钟过期）
        if (redisTemplate != null && patients != null && !patients.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, patients, 30, TimeUnit.MINUTES);
                log.info("患者列表已存入 Redis 缓存，患者数量: {}", patients.size());
            } catch (Exception e) {
                log.warn("Redis 缓存写入失败: {}", e.getMessage());
            }
        }
        
        return patients;
    }
}


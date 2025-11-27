# Redis 缓存功能详解

## 🎯 缓存到底有什么用？

### 实际业务场景

想象一下这个场景：

**医生在查看患者处方时：**
1. 第一次点击"查看处方" → 查询数据库（500ms）→ 显示处方
2. 医生刷新页面 → 又查询数据库（500ms）→ 显示处方
3. 医生切换标签页再回来 → 又查询数据库（500ms）→ 显示处方

**问题：**
- 处方数据在会话期间**不会变化**
- 但每次都要查数据库，**浪费资源**
- 用户体验差，**响应慢**

**解决方案：Redis 缓存**
- 第一次查询：数据库 → 存入 Redis
- 后续查询：直接从 Redis 读取（快 50 倍）
- 1 小时后自动过期，保证数据新鲜

---

## 📍 具体功能实现在哪里？

### 1. 处方查询缓存（已实现）

**文件位置：**
```
prescription-ai-service/src/main/java/com/neusoft/neu23/service/impl/PrescriptionServiceImpl.java
```

**具体代码：** 第 341-400 行

```java
@Override
public PrescriptionGenerateResponse getPrescriptionBySession(Integer patientId, Integer sessionId) {
    // Redis 缓存键：prescription:患者ID:会话ID
    String cacheKey = "prescription:" + patientId + ":" + sessionId;
    
    // 1. 先查 Redis 缓存
    PrescriptionGenerateResponse cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        log.info("从 Redis 缓存获取处方数据");
        return cached;  // 直接返回，不查数据库！
    }
    
    // 2. 缓存没有，查询数据库
    List<AiPrescription> prescriptions = this.list(queryWrapper);
    
    // 3. 存入 Redis，1 小时后过期
    redisTemplate.opsForValue().set(cacheKey, response, 1, TimeUnit.HOURS);
    
    return response;
}
```

**存储的数据：**
- 键：`prescription:1:1`（格式：`prescription:患者ID:会话ID`）
- 值：完整的处方 JSON 数据（包含所有药品信息）
- 过期时间：1 小时

---

## 💾 项目中哪些数据适合缓存？

### ✅ 已实现：处方数据缓存

**为什么缓存处方？**
- 处方生成后，在会话期间**不会变化**
- 医生可能**多次查看**同一处方
- 查询频率高，缓存效果好

**缓存位置：**
- `PrescriptionServiceImpl.getPrescriptionBySession()` 方法

---

### 🔄 可以缓存的其他数据

#### 1. **会话列表**（高频查询）

**文件位置：**
```
patient-service/src/main/java/com/assist/patient/service/impl/SessionServiceImpl.java
```

**当前实现（第 70-76 行）：**
```java
public List<Session> getSessionsByPatientId(Integer patientId) {
    // 每次都查数据库
    return sessionMapper.selectList(wrapper);
}
```

**可以优化为：**
```java
public List<Session> getSessionsByPatientId(Integer patientId) {
    String cacheKey = "sessions:patient:" + patientId;
    
    // 先查缓存
    List<Session> cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        return cached;
    }
    
    // 查数据库
    List<Session> sessions = sessionMapper.selectList(wrapper);
    
    // 存入缓存，30 分钟过期
    redisTemplate.opsForValue().set(cacheKey, sessions, 30, TimeUnit.MINUTES);
    
    return sessions;
}
```

**为什么缓存？**
- 患者列表页面**频繁刷新**
- 会话数据变化不频繁
- 可以大幅减少数据库查询

---

#### 2. **AI 诊断结果**（避免重复调用）

**文件位置：**
```
diagnosis-ai-service/src/main/java/com/assist/diagnosis/service/impl/DiagnosisServiceImpl.java
```

**当前实现（第 36-75 行）：**
```java
public DiagnosisEvaluateResponse evaluateDiagnosis(...) {
    // 每次都要调用 AI（耗时 3-5 秒）
    String aiResponse = chatClient.prompt().user(prompt).call().content();
    // ...
}
```

**可以优化为：**
```java
public DiagnosisEvaluateResponse evaluateDiagnosis(...) {
    String cacheKey = "diagnosis:" + patientId + ":" + sessionId;
    
    // 先查缓存
    DiagnosisEvaluateResponse cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        return cached;  // 直接返回，不调用 AI！
    }
    
    // 缓存没有，调用 AI（耗时 3-5 秒）
    String aiResponse = chatClient.prompt().user(prompt).call().content();
    
    // 存入缓存，2 小时过期
    redisTemplate.opsForValue().set(cacheKey, response, 2, TimeUnit.HOURS);
    
    return response;
}
```

**为什么缓存？**
- AI 调用**非常慢**（3-5 秒）
- 诊断结果在会话期间**不会变化**
- 可以节省 **AI 调用费用**（每次调用都要钱）

---

#### 3. **患者基本信息**（基础数据）

**文件位置：**
```
patient-service/src/main/java/com/assist/patient/service/impl/PatientServiceImpl.java
```

**可以缓存：**
```java
public Patient getByCaseNumber(String caseNumber) {
    String cacheKey = "patient:case:" + caseNumber;
    
    Patient cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        return cached;
    }
    
    Patient patient = patientMapper.selectByCaseNumber(caseNumber);
    
    // 存入缓存，1 小时过期
    redisTemplate.opsForValue().set(cacheKey, patient, 1, TimeUnit.HOURS);
    
    return patient;
}
```

**为什么缓存？**
- 患者信息**变化不频繁**
- 多个服务可能**同时查询**同一患者
- 减少数据库压力

---

#### 4. **对话历史**（频繁查询）

**文件位置：**
```
patient-service/src/main/java/com/assist/patient/service/impl/ChatServiceImpl.java
```

**当前实现（第 102-104 行）：**
```java
public List<ChatRecord> getChatHistory(Integer sessionId) {
    return chatRecordMapper.selectBySessionId(sessionId);
}
```

**可以优化为：**
```java
public List<ChatRecord> getChatHistory(Integer sessionId) {
    String cacheKey = "chat:history:" + sessionId;
    
    List<ChatRecord> cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        return cached;
    }
    
    List<ChatRecord> history = chatRecordMapper.selectBySessionId(sessionId);
    
    // 存入缓存，30 分钟过期
    redisTemplate.opsForValue().set(cacheKey, history, 30, TimeUnit.MINUTES);
    
    return history;
}
```

**为什么缓存？**
- 对话历史在会话期间**只增不减**
- 用户可能**频繁查看**历史记录
- 可以提升响应速度

---

## 📊 缓存数据总结

| 数据类型 | 缓存键格式 | 过期时间 | 为什么缓存 |
|---------|-----------|---------|-----------|
| **处方数据** | `prescription:患者ID:会话ID` | 1 小时 | ✅ 已实现，查询频繁 |
| **会话列表** | `sessions:patient:患者ID` | 30 分钟 | 🔄 可以优化 |
| **AI 诊断结果** | `diagnosis:患者ID:会话ID` | 2 小时 | 🔄 可以优化，避免重复调用 AI |
| **患者信息** | `patient:case:病例号` | 1 小时 | 🔄 可以优化 |
| **对话历史** | `chat:history:会话ID` | 30 分钟 | 🔄 可以优化 |

---

## 🎯 缓存的核心价值

### 1. **性能提升**
- 数据库查询：500ms
- Redis 查询：10ms
- **提升 50 倍**

### 2. **成本节省**
- 减少数据库查询 → 降低数据库压力
- 避免重复调用 AI → 节省 AI 调用费用（每次调用都要钱）

### 3. **用户体验**
- 响应速度快 → 用户等待时间短
- 系统更稳定 → 减少数据库压力

---

## 🔍 如何查看缓存的数据？

### 在 Redis Desktop Manager 中：

1. **处方缓存**
   - 键：`prescription:1:1`
   - 值：处方的 JSON 数据

2. **会话列表缓存**（如果实现）
   - 键：`sessions:patient:1`
   - 值：会话列表的 JSON 数据

3. **诊断结果缓存**（如果实现）
   - 键：`diagnosis:1:1`
   - 值：诊断结果的 JSON 数据

---

## 💡 总结

**缓存存储的是什么？**
- **查询结果**：处方、诊断、会话列表等
- **不是原始数据**：不是存储数据库表，而是存储**查询结果**

**为什么需要缓存？**
- **性能**：从 500ms 降到 10ms
- **成本**：减少数据库查询和 AI 调用
- **体验**：用户等待时间大幅缩短

**具体实现在哪里？**
- **处方缓存**：`PrescriptionServiceImpl.getPrescriptionBySession()` ✅ 已实现
- **其他缓存**：可以按照相同模式添加到其他 Service 中

现在你明白缓存的作用了吗？它就像给数据库加了一个"快速通道"，让频繁查询的数据可以快速获取！


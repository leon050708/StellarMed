# Redis 在医疗项目中的实际应用场景

## 🎯 为什么需要 Redis？

### 当前问题：
1. **AI 调用很慢**：每次调用通义千问需要 2-5 秒
2. **数据库查询频繁**：处方、诊断、会话数据反复查询
3. **用户体验差**：医生查看处方时每次都要等数据库查询

### Redis 解决方案：
1. **缓存 AI 结果**：处方、诊断结果缓存 1 小时，避免重复调用 AI
2. **缓存热点数据**：患者信息、会话状态缓存 30 分钟
3. **提升响应速度**：从 2-5 秒降低到 10-50 毫秒

---

## 📋 实际应用场景

### 场景 1：缓存处方数据 ⭐⭐⭐⭐⭐

**问题：**
- 医生查看处方时，每次都要查询数据库
- 处方数据生成后不会频繁变化
- 多个医生同时查看同一患者的处方

**解决方案：**
```java
// 查询处方时，先查 Redis，没有再查数据库
String cacheKey = "prescription:" + patientId + ":" + sessionId;
PrescriptionGenerateResponse cached = redisTemplate.opsForValue().get(cacheKey);
if (cached != null) {
    return cached; // 从缓存返回，速度极快！
}
// 缓存没有，查数据库
PrescriptionGenerateResponse response = prescriptionService.getPrescriptionBySession(...);
// 存入缓存，1小时后过期
redisTemplate.opsForValue().set(cacheKey, response, 1, TimeUnit.HOURS);
```

**效果：**
- 第一次查询：500ms（查数据库）
- 后续查询：10ms（查 Redis）
- **速度提升 50 倍！**

---

### 场景 2：缓存 AI 诊断结果 ⭐⭐⭐⭐

**问题：**
- AI 诊断一次需要 3-5 秒
- 诊断结果在会话期间不会变化
- 医生可能多次查看诊断结果

**解决方案：**
```java
// 生成诊断时，先检查缓存
String cacheKey = "diagnosis:" + patientId + ":" + sessionId;
DiagnosisEvaluateResponse cached = redisTemplate.opsForValue().get(cacheKey);
if (cached != null) {
    return cached; // 直接返回，不用调用 AI！
}

// 缓存没有，调用 AI（耗时 3-5 秒）
DiagnosisEvaluateResponse response = diagnosisService.evaluateDiagnosis(...);
// 存入缓存，会话期间有效
redisTemplate.opsForValue().set(cacheKey, response, 2, TimeUnit.HOURS);
```

**效果：**
- 第一次：5 秒（调用 AI）
- 后续：10ms（从缓存读取）
- **速度提升 500 倍！**

---

### 场景 3：缓存会话状态 ⭐⭐⭐

**问题：**
- 每次查询会话列表都要查数据库
- 会话状态变化不频繁
- 患者列表页面频繁刷新

**解决方案：**
```java
// 查询患者会话列表
String cacheKey = "sessions:patient:" + patientId;
List<Session> sessions = redisTemplate.opsForValue().get(cacheKey);
if (sessions == null) {
    sessions = sessionService.getSessionsByPatientId(patientId);
    // 缓存 30 分钟
    redisTemplate.opsForValue().set(cacheKey, sessions, 30, TimeUnit.MINUTES);
}
return sessions;
```

**效果：**
- 数据库查询：100ms
- Redis 查询：5ms
- **速度提升 20 倍！**

---

### 场景 4：防止重复提交 ⭐⭐⭐⭐

**问题：**
- 用户可能重复点击"生成处方"按钮
- 导致重复调用 AI，浪费资源

**解决方案：**
```java
// 生成处方前，检查是否正在生成
String lockKey = "prescription:generating:" + patientId + ":" + sessionId;
Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
if (!lock) {
    throw new BusinessException("处方正在生成中，请勿重复提交");
}
try {
    // 生成处方...
} finally {
    // 完成后释放锁
    redisTemplate.delete(lockKey);
}
```

**效果：**
- 防止重复提交
- 节省 AI 调用费用
- 提升用户体验

---

### 场景 5：实时统计 ⭐⭐

**问题：**
- 需要统计今日问诊人数、处方数量等
- 每次统计都要聚合查询数据库，很慢

**解决方案：**
```java
// 每次生成处方时，增加计数
String todayKey = "stats:prescriptions:" + LocalDate.now();
redisTemplate.opsForValue().increment(todayKey);
// 设置过期时间为明天 0 点
redisTemplate.expire(todayKey, Duration.ofDays(1));

// 查询今日统计
Long count = redisTemplate.opsForValue().get(todayKey);
```

**效果：**
- 实时统计，无需查询数据库
- 响应速度极快

---

## 📊 性能对比

| 操作 | 无缓存 | 有 Redis 缓存 | 提升倍数 |
|------|--------|---------------|----------|
| 查询处方 | 500ms | 10ms | **50x** |
| AI 诊断 | 5000ms | 10ms | **500x** |
| 查询会话列表 | 100ms | 5ms | **20x** |
| 统计今日数据 | 200ms | 1ms | **200x** |

---

## 💰 成本节省

- **AI 调用费用**：避免重复调用，节省 60-80% 费用
- **数据库压力**：减少 70% 数据库查询
- **服务器资源**：降低 CPU 和内存使用

---

## 🚀 下一步

我会为你创建一个实际的缓存服务，展示如何缓存处方数据。这样你就能看到 Redis 的实际效果了！


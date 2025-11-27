# Redis 缓存查看指南

## 📋 为什么在 Redis Desktop Manager 中看不到数据？

### 原因分析

1. **需要先触发查询才会写入缓存**
   - Redis 缓存是**懒加载**的，只有查询过数据才会写入
   - 如果只是打开页面但没有实际查询，Redis 中不会有数据

2. **缓存键的命名规则**
   - 处方缓存：`prescription:患者ID:会话ID`（例如：`prescription:1:1040`）
   - 会话列表缓存：`sessions:patient:患者ID`（例如：`sessions:patient:1`）
   - 所有会话缓存：`sessions:all`
   - 会话详情缓存：`session:会话ID`（例如：`session:1040`）

3. **缓存过期时间**
   - 处方缓存：1 小时
   - 会话相关缓存：30 分钟

---

## 🔍 如何在 Redis Desktop Manager 中查看缓存？

### 步骤 1：确保 Redis 正在运行

```bash
# 检查 Redis 是否运行
redis-cli ping
# 应该返回：PONG
```

### 步骤 2：触发缓存写入

#### 方法 A：通过前端页面触发

1. **查看会话列表**（会写入会话列表缓存）
   - 打开前端页面：`http://localhost:5173/sessions`
   - 点击"会话管理"
   - 第一次查询会写入缓存：`sessions:all` 或 `sessions:patient:患者ID`

2. **查看会话详情**（会写入会话详情缓存）
   - 点击某个会话的"查看详情"
   - 会写入缓存：`session:会话ID`

3. **查看助诊报告**（会写入处方缓存）
   - 进入会话详情页面
   - 查看助诊报告
   - 会写入缓存：`prescription:患者ID:会话ID`

#### 方法 B：通过 Postman 直接调用接口

1. **查询会话列表**
   ```
   GET http://localhost:8888/api/sessions?patientId=1
   ```
   会写入缓存：`sessions:patient:1`

2. **查询处方**
   ```
   GET http://localhost:8888/api/ai/prescriptions/query?patientId=1&sessionId=1040
   ```
   会写入缓存：`prescription:1:1040`

### 步骤 3：在 Redis Desktop Manager 中查看

1. **打开 Redis Desktop Manager**
   - 连接到 `localhost:6379`

2. **查看数据库 0（db0）**
   - 在左侧找到 `db0`，展开

3. **查找缓存键**
   - 按 `Ctrl+F` 搜索键名
   - 或者直接浏览，查找以下格式的键：
     - `prescription:*` - 处方缓存
     - `sessions:*` - 会话列表缓存
     - `session:*` - 会话详情缓存

4. **查看缓存值**
   - 点击键名，右侧会显示 JSON 格式的数据
   - 可以看到完整的会话或处方数据

5. **查看过期时间（TTL）**
   - 在键的详细信息中可以看到 TTL（Time To Live）
   - 处方缓存：3600 秒（1小时）
   - 会话缓存：1800 秒（30分钟）

---

## ✅ 验证缓存是否生效

### 方法 1：查看服务日志

**第一次查询（无缓存）**：
```
从数据库查询会话列表，patientId: 1
会话列表已存入 Redis 缓存，patientId: 1, 会话数量: 23
```

**第二次查询（有缓存）**：
```
从 Redis 缓存获取会话列表，patientId: 1, 会话数量: 23
```

### 方法 2：观察响应时间

- **无缓存**：500-1000ms（查询数据库）
- **有缓存**：10-50ms（从 Redis 读取）
- **提升倍数**：10-50 倍

### 方法 3：在 Redis Desktop Manager 中查看

1. 第一次查询后，应该能看到新的缓存键
2. 查看键的 TTL，确认过期时间正确
3. 查看键的值，确认数据正确

---

## 📊 当前已实现的 Redis 缓存

### 1. 处方查询缓存 ⭐⭐⭐⭐⭐
- **缓存键**：`prescription:患者ID:会话ID`
- **过期时间**：1 小时
- **位置**：`prescription-ai-service`
- **效果**：查询处方时，第二次查询速度提升 50 倍

### 2. 会话列表缓存 ⭐⭐⭐⭐（新增）
- **缓存键**：`sessions:patient:患者ID` 或 `sessions:all`
- **过期时间**：30 分钟
- **位置**：`patient-service`
- **效果**：查询会话列表时，第二次查询速度提升 10-20 倍

### 3. 会话详情缓存 ⭐⭐⭐（新增）
- **缓存键**：`session:会话ID`
- **过期时间**：30 分钟
- **位置**：`patient-service`
- **效果**：查询会话详情时，第二次查询速度提升 10-20 倍

---

## 🧪 测试步骤

### 完整测试流程

1. **启动所有服务**
   ```bash
   # 确保 Redis 运行
   redis-server
   
   # 启动后端服务
   cd 1126
   ./start-all-services.sh
   ```

2. **打开前端页面**
   - 访问：`http://localhost:5173/sessions`

3. **第一次查询会话列表**
   - 查看浏览器控制台或服务日志
   - 应该看到："从数据库查询会话列表"
   - 打开 Redis Desktop Manager，应该能看到 `sessions:all` 或 `sessions:patient:1`

4. **刷新页面（第二次查询）**
   - 查看日志，应该看到："从 Redis 缓存获取会话列表"
   - 响应时间应该明显更快

5. **查看会话详情**
   - 点击某个会话的"查看详情"
   - 第一次会写入 `session:会话ID` 缓存
   - 第二次查询会从缓存读取

6. **查看助诊报告**
   - 进入会话详情，查看助诊报告
   - 第一次会写入 `prescription:患者ID:会话ID` 缓存
   - 第二次查询会从缓存读取

---

## 🎯 预期结果

### 在 Redis Desktop Manager 中应该能看到：

```
db0
├── prescription:1:1040          (TTL: 3600秒)
├── prescription:1:1039          (TTL: 3600秒)
├── sessions:patient:1           (TTL: 1800秒)
├── sessions:all                 (TTL: 1800秒)
├── session:1040                 (TTL: 1800秒)
└── session:1039                 (TTL: 1800秒)
```

### 如果看不到数据，检查：

1. ✅ Redis 是否正在运行？
2. ✅ 是否已经触发过查询？
3. ✅ 服务日志是否有错误？
4. ✅ Redis 连接配置是否正确？

---

## 💡 提示

- **缓存是自动的**：不需要手动操作，查询数据时会自动写入
- **缓存会过期**：过期后会自动删除，重新查询时会再次写入
- **缓存会更新**：当数据变化时（如创建新会话），相关缓存会自动清除


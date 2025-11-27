# Postman 测试 Redis 缓存功能指南

## 📋 准备工作

1. **确保服务已启动**
   - `prescription-ai-service` 运行在 `http://localhost:8085`
   - `patient-service` 运行在 `http://localhost:8101`
   - Redis 服务器运行在 `localhost:6379`

2. **打开 Postman**

---

## 🧪 测试步骤

### 步骤 1：查询处方（第一次 - 会写入缓存）

**目的**：第一次查询会从数据库读取，并将结果存入 Redis 缓存

1. **创建新请求**
   - 点击 Postman 左上角 `+ New` → `HTTP Request`

2. **配置请求**
   - **方法**：选择 `GET`
   - **URL**：`http://localhost:8085/api/ai/prescriptions/query`
   - **注意**：如果通过网关访问，URL 是 `http://localhost:8888/api/ai/prescriptions/query`
   - **Params（参数）**：
     - `patientId`: `1` （替换为你的实际患者ID）
     - `sessionId`: `1` （替换为你的实际会话ID）

3. **发送请求**
   - 点击 `Send` 按钮
   - **观察响应时间**：应该比较慢（500ms 左右），因为是第一次查询数据库

4. **查看响应**
   ```json
   {
     "code": 0,
     "msg": "success",
     "data": {
       "prescriptions": [...],
       "message": "查询成功"
     }
   }
   ```

5. **查看服务日志**
   - 应该看到：`从数据库查询处方数据，patientId: 1, sessionId: 1`
   - 应该看到：`处方数据已存入 Redis 缓存，patientId: 1, sessionId: 1`

---

### 步骤 2：再次查询处方（第二次 - 从缓存读取）

**目的**：验证缓存是否生效，第二次查询应该很快

1. **使用同一个请求**
   - 直接点击 `Send` 再次发送（或者修改参数后发送）

2. **观察响应时间**
   - 应该非常快（10-50ms），因为从 Redis 读取

3. **查看服务日志**
   - 应该看到：`从 Redis 缓存获取处方数据，patientId: 1, sessionId: 1`
   - **不会看到**：`从数据库查询处方数据`（说明走的是缓存）

---

### 步骤 3：在 Redis Desktop Manager 中查看缓存

1. **打开 Another Redis Desktop Manager**

2. **连接到 Redis**
   - Host: `localhost`
   - Port: `6379`
   - Database: `0`

3. **查看缓存数据**
   - 在左侧找到 `db0`，展开
   - 查找键名：`prescription:1:1`（格式：`prescription:患者ID:会话ID`）
   - 点击该键，查看值（应该是处方的 JSON 数据）

4. **查看键的过期时间**
   - 键的 TTL（Time To Live）应该是 3600 秒（1小时）

---

### 步骤 4：测试缓存过期

**目的**：验证缓存会在 1 小时后过期

1. **等待 1 小时**（或者手动删除缓存键）

2. **再次查询**
   - 发送相同的请求
   - 应该会重新查询数据库并更新缓存

---

## 📊 性能对比测试

### 测试方法：连续发送 10 次请求

1. **在 Postman 中**
   - 选择请求
   - 点击右上角 `...` → `Run collection`（如果有 Collection）
   - 或者手动连续点击 `Send` 10 次

2. **观察响应时间**
   - **第一次**：~500ms（查数据库）
   - **后续 9 次**：~10-50ms（查 Redis）
   - **平均响应时间**：应该大幅降低

---

## 🔍 验证缓存是否生效的方法

### 方法 1：查看服务日志

**第一次查询（无缓存）**：
```
从数据库查询处方数据，patientId: 1, sessionId: 1
处方数据已存入 Redis 缓存，patientId: 1, sessionId: 1
```

**第二次查询（有缓存）**：
```
从 Redis 缓存获取处方数据，patientId: 1, sessionId: 1
```

### 方法 2：查看响应时间

- **无缓存**：500-1000ms
- **有缓存**：10-50ms
- **提升倍数**：10-50 倍

### 方法 3：在 Redis Desktop Manager 中查看

- 键名：`prescription:患者ID:会话ID`
- 值：处方的 JSON 数据
- TTL：3600 秒（1小时）

---

## 🛠️ 其他测试接口

### 测试 Redis 基础功能（patient-service）

1. **存储数据**
   - **方法**：`POST`
   - **URL**：`http://localhost:8101/api/redis/test/set`
   - **Params**：
     - `key`: `test_key`
     - `value`: `hello_redis`

2. **获取数据**
   - **方法**：`GET`
   - **URL**：`http://localhost:8101/api/redis/test/get`
   - **Params**：
     - `key`: `test_key`

3. **查看所有键**
   - **方法**：`GET`
   - **URL**：`http://localhost:8101/api/redis/test/keys`

---

## 📝 Postman Collection 配置示例

### 创建 Collection

1. 点击左侧 `Collections` → `+ New Collection`
2. 命名为：`Redis 缓存测试`

### 添加请求

1. **处方查询（第一次）**
   - Name: `查询处方 - 第一次`
   - Method: `GET`
   - URL: `http://localhost:8085/api/ai/prescriptions/query?patientId=1&sessionId=1`

2. **处方查询（第二次）**
   - Name: `查询处方 - 第二次（验证缓存）`
   - Method: `GET`
   - URL: `http://localhost:8085/api/ai/prescriptions/query?patientId=1&sessionId=1`

3. **Redis 测试 - 存储**
   - Name: `Redis 测试 - 存储数据`
   - Method: `POST`
   - URL: `http://localhost:8101/api/redis/test/set?key=test_key&value=hello_redis`

4. **Redis 测试 - 获取**
   - Name: `Redis 测试 - 获取数据`
   - Method: `GET`
   - URL: `http://localhost:8101/api/redis/test/get?key=test_key`

---

## ⚠️ 常见问题

### 1. 查询返回空数据

**原因**：数据库中可能没有该患者和会话的处方数据

**解决**：
- 先调用生成处方接口：`POST http://localhost:8085/api/ai/prescriptions/generate`
- 然后再查询

### 2. 看不到缓存数据

**原因**：
- Redis 连接失败
- 缓存键名不对

**解决**：
- 检查 Redis 是否运行：`redis-cli ping`
- 检查服务日志是否有错误

### 3. 响应时间没有提升

**原因**：
- Redis 未正确配置
- 缓存未生效

**解决**：
- 查看服务日志，确认是否有 "从 Redis 缓存获取" 的日志
- 检查 Redis 连接配置

---

## ✅ 成功标志

1. ✅ 第一次查询：日志显示 "从数据库查询"
2. ✅ 第二次查询：日志显示 "从 Redis 缓存获取"
3. ✅ 响应时间：第二次明显快于第一次
4. ✅ Redis Desktop Manager 中能看到缓存键
5. ✅ 缓存键的 TTL 为 3600 秒

---

## 🎯 测试目标

- [ ] 第一次查询成功（从数据库）
- [ ] 第二次查询成功（从缓存）
- [ ] 响应时间明显提升
- [ ] Redis Desktop Manager 中能看到缓存数据
- [ ] 缓存过期时间正确（1小时）

完成以上所有目标，说明 Redis 缓存功能正常工作！🎉


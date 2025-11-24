# 🚀 快速启动指南

## ✅ 启动前检查清单

### 1. 环境配置
- [x] JDK 17 已安装
- [x] Maven 已配置
- [x] Nacos Server 已启动（localhost:8080）✅
- [x] MySQL 数据库可访问（82.157.188.65:2000）

### 2. 配置信息确认

#### 数据库配置 ✅
```yaml
Host: 82.157.188.65
Port: 2000
Database: pool2
Username: root
Password: mysql_awJcEH
```

#### 通义千问 API ✅
```yaml
API Key: sk-e9a98e0c15064314addff519314d429b
Model: qwen-plus
```

#### 服务端口 ✅
```
test-suggestion-ai-service: 8084
```

---

## 📝 启动步骤

### 步骤 1: 启动 Nacos（如果还未启动）

#### Windows:
```bash
cd nacos/bin
startup.cmd -m standalone
```

#### Linux/Mac:
```bash
cd nacos/bin
sh startup.sh -m standalone
```

访问 Nacos 控制台：http://localhost:8848/nacos
- 用户名：nacos
- 密码：nacos

---

### 步骤 2: 编译项目

#### 在项目根目录执行：
```bash
# 清理并安装 common 模块
cd E:\3-1Teamwork\StellarMed
mvn clean install -pl common -am

# 编译 test-suggestion-ai-service
cd test-suggestion-ai-service
mvn clean compile
```

---

### 步骤 3: 运行服务

#### 方式一：使用 Maven 运行（推荐）
```bash
cd E:\3-1Teamwork\StellarMed\test-suggestion-ai-service
mvn spring-boot:run
```

#### 方式二：使用 IDEA 运行
1. 打开 `TestSuggestionAiServiceApplication.java`
2. 右键 → Run 'TestSuggestionAiServiceApplication'

---

### 步骤 4: 验证服务启动

#### 查看日志输出
看到以下内容表示启动成功：
```
========================================
✅ Test Suggestion AI Service Started Successfully!
========================================
```

#### 测试健康检查接口
```bash
curl http://localhost:8084/api/ai/test-suggestions/health
```

预期响应：
```json
{
  "code": 0,
  "msg": "success",
  "data": "Test Suggestion AI Service is running!"
}
```

#### 检查 Nacos 服务注册
访问 Nacos 控制台：http://localhost:8080/nacos
- 用户名/密码：nacos/nacos
- 在服务列表中应该能看到 `test-suggestion-ai-service`

---

## 🧪 功能测试

### 准备测试数据

在数据库中插入测试数据：

```sql
-- 1. 插入患者信息（如果没有）
INSERT INTO patient (id, name, gender, age, phone, created_time)
VALUES (1, '测试患者', '男', 30, '13800138000', NOW());

-- 2. 插入会话（如果没有）
INSERT INTO session (id, patient_id, status, created_time)
VALUES (1, 1, 'IN_PROGRESS', NOW());

-- 3. 插入结构化症状
INSERT INTO ai_symptom_structured 
(patient_id, session_id, symptom_name, severity, duration, create_time)
VALUES 
(1, 1, '发热', '高', '3天', NOW()),
(1, 1, '咳嗽', '中', '2天', NOW()),
(1, 1, '咽痛', '轻', '1天', NOW());

-- 4. 插入初步诊断
INSERT INTO ai_pre_diagnosis 
(patient_id, session_id, diagnosis, probability, reasoning, create_time)
VALUES (1, 1, '急性上呼吸道感染', 0.85, '基于发热、咳嗽等症状，考虑病毒或细菌感染', NOW());

-- 5. 插入风险评估
INSERT INTO ai_risk_assessment 
(patient_id, session_id, risk_level, reason, created_time)
VALUES (1, 1, 'MEDIUM', '症状较明显，需要进一步检查排除并发症', NOW());
```

### 测试生成检查建议

```bash
curl -X POST http://localhost:8084/api/ai/test-suggestions/generate \
  -H "Content-Type: application/json" \
  -d "{\"patientId\": 1, \"sessionId\": 1}"
```

### 查询检查建议

```bash
curl http://localhost:8084/api/ai/test-suggestions/session/1
```

---

## 🐛 常见问题排查

### 问题 1: 无法连接 Nacos

**症状：** 
```
Unable to connect to Nacos Server [localhost:8848]
```

**解决：**
1. 检查 Nacos 是否启动：访问 http://localhost:8848/nacos
2. 如果未启动，执行启动命令
3. 如果 Nacos 在其他地址，修改 `application.yml` 中的地址

### 问题 2: 数据库连接失败

**症状：**
```
Communications link failure
```

**解决：**
1. 检查网络是否能访问：`ping 82.157.188.65`
2. 检查端口是否开放：`telnet 82.157.188.65 2000`
3. 确认数据库账号密码正确
4. 检查数据库 `pool2` 是否存在

### 问题 3: 依赖找不到

**症状：**
```
Could not find artifact com.assist.common
```

**解决：**
```bash
# 先编译安装 common 模块
cd E:\3-1Teamwork\StellarMed
mvn clean install -pl common -am
```

### 问题 4: AI 调用失败

**症状：**
```
DASHSCOPE_API_KEY is invalid
```

**解决：**
1. 检查 API Key 是否正确配置在 `application.yml`
2. 确认 API Key 是否有效（登录阿里云控制台查看）
3. 检查网络是否能访问通义千问 API

### 问题 5: 生成的检查建议为空

**原因：**
- 数据库中没有对应的症状、诊断数据
- sessionId 不存在

**解决：**
1. 执行上面的测试数据 SQL
2. 确保 sessionId 存在且有关联数据

---

## 📊 监控和日志

### 查看日志
日志会输出到控制台，包含：
- 🔬 开始生成检查建议
- 📊 已获取数据统计
- 📝 构造的 Prompt
- 🤖 AI 模型返回结果
- ✅ 解析和保存成功信息
- ❌ 错误信息

### 日志级别
在 `application.yml` 中配置：
```yaml
logging:
  level:
    com.neusoft.neu23: debug  # 详细日志
    # com.neusoft.neu23: info  # 生产环境建议用 info
```

---

## 📞 需要帮助？

如果遇到问题：
1. 查看控制台日志
2. 查看 Nacos 服务列表，确认服务已注册
3. 检查数据库数据是否完整
4. 参考 README.md 中的详细说明

---

**祝运行顺利！🎉**


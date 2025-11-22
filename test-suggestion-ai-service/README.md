# Test Suggestion AI Service

## 📋 模块简介

**test-suggestion-ai-service** 是 StellarMed AI 助诊系统的检查建议生成模块。

### 核心功能
- 根据结构化症状、初步诊断和风险评估，通过 AI 生成医学检查建议
- 提供检查项目名称和检查理由
- 支持批量保存和查询检查建议

### 技术栈
- **Spring Boot 3.x**
- **Nacos** - 服务注册与配置中心
- **MyBatis-Plus** - 数据库 ORM
- **SpringAI + 通义千问** - AI 能力
- **MySQL** - 数据存储

---

## 🗂️ 项目结构

```
test-suggestion-ai-service/
├── src/main/java/com/neusoft/neu23/
│   ├── TestSuggestionAiServiceApplication.java  # 主启动类
│   ├── controller/
│   │   └── TestSuggestionController.java        # REST API 控制器
│   ├── service/
│   │   ├── TestSuggestionService.java           # 服务接口
│   │   └── impl/
│   │       └── TestSuggestionServiceImpl.java   # 服务实现（核心 AI 逻辑）
│   └── mapper/
│       ├── AiTestSuggestionMapper.java          # 检查建议 Mapper
│       ├── AiSymptomStructuredMapper.java       # 症状 Mapper（读取）
│       ├── AiPreDiagnosisMapper.java            # 诊断 Mapper（读取）
│       └── AiRiskAssessmentMapper.java          # 风险评估 Mapper（读取）
├── src/main/resources/
│   ├── application.yml                           # 应用配置
│   ├── bootstrap.yml                             # 启动配置
│   └── mapper/
│       └── AiTestSuggestionMapper.xml           # MyBatis XML 映射
└── pom.xml                                       # Maven 依赖配置
```

---

## 🚀 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Nacos Server 2.x

### 2. 配置说明

#### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://82.157.188.65:2000/pool2
    username: root
    password: mysql_awJcEH
```

#### 通义千问 API Key
```yaml
spring:
  ai:
    dashscope:
      api-key: sk-e9a98e0c15064314addff519314d429b
```

#### Nacos 配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8080
        namespace: dev
```

### 3. 启动服务

#### 方式一：IDEA 运行
1. 确保 Nacos 已启动
2. 运行 `TestSuggestionAiServiceApplication.java`
3. 看到以下输出表示启动成功：
```
========================================
✅ Test Suggestion AI Service Started Successfully!
========================================
```

#### 方式二：Maven 命令
```bash
cd test-suggestion-ai-service
mvn clean spring-boot:run
```

#### 方式三：打包运行
```bash
mvn clean package
java -jar target/test-suggestion-ai-service-1.0-SNAPSHOT.jar
```

### 4. 验证服务

访问健康检查接口：
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

---

## 📡 API 接口

### 1. 生成检查建议

**接口：** `POST /api/ai/test-suggestions/generate`

**请求示例：**
```json
{
  "patientId": 1,
  "sessionId": 1
}
```

**响应示例：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "testSuggestions": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1,
        "testName": "血常规",
        "reason": "检查白细胞计数，判断是否存在细菌感染",
        "createdTime": "2025-11-21T10:30:00"
      },
      {
        "id": 2,
        "patientId": 1,
        "sessionId": 1,
        "testName": "CRP（C反应蛋白）",
        "reason": "评估炎症程度，辅助判断感染类型",
        "createdTime": "2025-11-21T10:30:00"
      }
    ],
    "message": "检查建议生成成功"
  }
}
```

### 2. 查询检查建议

**接口：** `GET /api/ai/test-suggestions/session/{sessionId}`

**请求示例：**
```bash
curl http://localhost:8084/api/ai/test-suggestions/session/1
```

**响应格式：** 同上

---

## 🔄 工作流程

1. **接收请求**：Controller 接收 `patientId` 和 `sessionId`

2. **读取数据**：从数据库读取
   - 结构化症状（`ai_symptom_structured`）
   - 初步诊断（`ai_pre_diagnosis`）
   - 风险评估（`ai_risk_assessment`）

3. **构造 Prompt**：将症状、诊断、风险信息整合成结构化的 AI Prompt

4. **调用 AI 模型**：使用 SpringAI 调用通义千问 API

5. **解析响应**：将 AI 返回的 JSON 解析为检查建议列表

6. **保存数据库**：批量插入到 `ai_test_suggestion` 表

7. **返回结果**：返回标准化 API 响应

---

## 🗄️ 数据库表结构

### ai_test_suggestion

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 主键，自增 |
| patient_id | INT | 患者ID |
| session_id | INT | 会话ID |
| test_name | VARCHAR | 检查项目名称 |
| reason | VARCHAR | 检查理由 |
| created_time | TIMESTAMP | 创建时间 |

---

## 🧪 测试建议

### 完整测试流程

1. **启动依赖服务**
   - MySQL 数据库
   - Nacos 注册中心

2. **准备测试数据**
   ```sql
   -- 插入结构化症状
   INSERT INTO ai_symptom_structured 
   (patient_id, session_id, symptom_name, severity, duration, create_time)
   VALUES (1, 1, '发热', '高', '3天', NOW());
   
   -- 插入初步诊断
   INSERT INTO ai_pre_diagnosis 
   (patient_id, session_id, diagnosis, probability, reasoning, create_time)
   VALUES (1, 1, '急性上呼吸道感染', 0.85, '基于发热症状', NOW());
   
   -- 插入风险评估
   INSERT INTO ai_risk_assessment 
   (patient_id, session_id, risk_level, reason, created_time)
   VALUES (1, 1, 'MEDIUM', '症状需要进一步检查', NOW());
   ```

3. **调用生成接口**
   ```bash
   curl -X POST http://localhost:8084/api/ai/test-suggestions/generate \
     -H "Content-Type: application/json" \
     -d '{"patientId": 1, "sessionId": 1}'
   ```

4. **查询结果**
   ```bash
   curl http://localhost:8084/api/ai/test-suggestions/session/1
   ```

---

## 🐛 常见问题

### 1. 服务启动失败

**问题：** `Cannot connect to MySQL`

**解决：**
- 检查数据库连接配置是否正确
- 确认数据库服务是否启动
- 测试网络连接：`ping 82.157.188.65`

### 2. Nacos 连接失败

**问题：** `Unable to connect to Nacos Server`

**解决：**
- 启动 Nacos：`sh startup.sh -m standalone`
- 检查 Nacos 地址配置是否正确

### 3. AI 调用失败

**问题：** `DASHSCOPE_API_KEY is invalid`

**解决：**
- 检查 API Key 是否配置正确
- 确认 API Key 是否有效且未过期

### 4. 未生成检查建议

**问题：** 返回空列表

**可能原因：**
- 数据库中没有结构化症状数据
- sessionId 不存在或数据不完整

**解决：**
- 先调用 symptom-ai-service 生成结构化症状
- 确保数据库中有对应的 session 数据

---

## 📞 联系方式

- **Team:** StellarMed Development Team
- **Project:** AI 助诊系统
- **Module:** test-suggestion-ai-service

---

## 📄 License

Copyright © 2025 StellarMed Team. All rights reserved.


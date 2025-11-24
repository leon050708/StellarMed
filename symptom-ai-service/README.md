# Symptom AI Service - 症状AI服务

## 🚀 快速开始

### 1. 启动服务

```bash
mvn spring-boot:run
```

服务将在 `http://localhost:8201` 启动

### 2. 测试接口

**接口：** `POST /api/ai/symptoms/extract`

**请求示例：**
```bash
curl -X POST http://localhost:8201/api/ai/symptoms/extract \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "sessionId": 1001
  }'
```

### 3. 前置条件

- ✅ MySQL 数据库已启动并创建表结构
- ✅ Nacos 服务已启动（可选，如果使用服务发现）
- ✅ 通义千问 API Key 已配置在 `application.yml` 中

## 📝 测试数据准备

在测试前，需要在数据库中准备测试数据：

```sql
-- 1. 创建测试患者
INSERT INTO patient (id, name, age, gender, case_number, create_time) 
VALUES (1, '测试患者', '25', 'MALE', 'TEST-001', NOW());

-- 2. 创建测试会话
INSERT INTO session (session_id, patient_id, status, created_time) 
VALUES (1001, 1, 'ACTIVE', NOW());

-- 3. 创建测试聊天记录
INSERT INTO chat_record (patient_id, session_id, question, ai_reply, timestamp) 
VALUES (1, 1001, '我发烧两天了，最高体温39度', '请问还有其他症状吗？', NOW());

-- 4. 创建测试原始症状
INSERT INTO symptom_record (patient_id, session_id, symptom_text, severity, duration, extracted_time) 
VALUES (1, 1001, '发热', 'moderate', '2天', NOW());
```

## 🔧 配置说明

### application.yml 关键配置

- **端口：** 8201
- **服务名：** symptom-ai-service
- **数据库：** 已配置远程数据库
- **SpringAI：** 使用 OpenAI 兼容模式连接通义千问

## 📊 接口说明

### POST /api/ai/symptoms/extract

**功能：** 提取结构化症状

**请求体：**
```json
{
  "patientId": 1,
  "sessionId": 1001
}
```

**响应：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "structuredSymptoms": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "symptomName": "发热",
        "severity": "severe",
        "duration": "2天",
        "extraInfo": "最高体温39℃",
        "createTime": "2025-11-21T10:00:00"
      }
    ],
    "message": "症状结构化完成"
  }
}
```

## 🐛 常见问题

1. **AI 返回格式不正确**
   - 检查提示词配置
   - 查看日志中的 AI 返回内容

2. **数据库连接失败**
   - 检查数据库配置
   - 确保数据库服务已启动

3. **Nacos 连接失败**
   - 可以暂时注释掉 Nacos 配置
   - 或启动 Nacos 服务


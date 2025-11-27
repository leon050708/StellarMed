# StellarMed API接口完整文档

## 统一响应格式

所有接口返回统一格式：
```json
{
  "code": 0,        // 0表示成功，非0表示失败
  "msg": "success", // 响应消息
  "data": {}        // 响应数据
}
```

---

## 一、患者服务 (Patient Service) - 端口 8101

### 1. 创建或更新患者
**接口**: POST /api/patients  
**作用**: 创建新患者或更新已有患者信息

**请求参数** (请求体):
```json
{
  "id": 1,                    // 可选，更新时提供
  "name": "张三",              // 患者姓名
  "age": "35",                // 年龄
  "gender": "男",              // 性别
  "phoneNumber": "13800138000", // 手机号
  "idCard": "110101199001011234", // 身份证号
  "height": "175",            // 身高(cm)
  "weight": "70",             // 体重(kg)
  "caseNumber": "CASE001"     // 病例号
}
```

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": 1  // 患者ID
}
```

---

### 2. 获取所有患者列表
**接口**: GET /api/patients  
**作用**: 查询所有患者信息

**请求参数**: 无

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "age": "35",
      "gender": "男",
      "phoneNumber": "13800138000",
      "idCard": "110101199001011234",
      "height": "175",
      "weight": "70",
      "caseNumber": "CASE001",
      "createTime": "2025-01-01T10:00:00"
    }
  ]
}
```

---

### 3. 根据病例号查询患者
**接口**: GET /api/patients/by-case-number  
**作用**: 通过病例号查询患者信息

**请求参数** (Query参数):
- caseNumber: String (必需) - 病例号

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "name": "张三",
    "age": "35",
    "gender": "男",
    "phoneNumber": "13800138000",
    "idCard": "110101199001011234",
    "height": "175",
    "weight": "70",
    "caseNumber": "CASE001",
    "createTime": "2025-01-01T10:00:00"
  }
}
```

---

### 4. 创建会话
**接口**: POST /api/sessions  
**作用**: 为患者创建新的问诊会话

**请求参数** (Query参数):
- patientId: Integer (必需) - 患者ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "sessionId": 1001,
    "patientId": 1,
    "createdTime": "2025-01-01T10:00:00",
    "status": "active"
  }
}
```

---

### 5. 获取会话列表
**接口**: GET /api/sessions  
**作用**: 查询会话列表，支持按患者ID筛选

**请求参数** (Query参数):
- patientId: Integer (可选) - 患者ID，如果提供则只返回该患者的会话，不提供则返回所有会话

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "sessionId": 1001,
      "patientId": 1,
      "createdTime": "2025-01-01T10:00:00",
      "status": "active"
    },
    {
      "sessionId": 1002,
      "patientId": 1,
      "createdTime": "2025-01-01T09:00:00",
      "status": "completed"
    }
  ]
}
```

---

### 6. 根据会话ID获取会话详情
**接口**: GET /api/sessions/{sessionId}  
**作用**: 根据会话ID查询单个会话的详细信息

**请求参数** (路径参数):
- sessionId: Integer (必需) - 会话ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "sessionId": 1025,
    "patientId": 1,
    "createdTime": "2025-01-01T10:00:00",
    "status": "active"
  }
}
```

**错误情况** (会话不存在):
```json
{
  "code": -1,
  "msg": "会话不存在",
  "data": null
}
```

---

### 7. 关闭会话
**接口**: PATCH /api/sessions/{sessionId}/close  
**作用**: 关闭指定的问诊会话

**请求参数** (路径参数):
- sessionId: Integer (必需) - 会话ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 8. 保存聊天记录
**接口**: POST /api/chats  
**作用**: 保存患者与AI的对话记录

**请求参数** (请求体):
```json
{
  "chatId": 1,              // 可选，更新时提供
  "patientId": 1,           // 患者ID
  "sessionId": 1001,        // 会话ID
  "timestamp": "2025-01-01T10:00:00", // 时间戳
  "question": "我头疼",      // 患者问题
  "aiReply": "请详细描述一下头痛的情况" // AI回复
}
```

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": 1  // 聊天记录ID
}
```

---

### 9. AI对话
**接口**: POST /api/chat  
**作用**: 与患者进行实时AI对话，获取AI回复

**请求参数** (Query参数):
- sessionId: Integer (必需) - 会话ID
- patientId: Integer (必需) - 患者ID
- question: String (必需) - 患者问题

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "请详细描述一下头痛的情况，比如疼痛的位置、持续时间等"
}
```

---

### 10. 获取对话历史
**接口**: GET /api/chat/history/{sessionId}  
**作用**: 查询指定会话的所有对话记录

**请求参数** (路径参数):
- sessionId: Integer (必需) - 会话ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "chatId": 1,
      "patientId": 1,
      "sessionId": 1001,
      "timestamp": "2025-01-01T10:00:00",
      "question": "我头疼",
      "aiReply": "请详细描述一下头痛的情况"
    }
  ]
}
```

---

### 11. 记录原始症状
**接口**: POST /api/symptoms  
**作用**: 保存患者输入的原始症状文本

**请求参数** (请求体):
```json
{
  "symptomId": 1,           // 可选，更新时提供
  "patientId": 1,           // 患者ID
  "sessionId": 1001,        // 会话ID
  "symptomText": "头痛三天，伴有发热", // 症状文本
  "severity": "中等",        // 严重程度
  "duration": "3天",        // 持续时间
  "extractedTime": "2025-01-01T10:00:00" // 提取时间
}
```

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": 1  // 症状记录ID
}
```

---

## 二、症状AI服务 (Symptom AI Service) - 端口 8201

### 1. AI抽取结构化症状
**接口**: POST /api/ai/symptoms/extract  
**作用**: 使用AI将患者输入的症状文本提取为结构化症状数据

**请求参数** (请求体):
```json
{
  "patientId": 1,            // 患者ID (必需)
  "sessionId": 1001,         // 会话ID (必需)
  "symptomText": "头痛三天，伴有发热，体温38度" // 症状文本 (必需)
}
```

**返回值**:
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
        "symptomName": "头痛",
        "severity": "中等",
        "duration": "3天",
        "extraInfo": "伴有发热",
        "createTime": "2025-01-01T10:00:00"
      },
      {
        "id": 2,
        "patientId": 1,
        "sessionId": 1001,
        "symptomName": "发热",
        "severity": "中等",
        "duration": "3天",
        "extraInfo": "体温38度",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "message": "症状结构化提取成功"
  }
}
```

---

### 2. 健康检查
**接口**: GET /api/ai/health  
**作用**: 检查服务健康状态

**请求参数**: 无

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "Symptom AI Service is running!"
}
```

---

## 三、诊断AI服务 (Diagnosis AI Service) - 端口 8202

### 1. AI诊断和风险评估（合并接口）
**接口**: POST /api/ai/diagnosis/evaluate  
**作用**: 基于患者症状进行AI初步诊断和风险评估，同时返回诊断列表和风险评估结果

**请求参数** (请求体):
```json
{
  "patientId": 1,    // 患者ID (必需)
  "sessionId": 1001  // 会话ID (必需)
}
```

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "diagnoses": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "diagnosis": "上呼吸道感染",
        "probability": 0.85,
        "reasoning": "根据患者头痛、发热症状，结合病史分析",
        "createTime": "2025-01-01T10:00:00"
      },
      {
        "id": 2,
        "patientId": 1,
        "sessionId": 1001,
        "diagnosis": "病毒性感冒",
        "probability": 0.75,
        "reasoning": "症状符合病毒性感冒特征",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "riskAssessment": {
      "id": 1,
      "patientId": 1,
      "sessionId": 1001,
      "riskLevel": "低风险",
      "reason": "症状较轻，无严重并发症风险",
      "createdTime": "2025-01-01T10:00:00"
    },
    "message": "诊断和风险评估完成"
  }
}
```

---

### 2. 健康检查
**接口**: GET /api/ai/health  
**作用**: 检查服务健康状态

**请求参数**: 无

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "Diagnosis AI Service is running!"
}
```

---

## 四、检查建议AI服务 (Test Suggestion AI Service) - 端口 8083

### 1. 生成检查建议
**接口**: POST /api/ai/test-suggestions/generate  
**作用**: 基于患者症状、诊断和风险评估，生成AI检查建议

**请求参数** (请求体):
```json
{
  "patientId": 1,    // 患者ID (必需)
  "sessionId": 1001  // 会话ID (必需)
}
```

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "testSuggestions": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "testName": "血常规",
        "reason": "检查白细胞计数，判断是否存在细菌感染",
        "createdTime": "2025-01-01T10:00:00"
      },
      {
        "id": 2,
        "patientId": 1,
        "sessionId": 1001,
        "testName": "CRP（C反应蛋白）",
        "reason": "评估炎症程度，辅助判断感染类型",
        "createdTime": "2025-01-01T10:00:00"
      }
    ],
    "message": "检查建议生成成功"
  }
}
```

---

### 2. 查询检查建议
**接口**: GET /api/ai/test-suggestions/session/{sessionId}  
**作用**: 根据会话ID查询已生成的检查建议

**请求参数** (路径参数):
- sessionId: Integer (必需) - 会话ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "testSuggestions": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "testName": "血常规",
        "reason": "检查白细胞计数，判断是否存在细菌感染",
        "createdTime": "2025-01-01T10:00:00"
      }
    ],
    "message": "查询成功"
  }
}
```

---

### 3. 重新生成检查建议
**接口**: POST /api/ai/test-suggestions/regenerate  
**作用**: 删除旧的检查建议，重新生成新的检查建议（用于症状/诊断更新后）

**请求参数** (请求体):
```json
{
  "patientId": 1,    // 患者ID (必需)
  "sessionId": 1001  // 会话ID (必需)
}
```

**返回值**: 同"生成检查建议"接口

---

### 4. 健康检查
**接口**: GET /api/ai/test-suggestions/health  
**作用**: 检查服务健康状态

**请求参数**: 无

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "Test Suggestion AI Service is running!"
}
```

---

## 五、总结AI服务 (Summary AI Service) - 端口 8204

### 1. 生成会话总结
**接口**: POST /api/ai/session-summary/generate 或 GET /api/ai/session-summary/generate  
**作用**: 基于整个问诊会话生成AI总结报告

**请求参数** (Query参数):
- patientId: Integer (必需) - 患者ID
- sessionId: Integer (必需) - 会话ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "summaryId": 1,
    "patientId": 1,
    "sessionId": 1001,
    "summaryText": "患者主诉头痛三天，伴有发热，体温38度。AI初步诊断为上呼吸道感染，概率85%。建议进行血常规和CRP检查。",
    "reasoningChain": "基于症状提取结果，结合诊断和风险评估，生成综合总结",
    "createdTime": "2025-01-01T10:00:00"
  }
}
```

---

### 2. 生成会话总结并返回原始数据
**接口**: POST /api/ai/session-summary/generate-with-data  
**作用**: 生成会话总结，同时返回所有原始数据（症状、诊断、风险、检查建议、总结），供医生确认服务使用

**请求参数** (Query参数):
- patientId: Integer (必需) - 患者ID
- sessionId: Integer (必需) - 会话ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "symptoms": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "symptomName": "头痛",
        "severity": "中等",
        "duration": "3天",
        "extraInfo": "伴有发热",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "diagnoses": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "diagnosis": "上呼吸道感染",
        "probability": 0.85,
        "reasoning": "根据患者症状分析",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "riskAssessment": {
      "id": 1,
      "patientId": 1,
      "sessionId": 1001,
      "riskLevel": "低风险",
      "reason": "症状较轻",
      "createdTime": "2025-01-01T10:00:00"
    },
    "testSuggestions": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "testName": "血常规",
        "reason": "检查白细胞计数",
        "createdTime": "2025-01-01T10:00:00"
      }
    ],
    "sessionSummary": {
      "summaryId": 1,
      "patientId": 1,
      "sessionId": 1001,
      "summaryText": "患者主诉头痛三天...",
      "reasoningChain": "基于症状提取结果...",
      "createdTime": "2025-01-01T10:00:00"
    }
  }
}
```

---

## 六、处方AI服务 (Prescription AI Service) - 端口 8085

### 1. 生成处方建议
**接口**: POST /api/ai/prescriptions/generate  
**作用**: 基于患者症状、诊断、风险评估和检查建议，生成AI处方建议

**请求参数** (请求体):
```json
{
  "patientId": 1,    // 患者ID (必需)
  "sessionId": 1001  // 会话ID (必需)
}
```

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "prescriptions": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "drugName": "阿莫西林胶囊",
        "dosage": "500mg",
        "duration": "7天",
        "usageInstruction": "口服，每日三次，每次1粒",
        "reason": "针对细菌感染，有效控制炎症",
        "createTime": "2025-01-01T10:00:00"
      },
      {
        "id": 2,
        "patientId": 1,
        "sessionId": 1001,
        "drugName": "布洛芬缓释胶囊",
        "dosage": "300mg",
        "duration": "3天",
        "usageInstruction": "口服，每日两次，每次1粒",
        "reason": "缓解头痛和发热症状",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "message": "处方建议生成成功"
  }
}
```

---

### 2. 查询处方建议
**接口**: GET /api/ai/prescriptions/query  
**作用**: 根据患者ID和会话ID查询已生成的处方建议

**请求参数** (Query参数):
- patientId: Integer (必需) - 患者ID
- sessionId: Integer (必需) - 会话ID

**返回值**: 同"生成处方建议"接口

---

## 七、医生确认服务 (Doctor Confirm Service) - 端口 8301

### 1. 聚合查询助诊报告
**接口**: GET /api/doctor-confirm/ai-report  
**作用**: 聚合查询所有AI分析结果，包括患者信息、会话信息、症状、诊断、风险评估、检查建议、总结、处方等

**请求参数** (Query参数):
- patientId: Integer (必需) - 患者ID
- sessionId: Integer (必需) - 会话ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "patient": {
      "id": 1,
      "name": "张三",
      "age": "35",
      "gender": "男",
      "phoneNumber": "13800138000",
      "idCard": "110101199001011234",
      "height": "175",
      "weight": "70",
      "caseNumber": "CASE001",
      "createTime": "2025-01-01T10:00:00"
    },
    "session": {
      "sessionId": 1001,
      "patientId": 1,
      "createdTime": "2025-01-01T10:00:00",
      "status": "active"
    },
    "symptoms": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "symptomName": "头痛",
        "severity": "中等",
        "duration": "3天",
        "extraInfo": "伴有发热",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "diagnoses": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "diagnosis": "上呼吸道感染",
        "probability": 0.85,
        "reasoning": "根据患者症状分析",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "riskAssessment": {
      "id": 1,
      "patientId": 1,
      "sessionId": 1001,
      "riskLevel": "低风险",
      "reason": "症状较轻",
      "createdTime": "2025-01-01T10:00:00"
    },
    "testSuggestions": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "testName": "血常规",
        "reason": "检查白细胞计数",
        "createdTime": "2025-01-01T10:00:00"
      }
    ],
    "sessionSummary": {
      "summaryId": 1,
      "patientId": 1,
      "sessionId": 1001,
      "summaryText": "患者主诉头痛三天...",
      "reasoningChain": "基于症状提取结果...",
      "createdTime": "2025-01-01T10:00:00"
    },
    "prescriptions": [
      {
        "id": 1,
        "patientId": 1,
        "sessionId": 1001,
        "drugName": "阿莫西林胶囊",
        "dosage": "500mg",
        "duration": "7天",
        "usageInstruction": "口服，每日三次，每次1粒",
        "reason": "针对细菌感染",
        "createTime": "2025-01-01T10:00:00"
      }
    ]
  }
}
```

---

### 2. 医生最终确认诊断
**接口**: POST /api/doctor-confirm/final-diagnosis  
**作用**: 保存医生最终确认的诊断、处方等信息

**请求参数** (请求体):
```json
{
  "patientId": 1,              // 患者ID (必需)
  "sessionId": 1001,           // 会话ID (必需)
  "doctorId": 101,             // 医生ID (必需)
  "finalDiagnosis": "上呼吸道感染，建议进一步检查", // 最终诊断 (必需)
  "finalPrescription": "阿莫西林胶囊 500mg 每日三次 连服7天", // 最终处方 (必需)
  "comment": "患者症状较轻，建议观察" // 备注 (可选)
}
```

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

---

### 3. 获取AI生成的确认建议
**接口**: GET /api/doctor-confirm/ai/suggestion  
**作用**: 基于聚合的助诊报告，生成AI建议供医生参考

**请求参数** (Query参数):
- patientId: Integer (必需) - 患者ID
- sessionId: Integer (必需) - 会话ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "根据AI分析，建议重点关注上呼吸道感染的可能性，建议进行血常规检查以确认感染类型。风险评估为低风险，可考虑保守治疗。"
}
```

---

### 4. 获取诊断对比分析
**接口**: GET /api/doctor-confirm/ai/compare  
**作用**: 对比AI诊断和医生最终诊断的差异

**请求参数** (Query参数):
- sessionId: Integer (必需) - 会话ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "AI诊断：上呼吸道感染（概率85%），医生最终诊断：上呼吸道感染。诊断一致，AI建议合理。"
}
```

---

### 5. 获取诊断合理性评估
**接口**: GET /api/doctor-confirm/ai/evaluate  
**作用**: 评估AI诊断的合理性，提供风险提示

**请求参数** (Query参数):
- patientId: Integer (必需) - 患者ID
- sessionId: Integer (必需) - 会话ID

**返回值**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "AI诊断基于症状分析，概率较高。建议结合检查结果进一步确认。风险评估为低风险，治疗方案合理。"
}
```

---

## 八、网关服务 (Gateway Service) - 端口 8888

所有上述接口都通过网关访问，网关地址：http://localhost:8888

**示例**:
- 患者服务: http://localhost:8888/api/patients
- 症状AI: http://localhost:8888/api/ai/symptoms/extract
- 诊断AI: http://localhost:8888/api/ai/diagnosis/evaluate
- 医生确认: http://localhost:8888/api/doctor-confirm/ai-report

网关已配置CORS，支持跨域访问。

---

## 九、基础设施服务

### Nacos 服务发现
**端口**: 8848  
**访问地址**: http://localhost:8848  
**管理界面**: http://localhost:8848/nacos  
**默认账号**: nacos/nacos  
**作用**: 服务注册与发现中心，所有微服务都注册到此

---

## 注意事项

1. 所有接口都返回统一格式：`{code, msg, data}`
2. code=0 表示成功，非0表示失败
3. 所有AI服务接口处理时间可能较长，建议设置超时时间30秒以上
4. 前端统一通过网关(8888端口)访问所有服务
5. 网关已配置CORS，支持跨域访问
6. 所有必需参数必须提供，否则会返回参数错误
7. 日期时间格式统一为 ISO 8601 格式：`2025-01-01T10:00:00`


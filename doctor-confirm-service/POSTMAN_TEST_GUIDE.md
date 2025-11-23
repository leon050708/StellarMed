# Doctor Confirm Service - Postman 测试指南

## 📋 测试集合说明

本测试集合包含 `doctor-confirm-service` 的所有接口测试用例。

## 🚀 快速开始

### 1. 导入测试集合

1. 打开 Postman
2. 点击左上角 `Import` 按钮
3. 选择文件 `Doctor-Confirm-Service.postman_collection.json`
4. 导入成功后，在左侧 Collections 中可以看到 "Doctor Confirm Service API"

### 2. 配置环境变量（可选）

测试集合已包含默认变量，如需修改：

- `baseUrl`: http://localhost:8301
- `patientId`: 1
- `sessionId`: 1001
- `doctorId`: 101

## 📝 接口列表

### 1. 聚合查询助诊报告
**接口**: `GET /api/doctor-confirm/ai-report`

**说明**: 按照 2→3→4→5→6 顺序调用各个AI服务，聚合所有AI助诊数据。

**调用顺序**:
- 步骤2: symptom-ai-service (症状结构化)
- 步骤3: diagnosis-ai-service (初步诊断 + 风险评估)
- 步骤4: test-suggestion-ai-service (检查建议)
- 步骤5: summary-ai-service (问诊总结)
- 步骤6: prescription-ai-service (处方建议)

**请求参数**:
- `patientId`: 患者ID (必填)
- `sessionId`: 会话ID (必填)

**示例请求**:
```
GET http://localhost:8301/api/doctor-confirm/ai-report?patientId=1&sessionId=1001
```

**预期响应**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "patient": null,
    "session": null,
    "symptoms": [...],
    "diagnoses": [...],
    "riskAssessment": {...},
    "testSuggestions": [...],
    "sessionSummary": {...},
    "prescriptions": [...]
  }
}
```

---

### 2. 医生最终确认诊断
**接口**: `POST /api/doctor-confirm/final-diagnosis`

**说明**: 医生查看AI助诊报告后，输入最终诊断、处方等信息并保存到数据库。

**请求体**:
```json
{
  "patientId": 1,
  "sessionId": 1001,
  "doctorId": 101,
  "finalDiagnosis": "急性上呼吸道感染",
  "finalPrescription": "1. 阿莫西林胶囊 0.5g 每日3次，饭后服用，连续7天\n2. 布洛芬缓释胶囊 0.3g 每日2次，饭后服用，连续3天\n3. 多喝水，注意休息",
  "comment": "患者症状典型，建议按处方服药，如3天后症状未缓解，请及时复诊。"
}
```

**字段说明**:
- `patientId`: 患者ID (必填)
- `sessionId`: 会话ID (必填)
- `doctorId`: 医生ID (必填)
- `finalDiagnosis`: 最终诊断 (可选)
- `finalPrescription`: 最终处方 (可选)
- `comment`: 备注 (可选)

**预期响应**:
```json
{
  "code": 0,
  "msg": "医生最终确认保存成功",
  "data": null
}
```

---

### 3. 获取AI生成的确认建议
**接口**: `GET /api/doctor-confirm/ai/suggestion`

**说明**: 基于聚合的助诊报告，生成AI建议供医生参考。包括诊断合理性分析、风险点、用药注意事项等。

**请求参数**:
- `patientId`: 患者ID (必填)
- `sessionId`: 会话ID (必填)

**示例请求**:
```
GET http://localhost:8301/api/doctor-confirm/ai/suggestion?patientId=1&sessionId=1001
```

**预期响应**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "AI生成的确认建议文本..."
}
```

---

### 4. 获取诊断对比分析
**接口**: `GET /api/doctor-confirm/ai/compare`

**说明**: 对比AI诊断和医生最终诊断的差异，帮助医生了解AI诊断的准确性。

**⚠️ 注意**: 需要先调用接口2保存医生确认结果，否则会返回错误。

**请求参数**:
- `sessionId`: 会话ID (必填)

**示例请求**:
```
GET http://localhost:8301/api/doctor-confirm/ai/compare?sessionId=1001
```

**预期响应**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "诊断对比分析文本..."
}
```

**错误响应** (未找到医生确认记录):
```json
{
  "code": 1,
  "msg": "未找到该会话的最终诊断记录",
  "data": null
}
```

---

### 5. 获取诊断合理性评估
**接口**: `GET /api/doctor-confirm/ai/evaluate`

**说明**: 评估AI诊断的合理性，提供风险提示，帮助医生判断是否需要进一步检查或调整诊断。

**请求参数**:
- `patientId`: 患者ID (必填)
- `sessionId`: 会话ID (必填)

**示例请求**:
```
GET http://localhost:8301/api/doctor-confirm/ai/evaluate?patientId=1&sessionId=1001
```

**预期响应**:
```json
{
  "code": 0,
  "msg": "success",
  "data": "诊断合理性评估文本..."
}
```

## 🔄 测试流程建议

### 完整测试流程

1. **第一步**: 调用接口1获取AI助诊报告
   - 验证是否能成功调用所有23456服务
   - 检查返回的数据结构是否完整

2. **第二步**: 调用接口3获取AI确认建议
   - 验证AI建议生成功能

3. **第三步**: 调用接口5获取诊断合理性评估
   - 验证风险评估功能

4. **第四步**: 调用接口2保存医生确认结果
   - 验证数据保存功能
   - 注意：需要提供有效的 patientId, sessionId, doctorId

5. **第五步**: 调用接口4获取诊断对比分析
   - 验证对比分析功能
   - 注意：必须先完成第四步

## ⚠️ 注意事项

1. **服务依赖**: 接口1需要依赖以下服务正常运行：
   - symptom-ai-service (服务2)
   - diagnosis-ai-service (服务3)
   - test-suggestion-ai-service (服务4)
   - summary-ai-service (服务5)
   - prescription-ai-service (服务6)

2. **数据准备**: 
   - 确保数据库中有对应的 patientId 和 sessionId 数据
   - 确保各个AI服务已正确配置并能正常响应

3. **接口顺序**:
   - 接口4必须在接口2之后调用（需要医生确认数据）
   - 其他接口可以独立调用

4. **错误处理**:
   - 如果某个AI服务不可用，接口1会返回部分数据（失败的字段为空或空列表）
   - 检查响应中的 `code` 字段判断是否成功（0表示成功）

## 🐛 常见问题

### Q1: 接口1返回空数据
**原因**: 可能是某个AI服务未启动或配置错误
**解决**: 检查各个AI服务的运行状态和日志

### Q2: 接口4返回"未找到该会话的最终诊断记录"
**原因**: 未先调用接口2保存医生确认结果
**解决**: 先调用接口2保存数据，再调用接口4

### Q3: Feign调用超时
**原因**: AI服务响应时间过长或服务不可用
**解决**: 检查AI服务状态，调整Feign超时配置（application.yaml中的feign.client.config.default）

## 📞 技术支持

如有问题，请检查：
1. 服务日志: `doctor-confirm-service` 的日志输出
2. 服务状态: 确认所有依赖服务正常运行
3. 网络连接: 确认服务间网络连通性


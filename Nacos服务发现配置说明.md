# Nacos 服务发现配置说明

## ✅ 配置完成

所有服务的 Nacos 服务发现已启用！

## 📋 已修改的服务配置

### 1. patient-service (8101)
- ✅ `spring.cloud.nacos.discovery.enabled: true`
- ✅ 移除了 Feign 配置中的直接 URL，改为使用服务发现
- ✅ 命名空间: `public`
- ✅ 组: `DEFAULT_GROUP`

### 2. symptom-ai-service (8201)
- ✅ `spring.cloud.nacos.discovery.enabled: true`
- ✅ 命名空间: `public`
- ✅ 组: `DEFAULT_GROUP`

### 3. test-suggestion-ai-service (8083)
- ✅ `spring.cloud.nacos.discovery.enabled: true` (原本已启用)
- ✅ 命名空间: `public`
- ✅ 组: `DEFAULT_GROUP`

### 4. summary-ai-service (8204)
- ✅ `spring.cloud.nacos.discovery.enabled: true`
- ✅ `spring.cloud.nacos.discovery.watch.enabled: true`
- ✅ 命名空间: `public`
- ✅ 组: `DEFAULT_GROUP`

### 5. prescription-ai-service (8085)
- ✅ `spring.cloud.nacos.discovery.enabled: true`
- ✅ 命名空间: `public`
- ✅ 组: `DEFAULT_GROUP`

### 6. doctor-confirm-service (8301)
- ✅ `spring.cloud.nacos.discovery.enabled: true`
- ✅ 命名空间: `public`
- ✅ 组: `DEFAULT_GROUP`

## 🔍 验证服务注册

### 方法 1: 访问 Nacos 控制台

1. 打开浏览器访问: http://localhost:8848/nacos
2. 登录（用户名/密码: `nacos/nacos`）
3. 进入 **服务管理** -> **服务列表**
4. 应该能看到以下服务：
   - `patient-service`
   - `symptom-ai-service`
   - `test-suggestion-ai-service`
   - `summary-ai-service`
   - `prescription-ai-service`
   - `doctor-confirm-service`

### 方法 2: 查看服务日志

```bash
# 查看所有服务的 Nacos 注册日志
grep -i "nacos\|register" logs/*.log

# 查看特定服务的注册日志
grep -i "nacos\|register" logs/patient-service.log
```

### 方法 3: 使用 Nacos API

```bash
# 获取服务列表（需要认证）
curl -X GET "http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=100" \
  -H "Authorization: Bearer <token>"
```

## 🔄 服务发现工作原理

### 服务注册
1. 服务启动时，会自动向 Nacos 注册自己的服务信息
2. 注册信息包括：
   - 服务名（`spring.application.name`）
   - IP 地址
   - 端口号
   - 健康状态

### 服务发现
1. 服务间调用时，通过服务名查找目标服务
2. Nacos 返回可用的服务实例列表
3. 负载均衡器选择其中一个实例进行调用

### Feign 客户端配置

启用服务发现后，`patient-service` 中的 Feign 客户端会自动通过服务名调用：

```yaml
# 之前（直接 URL）
feign:
  client:
    config:
      symptom-ai-service:
        url: http://localhost:8201

# 现在（服务发现）
# 不需要配置 URL，直接使用服务名
# Feign 会自动从 Nacos 获取服务地址
```

## ⚠️ 注意事项

1. **Nacos 服务器必须运行**: 确保 Nacos 服务器在 `localhost:8848` 运行
2. **网络连通性**: 确保所有服务能够访问 Nacos 服务器
3. **命名空间一致**: 所有服务使用相同的命名空间（`public`）
4. **服务名唯一**: 确保每个服务的 `spring.application.name` 唯一

## 🛠️ 故障排查

### 服务未注册到 Nacos

1. **检查 Nacos 服务器状态**
   ```bash
   curl http://localhost:8848/nacos
   ```

2. **检查服务日志**
   ```bash
   tail -f logs/<service-name>.log | grep -i nacos
   ```

3. **检查配置**
   - 确认 `spring.cloud.nacos.discovery.enabled: true`
   - 确认 `spring.cloud.nacos.discovery.server-addr: localhost:8848`

4. **检查网络**
   - 确认服务能够访问 Nacos 服务器
   - 检查防火墙设置

### 服务间调用失败

1. **检查目标服务是否注册**
   - 在 Nacos 控制台查看服务列表

2. **检查服务名是否正确**
   - 确认 Feign 客户端使用的服务名与注册的服务名一致

3. **检查负载均衡器**
   - 确认 `spring.cloud.loadbalancer.enabled: true`

## 📝 相关配置

### 服务发现配置示例

```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: true  # 启用服务发现
        server-addr: localhost:8848  # Nacos 服务器地址
        namespace: public  # 命名空间
        group: DEFAULT_GROUP  # 服务组
        fail-fast: false  # 即使 Nacos 不可用也不快速失败
```

### 负载均衡配置

```yaml
spring:
  cloud:
    loadbalancer:
      enabled: true  # 启用负载均衡器
```

## 🎯 下一步

1. ✅ 所有服务已启用 Nacos 服务发现
2. ✅ 所有服务已重启并应用新配置
3. 🔍 在 Nacos 控制台验证服务注册
4. 🧪 测试服务间调用是否正常工作


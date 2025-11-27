# Redis 集成使用说明

## 概述

项目已成功集成 Redis 8.4.0 支持。Redis 配置已添加到所有微服务中，可以通过 `RedisTemplate` 在代码中使用。

## 配置说明

### 1. Redis 连接配置

所有服务的配置文件（`application.yml` 或 `application.yaml`）中已添加 Redis 配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:  # 如果 Redis 设置了密码，请在这里填写
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
```

### 2. 配置位置

Redis 配置已添加到以下服务的配置文件中：
- `patient-service/src/main/resources/application.yml`
- `gateway-service/src/main/resources/application.yml`
- `diagnosis-ai-service/src/main/resources/application.yaml`
- `symptom-ai-service/src/main/resources/application.yml`
- `test-suggestion-ai-service/src/main/resources/application.yml`
- `prescription-ai-service/src/main/resources/application.yml`
- `summary-ai-service/src/main/resources/application.yaml`
- `doctor-confirm-service/src/main/resources/application.yaml`

## 启动 Redis

### Windows 系统

1. **确保 Redis 已安装并运行**

   如果 Redis 未运行，请启动 Redis 服务器：
   ```bash
   # 在 Redis 安装目录下执行
   redis-server.exe
   ```

   或者如果 Redis 已作为 Windows 服务安装：
   ```bash
   # 启动 Redis 服务
   net start redis
   ```

2. **验证 Redis 是否运行**

   打开新的命令行窗口，执行：
   ```bash
   redis-cli ping
   ```
   
   如果返回 `PONG`，说明 Redis 运行正常。

### Linux/Mac 系统

1. **启动 Redis 服务器**
   ```bash
   redis-server
   ```

2. **验证 Redis 是否运行**
   ```bash
   redis-cli ping
   ```
   
   如果返回 `PONG`，说明 Redis 运行正常。

## 在代码中使用 Redis

### 1. 注入 RedisTemplate

在任何 Spring Bean 中，可以直接注入 `RedisTemplate`：

```java
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class YourService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void example() {
        // 存储数据
        redisTemplate.opsForValue().set("key", "value");
        
        // 获取数据
        Object value = redisTemplate.opsForValue().get("key");
        
        // 设置过期时间（秒）
        redisTemplate.opsForValue().set("key", "value", 60);
        
        // 删除数据
        redisTemplate.delete("key");
    }
}
```

### 2. 常用操作示例

```java
@Service
public class RedisExampleService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // String 操作
    public void stringOperations() {
        redisTemplate.opsForValue().set("user:1:name", "张三");
        String name = (String) redisTemplate.opsForValue().get("user:1:name");
    }
    
    // Hash 操作
    public void hashOperations() {
        redisTemplate.opsForHash().put("user:1", "name", "张三");
        redisTemplate.opsForHash().put("user:1", "age", 25);
        Object name = redisTemplate.opsForHash().get("user:1", "name");
    }
    
    // List 操作
    public void listOperations() {
        redisTemplate.opsForList().rightPush("list:1", "item1");
        redisTemplate.opsForList().rightPush("list:1", "item2");
        List<Object> list = redisTemplate.opsForList().range("list:1", 0, -1);
    }
    
    // Set 操作
    public void setOperations() {
        redisTemplate.opsForSet().add("set:1", "value1", "value2");
        Set<Object> set = redisTemplate.opsForSet().members("set:1");
    }
    
    // 设置过期时间
    public void expireOperations() {
        redisTemplate.opsForValue().set("key", "value");
        redisTemplate.expire("key", Duration.ofSeconds(60));
    }
}
```

## 注意事项

1. **Redis 密码配置**：如果 Redis 服务器设置了密码，请在各个服务的配置文件中填写 `spring.data.redis.password` 字段。

2. **Redis 连接地址**：如果 Redis 不在本地运行，请修改 `spring.data.redis.host` 为实际的 Redis 服务器地址。

3. **数据库选择**：默认使用数据库 0，如需使用其他数据库，修改 `spring.data.redis.database` 配置。

4. **序列化方式**：已配置使用 JSON 序列化，支持存储复杂对象。Key 使用 String 序列化，Value 使用 Jackson JSON 序列化。

## 验证 Redis 集成

启动任意一个服务后，检查日志中是否有 Redis 连接相关的错误信息。如果服务正常启动且没有 Redis 连接错误，说明集成成功。

## 故障排查

1. **连接失败**：
   - 检查 Redis 服务器是否运行：`redis-cli ping`
   - 检查配置的 host 和 port 是否正确
   - 检查防火墙设置

2. **认证失败**：
   - 检查 Redis 密码配置是否正确
   - 确认 Redis 服务器是否启用了密码认证

3. **序列化错误**：
   - 确保存储的对象实现了 `Serializable` 接口
   - 检查对象是否包含无法序列化的字段


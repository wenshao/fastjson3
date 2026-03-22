# 安全配置指南

本文档介绍 fastjson3 的安全配置和最佳实践。

## AutoType 安全

### 什么是 AutoType

AutoType 允许 JSON 中包含类型信息，实现多态类型的自动反序列化。

```json
{
    "@type": "com.example.User",
    "name": "张三"
}
```

### 安全风险

如果不加限制地启用 AutoType，攻击者可以构造恶意 JSON 执行任意代码。

### 安全配置

```java
// ❌ 危险：允许任意类型
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.SupportAutoType)  // 不要在生产环境这样做
    .build();
```

### 推荐做法

`SupportAutoType` 默认关闭，生产环境不要开启。如果确实需要多态反序列化，使用 `@JSONType` 注解限定已知子类型：

```java
// ✅ 安全：通过注解显式声明已知子类型
@JSONType(seeAlso = {Cat.class, Dog.class}, typeKey = "type")
public abstract class Animal {
    private String name;
}

@JSONType(typeName = "cat")
public class Cat extends Animal { }

@JSONType(typeName = "dog")
public class Dog extends Animal { }

// 反序列化时只接受声明的子类型
Animal animal = JSON.parse(json, Animal.class);
```

这比开启 `SupportAutoType` 安全得多，因为只有 `seeAlso` 中声明的类型会被实例化。

---

## 输入验证

### 基本验证

```java
// 验证 JSON 格式
if (!JSON.isValid(input)) {
    throw new IllegalArgumentException("Invalid JSON");
}
```

### JSON Schema 验证

```java
private static final JSONSchema USER_SCHEMA = JSONSchema.parseSchema("""
    {
        "type": "object",
        "properties": {
            "name": {"type": "string", "minLength": 1, "maxLength": 50},
            "email": {"type": "string", "format": "email"}
        },
        "required": ["name"]
    }
    """);

public void processUser(String json) {
    ValidateResult result = USER_SCHEMA.validate(json);
    if (!result.isSuccess()) {
        throw new IllegalArgumentException(result.getMessage());
    }
    // 处理用户数据...
}
```

### 大小限制

```java
public class SafeJsonParser {
    private static final int MAX_SIZE = 10 * 1024 * 1024;  // 10MB

    public void parse(String json) {
        if (json.length() > MAX_SIZE) {
            throw new IllegalArgumentException("JSON too large");
        }
        JSON.parseObject(json);
    }
}
```

---

## 特性配置

### 生产环境推荐配置

```java
ObjectMapper mapper = ObjectMapper.builder()
    // 禁用危险特性
    .disableRead(ReadFeature.SupportAutoType)        // 禁用 AutoType
    .disableRead(ReadFeature.AllowComments)          // 禁用注释
    .disableRead(ReadFeature.AllowSingleQuotes)      // 禁用单引号
    .disableRead(ReadFeature.AllowUnquotedFieldNames) // 禁用无引号字段

    // 启用安全特性
    .enableRead(ReadFeature.ErrorOnUnknownProperties) // 未知字段报错

    // 写入配置
    .enableWrite(WriteFeature.BrowserCompatible)      // 浏览器兼容
    .build();
```

### 开发环境配置

```java
ObjectMapper mapper = ObjectMapper.builder()
    // 允许宽松格式
    .enableRead(ReadFeature.AllowComments)
    .enableRead(ReadFeature.AllowSingleQuotes)
    .enableRead(ReadFeature.SupportSmartMatch)

    // 美化输出
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
```

### 环境分离

```java
@Configuration
public class JsonConfig {

    @Bean
    @Profile("prod")
    public ObjectMapper productionMapper() {
        return ObjectMapper.builder()
            .disableRead(ReadFeature.SupportAutoType)
            .enableRead(ReadFeature.ErrorOnUnknownProperties)
            .enableWrite(WriteFeature.BrowserCompatible)
            .build();
    }

    @Bean
    @Profile("dev")
    public ObjectMapper developmentMapper() {
        return ObjectMapper.builder()
            .enableRead(ReadFeature.AllowComments)
            .enableWrite(WriteFeature.PrettyFormat)
            .build();
    }
}
```

---

## 输出过滤

### 敏感信息脱敏

```java
ValueFilter desensitize = (obj, name, val) -> {
    if (val == null) return null;

    // 脱敏手机号
    if (name.equals("phone") && val instanceof String) {
        String phone = (String) val;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    // 脱敏身份证
    if (name.equals("idCard") && val instanceof String) {
        String idCard = (String) val;
        return idCard.substring(0, 6) + "********";
    }

    // 脱敏邮箱
    if (name.equals("email") && val instanceof String) {
        String email = (String) val;
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }

    return val;
};

ObjectMapper mapper = ObjectMapper.builder()
    .addValueFilter(desensitize)
    .build();
String json = mapper.writeValueAsString(user);
```

### 排除字段

```java
PropertyFilter securityFilter = (obj, name, val) -> {
    // 排除敏感字段
    Set<String> sensitiveFields = Set.of(
        "password", "ssn", "creditCard", "token", "apiKey"
    );
    return !sensitiveFields.contains(name);
};

ObjectMapper mapper = ObjectMapper.builder()
    .addPropertyFilter(securityFilter)
    .build();
String json = mapper.writeValueAsString(user);
```

### 使用 Mixin

```java
public abstract class SecureUserMixin {
    @JSONField(serialize = false)
    abstract String getPassword();

    @JSONField(serialize = false)
    abstract String getSsn();
}

ObjectMapper mapper = ObjectMapper.builder()
    .addMixIn(User.class, SecureUserMixin.class)
    .build();
```

---

## 处理不受信任的数据

### 使用 JSONValidator

```java
// 先验证结构
try (JSONValidator validator = JSONValidator.from(untrustedJson)) {
    if (!validator.validate()) {
        throw new SecurityException("Invalid JSON structure");
    }
}

// 再解析
JSONObject obj = JSON.parseObject(untrustedJson);
```

### 限制深度

```java
public class DepthLimitingParser {
    private static final int MAX_DEPTH = 10;

    public void parse(String json) {
        JSONParser parser = JSONParser.of(json);
        int depth = 0;

        while (parser.hasNext()) {
            JSONToken token = parser.getCurrentToken();
            if (token == JSONToken.LBRACE || token == JSONToken.LBRACKET) {
                depth++;
                if (depth > MAX_DEPTH) {
                    throw new SecurityException("JSON too deep");
                }
            } else if (token == JSONToken.RBRACE || token == JSONToken.RBRACKET) {
                depth--;
            }
            parser.nextToken();
        }
    }
}
```

---

## Web 应用安全

### Content-Type 验证

```java
@RestController
public class ApiController {

    @PostMapping("/data")
    public ResponseEntity<?> processData(
            @RequestHeader("Content-Type") String contentType,
            @RequestBody String json) {

        // 只接受 application/json
        if (!MediaType.APPLICATION_JSON_VALUE.equals(contentType)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body("Only application/json is supported");
        }

        // 验证 JSON
        if (!JSON.isValid(json)) {
            return ResponseEntity.badRequest().body("Invalid JSON");
        }

        // 处理数据...
    }
}
```

### CSRF 保护

```java
// JSON 请求通常不使用 CSRF token
// 但要确保有其他认证机制

@PostMapping("/api/data")
public ResponseEntity<?> processData(
        @RequestBody String json,
        Principal principal) {  // 认证用户

    if (principal == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // 处理数据...
}
```

---

## 日志安全

### 避免记录敏感数据

```java
// ❌ 危险
log.info("User data: {}", jsonData);  // 可能包含密码

// ✅ 安全
log.info("User data: {}", JSON.toJSONString(user,
    WriteFeature.BrowserCompatible));  // 转义特殊字符
```

### 使用过滤器记录

```java
PropertyFilter logFilter = (obj, name, val) -> {
    // 日志中不记录敏感字段
    return !Set.of("password", "token", "ssn").contains(name);
};

ObjectMapper logMapper = ObjectMapper.builder()
    .addPropertyFilter(logFilter)
    .build();
log.info("User data: {}", logMapper.writeValueAsString(user));
```

---

## 安全检查清单

部署前检查：

- [ ] 禁用 SupportAutoType（除非必要）
- [ ] 使用白名单或黑名单过滤类型
- [ ] 验证所有输入 JSON
- [ ] 限制 JSON 大小
- [ ] 使用 JSON Schema 验证结构
- [ ] 过滤敏感输出
- [ ] 配置适当的特性
- [ ] 启用日志审计
- [ ] 定期更新依赖

## 相关文档

- [📖 最佳实践 →](../best-practices.md)
- [📖 性能调优 →](../guides/performance.md)
- [📖 Spring Boot 集成 →](../guides/spring-boot.md)

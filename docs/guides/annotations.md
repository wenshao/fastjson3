# 注解使用指南

fastjson3 注解的完整使用指南，包含常见场景的最佳实践。

## 目录

- [字段注解](#字段注解)
- [类注解](#类注解)
- [构造器注解](#构造器注解)
- [实际场景](#实际场景)

---

## 字段注解

### @JSONField - 字段级控制

#### 1. 字段重命名

```java
public class User {
    @JSONField(name = "user_id")
    private Long userId;

    @JSONField(name = "user_name")
    private String userName;

    @JSONField(name = "is_vip")
    private Boolean vip;
}
// 输出: {"user_id":123,"user_name":"张三","is_vip":true}
```

**何时使用**：API 字段名与 Java 字段名不一致时

#### 2. 序列化控制

```java
public class User {
    @JSONField(serialize = false)  // 不序列化
    private String password;

    @JSONField(deserialize = false)  // 不反序列化
    private String calculatedValue;
}
```

**最佳实践**：敏感信息用注解，而非过滤器（性能更好）

#### 3. 日期格式化

```java
public class Event {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JSONField(format = "yyyy-MM-dd")
    private LocalDate date;

    @JSONField(format = "HH:mm:ss")
    private LocalTime time;

    @JSONField(format = "millis")  // 时间戳
    private Instant timestamp;
}
```

**格式说明**：

| 格式 | 说明 | 示例 |
|------|------|------|
| `yyyy-MM-dd` | 日期 | 2024-03-17 |
| `yyyy-MM-dd HH:mm:ss` | 日期时间 | 2024-03-17 14:30:00 |
| `millis` | 毫秒时间戳 | 1710680400000 |

#### 4. 序列化顺序

```java
public class Response {
    @JSONField(ordinal = 1)
    private int code;

    @JSONField(ordinal = 2)
    private String message;

    @JSONField(ordinal = 3)
    private T data;
}
// 输出顺序：code → message → data
```

**何时使用**：需要保证字段输出顺序时

#### 5. 替代字段名

```java
public class User {
    @JSONField(alternateNames = {"email", "emailAddress", "mail"})
    private String mail;
}
// JSON 中的 email/emailAddress/mail 都会映射到 mail
```

#### 6. 默认值

```java
public class Config {
    @JSONField(defaultValue = "8080")
    private int port;

    @JSONField(defaultValue = "localhost")
    private String host;

    @JSONField(defaultValue = "false")
    private boolean enabled;
}
```

**何时使用**：反序列化时字段缺失的情况

#### 7. 必填字段

```java
public class CreateUserRequest {
    @JSONField(required = true)
    private String username;

    @JSONField(required = true)
    private String password;
}
```

**行为**：反序列化时缺失会抛出异常

#### 8. 包含策略

```java
public class User {
    @JSONField(inclusion = Inclusion.ALWAYS)  // 总是包含
    private String displayName;

    @JSONField(inclusion = Inclusion.NON_NULL)  // 非 null 时包含
    private String nickname;

    @JSONField(inclusion = Inclusion.NON_EMPTY)  // 非空时包含
    private String notes;

    @JSONField(inclusion = Inclusion.NON_DEFAULT)  // 非默认值时包含
    private int status;
}
```

| 策略 | 说明 |
|------|------|
| `ALWAYS` | 总是包含 |
| `NON_NULL` | 值非 null 时包含 |
| `NON_EMPTY` | 值非空时包含（String 不为 ""，Collection 不为空） |
| `NON_DEFAULT` | 值不等于默认值时包含 |

#### 9. 自定义序列化器

```java
public class Money {
    @JSONField(serializeUsing = MoneySerializer.class)
    private BigDecimal amount;

    // 自定义序列化器
    public static class MoneySerializer implements ObjectWriter<Money> {
        @Override
        public void write(JSONGenerator gen, Object object, long features) {
            Money money = (Money) object;
            gen.writeString(money.amount.setScale(2, RoundingMode.HALF_UP) + "元");
        }
    }
}
```

#### 10. JSON Schema 验证

```java
public class Product {
    @JSONField(schema = "{\"minimum\": 0, \"maximum\": 100}")
    private int price;

    @JSONField(schema = "{\"pattern\": "^[A-Za-z0-9]+$\"}")
    private String productCode;
}
```

---

## 类注解

### @JSONType - 类级控制

#### 1. 命名策略

```java
@JSONType(naming = NamingStrategy.SnakeCase)
public class Config {
    private String serverName;      // -> server_name
    private Integer maxConnections; // -> max_connections
}
// 输出: {"server_name":"localhost","max_connections":100}
```

#### 2. 忽略多个字段

```java
@JSONType(ignores = {"password", "salt", "internalId", "secretKey"})
public class User {
    private String password;
    private String salt;
    private String internalId;
    private String secretKey;
    private String name;  // 只有 name 会被序列化
}
```

#### 3. 按字母排序

```java
@JSONType(alphabetic = true)
public class Data {
    private String z;
    private String a;
    private String m;
}
// 输出顺序：a, m, z
```

#### 4. 自定义序列化器

```java
@JSONType(serializer = UserSerializer.class)
public class User {
    // ...
}
```

---

## 构造器注解

### @JSONCreator - 反序列化构造

#### 基本用法

```java
public class Point {
    private final int x;
    private final int y;

    @JSONCreator
    public Point(
        @JSONField(name = "x") int x,
        @JSONField(name = "y") int y
    ) {
        this.x = x;
        this.y = y;
    }
}

Point p = JSON.parseObject("{\"x\":10,\"y\":20}", Point.class);
```

#### 工厂方法

```java
public class Value {
    private final int value;

    private Value(int value) {
        this.value = value;
    }

    @JSONCreator
    public static Value of(int value) {
        return new Value(value);
    }
}

Value v = JSON.parseObject("42", Value.class);
```

#### Delegating 注解

```java
public class Container {
    private Map<String, Object> properties;

    @JSONCreator
    public static Container create(Map<String, Object> properties) {
        return new Container(properties);
    }
}
```

---

## 实际场景

### 场景 1：API 响应格式

```java
public class ApiResponse<T> {
    @JSONField(ordinal = 1)
    private int code;

    @JSONField(ordinal = 2)
    private String message;

    @JSONField(ordinal = 3)
    private T data;

    @JSONField(include = Inclusion.NON_NULL)
    private String debugInfo;
}
```

### 场景 2：数据库实体映射

```java
@JSONType(naming = NamingStrategy.SnakeCase)
public class UserEntity {
    // 数据库字段：user_name, user_age
    // Java 字段：userName, userAge
    private String userName;
    private Integer userAge;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
```

### 场景 3：多环境配置

```java
public class Config {
    @JSONField(name = "db_url")
    private String databaseUrl;

    @JSONField(name = "cache_ttl")
    @JSONField(defaultValue = "3600")
    private int cacheTtl;

    @JSONField(required = true)
    private String environment;
}
```

### 场景 4：敏感信息保护

```java
public class UserProfile {
    @JSONField(serialize = false)
    private String password;

    @JSONField(serialize = false)
    private String secretKey;

    @JSONField(name = "phone")
    private String phoneNumber;  // 正常输出

    @JSONField(name = "email")
    private String email;  // 正常输出
}
```

### 场景 5：版本兼容

```java
public class UserDTO {
    // v1 字段
    @JSONField(name = "user_name")
    private String userName;

    // v2 新增字段
    @JSONField(name = "display_name", defaultValue = "")
    private String displayName;
}
```

---

## 最佳实践

### 1. 优先使用注解而非过滤器

```java
// ✅ 好：注解（编译时检查，性能好）
@JSONField(serialize = false)
private String password;

// ❌ 不好：过滤器（运行时检查，性能差）
PropertyFilter filter = (obj, name, val) -> !name.equals("password");
```

### 2. 复杂逻辑用自定义序列化器

```java
// 当注解不够用时
@JSONField(serializeUsing = MoneySerializer.class)
private BigDecimal amount;
```

### 3. 保持注解一致性

```java
// ✅ 好：命名策略统一
@JSONType(naming = NamingStrategy.SnakeCase)
public class Config {
    private String serverName;
}

// ❌ 不好：混合使用
public class Config {
    @JSONField(name = "serverName")  // 显式重命名
    private String ipAddress;  // 默认驼峰
}
```

### 4. 文档化注解意图

```java
/**
 * 用户实体
 *
 * 命名策略：snake_case
 * 忽略字段：password, secretKey
 */
@JSONType(naming = NamingStrategy.SnakeCase, ignores = {"password", "secretKey"})
public class User {
    // ...
}
```

## 相关文档

- [API 参考](../api/annotations.md)
- [POJO 序列化](pojo.md)
- [自定义序列化](../advanced/custom-serializer.md)

[← 返回指南索引](../README.md)

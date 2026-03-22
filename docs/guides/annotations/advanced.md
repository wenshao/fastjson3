# 注解高级用法

本文档介绍 fastjson3 注解的高级功能和最佳实践。

---

## 高级字段控制

### 替代字段名

```java
public class User {
    @JSONField(alternateNames = {"email", "emailAddress", "mail"})
    private String mail;
}
// JSON 中的 email/emailAddress/mail 都会映射到 mail
```

### 展开嵌套对象

> **注意：** `@JSONField(unwrapped = true)` 不受支持。如需展开嵌套对象，请使用自定义序列化器（`serializeUsing`）实现。

```java
public class User {
    private String name;
    private Address address;
}

public class Address {
    private String city;
    private String street;
}
// 如需输出 {"name":"Tom","city":"Beijing","street":"Main"}，
// 请使用 @JSONField(serializeUsing = ...) 自定义序列化器。
```

### 单值序列化

```java
public enum Status {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String code;

    Status(String code) {
        this.code = code;
    }

    @JSONField(value = true)
    public String getCode() {
        return code;
    }
}
// 输出: "active" 而不是 {"code":"active"}
```

### 任意属性

```java
public class DynamicData {
    private Map<String, Object> properties;

    @JSONField(anyGetter = true)
    public Map<String, Object> getProperties() {
        return properties;
    }

    @JSONField(anySetter = true)
    public void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }
}
```

### 标签过滤

```java
public class User {
    private String name;

    @JSONField(label = "admin")
    private String internalNote;

    @JSONField(label = "audit")
    private String auditLog;
}

// 使用时通过 LabelFilter 控制
```

---

## 自定义序列化

### 字段级序列化器

```java
public class Money {
    @JSONField(serializeUsing = MoneyWriter.class)
    private BigDecimal amount;

    public static class MoneyWriter implements ObjectWriter<BigDecimal> {
        @Override
        public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
            BigDecimal value = (BigDecimal) object;
            gen.writeString(value.setScale(2, RoundingMode.HALF_UP) + "元");
        }
    }
}
```

### 类级序列化器

> **注意：** `@JSONType` 不支持 `serializer` 参数。请在字段上使用 `@JSONField(serializeUsing = ...)` 指定自定义序列化器，或通过 `ObjectMapper.builder()` 注册全局 `ObjectWriter`。

```java
// 方式1：字段级自定义序列化器
public class Order {
    @JSONField(serializeUsing = UserWriter.class)
    private User user;
}

// 方式2：通过 ObjectMapper 注册
ObjectMapper mapper = ObjectMapper.builder()
    .build();
// 注册自定义 ObjectWriter<User>
```

### 自定义反序列化器

```java
public class User {
    @JSONField(deserializeUsing = EmailReader.class)
    private String email;
}

public class EmailReader implements ObjectReader<String> {
    @Override
    public String readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
        String email = parser.readString();
        // 自定义验证逻辑
        if (!email.contains("@")) {
            throw new RuntimeException("Invalid email");
        }
        return email.toLowerCase();
    }
}
```

---

## JSON Schema 验证

```java
public class Product {
    @JSONField(schema = "{\"minimum\": 0, \"maximum\": 100}")
    private int price;

    @JSONField(schema = "{\"pattern\": \"^[A-Za-z0-9]+$\"}")
    private String productCode;

    @JSONField(schema = "{\"minLength\": 3, \"maxLength\": 20}")
    private String name;
}
```

---

## 组合注解

### 多个注解组合

```java
public class UserDTO {
    @JSONField(name = "user_name")
    @JSONField(format = "trim")
    @JSONField(defaultValue = "Anonymous")
    private String userName;

    // 或使用 JSONType 组合
    @JSONType(naming = NamingStrategy.SnakeCase)
    @JSONType(ignores = {"password", "secret"})
    public class User {
        // ...
    }
}
```

---

## 注解继承

### 父类注解

```java
@JSONType(naming = NamingStrategy.SnakeCase)
public class BaseEntity {
    // 所有子类都会继承 snake_case 命名策略
}

public class User extends BaseEntity {
    private String userName;  // -> user_name
}
```

### 接口注解

```java
@JSONType(naming = NamingStrategy.KebabCase)
public interface Dto {
    // 实现类会继承命名策略
}
```

---

## Record 类注解

```java
public record User(
    @JSONField(name = "user_id") Long userId,
    @JSONField(name = "user_name") String userName,

    @JSONField(serialize = false) String password
) {}
```

---

## 注解与 Java Bean 规范

### Getter/Setter 注解

```java
public class User {
    private String name;

    // 注解放 getter 上，控制输出字段名
    @JSONField(name = "display_name")
    public String getName() {
        return name;
    }
}
```

### 字段 vs 方法注解优先级

```java
public class User {
    // 字段注解优先级低于方法注解
    @JSONField(name = "field_name")
    private String name;

    @JSONField(name = "method_name")  // 优先
    public String getName() {
        return name;
    }
}
```

---

## 性能考虑

### 注解 vs Filter

```java
// ✅ 好：注解（编译时检查，性能好）
@JSONField(serialize = false)
private String password;

// ❌ 不好：过滤器（运行时检查，性能差）
PropertyFilter filter = (obj, name, val) -> !name.equals("password");
```

### 命名策略性能

```java
// ✅ 好：类级统一命名策略
@JSONType(naming = NamingStrategy.SnakeCase)
public class Config {
    private String serverName;
    private Integer maxConnections;
}

// ⚠️ 不好：逐个字段指定
public class Config {
    @JSONField(name = "serverName")
    private String serverName;

    @JSONField(name = "maxConnections")
    private Integer maxConnections;
}
```

---

## 最佳实践

### 1. 保持注解一致性

```java
// ✅ 好：命名策略统一
@JSONType(naming = NamingStrategy.SnakeCase)
public class Config {
    private String serverName;
}

// ❌ 不好：混合使用
public class Config {
    @JSONField(name = "serverName")
    private String serverName;

    private String ipAddress;  // 默认驼峰
}
```

### 2. 复杂逻辑用自定义序列化器

```java
// 当注解不够用时
@JSONField(serializeUsing = ComplexSerializer.class)
private ComplexType value;
```

### 3. 文档化注解意图

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

### 4. 使用 @JSONField 管理版本兼容

```java
public class UserDTO {
    // v1 字段
    @JSONField(name = "user_name")
    private String userName;

    // v2 新增字段（提供默认值保证兼容）
    @JSONField(name = "display_name", defaultValue = "")
    private String displayName;
}
```

## 相关文档

- [注解基础 →](basics.md)
- [注解迁移 →](migration.md)

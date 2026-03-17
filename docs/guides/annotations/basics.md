# 注解基础

fastjson3 提供了完整的注解支持，用于控制 JSON 序列化和反序列化行为。

## 核心注解

| 注解 | 作用 | 目标 |
|------|------|------|
| `@JSONField` | 字段级控制 | 字段/方法 |
| `@JSONType` | 类级控制 | 类 |
| `@JSONCreator` | 构造器控制 | 构造器/工厂方法 |

---

## @JSONField - 字段级控制

### 字段重命名

```java
public class User {
    @JSONField(name = "user_id")
    private Long userId;

    @JSONField(name = "user_name")
    private String userName;
}
// 输出: {"user_id":123,"user_name":"张三"}
```

### 序列化控制

```java
public class User {
    @JSONField(serialize = false)  // 不序列化
    private String password;

    @JSONField(deserialize = false)  // 不反序列化
    private String calculatedValue;
}
```

### 日期格式化

```java
public class Event {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JSONField(format = "millis")  // 时间戳
    private Instant timestamp;
}
```

### 序列化顺序

```java
public class Response {
    @JSONField(ordinal = 1)
    private int code;

    @JSONField(ordinal = 2)
    private String message;

    @JSONField(ordinal = 3)
    private Object data;
}
```

### 默认值

```java
public class Config {
    @JSONField(defaultValue = "8080")
    private int port;

    @JSONField(defaultValue = "localhost")
    private String host;
}
```

### 必填字段

```java
public class CreateUserRequest {
    @JSONField(required = true)
    private String username;

    @JSONField(required = true)
    private String password;
}
```

### 包含策略

```java
public class User {
    @JSONField(inclusion = Inclusion.ALWAYS)  // 总是包含
    private String displayName;

    @JSONField(inclusion = Inclusion.NON_NULL)  // 非 null 时包含
    private String nickname;

    @JSONField(inclusion = Inclusion.NON_EMPTY)  // 非空时包含
    private String notes;
}
```

---

## @JSONType - 类级控制

### 命名策略

```java
@JSONType(naming = NamingStrategy.SnakeCase)
public class Config {
    private String serverName;      // -> server_name
    private Integer maxConnections; // -> max_connections
}
```

### 忽略字段

```java
@JSONType(ignores = {"password", "secretKey"})
public class User {
    private String password;
    private String secretKey;
    private String name;  // 只有 name 会被序列化
}
```

### 按字母排序

```java
@JSONType(alphabetic = true)
public class Data {
    private String z;
    private String a;
    private String m;
}
// 输出顺序：a, m, z
```

---

## @JSONCreator - 构造器控制

### 基本用法

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

### 工厂方法

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
```

---

## 常用场景

### API 响应

```java
public class ApiResponse<T> {
    @JSONField(ordinal = 1)
    private int code;

    @JSONField(ordinal = 2)
    private String message;

    @JSONField(ordinal = 3)
    private T data;
}
```

### 数据库映射

```java
@JSONType(naming = NamingStrategy.SnakeCase)
public class UserEntity {
    private String userName;
    private Integer userAge;
}
```

### 敏感信息

```java
public class UserProfile {
    @JSONField(serialize = false)
    private String password;

    @JSONField(serialize = false)
    private String secretKey;
}
```

## 相关文档

- [注解高级用法 →](advanced.md)
- [注解迁移 →](migration.md)

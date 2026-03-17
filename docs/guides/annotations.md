# 注解使用指南

fastjson3 注解的完整使用指南。

## 📚 文档导航

### 基础入门
- **[注解基础 →](annotations/basics.md)**
  - @JSONField 字段级控制
  - @JSONType 类级控制
  - @JSONCreator 构造器控制
  - 常用场景

### 高进阶
- **[注解高级用法 →](annotations/advanced.md)**
  - 高级字段控制
  - 自定义序列化
  - JSON Schema 验证
  - 注解继承
  - 性能考虑

### 迁移指南
- **[注解迁移 →](annotations/migration.md)**
  - Jackson 注解迁移
  - Gson 注解迁移
  - fastjson 1.x 注解迁移
  - 最佳实践

---

## 核心注解

| 注解 | 作用 | 目标 |
|------|------|------|
| `@JSONField` | 字段级控制 | 字段/方法 |
| `@JSONType` | 类级控制 | 类 |
| `@JSONCreator` | 构造器控制 | 构造器/工厂方法 |

---

## 快速开始

```java
public class User {
    // 字段重命名
    @JSONField(name = "user_id")
    private Long userId;

    // 日期格式化
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 忽略序列化
    @JSONField(serialize = false)
    private String password;
}

// 使用
String json = JSON.toJSONString(user);
// 输出: {"user_id":123,"create_time":"2026-03-17 10:00:00"}
```

---

## 常用场景

### API 响应格式

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
    private String userName;      // -> user_name
    private Integer userAge;      // -> user_age
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

---

## Jackson 注解兼容

fastjson3 **原生支持 Jackson 注解**，通常无需修改：

```java
// 这些 Jackson 注解在 fastjson3 中直接生效
public class User {
    @JsonProperty("user_name")
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    @JsonIgnore
    private String password;
}
```

详见 [注解迁移 →](annotations/migration.md)

---

## 与过滤器配合

| 方面 | 注解 | 过滤器 |
|------|------|--------|
| 性能 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 灵活性 | 编译时固定 | 运行时动态 |
| 复杂度 | 简单 | 复杂 |
| 推荐场景 | 固定规则 | 动态规则 |

详见 [过滤器指南 →](filters.md)

---

## 相关文档

- [API 参考](../api/annotations.md)
- [POJO 序列化](pojo.md)
- [过滤器使用](filters.md)

[← 返回指南索引](../README.md)

# 注解迁移指南

本文档介绍如何从其他 JSON 库迁移注解到 fastjson3。

---

## Jackson 注解

### 完全兼容

fastjson3 **原生支持大部分 Jackson 注解**，无需修改：

```java
// 这些 Jackson 注解在 fastjson3 中直接生效
public class User {
    @JsonProperty("user_name")
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    @JsonIgnore
    private String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nickname;
}
```

### 注解对照表

| Jackson 注解 | fastjson3 等价 | 原生支持 |
|--------------|--------------|---------|
| `@JsonProperty` | `@JSONField(name)` | ✅ |
| `@JsonAlias` | `@JSONField(alternateNames)` | ✅ |
| `@JsonIgnore` | `@JSONField(serialize=false)` | ✅ |
| `@JsonFormat` | `@JSONField(format)` | ✅ |
| `@JsonInclude` | `@JSONField(inclusion)` | ✅ |
| `@JsonUnwrapped` | `@JSONField(unwrapped=true)` | ✅ |
| `@JsonNaming` | `@JSONType(naming)` | ✅ |
| `@JsonCreator` | `@JSONCreator` | ✅ |
| `@JsonDeserialize` | `@JSONField(deserializeUsing)` | ✅ |
| `@JsonSerialize` | `@JSONField(serializeUsing)` | ✅ |
| `@JsonView` | - | ❌ |
| `@JsonIdentityInfo` | - | ❌ |

### 迁移示例

```java
// ===== Jackson =====
public class User {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty(value = "user_name", access = JsonProperty.Access.READ_ONLY)
    private String userName;

    @JsonProperty(required = true)
    private String email;
}

// ===== fastjson3（保留 Jackson 注解） =====
public class User {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty(value = "user_name", access = JsonProperty.Access.READ_ONLY)
    private String userName;

    @JsonProperty(required = true)
    private String email;
}

// ===== fastjson3（完全迁移） =====
public class User {
    @JSONField(name = "user_id")
    private Long userId;

    @JSONField(name = "user_name", deserialize = false)
    private String userName;

    @JSONField(required = true)
    private String email;
}
```

---

## Gson 注解

### @SerializedName

```java
// ===== Gson =====
public class User {
    @SerializedName("user_id")
    private Long userId;

    @SerializedName(value = "user_name", alternate = {"userName", "name"})
    private String userName;
}

// ===== fastjson3 =====
public class User {
    @JSONField(name = "user_id")
    private Long userId;

    @JSONField(name = "user_name", alternateNames = {"userName", "name"})
    private String userName;
}
```

### @Expose

```java
// ===== Gson =====
public class User {
    @Expose
    private String name;

    @Expose(serialize = false, deserialize = false)
    private String password;
}

// ===== fastjson3 =====
public class User {
    private String name;  // 默认序列化

    @JSONField(serialize = false, deserialize = false)
    private String password;
}
```

---

## fastjson 1.x 注解

### 完全兼容

```java
// fastjson 1.x 注解在 fastjson3 中完全兼容
public class User {
    @JSONField(name = "user_name")
    private String userName;

    @JSONField(format = "yyyy-MM-dd")
    private Date createTime;

    @JSONField(serialize = false)
    private String password;
}
```

### 新增属性

fastjson3 新增了一些注解属性：

| 新增属性 | 说明 |
|----------|------|
| `required` | 必填字段 |
| `defaultValue` | 默认值 |
| `alternateNames` | 别名 |
| `unwrapped` | 展开嵌套 |
| `label` | 标签过滤 |
| `schema` | JSON Schema |
| `anyGetter` | 任意属性获取 |
| `anySetter` | 任意属性设置 |

---

## org.json

org.json 不支持注解，需要手动解析。迁移到 fastjson3 可以大幅简化代码：

```java
// ===== org.json（手动解析） =====
JSONObject obj = new JSONObject(json);
String name = obj.getString("name");
int age = obj.getInt("age");

// ===== fastjson3 =====
@JSONType(naming = NamingStrategy.SnakeCase)
public class User {
    private String name;
    private Integer age;
}
User user = JSON.parseObject(json, User.class);
```

---

## 最佳实践

### 渐进式迁移

1. **保留原注解** - fastjson3 原生支持 Jackson/Gson 注解
2. **测试验证** - 确保行为一致
3. **逐步替换** - 可选地替换为 fastjson3 注解

### 何时替换注解

考虑替换原注解的情况：

- 需要使用 fastjson3 特有功能
- 统一代码风格
- 移除原库依赖

可以保留原注解的情况：

- 原注解功能完整
- 代码稳定，无需改动
- 多库共存场景

## 相关文档

- [注解基础 →](basics.md)
- [注解高级用法 →](advanced.md)

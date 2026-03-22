# 过滤器使用指南

fastjson3 过滤器的完整使用指南。

## 📚 文档导航

### 基础入门
- **[过滤器基础 →](filters/basics.md)**
  - NameFilter 字段名过滤
  - ValueFilter 值过滤
  - PropertyFilter 属性过滤
  - 组合使用

### 高进阶
- **[过滤器高级用法 →](filters/advanced.md)**
  - 高级过滤场景
  - 日志脱敏
  - API 版本控制
  - 客户端定制
  - ObjectMapper 集成

### 跨库对比
- **[跨库对比 →](filters/comparison.md)**
  - vs Jackson
  - vs Gson
  - 迁移指南
  - 性能对比

---

## 过滤器类型

| 过滤器 | 接口 | 用途 | 性能影响 |
|--------|------|------|----------|
| NameFilter | 修改字段名 | 重命名字段 | 小 |
| ValueFilter | 修改字段值 | 转换/脱敏 | 中 |
| PropertyFilter | 控制属性输出 | 决定是否输出 | 小 |

---

## 快速开始

```java
// 数据脱敏
ValueFilter phoneFilter = (obj, name, value) -> {
    if (name.equals("phone") && value instanceof String) {
        return maskPhone((String) value);
    }
    return value;
};

ObjectMapper mapper = ObjectMapper.builder().addValueFilter(phoneFilter).build();
String json = mapper.writeValueAsString(user);
```

---

## 常用场景

### 数据脱敏

```java
// 手机号脱敏
ValueFilter phoneFilter = (obj, name, value) -> {
    if (name.equals("phone")) {
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    return value;
};
```

### 字段过滤

```java
// 排除敏感字段
PropertyFilter securityFilter = (obj, name, value) -> {
    List<String> excluded = List.of("password", "secret", "token");
    return !excluded.contains(name);
};
```

### 字段重命名

```java
// 转换命名风格
NameFilter snakeCaseFilter = (obj, name, value) -> {
    return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
};
```

---

## 组合使用

```java
List<Filter> filters = Arrays.asList(
    // 1. 修改字段名
    (NameFilter) (obj, name, val) -> camelToSnake(name),

    // 2. 数据脱敏
    (ValueFilter) (obj, name, val) -> desensitize(name, val),

    // 3. 排除敏感字段
    (PropertyFilter) (obj, name, val) -> !isSensitive(name)
);

ObjectMapper mapper = ObjectMapper.builder()
    .addNameFilter((NameFilter) filters.get(0))
    .addValueFilter((ValueFilter) filters.get(1))
    .addPropertyFilter((PropertyFilter) filters.get(2))
    .build();
String json = mapper.writeValueAsString(obj);
```

---

## 注解 vs 过滤器

### 推荐模式

```java
// 1. 固定配置：使用注解（性能更好）
public class User {
    @JSONField(serialize = false)
    private String password;
}

// 2. 动态逻辑：使用过滤器
List<Filter> filters = Arrays.asList(
    (ValueFilter) (obj, name, val) -> dynamicDesensitize(name, val)
);
```

### 优先级

**注解 > 过滤器**

```java
public class User {
    @JSONField(name = "user_name")  // 注解优先
    private String userName;
}

// NameFilter 不会覆盖 @JSONField
NameFilter filter = (obj, name, val) -> name.toUpperCase();
// 输出: {"user_name":"张三"}  // 不是 "USER_NAME"
```

---

## 使用方式

```java
// 方式1：单个过滤器
ObjectMapper mapper = ObjectMapper.builder()
    .addPropertyFilter(filter)
    .build();
String json = mapper.writeValueAsString(obj);

// 方式2：多个过滤器
ObjectMapper mapper = ObjectMapper.builder()
    .addNameFilter(nameFilter)
    .addValueFilter(valueFilter)
    .build();
String json = mapper.writeValueAsString(obj);

// 方式3：复用 ObjectMapper
ObjectMapper mapper = ObjectMapper.builder()
    .addPropertyFilter(filter1)
    .addValueFilter(filter2)
    .build();
String json = mapper.writeValueAsString(obj);
```

---

## 执行顺序

**重要**：过滤器按添加的**反序**执行：

```java
List<Filter> filters = Arrays.asList(
    filter1,  // 第3个执行
    filter2,  // 第2个执行
    filter3   // 第1个执行
);
```

---

## 最佳实践

1. **优先使用注解** - 性能更好
2. **复用过滤器** - 避免重复创建
3. **可测试的过滤器** - 独立类便于测试
4. **注意执行顺序** - 后添加的先执行

---

## 相关文档

- [API 参考](../api/filters.md)
- [注解使用](annotations.md)
- [安全配置](../advanced/security.md)

[← 返回指南索引](../README.md)

# 过滤器基础

fastjson3 提供三种类型的过滤器用于控制 JSON 序列化。

## 过滤器类型

| 过滤器 | 接口 | 用途 | 性能影响 |
|--------|------|------|----------|
| NameFilter | 修改字段名 | 重命名字段 | 小 |
| ValueFilter | 修改字段值 | 转换/脱敏 | 中 |
| PropertyFilter | 控制属性输出 | 决定是否输出 | 小 |

---

## 添加过滤器

```java
// 方式1：单个过滤器
String json = JSON.toJSONString(obj, filter);

// 方式2：多个过滤器
String json = JSON.toJSONString(obj, filter1, filter2);

// 方式3：列表形式
List<Filter> filters = Arrays.asList(filter1, filter2);
String json = JSON.toJSONString(obj, filters);

// 方式4：ObjectMapper
ObjectMapper mapper = ObjectMapper.shared();
String json = mapper.writeValueAsString(obj, filters);
```

---

## NameFilter

### 基础用法

```java
// 所有字段转为大写
NameFilter filter = (obj, name, value) -> name.toUpperCase();

String json = JSON.toJSONString(user, filter);
// 输出: {"NAME":"张三","AGE":25}
```

### 条件重命名

```java
// 特定字段转小写
NameFilter filter = (obj, name, value) -> {
    if (name.equals("userName")) {
        return "username";
    }
    return name;
};
```

### 添加前缀/后缀

```java
// 添加前缀
NameFilter filter = (obj, name, value) -> "field_" + name;

// 添加后缀
NameFilter filter = (obj, name, value) -> name + "_field";
```

---

## ValueFilter

### 数据脱敏

```java
// 手机号脱敏
ValueFilter phoneFilter = (obj, name, value) -> {
    if (name.equals("phone") && value instanceof String) {
        String phone = (String) value;
        if (phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
    }
    return value;
};
```

### 类型转换

```java
ValueFilter typeConverter = (obj, name, value) -> {
    // Date 转时间戳
    if (value instanceof Date) {
        return ((Date) value).getTime();
    }
    return value;
};
```

### null 值处理

```java
// null 转空字符串
ValueFilter nullToEmpty = (obj, name, value) -> {
    return value == null ? "" : value;
};
```

---

## PropertyFilter

### 排除字段

```java
// 排除敏感字段
PropertyFilter securityFilter = (obj, name, value) -> {
    List<String> excluded = List.of("password", "secret", "token");
    return !excluded.contains(name);
};
```

### 按 null 值过滤

```java
// 排除 null 值字段
PropertyFilter excludeNulls = (obj, name, value) -> value != null;
```

### 按前缀过滤

```java
// 只输出特定前缀的字段
PropertyFilter prefixFilter = (obj, name, value) -> {
    return name.startsWith("public_");
};
```

---

## 组合使用

### 多级过滤

```java
List<Filter> filters = Arrays.asList(
    // 修改字段名
    (NameFilter) (obj, name, val) -> name.toLowerCase(),

    // 数据脱敏
    (ValueFilter) (obj, name, val) -> {
        if (name.equals("phone")) {
            return maskPhone((String) val);
        }
        return val;
    },

    // 排除敏感字段
    (PropertyFilter) (obj, name, val) -> !name.equals("password")
);
```

### 执行顺序

**重要**：过滤器按添加的**反序**执行：

```java
List<Filter> filters = Arrays.asList(
    filter1,  // 第3个执行
    filter2,  // 第2个执行
    filter3   // 第1个执行
);
```

---

## 与注解配合

### 注解优先级

```java
public class User {
    @JSONField(name = "user_name")  // 注解优先
    private String userName;
}

// NameFilter 不会覆盖 @JSONField
NameFilter filter = (obj, name, val) -> name.toUpperCase();
// 输出: {"user_name":"张三"}  // 不是 "USER_NAME"
```

### 推荐模式

```java
// 1. 基础配置：使用注解
public class User {
    @JSONField(serialize = false)
    private String password;
}

// 2. 动态逻辑：使用过滤器
List<Filter> filters = Arrays.asList(
    (ValueFilter) (obj, name, val) -> desensitize(name, val)
);
```

## 相关文档

- [过滤器高级用法 →](advanced.md)
- [跨库对比 →](comparison.md)

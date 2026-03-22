# 过滤器 API 参考

过滤器提供了在序列化时修改 JSON 输出的灵活方式。

## 概述

fastjson3 提供了四种类型的过滤器：

| 过滤器 | 接口 | 用途 |
|--------|------|------|
| NameFilter | 修改字段名 | 重命名字段 |
| ValueFilter | 修改字段值 | 转换值 |
| PropertyFilter | 控制属性输出 | 决定是否输出 |
| LabelFilter | 按标签过滤 | 视图控制 |

---

## NameFilter

修改 JSON 输出时的字段名。

### 接口定义

```java
@FunctionalInterface
public interface NameFilter {
    String apply(Object source, String name, Object value);
}
```

### 基本使用

```java
NameFilter filter = (source, name, value) -> {
    // 将所有字段转为大写
    return name.toUpperCase();
};

ObjectMapper mapper = ObjectMapper.builder()
    .addNameFilter(filter)
    .build();
String json = mapper.writeValueAsString(obj);
// {"NAME":"张三","AGE":25}
```

### 条件重命名

```java
NameFilter filter = (source, name, value) -> {
    if (name.equals("userName")) {
        return "username";  // 转小写
    }
    return name;
};
```

### 按类型处理

```java
NameFilter filter = (source, name, value) -> {
    if (source instanceof User) {
        // User 类的特殊处理
        if (name.equals("id")) return "userId";
    }
    return name;
};
```

---

## ValueFilter

修改字段值。

### 接口定义

```java
@FunctionalInterface
public interface ValueFilter {
    Object apply(Object source, String name, Object value);
}
```

### 基本使用

```java
ValueFilter filter = (source, name, value) -> {
    // 修剪所有字符串
    if (value instanceof String) {
        return ((String) value).trim();
    }
    return value;
};

ObjectMapper mapper = ObjectMapper.builder()
    .addValueFilter(filter)
    .build();
String json = mapper.writeValueAsString(obj);
```

### 类型转换

```java
ValueFilter filter = (source, name, value) -> {
    // Date 转时间戳
    if (value instanceof Date) {
        return ((Date) value).getTime();
    }
    return value;
};
```

### null 值处理

```java
ValueFilter filter = (source, name, value) -> {
    // null 转为空字符串
    if (value == null) {
        return "";
    }
    return value;
};
```

### 格式化值

```java
ValueFilter filter = (source, name, value) -> {
    // 金额格式化
    if (value instanceof BigDecimal) {
        return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP);
    }
    return value;
};
```

---

## PropertyFilter

完全控制属性是否输出。

### 接口定义

```java
@FunctionalInterface
public interface PropertyFilter {
    boolean apply(Object source, String name, Object value);
}
```

### 基本使用

```java
PropertyFilter filter = (source, name, value) -> {
    // 排除 password 字段
    return !name.equals("password");
};

ObjectMapper mapper = ObjectMapper.builder()
    .addPropertyFilter(filter)
    .build();
String json = mapper.writeValueAsString(obj);
```

### 多条件过滤

```java
PropertyFilter filter = (source, name, value) -> {
    // 排除多个字段
    if (List.of("password", "salt", "token").contains(name)) {
        return false;
    }
    // 排除 null 值
    if (value == null) {
        return false;
    }
    return true;
};
```

### 按类型过滤

```java
PropertyFilter filter = (source, name, value) -> {
    // 敏感类过滤所有内部字段
    if (source instanceof SensitiveData) {
        return !name.startsWith("internal");
    }
    return true;
};
```

### 排除空集合

```java
PropertyFilter filter = (source, name, value) -> {
    // 排除空集合
    if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
        return false;
    }
    if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
        return false;
    }
    return true;
};
```

---

## 在 ObjectMapper 中配置

过滤器通过 `ObjectMapper.builder()` 配置：

```java
ObjectMapper mapper = ObjectMapper.builder()
    // 添加各种过滤器
    .addNameFilter((source, name, value) -> name.toLowerCase())
    .addValueFilter((source, name, value) -> {
        if (value instanceof String) return ((String) value).trim();
        return value;
    })
    .addPropertyFilter((source, name, value) -> !name.equals("password"))
    .build();

String json = mapper.writeValueAsString(user);
```

---

## 与注解配合

过滤器和注解可以同时使用，注解优先级更高。

```java
public class User {
    @JSONField(name = "user_name")
    private String userName;

    @JSONField(serialize = false)  // 优先级更高
    private String password;
}

// NameFilter 不会影响被 @JSONField 标注的字段
```

---

## 自定义 ObjectWriter

更高性能的方式是实现自定义 ObjectWriter：

```java
public class FilteringWriter implements ObjectWriter<User> {
    @Override
    public void write(JSONGenerator gen, Object object,
                      Object fieldName, Type fieldType, long features) {
        User user = (User) object;
        gen.startObject();
        gen.writeNameValue("name", user.getName());
        // password 不输出
        gen.endObject();
    }
}

ObjectMapper mapper = ObjectMapper.builder()
    .build();
mapper.registerWriter(User.class, new FilteringWriter());
```

---

## 常用场景

### 敏感信息脱敏

```java
ValueFilter desensitize = (source, name, value) -> {
    if (value instanceof String str) {
        if (name.equals("phone")) {
            return str.substring(0, 3) + "****" + str.substring(7);
        }
        if (name.equals("idCard")) {
            return str.substring(0, 6) + "********";
        }
    }
    return value;
};
```

### API 版本控制

```java
PropertyFilter versionFilter = (source, name, value) -> {
    int apiVersion = getApiVersion();
    if (apiVersion < 2) {
        // v1 排除新字段
        return !name.startsWith("v2_");
    }
    return true;
};
```

---

## 性能考虑

过滤器有性能开销：

1. **每个字段都会调用** - 大对象注意性能
2. **优先使用注解** - 性能更好

```java
// ❌ 不好：运行时过滤
PropertyFilter filter = (source, name, value) -> !name.equals("password");

// ✅ 好：使用注解（编译时确定）
@JSONField(serialize = false)
private String password;
```

## 相关文档

- [📖 序列化基础 →](../start/02-basic-serialize.md)
- [📖 POJO 序列化 →](../guides/pojo.md)
- [🔧 自定义序列化 →](../advanced/custom-serializer.md)

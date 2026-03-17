# 过滤器 API 参考

过滤器提供了在序列化时修改 JSON 输出的灵活方式。

## 概述

fastjson3 提供了三种类型的过滤器：

| 过滤器 | 接口 | 用途 |
|--------|------|------|
| NameFilter | 修改字段名 | 重命名字段 |
| ValueFilter | 修改字段值 | 转换值 |
| PropertyFilter | 控制属性输出 | 决定是否输出 |

---

## NameFilter

修改 JSON 输出时的字段名。

### 接口定义

```java
@FunctionalInterface
public interface NameFilter extends SerializeFilter {
    String process(Object object, String name, Object value);
}
```

### 基本使用

```java
NameFilter filter = (object, name, value) -> {
    // 将所有字段转为大写
    return name.toUpperCase();
};

String json = JSON.toJSONString(obj, filter);
// {"NAME":"张三","AGE":25}
```

### 条件重命名

```java
NameFilter filter = (object, name, value) -> {
    if (name.equals("userName")) {
        return "username";  // 转小写
    }
    return name;
};
```

### 按类型处理

```java
NameFilter filter = (object, name, value) -> {
    if (object instanceof User) {
        // User 类的特殊处理
        if (name.equals("id")) return "userId";
    }
    return name;
};
```

### Lambda 简化

```java
// 单个字段重命名
NameFilter filter = NameFilter.of(object, name, value -> {
    return "custom_" + name;
});

// 组合多个过滤器
NameFilter filter1 = (obj, name, val) -> name + "_1";
NameFilter filter2 = (obj, name, val) -> name + "_2";
// 后添加的先执行
```

---

## ValueFilter

修改字段值。

### 接口定义

```java
@FunctionalInterface
public interface ValueFilter extends SerializeFilter {
    Object process(Object object, String name, Object value);
}
```

### 基本使用

```java
ValueFilter filter = (object, name, value) -> {
    // 修剪所有字符串
    if (value instanceof String) {
        return ((String) value).trim();
    }
    return value;
};

String json = JSON.toJSONString(obj, filter);
```

### 类型转换

```java
ValueFilter filter = (object, name, value) -> {
    // Date 转时间戳
    if (value instanceof Date) {
        return ((Date) value).getTime();
    }
    return value;
};
```

### null 值处理

```java
ValueFilter filter = (object, name, value) -> {
    // null 转为空字符串
    if (value == null) {
        return "";
    }
    return value;
};
```

### 格式化值

```java
ValueFilter filter = (object, name, value) -> {
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
public interface PropertyFilter extends SerializeFilter {
    boolean apply(Object object, String name, Object value);
}
```

### 基本使用

```java
PropertyFilter filter = (object, name, value) -> {
    // 排除 password 字段
    return !name.equals("password");
};

String json = JSON.toJSONString(obj, filter);
```

### 多条件过滤

```java
PropertyFilter filter = (object, name, value) -> {
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
PropertyFilter filter = (object, name, value) -> {
    // 敏感类过滤所有内部字段
    if (object instanceof SensitiveData) {
        return !name.startsWith("internal");
    }
    return true;
};
```

### 排除空集合

```java
PropertyFilter filter = (object, name, value) -> {
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

## 组合过滤器

多个过滤器可以组合使用。

### 使用 List

```java
List<Filter> filters = Arrays.asList(
    // 1. 修改字段名
    (NameFilter) (obj, name, val) -> name.toLowerCase(),
    // 2. 修改值
    (ValueFilter) (obj, name, val) -> {
        if (val instanceof String) return ((String) val).trim();
        return val;
    },
    // 3. 过滤属性
    (PropertyFilter) (obj, name, val) -> !name.equals("password")
);

String json = JSON.toJSONString(obj, filters);
```

### 链式调用

```java
// 先过滤属性，再修改值
PropertyFilter propertyFilter = /* ... */;
ValueFilter valueFilter = /* ... */;

String json = JSON.toJSONString(obj,
    propertyFilter,
    valueFilter
);
```

### 执行顺序

过滤器按照添加顺序的**反序**执行（后添加的先执行）。

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
NameFilter filter = (obj, name, val) -> name.toUpperCase();
```

---

## 在 ObjectMapper 中使用

### 配置全局过滤器

```java
// 目前不支持全局配置过滤器
// 需要在每次序列化时指定
String json = JSON.toJSONString(obj, filter);
```

### 自定义 Writer

更推荐的方式是实现自定义 ObjectWriter：

```java
public class FilteringWriter implements ObjectWriter<User> {
    @Override
    public void write(JSONGenerator gen, Object object, long features) {
        User user = (User) object;
        gen.writeStartObject();
        gen.writeStringField("name", user.getName());
        // password 不输出
        gen.writeEndObject();
    }
}

mapper.registerWriter(User.class, new FilteringWriter());
```

---

## 常用场景

### 敏感信息脱敏

```java
ValueFilter desensitize = (obj, name, val) -> {
    if (val instanceof String) {
        String str = (String) val;
        if (name.equals("phone")) {
            return str.substring(0, 3) + "****" + str.substring(7);
        }
        if (name.equals("idCard")) {
            return str.substring(0, 6) + "********";
        }
    }
    return val;
};
```

### 多语言支持

```java
NameFilter i18nFilter = (obj, name, val) -> {
    // 根据语言环境调整字段名
    Locale locale = Locale.getDefault();
    if (locale.getLanguage().equals("zh")) {
        return name;  // 中文保持原样
    } else {
        // 英文转驼峰
        return camelCase(name);
    }
};
```

### API 版本控制

```java
PropertyFilter versionFilter = (obj, name, val) -> {
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
2. **反射调用** - 比直接写慢
3. **优先使用注解** - 性能更好

```java
// ❌ 不好：每次序列化都过滤
PropertyFilter filter = (obj, name, val) -> !name.equals("password");

// ✅ 好：使用注解
@JSONField(serialize = false)
private String password;
```

---

## 完整示例

```java
public class FilterDemo {
    public static void main(String[] args) {
        User user = new User();
        user.setUserName("Admin");
        user.setPassword("secret");
        user.setEmail("admin@example.com");

        // 组合过滤器
        List<Filter> filters = Arrays.asList(
            // 1. 字段名转小写
            (NameFilter) (obj, name, val) -> name.toLowerCase(),

            // 2. 邮箱脱敏
            (ValueFilter) (obj, name, val) -> {
                if (val instanceof String && name.equals("email")) {
                    String email = (String) val;
                    int at = email.indexOf('@');
                    return email.charAt(0) + "***" + email.substring(at);
                }
                return val;
            },

            // 3. 排除 password
            (PropertyFilter) (obj, name, val) -> !name.equals("password")
        );

        String json = JSON.toJSONString(user, filters);
        // {"username":"Admin","email":"a***@example.com"}
    }
}
```

## 相关文档

- [📖 序列化基础 →](../start/02-basic-serialize.md)
- [📖 POJO 序列化 →](../guides/pojo.md)
- [🔧 自定义序列化 →](../advanced/custom-serializer.md)

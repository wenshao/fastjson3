# 过滤器 API 参考

过滤器提供了在序列化时修改 JSON 输出的灵活方式。

## 概述

fastjson3 提供了九种类型的过滤器：

| 过滤器 | 接口 | 用途 | 阶段 |
|--------|------|------|------|
| PropertyPreFilter | 按字段名过滤（不读值） | 高效属性排除 | 序列化前 |
| PropertyFilter | 按字段名+值过滤 | 决定是否输出 | 序列化时 |
| NameFilter | 修改字段名 | 重命名字段 | 序列化时 |
| ValueFilter | 修改字段值 | 转换值 | 序列化时 |
| ContextValueFilter | 带上下文的值转换 | 基于字段类型的值转换 | 序列化时 |
| BeforeFilter | 对象开始时注入字段 | 注入前置属性 | 序列化时 |
| AfterFilter | 对象结束时追加字段 | 注入后置属性 | 序列化时 |
| LabelFilter | 按标签过滤 | 视图控制 | 序列化时 |
| AutoTypeFilter | AutoType 白名单控制 | 安全类型校验 | 反序列化时 |

---

## PropertyPreFilter

序列化前按字段名过滤，不需要读取属性值，比 PropertyFilter 更高效。

### 接口定义

```java
@FunctionalInterface
public interface PropertyPreFilter {
    boolean apply(Object source, String name);
}
```

### 基本使用

```java
PropertyPreFilter filter = (source, name) -> {
    // 排除内部字段
    return !"internalField".equals(name);
};

ObjectMapper mapper = ObjectMapper.builder()
    .addPropertyPreFilter(filter)
    .build();
String json = mapper.writeValueAsString(obj);
```

### 按类型选择性排除

```java
PropertyPreFilter filter = (source, name) -> {
    if (source instanceof User) {
        // User 对象排除 password 和 salt
        return !List.of("password", "salt").contains(name);
    }
    return true;
};
```

### 常用场景

- 根据字段名批量排除敏感字段（不需要读取值，性能更好）
- 动态控制 API 返回字段（按权限、版本）
- 与 PropertyFilter 的区别：PropertyPreFilter 在读取属性值之前决定是否跳过，适用于决策仅依赖字段名的场景

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

## ContextValueFilter

带上下文信息的值过滤器，可以访问字段类型和字段名等上下文信息。

### 接口定义

```java
@FunctionalInterface
public interface ContextValueFilter {
    Object apply(Class<?> fieldClass, String fieldName, Object source, String name, Object value);
}
```

参数说明：
- `fieldClass` — 字段的声明类型
- `fieldName` — Java 字段名（可能与 JSON 属性名不同）
- `source` — 被序列化的对象
- `name` — JSON 属性名（经过 NameFilter 处理后的名称）
- `value` — 属性值

### 基本使用

```java
ContextValueFilter filter = (fieldClass, fieldName, source, name, value) -> {
    // 根据字段类型脱敏
    if (fieldClass == String.class && "password".equals(name)) {
        return "***";
    }
    return value;
};

ObjectMapper mapper = ObjectMapper.builder()
    .addContextValueFilter(filter)
    .build();
```

### 按字段类型格式化

```java
ContextValueFilter filter = (fieldClass, fieldName, source, name, value) -> {
    // 所有 BigDecimal 字段保留两位小数
    if (fieldClass == BigDecimal.class && value instanceof BigDecimal bd) {
        return bd.setScale(2, RoundingMode.HALF_UP);
    }
    // 所有 Date 字段格式化为 ISO 字符串
    if (fieldClass == Date.class && value instanceof Date d) {
        return d.toInstant().toString();
    }
    return value;
};
```

### 常用场景

- 根据字段声明类型而非运行时类型做值转换
- 区分 JSON 属性名和 Java 字段名进行不同处理
- 比 ValueFilter 提供更多上下文信息，适合复杂的转换逻辑

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

## BeforeFilter

在对象序列化开始时（`startObject()` 之后、任何字段写入之前）注入额外属性。

### 接口定义

```java
@FunctionalInterface
public interface BeforeFilter {
    void writeBefore(JSONGenerator generator, Object object);
}
```

### 基本使用

```java
BeforeFilter filter = (generator, object) -> {
    // 在每个对象开头注入版本号
    generator.writeName("_version");
    generator.writeInt32(1);
};

ObjectMapper mapper = ObjectMapper.builder()
    .addBeforeFilter(filter)
    .build();
String json = mapper.writeValueAsString(obj);
// {"_version":1,"name":"张三","age":25}
```

### 按类型注入不同字段

```java
BeforeFilter filter = (generator, object) -> {
    if (object instanceof User) {
        generator.writeName("_type");
        generator.writeString("user");
    } else if (object instanceof Order) {
        generator.writeName("_type");
        generator.writeString("order");
    }
};
```

### 常用场景

- 为所有对象注入公共元数据（版本号、类型标识）
- API 响应中统一添加前置字段
- 与 WriteClassName 类似但更灵活，可以自定义类型标识格式

---

## AfterFilter

在对象序列化结束时（所有字段写入之后、`endObject()` 之前）追加额外属性。

### 接口定义

```java
@FunctionalInterface
public interface AfterFilter {
    void writeAfter(JSONGenerator generator, Object object);
}
```

### 基本使用

```java
AfterFilter filter = (generator, object) -> {
    // 在每个对象末尾追加时间戳
    generator.writeName("_timestamp");
    generator.writeInt64(System.currentTimeMillis());
};

ObjectMapper mapper = ObjectMapper.builder()
    .addAfterFilter(filter)
    .build();
String json = mapper.writeValueAsString(obj);
// {"name":"张三","age":25,"_timestamp":1679875200000}
```

### 追加计算字段

```java
AfterFilter filter = (generator, object) -> {
    if (object instanceof Order order) {
        // 追加计算的总价字段
        generator.writeName("_totalDisplay");
        generator.writeString(order.getTotal().toPlainString() + " 元");
    }
};
```

### 常用场景

- 追加审计字段（时间戳、操作人）
- 添加计算属性（格式化后的显示值）
- 注入链接或元数据（HATEOAS 风格 API）

---

## LabelFilter

按标签过滤字段，配合 `@JSONField(label = "...")` 注解使用。

### 基本使用

```java
public class User {
    private String name;

    @JSONField(label = "admin")
    private String internalNote;

    @JSONField(label = "detail")
    private String bio;
}

// 只输出 admin 标签的字段
ObjectMapper mapper = ObjectMapper.builder()
    .addLabelFilter(label -> "admin".equals(label))
    .build();
```

---

## AutoTypeFilter

AutoType 白名单控制，用于反序列化时的安全类型校验。当启用 `ReadFeature.SupportAutoType` 时，JSON 中的 `@type` 字段会触发类型解析，AutoTypeFilter 控制哪些类名被允许。

### 接口定义

```java
@FunctionalInterface
public interface AutoTypeFilter {
    Class<?> apply(String typeName, Class<?> expectClass);

    // 工厂方法
    static AutoTypeFilter acceptNames(String... prefixes);
    static AutoTypeFilter acceptClasses(Class<?>... classes);
}
```

### 按包名前缀白名单

```java
// 只允许 com.myapp 包下的类
AutoTypeFilter filter = AutoTypeFilter.acceptNames("com.myapp.");

// AutoTypeFilter 接口已定义，AutoType 完整流程（JSONParser 解析 @type + filter 联动）
// 将在后续版本中实现。当前可通过自定义 ObjectReaderModule 实现类似功能。
```

### 按类型白名单

```java
// 只允许指定类及其子类
AutoTypeFilter filter = AutoTypeFilter.acceptClasses(
    Animal.class,
    Shape.class
);
```

### 自定义过滤逻辑

```java
AutoTypeFilter filter = (typeName, expectClass) -> {
    // 自定义白名单逻辑
    if (typeName.startsWith("com.myapp.model.")) {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    // 返回 null 表示拒绝
    return null;
};
```

### 常用场景

- 多态反序列化的安全控制（防止反序列化攻击）
- 限制 AutoType 只在可信的包或类范围内生效
- 替代全局 AutoType 开关，提供更细粒度的控制

---

## 在 ObjectMapper 中配置

过滤器通过 `ObjectMapper.builder()` 配置：

```java
ObjectMapper mapper = ObjectMapper.builder()
    // 序列化前过滤（不读取值，更高效）
    .addPropertyPreFilter((source, name) -> !name.startsWith("_"))
    // 字段名转换
    .addNameFilter((source, name, value) -> name.toLowerCase())
    // 值转换
    .addValueFilter((source, name, value) -> {
        if (value instanceof String) return ((String) value).trim();
        return value;
    })
    // 属性过滤（可访问值）
    .addPropertyFilter((source, name, value) -> !name.equals("password"))
    // 对象前置注入
    .addBeforeFilter((gen, obj) -> {
        gen.writeName("_version");
        gen.writeInt32(1);
    })
    // 对象后置追加
    .addAfterFilter((gen, obj) -> {
        gen.writeName("_ts");
        gen.writeInt64(System.currentTimeMillis());
    })
    .build();

String json = mapper.writeValueAsString(user);
```

---

## 过滤器执行顺序

多个过滤器的执行顺序如下：

1. **PropertyPreFilter** — 按字段名决定是否跳过（不读取值）
2. **BeforeFilter** — 在对象字段写入前注入属性
3. **PropertyFilter** — 按字段名+值决定是否输出
4. **NameFilter** — 转换字段名
5. **ValueFilter / ContextValueFilter** — 转换字段值
6. **AfterFilter** — 在对象字段写入后追加属性

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
PropertyPreFilter versionFilter = (source, name) -> {
    int apiVersion = getApiVersion();
    if (apiVersion < 2) {
        // v1 排除新字段
        return !name.startsWith("v2_");
    }
    return true;
};
```

### 审计日志注入

```java
AfterFilter auditFilter = (generator, object) -> {
    generator.writeName("_operator");
    generator.writeString(SecurityContext.getCurrentUser());
    generator.writeName("_operateTime");
    generator.writeString(LocalDateTime.now().toString());
};
```

---

## 性能考虑

过滤器有性能开销：

1. **每个字段都会调用** — 大对象注意性能
2. **PropertyPreFilter 比 PropertyFilter 更高效** — 不需要读取属性值
3. **优先使用注解** — 性能更好

```java
// ❌ 不好：运行时过滤
PropertyFilter filter = (source, name, value) -> !name.equals("password");

// ✅ 好：使用注解（编译时确定）
@JSONField(serialize = false)
private String password;

// ✅ 如果必须动态过滤，优先用 PropertyPreFilter
PropertyPreFilter filter = (source, name) -> !name.equals("password");
```

## 相关文档

- [序列化基础](../start/02-basic-serialize.md)
- [POJO 序列化](../guides/pojo.md)
- [自定义序列化](../advanced/custom-serializer.md)

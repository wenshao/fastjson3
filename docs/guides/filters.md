# 过滤器使用指南

fastjson3 过滤器的完整使用指南，包含实际场景和最佳实践。

## 目录

- [概述](#概述)
- [NameFilter](#namefilter)
- [ValueFilter](#valuefilter)
- [PropertyFilter](#propertyfilter)
- [组合使用](#组合使用)
- [实际场景](#实际场景)
- [最佳实践](#最佳实践)

---

## 概述

fastjson3 提供三种过滤器：

| 过滤器 | 接口 | 用途 | 性能影响 |
|--------|------|------|----------|
| NameFilter | 修改字段名 | 重命名字段 | 小 |
| ValueFilter | 修改字段值 | 转换/脱敏 | 中 |
| PropertyFilter | 控制属性输出 | 决定是否输出 | 小 |

### 添加过滤器

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

### 修改字段名

```java
// 所有字段转为大写
NameFilter filter = (obj, name, value) -> name.toUpperCase();

User user = new User("张三", 25);
String json = JSON.toJSONString(user, filter);
// 输出: {"NAME":"张三","AGE":25}
```

### 条件重命名

```java
// 特定字段转小写
NameFilter filter = (obj, name, value) -> {
    if (name.equals("userName")) {
        return "username";  // 转小写
    }
    return name;
};
```

### 前缀/后缀添加

```java
// 添加前缀
NameFilter filter = (obj, name, value) -> "field_" + name;

// 添加后缀
NameFilter filter = (obj, name, value) -> name + "_field";
```

### 按类型处理

```java
NameFilter filter = (obj, name, value) -> {
    if (obj instanceof User) {
        // User 类：蛇形命名
        return camelToSnake(name);
    } else if (obj instanceof Config) {
        // Config 类：保留原样
        return name;
    }
    return name;
};

String camelToSnake(String str) {
    return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
}
```

### API 版本控制

```java
NameFilter versionFilter = (obj, name, value) -> {
    int apiVersion = getApiVersion();
    if (apiVersion < 2) {
        // v1 使用旧字段名
        Map<String, String> v1Names = Map.of(
            "userName", "user_name",
            "createTime", "create_time"
        );
        return v1Names.getOrDefault(name, name);
    }
    return name;
};
```

---

## ValueFilter

### 数据脱敏

```java
// 手机号脱敏
ValueFilter phoneDesensitize = (obj, name, value) -> {
    if (name.equals("phone") && value instanceof String) {
        String phone = (String) value;
        if (phone.length() == 11) {
            // 13812345678 -> 138****5678
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
    }
    return value;
};
```

### 身份证脱敏

```java
ValueFilter idCardDesensitize = (obj, name, value) -> {
    if (name.equals("idCard") && value instanceof String) {
        String idCard = (String) value;
        if (idCard.length() == 18) {
            // 保留前6位，后4位
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
    }
    return value;
};
```

### 邮箱脱敏

```java
ValueFilter emailDesensitize = (obj, name, value) -> {
    if (name.equals("email") && value instanceof String) {
        String email = (String) value;
        int at = email.indexOf('@');
        if (at > 0) {
            // a***@example.com
            return email.charAt(0) + "***" + email.substring(at);
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
    // LocalDateTime 转字符串
    if (value instanceof LocalDateTime) {
        return value.toString();
    }
    return value;
};
```

### 精度控制

```java
ValueFilter precisionFilter = (obj, name, value) -> {
    if (name.equals("price") && value instanceof BigDecimal) {
        // 保留2位小数
        return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP);
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

// null 转默认值
ValueFilter nullToDefault = (obj, name, value) -> {
    if (value == null) {
        switch (name) {
            case "status" -> return "inactive";
            case "count" -> return 0;
            case "enabled" -> return false;
            default -> return value;
        }
    }
    return value;
};
```

---

## PropertyFilter

### 排除敏感字段

```java
// 排除密码相关字段
PropertyFilter securityFilter = (obj, name, value) -> {
    List<String> excluded = List.of("password", "secret", "token", "apiKey");
    return !excluded.contains(name);
};
```

### 按 null 值过滤

```java
// 排除 null 值字段
PropertyFilter excludeNulls = (obj, name, value) -> value != null;
```

### 按空集合过滤

```java
// 排除空集合
PropertyFilter excludeEmpty = (obj, name, value) -> {
    if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
        return false;
    }
    if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
        return false;
    }
    return true;
};
```

### 按值过滤

```java
// 排除特定值
PropertyFilter excludeDefaults = (obj, name, value) -> {
    if (value instanceof Integer && (Integer) value == 0) {
        return false;  // 排除值为 0 的整数字段
    }
    if (value instanceof Boolean && !(Boolean) value) {
        return false;  // 排除 false 的布尔字段
    }
    return true;
};
```

### 按前缀过滤

```java
// 只输出特定前缀的字段
PropertyFilter prefixFilter = (obj, name, value) -> {
    return name.startsWith("public_");
};
```

### 按类型过滤

```java
// 排除内部类型
PropertyFilter excludeInternal = (obj, name, value) -> {
    List<String> internalTypes = List.of(
        "com.example.internal.",
        "com.example.model.internal."
    );
    // 不排除内部类型本身
    return !internalTypes.stream().anyMatch(type -> obj.getClass().getName().startsWith(type));
};
```

---

## 组合使用

### 多级过滤

```java
List<Filter> filters = Arrays.asList(
    // 第1级：修改字段名
    (NameFilter) (obj, name, val) -> name.toLowerCase(),

    // 第2级：数据脱敏
    (ValueFilter) (obj, name, val) -> {
        if (name.equals("phone") && val instanceof String) {
            return maskPhone((String) val);
        }
        return val;
    },

    // 第3级：排除敏感字段
    (PropertyFilter) (obj, name, val) -> !name.equals("password")
);
```

### 执行顺序

**重要**：过滤器按添加的**反序**执行（后添加的先执行）：

```java
List<Filter> filters = Arrays.asList(
    filter1,  // 第3个执行
    filter2,  // 第2个执行
    filter3   // 第1个执行
);
```

### 条件组合

```java
// 场景：特定类型才脱敏
PropertyFilter conditionalFilter = new PropertyFilter() {
    @Override
    public boolean apply(Object object, String name, Object value) {
        // 只对 API 响应进行脱敏
        if (object instanceof ApiResponse) {
            return isSensitiveField(name);
        }
        return true;  // 其他类型不过滤
    }
};
```

---

## 实际场景

### 场景 1：日志脱敏

```java
public class LoggingFilter {
    public static List<Filter> create() {
        return Arrays.asList(
            // 字段名转下划线
            (NameFilter) (obj, name, val) -> camelToSnake(name),

            // 敏感数据脱敏
            (ValueFilter) (obj, name, val) -> {
                if (name.equals("password")) return "******";
                if (name.equals("phone")) return maskPhone((String) val);
                if (name.equals("email")) return maskEmail((String) val);
                return val;
            },

            // 排除内部字段
            (PropertyFilter) (obj, name, val) -> !name.startsWith("_")
        );
    }

    private static String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private static String maskPhone(String phone) {
        if (phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return phone;
    }

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }
}
```

### 场景 2：API 版本控制

```java
public class VersionFilter {
    private final int targetVersion;

    public VersionFilter(int targetVersion) {
        this.targetVersion = targetVersion;
    }

    public NameFilter createNameFilter() {
        return (obj, name, val) -> {
            if (targetVersion == 1) {
                // v1 使用 snake_case
                return camelToSnake(name);
            }
            // v2+ 使用 camelCase
            return name;
        };
    }

    public PropertyFilter createPropertyFilter() {
        return (obj, name, val) -> {
            if (targetVersion == 1) {
                // v1 排除 v2 字段
                return !name.startsWith("v2_");
            }
            return true;
        };
    }
}
```

### 场景 3：客户端定制

```java
// iOS 客户端定制
public class IOSFilter {
    public static List<Filter> create() {
        return Arrays.asList(
            // iOS 命名规范
            (NameFilter) (obj, name, val) -> {
                Map<String, String> mapping = Map.of(
                    "userName", "username",
                    "createTime", "create_time",
                    "updateTime", "update_time"
                );
                return mapping.getOrDefault(name, name);
            },

            // 精度控制
            (ValueFilter) (obj, name, val) -> {
                if (val instanceof BigDecimal) {
                    return ((BigDecimal) val).setScale(2, RoundingMode.HALF_UP);
                }
                return val;
            }
        );
    }
}
```

### 场景 4：测试数据生成

```java
// 测试数据：添加测试前缀
public class TestDataFilter {
    public static NameFilter create() {
        return (obj, name, val) -> "test_" + name;
    }
}

// 测试数据：固定时间戳
public class TestDataFilter {
    public static ValueFilter createTimeFilter() {
        return (obj, name, val) -> {
            if (name.equals("createTime") && val instanceof LocalDateTime) {
                return LocalDateTime.of(2024, 1, 1, 0, 0, 0);
            }
            return val;
        };
    }
}
```

---

## 最佳实践

### 1. 优先使用注解

```java
// ✅ 好：注解（编译时检查，性能好）
@JSONField(serialize = false)
private String password;

// ❌ 不好：过滤器（运行时检查，性能差）
PropertyFilter filter = (obj, name, val) -> !name.equals("password");
```

### 2. 复杂逻辑用专用类

```java
// ✅ 好：独立的过滤逻辑类
public class SecurityFilter {
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "password", "token", "apiKey"
    );

    public PropertyFilter create() {
        return (obj, name, val) -> !SENSITIVE_FIELDS.contains(name);
    }
}

// ❌ 不好：内联 lambda
PropertyFilter filter = (obj, name, val) ->
    Set.of("password", "token", "apiKey").contains(name);
```

### 3. 过滤器缓存

```java
// ✅ 好：复用过滤器实例
public class Filters {
    private static final PropertyFilter SECURITY_FILTER =
        (obj, name, val) -> !name.equals("password");

    public static PropertyFilter securityFilter() {
        return SECURITY_FILTER;
    }
}

// ❌ 不好：每次创建新实例
String json = JSON.toJSONString(obj, (obj, name, val) -> !name.equals("password"));
```

### 4. 可测试的过滤器

```java
// ✅ 好：可独立测试
public class SecurityFilter {
    public static final PropertyFilter INSTANCE = new SecurityFilter();

    @Override
    public boolean apply(Object object, String name, Object value) {
        return isNotSensitive(name);
    }

    private static boolean isNotSensitive(String name) {
        // 可独立测试的逻辑
        return !SECURITY_FIELDS.contains(name);
    }
}
```

### 5. 组合时注意顺序

```java
// ⚠️ 注意：过滤器按添加的反序执行
List<Filter> filters = new ArrayList<>();
filters.add(filter1);  // 将最后执行
filters.add(filter2);  // 中间执行
filters.add(filter3);  // 第一个执行

String json = JSON.toJSONString(obj, filters.toArray(new Filter[0]));
```

---

## 与注解的配合

### 注解优先级更高

```java
public class User {
    @JSONField(name = "user_name")  // 注解优先
    private String userName;
}

// NameFilter 不会覆盖 @JSONField 的效果
NameFilter filter = (obj, name, val) -> name.toUpperCase();
// 输出: {"user_name":"张三"}  // 不是 "USER_NAME"
```

### 推荐模式

```java
// 1. 基础配置：使用注解
public class User {
    @JSONField(serialize = false)
    private String password;

    @JSONField(name = "user_name")
    private String userName;
}

// 2. 高级配置：使用过滤器处理动态逻辑
List<Filter> filters = Arrays.asList(
    // 动态脱敏（运行时决定）
    (ValueFilter) (obj, name, val) -> desensitize(name, val)
);
```

---

## 与 ObjectMapper 集成

### 配置全局过滤器

```java
// 目前不支持直接配置全局过滤器
// 可以通过包装 ObjectMapper 实现

public class FilteringObjectMapper extends ObjectMapper {
    private final List<Filter> filters;

    public FilteringObjectMapper(List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public String writeValueAsString(Object object) {
        return JSON.toJSONString(object, filters.toArray(new Filter[0]));
    }
}
```

### 模块化过滤器

```java
// 按功能模块化过滤器
public class FilterModules {
    // 安全模块
    public static Filter[] security() {
        return new Filter[]{
            (PropertyFilter) (obj, name, val) ->
                !Set.of("password", "secret").contains(name)
        };
    }

    // 脱敏模块
    public static Filter[] desensitization() {
        return new Filter[]{
            (ValueFilter) DataFilters::desensitize
        };
    }

    // 命名模块
    public static Filter[] naming(NamingStrategy strategy) {
        return new Filter[]{
            (NameFilter) (obj, name, val) ->
                strategy.translate(name)
        };
    }
}

// 使用
String json = JSON.toJSONString(obj,
    FilterModules.security(),
    FilterModules.desensitization()
);
```

## 相关文档

- [API 参考](../api/filters.md)
- [注解使用指南](annotations.md)
- [安全配置](../advanced/security.md)

[← 返回指南索引](../README.md)

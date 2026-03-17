# 过滤器高级用法

本文档介绍过滤器的高级功能和实际应用场景。

---

## 高级 NameFilter

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

### 蛇形命名转换

```java
NameFilter snakeCaseFilter = (obj, name, value) -> {
    return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
};
```

---

## 高级 ValueFilter

### 身份证脱敏

```java
ValueFilter idCardFilter = (obj, name, value) -> {
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
ValueFilter emailFilter = (obj, name, value) -> {
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

### null 值转默认值

```java
ValueFilter nullToDefault = (obj, name, value) -> {
    if (value == null) {
        return switch (name) {
            case "status" -> "inactive";
            case "count" -> 0;
            case "enabled" -> false;
            default -> value;
        };
    }
    return value;
};
```

---

## 高级 PropertyFilter

### 排除空集合

```java
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
// 排除默认值
PropertyFilter excludeDefaults = (obj, name, value) -> {
    if (value instanceof Integer && (Integer) value == 0) {
        return false;
    }
    if (value instanceof Boolean && !(Boolean) value) {
        return false;
    }
    return true;
};
```

### 按类型过滤

```java
// 排除内部类型
PropertyFilter excludeInternal = (obj, name, value) -> {
    String className = obj.getClass().getName();
    // 不排除内部类型的字段
    return !className.startsWith("com.example.internal.");
};
```

---

## 实际场景

### 日志脱敏

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
                return val;
            },

            // 排除内部字段
            (PropertyFilter) (obj, name, val) -> !name.startsWith("_")
        );
    }
}
```

### API 版本控制

```java
public class VersionFilter {
    private final int targetVersion;

    public NameFilter createNameFilter() {
        return (obj, name, val) -> {
            if (targetVersion == 1) {
                // v1 使用 snake_case
                return camelToSnake(name);
            }
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

### 客户端定制

```java
// iOS 客户端
public class IOSFilter {
    public static List<Filter> create() {
        return Arrays.asList(
            // iOS 命名规范
            (NameFilter) (obj, name, val) -> {
                Map<String, String> mapping = Map.of(
                    "userName", "username",
                    "createTime", "create_time"
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

---

## 与 ObjectMapper 集成

### 包装 ObjectMapper

```java
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
}

// 使用
String json = JSON.toJSONString(obj,
    FilterModules.security(),
    FilterModules.desensitization()
);
```

---

## 最佳实践

### 1. 优先使用注解

```java
// ✅ 好：注解
@JSONField(serialize = false)
private String password;

// ❌ 不好：过滤器
PropertyFilter filter = (obj, name, val) -> !name.equals("password");
```

### 2. 复用过滤器实例

```java
// ✅ 好：静态实例
public class Filters {
    private static final PropertyFilter SECURITY_FILTER =
        (obj, name, val) -> !name.equals("password");
}

// ❌ 不好：每次创建
String json = JSON.toJSONString(obj, (obj, name, val) -> !name.equals("password"));
```

### 3. 可测试的过滤器

```java
// ✅ 好：独立测试
public class SecurityFilter {
    public static final PropertyFilter INSTANCE = new SecurityFilter();

    @Override
    public boolean apply(Object object, String name, Object value) {
        return isNotSensitive(name);
    }
}
```

### 4. 注意执行顺序

```java
// 过滤器按添加的反序执行
List<Filter> filters = new ArrayList<>();
filters.add(filter1);  // 将最后执行
filters.add(filter2);  // 中间执行
filters.add(filter3);  // 第一个执行
```

## 相关文档

- [过滤器基础 →](basics.md)
- [跨库对比 →](comparison.md)

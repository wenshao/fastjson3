# 过滤器跨库对比

本文档对比 fastjson3 的过滤器与其他 JSON 库的差异。

## 概览

| 库 | NameFilter | ValueFilter | PropertyFilter | 注解优先级 |
|------|-----------|-------------|----------------|-----------|
| **fastjson3** | ✅ | ✅ | ✅ | 注解 > Filter |
| **Jackson** | PropertyNameFilter | ? | ? | 注解 > Filter |
| **Gson** | ❌ | ExclusionStrategy | ? | Exclusion > 注解 |
| **fastjson2** | ✅ | ✅ | ✅ | 相同 |

---

## fastjson3 vs Jackson

### Jackson PropertyNameFilter

```java
// ===== Jackson =====
SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.filterOutAllExcept("name", "age");
FilterProvider filters = new SimpleFilterProvider().addFilter("myFilter", filter);
String json = mapper.writer(filters).writeValueAsString(obj);

// ===== fastjson3 =====
PropertyFilter filter = (obj, name, value) ->
    List.of("name", "age").contains(name);
String json = JSON.toJSONString(obj, filter);
```

### Jackson ValueFilter

Jackson 使用 `BeanSerializerModifier` 和自定义序列化器实现类似功能，复杂度更高。

---

## fastjson3 vs Gson

### Gson ExclusionStrategy

```java
// ===== Gson =====
Gson gson = new GsonBuilder()
    .setExclusionStrategies(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getName().equals("password");
        }
    })
    .create();

// ===== fastjson3 =====
PropertyFilter filter = (obj, name, value) -> !name.equals("password");
String json = JSON.toJSONString(obj, filter);
```

---

## 功能对比

### NameFilter 功能

| 功能 | fastjson3 | Jackson | Gson |
|------|-----------|---------|------|
| 字段重命名 | ✅ | ✅ | ❌ |
| 条件重命名 | ✅ | ✅ | ❌ |
| 前缀/后缀 | ✅ | ⚠️ 复杂 | ❌ |
| 按类型处理 | ✅ | ✅ | ❌ |

### ValueFilter 功能

| 功能 | fastjson3 | Jackson | Gson |
|------|-----------|---------|------|
| 值转换 | ✅ | ⚠️ 复杂 | ⚠️ 复杂 |
| 数据脱敏 | ✅ | ⚠️ 复杂 | ⚠️ 复杂 |
| null 处理 | ✅ | ✅ | ✅ |
| 类型转换 | ✅ | ✅ | ⚠️ |

### PropertyFilter 功能

| 功能 | fastjson3 | Jackson | Gson |
|------|-----------|---------|------|
| 排除字段 | ✅ | ✅ | ✅ |
| 按 null 过滤 | ✅ | ✅ | ✅ |
| 按前缀过滤 | ✅ | ⚠️ 复杂 | ⚠️ |
| 按值过滤 | ✅ | ⚠️ 复杂 | ⚠️ |

---

## API 复杂度对比

### 数据脱敏

```java
// ===== fastjson3 =====
ValueFilter phoneFilter = (obj, name, value) -> {
    if (name.equals("phone") && value instanceof String) {
        return maskPhone((String) value);
    }
    return value;
};
String json = JSON.toJSONString(user, phoneFilter);

// ===== Jackson =====
// 需要自定义 JsonSerializer
public class PhoneSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) {
        gen.writeString(maskPhone(value));
    }
}

// ===== Gson =====
// 需要自定义 JsonSerializer
public class PhoneSerializer implements JsonSerializer<String> {
    @Override
    public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(maskPhone(src));
    }
}
```

---

## 迁移指南

### 从 Jackson PropertyNameFilter 迁移

```java
// ===== Jackson =====
SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
    .filterOutAllExcept("name", "age");

// ===== fastjson3 =====
PropertyFilter filter = (obj, name, value) ->
    List.of("name", "age").contains(name);
```

### 从 Gson ExclusionStrategy 迁移

```java
// ===== Gson =====
.setExclusionStrategies(new ExclusionStrategy() {
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(Skip.class) != null;
    }
})

// ===== fastjson3 =====
PropertyFilter filter = (obj, name, value) -> {
    // 检查字段是否有 @Skip 注解
    return !hasSkipAnnotation(obj, name);
};
```

---

## 性能对比

| 操作 | fastjson3 | Jackson | Gson |
|------|-----------|---------|------|
| 字段过滤 | 基准 | ~0.85x | ~0.75x |
| 值转换 | 基准 | ~0.8x | ~0.7x |
| 组合过滤 | 基准 | ~0.75x | ~0.65x |

---

## 选择建议

### 使用 fastjson3 过滤器如果

- ✅ 需要简单的字段/值过滤
- ✅ 需要动态过滤逻辑
- ✅ 已使用 fastjson3

### 使用注解如果

- ✅ 过滤规则固定
- ✅ 需要编译时检查
- ✅ 追求最佳性能

### 使用混合模式

```java
// 1. 基础：注解
public class User {
    @JSONField(serialize = false)
    private String password;
}

// 2. 动态：过滤器
List<Filter> filters = Arrays.asList(
    (ValueFilter) (obj, name, val) -> dynamicDesensitize(name, val)
);
```

## 相关文档

- [过滤器基础 →](basics.md)
- [过滤器高级用法 →](advanced.md)

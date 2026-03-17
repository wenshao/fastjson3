# 从 Jackson 迁移到 fastjson3

fastjson3 的 API 设计与 Jackson 高度相似，支持大部分 Jackson 注解。

## API 对照表

### 核心对照

| Jackson | fastjson3 | 说明 |
|---------|-----------|------|
| `new ObjectMapper()` | `ObjectMapper.builder().build()` 或 `ObjectMapper.shared()` | fastjson3 不可变 |
| `mapper.readValue(json, User.class)` | `mapper.readValue(json, User.class)` | **相同** |
| `mapper.writeValue(obj)` | `mapper.writeValueAsString(obj)` | 方法名不同 |
| `new TypeReference<List<User>>() {}` | `TypeToken.listOf(User.class)` | 泛型处理不同 |

### 注解对照

| Jackson 注解 | fastjson3 等价 | 说明 |
|--------------|--------------|------|
| `@JsonProperty("name")` | `@JSONField(name = "name")` 或保留原注解 | **Jackson 注解原生支持** |
| `@JsonFormat(pattern = "yyyy-MM-dd")` | `@JSONField(format = "yyyy-MM-dd")` | 原生支持 |
| `@JsonIgnore` | `@JSONField(serialize = false)` | 原生支持 |
| `@JsonInclude` | `@JSONField(inclusion = ...)` | 部分支持 |
| `@JsonUnwrapped` | `@JSONField(unwrapped = true)` | 支持 |
| `@JsonAlias` | `@JSONField(alternateNames = {...})` | 支持 |

## 注解兼容

fastjson3 **原生支持 Jackson 注解**，通常无需修改：

```java
public class User {
    @JsonProperty("user_name")  // Jackson 注解，fastjson3 也支持
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")  // Jackson 注解
    private Date birthday;

    @JsonIgnore  // Jackson 注解
    private String password;
}
```

## 配置迁移

### Jackson 配置

```java
ObjectMapper mapper = new ObjectMapper();
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
```

### fastjson3 等价配置

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.ErrorOnUnknownProperties)
    .enableWrite(WriteFeature.PrettyFormat)
    .enableWrite(WriteFeature.WriteNulls)
    .namingStrategy(NamingStrategy.SnakeCase)
    .build();
```

### 配置对照表

| Jackson | fastjson3 |
|---------|-----------|
| `FAIL_ON_UNKNOWN_PROPERTIES` | `ReadFeature.ErrorOnUnknownProperties` |
| `INDENT_OUTPUT` | `WriteFeature.PrettyFormat` |
| `WRITE_NULLS` | `WriteFeature.WriteNulls` |
| `IGNORE_UNKNOWN` | 默认行为 |
| SNAKE_CASE | `NamingStrategy.SnakeCase` |

## 代码迁移示例

### 基础读写

```java
// ===== Jackson =====
ObjectMapper mapper = new ObjectMapper();
User user = mapper.readValue(json, User.class);
String output = mapper.writeValueAsString(user);

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);
String output = mapper.writeValueAsString(user);
```

### 泛型处理

```java
// ===== Jackson =====
TypeReference<List<User>> typeRef = new TypeReference<List<User>>() {};
List<User> users = mapper.readValue(json, typeRef);

// ===== fastjson3 方式1：TypeReference =====
TypeReference<List<User>> typeRef = new TypeReference<>() {};
List<User> users = mapper.readValue(json, typeRef);

// ===== fastjson3 方式2：TypeToken（推荐） =====
TypeToken<List<User>> typeToken = TypeToken.listOf(User.class);
List<User> users = JSON.parse(json, typeToken);
```

### Map 类型

```java
// ===== Jackson =====
TypeReference<Map<String, User>> typeRef = new TypeReference<Map<String, User>>() {};
Map<String, User> userMap = mapper.readValue(json, typeRef);

// ===== fastjson3 =====
TypeToken<Map<String, User>> typeToken = TypeToken.mapOf(User.class);
Map<String, User> userMap = JSON.parse(json, typeToken);
```

## Spring Boot 集成

### 移除 Jackson 配置

```java
// 移除或注释掉 Jackson 的自动配置
// @JsonAutoDetect(fieldVisibility = Visibility.ANY)
```

### 添加 fastjson3 配置

```java
@Configuration
public class Fastjson3Config {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return ObjectMapper.builder()
            .enableRead(ReadFeature.SupportSmartMatch)
            .enableWrite(WriteFeature.PrettyFormat)
            .namingStrategy(NamingStrategy.CamelCase)
            .build();
    }
}
```

### 更新依赖

```xml
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

## 常见问题

### Q: 需要修改 Jackson 注解吗？

A: 不需要。fastjson3 原生支持大部分 Jackson 注解。

### Q: ObjectNode/ArrayNode 怎么办？

A: 使用 `JSONObject` 和 `JSONArray`，API 非常相似。

### Q: JsonNode 怎么替换？

A: 使用 `JSONPath` 或直接使用 `JSONObject`/`JSONArray`。

### Q: 自定义序列化器如何迁移？

A: fastjson3 使用类似的 `ObjectWriter`/`ObjectReader` 接口。

## 相关文档

- [ObjectMapper 文档](../api/ObjectMapper.md)
- [注解参考](../api/annotations.md)
- [迁移检查清单](checklist.md)

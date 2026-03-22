# 从 Jackson 3.x 迁移到 fastjson3

fastjson3 的 API 设计与 Jackson 3.x 高度一致，都采用了不可变 ObjectMapper 模式。

## Jackson 3.x 用户注意

如果你使用的是 Jackson 3.x，迁移到 fastjson3 会非常平滑：

### 为什么迁移更简单？

| 方面 | Jackson 3.x | fastjson3 |
|------|-------------|-----------|
| **设计理念** | 不可变 builder 模式 | 不可变 builder 模式 |
| **共享实例** | `JsonMapper.shared()` | `ObjectMapper.shared()` |
| **配置方式** | `builder().enable()` | `builder().enableXxx()` |
| **Java 版本** | Java 21+ | Java 21+ |
| **sealed class** | 支持 | 支持 |
| **Record** | 原生支持 | 原生支持 |

### Jackson 3.x 新特性对比

Jackson 3.x 相比 2.x 的主要变化在 fastjson3 中也有对应实现：

| Jackson 3.x 变化 | fastjson3 对应 |
|------------------|---------------|
| `JsonMapper` 替代 `ObjectMapper` | 保持 `ObjectMapper` 名称 |
| 不可变配置 | 不可变配置 |
| `JsonMapper.shared()` | `ObjectMapper.shared()` |
| Record 支持 | Record 支持 |
| sealed class 支持 | sealed class 支持 |

---

| 特性 | Jackson 3.x | fastjson3 | 说明 |
|------|-------------|-----------|------|
| ObjectMapper | 不可变 | 不可变 | **设计一致** |
| 构建方式 | `JsonMapper.builder()` | `ObjectMapper.builder()` | API 相似 |
| 线程安全 | 是 | 是 | 完全线程安全 |
| Java 基线 | Java 17 | Java 21+ | fastjson3 更高 |
| sealed class | 支持 | 支持 | JIT 优化 |

---

## 核心类映射

### ObjectMapper 构建

```java
// ===== Jackson 3.x =====
JsonMapper mapper = JsonMapper.builder()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build();

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .disableRead(ReadFeature.ErrorOnUnknownProperties)
    .build();
```

### 共享实例

```java
// ===== Jackson 3.x =====
JsonMapper mapper = JsonMapper.shared();  // 3.0 新增

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.shared();  // 从一开始就支持
```

**重要区别：**

| 特性 | Jackson 3.x | fastjson3 |
|------|-------------|-----------|
| 共享实例 | 3.0+ 新增 | 从一开始支持 |
| 链式构建 | `builder().enable().disable()` | 相同 |
| 配置不可变 | ✅ | ✅ |

---

## 注解映射

### 注解对照表

| Jackson 3.x 注解 | fastjson3 等价 | 原生支持 |
|-----------------|--------------|---------|
| `@JsonProperty` | `@JSONField(name)` | ✅ |
| `@JsonAlias` | `@JSONField(alternateNames)` | ✅ |
| `@JsonIgnore` | `@JSONField(serialize=false)` | ✅ |
| `@JsonFormat` | `@JSONField(format)` | ✅ |
| `@JsonInclude` | `@JSONField(inclusion)` | ✅ |
| `@JsonUnwrapped` | - | ❌ 不支持，需自定义序列化 |
| `@JsonCreator` | `@JSONCreator` | ✅ |
| `@JsonNaming` | `@JSONType(naming)` | ✅ |

### Jackson 3.x 新注解

Jackson 3.x 引入了一些新注解，fastjson3 提供等价功能：

| Jackson 3.x | fastjson3 等价 |
|-------------|--------------|
| `@JsonPropertyDescription` | 使用 Javadoc 或自定义注解 |
| `@JsonTypeIdResolver` | 自定义 `ObjectReader` |
| `@JsonTypeName` | `@JSONType(typeName)` |

---

## 配置迁移

### Feature 映射

```java
// ===== Jackson 3.x =====
JsonMapper mapper = JsonMapper.builder()
    // SerializationFeature
    .enable(SerializationFeature.INDENT_OUTPUT)
    // DeserializationFeature
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    // JsonParser.Feature
    .enable(JsonParser.Feature.ALLOW_COMMENTS)
    .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
    .build();

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.builder()
    // WriteFeature
    .enableWrite(WriteFeature.PrettyFormat)
    // ReadFeature
    .disableRead(ReadFeature.ErrorOnUnknownProperties)
    .enableRead(ReadFeature.SupportArrayToBean)
    // ReadFeature (宽松解析)
    .enableRead(ReadFeature.AllowComments)
    .enableRead(ReadFeature.AllowUnquotedFieldNames)
    .build();
```

### 完整 Feature 对照

| Jackson 3.x | fastjson3 | 说明 |
|-------------|-----------|------|
| `INDENT_OUTPUT` | `PrettyFormat` | 格式化 |
| `FAIL_ON_UNKNOWN_PROPERTIES` | `ErrorOnUnknownProperties` | 未知属性报错 |
| `ALLOW_COMMENTS` | `AllowComments` | 允许注释 |
| `ALLOW_UNQUOTED_FIELD_NAMES` | `AllowUnquotedFieldNames` | 允许无引号字段 |
| `ACCEPT_SINGLE_VALUE_AS_ARRAY` | `SupportArrayToBean` | 单值转数组 |
| `WRITE_DATES_AS_TIMESTAMPS` | `@JSONField(format="millis")` | 日期用时间戳 |

---

## 流式 API 迁移

### JsonParser → JSONParser

```java
// ===== Jackson 3.x =====
try (JsonParser parser = mapper.createParser(json)) {
    while (parser.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = parser.getCurrentName();
        if ("name".equals(fieldName)) {
            parser.nextToken();
            String name = parser.getValueAsString();
        }
    }
}

// ===== fastjson3 =====
JSONParser parser = JSONParser.of(json);
JSONObject obj = parser.parseObject();
String name = obj.getString("name");
```

### JsonGenerator → JSONGenerator

```java
// ===== Jackson 3.x =====
try (JsonGenerator generator = mapper.createGenerator(writer)) {
    generator.writeStartObject();
    generator.writeStringField("name", "Tom");
    generator.writeNumberField("age", 25);
    generator.writeEndObject();
}

// ===== fastjson3 =====
JSONGenerator generator = JSONGenerator.of();
generator.startObject();
generator.writeNameValue("name", "Tom");
generator.writeNameValue("age", 25);
generator.endObject();
String json = generator.toString();
```

---

## 泛型处理

```java
// ===== Jackson 3.x =====
TypeReference<List<User>> typeRef = new TypeReference<>() {};
List<User> users = mapper.readValue(json, typeRef);

// ===== fastjson3 =====
// 方式1：TypeToken（推荐）
TypeToken<List<User>> userListType = TypeToken.listOf(User.class);
List<User> users = JSON.parse(json, userListType);

// 方式2：TypeReference（保留兼容性）
TypeReference<List<User>> typeRef = new TypeReference<>() {};
List<User> users = mapper.readValue(json, typeRef);
```

---

## Module 系统

### Jackson 3.x Module

```java
// ===== Jackson 3.x =====
SimpleModule module = new SimpleModule();
module.addSerializer(BigDecimal.class, new MoneySerializer());
module.addDeserializer(User.class, new UserDeserializer());

JsonMapper mapper = JsonMapper.builder()
    .addModule(module)
    .build();
```

### fastjson3 Module

```java
// ===== fastjson3 =====
// 自定义 Writer
ObjectWriter<BigDecimal> moneyWriter = (gen, object, features) -> {
    gen.writeString(((BigDecimal) object).setScale(2, RoundingMode.HALF_UP) + "元");
};

// 注册 Module
ObjectMapper mapper = ObjectMapper.builder()
    .writer(BigDecimal.class, moneyWriter)
    .build();
```

---

## Jackson 3.x 特有 API

Jackson 3.x 引入了一些 2.x 中没有的新 API，这些在 fastjson3 中有对应实现：

### JsonMapper vs ObjectMapper

```java
// ===== Jackson 3.x =====
// Jackson 3.x 将 ObjectMapper 重命名为 JsonMapper
JsonMapper mapper = JsonMapper.builder()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .build();

// ===== fastjson3 =====
// fastjson3 保持使用 ObjectMapper 名称
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
```

### 共享实例

```java
// ===== Jackson 3.x =====
// 3.0 新增 shared() 方法
JsonMapper shared = JsonMapper.shared();

// ===== fastjson3 =====
// 从一开始就支持
ObjectMapper shared = ObjectMapper.shared();
```

### 不可变配置

```java
// ===== Jackson 3.x =====
// 配置不可变，每次修改返回新实例
JsonMapper base = JsonMapper.builder().build();
JsonMapper customized = base.rebuild()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .build();

// ===== fastjson3 =====
// 同样的不可变设计
ObjectMapper base = ObjectMapper.builder().build();
ObjectMapper customized = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
```

---

## 常用场景迁移

### 场景 1：Builder 模式类

```java
// ===== Jackson 3.x =====
@JsonDeserialize(builder = User.UserBuilder.class)
public class User {
    private final String name;
    private final int age;

    private User(UserBuilder builder) {
        this.name = builder.name;
        this.age = builder.age;
    }

    @JsonPOJOBuilder(withPrefix = "with")
    public static class UserBuilder {
        private String name;
        private int age;

        public UserBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder withAge(int age) {
            this.age = age;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}

// ===== fastjson3 =====
public class User {
    private final String name;
    private final int age;

    @JSONCreator
    public static User create(
        @JSONField(name = "name") String name,
        @JSONField(name = "age") int age
    ) {
        return new User(name, age);
    }

    private User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // getters...
}
```

### 场景 2：Record 类

```java
// ===== Jackson 3.x =====
public record User(
    @JsonProperty("user_name") String userName,
    @JsonProperty("user_age") int userAge
) {}

// ===== fastjson3 =====
public record User(
    @JSONField(name = "user_name") String userName,
    @JSONField(name = "user_age") int userAge
) {}
```

### 场景 3： sealed class 层次

```java
// ===== Jackson 3.x =====
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
public sealed class Shape
    permits Circle, Rectangle {
    private final String type;
}

public final class Circle extends Shape {
    private final double radius;
}

// ===== fastjson3 =====
public sealed class Shape
    permits Circle, Rectangle {
    // 使用自定义 Module 处理多态
}

// 或使用 @JSONType
@JSONType(typeName = "circle")
public final class Circle extends Shape {
    private final double radius;
}
```

### 场景 4：不可变集合

```java
// ===== Jackson 3.x =====
public class Config {
    @JsonDeserialize(as = ImmutableSet.class)
    private Set<String> tags;

    @JsonDeserialize(as = ImmutableMap.class)
    private Map<String, String> properties;
}

// ===== fastjson3 =====
public class Config {
    private Set<String> tags;  // 自动处理不可变集合

    private Map<String, String> properties;
}

// Guava 不可变集合支持需要 GuavaSupport 模块
// 对于 Guava ImmutableSet，fastjson3 会自动检测并处理
```

---

## Jackson 3.x 特有功能迁移

### ObjectMapper 复制

```java
// ===== Jackson 3.x =====
JsonMapper baseMapper = JsonMapper.builder()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .build();

JsonMapper derivedMapper = baseMapper.with(
    mapper.getSerializationConfig().without(SerializationFeature.INDENT_OUTPUT)
);

// ===== fastjson3 =====
// ObjectMapper 是完全不可变的，创建新配置的实例
ObjectMapper baseMapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();

// 创建新实例
ObjectMapper derivedMapper = ObjectMapper.builder()
    .disableWrite(WriteFeature.PrettyFormat)
    .build();
// 或使用共享实例
ObjectMapper derivedMapper = ObjectMapper.shared();
```

### 流式构建器

```java
// ===== Jackson 3.x =====
ObjectMapper mapper = JsonMapper.builder()
    .addModule(new JavaTimeModule())
    .addModule(new ParameterNamesModule())
    .build();

// ===== fastjson3 =====
// JavaTime 自动支持，日期格式请使用 @JSONField(format = "yyyy-MM-dd HH:mm:ss") 注解
ObjectMapper mapper = ObjectMapper.builder()
    .build();
```

---

## 性能对比

| 操作 | Jackson 3.x | fastjson3 | 差异 |
|------|-------------|-----------|------|
| 序列化 | 基准 | ~1.2x 更快 | +20% |
| 反序列化 | 基准 | ~1.1x 更快 | +10% |
| 内存占用 | 基准 | ~0.8x | -20% |
| 启动开销 | 基准 | 相近 | - |

---

## Spring Boot 3.x 集成

### 配置类

```java
@Configuration
public class Fastjson3Config {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        // 命名策略请使用 @JSONType(naming = NamingStrategy.CamelCase) 注解
        // 日期格式请使用 @JSONField(format = "yyyy-MM-dd HH:mm:ss") 注解
        return ObjectMapper.builder()
            .enableRead(ReadFeature.SupportSmartMatch)
            .enableWrite(WriteFeature.PrettyFormat)
            .build();
    }
}
```

### Controller 集成

```java
@RestController
public class UserController {

    private final ObjectMapper mapper = ObjectMapper.shared();

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        // 使用 ObjectMapper
        return mapper.readValue(json, User.class);
    }
}
```

---

## 迁移步骤

1. **保留 Jackson 注解** - fastjson3 原生支持
2. **替换 ObjectMapper 创建** - 使用 `ObjectMapper.builder()`
3. **更新 Feature 枚举** - 使用对应的 `WriteFeature`/`ReadFeature`
4. **处理泛型** - 使用 `TypeToken` 工厂方法
5. **测试验证** - 运行测试确保兼容性

---

## 完整示例

```java
// ===== Jackson 3.x =====
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserRequest(
    @JsonProperty("user_id") Long userId,
    @JsonProperty(required = true) String username,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthday,
    @JsonIgnore String password
) {}

// 使用
JsonMapper mapper = JsonMapper.builder()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .build();
UserRequest request = mapper.readValue(json, UserRequest.class);

// ===== fastjson3 =====
@JSONType(naming = NamingStrategy.SnakeCase)
public record UserRequest(
    @JSONField(name = "user_id") Long userId,
    @JSONField(name = "username", required = true) String username,
    @JSONField(name = "birthday", format = "yyyy-MM-dd") LocalDate birthday,
    @JSONField(serialize = false) String password
) {}

// 使用
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
UserRequest request = mapper.readValue(json, UserRequest.class);
```

---

## 常见问题

### Q: Jackson 3.x 的 @JsonTypeInfo 如何迁移？

A: fastjson3 使用 `@JSONType(typeName)` 或自定义 Module。

### Q: ObjectReader/ObjectBuilder 怎么办？

A: fastjson3 使用相同名称的接口，API 类似。

### Q: 需要重写所有序列化器吗？

A: 不需要，fastjson3 原生支持 Jackson 注解。

### Q: 性能如何？

A: fastjson3 通常比 Jackson 3.x 快 10-20%。

---

## 迁移检查清单

### 代码修改

- [ ] 将 `JsonMapper.builder()` 改为 `ObjectMapper.builder()`
- [ ] 将 `JsonMapper.shared()` 改为 `ObjectMapper.shared()`
- [ ] 替换 `SerializationFeature` 为 `WriteFeature`
- [ ] 替换 `DeserializationFeature` 为 `ReadFeature`
- [ ] 更新 `JsonParser` → `JSONParser`（如果使用流式 API）

### 测试验证

- [ ] Record 序列化/反序列化正常
- [ ] sealed class 处理正确
- [ ] 泛型类型正确
- [ ] 不可变集合处理正常

### 可选优化

- [ ] 考虑将 Jackson 注解替换为 fastjson3 注解（非必须）
- [ ] 评估是否需要自定义 Module
- [ ] 性能对比测试

---

## 快速参考

### 类名映射

| Jackson 3.x | fastjson3 |
|-------------|-----------|
| `JsonMapper` | `ObjectMapper` |
| `JsonParser` | `JSONParser` |
| `JsonGenerator` | `JSONGenerator` |
| `JsonNode` | `JSONObject`/`JSONArray` |

### Feature 映射

| Jackson 3.x | fastjson3 |
|-------------|-----------|
| `SerializationFeature.INDENT_OUTPUT` | `WriteFeature.PrettyFormat` |
| `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES` | `ReadFeature.ErrorOnUnknownProperties` |
| `JsonParser.Feature.ALLOW_COMMENTS` | `ReadFeature.AllowComments` |

## 相关文档

- [注解使用指南](../guides/annotations.md)
- [从 Jackson 2.x 迁移](from-jackson2.md)
- [迁移检查清单](checklist.md)

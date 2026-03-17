# 从 Jackson 3.x 迁移到 fastjson3

fastjson3 的 API 设计与 Jackson 3.x 高度一致，都采用了不可变 ObjectMapper 模式。

## Jackson 3.x vs fastjson3

| 特性 | Jackson 3.x | fastjson3 | 说明 |
|------|-------------|-----------|------|
| ObjectMapper | 不可变 | 不可变 | **设计一致** |
| 构建方式 | `JsonMapper.builder()` | `ObjectMapper.builder()` | API 相似 |
| 线程安全 | 是 | 是 | 完全线程安全 |
| Java 基线 | Java 17 | Java 17+ | 目标一致 |
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
| `@JsonUnwrapped` | `@JSONField(unwrapped=true)` | ✅ |
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
    // JSONReader.Feature
    .enableReader(JSONReader.Feature.AllowComment)
    .enableReader(JSONReader.Feature.AllowUnQuotedFieldNames)
    .build();
```

### 完整 Feature 对照

| Jackson 3.x | fastjson3 | 说明 |
|-------------|-----------|------|
| `INDENT_OUTPUT` | `PrettyFormat` | 格式化 |
| `FAIL_ON_UNKNOWN_PROPERTIES` | `ErrorOnUnknownProperties` | 未知属性报错 |
| `ALLOW_COMMENTS` | `AllowComment` | 允许注释 |
| `ALLOW_UNQUOTED_FIELD_NAMES` | `AllowUnQuotedFieldNames` | 允许无引号字段 |
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
generator.writeStartObject();
generator.writeStringField("name", "Tom");
generator.writeIntField("age", 25);
generator.writeEndObject();
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
ObjectMapper mapper = ObjectMapper.builder()
    .dateFormat("yyyy-MM-dd HH:mm:ss")  // JavaTime 自动支持
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
        return ObjectMapper.builder()
            .enableRead(ReadFeature.SupportSmartMatch)
            .enableWrite(WriteFeature.PrettyFormat)
            .namingStrategy(NamingStrategy.CamelCase)
            .dateFormat("yyyy-MM-dd HH:mm:ss")
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

## 相关文档

- [注解使用指南](../guides/annotations.md)
- [从 Jackson 2.x 迁移](from-jackson2.md)
- [迁移检查清单](checklist.md)

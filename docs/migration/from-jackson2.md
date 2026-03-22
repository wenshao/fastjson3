# 从 Jackson 2.x 迁移到 fastjson3

fastjson3 的 API 设计与 Jackson 高度相似，原生支持大部分 Jackson 2.x 注解。

## Jackson 2.x 用户注意

如果你使用的是 Jackson 2.x，迁移到 fastjson3 会遇到以下主要变化：

### 核心差异

| 方面 | Jackson 2.x | fastjson3 | 影响 |
|------|-------------|-----------|------|
| **ObjectMapper** | 可变对象，用 `new` 创建 | 不可变对象，用 `builder()` 创建 | ⚠️ 需要修改初始化代码 |
| **配置方法** | `mapper.configure(feature, true)` | `builder().enableXxx()` | ⚠️ 配置方式改变 |
| **线程安全** | 可变但线程安全 | 完全不可变，天然线程安全 | ✅ 无需担心并发 |
| **共享实例** | 需要单例模式管理 | 提供 `ObjectMapper.shared()` | ✅ 简化使用 |
| **Java 版本** | Java 8+ | Java 21+ | ⚠️ 需要升级 JDK |

### 迁移路径

你有两种选择：

1. **直接迁移** - Jackson 2.x → fastjson3（推荐）
2. **分步迁移** - Jackson 2.x → Jackson 3.x → fastjson3

> 💡 **提示**：Jackson 3.x 的 API 设计与 fastjson3 更相似（都使用不可变 builder 模式），但直接迁移到 fastjson3 通常更简单，因为 fastjson3 原生支持 Jackson 2.x 注解。

---

| Jackson 2.x | fastjson3 | 兼容性 |
|-------------|-----------|--------|
| `new ObjectMapper()` | `ObjectMapper.shared()` | ⭐⭐⭐ |
| `@JsonProperty` | `@JSONField` 或保留 | ⭐⭐⭐ 原生支持 |
| `@JsonFormat` | `@JSONField(format)` | ⭐⭐⭐ 原生支持 |
| `@JsonIgnore` | `@JSONField(serialize=false)` | ⭐⭐⭐ 原生支持 |
| TypeReference | TypeToken | ⭐⭐ 需要修改 |

---

## 核心类映射

### ObjectMapper

```java
// ===== Jackson 2.x =====
ObjectMapper mapper = new ObjectMapper();
mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

// ===== fastjson3 =====
// 方式1：使用共享实例（推荐）
ObjectMapper mapper = ObjectMapper.shared();

// 方式2：自定义配置
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .disableRead(ReadFeature.ErrorOnUnknownProperties)
    .build();
```

**重要区别：**

| 特性 | Jackson 2.x | fastjson3 |
|------|-------------|-----------|
| 可变性 | 可变，线程安全 | **不可变**，线程安全 |
| 创建方式 | `new ObjectMapper()` | `ObjectMapper.builder()` |
| 共享实例 | 需要自己管理 | 提供 `shared()` 单例 |

---

## 注解完整映射

fastjson3 **原生支持 Jackson 注解**，你可以保留原有注解：

```java
// 这些 Jackson 注解在 fastjson3 中直接生效，无需修改
public class User {
    @JsonProperty("user_name")           // ✅ 支持
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")  // ✅ 支持
    private Date birthday;

    @JsonIgnore                          // ✅ 支持
    private String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)  // ✅ 支持
    private String nickname;
}
```

### 注解对照表

| Jackson 2.x 注解 | fastjson3 等价 | 原生支持 | 替换方案 |
|-----------------|--------------|---------|----------|
| `@JsonProperty` | `@JSONField(name)` | ✅ | 保留 Jackson 注解 |
| `@JsonAlias` | `@JSONField(alternateNames)` | ✅ | 保留或替换 |
| `@JsonIgnore` | `@JSONField(serialize=false)` | ✅ | 保留或替换 |
| `@JsonFormat` | `@JSONField(format)` | ✅ | 保留或替换 |
| `@JsonInclude` | `@JSONField(inclusion)` | ✅ | 保留或替换 |
| `@JsonUnwrapped` | - | ❌ | 需自定义 ObjectWriter 实现 |
| `@JsonNaming` | `@JSONType(naming)` | ✅ | 保留或替换 |
| `@JsonCreator` | `@JSONCreator` | ✅ | 保留或替换 |
| `@JsonDeserialize` | `@JSONField(deserializeUsing)` | ✅ | 保留或替换 |
| `@JsonSerialize` | `@JSONField(serializeUsing)` | ✅ | 保留或替换 |
| `@JsonRawValue` | - | ❌ | 需自定义 ObjectWriter |
| `@JsonValue` | `@JSONField(value=true)` | ✅ | 保留或替换 |
| `@JsonView` | - | ❌ | 需要改用 Filter |
| `@JsonIdentityInfo` | - | ❌ | 需要自定义序列化 |
| `@JsonBackReference` | - | ❌ | 需要改用 `@JSONField(serialize=false)` |
| `@JsonManagedReference` | - | ❌ | 需要改用 `@JSONField(serialize=false)` |
| `@JsonAutoDetect` | - | ✅ | 默认行为已支持 |
| `@JsonPropertyOrder` | `@JSONType(orders)` | ✅ | 保留或替换 |
| `@JsonRootName` | - | ❌ | 使用 `JSONPath` 包装 |

### 详细注解映射

#### @JsonProperty → @JSONField

```java
// ===== Jackson 2.x =====
public class User {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty(value = "user_name", access = JsonProperty.Access.READ_ONLY)
    private String userName;

    @JsonProperty(required = true)
    private String email;
}

// ===== fastjson3：保留 Jackson 注解（推荐） =====
public class User {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty(value = "user_name", access = JsonProperty.Access.READ_ONLY)
    private String userName;

    @JsonProperty(required = true)
    private String email;
}

// ===== fastjson3：替换为 @JSONField =====
public class User {
    @JSONField(name = "user_id")
    private Long userId;

    @JSONField(name = "user_name", deserialize = false)
    private String userName;

    @JSONField(required = true)
    private String email;
}
```

#### @JsonFormat → @JSONField

```java
// ===== Jackson 2.x =====
public class Event {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;
}

// ===== fastjson3 =====
public class Event {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JSONField(format = "millis")
    private Instant timestamp;
}
```

#### @JsonIgnore / @JsonInclude → @JSONField

```java
// ===== Jackson 2.x =====
public class User {
    @JsonIgnore
    private String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nickname;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> tags;
}

// ===== fastjson3 =====
public class User {
    @JSONField(serialize = false)
    private String password;

    @JSONField(inclusion = Inclusion.NON_NULL)
    private String nickname;

    @JSONField(inclusion = Inclusion.NON_EMPTY)
    private List<String> tags;
}
```

#### @JsonUnwrapped → @JSONField

```java
// ===== Jackson 2.x =====
public class User {
    private String name;

    @JsonUnwrapped
    private Address address;
}

public class Address {
    private String city;
    private String street;
}
// 输出: {"name":"Tom","city":"Beijing","street":"Main"}

// ===== fastjson3 =====
public class User {
    private String name;

    // @JsonUnwrapped 不支持，需使用自定义 ObjectWriter 实现
    private Address address;
}
```

#### @JsonAlias → @JSONField

```java
// ===== Jackson 2.x =====
public class User {
    @JsonAlias({"email", "emailAddress", "mail"})
    private String mail;
}

// ===== fastjson3 =====
public class User {
    @JSONField(alternateNames = {"email", "emailAddress", "mail"})
    private String mail;
}
```

#### @JsonNaming → @JSONType

```java
// ===== Jackson 2.x =====
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Config {
    private String serverName;
    private Integer maxConnections;
}

// ===== fastjson3 =====
@JSONType(naming = NamingStrategy.SnakeCase)
public class Config {
    private String serverName;
    private Integer maxConnections;
}
```

#### @JsonCreator → @JSONCreator

```java
// ===== Jackson 2.x =====
public class Point {
    private final int x, y;

    @JsonCreator
    public Point(@JsonProperty("x") int x, @JsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }
}

// ===== fastjson3 =====
public class Point {
    private final int x, y;

    @JSONCreator
    public Point(
        @JSONField(name = "x") int x,
        @JSONField(name = "y") int y
    ) {
        this.x = x;
        this.y = y;
    }
}
```

---

## Jackson 2.x vs Jackson 3.x 差异

在迁移前，了解 Jackson 2.x 和 3.x 的主要差异：

### API 变化

```java
// ===== Jackson 2.x =====
ObjectMapper mapper = new ObjectMapper();
mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

// ===== Jackson 3.x =====
JsonMapper mapper = JsonMapper.builder()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .defaultPropertyInclusion(JsonInclude.Include.NON_NULL)
    .build();

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .inclusion(Inclusion.NON_NULL)
    .build();
```

### 类名变化

| Jackson 2.x | Jackson 3.x | fastjson3 |
|-------------|-------------|-----------|
| `ObjectMapper` | `JsonMapper` | `ObjectMapper` |
| `ObjectReader` | `ObjectReader` | `ObjectReader` |
| `ObjectWriter` | `ObjectWriter` | `ObjectWriter` |
| `TypeReference` | `TypeReference` | `TypeReference` / `TypeToken` |

### Java 版本要求

| 版本 | Java 要求 |
|------|-----------|
| Jackson 2.x | Java 8+ |
| Jackson 3.x | Java 17+ |
| fastjson3 | Java 21+ |

**结论**：如果你需要升级到 Java 21，直接迁移到 fastjson3 可能比先升级到 Jackson 3.x 更简单。

---

## 配置迁移

### SerializationFeature 映射

| Jackson 2.x | fastjson3 | 说明 |
|-------------|-----------|------|
| `INDENT_OUTPUT` | `WriteFeature.PrettyFormat` | 格式化输出 |
| `WRITE_NULL_MAP_VALUES` | `WriteFeature.WriteNulls` | 输出 null 值 |
| `WRITE_ENUMS_USING_TO_STRING` | `WriteFeature.WriteEnumUsingToString` | 枚举用 toString |
| `WRITE_DATES_AS_TIMESTAMPS` | `@JSONField(format="millis")` | 日期用时间戳 |

### DeserializationFeature 映射

| Jackson 2.x | fastjson3 | 说明 |
|-------------|-----------|------|
| `FAIL_ON_UNKNOWN_PROPERTIES` | `ReadFeature.ErrorOnUnknownProperties` | 未知属性报错 |
| `ACCEPT_SINGLE_VALUE_AS_ARRAY` | `ReadFeature.SupportArrayToBean` | 单值转数组 |
| `ACCEPT_EMPTY_STRING_AS_NULL_OBJECT` | 默认行为 | 空字符串转 null |

### 完整配置示例

```java
// ===== Jackson 2.x =====
ObjectMapper mapper = new ObjectMapper();
mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

// ===== fastjson3 =====
// 命名策略请使用 @JSONType(naming = NamingStrategy.SnakeCase) 注解
// 日期格式请使用 @JSONField(format = "yyyy-MM-dd HH:mm:ss") 注解
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .enableWrite(WriteFeature.WriteNulls)
    .disableRead(ReadFeature.ErrorOnUnknownProperties)
    .enableRead(ReadFeature.SupportArrayToBean)
    .build();
```

---

## 常用场景迁移

### 场景 1：REST API 序列化

```java
// ===== Jackson 2.x =====
@JsonRootName("response")
public class ApiResponse<T> {
    @JsonProperty("code")
    private int code;

    @JsonProperty("msg")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("debug_info")
    private String debugInfo;
}

// ===== fastjson3 =====
public class ApiResponse<T> {
    @JSONField(name = "code", ordinal = 1)
    private int code;

    @JSONField(name = "msg", ordinal = 2)
    private String message;

    @JSONField(name = "data", ordinal = 3)
    private T data;

    @JSONField(name = "debug_info", inclusion = Inclusion.NON_NULL)
    private String debugInfo;
}
```

### 场景 2：多态类型处理

```java
// ===== Jackson 2.x =====
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Dog.class, name = "dog"),
    @JsonSubTypes.Type(value = Cat.class, name = "cat")
})
public abstract class Animal { }

// ===== fastjson3 =====
public abstract class Animal {
    @JSONField(name = "type")
    private String type;

    // 使用自定义反序列化器或 Module
}
```

### 场景 3：视图控制

```java
// ===== Jackson 2.x =====
public class Views {
    public static class Public {}
    public static class Internal extends Public {}
}

public class User {
    @JsonView(Views.Public.class)
    private String name;

    @JsonView(Views.Internal.class)
    private String internalId;
}

// 使用
mapper.setConfig(mapper.getSerializationConfig()
    .withView(Views.Public.class));

// ===== fastjson3：使用 Filter =====
public class User {
    private String name;
    private String internalId;
}

// 使用 PropertyFilter
PropertyFilter publicFilter = (obj, name, value) -> {
    if (obj instanceof User) {
        return !name.equals("internalId");
    }
    return true;
};

ObjectMapper mapper = ObjectMapper.builder()
    .addPropertyFilter(publicFilter)
    .build();
String json = mapper.writeValueAsString(user);
```

### 场景 4：循环引用处理

```java
// ===== Jackson 2.x =====
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
public class Node {
    private Long id;
    private List<Node> children;
}

// ===== fastjson3 =====
public class Node {
    private Long id;
    private List<Node> children;

    @JSONField(serialize = false)
    private Node parent;  // 避免循环
}
```

### 场景 5：自定义序列化器

```java
// ===== Jackson 2.x =====
public class MoneySerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeString(value.setScale(2, RoundingMode.HALF_UP) + "元");
    }
}

public class Order {
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal amount;
}

// ===== fastjson3 =====
public class MoneyWriter implements ObjectWriter<BigDecimal> {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        BigDecimal value = (BigDecimal) object;
        gen.writeString(value.setScale(2, RoundingMode.HALF_UP) + "元");
    }
}

public class Order {
    @JSONField(serializeUsing = MoneyWriter.class)
    private BigDecimal amount;
}
```

### 场景 6：条件属性序列化

```java
// ===== Jackson 2.x =====
public class User {
    private String name;

    @JsonFilter("dynamicFilter")
    private String sensitiveData;
}

// 使用 SimpleBeanPropertyFilter
FilterProvider filters = new SimpleFilterProvider()
    .addFilter("dynamicFilter", SimpleBeanPropertyFilter.filterOutAllExcept("name"));

// ===== fastjson3：使用 PropertyFilter =====
public class User {
    private String name;
    private String sensitiveData;
}

// 运行时决定
PropertyFilter filter = (obj, name, value) -> {
    if (isAdmin()) {
        return true;  // 管理员可见所有字段
    }
    return !name.equals("sensitiveData");  // 其他人排除敏感字段
};

ObjectMapper mapper = ObjectMapper.builder()
    .addPropertyFilter(filter)
    .build();
String json = mapper.writeValueAsString(user);
```

---

## 泛型处理

```java
// ===== Jackson 2.x =====
TypeReference<List<User>> typeRef = new TypeReference<List<User>>() {};
List<User> users = mapper.readValue(json, typeRef);

TypeReference<Map<String, List<User>>> complexTypeRef =
    new TypeReference<Map<String, List<User>>>() {};
Map<String, List<User>> data = mapper.readValue(json, complexTypeRef);

// ===== fastjson3 =====
// 方式1：TypeToken 工厂方法（推荐）
TypeToken<List<User>> userListType = TypeToken.listOf(User.class);
List<User> users = JSON.parse(json, userListType);

// 方式2：TypeReference（保留兼容性）
TypeReference<List<User>> typeRef = new TypeReference<>() {};
List<User> users = mapper.readValue(json, typeRef);
```

---

## 动态对象处理

```java
// ===== Jackson 2.x =====
// ObjectNode
ObjectNode node = mapper.createObjectNode();
node.put("name", "Tom");
node.put("age", 25);
ArrayNode array = node.putArray("tags");
array.add("java");
array.add("json");

// JsonNode
JsonNode root = mapper.readTree(json);
String name = root.get("name").asText();
int age = root.get("age").asInt();

// ===== fastjson3 =====
// JSONObject
JSONObject obj = new JSONObject();
obj.put("name", "Tom");
obj.put("age", 25);
JSONArray array = new JSONArray();
array.add("java");
array.add("json");
obj.put("tags", array);

// 或直接使用
JSONObject root = JSON.parseObject(json);
String name = root.getString("name");
int age = root.getIntValue("age");
```

---

## Spring Boot 集成

### 移除 Jackson 依赖

```xml
<!-- 排除 Jackson -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- 添加 fastjson3 -->
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

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

    @Bean
    public HttpMessageConverters fastjsonHttpMessageConverters(ObjectMapper objectMapper) {
        FastjsonHttpMessageConverter converter = new FastjsonHttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return new HttpMessageConverters(converter);
    }
}
```

---

## 常见问题

### Q: 需要修改所有 Jackson 注解吗？

A: **不需要**。fastjson3 原生支持大部分 Jackson 注解，可以渐进式迁移。

### Q: @JsonView 如何迁移？

A: fastjson3 不支持 JsonView。使用 PropertyFilter 实现类似功能。

### Q: Jackson 2.x 的 Module 如何迁移？

A: fastjson3 使用类似概念的 ObjectReaderModule/ObjectWriterModule。

### Q: 循环引用如何处理？

A: 使用 `@JSONField(serialize = false)` 或 `WriteFeature.ReferenceDetection`

---

## 完整迁移示例

```java
// ===== Jackson 2.x 原代码 =====
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserRequest {
    @JsonProperty("user_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long userId;

    @JsonProperty(required = true)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String birthday;

    @JsonIgnore
    private String password;

    @JsonUnwrapped
    private UserProfile profile;
}

// ===== fastjson3 迁移后（保留 Jackson 注解） =====
// 保留原有注解，fastjson3 会自动识别
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserRequest {
    @JsonProperty("user_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long userId;

    @JsonProperty(required = true)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String birthday;

    @JsonIgnore
    private String password;

    @JsonUnwrapped
    private UserProfile profile;
}

// ===== fastjson3 完全迁移（可选） =====
@JSONType(naming = NamingStrategy.SnakeCase)
public class UserRequest {
    @JSONField(name = "user_id", inclusion = Inclusion.NON_NULL)
    private Long userId;

    @JSONField(name = "birthday", format = "yyyy-MM-dd", required = true)
    private String birthday;

    @JSONField(serialize = false)
    private String password;

    // @JsonUnwrapped 不支持，需使用自定义 ObjectWriter 实现
    private UserProfile profile;
}
```

---

## 迁移检查清单

使用此清单确保迁移完整：

### 代码修改

- [ ] 替换 `new ObjectMapper()` 为 `ObjectMapper.shared()` 或 `ObjectMapper.builder()`
- [ ] 将 `mapper.configure()` 改为 builder 模式配置
- [ ] 替换 `SerializationFeature` 为 `WriteFeature`
- [ ] 替换 `DeserializationFeature` 为 `ReadFeature`
- [ ] 评估是否需要修改泛型处理（`TypeReference` → `TypeToken`）

### 测试验证

- [ ] 序列化测试通过
- [ ] 反序列化测试通过
- [ ] 泛型类型处理正确
- [ ] 日期格式正确
- [ ] null 值处理符合预期

### 可选优化

- [ ] 考虑将 Jackson 注解替换为 fastjson3 注解（非必须）
- [ ] 配置 ObjectMapper 单例
- [ ] 优化性能关键路径

---

## 快速参考

### 最小改动迁移（保留 Jackson 注解）

```java
// 原代码
ObjectMapper mapper = new ObjectMapper();
mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

// 最小改动
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
// 实体类注解保持不变！
```

### 完全迁移（替换为 fastjson3 注解）

```java
// 原代码
@JsonProperty("user_name")
@JsonFormat(pattern = "yyyy-MM-dd")
private String userName;

// 完全替换
@JSONField(name = "user_name", format = "yyyy-MM-dd")
private String userName;
```

## 相关文档

- [注解使用指南](../guides/annotations.md)
- [从 Jackson 3.x 迁移](from-jackson3.md)
- [迁移检查清单](checklist.md)

# 最佳实践与避坑指南

本文档总结了使用 fastjson3 的最佳实践和常见错误。

## ✅ 推荐做法

### 1. 复用 ObjectMapper

```java
// ✅ 好：全局复用
private static final ObjectMapper MAPPER = ObjectMapper.shared();

// ✅ 好：自定义配置后复用
private static final ObjectMapper MAPPER = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();

// ❌ 不好：每次创建
public String toJSON(Object obj) {
    return new ObjectMapper().writeValueAsString(obj);
}
```

### 2. 优先使用 byte[]

```java
// ✅ 好：UTF-8 数据用 byte[]
byte[] json = mapper.writeValueAsBytes(obj);
User user = mapper.readValue(jsonBytes, User.class);

// ⚠️ 可接受：需要 String 时
String json = mapper.writeValueAsString(obj);
```

### 3. 处理日期使用注解

```java
// ✅ 好：明确指定格式
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;

// ✅ 好：使用 Java Time API
@JSONField(format = "yyyy-MM-dd")
private LocalDate date;

// ⚠️ 可能有问题：依赖默认格式
private Date createTime;
```

### 4. 使用 TypeToken 处理泛型

```java
// ✅ 好
TypeToken<List<User>> type = new TypeToken<List<User>>() {};
List<User> users = mapper.readValue(json, type);

// ❌ 不好：丢失类型信息
List<User> users = (List<User>) mapper.readValue(json, List.class);
```

### 5. 生产环境安全配置

```java
// ✅ 好：禁用 AutoType（除非确实需要）
ObjectMapper mapper = ObjectMapper.builder()
    .disableRead(ReadFeature.SupportAutoType)
    .build();

// ✅ 好：严格模式
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.ErrorOnUnknownProperties)
    .build();
```

### 6. 使用预编译 JSONPath

```java
// ✅ 好：预编译
private static final JSONPath AUTHOR_PATH =
    JSONPath.compile("$.store.book[*].author");

// ❌ 不好：每次编译
JSONPath path = JSONPath.of("$.store.book[*].author");
```

## ❌ 常见错误

### 1. 不处理 null 值

```java
// ❌ 问题：boolean 默认为 false
@JSONField(deserialize = false)
private boolean vip;  // 总是 false

// ✅ 解决：使用 Boolean 包装类
@JSONField(deserialize = false)
private Boolean vip;  // 可以是 null
```

### 2. 循环引用导致 StackOverflow

```java
// ❌ 问题：循环引用
class Department {
    List<Employee> employees;
}
class Employee {
    Department department;  // 循环
}

// ✅ 解决方案1：忽略字段
@JSONField(serialize = false)
private Department department;

// ✅ 解决方案2：使用引用检测
ObjectMapper.builder()
    .enableWrite(WriteFeature.WriteClassName)
    .build();
```

### 3. 字段名不匹配

```java
// ❌ 问题：JSON 是 user_name，Java 是 userName
// 解析后 userName 为 null

// ✅ 解决方案1：智能匹配（默认启用）
.enableRead(ReadFeature.SupportSmartMatch)

// ✅ 解决方案2：注解指定
@JSONField(name = "user_name")
private String userName;
```

### 4. 泛型擦除

```java
// ❌ 问题：泛型被擦除
List<User> users = mapper.readValue(json, List.class);
// 实际是 List<Object>

// ✅ 解决：使用 TypeToken
TypeToken<List<User>> type = new TypeToken<List<User>>() {};
List<User> users = mapper.readValue(json, type);
```

### 5. 无参构造缺失

```java
// ❌ 问题：没有无参构造
class User {
    public User(String name) { this.name = name; }
}
// 反序列化失败

// ✅ 解决方案1：添加无参构造
class User {
    public User() {}
    public User(String name) { this.name = name; }
}

// ✅ 解决方案2：使用 @JSONCreator
@JSONCreator
public User(@JSONField(name = "name") String name) {
    this.name = name;
}
```

### 6. 线程不正确的使用

```java
// ❌ 问题：JSONObject 不是线程安全的
JSONObject shared = new JSONObject();  // 多线程共享
// 多个线程同时修改 -> 数据损坏

// ✅ 解决：每次创建新对象，或使用同步
private static final ObjectMapper MAPPER = ObjectMapper.shared();
// ObjectMapper 是线程安全的
```

## 性能相关

### 1. 大对象处理

```java
// ❌ 不好：一次性读入大文件
String json = Files.readString(Path.of("large.json"));
List<User> users = mapper.readValue(json, ...);

// ✅ 好：流式处理
try (InputStream is = Files.newInputStream(Path.of("large.json"))) {
    List<User> users = mapper.readValue(is, ...);
}
```

### 2. 频繁小对象序列化

```java
// ❌ 不好：每次都创建新 mapper
public String log(Object obj) {
    return new ObjectMapper().writeValueAsString(obj);
}

// ✅ 好：复用 mapper
private static final ObjectMapper LOG_MAPPER = ObjectMapper.shared();
public String log(Object obj) {
    return LOG_MAPPER.writeValueAsString(obj);
}
```

### 3. 启用不必要的特性

```java
// ❌ 不好：生产环境启用调试特性
.enableWrite(WriteFeature.PrettyFormat)  // 增加输出大小
.enableRead(ReadFeature.AllowComments)  // 安全风险

// ✅ 好：生产环境使用优化配置
ObjectMapper.builder()
    .readerCreator(ObjectReaderCreatorASM::createObjectReader)
    .enableWrite(WriteFeature.OptimizedForAscii)
    .build();
```

## 安全相关

### 1. AutoType 安全风险

```java
// ❌ 危险：允许任意类型
.enableRead(ReadFeature.SupportAutoType)

// ✅ 安全：禁用或使用白名单
.disableRead(ReadFeature.SupportAutoType)

// 或使用白名单
.autoTypeFilter(AutoTypeFilter.whitelist()
    .addClass("com.example.model.")
    .build())
```

### 2. 输入验证

```java
// ❌ 危险：不验证输入
public void process(String json) {
    Object obj = JSON.parse(json);
}

// ✅ 安全：先验证
public void process(String json) {
    if (!JSON.isValid(json)) {
        throw new IllegalArgumentException("Invalid JSON");
    }
    // 或使用 JSON Schema
    ValidateResult result = schema.validate(json);
    if (!result.isSuccess()) {
        throw new IllegalArgumentException(result.getMessage());
    }
}
```

## Android 特定

### 1. 使用 Android 专用 JAR

```gradle
// ✅ 好：使用 android 版本
implementation 'com.alibaba:fastjson3:3.0.0:android@aar'

// ⚠️ 普通 JAR 包含 ASM，Android 不支持
```

### 2. 避免反射

```java
// ✅ 好：使用注解配置
@JSONType(ignores = {"field1", "field2"})
public class Data {
    // ...
}

// ⚠️ 避免：运行时反射配置（在 Android 上慢）
```

## Spring Boot 集成

### 1. 正确配置 MessageConverter

```java
// ✅ 好：添加到最前面
@Override
public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    Fastjson3HttpMessageConverter converter = new Fastjson3HttpMessageConverter();
    converter.setObjectMapper(objectMapper);
    converters.add(0, converter);  // 优先级最高
}
```

### 2. 异常处理

```java
// ✅ 好：统一异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JSONException.class)
    public ResponseEntity<?> handleJsonException(JSONException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("JSON_ERROR", e.getMessage()));
    }
}
```

## 检查清单

部署前检查：

- [ ] 复用 ObjectMapper（非每次创建）
- [ ] 生产环境禁用 SupportAutoType
- [ ] 日期字段使用注解指定格式
- [ ] 泛型使用 TypeToken
- [ ] 处理可能的 null 值
- [ ] 大文件使用流式处理
- [ ] JSONPath 已预编译
- [ ] 不需要的特性已禁用
- [ ] 异常有适当处理
- [ ] （如适用）Android 使用专用 JAR

## 相关文档

- ❓ [常见问题 →](faq.md)
- 📖 [性能调优 →](guides/performance.md)
- 🔧 [安全配置 →](guides/security.md)

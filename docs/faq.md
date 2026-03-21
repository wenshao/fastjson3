# 常见问题解答 (FAQ)

## 基础问题

### Q: fastjson3 和 fastjson2 有什么区别？

**A:** 主要区别：

| 特性 | fastjson2 | fastjson3 |
|------|-----------|-----------|
| API 风格 | fastjson1 风格 | Jackson 3 风格 |
| JDK 要求 | JDK 8+ | JDK 21+ |
| 可变性 | 可变 | 不可变（ObjectMapper） |
| 构建 | 人工开发 | AI 编写 |

### Q: 为什么需要 JDK 21+？

**A:** fastjson3 使用了现代 Java 特性：
- `sealed` classes - 性能优化
- `switch` expressions - 更简洁的代码
- `pattern matching` - 类型安全
- `record` classes - 不可变数据

### Q: 是否兼容 fastjson2/fasterxml.jackson？

**A:** API 不完全兼容，但：
- 支持 Jackson 注解（`@JsonProperty` 等）
- 提供 [迁移指南](migration.md)

---

## 解析问题

### Q: 字段名不匹配怎么办？

**A:** 三种解决方案：

```java
// 方案1：智能匹配（默认）
ObjectMapper.builder().enableRead(ReadFeature.SupportSmartMatch)

// 方案2：注解指定
@JSONField(name = "user_name")
private String userName;

// 方案3：替代名称
@JSONField(alternateNames = {"email", "emailAddress"})
private String mail;
```

### Q: 日期解析失败？

**A:** 指定格式：

```java
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;

// 或使用 Java Time API
@JSONField(format = "yyyy-MM-dd")
private LocalDate date;
```

### Q: 泛型怎么解析？

**A:** 使用 `TypeToken`：

```java
TypeToken<List<User>> type = new TypeToken<List<User>>() {};
List<User> users = mapper.readValue(json, type);
```

### Q: JSON 字段在 Java 中不存在会报错吗？

**A:** 不会，默认忽略。要严格模式：

```java
.enableRead(ReadFeature.ErrorOnUnknownProperties)
```

---

## 序列化问题

### Q: null 值不输出？

**A:** 默认行为，要输出：

```java
JSON.toJSONString(obj, WriteFeature.WriteNulls)
```

### Q: 如何忽略字段？

**A:** 三种方式：

```java
// 方式1
@JSONField(serialize = false)
private String password;

// 方式2
@JSONType(ignores = {"password", "salt"})
public class User { }

// 方式3
private transient String tempData;  // transient
```

### Q: 枚举输出为数字？

**A:** 默认输出名称，要输出序数：

```java
JSON.toJSONString(obj, WriteFeature.WriteEnumUsingOrdinal)
```

### Q: 如何美化输出？

**A:**

```java
JSON.toJSONString(obj, WriteFeature.PrettyFormat)
```

---

## 性能问题

### Q: 性能不如预期？

**A:** 检查：

1. 是否复用 ObjectMapper
2. 是否使用 byte[] 而非 String
3. 是否使用默认反射路径（比 ASM 快 10-13%）
4. 是否启用了不必要的特性

### Q: 如何提升性能？

**A:** [性能调优指南](guides/performance.md)

关键点：
- 复用 `ObjectMapper`
- 使用 `toJSONBytes()`
- 保持默认反射路径（JIT 内联后比 ASM 快 10-13%）
- 禁用不需要的特性

---

## 安全问题

### Q: AutoType 安全吗？

**A:** 默认禁用，启用时需要注意：

```java
// ❌ 不安全：允许任意类型
.enableRead(ReadFeature.SupportAutoType)

// ✅ 安全：使用白名单
.autoTypeFilter(AutoTypeFilter.whitelist()
    .addClass("com.example.model.")
    .build())
```

### Q: 如何防御恶意 JSON？

**A:**

1. 禁用 `SupportAutoType`
2. 使用 JSON Schema 验证
3. 限制输入大小
4. 设置超时

---

## 错误处理

### Q: 如何获取详细错误信息？

**A:**

```java
try {
    mapper.readValue(json, User.class);
} catch (JSONException e) {
    System.err.println("错误：" + e.getMessage());
    System.err.println("位置：" + e.getErrorOffset());
}
```

### Q: JSONException 有哪些子类型？

**A:** 目前是统一的 `JSONException`，包含：
- 错误消息
- 错误位置
- 原始异常

---

## Spring Boot 集成

### Q: 如何集成到 Spring Boot？

**A:** [Spring Boot 集成指南](guides/spring-boot.md)

```java
@Configuration
public class Config {
    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapper.builder()
            .enableRead(ReadFeature.SupportSmartMatch)
            .build();
    }
}
```

### Q: 如何配置 HTTP 消息转换？

**A:**

```java
@Override
public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    Fastjson3HttpMessageConverter converter = new Fastjson3HttpMessageConverter();
    converter.setObjectMapper(objectMapper);
    converters.add(0, converter);
}
```

---

## Android

### Q: Android 支持？

**A:** 支持 Android 8+ (API 26+)

使用专用 JAR：

```gradle
implementation 'com.alibaba:fastjson3:3.0.0:android@aar'
```

### Q: Android 上性能如何？

**A:** ASM 不可用，但反射模式已优化。

---

## GraalVM

### Q: 支持 GraalVM Native Image？

**A:** 支持，需要配置反射：

```json
// reflect-config.json
{
  "reflect": [
    {
      "name": "com.example.User",
      "allDeclaredFields": true
    }
  ]
}
```

详见 [GraalVM 配置](advanced/graalvm.md)

---

## 其他

### Q: 支持 Jackson 注解？

**A:** 支持，包括：
- `@JsonProperty`
- `@JsonFormat`
- `@JsonIgnore`
- 等等...

### Q: 如何自定义序列化？

**A:** [自定义序列化器](advanced/custom-serializer.md)

```java
mapper.registerWriter(Money.class, new MoneyWriter());
```

### Q: 有线程安全问题吗？

**A:**
- `ObjectMapper` - 线程安全
- `JSON` 静态方法 - 线程安全
- `JSONObject`/`JSONArray` - **不**线程安全

### Q: 如何报告问题？

**A:** [GitHub Issues](https://github.com/wenshao/fastjson3/issues)

请包含：
- fastjson3 版本
- JDK 版本
- 复现代码
- 错误堆栈

---

## 未找到答案？

- 📖 查看 [完整文档](README.md)
- 📖 查看 [最佳实践](best-practices.md)
- 💻 查看 [示例代码](samples/)

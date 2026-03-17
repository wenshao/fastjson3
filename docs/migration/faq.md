# 迁移常见问题

## 通用问题

### Q: fastjson3 完全兼容 fastjson2 吗？

A: 基础 API (JSON.parseObject, JSON.toJSONString 等) 完全兼容。新增了 ObjectMapper 风格的 API，性能也有提升。

### Q: 需要修改现有代码吗？

A: 如果使用基础 API，通常不需要。使用 Jackson 注解的代码也不需要修改（fastjson3 原生支持大部分 Jackson 注解）。

### Q: 性能如何？

A: fastjson3 继承了 fastjson2 的高性能引擎，在某些场景下性能更好。建议进行性能测试验证。

### Q: 支持 Java 版本？

A: fastjson3 需要 Java 21+。

### Q: 如何处理日期？

A: 使用 `@JSONField(format = "...")` 注解或在 ObjectMapper 中配置日期格式。

### Q: 支持 Kotlin 吗？

A: 支持，fastjson3 可以处理 Kotlin 数据类。

### Q: 支持 GraalVM Native Image 吗？

A: 支持，fastjson3 有专门的 GraalVM 配置。

## fastjson2 迁移

### Q: TypeToken 怎么变了？

A: fastjson3 的 TypeToken 使用工厂方法：
```java
// fastjson2
new TypeToken<List<User>>() {}

// fastjson3
TypeToken.listOf(User.class)
```

### Q: 旧的 JSON 方法还能用吗？

A: 可以，基础 API 保持兼容。

## Jackson 迁移

### Q: 需要修改 Jackson 注解吗？

A: 不需要。fastjson3 原生支持大部分 Jackson 注解，包括：
- `@JsonProperty`
- `@JsonFormat`
- `@JsonIgnore`
- `@JsonUnwrapped`
- `@JsonAlias`

### Q: ObjectMapper 是线程安全的吗？

A: fastjson3 的 ObjectMapper 是不可变的，线程安全。建议使用 `ObjectMapper.shared()` 或复用实例。

### Q: JsonNode 怎么替换？

A: 使用 `JSONObject`、`JSONArray` 或 `JSONPath`。

### Q: 自定义序列化器如何迁移？

A: fastjson3 使用 `ObjectWriter`/`ObjectReader` 接口，概念类似。

## Gson 迁移

### Q: Gson 的 TypeToken 怎么办？

A: fastjson3 也有 TypeToken，但使用工厂方法：
```java
TypeToken<List<User>> type = TypeToken.listOf(User.class);
```

### Q: GsonBuilder 配置如何迁移？

A: 使用 ObjectMapper.builder()：
```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .dateFormat("yyyy-MM-dd")
    .build();
```

### Q: JsonParser 怎么替换？

A: 使用 `JSON.parseObject()` 或 `JSON.parseArray()`。

### Q: excludeFieldsWithModifiers 怎么办？

A: 使用 `@JSONField(serialize = false)` 或 `@JSONType(ignores = {...})`。

## org.json 迁移

### Q: length() 方法不见了？

A: 使用 `size()` 方法代替。

### Q: has() 方法变成什么了？

A: 使用 `containsKey()` 方法。

### Q: toString() 输出 JSON？

A: 使用 `toJSONString()` 方法。

### Q: JSONPointer 如何替换？

A: 使用 `JSONPath`，功能更强大且符合 RFC 9535 标准。

## Spring Boot 集成

### Q: 如何替换 Jackson？

A: 添加 fastjson3 依赖，配置 ObjectMapper Bean：

```java
@Configuration
public class Fastjson3Config {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return ObjectMapper.builder()
            .enableRead(ReadFeature.SupportSmartMatch)
            .build();
    }
}
```

### Q: 需要排除 Jackson 依赖吗？

A: 不需要，fastjson3 和 Jackson 可以共存。

### Q: @JsonAutoDetect 还有用吗？

A: fastjson3 默认支持所有字段访问，不需要额外配置。

## 性能优化

### Q: 如何提升序列化性能？

A: 几个优化建议：
1. 复用 ObjectMapper 实例
2. 使用 byte[] 处理 UTF-8 数据
3. 预编译 JSONPath
4. 启用 ASM（JVM 环境）

### Q: ObjectMapper 每次创建有性能问题吗？

A: 是的。建议使用 `ObjectMapper.shared()` 或复用实例。

### Q: TypeToken 需要缓存吗？

A: 是的。建议将常用的 TypeToken 缓存为静态常量。

## 错误处理

### Q: 如何处理解析错误？

A: 使用 try-catch 捕获异常：
```java
try {
    User user = JSON.parseObject(json, User.class);
} catch (JSONException e) {
    // 处理错误
}
```

### Q: 未知字段报错怎么办？

A: 配置：
```java
ObjectMapper mapper = ObjectMapper.builder()
    .disableRead(ReadFeature.ErrorOnUnknownProperties)
    .build();
```

### Q: null 值不序列化？

A: 配置：
```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.WriteNulls)
    .build();
```

## 其他

### Q: 如何调试序列化问题？

A: 启用美化输出查看结构：
```java
String json = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
```

### Q: 支持哪些注解？

A: 查看 [注解参考](../api/annotations.md)。

### Q: 有在线文档吗？

A: 查看项目文档目录。

### Q: 如何报告问题？

A: 在 GitHub 上提交 Issue，附上：
- fastjson3 版本
- 复现代码
- 错误日志
- 预期行为

---

更多问题请参考 [API 文档](../api/) 或提交 Issue。

# ObjectMapper 类参考

`ObjectMapper` 是 fastjson3 的核心类，提供线程安全、不可变的 JSON 映射功能。

## 创建实例

### 使用共享实例

```java
// 使用预配置的共享实例（推荐）
ObjectMapper mapper = ObjectMapper.shared();
```

### 使用 Builder

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.AllowComments)
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
```

### 从现有 mapper 派生

```java
// 创建新配置，不影响原 mapper
ObjectMapper prettyMapper = mapper.rebuild()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
```

## 读取方法

### readValue

```java
// 从字符串读取
public <T> T readValue(String content, Class<T> type)

// 从字节数组读取
public <T> T readValue(byte[] content, Class<T> type)

// 从 InputStream 读取
public <T> T readValue(InputStream src, Class<T> type)

// 从 Reader 读取
public <T> T readValue(Reader src, Class<T> type)

// 从 File 读取
public <T> T readValue(File src, Class<T> type)

// 读取泛型类型
public <T> T readValue(String content, TypeReference<T> typeRef)
public <T> T readValue(InputStream src, TypeReference<T> typeRef)
```

**示例：**

```java
// 基本读取
User user = mapper.readValue(jsonStr, User.class);

// 读取列表
TypeToken<List<User>> type = new TypeToken<List<User>>() {};
List<User> users = mapper.readValue(jsonStr, type);

// 从文件读取
User user = mapper.readValue(new File("user.json"), User.class);
```

### readValue 多态

```java
// 读取为 JSONObject
public JSONObject readObject(String content)

// 读取为 JSONArray
public JSONArray readArray(String content)

// 读取为任意类型
public Object readAny(String content)
```

## 写入方法

### writeValue

```java
// 写入为字符串
public String writeValueAsString(Object obj)

// 写入为字节数组
public byte[] writeValueAsBytes(Object obj)

// 写入到 OutputStream
public void writeValue(OutputStream out, Object obj)

// 写入到 Writer
public void writeValue(Writer out, Object obj)

// 写入到 File
public void writeValue(File resultFile, Object obj)
```

**示例：**

```java
// 写入为字符串
String json = mapper.writeValueAsString(user);

// 写入为字节
byte[] bytes = mapper.writeValueAsBytes(user);

// 写入到文件
mapper.writeValue(new File("output.json"), user);

// 写入到输出流
mapper.writeValue(response.getOutputStream(), data);
```

## 配置方法

### Builder 配置

```java
public static Builder builder() {
    return new Builder();
}

public Builder rebuild() {
    return new Builder(this);
}
```

### Builder 方法

```java
// 读取特性
public Builder enableRead(ReadFeature... features)
public Builder disableRead(ReadFeature... features)
public Builder readFeatures(ReadFeature[] features)

// 写入特性
public Builder enableWrite(WriteFeature... features)
public Builder disableWrite(WriteFeature... features)
public Builder writeFeatures(WriteFeature[] features)

// 自定义 Creator
public Builder readerCreator(Function<Class<?>, ObjectReader<?>> creator)
public Builder writerCreator(Function<Class<?>, ObjectWriter<?>> creator)

// Mixin
public Builder addMixin(Class<?> target, Class<?> mixin)

// 模块
public Builder registerModule(ObjectReaderModule module)
public Builder registerModule(ObjectWriterModule module)
```

## 自定义扩展

### 注册 Reader/Writer

```java
// 注册 ObjectReader
public ObjectMapper registerReader(Class<?> type, ObjectReader<?> reader)

// 注册 ObjectWriter
public ObjectMapper registerWriter(Class<?> type, ObjectWriter<?> writer)

// Lambda 式注册
public ObjectMapper registerReader(Class<?> type,
    Function<JSONParser, Object> reader)

public ObjectMapper registerWriter(Class<?> type,
    BiConsumer<JSONGenerator, Object> writer)
```

**示例：**

```java
// 注册 Money 类型
mapper.registerReader(Money.class, (parser, type, features) -> {
    return Money.parse(parser.readString());
});

mapper.registerWriter(Money.class, (gen, obj, features) -> {
    gen.writeString(obj.toString());
});
```

### Mixin 配置

```java
// 添加 Mixin
public ObjectMapper addMixin(Class<?> target, Class<?> mixin)
```

**示例：**

```java
// 为第三方类添加配置
public abstract class ThirdPartyMixin {
    @JSONField(name = "id")
    abstract long getId();

    @JSONField(serialize = false)
    abstract String getInternalState();
}

mapper.addMixin(ThirdPartyClass.class, ThirdPartyMixin.class);
```

## 特性配置

### 常用配置组合

```java
// 宽松解析
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(
        ReadFeature.AllowComments,
        ReadFeature.AllowSingleQuotes,
        ReadFeature.AllowUnquotedFieldNames,
        ReadFeature.SupportSmartMatch
    )
    .build();

// 严格解析
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.ErrorOnUnknownProperties)
    .build();

// 美化输出
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();

// 浏览器兼容
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.BrowserCompatible)
    .build();
```

## 性能优化

### 默认配置（推荐）

默认配置下：Reader 使用 REFLECT（反射路径，JIT 深度内联后最快）；Writer 使用 AUTO（会在满足条件时尝试 ASM，否则回退反射）。大部分场景下无需额外配置。

```java
// 默认配置，直接使用
ObjectMapper mapper = ObjectMapper.shared();

// 如需强制 Writer 也使用反射路径（避免 ASM 尝试开销）：
ObjectMapper reflectOnly = ObjectMapper.builder()
    .writerProvider(new com.alibaba.fastjson3.writer.ReflectObjectWriterProvider())
    .build();
```

> 实测中反射路径经 JIT 深度内联后比 ASM 快 10-13%。AUTO 模式下 ASM 生成失败会自动回退反射，不影响正确性。

### ASCII 优化

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.OptimizedForAscii)
    .build();
```

## 线程安全

`ObjectMapper` 是**线程安全**的：

- 所有配置字段都是 `final`
- 构建后不可修改
- 可以安全地在多线程环境中共享

```java
// ✅ 好：全局共享
private static final ObjectMapper MAPPER = ObjectMapper.shared();

// ✅ 好：单例模式
private static final ObjectMapper MAPPER = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
```

## 最佳实践

1. **复用实例** - 不要每次创建新的 ObjectMapper

```java
// ❌ 不好
public String toJSON(Object obj) {
    return new ObjectMapper().writeValueAsString(obj);
}

// ✅ 好
private static final ObjectMapper MAPPER = ObjectMapper.shared();
public String toJSON(Object obj) {
    return MAPPER.writeValueAsString(obj);
}
```

2. **合理配置特性** - 只启用需要的特性

3. **使用 byte[]** - 处理 UTF-8 数据时优先使用字节数组

## 相关文档

- [📖 JSON 类参考 →](JSON.md)
- [📖 特性枚举 →](features.md)
- [📖 性能调优 →](../guides/performance.md)

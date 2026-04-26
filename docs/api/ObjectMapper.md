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

// 读取泛型类型
public <T> T readValue(String content, TypeReference<T> typeRef)
public <T> T readValue(InputStream src, TypeReference<T> typeRef)
```

**示例：**

```java
// 基本读取
User user = mapper.readValue(jsonStr, User.class);

// 读取列表
List<User> users = mapper.readValue(jsonStr, new TypeReference<List<User>>() {});
```

### readValue 多态

```java
// 读取为 JSONObject
public JSONObject readObject(String content)

// 读取为 JSONArray
public JSONArray readArray(String content)
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
```

**示例：**

```java
// 写入为字符串
String json = mapper.writeValueAsString(user);

// 写入为字节
byte[] bytes = mapper.writeValueAsBytes(user);

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

// 写入特性
public Builder enableWrite(WriteFeature... features)
public Builder disableWrite(WriteFeature... features)

// 自定义 Creator
public Builder readerCreator(Function<Class<?>, ObjectReader<?>> creator)
public Builder writerCreator(Function<Class<?>, ObjectWriter<?>> creator)

// 自定义 Map / List 后备存储 (per-mapper)
public Builder mapSupplier(Supplier<? extends Map<String, Object>> supplier)
public Builder listSupplier(Supplier<? extends List<Object>> supplier)

// Mixin
public Builder addMixIn(Class<?> target, Class<?> mixIn)

// 模块
public Builder addReaderModule(ObjectReaderModule module)
public Builder addWriterModule(ObjectWriterModule module)
```

### 自定义 Map / List 后备存储

`mapSupplier` / `listSupplier` 让 mapper 在解析未类型化 JSON 时（`{...}` / `[...]`）使用调用者指定的 `Map` / `List` 实现作为 `JSONObject` / `JSONArray` 的内部存储。等价于 fastjson2 的 `JSONReader.Context.setObjectSupplier` / `setArraySupplier`，但是 per-mapper 而非 per-context。

```java
// 用 ConcurrentHashMap 作为 JSONObject 内部存储
ObjectMapper mapper = ObjectMapper.builder()
        .mapSupplier(java.util.concurrent.ConcurrentHashMap::new)
        .listSupplier(java.util.LinkedList::new)
        .build();

JSONObject obj = (JSONObject) mapper.readValue("{\"a\":1,\"b\":[2,3]}");
// obj 仍然是 JSONObject 类型；其内部 innerMap 是 ConcurrentHashMap
// obj.get("b") 是 JSONArray，innerList 是 LinkedList
```

**适用场景：**
- 多线程读 JSONObject：`ConcurrentHashMap`
- 需要键排序：`TreeMap`
- 频繁头部插入的 List：`LinkedList`

**生效范围**：
- 顶层未类型化解析 `mapper.readValue(json)` 推断为 `JSONObject` / `JSONArray`。
- 直接入口 `mapper.readObject(json)` / `mapper.readObject(byte[])` / `mapper.readArray(json)` / `mapper.readArray(byte[])`。
- 上述节点内部递归创建的所有子节点（嵌套 `JSONObject` / `JSONArray`）。

**不生效**：
- 类型化解析 `mapper.readValue(json, Type)`（含 `JSONObject.class` / `MyBean.class` / `TypeReference<Map<String,Object>>` / `TypeReference<List<Object>>`）走 ObjectReader 路径，内部使用各自的 `Map` / `List` 实现，不会应用 supplier。
- 全局静态 `JSON.parse(...)` / `JSON.parseObject(...)` 走 shared mapper，不应用 per-mapper supplier；如需全局影响 `new JSONObject()` 与默认未类型化解析路径，用 `JSONObject.setMapCreator(...)`（`JSONArray` 当前无对应全局 setter）。

## 自定义扩展

### 注册 Reader/Writer

```java
// 注册 ObjectReader
public <T> void registerReader(Type type, ObjectReader<T> reader)

// 注册 ObjectWriter
public <T> void registerWriter(Type type, ObjectWriter<T> writer)
```

### Mixin 配置（仅 Builder）

Mixin 只能通过 Builder 配置，ObjectMapper 实例上没有 `addMixIn` 方法。

**示例：**

```java
// 为第三方类添加配置
public abstract class ThirdPartyMixin {
    @JSONField(name = "id")
    abstract long getId();

    @JSONField(serialize = false)
    abstract String getInternalState();
}

ObjectMapper mapper = ObjectMapper.builder()
    .addMixIn(ThirdPartyClass.class, ThirdPartyMixin.class)
    .build();
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

JVM 环境下，Reader 和 Writer 都默认使用 AUTO provider — 简单 POJO 自动走 ASM 字节码生成路径，复杂类型（Record、Sealed Class、`@JSONType(schema=...)`）自动 fallback 到反射。Android / Native Image 平台上 ASM 不可用，AUTO 会选择反射。大部分场景下无需额外配置。

```java
// 默认配置已是最优，直接使用
ObjectMapper mapper = ObjectMapper.shared();

// 需要强制 REFLECT 路径（测试 / 对照用途）：
ObjectMapper reflectOnly = ObjectMapper.builder()
    .writerProvider(new com.alibaba.fastjson3.writer.ReflectObjectWriterProvider())
    .readerProvider(new com.alibaba.fastjson3.reader.ReflectObjectReaderProvider())
    .build();
```

> Path B（PR #72–#81）完成后，ASM 路径在 x86_64 和 aarch64 上 Parse 和 Write 全面超过 fastjson2 2.0.61（109–119% 区间）。详见 [`docs/benchmark/benchmark_3.0.0-SNAPSHOT-66a5e2a.md`](../benchmark/benchmark_3.0.0-SNAPSHOT-66a5e2a.md)。AUTO 模式下 ASM 生成失败会自动回退反射，不影响正确性。

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

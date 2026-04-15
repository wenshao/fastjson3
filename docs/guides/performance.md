# 性能调优指南

fastjson3 已经做了很多性能优化，但正确的使用方式可以进一步提升性能。

## 核心优化建议

### 1. 复用 ObjectMapper

**最重要！** ObjectMapper 是线程安全的，应该复用。

```java
// ❌ 不好：每次创建新实例
public String toJSON(Object obj) {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(obj);
}

// ✅ 好：使用共享实例
private static final ObjectMapper MAPPER = ObjectMapper.shared();

public String toJSON(Object obj) {
    return MAPPER.writeValueAsString(obj);
}

// ✅ 最好：自定义配置并复用
private static final ObjectMapper MAPPER = ObjectMapper.builder()
    .enableWrite(WriteFeature.OptimizedForAscii)
    .build();
```

### 2. 使用字节数组而非字符串

处理 UTF-8 数据时，使用 `byte[]` 比使用 `String` 更快。

```java
// ❌ 慢
String json = mapper.writeValueAsString(obj);
User user = mapper.readValue(json, User.class);

// ✅ 快
byte[] json = mapper.writeValueAsBytes(obj);
User user = mapper.readValue(json, User.class);
```

**原因：** UTF-8 字节可以直接操作，无需字符转换。

### 3. 默认配置已是最优

fastjson3 默认配置已经是最快的。JVM 环境下 Reader 和 Writer 都通过 `AutoObjectReaderProvider` / `AutoObjectWriterProvider` 自动为简单 POJO 选择 ASM 代码生成路径，无需手动配置。

```java
// ✅ 默认配置已经是最快的
ObjectMapper mapper = ObjectMapper.shared();
```

**原因：** 完成 Path B（PR #72–#81）后，ASM 生成的 Reader/Writer 在 x86_64 和 aarch64 上都显著超过 fastjson2：
- Parse: ASM **115–119%** of fj2（aarch64/x86_64）
- Write: ASM **110–144%** of fj2

ASM 路径通过**按字段展开 + 按长度特化**（`writeName1L/2L`、`readStringValueFast`）配合 Unsafe 字段访问，让 JIT 能深度内联到调用方。详见 [`docs/benchmark/`](../benchmark/) 下的最新报告。

> **何时手动指定路径？** 只在需要强制统一行为的测试环境下才建议手动指定：
> ```java
> ObjectMapper asmOnly = ObjectMapper.builder()
>     .writerCreatorType(WriterCreatorType.ASM)
>     .readerCreatorType(ReaderCreatorType.ASM)
>     .build();
> ```
> Native-image / Android 环境会自动退回反射路径；用户代码无需关心。

### 4. 使用预编译的 JSONPath

```java
// ❌ 不好：每次编译
public List<String> getAuthors(String json) {
    JSONPath path = JSONPath.of("$.store.book[*].author");
    return path.extract(json, List.class);
}

// ✅ 好：预编译
private static final JSONPath AUTHOR_PATH =
    JSONPath.of("$.store.book[*].author");

public List<String> getAuthors(String json) {
    return AUTHOR_PATH.extract(json, List.class);
}
```

### 5. 缓存 TypeToken

```java
// ❌ 不好
public List<User> parseUsers(String json) {
    TypeToken<List<User>> type = new TypeToken<List<User>>() {};
    return JSON.parseObject(json, type);
}

// ✅ 好
private static final TypeToken<List<User>> USERS_TYPE =
    new TypeToken<List<User>>() {};

public List<User> parseUsers(String json) {
    return JSON.parseObject(json, USERS_TYPE);
}
```

## 特性配置优化

### 针对优化场景选择特性

```java
// ASCII 内容优化（纯英文、数字）
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.OptimizedForAscii)
    .build();

// 浏览器兼容（会降低性能）
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.BrowserCompatible)
    .build();

// 写入 null 会增加输出大小
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.WriteNulls)
    .build();
```

### 禁用不需要的特性

```java
// 不支持多态类型时禁用
ObjectMapper mapper = ObjectMapper.builder()
    .disableRead(ReadFeature.SupportAutoType)
    .build();

// 不需要智能匹配时禁用
ObjectMapper mapper = ObjectMapper.builder()
    .disableRead(ReadFeature.SupportSmartMatch)
    .build();
```

## 数据处理优化

### 大文件处理

```java
// 使用 InputStream 而非一次性读取
try (InputStream is = new FileInputStream("large.json")) {
    User user = mapper.readValue(is, User.class);
}

// 或使用 Reader
try (Reader reader = new FileReader("data.json")) {
    List<User> users = mapper.readValue(reader,
        new TypeToken<List<User>>() {});
}
```

### 批量操作

```java
// ✅ 好：批量提取（使用多路径 API）
String[] paths = {"$.name", "$.age", "$.email"};
Type[] types = {String.class, Integer.class, String.class};
JSONPath multiPath = JSONPath.of(paths, types);

Object[] values = multiPath.eval(json);

// ⚠️ 较慢：多次解析
String name = JSONPath.of("$.name").eval(json, String.class);
Integer age = JSONPath.of("$.age").eval(json, Integer.class);
```

## 内存优化

### 使用流式处理

```java
// ❌ 高内存
List<User> users = mapper.readValue(bigJson,
    new TypeToken<List<User>>() {});

// ✅ 低内存：逐个处理
try (JSONParser parser = JSONParser.ofUTF8(new File("large.json"))) {
    while (parser.hasNext()) {
        if (parser.getCurrentToken() == JSONToken.START_OBJECT) {
            User user = parser.readObject(User.class);
            processUser(user);  // 立即处理
        }
        parser.nextToken();
    }
}
```

### 缓冲区池化

fastjson3 内部使用缓冲区池化，通常无需额外配置。

```java
// 自动池化，无需手动管理
byte[] bytes = JSON.toJSONBytes(obj);
```

## 不同场景的最佳配置

### 高吞吐量场景

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.OptimizedForAscii)
    .disableRead(ReadFeature.SupportAutoType)  // 如果不需要
    .build();
```

### 低延迟场景

```java
// 使用默认配置（已是最优）+ 预热
ObjectMapper mapper = ObjectMapper.shared();

// 预热 JIT
for (int i = 0; i < 1000; i++) {
    mapper.writeValueAsString(sampleObject);
}
```

### 内存敏感场景

```java
// 使用 byte[] 而非 String
byte[] json = mapper.writeValueAsBytes(obj);

// 禁用不需要的特性
ObjectMapper mapper = ObjectMapper.builder()
    .disableRead(ReadFeature.SupportSmartMatch)
    .build();
```

## 性能对比

### 输入输出格式

| 操作 | 相对性能 | 说明 |
|------|----------|------|
| `writeValueAsBytes()` / `toJSONBytes()` | 100% | 基准（UTF-8 直接输出） |
| `writeValueAsString()` / `toJSONString()` | ~85% | 需要额外编码转换 |
| `readValue(byte[], ...)` | 100% | 基准（UTF-8 直接解析） |
| `readValue(String, ...)` | ~90% | 需要字符解析路径 |
| `JSONPath.extract()` | 100% | 基准（零拷贝路径） |
| 先 `readValue` 再查询 | ~70% | 需要完整物化 |

### Reader/Writer 路径（对比 fastjson2 2.0.61）

以下是 [`benchmark_3.0.0-SNAPSHOT-66a5e2a.md`](../benchmark/benchmark_3.0.0-SNAPSHOT-66a5e2a.md) 的主要结果（Eishay 场景，JDK 25，3 fork × 5 iter，单线程）：

| 场景 | aarch64 (Neoverse N2) | x86_64 (EPYC 9T95) |
|------|---------------------:|-------------------:|
| `EishayParseUTF8Bytes` — ASM / fj2 | **115.25%** ✅ | **118.79%** ✅ |
| `EishayParseString` — ASM / fj2 | **100.09%** ✅ | **107.00%** ✅ |
| `EishayWriteUTF8Bytes` — ASM / fj2 | **110.57%** ✅ | **110.01%** ✅ |
| `EishayWriteString` — ASM / fj2 | **126.01%** ✅ | **143.55%** ✅ |

- **ASM 路径**（当前默认）全平台全场景超过 fastjson2 2.0.61。
- **反射路径**（仅 native-image / Android / 复杂类型 fallback）会慢 15–30pp，不在业务路径上。

## 性能分析

### 测量性能

```java
// 使用 JMH 进行准确基准测试
@BenchmarkMode(Mode.Throughput)
@State(Scope.Thread)
public class JsonBenchmark {

    private ObjectMapper mapper;
    private User user;

    @Setup
    public void setup() {
        mapper = ObjectMapper.shared();
        user = new User("张三", 25);
    }

    @Benchmark
    public String serialize() {
        return mapper.writeValueAsString(user);
    }
}
```

### 简单测试

```java
long start = System.nanoTime();
for (int i = 0; i < 10000; i++) {
    mapper.writeValueAsString(user);
}
long duration = System.nanoTime() - start;
System.out.printf("平均: %.2f μs/op%n", duration / 10000.0 / 1000);
```

## JVM 优化

### JIT 预热

JVM 需要预热才能达到最佳性能：

```java
// 预热循环
for (int i = 0; i < 1000; i++) {
    mapper.writeValueAsString(sampleObject);
}
// 现在开始实际测量
```

### 内存设置

```bash
# 增加堆内存
java -Xmx2g -Xms2g YourApp

# 使用 G1GC
java -XX:+UseG1GC YourApp
```

## 最佳实践总结

1. **复用 ObjectMapper** - 单例或共享实例
2. **优先 byte[]** - UTF-8 数据使用字节
3. **使用默认配置** - AUTO provider 自动走 ASM 路径，全平台超过 fastjson2
4. **预编译 JSONPath** - 缓存编译结果
5. **禁用不需要的特性** - 减少开销
6. **使用流式处理** - 大数据量场景

## 相关文档

- 🏗️ [优化技术深入 →](../internals/optimization.md)
- 📖 [POJO 序列化 →](pojo.md)
- 📋 [ObjectMapper API →](../api/ObjectMapper.md)

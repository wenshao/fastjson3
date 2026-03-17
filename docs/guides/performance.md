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

### 3. 启用 ASM 字节码生成

生产环境启用 ASM 可以提升 10-20% 性能。

```java
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreator(ObjectReaderCreatorASM::createObjectReader)
    .writerCreator(ObjectWriterCreatorASM::createObjectWriter)
    .build();
```

**注意：** Android 平台不支持 ASM，会自动回退到反射。

### 4. 使用预编译的 JSONPath

```java
// ❌ 不好：每次编译
public List<String> getAuthors(String json) {
    JSONPath path = JSONPath.of("$.store.book[*].author");
    return path.extract(json, List.class);
}

// ✅ 好：预编译
private static final JSONPath AUTHOR_PATH =
    JSONPath.compile("$.store.book[*].author");

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
// ✅ 好：批量提取
JSONPath.TypedMultiPath multi = JSONPath.typedMulti()
    .path("$.name", String.class)
    .path("$.age", Integer.class)
    .path("$.email", String.class)
    .build();

Object[] values = multi.extract(json);

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
    .readerCreator(ObjectReaderCreatorASM::createObjectReader)
    .writerCreator(ObjectWriterCreatorASM::createObjectWriter)
    .enableWrite(WriteFeature.OptimizedForAscii)
    .disableRead(ReadFeature.SupportAutoType)  // 如果不需要
    .build();
```

### 低延迟场景

```java
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreator(ObjectReaderCreatorASM::createObjectReader)
    .writerCreator(ObjectWriterCreatorASM::createObjectWriter)
    .build();

// 预热
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

不同操作的性能参考（相对值）：

| 操作 | 相对性能 | 说明 |
|------|----------|------|
| `toJSONBytes()` | 100% | 基准 |
| `toJSONString()` | ~85% | 需要额外编码转换 |
| 反射模式 | 100% | 基准 |
| ASM 模式 | ~110% | 约 10% 提升 |
| `ObjectPath.extract()` | 100% | 基准 |
| 先解析再查询 | ~70% | 需要完整解析 |

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
3. **启用 ASM** - 生产环境启用
4. **预编译 JSONPath** - 缓存编译结果
5. **禁用不需要的特性** - 减少开销
6. **使用流式处理** - 大数据量场景

## 相关文档

- 🏗️ [优化技术深入 →](../internals/optimization.md)
- 📖 [POJO 序列化 →](pojo.md)
- 📋 [ObjectMapper API →](../api/ObjectMapper.md)

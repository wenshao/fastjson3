# 项目定位

fastjson3 是 fastjson2 的下一代版本。

## 目标

- **Java 21+ baseline** - sealed class、pattern matching、switch expression、record patterns
- **性能超越** - 比 wast、fastjson2 更快
- **API 对标 Jackson 3** - 不可变 ObjectMapper、Builder 模式
- **多平台支持** - GraalVM Native Image、Android 8+

## 设计理念

### 1. 现代 Java 特性

使用 Java 21+ 的新特性：

```java
// sealed class：类型安全的层次结构
public sealed class JSONParser
    permits JSONParser.Str, JSONParser.UTF8, JSONParser.CharArray {
    // ...
}

// pattern matching：简洁的模式匹配
String result = switch (typeTag) {
    case TYPE_STRING -> "string";
    case TYPE_INT -> "int";
    default -> "object";
};

// record：不可变数据载体
public record User(String name, int age) { }
```

### 2. 不可变性优先

```java
// ✅ 好：不可变
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.SupportSmartMatch)
    .build();  // 构建后不可修改

// ❌ 不好：可变
ObjectMapper mapper = new ObjectMapper();
mapper.configure(SupportSmartMatch, true);  // 可修改，非线程安全
```

### 3. 平台适配

通过 `@JVMOnly` 注解标记平台专有代码：

```java
@JVMOnly
class ObjectReaderCreatorASM {
    // Android 构建时自动排除
}
```

## 与其他库对比

| 特性 | fastjson3 | fastjson2 | Jackson 3 | Gson |
|------|-----------|-----------|------------|------|
| Java 版本 | 17+ | 8+ | 17+ | 8+ |
| sealed class | ✅ | ❌ | ✅ | ❌ |
| 不可变 ObjectMapper | ✅ | ❌ | ✅ | ❌ |
| ASM 字节码生成 | ✅ | ✅ | ❌ | ❌ |
| Android 支持 | ✅ 独立构建 | ✅ | ✅ | ✅ |
| GraalVM 支持 | ✅ | ✅ | ✅ | ⚠️ |
| JSON Schema | ✅ Draft 2020-12 | ❌ | ✅ | ❌ |
| JSONPath RFC 9535 | ✅ | ⚠️ | ❌ | ❌ |

## 应用场景

### 最适合

- **现代 Java 项目**（Java 21+）
- **高性能场景**（需要极致性能）
- **微服务**（低延迟、高吞吐）
- **Android 应用**（需要独立构建）

### 也可以

- **Java 8 项目**（部分特性需要适配）
- **Spring Boot 3.x**（原生集成）
- **GraalVM Native Image**（原生应用）

## 相关文档

- [核心架构](core.md)
- [性能优化](../optimization/)

[← 返回索引](README.md)

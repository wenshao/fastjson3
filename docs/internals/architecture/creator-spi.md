# Creator SPI

ObjectReader/ObjectWriter 的可插拔创建策略。

## 概述

fastjson3 提供了 `ObjectReaderProvider` 和 `ObjectWriterProvider` 接口来控制 Reader/Writer 的创建策略：

- **AUTO**: 自动选择最佳策略（**默认**，JVM 上走 ASM，Android / Native Image 自动回退反射）
- **ASM**: 强制使用 ASM 字节码生成
- **REFLECT**: 强制使用反射 + Unsafe

## Provider 接口

### ReaderCreatorType

```java
public enum ReaderCreatorType {
    /** 自动选择最佳策略 */
    AUTO,
    /** 强制使用 ASM 字节码生成 */
    ASM,
    /** 强制使用反射 */
    REFLECT
}
```

### WriterCreatorType

```java
public enum WriterCreatorType {
    /** 自动选择最佳策略 */
    AUTO,
    /** 强制使用 ASM 字节码生成 */
    ASM,
    /** 强制使用反射 */
    REFLECT
}
```

## 使用方式

### 指定 Reader 策略

```java
// 使用反射（适用于需要动态加载类的场景）
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreatorType(ReaderCreatorType.REFLECT)
    .build();
```

```java
// 使用 ASM（跨 ClassLoader 场景，性能略低于反射）
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreatorType(ReaderCreatorType.ASM)
    .build();
```

```java
// 自动选择
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreatorType(ReaderCreatorType.AUTO)
    .build();
```

### 指定 Writer 策略

```java
// 使用反射
ObjectMapper mapper = ObjectMapper.builder()
    .writerCreatorType(WriterCreatorType.REFLECT)
    .build();
```

```java
// 使用 ASM
ObjectMapper mapper = ObjectMapper.builder()
    .writerCreatorType(WriterCreatorType.ASM)
    .build();
```

### 同时指定 Reader 和 Writer

```java
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreatorType(ReaderCreatorType.ASM)
    .writerCreatorType(WriterCreatorType.ASM)
    .build();
```

## Provider 实现

### AutoObjectReaderProvider / AutoObjectWriterProvider

自动选择最佳策略：

- Native Image 环境 → 使用反射
- Android 环境 → 使用反射
- 普通 POJO → 尝试 ASM，失败则回退到反射
- 复杂类型（Record、Sealed Class）→ 使用反射

### ASMObjectReaderProvider / ASMObjectWriterProvider

强制使用 ASM 字节码生成：

- **性能最优路径**（Path B 完成后，x86_64 / aarch64 上 Parse / Write 全面超过 fj2 2.0.61）
- 启动时间稍长（需要生成字节码）
- 不适用于 Native Image 和 Android

### ReflectObjectReaderProvider / ReflectObjectWriterProvider

强制使用反射 + Unsafe：

- 启动快
- 适用于 Native Image / Android
- 比 ASM 路径慢 ~15–30 pp（Parse），~5–10 pp（Write）

## 性能对比

Path B（PR #72–#81）完成后，ASM 路径全面领先：

### Parse UTF8Bytes（按 fj2 2.0.61 归一）

| 平台 | ASM | 反射 | ASM - 反射 |
|------|-----:|-----:|----------:|
| aarch64 (Neoverse N2) | **115.25%** | 73.05% | +42.2 pp |
| x86_64 (EPYC 9T95) | **118.79%** | 79.56% | +39.2 pp |

### Write UTF8Bytes（按 fj2 2.0.61 归一）

| 平台 | ASM | 反射 | ASM - 反射 |
|------|-----:|-----:|----------:|
| aarch64 | **110.57%** | 102.63% | +7.9 pp |
| x86_64 | **110.01%** | 98.79% | +11.2 pp |

数据来源：[`docs/benchmark/benchmark_3.0.0-SNAPSHOT-66a5e2a.md`](../../benchmark/benchmark_3.0.0-SNAPSHOT-66a5e2a.md)。

### 为什么 ASM 比反射更快

ASM 生成的 Reader/Writer 通过以下机制全面碾压反射路径：

- **Unsafe 字段访问** - ASM 生成字节码直接调用 `JDKUtils.getInt/putLongDirect`，跳过反射的访问检查
- **按字段展开** - 每个字段的 name-match 和 value-write 都内联到一个大方法中，没有 per-field dispatch cost
- **`readStringValueFast` SWAR** - PR #74 的 parse 路径每 8 字节检查一次结束符，比反射路径的 per-byte 快 6–8x
- **`writeName1L/2L` 按长度特化** - PR #81 的 write 路径让每个 `"jsonName":` 只用 1 条 `ldc2_w + invokevirtual`，ASM 生成的 `OW_*.write` 始终落在 JIT 的 `FreqInlineSize=325` 预算内

### 建议

- **使用默认配置**（AUTO provider）：JVM 上自动选择 ASM 路径，Android / Native Image 自动 fallback 到反射
- 如果需要强制行为一致（比如性能测试对比），用 `writerCreatorType / readerCreatorType` 显式指定

## 平台差异

| 平台 | 默认 Reader 策略 | 默认 Writer 策略 | 说明 |
|------|------------------|------------------|------|
| JDK 21+ (JVM) | AUTO → ASM | AUTO → ASM | 简单 POJO 走 ASM，复杂类型自动回退反射 |
| Android | REFLECT | REFLECT | ASM 不可用（DEX 不支持运行时字节码），自动回退 |
| Native Image | REFLECT | REFLECT | ASM 不可用（需要构建时反射注册），自动回退 |

## 自定义 Provider

```java
// 自定义 Provider：添加缓存
public class CachedObjectReaderProvider implements ObjectReaderProvider {
    private final Map<Class<?>, ObjectReader<?>> cache = new ConcurrentHashMap<>();
    private final ObjectReaderProvider delegate;

    public CachedObjectReaderProvider(ObjectReaderProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> ObjectReader<T> getObjectReader(Class<T> type) {
        return (ObjectReader<T>) cache.computeIfAbsent(type, t ->
            delegate.getObjectReader(t));
    }

    @Override
    public ReaderCreatorType getCreatorType() {
        return delegate.getCreatorType();
    }
}

// 使用自定义 Provider
ObjectMapper mapper = ObjectMapper.builder()
    .readerProvider(new CachedObjectReaderProvider(
        ObjectReaderProvider.of(ReaderCreatorType.ASM)))
    .build();
```

## 资源清理

Provider 可能缓存生成的 ASM 类。使用 `cleanup()` 方法释放资源：

```java
// 共享 Provider（默认）- cleanup 是 no-op
ObjectReaderProvider shared = ObjectReaderProvider.defaultProvider();
shared.cleanup(); // 不做任何事

// per-ObjectMapper Provider - cleanup 清理缓存
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreatorType(ReaderCreatorType.ASM)
    .build();
// mapper 持有独立的 Provider 实例
// 当 mapper 不再需要时，可以调用 cleanup()
```

## 相关文档

- [ASM 字节码生成](../optimization/asm-bytecode.md)
- [字段处理架构](field-handling.md)
- [ObjectMapper API](../../api/ObjectMapper.md)

[← 返回索引](README.md)

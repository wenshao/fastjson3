# Creator SPI

ObjectReader/ObjectWriter 的可插拔创建策略。

## 概述

fastjson3 提供了 `ObjectReaderProvider` 和 `ObjectWriterProvider` 接口来控制 Reader/Writer 的创建策略：

- **REFLECT**: 反射 + Unsafe（默认）
- **AUTO**: 自动选择最佳策略
- **ASM**: 强制使用 ASM 字节码生成

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
// 使用 ASM（最佳性能，JDK 21+ 推荐）
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

- 性能最优（约 7-20% 提升）
- 启动时间稍长（需要生成字节码）
- 不适用于 Native Image

### ReflectObjectReaderProvider / ReflectObjectWriterProvider

强制使用反射 + Unsafe：

- 启动快
- 适用于 Native Image
- Write 性能与 ASM 持平

## 性能对比

### Writer（序列化）

| 场景 | 反射 | ASM | 提升 |
|------|------|-----|------|
| 简单 POJO | 100% | ~120% | +20% |
| 嵌套 POJO | 100% | ~120% | +20% |

**Writer 结论：** ASM 显著更快，推荐使用。

### Reader（反序列化）

| 场景 | 反射 | ASM | 差异 |
|------|------|-----|------|
| 简单 POJO | 100% | ~116% | **快 16%** |
| 嵌套 POJO | 100% | ~91% | **慢 9%** |

**Reader 结论：**
- 对于只包含基本类型的简单 POJO，ASM 更快
- 对于包含嵌套对象的 POJO，当前 ASM 实现比反射慢

**原因分析：** 当前 ASM 实现对嵌套对象字段会 fallback 到反射路径，产生额外开销（接口调用、offset 同步）。这是"伪 ASM"问题，未来优化后可能会改善。

### 建议

- **默认使用 REFLECT**：稳定可靠，性能一致
- **Writer 可使用 ASM**：序列化性能提升明显
- **特定场景 ASM**：如果确认只有基本类型字段，可以尝试 ASM

## 平台差异

| 平台 | 默认 Reader 策略 | 默认 Writer 策略 | 说明 |
|------|------------------|------------------|------|
| JDK 21+ | REFLECT | AUTO | Reader 当前反射更稳定；Writer ASM 更快 |
| Android | REFLECT | AUTO | ASM 不可用 |
| Native Image | REFLECT | AUTO | ASM 不可用 |

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
- [ObjectReader API](../../api/ObjectReader.md)

[← 返回索引](README.md)

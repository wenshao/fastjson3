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

| 场景 | 反射 | ASM | 差异 |
|------|------|-----|------|
| 简单 POJO (Users) | **100%** | ~87% | 反射快 13% |
| 嵌套 POJO (User+Friend) | **100%** | ~87% | 反射快 13% |

### Reader（反序列化）

| 场景 | 反射 | ASM | 差异 |
|------|------|-----|------|
| 简单 POJO | **100%** | ~90% | 反射快 10% |
| 嵌套 POJO | **100%** | ~83% | 反射快 17% |

### 为什么反射比 ASM 更快

反射路径的 `readFieldsLoop` / `writeFields` 循环结构紧凑（方法体 < 325 bytes），JIT 能将整个 FieldWriter/FieldReader 调用链内联为一个编译单元。ASM 生成的方法体将所有字段展开在一个方法中，超出 JIT 内联预算，关键解析/序列化方法（如 `writeNameString`、`readStringOff`）无法被内联。

### 建议

- **使用默认配置**：反射路径已是最快路径，无需手动配置 ASM
- ASM 作为跨 ClassLoader 场景的兼容方案保留

## 平台差异

| 平台 | 默认 Reader 策略 | 默认 Writer 策略 | 说明 |
|------|------------------|------------------|------|
| JDK 21+ | REFLECT | REFLECT | 反射路径 JIT 优化最佳 |
| Android | REFLECT | REFLECT | ASM 不可用 |
| Native Image | REFLECT | REFLECT | ASM 不可用 |

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

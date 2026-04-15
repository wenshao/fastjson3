# 核心架构

fastjson3 的核心组件和设计理念。

## 架构图

```
                          ObjectMapper (immutable, thread-safe)
                         ┌──────┴──────┐
                    ObjectReader    ObjectWriter
                    (interface)     (interface)
                         │               │
              ┌──────────┼──────────┐     ┌┴─────────────┐
           Reflection   ASM    Module  Reflection   ASM
           Creator    Creator         Creator     Creator
                         │                          │
                   @JVMOnly                   @JVMOnly
```

## ObjectMapper

### 不可变设计

```java
public final class ObjectMapper {
    private final long readerFeatures;
    private final long writerFeatures;
    private final Function<Class<?>, ObjectReader<?>> readerCreator;
    private final Function<Class<?>, ObjectWriter<?>> writerCreator;

    // 私有构造函数
    private ObjectMapper(Builder builder) {
        this.readerFeatures = builder.readerFeatures;
        this.writerFeatures = builder.writerFeatures;
        this.readerCreator = builder.readerCreator;
        this.writerCreator = builder.writerCreator;
    }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    // 共享实例
    public static ObjectMapper shared() {
        return SharedInstance.DEFAULT;
    }
}
```

### 为什么不可变？

1. **线程安全** - 无需同步
2. **可预测** - 行为不会改变
3. **可缓存** - 子对象可以预创建

## ObjectReader / ObjectWriter

### 接口定义

```java
public interface ObjectReader<T> {
    T read(JSONParser parser) throws IOException;
    T read(byte[] bytes) throws IOException;
}

public interface ObjectWriter<T> {
    void write(JSONGenerator gen, Object object) throws IOException;
    byte[] writeAsBytes(T object) throws IOException;
}
```

### Creator SPI

可插拔的创建策略：

```java
// 反射创建器（默认）
ObjectReaderCreator reflectionCreator = clazz ->
    new ObjectReaderReflective(clazz);

// ASM 创建器（可选）
ObjectReaderCreator asmCreator = clazz ->
    new ObjectReaderASMGenerator().createReader(clazz);

// 模块创建器（扩展）
ObjectReaderCreator moduleCreator = clazz ->
    MyCustomModule.createReader(clazz);
```

## 设计理念

### 1. 不可变性

所有核心组件都是不可变的：

- `ObjectMapper` - 构建后不可修改
- `ReadFeature` / `WriteFeature` - 枚举常量
- `JSONPath` - 编译后不可变

### 2. 可插拔

通过 SPI 扩展：

```java
// AUTO provider 是默认，JVM 上自动走 ASM。显式指定只用于测试 / 特殊对照
ObjectMapper mapper = ObjectMapper.builder()
    .writerCreatorType(WriterCreatorType.ASM)
    .readerCreatorType(ReaderCreatorType.ASM)
    .modules(List.of(new CustomModule()))
    .build();
```

### 3. 平台适配

通过注解标记平台专有代码：

```java
@JVMOnly
class ObjectReaderCreatorASM {
    // Android 构建时自动排除
}
```

## 相关文档

- [Creator SPI](creator-spi.md)
- [平台支持](platforms.md)

[← 返回索引](README.md)

# Creator SPI

ObjectReader/ObjectWriter 的可插拔创建策略。

## 设计

```java
// Creator 是一个函数接口
Function<Class<?>, ObjectReader<?>> readerCreator
Function<Class<?>, ObjectWriter<?>> writerCreator
```

## 默认实现

### 反射 Creator

```java
public final class ObjectReaderCreatorReflective
    implements Function<Class<?>, ObjectReader<?>> {

    @Override
    public ObjectReader<?> apply(Class<?> clazz) {
        // 使用反射创建 FieldReader
        List<FieldReader> fieldReaders = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            int modifier = field.getModifiers();
            if (Modifier.isStatic(modifier)) continue;

            TypeTag typeTag = getTypeTag(field.getType());
            long fieldOffset = UNSAFE.objectFieldOffset(field);
            String fieldName = field.getName();

            fieldReaders.add(new FieldReader(
                typeTag, fieldName, fieldOffset, ordinal
            ));
        }

        return new ReflectiveObjectReader(fieldReaders.toArray(FieldReader[]::new));
    }
}
```

### ASM Creator

```java
public final class ObjectReaderCreatorASM
    implements Function<Class<?>, ObjectReader<?>> {

    @Override
    public ObjectReader<?> apply(Class<?> clazz) {
        // 使用 ASM 生成专门的 Reader 类
        byte[] bytecode = ASMUtils.generateReaderClass(clazz);
        Class<?> readerClass = defineClass(bytecode);
        return (ObjectReader<?>) readerClass.newInstance();
    }
}
```

## 性能对比

| 操作 | 反射 | ASM | 提升 |
|------|------|-----|------|
| Read | 100% | 107% | +7% |
| Write | 100% | 100% | 持平 |

**Write 持平原因：** 反射 Writer 已通过 Unsafe 高度优化。

## 使用方式

### 配置 ASM Creator

```java
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreator(ObjectReaderCreatorASM::createObjectReader)
    .writerCreator(ObjectWriterCreatorASM::createObjectWriter)
    .build();
```

### 条件启用

```java
Function<Class<?>, ObjectReader<?>> readerCreator =
    JDKUtils.UNSAFE_AVAILABLE && !JDKUtils.NATIVE_IMAGE
        ? new ObjectReaderCreatorASM()
        : new ObjectReaderCreatorReflective();
```

## 自定义 Creator

```java
// 自定义 Creator：添加缓存
public class CachedObjectReaderCreator
    implements Function<Class<?>, ObjectReader<?>> {

    private final Map<Class<?>, ObjectReader<?>> cache = new ConcurrentHashMap<>();
    private final Function<Class<?>, ObjectReader<?>> delegate;

    public CachedObjectReaderCreator(Function<Class<?>, ObjectReader<?>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public ObjectReader<?> apply(Class<?> clazz) {
        return cache.computeIfAbsent(clazz, delegate);
    }
}
```

## 相关文档

- [ASM 字节码生成](../optimization/asm-bytecode.md)
- [字段处理架构](field-handling.md)

[← 返回索引](README.md)

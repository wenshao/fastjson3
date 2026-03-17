# Type Tag 分派

**性能提升**：~5% | **难度**：⭐⭐ | **适用场景**：多态调用

## 问题描述

传统多态调用在类型过多时性能下降：

```java
// ❌ Megamorphic callsite（>3 种类型）
interface FieldWriter {
    void write(JSONGenerator gen, Object bean);
}
// StringFieldWriter, IntFieldWriter, LongFieldWriter, BooleanWriter...
```

**JIT 限制**：超过 3 种类型退化为虚表调用，无法内联。

## 解决方案：Type Tag + Switch

```java
// ✅ 单态调用点
public final class FieldWriter {
    final int typeTag;  // TYPE_STRING=1, TYPE_INT=2, ...

    public void writeField(JSONGenerator gen, Object bean) {
        switch (typeTag) {  // 编译为跳转表
            case TYPE_STRING -> writeString(gen, bean);
            case TYPE_INT -> writeInt(gen, bean);
            // ...
        }
    }
}
```

## 性能对比

| 实现方式 | 相对性能 | JIT 优化 |
|----------|----------|----------|
| 接口多态（2种） | 100% | 可内联 |
| 接口多态（3种） | 100% | 可内联 |
| 接口多态（>3种） | ~85% | 退化为虚表调用 |
| Type Tag + Switch | ~95% | 跳转表，部分优化 |

## 核心概念

### Megamorphic Callsite 问题

```java
// 当一个调用点有多种类型时
void process(Object obj) {
    if (obj instanceof TypeA) f1(obj);
    else if (obj instanceof TypeB) f2(obj);
    else if (obj instanceof TypeC) f3(obj);
    else if (obj instanceof TypeD) f4(obj);  // Megamorphic!
}
```

JIT 编译器对**超过 3 种类型**的调用点优化有限。

### Type Tag 解决方案

```java
// 不用 instanceof，用整数 tag
class FieldWriter {
    static final int TYPE_STRING = 1;
    static final int TYPE_INT = 2;
    static final int TYPE_LONG = 3;

    final int typeTag;

    void write(JSONGenerator gen, Object bean) {
        switch (typeTag) {  // 编译为 tableswitch 或 lookupswitch
            case TYPE_STRING -> gen.writeString((String) value);
            case TYPE_INT -> gen.writeInt((int) value);
            case TYPE_LONG -> gen.writeLong((long) value);
        }
    }
}
```

## 实现方式

### 方式 1：switch（Java 17+ 语法）

```java
public final class FieldWriter {
    private final int typeTag;
    private final String fieldName;
    private final long fieldOffset;  // Unsafe 偏移量

    public void write(JSONGenerator gen, Object bean) {
        switch (typeTag) {
            case TypeTags.STRING -> {
                String value = UNSAFE.getString(bean, fieldOffset);
                gen.writeString(value);
                break;
            }
            case TypeTags.INT -> {
                int value = UNSAFE.getInt(bean, fieldOffset);
                gen.writeInt(value);
                break;
            }
            case TypeTags.LONG -> {
                long value = UNSAFE.getLong(bean, fieldOffset);
                gen.writeLong(value);
                break;
            }
            default -> throw new IllegalStateException("Unknown type: " + typeTag);
        }
    }
}
```

### 方式 2：跳转表（手动实现）

```java
public final class FieldWriter {
    private final int typeTag;
    private static final WriteFunction[] WRITE_FUNCTIONS = {
        (gen, bean, offset) -> gen.writeString(UNSAFE.getString(bean, offset)),
        (gen, bean, offset) -> gen.writeInt(UNSAFE.getInt(bean, offset)),
        (gen, bean, offset) -> gen.writeLong(UNSAFE.getLong(bean, offset)),
    };

    private final WriteFunction writeFunction;

    public FieldWriter(int typeTag, long fieldOffset) {
        this.typeTag = typeTag;
        this.writeFunction = WRITE_FUNCTIONS[typeTag];
    }

    public void write(JSONGenerator gen, Object bean) {
        writeFunction.write(gen, bean, fieldOffset);
    }
}
```

## TypeTag 定义

```java
public final class TypeTags {
    // 基础类型
    public static final int BOOL = 1;
    public static final int BYTE = 2;
    public static final int SHORT = 3;
    public static final int INT = 4;
    public static final int LONG = 5;
    public static final int FLOAT = 6;
    public static final int DOUBLE = 7;
    public static final int STRING = 8;
    public static final int CHAR = 9;

    // 对象类型
    public static final int OBJECT = 20;
    public static final int LIST = 21;
    public static final int MAP = 22;
    public static final int SET = 23;

    // 特殊类型
    public static final int NULL = 30;
    public static final int BIG_INTEGER = 31;
    public static final int BIG_DECIMAL = 32;
}
```

## 字节码对比

### 接口调用方式

```
ALOAD 1           // 加载 gen
ALOAD 2           // 加载 fieldWriter
INVOKEINTERFACE FieldWriter.write  // 虚方法调用
// 每次调用都要查虚表
```

### Type Tag 方式

```
ALOAD 1           // 加载 gen
ALOAD 2           // 加载 fieldWriter
GETFIELD FieldWriter.typeTag  // 读取 int
LOOKUPSWITCH      // 跳转表
  case 1: L001
  case 2: L002
  case 3: L003
L001:
  // 直接调用 writeString
  INVOKESTATIC JSONGenerator.writeString
L002:
  // 直接调用 writeInt
  INVOKESTATIC JSONGenerator.writeInt
// 跳转表 + 直接调用，更容易内联
```

## 应用场景

### 场景 1：字段序列化器

```java
public class ObjectWriterCreator {
    public ObjectWriter<?> createObjectWriter(Class<?> clazz) {
        List<FieldWriter> fieldWriters = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            int typeTag = getTypeTag(field.getType());
            long fieldOffset = UNSAFE.objectFieldOffset(field);
            fieldWriters.add(new FieldWriter(field.getName(), typeTag, fieldOffset));
        }

        return new CompiledObjectWriter(fieldWriters.toArray(new FieldWriter[0]));
    }
}
```

### 场景 2：JSONPath 求值

```java
public abstract class JSONPath {
    public abstract Object eval(Object root);

    // 使用 Type Tag 优化
    private static final class PropertyPath extends JSONPath {
        final int typeTag;
        final long offset;

        @Override
        public Object eval(Object root) {
            switch (typeTag) {
                case TypeTags.STRING:
                    return UNSAFE.getString(root, offset);
                case TypeTags.INT:
                    return UNSAFE.getInt(root, offset);
                case TypeTags.LONG:
                    return UNSAFE.getLong(root, offset);
                default:
                    return fallback(root);
            }
        }
    }
}
```

## 你可以学到什么

1. **避免 megamorphic 调用** - 超过 3 种类型使用 switch
2. **Type Tag 模式** - 用枚举代替多态
3. **内联友好** - switch 更容易被 JIT 优化
4. **跳转表** - Java switch 的底层实现

## 参考资料

- [JVM 调用点优化](https://wiki.openjdk.org/display/HotSpot/Server%20Compiler%20Inlining)
- [Tableswitch 指令](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-3.html#jvms-3.10)
- [Megamorphic 调用问题](https://medium.com/javaperformance/megamorphic-call-site-performance-issues-with-jdk-8-and-later-9c814a07f76c)

## 相关优化

- [ASM 字节码生成](asm-bytecode.md) - CodeGen 时生成专用代码
- [转移计算](shift-computation.md) - 将类型判断转移到 CodeGen 阶段

[← 返回索引](README.md)

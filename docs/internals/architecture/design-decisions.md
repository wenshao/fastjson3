# 设计决策记录

fastjson3 的关键设计决策及其原因。

## 为什么使用 sealed class？

### 背景

Java 17 引入了 sealed class，允许类声明其允许的子类。

### 决策

使用 sealed class 定义 JSONParser 和 JSONGenerator 的层次结构。

### 原因

1. **性能** - JIT 可以做 devirtualization（去虚化）
   - 将虚方法调用优化为直接调用或内联
   - 性能提升约 5-10%

2. **可维护性** - 编译时检查所有子类
   - 新增子类必须在 permits 列表中声明
   - 防止意外的继承

3. **可读性** - 清晰表达封闭层次结构
   - 代码即文档，一眼看出所有子类

### 示例

```java
// 编译时已知所有子类
sealed class JSONParser
    permits JSONParser.Str, JSONParser.UTF8, JSONParser.CharArray {
}

// JIT 可以优化虚方法调用
JSONParser parser = new JSONParser.UTF8(bytes);
parser.readObject();  // 无虚表查找，可内联
```

## 为什么使用 @JVMOnly 而非 Maven profiles？

### 背景

需要区分 JVM 专有代码（如 ASM）和跨平台代码。

### 决策

使用 `@JVMOnly` 注解标记 JVM 专有代码。

### 原因

1. **源码级标记** - 一目了然
   - 注解直接在代码上，不需要外部配置文件

2. **自动排除** - Android profile 自动扫描
   - Maven 编译器插件自动排除带 @JVMOnly 的类

3. **无需维护** - 新增类只需加注解
   - 不需要同步修改 pom.xml 中的 exclude 列表

### 示例

```java
@JVMOnly
class ObjectReaderCreatorASM {
    // Android 构建时自动排除
}
```

### 对比

| 方案 | 优点 | 缺点 |
|------|------|------|
| Maven profile | 标准做法 | 需要同步 exclude 列表 |
| @JVMOnly | 自动化，无同步问题 | 需要 profile 配置扫描 |

## 为什么用 Type Tag 而非多态？

### 背景

当接口有超过 3 种实现时，JIT 会退化为 megamorphic callsite。

### 决策

使用 Type Tag + switch 代替多态调用。

### 原因

1. **避免 megamorphic** - 超过 3 种类型性能下降
2. **内联友好** - switch 更容易被 JIT 优化
3. **内存效率** - 单个 int vs 对象引用

### 示例

```java
// ❌ 多态：megamorphic
interface FieldWriter {
    void write(JSONGenerator gen, Object bean);
}
// 超过 3 种子类 → JIT 退化为间接跳转

// ✅ Type Tag：单态调用点
public final void writeField(JSONGenerator gen, Object bean) {
    switch (typeTag) {
        case TYPE_STRING -> writeString(gen, bean);  // 可内联
        case TYPE_INT -> writeInt(gen, bean);        // 可内联
    }
}
```

## 为什么默认使用反射而非 ASM？

### 决策

ObjectReader/ObjectWriter 默认使用反射，ASM 需要显式启用。

### 原因

1. **兼容性** - 反射适用于所有平台
2. **启动速度** - 无需字节码生成开销
3. **渐进式优化** - 用户按需启用 ASM

### 性能权衡

| 操作 | 反射 | ASM | 提升 |
|------|------|-----|------|
| Read | 100% | 107% | +7% |
| Write | 100% | 100% | 持平 |

### 启用方式

```java
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreator(ObjectReaderCreatorASM::createObjectReader)
    .writerCreator(ObjectWriterCreatorASM::createObjectWriter)
    .build();
```

## 为什么用 long[] 存储字段名？

### 决策

字段名预编码为 `long[]` 而非 `byte[]`。

### 原因

1. **批量写入** - 每次 putLong 写入 8 字节
2. **内存对齐** - long 写入需要 8 字节对齐
3. **缓存友好** - 连续内存访问

### 示例

```java
// ✅ long[]：8 字节一次写入
static final long[] NAME = {
    0x22757365724e616dL,  // "userNam
    0x65223a0000000000L,  // e":\0\0\0\0\0\0
};
// 2 次 putLong 写入 11 字节

// ❌ byte[]：8 次 putByte 写入
static final byte[] NAME = "\"userName\":".getBytes();
// 11 次 putByte 写入
```

## 相关文档

- [解析器架构](parser.md)
- [字段处理架构](field-handling.md)
- [性能优化](../optimization/)

[← 返回索引](README.md)

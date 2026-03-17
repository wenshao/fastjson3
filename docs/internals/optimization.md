# 性能优化技术详解

fastjson3 在性能优化上做了大量工作，本文档详细介绍这些技术，你也可以将这些技巧应用到自己的项目中。

> 💡 **阅读建议**：无论你是否使用 fastjson3，这些优化技术都能帮助你编写更高效的代码。

---

## 优化概览

| 优化技术 | 性能提升 | 难度 | 适用场景 |
|----------|----------|------|----------|
| SWAR 并行处理 | ~10% | ⭐⭐⭐ | 字符串处理 |
| ASM 字节码生成 | ~10-20% | ⭐⭐⭐⭐ | 反射替代 |
| Unsafe 直接操作 | ~5% | ⭐⭐⭐ | 高频路径 |
| 字段预编码 | ~5% | ⭐⭐ | 固定格式 |
| 缓冲区池化 | ~3% | ⭐⭐ | IO 操作 |
| 单遍检查复制 | ~8% | ⭐⭐⭐ | 字符串复制 |
| 融合容量检查 | ~3% | ⭐⭐ | 缓冲区操作 |
| Type Tag 分派 | ~5% | ⭐⭐ | 多态调用 |

---

## 1. SWAR 并行处理 (SIMD Within A Register)

### 问题

检查字符串中是否有需要转义的字符，传统方式逐字节检查：

```java
// ❌ 传统方式：8 次分支
for (int i = 0; i < 8; i++) {
    byte b = bytes[i];
    if (b == '"' || b == '\\' || b < 0x20) {
        // 需要转义
    }
}
```

**问题**：每个字节都需要分支判断，CPU 分支预测开销大。

### 解决方案：SWAR

一次处理 8 个字节，用位运算代替分支：

```java
// ✅ SWAR 方式：3 次位运算
static boolean noEscape8(long v) {
    long hiMask = 0x8080808080808080L;
    long lo = 0x0101010101010101L;

    // 检测反斜杠 '\\' (0x5C)
    // XOR 0xA3 使 0x5C→0xFF，加 0x01 溢出设置高位
    long notBackslash = (v ^ 0xA3A3A3A3A3A3A3A3L) + lo & hiMask;

    // 快速路径：检查所有字节 > '"' 且不是 '\\'
    if ((notBackslash & v + 0x5D5D5D5D5D5D5D5DL) == hiMask) {
        return true;  // 覆盖 99% 的普通 ASCII 文本
    }

    // 慢速路径：精确检测控制字符和引号
    long ctrl = (v - 0x2020202020202020L) & ~v & hiMask;
    long xq = v ^ 0x2222222222222222L;
    long quote = (xq - lo) & ~xq & hiMask;
    return (ctrl | quote | (notBackslash ^ hiMask)) == 0;
}
```

### 性能对比

| 方法 | 每次操作 | 分支数 | 性能 |
|------|----------|--------|------|
| 传统逐字节 | 8 次比较 | 8 | 基准 |
| SWAR 快速路径 | 3 次位运算 | 1 | **~3x** |

### 你可以学到什么

1. **用位运算代替分支** - 减少 CPU 分支预测失败
2. **并行处理** - 一次处理多个数据单元
3. **快速/慢速路径** - 常见情况快速处理，特殊情况回退

### 应用到你的项目

```java
// 示例：检查字符串是否全是 ASCII
public static boolean isAllAscii(String str) {
    // 逐字节检查（慢）
    for (int i = 0; i < str.length(); i++) {
        if (str.charAt(i) > 127) return false;
    }
    return true;

    // SWAR 方式（快）：一次检查 8 个字节
    // 使用 ByteArray 或 Unsafe 操作...
}
```

---

## 2. ASM 字节码生成

### 问题

Java 反射有性能开销：

```java
// ❌ 反射：每次调用都有开销
Field field = clazz.getDeclaredField("name");
field.setAccessible(true);
String value = (String) field.get(obj);  // 方法调用开销
```

**开销来源**：
- 方法调用虚表查找
- 参数校验
- 安全检查
- JIT 难以内联

### 解决方案：ASM 动态生成类

运行时生成专门的字节码类，直接访问字段：

```java
// ✅ ASM 生成的类（等价于）
public class ObjectWriter_User_1 implements ObjectWriter<User> {
    @Override
    public void write(JSONGenerator gen, Object object, long features) {
        User user = (User) object;
        gen.writeString(user.name);  // 直接字段访问，无反射
    }
}
```

**生成的字节码直接 `getfield`，JIT 可以完全内联。**

### 性能对比

| 方法 | 相对性能 | 说明 |
|------|----------|------|
| 反射 Reader | 100% | 基准 |
| ASM Reader | ~107% | **+7%** |
| 反射 Writer | 100% | 基准（已高度优化） |
| ASM Writer | ~100% | 持平 |

### 你可以学到什么

1. **反射瓶颈** - 高频调用时反射开销明显
2. **动态代码生成** - 运行时生成专门代码
3. **字节码优化** - `getfield` 比 `Method.invoke` 快

### 应用到你的项目

```java
// 方案1：使用 ASM（复杂但灵活）
import org.objectweb.asm.*;

// 方案2：使用 MethodHandle（JDK 7+）
MethodHandles.Lookup lookup = MethodHandles.lookup();
MethodHandle getter = lookup.findGetter(User.class, "name", String.class);
String value = (String) getter.invokeExact(user);

// 方案3：Lambda Metafactory（JDK 8+）
MethodHandle mh = lookup.findVirtual(User.class, "getName", MethodType.methodType(String.class));
CallSite site = LambdaMetafactory.metafactory(
    lookup, "apply", MethodType.methodType(Function.class),
    mh.type(), mh, mh.type()
);
Function<User, String> getter = (Function<User, String>) site.getTarget().invoke();
```

---

## 3. Unsafe 直接内存操作

### 问题

Java 数组访问有边界检查：

```java
// ❌ 每次访问都有边界检查
byte[] buf = new byte[1024];
for (int i = 0; i < buf.length - 8; i++) {
    long v = /* 从 buf[i] 读取 8 字节 */;
}
```

**开销**：每次数组访问都检查边界（JIT 优化后仍可能有残留）。

### 解决方案：Unsafe

```java
// ✅ Unsafe 直接访问
static long getLongDirect(byte[] array, int offset) {
    return UNSAFE.getLong(array, BYTE_ARRAY_BASE_OFFSET + offset);
}
```

**一次边界检查，后续直接内存访问。**

### 性能对比

| 操作 | 普通 array[] | Unsafe |
|------|-------------|--------|
| 读取 8 字节 | 多次边界检查 | **一次检查** |
| 写入 8 字节 | 多次边界检查 | **一次检查** |

### 你可以学到什么

1. **减少边界检查** - 批量操作时优势明显
2. **内存对齐** - long 读写需要 8 字节对齐
3. **安全性考虑** - 确保 `ensureCapacity` 预留足够空间

### 应用到你的项目

```java
// 方案1：JDK Unsafe（内部 API）
sun.misc.Unsafe unsafe = getUnsafe();
long value = unsafe.getLong(array, offset);

// 方案2：ByteBuffer（标准 API）
ByteBuffer buffer = ByteBuffer.wrap(array);
buffer.order(ByteOrder.LITTLE_ENDIAN);
long value = buffer.getLong();

// 方案3：VarHandle（JDK 9+，推荐）
static final VarHandle LONG_HANDLE =
    MethodHandles.lookup().findStaticVarHandle(ByteBuffer.class, "accessMode", long.class);
```

---

## 4. 字段预编码

### 问题

序列化时重复写入相同的字段名：

```java
// ❌ 每次都要写入 "user_name"
gen.writeFieldName("user_name");  // 写 'u','s','e','r','_','n','a','m','e'
gen.writeValue(user.name);
```

### 解决方案：预编码为 long[]

```java
// ✅ 初始化时预编码
static long[] encodeFieldName(String name) {
    byte[] bytes = ("\"" + name + "\":").getBytes(StandardCharsets.UTF_8);
    long[] longs = new long[(bytes.length + 7) / 8];
    // 填充并转换为 long...
    return longs;
}

// 写入时使用 putLong
static void writeName(long[] longs, int byteLen) {
    for (int i = 0; i < longs.length; i++) {
        JDKUtils.putLongDirect(buf, pos, longs[i]);
        pos += 8;
    }
    count += byteLen;  // 使用实际字节数
}
```

### 性能对比

| 方法 | 调用次数 | 性能 |
|------|----------|------|
| 逐字节写入 | N 次 | 基准 |
| 预编码 long[] | N/8 次 | **~30%** 提升 |

### 你可以学到什么

1. **预计算常量** - 固定内容预先编码
2. **批量写入** - 减少调用次数
3. **内存对齐** - long 写入需要 8 字节对齐

---

## 5. 单遍检查复制

### 问题

传统方式先扫描再复制：

```java
// ❌ 两次遍历
int escPos = findEscapePosition(str);  // 第一次扫描
if (escPos >= 0) {
    copyWithEscape(str, escPos);  // 第二次处理
} else {
    arraycopy(str);  // 复制
}
```

### 解决方案：边检查边复制

```java
// ✅ 一次遍历
for (int i = 0; i < len - 7; i += 8) {
    long v = getLongDirect(value, i);
    if (noEscape8(v)) {
        putLongDirect(buf, pos, v);  // 安全，直接复制
        pos += 8;
    } else {
        // 发现转义，切换到逐字节处理
        writeEscaped(value, i, len, pos);
        return;
    }
}
```

### 你可以学到什么

1. **减少遍历次数** - 合并扫描和操作
2. **快速路径优化** - 常见情况不走慢路径
3. **简洁代码** - 避免过多路径影响 JIT 内联

---

## 6. Type Tag 分派

### 问题

传统多态调用在类型过多时性能下降：

```java
// ❌ Megamorphic callsite（>3 种类型）
interface FieldWriter {
    void write(JSONGenerator gen, Object bean);
}
// StringWriter, IntWriter, LongWriter, BooleanWriter...
```

**JIT 限制**：超过 3 种类型退化为虚表调用。

### 解决方案：Type Tag + Switch

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

### 你可以学到什么

1. **避免 megamorphic 调用** - 超过 3 种类型使用 switch
2. **Type Tag 模式** - 用枚举代替多态
3. **内联友好** - switch 更容易被 JIT 优化

---

## 7. 缓冲区池化

### 问题

频繁分配缓冲区导致 GC 压力：

```java
// ❌ 每次分配新缓冲区
byte[] buffer = new byte[8192];
// 使用后丢弃
```

### 解决方案：线程本地池

```java
// ✅ 复用缓冲区
static final ThreadLocal<byte[]> BUFFER_POOL =
    ThreadLocal.withInitial(() -> new byte[8192]);

byte[] buffer = BUFFER_POOL.get();
// 使用后不归还，下次线程复用
```

### 你可以学到什么

1. **减少分配** - 复用大对象
2. **线程安全** - ThreadLocal 避免竞争
3. **权衡** - 内存 vs GC 压力

---

## 8. 融合容量检查

### 问题

每个字段分别检查容量：

```java
// ❌ 两次检查
writeFieldName(name);  // ensureCapacity
writeIntValue(value);  // ensureCapacity
```

### 解决方案：合并检查

```java
// ✅ 一次检查
void writeNameInt(long[] nameLongs, int nameLen, int value) {
    ensureCapacity(nameLen + 12);  // name + int(最多11位) + comma(1)
    writeNameLongs(nameLongs, nameLen);
    writeInt(value);
    buf[count++] = ',';
}
```

---

## 优化设计原则

总结 fastjson3 的优化思路：

1. **减少分支** - SWAR、Type Tag
2. **减少遍历** - 单遍检查复制
3. **减少调用** - 字段预编码、融合检查
4. **避免反射** - ASM 字节码生成
5. **内存复用** - 缓冲区池化
6. **快速路径** - 常见情况特殊处理

## 参考资料

- [SWAR 技术](https://www.chessprogramming.org/SIMD_and_SWAR_Tricks)
- [Java Unsafe 指南](https://www.baeldung.com/java-unsafe)
- [ASM 官方文档](https://asm.ow2.io/)

## 相关文档

- [🏗️ 整体架构 →](architecture.md)
- [📖 性能调优指南 →](../guides/performance.md)

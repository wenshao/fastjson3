# 性能优化技术

fastjson3 的性能优化技术文档已按主题拆分为独立文件。

## 📁 优化文档索引

[📘 查看优化技术索引 →](optimization/)

fastjson3 在性能优化上做了大量工作，本文档详细介绍这些技术，你也可以将这些技巧应用到自己的项目中。

> 💡 **阅读建议**：无论你是否使用 fastjson3，这些优化技术都能帮助你编写更高效的代码。

---

## 优化概览

| 优化技术 | 性能提升 | 难度 | 适用场景 |
|----------|----------|------|----------|
| SWAR 并行处理 | ~10% | ⭐⭐⭐ | 字符串处理 |
| ASM 字节码生成（Path B） | **+20-40 pp** 相对反射 | ⭐⭐⭐⭐⭐ | Reader/Writer 默认路径 |
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

## 2. ASM 字节码生成（跨 ClassLoader 兼容方案）

### 背景

fastjson3 的反射路径（默认）通过 `readFieldsLoop` / `writeFields` 紧凑循环结构，让 JIT 能将
整个 FieldWriter/FieldReader 调用链深度内联为一个编译单元，这是最快的路径。

但在某些场景下（如 OSGi、应用服务器热部署、模块化系统），目标类和 fastjson3 不在同一个
ClassLoader 中，反射路径的 Unsafe 字段访问可能受限。ASM 字节码生成通过在目标类的
ClassLoader 中动态定义 reader/writer 类来解决这个问题。

### 问题

跨 ClassLoader 场景下字段访问受限：

```java
// 跨 ClassLoader 时，Unsafe 字段访问可能受模块系统限制
// ASM 在目标 ClassLoader 中生成类，绕过此限制
```

### 解决方案：ASM 动态生成类

运行时生成专门的字节码类，直接访问字段：

```java
// ASM 生成的类（等价于），定义在目标类的 ClassLoader 中
public class ObjectWriter_User_1 implements ObjectWriter<User> {
    @Override
    public void write(JSONGenerator gen, Object object, long features) {
        User user = (User) object;
        gen.writeString(user.name);  // 直接 getfield 访问
    }
}
```

### 性能对比

Path B（PR #72–#81）完成后，ASM 路径在 x86_64 和 aarch64 上全面超过 fj2 2.0.61 和反射路径：

| 方法 | ASM / fj2 | 反射 / fj2 | 说明 |
|------|---------:|----------:|------|
| Parse UTF8Bytes (aarch64) | **115.25%** | 72.47% | ASM Unsafe 字段访问 + `readStringValueFast` SWAR |
| Parse UTF8Bytes (x86_64) | **118.79%** | 79.56% | 同上，x86 AVX 也参与 |
| Write UTF8Bytes (aarch64) | **110.57%** | 102.63% | PR #81 `writeName1L/2L` 按长度特化让 `OW_*.write` 落在 ARM C2 `FreqInlineSize=325` 预算内 |
| Write UTF8Bytes (x86_64) | **110.01%** | 98.79% | PR #80 W#1+W#2+W#4 整体重写 |

数据来源：[`docs/benchmark/benchmark_3.0.0-SNAPSHOT-66a5e2a.md`](../benchmark/benchmark_3.0.0-SNAPSHOT-66a5e2a.md)。

### 你可以学到什么

1. **Path B 关键教训** - JIT 内联预算（FreqInlineSize ≈ 325 字节码）是硬边界，ASM 生成的大方法必须控制在预算内才能被 caller 内联。PR #81 的 `writeName1L/2L` 按长度特化就是为了把 `OW_Media.write` 从 460 字节压到预算内
2. **Unsafe 字段访问优于 getter/setter** - ASM 生成的字节码使用 `JDKUtils.getInt/putLongDirect` 直接操作对象内存，跳过反射的访问检查和边界检查
3. **按字段展开 + 按长度/类型特化** - fj2 的 `writeNameXRaw` per-length 模式（fj3 在 PR #81 中移植）通过为每个 name 长度生成专用的小方法，让所有调用都能被内联

### 应用到你的项目

```java
// 方案1：使用 ASM（跨 ClassLoader 兼容）
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
void writeNameInt(long[] nameByteLongs, int nameLen, int value) {
    ensureCapacity(nameLen + 12);  // name + int(最多11位) + comma(1)
    writeNameLongs(nameByteLongs, nameLen);
    writeInt(value);
    buf[count++] = ',';
}
```

---

## 8. 转移计算（Shift Computation）

### 问题

运行时重复计算相同内容：

```java
// ❌ 每次序列化都要计算
public void writeUser(JSONGenerator gen, User user) {
    gen.writeFieldName("userName");        // 每次写入字符串
    gen.writeString(user.getUserName());

    gen.writeFieldName("userAge");         // 每次写入字符串
    gen.writeInt(user.getUserAge());

    gen.writeFieldName("userEmail");       // 每次写入字符串
    gen.writeString(user.getUserEmail());
}
```

**开销**：
- 字段名字符串每次都写入
- 字段类型判断每次都执行
- 格式化模式每次都解析

### 解决方案：CodeGen 阶段预计算

将计算转移到**类定义阶段**或**代码生成阶段**：

```java
// ✅ CodeGen 阶段生成的专用代码
public final class UserWriter implements ObjectWriter<User> {
    // 定义阶段：预编码字段名
    private static final long[] NAME_USER_NAME = encodeFieldName("userName");
    private static final long[] NAME_USER_AGE = encodeFieldName("userAge");
    private static final long[] NAME_USER_EMAIL = encodeFieldName("userEmail");
    private static final int NAME_USER_NAME_LEN = "userName\":".length();
    private static final int NAME_USER_AGE_LEN = "userAge\":".length();

    // 运行时：直接使用预计算结果
    public void write(JSONGenerator gen, Object object) {
        User user = (User) object;

        // 直接写入预编码的 long[]，无字符串处理
        gen.writeNameLongs(NAME_USER_NAME, NAME_USER_NAME_LEN);
        gen.writeString(user.name);

        gen.writeNameLongs(NAME_USER_AGE, NAME_USER_AGE_LEN);
        gen.writeInt(user.age);

        gen.writeNameLongs(NAME_USER_EMAIL, NAME_USER_EMAIL_LEN);
        gen.writeString(user.email);
    }
}
```

### 转移计算的层次

| 计算时机 | 示例 | 性能提升 |
|----------|------|----------|
| **运行时每次** | `gen.writeFieldName("name")` | 基准（最慢） |
| **类加载时一次** | `static final long[] NAME = encode("name")` | ~30% |
| **代码生成时** | ASM 生成专用 Writer 类 | ~50% |

### 实际示例

#### 1. 字段名预编码（类加载时）

```java
// ❌ 运行时：每次都转码写字符串
gen.writeFieldName("user_name");  // "user_name" -> UTF-8 bytes -> write

// ✅ 定义时：预编码为 long[]
static final long[] NAME_USER_NAME = {
    0x225f757365725f6eL,  // "\"user_n"
    0x616d65223a000000L  // "ame\":\0\0\0"
};
static final int NAME_LEN = 11;  // "\"user_name\":"

// 运行时：直接 putLong
gen.writeNameLongs(NAME_USER_NAME, NAME_LEN);
```

#### 2. 类型判断预计算（代码生成时）

```java
// ❌ 运行时：每次判断类型
public void writeField(Object value) {
    if (value instanceof String) {
        writeString((String) value);
    } else if (value instanceof Integer) {
        writeInt((Integer) value);
    } else if (value instanceof Long) {
        writeLong((Long) value);
    }
    // ...
}

// ✅ 代码生成时：为每种类型生成专门方法
// ASM 生成的 StringFieldWriter
public void writeField(JSONGenerator gen, Object bean) {
    gen.writeString(((User) bean).name);  // 无类型判断
}

// ASM 生成的 IntFieldWriter
public void writeField(JSONGenerator gen, Object bean) {
    gen.writeInt(((User) bean).age);  // 无类型判断
}
```

#### 3. 格式化模式预解析（定义时）

```java
// ❌ 运行时：每次解析格式字符串
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;
// 每次序列化都要解析 "yyyy-MM-dd HH:mm:ss"

// ✅ 定义时：预解析为格式化对象
static class DateFormatter {
    static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  // 类加载时解析一次
}
// 运行时直接使用 FORMATTER.format()
```

#### 4. JSONPath 预编译（使用时）

```java
// ❌ 每次都编译路径
for (int i = 0; i < 10000; i++) {
    Object result = JSONPath.of("$.store.book[*].price").eval(json);
}

// ✅ 编译一次，复用多次
JSONPath path = JSONPath.of("$.store.book[*].price");  // 编译一次
for (int i = 0; i < 10000; i++) {
    Object result = path.eval(json);  // 直接求值
}
```

### 性能对比

| 操作 | 运行时计算 | 预计算 | 提升 |
|------|-----------|--------|------|
| 写字段名 | 字符串转码 | long[] 写入 | **~30%** |
| 类型判断 | instanceof 分支 | 直接调用 | **~20%** |
| 格式解析 | 每次解析 | 预解析 | **~15%** |
| 路径编译 | 每次编译 | 编译一次 | **~100x** |

### 你可以学到什么

1. **定义阶段计算** - 类加载时计算一次，运行时直接用
2. **代码生成优化** - ASM 生成专用代码，消除分支
3. **预编译复用** - 编译一次，复用多次
4. **常量提取** - 固定内容预计算

### 应用到你的项目

```java
// 1. 字符串常量预编码
public class Constants {
    public static final byte[] HEADER = ("HTTP/1.1 200 OK\r\n").getBytes(StandardCharsets.UTF_8);
    public static final long[] HEADER_LONG = toLongArray(HEADER);
}

// 2. 格式化器预创建
private static final DateTimeFormatter DATE_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd");

// 3. 正则表达式预编译
private static final Pattern EMAIL_PATTERN =
    Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

// 4. 配置预解析
private static final Config CONFIG = Config.load();  // 启动时加载

// 5. 使用 AnnotationProcessor 或 APT 在编译时生成代码
@Generated("UserProcessor")
public class UserWriter {
    // 编译时生成的代码，零运行时开销
}
```

### 最佳实践

| 场景 | 预计算方式 | 时机 |
|------|-----------|------|
| 字符串常量 | 预编码为 byte[] | 类加载时 |
| 格式化模式 | 预解析为 Formatter | 类加载时 |
| 正则表达式 | 预编译为 Pattern | 类加载时 |
| 配置文件 | 预解析为对象 | 应用启动时 |
| 模板 | 预编译为代码 | 编译时（APT） |
| SQL/JSONPath | 预编译 | 定义时或首次使用时 |

---

## 优化设计原则

总结 fastjson3 的优化思路：

1. **减少分支** - SWAR、Type Tag
2. **减少遍历** - 单遍检查复制
3. **减少调用** - 字段预编码、融合检查
4. **JIT 友好架构** - 紧凑循环结构
5. **内存复用** - 缓冲区池化
6. **快速路径** - 常见情况特殊处理
7. **转移计算** - 将计算从运行时转移到定义/CodeGen 阶段

## 参考资料

- [SWAR 技术](https://www.chessprogramming.org/SIMD_and_SWAR_Tricks)
- [Java Unsafe 指南](https://www.baeldung.com/java-unsafe)
- [ASM 官方文档](https://asm.ow2.io/)

## 相关文档

- [🏗️ 整体架构 →](architecture.md)
- [📖 性能调优指南 →](../guides/performance.md)

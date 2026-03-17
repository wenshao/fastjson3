# Unsafe 直接内存操作

**性能提升**：~5% | **难度**：⭐⭐⭐ | **适用场景**：高频路径

## 问题描述

Java 数组访问有边界检查：

```java
// ❌ 每次访问都有边界检查
byte[] buf = new byte[1024];
for (int i = 0; i < buf.length - 8; i++) {
    long v = /* 从 buf[i] 读取 8 字节 */;
}
```

**开销**：每次数组访问都检查边界（JIT 优化后仍可能有残留）。

## 解决方案：Unsafe

```java
// ✅ Unsafe 直接访问
static long getLongDirect(byte[] array, int offset) {
    return UNSAFE.getLong(array, BYTE_ARRAY_BASE_OFFSET + offset);
}
```

**一次边界检查，后续直接内存访问。**

## 性能对比

| 操作 | 普通 array[] | Unsafe |
|------|-------------|--------|
| 读取 8 字节 | 多次边界检查 | **一次检查** |
| 写入 8 字节 | 多次边界检查 | **一次检查** |

## Unsafe 基础

### 获取 Unsafe 实例

```java
import sun.misc.Unsafe;

public class UnsafeAccessor {
    private static final Unsafe UNSAFE;
    private static final long BYTE_ARRAY_BASE_OFFSET;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
            BYTE_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static long getLong(byte[] array, int offset) {
        return UNSAFE.getLong(array, BYTE_ARRAY_BASE_OFFSET + offset);
    }

    public static void putLong(byte[] array, int offset, long value) {
        UNSAFE.putLong(array, BYTE_ARRAY_BASE_OFFSET + offset, value);
    }
}
```

### 内存对齐

long 读写需要 8 字节对齐：

```java
// ❌ 错误：未对齐访问可能导致性能下降或崩溃
long value = UNSAFE.getLong(array, offset);  // offset 可能不是 8 的倍数

// ✅ 正确：确保对齐
assert (offset & 7) == 0;  // offset 必须是 8 的倍数
long value = UNSAFE.getLong(array, BYTE_ARRAY_BASE_OFFSET + offset);
```

## 应用场景

### 场景 1：批量复制

```java
// 传统方式：逐字节复制
public static void copy(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
    for (int i = 0; i < length; i++) {
        dest[destPos + i] = src[srcPos + i];
    }
}

// Unsafe 方式：8 字节批量复制
public static void copyFast(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
    int i = 0;

    // 8 字节一批
    for (; i <= length - 8; i += 8) {
        long v = UNSAFE.getLong(src, BYTE_ARRAY_BASE_OFFSET + srcPos + i);
        UNSAFE.putLong(dest, BYTE_ARRAY_BASE_OFFSET + destPos + i, v);
    }

    // 剩余字节
    for (; i < length; i++) {
        dest[destPos + i] = src[srcPos + i];
    }
}
```

### 场景 2：字符串比较

```java
// 比较两个字节数组是否相等
public static boolean equals(byte[] a, byte[] b) {
    if (a.length != b.length) return false;

    int i = 0;
    // 8 字节一批比较
    for (; i <= a.length - 8; i += 8) {
        long va = UNSAFE.getLong(a, BYTE_ARRAY_BASE_OFFSET + i);
        long vb = UNSAFE.getLong(b, BYTE_ARRAY_BASE_OFFSET + i);
        if (va != vb) return false;
    }

    // 剩余字节
    for (; i < a.length; i++) {
        if (a[i] != b[i]) return false;
    }
    return true;
}
```

### 场景 3：避免数组越界检查

```java
// 传统方式
for (int i = 0; i < array.length; i++) {
    sum += array[i];  // 每次循环都有边界检查
}

// Unsafe 方式
long end = BYTE_ARRAY_BASE_OFFSET + array.length;
for (long offset = BYTE_ARRAY_BASE_OFFSET; offset < end; offset++) {
    sum += UNSAFE.getByte(array, offset);  // 手动确保安全
}
```

## 安全考虑

### 容量检查

```java
public static void safeCopy(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
    // ✅ 使用前检查
    if (srcPos < 0 || destPos < 0 || length < 0 ||
        srcPos + length > src.length ||
        destPos + length > dest.length) {
        throw new IndexOutOfBoundsException();
    }

    // 检查通过后，Unsafe 操作是安全的
    int i = 0;
    for (; i <= length - 8; i += 8) {
        long v = UNSAFE.getLong(src, BYTE_ARRAY_BASE_OFFSET + srcPos + i);
        UNSAFE.putLong(dest, BYTE_ARRAY_BASE_OFFSET + destPos + i, v);
    }
    // ...
}
```

### 内存屏障

```java
// 在多线程场景，确保内存可见性
UNSAFE.putOrderedLong(object, offset, value);  // 有序写入
UNSAFE.loadFence();  // 加载屏障
UNSAFE.storeFence(); // 存储屏障
```

## JDK 替代方案

### 1. ByteBuffer（标准 API）

```java
ByteBuffer buffer = ByteBuffer.wrap(array);
buffer.order(ByteOrder.LITTLE_ENDIAN);
long value = buffer.getLong();  // 相对 Unsafe 更安全
```

### 2. VarHandle（JDK 9+，推荐）

```java
static final VarHandle LONG_HANDLE =
    MethodHandles.lookup()
        .findVarHandle(byte[].class, long.class, "value");

long value = (long) LONG_HANDLE.getVolatile(array, offset);
```

### 对比

| 方案 | 性能 | 安全性 | 标准 |
|------|------|--------|------|
| Unsafe | 最高 | 低 | ❌ 内部 API |
| ByteBuffer | 高 | 高 | ✅ 标准 |
| VarHandle | 高 | 高 | ✅ 标准 |

## 你可以学到什么

1. **减少边界检查** - 批量操作时优势明显
2. **内存对齐** - long 读写需要 8 字节对齐
3. **安全性考虑** - 确保 `ensureCapacity` 预留足够空间
4. **权衡** - 性能 vs 安全性

## 参考资料

- [Java Unsafe 指南](https://www.baeldung.com/java-unsafe)
- [VarHandle 详解](https://openjdk.org/projects/jdk9/features/jigsaw)
- [内存屏障与 Java 并发](https://www.cs.umd.edu/~pugh/java/memoryModel/)

## 相关优化

- [SWAR 并行处理](swar.md) - 需要 Unsafe 基础
- [字段预编码](field-pre-encoding.md) - 使用 Unsafe 写入

[← 返回索引](README.md)

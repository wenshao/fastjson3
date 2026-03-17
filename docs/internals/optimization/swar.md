# SWAR 并行处理 (SIMD Within A Register)

**性能提升**：~10% | **难度**：⭐⭐⭐ | **适用场景**：字符串处理

## 问题描述

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

## 解决方案：SWAR

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

## 性能对比

| 方法 | 每次操作 | 分支数 | 性能 |
|------|----------|--------|------|
| 传统逐字节 | 8 次比较 | 8 | 基准 |
| SWAR 快速路径 | 3 次位运算 | 1 | **~3x** |

## 核心思想

### 1. 并行处理

一次读取 8 个字节（64 位 long），并行处理：

```java
long v = UNSAFE.getLong(byteArray, offset);
```

### 2. 位运算代替分支

使用位运算的特性来检测多个条件：

- **加法溢出检测**：`(x + lo) & hiMask` 检测是否溢出
- **减法范围检测**：`(v - min) & ~v` 检测是否在范围内
- **XOR 相等检测**：`(v ^ pattern)` 检测是否等于某个值

### 3. 快速/慢速路径

- **快速路径**：处理 99% 的普通情况，无分支
- **慢速路径**：精确处理特殊情况，有分支

## 应用示例

### 示例 1：检查全是 ASCII

```java
// 传统方式
public static boolean isAllAscii(byte[] bytes) {
    for (byte b : bytes) {
        if (b < 0) return false;  // 负数表示非 ASCII
    }
    return true;
}

// SWAR 方式
public static boolean isAllAsciiSWAR(byte[] bytes) {
    long hiMask = 0x8080808080808080L;
    for (int i = 0; i <= bytes.length - 8; i += 8) {
        long v = UNSAFE.getLong(bytes, BYTE_ARRAY_BASE_OFFSET + i);
        if ((v & hiMask) != 0) return false;  // 检测高位
    }
    // 处理剩余字节
    return true;
}
```

### 示例 2：查找字符

```java
// 传统方式
public static int findChar(byte[] bytes, char target) {
    for (int i = 0; i < bytes.length; i++) {
        if (bytes[i] == target) return i;
    }
    return -1;
}

// SWAR 方式（简化）
public static int findCharSWAR(byte[] bytes, char target) {
    long targetMask = target * 0x0101010101010101L;
    for (int i = 0; i <= bytes.length - 8; i += 8) {
        long v = UNSAFE.getLong(bytes, BYTE_ARRAY_BASE_OFFSET + i);
        long xor = v ^ targetMask;
        long hasZero = (xor - 0x0101010101010101L) & ~xor & 0x8080808080808080L;
        if (hasZero != 0) {
            // 找到了，精确定位
            return i + Long.numberOfTrailingZeros(hasZero) / 8;
        }
    }
    return -1;
}
```

## 你可以学到什么

1. **用位运算代替分支** - 减少 CPU 分支预测失败
2. **并行处理** - 一次处理多个数据单元
3. **快速/慢速路径** - 常见情况快速处理，特殊情况回退
4. **SIMD 思想** - 在单个寄存器中并行处理数据

## 参考资料

- [SWAR 技术详解](https://www.chessprogramming.org/SIMD_and_SWAR_Tricks)
- [SIMD 在字符串处理中的应用](https://gist.github.com/artsybasov/719279)
- [Hacker's Delight](http://www.hackersdelight.org/) - 位运算经典书籍

## 相关优化

- [单遍检查复制](single-pass-copy.md) - 结合 SWAR 的复制优化
- [Unsafe 直接操作](unsafe-memory.md) - SWAR 需要的基础

[← 返回索引](README.md)

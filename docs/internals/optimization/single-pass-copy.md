# 单遍检查复制

**性能提升**：~8% | **难度**：⭐⭐⭐ | **适用场景**：字符串复制

## 问题描述

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

**问题**：大多数情况下没有转义字符，但仍要扫描两次。

## 解决方案：边检查边复制

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

## 核心思想

### 快速路径优化

```
大部分情况（99%）：无转义字符
├── 快速路径：直接复制，无分支
└── 一次遍历完成

少部分情况（1%）：有转义字符
├── 切换到慢速路径
└── 逐字节处理转义
```

### 代码对比

```java
// 传统方式：两次遍历
public String escape(String str) {
    // 第一次遍历：检查是否需要转义
    boolean needEscape = false;
    for (int i = 0; i < str.length(); i++) {
        if (needEscape(str.charAt(i))) {
            needEscape = true;
            break;
        }
    }

    // 第二次遍历：处理转义
    if (needEscape) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (needEscape(c)) {
                sb.append(escapeChar(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    } else {
        return str;  // 不需要转义
    }
}

// 单遍方式：边检查边复制
public String escape(String str) {
    char[] chars = str.toCharArray();
    for (int i = 0; i < chars.length; i++) {
        char c = chars[i];
        if (needEscape(c)) {
            // 发现转义，切换到处理模式
            return escapeWithFallback(chars, i);
        }
    }
    // 无转义，直接返回
    return str;
}
```

## SWAR 结合使用

```java
public void writeString(String str) {
    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    int len = bytes.length;

    int i = 0;
    // 8 字节一批检查
    for (; i <= len - 8; i += 8) {
        long v = getLongDirect(bytes, i);

        // SWAR 检查：是否需要转义
        if (noEscape8(v)) {
            // 安全：直接复制 8 字节
            putLongDirect(buf, pos, v);
            pos += 8;
        } else {
            // 发现转义字符
            break;
        }
    }

    // 处理剩余字节或转义字符
    if (i < len) {
        writeWithEscape(bytes, i, len);
    }
}
```

## 性能分析

### 为什么快？

1. **减少遍历**：常见情况一次遍历
2. **批量处理**：8 字节一次复制
3. **分支预测友好**：快速路径无分支

### 性能对比

| 场景 | 传统方式 | 单遍方式 | 提升 |
|------|----------|----------|------|
| 无转义（99%） | 2 次遍历 | 1 次遍历 | **~50%** |
| 有转义（1%） | 2 次遍历 | 1-2 次遍历 | **~10%** |
| 整体平均 | - | - | **~8%** |

## 完整实现

```java
public class StringEscaper {
    public static void write(JSONGenerator gen, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        int len = bytes.length;

        // 确保容量
        gen.ensureCapacity(len * 2);  // 最坏情况：全转义

        int pos = gen.count;
        byte[] buf = gen.buf;

        int i = 0;

        // 快速路径：8 字节批量检查和复制
        for (; i <= len - 8; i += 8) {
            long v = getLongDirect(bytes, i);

            if (noEscape8(v)) {
                // 无转义字符，直接复制 8 字节
                putLongDirect(buf, pos, v);
                pos += 8;
            } else {
                // 发现转义字符，切换到慢速路径
                break;
            }
        }

        // 慢速路径：处理剩余字节或转义字符
        writeSlowPath(bytes, i, len, buf, pos);
    }

    private static void writeSlowPath(byte[] bytes, int start, int end,
                                       byte[] buf, int pos) {
        for (int i = start; i < end; i++) {
            byte b = bytes[i];
            switch (b) {
                case '"' -> { buf[pos++] = '\\'; buf[pos++] = '"'; }
                case '\\' -> { buf[pos++] = '\\'; buf[pos++] = '\\'; }
                case '\n' -> { buf[pos++] = '\\'; buf[pos++] = 'n'; }
                case '\r' -> { buf[pos++] = '\\'; buf[pos++] = 'r'; }
                case '\t' -> { buf[pos++] = '\\'; buf[pos++] = 't'; }
                default -> { buf[pos++] = b; }
            }
        }
    }

    // SWAR 检查 8 字节是否需要转义
    private static boolean noEscape8(long v) {
        long hiMask = 0x8080808080808080L;
        long lo = 0x0101010101010101L;
        long notBackslash = (v ^ 0xA3A3A3A3A3A3A3A3L) + lo & hiMask;
        return (notBackslash & v + 0x5D5D5D5D5D5D5D5DL) == hiMask;
    }
}
```

## 你可以学到什么

1. **减少遍历次数** - 合并扫描和操作
2. **快速路径优化** - 常见情况不走慢路径
3. **简洁代码** - 避免过多路径影响 JIT 内联
4. **批量处理** - 8 字节一批处理

## 应用场景

| 场景 | 适用性 | 说明 |
|------|--------|------|
| JSON 字符串转义 | ✅ 最佳 | 大多数无转义，快速路径 |
| XML 转义 | ✅ 适用 | 类似模式 |
| HTML 转义 | ✅ 适用 | 类似模式 |
| Base64 编码 | ❌ 不适用 | 每个字符都要转换 |

## 参考资料

- [SWAR 技术](swar.md) - 单遍检查的基础
- [SIMD 优化](https://www.agner.org/optimize/optimizing_cpp.pdf) - 第 13 章

## 相关优化

- [SWAR 并行处理](swar.md) - 批量检查的基础
- [字段预编码](field-pre-encoding.md) - 直接复制优化

[← 返回索引](README.md)

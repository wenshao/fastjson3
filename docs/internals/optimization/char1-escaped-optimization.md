# CHAR1_ESCAPED 优化：单字符转义查找表

## 概述

`CHAR1_ESCAPED` 优化借鉴自 fastjson2，使用查找表替代 switch 语句处理单字符转义序列，减少分支误预测。

## 实现原理

### 传统 switch 方式

```java
switch (c) {
    case '"', '\\', '/' -> sb.append(c);
    case 'b' -> sb.append('\b');
    case 'f' -> sb.append('\f');
    case 'n' -> sb.append('\n');
    case 'r' -> sb.append('\r');
    case 't' -> sb.append('\t');
    // ...
    default -> throw new JSONException("invalid escape: \\" + c);
}
```

**缺点**：
- 多个分支可能导致分支误预测
- JIT 编译器优化有限

### 查找表方式

```java
static final int[] CHAR1_ESCAPED = new int[128];
static {
    java.util.Arrays.fill(CHAR1_ESCAPED, -1);
    CHAR1_ESCAPED['0'] = '\0';
    CHAR1_ESCAPED['1'] = '\1';
    CHAR1_ESCAPED['2'] = '\2';
    CHAR1_ESCAPED['3'] = '\3';
    CHAR1_ESCAPED['4'] = '\4';
    CHAR1_ESCAPED['5'] = '\5';
    CHAR1_ESCAPED['6'] = '\6';
    CHAR1_ESCAPED['7'] = '\7';
    CHAR1_ESCAPED['b'] = '\b';
    CHAR1_ESCAPED['t'] = '\t';
    CHAR1_ESCAPED['n'] = '\n';
    CHAR1_ESCAPED['v'] = '\u000b';
    CHAR1_ESCAPED['f'] = '\f';
    CHAR1_ESCAPED['r'] = '\r';
    CHAR1_ESCAPED['"'] = '"';
    CHAR1_ESCAPED['\''] = '\'';
    CHAR1_ESCAPED['/'] = '/';
    CHAR1_ESCAPED['\\'] = '\\';
    // ...
}

// 使用：
int mapped = CHAR1_ESCAPED[b];
if (mapped >= 0) {
    sb.append((char) mapped);
    continue;
}
```

**优点**：
- 单次数组访问，无分支
- CPU 缓存友好
- JIT 优化更好

## 在 fastjson3 中的使用

### readStringEscaped 方法

```java
private String readStringEscaped(int start, int quote) {
    StringBuilder sb = new StringBuilder(offset - start + 16);
    if (offset > start) {
        sb.append(new String(bytes, start, offset - start, StandardCharsets.UTF_8));
    }
    while (offset < end) {
        int b = bytes[offset++] & 0xFF;
        if (b == quote) {
            return sb.toString();
        }
        if (b == '\\') {
            if (offset >= end) {
                throw new JSONException("unterminated string escape");
            }
            b = bytes[offset++] & 0xFF;
            // Fast path: use lookup table for single-char escapes
            if (b < CHAR1_ESCAPED.length) {
                int mapped = CHAR1_ESCAPED[b];
                if (mapped >= 0) {
                    sb.append((char) mapped);
                    continue;
                }
            }
            // Special case: unicode escape
            if (b == 'u') {
                // unicode escape handling...
            } else {
                throw new JSONException("invalid escape: \\" + (char) b);
            }
        } else if (b < 0x80) {
            sb.append((char) b);
        } else {
            // Multi-byte UTF-8 sequence...
        }
    }
    throw new JSONException("unterminated string");
}
```

## 支持的转义序列

| 转义 | 含义 | ASCII码 |
|------|------|--------|
| `\0` | null | 0 |
| `\b` | backspace | 8 |
| `\t` | horizontal tab | 9 |
| `\n` | newline | 10 |
| `\v` | vertical tab | 11 |
| `\f` | form feed | 12 |
| `\r` | carriage return | 13 |
| `\"` | double quote | 34 |
| `\'` | single quote | 39 |
| `\\` | backslash | 92 |
| `\/` | slash | 47 |

## 性能特点

### 最佳场景
- JSON 字符串中包含大量转义序列
- 常见转义：`\n`, `\t`, `\r`, `\"`

### 基准测试结果

```
No escapes avg:   19 ms (52,632 ops/s)
With escapes avg: 60 ms (16,667 ops/s)
```

## 正确性验证

运行验证测试：
```bash
java -cp "core3/target/classes:core3/target/test-classes:..." \
    com.alibaba.fastjson3.benchmark.EscapeCharBenchmark
```

预期输出：
```
✓ Escape sequences parsed correctly!
```

## 参考资料

- fastjson2: `com.alibaba.fastjson2.JSONReader.CHAR1_ESCAPED`
- 查找表优化技术：用空间换时间，减少分支预测失败

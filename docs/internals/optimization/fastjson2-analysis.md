# fastjson2 vs fastjson3 优化技巧对比分析

## 1. 数字解析优化：digit2 (SWAR) ✅ 已实现

### fastjson2 实现
```java
// IOUtils.java
public static int digit2(byte[] buf, int off) {
    short x = UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + off);
    if (BIG_ENDIAN) {
        x = Short.reverseBytes(x);
    }
    int d;
    // SWAR技巧：一次验证2个数字字符
    if ((((x & 0xF0F0) - 0x3030) | (((d = x & 0x0F0F) + 0x0606) & 0xF0F0)) != 0) {
        return -1;
    }
    return (d & 0xF) * 10 + (d >> 8);
}

// 在 readInt32Value 中使用：每次处理2位数字
while (offset + 1 < end
        && (d = IOUtils.digit2(bytes, offset)) != -1
        && Integer.MIN_VALUE / 100 <= result && result <= 0) {
    result = result * 100 - d;
    offset += 2;  // 每次前进2位
}
```

### fastjson3 实现 ✅
```java
// 已在 JSONParser.UTF8.readIntDirect() 中实现
static int digit2(byte[] bytes, int off) {
    int x = ((bytes[off] & 0xFF) << 8) | (bytes[off + 1] & 0xFF);
    int d = x & 0x0F0F;
    if ((((x & 0xF0F0) - 0x3030) | ((d + 0x0606) & 0xF0F0)) != 0) {
        return -1;
    }
    return (d & 0xF) * 10 + (d >> 8);
}

// 使用：每次处理2位数字
while (off + 1 < e) {
    int d2 = digit2(b, off);
    if (d2 < 0) break;
    if (value > 21474836) break;  // Integer.MAX_VALUE / 100
    value = value * 100 + d2;
    off += 2;
}
```

**实现日期**: 2026-03-17
**性能**: 在 8 位整数场景下减少 50% 循环迭代

---

## 2. 短字段名完全展开 (Unrolled Field Name)

### fastjson2 实现
```java
// 对 1-8 字符的字段名完全展开，避免循环
if ((c0 = bytes[offset]) == quote) {
    nameValue = 0;
} else if ((c1 = bytes[offset + 1]) == quote
    && c0 != '\\' && c0 > 0) {
    nameValue = c0;
    this.nameLength = 1;
    offset += 2;
} else if ((c2 = bytes[offset + 2]) == quote
    && c0 != '\\' && c1 != '\\'
    && c0 >= 0 && c1 > 0) {
    nameValue = (c1 << 8) + c0;
    this.nameLength = 2;
    offset += 3;
}
// ... 3-8 字符的类似展开
```

### fastjson3 实现
```java
// 使用循环，没有针对短字段名优化
for (;;) {
    int c1 = b[off] & 0xFF;
    if (c1 == quote) {
        off++;
        break;
    }
    // ...
    hash += c1 + c2;
    off += 2;
}
```

**建议**：对常见短字段名（id, name, age等）进行完全展开优化

---

## 3. 转义字符查找 (indexOfSlash)

### fastjson2 实现
```java
// 缓存上次找到的反斜杠位置
int slashIndex = this.nextEscapeIndex;
if (slashIndex == ESCAPE_INDEX_NOT_SET
    || (slashIndex != -1 && slashIndex < offset)) {
    jsonReader.nextEscapeIndex = slashIndex = IOUtils.indexOfSlash(bytes, offset, end);
}

// 使用 SWAR 向量化查找
public static int indexOfSlashV(byte[] buf, int fromIndex, int max) {
    int i = fromIndex;
    while (i < upperBound
            && notContains(UNSAFE.getLong(buf, address), 0x5C5C5C5C5C5C5C5CL)) {
        i += 8;
    }
    return indexOfChar(buf, '\\', i, max);
}
```

### fastjson3 实现
```java
// 使用 Vector API 一次扫描 quote、backslash、non-ASCII
if (JDKUtils.VECTOR_SUPPORT) {
    off = VectorizedScanner.scanStringSimple(b, off, e);
}
// 没有 indexOfSlash 缓存
```

**分析**：fastjson3 使用 Vector API 已经很快，但缺少缓存机制

---

## 4. INT_VALUE_END 查找表

### fastjson2 实现
```java
static final boolean[] INT_VALUE_END = new boolean[256];
static {
    Arrays.fill(INT_VALUE_END, true);
    char[] chars = {'.', 'e', 'E', 't', 'f', 'n', '{', '[',
                    '0', '1', '2', '2', '3', '4', '5', '6', '7', '8', '9'};
    for (char ch : chars) {
        INT_VALUE_END[ch] = false;  // 这些字符表示数字结束
    }
}

// 快速检查数字是否结束
if (INT_VALUE_END[ch & 0xff]) {
    // 数字结束
}
```

### fastjson3 实现
```java
// 使用条件判断
if (c < '0' || c > '9') {
    break;
}
```

**建议**：使用 INT_VALUE_END 查找表可以减少分支

---

## 5. 单字符转义查找表 ✅ 已实现

### fastjson2 实现
```java
static final byte[] CHAR1_ESCAPED;
static {
    byte[] char1_escaped = new byte[128];
    Arrays.fill(char1_escaped, (byte) -1);
    byte[] mapping = {
        '0', '\0', '1', '\1', ..., 'n', '\n', 'r', '\r', ...
    };
    for (int i = 0; i < mapping.length; i += 2) {
        char1_escaped[mapping[i]] = mapping[i + 1];
    }
    CHAR1_ESCAPED = char1_escaped;
}

// 使用：
c = char1(c);  // 直接查表
```

### fastjson3 实现 ✅
```java
// 已实现 CHAR1_ESCAPED 查找表
static final int[] CHAR1_ESCAPED = new int[128];
static {
    java.util.Arrays.fill(CHAR1_ESCAPED, -1);
    CHAR1_ESCAPED['0'] = '\0';
    CHAR1_ESCAPED['1'] = '\1';
    // ... 其他映射
    CHAR1_ESCAPED['n'] = '\n';
    CHAR1_ESCAPED['t'] = '\t';
    CHAR1_ESCAPED['r'] = '\r';
    // ...
}

// 使用：
int mapped = CHAR1_ESCAPED[b];
if (mapped >= 0) {
    sb.append((char) mapped);
    continue;
}
```

**实现日期**: 2026-03-17
**性能**: 使用查找表替代 switch，减少分支误预测

---

## 6. 空白字符位掩码

### fastjson2 和 fastjson3 都已实现 ✅
```java
static final long SPACE = 1L | (1L << ' ') | (1L << '\n') | (1L << '\r')
                       | (1L << '\f') | (1L << '\t') | (1L << '\b');

if (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
    // 跳过空白
}
```

---

## 总结：可借鉴的优化技巧

| 优化技巧 | fastjson2 | fastjson3 | 状态 | 优先级 |
|---------|-----------|-----------|------|--------|
| digit2 (2位数字并行) | ✅ | ✅ | 已实现 | ⭐⭐⭐ |
| 短字段名展开 | ✅ | ⚠️ | 部分实现(2字节展开) | ⭐⭐⭐ |
| indexOfSlash 缓存 | ✅ | ❌ | 架构不兼容 | ⭐ 中 |
| INT_VALUE_END 查找表 | ✅ | ❌ | 待实现 | ⭐⭐ 中 |
| 单字符转义查找表 | ✅ | ✅ | 已实现 | ⭐⭐ |
| Vector API 扫描 | ❌ | ✅ | 已有 | - |
| 空白字符位掩码 | ✅ | ✅ | 已有 | - |

---

## 建议实现的优化

### ~~1. digit2 方法（最大收益）~~ ✅ 已完成
数字解析时每次处理 2 位数字，已实现并验证正确性。

**相关文件**:
- `core3/src/main/java/com/alibaba/fastjson3/JSONParser.java` (digit2 方法, readIntDirect)
- `core3/src/test/java/com/alibaba/fastjson3/benchmark/Digit2BeforeAfterBenchmark.java`

### ~~2. 单字符转义查找表~~ ✅ 已完成
使用查找表代替 switch 处理单字符转义。

**相关文件**:
- `core3/src/main/java/com/alibaba/fastjson3/JSONParser.java` (CHAR1_ESCAPED)
- `core3/src/test/java/com/alibaba/fastjson3/benchmark/EscapeCharBenchmark.java`

### 3. 短字段名展开
对常见短字段名（id, name, age 等）进行完全展开，减少循环开销。
**注**: fastjson3 已实现 2 字节展开循环，完全展开收益可能有限。

### 4. INT_VALUE_END 查找表
减少数字结束检查的分支。

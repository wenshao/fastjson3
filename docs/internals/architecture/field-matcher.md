# 字段名匹配

FieldNameMatcher 的两级策略。

## 两级策略

```
1. Byte Comparison (主路径)
   └── 预编码字段名为 byte[]
   └── first-byte dispatch
   └── 直接字节比较

2. Hash-based (回退)
   └── 对 String/char[] 或转义序列
   └── 三种哈希策略自动选择
```

## 策略 1：字节比较

### 工作原理

```java
// 预编码字段名
byte[] fieldNameBytes = "userName".getBytes(UTF_8);

// 首字节分派
switch (firstChar) {
    case 'u' -> compareWithUsers();  // user*, userName*
    case 'a' -> compareWithAccounts();  // account*, age*
    // ...
}
```

### 性能优势

| 方法 | 比较次数 | 分支数 |
|------|----------|--------|
| String.equals() | 逐字符 | N |
| Byte[] + firstByte | 逐字节 | 1 + N/8 |

## 策略 2：哈希匹配

### 三种哈希策略

| 策略 | 公式 | 适用场景 |
|------|------|----------|
| PLHV | `hash += byte` | 默认，加法快速 |
| BIHV | `hash = hash*31 + byte` | 碰撞时切换 |
| PRHV | `hash = hash*31 + byte` | 再碰撞时切换 |

### 零碰撞保证

```java
// 自动选择策略直到零碰撞
public FieldNameMatcher(String[] fieldNames) {
    // 尝试 PLHV
    hashInfo = computeHashPLHV(fieldNames);
    if (hasCollision(hashInfo)) {
        // 尝试 BIHV
        hashInfo = computeHashBIHV(fieldNames);
        if (hasCollision(hashInfo)) {
            // 尝试 PRHV
            hashInfo = computeHashPRHV(fieldNames);
        }
    }
}
```

### 哈希计算

```java
// PLHV: Polynomial with Linear Variable
long hash = 0;
for (byte b : bytes) {
    hash += b;  // 简单加法
}

// BIHV: Bernstein with Integer Variable
long hash = 0;
for (byte b : bytes) {
    hash = hash * 31 + b;  // 31 是魔数
}

// PRHV: Prime with Random Variable
long hash = 0;
for (byte b : bytes) {
    hash = hash * 31 + b;  // 质数乘法
}
```

## 匹配流程

```java
public int matchFieldName(JSONParser parser) {
    // 1. 尝试字节比较（快速路径）
    if (byteComparisonMatch(parser)) {
        return matchedIndex;
    }

    // 2. 回退到哈希匹配
    return hashBasedMatch(parser);
}
```

## 性能对比

| 场景 | 字节比较 | 哈希匹配 |
|------|----------|----------|
| 纯 ASCII 字段名 | 99% 命中 | 1% 回退 |
| 含转义字段名 | 回退 | 100% |
| Unicode 字段名 | 回退 | 100% |

## 你可以学到什么

1. **两级策略** - 快速路径 + 回退路径
2. **首字节分派** - 减少比较次数
3. **哈希策略** - 自动选择避免碰撞
4. **零碰撞** - 保证正确性的前提下追求性能

## 相关文档

- [SWAR 并行处理](../optimization/swar.md)
- [字段处理架构](field-handling.md)

[← 返回索引](README.md)

# digit2 优化：2位数字并行解析

## 概述

`digit2` 优化借鉴自 fastjson2，使用 SWAR (SIMD Within A Register) 技术一次并行解析 2 位十进制数字，减少循环迭代次数。

## 实现原理

### SWAR 技术解释

```java
static int digit2(byte[] bytes, int off) {
    // 1. 将两个字节组合成一个 int (大端序)
    int x = ((bytes[off] & 0xFF) << 8) | (bytes[off + 1] & 0xFF);

    // 2. 提取低4位（每个字节的数字部分）
    int d = x & 0x0F0F;

    // 3. SWAR 验证：检查两个字节是否都是 '0'-'9'
    //    - (x & 0xF0F0) 提取高4位
    //    - (x & 0xF0F0) - 0x3030 检查高4位是否为 0x30 ('0' 的高4位)
    //    - (d + 0x0606) & 0xF0F0 检查低4位是否 <= 9
    if ((((x & 0xF0F0) - 0x3030) | ((d + 0x0606) & 0xF0F0)) != 0) {
        return -1;  // 不是有效数字
    }

    // 4. 计算数值: (d >> 8) 是第一个数字（十位），(d & 0xF) 是第二个数字（个位）
    return (d >> 8) * 10 + (d & 0xF);
}
```

### 为什么有效

1. **减少循环次数**: 对于 8 位数字，从 8 次循环减少到 4 次
2. **减少分支**: 一次验证 2 个字符，减少条件判断
3. **CPU 流水线友好**: 并行操作更利于 CPU 指令流水线

## 在 fastjson3 中的使用

### JSONParser.UTF8.readIntDirect()

```java
public int readIntDirect() {
    // ... 初始化代码 ...

    // 使用 digit2 每次处理 2 位数字
    while (off + 1 < e) {
        int d2 = digit2(b, off);
        if (d2 < 0) break;
        // 溢出检查：处理边界值 (Integer.MAX_VALUE = 2147483647, Integer.MIN_VALUE = -2147483648)
        if (value > 21474836 || (value == 21474836 && d2 > (neg ? 48 : 47))) break;
        value = value * 100 + d2;
        off += 2;
    }

    // 处理剩余的单个数字（如果有奇数位）
    while (off < e) {
        c = b[off] & 0xFF;
        if (c < '0' || c > '9') break;
        int digit = c - '0';
        // 溢出检查：处理边界值
        if (value > 214748364 || (value == 214748364 && digit > (neg ? 8 : 7))) break;
        value = value * 10 + digit;
        off++;
    }

    return neg ? -value : value;
}
```

## 性能特点

### 最适合的场景

- **多位整数**: 4 位及以上的整数
- **时间戳**: 如 1234567890
- **ID 值**: 如 1000000, 98765432

### 效果

| 数字位数 | 传统迭代次数 | digit2 迭代次数 | 减少比例 |
|---------|-------------|----------------|---------|
| 2 位 | 2 | 1 | 50% |
| 4 位 | 4 | 2 | 50% |
| 6 位 | 6 | 3 | 50% |
| 8 位 | 8 | 4 | 50% |

## 正确性验证

运行验证测试：
```bash
java -cp "core3/target/classes:core3/target/test-classes:..." \
    com.alibaba.fastjson3.benchmark.Digit2VerificationTest
```

预期输出：
```
✓ ALL TESTS PASSED
```

## 性能测试

运行性能测试：
```bash
java -cp "core3/target/classes:core3/target/test-classes:..." \
    com.alibaba.fastjson3.benchmark.Digit2BeforeAfterBenchmark
```

## 与 fastjson2 的差异

### fastjson2 的实现
- 使用 `UNSAFE.getShort()` 直接读取 short
- 使用负数累积方式 (`result = result * 100 - d`)
- 更适合大端序/小端序处理

### fastjson3 的实现
- 使用字节组合方式
- 使用正数累积方式 (`value = value * 100 + d2`)
- 更简单，可读性更好

## 参考资料

- fastjson2: `com.alibaba.fastjson2.util.IOUtils.digit2()`
- SWAR 技术: SIMD Within A Register - 在单个寄存器中执行并行操作

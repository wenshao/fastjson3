# 字面量 int 匹配优化

**性能提升**：~30% | **难度**：⭐ | **适用场景**：固定字符串字面量比较

## 问题描述

比较 JSON 布尔字面量时，传统方式逐字节比较：

```java
// ❌ 逐字节比较：4次比较 + 3次逻辑与
if (b[off] == 't' && b[off + 1] == 'r' && b[off + 2] == 'u' && b[off + 3] == 'e') {
    return true;
}
```

**开销**：多次分支判断，CPU 分支预测失效时性能下降。

## 解决方案：int 单次比较

将 4 字节字符串编码为单个 int，实现单次比较：

```java
// ✅ 预编码字面量为 int
private static final int LITERAL_TRUE = ('t' << 24) | ('r' << 16) | ('u' << 8) | 'e';
private static final int LITERAL_FALSE4 = ('f' << 24) | ('a' << 16) | ('l' << 8) | 's';

// 运行时：提取 4 字节为 int，单次比较
int word = (b[off] & 0xFF) << 24 | (b[off + 1] & 0xFF) << 16 | (b[off + 2] & 0xFF) << 8 | (b[off + 3] & 0xFF);
if (word == LITERAL_TRUE) {
    return true;
}
// "false" 先比较前4字节，再验证第5字节
if (off + 5 <= e && word == LITERAL_FALSE4 && b[off + 4] == 'e') {
    return false;
}
```

**优势**：单次 int 比较，无分支。

## 性能对比

| 方法 | 操作 | 性能 (ns/op) | 相对差异 |
|------|------|--------------|----------|
| 逐字节比较 | 4次char比较 + 3次&& | 2.27 | 基准 |
| int 比较 | 移位/OR + 1次int比较 | 1.55 | **+32%** |

## 技术细节

### 1. 字节编码

```java
// "true" = 0x74 0x72 0x75 0x65
// 手动构建大端序 int：
't' << 24       // 0x74000000
| 'r' << 16     // 0x00720000
| 'u' << 8      // 0x00007500
| 'e'           // 0x00000065
= 0x74727565    // LITERAL_TRUE
```

### 2. 字节提取

```java
int word = (b[off] & 0xFF) << 24     // byte → unsigned int
         | (b[off+1] & 0xFF) << 16
         | (b[off+2] & 0xFF) << 8
         | (b[off+3] & 0xFF);
```

**为什么 & 0xFF？** Java byte 是有符号的（-128~127），需要转换为无符号值（0~255）后再移位。

### 3. 为什么不用 Unsafe.getInt？

| 方面 | 手动移位 | Unsafe.getInt |
|------|----------|---------------|
| 性能 | **更快** (0.015 ns/op) | 更慢 (0.020 ns/op) |
| 字节序 | 固定大端序 | 平台依赖，需转换 |
| 可移植性 | 纯 Java | 内部 API |
| JIT 优化 | 完全优化 | 有额外开销 |

**结论**：手动移位方案已是最优。

## 完整示例

```java
public boolean readBooleanDirect() {
    final byte[] b = this.bytes;
    int off = this.offset;
    final int e = this.end;

    if (off + 4 <= e) {
        int word = (b[off] & 0xFF) << 24
                 | (b[off + 1] & 0xFF) << 16
                 | (b[off + 2] & 0xFF) << 8
                 | (b[off + 3] & 0xFF);

        if (word == LITERAL_TRUE) {
            this.offset = off + 4;
            return true;
        }
        if (off + 5 <= e && word == LITERAL_FALSE4 && b[off + 4] == 'e') {
            this.offset = off + 5;
            return false;
        }
    }
    throw new JSONException("expected 'true' or 'false'");
}
```

## 适用场景

✅ **适合**：
- 固定长度的短字符串（2-8 字节）
- 热路径上的字面量匹配
- JSON 关键字（true/false/null）

❌ **不适合**：
- 变长字符串
- 长字符串（>8 字节）
- 非关键路径

## 扩展应用

```java
// 其他 JSON 字面量也可以用此技巧
private static final int LITERAL_NULL = ('n' << 24) | ('u' << 16) | ('l' << 8) | 'l';

// 数字快速识别
private static final int DIGITS_0123 = ('0' << 24) | ('1' << 16) | ('2' << 8) | '3';
```

## 参考资料

- [fastjson2 源码](https://github.com/alibaba/fastjson2) - 原始实现
- [分支预测优化](https://stackoverflow.com/questions/11227809/why-is-it-faster-to-process-a-sorted-array-than-an-unsorted-array)

## 相关优化

- [位掩码优化](whitespace-bitmask-optimization.md) - 类似的位操作技巧
- [SWAR 并行处理](swar.md) - 更宽的 SIMD 处理

[← 返回索引](README.md)

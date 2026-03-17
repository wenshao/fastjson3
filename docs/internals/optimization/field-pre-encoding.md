# 字段预编码

**性能提升**：~5% | **难度**：⭐⭐ | **适用场景**：固定格式

## 问题描述

序列化时重复写入相同的字段名：

```java
// ❌ 每次都要写入 "user_name"
gen.writeFieldName("user_name");  // 写 'u','s','e','r','_','n','a','m','e'
gen.writeValue(user.name);
```

每次调用 `writeFieldName` 都需要：
1. 字符串转 UTF-8 字节
2. 逐字节写入缓冲区

## 解决方案：预编码为 long[]

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

## 性能对比

| 方法 | 调用次数 | 性能 |
|------|----------|------|
| 逐字节写入 | N 次 | 基准 |
| 预编码 long[] | N/8 次 | **~30%** 提升 |

## 工作原理

### 预编码过程

```java
// 原始字段名
String fieldName = "userName";

// 编码为 JSON 字段名格式
String jsonFieldName = "\"userName\":";  // "userName":

// 转换为 UTF-8 字节
byte[] bytes = jsonFieldName.getBytes(StandardCharsets.UTF_8);
// [34, 117, 115, 101, 114, 78, 97, 109, 101, 58]
//  ["  u    s    e    r    N    a    m    e    :]

// 转换为 long[]（每 8 字节一个 long）
long[] longs = new long[2];
longs[0] = bytesToLong(bytes, 0);  // 前 8 字节
longs[1] = bytesToLong(bytes, 8);  // 后 2 字节 + padding
```

### 写入过程

```java
// 预编码的数据
static final long[] NAME_USER_NAME = {
    0x22757365724e616dL,  // "userNam
    0x65223a0000000000L,  // e":\0\0\0\0\0\0
};
static final int NAME_USER_NAME_LEN = 11;  // 实际字节数

// 写入时
writeNameLongs(NAME_USER_NAME, NAME_USER_NAME_LEN);
// 1. putLong(buf, pos, 0x22757365724e616d)
// 2. putLong(buf, pos + 8, 0x65223a0000000000)
// 3. count += 11 (不是 16!)
```

## 完整实现

```java
public class FieldWriter {
    private final long[] nameLongs;  // 预编码的字段名
    private final int nameBytes;     // 实际字节数

    public FieldWriter(String fieldName) {
        this.nameLongs = encodeFieldName(fieldName);
        this.nameBytes = ("\"" + fieldName + "\":").getBytes(StandardCharsets.UTF_8).length;
    }

    // 编码字段名为 long[]
    private static long[] encodeFieldName(String name) {
        byte[] bytes = ("\"" + name + "\":").getBytes(StandardCharsets.UTF_8);
        long[] longs = new long[(bytes.length + 7) / 8];
        for (int i = 0; i < longs.length; i++) {
            longs[i] = bytesToLong(bytes, i * 8);
        }
        return longs;
    }

    private static long bytesToLong(byte[] bytes, int offset) {
        long result = 0;
        for (int i = 0; i < 8 && offset + i < bytes.length; i++) {
            result |= (bytes[offset + i] & 0xFFL) << (i * 8);
        }
        return result;
    }

    // 写入字段名
    public void writeFieldName(JSONGenerator gen) {
        gen.writeNameLongs(nameLongs, nameBytes);
    }
}
```

## 内存对齐处理

### 问题

字段名长度可能不是 8 的倍数：

```
"userName": = 11 字节
"userAge": = 10 字节
```

### 解决方案

```java
// 预编码时填充
long[] encodeFieldName(String name) {
    byte[] bytes = ("\"" + name + "\":").getBytes(StandardCharsets.UTF_8);
    int longCount = (bytes.length + 7) / 8;
    long[] longs = new long[longCount];

    // 最后一个 long 只需要有效字节
    for (int i = 0; i < longCount; i++) {
        longs[i] = 0;
        for (int j = 0; j < 8; j++) {
            int pos = i * 8 + j;
            if (pos < bytes.length) {
                longs[i] |= (bytes[pos] & 0xFFL) << (j * 8);
            }
        }
    }
    return longs;
}

// 写入时使用实际字节数
void writeNameLongs(long[] longs, int actualBytes) {
    int fullLongs = actualBytes / 8;
    for (int i = 0; i < longs.length; i++) {
        UNSAFE.putLong(buf, pos, longs[i]);
        pos += 8;
    }
    count += actualBytes;  // 使用实际长度，不是填充后的长度
}
```

## 性能分析

### 为什么快？

1. **减少 UTF-8 编码**：类加载时编码一次
2. **减少调用次数**：8 字节一次 putLong
3. **CPU 缓存友好**：连续的 long 读写

### 适用场景

| 场景 | 适用性 | 说明 |
|------|--------|------|
| 固定字段名 | ✅ 最佳 | 预编码一次，无限复用 |
| 动态字段名 | ❌ 不适用 | 每次都要编码 |
| 大量重复写入 | ✅ 适用 | 摊销预编码成本 |

## 应用示例

### 示例 1：HTTP 头预编码

```java
public class HttpHeaders {
    // 预编码常用 HTTP 头
    private static final long[] CONTENT_TYPE = encode("Content-Type: ");
    private static final long[] CONTENT_LENGTH = encode("Content-Length: ");
    private static final long[] SERVER = encode("Server: ");

    public static void writeContentType(byte[] buf, int pos, String value) {
        writeLongs(buf, pos, CONTENT_TYPE);
        pos += CONTENT_TYPE.length;
        writeString(buf, pos, value);
    }
}
```

### 示例 2：协议消息预编码

```java
public class MessageProtocol {
    // 消息格式：{"type":"XXX","data":...}
    private static final long[] TYPE_PREFIX = encode("\"type\":\"");
    private static final long[] TYPE_SUFFIX = encode("\",\"data\":");
    private static final long[] END_BRACE = encode("}");

    public void writeMessage(String type, Object data) {
        writeLongs(TYPE_PREFIX);
        writeString(type);
        writeLongs(TYPE_SUFFIX);
        writeValue(data);
        writeLongs(END_BRACE);
    }
}
```

## 你可以学到什么

1. **预计算常量** - 固定内容预先编码
2. **批量写入** - 减少调用次数
3. **内存对齐** - long 写入需要 8 字节对齐
4. **按实际长度计数** - 填充不影响结果长度

## 参考资料

- [Java 内存模型](https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html)
- [Long 数组优化](https://shipilev.net/blog/2014/06/arrays-of-wisdom/)

## 相关优化

- [转移计算](shift-computation.md) - 预编码是转移计算的典型应用
- [Unsafe 直接操作](unsafe-memory.md) - putLong 的基础

[← 返回索引](README.md)

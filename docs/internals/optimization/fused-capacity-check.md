# 融合容量检查

**性能提升**：~3% | **难度**：⭐⭐ | **适用场景**：缓冲区操作

## 问题描述

每个字段分别检查容量：

```java
// ❌ 两次检查
writeFieldName(name);  // ensureCapacity
writeIntValue(value);  // ensureCapacity
```

**问题**：多次检查浪费 CPU 周期。

## 解决方案：合并检查

```java
// ✅ 一次检查
void writeNameInt(long[] nameByteLongs, int nameBytesLen, int value) {
    ensureCapacity(nameBytesLen + 12);  // name + int(最多11位) + comma(1)
    writeNameLongs(nameByteLongs, nameBytesLen);
    writeInt(value);
    buf[count++] = ',';
}
```

## 性能分析

### 传统方式

```java
// 每次 writeFieldName 都检查
public void writeFieldName(String name) {
    byte[] bytes = ("\"" + name + "\":").getBytes();
    ensureCapacity(bytes.length);  // 检查 1
    write(bytes);
}

public void writeInt(int value) {
    ensureCapacity(11);  // 检查 2
    writeIntImpl(value);
}

// 调用 writeFieldName + writeInt = 2 次检查
```

### 融合方式

```java
// 一次检查，多次写入
public void writeField(String name, int value) {
    int nameBytesLen = name.length() + 3;  // "" + name + :
    int intLen = 11;  // 最多 11 位
    int commaLen = 1;

    ensureCapacity(nameBytesLen + intLen + commaLen);  // 只检查 1 次

    writeNameEncoded(name);  // 预编码字段名
    writeInt(value);
    buf[count++] = ',';
}
```

## 工作原理

### 容量计算

```java
// 精确计算需要的容量
int requiredCapacity = pos + nameBytesLen + valueLen + comma;

// 一次性扩容
if (requiredCapacity > buf.length) {
    expandCapacity(requiredCapacity);
}

// 后续写入无需检查
writeNameLongs(nameByteLongs, nameBytesLen);
writeInt(value);
buf[count++] = ',';
```

### 最大值优化

```java
// 使用最大值避免动态计算
void writeStringField(String name, String value) {
    // name 最大长度 + 字符串最大长度（假设 256）
    ensureCapacity(256);  // 一次性分配足够空间

    writeNameEncoded(name);
    writeString(value);
}

// 优点：一次检查
// 缺点：可能分配过多内存
```

## 完整实现

```java
public class JSONGenerator {
    private byte[] buf;
    private int count;

    // 融合写入：字段名 + 字符串值
    public void writeStringField(long[] nameByteLongs, int nameBytesLen, String value) {
        int valueLen = value.length() * 3;  // UTF-8 最坏情况：1 字符 = 3 字节
        ensureCapacity(count + nameBytesLen + valueLen + 3);  // +3 = "" + ,

        // 写入字段名
        writeNameLongs(nameByteLongs, nameBytesLen);

        // 写入字符串值
        writeString(value);

        // 写入逗号
        buf[count++] = ',';
    }

    // 融合写入：字段名 + 整数值
    public void writeIntField(long[] nameByteLongs, int nameBytesLen, int value) {
        // int 最大 11 位（-2147483648）
        ensureCapacity(count + nameBytesLen + 11 + 1);

        writeNameLongs(nameByteLongs, nameBytesLen);
        writeInt(value);
        buf[count++] = ',';
    }

    // 融合写入：字段名 + 长整数值
    public void writeLongField(long[] nameByteLongs, int nameBytesLen, long value) {
        // long 最大 20 位
        ensureCapacity(count + nameBytesLen + 20 + 1);

        writeNameLongs(nameByteLongs, nameBytesLen);
        writeLong(value);
        buf[count++] = ',';
    }
}
```

## 批量写入

### 场景 1：对象序列化

```java
public void writeObject(Object obj) {
    // 计算总容量（预估值）
    int estimated = estimateSize(obj);
    ensureCapacity(estimated);

    // 逐个写入字段，无需每次检查
    for (FieldWriter writer : fieldWriters) {
        writer.write(this, obj);  // 内部不再检查
    }
}
```

### 场景 2：数组序列化

```java
public void writeArray(Object[] array) {
    // 估算：每个对象平均 100 字节
    int estimated = array.length * 100;
    ensureCapacity(estimated);

    buf[count++] = '[';
    for (int i = 0; i < array.length; i++) {
        if (i > 0) buf[count++] = ',';
        writeValue(array[i]);  // 内部不再检查
    }
    buf[count++] = ']';
}
```

## 你可以学到什么

1. **合并检查** - 一次检查，多次写入
2. **预估容量** - 提前计算总需求
3. **批量操作** - 减少检查次数
4. **权衡** - 精确计算 vs 最大值

## 参考资料

- [Buffer 扩容策略](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/nio/ByteBuffer.java)

## 相关优化

- [字段预编码](field-pre-encoding.md) - 常结合融合检查
- [转移计算](shift-computation.md) - CodeGen 时计算容量

[← 返回索引](README.md)

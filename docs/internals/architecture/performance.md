# 性能优化架构

fastjson3 的分层优化策略。

## 优化层次

```
应用层
    │
    ├─ 复用 ObjectMapper
    ├─ 使用 byte[] 而非 String
    └─ 启用 ASM
    │
框架层
    │
    ├─ sealed class (JIT 优化)
    ├─ Type Tag 分派 (避免 megamorphic)
    ├─ 字段名预编码
    └─ 缓冲区池化
    │
实现层
    │
    ├─ SWAR (noEscape8)
    ├─ Unsafe 直接操作
    ├─ 单遍检查复制
    ├─ 融合容量检查
    └─ putLong 批量写入
```

## 关键技术

### 1. SWAR - 8 字节并行转义检测

**提升**：~10%

```java
// 一次检查 8 个字节
static boolean noEscape8(long v) {
    long hiMask = 0x8080808080808080L;
    long notBackslash = (v ^ 0xA3A3A3A3A3A3A3A3A3L) + 0x01 & hiMask;
    return (notBackslash & v + 0x5D5D5D5D5D5D5D5DL) == hiMask;
}
```

### 2. ASM - 运行时字节码生成

**提升**：~7% (Read)

```java
// 生成的类直接访问字段，无反射
public class UserWriter {
    public void write(JSONGenerator gen, Object object) {
        User user = (User) object;
        gen.writeString(user.name);  // getfield，可内联
    }
}
```

### 3. Unsafe - 绕过边界检查

**提升**：~5%

```java
// 直接内存访问，无边界检查
long value = UNSAFE.getLong(byteArray, BYTE_ARRAY_BASE_OFFSET + offset);
```

### 4. 预编码 - 字段名 long[]

**提升**：~5%

```java
// 类加载时编码
static final long[] NAME_USER_NAME = encodeFieldName("userName");

// 运行时直接写入
gen.writeNameLongs(NAME_USER_NAME, NAME_LEN);
```

### 5. 池化 - 线程本地缓冲区

**提升**：~3%

```java
static final ThreadLocal<byte[]> BUFFER_POOL =
    ThreadLocal.withInitial(() -> new byte[8192]);
```

## 总体性能提升

| 基准 | fastjson2 | fastjson3 | 提升 |
|------|-----------|-----------|------|
| UsersWriteUTF8 | 100% | 136% | **+36%** |

## 优化设计原则

1. **减少分支** - SWAR、Type Tag
2. **减少遍历** - 单遍检查复制
3. **减少调用** - 字段预编码、融合检查
4. **避免反射** - ASM 字节码生成
5. **内存复用** - 缓冲区池化
6. **快速路径** - 常见情况特殊处理
7. **转移计算** - 将计算从运行时转移到定义/CodeGen 阶段

## 应用层优化

### 复用 ObjectMapper

```java
// ❌ 每次创建
for (int i = 0; i < 10000; i++) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(obj);
}

// ✅ 复用共享实例
ObjectMapper mapper = ObjectMapper.shared();
for (int i = 0; i < 10000; i++) {
    mapper.writeValue(obj);
}
```

### 使用 byte[]

```java
// ❌ String 中间格式
String json = mapper.writeValueAsString(obj);
byte[] bytes = json.getBytes(UTF_8);  // 二次编码

// ✅ 直接 byte[]
byte[] bytes = mapper.writeValueAsBytes(obj);
```

## 相关文档

- [性能优化技术详解](../optimization/)
- [性能调优指南](../../guides/performance.md)

[← 返回索引](README.md)

# 解析器架构

fastjson3 的 JSON 解析和生成组件。

## JSONParser

### sealed class 层次结构

```
sealed JSONParser
├── JSONParser.Str         (String 输入，非 Latin1 fallback)
├── JSONParser.UTF8        (byte[] 输入，bean 解析主热路径)
│   └── JSONParser.LATIN1  (Latin1 零拷贝，tree 解析 + String 输入主热路径)
└── JSONParser.CharArray   (char[] 输入)
```

### LATIN1 parser

对 JDK 9+ 的 Latin1 compact string（绝大多数 ASCII JSON），`JSONParser.of(String)` 自动创建 LATIN1 parser：

- **零拷贝**：通过 Unsafe 直接获取 String 内部 byte[]，避免 `charAt()` 虚拟调用和 `getBytes(UTF_8)` 拷贝
- **SWAR 字符串扫描**：8 字节并行查找引号/反斜杠（无 high-bit 检查，Latin1 所有字节均为合法单字符）
- **内联 readObject/readArray**：tree 解析完全内联，无 `readAny()` 虚方法调度
- **readNumber 直接解析**：整数直接从 byte[] 解析，2-digit 批量处理，无 String 分配
- **NameCache**：静态字段名缓存（hash\*31），重复字段名复用 String 实例

LATIN1 extends UTF8，继承所有 bean 解析方法（readFieldNameMatch、readIntOff 等），只 override 字符串相关方法。参考 fastjson2 的 `JSONReaderASCII`。

```java
// String 输入 — 自动选择 LATIN1（零拷贝）
JSONObject obj = JSON.parseObject(jsonString);

// byte[] tree 解析 — ASCII 时通过 ObjectMapper 路由到 LATIN1
JSONObject obj = JSON.parseObject(utf8Bytes);

// byte[] bean 解析 — 仍用 UTF8（ASM ObjectReader 特化）
MediaContent mc = JSON.parseObject(bytes, MediaContent.class);
```

### JSONObjectMap

`JSONObject` 默认使用 `JSONObjectMap`（flat parallel arrays）替代 `LinkedHashMap` 作为内部存储：

- **无 Entry 节点分配**：String[] keys + Object[] values 平行数组
- **线性扫描**：小 map（≤16 条目）直接扫描，大 map 按需构建 hash index
- **保持插入顺序**：数组天然有序
- **可配置**：`JSONObject.setMapCreator(Supplier)` 切回 LinkedHashMap 模式

### 为什么使用 sealed？

#### 1. JIT 优化

sealed class 允许 JIT 编译器做 **devirtualization**：

```java
// 编译时已知所有子类
sealed class JSONParser permits JSONParser.UTF8, JSONParser.Str {
    public abstract int getCurrent();
}

// JIT 可以将虚方法调用优化为直接调用
JSONParser parser = new JSONParser.UTF8(bytes);
int ch = parser.getCurrent();  // 无虚表查找
```

#### 2. 类型安全

编译时检查所有子类：

```java
// ✅ 编译错误：忘记列出子类
sealed class JSONParser permits JSONParser.UTF8 {
    // JSONParser.Str 不在 permits 列表中
}

// ✅ 正确：列出所有子类
sealed class JSONParser permits JSONParser.UTF8, JSONParser.Str {
    // ...
}
```

### 性能提升

| 操作 | 普通 class | sealed class | 提升 |
|------|-----------|--------------|------|
| 虚方法调用 | 需要查虚表 | 可直接调用 | 5-10% |
| 内联 | 不确定 | 可内联 | 10-20% |

## JSONGenerator

### sealed class 层次结构

```
sealed JSONGenerator
├── JSONGenerator.Char     (char[] 缓冲区，String 输出)
└── JSONGenerator.UTF8     (byte[] 缓冲区，byte[] 输出，主热路径)
```

### 热路径优化

```java
// UTF8Generator 是最常用的（99%+）
final class JSONGenerator.UTF8 extends JSONGenerator {
    // 专门优化 UTF-8 编码
    public void writeString(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        // 直接写入 UTF-8 字节
        System.arraycopy(bytes, 0, buf, count, bytes.length);
        count += bytes.length;
    }
}
```

## 解析流程

### UTF8Parser 主流程

```
输入: byte[] json
  ↓
跳过 BOM (如果有)
  ↓
逐字节解析
  ├─ '{' → 进入对象模式
  ├─ '[' → 进入数组模式
  ├─ '"' → 解析字符串
  ├─ '0'-'9'/'-' → 解析数字
  └─ 't'/'f'/'n' → 解析字面量 (true/false/null)
  ↓
跳过空白
  ↓
返回: JSONObject / JSONArray / String / Number / Boolean / null
```

## 性能优化

### 1. 单字节读取

```java
// 使用 Unsafe 直接读取，无边界检查
long word = UNSAFE.getLong(byteArray, BYTE_ARRAY_BASE_OFFSET + offset);
```

### 2. 快速路径

```java
// 大多数 JSON 是简单结构
if (ch == '{') {
    // 对象解析的快速路径
    return parseObject();
}
```

### 3. 位掩码空白字符检测

使用位运算代替数组查找，更快的空白字符检测：

```java
// 使用 long 作为位掩码，占用 8 字节（vs 数组的 256 字节）
static final long SPACE = 1L | (1L << ' ') | (1L << '\n') | (1L << '\r') | (1L << '\f') | (1L << '\t') | (1L << '\b');

// 快速空白字符检查：先筛选再位运算
static boolean isWhitespace(int ch) {
    return ch <= ' ' && ((1L << ch) & SPACE) != 0;
}
```

优势：
- **内存占用小**：8 bytes vs 256 bytes，CPU 缓存更友好
- **CPU 指令少**：位运算 vs 数组访问+边界检查
- **分支预测友好**：`ch <= ' '` 先过滤大多数非空白字符

## 相关文档

- [性能优化技术](../optimization/)
- [字段名匹配](field-matcher.md)

[← 返回索引](README.md)

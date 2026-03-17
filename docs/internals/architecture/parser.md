# 解析器架构

fastjson3 的 JSON 解析和生成组件。

## JSONParser

### sealed class 层次结构

```
sealed JSONParser
├── JSONParser.Str         (String 输入)
├── JSONParser.UTF8        (byte[] 输入，主热路径)
└── JSONParser.CharArray   (char[] 输入)
```

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

### 3. 空白字符检测

当前实现使用 boolean 数组查找表：

```java
static final boolean[] WHITESPACE = new boolean[256];
static {
    WHITESPACE[' ']  = true;
    WHITESPACE['\t'] = true;
    WHITESPACE['\n'] = true;
    WHITESPACE['\r'] = true;
}

// skipWhitespace 中使用：
if (c > ' ' || !WHITESPACE[c]) break;
```

潜在改进方向（尚未实现）：使用 long 位掩码替代数组查找，
可以减少内存占用（8 bytes vs 256 bytes），提升缓存局部性：

```java
// 未来可考虑：位掩码方案
static final long SPACE = 1L | (1L << ' ') | (1L << '\n') | (1L << '\r') | (1L << '\f') | (1L << '\t') | (1L << '\b');
static boolean isWhitespace(int ch) {
    return ch <= ' ' && ((1L << ch) & SPACE) != 0;
}
```

## 相关文档

- [性能优化技术](../optimization/)
- [字段名匹配](field-matcher.md)

[← 返回索引](README.md)

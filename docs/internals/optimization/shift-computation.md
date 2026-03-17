# 转移计算（Shift Computation）

**性能提升**：~30-50% | **难度**：⭐⭐⭐ | **适用场景**：通用优化

## 核心思想

**将计算从运行时转移到定义阶段（包括 CodeGen 阶段）**

```
运行时计算（慢） → 定义阶段计算（快） → CodeGen 阶段计算（最快）
```

## 问题示例

### 运行时每次计算

```java
// ❌ 每次序列化都要计算
public void writeUser(JSONGenerator gen, User user) {
    gen.writeFieldName("userName");        // 每次写入字符串
    gen.writeString(user.getUserName());

    gen.writeFieldName("userAge");         // 每次写入字符串
    gen.writeInt(user.getUserAge());

    gen.writeFieldName("userEmail");       // 每次写入字符串
    gen.writeString(user.getUserEmail());
}
```

**开销**：
- 字段名字符串每次都写入
- 字段类型判断每次都执行
- 格式化模式每次都解析

## 解决方案层次

### 层次 1：运行时计算（最慢）

```java
// 每次调用都计算
gen.writeFieldName("user_name");  // 字符串 → UTF-8 → write
```

### 层次 2：类加载时计算（快）

```java
// 类加载时编码一次
static final long[] NAME_USER_NAME = encodeFieldName("userName");
static final int NAME_USER_NAME_LEN = "userName\":".length();

// 运行时直接使用
gen.writeNameLongs(NAME_USER_NAME, NAME_USER_NAME_LEN);
```

### 层次 3：代码生成时（最快）

```java
// ASM 生成专用 Writer 类
public final class UserWriter implements ObjectWriter<User> {
    // 无任何运行时计算
    public void write(JSONGenerator gen, Object object) {
        User user = (User) object;

        // 硬编码的字段名（编译时确定）
        gen.writeNameLongs(PRE_ENCODED_NAME, 11);
        gen.writeString(user.name);

        gen.writeNameLongs(PRE_ENCODED_AGE, 6);
        gen.writeInt(user.age);
    }
}
```

## 实际应用

### 1. 字段名预编码

#### 运行时（慢）

```java
public void writeField(String fieldName, Object value) {
    byte[] nameBytes = ("\"" + fieldName + "\":").getBytes(UTF_8);
    // 每次：字符串拼接 + UTF-8 编码 + 数组分配
    write(nameBytes);
}
```

#### 类加载时（快）

```java
public class FieldWriter {
    private static final long[] NAME_LONGS;
    private static final int NAME_LEN;

    static {
        NAME_LONGS = encodeFieldName("userName");  // 类加载时执行一次
        NAME_LEN = 11;
    }

    public void writeField(Object bean) {
        gen.writeNameLongs(NAME_LONGS, NAME_LEN);  // 直接使用
    }
}
```

#### CodeGen 时（最快）

```java
// ASM 生成的类中，字段名直接硬编码为 long 常量
public class UserWriter {
    private static final long NAME_USER_NAME = 0x22757365724e616dL;  // "userNam
    private static final long NAME_USER_AGE = 0x2275736572416765L;    // "userAge

    public void write(JSONGenerator gen, User user) {
        // 字段名是编译时常量，零运行时开销
        gen.buf[gen.pos++] = (byte)(NAME_USER_NAME >> 0);
        gen.buf[gen.pos++] = (byte)(NAME_USER_NAME >> 8);
        // ...
    }
}
```

### 2. 类型判断预计算

#### 运行时判断

```java
public void writeField(Object value) {
    if (value instanceof String) {      // 类型检查
        writeString((String) value);
    } else if (value instanceof Integer) {
        writeInt((Integer) value);
    } else if (value instanceof Long) {
        writeLong((Long) value);
    }
    // 每次都要判断类型
}
```

#### CodeGen 专用方法

```java
// 为 String 字段生成的方法
public void write_userName(JSONGenerator gen, Object bean) {
    gen.writeString(((User) bean).name);  // 无类型判断
}

// 为 int 字段生成的方法
public void write_userAge(JSONGenerator gen, Object bean) {
    gen.writeInt(((User) bean).age);  // 无类型判断
}
```

### 3. 格式化模式预解析

#### 运行时解析

```java
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;

// 每次序列化都要解析 "yyyy-MM-dd HH:mm:ss"
```

#### 定义时解析

```java
public class DateFieldWriter {
    // 类加载时解析一次
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void write(Date value) {
        String str = FORMATTER.format(value);  // 直接使用
        writeString(str);
    }
}
```

### 4. JSONPath 预编译

#### 每次编译

```java
for (int i = 0; i < 10000; i++) {
    Object result = JSONPath.of("$.store.book[*].price").eval(json);
    // 每次循环都编译路径
}
```

#### 编译一次，复用多次

```java
JSONPath path = JSONPath.of("$.store.book[*].price");  // 编译一次
for (int i = 0; i < 10000; i++) {
    Object result = path.eval(json);  // 直接求值
}
```

### 5. 反射缓存

#### 每次反射

```java
public void writeField(Object bean, String fieldName) throws Exception {
    Field field = bean.getClass().getDeclaredField(fieldName);  // 每次查找
    field.setAccessible(true);
    Object value = field.get(bean);
    writeValue(value);
}
```

#### 缓存 Field

```java
public class FieldWriter {
    private final Field field;

    public FieldWriter(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        this.field = clazz.getDeclaredField(fieldName);  // 查找一次
        this.field.setAccessible(true);
    }

    public Object getValue(Object bean) throws IllegalAccessException {
        return field.get(bean);  // 直接使用
    }
}
```

## 性能对比

| 计算时机 | 示例 | 相对性能 |
|----------|------|----------|
| 运行时每次 | `writeFieldName("name")` | 100%（基准） |
| 类加载时 | `static final long[] NAME = encode("name")` | **~130%** |
| CodeGen 时 | ASM 生成专用 Writer | **~150%** |

## 设计模式

### 模式 1：静态常量

```java
// 类加载时计算一次，复用多次
public class Constants {
    public static final byte[] HEADER;
    public static final int HEADER_LENGTH;

    static {
        HEADER = ("HTTP/1.1 200 OK\r\n").getBytes(UTF_8);
        HEADER_LENGTH = HEADER.length;
    }
}
```

### 模式 2：初始化缓存

```java
public class ParserCache {
    private static final Map<String, DateTimeFormatter> FORMATTERS = new HashMap<>();

    static {
        FORMATTERS.put("date", DateTimeFormatter.ISO_DATE);
        FORMATTERS.put("time", DateTimeFormatter.ISO_TIME);
        FORMATTERS.put("datetime", DateTimeFormatter.ISO_DATE_TIME);
    }
}
```

### 模式 3：代码生成

```java
// 注解处理器在编译时生成代码
@Generated("UserProcessor")
public class UserWriter {
    // 所有计算都在编译时完成
    // 运行时零开销
}
```

## 应用场景总结

| 场景 | 预计算方式 | 时机 | 性能提升 |
|------|-----------|------|----------|
| 字符串常量 | 预编码为 byte[] | 类加载时 | ~30% |
| 格式化模式 | 预解析为 Formatter | 类加载时 | ~15% |
| 正则表达式 | 预编译为 Pattern | 类加载时 | ~50% |
| 配置文件 | 预解析为对象 | 应用启动时 | ~100% |
| 反射元数据 | 预获取 Field/Method | 类加载时 | ~20% |
| 模板 | 预编译为代码 | 编译时 | ~200% |

## 你可以学到什么

1. **定义阶段计算** - 类加载时计算一次，运行时直接用
2. **代码生成优化** - ASM 生成专用代码，消除分支
3. **预编译复用** - 编译一次，复用多次
4. **常量提取** - 固定内容预计算
5. **分层优化** - 选择合适的计算时机

## 最佳实践

### 1. 识别计算开销

```java
// ❌ 高开销：每次都计算
for (int i = 0; i < 10000; i++) {
    Pattern pattern = Pattern.compile(regex);  // 每次编译！
    Matcher m = pattern.matcher(input);
}

// ✅ 低开销：编译一次
Pattern pattern = Pattern.compile(regex);  // 编译一次
for (int i = 0; i < 10000; i++) {
    Matcher m = pattern.matcher(input);
}
```

### 2. 选择合适的时机

```java
// 运行时 = 太频繁
// 类加载时 = 适中（推荐）
// 编译时 = 最佳（但实现复杂）
```

### 3. 权衡实现复杂度

```
运行时计算 → 类加载时计算 → CodeGen
    简单         中等           复杂
    慢          快             最快
```

## 参考资料

- [编译时元编程](https://www.baeldung.com/java-annotation-processing)
- [Java APT 教程](https://github.com/google/auto/tree/master/service)

## 相关优化

- [字段预编码](field-pre-encoding.md) - 预编码的具体实现
- [ASM 字节码生成](asm-bytecode.md) - CodeGen 的基础

[← 返回索引](README.md)

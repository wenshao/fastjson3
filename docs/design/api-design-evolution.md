# 统一 parse/write API 设计方案 v4 (最终检查版)

## 第五轮审计 - 实现细节检查

---

## 一、线程安全性分析

### 1.1 TypeToken 线程安全

**分析**: TypeToken 是不可变对象，完全线程安全

```java
public final class TypeToken<T> {
    private final Type type;  // final 字段

    // 所有方法都是纯函数，无状态修改
    public Type type() { return type; }
}
```

**结论**: ✅ 线程安全

### 1.2 ParseConfig/WriteConfig 线程安全

**分析**: 枚举是线程安全的，但 `features()` 方法返回数组

```java
public enum ParseConfig {
    LENIENT(new ReadFeature[]{...});

    private final ReadFeature[] features;

    public ReadFeature[] features() {
        return features.clone();  // ✅ 返回防御性拷贝
    }
}
```

**结论**: ✅ 线程安全

### 1.3 JSON 静态方法线程安全

**分析**: 所有方法都是静态的，委托给 `ObjectMapper.shared()`

```java
public static <T> T parse(String json, Class<T> type) {
    return ObjectMapper.shared().readValue(json, type);
}
```

**问题**: `ObjectMapper.shared()` 的线程安全性需要确认

**检查 ObjectMapper.java**:
```java
private static final ObjectMapper SHARED = new ObjectMapper(...);
public static ObjectMapper shared() { return SHARED; }
```

**分析**: ObjectMapper 使用 `ConcurrentHashMap` 作为缓存，是线程安全的

**结论**: ✅ 线程安全

---

## 二、错误处理分析

### 2.1 输入验证

| 输入 | 当前行为 | 应该的行为 |
|------|----------|------------|
| `null` | 返回 `null` | ✅ 正确 |
| `""` | 抛出异常 | ✅ 正确 (无效 JSON) |
| `"null"` | 返回 `null` | ✅ 正确 |
| 无效 JSON | 抛出 `JSONException` | ✅ 正确 |

### 2.2 类型转换错误

**场景**: JSON 字符串 `"hello"` 无法转换为 `Integer`

**期望行为**: 抛出 `JSONException` 并附带详细错误信息

```java
try {
    Integer value = JSON.parse("\"hello\"", Integer.class);
} catch (JSONException e) {
    // 错误消息应包含:
    // - 源类型 (String)
    // - 目标类型 (Integer)
    // - 实际值 ("hello")
    // - 位置信息
}
```

**当前 JSONArray 实现** (已改进):
```java
throw new JSONException("Cannot cast JSONArray[%d] from %s to Integer: %s"
    .formatted(index, val.getClass().getSimpleName(), val));
```

**需要**: 统一所有类型转换的错误消息格式

### 2.3 配置冲突检测

**问题**: 某些配置组合可能冲突

**示例**:
```java
// 这两个特性可能冲突
ReadFeature.ErrorOnUnknownProperties
ReadFeature.SupportAutoType  // 可能引入未知属性
```

**决策**: 暂不检测冲突，让用户自己负责
- 原因: 增加复杂度，某些组合可能有意义
- 文档说明: 在 Javadoc 中警告潜在冲突

---

## 三、边界情况分析

### 3.1 泛型类型擦除

**问题**: TypeToken 无法完全避免类型擦除

```java
// 这样使用时，类型 T 在运行时被擦除
public <T> T parse(String json, Class<T> type) {
    // type 是 Class<T>，T 在运行时不可用
    // 这是 Java 类型擦除的限制，无法完全避免
}
```

**解决方案**: 使用 `Type` 而非 `Class<T>`

```java
public <T> T parse(String json, TypeToken<T> token) {
    Type type = token.type();  // 保留完整类型信息
    ObjectReader<T> reader = getObjectReader(type);
    return reader.readObject(parser, type, null, features);
}
```

### 3.2 原始类型 vs 包装类型

**场景**: 解析为基本类型

```java
int value = JSON.parse("42", int.class);      // 原始类型
Integer value = JSON.parse("42", Integer.class);  // 包装类型
```

**问题**: 原始类型不能为 null

**解决方案**: 现有的 ObjectMapper 已经处理

```java
// 如果 JSON 是 null
JSON.parse("null", int.class);      // 抛出异常? 返回 0?
JSON.parse("null", Integer.class);  // 返回 null
```

**需要**: 明确文档说明

### 3.3 循环引用

**问题**: 对象图中的循环引用

```java
class Node {
    Node next;
}
Node a = new Node();
Node b = new Node();
a.next = b;
b.next = a;  // 循环
JSON.write(a);  // StackOverflowError?
```

**解决方案**: 使用 `WriteFeature.ReferenceDetection`

```java
// 注意: WriteConfig.of() 未实现，实际使用 WriteFeature 直接传入
String json = JSON.toJSONString(a, WriteFeature.ReferenceDetection);
```

### 3.4 大文件处理

**问题**: 解析大文件可能导致内存溢出

**当前**: JSONParser 使用流式处理，内存友好

**新 API 影响**: `parse(String)` 需要整个字符串在内存中

**解决方案**: 保持现有的流式 API (`JSONParser`, `ObjectMapper`)

```java
// 对于大文件，直接使用 ObjectMapper
try (JSONParser parser = JSONParser.of(inputStream)) {
    return parser.read(type);
}
```

---

## 四、性能影响分析

### 4.1 方法调用开销

**新 API**: 每个调用增加一层静态方法委托

```java
public static <T> T parse(String json, Class<T> type) {
    return ObjectMapper.shared().readValue(json, type);
}
```

**影响**: 可忽略 (JVM 内联优化)

### 4.2 TypeToken 创建开销

**分析**: TypeToken 创建涉及反射

```java
TypeToken<List<User>> type = TypeToken.listOf(User.class);
```

**开销**: 每次创建新的 `ParameterizedTypeImpl` 对象

**优化**: 可以缓存常用的 TypeToken

```java
// 用户可以缓存
private static final TypeToken<List<User>> USER_LIST_TYPE =
    TypeToken.listOf(User.class);

// 使用
List<User> users = JSON.parse(json, USER_LIST_TYPE);
```

### 4.3 配置掩码计算

**当前**: 每次调用 `config.mask()` 都重新计算

```java
public long mask() {
    return ReadFeature.of(features);  // 每次遍历 features 数组
}
```

**优化**: 缓存掩码值

```java
public enum ParseConfig {
    LENIENT(new ReadFeature[]{...}) {
        private final long mask = ReadFeature.of(features);
        public long mask() { return mask; }
    };
}
```

---

## 五、与现有代码集成

### 5.1 保持现有方法

**决策**: 不删除任何现有方法，只添加新方法

```java
// 现有方法 (保持)
public static <T> T parseObject(String json, Class<T> type) { ... }

// 新方法
public static <T> T parse(String json, Class<T> type) {
    return parseObject(json, type);  // 委托
}
```

### 5.2 配置兼容性

**现有**: `ReadFeature...` 可变参数
**新增**: `ParseConfig` 枚举
**关系**: `ParseConfig` 内部使用 `ReadFeature`

```java
public enum ParseConfig {
    LENIENT(new ReadFeature[]{...});

    public long mask() {
        return ReadFeature.of(features);
    }
}
```

**兼容**: 完全兼容，可以继续使用 `ReadFeature`

### 5.3 TypeReference 兼容性

**现有**: `TypeReference<T>`
**新增**: `TypeToken<T>`
**关系**: `TypeToken.of(TypeReference)` 桥接

```java
// 现有代码继续工作
TypeReference<List<User>> ref = new TypeReference<List<User>>() {};
List<User> users = JSON.parseObject(json, ref);

// 新代码更简洁
TypeToken<List<User>> token = TypeToken.listOf(User.class);
List<User> users = JSON.parse(json, token);
```

---

## 六、测试策略

### 6.1 单元测试覆盖

| 场景 | 测试用例 |
|------|----------|
| 正常解析 | 各种类型 |
| 空输入 | null, "" |
| 无效 JSON | 语法错误 |
| 类型转换 | 不兼容类型 |
| 配置 | 所有 ParseConfig |
| TypeToken | 各种泛型 |
| 线程安全 | 并发调用 |

### 6.2 兼容性测试

确保新 API 和旧 API 产生相同结果:

```java
String json = "...";

// 旧 API
User user1 = JSON.parseObject(json, User.class);

// 新 API
User user2 = JSON.parse(json, User.class);

// 断言
assertEquals(user1, user2);
```

### 6.3 性能测试

对比新 API 和旧 API 的性能:

```java
@Benchmark
public User oldAPI() {
    return JSON.parseObject(json, User.class);
}

@Benchmark
public User newAPI() {
    return JSON.parse(json, User.class);
}
```

---

## 七、实现检查清单

### 7.1 必须实现

- [ ] `TypeToken` 类
- [ ] `ParseConfig` 枚举
- [ ] `WriteConfig` 枚举
- [ ] `parse(String, Class)` 及重载
- [ ] `parse(String, TypeToken)` 及重载
- [ ] `parseAny(String)` 及重载
- [ ] `parseList(String, Class)`
- [ ] `parseSet(String, Class)`
- [ ] `parseMap(String, Class)` (单类型参数)
- [ ] `parseTypedArray(String, Class)`
- [ ] `write(Object)` 及重载
- [ ] `writePretty(Object)`
- [ ] `writeCompact(Object)`

### 7.2 文档要求

- [ ] 所有新方法的 Javadoc
- [ ] TypeToken 使用示例
- [ ] 配置选项说明
- [ ] 迁移指南

### 7.3 兼容性要求

- [ ] 现有方法保持不变
- [ ] 新旧 API 结果一致
- [ ] 现有单元测试通过

---

## 八、遗留问题

### 8.1 WriteConfig.COMPACT

**问题**: COMPACT 是否真的需要?

**分析**: DEFAULT 配置已经产生紧凑输出

**决策**: 移除 COMPACT，保持简单

```java
// v3 设计 (有 COMPACT)
enum WriteConfig { DEFAULT, PRETTY, COMPACT, ... }

// v4 设计 (移除 COMPACT)
enum WriteConfig { DEFAULT, PRETTY, WITH_NULLS, PRETTY_WITH_NULLS }
```

### 8.2 parseJavaArray 命名

**v3**: `parseTypedArray`
**选项**: `parseArrayTyped`, `parseObjectArray`, `parseJavaArray`

**决策**: 保持 `parseTypedArray`

### 8.3 配置组合

**问题**: 如何组合预设和额外特性?

**选项 A**: 只能用预设或特性，不能混合
**选项 B**: 允许在预设基础上添加特性

**决策**: 暂不支持混合，保持简单
- 如需混合，直接使用 `ReadFeature`

---

## 九、最终 API 摘要

```java
// === 核心 ===
<T> T                  parse(String, Class<T>)
<T> T                  parse(String, Class<T>, ParseConfig)
<T> T                  parse(String, TypeToken<T>)
<T> T                  parse(String, TypeToken<T>, ParseConfig)
<T> T                  parse(byte[], ...)
Object                 parseAny(String)
Object                 parseAny(String, ParseConfig)

// === 容器 ===
JSONObject             parseObject(String)
JSONArray              parseArray(String)

// === 集合 ===
<T> List<T>            parseList(String, Class<T>)
<T> Set<T>             parseSet(String, Class<T>)
<V> Map<String,V>      parseMap(String, Class<V>)
<T> T[]                parseTypedArray(String, Class<T>)

// === 序列化 ===
String                 write(Object)
String                 write(Object, WriteConfig)
byte[]                 writeBytes(Object)
byte[]                 writeBytes(Object, WriteConfig)
String                 writePretty(Object)
String                 writeCompact(Object)  // 待确认

// === 配置 ===
enum ParseConfig        { DEFAULT, LENIENT, STRICT, API }
enum WriteConfig        { DEFAULT, PRETTY, WITH_NULLS, PRETTY_WITH_NULLS }

// === 类型 ===
class TypeToken<T>      { of(...), listOf(...), mapOf(...) }
```

---

## 十、准备实现

所有设计细节已审查完毕，可以开始实现。

**实现顺序**:
1. TypeToken 类 (独立，无依赖)
2. ParseConfig/WriteConfig 枚举 (独立)
3. JSON 类新增方法 (依赖 1 和 2)
4. 单元测试
5. 文档更新

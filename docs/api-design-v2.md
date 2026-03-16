# 统一 parse/write API 设计方案 v2

## 第二轮审计发现及修正

---

## 一、v1 方案问题及修正

### 1.1 TypeToken 设计问题

**v1 问题**:
```java
// 复杂嵌套如何构建?
TypeToken<Map<String, List<User>>> complex =
    TypeToken.of(User.class).asList().asMapOf(User.class); // ❌ 类型错误
```

**修正方案**:
```java
// TypeToken 应该是不可变的类型标记，不是构建器
interface TypeToken<T> {
    Type type();  // 获取实际的 java.lang.reflect.Type

    // 静态工厂方法
    static <T> TypeToken<T> of(Class<T> type) { ... }
    static <T> TypeToken<List<T>> listOf(Class<T> element) { ... }
    static <T> TypeToken<Set<T>> setOf(Class<T> element) { ... }
    static <V> TypeToken<Map<String, V>> mapOf(Class<V> value) { ... }
    static <T> TypeToken<T> of(Type type) { ... }  // 用于 TypeReference
    static <T> TypeToken<T> of(TypeReference<T> ref) { ... }  // 兼容

    // 组合器 (用于复杂嵌套)
    default <R> TypeToken<R> map(Function<TypeToken<T>, TypeToken<R>> mapper) { ... }
}

// 使用示例
TypeToken<List<User>> userList = TypeToken.listOf(User.class);
TypeToken<Map<String, User>> userMap = TypeToken.mapOf(User.class);

// 复杂嵌套: 使用 TypeReference 或 组合
TypeToken<Map<String, List<User>>> complex =
    TypeToken.of(new TypeReference<Map<String, List<User>>>() {});
```

### 1.2 parseObject 保留问题

**v1 问题**: parseObject 是否保留?

**决策**: **保留 parseObject/parseArray 作为便捷方法**

原因:
- `parseObject()` 明确表达返回 JSONObject
- `parse(json, JSONObject.class)` 过于冗长
- 与 `toJSONString()` 对称

修正:
```java
// 保留的便捷方法 (不标记 deprecated)
JSONObject JSON.parseObject(String input)
JSONArray  JSON.parseArray(String input)

// 新的通用方法
<T> T JSON.parse(String input, Class<T> type)
<T> T JSON.parse(String input, TypeToken<T> token)
```

### 1.3 byte[] 输入问题

**v1 问题**: byte[] 是否需要单独方法?

**决策**: **保持单独方法**

原因:
- UTF-8 解析是常见场景 (性能优化路径)
- 方法重载可以处理
- 与当前 Jackson/Gson 兼容

修正:
```java
// String 输入
<T> T JSON.parse(String json, Class<T> type)
<T> T JSON.parse(String json, Class<T> type, ParseConfig config)

// byte[] 输入 (UTF-8)
<T> T JSON.parse(byte[] json, Class<T> type)
<T> T JSON.parse(byte[] json, Class<T> type, ParseConfig config)

// InputStream 输入 (可选，Phase 2)
<T> T JSON.parse(InputStream json, Class<T> type)
```

### 1.4 配置扩展问题

**v1 问题**: 枚举不可扩展

**修正**: **使用 sealed interface + enum 实现**

```java
sealed interface ParseConfig permits ParseConfig.Standard {
    long features();

    enum Standard implements ParseConfig {
        DEFAULT(0),
        LENIENT(ReadFeature.of(
            ReadFeature.AllowComments,
            ReadFeature.AllowSingleQuotes,
            ReadFeature.AllowUnquotedFieldNames,
            ReadFeature.SupportSmartMatch
        )),
        STRICT(ReadFeature.of(
            ReadFeature.ErrorOnUnknownProperties,
            ReadFeature.ErrorOnNullForPrimitives
        )),
        API(ReadFeature.of(
            ReadFeature.ErrorOnUnknownProperties,
            ReadFeature.ErrorOnNullForPrimitives,
            ReadFeature.UseBigDecimalForDoubles
        ));

        final long features;
        Standard(long features) { this.features = features; }
        public long features() { return features; }
    }

    // 自定义配置
    static ParseConfig custom(ReadFeature... features) {
        return new Custom(ReadFeature.of(features));
    }

    record Custom(long features) implements ParseConfig {
        public long features() { return features; }
    }
}
```

---

## 二、最终 API 设计

### 2.1 核心解析方法

```java
class JSON {
    // === 核心方法 ===

    // 解析为指定类型 (String 输入)
    <T> T           parse(String json, Class<T> type)
    <T> T           parse(String json, Class<T> type, ParseConfig config)
    <T> T           parse(String json, TypeToken<T> typeToken)
    <T> T           parse(String json, TypeToken<T> typeToken, ParseConfig config)

    // 解析为指定类型 (byte[] 输入)
    <T> T           parse(byte[] json, Class<T> type)
    <T> T           parse(byte[] json, Class<T> type, ParseConfig config)
    <T> T           parse(byte[] json, TypeToken<T> typeToken)
    <T> T           parse(byte[] json, TypeToken<T> typeToken, ParseConfig config)

    // 自动类型检测
    Object          parseAny(String json)
    Object          parseAny(String json, ParseConfig config)

    // === 便捷方法 ===

    // 解析为 JSON 容器类型
    JSONObject      parseObject(String json)
    JSONObject      parseObject(byte[] json)
    JSONArray       parseArray(String json)
    JSONArray       parseArray(byte[] json)

    // 解析为集合类型 (常用)
    <T> List<T>     parseList(String json, Class<T> element)
    <T> List<T>     parseList(byte[] json, Class<T> element)

    <T> Set<T>      parseSet(String json, Class<T> element)
    <T> Set<T>      parseSet(byte[] json, Class<T> element)

    <K,V> Map<K,V>  parseMap(String json, Class<K> key, Class<V> value)
    <K,V> Map<K,V>  parseMap(byte[] json, Class<K> key, Class<V> value)

    // 解析为数组类型
    <T> T[]         parseJavaArray(String json, Class<T> element)
    <T> T[]         parseJavaArray(byte[] json, Class<T> element)

    // === 兼容方法 (保持) ===

    // TypeReference 支持 (用于复杂泛型)
    <T> T           parse(String json, TypeReference<T> typeRef)
    <T> T           parse(String json, Type type)
    <T> T           parse(String json, Type type, ParseConfig config)
}
```

### 2.2 核心序列化方法

```java
class JSON {
    // === 核心方法 ===

    // 序列化为字符串
    String          write(Object obj)
    String          write(Object obj, WriteConfig config)

    // 序列化为字节 (UTF-8)
    byte[]          writeBytes(Object obj)
    byte[]          writeBytes(Object obj, WriteConfig config)

    // === 便捷方法 ===

    // 带配置的快捷方式
    String          writePretty(Object obj)        // 等同于 write(obj, WriteConfig.PRETTY)
    String          writeCompact(Object obj)       // 等同于 write(obj, WriteConfig.COMPACT)

    // === 兼容方法 (标记 deprecated) ===

    @Deprecated
    String          toJSONString(Object obj)

    @Deprecated
    String          toJSONString(Object obj, WriteFeature... features)

    @Deprecated
    byte[]          toJSONBytes(Object obj)
}
```

### 2.3 验证方法

```java
class JSON {
    // 验证 (保持不变，这些方法已经很好)
    boolean         isValid(String json)
    boolean         isValidObject(String json)
    boolean         isValidArray(String json)
}
```

### 2.4 TypeToken 最终设计

```java
/**
 * 类型标记，用于在运行时保留泛型类型信息。
 *
 * <p>与 TypeReference 相比，TypeToken 提供更简洁的 API
 * 用于常见类型，同时保持对复杂类型的支持。</p>
 *
 * <h3>使用示例:</h3>
 * <pre>
 * // 简单类型
 * TypeToken&lt;User&gt; userType = TypeToken.of(User.class);
 *
 * // 集合类型
 * TypeToken&lt;List&lt;User&gt;&gt; userList = TypeToken.listOf(User.class);
 * TypeToken&lt;Set&lt;User&gt;&gt; userSet = TypeToken.setOf(User.class);
 *
 * // 映射类型 (JSON 对象键总是 String)
 * TypeToken&lt;Map&lt;String, User&gt;&gt; userMap = TypeToken.mapOf(User.class);
 *
 * // 复杂嵌套 - 使用 TypeReference
 * TypeToken&lt;Map&lt;String, List&lt;User&gt;&gt;&gt; complex =
 *     TypeToken.of(new TypeReference&lt;Map&lt;String, List&lt;User&gt;&gt;&gt;() {});
 * </pre>
 */
public final class TypeToken<T> {
    private final Type type;

    private TypeToken(Type type) {
        this.type = type;
    }

    /** 获取实际的 java.lang.reflect.Type */
    public Type type() {
        return type;
    }

    // === 静态工厂方法 ===

    /** 简单类型 */
    public static <T> TypeToken<T> of(Class<T> type) {
        return new TypeToken<>(type);
    }

    /** 任意 Type (用于 TypeReference) */
    public static <T> TypeToken<T> of(Type type) {
        return new TypeToken<>(type);
    }

    /** 从 TypeReference 创建 */
    public static <T> TypeToken<T> of(TypeReference<T> ref) {
        return new TypeToken<>(ref.getType());
    }

    /** List&lt;T&gt; */
    public static <T> TypeToken<List<T>> listOf(Class<T> element) {
        return new TypeToken<>(parameterizedType(List.class, element));
    }

    /** Set&lt;T&gt; */
    public static <T> TypeToken<Set<T>> setOf(Class<T> element) {
        return new TypeToken<>(parameterizedType(Set.class, element));
    }

    /** Map&lt;String, V&gt; (JSON 对象键总是 String) */
    public static <V> TypeToken<Map<String, V>> mapOf(Class<V> valueType) {
        return new TypeToken<>(parameterizedType(Map.class, String.class, valueType));
    }

    /** T[] (Java 数组) */
    public static <T> TypeToken<T[]> arrayOf(Class<T> element) {
        // Generic array type
        return new TypeToken<>(genericArrayType(element));
    }

    // === 私有辅助方法 ===

    private static Type parameterizedType(Class<?> raw, Class<?>... typeArguments) {
        // 实现省略
    }

    private static Type genericArrayType(Class<?> component) {
        // 实现省略
    }
}
```

### 2.5 配置枚举最终设计

```java
/**
 * 解析配置预设
 */
public enum ParseConfig {
    /** 标准 JSON 解析 */
    DEFAULT,

    /**
     * 宽松解析 - 适用于配置文件
     *
     * <p>启用: 注释、单引号、无引号字段名、智能匹配</p>
     */
    LENIENT,

    /**
     * 严格解析 - 适用于 API 契约
     *
     * <p>启用: 未知属性报错、null 值用于基本类型报错</p>
     */
    STRICT,

    /**
     * API 模式 - 严格 + 类型安全
     *
     * <p>启用: 严格模式 + BigDecimal 用于 double</p>
     */
    API;

    /** 获取对应的 ReadFeature 标志 */
    public ReadFeature[] features() {
        return switch (this) {
            case DEFAULT -> new ReadFeature[0];
            case LENIENT -> new ReadFeature[]{
                ReadFeature.AllowComments,
                ReadFeature.AllowSingleQuotes,
                ReadFeature.AllowUnquotedFieldNames,
                ReadFeature.SupportSmartMatch
            };
            case STRICT -> new ReadFeature[]{
                ReadFeature.ErrorOnUnknownProperties,
                ReadFeature.ErrorOnNullForPrimitives
            };
            case API -> new ReadFeature[]{
                ReadFeature.ErrorOnUnknownProperties,
                ReadFeature.ErrorOnNullForPrimitives,
                ReadFeature.UseBigDecimalForDoubles
            };
        };
    }

    /** 获取特性掩码 */
    public long mask() {
        return ReadFeature.of(features());
    }
}

/**
 * 序列化配置预设
 */
public enum WriteConfig {
    /** 标准 JSON 序列化 */
    DEFAULT,

    /**
     * 美化输出 - 适用于日志
     *
     * <p>启用: 格式化</p>
     */
    PRETTY,

    /**
     * 紧凑输出 - 适用于网络传输
     *
     * <p>启用: 无额外空格</p>
     */
    COMPACT,

    /**
     * 包含 null 值
     *
     * <p>启用: 写入 null 字段</p>
     */
    WITH_NULLS,

    /**
     * 美化 + 包含 null
     *
     * <p>启用: 格式化 + 写入 null 字段</p>
     */
    PRETTY_WITH_NULLS;

    /** 获取对应的 WriteFeature 标志 */
    public WriteFeature[] features() {
        return switch (this) {
            case DEFAULT -> new WriteFeature[0];
            case PRETTY -> new WriteFeature[]{WriteFeature.PrettyFormat};
            case COMPACT -> new WriteFeature[]{WriteFeature.OptimizedForAscii};
            case WITH_NULLS -> new WriteFeature[]{WriteFeature.WriteNulls};
            case PRETTY_WITH_NULLS -> new WriteFeature[]{
                WriteFeature.PrettyFormat,
                WriteFeature.WriteNulls
            };
        };
    }

    /** 获取特性掩码 */
    public long mask() {
        return WriteFeature.of(features());
    }
}
```

---

## 三、完整使用示例

### 3.1 基础用法

```java
// 解析对象
User user = JSON.parse(json, User.class);

// 解析列表
List<User> users = JSON.parseList(json, User.class);

// 解析 Map
Map<String, User> userMap = JSON.parseMap(json, String.class, User.class);

// 序列化
String json = JSON.write(user);
String pretty = JSON.write(user, WriteConfig.PRETTY);
```

### 3.2 配置使用

```java
// 宽松解析配置文件
User user = JSON.parse(configJson, User.class, ParseConfig.LENIENT);

// 严格 API 解析
User user = JSON.parse(apiJson, User.class, ParseConfig.API);

// 美化输出
String json = JSON.write(obj, WriteConfig.PRETTY);
```

### 3.3 复杂泛型

```java
// 使用 TypeToken (简单嵌套)
TypeToken<List<User>> type = TypeToken.listOf(User.class);
List<User> users = JSON.parse(json, type);

// 使用 TypeReference (复杂嵌套)
TypeToken<Map<String, List<User>>> complex =
    TypeToken.of(new TypeReference<Map<String, List<User>>>() {});
Map<String, List<User>> data = JSON.parse(json, complex);
```

---

## 四、迁移路径

### 4.1 第一阶段: 添加新 API (共存)
- 新增 `parse/write` 方法
- 新增 `TypeToken` 类
- 新增 `ParseConfig/WriteConfig` 枚举
- 旧方法保持不变

### 4.2 第二阶段: 标记废弃
- `toJSONString/toJSONBytes` → 标记 `@Deprecated`
- 文档推荐使用新 API

### 4.3 第三阶段: 移除废弃方法
- 移除 `@Deprecated` 方法 (如果有大版本升级)

---

## 五、待确认事项

1. **parseJavaArray 命名**: 是否改为 `parseArrayTyped` 或 `parseTypedArray`?
2. **parseMap 的键类型**: JSON 对象键总是 String，是否简化为 `parseMap(String, Class<V>)`?
3. **WriteConfig.COMPACT**: 是否真的需要，还是 DEFAULT 已经足够紧凑?

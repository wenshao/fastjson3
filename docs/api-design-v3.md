# 统一 parse/write API 设计方案 v3 (最终版)

## 第三轮审计 - 边界情况与实现细节

---

## 一、v2 方案问题分析

### 1.1 parseMap 键类型问题

**v2 问题**:
```java
<K,V> Map<K,V> JSON.parseMap(String json, Class<K> key, Class<V> value)
```

**问题分析**:
- JSON 对象键只能是字符串
- 允许 `Class<K>` 会给错误期望
- 类型不安全: 如果传入 `Integer.class` 会运行时失败

**修正**:
```java
// 简化: 键类型固定为 String
<V> Map<String, V> JSON.parseMap(String json, Class<V> value)
<V> Map<String, V> JSON.parseMap(byte[] json, Class<V> value)
```

### 1.2 方法命名冲突

**v2 问题**:
```java
// 现有方法 (保留)
JSON.parseArray(String json) -> JSONArray

// 新方法
<T> List<T> JSON.parseList(String json, Class<T> element)

// 问题: parseArray 和 parseList 命名不一致
// parseArray 返回 JSONArray
// parseList 返回 List<T>
```

**修正**: 保持现有命名约定
- `parseArray(String)` → `JSONArray` (保留，无歧义)
- `parseArray(String, Class)` → `List<T>` (保留，语义清晰)
- 新增 `parseList` 作为 `parseArray(String, Class)` 的别名 (可选)

**最终决策**:
```java
// 不新增 parseList，使用现有的 parseArray
<T> List<T> JSON.parseArray(String json, Class<T> element)
<T> List<T> JSON.parseArray(byte[] json, Class<T> element)

// 但可以添加 readList 作为更语义化的别名 (可选)
<T> List<T> JSON.readList(String json, Class<T> element)
```

### 1.3 null 输入处理一致性

**v2 问题**: 没有明确 null 输入行为

**修正**: 统一 null 处理策略
```java
/**
 * 解析 JSON 字符串。
 *
 * @param json JSON 字符串，如果为 null 或空则返回 null
 * @param type 目标类型
 * @return 解析后的对象，如果输入为 null/空则返回 null
 * @throws JSONException 如果 JSON 语法错误或类型转换失败
 */
<T> T JSON.parse(String json, Class<T> type) {
    if (json == null || json.isEmpty()) {
        return null;
    }
    // ...
}
```

### 1.4 空字符串 vs null

**问题**: `""` (空字符串) 和 `null` 应该有不同行为吗?

**决策**:
- `null` 输入 → 返回 `null`
- `""` (空字符串) → 抛出异常 (无效 JSON)
- `"null"` → 返回 `null`

### 1.5 TypeToken 类型擦除问题

**v2 问题**: TypeToken 不能完全避免类型擦除

**问题示例**:
```java
// 这样使用仍然会有 unchecked 警告
TypeToken<List<User>> type = TypeToken.listOf(User.class);
List<User> users = JSON.parse(json, type);  // 内部仍然有 cast
```

**解决方案**: TypeToken 内部使用 `Type` 而非 `Class<T>`，避免类型擦除

```java
public final class TypeToken<T> {
    private final Type type;  // 不是 Class<T>

    // 使用时
    public <T> T parse(String json, TypeToken<T> token) {
        ObjectReader<T> reader = getObjectReader(token.type());
        // ...
    }
}
```

---

## 二、最终 API 设计

### 2.1 完整方法签名

```java
public final class JSON {

    // ==================== 核心解析方法 ====================

    /**
     * 解析 JSON 为指定类型 (String 输入)
     */
    static <T> T parse(String json, Class<T> type);

    /**
     * 解析 JSON 为指定类型 (String 输入，带配置)
     */
    static <T> T parse(String json, Class<T> type, ParseConfig config);

    /**
     * 解析 JSON 为指定类型 (byte[] 输入，UTF-8)
     */
    static <T> T parse(byte[] json, Class<T> type);

    /**
     * 解析 JSON 为指定类型 (byte[] 输入，带配置)
     */
    static <T> T parse(byte[] json, Class<T> type, ParseConfig config);

    /**
     * 解析 JSON 为指定类型 (TypeToken，支持泛型)
     */
    static <T> T parse(String json, TypeToken<T> typeToken);

    /**
     * 解析 JSON 为指定类型 (TypeToken，带配置)
     */
    static <T> T parse(String json, TypeToken<T> typeToken, ParseConfig config);

    /**
     * 解析 JSON 为指定类型 (TypeToken，byte[] 输入)
     */
    static <T> T parse(byte[] json, TypeToken<T> typeToken);

    /**
     * 解析 JSON 为指定类型 (TypeToken，byte[] 输入，带配置)
     */
    static <T> T parse(byte[] json, TypeToken<T> typeToken, ParseConfig config);

    // ==================== 自动类型检测 ====================

    /**
     * 解析 JSON 为自动检测的类型 (JSONObject, JSONArray, String, Number, Boolean, null)
     */
    static Object parseAny(String json);

    /**
     * 解析 JSON 为自动检测的类型 (带配置)
     */
    static Object parseAny(String json, ParseConfig config);

    /**
     * 解析 JSON 为自动检测的类型 (byte[] 输入)
     */
    static Object parseAny(byte[] json);

    // ==================== JSON 容器类型 ====================

    /**
     * 解析 JSON 为 JSONObject
     */
    static JSONObject parseObject(String json);

    /**
     * 解析 JSON 为 JSONObject (byte[] 输入)
     */
    static JSONObject parseObject(byte[] json);

    /**
     * 解析 JSON 为 JSONArray
     */
    static JSONArray parseArray(String json);

    /**
     * 解析 JSON 为 JSONArray (byte[] 输入)
     */
    static JSONArray parseArray(byte[] json);

    // ==================== 集合类型 ====================

    /**
     * 解析 JSON 为 List&lt;T&gt;
     */
    static <T> List<T> parseList(String json, Class<T> element);

    /**
     * 解析 JSON 为 List&lt;T&gt; (byte[] 输入)
     */
    static <T> List<T> parseList(byte[] json, Class<T> element);

    /**
     * 解析 JSON 为 Set&lt;T&gt;
     */
    static <T> Set<T> parseSet(String json, Class<T> element);

    /**
     * 解析 JSON 为 Set&lt;T&gt; (byte[] 输入)
     */
    static <T> Set<T> parseSet(byte[] json, Class<T> element);

    /**
     * 解析 JSON 为 Map&lt;String, V&gt;
     * (JSON 对象键总是字符串)
     */
    static <V> Map<String, V> parseMap(String json, Class<V> value);

    /**
     * 解析 JSON 为 Map&lt;String, V&gt; (byte[] 输入)
     */
    static <V> Map<String, V> parseMap(byte[] json, Class<V> value);

    /**
     * 解析 JSON 为 Java 数组 T[]
     */
    static <T> T[] parseTypedArray(String json, Class<T> element);

    /**
     * 解析 JSON 为 Java 数组 T[] (byte[] 输入)
     */
    static <T> T[] parseTypedArray(byte[] json, Class<T> element);

    // ==================== 兼容方法 (保留) ====================

    /**
     * 解析 JSON 为指定类型 (使用 TypeReference)
     */
    static <T> T parse(String json, Type type);

    /**
     * 解析 JSON 为指定类型 (使用 TypeReference，带配置)
     */
    static <T> T parse(String json, Type type, ParseConfig config);

    /**
     * 解析 JSON 为指定类型 (使用 TypeReference)
     */
    static <T> T parse(String json, TypeReference<T> typeRef);

    /**
     * 解析 JSON 为指定类型 (使用 TypeReference，带配置)
     */
    static <T> T parse(String json, TypeReference<T> typeRef, ParseConfig config);

    // ==================== 核心序列化方法 ====================

    /**
     * 序列化对象为 JSON 字符串
     */
    static String write(Object obj);

    /**
     * 序列化对象为 JSON 字符串 (带配置)
     */
    static String write(Object obj, WriteConfig config);

    /**
     * 序列化对象为 UTF-8 字节数组
     */
    static byte[] writeBytes(Object obj);

    /**
     * 序列化对象为 UTF-8 字节数组 (带配置)
     */
    static byte[] writeBytes(Object obj, WriteConfig config);

    // ==================== 序列化便捷方法 ====================

    /**
     * 序列化对象为美化 JSON 字符串
     * 等同于 write(obj, WriteConfig.PRETTY)
     */
    static String writePretty(Object obj);

    /**
     * 序列化对象为紧凑 JSON 字符串
     * 等同于 write(obj, WriteConfig.COMPACT)
     */
    static String writeCompact(Object obj);

    // ==================== 验证方法 (保持不变) ====================

    static boolean isValid(String json);
    static boolean isValid(byte[] json);
    static boolean isValidObject(String json);
    static boolean isValidObject(byte[] json);
    static boolean isValidArray(String json);
    static boolean isValidArray(byte[] json);

    // ==================== 便捷工厂方法 (保持不变) ====================

    static JSONObject object();
    static JSONObject object(String key, Object value);
    static JSONArray array();
    static JSONArray array(Object... items);
}
```

### 2.2 TypeToken 最终实现

```java
package com.alibaba.fastjson3;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类型标记，用于在运行时保留泛型类型信息。
 *
 * <h3>与 TypeReference 的区别:</h3>
 * <ul>
 *   <li>TypeToken 使用静态工厂方法，语法更简洁</li>
 *   <li>TypeReference 使用匿名子类，支持任意复杂类型</li>
 *   <li>对于简单泛型，优先使用 TypeToken</li>
 *   <li>对于复杂嵌套，使用 TypeReference 然后 TypeToken.of()</li>
 * </ul>
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
 * // 映射类型
 * TypeToken&lt;Map&lt;String, User&gt;&gt; userMap = TypeToken.mapOf(User.class);
 *
 * // 复杂嵌套 - 结合 TypeReference 使用
 * TypeToken&lt;Map&lt;String, List&lt;User&gt;&gt;&gt; complex =
 *     TypeToken.of(new TypeReference&lt;Map&lt;String, List&lt;User&gt;&gt;&gt;() {});
 * </pre>
 *
 * @param <T> 类型参数
 */
public final class TypeToken<T> {
    private final Type type;

    private TypeToken(Type type) {
        this.type = type;
    }

    /**
     * 获取实际的 java.lang.reflect.Type
     */
    public Type type() {
        return type;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建简单类型的 TypeToken
     */
    public static <T> TypeToken<T> of(Class<T> type) {
        return new TypeToken<>(type);
    }

    /**
     * 从 Type 创建 TypeToken (用于 TypeReference)
     */
    public static <T> TypeToken<T> of(Type type) {
        return new TypeToken<>(type);
    }

    /**
     * 从 TypeReference 创建 TypeToken
     */
    public static <T> TypeToken<T> of(TypeReference<T> ref) {
        return new TypeToken<>(ref.getType());
    }

    /**
     * 创建 List&lt;T&gt; 类型的 TypeToken
     */
    public static <T> TypeToken<List<T>> listOf(Class<T> element) {
        return new TypeToken<>(new ParameterizedTypeImpl(List.class, element));
    }

    /**
     * 创建 Set&lt;T&gt; 类型的 TypeToken
     */
    public static <T> TypeToken<Set<T>> setOf(Class<T> element) {
        return new TypeToken<>(new ParameterizedTypeImpl(Set.class, element));
    }

    /**
     * 创建 Map&lt;String, V&gt; 类型的 TypeToken
     * (JSON 对象键总是字符串)
     */
    public static <V> TypeToken<Map<String, V>> mapOf(Class<V> valueType) {
        return new TypeToken<>(new ParameterizedTypeImpl(Map.class, String.class, valueType));
    }

    /**
     * 创建 T[] 数组类型的 TypeToken
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeToken<T[]> arrayOf(Class<T> element) {
        // 使用 Array.newInstance 创建泛型数组类型
        Object array = Array.newInstance(element, 0);
        return new TypeToken<>(array.getClass());
    }

    // ==================== 内部实现类 ====================

    private static final class ParameterizedTypeImpl implements ParameterizedType {
        private final Class<?> rawType;
        private final Type[] typeArguments;

        ParameterizedTypeImpl(Class<?> rawType, Class<?>... typeArguments) {
            this.rawType = rawType;
            this.typeArguments = typeArguments;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(rawType.getName());
            if (typeArguments.length > 0) {
                sb.append("<");
                for (int i = 0; i < typeArguments.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(typeArguments[i].getTypeName());
                }
                sb.append(">");
            }
            return sb.toString();
        }
    }
}
```

### 2.3 配置枚举最终实现

```java
package com.alibaba.fastjson3;

/**
 * 解析配置预设
 */
public enum ParseConfig {
    /** 标准 JSON 解析 */
    DEFAULT(new ReadFeature[0]),

    /**
     * 宽松解析 - 适用于配置文件
     *
     * <p>启用: 注释、单引号、无引号字段名、智能匹配</p>
     */
    LENIENT(new ReadFeature[]{
        ReadFeature.AllowComments,
        ReadFeature.AllowSingleQuotes,
        ReadFeature.AllowUnquotedFieldNames,
        ReadFeature.SupportSmartMatch
    }),

    /**
     * 严格解析 - 适用于 API 契约
     *
     * <p>启用: 未知属性报错、null 值用于基本类型报错</p>
     */
    STRICT(new ReadFeature[]{
        ReadFeature.ErrorOnUnknownProperties,
        ReadFeature.ErrorOnNullForPrimitives
    }),

    /**
     * API 模式 - 严格 + 类型安全
     *
     * <p>启用: 严格模式 + BigDecimal 用于 double</p>
     */
    API(new ReadFeature[]{
        ReadFeature.ErrorOnUnknownProperties,
        ReadFeature.ErrorOnNullForPrimitives,
        ReadFeature.UseBigDecimalForDoubles
    });

    private final ReadFeature[] features;

    ParseConfig(ReadFeature[] features) {
        this.features = features;
    }

    /**
     * 获取对应的 ReadFeature 标志
     */
    public ReadFeature[] features() {
        return features.clone();
    }

    /**
     * 获取特性掩码
     */
    public long mask() {
        return ReadFeature.of(features);
    }
}
```

---

## 三、实现注意事项

### 3.1 方法委托

新方法应该委托给现有的 ObjectMapper 方法，避免重复代码：

```java
static <T> T parse(String json, Class<T> type, ParseConfig config) {
    if (json == null || json.isEmpty()) {
        return null;
    }
    // 使用现有的 ObjectMapper
    ObjectMapper mapper = ObjectMapper.shared();
    // 应用配置
    long features = config.mask();
    // 委托给现有方法
    return mapper.readValue(json, type);  // 需要支持传入 features
}
```

### 3.2 向后兼容

保持现有方法工作，新方法作为额外选项：

```java
// 现有方法保持不变
public static <T> T parseObject(String json, Class<T> type) {
    return ObjectMapper.shared().readValue(json, type);
}

// 新方法
public static <T> T parse(String json, Class<T> type) {
    return parseObject(json, type);  // 委托给现有方法
}
```

### 3.3 性能考虑

- TypeToken 应该是不可变的，可以被缓存
- 配置枚举的 features() 应该返回不可变数组
- 避免在热路径上创建不必要的对象

---

## 四、使用示例完整版

```java
// === 基础解析 ===
User user = JSON.parse(json, User.class);
List<User> users = JSON.parseList(json, User.class);
Map<String, User> userMap = JSON.parseMap(json, User.class);
User[] userArray = JSON.parseTypedArray(json, User.class);

// === 带配置 ===
User user = JSON.parse(json, User.class, ParseConfig.LENIENT);
String pretty = JSON.write(obj, WriteConfig.PRETTY);

// === TypeToken ===
TypeToken<List<User>> type = TypeToken.listOf(User.class);
List<User> users = JSON.parse(json, type);

// === 兼容 TypeReference ===
TypeToken<Map<String, List<User>>> complex =
    TypeToken.of(new TypeReference<Map<String, List<User>>>() {});
Map<String, List<User>> data = JSON.parse(json, complex);

// === 容器类型 ===
JSONObject obj = JSON.parseObject(json);
JSONArray arr = JSON.parseArray(json);

// === 便捷方法 ===
String pretty = JSON.writePretty(obj);
String compact = JSON.writeCompact(obj);
```

---

## 五、API 设计检查清单

- [x] 方法命名一致
- [x] 参数类型合理 (parseMap 键类型固定为 String)
- [x] null 输入处理明确
- [x] 泛型类型安全 (TypeToken)
- [x] 配置语义化
- [x] 向后兼容
- [x] 性能考虑
- [x] 文档完整

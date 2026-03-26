# JSON 类参考

`JSON` 是静态工具类，提供便捷的 JSON 操作方法。所有方法都委托给 `ObjectMapper.shared()`。

## 类声明

```java
public final class JSON {
    // 私有构造，不可实例化
    private JSON() {}
}
```

## 解析方法

### parseObject

```java
// 解析为 JSONObject
public static JSONObject parseObject(String json)

// 解析为指定类型
public static <T> T parseObject(String json, Class<T> type)

// 带特性
public static <T> T parseObject(String json, Class<T> type, ReadFeature... features)

// 解析泛型类型
public static <T> T parseObject(String json, Type type)

// 使用 TypeReference
public static <T> T parseObject(String json, TypeReference<T> typeRef)

// 从 byte[] 解析
public static JSONObject parseObject(byte[] jsonBytes)
public static <T> T parseObject(byte[] jsonBytes, Class<T> type)
public static <T> T parseObject(byte[] jsonBytes, Class<T> type, ReadFeature... features)
```

**示例：**

```java
// 解析为动态对象
JSONObject obj = JSON.parseObject("{\"name\":\"张三\"}");
String name = obj.getString("name");

// 解析为对象
User user = JSON.parseObject("{\"name\":\"张三\"}", User.class);

// 解析泛型
TypeReference<List<User>> ref = new TypeReference<List<User>>() {};
List<User> users = JSON.parseObject(jsonStr, ref);
```

### parseArray

```java
// 解析为 JSONArray
public static JSONArray parseArray(String json)
public static JSONArray parseArray(byte[] jsonBytes)

// 解析为对象列表
public static <T> List<T> parseArray(String json, Class<T> type)
```

**示例：**

```java
// 解析为动态数组
JSONArray arr = JSON.parseArray("[1,2,3]");

// 解析为对象列表
List<User> users = JSON.parseArray(jsonStr, User.class);
```

### parse（统一解析 API）

```java
// 自动检测类型
public static Object parse(String json)

// 指定类型
public static <T> T parse(String json, Class<T> type)

// 指定类型 + 配置预设
public static <T> T parse(String json, Class<T> type, ParseConfig config)

// 使用 TypeToken（泛型类型）
public static <T> T parse(String json, TypeToken<T> typeToken)
public static <T> T parse(String json, TypeToken<T> typeToken, ParseConfig config)

// 使用 TypeReference
public static <T> T parse(String json, TypeReference<T> typeRef)
public static <T> T parse(String json, TypeReference<T> typeRef, ParseConfig config)

// 使用 Type
public static <T> T parse(String json, Type type)
public static <T> T parse(String json, Type type, ParseConfig config)

// 从 byte[] 解析（以上均有 byte[] 版本）
public static <T> T parse(byte[] jsonBytes, Class<T> type)
public static <T> T parse(byte[] jsonBytes, Class<T> type, ParseConfig config)
public static <T> T parse(byte[] jsonBytes, TypeToken<T> typeToken)
public static <T> T parse(byte[] jsonBytes, TypeToken<T> typeToken, ParseConfig config)
```

**示例：**

```java
// 基本解析
User user = JSON.parse(json, User.class);

// 宽松模式（适合配置文件，允许注释、单引号等）
User config = JSON.parse(configJson, User.class, ParseConfig.LENIENT);

// 严格模式（适合 API，未知属性报错）
User user = JSON.parse(apiJson, User.class, ParseConfig.STRICT);

// 泛型类型解析
List<User> users = JSON.parse(json, TypeToken.listOf(User.class));
Map<String, User> map = JSON.parse(json, TypeToken.mapOf(User.class));
```

### 集合类型解析

```java
// List
public static <E> List<E> parseList(String json, Class<E> elementType)
public static <E> List<E> parseList(String json, Class<E> elementType, ParseConfig config)

// Set
public static <E> Set<E> parseSet(String json, Class<E> elementType)
public static <E> Set<E> parseSet(String json, Class<E> elementType, ParseConfig config)

// Map<String, V>
public static <V> Map<String, V> parseMap(String json, Class<V> valueType)
public static <V> Map<String, V> parseMap(String json, Class<V> valueType, ParseConfig config)

// T[]
public static <E> E[] parseTypedArray(String json, Class<E> elementType)
public static <E> E[] parseTypedArray(String json, Class<E> elementType, ParseConfig config)
```

**示例：**

```java
List<User> users = JSON.parseList(json, User.class);
Set<String> tags = JSON.parseSet(json, String.class);
Map<String, User> userMap = JSON.parseMap(json, User.class);
User[] usersArray = JSON.parseTypedArray(json, User.class);
```

## 序列化方法

### toJSONString

```java
// 基本序列化
public static String toJSONString(Object object)

// 带特性
public static String toJSONString(Object object, WriteFeature... features)
```

**示例：**

```java
// 基本序列化
String json = JSON.toJSONString(user);

// 美化输出
String pretty = JSON.toJSONString(user, WriteFeature.PrettyFormat);

// 包含 null
String withNulls = JSON.toJSONString(user, WriteFeature.WriteNulls);
```

### toJSONBytes

```java
// 序列化为 UTF-8 字节数组
public static byte[] toJSONBytes(Object object)

// 带特性
public static byte[] toJSONBytes(Object object, WriteFeature... features)
```

### write（统一序列化 API）

```java
// 基本序列化
public static String write(Object obj)

// 带配置预设
public static String write(Object obj, WriteConfig config)

// 序列化为 byte[]
public static byte[] writeBytes(Object obj)
public static byte[] writeBytes(Object obj, WriteConfig config)
```

**示例：**

```java
// 美化输出
String json = JSON.write(user, WriteConfig.PRETTY);

// 包含 null
String json = JSON.write(user, WriteConfig.WITH_NULLS);

// 美化 + null
String json = JSON.write(user, WriteConfig.PRETTY_WITH_NULLS);
```

## 验证方法

```java
// 验证是否为有效 JSON
public static boolean isValid(String json)
public static boolean isValid(byte[] jsonBytes)

// 验证是否为 JSON 对象
public static boolean isValidObject(String json)
public static boolean isValidObject(byte[] jsonBytes)

// 验证是否为 JSON 数组
public static boolean isValidArray(String json)
public static boolean isValidArray(byte[] jsonBytes)
```

**示例：**

```java
if (JSON.isValid(jsonStr)) {
    // 处理有效 JSON
}

if (JSON.isValidObject(jsonStr)) {
    JSONObject obj = JSON.parseObject(jsonStr);
}
```

## 便捷构建方法

```java
// 创建空 JSONObject
public static JSONObject object()

// 创建含一个键值对的 JSONObject
public static JSONObject object(String key, Object value)

// 创建空 JSONArray
public static JSONArray array()

// 创建含初始元素的 JSONArray
public static JSONArray array(Object... items)
```

**示例：**

```java
JSONObject obj = JSON.object("name", "张三");
JSONArray arr = JSON.array(1, 2, 3);
```

## JSONPath 快捷方法

```java
// 对 JSON 字符串执行 JSONPath 表达式
public static <T> T eval(String json, String path, Class<T> type)
```

**示例：**

```java
String title = JSON.eval(json, "$.store.book[0].title", String.class);
```

## JSON Merge Patch (RFC 7396)

```java
// 字符串形式
public static String mergePatch(String target, String patch)

// 对象形式
public static Object mergePatch(Object target, Object patch)
```

**示例：**

```java
String target = "{\"a\":1,\"b\":2}";
String patch = "{\"b\":null,\"c\":3}";
String result = JSON.mergePatch(target, patch);
// result: {"a":1,"c":3}
```

## 更多解析重载

### 从 byte[] / InputStream / Reader / char[] / URL 解析

```java
// byte[] → 自动检测类型
public static Object parse(byte[] jsonBytes)
public static Object parse(byte[] jsonBytes, ReadFeature... features)

// InputStream → 自动检测
public static Object parse(InputStream in)

// char[] → 自动检测
public static Object parse(char[] chars)

// byte[] → 泛型类型
public static <T> T parseObject(byte[] jsonBytes, Type type)
public static <T> T parseObject(byte[] jsonBytes, Type type, ReadFeature... features)

// String → 泛型类型 + 特性
public static <T> T parseObject(String json, Type type, ReadFeature... features)

// char[] → JSONObject / 类型
public static JSONObject parseObject(char[] chars)
public static <T> T parseObject(char[] chars, Class<T> type)

// URL → JSONObject / 类型
public static JSONObject parseObject(URL url)
public static <T> T parseObject(URL url, Class<T> type)

// Reader → JSONObject / 类型
public static JSONObject parseObject(Reader reader)
public static <T> T parseObject(Reader reader, Class<T> type)

// InputStream → 泛型
public static <T> T parseObject(InputStream in, Type type)
public static <T> T parseObject(InputStream in, Class<T> type, ReadFeature... features)

// parseArray 额外重载
public static <T> List<T> parseArray(String json, Class<T> type, ReadFeature... features)
public static <T> List<T> parseArray(byte[] jsonBytes, Class<T> type)
public static <T> List<T> parseArray(byte[] jsonBytes, Class<T> type, ReadFeature... features)
public static JSONArray parseArray(InputStream in)
public static JSONArray parseArray(Reader reader)
public static <T> List<T> parseArray(InputStream in, Class<T> type)

// parseAny — 自动检测类型
public static Object parseAny(String json)
public static Object parseAny(String json, ParseConfig config)

// 集合解析 byte[] 版本
public static <E> List<E> parseList(byte[] jsonBytes, Class<E> elementType)
public static <E> List<E> parseList(byte[] jsonBytes, Class<E> elementType, ParseConfig config)
public static <E> Set<E> parseSet(byte[] jsonBytes, Class<E> elementType)
public static <E> Set<E> parseSet(byte[] jsonBytes, Class<E> elementType, ParseConfig config)
public static <V> Map<String, V> parseMap(byte[] jsonBytes, Class<V> valueType)
public static <V> Map<String, V> parseMap(byte[] jsonBytes, Class<V> valueType, ParseConfig config)
public static <E> E[] parseTypedArray(byte[] jsonBytes, Class<E> elementType)
public static <E> E[] parseTypedArray(byte[] jsonBytes, Class<E> elementType, ParseConfig config)
```

## 带 Filter 的序列化

```java
// 单 Filter
public static String toJSONString(Object obj, Filter filter, WriteFeature... features)

// 多 Filter
public static String toJSONString(Object obj, Filter[] filters, WriteFeature... features)

// byte[] + Filter
public static byte[] toJSONBytes(Object obj, Filter[] filters, WriteFeature... features)
```

**示例：**

```java
// 排除密码字段
PropertyFilter filter = (source, name, value) -> !"password".equals(name);
String json = JSON.toJSONString(user, filter);

// 转换字段名为大写
NameFilter nameFilter = (source, name, value) -> name.toUpperCase();
String json = JSON.toJSONString(user, nameFilter);
```

## OutputStream 写入

```java
// 写入 OutputStream（返回写入字节数）
public static int writeTo(OutputStream out, Object object)
public static int writeTo(OutputStream out, Object object, WriteFeature... features)
public static int writeTo(OutputStream out, Object object, Filter[] filters, WriteFeature... features)
```

**示例：**

```java
try (OutputStream out = new FileOutputStream("user.json")) {
    JSON.writeTo(out, user);
}
```

## 转换方法

```java
// Java 对象 → JSONObject/JSONArray（JSON 树）
public static Object toJSON(Object object)

// JSON 树 → Java 对象
public static <T> T toJavaObject(Object object, Class<T> clazz)

// 深拷贝（通过 JSON 序列化/反序列化）
public static <T> T copy(T object)

// 跨类型拷贝
public static <T> T copyTo(Object object, Class<T> targetClass)
```

**示例：**

```java
// POJO → JSONObject
JSONObject obj = (JSONObject) JSON.toJSON(user);

// JSONObject → POJO
User user = JSON.toJavaObject(obj, User.class);

// 深拷贝
User copy = JSON.copy(original);

// 跨类型拷贝
UserDTO dto = JSON.copyTo(userEntity, UserDTO.class);
```

## 全局注册

```java
// 注册自定义 ObjectReader（传 null 取消注册）
public static void register(Type type, ObjectReader<?> objectReader)

// 注册自定义 ObjectWriter（传 null 取消注册）
public static void register(Type type, ObjectWriter<?> objectWriter)

// 取消注册（同时移除 reader 和 writer）
public static void unregister(Type type)

// 注册 MixIn 注解
public static void mixIn(Class<?> target, Class<?> mixinSource)
```

**示例：**

```java
// 自定义序列化器
JSON.register(Money.class, new MoneyWriter());

// 取消注册
JSON.unregister(Money.class);

// MixIn 对第三方类注入注解
JSON.mixIn(ThirdPartyClass.class, ThirdPartyMixin.class);
```

## 全局配置

```java
// 启用全局解析特性
public static void config(ReadFeature... features)
public static void config(ReadFeature feature, boolean state)

// 启用全局序列化特性
public static void config(WriteFeature... features)
public static void config(WriteFeature feature, boolean state)

// 查询全局特性状态
public static boolean isEnabled(ReadFeature feature)
public static boolean isEnabled(WriteFeature feature)
```

**示例：**

```java
// 全局允许单引号 JSON
JSON.config(ReadFeature.AllowSingleQuotes);

// 全局美化输出
JSON.config(WriteFeature.PrettyFormat);

// 查询
if (JSON.isEnabled(ReadFeature.AllowSingleQuotes)) { ... }

// 禁用
JSON.config(WriteFeature.PrettyFormat, false);
```

> **注意**：`config()` 设置的全局特性影响所有 `JSON.parse*` / `JSON.toJSONString` / `JSON.toJSONBytes` 方法。
> 带显式 `ReadFeature...` 或 `ParseConfig` 参数的方法不受全局配置影响。

## char[] 验证

```java
public static boolean isValid(char[] chars)
public static boolean isValidObject(char[] chars)
public static boolean isValidArray(char[] chars)
```

## 便捷方法

```java
// 美化输出
public static String writePretty(Object obj)

// 紧凑输出
public static String writeCompact(Object obj)
```

## 使用建议

1. **简单场景** - 使用 `JSON` 静态方法足够

```java
String json = JSON.toJSONString(user);
User user = JSON.parseObject(json, User.class);
```

2. **语义化配置** - 使用 `parse` / `write` + 预设

```java
User user = JSON.parse(json, User.class, ParseConfig.LENIENT);
String json = JSON.write(user, WriteConfig.PRETTY);
```

3. **需要自定义配置** - 使用 `ObjectMapper`

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
String json = mapper.writeValueAsString(user);
```

4. **高性能场景** - 使用 `toJSONBytes()` 而非 `toJSONString()`

```java
byte[] bytes = JSON.toJSONBytes(user);
```

## 线程安全

`JSON` 类的所有方法都是静态的，无状态，完全线程安全。

## 相关文档

- [📖 ObjectMapper 参考 →](ObjectMapper.md)
- [📖 JSONObject 参考 →](JSONObject.md)
- [📖 JSONArray 参考 →](JSONArray.md)
- [📋 特性枚举 →](features.md)
- [📖 注解参考 →](annotations.md)

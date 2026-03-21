# JSON 类参考

`JSON` 是静态工具类，提供便捷的 JSON 操作方法。

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
public static JSONObject parseObject(String str)

// 解析为指定类型
public static <T> T parseObject(String str, Class<T> clazz)

// 解析泛型类型
public static <T> T parseObject(String str, TypeReference<T> reference)

// 从 byte[] 解析
public static <T> T parseObject(byte[] bytes, Class<T> clazz)

// 从 InputStream 解析
public static <T> T parseObject(InputStream is, Class<T> clazz)

// 从 Reader 解析
public static <T> T parseObject(Reader reader, Class<T> clazz)
```

**示例：**

```java
// 解析为动态对象
JSONObject obj = JSON.parseObject("{\"name\":\"张三\"}");
String name = obj.getString("name");

// 解析为对象
User user = JSON.parseObject("{\"name\":\"张三\"}", User.class);

// 解析泛型
TypeToken<List<User>> type = new TypeToken<List<User>>() {};
List<User> users = JSON.parseObject(jsonStr, type);
```

### parseArray

```java
// 解析为 JSONArray
public static JSONArray parseArray(String str)

// 解析为对象列表
public static <T> List<T> parseArray(String str, Class<T> clazz)
```

**示例：**

```java
// 解析为动态数组
JSONArray arr = JSON.parseArray("[1,2,3]");

// 解析为对象列表
List<User> users = JSON.parseArray(jsonStr, User.class);
```

### parse

```java
// 解析为任意类型
public static Object parse(String str)
```

**示例：**

```java
Object obj = JSON.parse(jsonStr);
if (obj instanceof JSONObject) {
    // 处理对象
} else if (obj instanceof JSONArray) {
    // 处理数组
}
```

## 序列化方法

### toJSONString

```java
// 基本序列化
public static String toJSONString(Object object)

// 带特性
public static String toJSONString(Object object, WriteFeature... features)

// 带过滤器
public static String toJSONString(Object object, Filter... filters)

// 带格式和特性
public static String toJSONString(
    Object object,
    SerializeFilter[] filters,
    WriteFeature... features
)
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
// 基本序列化为字节
public static byte[] toJSONBytes(Object object)

// 带特性
public static byte[] toJSONBytes(Object object, WriteFeature... features)
```

**示例：**

```java
byte[] bytes = JSON.toJSONBytes(user);
```

## 验证方法

### isValid

```java
// 验证是否为有效 JSON
public static boolean isValid(String jsonStr)

// 验证是否为 JSON 对象
public static boolean isValidObject(String jsonStr)

// 验证是否为 JSON 数组
public static boolean isValidArray(String jsonStr)
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

## 特性相关

```java
// 获取默认配置
public static ReadFeature[] getDefaultReadFeatures()
public static WriteFeature[] getDefaultWriteFeatures()

// 设置默认配置
public static void setDefaultReadFeatures(ReadFeature... features)
public static void setDefaultWriteFeatures(WriteFeature... features)
```

## 常用快捷方法

```java
// 转换为 Java 对象
public static <T> T toJavaObject(Object obj, Class<T> clazz)

// 转换为列表
public static <T> List<T> toJavaList(JSONArray array, Class<T> clazz)

// 获取对象
public static JSONObject parseObject(String str)
public static JSONArray parseArray(String str)
```

## 使用建议

1. **简单场景** - 使用 `JSON` 静态方法足够

```java
String json = JSON.toJSONString(user);
User user = JSON.parseObject(json, User.class);
```

2. **需要自定义配置** - 使用 `ObjectMapper`

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
String json = mapper.writeValueAsString(user);
```

3. **高性能场景** - 使用 `toJSONBytes()` 而非 `toJSONString()`

```java
byte[] bytes = JSON.toJSONBytes(user);
```

## JSON Merge Patch (RFC 7396)

```java
// 字符串 API
String result = JSON.mergePatch(target, patch);

// 对象 API
Object result = JSON.mergePatch(targetObj, patchObj);
```

规则：patch 中的 `null` 值删除对应字段，对象值递归合并，其他值直接替换。

## 线程安全

`JSON` 类的所有方法都是静态的，无状态，完全线程安全。

## 相关文档

- [📖 ObjectMapper 参考 →](ObjectMapper.md)
- [📖 JSONObject 参考 →](JSONObject.md)
- [📖 JSONArray 参考 →](JSONArray.md)
- [📖 注解参考 →](annotations.md)

# fastjson3 速查表

**版本**：fastjson3 3.0.0 | **Java 要求**：JDK 21+

快速参考常用操作。

## 基础操作

```java
// ===== 解析 =====
// 解析为对象
User user = JSON.parseObject(json, User.class);

// 解析为列表
List<User> users = JSON.parseArray(json, User.class);

// 解析为动态对象
JSONObject obj = JSON.parseObject(json);
JSONArray arr = JSON.parseArray(json);

// 使用 ObjectMapper
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);

// ===== 序列化 =====
// 对象转 JSON
String json = JSON.toJSONString(user);

// 美化输出
String json = JSON.toJSONString(user, WriteFeature.PrettyFormat);

// 转字节
byte[] bytes = JSON.toJSONBytes(user);

// 使用 ObjectMapper
String json = mapper.writeValueAsString(user);
byte[] bytes = mapper.writeValueAsBytes(user);

// ===== 验证 =====
boolean valid = JSON.isValid(json);           // 有效 JSON
boolean isObj = JSON.isValidObject(json);     // 是对象
boolean isArr = JSON.isValidArray(json);      // 是数组
```

## 常用注解

```java
@JSONField(name = "user_name")           // 字段重命名
@JSONField(format = "yyyy-MM-dd")        // 日期格式
@JSONField(serialize = false)            // 不序列化
@JSONField(deserialize = false)          // 不反序列化
@JSONField(ordinal = 1)                  // 序列化顺序
@JSONField(alternateNames = {"a", "b"}) // 替代名称

@JSONType(naming = NamingStrategy.SnakeCase)  // 类级命名策略
@JSONType(ignores = {"field1", "field2"})       // 忽略多个字段
```

## 常用配置

```java
// ObjectMapper 配置
ObjectMapper mapper = ObjectMapper.builder()
    // 读取特性
    .enableRead(ReadFeature.SupportSmartMatch)     // 智能匹配
    .enableRead(ReadFeature.AllowComments)         // 允许注释
    .enableRead(ReadFeature.ErrorOnUnknownProperties) // 严格模式
    // 写入特性
    .enableWrite(WriteFeature.PrettyFormat)        // 美化
    .enableWrite(WriteFeature.WriteNulls)          // 输出 null
    .enableWrite(WriteFeature.BrowserCompatible)   // 浏览器兼容
    .build();
```

## JSONPath

```java
// 预编译（推荐）
JSONPath path = JSONPath.of("$.store.book[*].author");
List<String> authors = path.extract(json, List.class);

// 直接使用
String title = JSONPath.of("$.store.book[0].title").eval(json, String.class);

// 多路径提取
String[] paths = {"$.name", "$.age"};
Type[] types = {String.class, Integer.class};
JSONPath multiPath = JSONPath.of(paths, types);
Object[] values = multiPath.eval(json);
```

## JSON Pointer / Patch

```java
// JSON Pointer (RFC 6901) — 定位值
JSONPointer ptr = JSONPointer.of("/store/book/0/title");
String title = ptr.eval(doc, String.class);
ptr.set(doc, "New Title");
ptr.remove(doc);

// JSON Patch (RFC 6902) — 批量修改
String patchResult = JSONPatch.apply(target,
    "[{\"op\":\"add\",\"path\":\"/x\",\"value\":1}]");
```

## 常见问题快速解决

| 问题 | 解决方案 |
|------|----------|
| 字段名不匹配 | `.enableRead(ReadFeature.SupportSmartMatch)` 或 `@JSONField(name="...")` |
| 日期格式不对 | `@JSONField(format="yyyy-MM-dd")` |
| 不输出 null | `.enableWrite(WriteFeature.WriteNulls)` |
| 忽略字段 | `@JSONField(serialize=false)` |
| 泛型解析 | 使用 `TypeToken<List<User>>` |
| 循环引用 | 使用 `@JSONField(serialize=false)` 或引用检测 |

## 性能优化

```java
// 复用 Mapper（最重要的优化）
private static final ObjectMapper MAPPER = ObjectMapper.shared();

// 使用 byte[]（比 toJSONString 快）
byte[] json = JSON.toJSONBytes(obj);
```

> **注意**: 默认配置经 JIT 深度内联后比 ASM 快 10-13%，无需额外配置。
> ASM 仅作为跨 ClassLoader 等兼容性场景的后备方案。

## 泛型处理

```java
// List
TypeToken<List<User>> type = new TypeToken<List<User>>() {};
List<User> users = mapper.readValue(json, type);

// Map<String, User>
TypeToken<Map<String, User>> type = new TypeToken<Map<String, User>>() {};
Map<String, User> map = mapper.readValue(json, type);

// 复杂泛型
TypeToken<Map<String, List<User>>> type =
    new TypeToken<Map<String, List<User>>>() {};
```

## 日期处理

```java
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;

@JSONField(format = "yyyy-MM-dd")
private LocalDate date;

@JSONField(format = "millis")
private Date timestamp;  // 时间戳

// Java Time API
@JSONField(format = "yyyy-MM-dd")
private LocalDate localDate;
```

## 安全配置

```java
// 生产环境推荐
ObjectMapper.builder()
    .disableRead(ReadFeature.SupportAutoType)       // 禁用 AutoType
    .disableRead(ReadFeature.AllowComments)         // 禁用注释
    .enableRead(ReadFeature.ErrorOnUnknownProperties) // 严格模式
    .build();
```

## 过滤器

```java
// 过滤字段
PropertyFilter filter = (obj, name, val) ->
    !Set.of("password", "token").contains(name);
String json = JSON.toJSONString(obj, filter);

// 脱敏
ValueFilter desensitize = (obj, name, val) -> {
    if (name.equals("phone") && val instanceof String) {
        String s = (String) val;
        return s.substring(0, 3) + "****" + s.substring(7);
    }
    return val;
};
```

## 完整示例模板

```java
// 1. 定义类
@JSONType(naming = NamingStrategy.SnakeCase)
public class User {
    @JSONField(name = "id")
    private Long userId;

    @JSONField(name = "name", format = "trim")
    private String userName;

    // getters & setters
}

// 2. 创建 Mapper
private static final ObjectMapper MAPPER = ObjectMapper.builder()
    .enableRead(ReadFeature.SupportSmartMatch)
    .enableWrite(WriteFeature.PrettyFormat)
    .build();

// 3. 使用
String json = MAPPER.writeValueAsString(user);
User user = MAPPER.readValue(json, User.class);
```

## 错误消息速查

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| `NumberFormatException` | 数字格式错误 | 检查字段值 |
| `JSONException: syntax error` | JSON 语法错误 | 用 `JSON.isValid()` 验证 |
| `NullPointerException` | 字段为 null | 使用 `getString("key", default)` |
| `undefined` | 字段不存在 | 检查字段名 |

## 更多信息

- [📚 完整文档](README.md)
- [📖 入门教程](start/)
- [📖 场景指南](guides/)
- [❓ 常见问题](faq.md)

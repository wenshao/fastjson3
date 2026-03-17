# 解析 JSON 基础

> 📚 本系列教程：
> - [← 上一节：5分钟上手](00-5-minutes.md) | [下一节：序列化基础 →](02-basic-serialize.md)

## 基础解析

### 解析为 Java 对象

```java
String json = """
    {
        "name": "张三",
        "age": 25,
        "email": "zhangsan@example.com"
    }
    """;

User user = JSON.parseObject(json, User.class);
System.out.println(user.getName());  // 张三
```

### 解析为列表

```java
String json = """
    [
        {"name": "张三", "age": 25},
        {"name": "李四", "age": 30}
    ]
    """;

List<User> users = JSON.parseArray(json, User.class);
```

### 解析为动态对象

```java
// 不确定 JSON 结构时使用
JSONObject obj = JSON.parseObject(json);

String name = obj.getString("name");
int age = obj.getIntValue("age");
Boolean vip = obj.getBooleanValue("vip");

// 安全获取（不存在时返回默认值）
String city = obj.getString("city", "未知");
```

## 处理复杂结构

### 嵌套对象

```java
String json = """
    {
        "name": "张三",
        "address": {
            "city": "北京",
            "district": "朝阳区"
        }
    }
    """;

User user = JSON.parseObject(json, User.class);
// user.address.city 会自动映射
```

### 嵌套列表

```java
public class User {
    public String name;
    public List<String> tags;  // 自动映射
}

String json = """
    {
        "name": "张三",
        "tags": ["Java", "Python", "Go"]
    }
    """;
```

### 泛型集合

```java
// Map<String, User>
String json = "{\"user1\":{\"name\":\"张三\"}}";
TypeToken<Map<String, User>> type = new TypeToken<Map<String, User>>() {};
Map<String, User> users = JSON.parseObject(json, type);

// 更复杂的泛型
TypeToken<Map<String, List<User>>> type =
    new TypeToken<Map<String, List<User>>>() {};
```

## 配置解析行为

### 允许宽松格式

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(
        ReadFeature.AllowComments,      // 允许注释
        ReadFeature.AllowSingleQuotes,  // 允许单引号
        ReadFeature.AllowUnquotedFieldNames  // 允许无引号字段
    )
    .build();

// 现在可以解析：
String json = """
    {
        // 这是注释
        name: "张三",      // 无引号字段
        'age': 25         // 单引号
    }
    """;
```

### 严格模式

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.ErrorOnUnknownProperties)  // 未知字段报错
    .build();
```

## 字段名匹配策略

### 智能匹配（默认）

```java
// JSON 中的字段会自动匹配：
// user_name, userName, UserName → 都能匹配 userName 字段
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.SupportSmartMatch)
    .build();
```

### 注解指定名称

```java
public class User {
    @JSONField(name = "user_id")    // JSON → Java
    private Long userId;

    @JSONField(alternateNames = {"email", "emailAddress"})  // 多个可能的名称
    private String mail;
}
```

### 忽略字段

```java
public class User {
    @JSONField(deserialize = false)  // 反序列化时忽略
    private String internalId;

    // 或在类级别
}
@JSONType(ignores = {"password", "salt"})
public class User {
    private String password;
    private String salt;
}
```

## 常见问题

### Q: 解析时字段不存在会报错吗？

A: 不会。默认情况下，JSON 中多余的字段会被忽略。如需严格模式，启用 `ErrorOnUnknownProperties`。

### Q: 如何处理 null 值？

A: null 值会被正常解析。如果字段是 `int` 等基本类型，会使用默认值（0）。

### Q: 日期格式不对怎么办？

A: 使用 `@JSONField(format = "...")` 指定格式：

```java
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;
```

## 完整示例

```java
public class Demo {
    public static void main(String[] args) {
        // JSON 字符串
        String json = """
            {
                "name": "张三",
                "age": 25,
                "vip": true,
                "tags": ["Java", "Python"],
                "address": {
                    "city": "北京",
                    "street": "朝阳区"
                }
            }
            """;

        // 解析
        User user = JSON.parseObject(json, User.class);

        // 访问
        System.out.println(user.getName());
        System.out.println(user.getAddress().getCity());

        // 使用动态对象
        JSONObject obj = JSON.parseObject(json);
        String name = obj.getString("name");
        JSONArray tags = obj.getJSONArray("tags");
    }
}
```

## 下一步

- 📖 [学习序列化 →](02-basic-serialize.md)
- 📖 [JSONPath 查询 →](../guides/jsonpath.md)
- 📋 [JSON API 完整参考 →](../api/JSON.md)

**快速提示：**
- 使用 `ObjectMapper.shared()` 获得更好的性能
- 不确定结构时用 `JSONObject`
- 复杂泛型用 `TypeToken`

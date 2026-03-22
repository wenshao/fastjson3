# 从 Gson 迁移到 fastjson3

Gson 和 fastjson3 的 API 风格不同，但核心概念相似。

## API 对照表

### 核心对照

| Gson | fastjson3 | 说明 |
|------|-----------|------|
| `new Gson()` | `ObjectMapper.shared()` | 使用共享实例 |
| `gson.fromJson(json, User.class)` | `mapper.readValue(json, User.class)` | 方法名不同 |
| `gson.toJson(obj)` | `mapper.writeValueAsString(obj)` | 方法名不同 |
| `new GsonBuilder().create()` | `ObjectMapper.builder().build()` | 构建模式 |

### 注解对照

| Gson 注解 | fastjson3 等价 |
|-----------|--------------|
| `@SerializedName("name")` | `@JSONField(name = "name")` |
| `@Expose` | `@JSONField(serialize = true)` |
| `@Since(1.0)` | 不支持，使用版本控制 |

## 代码迁移示例

### 基础读写

```java
// ===== Gson =====
Gson gson = new Gson();
User user = gson.fromJson(json, User.class);
String output = gson.toJson(user);

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);
String output = mapper.writeValueAsString(user);
```

### 泛型处理

```java
// ===== Gson =====
Type userListType = new TypeToken<List<User>>() {}.getType();
List<User> users = gson.fromJson(json, userListType);

// ===== fastjson3 方式1：TypeReference =====
TypeReference<List<User>> typeRef = new TypeReference<>() {};
List<User> users = JSON.parseObject(json, typeRef);

// ===== fastjson3 方式2：TypeToken（推荐） =====
TypeToken<List<User>> typeToken = TypeToken.listOf(User.class);
List<User> users = JSON.parse(json, typeToken);
```

### 注解迁移

```java
// ===== Gson =====
public class User {
    @SerializedName("user_name")
    private String userName;

    @SerializedName("user_age")
    private int age;
}

// ===== fastjson3 =====
public class User {
    @JSONField(name = "user_name")
    private String userName;

    @JSONField(name = "user_age")
    private int age;
}
```

## 日期处理

### Gson 日期配置

```java
// Gson: 全局配置
Gson gson = new GsonBuilder()
    .setDateFormat("yyyy-MM-dd HH:mm:ss")
    .create();
```

### fastjson3 等价配置

```java
// 方式1：注解（推荐）
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;

// 方式2：ObjectMapper（日期格式通过注解配置，不支持 .dateFormat()）
ObjectMapper mapper = ObjectMapper.builder()
    .build();
```

## GsonBuilder 配置迁移

### Gson 常用配置

```java
Gson gson = new GsonBuilder()
    .setPrettyPrinting()           // 美化输出
    .serializeNulls()              // 序列化 null
    .disableHtmlEscaping()         // 禁用 HTML 转义
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)  // 命名策略
    .create();
```

### fastjson3 等价配置

```java
// 命名策略请使用 @JSONType(naming = NamingStrategy.SnakeCase) 注解
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)      // 美化输出
    .enableWrite(WriteFeature.WriteNulls)         // 序列化 null
    .disableWrite(WriteFeature.EscapeNoneAscii)   // 禁用 HTML 转义
    .build();
```

## JsonElement 迁移

Gson 的 `JsonElement` 层次结构：

```java
// ===== Gson =====
JsonElement element = JsonParser.parseString(json);
if (element.isJsonObject()) {
    JsonObject obj = element.getAsJsonObject();
    String name = obj.get("name").getAsString();
}
```

### fastjson3 等价操作

```java
// ===== fastjson3 =====
JSONObject obj = JSON.parseObject(json);
String name = obj.getString("name");

// 或者使用 JSONPath
String name = JSONPath.eval(json, "$.name", String.class);
```

## 完整示例

```java
// ===== Gson =====
public class Example {
    public static void main(String[] args) {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

        User user = gson.fromJson(json, User.class);
        String output = gson.toJson(user);
    }
}

// ===== fastjson3 =====
public class Example {
    public static void main(String[] args) {
        ObjectMapper mapper = ObjectMapper.builder()
            .enableWrite(WriteFeature.PrettyFormat)
            .build();

        User user = mapper.readValue(json, User.class);
        String output = mapper.writeValueAsString(user);
    }
}
```

## 迁移步骤

1. **替换依赖**

```xml
<!-- 移除 Gson -->
<!--<dependency>-->
<!--    <groupId>com.google.code.gson</groupId>-->
<!--    <artifactId>gson</artifactId>-->
<!--</dependency>-->

<!-- 添加 fastjson3 -->
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

2. **更新导入**

```java
// 移除
// import com.google.gson.*;
// import com.google.gson.reflect.TypeToken;

// 添加
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeToken;
```

3. **替换注解**

   将 `@SerializedName` 替换为 `@JSONField`。

4. **更新 API 调用**

   按照对照表更新 API 调用。

5. **测试**

   运行所有测试确保功能正常。

## 常见问题

### Q: Gson 的 TypeToken 怎么办？

A: fastjson3 也有 `TypeToken`，但用法不同。使用工厂方法如 `TypeToken.listOf(Class)`。

### Q: JsonParser 怎么替换？

A: 使用 `JSON.parseObject()` 或 `JSON.parseArray()`。

### Q: Gson 的 excludeFieldsWithModifiers 怎么办？

A: 使用 `@JSONField(serialize = false)` 或 `@JSONType(ignores = {...})`。

## 相关文档

- [ObjectMapper 文档](../api/ObjectMapper.md)
- [注解参考](../api/annotations.md)
- [迁移检查清单](checklist.md)

# 从 org.json 迁移到 fastjson3

org.json 是一个简单的 JSON 库，fastjson3 提供了更强大的功能。

## API 对照表

### 核心对照

| org.json | fastjson3 | 说明 |
|----------|-----------|------|
| `new JSONObject(json)` | `JSON.parseObject(json)` | 静态方法 |
| `new JSONArray(json)` | `JSON.parseArray(json)` | 静态方法 |
| `obj.getString("key")` | `obj.getString("key")` | 相同 |
| `obj.getInt("key")` | `obj.getIntValue("key")` | 方法名不同 |
| `obj.getLong("key")` | `obj.getLong("key")` | 相同 |
| `obj.getDouble("key")` | `obj.getDouble("key")` | 相同 |
| `obj.getBoolean("key")` | `obj.getBooleanValue("key")` | 方法名不同 |
| `obj.optXXX("key", def)` | `obj.getXXX("key", def)` | 默认值方式不同 |
| `obj.put("key", value)` | `obj.put("key", value)` | 相同 |
| `obj.has("key")` | `obj.containsKey("key")` | 方法名不同 |
| `obj.length()` | `obj.size()` | 方法名不同 |
| `obj.names()` | `obj.keySet()` | 返回类型不同 |
| `obj.toString()` | `obj.toJSONString()` | 方法名不同 |

## 代码迁移示例

### JSONObject 基础操作

```java
// ===== org.json =====
JSONObject obj = new JSONObject(json);
String name = obj.getString("name");
int age = obj.getInt("age");
boolean isActive = obj.getBoolean("active");
obj.put("city", "北京");
String result = obj.toString();

// ===== fastjson3 =====
JSONObject obj = JSON.parseObject(json);
String name = obj.getString("name");
int age = obj.getIntValue("age");
boolean isActive = obj.getBooleanValue("active");
obj.put("city", "北京");
String result = obj.toJSONString();
```

### JSONArray 操作

```java
// ===== org.json =====
JSONArray arr = new JSONArray(json);
String first = arr.getString(0);
int length = arr.length();
JSONObject item = arr.getJSONObject(0);

// ===== fastjson3 =====
JSONArray arr = JSON.parseArray(json);
String first = arr.getString(0);
int length = arr.size();
JSONObject item = arr.getJSONObject(0);
```

### 遍历 JSONObject

```java
// ===== org.json =====
JSONObject obj = new JSONObject(json);
JSONArray keys = obj.names();
for (int i = 0; i < keys.length(); i++) {
    String key = keys.getString(i);
    Object value = obj.get(key);
}

// ===== fastjson3 方式1 =====
JSONObject obj = JSON.parseObject(json);
for (String key : obj.keySet()) {
    Object value = obj.get(key);
}

// ===== fastjson3 方式2（推荐）=====
JSONObject obj = JSON.parseObject(json);
obj.forEach((key, value) -> {
    // 处理每个键值对
});
```

### 遍历 JSONArray

```java
// ===== org.json =====
JSONArray arr = new JSONArray(json);
for (int i = 0; i < arr.length(); i++) {
    JSONObject item = arr.getJSONObject(i);
}

// ===== fastjson3 =====
JSONArray arr = JSON.parseArray(json);
for (int i = 0; i < arr.size(); i++) {
    JSONObject item = arr.getJSONObject(i);
}

// 或者使用 forEach
arr.forEach(item -> {
    JSONObject obj = (JSONObject) item;
});
```

### 默认值处理

```java
// ===== org.json =====
String name = obj.optString("name", "未知");
int age = obj.optInt("age", 0);

// ===== fastjson3 =====
String name = obj.getString("name");   // null if missing
int age = obj.getIntValue("age");      // 0 if missing (基本类型方法有默认值)
```

### JSONPointer 迁移

org.json 的 JSONPointer：

```java
// ===== org.json =====
JSONPointer pointer = new JSONPointer("$.store.book[0].title");
Object result = pointer.queryFrom(jsonObject);

// ===== fastjson3 =====
String result = JSONPath.eval(json, "$.store.book[0].title", String.class);

// 或者使用预编译路径
JSONPath path = JSONPath.of("$.store.book[0].title");
String result = path.eval(json, String.class);
```

### 对象序列化

```java
// ===== org.json =====
// org.json 没有直接的对象序列化，需要手动构建
JSONObject obj = new JSONObject();
obj.put("name", "张三");
obj.put("age", 25);

// ===== fastjson3 =====
User user = new User("张三", 25);
String json = JSON.toJSONString(user);

// 或者使用 ObjectMapper
ObjectMapper mapper = ObjectMapper.shared();
String json = mapper.writeValueAsString(user);
```

### 类型转换

```java
// ===== org.json =====
// org.json 需要手动转换
JSONObject obj = new JSONObject(json);
User user = new User();
user.setName(obj.getString("name"));
user.setAge(obj.getInt("age"));

// ===== fastjson3 =====
// 直接转换
User user = JSON.parseObject(json, User.class);

// 或者使用 ObjectMapper
User user = mapper.readValue(json, User.class);
```

## 完整示例对比

```java
// ===== org.json 完整示例 =====
public class OrgJsonExample {
    public static void main(String[] args) throws Exception {
        // 解析
        JSONObject obj = new JSONObject(jsonString);

        // 读取
        String name = obj.getString("name");
        int age = obj.getInt("age");

        // 检查
        if (obj.has("email")) {
            String email = obj.getString("email");
        }

        // 修改
        obj.put("city", "北京");

        // 数组
        JSONArray arr = obj.getJSONArray("items");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject item = arr.getJSONObject(i);
        }

        // 输出
        String result = obj.toString(2);  // 缩进2
    }
}

// ===== fastjson3 等价示例 =====
public class Fastjson3Example {
    public static void main(String[] args) {
        // 解析
        JSONObject obj = JSON.parseObject(jsonString);

        // 读取
        String name = obj.getString("name");
        int age = obj.getIntValue("age");

        // 检查
        String email = obj.getString("email");  // null 如果不存在
        if (email != null) {
            // 处理
        }

        // 修改
        obj.put("city", "北京");

        // 数组
        JSONArray arr = obj.getJSONArray("items");
        for (int i = 0; i < arr.size(); i++) {
            JSONObject item = arr.getJSONObject(i);
        }

        // 输出（带格式）
        String result = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
    }
}
```

## 迁移步骤

1. **替换依赖**

```xml
<!-- 移除 org.json -->
<!--<dependency>-->
<!--    <groupId>org.json</groupId>-->
<!--    <artifactId>json</artifactId>-->
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
// import org.json.*;

// 添加
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.JSONArray;
```

3. **更新 API 调用**

   按照对照表更新方法名：
   - `length()` → `size()`
   - `has()` → `containsKey()`
   - `toString()` → `toJSONString()`
   - `getInt()` → `getIntValue()`
   - `getBoolean()` → `getBooleanValue()`

4. **测试**

   运行所有测试确保功能正常。

## 常见问题

### Q: JSONException 怎么处理？

A: fastjson3 使用 `RuntimeException`，通常不需要显式捕获。

### Q: optXXX 方法等价于什么？

A: 使用带默认值的 `getXXX("key", defaultValue)` 方法。

### Q: 如何遍历 JSONObject？

A: 使用 `forEach()` 或增强 for 循环遍历 `keySet()`。

### Q: JSONPointer 如何替换？

A: 使用 `JSONPath`，功能更强大。

## 相关文档

- [JSON API 文档](../api/JSON.md)
- [JSONObject 文档](../api/JSONObject.md)
- [JSONPath 指南](../guides/jsonpath.md)

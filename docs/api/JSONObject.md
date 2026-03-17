# JSONObject 类参考

动态 JSON 对象，用于处理未知结构的 JSON 数据。

## 创建实例

```java
// 空对象
JSONObject obj = new JSONObject();

// 从 JSON 字符串创建
JSONObject obj = JSON.parseObject(jsonString);

// 从 Map 创建
JSONObject obj = new JSONObject(map);
```

## 添加值

```java
JSONObject obj = new JSONObject();

// 基本类型
obj.put("name", "张三");
obj.put("age", 25);
obj.put("vip", true);
obj.put("balance", 99.99);

// 对象
obj.put("address", new JSONObject());
obj.put("tags", new JSONArray());

// null
obj.put("nullable", null);
```

## 获取值

### 基本获取

```java
JSONObject obj = JSON.parseObject("{\"name\":\"张三\",\"age\":25}");

// 自动类型转换
String name = obj.getString("name");
int age = obj.getIntValue("age");
long id = obj.getLongValue("id");
double score = obj.getDoubleValue("score");
boolean vip = obj.getBooleanValue("vip");
```

### 安全获取（带默认值）

```java
// 如果不存在，返回默认值
String name = obj.getString("name", "Unknown");
int age = obj.getIntValue("age", 0);
boolean vip = obj.getBooleanValue("vip", false);
```

### 获取对象

```java
// 获取嵌套对象
JSONObject address = obj.getJSONObject("address");
JSONArray tags = obj.getJSONArray("tags");

// 转换为 Java 对象
User user = obj.toJavaObject(User.class);

// 获取任意类型
Object value = obj.get("key");
```

## 检查和查询

```java
// 检查键是否存在
boolean hasName = obj.containsKey("name");
boolean isEmpty = obj.isEmpty();
int size = obj.size();

// 获取所有键
Set<String> keys = obj.keySet();

// 获取所有值
Collection<Object> values = obj.values();

// 获取条目
Set<Map.Entry<String, Object>> entries = obj.entrySet();
```

## 修改值

```java
JSONObject obj = new JSONObject();

// 添加或替换
obj.put("name", "张三");
obj.put("name", "李四");  // 替换

// 仅在不存在时添加
obj.putIfAbsent("name", "王五");

// 删除
Object removed = obj.remove("name");

// 清空
obj.clear();
```

## 遍历

```java
JSONObject obj = JSON.parseObject("{\"name\":\"张三\",\"age\":25}");

// 方式1：forEach
obj.forEach((key, value) -> {
    System.out.println(key + " = " + value);
});

// 方式2：entrySet
for (Map.Entry<String, Object> entry : obj.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}

// 方式3：keySet
for (String key : obj.keySet()) {
    Object value = obj.get(key);
}
```

## 类型转换

```java
JSONObject obj = JSON.parseObject("{\"name\":\"张三\",\"age\":25}");

// 转为 JSON 字符串
String json = obj.toString();
String pretty = obj.toJSONString(JSONWriter.Feature.PrettyFormat);

// 转为 byte[]
byte[] bytes = obj.toJSONBytes();

// 转为 Map
Map<String, Object> map = obj.getInnerMap();

// 转为 Java 对象
User user = obj.toJavaObject(User.class);
```

## 嵌套操作

```java
JSONObject obj = new JSONObject();
obj.put("user", new JSONObject());
obj.getJSONObject("user").put("name", "张三");

// 链式操作
obj.getJSONObject("address")
   .put("city", "北京")
   .put("district", "朝阳区");

// 获取嵌套值
String city = obj.getJSONObject("address").getString("city");
```

## 与 JSONArray 配合

```java
JSONObject obj = new JSONObject();
obj.put("items", new JSONArray());

JSONArray items = obj.getJSONArray("items");
items.add("item1");
items.add(123);
```

## JSONPath 查询

```java
JSONObject obj = JSON.parseObject(jsonString);

// 使用 JSONPath 查询
JSONPath path = JSONPath.of("$.user.name");
String name = path.eval(obj, String.class);

// 或使用便捷方法
Object result = obj.eval("$.user.address.city");
```

## 常用方法速查

| 方法 | 描述 | 示例 |
|------|------|------|
| `put(String, Object)` | 添加/替换值 | `obj.put("key", "value")` |
| `get(String)` | 获取任意值 | `obj.get("key")` |
| `getString(String)` | 获取字符串 | `obj.getString("name")` |
| `getString(String, String)` | 获取字符串（带默认） | `obj.getString("name", "")` |
| `getIntValue(String)` | 获取 int | `obj.getIntValue("age")` |
| `getLongValue(String)` | 获取 long | `obj.getLongValue("id")` |
| `getDoubleValue(String)` | 获取 double | `obj.getDoubleValue("price")` |
| `getBooleanValue(String)` | 获取 boolean | `obj.getBooleanValue("vip")` |
| `getJSONObject(String)` | 获取嵌套对象 | `obj.getJSONObject("addr")` |
| `getJSONArray(String)` | 获取嵌套数组 | `obj.getJSONArray("tags")` |
| `containsKey(String)` | 检查键存在 | `obj.containsKey("name")` |
| `remove(String)` | 删除键 | `obj.remove("key")` |
| `size()` | 获取大小 | `obj.size()` |
| `isEmpty()` | 检查是否为空 | `obj.isEmpty()` |
| `clear()` | 清空 | `obj.clear()` |
| `toJavaObject(Class)` | 转为 Java 对象 | `obj.toJavaObject(User.class)` |

## 线程安全

**JSONObject 不是线程安全的。**

```java
// ❌ 不好：多线程共享
JSONObject shared = new JSONObject();
// 多个线程同时修改 -> 数据损坏

// ✅ 好：每个线程创建自己的
// 或使用同步
synchronized (obj) {
    obj.put("key", "value");
}

// ✅ 更好：使用不可变的 ObjectMapper
ObjectMapper mapper = ObjectMapper.shared();
```

## 性能建议

1. **避免频繁创建**
```java
// ❌ 不好
for (Item item : items) {
    JSONObject obj = new JSONObject();  // 循环内创建
    obj.put("name", item.getName());
}

// ✅ 好：复用或直接映射
List<Item> result = items;  // 直接使用
```

2. **使用类型化对象**
```java
// ❌ 可能不好：大量动态操作
JSONObject obj = /* ... */;
for (int i = 0; i < 10000; i++) {
    obj.getString("field" + i);  // 动态键名
}

// ✅ 好：使用 Java 对象
MyData data = obj.toJavaObject(MyData.class);
```

## 完整示例

```java
public class Demo {
    public static void main(String[] args) {
        // 创建对象
        JSONObject user = new JSONObject();
        user.put("name", "张三");
        user.put("age", 25);
        user.put("vip", true);

        // 嵌套对象
        JSONObject address = new JSONObject();
        address.put("city", "北京");
        address.put("district", "朝阳区");
        user.put("address", address);

        // 数组
        JSONArray tags = new JSONArray();
        tags.add("Java");
        tags.add("Python");
        user.put("tags", tags);

        // 输出 JSON
        System.out.println(user.toJSONString());
        // {"name":"张三","age":25,"vip":true,"address":{"city":"北京","district":"朝阳区"},"tags":["Java","Python"]}

        // 获取值
        String name = user.getString("name");
        String city = user.getJSONObject("address").getString("city");

        // 转换为 Java 对象
        User userObj = user.toJavaObject(User.class);

        // 遍历
        user.forEach((key, value) -> {
            System.out.println(key + " = " + value);
        });
    }
}
```

## 相关文档

- [📋 JSONArray 参考 →](JSONArray.md)
- [📋 JSON 类参考 →](JSON.md)
- [📖 解析基础 →](../start/01-basic-parse.md)

# JSONObject 类参考

动态 JSON 对象，用于处理未知结构的 JSON 数据。

## 创建实例

```java
// 空对象（默认使用优化的 JSONObjectMap 存储）
JSONObject obj = new JSONObject();

// 从 JSON 字符串创建
JSONObject obj = JSON.parseObject(jsonString);

// 从 Map 创建（拷贝到 LinkedHashMap）
JSONObject obj = new JSONObject(map);

// 自定义内部存储（fastjson2 ObjectSupplier 等价物）
JSONObject obj = new JSONObject(java.util.concurrent.ConcurrentHashMap::new);
// 此时 obj 内部 Map 是 ConcurrentHashMap，所有 put/get/iterate 都委托给它
```

### 自定义 Map 后备存储

三种切换内部 Map 的方式：

| 方式 | 作用域 | API |
|---|---|---|
| `JSONObject.setMapCreator(Supplier)` | 全局静态 | 影响所有 `new JSONObject()` |
| `ObjectMapper.builder().mapSupplier(...)` | per-mapper | 仅影响该 mapper 解析出的 JSONObject |
| `new JSONObject(Supplier)` | per-instance | 直接 ctor 注入 |

详见 [ObjectMapper § 自定义 Map / List 后备存储](./ObjectMapper.md#自定义-map--list-后备存储)。

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

### 便捷构建方法

```java
JSONObject obj = new JSONObject();

// putObject：创建并放入子 JSONObject，返回新创建的子对象
JSONObject address = obj.putObject("address");
address.put("city", "北京");
address.put("district", "朝阳区");

// putArray：创建并放入子 JSONArray，返回新创建的子数组
JSONArray tags = obj.putArray("tags");
tags.add("Java");
tags.add("Python");
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

### null 处理

```java
// getString 返回 null，getIntValue 返回 0（基本类型方法有默认值）
String name = obj.getString("name");       // null if missing
int age = obj.getIntValue("age");          // 0 if missing
boolean vip = obj.getBooleanValue("vip");  // false if missing
```

### 类型化获取

```java
// 获取并转换为指定类型
User user = obj.getObject("user", User.class);

// 获取 List 并转换元素类型
List<String> tags = obj.getList("tags", String.class);
List<User> users = obj.getList("users", User.class);

// 获取 byte[]（Base64 解码）
byte[] data = obj.getBytes("avatar");

// 获取 Byte / Short（包装类型，key 不存在时返回 null）
Byte flag = obj.getByte("flag");
Short code = obj.getShort("code");

// 获取 byte / short（基本类型，key 不存在时返回 0）
byte flagValue = obj.getByteValue("flag");
short codeValue = obj.getShortValue("code");
```

### 日期时间获取

```java
// java.util.Date
Date createTime = obj.getDate("createTime");

// java.time 类型
Instant instant = obj.getInstant("timestamp");
LocalDate birthday = obj.getLocalDate("birthday");
LocalDateTime createAt = obj.getLocalDateTime("createAt");
LocalTime startTime = obj.getLocalTime("startTime");
OffsetDateTime eventTime = obj.getOffsetDateTime("eventTime");
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

### JSONPath 查询

```java
// 使用 getByPath 进行 JSONPath 查询
Object city = obj.getByPath("$.address.city");
Object firstTag = obj.getByPath("$.tags[0]");
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
String pretty = obj.toJSONString(WriteFeature.PrettyFormat);

// 转为 byte[]
byte[] bytes = JSON.toJSONBytes(obj);

// JSONObject extends LinkedHashMap，可直接当 Map 使用（默认情况下操作委托给优化的内部 innerMap）

// 转为 Java 对象
User user = obj.toJavaObject(User.class);

// 泛型类型转换
Map<String, List<User>> result = obj.toJavaObject(
    new TypeReference<Map<String, List<User>>>() {}
);

// 使用 Type 转换
Type type = new TypeReference<Map<String, User>>() {}.getType();
Map<String, User> map = obj.toJavaObject(type);
```

## 嵌套操作

```java
JSONObject obj = new JSONObject();

// 使用 putObject 便捷创建嵌套对象
JSONObject address = obj.putObject("address");
address.put("city", "北京");
address.put("district", "朝阳区");

// 使用 putArray 便捷创建嵌套数组
JSONArray tags = obj.putArray("tags");
tags.add("Java");
tags.add("Python");

// 获取嵌套值
String city = obj.getJSONObject("address").getString("city");
```

## 与 JSONArray 配合

```java
JSONObject obj = new JSONObject();
JSONArray items = obj.putArray("items");
items.add("item1");
items.add(123);
```

## JSONPath 查询

```java
JSONObject obj = JSON.parseObject(jsonString);

// 使用 JSONPath 查询
JSONPath path = JSONPath.of("$.user.name");
String name = path.eval(obj, String.class);

// 使用 getByPath 便捷方法
Object result = obj.getByPath("$.user.address.city");
```

## 常用方法速查

| 方法 | 描述 | 示例 |
|------|------|------|
| `put(String, Object)` | 添加/替换值 | `obj.put("key", "value")` |
| `putObject(String)` | 创建并放入子 JSONObject | `obj.putObject("addr")` |
| `putArray(String)` | 创建并放入子 JSONArray | `obj.putArray("tags")` |
| `get(String)` | 获取任意值 | `obj.get("key")` |
| `getString(String)` | 获取字符串 | `obj.getString("name")` |
| `getInteger(String)` | 获取 Integer | `obj.getInteger("age")` |
| `getIntValue(String)` | 获取 int | `obj.getIntValue("age")` |
| `getLong(String)` | 获取 Long | `obj.getLong("id")` |
| `getLongValue(String)` | 获取 long | `obj.getLongValue("id")` |
| `getDoubleValue(String)` | 获取 double | `obj.getDoubleValue("price")` |
| `getBooleanValue(String)` | 获取 boolean | `obj.getBooleanValue("vip")` |
| `getByte(String)` | 获取 Byte | `obj.getByte("flag")` |
| `getByteValue(String)` | 获取 byte | `obj.getByteValue("flag")` |
| `getShort(String)` | 获取 Short | `obj.getShort("code")` |
| `getShortValue(String)` | 获取 short | `obj.getShortValue("code")` |
| `getBytes(String)` | 获取 byte[]（Base64 解码） | `obj.getBytes("data")` |
| `getObject(String, Class)` | 获取并转换为指定类型 | `obj.getObject("user", User.class)` |
| `getList(String, Class)` | 获取 List 并转换元素类型 | `obj.getList("items", Item.class)` |
| `getDate(String)` | 获取 Date | `obj.getDate("time")` |
| `getInstant(String)` | 获取 Instant | `obj.getInstant("ts")` |
| `getLocalDate(String)` | 获取 LocalDate | `obj.getLocalDate("birthday")` |
| `getLocalDateTime(String)` | 获取 LocalDateTime | `obj.getLocalDateTime("createAt")` |
| `getLocalTime(String)` | 获取 LocalTime | `obj.getLocalTime("start")` |
| `getOffsetDateTime(String)` | 获取 OffsetDateTime | `obj.getOffsetDateTime("event")` |
| `getJSONObject(String)` | 获取嵌套对象 | `obj.getJSONObject("addr")` |
| `getJSONArray(String)` | 获取嵌套数组 | `obj.getJSONArray("tags")` |
| `getByPath(String)` | JSONPath 查询 | `obj.getByPath("$.user.name")` |
| `containsKey(String)` | 检查键存在 | `obj.containsKey("name")` |
| `remove(String)` | 删除键 | `obj.remove("key")` |
| `size()` | 获取大小 | `obj.size()` |
| `isEmpty()` | 检查是否为空 | `obj.isEmpty()` |
| `clear()` | 清空 | `obj.clear()` |
| `toJavaObject(Class)` | 转为 Java 对象 | `obj.toJavaObject(User.class)` |
| `toJavaObject(Type)` | 泛型类型转换 | `obj.toJavaObject(type)` |
| `toJavaObject(TypeReference)` | TypeReference 转换 | `obj.toJavaObject(new TypeReference<>(){})` |

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

3. **使用 putObject/putArray 构建嵌套结构**
```java
// ✅ 好：使用便捷方法，代码更简洁
JSONObject root = new JSONObject();
root.putObject("user").put("name", "张三");
root.putArray("tags").add("Java");

// ❌ 不好：手动创建和放入
JSONObject root = new JSONObject();
JSONObject user = new JSONObject();
user.put("name", "张三");
root.put("user", user);
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

        // 使用便捷方法创建嵌套结构
        JSONObject address = user.putObject("address");
        address.put("city", "北京");
        address.put("district", "朝阳区");

        JSONArray tags = user.putArray("tags");
        tags.add("Java");
        tags.add("Python");

        // 输出 JSON
        System.out.println(user.toJSONString());
        // {"name":"张三","age":25,"vip":true,"address":{"city":"北京","district":"朝阳区"},"tags":["Java","Python"]}

        // 获取值
        String name = user.getString("name");
        String city = user.getJSONObject("address").getString("city");

        // 类型化获取
        User userObj = user.toJavaObject(User.class);
        List<String> tagList = user.getList("tags", String.class);

        // JSONPath 查询
        Object district = user.getByPath("$.address.district");

        // 日期时间获取
        JSONObject event = JSON.parseObject("{\"time\":\"2024-01-15T10:30:00\"}");
        LocalDateTime time = event.getLocalDateTime("time");

        // 遍历
        user.forEach((key, value) -> {
            System.out.println(key + " = " + value);
        });
    }
}
```

## 相关文档

- [JSONArray 参考](JSONArray.md)
- [JSON 类参考](JSON.md)
- [解析基础](../start/01-basic-parse.md)

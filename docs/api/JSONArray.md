# JSONArray 类参考

动态 JSON 数组，用于处理 JSON 列表数据。

## 创建实例

```java
// 空数组
JSONArray arr = new JSONArray();

// 从 JSON 字符串创建
JSONArray arr = JSON.parseArray(jsonString);

// 从 List 创建
JSONArray arr = new JSONArray(list);

// 使用元素创建
JSONArray arr = JSON.array("a", "b", "c");
```

## 添加元素

```java
JSONArray arr = new JSONArray();

// 基本类型
arr.add("item");
arr.add(123);
arr.add(true);
arr.add(99.99);

// 对象
arr.add(new JSONObject());
arr.add(new JSONArray());

// null
arr.add((Object) null);

// 在指定位置插入
arr.add(0, "first");
```

## 获取元素

```java
JSONArray arr = JSON.parseArray("[\"a\",\"b\",123,true]");

// 获取任意类型
Object item = arr.get(0);

// 类型化获取
String str = arr.getString(0);
Integer num = arr.getInteger(2);
Long num = arr.getLong(2);
Double d = arr.getDouble(3);
Boolean b = arr.getBoolean(3);

// 获取嵌套对象
JSONObject obj = arr.getJSONObject(0);
JSONArray nested = arr.getJSONArray(1);
```

## 安全获取

```java
// 如果索引越界或值为 null，返回 null / 0
String item = arr.getString(0);
int num = arr.getIntValue(0);
```

## 修改元素

```java
JSONArray arr = new JSONArray();

// 添加元素
arr.add("a");
arr.add("b");

// 替换元素
arr.set(0, "x");

// 删除元素
Object removed = arr.remove(0);
arr.remove(0);  // 返回 boolean
```

## 查询和检查

```java
JSONArray arr = JSON.parseArray("[1,2,3]");

// 大小
int size = arr.size();
boolean isEmpty = arr.isEmpty();

// 检查包含
boolean contains = arr.contains(1);

// 查找元素
int index = arr.indexOf(2);

// 获取子数组
List<Object> sub = arr.subList(0, 2);
```

## 遍历

```java
JSONArray arr = JSON.parseArray("[\"a\",\"b\",\"c\"]");

// 方式1：forEach
arr.forEach(item -> {
    System.out.println(item);
});

// 方式2：fori
for (int i = 0; i < arr.size(); i++) {
    Object item = arr.get(i);
}

// 方式3：增强 for
for (Object item : arr) {
    System.out.println(item);
}
```

## 类型转换

```java
JSONArray arr = JSON.parseArray("[1,2,3]");

// 转为 JSON 字符串
String json = arr.toString();
String pretty = arr.toJSONString(WriteFeature.PrettyFormat);

// 转为 byte[]
byte[] bytes = JSON.toJSONBytes(arr);

// JSONArray extends ArrayList，可直接当 List 使用

// 转为类型化列表
List<String> strings = arr.toJavaList(String.class);
List<User> users = arr.toJavaList(User.class);
```

## 流式操作

```java
JSONArray arr = JSON.parseArray("[1,2,3,4,5]");

// 转为 Stream
arr.stream()
   .map(Object::toString)
   .forEach(System.out::println);

// 过滤
arr.stream()
   .filter(o -> (Integer)o > 2)
   .collect(Collectors.toList());

// 映射
List<String> strings = arr.stream()
   .map(Object::toString)
   .collect(Collectors.toList());
```

## 与 JSONObject 配合

```java
JSONArray arr = new JSONArray();

// 添加对象
JSONObject obj = new JSONObject();
obj.put("name", "张三");
obj.put("age", 25);
arr.add(obj);

// 获取对象
JSONObject first = arr.getJSONObject(0);
String name = first.getString("name");
```

## 数组操作

```java
JSONArray arr = new JSONArray();
arr.add("a");
arr.add("b");
arr.add("c");

// 添加多个元素
arr.addAll(Arrays.asList("d", "e"));

// 清空
arr.clear();

// 排序（需要可比较元素）
// arr.sort(Comparator);
```

## 类型化获取

```java
JSONArray arr = JSON.parseArray("[1,2,3,4,5]");

// 转为 List<Integer>
List<Integer> list = arr.toJavaList(Integer.class);
```

## 常用方法速查

| 方法 | 描述 | 示例 |
|------|------|------|
| `add(Object)` | 添加元素 | `arr.add("item")` |
| `add(int, Object)` | 在指定位置添加 | `arr.add(0, "first")` |
| `get(int)` | 获取元素 | `arr.get(0)` |
| `getString(int)` | 获取字符串 | `arr.getString(0)` |
| `getInteger(int)` | 获取 Integer | `arr.getInteger(0)` |
| `getIntValue(int)` | 获取 int | `arr.getIntValue(0)` |
| `getLong(int)` | 获取 Long | `arr.getLong(0)` |
| `getLongValue(int)` | 获取 long | `arr.getLongValue(0)` |
| `getDouble(int)` | 获取 Double | `arr.getDouble(0)` |
| `getDoubleValue(int)` | 获取 double | `arr.getDoubleValue(0)` |
| `getBoolean(int)` | 获取 Boolean | `arr.getBoolean(0)` |
| `getBooleanValue(int)` | 获取 boolean | `arr.getBooleanValue(0)` |
| `getJSONObject(int)` | 获取嵌套对象 | `arr.getJSONObject(0)` |
| `getJSONArray(int)` | 获取嵌套数组 | `arr.getJSONArray(0)` |
| `set(int, Object)` | 替换元素 | `arr.set(0, "new")` |
| `remove(int)` | 删除元素 | `arr.remove(0)` |
| `size()` | 获取大小 | `arr.size()` |
| `isEmpty()` | 检查是否为空 | `arr.isEmpty()` |
| `clear()` | 清空 | `arr.clear()` |
| `contains(Object)` | 检查包含 | `arr.contains("item")` |
| `indexOf(Object)` | 查找索引 | `arr.indexOf("item")` |
| `toJavaList(Class)` | 转为类型化列表 | `arr.toJavaList(User.class)` |

## 线程安全

**JSONArray 不是线程安全的。**

```java
// ❌ 不好：多线程共享
JSONArray shared = new JSONArray();
// 多个线程同时修改 -> 数据损坏

// ✅ 好：同步或使用线程安全集合
List<Object> syncList = Collections.synchronizedList(new ArrayList<>());
```

## 性能建议

1. **初始容量**
```java
// 如果知道大小，指定初始容量
JSONArray arr = new JSONArray(1000);
```

2. **避免频繁扩容**
```java
// ✅ 好：指定大小
JSONArray arr = new JSONArray(items.size());
arr.addAll(items);
```

3. **使用类型化列表**
```java
// 如果类型确定，直接使用 List
List<User> users = arr.toJavaList(User.class);
// 比动态类型访问更快
```

## 完整示例

```java
public class Demo {
    public static void main(String[] args) {
        // 创建数组
        JSONArray users = new JSONArray();

        // 添加对象
        JSONObject user1 = new JSONObject();
        user1.put("name", "张三");
        user1.put("age", 25);
        users.add(user1);

        JSONObject user2 = new JSONObject();
        user2.put("name", "李四");
        user2.put("age", 30);
        users.add(user2);

        // 输出
        System.out.println(users.toJSONString());
        // [{"name":"张三","age":25},{"name":"李四","age":30}]

        // 遍历
        for (int i = 0; i < users.size(); i++) {
            JSONObject user = users.getJSONObject(i);
            System.out.println(user.getString("name"));
        }

        // 转为 Java 列表
        List<User> userList = users.toJavaList(User.class);

        // 流式操作
        users.stream()
             .map(obj -> ((JSONObject)obj).getString("name"))
             .forEach(System.out::println);
    }
}
```

## 相关文档

- [📋 JSONObject 参考 →](JSONObject.md)
- [📋 JSON 类参考 →](JSON.md)
- [📖 解析基础 →](../start/01-basic-parse.md)

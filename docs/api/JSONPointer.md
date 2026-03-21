# JSONPointer 类参考

`JSONPointer` 实现 JSON Pointer (RFC 6901)，用于定位 JSON 文档中的特定值。

## 类声明

```java
public class JSONPointer {
    public static JSONPointer of(String pointer);

    public Object eval(Object root);
    public <T> T eval(Object root, Class<T> type);
    public void set(Object root, Object value);
    public void remove(Object root);
    public boolean exists(Object root);
    public String getPointer();
    public String[] getTokens();
    public static String escape(String token);
}
```

## 创建

```java
// 从字符串创建
JSONPointer ptr = JSONPointer.of("/store/book/0/title");
```

## 核心方法

### eval — 取值

```java
// 从 JSONObject/JSONArray 中取值
Object value = ptr.eval(doc);

// 带类型转换
String title = ptr.eval(doc, String.class);
```

### set — 设值

```java
// 设置指定路径的值
ptr.set(doc, "New Title");
```

### remove — 删除

```java
// 删除指定路径的值
ptr.remove(doc);
```

## 路径语法

| 表达式 | 含义 |
|--------|------|
| `/foo` | 对象的 `foo` 字段 |
| `/foo/0` | `foo` 数组的第 0 个元素 |
| `/foo/bar/baz` | 嵌套路径 |
| `` (空字符串) | 文档根 |
| `/foo~0bar` | `~` 转义为 `~0` |
| `/foo~1bar` | `/` 转义为 `~1` |

## 示例

```java
String json = "{\"store\":{\"book\":[{\"title\":\"Hello\"}]}}";
JSONObject doc = JSON.parseObject(json);

JSONPointer ptr = JSONPointer.of("/store/book/0/title");

// 取值
String title = ptr.eval(doc, String.class);  // "Hello"

// 设值
ptr.set(doc, "World");

// 删除
ptr.remove(doc);
```

## 相关文档

- [JSON 类参考](JSON.md)
- [JSONPatch 类参考](JSONPatch.md)

# JSONPath 类参考

JSONPath 是一种强大的 JSON 查询语言，fastjson3 实现了 [RFC 9535](https://www.rfc-editor.org/rfc/rfc9535.html) 标准。

## 创建 JSONPath

### 静态方法创建

```java
// 单次使用
JSONPath path = JSONPath.of("$.store.book[0].title");
```

### 预编译创建（推荐）

```java
// 预编译一次，重复使用
JSONPath path = JSONPath.of("$.store.book[*].author");
```

### 从表达式创建

```java
String expression = "$.store.book[*].author";
JSONPath path = JSONPath.of(expression);
```

---

## 评估方法

### eval - 从对象评估

```java
JSONObject root = JSON.parseObject(json);
JSONPath path = JSONPath.of("$.store.book[0].title");

// 提取单个值
String title = path.eval(root, String.class);

// 提取对象
JSONObject book = path.eval(root, JSONObject.class);

// 提取列表
List<String> titles = path.eval(root, List.class);
```

### extract - 从字符串提取

```java
String json = "{\"store\":{\"book\":[{\"title\":\"Java\"}]}}";
JSONPath path = JSONPath.of("$.store.book[0].title");

// 流式提取，无需完整解析
String title = path.extract(json, String.class);
```

### extract - 提取列表

```java
JSONPath path = JSONPath.of("$.store.book[*].title");
List<String> titles = path.extract(json, List.class);
```

### set - 设置值

```java
JSONObject root = JSON.parseObject(json);
JSONPath path = JSONPath.of("$.store.book[0].price");

// 设置新值
path.set(root, 29.99);
```

---

## 路径语法

### 根节点

```java
"$"              // 根节点
"$.store"        // 根节点的 store 子节点
```

### 子节点

```java
"$.store.book"           // 点号访问
"$['store']['book']"     // 括号访问（特殊字符时必需）
"$[\"store\"][\"book\"]" // 转义引号
```

### 数组索引

```java
"$.store.book[0]"        // 第一个元素
"$.store.book[-1]"       // 最后一个元素
"$.store.book[0,1,2]"    // 多个索引
"$.store.book[0:2]"      // 切片 [0, 2)
"$.store.book[1:]"       // 从 1 到结尾
"$.store.book[:2]"       # 从开头到 2
```

### 通配符

```java
"$.store.*"              // 所有子节点
"$.store.book[*]"        // 数组所有元素
"$.store..price"         // 后代所有 price
"$..*"                    // 所有后代节点
```

### 过滤表达式

```java
// 基础过滤
"$.store.book[?(@.price < 10)]"

// 多条件
"$.store.book[?(@.price < 10 && @.category == 'fiction')]"

// 字符串匹配
"$.store.book[?(@.category == 'reference')]"

// 存在性
"$.store.book[?(@.author)]"
```

### 查询表达式

```java
"$.store.book[*].author"         // 所有作者
"$.store.book[*].[author,price]" // 作者和价格
```

---

## TypedMultiPath - 多路径类型化提取

提取多个不同类型的值。

### 创建

```java
String[] paths = {"$.name", "$.age", "$.vip", "$.balance"};
Type[] types = {String.class, Integer.class, Boolean.class, Double.class};
JSONPath multi = JSONPath.of(paths, types);
```

### 提取

```java
String json = """
    {"name":"张三","age":25,"vip":true,"balance":99.99}
    """;

Object[] values = multi.extract(json);
// values[0] = "张三" (String)
// values[1] = 25 (Integer)
// values[2] = true (Boolean)
// values[3] = 99.99 (Double)
```

### 从对象提取

```java
JSONObject root = JSON.parseObject(json);
Object[] values = multi.eval(root);
```

---

## 常用场景

### 提取单个值

```java
String json = "{\"user\":{\"name\":\"张三\",\"age\":25}}";

JSONPath path = JSONPath.of("$.user.name");
String name = path.extract(json, String.class);  // "张三"
```

### 提取列表

```java
String json = """
    {"users":[
        {"name":"张三","age":25},
        {"name":"李四","age":30}
    ]}
    """;

JSONPath path = JSONPath.of("$.users[*].name");
List<String> names = path.extract(json, List.class);  // ["张三", "李四"]
```

### 条件过滤

```java
// 价格小于 10 的书
JSONPath path = JSONPath.of("$.store.book[?(@.price < 10)]");
JSONArray books = path.eval(json, JSONArray.class);
```

### 深层查询

```java
// 所有 price，不管层级
JSONPath path = JSONPath.of("$..price");
List<Double> prices = path.eval(json, List.class);
```

---

## 性能建议

### 1. 预编译并复用

```java
// ✅ 好：预编译一次，重复使用
private static final JSONPath PRICE_PATH =
    JSONPath.of("$.store.book[*].price");

// 每次复用
List<Double> prices = PRICE_PATH.extract(json, List.class);

// ❌ 不好：每次创建新实例
JSONPath path = JSONPath.of("$.store.book[*].price");
```

### 2. 使用 extract 而非 eval

```java
// ✅ 更快：直接从字符串提取
String title = path.extract(json, String.class);

// ⚠️ 较慢：需要先完整解析
JSONObject root = JSON.parseObject(json);
String title = path.eval(root, String.class);
```

### 3. 使用多路径提取

```java
// ✅ 好：一次提取多个值
String[] paths = {"$.name", "$.age"};
Type[] types = {String.class, Integer.class};
JSONPath multi = JSONPath.of(paths, types);
Object[] values = multi.eval(json);

// ⚠️ 较慢：多次查询
String name = JSONPath.of("$.name").eval(json, String.class);
Integer age = JSONPath.of("$.age").eval(json, Integer.class);
```

---

## 与 JSONObject 配合

```java
JSONObject root = JSON.parseObject(json);

// 使用 JSONPath 查询
JSONPath path = JSONPath.of("$.user.name");
String name = path.eval(root, String.class);

// 或使用便捷方法
String name = (String) root.eval("$.user.name");
```

---

## 路径片段

JSONPath 由多个路径片段组成：

| 片段类型 | 语法 | 说明 |
|---------|------|------|
| 根节点 | `$` | JSON 根 |
| 子节点 | `.name` 或 `["name"]` | 对象属性 |
| 通配符 | `.*` 或 `[*]` | 所有子节点/元素 |
| 数组索引 | `[0]`, `[0,1]`, `[0:2]` | 数组访问 |
| 过滤 | `[?(expr)]` | 条件过滤 |
| 后代 | `..name` | 所有后代 |
| 查询 | `[expr,expr]` | 多值查询 |

---

## 常用表达式速查

| 场景 | 表达式 |
|------|--------|
| 根节点 | `$` |
| 子节点 | `$.field` |
| 数组第一个 | `$.array[0]` |
| 数组最后一个 | `$.array[-1]` |
| 数组切片 | `$.array[0:5]` |
| 所有元素 | `$.array[*]` |
| 所有子节点 | `$.object.*` |
| 后代查询 | `$..field` |
| 条件过滤 | `$.array[?(@.field > 10)]` |
| 多条件 | `$.array[?(@.a > 10 && @.b < 20)]` |
| 多个字段 | `$.array[*].[a,b,c]` |

---

## 错误处理

```java
try {
    String value = path.extract(json, String.class);
} catch (JSONException e) {
    // 路径不存在
    System.err.println("Path not found: " + e.getMessage());
}

// 安全获取
String value = path.extract(json, String.class);
if (value == null) {
    // 处理 null
}
```

---

## 完整示例

```java
public class JSONPathDemo {
    public static void main(String[] args) {
        String json = """
            {
                "store": {
                    "book": [
                        {"category": "reference", "author": "Nigel Rees", "price": 8.95},
                        {"category": "fiction", "author": "Evelyn Waugh", "price": 12.99}
                    ],
                    "bicycle": {"color": "red", "price": 19.95}
                }
            }
            """;

        // 1. 提取单个值
        JSONPath path1 = JSONPath.of("$.store.book[0].author");
        String author = path1.extract(json, String.class);
        System.out.println(author);  // Nigel Rees

        // 2. 提取列表
        JSONPath path2 = JSONPath.of("$.store.book[*].author");
        List<String> authors = path2.extract(json, List.class);
        System.out.println(authors);  // [Nigel Rees, Evelyn Waugh]

        // 3. 条件过滤
        JSONPath path3 = JSONPath.of("$.store.book[?(@.price < 10)]");
        JSONArray cheapBooks = path3.eval(json, JSONArray.class);
        System.out.println(cheapBooks.toJSONString());

        // 4. 多路径提取
        String[] paths = {"$.store.book[0].author", "$.store.book[0].price", "$.store.bicycle.color"};
        Type[] types = {String.class, Double.class, String.class};
        JSONPath multi = JSONPath.of(paths, types);
        Object[] values = multi.eval(json);
        System.out.println(Arrays.toString(values));
    }
}
```

## 相关文档

- [📖 JSONPath 指南 →](../guides/jsonpath.md)
- [📖 JSON Schema 验证 →](../guides/validation.md)
- [📋 JSON 类参考 →](JSON.md)

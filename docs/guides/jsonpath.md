# JSONPath 查询完整指南

fastjson3 实现了 [RFC 9535](https://www.rfc-editor.org/rfc/rfc9535.html) JSONPath 标准，用于从 JSON 中提取数据。

## 基础语法

### 根节点和子节点

```java
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

// 根节点
JSONPath rootPath = JSONPath.of("$");
JSONObject root = rootPath.eval(json, JSONObject.class);

// 子节点
JSONPath path = JSONPath.of("$.store");
JSONObject store = path.eval(json, JSONObject.class);
```

### 数组索引

```java
// 第一个元素（从 0 开始）
JSONPath path = JSONPath.of("$.store.book[0]");
JSONObject book = path.eval(json, JSONObject.class);

// 最后一个元素
JSONPath path = JSONPath.of("$.store.book[^(length-1)]");
JSONObject lastBook = path.eval(json, JSONObject.class);

// 多个索引
JSONPath path = JSONPath.of("$.store.book[0,1]");
JSONArray books = path.eval(json, JSONArray.class);
```

### 通配符

```java
// 所有子节点
JSONPath path = JSONPath.of("$.store.*");
JSONObject all = path.eval(json, JSONObject.class);

// 数组所有元素
JSONPath path = JSONPath.of("$.store.book[*]");
JSONArray allBooks = path.eval(json, JSONArray.class);

// 所有作者
JSONPath path = JSONPath.of("$.store.book[*].author");
List<String> authors = path.extract(json, List.class);
```

### 过滤表达式

```java
// 价格小于 10 的书
JSONPath path = JSONPath.of("$.store.book[?(@.price < 10)]");
JSONArray cheapBooks = path.eval(json, JSONArray.class);

// fiction 类别的书
JSONPath path = JSONPath.of("$.store.book[?(@.category == 'fiction')]");
JSONArray fictionBooks = path.eval(json, JSONArray.class);

// 多条件
JSONPath path = JSONPath.of("$.store.book[?(@.price < 15 && @.category == 'fiction')]");
```

### 切片

```java
// 前两本书
JSONPath path = JSONPath.of("$.store.book[0:2]");
JSONArray firstTwo = path.eval(json, JSONArray.class);

// 最后两本书
JSONPath path = JSONPath.of("$.store.book[-2:]");
JSONArray lastTwo = path.eval(json, JSONArray.class);

// 步长（每两个取一个）
JSONPath path = JSONPath.of("$.store.book[::2]");
```

### 查询表达式

```java
// 所有价格
JSONPath path = JSONPath.of("$.store.book[*].price");
List<Double> prices = path.extract(json, List.class);

// 所有作者和价格
JSONPath path = JSONPath.of("$.store.book[*].[author, price]");
```

## 使用方式

### 方式1: 编译后重用（推荐）

```java
// 编译一次
JSONPath path = JSONPath.compile("$.store.book[*].author");

// 多次使用
String json1 = /* ... */;
String json2 = /* ... */;

List<String> authors1 = path.extract(json1, List.class);
List<String> authors2 = path.extract(json2, List.class);
```

### 方式2: 直接创建

```java
JSONPath path = JSONPath.of("$.store.book[0].title");
String title = path.eval(json, String.class);
```

### 方式3: 从已解析对象查询

```java
JSONObject root = JSON.parseObject(json);
JSONPath path = JSONPath.of("$.store.book[*].author");
List<String> authors = path.eval(root, List.class);
```

### 方式4: 流式提取（无需完整解析）

```java
JSONPath path = JSONPath.of("$.store.book[*].author");

// 直接从 JSON 字符串提取，性能更高
String author = path.extract(json, String.class);
```

## 类型化提取

### 单个值

```java
JSONPath path = JSONPath.of("$.store.book[0].price");
Double price = path.eval(json, Double.class);
```

### 列表

```java
JSONPath path = JSONPath.of("$.store.book[*].author");
List<String> authors = path.eval(json, List.class);
```

### 多路径类型化提取

```java
JSONPath.TypedMultiPath multi = JSONPath.typedMulti()
    .path("$.store.book[0].author", String.class)
    .path("$.store.book[0].price", Double.class)
    .path("$.store.book[0].category", String.class)
    .build();

Object[] values = multi.extract(json);
// values[0] = "Nigel Rees" (String)
// values[1] = 8.95 (Double)
// values[2] = "reference" (String)
```

## 实际应用

### API 响应提取

```java
public class ApiClient {
    private static final JSONPath DATA_PATH =
        JSONPath.compile("$.data.items[*]");

    private static final JSONPath ERROR_PATH =
        JSONPath.compile("$.error.message");

    public List<Item> getItems(String jsonResponse) {
        // 提取数据
        JSONArray items = DATA_PATH.extract(jsonResponse, JSONArray.class);
        return JSON.parseArray(items.toJSONString(), Item.class);
    }

    public void checkError(String jsonResponse) {
        String error = ERROR_PATH.extract(jsonResponse, String.class);
        if (error != null) {
            throw new ApiException(error);
        }
    }
}
```

### 配置文件读取

```java
String configJson = """
    {
        "database": {
            "primary": {
                "host": "localhost",
                "port": 5432,
                "name": "mydb"
            },
            "replica": [
                {"host": "replica1.example.com", "port": 5432},
                {"host": "replica2.example.com", "port": 5432}
            ]
        }
    }
    """;

// 提取主库配置
JSONPath primaryPath = JSONPath.of("$.database.primary");
JSONObject primary = primaryPath.eval(configJson, JSONObject.class);

// 提取所有副本地址
JSONPath replicaPath = JSONPath.of("$.database.replica[*].host");
List<String> hosts = replicaPath.extract(configJson, List.class);
```

### 日志分析

```java
String logJson = """
    {
        "events": [
            {"timestamp": "2026-03-17T10:00:00", "level": "ERROR", "message": "Failed"},
            {"timestamp": "2026-03-17T10:01:00", "level": "INFO", "message": "Success"},
            {"timestamp": "2026-03-17T10:02:00", "level": "ERROR", "message": "Timeout"}
        ]
    }
    """;

// 提取所有错误事件
JSONPath errorPath = JSONPath.of("$.events[?(@.level == 'ERROR')]");
JSONArray errors = errorPath.eval(logJson, JSONArray.class);

// 提取所有错误消息
JSONPath messagePath = JSONPath.of("$.events[?(@.level == 'ERROR')].message");
List<String> errorMessages = messagePath.extract(logJson, List.class);
```

## 性能建议

### 1. 编译并复用

```java
// ✅ 好：编译一次，重复使用
private static final JSONPath PRICE_PATH = JSONPath.compile("$.store.book[*].price");

public double getTotalPrice(String json) {
    List<Double> prices = PRICE_PATH.extract(json, List.class);
    return prices.stream().mapToDouble(Double::doubleValue).sum();
}

// ❌ 不好：每次编译
public double getTotalPrice(String json) {
    JSONPath path = JSONPath.of("$.store.book[*].price");
    // ...
}
```

### 2. 使用流式提取

```java
// ✅ 更快：无需完整解析
String author = path.extract(json, String.class);

// ⚠️ 较慢：需要先完整解析
JSONObject root = JSON.parseObject(json);
String author = path.eval(root, String.class);
```

### 3. 使用多路径提取

```java
// ✅ 一次提取多个值
JSONPath.TypedMultiPath multi = JSONPath.typedMulti()
    .path("$.name", String.class)
    .path("$.age", Integer.class)
    .path("$.email", String.class)
    .build();

Object[] values = multi.extract(json);

// ⚠️ 多次查询
String name = JSONPath.of("$.name").eval(json, String.class);
Integer age = JSONPath.of("$.age").eval(json, Integer.class);
String email = JSONPath.of("$.email").eval(json, String.class);
```

## 常用表达式

| 场景 | 表达式 |
|------|--------|
| 根节点 | `$` |
| 子节点 | `$.field` |
| 数组第一个 | `$.array[0]` |
| 数组最后一个 | `$.array[^(length-1)]` |
| 所有元素 | `$.array[*]` |
| 所有子节点 | `$.object.*` |
| 过滤 | `$.array[?(@.field > 10)]` |
| 切片 | `$.array[0:5]` |
| 嵌套查询 | `$.store.book[*].author` |

## 参考链接

- [RFC 9535 JSONPath](https://www.rfc-editor.org/rfc/rfc9535.html)

## 相关文档

- 📖 [JSON Schema 验证 →](validation.md)
- 📖 [POJO 序列化 →](pojo.md)
- 📋 [JSONPath API 参考 →](../api/JSONPath.md)

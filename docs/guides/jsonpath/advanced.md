# JSONPath 高级用法

本文档介绍 JSONPath 的高级功能和性能优化技巧。

---

## 类型化提取

### TypedMultiPath - 一次提取多个值

```java
JSONPath.TypedMultiPath multi = JSONPath.typedMulti()
    .path("$.store.book[0].author", String.class)
    .path("$.store.book[0].price", Double.class)
    .path("$.store.book[0].category", String.class)
    .path("$.store.bicycle.color", String.class)
    .build();

Object[] values = multi.extract(json);
// values[0] = "Nigel Rees" (String)
// values[1] = 8.95 (Double)
// values[2] = "reference" (String)
// values[3] = "red" (String)
```

### 直接提取到对象

```java
JSONPath path = JSONPath.of("$.store.book[0]");
Book book = path.eval(json, Book.class);
```

---

## 性能优化

### 1. 编译并复用

```java
// ✅ 好：编译一次，重复使用
private static final JSONPath PRICE_PATH =
    JSONPath.compile("$.store.book[*].price");

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

### 2. 使用 extract() 而非 eval()

```java
// ✅ 更快：流式提取，无需完整解析
String author = path.extract(json, String.class);

// ⚠️ 较慢：需要先完整解析
JSONObject root = JSON.parseObject(json);
String author = path.eval(root, String.class);
```

### 3. 批量提取

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

### 4. 避免深层扫描

```java
// ⚠️ 慢：扫描整个 JSON 树
JSONPath.of("$..price")

// ✅ 快：指定确切路径
JSONPath.of("$.store.book[*].price")
```

---

## 高级过滤表达式

### 正则表达式匹配

```java
// 作者名包含 "Rees"
JSONPath.of("$.store.book[?(@.author =~ /Rees/)]")

// ISBN 匹配模式
JSONPath.of("$.store.book[?(@.isbn =~ /^978/)]")
```

### 存在性检查

```java
// 有 price 字段的书
JSONPath.of("$.store.book[?(@.price)]")

// 没有 discount 字段的书
JSONPath.of("$.store.book[?(!@.discount)]")
```

### 数组操作

```java
// 数组长度
JSONPath.of("$.store.book[?(@.authors.length() > 1)]")

// 数组包含
JSONPath.of("$.store.book[?(@.tags contains 'fiction')]")
```

### 类型检查

```java
// 字段类型为字符串
JSONPath.of("$[?(@.name @type 'string')]")

// 字段类型为数组
JSONPath.of("$[?(@.items @type 'array')]")
```

---

## 聚合函数

### count - 计数

```java
// 统计书的数量
JSONPath path = JSONPath.of("$.store.book.count()");
Integer count = path.eval(json, Integer.class);
```

### sum - 求和

```java
// 计算总价
JSONPath path = JSONPath.of("$.store.book[*].price.sum()");
Double total = path.eval(json, Double.class);
```

### min/max - 最值

```java
// 最低价格
JSONPath path = JSONPath.of("$.store.book[*].price.min()");
Double minPrice = path.eval(json, Double.class);

// 最高价格
JSONPath path = JSONPath.of("$.store.book[*].price.max()");
Double maxPrice = path.eval(json, Double.class);
```

### avg - 平均值

```java
// 平均价格
JSONPath path = JSONPath.of("$.store.book[*].price.avg()");
Double avgPrice = path.eval(json, Double.class);
```

---

## 实际应用场景

### API 响应处理

```java
public class ApiClient {
    // 预编译路径
    private static final JSONPath DATA_PATH =
        JSONPath.compile("$.data.items[*]");
    private static final JSONPath ERROR_PATH =
        JSONPath.compile("$.error.message");
    private static final JSONPath TOTAL_PATH =
        JSONPath.compile("$.data.total");

    public Result parseResponse(String jsonResponse) {
        // 批量提取
        JSONPath.TypedMultiPath multi = JSONPath.typedMulti()
            .path("$.data.total", Integer.class)
            .path("$.data.page", Integer.class)
            .path("$.error.code", String.class)
            .build();

        Object[] values = multi.extract(jsonResponse);
        Integer total = (Integer) values[0];
        Integer page = (Integer) values[1];
        String errorCode = (String) values[2];

        if (errorCode != null) {
            throw new ApiException(errorCode);
        }

        JSONArray items = DATA_PATH.extract(jsonResponse, JSONArray.class);
        return new Result(total, page, JSON.parseArray(items.toJSONString(), Item.class));
    }
}
```

### 配置文件读取

```java
String configJson = """
    {
        "database": {
            "primary": {"host": "localhost", "port": 5432},
            "replica": [
                {"host": "replica1.example.com", "port": 5432},
                {"host": "replica2.example.com", "port": 5433}
            ]
        },
        "cache": {
            "redis": {"host": "localhost", "port": 6379}
        }
    }
    """;

// 提取所有数据库地址
JSONPath dbPath = JSONPath.of("$..host");
List<String> allHosts = dbPath.extract(configJson, List.class);
// 结果: ["localhost", "replica1.example.com", "replica2.example.com", "localhost"]

// 只提取 replica 地址
JSONPath replicaPath = JSONPath.of("$.database.replica[*].host");
List<String> replicaHosts = replicaPath.extract(configJson, List.class);
// 结果: ["replica1.example.com", "replica2.example.com"]
```

### 日志分析

```java
String logJson = """
    {
        "events": [
            {"timestamp": "2026-03-17T10:00:00", "level": "ERROR", "message": "Failed", "code": 500},
            {"timestamp": "2026-03-17T10:01:00", "level": "INFO", "message": "Success", "code": 200},
            {"timestamp": "2026-03-17T10:02:00", "level": "ERROR", "message": "Timeout", "code": 504}
        ]
    }
    """;

// 提取所有错误事件
JSONPath errorPath = JSONPath.of("$.events[?(@.level == 'ERROR')]");
JSONArray errors = errorPath.eval(logJson, JSONArray.class);

// 提取错误代码统计
JSONPath errorCodePath = JSONPath.of("$.events[?(@.level == 'ERROR')].code");
List<Integer> errorCodes = errorCodePath.extract(logJson, List.class);
// 结果: [500, 504]

// 提取所有唯一错误级别
JSONPath levelPath = JSONPath.of("$.events[*].level");
List<String> levels = levelPath.extract(logJson, List.class);
Set<String> uniqueLevels = new HashSet<>(levels);
// 结果: ["ERROR", "INFO"]
```

### 数据转换

```java
String productsJson = """
    {
        "products": [
            {"id": 1, "name": "A", "price": 10},
            {"id": 2, "name": "B", "price": 20},
            {"id": 3, "name": "C", "price": 30}
        ]
    }
    """;

// 提取所有 ID
JSONPath idPath = JSONPath.of("$.products[*].id");
List<Integer> ids = idPath.extract(productsJson, List.class);

// 提取价格大于 15 的产品名称
JSONPath expensivePath = JSONPath.of("$.products[?(@.price > 15)].name");
List<String> expensiveNames = expensivePath.extract(productsJson, List.class);
// 结果: ["B", "C"]

// 计算平均价格
JSONPath avgPath = JSONPath.of("$.products[*].price.avg()");
Double avgPrice = avgPath.eval(productsJson, Double.class);
// 结果: 20.0
```

---

## 调试技巧

### 打印路径结果

```java
JSONPath path = JSONPath.of("$.store.book[*]");
Object result = path.eval(json);

if (result instanceof JSONArray) {
    System.out.println("结果是数组，元素数量: " + ((JSONArray) result).size());
} else if (result instanceof JSONObject) {
    System.out.println("结果是对象，键数量: " + ((JSONObject) result).size());
} else {
    System.out.println("结果: " + result);
}
```

### 验证路径

```java
try {
    JSONPath path = JSONPath.of("$.store.book[0].title");
    Object result = path.eval(json);
    System.out.println("路径有效: " + result);
} catch (PathNotFoundException e) {
    System.out.println("路径不存在: " + e.getPath());
}
```

### 逐步构建路径

```java
// 从简单路径开始，逐步添加
JSONPath base = JSONPath.of("$.store");
System.out.println(base.eval(json));

JSONPath books = JSONPath.of("$.store.book");
System.out.println(books.eval(json));

JSONPath firstBook = JSONPath.of("$.store.book[0]");
System.out.println(firstBook.eval(json));
```

---

## 常见问题

### Q: 如何处理不存在的路径？

```java
// 使用 extractOrNull()
String value = path.extractOrNull(json, String.class);

// 或者检查结果
Object result = path.eval(json);
if (result == null || result instanceof JSONNull) {
    // 路径不存在
}
```

### Q: 如何提取数组中的单个值？

```java
// 提取第一个作者
JSONPath path = JSONPath.of("$.store.book[0].author");
String author = path.eval(json, String.class);

// 或使用 extract
String author = path.extract(json, String.class);
```

### Q: 如何处理大型 JSON？

```java
// 使用流式解析，只提取需要的部分
JSONPath path = JSONPath.of("$.store.book[*].author");

// 直接从字符串提取，避免完整解析
List<String> authors = path.extract(jsonString, List.class);
```

## 相关文档

- [JSONPath 基础语法 →](basics.md)
- [跨库对比 →](comparison.md)

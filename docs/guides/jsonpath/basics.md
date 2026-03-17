# JSONPath 基础语法

fastjson3 实现了 [RFC 9535](https://www.rfc-editor.org/rfc/rfc9535.html) JSONPath 标准。

## 快速开始

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

// 创建 JSONPath
JSONPath path = JSONPath.of("$.store.book[0].author");

// 提取数据
String author = path.eval(json, String.class);
// 结果: "Nigel Rees"
```

---

## 路径表达式

### 根节点

```java
// $ 表示根节点
JSONPath path = JSONPath.of("$");
JSONObject root = path.eval(json, JSONObject.class);
```

### 子节点访问

```java
// 点号表示法
JSONPath path = JSONPath.of("$.store.bicycle");

// 等价于
JSONPath path = JSONPath.of("$['store']['bicycle']");
```

### 数组索引

```java
// 第一个元素（从 0 开始）
JSONPath.of("$.store.book[0]")

// 第二个元素
JSONPath.of("$.store.book[1]")

// 最后一个元素
JSONPath.of("$.store.book[^(length-1)]")
```

### 深层扫描

```java
// 在任意层级查找 book
JSONPath.of("$..book")

// 在任意层级查找 price
JSONPath.of("$..price")
```

---

## 通配符

### * 通配符

```java
// 所有子节点
JSONPath.of("$.store.*")
// 返回: {"book": [...], "bicycle": {...}}

// 数组所有元素
JSONPath.of("$.store.book[*]")
// 返回: 所有书的数组

// 嵌套通配符
JSONPath.of("$.store.book[*].author")
// 返回: 所有作者的数组
```

### .. 通配符（深层扫描）

```java
// 查找所有 price 属性
JSONPath.of("$..price")
// 返回: [8.95, 12.99, 19.95]
```

---

## 过滤表达式

### 基础过滤

```java
// 价格小于 10 的书
JSONPath.of("$.store.book[?(@.price < 10)]")

// category 为 fiction 的书
JSONPath.of("$.store.book[?(@.category == 'fiction')]")
```

### 比较运算符

| 运算符 | 说明 | 示例 |
|--------|------|------|
| `==` | 等于 | `[?(@.category == 'fiction')]` |
| `!=` | 不等于 | `[?(@.price != 10)]` |
| `<` | 小于 | `[?(@.price < 10)]` |
| `<=` | 小于等于 | `[?(@.price <= 10)]` |
| `>` | 大于 | `[?(@.price > 10)]` |
| `>=` | 大于等于 | `[?(@.price >= 10)]` |

### 逻辑运算符

```java
// AND：价格小于 15 且类别为 fiction
JSONPath.of("$.store.book[?(@.price < 15 && @.category == 'fiction')]")

// OR：类别为 fiction 或 reference
JSONPath.of("$.store.book[?(@.category == 'fiction' || @.category == 'reference')]")

// NOT：价格不等于 10
JSONPath.of("$.store.book[?(!(@.price == 10))]")
```

### 数组过滤

```java
// 数组长度大于 2
JSONPath.of("$[?(@.length() > 2)]")

// 数组为空
JSONPath.of("$[?(@.length() == 0)]")
```

---

## 切片表达式

### 基础切片

```java
// 前两本书
JSONPath.of("$.store.book[0:2]")

// 第二本到最后一本
JSONPath.of("$.store.book[1:]")

// 前两本（不包含结束索引）
JSONPath.of("$.store.book[:2]")
```

### 负索引

```java
// 最后一本
JSONPath.of("$.store.book[-1:]")

// 最后两本
JSONPath.of("$.store.book[-2:]")

// 除最后一本外的所有
JSONPath.of("$.store.book[:-1]")
```

### 步长

```java
// 每隔一个取一个
JSONPath.of("$.store.book[::2]")

// 反向遍历
JSONPath.of("$.store.book[::-1]")
```

---

## 多字段选择

```java
// 选择多个字段
JSONPath.of("$.store.book[0]['author', 'price']")
// 返回: ["Nigel Rees", 8.95]

// 所有书的作者和价格
JSONPath.of("$.store.book[*]['author', 'price']")
```

---

## 使用方式

### 方式1: 预编译后重用（推荐）

```java
// 预编译一次
private static final JSONPath AUTHOR_PATH =
    JSONPath.of("$.store.book[*].author");

// 多次使用
List<String> authors1 = AUTHOR_PATH.extract(json1, List.class);
List<String> authors2 = AUTHOR_PATH.extract(json2, List.class);
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

---

## 常用表达式速查

| 场景 | 表达式 |
|------|--------|
| 根节点 | `$` |
| 子节点 | `$.field` 或 `$['field']` |
| 数组第一个 | `$.array[0]` |
| 数组最后一个 | `$.array[-1:]` |
| 数组切片 | `$.array[0:5]` |
| 所有元素 | `$.array[*]` |
| 所有子节点 | `$.object.*` |
| 深层扫描 | `$..field` |
| 过滤 | `$.array[?(@.field > 10)]` |
| 多条件 | `$.array[?(@.x > 10 && @.y < 20)]` |

## 相关文档

- [JSONPath 高级用法 →](advanced.md)
- [跨库对比 →](comparison.md)

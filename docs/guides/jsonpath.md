# JSONPath 查询指南

fastjson3 实现了 [RFC 9535](https://www.rfc-editor.org/rfc/rfc9535.html) JSONPath 标准，提供强大的 JSON 数据查询能力。

## 📚 文档导航

### 基础入门
- **[基础语法 →](jsonpath/basics.md)**
  - 路径表达式
  - 过滤表达式
  - 通配符
  - 切片操作
  - 常用表达式速查

### 高进阶
- **[高级用法 →](jsonpath/advanced.md)**
  - 类型化提取
  - 性能优化技巧
  - 聚合函数
  - 实际应用场景
  - 调试技巧

### 跨库对比
- **[跨库对比 →](jsonpath/comparison.md)**
  - vs Jackson JsonPointer
  - vs Gson
  - vs org.json
  - vs Jayway JsonPath
  - 迁移指南

---

## 快速开始

```java
String json = """
    {
        "store": {
            "book": [
                {"category": "reference", "author": "Nigel Rees", "price": 8.95},
                {"category": "fiction", "author": "Evelyn Waugh", "price": 12.99}
            ]
        }
    }
    """;

// 创建路径
JSONPath path = JSONPath.of("$.store.book[*].author");

// 提取数据
List<String> authors = path.extract(json, List.class);
// 结果: ["Nigel Rees", "Evelyn Waugh"]
```

---

## 常用表达式速查

| 场景 | 表达式 | 说明 |
|------|--------|------|
| 根节点 | `$` | JSON 根节点 |
| 子节点 | `$.field` | 访问字段 |
| 数组第一个 | `$.array[0]` | 索引访问 |
| 数组切片 | `$.array[0:5]` | 切片操作 |
| 所有元素 | `$.array[*]` | 通配符 |
| 过滤 | `$.array[?(@.x > 10)]` | 条件过滤 |
| 深层扫描 | `$..field` | 任意层级 |

---

## 核心特性

- ✅ **RFC 9535 标准** - 完整实现最新 JSONPath 标准
- ✅ **高性能** - 流式解析，无需完整构建对象树
- ✅ **类型安全** - 支持类型化提取
- ✅ **可复用** - 编译一次，多次使用
- ✅ **功能完整** - 过滤、切片、聚合、深层扫描

---

## 与其他库对比

| 库 | 标准 | 性能 | 过滤支持 |
|------|------|------|----------|
| **fastjson3** | RFC 9535 | ⭐⭐⭐⭐⭐ | ✅ |
| Jackson | RFC 6901 | ⭐⭐⭐⭐ | ❌ |
| Gson | - | ⭐⭐⭐ | ❌ |
| org.json | 部分 | ⭐⭐ | ❌ |

详见 [跨库对比 →](jsonpath/comparison.md)

---

## 示例场景

### API 响应提取

```java
// 提取所有商品价格
JSONPath path = JSONPath.of("$.data.items[*].price");
List<Double> prices = path.extract(apiResponse, List.class);
```

### 配置文件读取

```java
// 提取数据库配置
JSONPath path = JSONPath.of("$.config.database.primary");
JSONObject dbConfig = path.eval(configJson, JSONObject.class);
```

### 日志分析

```java
// 提取所有错误日志
JSONPath path = JSONPath.of("$.events[?(@.level == 'ERROR')]");
JSONArray errors = path.eval(logJson, JSONArray.class);
```

---

## 相关文档

- 📖 [JSON Schema 验证 →](validation.md)
- 📖 [POJO 序列化 →](pojo.md)
- 📋 [JSONPath API 参考 →](../api/JSONPath.md)

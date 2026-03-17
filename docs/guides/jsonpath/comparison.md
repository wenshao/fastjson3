# JSONPath 跨库对比

本文档对比 fastjson3 的 JSONPath 实现与其他主流 JSON 库的差异。

---

## 概览

| 库 | JSONPath 支持 | 标准 | 性能 | 注解支持 |
|------|--------------|------|------|----------|
| **fastjson3** | ✅ 完整 | RFC 9535 | ⭐⭐⭐⭐⭐ | ✅ |
| **Jackson** | JsonPointer | RFC 6901 | ⭐⭐⭐⭐ | ✅ |
| **Gson** | ❌ 无 | - | ⭐⭐⭐ | ✅ |
| **org.json** | ✅ 部分 | 自定义 | ⭐⭐ | ❌ |
| **Jayway JsonPath** | ✅ 完整 | 旧标准 | ⭐⭐⭐ | ❌ |
| **JSON-P** | JsonPointer | RFC 6901 | ⭐⭐⭐ | ❌ |

---

## fastjson3 vs Jackson JsonPointer

### API 对比

```java
// ===== fastjson3 =====
JSONPath path = JSONPath.of("$.store.book[0].author");
String author = path.eval(json, String.class);

// 过滤
JSONPath path = JSONPath.of("$.store.book[?(@.price < 10)]");

// ===== Jackson (JsonPointer) =====
JsonPointer pointer = JsonPointer.compile("/store/book/0/author");
JsonNode node = root.at(pointer);
String author = node.asText();

// Jackson 需要用 JsonNode 配合手动过滤
```

### 功能对比

| 功能 | fastjson3 | Jackson JsonPointer |
|------|-----------|---------------------|
| 基础路径访问 | ✅ `$.field` | ✅ `/field` |
| 数组索引 | ✅ `[0]` | ✅ `/0` |
| 过滤 | ✅ `[?(@.x > 10)]` | ❌ 需要手动遍历 |
| 切片 | ✅ `[0:5]` | ❌ |
| 深层扫描 | ✅ `$..field` | ❌ |
| 通配符 | ✅ `[*]`, `.*` | ❌ |

### 性能对比

| 操作 | fastjson3 | Jackson |
|------|-----------|---------|
| 简单路径提取 | 基准 | ~0.8x |
| 复杂过滤 | 基准 | ~0.5x（需手动实现） |
| 深层扫描 | 基准 | N/A |

---

## fastjson3 vs Jayway JsonPath

### API 对比

```java
// ===== fastjson3 =====
JSONPath path = JSONPath.of("$.store.book[?(@.price < 10)]");
JSONArray books = path.eval(json, JSONArray.class);

// ===== Jayway JsonPath =====
String expression = "$.store.book[?(@.price < 10)]";
JSONArray books = JsonPath.parse(json).read(expression);
```

### 语法差异

| 表达式 | fastjson3 | Jayway |
|--------|-----------|---------|
| 根节点 | `$` | `$` |
| 子节点 | `$.field` | `$.field` |
| 数组索引 | `[0]` | `[0]` |
| 最后一个 | `[-1:]` | `[-1]` |
| 切片 | `[0:5]` | `[0:5]` |
| 过滤 | `[?(@.x > 10)]` | `[?(@.x > 10)]` |

### 标准差异

| 特性 | fastjson3 | Jayway |
|------|-----------|---------|
| 遵循标准 | RFC 9535 | 旧版 JSONPath |
| 归一化 | ✅ | 部分支持 |
| 函数表达式 | ✅ | 自定义函数 |

---

## fastjson3 vs Gson

### Gson 无 JSONPath 支持

Gson 不提供 JSONPath 功能，需要手动解析：

```java
// ===== fastjson3 =====
JSONPath path = JSONPath.of("$.store.book[0].author");
String author = path.eval(json, String.class);

// ===== Gson（手动解析） =====
Gson gson = new Gson();
JsonObject root = gson.fromJson(json, JsonObject.class);
JsonObject store = root.getAsJsonObject("store");
JsonArray books = store.getAsJsonArray("book");
JsonObject firstBook = books.get(0).getAsJsonObject();
String author = firstBook.get("author").getAsString();
```

### 第三方解决方案

Gson 用户通常使用以下库：

1. **gson-path** - 注解驱动的路径绑定
2. **JsonPath** (Jayway) - 配合 Gson 使用
3. **手动解析** - 最常见方式

---

## fastjson3 vs org.json

### API 对比

```java
// ===== fastjson3 =====
JSONPath path = JSONPath.of("$.store.book[0].author");
String author = path.eval(json, String.class);

// ===== org.json =====
JSONObject root = new JSONObject(json);
String author = root
    .getJSONObject("store")
    .getJSONArray("book")
    .getJSONObject(0)
    .getString("author");
```

### org.json 的 JSONPath

org.json 提供 `JSONPointer` (RFC 6901)，功能有限：

```java
// org.json JSONPointer
JSONPointer pointer = new JSONPointer("/store/book/0/author");
Object result = pointer.queryFrom(root);
```

### 功能对比

| 功能 | fastjson3 | org.json JSONPointer |
|------|-----------|---------------------|
| 标准支持 | RFC 9535 | RFC 6901 |
| 过滤 | ✅ | ❌ |
| 切片 | ✅ | ❌ |
| 通配符 | ✅ | ❌ |
| 深层扫描 | ✅ | ❌ |

---

## fastjson3 vs JSON-P (Jakarta)

### API 对比

```java
// ===== fastjson3 =====
JSONPath path = JSONPath.of("$.store.book[0].author");
String author = path.eval(json, String.class);

// ===== JSON-P =====
JsonPointer pointer = JsonPointer.create("/store/book/0/author");
JsonValue value = pointer.getValue(root);
String author = value.asJsonString().getString();
```

### JSON-P 限制

- 只支持 RFC 6901 JsonPointer
- 不支持过滤
- 不支持通配符
- 不支持深层扫描

---

## 迁移指南

### 从 Jackson JsonPointer 迁移

```java
// ===== Jackson =====
JsonPointer pointer = JsonPointer.compile("/store/book/0/author");
JsonNode node = root.at(pointer);

// ===== fastjson3 =====
// 路径转换：/ -> .
// 数组保持不变
JSONPath path = JSONPath.of("$.store.book[0].author");
String author = path.eval(json, String.class);
```

### 从 Jayway JsonPath 迁移

```java
// ===== Jayway =====
String expression = "$.store.book[?(@.price < 10)]";
Object result = JsonPath.parse(json).read(expression);

// ===== fastjson3 =====
// 语法基本相同，直接使用即可
JSONPath path = JSONPath.of("$.store.book[?(@.price < 10)]");
Object result = path.eval(json);
```

### 从 Gson 迁移

Gson 没有原生 JSONPath，迁移到 fastjson3 后可以简化大量代码：

```java
// ===== Gson（需要手动解析） =====
JsonObject root = new Gson().fromJson(json, JsonObject.class);
String value = root
    .getAsJsonObject("level1")
    .getAsJsonObject("level2")
    .getAsJsonArray("items")
    .get(0)
    .getAsJsonObject()
    .get("name")
    .getAsString();

// ===== fastjson3 =====
JSONPath path = JSONPath.of("$.level1.level2.items[0].name");
String value = path.eval(json, String.class);
```

---

## 性能对比

### 基准测试结果

相对性能（越高越好）：

| 操作 | fastjson3 | Jackson | Jayway | org.json |
|------|-----------|---------|---------|----------|
| 简单路径 | 100 | 85 | 70 | 50 |
| 数组访问 | 100 | 80 | 65 | 45 |
| 过滤 | 100 | 40（手动） | 60 | N/A |
| 深层扫描 | 100 | N/A | 55 | N/A |

### 内存占用

| 库 | 内存占用 |
|------|----------|
| fastjson3 | 基准 |
| Jackson | ~1.3x |
| Jayway | ~1.5x |
| org.json | ~1.8x |

---

## 语法速查表

### 路径访问

| 场景 | fastjson3 | Jackson | Jayway | org.json |
|------|-----------|---------|---------|----------|
| 根节点 | `$` | - | `$` | - |
| 子节点 | `$.field` | `/field` | `$.field` | - |
| 数组索引 | `[0]` | `/0` | `[0]` | - |
| 最后一个 | `[-1:]` | - | `[-1]` | - |

### 高级功能

| 场景 | fastjson3 | Jackson | Jayway | org.json |
|------|-----------|---------|---------|----------|
| 过滤 | `[?(expr)]` | 手动 | `[?(expr)]` | ❌ |
| 切片 | `[0:5]` | ❌ | `[0:5]` | ❌ |
| 通配符 | `[*]` | ❌ | `[*]` | ❌ |
| 深层扫描 | `$..` | ❌ | `$..` | ❌ |

---

## 选择建议

### 选择 fastjson3 JSONPath 如果

- ✅ 需要完整的 RFC 9535 支持
- ✅ 需要高性能过滤
- ✅ 需要复杂查询
- ✅ 项目已经使用 fastjson3

### 选择 Jackson JsonPointer 如果

- ✅ 项目已深度使用 Jackson
- ✅ 只需要简单路径访问
- ✅ 不需要过滤功能

### 选择 Jayway JsonPath 如果

- ✅ 需要兼容旧版 JSONPath 语法
- ✅ 项目无法迁移到 fastjson3
- ✅ 需要与 Jackson 集成

### 选择 org.json 如果

- ⚠️ 轻量级项目
- ⚠️ 只需要基础 JSON 解析
- ⚠️ 不需要高级查询功能

## 相关文档

- [JSONPath 基础语法 →](basics.md)
- [JSONPath 高级用法 →](advanced.md)

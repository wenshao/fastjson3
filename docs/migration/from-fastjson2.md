# 从 fastjson2 迁移到 fastjson3

fastjson3 与 fastjson2 **高度兼容**，大部分代码无需修改。

## API 兼容性

### 完全兼容的 API

以下 API 在两个库中**完全相同**：

```java
// 解析 JSON
JSONObject obj = JSON.parseObject(json);
User user = JSON.parseObject(json, User.class);
List<User> users = JSON.parseArray(json, User.class);

// 序列化
String json = JSON.toJSONString(user);
byte[] bytes = JSON.toJSONBytes(user);

// JSONPath
JSONPath path = JSONPath.of("$.store.book[*].title");
List<String> titles = path.extract(json, List.class);
```

### 新增 ObjectMapper API

fastjson3 新增 Jackson 风格的 `ObjectMapper`（推荐使用）：

```java
// fastjson3 新 API
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);
String json = mapper.writeValueAsString(user);
```

## 迁移步骤

### 1. 替换依赖

```xml
<!-- 移除 fastjson2 -->
<!--<dependency>-->
<!--    <groupId>com.alibaba.fastjson2</groupId>-->
<!--    <artifactId>fastjson2</artifactId>-->
<!--    <version>2.x.x</version>-->
<!--</dependency>-->

<!-- 添加 fastjson3 -->
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 2. 更新导入

```java
// 旧的导入（可能需要更新）
import com.alibaba.fastjson2.JSON;

// 新的导入
import com.alibaba.fastjson3.JSON;
```

### 3. 测试现有代码

大多数情况下，现有代码应该无需修改即可运行。

### 4. 可选：采用新 API

```java
// 旧方式（仍然有效）
User user = JSON.parseObject(json, User.class);

// 新方式（推荐，更灵活）
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);
```

## 特性对照表

| fastjson2 | fastjson3 | 状态 |
|-----------|-----------|------|
| `JSONReader` | `JSONReader` | ✅ 相同 |
| `JSONWriter` | `JSONWriter` | ✅ 相同 |
| `JSONObject` | `JSONObject` | ✅ 相同 |
| `JSONArray` | `JSONArray` | ✅ 相同 |
| `JSONPath` | `JSONPath` | ✅ 相同 |
| `@JSONField` | `@JSONField` | ✅ 相同 |
| `@JSONType` | `@JSONType` | ✅ 相同 |
| `ObjectMapper` | `ObjectMapper` | ✅ 新增 |

## 完整示例

```java
// ===== fastjson2 =====
String json = JSON.toJSONString(user, WriteFeature.PrettyFormat);
User parsed = JSON.parseObject(json, User.class);

// ===== fastjson3 方式1：兼容 API =====
String json = JSON.toJSONString(user, WriteFeature.PrettyFormat);
User parsed = JSON.parseObject(json, User.class);

// ===== fastjson3 方式2：推荐 API =====
ObjectMapper mapper = ObjectMapper.shared();
String json = mapper.writeValueAsString(user);
User parsed = mapper.readValue(json, User.class);
```

## 常见问题

### Q: 需要修改代码吗？

A: 大多数情况下不需要。基础 API 完全兼容。

### Q: 性能如何？

A: fastjson3 继承了 fastjson2 的高性能引擎，性能相当或更好。

### Q: 注解兼容吗？

A: 是的，`@JSONField`、`@JSONType` 等注解完全兼容。

### Q: Kotlin 支持如何？

A: fastjson3 对 Kotlin 数据类的支持与 fastjson2 相同。

## 相关文档

- [API 参考](../api/)
- [ObjectMapper 文档](../api/ObjectMapper.md)
- [迁移检查清单](checklist.md)

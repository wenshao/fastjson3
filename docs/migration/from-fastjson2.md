# 从 fastjson2 迁移到 fastjson3

fastjson3 与 fastjson2 **高度兼容**，大部分代码无需修改。

## fastjson2 用户注意

如果你使用的是 fastjson2，迁移到 fastjson3 会非常简单：

### 为什么迁移更简单？

| 方面 | fastjson2 | fastjson3 |
|------|-----------|-----------|
| **核心 API** | `JSON.parseObject()` | `JSON.parseObject()` | ✅ 相同 |
| **注解** | `@JSONField`, `@JSONType` | `@JSONField`, `@JSONType` | ✅ 相同 |
| **Feature 枚举** | `WriteFeature`, `ReadFeature` | `WriteFeature`, `ReadFeature` | ✅ 相同 |
| **JSONPath** | `JSONPath.of()` | `JSONPath.of()` | ✅ 相同 |
| **包名** | `com.alibaba.fastjson2` | `com.alibaba.fastjson3` | ⚠️ 需要修改 |
| **Java 版本** | Java 8+ | Java 21+ | ⚠️ 需要升级 |

### fastjson2 vs fastjson3 对比

| 特性 | fastjson2 | fastjson3 |
|------|-----------|-----------|
| **性能** | 高 | 更高 |
| **Jackson 风格 API** | 部分 | 完整 (`ObjectMapper`) |
| **Record 支持** | 有 | 更好 |
| **sealed class** | 部分 | 完整 |
| **Kotlin 支持** | 好 | 更好 |
| **GraalVM** | 支持 | 原生支持 |

### 迁移决策

```
是否能升级到 Java 21？
│
├─ 是 → 直接迁移到 fastjson3（推荐）
│       - 包名替换
│       - 增量采用新 API
│
└─ 否 → 继续使用 fastjson2
        - fastjson2 仍在维护
        - API 与 fastjson3 几乎相同
```

---

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

### fastjson2 特有功能对比

fastjson2 和 fastjson3 在以下方面有所不同：

| 功能 | fastjson2 | fastjson3 |
|------|-----------|-----------|
| **ObjectMapper** | 基础支持 | 完整 Jackson 风格 |
| **JSONReader.Feature** | ✅ | ✅（相同） |
| **JSONWriter.Feature** | ✅ | ✅（相同） |
| **直接使用 ASM** | 内部 | 内部（更优化） |
| **Kotlin 数据类** | 支持 | 更好支持 |
| **Record** | 支持 | 原生支持 |

### JSONReader / JSONWriter（完全相同）

```java
// ===== fastjson2 =====
JSONReader reader = JSONReader.of(json);
User user = reader.read(User.class);

JSONWriter writer = JSONWriter.of(JSONWriter.Feature.PrettyFormat);
writer.writeObject(user);

// ===== fastjson3 =====
// API 完全相同
JSONReader reader = JSONReader.of(json);
User user = reader.read(User.class);

JSONWriter writer = JSONWriter.of(JSONWriter.Feature.PrettyFormat);
writer.writeObject(user);
```

### JSONPath（完全相同）

```java
// fastjson2 和 fastjson3 完全相同
JSONPath path = JSONPath.of("$.store.book[*].author");
List<String> authors = path.extract(json);
```

---

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

---

## 迁移检查清单

### 代码修改

- [ ] 更新 Maven/Gradle 依赖
- [ ] 批量替换包名 `fastjson2` → `fastjson3`
- [ ] 运行测试验证兼容性
- [ ] （可选）采用 `ObjectMapper` 新 API

### 最小改动迁移

```bash
# 批量替换包名
find . -name "*.java" -type f -exec sed -i 's/com\.alibaba\.fastjson2\./com.alibaba.fastjson3./g' {} +
```

### 可选优化

- [ ] 采用 `ObjectMapper.shared()` 替代静态方法
- [ ] 使用 `JSONReader.Feature` / `JSONWriter.Feature`
- [ ] 利用 Java 21+ 新特性（Record、sealed class）

---

## 快速参考

### 包名映射

| fastjson2 | fastjson3 |
|-----------|-----------|
| `com.alibaba.fastjson2.JSON` | `com.alibaba.fastjson3.JSON` |
| `com.alibaba.fastjson2.JSONObject` | `com.alibaba.fastjson3.JSONObject` |
| `com.alibaba.fastjson2.annotation.JSONField` | `com.alibaba.fastjson3.annotation.JSONField` |

### 完全相同的 API

```java
// 这些 API 在 fastjson2 和 fastjson3 中完全相同
JSON.parseObject(json)
JSON.toJSONString(obj)
JSONPath.of(path)
WriteFeature.PrettyFormat
ReadFeature.SupportAutoType
```

## 相关文档

- [API 参考](../api/)
- [ObjectMapper 文档](../api/ObjectMapper.md)
- [迁移检查清单](checklist.md)

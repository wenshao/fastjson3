# 迁移到 fastjson3

本文档帮助你从其他 JSON 库迁移到 fastjson3。设计为易于人类阅读和大语言模型（LLM）辅助迁移。

---

## 快速开始

### 1. 选择你的来源库

| 来源库 | 迁移文档 | 难度 | Java 要求 |
|--------|----------|------|----------|
| fastjson 1.x | [from-fastjson1.md](from-fastjson1.md) | ⭐⭐ | Java 6 → 21 |
| fastjson2 | [from-fastjson2.md](from-fastjson2.md) | ⭐ | Java 8 → 21 |
| Jackson 2.x | [from-jackson2.md](from-jackson2.md) | ⭐⭐ | Java 8 → 21 |
| Jackson 3.x | [from-jackson3.md](from-jackson3.md) | ⭐ | Java 17 → 21 |
| Gson | [from-gson.md](from-gson.md) | ⭐⭐ | Java 8 → 21 |
| org.json | [from-org-json.md](from-org-json.md) | ⭐⭐ | Java 8 → 21 |

### 2. 检测你的项目

```bash
# 检测 Jackson
grep -r "jackson" pom.xml build.gradle

# 检测 fastjson
grep -r "fastjson" pom.xml build.gradle

# 检测 Gson
grep -r "gson" pom.xml build.gradle

# 检测 org.json
grep -r "org.json" pom.xml build.gradle
```

### 3. 更新依赖

```xml
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## 代码模式索引

### 模式：解析 JSON

| 来源库 | 旧代码 | fastjson3 |
|--------|--------|-----------|
| Jackson | `mapper.readValue(json, User.class)` | `mapper.readValue(json, User.class)` |
| Gson | `gson.fromJson(json, User.class)` | `JSON.parseObject(json, User.class)` |
| fastjson1 | `JSON.parseObject(json, User.class)` | `JSON.parseObject(json, User.class)` |

### 模式：序列化对象

| 来源库 | 旧代码 | fastjson3 |
|--------|--------|-----------|
| Jackson | `mapper.writeValueAsString(obj)` | `mapper.writeValueAsString(obj)` |
| Gson | `gson.toJson(obj)` | `JSON.toJSONString(obj)` |
| fastjson1 | `JSON.toJSONString(obj)` | `JSON.toJSONString(obj)` |

### 模式：创建 ObjectMapper

| 来源库 | 旧代码 | fastjson3 |
|--------|--------|-----------|
| Jackson 2.x | `new ObjectMapper()` | `ObjectMapper.builder().build()` |
| Jackson 3.x | `JsonMapper.builder().build()` | `ObjectMapper.builder().build()` |
| fastjson2 | `new JSONObject()` | `ObjectMapper.builder().build()` |

### 模式：泛型处理

| 来源库 | 旧代码 | fastjson3 |
|--------|--------|-----------|
| Jackson | `new TypeReference<List<User>>(){}` | `TypeToken.listOf(User.class)` |
| Gson | `new TypeToken<List<User>>(){}` | `TypeToken.listOf(User.class)` |
| fastjson1 | `new TypeReference<List<User>>(){}` | `TypeToken.listOf(User.class)` |

### 模式：字段注解

| 来源库 | 旧注解 | fastjson3 |
|--------|--------|-----------|
| Jackson | `@JsonProperty("name")` | `@JSONField(name = "name")` 或保留 |
| Gson | `@SerializedName("name")` | `@JSONField(name = "name")` |
| fastjson1 | `@JSONField(name = "name")` | `@JSONField(name = "name")` |

**注意**：fastjson3 原生支持 Jackson 和 fastjson 1.x 注解，通常无需修改。

---

## 完整迁移示例

### Jackson 2.x → fastjson3

**原代码：**
```java
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper mapper = new ObjectMapper();
mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
User user = mapper.readValue(json, User.class);
String output = mapper.writeValueAsString(user);
```

**迁移后：**
```java
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.WriteFeature;

ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
User user = mapper.readValue(json, User.class);
String output = mapper.writeValueAsString(user);
```

### fastjson 1.x → fastjson3

**原代码：**
```java
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

String json = JSON.toJSONString(user,
    SerializerFeature.WriteMapNullValue,
    SerializerFeature.PrettyFormat);
```

**迁移后：**
```java
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.WriteFeature;

String json = JSON.toJSONString(user,
    WriteFeature.WriteNulls,
    WriteFeature.PrettyFormat);
```

### Gson → fastjson3

**原代码：**
```java
import com.google.gson.Gson;

Gson gson = new Gson();
String json = gson.toJson(user);
User user = gson.fromJson(json, User.class);
```

**迁移后：**
```java
import com.alibaba.fastjson3.JSON;

String json = JSON.toJSONString(user);
User user = JSON.parseObject(json, User.class);
```

---

## Feature 枚举映射

### SerializationFeature → WriteFeature

| Jackson 2.x | fastjson3 |
|-------------|-----------|
| `INDENT_OUTPUT` | `PrettyFormat` |
| `WRITE_NULL_MAP_VALUES` | `WriteNulls` |
| `WRITE_ENUMS_USING_TO_STRING` | `WriteEnumUsingToString` |
| `WRITE_DATES_AS_TIMESTAMPS` | `@JSONField(format="millis")` |

### SerializerFeature → WriteFeature

| fastjson 1.x | fastjson3 |
|--------------|-----------|
| `WriteMapNullValue` | `WriteNulls` |
| `PrettyFormat` | `PrettyFormat` |
| `WriteDateUseDateFormat` | 使用 `@JSONField(format = "...")` 注解 |
| `DisableCircularReferenceDetect` | 禁用 `ReferenceDetection` |

---

## 注解映射

### 字段注解

| Jackson | fastjson3 | 兼容性 |
|---------|-----------|--------|
| `@JsonProperty` | `@JSONField(name)` | ✅ 原生支持 |
| `@JsonAlias` | `@JSONField(alternateNames)` | ✅ 原生支持 |
| `@JsonIgnore` | `@JSONField(serialize=false)` | ✅ 原生支持 |
| `@JsonFormat` | `@JSONField(format)` | ✅ 原生支持 |
| `@JsonInclude` | `@JSONField(inclusion)` | ✅ 原生支持 |
| `@JsonUnwrapped` | - | ❌ 不支持，需自定义序列化 |
| `@JsonNaming` | `@JSONType(naming)` | ✅ 原生支持 |
| `@JsonCreator` | `@JSONCreator` | ✅ 原生支持 |

### 类注解

| Jackson | fastjson3 | 兼容性 |
|---------|-----------|--------|
| `@JsonNaming` | `@JSONType(naming)` | ✅ 原生支持 |
| `@JsonPropertyOrder` | `@JSONType(orders)` | ✅ 原生支持 |
| `@JsonInclude` | `@JSONType(inclusion)` | ✅ 原生支持 |

---

## 常见问题

### Q: 需要修改 Jackson 注解吗？

A: 不需要。fastjson3 原生支持大部分 Jackson 注解，可以直接使用。

### Q: 需要修改 fastjson 1.x 注解吗？

A: 不需要。fastjson3 的注解与 1.x 完全兼容。

### Q: fastjson3 的 Java 版本要求？

A: Java 21 或更高版本。

### Q: 如何迁移泛型代码？

A: 使用 `TypeToken` 工厂方法替代 `TypeReference`：
```java
TypeToken.listOf(User.class)
TypeToken.mapOf(User.class)
TypeToken.arrayOf(User.class)
```

---

## 辅助工具

### 批量替换包名

```bash
# Jackson → fastjson3 (保留注解)
find . -name "*.java" -exec sed -i 's/import com.fasterxml.jackson.*/import com.alibaba.fastjson3./g' {} +

# fastjson1 → fastjson3
find . -name "*.java" -exec sed -i 's/com\.alibaba\.fastjson\./com.alibaba.fastjson3./g' {} +

# Feature 枚举
find . -name "*.java" -exec sed -i 's/SerializerFeature/WriteFeature/g' {} +
```

### 验证迁移

```bash
# 编译检查
mvn clean compile

# 运行测试
mvn test
```

---

## 相关文档

- [迁移检查清单](checklist.md)
- [常见问题 FAQ](faq.md)
- [API 参考](../api/)
- [注解使用指南](../guides/annotations.md)

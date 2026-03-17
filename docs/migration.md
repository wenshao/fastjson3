# 迁移指南

本文档帮助你从其他 JSON 库迁移到 fastjson3。

## 快速导航

| 来源库 | 迁移难度 | 兼容性 |
|--------|----------|--------|
| fastjson2 | ⭐ 低 | 高度兼容 |
| Jackson 2.x/3.x | ⭐⭐ 中 | API 相似 |
| Gson | ⭐⭐ 中 | 需要适应 |
| org.json | ⭐⭐ 中 | 完全重写 |

---

## 从 fastjson2 迁移

### API 兼容性

大部分 API **完全兼容**，无需修改代码：

```java
// fastjson2 / fastjson3 都支持
JSONObject obj = JSON.parseObject(json);
User user = JSON.parseObject(json, User.class);
List<User> users = JSON.parseArray(json, User.class);
String json = JSON.toJSONString(user);
byte[] bytes = JSON.toJSONBytes(user);
```

### 新增 API

fastjson3 新增 Jackson 3 风格的 `ObjectMapper`：

```java
// fastjson3 新 API（推荐）
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);
String json = mapper.writeValueAsString(user);
```

### 迁移步骤

1. **直接替换依赖**

```xml
<!-- 移除 -->
<!--<dependency>-->
<!--    <groupId>com.alibaba.fastjson2</groupId>-->
<!--    <artifactId>fastjson2</artifactId>-->
<!--    <version>2.x.x</version>-->
<!--</dependency>-->

<!-- 添加 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

2. **测试现有代码**

现有代码应该无需修改即可运行。

3. **可选：采用新 API**

```java
// 旧方式
User user = JSON.parseObject(json, User.class);

// 新方式（更灵活）
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);
```

### 特性对照

| fastjson2 | fastjson3 | 说明 |
|-----------|-----------|------|
| `JSONReader` | ✅ 支持 | 相同 API |
| `JSONWriter` | ✅ 支持 | 相同 API |
| `JSONObject` | ✅ 支持 | 相同 API |
| `JSONArray` | ✅ 支持 | 相同 API |
| `JSONPath` | ✅ 支持 | 相同 API |
| `@JSONField` | ✅ 支持 | 相同注解 |
| `@JSONType` | ✅ 支持 | 相同注解 |
| `ObjectMapper` | ✅ 新增 | Jackson 风格 |

### 特性名称变化

部分特性名称可能调整，请参考 `ReadFeature` 和 `WriteFeature` 枚举。

---

## 从 Jackson 迁移

### API 对照表

| Jackson | fastjson3 | 说明 |
|---------|-----------|------|
| `new ObjectMapper()` | `ObjectMapper.builder().build()` 或 `ObjectMapper.shared()` | fastjson3 不可变 |
| `mapper.readValue(json, User.class)` | `mapper.readValue(json, User.class)` | **相同** |
| `mapper.writeValue(obj)` | `mapper.writeValueAsString(obj)` | 方法名不同 |
| `@JsonProperty` | `@JSONField` 或 `@JsonProperty` | fastjson3 支持两种 |
| `@JsonFormat` | `@JSONField(format="...")` | 格式化注解 |
| `@JsonIgnore` | `@JSONField(serialize=false)` | 忽略序列化 |
| `TypeReference` | `TypeToken` | 泛型处理 |

### 注解兼容

fastjson3 **原生支持 Jackson 注解**：

```java
public class User {
    @JsonProperty("user_name")  // Jackson 注解，fastjson3 也支持
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")  // Jackson 注解
    private Date birthday;

    @JsonIgnore  // Jackson 注解
    private String password;
}
```

**支持的 Jackson 注解：**

- `@JsonProperty` - 字段重命名
- `@JsonFormat` - 日期格式
- `@JsonIgnore` - 忽略字段
- `@JsonInclude` - 包含策略（部分支持）
- `@JsonUnwrapped` - 展平嵌套
- `@JsonAlias` - 替代名称

### 配置迁移

```java
// Jackson
ObjectMapper mapper = new ObjectMapper();
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
mapper.configure(SerializationFeature.WRITE_NULLS, true);

// fastjson3
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.ErrorOnUnknownProperties)
    .enableWrite(WriteFeature.PrettyFormat)
    .enableWrite(WriteFeature.WriteNulls)
    .build();
```

### 完整迁移示例

```java
// ===== Jackson =====
ObjectMapper mapper = new ObjectMapper();

// 解析
User user = mapper.readValue(json, User.class);

// 序列化
String json = mapper.writeValueAsString(user);

// 泛型
TypeReference<List<User>> typeRef = new TypeReference<List<User>>() {};
List<User> users = mapper.readValue(json, typeRef);

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.shared();

// 解析（相同）
User user = mapper.readValue(json, User.class);

// 序列化（方法名不同）
String json = mapper.writeValueAsString(user);

// 泛型（使用 TypeToken）
TypeToken<List<User>> typeToken = new TypeToken<List<User>>() {};
List<User> users = mapper.readValue(json, typeToken);
```

### Spring Boot 集成

```java
// Jackson 自动配置（移除）
// @JsonAutoDetect(fieldVisibility = Visibility.ANY)

// fastjson3 配置
@Configuration
public class Fastjson3Config {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return ObjectMapper.builder()
            .enableRead(ReadFeature.SupportSmartMatch)
            .enableWrite(WriteFeature.PrettyFormat)
            .build();
    }
}
```

---

## 从 Gson 迁移

### API 对照表

| Gson | fastjson3 | 说明 |
|------|-----------|------|
| `new Gson()` | `ObjectMapper.shared()` | 使用共享实例 |
| `gson.fromJson(json, User.class)` | `mapper.readValue(json, User.class)` | 方法名不同 |
| `gson.toJson(obj)` | `mapper.writeValueAsString(obj)` | 方法名不同 |
| `@SerializedName` | `@JSONField(name="...")` | 注解不同 |

### 注解迁移

```java
// Gson
@SerializedName("user_name")
private String userName;

// fastjson3
@JSONField(name = "user_name")
private String userName;
```

### 日期处理

```java
// Gson
Gson gson = new GsonBuilder()
    .setDateFormat("yyyy-MM-dd HH:mm:ss")
    .create();

// fastjson3
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;
```

### 完整迁移示例

```java
// ===== Gson =====
Gson gson = new Gson();
User user = gson.fromJson(json, User.class);
String json = gson.toJson(user);

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);
String json = mapper.writeValueAsString(user);
```

---

## 从 org.json 迁移

### API 对照表

| org.json | fastjson3 | 说明 |
|----------|-----------|------|
| `new JSONObject(json)` | `JSON.parseObject(json)` | 静态方法 |
| `new JSONArray(json)` | `JSON.parseArray(json)` | 静态方法 |
| `obj.getString("key")` | `obj.getString("key")` | 相同 |
| `obj.optXXX("key")` | `obj.getString("key", def)` | 默认值方式不同 |
| `obj.put("key", value)` | `obj.put("key", value)` | 相同 |
| `obj.has("key")` | `obj.containsKey("key")` | 方法名不同 |

### 完整迁移示例

```java
// ===== org.json =====
JSONObject obj = new JSONObject(json);
String name = obj.getString("name");
int age = obj.getInt("age");
obj.put("city", "北京");
String result = obj.toString();

// ===== fastjson3 =====
JSONObject obj = JSON.parseObject(json);
String name = obj.getString("name");
int age = obj.getIntValue("age");
obj.put("city", "北京");
String result = obj.toJSONString();
```

### JSONArray 迁移

```java
// ===== org.json =====
JSONArray arr = new JSONArray(json);
String first = arr.getString(0);
int length = arr.length();

// ===== fastjson3 =====
JSONArray arr = JSON.parseArray(json);
String first = arr.getString(0);
int length = arr.size();
```

---

## 从其他库迁移

### net.sf.json

```java
// net.sf.json-lib
JSONObject obj = JSONObject.fromObject(json);

// fastjson3
JSONObject obj = JSON.parseObject(json);
```

### Flexjson

```java
// Flexjson
JSONSerializer serializer = new JSONSerializer();
String json = serializer.serialize(obj);

// fastjson3
String json = JSON.toJSONString(obj);
```

---

## 迁移检查清单

### 依赖替换

- [ ] 移除旧库依赖
- [ ] 添加 fastjson3 依赖
- [ ] 更新版本号

### 代码更新

- [ ] 更新 import 语句
- [ ] 更新注解（如果需要）
- [ ] 更新 API 调用

### 配置更新

- [ ] 更新 ObjectMapper 配置
- [ ] 更新特性配置
- [ ] 更新日期格式

### 测试

- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 性能测试通过
- [ ] 边界情况测试

### 部署

- [ ] 本地验证
- [ ] 测试环境验证
- [ ] 灰度发布
- [ ] 监控错误日志

---

## 常见迁移问题

### Q: fastjson3 完全兼容 fastjson2 吗？

A: 基础 API 完全兼容，新增了 ObjectMapper 风格的 API。

### Q: 需要修改代码吗？

A: 如果使用基础 API (JSON.parseObject 等)，通常不需要。使用 Jackson 注解的代码也不需要修改。

### Q: 性能如何？

A: fastjson3 继承了 fastjson2 的高性能引擎，性能相当或更好。

### Q: 如何处理日期？

A: 使用 `@JSONField(format = "...")` 注解或 Java Time API。

### Q: 支持 Kotlin 吗？

A: 支持，fastjson3 可以处理 Kotlin 数据类。

---

## 完整示例

### fastjson2 → fastjson3

```java
// fastjson2
String json = JSON.toJSONString(user, WriteFeature.PrettyFormat);
User user = JSON.parseObject(json, User.class);

// fastjson3（方式1：兼容 API）
String json = JSON.toJSONString(user, WriteFeature.PrettyFormat);
User user = JSON.parseObject(json, User.class);

// fastjson3（方式2：推荐 API）
ObjectMapper mapper = ObjectMapper.shared();
String json = mapper.writeValueAsString(user);
User user = mapper.readValue(json, User.class);
```

### Jackson → fastjson3

```java
// Jackson
ObjectMapper mapper = new ObjectMapper();
mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

// fastjson3（注解通常不需要改）
// @JsonProperty("user_name") 仍然有效

ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
```

### Gson → fastjson3

```java
// Gson
Gson gson = new GsonBuilder()
    .setDateFormat("yyyy-MM-dd")
    .create();

// fastjson3
@JSONField(format = "yyyy-MM-dd")
private Date date;

ObjectMapper mapper = ObjectMapper.shared();
```

---

## 相关文档

- [📖 API 参考](api/)
- [📖 注解参考](api/annotations.md)
- [❓ 常见问题](faq.md)

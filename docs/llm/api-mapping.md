# API 映射表

本文档提供完整的 API 映射表，便于大语言模型（LLM）在迁移代码时查找对应 API。

---

## 核心类映射

### ObjectMapper

| 来源库 | 类名 | fastjson3 | 说明 |
|--------|------|-----------|------|
| Jackson 2.x | `ObjectMapper` | `ObjectMapper` | 创建方式不同 |
| Jackson 3.x | `JsonMapper` | `ObjectMapper` | 创建方式不同 |
| Gson | `Gson` | `ObjectMapper` | API 不同 |
| fastjson1 | `JSON` | `ObjectMapper` | 推荐 ObjectMapper |

### JSON 解析类

| 来源库 | 类名 | fastjson3 | 说明 |
|--------|------|-----------|------|
| Jackson | `JsonNode` | `JSONObject` | API 不同 |
| Jackson | `ObjectNode` | `JSONObject` | API 不同 |
| Jackson | `ArrayNode` | `JSONArray` | API 不同 |
| Gson | `JsonElement` | `JSON` | 基类概念 |
| Gson | `JsonObject` | `JSONObject` | API 不同 |
| Gson | `JsonArray` | `JSONArray` | API 不同 |
| fastjson1 | `JSONObject` | `JSONObject` | API 相同 |
| fastjson1 | `JSONArray` | `JSONArray` | API 相同 |

---

## 方法映射

### 解析 JSON 字符串

| 来源库 | 方法 | fastjson3 |
|--------|------|-----------|
| Jackson | `mapper.readValue(json, Class)` | `mapper.readValue(json, Class)` |
| Jackson | `mapper.readTree(json)` | `JSON.parseObject(json)` |
| Gson | `gson.fromJson(json, Class)` | `JSON.parseObject(json, Class)` |
| fastjson1 | `JSON.parseObject(json, Class)` | `JSON.parseObject(json, Class)` |

### 序列化对象

| 来源库 | 方法 | fastjson3 |
|--------|------|-----------|
| Jackson | `mapper.writeValueAsString(obj)` | `mapper.writeValueAsString(obj)` |
| Gson | `gson.toJson(obj)` | `JSON.toJSONString(obj)` |
| fastjson1 | `JSON.toJSONString(obj)` | `JSON.toJSONString(obj)` |

### 泛型处理

| 来源库 | 方法 | fastjson3 |
|--------|------|-----------|
| Jackson | `new TypeReference<List<T>>(){}` | `TypeToken.listOf(T.class)` |
| Gson | `new TypeToken<List<T>>(){}` | `TypeToken.listOf(T.class)` |
| fastjson1 | `new TypeReference<List<T>>(){}` | `TypeToken.listOf(T.class)` |

---

## 枚举映射

### 序列化 Feature

| Jackson 2.x SerializerFeature | fastjson3 WriteFeature |
|------------------------------|----------------------|
| `INDENT_OUTPUT` | `PrettyFormat` |
| `WRITE_NULL_MAP_VALUES` | `WriteNulls` |
| `WRITE_ENUMS_USING_TO_STRING` | `WriteEnumUsingToString` |
| `WRITE_DATES_AS_TIMESTAMPS` | `@JSONField(format="millis")` |
| `WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED` | - |

| fastjson1 SerializerFeature | fastjson3 WriteFeature |
|---------------------------|----------------------|
| `WriteMapNullValue` | `WriteNulls` |
| `PrettyFormat` | `PrettyFormat` |
| `WriteDateUseDateFormat` | 使用 `@JSONField(format = "...")` 注解 |
| `WriteEnumUsingToString` | `WriteEnumUsingToString` |
| `DisableCircularReferenceDetect` | 禁用 `ReferenceDetection` |
| `BrowserCompatible` | (默认行为) |
| `WriteNonStringValueAsString` | (默认行为) |

### 反序列化 Feature

| Jackson 2.x DeserializationFeature | fastjson3 ReadFeature |
|----------------------------------|---------------------|
| `FAIL_ON_UNKNOWN_PROPERTIES` | `ErrorOnUnknownProperties` |
| `ACCEPT_SINGLE_VALUE_AS_ARRAY` | `SupportArrayToBean` |
| `ALLOW_UNQUOTED_FIELD_NAMES` | `AllowUnquotedFieldNames` |
| `ALLOW_COMMENTS` | `AllowComments` |

| fastjson1 Feature | fastjson3 ReadFeature |
|------------------|---------------------|
| `AllowComments` | `AllowComments` |
| `AllowUnquotedFieldNames` | `AllowUnquotedFieldNames` |
| `SupportSmartMatch` | `SupportSmartMatch` |
| `SupportArrayToBean` | `SupportArrayToBean` |
| `AutoCloseSource` | (默认行为) |

---

## 注解映射

### 字段级注解

| Jackson | fastjson3 | 说明 |
|---------|-----------|------|
| `@JsonProperty("name")` | `@JSONField(name = "name")` | ✅ 原生支持 |
| `@JsonAlias({"a", "b"})` | `@JSONField(alternateNames = {"a", "b"})` | ✅ 原生支持 |
| `@JsonIgnore` | `@JSONField(serialize = false)` | ✅ 原生支持 |
| `@JsonFormat(pattern = "fmt")` | `@JSONField(format = "fmt")` | ✅ 原生支持 |
| `@JsonInclude(NON_NULL)` | `@JSONField(inclusion = Inclusion.NON_NULL)` | ✅ 原生支持 |
| `@JsonUnwrapped` | 不支持，需自定义序列化器 | ⚠️ 手动处理 |
| `@JsonProperty(required = true)` | `@JSONField(required = true)` | ✅ 原生支持 |
| `@JsonSerialize(using = X.class)` | `@JSONField(serializeUsing = X.class)` | ✅ 原生支持 |
| `@JsonDeserialize(using = X.class)` | `@JSONField(deserializeUsing = X.class)` | ✅ 原生支持 |

| Gson | fastjson3 | 说明 |
|-------|-----------|------|
| `@SerializedName("name")` | `@JSONField(name = "name")` | 需要替换 |
| `@Expose` | (默认行为) | 需要删除或保留 |
| `@Expose(serialize = false)` | `@JSONField(serialize = false)` | 需要替换 |

### 类级注解

| Jackson | fastjson3 | 说明 |
|---------|-----------|------|
| `@JsonNaming(SnakeCaseStrategy.class)` | `@JSONType(naming = NamingStrategy.SnakeCase)` | ✅ 原生支持 |
| `@JsonPropertyOrder({"a", "b"})` | `@JSONType(orders = {"a", "b"})` | ✅ 原生支持 |
| `@JsonInclude(JsonInclude.Include.NON_NULL)` | `@JSONType(inclusion = Inclusion.NON_NULL)` | ✅ 原生支持 |

| fastjson1 | fastjson3 | 说明 |
|-----------|-----------|------|
| `@JSONField(name = "xxx")` | `@JSONField(name = "xxx")` | 相同 |
| `@JSONType(naming = ...)` | `@JSONType(naming = ...)` | 相同 |
| `@JSONCreator` | `@JSONCreator` | 相同 |

---

## 命名策略映射

| Jackson | fastjson3 |
|---------|-----------|
| `PropertyNamingStrategies.SnakeCaseStrategy` | `NamingStrategy.SnakeCase` |
| `PropertyNamingStrategies.LowerCaseStrategy` | `NamingStrategy.CamelCase` |
| `PropertyNamingStrategies.KebabCaseStrategy` | `NamingStrategy.KebabCase` |
| `PropertyNamingStrategies.UpperCamelCaseStrategy` | `NamingStrategy.PascalCase` |

---

## JSONObject API 映射

### 读取字段

| Jackson JsonNode | fastjson3 JSONObject |
|------------------|---------------------|
| `node.get("field").asText()` | `obj.getString("field")` |
| `node.get("field").asInt()` | `obj.getIntValue("field")` |
| `node.get("field").asLong()` | `obj.getLongValue("field")` |
| `node.get("field").asDouble()` | `obj.getDoubleValue("field")` |
| `node.get("field").asBoolean()` | `obj.getBooleanValue("field")` |
| `node.has("field")` | `obj.containsKey("field")` |
| `node.isObject()` | `obj instanceof JSONObject` |
| `node.isArray()` | `obj instanceof JSONArray` |

### Gson JsonObject

| Gson JsonObject | fastjson3 JSONObject |
|----------------|---------------------|
| `obj.get("field").getAsString()` | `obj.getString("field")` |
| `obj.get("field").getAsInt()` | `obj.getIntValue("field")` |
| `obj.has("field")` | `obj.containsKey("field")` |
| `obj.isJsonNull()` | `obj.get("field") == null` |

---

## JSONArray API 映射

### 访问元素

| Jackson ArrayNode | fastjson3 JSONArray |
|------------------|---------------------|
| `node.get(0)` | `arr.get(0)` |
| `node.size()` | `arr.size()` |
| `node.isArray()` | `arr instanceof JSONArray` |
| `node.elements()` | `arr.iterator()` |

### Gson JsonArray

| Gson JsonArray | fastjson3 JSONArray |
|----------------|---------------------|
| `array.get(i)` | `arr.get(i)` |
| `array.size()` | `arr.size()` |

---

## 包名映射

### Maven 依赖

```xml
<!-- Jackson -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- fastjson3 -->
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 导入包名

| 来源库 | 包名 | fastjson3 包名 |
|--------|------|---------------|
| Jackson 2.x | `com.fasterxml.jackson.*` | `com.alibaba.fastjson3.*` |
| Jackson 3.x | `com.fasterxml.jackson.*` | `com.alibaba.fastjson3.*` |
| Gson | `com.google.gson.*` | `com.alibaba.fastjson3.*` |
| fastjson1 | `com.alibaba.fastjson.*` | `com.alibaba.fastjson3.*` |
| fastjson2 | `com.alibaba.fastjson2.*` | `com.alibaba.fastjson3.*` |

---

## 常用代码转换

### 读取配置文件

| 来源库 | 代码 | fastjson3 |
|--------|------|-----------|
| Jackson | `mapper.configure(feat, true)` | `builder().enableXxx()` |
| Jackson | `mapper.setSerializationInclusion()` | `builder().inclusion()` |
| fastjson1 | `JSON.DEFAULT_GENERATE_FEATURE` | 不支持，使用 ObjectMapper |

### JSONPath 查询

| 来源库 | 代码 | fastjson3 |
|--------|------|-----------|
| fastjson1 | `JSONPath.eval(path)` | `JSONPath.eval(path)` |
| fastjson2 | `JSONPath.of(path)` | `JSONPath.of(path)` |
| Jackson | `JsonPointer.compile("/field")` | `JSONPath.of("$.field")` |

---

## 类型映射

### Java 类型

| Jackson/Gson 类型 | fastjson3 类型 |
|------------------|---------------|
| `TypeReference<T>` | `TypeReference<T>` |
| `TypeToken<T>` | `TypeToken<T>` |
| `JsonNode` | `JSON` |
| `ObjectNode` | `JSONObject` |
| `ArrayNode` | `JSONArray` |
| `JsonParser` | `JSONReader` |
| `JsonGenerator` | `JSONGenerator` |

---

## Spring Boot 集成

| 来源库 | 配置方式 | fastjson3 配置方式 |
|--------|----------|-------------------|
| Jackson | `@Primary ObjectMapper` bean | `@Primary ObjectMapper` bean |
| Jackson | `Jackson2ObjectMapperBuilder` | `ObjectMapper.builder()` |
| Gson | `GsonBuilder` | `ObjectMapper.builder()` |
| fastjson2 | `FastJsonHttpMessageConverter` | `FastjsonHttpMessageConverter` |

---

## 异常处理

| 来源库 | 异常 | fastjson3 |
|--------|------|-----------|
| Jackson | `JsonProcessingException` | `JSONException` |
| Jackson | `JsonMappingException` | `JSONException` |
| Jackson | `IOException` | `IOException` |
| Gson | `JsonSyntaxException` | `JSONException` |
| fastjson1 | `JSONException` | `JSONException` |

---

## 相关文档

- [代码模式库](../patterns/README.md)
- [迁移脚本模板](scripts.md)

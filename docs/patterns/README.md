# 代码模式库

本文档提供常见代码模式的迁移示例，便于大语言模型（LLM）辅助迁移。

---

## 模式：创建 ObjectMapper

### Jackson 2.x → fastjson3

```java
// ===== 原代码 (Jackson 2.x) =====
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper mapper = new ObjectMapper();

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.ObjectMapper;

// 方式1：默认配置
ObjectMapper mapper = ObjectMapper.shared();

// 方式2：自定义配置
ObjectMapper mapper = ObjectMapper.builder().build();
```

### Jackson 3.x → fastjson3

```java
// ===== 原代码 (Jackson 3.x) =====
import com.fasterxml.jackson.databind.json.JsonMapper;

JsonMapper mapper = JsonMapper.builder().build();

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.ObjectMapper;

ObjectMapper mapper = ObjectMapper.builder().build();
```

### fastjson 1.x → fastjson3

```java
// ===== 原代码 (fastjson 1.x) =====
import com.alibaba.fastjson.JSON;

// 直接使用静态方法
JSONObject obj = JSON.parseObject(json);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.JSON;

// 方式1：静态方法（保持不变）
JSONObject obj = JSON.parseObject(json);

// 方式2：使用 ObjectMapper
ObjectMapper mapper = ObjectMapper.shared();
JSONObject obj = mapper.readValue(json, JSONObject.class);
```

---

## 模式：解析 JSON 字符串

### Jackson → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper mapper = new ObjectMapper();
User user = mapper.readValue(jsonString, User.class);
List<User> users = mapper.readValue(jsonString, new TypeReference<List<User>>() {});

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.TypeToken;

ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(jsonString, User.class);

// 方式1：使用 TypeToken（推荐）
List<User> users = JSON.parse(jsonString, TypeToken.listOf(User.class));

// 方式2：使用 TypeReference（兼容）
List<User> users = mapper.readValue(jsonString, new TypeReference<List<User>>() {});
```

### Gson → fastjson3

```java
// ===== 原代码 (Gson) =====
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

Gson gson = new Gson();
User user = gson.fromJson(jsonString, User.class);
List<User> users = gson.fromJson(jsonString, new TypeToken<List<User>>() {});

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.TypeToken;

User user = JSON.parseObject(jsonString, User.class);
List<User> users = JSON.parse(jsonString, TypeToken.listOf(User.class));
```

### fastjson 1.x → fastjson3

```java
// ===== 原代码 (fastjson 1.x) =====
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

User user = JSON.parseObject(jsonString, User.class);
List<User> users = JSON.parseObject(jsonString, new TypeReference<List<User>>() {});

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.TypeToken;

User user = JSON.parseObject(jsonString, User.class);
List<User> users = JSON.parse(jsonString, TypeToken.listOf(User.class));
```

---

## 模式：序列化对象

### Jackson → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(user);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.ObjectMapper;

ObjectMapper mapper = ObjectMapper.shared();
String json = mapper.writeValueAsString(user);
```

### Gson → fastjson3

```java
// ===== 原代码 (Gson) =====
import com.google.gson.Gson;

Gson gson = new Gson();
String json = gson.toJson(user);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.JSON;

String json = JSON.toJSONString(user);
```

### fastjson 1.x → fastjson3

```java
// ===== 原代码 (fastjson 1.x) =====
import com.alibaba.fastjson.JSON;

String json = JSON.toJSONString(user);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.JSON;

String json = JSON.toJSONString(user);
// API 完全相同
```

---

## 模式：配置格式化输出

### Jackson → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

ObjectMapper mapper = new ObjectMapper();
mapper.enable(SerializationFeature.INDENT_OUTPUT);
String json = mapper.writeValueAsString(obj);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.WriteFeature;

ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
String json = mapper.writeValueAsString(obj);
```

### fastjson 1.x → fastjson3

```java
// ===== 原代码 (fastjson 1.x) =====
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

String json = JSON.toJSONString(obj, SerializerFeature.PrettyFormat);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.WriteFeature;

String json = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
```

---

## 模式：处理 null 值

### Jackson → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

ObjectMapper mapper = new ObjectMapper();
mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
String json = mapper.writeValueAsString(obj);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.WriteFeature;

ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.WriteNulls)
    .build();
String json = mapper.writeValueAsString(obj);
```

### Gson → fastjson3

```java
// ===== 原代码 (Gson) =====
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

Gson gson = new GsonBuilder()
    .serializeNulls()
    .create();
String json = gson.toJson(obj);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.WriteFeature;

String json = JSON.toJSONString(obj, WriteFeature.WriteNulls);
```

---

## 模式：字段注解

### Jackson @JsonProperty → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_name")
    private String userName;
}

// ===== 迁移后 (fastjson3) - 保留 Jackson 注解 =====
// fastjson3 原生支持 Jackson 注解，无需修改
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_name")
    private String userName;
}

// ===== 迁移后 (fastjson3) - 替换为 fastjson3 注解 =====
import com.alibaba.fastjson3.annotation.JSONField;

public class User {
    @JSONField(name = "user_id")
    private Long userId;

    @JSONField(name = "user_name")
    private String userName;
}
```

### Gson @SerializedName → fastjson3

```java
// ===== 原代码 (Gson) =====
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_id")
    private Long userId;
}

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.annotation.JSONField;

public class User {
    @JSONField(name = "user_id")
    private Long userId;
}
```

### Jackson @JsonIgnore → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
    @JsonIgnore
    private String password;
}

// ===== 迁移后 (fastjson3) - 保留 Jackson 注解 =====
import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
    @JsonIgnore
    private String password;
}

// ===== 迁移后 (fastjson3) - 替换为 fastjson3 注解 =====
import com.alibaba.fastjson3.annotation.JSONField;

public class User {
    @JSONField(serialize = false)
    private String password;
}
```

---

## 模式：日期格式化

### Jackson @JsonFormat → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.annotation.JsonFormat;

public class Event {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}

// ===== 迁移后 (fastjson3) - 保留 Jackson 注解 =====
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class Event {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

// ===== 迁移后 (fastjson3) - 替换为 fastjson3 注解 =====
import com.alibaba.fastjson3.annotation.JSONField;
import java.time.LocalDateTime;

public class Event {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
```

### fastjson 1.x @JSONField → fastjson3

```java
// ===== 原代码 (fastjson 1.x) =====
import com.alibaba.fastjson.annotation.JSONField;
import java.util.Date;

public class Event {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}

// ===== 迁移后 (fastjson3) - 使用 Java Time API =====
import com.alibaba.fastjson3.annotation.JSONField;
import java.time.LocalDateTime;

public class Event {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
```

---

## 模式：泛型集合

### Jackson → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.core.type.TypeReference;

List<User> users = mapper.readValue(json, new TypeReference<List<User>>() {});
Map<String, User> userMap = mapper.readValue(json, new TypeReference<Map<String, User>>() {});

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.TypeToken;

List<User> users = JSON.parse(json, TypeToken.listOf(User.class));
Map<String, User> userMap = JSON.parse(json, TypeToken.mapOf(User.class));
```

### Gson → fastjson3

```java
// ===== 原代码 (Gson) =====
import com.google.gson.reflect.TypeToken;

List<User> users = gson.fromJson(json, new TypeToken<List<User>>() {});

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.TypeToken;

List<User> users = JSON.parse(json, TypeToken.listOf(User.class));
```

---

## 模式：动态对象

### Jackson JsonNode → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper mapper = new ObjectMapper();
JsonNode root = mapper.readTree(json);
String name = root.get("name").asText();
int age = root.get("age").asInt();

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;

JSONObject root = JSON.parseObject(json);
String name = root.getString("name");
Integer age = root.getIntValue("age");
```

### Gson JsonElement → fastjson3

```java
// ===== 原代码 (Gson) =====
import com.google.gson.JsonObject;
import com.google.gson.Gson;

Gson gson = new Gson();
JsonObject root = gson.fromJson(json, JsonObject.class);
String name = root.get("name").getAsString();

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;

JSONObject root = JSON.parseObject(json);
String name = root.getString("name");
```

---

## 模式：未知属性处理

### Jackson → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.databind.DeserializationFeature;

ObjectMapper mapper = new ObjectMapper();
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ReadFeature;

ObjectMapper mapper = ObjectMapper.builder()
    .disableRead(ReadFeature.ErrorOnUnknownProperties)
    .build();
```

---

## 模式：数组处理

### Jackson → fastjson3

```java
// ===== 原代码 (Jackson) =====
User[] users = mapper.readValue(json, User[].class);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.TypeToken;

List<User> users = JSON.parse(json, TypeToken.listOf(User.class));
// 或
User[] users = JSON.parseObject(json, User[].class);
```

---

## 模式：枚举序列化

### Jackson → fastjson3

```java
// ===== 原代码 (Jackson) =====
import com.fasterxml.jackson.databind.SerializationFeature;

mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

// ===== 迁移后 (fastjson3) =====
import com.alibaba.fastjson3.WriteFeature;

ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.WriteEnumUsingToString)
    .build();
```

---

## 使用说明

### 给 LLM 的提示词模板

当使用 LLM 辅助迁移时，可以使用以下模板：

```
请将以下 {来源库} 代码迁移到 fastjson3：

{代码片段}

参考以下模式：
- 代码模式库：docs/patterns/README.md
- API 映射表：docs/llm/api-mapping.md
- 迁移指南：docs/migration/

要求：
1. 保持原有功能不变
2. 使用 fastjson3 最佳实践
3. 保留原注解（fastjson3 原生支持 {来源库} 注解）
```

### 给 LLM 的上下文

```
fastjson3 是 Java 21+ JSON 库，具有以下特性：
- API 设计类似 Jackson 3.x（不可变 ObjectMapper）
- 原生支持 Jackson 和 fastjson 1.x 注解
- 高性能，约 fastjson 1.x 的 2 倍性能
- 完整支持 Record、sealed class
```

---

## 相关文档

- [API 映射表](../llm/api-mapping.md)
- [迁移脚本模板](../llm/scripts.md)

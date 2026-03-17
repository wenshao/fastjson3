# 从 fastjson 1.x 迁移到 fastjson3

fastjson3 是 fastjson 1.x 的全面重写版本，提供了更好的性能和更现代的 API。

## 快速对照

| fastjson 1.x | fastjson3 | 变化 |
|--------------|-----------|------|
| `JSON.parseObject(str)` | `JSON.parseObject(str)` | **相同** |
| `JSON.toJSONString(obj)` | `JSON.toJSONString(obj)` | **相同** |
| `SerializerFeature` | `WriteFeature` | 重命名 |
| `Feature` | `ReadFeature` | 重命名 |
| `@JSONField(serialize=false)` | `@JSONField(serialize=false)` | **相同** |
| `TypeReference` | `TypeToken` | API 改变 |

---

## 核心变化

### 1. 包名变更

```java
// ===== fastjson 1.x =====
import com.alibaba.fastjson.*;
import com.alibaba.fastjson.annotation.*;
import com.alibaba.fastjson.serializer.*;

// ===== fastjson3 =====
import com.alibaba.fastjson3.*;
import com.alibaba.fastjson3.annotation.*;
import com.alibaba.fastjson3.util.*;
```

### 2. Java 版本要求

| 版本 | Java 最低版本 |
|------|--------------|
| fastjson 1.x | Java 6 |
| fastjson3 | Java 17 |

**重要：** fastjson3 需要 Java 17+

### 3. Maven 依赖

```xml
<!-- fastjson 1.x -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.83</version>
</dependency>

<!-- fastjson3 -->
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## Feature 枚举映射

### SerializerFeature → WriteFeature

| fastjson 1.x | fastjson3 | 说明 |
|--------------|-----------|------|
| `QuoteFieldNames` | (默认行为) | 字段名带引号现在是默认 |
| `WriteMapNullValue` | `WriteNulls` | 输出 null 值 |
| `WriteNullStringAsEmpty` | `WriteNullStringAsEmpty` | null 字符串转空 |
| `WriteNullBooleanAsFalse` | `WriteNullBooleanAsFalse` | null 布尔转 false |
| `WriteNullListAsEmpty` | `WriteNullListAsEmpty` | null 列表转空 |
| `PrettyFormat` | `PrettyFormat` | 格式化输出 |
| `WriteClassName` | `WriteClassName` | 写入类名 |
| `DisableCircularReferenceDetect` | `ReferenceDetection` | 循环引用检测 |
| `WriteEnumUsingToString` | `WriteEnumUsingToString` | 枚举用 toString |
| `BrowserCompatible` | (默认行为) | 浏览器兼容 |
| `WriteNonStringValueAsString` | (默认行为) | 非 String 值转字符串 |

### Feature → ReadFeature

| fastjson 1.x | fastjson3 | 说明 |
|--------------|-----------|------|
| `AutoCloseSource` | (默认行为) | 自动关闭源 |
| `AllowComment` | `AllowComment` | 允许注释 |
| `AllowUnQuotedFieldNames` | `AllowUnQuotedFieldNames` | 允许无引号字段 |
| `AllowSingleQuotes` | `AllowSingleQuotes` | 允许单引号 |
| `IgnoreAutoMatch` | - | 忽略自动匹配 |
| `SupportSmartMatch` | `SupportSmartMatch` | 智能匹配 |
| `SupportArrayToBean` | `SupportArrayToBean` | 数组转 Bean |

### 使用方式变化

```java
// ===== fastjson 1.x =====
String json = JSON.toJSONString(obj,
    SerializerFeature.WriteMapNullValue,
    SerializerFeature.PrettyFormat,
    SerializerFeature.WriteDateUseDateFormat);

User user = JSON.parseObject(json,
    Feature.AllowComment,
    Feature.AllowSingleQuotes);

// ===== fastjson3 =====
// 方式1：使用 writeFeatures/readFeatures
String json = JSON.toJSONString(obj,
    WriteFeature.WriteNulls,
    WriteFeature.PrettyFormat);

User user = JSON.parseObject(json,
    ReadFeature.AllowComment,
    ReadFeature.AllowSingleQuotes);

// 方式2：使用 ObjectMapper 配置日期格式
ObjectMapper mapper = ObjectMapper.builder()
    .dateFormat("yyyy-MM-dd HH:mm:ss")
    .build();
String json = mapper.writeValueAsString(obj);

User user = JSON.parseObject(json,
    JSONReader.Feature.AllowComment,
    JSONReader.Feature.AllowSingleQuotes);
```

---

## 泛型处理

```java
// ===== fastjson 1.x =====
TypeReference<List<User>> userType = new TypeReference<List<User>>() {};
List<User> users = JSON.parseObject(json, userType);

TypeReference<Map<String, List<User>>> mapType =
    new TypeReference<Map<String, List<User>>>() {};
Map<String, List<User>> map = JSON.parseObject(json, mapType);

// ===== fastjson3 =====
// 方式1：TypeToken 工厂方法（推荐）
TypeToken<List<User>> userListType = TypeToken.listOf(User.class);
List<User> users = JSON.parse(json, userListType);

TypeToken<Map<String, User>> mapType = TypeToken.mapOf(User.class);
Map<String, User> map = JSON.parse(json, mapType);

// 方式2：直接使用 parse
List<User> users = JSON.parse(json, TypeToken.listOf(User.class));

// 方式3：parseObject 直接指定类型
List<User> users = JSON.parseObject(json,
    new TypeReference<List<User>>() {});  // 兼容旧方式
```

---

## 注解变化

### @JSONField

大部分属性保持兼容，新增了一些属性：

```java
// ===== fastjson 1.x =====
@JSONField(name = "user_name", format = "yyyy-MM-dd")
private Date createTime;

// ===== fastjson3 =====
// 完全兼容，同时新增了更多选项
@JSONField(name = "user_name", format = "yyyy-MM-dd")
private LocalDateTime createTime;  // 支持 Java Time API
```

### 新增注解属性

| 属性 | 说明 | 示例 |
|------|------|------|
| `required` | 必填字段 | `@JSONField(required = true)` |
| `defaultValue` | 默认值 | `@JSONField(defaultValue = "0")` |
| `inclusion` | 包含策略 | `@JSONField(inclusion = Inclusion.NON_NULL)` |
| `alternateNames` | 别名 | `@JSONField(alternateNames = {"email", "mail"})` |
| `unwrapped` | 展开嵌套 | `@JSONField(unwrapped = true)` |

---

## API 变化

### 静态方法

```java
// ===== fastjson 1.x =====
// 解析
JSONObject obj = JSON.parseObject(jsonStr);
JSONArray arr = JSON.parseArray(jsonStr);
User user = JSON.parseObject(jsonStr, User.class);
List<User> users = JSON.parseArray(jsonStr, User.class);

// 序列化
String json = JSON.toJSONString(obj);
String json = JSON.toJSONString(obj, SerializerFeature.PrettyFormat);

// ===== fastjson3 =====
// 解析 (API 保持兼容)
JSONObject obj = JSON.parseObject(jsonStr);
JSONArray arr = JSON.parseArray(jsonStr);
User user = JSON.parseObject(jsonStr, User.class);
List<User> users = JSON.parseArray(jsonStr, User.class);

// 序列化 (API 保持兼容)
String json = JSON.toJSONString(obj);
String json = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
```

### JSONObject/JSONArray

```java
// ===== fastjson 1.x =====
JSONObject obj = new JSONObject();
obj.put("name", "Tom");
obj.put("age", 25);

String name = obj.getString("name");
Integer age = obj.getInteger("age");

JSONArray arr = obj.getJSONArray("items");
int size = arr.size();

// ===== fastjson3 =====
// API 完全兼容
JSONObject obj = new JSONObject();
obj.put("name", "Tom");
obj.put("age", 25);

String name = obj.getString("name");
Integer age = obj.getIntValue("age");  // 方法名略有变化

JSONArray arr = obj.getJSONArray("items");
int size = arr.size();
```

### 方法名变化

| fastjson 1.x | fastjson3 | 说明 |
|--------------|-----------|------|
| `getInteger()` | `getIntValue()` | 方法名统一 |
| `getLong()` | `getLongValue()` | 方法名统一 |
| `getShort()` | `getShortValue()` | 方法名统一 |
| `getByte()` | `getByteValue()` | 方法名统一 |
| `getFloat()` | `getFloatValue()` | 方法名统一 |
| `getDouble()` | `getDoubleValue()` | 方法名统一 |
| `getBoolean()` | `getBooleanValue()` | 方法名统一 |

---

## Filter 变化

### Filter 接口

```java
// ===== fastjson 1.x =====
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.serializer.NameFilter;
import com.alibaba.fastjson.serializer.PropertyFilter;

ValueFilter valueFilter = (obj, name, value) -> {
    if (value == null) return "";
    return value;
};

NameFilter nameFilter = (obj, name, value) -> {
    return name.toUpperCase();
};

PropertyFilter propertyFilter = (obj, name, value) -> {
    return !name.equals("password");
};

// ===== fastjson3 =====
// 接口名称相同，但包名变了
import com.alibaba.fastjson3.filter.ValueFilter;
import com.alibaba.fastjson3.filter.NameFilter;
import com.alibaba.fastjson3.filter.PropertyFilter;

// 接口完全兼容
ValueFilter valueFilter = (obj, name, value) -> {
    if (value == null) return "";
    return value;
};
```

---

## 配置变化

### 全局配置

```java
// ===== fastjson 1.x =====
JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
JSON.DEFAULT_PARSER_FEATURE |= Feature.AllowComment.getMask();

// ===== fastjson3 =====
// 推荐：使用 ObjectMapper
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.WriteNulls)
    .enableRead(ReadFeature.AllowComment)
    .dateFormat("yyyy-MM-dd HH:mm:ss")
    .build();

String json = mapper.writeValueAsString(obj);
```

### 日期格式

```java
// ===== fastjson 1.x =====
JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
SerializerFeature.config(SerializerFeature.WriteDateUseDateFormat, true);

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.builder()
    .dateFormat("yyyy-MM-dd HH:mm:ss")
    .build();

// 或使用注解
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;
```

---

## 自定义序列化

```java
// ===== fastjson 1.x =====
public class MoneySerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object,
                      Object fieldName, Type fieldType, int features)
            throws IOException {
        BigDecimal value = (BigDecimal) object;
        serializer.write(value.setScale(2, RoundingMode.HALF_UP) + "元");
    }
}

// 使用
SerializeConfig.get().put(BigDecimal.class, new MoneySerializer());

// ===== fastjson3 =====
public class MoneyWriter implements ObjectWriter<BigDecimal> {
    @Override
    public void write(JSONGenerator gen, Object object, long features) {
        BigDecimal value = (BigDecimal) object;
        gen.writeString(value.setScale(2, RoundingMode.HALF_UP) + "元");
    }
}

// 使用
@JSONField(serializeUsing = MoneyWriter.class)
private BigDecimal amount;
```

---

## 循环引用处理

```java
// ===== fastjson 1.x =====
class Node {
    String name;
    Node parent;
    List<Node> children;
}

// 默认启用循环引用检测
String json = JSON.toJSONString(node);

// 禁用循环引用检测
String json = JSON.toJSONString(node,
    SerializerFeature.DisableCircularReferenceDetect);

// ===== fastjson3 =====
// 默认启用循环引用检测
String json = JSON.toJSONString(node);

// 禁用循环引用检测（通过 ObjectMapper）
ObjectMapper mapper = ObjectMapper.builder()
    .disableWrite(WriteFeature.ReferenceDetection)
    .build();
String json = mapper.writeValueAsString(node);
```

---

## 常用场景迁移

### 场景 1：日期处理

```java
// ===== fastjson 1.x =====
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;

// ===== fastjson3 =====
// 兼容旧写法，同时支持 Java Time API
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime createTime;  // 推荐使用
```

### 场景 2：null 值处理

```java
// ===== fastjson 1.x =====
String json = JSON.toJSONString(obj,
    SerializerFeature.WriteMapNullValue,
    SerializerFeature.WriteNullStringAsEmpty,
    SerializerFeature.WriteNullListAsEmpty);

// ===== fastjson3 =====
String json = JSON.toJSONString(obj,
    WriteFeature.WriteNulls,
    WriteFeature.WriteNullStringAsEmpty,
    WriteFeature.WriteNullListAsEmpty);
```

### 场景 3：字段过滤

```java
// ===== fastjson 1.x =====
SimplePropertyFilter filter = SimplePropertyFilter.filterAllExcept("password");
String json = JSON.toJSONString(obj, filter);

// ===== fastjson3 =====
PropertyFilter filter = (obj, name, value) -> !name.equals("password");
String json = JSON.toJSONString(obj, filter);
```

### 场景 4：字段重命名

```java
// ===== fastjson 1.x =====
NameFilter filter = (obj, name, value) -> {
    if ("userName".equals(name)) return "username";
    return name;
};
String json = JSON.toJSONString(obj, filter);

// ===== fastjson3 =====
// 方式1：注解
@JSONField(name = "username")
private String userName;

// 方式2：Filter
NameFilter filter = (obj, name, value) -> {
    if ("userName".equals(name)) return "username";
    return name;
};
```

---

## 性能对比

| 操作 | fastjson 1.x | fastjson3 | 提升 |
|------|--------------|-----------|------|
| 序列化 | 基准 | ~1.5x | +50% |
| 反序列化 | 基准 | ~1.3x | +30% |
| 内存占用 | 基准 | ~0.7x | -30% |

---

## 迁移步骤

1. **更新依赖** - 修改 Maven/Gradle 依赖
2. **修改导入** - 批量替换包名 `com.alibaba.fastjson` → `com.alibaba.fastjson3`
3. **替换 Feature** - `SerializerFeature` → `WriteFeature`，`Feature` → `ReadFeature`
4. **更新泛型处理** - 使用 `TypeToken` 工厂方法
5. **测试验证** - 运行测试确保兼容性

### 批量替换命令

```bash
# 包名替换
find . -name "*.java" -type f -exec sed -i 's/com\.alibaba\.fastjson\./com.alibaba.fastjson3./g' {} +

# Feature 替换
find . -name "*.java" -type f -exec sed -i 's/SerializerFeature/WriteFeature/g' {} +
find . -name "*.java" -type f -exec sed -i 's/\.Feature\./ReadFeature./g' {} +
```

---

## 兼容性说明

### 向后兼容

fastjson3 保持了与 fastjson 1.x 的高度兼容：

- ✅ 核心静态方法 (`parseObject`, `toJSONString`)
- ✅ `@JSONField` 注解
- ✅ `JSONObject`/`JSONArray` API
- ✅ Filter 接口

### 不兼容变化

- ❌ Java 版本要求 (17+)
- ❌ 包名变更
- ❌ `TypeReference` 建议改用 `TypeToken`
- ❌ `SerializeConfig`/`ParserConfig` 使用方式变化

---

## 完整示例

```java
// ===== fastjson 1.x =====
package com.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;

import java.util.Date;

public class Fastjson1Example {
    public static class User {
        @JSONField(name = "user_id")
        private Long userId;

        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date createTime;

        @JSONField(serialize = false)
        private String password;

        // getters/setters...
    }

    public static void main(String[] args) {
        User user = new User();
        user.setUserId(1001L);
        user.setCreateTime(new Date());

        // 序列化
        String json = JSON.toJSONString(user,
            SerializerFeature.WriteMapNullValue,
            SerializerFeature.PrettyFormat);

        // 反序列化
        User parsed = JSON.parseObject(json, User.class);
    }
}

// ===== fastjson3 =====
package com.example;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.WriteFeature;

import java.time.LocalDateTime;

public class Fastjson3Example {
    public static class User {
        @JSONField(name = "user_id")
        private Long userId;

        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;  // 使用 Java Time API

        @JSONField(serialize = false)
        private String password;

        // getters/setters...
    }

    public static void main(String[] args) {
        User user = new User();
        user.setUserId(1001L);
        user.setCreateTime(LocalDateTime.now());

        // 序列化
        String json = JSON.toJSONString(user,
            WriteFeature.WriteNulls,
            WriteFeature.PrettyFormat);

        // 反序列化
        User parsed = JSON.parseObject(json, User.class);
    }
}
```

## 常见问题

### Q: 必须使用 Java 17 吗？

A: 是的，fastjson3 需要 Java 17+。如果不能升级，继续使用 fastjson 1.x 或 fastjson2。

### Q: 必须修改所有代码吗？

A: 不必须。核心 API (`parseObject`, `toJSONString`) 完全兼容，可以渐进式迁移。

### Q: 性能提升明显吗？

A: 是的，序列化提升约 50%，反序列化提升约 30%。

### Q: 如何处理 TypeReference？

A: 推荐使用 `TypeToken` 工厂方法（`listOf()`, `mapOf()`），也可以继续使用 `TypeReference`。

## 相关文档

- [从 fastjson2 迁移](from-fastjson2.md)
- [注解使用指南](../guides/annotations.md)
- [迁移检查清单](checklist.md)

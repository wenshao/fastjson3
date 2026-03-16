# Fastjson3 易用性改进计划

> **项目状态**: 新项目，可进行破坏性更改
> **创建日期**: 2026-03-17
> **目标**: 提升开发者体验，降低学习曲线，提高类型安全性

---

## 一、核心问题总结

### 1. API 不一致
- `parseObject` vs `parseArray` vs `parse` 命名混乱
- JSONObject 和 JSONArray 的 `get` 方法行为不一致
- 特性配置分散且难以理解

### 2. 配置复杂度过高
- 36+ 个 ReadFeature 和 20+ 个 WriteFeature
- 缺少语义化的配置预设
- 特性组合未验证冲突

### 3. 类型安全问题
- 过多 `@SuppressWarnings("unchecked")`
- TypeReference 语法繁琐
- 泛型集合解析不便

### 4. 错误处理不友好
- 错误消息缺少上下文（路径、位置）
- 单一 JSONException 类型
- null 处理不一致

---

## 二、改进方案 (按优先级)

### 🔴 高优先级 - API 核心重构

#### 1. 统一解析/序列化 API

**当前问题**:
```java
JSON.parseObject(json, User.class)      // 对象
JSON.parseArray(json, User.class)       // 列表
JSON.parse(json)                        // 任意类型
```

**改进方案**:
```java
// 统一的入口方法
<T> T JSON.parse(String json, Class<T> type)
<T> T JSON.parse(String json, Class<T> type, Config config)
<T> T JSON.parse(String json, TypeToken<T> typeToken)

// 便捷方法
<T> List<T> JSON.parseList(String json, Class<T> element)
<T> Map<String, T> JSON.parseMap(String json, Class<T> value)
JSONObject JSON.parseObject(String json)
JSONArray JSON.parseArray(String json)

// 统一的序列化
String JSON.write(Object obj)
String JSON.write(Object obj, Config config)
byte[] JSON.writeBytes(Object obj)
byte[] JSON.writeBytes(Object obj, Config config)
```

**优先级**: P0 (必须)
**影响范围**: `JSON.java` 公共 API
**工作量**: 2-3 天

---

#### 2. 语义化配置系统

**当前问题**:
```java
// 需要知道多个特性才能完成简单配置
.enableRead(ReadFeature.AllowComments,
           ReadFeature.AllowSingleQuotes,
           ReadFeature.AllowUnquotedFieldNames,
           ReadFeature.SupportSmartMatch)
```

**改进方案**:
```java
// 语义化配置
enum ReadConfig {
    DEFAULT,           // 标准 JSON
    LENIENT,           // 宽松: 注释、单引号、无引号字段
    STRICT,            // 严格: 未知属性报错
    API,               // API 验证: 严格 + 类型检查
    DEVELOPMENT        // 开发模式: 宽松 + 调试信息
}

enum WriteConfig {
    DEFAULT,           // 标准 JSON
    PRETTY,            // 美化输出
    COMPACT,           // 紧凑输出 (无空格)
    WITH_NULLS,        // 包含 null 值
    PRETTY_WITH_NULLS  // 美化 + null
}

// 使用
ObjectMapper mapper = ObjectMapper.builder()
    .readConfig(ReadConfig.LENIENT)
    .writeConfig(WriteConfig.PRETTY)
    .build();

// 高级配置仍可用
mapper = ObjectMapper.builder()
    .readConfig(ReadConfig.LENIENT)
    .andEnable(ReadFeature.SupportAutoType)  // 额外特性
    .build();
```

**优先级**: P0 (必须)
**影响范围**: `ObjectMapper.java`, `ReadFeature.java`, `WriteFeature.java`
**工作量**: 3-4 天

---

#### 3. 类型安全的集合解析

**当前问题**:
```java
// TypeReference 语法繁琐
TypeReference<List<User>> typeRef = new TypeReference<List<User>>() {};
List<User> users = JSON.parseObject(json, typeRef);

// 类型不安全
List<User> users = (List<User>) JSON.parseObject(json, List.class);
```

**改进方案**:
```java
// 直接解析泛型集合
List<User> users = JSON.parseList(json, User.class);
Map<String, User> users = JSON.parseMap(json, User.class);
Set<User> users = JSON.parseSet(json, User.class);

// 复杂泛型用 TypeToken (简化版 TypeReference)
TypeToken<List<User>> type = TypeToken.of(User.class).list();
List<User> users = JSON.parse(json, type);

// 嵌套泛型
TypeToken<Map<String, List<User>>> type =
    TypeToken.of(String.class).mapOf(TypeToken.of(User.class).list());
```

**优先级**: P0 (必须)
**影响范围**: `JSON.java`, 新增 `TypeToken.java`
**工作量**: 2-3 天

---

### 🟡 中优先级 - 体验提升

#### 4. 流式构建器

**当前问题**:
```java
JSONObject obj = new JSONObject();
obj.put("name", "John");
obj.put("age", 30);
obj.put("address", addr);

// 无法链式调用
```

**改进方案**:
```java
// 静态构建器
JSONObject obj = JSONObject.builder()
    .put("name", "John")
    .put("age", 30)
    .put("active", true)
    .put("tags", "a", "b", "c")
    .build();

JSONArray arr = JSONArray.builder()
    .add("item1")
    .add(JSONObject.builder().put("key", "value").build())
    .build();

// 可变对象保持现有 API
obj.put("newKey", "newValue");  // 仍然有效
```

**优先级**: P1 (高价值)
**影响范围**: `JSONObject.java`, `JSONArray.java`
**工作量**: 1-2 天

---

#### 5. 异常层次结构

**当前问题**:
```java
// 所有错误都是 JSONException，无法区分处理
try {
    JSON.parse(json);
} catch (JSONException e) {
    // 是解析错误？类型转换错误？还是配置错误？
}
```

**改进方案**:
```java
// 异常层次
JSONException (基类)
├── JSONParseException       // 解析错误
├── JSONWriteException       // 序列化错误
├── JSONTypeException        // 类型转换错误
├── JSONSchemaException      // Schema 验证错误
└── JSONConfigException      // 配置错误

// 使用
try {
    JSON.parse(json);
} catch (JSONParseException e) {
    log.error("语法错误，位置: {}", e.getPosition());
} catch (JSONTypeException e) {
    log.error("类型不匹配: {} -> {}", e.getSourceType(), e.getTargetType());
}
```

**优先级**: P1 (高价值)
**影响范围**: 新增异常类，修改所有 throw 点
**工作量**: 2-3 天

---

#### 6. 改进的错误消息

**当前问题**:
```java
// 缺少上下文
throw new JSONException("Cannot cast to Integer");
```

**改进方案**:
```java
// 包含路径、位置、类型信息
throw new JSONTypeException(
    "Cannot convert JSON value to target type",
    "$.users[0].age",           // JSONPath
    45,                         // 位置
    "String",                   // 源类型
    "Integer",                  // 目标类型
    "\"thirty\""                // 实际值
);

// 显示为:
// JSONTypeException: Cannot convert value at $.users[0].age (position 45)
//   from String "thirty" to Integer
```

**优先级**: P1 (高价值)
**影响范围**: 所有异常抛出点
**工作量**: 3-4 天

---

### 🟢 低优先级 - 锦上添花

#### 7. JSONPath 便捷方法

**改进方案**:
```java
// 静态导入
import static com.alibaba.fastjson3.JSONPath.*;

// 直接提取值
String name = JSONPath.eval(json, "$.user.name");
Integer age = JSONPath.eval(json, "$.user.age", Integer.class);

// 链式操作
JSONPath path = JSONPath.compile("$.store.book[*].author");
List<String> authors = path.evalList(json);

// 修改并序列化
String result = JSONPath.modify(json, "$.user.age", 30);
```

**优先级**: P2
**影响范围**: `JSONPath.java`
**工作量**: 2 天

---

#### 8. Optional 风格的 API

**改进方案**:
```java
// 返回 Optional 而非 null
Optional<String> name = obj.opt("name");
OptionalInt age = obj.optInt("age");
Optional<Integer> maybeAge = obj.optInteger("age");

// 链式操作
String city = obj.opt("address")
    .map(addr -> addr.getJSONObject())
    .map(addr -> addr.getString("city"))
    .orElse("Unknown");
```

**优先级**: P2
**影响范围**: `JSONObject.java`, `JSONArray.java`
**工作量**: 1-2 天

---

#### 9. 记录支持

**改进方案**:
```java
// 创建类似 Java Record 的 JSON 映射
@JSONRecord
public record User(
    String name,
    int age,
    @JSONField(name = "user_id") long id
) {}

// 使用
User user = JSON.parse(json, User.class);
String json = JSON.write(user);
```

**优先级**: P2
**影响范围**: 新注解和处理器
**工作量**: 3-4 天

---

## 三、实施路线图

### Phase 1: 基础重构 (Week 1-2)
- [ ] 统一 API (`parse`/`write`)
- [ ] 语义化配置 (`ReadConfig`/`WriteConfig`)
- [ ] 类型安全集合 (`parseList`/`parseMap`)

### Phase 2: 体验提升 (Week 3-4)
- [ ] 流式构建器 (`JSONObject.builder()`)
- [ ] 异常层次结构
- [ ] 改进错误消息

### Phase 3: 高级功能 (Week 5-6)
- [ ] JSONPath 便捷方法
- [ ] Optional 风格 API
- [ ] @JSONRecord 支持

---

## 四、迁移指南示例

```java
// === 旧 API ===
String json = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
User user = JSON.parseObject(json, User.class);
List<User> users = JSON.parseArray(json, User.class);

// === 新 API ===
String json = JSON.write(obj, WriteConfig.PRETTY);
User user = JSON.parse(json, User.class);
List<User> users = JSON.parseList(json, User.class);
```

```java
// === 旧 API ===
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.AllowComments, ReadFeature.AllowSingleQuotes)
    .enableWrite(WriteFeature.PrettyFormat, WriteFeature.WriteNulls)
    .build();

// === 新 API ===
ObjectMapper mapper = ObjectMapper.builder()
    .readConfig(ReadConfig.LENIENT)
    .writeConfig(WriteConfig.PRETTY_WITH_NULLS)
    .build();
```

---

## 五、向后兼容策略

虽然这是新项目，但仍建议：

1. **保留旧 API 标记为 @Deprecated**
2. **提供迁移工具/脚本**
3. **文档中明确标注新旧 API 对比**
4. **逐步迁移，先新后旧**

---

## 六、成功指标

- [ ] 解析常见操作代码行数减少 50%
- [ ] 类型安全警告减少 90%
- [ ] 新用户 5 分钟内能完成基本操作
- [ ] StackOverflow 相关问题减少
- [ ] API 方法总数减少 (通过合并相似功能)

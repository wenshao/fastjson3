# Fastjson3 易用性改进计划

> **文档性质**: 改进提案与实施追踪。部分提案已实现，部分仍在讨论中。当前 API 请参考 [README.md](README.md) 和 [docs/](docs/)。
>
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
- 17 个 ReadFeature 和 20 个 WriteFeature
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

#### 1. 统一解析/序列化 API ✅ 已实现

已在 `JSON.java` 中实现统一的 `parse` / `write` API：

```java
// 统一解析
<T> T JSON.parse(String json, Class<T> type)
<T> T JSON.parse(String json, Class<T> type, ParseConfig config)
<T> T JSON.parse(String json, TypeToken<T> typeToken)

// 统一序列化
String JSON.write(Object obj)
String JSON.write(Object obj, WriteConfig config)
byte[] JSON.writeBytes(Object obj)
byte[] JSON.writeBytes(Object obj, WriteConfig config)
```

旧 API（`parseObject`/`parseArray`/`toJSONString`/`toJSONBytes`）保留兼容。

---

#### 2. 语义化配置系统 ✅ 已实现

已实现 `ParseConfig` 和 `WriteConfig` 枚举：

```java
// ParseConfig 预设
ParseConfig.DEFAULT   // 标准 JSON
ParseConfig.LENIENT   // 宽松: 注释、单引号、无引号字段、智能匹配
ParseConfig.STRICT    // 严格: 未知属性报错、null 基本类型报错
ParseConfig.API       // API: 严格 + BigDecimal

// WriteConfig 预设
WriteConfig.DEFAULT           // 标准输出
WriteConfig.PRETTY            // 美化输出
WriteConfig.WITH_NULLS        // 包含 null
WriteConfig.PRETTY_WITH_NULLS // 美化 + null

// 使用
User user = JSON.parse(json, User.class, ParseConfig.LENIENT);
String json = JSON.write(obj, WriteConfig.PRETTY);
```

---

#### 3. 类型安全的集合解析 ✅ 已实现

已在 `JSON.java` 中实现便捷集合解析方法：

```java
List<User> users = JSON.parseList(json, User.class);
Set<User> users = JSON.parseSet(json, User.class);
Map<String, User> map = JSON.parseMap(json, User.class);
User[] arr = JSON.parseTypedArray(json, User.class);

// TypeToken 也已实现
TypeToken<List<User>> type = TypeToken.listOf(User.class);
List<User> users = JSON.parse(json, type);
```

---

### 🟡 中优先级 - 体验提升

#### 4. 流式构建器

**状态**: 未实现

```java
// 提案：
JSONObject obj = JSONObject.builder()
    .put("name", "John")
    .put("age", 30)
    .build();
```

**已有替代方案**: `JSON.object("key", value)` 和 `fluentPut()`

---

#### 5. 异常层次结构

**状态**: 未实现

```java
// 提案：
JSONException (基类)
├── JSONParseException       // 解析错误
├── JSONWriteException       // 序列化错误
├── JSONTypeException        // 类型转换错误
├── JSONSchemaException      // Schema 验证错误
└── JSONConfigException      // 配置错误
```

---

#### 6. 改进的错误消息

**状态**: 未实现

```java
// 提案：包含路径、位置、类型信息
throw new JSONTypeException(
    "Cannot convert JSON value to target type",
    "$.users[0].age",           // JSONPath
    45,                         // 位置
    "String",                   // 源类型
    "Integer"                   // 目标类型
);
```

---

### 🟢 低优先级 - 锦上添花

#### 7. JSONPath 便捷方法

**状态**: 部分实现（`JSON.eval()` 已提供）

#### 8. Optional 风格的 API

**状态**: 未实现

#### 9. 记录支持

**状态**: 已原生支持 Java Record 的序列化/反序列化，无需额外注解

---

## 三、实施路线图

### Phase 1: 基础重构 ✅ 已完成
- [x] 统一 API (`parse`/`write`)
- [x] 语义化配置 (`ParseConfig`/`WriteConfig`)
- [x] 类型安全集合 (`parseList`/`parseMap`/`parseSet`/`parseTypedArray`)

### Phase 2: 体验提升 (待定)
- [ ] 流式构建器 (`JSONObject.builder()`)
- [ ] 异常层次结构
- [ ] 改进错误消息

### Phase 3: 高级功能 (待定)
- [ ] Optional 风格 API

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

---

## 五、向后兼容策略

虽然这是新项目，但仍建议：

1. **保留旧 API** - `parseObject`/`parseArray`/`toJSONString` 不会删除
2. **新旧 API 共存** - 用户可按习惯选择
3. **文档中推荐新 API** - 引导新用户使用统一风格

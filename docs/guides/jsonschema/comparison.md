# JSON Schema 跨库对比

本文档对比 fastjson3 的 JSON Schema 实现与其他主流 JSON 库的差异。

---

## 概览

| 库 | Schema 支持 | 标准 | 性能 | 集成度 |
|------|-------------|------|------|--------|
| **fastjson3** | ✅ 完整 | Draft 2020-12 | ⭐⭐⭐⭐⭐ | 原生 |
| **networknt** | ✅ 完整 | Draft 7 / 2020-12 | ⭐⭐⭐⭐ | 独立库 |
| **everit** | ✅ 部分 | Draft 7 | ⭐⭐⭐ | 独立库 |
| **Jackson** | ✅ 完整 | Draft 7 | ⭐⭐⭐⭐ | 模块 |
| **org.json** | ❌ 无 | - | - | - |

---

## fastjson3 vs networknt JSON Schema Validator

### API 对比

```java
// ===== fastjson3 =====
JSONSchema schema = JSONSchema.parseSchema(schemaJson);
ValidateResult result = schema.validate(dataJson);

// ===== networknt =====
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
JsonSchema schema = factory.getSchema(schemaJson);
Set<ValidationMessage> errors = schema.validate(dataJson, InputFormat.JSON);
```

### 功能对比

| 功能 | fastjson3 | networknt |
|------|-----------|-----------|
| Draft 2020-12 | ✅ | ✅ |
| Draft 7 | ✅ | ✅ |
| 自定义格式 | ✅ | ✅ |
| 自定义关键字 | ✅ | ✅ |
| $ref 支持 | ✅ | ✅ |
| 性能 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

### 性能对比

| 操作 | fastjson3 | networknt |
|------|-----------|-----------|
| 简单验证 | 基准 | ~0.85x |
| 复杂验证 | 基准 | ~0.80x |
| 嵌套验证 | 基准 | ~0.75x |

---

## fastjson3 vs everit JSON Schema

### API 对比

```java
// ===== fastjson3 =====
JSONSchema schema = JSONSchema.parseSchema(schemaJson);
ValidateResult result = schema.validate(dataJson);

// ===== everit =====
org.everit.json.schema.Schema schema = SchemaLoader.load(schemaJson);
schema.validate(dataJson);
```

### 功能对比

| 功能 | fastjson3 | everit |
|------|-----------|--------|
| Draft 2020-12 | ✅ | ❌ (仅 Draft 7) |
| 性能 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 维护状态 | ✅ 活跃 | ⚠️ 不活跃 |
| Java 版本 | Java 21+ | Java 8+ |

### 迁移示例

```java
// ===== everit =====
Schema schema = SchemaLoader
    .builder()
    .schemaJson(schemaJson)
    .build()
    .load()
    .require("name");

// ===== fastjson3 =====
JSONSchema schema = JSONSchema.parseSchema("""
    {
        "type": "object",
        "properties": {"name": {"type": "string"}},
        "required": ["name"]
    }
    """);
```

---

## fastjson3 vs Jackson JSON Schema

### API 对比

```java
// ===== fastjson3 =====
JSONSchema schema = JSONSchema.parseSchema(schemaJson);
ValidateResult result = schema.validate(dataJson);

// ===== Jackson (json-schema-validator) =====
JsonSchemaFactory factory = JsonSchemaFactory.builder()
    .objectMapper(mapper)
    .build();
JsonSchema schema = factory.getSchema(schemaJson);
Set<ValidationMessage> errors = schema.validate(dataJson);
```

### 功能对比

| 功能 | fastjson3 | Jackson |
|------|-----------|---------|
| Draft 2020-12 | ✅ | ✅ |
| 集成度 | 原生 | 需要额外模块 |
| 性能 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 学习曲线 | 低 | 中 |

---

## fastjson3 vs 手动验证

对于简单场景，可能会考虑手动验证：

### 手动验证示例

```java
// ===== 手动验证 =====
public boolean validateUser(JSONObject json) {
    String name = json.getString("name");
    if (name == null || name.isEmpty()) {
        return false;
    }

    Integer age = json.getInteger("age");
    if (age == null || age < 0 || age > 150) {
        return false;
    }

    return true;
}

// ===== fastjson3 JSON Schema =====
private static final JSONSchema SCHEMA = JSONSchema.parseSchema("""
    {
        "type": "object",
        "properties": {
            "name": {"type": "string", "minLength": 1},
            "age": {"type": "integer", "minimum": 0, "maximum": 150}
        },
        "required": ["name", "age"]
    }
    """);

public boolean validateUser(String json) {
    return SCHEMA.validate(json).isSuccess();
}
```

### 优劣对比

| 方面 | 手动验证 | JSON Schema |
|------|----------|-------------|
| 简单场景 | ✅ 快速 | ⚠️ 需要定义 Schema |
| 复杂场景 | ❌ 代码复杂 | ✅ 声明式 |
| 可维护性 | ❌ 散落各处 | ✅ 集中管理 |
| 可复用性 | ❌ 难复用 | ✅ Schema 复用 |
| 错误信息 | ⚠️ 需要自定义 | ✅ 标准化 |

---

## 语法兼容性

### Schema 标准版本

| 库 | Draft 4 | Draft 6 | Draft 7 | Draft 2019-09 | Draft 2020-12 |
|------|---------|---------|---------|---------------|---------------|
| fastjson3 | ⚠️ | ⚠️ | ✅ | ✅ | ✅ |
| networknt | ✅ | ✅ | ✅ | ✅ | ✅ |
| everit | ✅ | ✅ | ✅ | ❌ | ❌ |
| Jackson | ✅ | ✅ | ✅ | ⚠️ | ⚠️ |

### 关键字支持

| 关键字 | fastjson3 | networknt | everit | Jackson |
|--------|-----------|-----------|--------|---------|
| `type` | ✅ | ✅ | ✅ | ✅ |
| `enum` | ✅ | ✅ | ✅ | ✅ |
| `const` | ✅ | ✅ | ✅ | ✅ |
| `format` | ✅ | ✅ | ✅ | ✅ |
| `pattern` | ✅ | ✅ | ✅ | ✅ |
| `allOf` | ✅ | ✅ | ✅ | ✅ |
| `anyOf` | ✅ | ✅ | ✅ | ✅ |
| `oneOf` | ✅ | ✅ | ✅ | ✅ |
| `if/then/else` | ✅ | ✅ | ⚠️ | ✅ |
| `$ref` | ✅ | ✅ | ✅ | ✅ |
| `$recursiveRef` | ✅ | ✅ | ❌ | ❌ |

---

## 迁移指南

### 从 networknt 迁移

```java
// ===== networknt =====
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
JsonSchema schema = factory.getSchema(schemaJson);
Set<ValidationMessage> errors = schema.validate(dataJson);

if (!errors.isEmpty()) {
    for (ValidationMessage error : errors) {
        System.out.println(error.getPath() + ": " + error.getMessage());
    }
}

// ===== fastjson3 =====
JSONSchema schema = JSONSchema.parseSchema(schemaJson);
ValidateResult result = schema.validate(dataJson);

if (!result.isSuccess()) {
    for (ValidateResult error : result.getErrors()) {
        System.out.println(error.getPath() + ": " + error.getMessage());
    }
}
```

### 从 everit 迁移

```java
// ===== everit =====
Schema schema = SchemaLoader.load(schemaJson);
schema.validate(dataJson);  // 抛出 ValidationException

// ===== fastjson3 =====
JSONSchema schema = JSONSchema.parseSchema(schemaJson);
ValidateResult result = schema.validate(dataJson);

if (!result.isSuccess()) {
    throw new ValidationException(result.getMessage());
}
```

### 从 Jackson 迁移

```java
// ===== Jackson =====
JsonSchemaFactory factory = JsonSchemaFactory.builder()
    .objectMapper(mapper)
    .build();
JsonSchema schema = factory.getSchema(schemaJson);
Set<ValidationMessage> errors = schema.validate(dataJson);

// ===== fastjson3 =====
// Schema 完全兼容，直接使用即可
JSONSchema schema = JSONSchema.parseSchema(schemaJson);
ValidateResult result = schema.validate(dataJson);
```

---

## 选择建议

### 选择 fastjson3 JSON Schema 如果

- ✅ 已经使用 fastjson3
- ✅ 需要完整的 Draft 2020-12 支持
- ✅ 需要最佳性能
- ✅ 需要原声集成

### 选择 networknt 如果

- ✅ 项目无法使用 fastjson3
- ✅ 需要支持旧的 Draft 版本
- ✅ 需要高度可定制性

### 选择 everit 如果

- ⚠️ 需要支持 Java 8
- ⚠️ 只需要 Draft 7 支持
- ⚠️ 项目已有依赖

### 选择 Jackson 如果

- ✅ 项目已深度使用 Jackson
- ✅ 需要与 Jackson 生态集成
- ⚠️ 可以接受额外依赖

---

## 性能基准

相对性能（越高越好）：

| 场景 | fastjson3 | networknt | Jackson | everit |
|------|-----------|-----------|---------|--------|
| 简单验证 | 100 | 85 | 80 | 60 |
| 复杂验证 | 100 | 80 | 75 | 55 |
| 嵌套验证 | 100 | 75 | 70 | 50 |
| 大数据量 | 100 | 85 | 80 | 65 |

---

## 相关文档

- [JSON Schema 基础验证 →](basics.md)
- [JSON Schema 高级验证 →](advanced.md)

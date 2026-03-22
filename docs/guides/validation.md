# JSON Schema 验证指南

fastjson3 支持 [Draft 2020-12](https://json-schema.org/draft/2020-12/release-notes.html) JSON Schema 规范。

## 📚 文档导航

### 基础入门
- **[基础验证 →](jsonschema/basics.md)**
  - 类型约束
  - 数值约束
  - 字符串约束
  - 数组约束
  - 对象约束
  - 枚举和常量

### 高进阶
- **[高级验证 →](jsonschema/advanced.md)**
  - 条件验证
  - 组合验证
  - 复杂嵌套
  - 性能优化
  - 错误处理

### 跨库对比
- **[跨库对比 →](jsonschema/comparison.md)**
  - vs networknt
  - vs everit
  - vs Jackson
  - 迁移指南

---

## 快速开始

```java
// 定义 Schema
String schemaJson = """
    {
        "type": "object",
        "properties": {
            "name": {"type": "string"},
            "age": {"type": "integer", "minimum": 0},
            "email": {"type": "string", "format": "email"}
        },
        "required": ["name", "age"]
    }
    """;

// 验证
JSONSchema schema = JSONSchema.parseSchema(schemaJson);
ValidateResult result = schema.validate(dataJson);

if (result.isSuccess()) {
    System.out.println("验证通过");
} else {
    System.out.println("验证失败: " + result.getMessage());
}
```

---

## 常用约束速查

| 类型 | 约束示例 |
|------|----------|
| 字符串 | `{"type": "string", "minLength": 3, "maxLength": 20}` |
| 整数 | `{"type": "integer", "minimum": 0, "maximum": 150}` |
| 小数 | `{"type": "number", "multipleOf": 0.01}` |
| 邮箱 | `{"type": "string", "format": "email"}` |
| 数组 | `{"type": "array", "minItems": 1, "maxItems": 10}` |
| 枚举 | `{"enum": ["active", "inactive"]}` |

---

## 核心特性

- ✅ **Draft 2020-12** - 完整支持最新标准
- ✅ **高性能** - 优化的验证引擎
- ✅ **类型安全** - 完整的类型验证
- ✅ **可复用** - Schema 编译后缓存
- ✅ **详细错误** - 精确的错误位置和原因

---

## 与其他库对比

| 库 | 标准 | 性能 | 维护状态 |
|------|------|------|----------|
| **fastjson3** | Draft 2020-12 | ⭐⭐⭐⭐⭐ | ✅ 活跃 |
| networknt | Draft 2020-12 | ⭐⭐⭐⭐ | ✅ 活跃 |
| everit | Draft 7 | ⭐⭐⭐ | ⚠️ 不活跃 |
| Jackson | Draft 7 | ⭐⭐⭐⭐ | ✅ 活跃 |

详见 [跨库对比 →](jsonschema/comparison.md)

---

## 相关文档

- 📖 [JSONPath 查询指南 →](jsonpath.md)
- 📖 [POJO 序列化 →](pojo.md)
- 📋 [JSON Schema API 参考 →](../api/schema.md)

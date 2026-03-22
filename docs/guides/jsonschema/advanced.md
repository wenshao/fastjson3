# JSON Schema 高级验证

本文档介绍 JSON Schema 的高级验证功能和最佳实践。

---

## 条件验证

### if-then-else

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "country": {"type": "string"},
            "postalCode": {"type": "string"}
        },
        "if": {
            "properties": {
                "country": {"const": "US"}
            }
        },
        "then": {
            "properties": {
                "postalCode": {"pattern": "^\\\\d{5}(-\\\\d{4})?$"}
            }
        },
        "else": {
            "properties": {
                "postalCode": {"pattern": "^[A-Z]{1,2}\\\\d[A-Z\\\\d]? \\\\d[A-Z]{2}$"}
            }
        }
    }
    """;
```

### 条件逻辑

| 关键字 | 说明 |
|--------|------|
| `if` | 条件判断 |
| `then` | if 为 true 时验证 |
| `else` | if 为 false 时验证 |

---

## 组合验证

### allOf - 全部满足

```java
// 密码必须满足所有条件
String schema = """
    {
        "type": "object",
        "properties": {
            "password": {
                "type": "string",
                "allOf": [
                    {"minLength": 8},
                    {"pattern": "[A-Z]"},   // 至少一个大写
                    {"pattern": "[a-z]"},   // 至少一个小写
                    {"pattern": "[0-9]"}    // 至少一个数字
                ]
            }
        }
    }
    """;
```

### anyOf - 至少满足一个

```java
// 坐标可以是地理坐标或笛卡尔坐标
String schema = """
    {
        "type": "object",
        "properties": {
            "location": {
                "anyOf": [
                    {
                        "type": "object",
                        "properties": {
                            "lat": {"type": "number"},
                            "lng": {"type": "number"}
                        },
                        "required": ["lat", "lng"]
                    },
                    {
                        "type": "object",
                        "properties": {
                            "x": {"type": "number"},
                            "y": {"type": "number"}
                        },
                        "required": ["x", "y"]
                    }
                ]
            }
        }
    }
    """;
```

### oneOf - 仅满足一个

```java
// 只能是其中一种类型
String schema = """
    {
        "type": "object",
        "properties": {
            "value": {
                "oneOf": [
                    {"type": "string"},
                    {"type": "integer"},
                    {"type": "boolean"}
                ]
            }
        }
    }
    """;
```

### not - 不满足

```java
// 不能是指定的类型
String schema = """
    {
        "type": "object",
        "properties": {
            "id": {
                "not": {"type": "string"}
            }
        }
    }
    """;
```

---

## 复杂嵌套

### 递归引用（$ref）

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "name": {"type": "string"},
            "children": {
                "type": "array",
                "items": {"$ref": "#/definitions/node"}
            }
        },
        "definitions": {
            "node": {
                "type": "object",
                "properties": {
                    "name": {"type": "string"},
                    "children": {
                        "type": "array",
                        "items": {"$ref": "#/definitions/node"}
                    }
                }
            }
        }
    }
    """;
```

---

## 数组高级约束

### 元组验证

```java
// 固定长度的数组，每个位置有不同类型
String schema = """
    {
        "type": "array",
        "items": [
            {"type": "string"},   // 第一个元素是字符串
            {"type": "integer"},  // 第二个元素是整数
            {"type": "boolean"}   // 第三个元素是布尔
        ],
        "additionalItems": false  // 不允许其他元素
    }
    """;
```

### minContains / maxContains

```java
// 数组中至少包含 2 个符合条件的元素
String schema = """
    {
        "type": "array",
        "items": {"type": "number"},
        "contains": {
            "type": "number",
            "minimum": 10
        },
        "minContains": 2
    }
    """;
```

---

## 依赖验证

### properties 依赖

```java
// 如果有 creditCard，必须有 billingAddress
String schema = """
    {
        "type": "object",
        "properties": {
            "creditCard": {"type": "string"},
            "billingAddress": {"type": "string"}
        },
        "dependentRequired": {
            "creditCard": ["billingAddress"]
        }
    }
    """;
```

### schema 依赖

```java
// 如果有 creditCard，必须符合特定格式
String schema = """
    {
        "type": "object",
        "properties": {
            "creditCard": {"type": "string"},
            "cvv": {"type": "string"}
        },
        "dependentSchemas": {
            "creditCard": {
                "properties": {
                    "cvv": {
                        "type": "string",
                        "pattern": "^\\\\d{3,4}$"
                    }
                },
                "required": ["cvv"]
            }
        }
    }
    """;
```

---

## 自定义验证器

```java
// 使用 fastjson3 提供的验证器
DomainValidator domainValidator = new DomainValidator();
domainValidator.isValid("example.com");  // true

EmailValidator emailValidator = new EmailValidator();
emailValidator.isValid("user@example.com");  // true

IPAddressValidator ipValidator = new IPAddressValidator();
ipValidator.isValid("192.168.1.1");  // true

UUIDValidator uuidValidator = new UUIDValidator();
uuidValidator.isValid("550e8400-e29b-41d4-a716-446655440000");  // true
```

---

## 性能优化

### 缓存 Schema

```java
// ✅ 好：编译一次，重复使用
private static final JSONSchema USER_SCHEMA = JSONSchema.parseSchema(schemaJson);

public void validateUser(String userJson) {
    ValidateResult result = USER_SCHEMA.validate(userJson);
    // ...
}

// ❌ 不好：每次都编译
public void validateUser(String userJson) {
    JSONSchema schema = JSONSchema.parseSchema(schemaJson);
    ValidateResult result = schema.validate(userJson);
    // ...
}
```

### 简化 Schema

```java
// ❌ 复杂的正则
{
    "type": "string",
    "pattern": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[A-Z]{2,}$"
}

// ✅ 使用预定义格式
{
    "type": "string",
    "format": "email"
}
```

### 避免深层嵌套

```java
// ❌ 深层嵌套
{
    "type": "object",
    "properties": {
        "a": {
            "type": "object",
            "properties": {
                "b": {
                    "type": "object",
                    "properties": {
                        "c": {"type": "string"}
                    }
                }
            }
        }
    }
}

// ✅ 使用 $ref
{
    "type": "object",
    "properties": {
        "a": {"$ref": "#/definitions/a"}
    },
    "definitions": {
        "a": {...},
        "b": {...},
        "c": {...}
    }
}
```

---

## 实际应用

### API 请求验证

```java
public class RequestValidator {
    private static final JSONSchema CREATE_USER_SCHEMA = JSONSchema.parseSchema("""
        {
            "type": "object",
            "properties": {
                "name": {"type": "string", "minLength": 1, "maxLength": 50},
                "email": {"type": "string", "format": "email"},
                "age": {"type": "integer", "minimum": 13, "maximum": 120},
                "address": {
                    "type": "object",
                    "properties": {
                        "city": {"type": "string"},
                        "street": {"type": "string"},
                        "zip": {"type": "string", "pattern": "^\\\\d{6}$"}
                    },
                    "required": ["city"]
                }
            },
            "required": ["name", "email"]
        }
        """);

    public static void validate(String json) {
        ValidateResult result = CREATE_USER_SCHEMA.validate(json);
        if (!result.isSuccess()) {
            List<ValidateResult> errors = result.getErrors();
            // 构建详细错误信息
            String message = errors.stream()
                .map(e -> e.getPath() + ": " + e.getMessage())
                .collect(Collectors.joining(", "));
            throw new ValidationException(message);
        }
    }
}
```

### 动态 Schema 验证

```java
public class DynamicValidator {
    // 根据类型选择不同的 Schema
    public void validate(String type, String data) {
        JSONSchema schema = getSchemaForType(type);
        ValidateResult result = schema.validate(data);
        if (!result.isSuccess()) {
            throw new ValidationException(result.getMessage());
        }
    }

    private JSONSchema getSchemaForType(String type) {
        return switch (type) {
            case "user" -> JSONSchema.parseSchema(USER_SCHEMA_JSON);
            case "product" -> JSONSchema.parseSchema(PRODUCT_SCHEMA_JSON);
            case "order" -> JSONSchema.parseSchema(ORDER_SCHEMA_JSON);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}
```

### 多 Schema 验证

```java
// 使用 allOf 验证多个条件
public class MultiSchemaValidator {
    private static final JSONSchema BASE_SCHEMA = JSONSchema.parseSchema("""
        {
            "type": "object",
            "properties": {
                "id": {"type": "string"},
                "createdAt": {"type": "string", "format": "date-time"}
            },
            "required": ["id", "createdAt"]
        }
        """);

    private static final JSONSchema EXTENDED_SCHEMA = JSONSchema.parseSchema("""
        {
            "type": "object",
            "properties": {
                "name": {"type": "string"},
                "email": {"type": "string", "format": "email"}
            },
            "required": ["name", "email"]
        }
        """);

    public void validateWithExtension(String json) {
        ValidateResult baseResult = BASE_SCHEMA.validate(json);
        ValidateResult extResult = EXTENDED_SCHEMA.validate(json);

        if (!baseResult.isSuccess() || !extResult.isSuccess()) {
            throw new ValidationException("Validation failed");
        }
    }
}
```

---

## 错误处理

### 详细错误信息

```java
ValidateResult result = schema.validate(dataJson);

if (!result.isSuccess()) {
    // 获取错误路径
    String path = result.getPath();

    // 获取错误消息
    String message = result.getMessage();

    // 获取错误值
    Object value = result.getValue();

    // 获取所有错误
    List<ValidateResult> errors = result.getErrors();
    for (ValidateResult error : errors) {
        System.out.println("路径: " + error.getPath());
        System.out.println("关键字: " + error.getKeyword());
        System.out.println("错误: " + error.getMessage());
    }
}
```

### 自定义错误消息

```java
public class UserFriendlyValidator {
    public static String getFriendlyMessage(ValidateResult result) {
        if (result.isSuccess()) {
            return "验证通过";
        }

        return result.getErrors().stream()
            .map(error -> switch (error.getKeyword()) {
                case "required" -> "缺少必需字段: " + error.getPath();
                case "minLength" -> "字段长度不足: " + error.getPath();
                case "pattern" -> "格式不正确: " + error.getPath();
                case "format" -> "格式验证失败: " + error.getPath();
                case "minimum" -> "值太小: " + error.getPath();
                case "maximum" -> "值太大: " + error.getPath();
                default -> "验证失败: " + error.getPath();
            })
            .collect(Collectors.joining("; "));
    }
}
```

---

## 最佳实践

1. **使用 format 而非 pattern** - 预定义格式性能更好

```java
// ✅ 好
{"type": "string", "format": "email"}

// ⚠️ 仅在必要时使用 pattern
{"type": "string", "pattern": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[A-Z]{2,}$"}
```

2. **合理设置 required** - 只对真正必需的字段设置

3. **使用 $ref 复用** - 避免重复定义

4. **提供清晰的错误** - 根据验证结果返回用户友好的错误

5. **Schema 版本控制** - 使用 $schema 声明版本

```java
String schema = """
    {
        "$schema": "https://json-schema.org/draft/2020-12/schema",
        "type": "object",
        ...
    }
    """;
```

---

## 常见问题

### Q: 如何验证可选字段？

```java
// 不放在 required 中，定义时设为可选
{
    "type": "object",
    "properties": {
        "name": {"type": "string"},      // 必需
        "nickname": {"type": "string"}    // 可选
    },
    "required": ["name"]
}
```

### Q: 如何验证联合类型？

```java
{
    "type": "object",
    "properties": {
        "value": {
            "oneOf": [
                {"type": "string"},
                {"type": "integer"},
                {"type": "null"}
            ]
        }
    }
}
```

### Q: 如何验证动态键名的对象？

```java
// 使用 patternProperties
{
    "type": "object",
    "patternProperties": {
        "^msg_": {
            "type": "string"
        }
    },
    "additionalProperties": false
}
```

## 相关文档

- [JSON Schema 基础验证 →](basics.md)
- [跨库对比 →](comparison.md)

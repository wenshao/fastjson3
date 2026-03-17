# JSON Schema 基础验证

fastjson3 支持 [Draft 2020-12](https://json-schema.org/draft/2020-12/release-notes.html) JSON Schema 规范。

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

// 创建 Schema
JSONSchema schema = JSONSchema.of(schemaJson);

// 验证数据
String dataJson = """{"name": "张三", "age": 25, "email": "zhangsan@example.com"}""";

ValidateResult result = schema.validate(dataJson);

if (result.isSuccess()) {
    System.out.println("验证通过");
} else {
    System.out.println("验证失败: " + result.getMessage());
}
```

---

## 类型约束

### 基础类型

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "name": {"type": "string"},
            "age": {"type": "integer"},
            "height": {"type": "number"},
            "vip": {"type": "boolean"},
            "tags": {"type": "array"},
            "metadata": {"type": "object"},
            "comment": {"type": "null"}
        }
    }
    """;
```

### 类型说明

| JSON Schema 类型 | 对应 Java 类型 |
|-----------------|---------------|
| `string` | String |
| `integer` | Integer, Long, BigInteger |
| `number` | Double, BigDecimal |
| `boolean` | Boolean |
| `array` | List, Array |
| `object` | JSONObject, Map |
| `null` | null |

### 多类型

```java
// 允许字符串或数字
String schema = """
    {
        "type": "object",
        "properties": {
            "count": {
                "type": ["integer", "null"]  // 整数或 null
            },
            "value": {
                "type": ["string", "number"]  // 字符串或数字
            }
        }
    }
    """;
```

---

## 数值约束

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "age": {
                "type": "integer",
                "minimum": 0,           // 最小值
                "maximum": 150,         // 最大值
                "exclusiveMinimum": 0   // 大于（不含），建议使用 "exclusiveMinimum": 0
            },
            "price": {
                "type": "number",
                "minimum": 0,
                "exclusiveMinimum": true,  // 大于 0（不含 0）
                "multipleOf": 0.01          // 必须是 0.01 的倍数
            },
            "quantity": {
                "type": "integer",
                "multipleOf": 10  // 必须是 10 的倍数
            }
        }
    }
    """;
```

### 数值约束属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `minimum` | number | 最小值（包含） |
| `maximum` | number | 最大值（包含） |
| `exclusiveMinimum` | number/boolean | 大于（不含） |
| `exclusiveMaximum` | number/boolean | 小于（不含） |
| `multipleOf` | number | 必须是该值的倍数 |

---

## 字符串约束

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "username": {
                "type": "string",
                "minLength": 3,
                "maxLength": 20
            },
            "password": {
                "type": "string",
                "minLength": 8
            },
            "code": {
                "type": "string",
                "pattern": "^[A-Z0-9]+$"
            },
            "email": {
                "type": "string",
                "format": "email"
            },
            "website": {
                "type": "string",
                "format": "uri"
            },
            "birthday": {
                "type": "string",
                "format": "date"
            }
        }
    }
    """;
```

### 字符串约束属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `minLength` | integer | 最小长度 |
| `maxLength` | integer | 最大长度 |
| `pattern` | string | 正则表达式模式 |
| `format` | string | 预定义格式 |

### 预定义格式

| 格式 | 说明 | 示例 |
|------|------|------|
| `email` | 邮箱地址 | `user@example.com` |
| `uri` / `url` | URI 地址 | `https://example.com` |
| `date` | 日期 | `2026-03-17` |
| `time` | 时间 | `10:30:00` |
| `date-time` | 日期时间 | `2026-03-17T10:30:00Z` |
| `uuid` | UUID | `550e8400-e29b-41d4-a716-446655440000` |
| `hostname` | 主机名 | `example.com` |
| `ipv4` | IPv4 地址 | `192.168.1.1` |
| `ipv6` | IPv6 地址 | `::1` |

---

## 数组约束

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "tags": {
                "type": "array",
                "items": {"type": "string"},
                "minItems": 1,
                "maxItems": 5,
                "uniqueItems": true
            },
            "scores": {
                "type": "array",
                "items": {
                    "type": "integer",
                    "minimum": 0,
                    "maximum": 100
                }
            },
            "matrix": {
                "type": "array",
                "items": {
                    "type": "array",
                    "items": {"type": "number"}
                }
            }
        }
    }
    """;
```

### 数组约束属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `items` | schema/object | 数组元素的模式 |
| `minItems` | integer | 最少元素数量 |
| `maxItems` | integer | 最多元素数量 |
| `uniqueItems` | boolean | 元素是否唯一 |
| `contains` | schema | 必须包含符合的元素 |

### 前置项目验证

```java
// 验证数组中至少有一个元素符合条件
String schema = """
    {
        "type": "array",
        "items": {"type": "number"},
        "contains": {
            "type": "number",
            "minimum": 100
        }
    }
    """;
```

---

## 对象约束

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "name": {"type": "string"},
            "age": {"type": "integer"}
        },
        "required": ["name"],
        "additionalProperties": false,
        "minProperties": 1,
        "maxProperties": 5
    }
    """;
```

### 对象约束属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `properties` | object | 定义属性及其模式 |
| `required` | array | 必需属性列表 |
| `additionalProperties` | boolean/schema | 是否允许额外属性 |
| `minProperties` | integer | 最少属性数量 |
| `maxProperties` | integer | 最多属性数量 |
| `propertyNames` | object | 属性名的模式 |

### 额外属性控制

```java
// 不允许额外属性
String schema1 = """
    {
        "type": "object",
        "properties": {"name": {"type": "string"}},
        "additionalProperties": false
    }
    """;

// 额外属性必须是字符串
String schema2 = """
    {
        "type": "object",
        "properties": {"name": {"type": "string"}},
        "additionalProperties": {"type": "string"}
    }
    """;

// 属性名必须符合模式
String schema3 = """
    {
        "type": "object",
        "propertyNames": {
            "pattern": "^[a-z][a-zA-Z0-9]*$"
        }
    }
    """;
```

---

## 枚举值

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "status": {
                "type": "string",
                "enum": ["active", "inactive", "pending"]
            },
            "priority": {
                "type": "integer",
                "enum": [1, 2, 3]
            },
            "mode": {
                "type": ["string", "null"],
                "enum": ["auto", "manual", null]
            }
        }
    }
    """;
```

---

## 常量值

```java
// 字段必须是固定值
String schema = """
    {
        "type": "object",
        "properties": {
            "version": {
                "const": "1.0.0"
            },
            "type": {
                "const": "user"
            }
        }
    }
    """;
```

---

## 默认值

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "name": {
                "type": "string",
                "default": "Anonymous"
            },
            "count": {
                "type": "integer",
                "default": 0
            },
            "enabled": {
                "type": "boolean",
                "default": true
            }
        }
    }
    """;
```

---

## 嵌套对象

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "name": {"type": "string"},
            "address": {
                "type": "object",
                "properties": {
                    "street": {"type": "string"},
                    "city": {"type": "string"},
                    "zip": {
                        "type": "string",
                        "pattern": "^\\\\d{6}$"
                    }
                },
                "required": ["city"]
            }
        }
    }
    """;
```

---

## 必需字段

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "id": {"type": "string"},
            "name": {"type": "string"},
            "email": {"type": "string"},
            "age": {"type": "integer"}
        },
        "required": ["id", "name"]
    }
    """;
```

---

## 常用验证模式

### 用户注册验证

```java
String userSchema = """
    {
        "type": "object",
        "properties": {
            "username": {
                "type": "string",
                "minLength": 3,
                "maxLength": 20,
                "pattern": "^[a-zA-Z0-9_]+$"
            },
            "email": {
                "type": "string",
                "format": "email"
            },
            "age": {
                "type": "integer",
                "minimum": 13,
                "maximum": 120
            },
            "password": {
                "type": "string",
                "minLength": 8
            }
        },
        "required": ["username", "email", "password"]
    }
    """;
```

### 配置验证

```java
String configSchema = """
    {
        "type": "object",
        "properties": {
            "port": {
                "type": "integer",
                "minimum": 1,
                "maximum": 65535
            },
            "host": {
                "type": "string",
                "format": "hostname"
            },
            "ssl": {
                "type": "boolean",
                "default": false
            },
            "timeout": {
                "type": "integer",
                "minimum": 0,
                "default": 30
            }
        },
        "required": ["port", "host"]
    }
    """;
```

### 分页参数验证

```java
String pageSchema = """
    {
        "type": "object",
        "properties": {
            "page": {
                "type": "integer",
                "minimum": 1,
                "default": 1
            },
            "size": {
                "type": "integer",
                "minimum": 1,
                "maximum": 100,
                "default": 20
            },
            "sort": {
                "type": "string",
                "enum": ["name", "date", "views"]
            }
        }
    }
    """;
```

---

## 相关文档

- [JSON Schema 高级验证 →](advanced.md)
- [跨库对比 →](comparison.md)

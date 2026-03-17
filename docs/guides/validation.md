# JSON Schema 验证指南

JSON Schema 是一种声明 JSON 数据结构的标准方式，fastjson3 支持 [Draft 2020-12](https://json-schema.org/draft/2020-12/release-notes.html) 规范。

## 基础验证

### 创建 Schema

```java
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

JSONSchema schema = JSONSchema.of(schemaJson);
```

### 验证数据

```java
String dataJson = """
    {"name": "张三", "age": 25, "email": "zhangsan@example.com"}
    """;

ValidateResult result = schema.validate(dataJson);

if (result.isSuccess()) {
    System.out.println("验证通过");
} else {
    System.out.println("验证失败: " + result.getMessage());
    // 获取所有错误
    List<ValidateResult> errors = result.getErrors();
}
```

## 常用约束

### 类型约束

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
            "metadata": {"type": "object"}
        }
    }
    """;
```

### 数值约束

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "age": {
                "type": "integer",
                "minimum": 0,       // 最小值
                "maximum": 150      // 最大值
            },
            "price": {
                "type": "number",
                "exclusiveMinimum": 0,  // 大于（不含）
                "multipleOf": 0.01      // 必须是 0.01 的倍数
            }
        }
    }
    """;
```

### 字符串约束

```java
String schema = """
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
            "url": {
                "type": "string",
                "format": "uri"
            }
        }
    }
    """;
```

### 数组约束

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
                "items": {"type": "integer", "minimum": 0, "maximum": 100}
            }
        }
    }
    """;
```

### 嵌套对象

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "name": {"type": "string"},
            "address": {
                "type": "object",
                "properties": {
                    "city": {"type": "string"},
                    "street": {"type": "string"},
                    "zip": {"type": "string", "pattern": "^\\d{6}$"}
                },
                "required": ["city"]
            }
        }
    }
    """;
```

## 高级验证

### 条件验证

```java
String schema = """
    {
        "type": "object",
        "properties": {
            "country": {"type": "string"},
            "postalCode": {"type": "string"}
        },
        "if": {
            "properties": {"country": {"const": "US"}}
        },
        "then": {
            "properties": {
                "postalCode": {"pattern": "^\\d{5}(-\\d{4})?$"}
            }
        },
        "else": {
            "properties": {
                "postalCode": {"pattern": "^[A-Z]{1,2}\\d[A-Z\\d]? \\d[A-Z]{2}$"}
            }
        }
    }
    """;
```

### 枚举值

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
            }
        }
    }
    """;
```

### 组合验证

```java
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

## 预定义验证器

fastjson3 提供了一些常用的验证器：

```java
// 域名验证
DomainValidator domainValidator = new DomainValidator();
domainValidator.isValid("example.com");

// 邮箱验证
EmailValidator emailValidator = new EmailValidator();
emailValidator.isValid("user@example.com");

// IP 地址验证
IPAddressValidator ipValidator = new IPAddressValidator();
ipValidator.isValid("192.168.1.1");

// UUID 验证
UUIDValidator uuidValidator = new UUIDValidator();
uuidValidator.isValid("550e8400-e29b-41d4-a716-446655440000");
```

## 实际应用

### API 请求验证

```java
public class UserService {
    private static final JSONSchema USER_SCHEMA = JSONSchema.of("""
        {
            "type": "object",
            "properties": {
                "name": {"type": "string", "minLength": 1, "maxLength": 50},
                "email": {"type": "string", "format": "email"},
                "age": {"type": "integer", "minimum": 0, "maximum": 150}
            },
            "required": ["name", "email"]
        }
        """);

    public void createUser(String userJson) {
        ValidateResult result = USER_SCHEMA.validate(userJson);
        if (!result.isSuccess()) {
            throw new IllegalArgumentException(
                "Invalid user data: " + result.getMessage()
            );
        }
        // 处理用户创建...
    }
}
```

### 配置文件验证

```java
public class ConfigValidator {
    private static final JSONSchema CONFIG_SCHEMA = JSONSchema.of("""
        {
            "type": "object",
            "properties": {
                "server": {
                    "type": "object",
                    "properties": {
                        "port": {"type": "integer", "minimum": 1, "maximum": 65535},
                        "host": {"type": "string"}
                    },
                    "required": ["port"]
                },
                "database": {
                    "type": "object",
                    "properties": {
                        "url": {"type": "string", "format": "uri"},
                        "maxConnections": {"type": "integer", "minimum": 1}
                    },
                    "required": ["url"]
                }
            },
            "required": ["server"]
        }
        """);

    public static void validateConfig(String configJson) {
        ValidateResult result = CONFIG_SCHEMA.validate(configJson);
        if (!result.isSuccess()) {
            throw new IllegalStateException(
                "Invalid config: " + result.getMessage()
            );
        }
    }
}
```

## 错误处理

### 详细错误信息

```java
ValidateResult result = schema.validate(dataJson);

if (!result.isSuccess()) {
    // 获取单个错误消息
    String message = result.getMessage();

    // 获取所有错误
    List<ValidateResult> errors = result.getErrors();
    for (ValidateResult error : errors) {
        System.out.println("路径: " + error.getPath());
        System.out.println("错误: " + error.getMessage());
        System.out.println("值: " + error.getValue());
    }
}
```

## 最佳实践

1. **缓存 Schema** - Schema 编译后应该缓存复用

```java
private static final JSONSchema USER_SCHEMA = JSONSchema.of(schemaJson);
```

2. **使用预定义格式** - 优先使用 `format` 而非 `pattern`

```java
// ✅ 好
{"type": "string", "format": "email"}

// ❌ 除非必要，否则不要自定义正则
{"type": "string", "pattern": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Z]{2,}$"}
```

3. **合理设置 required** - 只对真正必需的字段设置

4. **提供清晰的错误消息** - 根据验证结果返回用户友好的错误

## 参考链接

- [JSON Schema 官方规范](https://json-schema.org/)
- [Draft 2020-12 规范](https://json-schema.org/draft/2020-12/release-notes.html)

## 相关文档

- 📖 [JSONPath 查询指南 →](jsonpath.md)
- 📖 [POJO 序列化指南 →](pojo.md)
- 📋 [JSON Schema API 参考 →](../api/schema.md)

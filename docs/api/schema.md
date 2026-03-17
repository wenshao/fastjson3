# JSON Schema API 参考

fastjson3 支持 JSON Schema Draft 2020-12 规范，用于验证 JSON 数据结构。

## JSONSchema 类

核心的 Schema 类，用于编译和验证 JSON Schema。

### 创建 Schema

```java
// 从 JSON 字符串创建
String schemaJson = """
    {
        "type": "object",
        "properties": {
            "name": {"type": "string"},
            "age": {"type": "integer"}
        }
    }
    """;
JSONSchema schema = JSONSchema.of(schemaJson);

// 从 JSONObject 创建
JSONObject schemaObj = JSON.parseObject(schemaJson);
JSONSchema schema = JSONSchema.of(schemaObj);

// 从文件创建
String schemaJson = Files.readString(Path.of("schema.json"));
JSONSchema schema = JSONSchema.of(schemaJson);
```

### 验证数据

```java
String dataJson = "{\"name\":\"张三\",\"age\":25}";

// 验证
ValidateResult result = schema.validate(dataJson);

if (result.isSuccess()) {
    System.out.println("验证通过");
} else {
    System.out.println("验证失败: " + result.getMessage());
}
```

### 获取错误详情

```java
ValidateResult result = schema.validate(dataJson);

if (!result.isSuccess()) {
    // 单个错误消息
    String message = result.getMessage();

    // 所有错误
    List<ValidateResult> errors = result.getErrors();
    for (ValidateResult error : errors) {
        System.out.println("路径: " + error.getPath());
        System.out.println("错误: " + error.getMessage());
        System.out.println("值: " + error.getValue());
    }
}
```

---

## ValidateResult 类

验证结果类。

### 属性

```java
public class ValidateResult {
    // 验证是否成功
    public boolean isSuccess()

    // 错误消息
    public String getMessage()

    // 错误路径（JSONPath）
    public String getPath()

    // 错误值
    public Object getValue()

    // 所有子错误
    public List<ValidateResult> getErrors()

    // 错误代码
    public String getErrorCode()
}
```

### 使用示例

```java
ValidateResult result = schema.validate(data);

if (!result.isSuccess()) {
    // 检查错误类型
    if (result.getErrorCode().equals("type")) {
        System.err.println("类型错误: " + result.getMessage());
    } else if (result.getErrorCode().equals("required")) {
        System.err.println("缺少必填字段: " + result.getPath());
    }

    // 遍历所有错误
    for (ValidateResult error : result.getErrors()) {
        System.err.printf("%s: %s%n", error.getPath(), error.getMessage());
    }
}
```

---

## 预定义验证器

fastjson3 提供了常用的预定义验证器。

### DomainValidator - 域名验证

```java
DomainValidator validator = new DomainValidator();

// 验证
boolean valid = validator.isValid("example.com");  // true
boolean valid = validator.isValid("sub.example.com");  // true
boolean valid = validator.isValid("invalid");  // false
```

### EmailValidator - 邮箱验证

```java
EmailValidator validator = new EmailValidator();

boolean valid = validator.isValid("user@example.com");  // true
boolean valid = validator.isValid("invalid");  // false
```

### IPAddressValidator - IP 地址验证

```java
IPAddressValidator validator = new IPAddressValidator();

boolean valid = validator.isValid("192.168.1.1");  // true
boolean valid = validator.isValid("::1");  // true (IPv6)
boolean valid = validator.isValid("invalid");  // false
```

### UUIDValidator - UUID 验证

```java
UUIDValidator validator = new UUIDValidator();

boolean valid = validator.isValid("550e8400-e29b-41d4-a716-446655440000");  // true
boolean valid = validator.isValid("invalid");  // false
```

### URIValidator - URI 验证

```java
URIValidator validator = new URIValidator();

boolean valid = validator.isValid("https://example.com");  // true
boolean valid = validator.isValid("invalid");  // false
```

---

## Schema 规范

### 类型约束

```json
{
    "type": "object",
    "properties": {
        "name": {"type": "string"},
        "age": {"type": "integer"},
        "height": {"type": "number"},
        "vip": {"type": "boolean"},
        "tags": {"type": "array"},
        "metadata": {"type": "object"},
        "nullable": {"type": "null"}
    }
}
```

### 数值约束

```json
{
    "properties": {
        "age": {
            "type": "integer",
            "minimum": 0,
            "maximum": 150
        },
        "price": {
            "type": "number",
            "exclusiveMinimum": 0,
            "multipleOf": 0.01
        }
    }
}
```

### 字符串约束

```json
{
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
```

### 数组约束

```json
{
    "properties": {
        "items": {
            "type": "array",
            "items": {"type": "string"},
            "minItems": 1,
            "maxItems": 10,
            "uniqueItems": true
        }
    }
}
```

### 对象约束

```json
{
    "properties": {
        "user": {
            "type": "object",
            "properties": {
                "name": {"type": "string"},
                "age": {"type": "integer"}
            },
            "required": ["name"],
            "additionalProperties": false
        }
    }
}
```

### 枚举值

```json
{
    "properties": {
        "status": {
            "type": "string",
            "enum": ["active", "inactive", "pending"]
        }
    }
}
```

### 条件验证

```json
{
    "if": {
        "properties": {"country": {"const": "US"}}
    },
    "then": {
        "properties": {
            "zipCode": {"pattern": "^\\d{5}(-\\d{4})?$"}
        }
    }
}
```

---

## 实际应用

### API 请求验证

```java
@RestController
public class UserController {
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

    @PostMapping("/users")
    public ResponseEntity<?> create(@RequestBody String jsonBody) {
        ValidateResult result = USER_SCHEMA.validate(jsonBody);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest()
                .body(result.getMessage());
        }

        User user = JSON.parseObject(jsonBody, User.class);
        return ResponseEntity.ok(userService.save(user));
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
                }
            },
            "required": ["server"]
        }
        """);

    public static void validate(String configJson) {
        ValidateResult result = CONFIG_SCHEMA.validate(configJson);
        if (!result.isSuccess()) {
            throw new IllegalStateException(
                "Invalid config: " + result.getMessage()
            );
        }
    }
}
```

---

## 性能建议

### 缓存 Schema

```java
// ✅ 好：编译一次，重复使用
private static final JSONSchema USER_SCHEMA = JSONSchema.of(schemaJson);

public void validateUser(String json) {
    USER_SCHEMA.validate(json);
}

// ❌ 不好：每次创建
public void validateUser(String json) {
    JSONSchema schema = JSONSchema.of(schemaJson);  // 每次编译
    schema.validate(json);
}
```

---

## 常用 Schema 模板

### 基本对象

```json
{
    "type": "object",
    "properties": {},
    "required": [],
    "additionalProperties": true
}
```

### 用户对象

```json
{
    "type": "object",
    "properties": {
        "id": {"type": "integer"},
        "name": {"type": "string", "minLength": 1},
        "email": {"type": "string", "format": "email"},
        "age": {"type": "integer", "minimum": 0}
    },
    "required": ["id", "name"]
}
```

### 分页响应

```json
{
    "type": "object",
    "properties": {
        "data": {"type": "array"},
        "total": {"type": "integer"},
        "page": {"type": "integer"},
        "pageSize": {"type": "integer"}
    },
    "required": ["data", "total"]
}
```

---

## 完整示例

```java
public class SchemaDemo {
    public static void main(String[] args) {
        // 定义 Schema
        String schemaJson = """
            {
                "type": "object",
                "properties": {
                    "name": {"type": "string", "minLength": 1},
                    "age": {"type": "integer", "minimum": 0, "maximum": 150},
                    "email": {"type": "string", "format": "email"}
                },
                "required": ["name", "age"]
            }
            """;

        JSONSchema schema = JSONSchema.of(schemaJson);

        // 验证有效数据
        String validJson = "{\"name\":\"张三\",\"age\":25,\"email\":\"test@example.com\"}";
        ValidateResult result1 = schema.validate(validJson);
        System.out.println("有效数据: " + result1.isSuccess());  // true

        // 验证无效数据
        String invalidJson = "{\"age\":25}";  // 缺少 name
        ValidateResult result2 = schema.validate(invalidJson);
        System.out.println("无效数据: " + result2.isSuccess());  // false
        System.out.println("错误: " + result2.getMessage());
    }
}
```

## 相关文档

- [📖 JSON Schema 验证指南 →](../guides/validation.md)
- [📖 JSONPath 指南 →](../guides/jsonpath.md)
- [📋 JSON 类参考 →](JSON.md)

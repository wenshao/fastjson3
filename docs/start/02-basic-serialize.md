# 序列化基础

> 📚 本系列教程：
> - [← 上一节：解析基础](01-basic-parse.md) | [下一节：常见问题 →](03-common-issues.md)

## 基础序列化

### 对象转 JSON

```java
User user = new User();
user.setName("张三");
user.setAge(25);

// 转为 JSON 字符串
String json = JSON.toJSONString(user);
// {"name":"张三","age":25}

// 转为字节数组（更快）
byte[] bytes = JSON.toJSONBytes(user);
```

### 美化输出

```java
String json = JSON.toJSONString(user, WriteFeature.PrettyFormat);

// 输出：
// {
//     "name": "张三",
//     "age": 25
// }
```

### 使用 ObjectMapper

```java
ObjectMapper mapper = ObjectMapper.shared();

String json = mapper.writeValueAsString(user);
byte[] bytes = mapper.writeValueAsBytes(user);

// 写入文件
mapper.writeValue(new File("user.json"), user);
```

## 控制输出

### 字段重命名

```java
public class User {
    @JSONField(name = "user_name")
    private String userName;

    @JSONField(name = "id")
    private Long userId;
}
// 输出: {"user_name":"张三","id":123}
```

### 忽略字段

```java
public class User {
    @JSONField(serialize = false)  // 不序列化
    private String password;

    // 多个字段
}
@JSONType(ignores = {"password", "salt", "internalId"})
public class User {
    private String password;
    private String salt;
    private String internalId;
}
```

### 控制字段顺序

```java
public class User {
    @JSONField(ordinal = 1)  // 数值越小越靠前
    private Long id;

    @JSONField(ordinal = 2)
    private String name;
}
```

### 处理 null 值

```java
// 默认不输出 null
User user = new User();
user.setName("张三");
// {"name":"张三"}

// 输出 null
String json = JSON.toJSONString(user, WriteFeature.WriteNulls);
// {"name":"张三","age":null,"email":null}
```

## 日期处理

### 格式化日期

```java
public class Event {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JSONField(format = "yyyy-MM-dd")
    private LocalDate date;
}

// 输出: {"createTime":"2026-03-17 14:30:00","date":"2026-03-17"}
```

### 使用时间戳

```java
public class Event {
    @JSONField(format = "millis")  // 毫秒时间戳
    private Date createTime;
}
```

## 枚举处理

```java
public enum Status {
    ACTIVE, INACTIVE
}

public class User {
    private Status status;
}

// 默认输出名称
// {"status":"ACTIVE"}

// 输出序号
String json = JSON.toJSONString(user,
    WriteFeature.WriteEnumUsingOrdinal);
// {"status":0}

// 使用 toString()
@JSONField(writeUsing = StatusSerializer.class)
private Status status;
```

## 高级配置

### 浏览器兼容

```java
// 转义特殊字符，兼容 JavaScript
String json = JSON.toJSONString(user,
    WriteFeature.BrowserCompatible);
```

### 环形引用处理

```java
public class Department {
    public String name;
    public List<Employee> employees;
}

public class Employee {
    public String name;
    public Department department;  // 循环引用
}

// 使用引用检测
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.WriteClassName)
    .build();
```

## 完整示例

```java
public class User {
    @JSONField(name = "user_id", ordinal = 1)
    private Long id;

    @JSONField(name = "user_name", ordinal = 2)
    private String name;

    @JSONField(serialize = false)
    private String password;

    @JSONField(format = "yyyy-MM-dd")
    private LocalDate birthday;
}

public class Demo {
    public static void main(String[] args) {
        User user = new User();
        user.setId(1L);
        user.setName("张三");
        user.setPassword("secret");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        // 基本序列化
        String json = JSON.toJSONString(user);
        System.out.println(json);
        // {"user_id":1,"user_name":"张三","birthday":"2000-01-01"}

        // 美化输出
        String pretty = JSON.toJSONString(user, WriteFeature.PrettyFormat);
        System.out.println(pretty);

        // 包含 null
        User empty = new User();
        String withNulls = JSON.toJSONString(empty, WriteFeature.WriteNulls);
    }
}
```

## 性能建议

```java
// ❌ 不好：每次创建新 mapper
public String toJSON(User user) {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(user);
}

// ✅ 好：复用 mapper
private static final ObjectMapper MAPPER = ObjectMapper.shared();

public String toJSON(User user) {
    return MAPPER.writeValueAsString(user);
}
```

## 下一步

- ❓ [常见问题 →](03-common-issues.md)
- 📖 [POJO 完整指南 →](../guides/pojo.md)
- 📋 [ObjectMapper API 参考 →](../api/ObjectMapper.md)

**快速提示：**
- 复用 `ObjectMapper` 实例
- 优先使用 `toJSONBytes()` 而非 `toJSONString()`
- 生产环境启用 ASM 提升性能

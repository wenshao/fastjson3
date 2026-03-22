# POJO 序列化完整指南

本指南涵盖 Java 对象与 JSON 互转的完整知识。

## 基础映射

### 简单对象

```java
public class User {
    private String name;
    private int age;
    // getters & setters or public fields
}

// 序列化
User user = new User();
user.setName("张三");
user.setAge(25);
String json = JSON.toJSONString(user);
// {"name":"张三","age":25}

// 反序列化
User user = JSON.parseObject(json, User.class);
```

### 字段访问

fastjson3 支持多种字段访问方式：

```java
// 方式1: public 字段
public class User {
    public String name;  // 直接访问
}

// 方式2: getter/setter
public class User {
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// 方式3: 强制字段访问（无需 getter/setter）
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.FieldBased)
    .build();
```

## 注解控制

### @JSONField - 字段级注解

```java
public class Product {
    // 重命名字段
    @JSONField(name = "product_id")
    private Long id;

    // 日期格式
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 序列化顺序
    @JSONField(ordinal = 1)
    private String name;

    // 忽略序列化
    @JSONField(serialize = false)
    private String internalNote;

    // 忽略反序列化
    @JSONField(deserialize = false)
    private String computedField;

    // 替代字段名（多个可能的 JSON 字段名）
    @JSONField(alternateNames = {"email", "emailAddress", "mail"})
    private String email;
}
```

### @JSONType - 类级注解

```java
// 命名策略
@JSONType(naming = NamingStrategy.SnakeCase)
public class Config {
    private String serverName;  // → "server_name"
    private Integer maxConnections;  // → "max_connections"
}

// 忽略多个字段
@JSONType(ignores = {"password", "salt", "internalId"})
public class User {
    private String password;
    private String salt;
    private String internalId;
}

// 自定义序列化器（通过字段注解指定）
// 注意：@JSONType 不支持 serializer 参数，
// 请使用 @JSONField(serializeUsing = ...) 或通过 ObjectMapper 注册
public class User {
    // ...
}
```

### @JSONCreator - 自定义构造

```java
public class Point {
    private final int x;
    private final int y;

    @JSONCreator
    public Point(
        @JSONField(name = "x") int x,
        @JSONField(name = "y") int y
    ) {
        this.x = x;
        this.y = y;
    }
}

// 解析
Point p = JSON.parseObject("{\"x\":10,\"y\":20}", Point.class);
```

## 复杂类型

### 集合

```java
public class Order {
    private List<String> items;
    private Set<String> tags;
    private Map<String, Object> metadata;
}

// 自动映射
String json = """
    {
        "items": ["A", "B", "C"],
        "tags": ["urgent", "vip"],
        "metadata": {"key": "value"}
    }
    """;
Order order = JSON.parseObject(json, Order.class);
```

### 嵌套对象

```java
public class User {
    private String name;
    private Address address;  // 嵌套对象
}

public class Address {
    private String city;
    private String street;
}

String json = """
    {
        "name": "张三",
        "address": {
            "city": "北京",
            "street": "朝阳区"
        }
    }
    """;
User user = JSON.parseObject(json, User.class);
```

### 泛型处理

```java
// List<User>
TypeToken<List<User>> listType = new TypeToken<List<User>>() {};
List<User> users = JSON.parseObject(json, listType);

// Map<String, User>
TypeToken<Map<String, User>> mapType = new TypeToken<Map<String, User>>() {};
Map<String, User> userMap = JSON.parseObject(json, mapType);

// 复杂泛型 Map<String, List<User>>
TypeToken<Map<String, List<User>>> complexType =
    new TypeToken<Map<String, List<User>>>() {};
Map<String, List<User>> grouped = JSON.parseObject(json, complexType);
```

## 日期处理

### Date 类型

```java
public class Event {
    // 格式化
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 时间戳
    @JSONField(format = "millis")
    private Date timestamp;

    // 使用 ISO 格式
    @JSONField(format = "iso")
    private Date isoTime;
}
```

### Java Time API

```java
public class Event {
    @JSONField(format = "yyyy-MM-dd")
    private LocalDate date;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    @JSONField(format = "HH:mm:ss")
    private LocalTime time;

    @JSONField(format = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime zonedDateTime;
}
```

## 枚举处理

```java
public enum Status {
    ACTIVE, INACTIVE, PENDING
}

public class User {
    private Status status;
}

// 默认输出序号
// {"status":0}

// 配置输出名称
String json = JSON.toJSONString(user,
    WriteFeature.WriteEnumsUsingName);

// 或在序列化时指定 WriteFeature
// 注意：@JSONField 不支持 writeEnumUsingName 参数，
// 请使用 WriteFeature.WriteEnumsUsingName
```

## 多态类型

### 使用 @JSONType

```java
@JSONType(typeName = "dog", seeAlso = {Dog.class, Cat.class})
public abstract class Animal {
    public String name;
}

@JSONType(typeName = "dog")
public class Dog extends Animal {
    public String breed;
}

@JSONType(typeName = "cat")
public class Cat extends Animal {
    public boolean indoor;
}
```

### 启用自动类型

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.SupportAutoType)
    .build();

// JSON 包含类型信息
String json = """
    {"@type":"com.example.Dog","name":"Buddy","breed":"Labrador"}
    """;
Animal animal = mapper.readValue(json, Animal.class);
```

## 忽略字段

### 方式总结

```java
public class User {
    // 1. 使用 transient（仅序列化时忽略）
    private transient String tempData;

    // 2. 使用注解
    @JSONField(serialize = false)
    private String password;

    @JSONField(deserialize = false)
    private String computedField;

    // 3. 类级别忽略
}
@JSONType(ignores = {"internalField"})
public class User {
    private String internalField;
}
```

## 最佳实践

### 1. 提供无参构造

```java
// ✅ 好
public class User {
    private String name;
    public User() {}  // 无参构造
    public User(String name) { this.name = name; }
}

// ⚠️ 如果没有无参构造，需要用 @JSONCreator 指定
```

### 2. 不可变对象

```java
public final class ImmutableUser {
    private final String name;
    private final int age;

    @JSONCreator
    public ImmutableUser(
        @JSONField(name = "name") String name,
        @JSONField(name = "age") int age
    ) {
        this.name = name;
        this.age = age;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
}
```

### 3. 复用 ObjectMapper

```java
// ✅ 好
private static final ObjectMapper MAPPER = ObjectMapper.shared();

public String toJSON(User user) {
    return MAPPER.writeValueAsString(user);
}

// ❌ 不好
public String toJSON(User user) {
    ObjectMapper mapper = new ObjectMapper();  // 每次创建
    return mapper.writeValueAsString(user);
}
```

## 完整示例

```java
@JSONType(naming = NamingStrategy.SnakeCase)
public class User {
    @JSONField(name = "id", ordinal = 1)
    private Long userId;

    @JSONField(ordinal = 2)
    private String name;

    @JSONField(format = "yyyy-MM-dd", ordinal = 3)
    private LocalDate birthday;

    @JSONField(serialize = false)
    private String password;

    @JSONField(alternateNames = {"mail", "emailAddress"})
    private String email;

    private Address address;

    @JSONType(ignores = {"password"})
    public static class Address {
        private String city;
        private String street;
    }

    // getters & setters
}

public class Demo {
    private static final ObjectMapper MAPPER = ObjectMapper.shared();

    public static void main(String[] args) {
        User user = new User();
        user.setUserId(1L);
        user.setName("张三");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setEmail("zhangsan@example.com");

        // 序列化
        String json = MAPPER.writeValueAsString(user);
        System.out.println(json);
        // {"id":1,"name":"张三","birthday":"2000-01-01","email":"zhangsan@example.com"}

        // 反序列化
        User parsed = MAPPER.readValue(json, User.class);
    }
}
```

## 相关文档

- 📖 [解析基础 →](../start/01-basic-parse.md)
- 📖 [序列化基础 →](../start/02-basic-serialize.md)
- 📖 [JSONPath 查询 →](jsonpath.md)
- 📋 [注解 API 参考 →](../api/annotations.md)

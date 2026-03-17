# 注解参考

fastjson3 提供了丰富的注解来控制 JSON 序列化和反序列化行为。

## @JSONField

字段级注解，控制单个字段的 JSON 行为。

### 属性

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface JSONField {
    String name() default "";           // JSON 字段名
    String format() default "";         // 日期/数字格式
    String locale() default "";         // 区域设置
    String pattern() default "";        // 正则模式（验证）
    boolean serialize() default true;   // 是否序列化
    boolean deserialize() default true; // 是否反序列化
    int ordinal() default 0;            // 序列化顺序
    boolean ignore() default false;     // 是否忽略
    String[] alternateNames() default {};// 替代字段名
    String defaultValue() default "";    // 默认值
    boolean parseElapsedTime() default false;  // 解析为时间戳
}
```

### 使用示例

#### 字段重命名

```java
public class User {
    @JSONField(name = "user_id")
    private Long userId;

    @JSONField(name = "user_name")
    private String userName;
}
// 输出: {"user_id":123,"user_name":"张三"}
```

#### 日期格式化

```java
public class Event {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JSONField(format = "yyyy-MM-dd")
    private LocalDate date;

    @JSONField(format = "HH:mm:ss")
    private LocalTime time;

    @JSONField(format = "millis")
    private Date timestamp;  // 时间戳
}
```

#### 序列化控制

```java
public class User {
    @JSONField(serialize = false)  // 不序列化
    private String password;

    @JSONField(deserialize = false)  // 不反序列化
    private String computedValue;
}
```

#### 序列化顺序

```java
public class User {
    @JSONField(ordinal = 1)
    private Long id;

    @JSONField(ordinal = 2)
    private String name;

    @JSONField(ordinal = 3)
    private String email;
}
// 输出顺序：id, name, email
```

#### 替代字段名

```java
public class User {
    @JSONField(alternateNames = {"email", "emailAddress", "mail"})
    private String mail;
}
// JSON 中的 email/emailAddress/mail 都会映射到 mail
```

#### 使用自定义序列化器

```java
public class User {
    @JSONField(writeUsing = MoneySerializer.class)
    @JSONField(parseUsing = MoneyDeserializer.class)
    private Money balance;
}
```

---

## @JSONType

类级注解，控制整个类的 JSON 行为。

### 属性

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JSONType {
    NamingStrategy naming() default NamingStrategy.Never;  // 命名策略
    boolean ignore() default false;                       // 是否忽略
    String[] ignores() default {};                        // 忽略的字段
    Class<?> serializer() default Void.class;             // 自定义序列化器
    Class<?> deserializer() default Void.class;           // 自定义反序列化器
    String format() default "";                           // 日期格式
    boolean alphabetic() default false;                   // 按字母排序
    int[] orders() default {};                            // 字段顺序
}
```

### 使用示例

#### 命名策略

```java
@JSONType(naming = NamingStrategy.SnakeCase)
public class Config {
    private String serverName;      // -> server_name
    private Integer maxConnections; // -> max_connections
}
```

#### 忽略多个字段

```java
@JSONType(ignores = {"password", "salt", "internalId"})
public class User {
    private String password;
    private String salt;
    private String internalId;
}
```

#### 自定义序列化器

```java
@JSONType(serializer = UserSerializer.class)
public class User {
    // ...
}
```

#### 按字母排序

```java
@JSONType(alphabetic = true)
public class Data {
    private String z;
    private String a;
    private String m;
}
// 输出顺序：a, m, z
```

---

## @JSONCreator

标注用于反序列化的构造方法或工厂方法。

### 使用示例

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

### 工厂方法

```java
public class Value {
    private final int value;

    private Value(int value) {
        this.value = value;
    }

    @JSONCreator
    public static Value of(int value) {
        return new Value(value);
    }
}
```

---

## @JSONField 在方法上的使用

可以在 getter/setter 方法上使用注解。

```java
public class User {
    private String n;

    @JSONField(name = "name")
    public String getN() { return n; }

    @JSONField(name = "name")
    public void setN(String n) { this.n = n; }
}
```

---

## 注解继承

子类继承父类的注解配置。

```java
@JSONType(naming = NamingStrategy.SnakeCase)
public class BaseEntity {
    private Long id;
    private Date createTime;
}

public class User extends BaseEntity {
    private String userName;  // 继承 SnakeCase 策略
}
```

---

## Jackson 注解兼容

fastjson3 支持常见的 Jackson 注解。

### 支持的 Jackson 注解

| Jackson 注解 | fastjson3 支持 |
|-------------|---------------|
| `@JsonProperty` | ✅ 完全支持 |
| `@JsonFormat` | ✅ 支持 pattern |
| `@JsonIgnore` | ✅ 支持 |
| `@JsonInclude` | ✅ 部分支持 |
| `@JsonUnwrapped` | ✅ 支持 |
| `@JsonAlias` | ✅ 支持（类似 alternateNames） |

### 示例

```java
public class User {
    @JsonProperty("user_name")  // Jackson 注解，fastjson3 也支持
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")  // Jackson 注解
    private Date birthday;

    @JsonIgnore  // Jackson 注解
    private String password;
}
```

---

## 注解优先级

当多个注解同时存在时：

1. **字段上的注解** > 方法上的注解
2. **子类注解** > 父类注解
3. **@JSONField** > Jackson 注解

```java
public class User {
    // 使用这个注解
    @JSONField(name = "username")

    // 忽略这个（Jackson）
    @JsonProperty("user_name")
    private String userName;
}
```

---

## 命名策略

`NamingStrategy` 枚举定义的字段命名转换规则。

| 策略 | 说明 | 示例 |
|------|------|------|
| `Never` | 不转换 | `userName` → `userName` |
| `CamelCase` | 驼峰 | `userName` → `userName` |
| `PascalCase` | 帕斯卡 | `userName` → `UserName` |
| `SnakeCase` | 蛇形 | `userName` → `user_name` |
| `KebabCase` | 短线 | `userName` → `user-name` |
| `UpperCase` | 大写 | `userName` → `USERNAME` |

### 使用

```java
@JSONType(naming = NamingStrategy.SnakeCase)
public class Config {
    private String serverName;
    private Integer maxConnections;
}
// 输出: {"server_name":"...","max_connections":...}
```

---

## 完整示例

```java
@JSONType(naming = NamingStrategy.SnakeCase, ignores = {"password"})
public class User {
    @JSONField(name = "id", ordinal = 1)
    private Long userId;

    @JSONField(ordinal = 2)
    private String userName;

    @JSONField(format = "yyyy-MM-dd", ordinal = 3)
    private LocalDate birthday;

    @JSONField(serialize = false)
    private String password;

    @JSONField(alternateNames = {"mail", "emailAddress"})
    private String email;

    private String internalNote;  // 被 @JSONType ignores 排除

    @JSONCreator
    public User(
        @JSONField(name = "user_id") Long userId,
        @JSONField(name = "user_name") String userName
    ) {
        this.userId = userId;
        this.userName = userName;
    }
}

// 使用
User user = JSON.parseObject(json, User.class);
String json = JSON.toJSONString(user);
```

## 相关文档

- [📋 特性枚举 →](features.md)
- [📖 POJO 序列化 →](../guides/pojo.md)
- [📖 解析基础 →](../start/01-basic-parse.md)

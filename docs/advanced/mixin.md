# Mixin 配置

Mixin 允许你为第三方类添加 JSON 注解配置，无需修改原始类。

## 概述

当你需要控制**无法修改的类**（如第三方库中的类）的序列化行为时，Mixin 是最佳解决方案。

---

## 基本用法

### 定义 Mixin 接口

```java
// 第三方类（无法修改）
public class ThirdPartyUser {
    private String usrName;
    private String usrEmail;
    private String internalId;
    // getters & setters
}

// 定义 Mixin 接口
public abstract class ThirdPartyUserMixin {
    // 重命名字段
    @JSONField(name = "username")
    abstract String getUsrName();

    @JSONField(name = "email")
    abstract String getUsrEmail();

    // 忽略字段
    @JSONField(serialize = false)
    abstract String getInternalId();
}
```

### 应用 Mixin

```java
ObjectMapper mapper = ObjectMapper.builder()
    .addMixIn(ThirdPartyUser.class, ThirdPartyUserMixin.class)
    .build();
```

### 效果

```java
ThirdPartyUser user = new ThirdPartyUser();
user.setUsrName("张三");
user.setUsrEmail("test@example.com");
user.setInternalId("12345");

String json = mapper.writeValueAsString(user);
// 输出: {"username":"张三","email":"test@example.com"}
// internalId 被忽略
```

---

## 字段重命名

```java
// 第三方类
public class Product {
    private String product_id;
    private String product_name;
    // getters & setters
}

// Mixin
public abstract class ProductMixin {
    @JSONField(name = "id")
    abstract String getProduct_id();

    @JSONField(name = "name")
    abstract String getProduct_name();
}
```

---

## 忽略字段

```java
// 第三方类包含敏感信息
public class ExternalUser {
    private String name;
    private String password;
    private String ssn;
    // getters & setters
}

// Mixin：忽略敏感字段
public abstract class ExternalUserMixin {
    @JSONField(serialize = false)
    abstract String getPassword();

    @JSONField(serialize = false)
    abstract String getSsn();
}
```

---

## 添加格式化

```java
// 第三方类
public class Event {
    private Date createTime;
    private BigDecimal amount;
    // getters & setters
}

// Mixin：添加格式化
public abstract class EventMixin {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    abstract Date getCreateTime();

    @JSONField(format = "0.00")
    abstract BigDecimal getAmount();
}
```

---

## 控制序列化顺序

```java
// 第三方类
public class Data {
    private String z;
    private String a;
    private String m;
    // getters & setters
}

// Mixin：指定顺序
public abstract class DataMixin {
    @JSONField(ordinal = 1)
    abstract String getA();

    @JSONField(ordinal = 2)
    abstract String getM();

    @JSONField(ordinal = 3)
    abstract String getZ();
}
```

---

## 多个 Mixin

### 为单个类添加多个 Mixin

```java
// Mixin1：字段重命名
public abstract class UserRenameMixin {
    @JSONField(name = "username")
    abstract String getName();
}

// Mixin2：格式化
public abstract class UserFormatMixin {
    @JSONField(format = "yyyy-MM-dd")
    abstract Date getBirthday();
}

// 应用（后面的会覆盖前面的）
ObjectMapper mapper = ObjectMapper.builder()
    .addMixIn(User.class, UserRenameMixin.class)
    .addMixIn(User.class, UserFormatMixin.class)
    .build();
```

### 为多个类使用同一 Mixin

```java
// 通用 Mixin
public abstract class TimestampMixin {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    abstract Date getCreateTime();
}

// 应用到多个类
ObjectMapper mapper = ObjectMapper.builder()
    .addMixIn(Order.class, TimestampMixin.class)
    .addMixIn(User.class, TimestampMixin.class)
    .addMixIn(Event.class, TimestampMixin.class)
    .build();
```

---

## Mixin 与接口

### 为接口添加 Mixin

```java
// 接口
public interface User {
    String getName();
    String getEmail();
}

// Mixin
public abstract class UserMixin {
    @JSONField(name = "username")
    abstract String getName();

    @JSONField(name = "email_address")
    abstract String getEmail();
}

// 应用
ObjectMapper mapper = ObjectMapper.builder()
    .addMixIn(User.class, UserMixin.class)  // 注意：这里是接口
    .build();
```

---

## Mixin 与继承

### 子类覆盖父类 Mixin

```java
// 父类 Mixin
public abstract class BaseMixin {
    @JSONField(name = "base_id")
    abstract Long getId();
}

// 子类 Mixin
public abstract class ExtendedMixin extends BaseMixin {
    @JSONField(name = "id")  // 覆盖父类
    abstract Long getId();
}
```

---

## 完整示例

### 第三方库类

```java
// 无法修改的第三方类
package com.example.external;

public class ExternalData {
    private String strName;
    private String strEmail;
    private Date dtCreateTime;
    private String strPassword;
    private BigDecimal decAmount;

    // getters & setters...
}
```

### Mixin 配置

```java
package com.example.config;

import com.alibaba.fastjson3.annotation.JSONField;
import com.example.external.ExternalData;

public abstract class ExternalDataMixin {
    // 字段重命名
    @JSONField(name = "name")
    abstract String getStrName();

    @JSONField(name = "email")
    abstract String getStrEmail();

    @JSONField(name = "createTime")
    abstract Date getDtCreateTime();

    // 格式化
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    abstract Date getDtCreateTime();

    @JSONField(format = "0.00")
    abstract BigDecimal getDecAmount();

    // 忽略敏感字段
    @JSONField(serialize = false)
    abstract String getStrPassword();

    // 序列化顺序
    @JSONField(ordinal = 1)
    abstract String getStrName();

    @JSONField(ordinal = 2)
    abstract String getStrEmail();
}
```

### 应用配置

```java
@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapper.builder()
            // 应用第三方类的 Mixin
            .addMixIn(com.example.external.ExternalData.class,
                     com.example.config.ExternalDataMixin.class)

            // 其他配置...
            .enableRead(ReadFeature.SupportSmartMatch)
            .build();
    }
}
```

---

## Mixin vs 注解

| 方面 | Mixin | 注解 |
|------|-------|------|
| 修改源代码 | 不需要 | 需要 |
| 适用场景 | 第三方类 | 自己的类 |
| 可维护性 | 分离 | 集中 |
| 优先级 | 相同 | 相同 |

---

## 最佳实践

1. **Mixin 命名** - 使用 `TargetMixin` 格式
2. **包结构** - Mixin 与目标类放在不同包
3. **文档化** - 记录每个 Mixin 的用途
4. **版本控制** - 当第三方类更新时，检查 Mixin 是否需要更新

---

## 常见问题

### Q: Mixin 可以用于 private 方法吗？

A: 可以，但需要确保对应的 getter/setter 是可访问的。

### Q: Mixin 可以添加字段吗？

A: 不能，Mixin 只能修改现有字段的 JSON 行为，不能添加新字段。

### Q: 多个 Mixin 冲突怎么办？

A: 后注册的 Mixin 会覆盖前面的配置。

## 相关文档

- [📖 注解参考 →](../api/annotations.md)
- [📖 POJO 序列化 →](../guides/pojo.md)
- [🔧 自定义序列化 →](custom-serializer.md)
- [🔧 模块系统 →](modules.md)

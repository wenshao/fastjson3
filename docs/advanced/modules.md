# 模块系统

fastjson3 的模块系统允许你将相关的序列化/反序列化配置组织到可重用的模块中。

## 概述

模块是 `ObjectReaderModule` 和 `ObjectWriterModule` 接口的实现，用于：

- 批量注册自定义序列化器
- 批量注册自定义反序列化器
- 注册 Mixin 配置
- 添加全局特性

---

## 创建模块

### 基础模块

```java
public class MoneyModule implements ObjectWriterModule, ObjectReaderModule {
    @Override
    public void init(ObjectMapper mapper) {
        // 注册 Money 序列化器
        mapper.registerWriter(Money.class, new MoneyWriter());

        // 注册 Money 反序列化器
        mapper.registerReader(Money.class, new MoneyReader());

        // 注册 Currency
        mapper.registerWriter(Currency.class, new CurrencyWriter());
        mapper.registerReader(Currency.class, new CurrencyReader());
    }
}
```

### 只写模块

```java
public class SerializationModule implements ObjectWriterModule {
    @Override
    public void init(ObjectMapper mapper) {
        mapper.registerWriter(Money.class, new MoneyWriter());
        mapper.registerWriter(LocalDate.class, new LocalDateWriter());
        mapper.registerWriter(LocalDateTime.class, new LocalDateTimeWriter());
    }
}
```

### 只读模块

```java
public class DeserializationModule implements ObjectReaderModule {
    @Override
    public void init(ObjectMapper mapper) {
        mapper.registerReader(Money.class, new MoneyReader());
        mapper.registerReader(LocalDate.class, new LocalDateReader());
    }
}
```

---

## 注册模块

### 单个模块

```java
ObjectMapper mapper = ObjectMapper.builder()
    .addReaderModule(new MoneyModule())
    .addWriterModule(new MoneyModule())
    .build();
```

### 多个模块

```java
ObjectMapper mapper = ObjectMapper.builder()
    .addReaderModule(new MoneyModule())
    .addWriterModule(new MoneyModule())
    .addReaderModule(new JavaTimeModule())
    .addWriterModule(new JavaTimeModule())
    .addReaderModule(new JodaTimeModule())
    .addWriterModule(new JodaTimeModule())
    .build();
```

---

## 常用模块

### Java Time 模块

```java
public class JavaTimeModule implements ObjectWriterModule, ObjectReaderModule {
    @Override
    public void init(ObjectMapper mapper) {
        // LocalDate
        mapper.registerWriter(LocalDate.class, (gen, obj, features) -> {
            gen.writeString(((LocalDate) obj).toString());
        });
        mapper.registerReader(LocalDate.class, (parser, type, fieldName, features) -> {
            return LocalDate.parse(parser.readString());
        });

        // LocalDateTime
        mapper.registerWriter(LocalDateTime.class, (gen, obj, features) -> {
            gen.writeString(((LocalDateTime) obj).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        });
        mapper.registerReader(LocalDateTime.class, (parser, type, fieldName, features) -> {
            return LocalDateTime.parse(parser.readString());
        });

        // LocalTime
        mapper.registerWriter(LocalTime.class, (gen, obj, features) -> {
            gen.writeString(((LocalTime) obj).format(DateTimeFormatter.ISO_LOCAL_TIME));
        });
        mapper.registerReader(LocalTime.class, (parser, type, fieldName, features) -> {
            return LocalTime.parse(parser.readString());
        });
    }
}
```

### Joda Time 模块

```java
public class JodaTimeModule implements ObjectWriterModule, ObjectReaderModule {
    @Override
    public void init(ObjectMapper mapper) {
        // DateTime
        mapper.registerWriter(DateTime.class, (gen, obj, features) -> {
            gen.writeString(((DateTime) obj).toString());
        });
        mapper.registerReader(DateTime.class, (parser, type, fieldName, features) -> {
            return new DateTime(parser.readString());
        });
    }
}
```

### 枚举模块

```java
public class EnumModule implements ObjectWriterModule, ObjectReaderModule {
    @Override
    public void init(ObjectMapper mapper) {
        // 所有枚举转为小写字符串
        mapper.registerWriter(Enum.class, (gen, obj, features) -> {
            gen.writeString(((Enum<?>) obj).name().toLowerCase());
        });

        // 反序列化时忽略大小写
        mapper.registerReader(Enum.class, (parser, type, fieldName, features) -> {
            String value = parser.readString();
            Class<? extends Enum> enumClass = (Class<? extends Enum>) type;
            return Enum.valueOf(enumClass, value.toUpperCase());
        });
    }
}
```

---

## 条件模块

### 环境感知模块

```java
public class EnvironmentModule implements ObjectWriterModule {
    private final boolean isProduction;

    public EnvironmentModule() {
        this.isProduction = "prod".equals(System.getProperty("env"));
    }

    @Override
    public void init(ObjectMapper mapper) {
        if (isProduction) {
            // 生产环境：脱敏
            mapper.registerWriter(User.class, new SafeUserWriter());
        } else {
            // 开发环境：完整信息
            mapper.registerWriter(User.class, new FullUserWriter());
        }
    }
}
```

### 调试模块

```java
public class DebugModule implements ObjectWriterModule {
    private final boolean debug;

    public DebugModule(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void init(ObjectMapper mapper) {
        if (debug) {
            mapper.registerWriter(Object.class, new DebugWriter());
        }
    }
}
```

---

## 模块顺序

模块按照注册顺序执行，后注册的可以覆盖前面的。

```java
ObjectMapper mapper = ObjectMapper.builder()
    .addReaderModule(new BaseModule())        // 先执行
    .addWriterModule(new BaseModule())
    .addReaderModule(new OverrideModule())     // 可能覆盖前面的
    .addWriterModule(new OverrideModule())
    .build();
```

---

## 模块依赖

### 解决依赖

```java
public class ExtendedModule implements ObjectWriterModule, ObjectReaderModule {
    @Override
    public void init(ObjectMapper mapper) {
        // 确保基础模块已注册
        if (mapper.getRegisteredWriter(Money.class) == null) {
            throw new IllegalStateException("MoneyModule required");
        }

        // 添加额外配置...
    }
}
```

### 组合模块

```java
public class CompleteModule implements ObjectWriterModule, ObjectReaderModule {
    private final List<Object> modules = new ArrayList<>();

    public CompleteModule addModule(Object module) {
        modules.add(module);
        return this;
    }

    @Override
    public void init(ObjectMapper mapper) {
        for (Object module : modules) {
            if (module instanceof ObjectWriterModule) {
                ((ObjectWriterModule) module).init(mapper);
            }
            if (module instanceof ObjectReaderModule) {
                ((ObjectReaderModule) module).init(mapper);
            }
        }
    }
}

// 使用
ObjectMapper mapper = ObjectMapper.builder()
    .addReaderModule(new CompleteModule()
        .addModule(new JavaTimeModule())
        .addModule(new MoneyModule())
    )
    .addWriterModule(new CompleteModule()
        .addModule(new JavaTimeModule())
        .addModule(new MoneyModule())
    )
    .build();
```

---

## 完整示例

### 定义业务模块

```java
public class BusinessModule implements ObjectWriterModule, ObjectReaderModule {
    @Override
    public void init(ObjectMapper mapper) {
        // Money 类型
        mapper.registerWriter(Money.class, new MoneyWriter());
        mapper.registerReader(Money.class, new MoneyReader());

        // PhoneNumber 类型
        mapper.registerWriter(PhoneNumber.class, new PhoneNumberWriter());
        mapper.registerReader(PhoneNumber.class, new PhoneNumberReader());

        // Address 类型
        mapper.registerWriter(Address.class, new AddressWriter());
        mapper.registerReader(Address.class, new AddressReader());
    }
}
```

### 应用配置

```java
@Configuration
public class JsonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapper.builder()
            // 基础配置
            .enableRead(ReadFeature.SupportSmartMatch)
            .enableWrite(WriteFeature.PrettyFormat)

            // 注册模块
            .addReaderModule(new JavaTimeModule())
            .addWriterModule(new JavaTimeModule())
            .addReaderModule(new BusinessModule())
            .addWriterModule(new BusinessModule())

            // AUTO provider 已是默认，JVM 上自动走 ASM 路径，无需手动指定

            .build();
    }
}
```

---

## 最佳实践

1. **模块职责单一** - 每个模块只处理一类类型
2. **可组合** - 模块之间应该可以自由组合
3. **无状态** - 模块应该是无状态的，可以安全共享
4. **文档化** - 为模块提供清晰的文档说明

## 相关文档

- [📖 Spring Boot 集成 →](../guides/spring-boot.md)
- [🔧 自定义序列化 →](custom-serializer.md)
- [🔧 Mixin 配置 →](mixin.md)

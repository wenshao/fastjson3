# 自定义序列化器

当默认的序列化行为不能满足需求时，可以实现自定义序列化器。

## ObjectWriter - 序列化器

### 基本实现

```java
public class MoneyWriter implements ObjectWriter<Money> {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        Money money = (Money) object;
        // 自定义序列化逻辑
        gen.writeString(money.toString());  // "$100.00"
    }
}

// 注册
ObjectMapper mapper = ObjectMapper.builder()
    .registerWriter(Money.class, new MoneyWriter())
    .build();
```

### 复杂序列化

```java
public class UserWriter implements ObjectWriter<User> {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        User user = (User) object;

        try {
            gen.startObject();

            // 自定义字段名
            gen.writeName("user_id");
            gen.writeInt64(user.getId());

            // 条件输出
            if (user.getEmail() != null) {
                gen.writeName("email");
                gen.writeString(user.getEmail());
            }

            // 自定义格式
            gen.writeName("full_name");
            gen.writeString(user.getFirstName() + " " + user.getLastName());

            gen.endObject();
        } catch (Exception e) {
            throw new JSONException("Write error", e);
        }
    }
}
```

### 使用注解注册

```java
public class Product {
    @JSONField(serializeUsing = MoneyWriter.class)
    private Money price;
}
```

---

## ObjectReader - 反序列化器

### 基本实现

```java
public class MoneyReader implements ObjectReader<Money> {
    @Override
    public Money readObject(JSONParser parser, Type type, Object fieldName, long features) {
        String value = parser.readString();
        return Money.parse(value);  // 解析 "$100.00"
    }
}

// 注册
ObjectMapper mapper = ObjectMapper.builder()
    .registerReader(Money.class, new MoneyReader())
    .build();
```

### 复杂反序列化

```java
public class UserReader implements ObjectReader<User> {
    @Override
    public User readObject(JSONParser parser, Type type, Object fieldName, long features) {
        try {
            User user = new User();

            // 读取对象开始
            parser.expect('{');

            // 逐字段读取
            for (;;) {
                if (parser.getCurrentToken() == JSONToken.RBRACE) {
                    break;
                }

                // 读取字段名
                String key = parser.readFieldName();

                // 根据字段名处理
                switch (key) {
                    case "user_id" -> user.setId(parser.readIntValue());
                    case "full_name" -> {
                        String[] parts = parser.readString().split(" ");
                        user.setFirstName(parts[0]);
                        user.setLastName(parts[1]);
                    }
                    default -> parser.skipValue();  // 跳过未知字段
                }
            }

            return user;
        } catch (Exception e) {
            throw new JSONException("Read error", e);
        }
    }
}
```

---

## 使用 Java 8+ Lambda

### 简化序列化器

```java
ObjectMapper mapper = ObjectMapper.builder()
    .registerWriter(Money.class, (gen, obj, fieldName, fieldType, features) -> {
        Money money = (Money) obj;
        gen.writeString(money.toString());
    })
    .build();
```

### 简化反序列化器

```java
ObjectMapper mapper = ObjectMapper.builder()
    .registerReader(LocalDate.class, (parser, type, fieldName, features) -> {
        String str = parser.readString();
        return LocalDate.parse(str);
    })
    .build();
```

---

## 处理泛型

### 泛型序列化器

```java
public class ResponseWriter implements ObjectWriter<Response<?>> {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        Response<?> response = (Response<?>) object;
        gen.startObject();
        gen.writeName("code");
        gen.writeInt32(response.getCode());
        gen.writeName("message");
        gen.writeString(response.getMessage());

        // 处理泛型数据
        if (response.getData() != null) {
            gen.writeName("data");
            // 需要根据实际类型序列化
            gen.writeAny(response.getData());
        }

        gen.endObject();
    }
}
```

### 泛型反序列化器

```java
public class ListReader implements ObjectReader<List<?>> {
    @Override
    public List<?> readObject(JSONParser parser, Type type, Object fieldName, long features) {
        // 获取元素类型
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type elementType = pType.getActualTypeArguments()[0];
            // 根据元素类型读取...
        }
        // ...
    }
}
```

---

## 条件序列化

### 基于权限

```java
public class SecureUserWriter implements ObjectWriter<User> {
    private final boolean includeSensitive;

    public SecureUserWriter(boolean includeSensitive) {
        this.includeSensitive = includeSensitive;
    }

    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        User user = (User) object;
        gen.startObject();

        gen.writeName("name");
        gen.writeString(user.getName());

        // 根据权限决定是否输出
        if (includeSensitive) {
            gen.writeName("ssn");
            gen.writeString(user.getSsn());
        }

        gen.endObject();
    }
}
```

### 基于环境

```java
public class EnvironmentAwareWriter implements ObjectWriter<Config> {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        Config config = (Config) object;
        gen.startObject();

        // 生产环境不输出敏感信息
        if ("prod".equals(System.getProperty("env"))) {
            gen.writeName("apiKey");
            gen.writeString("***");
        } else {
            gen.writeName("apiKey");
            gen.writeString(config.getApiKey());
        }

        gen.endObject();
    }
}
```

---

## 完整示例

### 自定义日期序列化

```java
public class CustomDateSerializer implements ObjectWriter<Date> {
    private final String pattern;

    public CustomDateSerializer(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        Date date = (Date) object;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        gen.writeString(sdf.format(date));
    }
}

// 注册
ObjectMapper mapper = ObjectMapper.builder()
    .registerWriter(Date.class, new CustomDateSerializer("yyyy-MM-dd'T'HH:mm:ssXXX"))
    .build();
```

### 自定义枚举序列化

```java
public class LowerCaseEnumWriter implements ObjectWriter<Enum<?>> {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        Enum<?> enumValue = (Enum<?>) object;
        gen.writeString(enumValue.name().toLowerCase());
    }
}

// 注册所有枚举
ObjectMapper mapper = ObjectMapper.builder()
    .registerWriter(Enum.class, new LowerCaseEnumWriter())
    .build();
```

---

## 性能考虑

### 避免反射

```java
// ❌ 不好：使用反射
public class ReflectiveWriter implements ObjectWriter<Object> {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        for (Field field : object.getClass().getDeclaredFields()) {
            // 反射获取值...
        }
    }
}

// ✅ 好：直接访问
public class UserWriter implements ObjectWriter<User> {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        User user = (User) object;
        gen.writeName("name");
        gen.writeString(user.name);  // 直接字段访问
    }
}
```

### 缓存反射结果

```java
public class CachedWriter implements ObjectWriter<User> {
    private static final Field NAME_FIELD;

    static {
        try {
            NAME_FIELD = User.class.getDeclaredField("name");
            NAME_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        try {
            User user = (User) object;
            gen.writeName("name");
            gen.writeString((String) NAME_FIELD.get(user));
        } catch (IllegalAccessException e) {
            throw new JSONException(e);
        }
    }
}
```

---

## 调试技巧

### 日志输出

```java
public class DebugWriter implements ObjectWriter<Object> {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        System.out.println("Serializing: " + object.getClass().getName());
        System.out.println("Value: " + object);
        // 正常序列化...
    }
}
```

### 验证数据

```java
public class ValidatingWriter implements ObjectWriter<User> {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        User user = (User) object;

        // 验证
        if (user.getName() == null) {
            throw new JSONException("Name cannot be null");
        }

        // 继续序列化...
    }
}
```

## 相关文档

- [📖 POJO 序列化 →](../guides/pojo.md)
- [📖 高级主题 →](.)
- [📋 注解参考 →](../api/annotations.md)

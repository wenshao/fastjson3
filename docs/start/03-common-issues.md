# 新手常见问题

> 📚 本系列教程：
> - [← 上一节：序列化基础](02-basic-serialize.md)

## 解析问题

### JSON 字段名与 Java 字段名不匹配

```java
// JSON: {"user_name":"张三"}
// Java: private String userName;

// 方案1: 智能匹配（默认）
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.SupportSmartMatch)
    .build();

// 方案2: 注解指定
@JSONField(name = "user_name")
private String userName;
```

### 日期解析失败

```java
// 默认格式可能不匹配
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;

// 或使用 Java Time API
@JSONField(format = "yyyy-MM-dd")
private LocalDate date;
```

### 泛型解析错误

```java
// ❌ 错误
List<User> users = JSON.parseArray(json, List.class);

// ✅ 正确
List<User> users = JSON.parseArray(json, User.class);

// ✅ 复杂泛型
TypeToken<Map<String, List<User>>> type =
    new TypeToken<Map<String, List<User>>>() {};
Map<String, List<User>> map = JSON.parseObject(json, type);
```

## 序列化问题

### 不想要某些字段

```java
// 单个字段
@JSONField(serialize = false)
private String password;

// 多个字段
@JSONType(ignores = {"password", "salt", "internalId"})
public class User {
    private String password;
    private String salt;
}
```

### null 值不输出

```java
// 默认行为：null 字段不输出

// 需要输出 null
String json = JSON.toJSONString(obj, WriteFeature.WriteNulls);
```

### 日期格式不对

```java
@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private Date createTime;

// 或使用 Timestamp
@JSONField(format = "millis")
private Date createTime;
```

### 枚举输出是数字

```java
// 默认输出名称
// {"status":"ACTIVE"}

// 输出序号
String json = JSON.toJSONString(obj,
    WriteFeature.WriteEnumUsingOrdinal);

// 推荐：使用 name()
```

## 性能问题

### 大数据量处理慢

```java
// ❌ 慢：使用 String
String json = mapper.writeValueAsString(largeObj);

// ✅ 快：使用 byte[]
byte[] json = mapper.writeValueAsBytes(largeObj);
```

### 频繁创建 ObjectMapper

```java
// ❌ 不好：fastjson3 不支持 new ObjectMapper()
// 以下代码无法编译，仅作反面示例
public String toJSON(Object obj) {
    ObjectMapper mapper = new ObjectMapper();  // 编译错误！
    return mapper.writeValueAsString(obj);
}

// ✅ 好：使用共享实例
private static final ObjectMapper MAPPER = ObjectMapper.shared();

public String toJSON(Object obj) {
    return MAPPER.writeValueAsString(obj);
}
```

### 循环引用报错

```java
// 使用引用检测
@JSONType(ignores = {"parent"})  // 忽略循环字段
public class TreeNode {
    private String name;
    private TreeNode parent;
}

// 或使用 @JSONField(serialize = false)
```

## 配置问题

### 不允许注释/单引号

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(
        ReadFeature.AllowComments,      // 允许注释
        ReadFeature.AllowSingleQuotes,  // 允许单引号
        ReadFeature.AllowUnquotedFieldNames  // 允许无引号字段
    )
    .build();
```

### 严格模式：未知字段报错

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.ErrorOnUnknownProperties)
    .build();
```

## 其他问题

### 中文乱码

```java
// 使用字节数组并指定编码
byte[] bytes = JSON.toJSONBytes(obj);
String json = new String(bytes, StandardCharsets.UTF_8);

// 或直接写字节流
mapper.writeValue(new FileOutputStream("out.json"), obj);
```

### 性能优化

```java
// 启用 ASM（生产环境）
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreator(ObjectReaderCreatorASM::createObjectReader)
    .writerCreator(ObjectWriterCreatorASM::createObjectWriter)
    .build();
```

### Jackson 注解兼容

```java
// fastjson3 支持 Jackson 注解，无需修改代码
public class User {
    @JsonProperty("user_name")  // Jackson 注解
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")  // Jackson 注解
    private Date birthday;
}
```

## 仍需帮助？

- 📖 [查看完整 FAQ →](../faq.md)
- 📖 [最佳实践 →](../best-practices.md)
- 💻 [查看示例代码 →](../samples/basic/)

**快速提示：**
- 大多数问题通过注解 `@JSONField` 可以解决
- 性能问题：复用 ObjectMapper、使用 byte[]、启用 ASM
- 查看 [guides/](../guides/) 目录获取完整场景指南

# 5分钟上手 fastjson3

> 📚 本系列教程：
> - [← 上一节](.) | [下一节：解析基础 →](01-basic-parse.md)

## 安装

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

## 3个最常用的操作

### 1️⃣ 解析 JSON

```java
// 解析为对象
String json = "{\"name\":\"张三\",\"age\":25}";
User user = JSON.parseObject(json, User.class);

// 解析为列表
String jsonArray = "[{\"name\":\"张三\"},{\"name\":\"李四\"}]";
List<User> users = JSON.parseArray(jsonArray, User.class);
```

### 2️⃣ 生成 JSON

```java
// 对象转 JSON
User user = new User("张三", 25);
String json = JSON.toJSONString(user);
// {"name":"张三","age":25}

// 美化输出
String pretty = JSON.toJSONString(user, WriteFeature.PrettyFormat);
```

### 3️⃣ 使用 ObjectMapper（推荐）

```java
// 创建一个 mapper，重复使用
ObjectMapper mapper = ObjectMapper.shared();

// 解析
User user = mapper.readValue(json, User.class);

// 序列化
String json = mapper.writeValueAsString(user);
```

## 常用注解

```java
public class User {
    @JSONField(name = "user_name")  // 字段重命名
    private String userName;

    @JSONField(format = "yyyy-MM-dd")  // 日期格式
    private LocalDate birthday;

    @JSONField(serialize = false)  // 不序列化
    private String password;
}
```

## 下一步

- 📖 [解析 JSON 详细教程 →](01-basic-parse.md)
- 📖 [序列化详细教程 →](02-basic-serialize.md)
- ❓ [遇到问题？看常见问题 →](03-common-issues.md)

**快速提示：**
- 复用 `ObjectMapper` 实例以获得最佳性能
- 使用 `byte[]` 而非 `String` 处理大数据
- 查看 [完整示例](../samples/basic/)

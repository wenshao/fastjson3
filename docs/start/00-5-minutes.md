# 5分钟上手 fastjson3

> Java 要求: JDK 21+

> 📚 本系列教程：
> - [← 上一节](.) | [下一节：解析基础 →](01-basic-parse.md)

## 安装

**Maven**

```xml
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Gradle**

```gradle
implementation 'com.alibaba.fastjson3:fastjson3:3.0.0'
```

## 示例 POJO

以下所有示例都使用这个类：

```java
public class User {
    private String name;
    private int age;

    public User() {}
    public User(String name, int age) { this.name = name; this.age = age; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
```

## 3 种 API 风格

### 风格 1 — `JSON.parse()` / `JSON.write()`（推荐）

最简洁的 API，通过 `ParseConfig` / `WriteConfig` 预设控制行为：

```java
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.ParseConfig;
import com.alibaba.fastjson3.WriteConfig;

// 解析为对象
String json = "{\"name\":\"张三\",\"age\":25}";
User user = JSON.parse(json, User.class);

// 宽松模式解析（允许注释、单引号等）
User user2 = JSON.parse(json, User.class, ParseConfig.LENIENT);

// 解析为列表
String jsonArray = "[{\"name\":\"张三\"},{\"name\":\"李四\"}]";
List<User> users = JSON.parseList(jsonArray, User.class);

// 序列化
String output = JSON.write(user);
// {"name":"张三","age":25}

// 美化输出
String pretty = JSON.write(user, WriteConfig.PRETTY);
```

### 风格 2 — `ObjectMapper`（复杂配置）

适合需要自定义 Reader/Writer、过滤器等高级场景，实例应重复使用：

```java
import com.alibaba.fastjson3.ObjectMapper;

// 使用共享实例
ObjectMapper mapper = ObjectMapper.shared();

// 解析
User user = mapper.readValue(json, User.class);

// 序列化
String json = mapper.writeValueAsString(user);

// 需要自定义配置时，使用 builder 创建专用实例
ObjectMapper custom = ObjectMapper.builder()
    .enableRead(ReadFeature.SupportSmartMatch)
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
```

### 风格 3 — `JSON.parseObject()` / `JSON.toJSONString()`（兼容）

与 fastjson2 同名的 API，方便老用户迁移：

```java
import com.alibaba.fastjson3.JSON;

// 解析为对象
User user = JSON.parseObject(json, User.class);

// 解析为列表
List<User> users = JSON.parseArray(jsonArray, User.class);

// 序列化
String json = JSON.toJSONString(user);

// 美化输出
String pretty = JSON.toJSONString(user, WriteFeature.PrettyFormat);
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

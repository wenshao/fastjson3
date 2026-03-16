# 统一 parse/write API 设计方案 v1

## 一、当前 API 问题清单

### 1.1 方法命名不一致
```
parse()        → 自动检测类型
parseObject()  → 指定类型 (名称误导，不是只解析对象)
parseArray()   → List<T> 或 JSONArray
toJSONString() → 序列化
toJSONBytes()  → 序列化为字节
```

### 1.2 方法重载过多
- `parseObject`: 8 个重载
- `parseArray`: 3 个重载
- `toJSONString`: 3 个重载
- `toJSONBytes`: 3 个重载
- **总计**: 17+ 个方法

### 1.3 配置方式混乱
```java
// 方式1: ReadFeature 可变参数
parseObject(json, User.class, ReadFeature.AllowComments)

// 方式2: ParsePreset 枚举
parseObject(json, User.class, ParsePreset.LENIENT)

// 问题: 两种方式并存，用户不知道选哪个
```

### 1.4 缺少泛型集合的直接支持
```java
// 当前: 必须使用 TypeReference
TypeReference<List<User>> typeRef = new TypeReference<List<User>>() {};
List<User> users = JSON.parseObject(json, typeRef);

// 期望: 更简洁
List<User> users = JSON.parseList(json, User.class);
```

---

## 二、新 API 设计

### 2.1 核心方法 (统一入口)

```java
// 解析 - 返回指定类型
<T> T JSON.parse(String input, Class<T> type)
<T> T JSON.parse(String input, Class<T> type, ParseConfig config)
<T> T JSON.parse(String input, TypeToken<T> typeToken)
<T> T JSON.parse(String input, TypeToken<T> typeToken, ParseConfig config)

// 序列化
String JSON.write(Object obj)
String JSON.write(Object obj, WriteConfig config)
byte[] JSON.writeBytes(Object obj)
byte[] JSON.writeBytes(Object obj, WriteConfig config)
```

### 2.2 便捷方法

```java
// 常见类型的便捷解析
JSONObject   JSON.parseObject(String input)
JSONArray    JSON.parseArray(String input)
<T> List<T>  JSON.parseList(String input, Class<T> element)
<K,V> Map<K,V> JSON.parseMap(String input, Class<K> key, Class<V> value)
<T> Set<T>   JSON.parseSet(String input, Class<T> element)
<T> T[]       JSON.parseArray(String input, Class<T> element) // Java 数组

// 自动类型检测 (保留)
Object JSON.parseAny(String input)
Object JSON.parseAny(String input, ParseConfig config)
```

### 2.3 配置枚举

```java
enum ParseConfig {
    DEFAULT,    // 标准 JSON
    LENIENT,    // 宽松: 注释、单引号
    STRICT,     // 严格: 未知属性报错
    API         // API 模式: 严格 + 类型检查
}

enum WriteConfig {
    DEFAULT,           // 标准 JSON
    PRETTY,            // 美化
    COMPACT,           // 紧凑 (无空格)
    WITH_NULLS,        // 包含 null
    PRETTY_WITH_NULLS  // 美化 + null
}
```

### 2.4 TypeToken 系统

```java
class TypeToken<T> {
    // 基础类型
    static <T> TypeToken<T> of(Class<T> type)

    // 构建集合类型
    TypeToken<List<T>> asList()
    TypeToken<Set<T>> asSet()
    TypeToken<T[]> asArray()

    // 构建映射类型 (键必须是 String，因为 JSON 对象键是字符串)
    <V> TypeToken<Map<String, V>> asMapOf(Class<V> valueType)

    // 嵌套构建
    static <T> TypeToken<List<T>> listOf(Class<T> element)
    static <V> TypeToken<Map<String, V>> mapOf(Class<V> value)
    static <T> TypeToken<T> of(Type type)  // 用于 TypeReference
}

// 使用示例
TypeToken<List<User>> userList = TypeToken.of(User.class).asList();
TypeToken<Map<String, User>> userMap = TypeToken.of(User.class).asMapOf(User.class);

// 复杂嵌套
TypeToken<Map<String, List<User>>> complex =
    TypeToken.of(User.class).asList().asMapOf(User.class); // 问题: 如何实现?
```

---

## 三、使用示例对比

### 3.1 基础解析

```java
// 当前 API
JSONObject obj = JSON.parseObject(json);
User user = JSON.parseObject(json, User.class);
List<User> users = JSON.parseArray(json, User.class);

// 新 API
JSONObject obj = JSON.parse(json, JSONObject.class);  // 或 JSON.parseObject(json)
User user = JSON.parse(json, User.class);
List<User> users = JSON.parseList(json, User.class);
```

### 3.2 带配置的解析

```java
// 当前 API
User user = JSON.parseObject(json, User.class,
    ReadFeature.AllowComments, ReadFeature.AllowSingleQuotes);
User user = JSON.parseObject(json, User.class, ParsePreset.LENIENT);

// 新 API
User user = JSON.parse(json, User.class, ParseConfig.LENIENT);
```

### 3.3 泛型类型

```java
// 当前 API
TypeReference<List<User>> typeRef = new TypeReference<List<User>>() {};
List<User> users = JSON.parseObject(json, typeRef);

// 新 API (简单泛型)
List<User> users = JSON.parseList(json, User.class);

// 新 API (复杂泛型)
TypeToken<Map<String, List<User>>> type =
    TypeToken.mapOf(TypeToken.listOf(User.class));
Map<String, List<User>> data = JSON.parse(json, type);
```

### 3.4 序列化

```java
// 当前 API
String json = JSON.toJSONString(obj);
String json = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
String json = JSON.toJSONString(obj, WritePreset.PRETTY);

// 新 API
String json = JSON.write(obj);
String json = JSON.write(obj, WriteConfig.PRETTY);
```

---

## 四、向后兼容策略

### 4.1 保留旧方法标记为 @Deprecated

```java
@Deprecated(since = "3.0.0", forRemoval = true)
public static <T> T parseObject(String json, Class<T> type) {
    return parse(json, type);  // 委托给新 API
}
```

### 4.2 迁移工具

提供自动迁移脚本:

```bash
# 命令行工具
fastjson3-migrate --source-dir src/main/java

# 替换规则
parseObject( -> parse(, XXX.class) -> parse(, XXX.class)
toJSONString( -> write(
```

---

## 五、待解决问题

### 5.1 TypeToken 嵌套构建
```
问题: Map<String, List<User>> 如何用 TypeToken 构建?
方案A: TypeToken.of(User.class).asList().asMapOf(...)  // 链式调用
方案B: TypeToken.mapOf(TypeToken.listOf(User.class))   // 静态工厂
方案C: 保持 TypeReference 用于复杂类型
```

### 5.2 parseObject 命名
```
问题: 新 API 中是否保留 parseObject 方法?
选项A: 完全移除，统一使用 parse
选项B: 保留作为 parse(..., JSONObject.class) 的快捷方式
选项C: 保留但改名为 parseObj (更短)
```

### 5.3 输入类型统一
```
问题: byte[] 输入是否需要单独方法?
选项A: parse(byte[]) 和 parse(String) 分开
选项B: 统一为 parse(CharSequence) 方法
选项C: 保持现状，parseObject(byte[]) 重载
```

### 5.4 配置继承
```
问题: ParseConfig 能否扩展?
方案A: 枚举不可扩展
方案B: 改为 final class with static instances
方案C: 使用 Builder 模式
```

---

## 六、实施计划

1. **第一步**: 创建 TypeToken 类
2. **第二步**: 添加新 parse/write 方法 (与旧方法共存)
3. **第三步**: 添加便捷方法 (parseList, parseMap 等)
4. **第四步**: 旧方法标记 @Deprecated
5. **第五步**: 更新文档和示例

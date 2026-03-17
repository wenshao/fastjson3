# LLM 辅助迁移指南

本文档为大型语言模型（LLM）提供迁移 fastjson3 的快速参考。

---

## 快速查询

### 我在使用... → 迁移文档

| 当前使用 | 迁移文档 | Java 要求 |
|----------|----------|----------|
| Jackson 2.x | [from-jackson2.md](../migration/from-jackson2.md) | Java 8 → 17 |
| Jackson 3.x | [from-jackson3.md](../migration/from-jackson3.md) | Java 17 |
| fastjson 1.x | [from-fastjson1.md](../migration/from-fastjson1.md) | Java 6 → 17 |
| fastjson2 | [from-fastjson2.md](../migration/from-fastjson2.md) | Java 8 → 17 |
| Gson | [from-gson.md](../migration/from-gson.md) | Java 8 → 17 |
| org.json | [from-org-json.md](../migration/from-org-json.md) | Java 8 → 17 |

### API 映射表

| 需要查找... | 文档 |
|------------|------|
| 类名映射 | [API 映射表](api-mapping.md) |
| 方法映射 | [API 映射表](api-mapping.md) |
| 注解映射 | [API 映射表](api-mapping.md) |
| Feature 映射 | [API 映射表](api-mapping.md) |

### 代码模式

| 场景 | 文档 |
|------|------|
| 创建 ObjectMapper | [代码模式库](../patterns/README.md) |
| 解析 JSON | [代码模式库](../patterns/README.md) |
| 序列化对象 | [代码模式库](../patterns/README.md) |
| 泛型处理 | [代码模式库](../patterns/README.md) |
| 字段注解 | [代码模式库](../patterns/README.md) |
| 配置选项 | [代码模式库](../patterns/README.md) |

### 迁移脚本

| 需求 | 文档 |
|------|------|
| 批量替换 | [迁移脚本](scripts.md) |
| 依赖更新 | [迁移脚本](scripts.md) |
| 验证脚本 | [迁移脚本](scripts.md) |

---

## 核心迁移规则

### 规则 1：保留原注解

fastjson3 原生支持 Jackson 和 fastjson 1.x 注解，通常无需修改：

```java
// 这些注解在 fastjson3 中直接生效
@JsonProperty("user_name")
@JsonFormat(pattern = "yyyy-MM-dd")
@JsonIgnore
@JSONField(name = "user_name")  // fastjson 1.x
```

### 规则 2：使用 ObjectMapper.builder()

所有来源库迁移都应使用 builder 模式：

```java
// ✅ 正确
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();

// ❌ 错误（Jackson 2.x 风格）
ObjectMapper mapper = new ObjectMapper();
```

### 规则 3：泛型使用 TypeToken

```java
// ✅ 正确
List<User> users = JSON.parse(json, TypeToken.listOf(User.class));

// ❌ 错误（Jackson 风格）
List<User> users = mapper.readValue(json, new TypeReference<List<User>>() {});
```

### 规则 4：更新包名

```java
// Jackson → fastjson3
import com.fasterxml.jackson.* → import com.alibaba.fastjson3.*

// fastjson1 → fastjson3
import com.alibaba.fastjson.* → import com.alibaba.fastjson3.*

// Gson → fastjson3
import com.google.gson.* → import com.alibaba.fastjson3.*
```

---

## 常见代码模式

### 模式 1：解析 JSON 为对象

**所有库通用模式：**
```java
// fastjson3
User user = JSON.parseObject(jsonString, User.class);
```

### 模式 2：序列化对象为 JSON

**所有库通用模式：**
```java
// fastjson3
String json = JSON.toJSONString(obj);
```

### 模式 3：解析 JSON 为集合

```java
// fastjson3
List<User> users = JSON.parse(jsonString, TypeToken.listOf(User.class));
```

### 模式 4：格式化输出

```java
// fastjson3
String json = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
```

---

## 快速 API 参考

### 核心 API

```java
// 解析
JSON.parseObject(json, Class)
JSON.parse(json, TypeToken)
JSON.parseArray(json, Class)

// 序列化
JSON.toJSONString(obj)
JSON.toJSONString(obj, WriteFeature...)

// ObjectMapper
ObjectMapper.shared()
ObjectMapper.builder()
mapper.readValue(json, Class)
mapper.writeValueAsString(obj)
```

### TypeToken 工厂方法

```java
TypeToken.listOf(User.class)
TypeToken.setOf(User.class)
TypeToken.mapOf(User.class)
TypeToken.arrayOf(User.class)
```

### WriteFeature 常用

```java
WriteFeature.PrettyFormat
WriteFeature.WriteNulls
WriteFeature.WriteNullStringAsEmpty
WriteFeature.WriteNullListAsEmpty
WriteFeature.WriteEnumUsingToString
```

### ReadFeature 常用

```java
ReadFeature.ErrorOnUnknownProperties
ReadFeature.SupportSmartMatch
ReadFeature.AllowComment
ReadFeature.AllowSingleQuotes
```

---

## 注解速查

### 字段注解

```java
@JSONField(name = "custom_name")           // 重命名
@JSONField(serialize = false)              // 不序列化
@JSONField(deserialize = false)            // 不反序列化
@JSONField(format = "yyyy-MM-dd")           // 格式化
@JSONField(ordinal = 1)                    // 序号
@JSONField(required = true)                // 必填
@JSONField(defaultValue = "default")        // 默认值
@JSONField(alternateNames = {"a", "b"})    // 别名
@JSONField(inclusion = Inclusion.NON_NULL) // 包含策略
```

### 类注解

```java
@JSONType(naming = NamingStrategy.SnakeCase)                   // 命名策略
@JSONType(orders = {"field1", "field2"})                        // 字段顺序
@JSONType(inclusion = Inclusion.NON_NULL)                      // 类级包含策略
@JSONType(ignores = {"field1", "field2"})                        // 忽略字段
@JSONType(alphabetic = true)                                     // 字母排序
```

### 构造器注解

```java
@JSONCreator
public ClassName(@JSONField(name = "param") Type param) {
    this.param = param;
}
```

---

## 错误修复指南

### 错误：找不到类

```
错误：Cannot import com.fasterxml.jackson.databind.ObjectMapper
解决：替换为 com.alibaba.fastjson3.ObjectMapper
```

### 错误：找不到方法

```
错误：Method configure() not found
解决：使用 builder() 模式创建 ObjectMapper
```

### 错误：找不到枚举

```
错误：SerializerFeature cannot be resolved
解决：替换为 WriteFeature
```

### 错误：泛型编译错误

```
错误：TypeReference cannot be instantiated
解决：使用 TypeToken 工厂方法
```

---

## 验证清单

迁移完成后，验证以下项目：

- [ ] 项目编译成功
- [ ] 所有测试通过
- [ ] 单元测试覆盖 JSON 序列化/反序列化
- [ ] 集成测试通过
- [ ] 性能测试无明显下降
- [ ] 日志输出无异常
- [ ] 检查无残留的旧库引用

---

## 给 LLM 的提示词模板

### 基础迁移

```
我需要将以下 {来源库} 代码迁移到 fastjson3：

{代码片段}

请按照以下规则：
1. 更新导入为 com.alibaba.fastjson3.*
2. 使用 ObjectMapper.builder() 创建配置
3. 保留原注解（fastjson3 原生支持）
4. 泛型使用 TypeToken 工厂方法

参考：docs/llm/README.md
```

### 复杂迁移

```
请帮我将以下 {来源库} 项目迁移到 fastjson3：

项目信息：
- 当前库：{来源库}
- 项目规模：{规模}
- Java 版本：{版本}

迁移需求：
1. 生成完整的迁移步骤
2. 提供批量替换脚本
3. 列出需要手动修改的地方
4. 提供验证方法

参考文档：
- docs/migration/
- docs/patterns/README.md
- docs/llm/api-mapping.md
```

---

## 相关文档

- [API 映射表](api-mapping.md)
- [迁移脚本](scripts.md)
- [代码模式库](../patterns/README.md)
- [迁移指南索引](../migration/README.md)

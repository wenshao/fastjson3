# API 速查索引 | Quick Reference

> 本文件为 AI 编码助手和开发者提供单一查找入口。所有方法签名均已验证。

## 按场景查找

### 我要解析 JSON

| 场景 | 方法 | 返回类型 |
|------|------|----------|
| 解析为对象 | `JSON.parse(json, User.class)` | `T` |
| 宽松解析 | `JSON.parse(json, User.class, ParseConfig.LENIENT)` | `T` |
| 严格解析 | `JSON.parse(json, User.class, ParseConfig.STRICT)` | `T` |
| 解析为 List | `JSON.parseList(json, User.class)` | `List<T>` |
| 解析为 Set | `JSON.parseSet(json, User.class)` | `Set<T>` |
| 解析为 Map | `JSON.parseMap(json, User.class)` | `Map<String, V>` |
| 解析为数组 | `JSON.parseTypedArray(json, User.class)` | `T[]` |
| 解析为动态对象 | `JSON.parseObject(json)` | `JSONObject` |
| 解析为动态数组 | `JSON.parseArray(json)` | `JSONArray` |
| 泛型类型 | `JSON.parse(json, TypeToken.listOf(User.class))` | `T` |
| 使用 TypeReference | `JSON.parseObject(json, new TypeReference<List<User>>(){})` | `T` |
| 使用 ObjectMapper | `mapper.readValue(json, User.class)` | `T` |
| byte[] 输入 | `JSON.parse(bytes, User.class)` | `T` |

### 我要生成 JSON

| 场景 | 方法 | 返回类型 |
|------|------|----------|
| 序列化 | `JSON.write(obj)` | `String` |
| 美化输出 | `JSON.write(obj, WriteConfig.PRETTY)` | `String` |
| 包含 null | `JSON.write(obj, WriteConfig.WITH_NULLS)` | `String` |
| 转 byte[] | `JSON.toJSONBytes(obj)` | `byte[]` |
| 使用 ObjectMapper | `mapper.writeValueAsString(obj)` | `String` |
| 兼容写法 | `JSON.toJSONString(obj)` | `String` |
| 带特性 | `JSON.toJSONString(obj, WriteFeature.PrettyFormat)` | `String` |

### 我要验证 JSON

| 场景 | 方法 | 返回类型 |
|------|------|----------|
| 有效 JSON | `JSON.isValid(json)` | `boolean` |
| 是对象 | `JSON.isValidObject(json)` | `boolean` |
| 是数组 | `JSON.isValidArray(json)` | `boolean` |

### 我要查询 JSON (JSONPath)

| 场景 | 方法 |
|------|------|
| 快捷查询 | `JSON.eval(json, "$.name", String.class)` |
| 编译路径 | `JSONPath.of("$.store.book[*]")` |
| 执行查询 | `path.eval(root, String.class)` |
| 流式提取 | `path.extract(json, String.class)` |

### 我要修改 JSON (Pointer/Patch)

| 场景 | 方法 |
|------|------|
| 定位值 | `JSONPointer.of("/a/b").eval(doc)` |
| 设置值 | `JSONPointer.of("/a/b").set(doc, value)` |
| 删除值 | `JSONPointer.of("/a/b").remove(doc)` |
| 批量修改 | `JSONPatch.apply(target, patch)` |
| 合并补丁 | `JSON.mergePatch(target, patch)` |

## ParseConfig 预设

| 预设 | 包含的 ReadFeature | 适用场景 |
|------|-------------------|----------|
| `DEFAULT` | (无) | 标准 JSON |
| `LENIENT` | AllowComments, AllowSingleQuotes, AllowUnquotedFieldNames, SupportSmartMatch | 配置文件 |
| `STRICT` | ErrorOnUnknownProperties, ErrorOnNullForPrimitives | API 接口 |
| `API` | STRICT + UseBigDecimalForDoubles | 金融精度 |

## WriteConfig 预设

| 预设 | 包含的 WriteFeature | 适用场景 |
|------|-------------------|----------|
| `DEFAULT` | (无) | 标准输出 |
| `PRETTY` | PrettyFormat | 调试日志 |
| `WITH_NULLS` | WriteNulls | 完整字段 |
| `PRETTY_WITH_NULLS` | PrettyFormat, WriteNulls | 调试+完整 |

## 注解速查

| 注解 | 用途 | 示例 |
|------|------|------|
| `@JSONField(name="x")` | 重命名 | `@JSONField(name="user_name")` |
| `@JSONField(format="...")` | 日期格式 | `@JSONField(format="yyyy-MM-dd")` |
| `@JSONField(serialize=false)` | 不序列化 | 密码、内部字段 |
| `@JSONField(ordinal=1)` | 排序 | 控制输出顺序 |
| `@JSONField(alternateNames={"a","b"})` | 多名称 | 兼容多种字段名 |
| `@JSONField(serializeUsing=X.class)` | 自定义序列化 | 特殊格式 |
| `@JSONField(value=true)` | 整体值 | 枚举单值 |
| `@JSONType(naming=SnakeCase)` | 命名策略 | 类级驼峰转蛇形 |
| `@JSONType(ignores={"a","b"})` | 忽略字段 | 类级排除 |
| `@JSONType(seeAlso={A.class})` | 多态 | 子类型 |
| `@JSONCreator` | 构造方法 | 不可变对象 |

## ObjectMapper.builder() 方法

| 方法 | 用途 |
|------|------|
| `enableRead(ReadFeature...)` | 启用解析特性 |
| `disableRead(ReadFeature...)` | 禁用解析特性 |
| `enableWrite(WriteFeature...)` | 启用序列化特性 |
| `disableWrite(WriteFeature...)` | 禁用序列化特性 |
| `addReaderModule(module)` | 添加反序列化模块 |
| `addWriterModule(module)` | 添加序列化模块 |
| `addPropertyFilter(filter)` | 属性过滤器 |
| `addValueFilter(filter)` | 值过滤器 |
| `addNameFilter(filter)` | 名称过滤器 |
| `addMixIn(target, mixin)` | Mixin 注解 |
| `useJacksonAnnotation(bool)` | Jackson 注解支持 |

## 该用哪个 API？

```
解析 JSON
├── 简单场景 → JSON.parse(json, Type.class)
├── 需要宽松/严格 → JSON.parse(json, Type.class, ParseConfig.LENIENT)
├── 复杂配置 → ObjectMapper.builder()...build()
├── 兼容 fastjson2 → JSON.parseObject(json, Type.class)
└── 集合类型 → JSON.parseList() / parseSet() / parseMap()

序列化 JSON
├── 简单场景 → JSON.write(obj)
├── 美化/null → JSON.write(obj, WriteConfig.PRETTY)
├── 复杂配置 → ObjectMapper.builder()...build()
├── 兼容 fastjson2 → JSON.toJSONString(obj)
└── 高性能 → JSON.toJSONBytes(obj) (byte[] 更快)
```

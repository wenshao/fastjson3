# 特性枚举参考

fastjson3 使用特性枚举来控制解析和序列化行为。

## ReadFeature - 解析特性

### 常用特性

| 特性 | 默认值 | 用途 |
|------|--------|------|
| `SupportSmartMatch` | false | 智能字段名匹配（忽略大小写、下划线） |
| `AllowSingleQuotes` | false | 允许单引号字符串 |
| `AllowUnquotedFieldNames` | false | 允许无引号字段名 |
| `AllowComments` | false | 允许 `//` 和 `/* */` 注释 |
| `ErrorOnUnknownProperties` | false | 遇到未知属性时报错 |
| `SupportAutoType` | false | 支持多态类型自动检测 |
| `FieldBased` | false | 使用字段访问而非 getter/setter |

### 完整列表

```java
public enum ReadFeature {
    FieldBased,                // 使用字段访问而非 getter/setter
    AllowSingleQuotes,         // 允许单引号字符串
    AllowUnquotedFieldNames,   // 允许无引号字段名
    AllowComments,             // 允许 // 和 /* */ 注释
    UseBigDecimalForFloats,    // 浮点数使用 BigDecimal
    UseBigDecimalForDoubles,   // double 使用 BigDecimal
    TrimString,                // 去除字符串两端空白
    ErrorOnUnknownProperties,  // 遇到未知属性时报错
    ErrorOnNullForPrimitives,  // null 赋值给基本类型时报错
    SupportSmartMatch,         // 智能字段名匹配（忽略大小写、下划线）
    SupportAutoType,           // 支持多态类型自动检测
    InitStringFieldAsEmpty,    // 字符串字段初始化为 "" 而非 null
    NullOnError,               // 解析错误返回 null 而非抛异常
    SupportArrayToBean,        // 支持 JSON 数组映射到 Java Bean
    EmptyStringAsNull,         // 空字符串视为 null
    DuplicateKeyValueAsArray,  // 重复 key 合并为数组
    Base64StringAsByteArray,   // Base64 字符串解码为 byte[]
}
```

## WriteFeature - 序列化特性

### 常用特性

| 特性 | 默认值 | 用途 |
|------|--------|------|
| `PrettyFormat` | false | 美化格式输出（缩进） |
| `WriteNulls` | false | 输出 null 值字段 |
| `WriteEnumsUsingName` | false | 枚举使用 name() 而非序数 |
| `WriteLongAsString` | false | Long 转为字符串（JS 兼容） |
| `BrowserCompatible` | false | 浏览器兼容模式 |
| `SortMapEntriesByKeys` | false | 按 Key 排序 Map 条目 |
| `OptimizedForAscii` | false | 针对 ASCII 内容优化 |

### 完整列表

```java
public enum WriteFeature {
    FieldBased,                // 使用字段访问而非 getter/setter
    PrettyFormat,              // 美化格式输出
    WriteNulls,                // 输出 null 值字段
    WriteNullListAsEmpty,      // null 列表输出为 []
    WriteNullStringAsEmpty,    // null 字符串输出为 ""
    WriteNullNumberAsZero,     // null 数字输出为 0
    WriteNullBooleanAsFalse,   // null 布尔输出为 false
    WriteEnumsUsingName,       // 枚举使用 name()
    WriteEnumUsingToString,    // 枚举使用 toString()
    WriteClassName,            // 输出类名（多态）
    SortMapEntriesByKeys,      // 按 Key 排序 Map
    EscapeNoneAscii,           // 转义非 ASCII 字符
    WriteBigDecimalAsPlain,    // BigDecimal 不使用科学计数法
    WriteLongAsString,         // long 转字符串（JS 兼容）
    WriteByteArrayAsBase64,    // byte[] 输出为 Base64
    BeanToArray,               // Bean 序列化为数组
    ReferenceDetection,        // 检测循环引用
    BrowserCompatible,         // 浏览器兼容（转义特殊字符）
    WriteNonStringValueAsString, // 非字符串值转为字符串
    OptimizedForAscii,         // 针对 ASCII 内容优化
}
```

## 使用示例

### 启用多个特性

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(
        ReadFeature.AllowComments,
        ReadFeature.AllowSingleQuotes,
        ReadFeature.SupportSmartMatch
    )
    .enableWrite(
        WriteFeature.PrettyFormat,
        WriteFeature.WriteNulls
    )
    .build();
```

### 静态方法中使用

```java
String json = JSON.toJSONString(obj,
    WriteFeature.PrettyFormat,
    WriteFeature.WriteNulls
);
```

## 语义化预设

推荐使用 `ParseConfig` 和 `WriteConfig` 预设，而非手动组合特性：

### ParseConfig

| 预设 | 包含的特性 | 适用场景 |
|------|-----------|----------|
| `DEFAULT` | 无额外特性 | 标准 JSON 解析 |
| `LENIENT` | AllowComments, AllowSingleQuotes, AllowUnquotedFieldNames, SupportSmartMatch | 配置文件 |
| `STRICT` | ErrorOnUnknownProperties, ErrorOnNullForPrimitives | API 接口 |
| `API` | STRICT + UseBigDecimalForDoubles | 金融/精度场景 |

```java
// 宽松解析（配置文件）
User config = JSON.parse(configJson, User.class, ParseConfig.LENIENT);

// 严格解析（API）
User user = JSON.parse(apiJson, User.class, ParseConfig.STRICT);
```

### WriteConfig

| 预设 | 包含的特性 | 适用场景 |
|------|-----------|----------|
| `DEFAULT` | 无额外特性 | 标准输出 |
| `PRETTY` | PrettyFormat | 调试/日志 |
| `WITH_NULLS` | WriteNulls | 需要完整字段 |
| `PRETTY_WITH_NULLS` | PrettyFormat + WriteNulls | 调试 + 完整字段 |

```java
// 美化输出
String json = JSON.write(obj, WriteConfig.PRETTY);

// 包含 null
String json = JSON.write(obj, WriteConfig.WITH_NULLS);
```

## 特性冲突

某些特性组合可能产生冲突：

```java
// 冲突：WriteEnumsUsingName 和 WriteEnumUsingToString
// 后设置的会覆盖前面的

// 正确做法：只设置一个
.enableWrite(WriteFeature.WriteEnumUsingToString)  // 或 WriteEnumsUsingName
```

## 性能影响

| 特性 | 性能影响 | 说明 |
|------|----------|------|
| `SupportSmartMatch` | 轻微 | 需要额外匹配逻辑 |
| `PrettyFormat` | 中等 | 需要格式化处理 |
| `BrowserCompatible` | 中等 | 需要额外转义 |
| `WriteNulls` | 轻微 | 增加输出大小 |
| `OptimizedForAscii` | 提升 | ASCII 内容更快 |

## 相关文档

- [📖 性能调优指南 →](../guides/performance.md)
- [📖 ObjectMapper 参考 →](ObjectMapper.md)
- [📖 JSON 类参考 →](JSON.md)

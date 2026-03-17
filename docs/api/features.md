# 特性枚举参考

fastjson3 使用特性枚举来控制解析和序列化行为。

## ReadFeature - 解析特性

### 常用特性

| 特性 | 默认值 | 用途 |
|------|--------|------|
| `SupportSmartMatch` | true | 智能字段名匹配（忽略大小写、下划线） |
| `AllowSingleQuotes` | false | 允许单引号字符串 |
| `AllowUnquotedFieldNames` | false | 允许无引号字段名 |
| `AllowComments` | false | 允许 `//` 和 `/* */` 注释 |
| `ErrorOnUnknownProperties` | false | 遇到未知属性时报错 |
| `SupportAutoType` | false | 支持多态类型自动检测 |
| `FieldBased` | false | 使用字段访问而非 getter/setter |

### 完整列表

```java
public enum ReadFeature {
    // 字段访问
    FieldBased,

    // 类型支持
    SupportArrayToBean,
    SupportAutoType,
    SupportClassForName,
    SupportJacksonAnnotation,  // 支持 Jackson 注解

    // 字段名匹配
    SupportSmartMatch,
    SupportSmartMatchPrefix,
    IgnoreCase,

    // 宽松格式
    AllowSingleQuotes,
    AllowUnquotedFieldNames,
    AllowComments,
    AllowNonQuotes,

    // 错误处理
    ErrorOnUnknownProperties,
    ErrorOnNotMatchForEnum,
    IgnoreError,
    UnSafe,

    // null 处理
    EmptyStringAsNull,
    IgnoreSetNullValueForBean,

    // 字符串处理
    TrimString,
    TrimStringValue,
    InitStringFieldAsEmpty,

    // 集合处理
    InitCollectionFieldAsEmpty,
    NotReadEmptyArray,

    // 其他
    IgnoreAutoTypeNotMatch,
    IgnoreAutoTypeForNotExistsClass,
    // ... 更多特性
}
```

## WriteFeature - 序列化特性

### 常用特性

| 特性 | 默认值 | 用途 |
|------|--------|------|
| `PrettyFormat` | false | 美化格式输出（缩进） |
| `WriteNulls` | false | 输出 null 值字段 |
| `WriteEnumUsingName` | true | 枚举使用 name() 而非序数 |
| `WriteLongAsString` | false | Long 转为字符串（JS 兼容） |
| `BrowserCompatible` | false | 浏览器兼容模式 |
| `SortMapEntriesByKeys` | false | 按 Key 排序 Map 条目 |
| `OptimizedForAscii` | false | 针对 ASCII 内容优化 |

### 完整列表

```java
public enum WriteFeature {
    // 类名
    WriteClassName,
    NotWriteArrayListClassName,
    NotWriteHashSetClassName,

    // null 处理
    WriteNulls,
    WriteNullListAsEmpty,
    WriteNullStringAsEmpty,
    WriteNullBooleanAsFalse,
    NullAsDefaultValue,

    // 枚举
    WriteEnumUsingToString,
    WriteEnumUsingOrdinal,
    WriteEnumUsingName,

    // 格式化
    PrettyFormat,
    BrowserCompatible,
    WriteNameAsUpperCase,
    WriteTrimString,

    // 数字
    WriteLongAsString,
    WriteBooleanAsNumber,
    WriteBigDecimalAsPlain,

    // 性能
    OptimizedForAscii,
    WriteByteArrayAsBase64,

    // Map
    SortMapEntriesByKeys,
    WriteMapNullOrEmptyValueType,

    // 其他
    IgnoreError,
    NotWriteDefaultValue,
    NotWriteEmptyArray,
    SkipWriteNullOrEmpty,
    // ... 更多特性
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

### 禁用特性

```java
ObjectMapper mapper = ObjectMapper.builder()
    .disableRead(ReadFeature.SupportAutoType)  // 安全考虑
    .disableRead(ReadFeature.SupportSmartMatch)
    .build();
```

### 静态方法中使用

```java
String json = JSON.toJSONString(obj,
    WriteFeature.PrettyFormat,
    WriteFeature.WriteNulls
);
```

## 特性组合预设

### 宽松解析（适合配置文件）

```java
ReadFeature[] LENIENT_READ = {
    ReadFeature.AllowComments,
    ReadFeature.AllowSingleQuotes,
    ReadFeature.AllowUnquotedFieldNames,
    ReadFeature.SupportSmartMatch
};
```

### 严格模式（适合 API）

```java
ReadFeature[] STRICT_READ = {
    ReadFeature.ErrorOnUnknownProperties
};
```

### 美化输出

```java
WriteFeature[] PRETTY_WRITE = {
    WriteFeature.PrettyFormat,
    WriteFeature.WriteNulls
};
```

### 性能优化

```java
WriteFeature[] FAST_WRITE = {
    WriteFeature.OptimizedForAscii
};
```

## 特性冲突

某些特性组合可能产生冲突：

```java
// 冲突：WriteEnumUsingName 和 WriteEnumUsingOrdinal
// 后设置的会覆盖前面的

// 正确做法：只设置一个
.enableWrite(WriteFeature.WriteEnumUsingOrdinal)  // 或 UsingName
```

## 性能影响

| 特性 | 性能影响 | 说明 |
|------|----------|------|
| `SupportSmartMatch` | 轻微 | 需要额外匹配逻辑 |
| `PrettyFormat` | 中等 | 需要格式化处理 |
| `BrowserCompatible` | 中等 | 需要额外转义 |
| `WriteNulls` | 轻微 | 增加输出大小 |
| `OptimizedForAscii` | 提升 | ASCII 内容更快 |
| ASM Creator | 提升 10-20% | 字节码生成 |

## 最佳实践

1. **生产环境** - 关闭调试特性，启用性能优化

```java
ObjectMapper mapper = ObjectMapper.builder()
    .disableRead(ReadFeature.AllowComments)  // 安全考虑
    .disableRead(ReadFeature.SupportAutoType)
    .enableWrite(WriteFeature.OptimizedForAscii)  // 性能
    .readerCreator(ObjectReaderCreatorASM::createObjectReader)  // ASM
    .build();
```

2. **开发环境** - 启用辅助特性

```java
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.AllowComments)
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
```

## 相关文档

- [📖 性能调优指南 →](../guides/performance.md)
- [📖 ObjectMapper 参考 →](ObjectMapper.md)

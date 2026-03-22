# 特性枚举参考

fastjson3 使用特性枚举来控制解析和序列化行为。每个特性对应一个 `long` 位掩码中的一位，检查开销为 O(1)。

## ReadFeature - 解析特性（共 37 个）

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
| `UseBigDecimalForDoubles` | false | double 使用 BigDecimal（金融精度场景） |
| `TrimString` | false | 去除字符串两端空白 |
| `NullOnError` | false | 解析错误返回 null 而非抛异常 |

### 完整列表

```java
public enum ReadFeature {
    // ===== 基础解析控制 =====
    FieldBased,                    // 使用字段访问而非 getter/setter
    AllowSingleQuotes,             // 允许单引号字符串
    AllowUnquotedFieldNames,       // 允许无引号字段名
    AllowComments,                 // 允许 // 和 /* */ 注释
    DisableSingleQuote,            // 禁止单引号字符串支持

    // ===== 数值类型控制 =====
    UseBigDecimalForFloats,        // 浮点数使用 BigDecimal
    UseBigDecimalForDoubles,       // double 使用 BigDecimal
    UseLongForInts,                // 整数使用 Long（而非 Integer）
    UseBigIntegerForInts,          // 整数使用 BigInteger
    UseDoubleForDecimals,          // 小数使用 double（而非 BigDecimal）
    NonErrorOnNumberOverflow,      // 数字溢出时不抛异常，静默截断

    // ===== 字符串处理 =====
    TrimString,                    // 去除字符串两端空白
    InitStringFieldAsEmpty,        // 字符串字段初始化为 "" 而非 null
    EmptyStringAsNull,             // 空字符串视为 null
    Base64StringAsByteArray,       // Base64 字符串解码为 byte[]
    DisableStringArrayUnwrapping,  // 禁止单元素字符串数组自动展开

    // ===== 错误处理 =====
    ErrorOnUnknownProperties,      // 遇到未知属性时报错
    ErrorOnNullForPrimitives,      // null 赋值给基本类型时报错
    ErrorOnEnumNotMatch,           // 枚举值不匹配时报错
    ErrorOnNotSupportAutoType,     // AutoType 遇到不支持类型时报错
    ErrorOnNoneSerializable,       // 遇到非 Serializable 类型时报错
    NullOnError,                   // 解析错误返回 null 而非抛异常

    // ===== 字段匹配与映射 =====
    SupportSmartMatch,             // 智能字段名匹配（忽略大小写、下划线）
    SupportArrayToBean,            // 支持 JSON 数组映射到 Java Bean
    DuplicateKeyValueAsArray,      // 重复 key 合并为数组
    NonStringKeyAsString,          // 非 String 类型 Map key 转为 String

    // ===== AutoType 控制 =====
    SupportAutoType,               // 支持多态类型自动检测
    SupportClassForName,           // 支持 Class.forName() 类型解析
    IgnoreAutoTypeNotMatch,        // 静默忽略 AutoType 类型不匹配

    // ===== null 值处理 =====
    IgnoreSetNullValue,            // 忽略 null 值 — JSON 值为 null 时不调用 setter
    IgnoreNullPropertyValue,       // 忽略 null 属性值 — 不调用 setter

    // ===== 布尔转换 =====
    NonZeroNumberCastToBooleanAsTrue, // 非零数字转布尔 true（0→false，其他→true）

    // ===== 杂项 =====
    UseNativeObject,               // 无类型解析时使用 HashMap/ArrayList 而非 JSONObject/JSONArray
    DisableReferenceDetect,        // 禁用引用检测（$ref 处理）
    UseDefaultConstructorAsPossible, // 尽可能使用无参构造函数
    IgnoreCheckClose,              // 跳过资源关闭检查（性能优化）
    IgnoreNoneSerializable,        // 忽略非 Serializable 类型
}
```

## WriteFeature - 序列化特性（共 45 个）

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
| `WriteClassName` | false | 输出类名（多态序列化） |
| `NotWriteDefaultValue` | false | 跳过默认值字段 |
| `NullAsDefaultValue` | false | null 输出为对应类型的默认值 |

### 完整列表

```java
public enum WriteFeature {
    // ===== 基础输出控制 =====
    FieldBased,                        // 使用字段访问而非 getter/setter
    PrettyFormat,                      // 美化格式输出
    PrettyFormatWith2Space,            // 2 空格缩进（PrettyFormat 默认）
    PrettyFormatWith4Space,            // 4 空格缩进（覆盖 2 空格）
    OptimizedForAscii,                 // 针对 ASCII 内容优化

    // ===== null 值输出 =====
    WriteNulls,                        // 输出 null 值字段
    WriteNullListAsEmpty,              // null 列表输出为 []
    WriteNullStringAsEmpty,            // null 字符串输出为 ""
    WriteNullNumberAsZero,             // null 数字输出为 0
    WriteNullBooleanAsFalse,           // null 布尔输出为 false
    NullAsDefaultValue,                // 综合：null 数字→0，null 字符串→""，null 布尔→false，null 列表→[]
    WriteMapNullValue,                 // Map 中的 null 值也输出（独立于 WriteNulls，后者控制 POJO 字段）

    // ===== 枚举输出 =====
    WriteEnumsUsingName,               // 枚举使用 name()
    WriteEnumUsingToString,            // 枚举使用 toString()
    WriteEnumUsingOrdinal,             // 枚举使用 ordinal()

    // ===== 类型名输出（多态） =====
    WriteClassName,                    // 输出 @type 类名
    NotWriteRootClassName,             // 跳过根对象的 @type
    NotWriteHashMapArrayListClassName, // 跳过 HashMap/ArrayList/LinkedHashMap 的 @type
    NotWriteSetClassName,              // 跳过 Set/HashSet/LinkedHashSet 的 @type
    NotWriteNumberClassName,           // 跳过 Number 子类（Integer、Long 等）的 @type
    WriteThrowableClassName,           // 异常对象输出 @type

    // ===== 数值输出 =====
    WriteLongAsString,                 // long 转字符串（JS 兼容）
    WriteBigDecimalAsPlain,            // BigDecimal 不使用科学计数法
    WriteNonStringValueAsString,       // 非字符串值转为字符串
    WriteBooleanAsNumber,              // boolean 输出为 0/1
    WriteFloatSpecialAsString,         // float/double NaN 和 Infinity 输出为字符串

    // ===== 字段过滤 =====
    NotWriteDefaultValue,              // 跳过默认值字段（0/false/null）
    NotWriteEmptyArray,                // 跳过空集合/数组
    IgnoreNoneSerializable,            // 跳过非 Serializable 类型的字段
    ErrorOnNoneSerializable,           // 遇到非 Serializable 类型时报错
    IgnoreErrorGetter,                 // getter 异常时视为 null 而非抛出
    IgnoreNonFieldGetter,              // 忽略没有对应字段的 getter（如 getClass()）

    // ===== 特殊格式 =====
    BeanToArray,                       // Bean 序列化为数组（按字段顺序）
    WriteByteArrayAsBase64,            // byte[] 输出为 Base64
    WriteDateAsMillis,                 // Date 输出为毫秒时间戳
    WritePairAsJavaBean,               // Map.Entry/Pair 输出为 {"key":..., "value":...}

    // ===== 引用检测 =====
    ReferenceDetection,                // 检测循环引用

    // ===== 浏览器兼容 =====
    BrowserCompatible,                 // 转义 <, >, (, )
    BrowserSecure,                     // 严格转义 <, >, (, ), &, '

    // ===== 引号控制 =====
    EscapeNoneAscii,                   // 转义非 ASCII 字符
    UseSingleQuotes,                   // 使用单引号
    UnquoteFieldName,                  // 字段名不加引号（非标准 JSON，用于 JS eval）

    // ===== Map Key 处理 =====
    SortMapEntriesByKeys,              // 按 Key 排序 Map
    WriteNonStringKeyAsString,         // 非 String 类型 Map key 转为 String

    // ===== 缓冲区 =====
    LargeObject,                       // 允许大对象序列化（缓冲区上限从默认值提高到 1GB）
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

### null 值处理组合

```java
// 方式1：分别指定各类型的 null 输出
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(
        WriteFeature.WriteNullStringAsEmpty,
        WriteFeature.WriteNullNumberAsZero,
        WriteFeature.WriteNullBooleanAsFalse,
        WriteFeature.WriteNullListAsEmpty
    )
    .build();

// 方式2：使用 NullAsDefaultValue 一次性设置
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(WriteFeature.NullAsDefaultValue)
    .build();
```

### 多态序列化控制

```java
// 输出类名但跳过常见容器类型
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(
        WriteFeature.WriteClassName,
        WriteFeature.NotWriteHashMapArrayListClassName,
        WriteFeature.NotWriteSetClassName,
        WriteFeature.NotWriteNumberClassName
    )
    .build();
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
// 冲突1：WriteEnumsUsingName 和 WriteEnumUsingToString 和 WriteEnumUsingOrdinal
// 只应设置其中一个，后设置的会覆盖前面的
.enableWrite(WriteFeature.WriteEnumUsingToString)  // 或 WriteEnumsUsingName 或 WriteEnumUsingOrdinal

// 冲突2：PrettyFormatWith2Space 和 PrettyFormatWith4Space
// 4 空格会覆盖 2 空格
.enableWrite(WriteFeature.PrettyFormat, WriteFeature.PrettyFormatWith4Space)

// 冲突3：AllowSingleQuotes 和 DisableSingleQuote
// DisableSingleQuote 优先级更高
```

## 性能影响

| 特性 | 性能影响 | 说明 |
|------|----------|------|
| `OptimizedForAscii` | **提升** | ASCII 内容更快 |
| `IgnoreCheckClose` | **提升** | 跳过关闭检查 |
| `FieldBased` | **提升** | 字段访问比反射调用 getter/setter 更快 |
| `SupportSmartMatch` | 轻微下降 | 需要额外匹配逻辑 |
| `PrettyFormat` | 中等下降 | 需要格式化处理 |
| `BrowserCompatible` | 中等下降 | 需要额外转义 |
| `BrowserSecure` | 中等下降 | 需要更多转义处理 |
| `WriteNulls` | 轻微下降 | 增加输出大小 |
| `ReferenceDetection` | 中等下降 | 需要跟踪对象引用 |
| `WriteClassName` | 轻微下降 | 增加 @type 字段 |
| `LargeObject` | 视情况 | 提高缓冲区上限，大对象场景需要 |

## 相关文档

- [性能调优指南](../guides/performance.md)
- [ObjectMapper 参考](ObjectMapper.md)
- [JSON 类参考](JSON.md)

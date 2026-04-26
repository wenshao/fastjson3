# 从 fastjson2 迁移到 fastjson3

fastjson3 与 fastjson2 **高度兼容**，大部分代码无需修改。

## fastjson2 用户注意

如果你使用的是 fastjson2，迁移到 fastjson3 会非常简单：

### 为什么迁移更简单？

| 方面 | fastjson2 | fastjson3 | 状态 |
|------|-----------|-----------|------|
| **核心 API** | `JSON.parseObject()` | `JSON.parseObject()` | 相同 |
| **注解** | `@JSONField`, `@JSONType` | `@JSONField`, `@JSONType` | 相同 |
| **Feature 枚举** | `JSONWriter.Feature`, `JSONReader.Feature` | `WriteFeature`, `ReadFeature` | 改名 |
| **JSONPath** | `JSONPath.of()` | `JSONPath.of()` | 相同 |
| **包名** | `com.alibaba.fastjson2` | `com.alibaba.fastjson3` | 需要修改 |
| **Java 版本** | Java 8+ | Java 21+ | 需要升级 |

### fastjson2 vs fastjson3 对比

| 特性 | fastjson2 | fastjson3 |
|------|-----------|-----------|
| **性能** | 高 | 更高 |
| **Jackson 风格 API** | 部分 | 完整 (`ObjectMapper`) |
| **Record 支持** | 有 | 更好 |
| **sealed class** | 部分 | 完整 |
| **Kotlin 支持** | 好 | 更好 |
| **GraalVM** | 支持 | 原生支持 |

### 迁移决策

```
是否能升级到 Java 21？
│
├─ 是 → 直接迁移到 fastjson3（推荐）
│       - 包名替换
│       - Feature 枚举重命名
│       - 增量采用新 API
│
└─ 否 → 继续使用 fastjson2
        - fastjson2 仍在维护
        - API 与 fastjson3 几乎相同
```

---

## 迁移步骤

### 1. 替换依赖

```xml
<!-- 移除 fastjson2 -->
<!--<dependency>-->
<!--    <groupId>com.alibaba.fastjson2</groupId>-->
<!--    <artifactId>fastjson2</artifactId>-->
<!--    <version>2.x.x</version>-->
<!--</dependency>-->

<!-- 添加 fastjson3 -->
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 2. 更新导入和包名

```bash
# 批量替换包名
find . -name "*.java" -type f -exec sed -i 's/com\.alibaba\.fastjson2\./com.alibaba.fastjson3./g' {} +
```

### 3. 更新 Feature 枚举

```java
// ===== fastjson2 =====
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONReader;
JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
JSON.parseObject(json, User.class, JSONReader.Feature.SupportSmartMatch);

// ===== fastjson3 =====
import com.alibaba.fastjson3.WriteFeature;
import com.alibaba.fastjson3.ReadFeature;
JSON.toJSONString(obj, WriteFeature.PrettyFormat);
JSON.parseObject(json, User.class, ReadFeature.SupportSmartMatch);
```

### 4. 更新 JSONReader/JSONWriter 使用

```java
// ===== fastjson2 =====
JSONReader reader = JSONReader.of(json);
User user = reader.read(User.class);

JSONWriter writer = JSONWriter.of(JSONWriter.Feature.PrettyFormat);
writer.writeObject(user);

// ===== fastjson3 =====
JSONParser parser = JSONParser.of(json);
User user = parser.read(User.class);

JSONGenerator generator = JSONGenerator.of(WriteFeature.PrettyFormat);
generator.writeAny(user);
```

### 5. 测试现有代码

大多数情况下，完成上述替换后，现有代码应该无需其他修改即可运行。

### 6. 可选：采用新 API

```java
// 旧方式（仍然有效）
User user = JSON.parseObject(json, User.class);

// 新方式（推荐，更灵活）
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);
```

---

## WriteFeature 对照表

| fastjson2 (JSONWriter.Feature) | fastjson3 (WriteFeature) | 状态 |
|------|------|------|
| `FieldBased` | `FieldBased` | 相同 |
| `PrettyFormat` | `PrettyFormat` | 相同 |
| `WriteNulls` | `WriteNulls` | 相同 |
| `WriteNullListAsEmpty` | `WriteNullListAsEmpty` | 相同 |
| `WriteNullStringAsEmpty` | `WriteNullStringAsEmpty` | 相同 |
| `WriteNullNumberAsZero` | `WriteNullNumberAsZero` | 相同 |
| `WriteNullBooleanAsFalse` | `WriteNullBooleanAsFalse` | 相同 |
| `WriteEnumsUsingName` | `WriteEnumsUsingName` | 相同 |
| `WriteEnumUsingToString` | `WriteEnumUsingToString` | 相同 |
| `WriteClassName` | `WriteClassName` | 相同 |
| `SortMapEntriesByKeys` | `SortMapEntriesByKeys` | 相同 |
| `EscapeNoneAscii` | `EscapeNoneAscii` | 相同 |
| `WriteBigDecimalAsPlain` | `WriteBigDecimalAsPlain` | 相同 |
| `WriteLongAsString` | `WriteLongAsString` | 相同 |
| `WriteByteArrayAsBase64` | `WriteByteArrayAsBase64` | 相同 |
| `BeanToArray` | `BeanToArray` | 相同 |
| `ReferenceDetection` | `ReferenceDetection` | 相同 |
| `BrowserCompatible` | `BrowserCompatible` | 相同 |
| `WriteNonStringValueAsString` | `WriteNonStringValueAsString` | 相同 |
| `OptimizedForAscii` | `OptimizedForAscii` | 相同 |
| `NotWriteDefaultValue` | `NotWriteDefaultValue` | 相同 |
| `NotWriteEmptyArray` | `NotWriteEmptyArray` | 相同 |
| `WriteEnumUsingOrdinal` | `WriteEnumUsingOrdinal` | 相同 |
| `WriteBooleanAsNumber` | `WriteBooleanAsNumber` | 相同 |
| `BrowserSecure` | `BrowserSecure` | 相同 |
| `NullAsDefaultValue` | `NullAsDefaultValue` | 相同 |
| `WritePairAsJavaBean` | `WritePairAsJavaBean` | 相同 |
| `WriteNonStringKeyAsString` | `WriteNonStringKeyAsString` | 相同 |
| `NotWriteRootClassName` | `NotWriteRootClassName` | 相同 |
| `NotWriteHashMapArrayListClassName` | `NotWriteHashMapArrayListClassName` | 相同 |
| `IgnoreNonFieldGetter` | `IgnoreNonFieldGetter` | 相同 |
| `NotWriteSetClassName` | `NotWriteSetClassName` | 相同 |
| `NotWriteNumberClassName` | `NotWriteNumberClassName` | 相同 |
| — | `PrettyFormatWith2Space` | 新增 |
| — | `PrettyFormatWith4Space` | 新增 |
| — | `UseSingleQuotes` | 新增 |
| — | `IgnoreNoneSerializable` | 新增 |
| — | `ErrorOnNoneSerializable` | 新增 |
| — | `IgnoreErrorGetter` | 新增 |
| — | `WriteFloatSpecialAsString` | 新增 |
| — | `WriteDateAsMillis` | 新增 |
| — | `WriteMapNullValue` | 新增 |
| — | `WriteThrowableClassName` | 新增 |
| — | `UnquoteFieldName` | 新增 |
| — | `LargeObject` | 新增 |

## ReadFeature 对照表

| fastjson2 (JSONReader.Feature) | fastjson3 (ReadFeature) | 状态 |
|------|------|------|
| `FieldBased` | `FieldBased` | 相同 |
| `AllowSingleQuotes` | `AllowSingleQuotes` | 相同 |
| `AllowUnquotedFieldNames` | `AllowUnquotedFieldNames` | 相同 |
| `AllowComments` | `AllowComments` | 相同 |
| `UseBigDecimalForFloats` | `UseBigDecimalForFloats` | 相同 |
| `UseBigDecimalForDoubles` | `UseBigDecimalForDoubles` | 相同 |
| `TrimString` | `TrimString` | 相同 |
| `ErrorOnUnknownProperties` | `ErrorOnUnknownProperties` | 相同 |
| `ErrorOnNullForPrimitives` | `ErrorOnNullForPrimitives` | 相同 |
| `SupportSmartMatch` | `SupportSmartMatch` | 相同 |
| `SupportAutoType` | `SupportAutoType` | 相同 |
| `InitStringFieldAsEmpty` | `InitStringFieldAsEmpty` | 相同 |
| `NullOnError` | `NullOnError` | 相同 |
| `SupportArrayToBean` | `SupportArrayToBean` | 相同 |
| `EmptyStringAsNull` | `EmptyStringAsNull` | 相同 |
| `DuplicateKeyValueAsArray` | `DuplicateKeyValueAsArray` | 相同 |
| `Base64StringAsByteArray` | `Base64StringAsByteArray` | 相同 |
| `ErrorOnEnumNotMatch` | `ErrorOnEnumNotMatch` | 相同 |
| `IgnoreSetNullValue` | `IgnoreSetNullValue` | 相同 |
| `UseNativeObject` | `UseNativeObject` | 相同 |
| `NonZeroNumberCastToBooleanAsTrue` | `NonZeroNumberCastToBooleanAsTrue` | 相同 |
| `DisableReferenceDetect` | `DisableReferenceDetect` | 相同 |
| `UseLongForInts` | `UseLongForInts` | 相同 |
| `NonErrorOnNumberOverflow` | `NonErrorOnNumberOverflow` | 相同 |
| `SupportClassForName` | `SupportClassForName` | 相同 |
| `UseDefaultConstructorAsPossible` | `UseDefaultConstructorAsPossible` | 相同 |
| `ErrorOnNotSupportAutoType` | `ErrorOnNotSupportAutoType` | 相同 |
| `IgnoreAutoTypeNotMatch` | `IgnoreAutoTypeNotMatch` | 相同 |
| — | `IgnoreNullPropertyValue` | 新增 |
| — | `IgnoreCheckClose` | 新增 |
| — | `UseBigIntegerForInts` | 新增 |
| — | `UseDoubleForDecimals` | 新增 |
| — | `DisableSingleQuote` | 新增 |
| — | `DisableStringArrayUnwrapping` | 新增 |
| — | `IgnoreNoneSerializable` | 新增 |
| — | `ErrorOnNoneSerializable` | 新增 |
| — | `NonStringKeyAsString` | 新增 |

## Filter 对照表

| fastjson2 | fastjson3 | 迁移说明 |
|-----------|-----------|----------|
| `NameFilter` | `NameFilter` | 接口相同，包名变更为 `com.alibaba.fastjson3.filter` |
| `ValueFilter` | `ValueFilter` | 接口相同，包名变更 |
| `PropertyFilter` | `PropertyFilter` | 接口相同，包名变更 |
| `PropertyPreFilter` | `PropertyPreFilter` | 接口相同，包名变更 |
| `BeforeFilter` | `BeforeFilter` | 接口相同，参数类型从 `JSONWriter` 改为 `JSONGenerator` |
| `AfterFilter` | `AfterFilter` | 接口相同，参数类型从 `JSONWriter` 改为 `JSONGenerator` |
| `ContextValueFilter` | `ContextValueFilter` | 接口相同，包名变更 |
| `LabelFilter` | `LabelFilter` | 接口相同，包名变更 |
| — | `AutoTypeFilter` | 新增，提供 `acceptNames()` 和 `acceptClasses()` 工厂方法 |

### BeforeFilter / AfterFilter 迁移

```java
// ===== fastjson2 =====
import com.alibaba.fastjson2.filter.BeforeFilter;
import com.alibaba.fastjson2.JSONWriter;

BeforeFilter filter = new BeforeFilter() {
    @Override
    public void writeBefore(JSONWriter writer, Object object) {
        writer.writeName("_version");
        writer.writeInt32(1);
    }
};

// ===== fastjson3 =====
import com.alibaba.fastjson3.filter.BeforeFilter;
import com.alibaba.fastjson3.JSONGenerator;

BeforeFilter filter = (generator, object) -> {
    generator.writeName("_version");
    generator.writeInt32(1);
};
```

---

## 注解属性对照表

### @JSONField 对照

| 属性 | fastjson2 | fastjson3 | 状态 |
|------|-----------|-----------|------|
| `name` | `String` | `String` | 相同 |
| `alternateNames` | `String[]` | `String[]` | 相同 |
| `format` | `String` | `String` | 相同 |
| `ordinal` | `int` | `int` | 相同 |
| `serialize` | `boolean` | `boolean` | 相同 |
| `deserialize` | `boolean` | `boolean` | 相同 |
| `defaultValue` | `String` | `String` | 相同 |
| `required` | — | `boolean` | 新增 |
| `inclusion` | — | `Inclusion` | 新增 |
| `value` | `boolean` | `boolean` | 相同 |
| `serializeUsing` | `Class<?>` | `Class<?>` | 相同 |
| `deserializeUsing` | `Class<?>` | `Class<?>` | 相同 |
| `label` | `String` | `String` | 相同 |
| `anyGetter` | — | `boolean` | 新增 |
| `anySetter` | — | `boolean` | 新增 |
| `unwrapped` | `boolean` | `boolean` | 相同 |
| `serializeFeatures` | `JSONWriter.Feature[]` | `WriteFeature[]` | 类型改名 |
| `deserializeFeatures` | `JSONReader.Feature[]` | `ReadFeature[]` | 类型改名 |
| `schema` | — | `String` | 新增 |

### @JSONType 对照

| 属性 | fastjson2 | fastjson3 | 状态 |
|------|-----------|-----------|------|
| `naming` | `NamingStrategy` | `NamingStrategy` | 相同 |
| `includes` | `String[]` | `String[]` | 相同 |
| `ignores` | `String[]` | `String[]` | 相同 |
| `orders` | `String[]` | `String[]` | 相同 |
| `alphabetic` | `boolean` | `boolean` | 相同 |
| `typeName` | `String` | `String` | 相同 |
| `typeKey` | `String` | `String` | 相同 |
| `seeAlso` | `Class<?>[]` | `Class<?>[]` | 相同 |
| `builder` | `Class<?>` | `Class<?>` | 相同 |
| `inclusion` | — | `Inclusion` | 新增 |
| `serializer` | `Class<?>` | `Class<?>` | 相同 |
| `schema` | — | `String` | 新增 |

---

## API 对照表

### JSON 类静态方法

| fastjson2 | fastjson3 | 状态 |
|-----------|-----------|------|
| `JSON.parseObject(String)` | `JSON.parseObject(String)` | 相同 |
| `JSON.parseObject(String, Class)` | `JSON.parseObject(String, Class)` | 相同 |
| `JSON.parseObject(String, Type)` | `JSON.parseObject(String, Type)` | 相同 |
| `JSON.parseObject(String, TypeReference)` | `JSON.parseObject(String, TypeReference)` | 相同 |
| `JSON.parseArray(String)` | `JSON.parseArray(String)` | 相同 |
| `JSON.parseArray(String, Class)` | `JSON.parseArray(String, Class)` | 相同 |
| `JSON.toJSONString(Object)` | `JSON.toJSONString(Object)` | 相同 |
| `JSON.toJSONString(Object, JSONWriter.Feature...)` | `JSON.toJSONString(Object, WriteFeature...)` | Feature 类型改名 |
| `JSON.toJSONBytes(Object)` | `JSON.toJSONBytes(Object)` | 相同 |
| `JSON.isValid(String)` | `JSON.isValid(String)` | 相同 |
| `JSON.isValidObject(String)` | `JSON.isValidObject(String)` | 相同 |
| `JSON.isValidArray(String)` | `JSON.isValidArray(String)` | 相同 |
| `JSON.parse(byte[])` | `JSON.parse(byte[])` | 相同 |
| `JSON.parse(InputStream)` | `JSON.parse(InputStream)` | 相同 |
| `JSON.parseObject(byte[], Type)` | `JSON.parseObject(byte[], Type)` | 相同 |
| `JSON.parseObject(String, Type, Feature...)` | `JSON.parseObject(String, Type, ReadFeature...)` | Feature 类型改名 |
| `JSON.parseObject(char[])` | `JSON.parseObject(char[])` | 相同 |
| `JSON.parseObject(URL)` | `JSON.parseObject(URL)` | 相同 |
| `JSON.parseObject(Reader)` | `JSON.parseObject(Reader)` | 相同 |
| `JSON.parseArray(byte[], Class)` | `JSON.parseArray(byte[], Class)` | 相同 |
| `JSON.parseArray(InputStream)` | `JSON.parseArray(InputStream)` | 相同 |
| `JSON.parseArray(String, Class, Feature...)` | `JSON.parseArray(String, Class, ReadFeature...)` | Feature 类型改名 |
| `JSON.toJSONString(Object, Filter, Feature...)` | `JSON.toJSONString(Object, Filter, WriteFeature...)` | Feature 类型改名 |
| `JSON.toJSONBytes(Object, Filter[], Feature...)` | `JSON.toJSONBytes(Object, Filter[], WriteFeature...)` | Feature 类型改名 |
| `JSON.writeTo(OutputStream, Object)` | `JSON.writeTo(OutputStream, Object)` | 相同 |
| `JSON.toJSON(Object)` | `JSON.toJSON(Object)` | 相同 |
| `JSON.toJavaObject(Object, Class)` | `JSON.toJavaObject(Object, Class)` | 相同 |
| `JSON.copy(Object)` | `JSON.copy(Object)` | 相同 |
| `JSON.copyTo(Object, Class)` | `JSON.copyTo(Object, Class)` | 相同 |
| `JSON.register(Type, ObjectReader)` | `JSON.register(Type, ObjectReader)` | 相同 |
| `JSON.register(Type, ObjectWriter)` | `JSON.register(Type, ObjectWriter)` | 相同 |
| `JSON.mixIn(Class, Class)` | `JSON.mixIn(Class, Class)` | 相同 |
| `JSON.config(Feature...)` | `JSON.config(ReadFeature/WriteFeature...)` | 类型改名 |
| `JSON.isEnabled(Feature)` | `JSON.isEnabled(ReadFeature/WriteFeature)` | 类型改名 |

### JSONObject 方法

| fastjson2 | fastjson3 | 状态 |
|-----------|-----------|------|
| `getString(String)` | `getString(String)` | 相同 |
| `getIntValue(String)` | `getIntValue(String)` | 相同 |
| `getLongValue(String)` | `getLongValue(String)` | 相同 |
| `getDoubleValue(String)` | `getDoubleValue(String)` | 相同 |
| `getBooleanValue(String)` | `getBooleanValue(String)` | 相同 |
| `getJSONObject(String)` | `getJSONObject(String)` | 相同 |
| `getJSONArray(String)` | `getJSONArray(String)` | 相同 |
| `toJavaObject(Class)` | `toJavaObject(Class)` | 相同 |
| — | `getObject(String, Class)` | 新增 |
| — | `getList(String, Class)` | 新增 |
| — | `getDate(String)` | 新增 |
| — | `getInstant(String)` | 新增 |
| — | `getLocalDate(String)` | 新增 |
| — | `getLocalDateTime(String)` | 新增 |
| — | `getLocalTime(String)` | 新增 |
| — | `getOffsetDateTime(String)` | 新增 |
| — | `getByte(String)` / `getByteValue(String)` | 新增 |
| — | `getShort(String)` / `getShortValue(String)` | 新增 |
| — | `getBytes(String)` | 新增 |
| — | `putObject(String)` | 新增 |
| — | `putArray(String)` | 新增 |
| — | `getByPath(String)` | 新增 |
| — | `toJavaObject(Type)` | 新增 |
| — | `toJavaObject(TypeReference)` | 新增 |

### JSONParser / JSONGenerator（API 变更）

| fastjson2 | fastjson3 | 说明 |
|-----------|-----------|------|
| `JSONReader` | `JSONParser` | 类名变更 |
| `JSONReader.of(String)` | `JSONParser.of(String)` | 工厂方法相同 |
| `JSONReader.Feature` | `ReadFeature` | 独立枚举 |
| `JSONWriter` | `JSONGenerator` | 类名变更 |
| `JSONWriter.of(...)` | `JSONGenerator.of(...)` | 工厂方法相同 |
| `JSONWriter.Feature` | `WriteFeature` | 独立枚举 |
| `writer.writeObject(obj)` | `generator.writeAny(obj)` | 方法名变更 |

### ObjectMapper（新增）

fastjson3 新增 Jackson 风格的 `ObjectMapper`：

```java
// fastjson3 推荐 API
ObjectMapper mapper = ObjectMapper.shared();    // 共享默认实例
ObjectMapper mapper = ObjectMapper.builder()    // 自定义构建
    .enableRead(ReadFeature.AllowComments)
    .enableWrite(WriteFeature.PrettyFormat)
    .build();

// 读取
User user = mapper.readValue(json, User.class);
User user = mapper.readValue(bytes, User.class);
List<User> users = mapper.readValue(json, new TypeReference<List<User>>() {});

// 写入
String json = mapper.writeValueAsString(user);
byte[] bytes = mapper.writeValueAsBytes(user);
```

### ObjectSupplier / ArraySupplier（per-mapper 等价物）

fastjson2 通过 `JSONReader.Context.setObjectSupplier` / `setArraySupplier` 在 per-context（即 per-call）层面切换 `JSONObject` / `JSONArray` 的内部 `Map` / `List` 实现。fastjson3 提供 per-mapper 等价物，配置后该 mapper 的所有未类型化解析都生效：

```java
// ===== fastjson2 =====
JSONReader.Context ctx = JSONFactory.createReadContext();
ctx.setObjectSupplier(ConcurrentHashMap::new);
ctx.setArraySupplier(LinkedList::new);
Object obj = JSONReader.of(json, ctx).readAny();

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.builder()
    .mapSupplier(ConcurrentHashMap::new)
    .listSupplier(LinkedList::new)
    .build();
Object obj = mapper.readValue("{\"a\":[1,2]}");
// 内部 innerMap 是 ConcurrentHashMap，innerList 是 LinkedList
```

也可在构造时直接传入：`new JSONObject(ConcurrentHashMap::new)` / `new JSONArray(LinkedList::new)`。

全局等价物（影响 `new JSONObject()` 与 shared mapper 默认解析路径）：`JSONObject.setMapCreator(ConcurrentHashMap::new)`。fastjson3 的 `JSONArray` 当前没有对应的 `setListCreator`，需要全局列表后备时通过 per-mapper `listSupplier` 解决。

> 生效范围：未类型化路径——`mapper.readValue(json)`（推断为 `JSONObject` / `JSONArray`）以及直接入口 `mapper.readObject(...)` / `mapper.readArray(...)`，含它们内部递归创建的子节点。所有类型化解析（`readValue(json, Bean.class)` / `readValue(json, JSONObject.class)` / `readValue(json, TypeReference<Map>())` 等）走 ObjectReader 路径，不应用 supplier。详见 [ObjectMapper#自定义-map--list-后备存储](../api/ObjectMapper.md#自定义-map--list-后备存储)。

---

## 迁移代码示例

### WriteClassName 迁移

```java
// ===== fastjson2 =====
JSON.toJSONString(obj, JSONWriter.Feature.WriteClassName);

// ===== fastjson3 =====
// 基本用法
JSON.toJSONString(obj, WriteFeature.WriteClassName);

// 精细控制：跳过常见容器类型的 @type
ObjectMapper mapper = ObjectMapper.builder()
    .enableWrite(
        WriteFeature.WriteClassName,
        WriteFeature.NotWriteHashMapArrayListClassName,
        WriteFeature.NotWriteSetClassName,
        WriteFeature.NotWriteNumberClassName,
        WriteFeature.NotWriteRootClassName  // 跳过根对象的 @type
    )
    .build();
String json = mapper.writeValueAsString(obj);
```

### Filter 迁移

```java
// ===== fastjson2 =====
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.ValueFilter;

NameFilter nameFilter = (source, name, value) -> name.toLowerCase();
ValueFilter valueFilter = (source, name, value) -> {
    if (value instanceof Date) return ((Date) value).getTime();
    return value;
};

JSON.toJSONString(obj, new SerializeFilter[]{nameFilter, valueFilter},
    JSONWriter.Feature.PrettyFormat);

// ===== fastjson3 =====
import com.alibaba.fastjson3.filter.NameFilter;
import com.alibaba.fastjson3.filter.ValueFilter;

NameFilter nameFilter = (source, name, value) -> name.toLowerCase();
ValueFilter valueFilter = (source, name, value) -> {
    if (value instanceof Date) return ((Date) value).getTime();
    return value;
};

// 推荐使用 ObjectMapper
ObjectMapper mapper = ObjectMapper.builder()
    .addNameFilter(nameFilter)
    .addValueFilter(valueFilter)
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
String json = mapper.writeValueAsString(obj);
```

### @JSONField(unwrapped) 使用

```java
// fastjson3 的 unwrapped 功能——将嵌套对象的属性展平到父对象
public class User {
    private String name;

    @JSONField(unwrapped = true)
    private Address address;  // address.street、address.city 会展平到 User 层级
}

public class Address {
    private String street;
    private String city;
}

// 序列化结果：
// {"name":"张三","street":"中关村大街","city":"北京"}
// 而非：
// {"name":"张三","address":{"street":"中关村大街","city":"北京"}}

User user = new User();
user.setName("张三");
Address addr = new Address();
addr.setStreet("中关村大街");
addr.setCity("北京");
user.setAddress(addr);

String json = JSON.toJSONString(user);
// {"name":"张三","street":"中关村大街","city":"北京"}
```

### @JSONType(serializer) 使用

```java
// 使用 @JSONType(serializer) 自定义整个类的序列化逻辑
@JSONType(serializer = MoneySerializer.class)
public class Money {
    private BigDecimal amount;
    private String currency;
}

public class MoneySerializer implements ObjectWriter<Money> {
    @Override
    public void write(JSONGenerator gen, Object object,
                      Object fieldName, Type fieldType, long features) {
        Money money = (Money) object;
        gen.writeString(money.getAmount().toPlainString() + " " + money.getCurrency());
    }
}

// 序列化结果：
Money money = new Money(new BigDecimal("99.99"), "CNY");
String json = JSON.toJSONString(money);
// "99.99 CNY"
```

### Throwable 序列化

```java
// fastjson3 新增 WriteThrowableClassName 特性
// 序列化异常对象时自动添加 @type 字段

try {
    // ... 业务代码
} catch (Exception e) {
    // 不带类名
    String json = JSON.toJSONString(e);
    // {"message":"something went wrong","stackTrace":[...]}

    // 带类名（方便反序列化恢复异常类型）
    String json = JSON.toJSONString(e, WriteFeature.WriteThrowableClassName);
    // {"@type":"java.lang.IllegalArgumentException","message":"something went wrong",...}
}
```

### anyGetter / anySetter 使用

```java
// fastjson3 新增 @JSONField(anyGetter/anySetter) 支持动态属性
public class DynamicBean {
    private String name;
    private Map<String, Object> extra = new LinkedHashMap<>();

    // 动态属性在序列化时展开到对象根层级
    @JSONField(anyGetter = true)
    public Map<String, Object> getExtra() { return extra; }

    // 反序列化时，未知属性收集到 Map
    @JSONField(anySetter = true)
    public void setExtra(String key, Object value) { extra.put(key, value); }
}

DynamicBean bean = new DynamicBean();
bean.setName("test");
bean.getExtra().put("custom1", "value1");
bean.getExtra().put("custom2", 42);

String json = JSON.toJSONString(bean);
// {"name":"test","custom1":"value1","custom2":42}
```

---

## 完整示例

```java
// ===== fastjson2 =====
String json = JSON.toJSONString(user, JSONWriter.Feature.PrettyFormat);
User parsed = JSON.parseObject(json, User.class);

// ===== fastjson3 方式1：兼容 API =====
String json = JSON.toJSONString(user, WriteFeature.PrettyFormat);
User parsed = JSON.parseObject(json, User.class);

// ===== fastjson3 方式2：推荐 API =====
ObjectMapper mapper = ObjectMapper.shared();
String json = mapper.writeValueAsString(user);
User parsed = mapper.readValue(json, User.class);
```

## 常见问题

### Q: 需要修改代码吗？

A: 主要修改包名（`fastjson2` -> `fastjson3`）和 Feature 枚举引用（`JSONWriter.Feature` -> `WriteFeature`、`JSONReader.Feature` -> `ReadFeature`）。核心 API 完全兼容。

### Q: 性能如何？

A: fastjson3 继承了 fastjson2 的高性能引擎，性能相当或更好。

### Q: 注解兼容吗？

A: 是的，`@JSONField`、`@JSONType` 等注解完全兼容。fastjson3 新增了 `required`、`inclusion`、`anyGetter`、`anySetter`、`schema` 等属性。

### Q: Kotlin 支持如何？

A: fastjson3 对 Kotlin 数据类的支持与 fastjson2 相同或更好。

### Q: BeforeFilter/AfterFilter 参数变了？

A: 是的，参数类型从 `JSONWriter` 改为 `JSONGenerator`，方法签名保持不变。

---

## 迁移检查清单

### 代码修改

- [ ] 更新 Maven/Gradle 依赖
- [ ] 批量替换包名 `fastjson2` -> `fastjson3`
- [ ] 替换 `JSONWriter.Feature` -> `WriteFeature`
- [ ] 替换 `JSONReader.Feature` -> `ReadFeature`
- [ ] 替换 `JSONReader` -> `JSONParser`（如使用）
- [ ] 替换 `JSONWriter` -> `JSONGenerator`（如使用）
- [ ] 更新 BeforeFilter/AfterFilter 中的 `JSONWriter` 参数为 `JSONGenerator`
- [ ] 运行测试验证兼容性
- [ ] （可选）采用 `ObjectMapper` 新 API

### 最小改动迁移

```bash
# 批量替换包名
find . -name "*.java" -type f -exec sed -i 's/com\.alibaba\.fastjson2\./com.alibaba.fastjson3./g' {} +

# 替换 Feature 枚举
find . -name "*.java" -type f -exec sed -i 's/JSONWriter\.Feature\./WriteFeature./g' {} +
find . -name "*.java" -type f -exec sed -i 's/JSONReader\.Feature\./ReadFeature./g' {} +
```

### 可选优化

- [ ] 采用 `ObjectMapper.shared()` 替代静态方法
- [ ] 使用新增的 `PropertyPreFilter` 替代 `PropertyFilter`（性能更好）
- [ ] 使用 `AutoTypeFilter` 替代全局 AutoType 开关
- [ ] 利用新增的 JSONObject 方法（`putObject`、`putArray`、`getByPath` 等）
- [ ] 利用 Java 21+ 新特性（Record、sealed class）

---

## 快速参考

### 包名映射

| fastjson2 | fastjson3 |
|-----------|-----------|
| `com.alibaba.fastjson2.JSON` | `com.alibaba.fastjson3.JSON` |
| `com.alibaba.fastjson2.JSONObject` | `com.alibaba.fastjson3.JSONObject` |
| `com.alibaba.fastjson2.JSONArray` | `com.alibaba.fastjson3.JSONArray` |
| `com.alibaba.fastjson2.JSONPath` | `com.alibaba.fastjson3.JSONPath` |
| `com.alibaba.fastjson2.JSONReader` | `com.alibaba.fastjson3.JSONParser` |
| `com.alibaba.fastjson2.JSONWriter` | `com.alibaba.fastjson3.JSONGenerator` |
| `com.alibaba.fastjson2.JSONReader.Feature` | `com.alibaba.fastjson3.ReadFeature` |
| `com.alibaba.fastjson2.JSONWriter.Feature` | `com.alibaba.fastjson3.WriteFeature` |
| `com.alibaba.fastjson2.annotation.JSONField` | `com.alibaba.fastjson3.annotation.JSONField` |
| `com.alibaba.fastjson2.annotation.JSONType` | `com.alibaba.fastjson3.annotation.JSONType` |
| `com.alibaba.fastjson2.filter.*` | `com.alibaba.fastjson3.filter.*` |

### 完全相同的 API

```java
// 这些 API 在 fastjson2 和 fastjson3 中完全相同（仅包名不同）
JSON.parseObject(json)
JSON.parseObject(json, User.class)
JSON.parseArray(json, User.class)
JSON.toJSONString(obj)
JSON.toJSONBytes(obj)
JSON.isValid(json)
JSONPath.of(path)
JSONObject.getString(key)
JSONObject.getIntValue(key)
JSONObject.toJavaObject(clazz)
```

## 相关文档

- [API 参考](../api/)
- [ObjectMapper 文档](../api/ObjectMapper.md)
- [特性枚举参考](../api/features.md)
- [过滤器参考](../api/filters.md)
- [JSONObject 参考](../api/JSONObject.md)

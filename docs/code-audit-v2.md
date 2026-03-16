# 统一 parse/write API 代码审计报告 v2

## 严重问题 - 必须修复

### 1. readList 性能灾难

**位置**: `ObjectMapper.readList()`

**问题代码**:
```java
} else if (item instanceof JSONObject jsonObj && objectReader != null) {
    // 问题: 先序列化为 JSON 字符串，再解析！
    String itemJson = JSON.toJSONString(jsonObj);
    try (JSONParser itemParser = JSONParser.of(itemJson, ...)) {
        list.add(objectReader.readObject(itemParser, type, null, features));
    }
}
```

**问题分析**:
1. JSONObject 已经在内存中
2. 然后序列化为 JSON 字符串 (IO 操作)
3. 再解析 JSON 字符串 (词法分析)
4. 最后转换为目标类型

**影响**: 对于每个数组元素，都会进行一次完整的序列化+解析

**正确做法**: 直接从 JSONObject 转换为目标类型

```java
} else if (item instanceof JSONObject jsonObj && objectReader != null) {
    // 直接转换，无需序列化
    // 使用 ObjectReader 直接从 JSONObject 的字段读取
    list.add(convertJsonObject(jsonObj, type, objectReader, features));
}
```

**修复优先级**: P0 (严重性能问题)

---

### 2. readMap 同样的问题

**位置**: `ObjectMapper.readMap()`

**同样的序列化-解析循环**:
```java
String itemJson = JSON.toJSONString(jsonObj);
try (JSONParser itemParser = JSONParser.of(itemJson, ...)) {
    map.put(key, objectReader.readObject(itemParser, valueType, null, features));
}
```

---

## 中等问题

### 3. parseList/parseSet/parseMap 缺少 ParseConfig 参数

**影响**: 用户无法对集合解析使用配置

**修复**: 添加重载

```java
public static <E> List<E> parseList(String json, Class<E> elementType, ParseConfig config)
public static <E> Set<E> parseSet(String json, Class<E> elementType, ParseConfig config)
public static <V> Map<String, V> parseMap(String json, Class<V> valueType, ParseConfig config)
```

---

### 4. ParseConfig.features() 每次克隆数组

**问题**: `features.clone()` 在热路径上创建新数组

**修复**: 返回不可变视图

```java
public enum ParseConfig {
    LENIENT(new ReadFeature[]{...}) {
        private final ReadFeature[] CACHED_FEATURES = features.clone();
        public ReadFeature[] features() {
            return CACHED_FEATURES;  // 返回缓存的副本
        }
    };
}
```

---

### 5. 空字符串处理

**当前**: `"".isEmpty()` 返回 null
**应该**: 抛出 `JSONException`（空字符串不是有效 JSON）

---

## 轻微问题

### 6. writeCompact 可能多余

---

### 7. parseAny 命名

建议添加 `@see` 链接到 `parse()`

---

## 修复优先级

| 优先级 | 问题 | 影响 |
|--------|------|------|
| P0 | readList/readMap 序列化-解析循环 | 严重性能问题 |
| P1 | parseList/parseSet/parseMap 缺少 ParseConfig | API 不一致 |
| P1 | 空字符串处理 | 语义错误 |
| P2 | features() 数组克隆 | 性能 |
| P3 | writeCompact 多余 | API 简洁性 |

---

## 修复方案

### P0-1: 修复 readList 性能问题

需要修改 `ObjectReader` 接口，添加直接从 JSONObject 读取的方法：

```java
public interface ObjectReader<T> {
    T readObject(JSONParser parser, Type fieldType, Object fieldName, long features);

    // 新增: 直接从 JSONObject 读取
    default T readFromJSONObject(JSONObject obj, Class<T> type, long features) {
        // 默认实现：使用现有方式
        // 子类可以覆盖以提供更高效的实现
        throw new UnsupportedOperationException();
    }
}
```

或者，使用 JSONPath 直接提取字段：

```java
// 简化方案：使用现有的字段读取逻辑
ObjectReaderImpl> reader = getObjectReader(type);
// 直接从 Map 读取，无需序列化
```

### P0-2: 临时修复方案

如果不能修改 ObjectReader 接口，可以使用以下临时方案：

```java
} else if (item instanceof JSONObject jsonObj && objectReader != null) {
    // 使用 ObjectMapper 直接转换 Map
    Map<String, Object> map = jsonObj.getInternalMap();  // 需要暴露内部 Map
    T converted = objectMapper.convertValue(map, type);
    list.add(converted);
}
```

---

## 测试验证

修复后需要验证：

1. **性能测试**: 对比修复前后的性能
2. **正确性测试**: 确保转换结果一致
3. **边界测试**: null、空数组、嵌套对象

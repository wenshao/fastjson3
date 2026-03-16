# 统一 parse/write API 代码审计 v3 - 最终修复方案

## 严重性能问题分析

### 问题根源

`ObjectReader.readObject()` 只接受 `JSONParser`，不支持直接从 `JSONObject` 读取。

当前实现：
```java
// readList 中
String itemJson = JSON.toJSONString(jsonObj);  // 序列化
try (JSONParser itemParser = JSONParser.of(itemJson, ...)) {
    list.add(objectReader.readObject(...));     // 解析
}
```

### 为什么这样设计？

1. **架构原因**: `ObjectReader` 设计为从流式解析器读取，而不是从内存中的对象
2. **代码复用**: 使用相同的解析路径，避免重复逻辑
3. **历史原因**: 继承自 fastjson2 的设计

### 性能影响

对于包含 N 个对象的数组：
- 每个对象都需要：序列化 → 解析
- 时间复杂度：O(N × (序列化 + 解析))
- 理想情况：O(N × 解析)

---

## 修复方案

### 方案 A: 添加 ObjectReader.readFromJSONObject (理想方案)

修改 `ObjectReader` 接口：

```java
public interface ObjectReader<T> {
    T readObject(JSONParser parser, Type fieldType, Object fieldName, long features);

    /**
     * Read from a JSONObject without serialization.
     * Default implementation falls back to serialization-deserialization.
     */
    default T readFromJSONObject(JSONObject jsonObj, long features) {
        // 默认：使用现有的方式（序列化-解析）
        String json = JSON.toJSONString(jsonObj);
        try (JSONParser parser = JSONParser.of(json)) {
            return readObject(parser, null, null, features);
        }
    }
}
```

**优点**:
- 向后兼容（有默认实现）
- 允许特定类型优化
- 逐步迁移

**缺点**:
- 需要修改核心接口
- 需要为每个 ObjectReader 实现优化版本

**工作量**: 中等

---

### 方案 B: 使用反射直接转换 (快速方案)

在 `ObjectMapper` 中添加新方法：

```java
@SuppressWarnings("unchecked")
private <T> T convertJsonObjectToObject(JSONObject jsonObj, Class<T> type, long features) {
    try {
        // 1. 创建目标实例
        T instance = type.getDeclaredConstructor().newInstance();

        // 2. 遍历 JSONObject 的字段
        for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            // 3. 查找对应的 Java 字段
            Field field = findField(type, fieldName);
            if (field != null) {
                field.setAccessible(true);
                // 简单类型直接设置，复杂类型递归处理
                field.set(instance, convertValue(value, field.getType()));
            }
        }
        return instance;
    } catch (Exception e) {
        throw new JSONException("Failed to convert JSONObject to " + type.getName(), e);
    }
}
```

**优点**:
- 无需修改接口
- 立即提升性能

**缺点**:
- 不处理注解 (@JSONField, @JSONType)
- 不处理自定义反序列化器
- 大量重复代码

**工作量**: 较小

---

### 方案 C: 保持现状 + 添加警告 (最简单)

在代码中添加 TODO 注释：

```java
// TODO: 优化性能 - 避免序列化-解析循环
// 当前实现: JSONObject -> JSON字符串 -> JSONParser -> POJO
// 理想实现: JSONObject -> POJO (直接字段映射)
String itemJson = JSON.toJSONString(jsonObj);
try (JSONParser itemParser = JSONParser.of(itemJson, ...)) {
    list.add(objectReader.readObject(...));
}
```

并在文档中说明：

```java
/**
 * Parse JSON string to typed list.
 * <p><b>Performance note:</b> When array elements need type conversion
 * (e.g., JSONObject to POJO), this method uses a serialize-deserialize cycle.
 * For maximum performance, consider using {@code TypeToken<List<T>>}
 * or implementing a custom {@link ObjectReader}.</p>
 */
```

---

## 推荐: 方案 C (暂时) + 方案 A (长期)

**理由**:
1. 方案 C 风险最小，不影响现有功能
2. 方案 A 是正确方向，但需要更多时间
3. 可以在 Phase 2 或 Phase 3 实施方案 A

---

## 其他问题修复

### 1. 添加 ParseConfig 参数支持

```java
// JSON.java
public static <E> List<E> parseList(String json, Class<E> elementType, ParseConfig config) {
    if (json == null || json.isEmpty()) {
        return null;
    }
    return ObjectMapper.shared().readList(json, elementType, config.mask());
}

// 同样添加 parseSet, parseMap 的重载
```

### 2. 缓存 features 数组

```java
public enum ParseConfig {
    LENIENT(new ReadFeature[]{...}) {
        private final ReadFeature[] CACHED = features.clone();
        @Override
        public ReadFeature[] features() {
            return CACHED;  // 返回缓存副本
        }
    };
    // ...
}
```

### 3. 空字符串处理

**决策**: 保持现有行为（返回 null）

**理由**:
- 一致性：所有解析方法都这样处理
- 实用性：避免空输入抛出异常
- 符合现有惯例

---

## 实施计划

### 第一阶段 (当前)

- [ ] 添加 parseList/parseSet/parseMap 的 ParseConfig 重载
- [ ] 缓存 features 数组
- [ ] 添加性能警告到 Javadoc
- [ ] 编写性能测试用例

### 第二阶段 (未来)

- [ ] 设计 ObjectReader.readFromJSONObject
- [ ] 实现常用类型的优化版本
- [ ] 性能基准测试
- [ ] 文档更新

---

## 测试用例

```java
@Test
void testParseListWithConfig() {
    String json = "[/* comment */ { name: 'John' }]";
    List<User> users = JSON.parseList(json, User.class, ParseConfig.LENIENT);
    assertEquals(1, users.size());
    assertEquals("John", users.get(0).name);
}

@Test
void testParseListPerformance() {
    // 验证性能
    String json = "[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]";

    // 预热
    for (int i = 0; i < 1000; i++) {
        JSON.parseList(json, User.class);
    }

    // 测试
    long start = System.nanoTime();
    for (int i = 0; i < 10000; i++) {
        JSON.parseList(json, User.class);
    }
    long duration = System.nanoTime() - start;

    // 验证性能在合理范围内
    assertTrue(duration < 1_000_000_000); // 1秒内完成
}
```

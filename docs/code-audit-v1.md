# 统一 parse/write API 代码审计报告

## 第一轮审计发现

### 1. API 不一致 - 缺少 ParseConfig 参数

**问题**: `parseList/parseSet/parseMap/parseTypedArray` 方法没有支持 `ParseConfig` 参数的版本

```java
// 当前: parse 不支持配置
List<User> users = JSON.parseList(json, User.class);

// 但 parse 支持
User user = JSON.parse(json, User.class, ParseConfig.LENIENT);

// 不一致: 无法对 parseList 使用 LENIENT 配置
```

**影响**: 用户无法对集合解析使用宽松配置（如配置文件场景）

**建议**: 添加支持 ParseConfig 参数的重载

---

### 2. 性能问题 - ParseConfig.features() 每次创建新数组

**问题**:
```java
public ReadFeature[] features() {
    return features.clone();  // 每次调用都克隆数组
}
```

**影响**: 热路径上频繁调用会创建不必要的数组

**建议**: 缓存不可变数组，或返回 `UnmodifiableList`

---

### 3. 空字符串处理不一致

**问题**:
```java
// parse 方法
if (json == null || json.isEmpty()) {
    return null;  // 空字符串返回 null
}

// parseAny 方法
if (json == null || json.isEmpty()) {
    return null;  // 空字符串返回 null
}

// parseObject/parseArray (旧方法)
// 应该检查它们的行为
```

**验证**: 空字符串 `""` 应该抛出异常（无效 JSON），还是返回 null？

**当前行为**: 返回 null
**建议行为**: 空字符串应该抛出异常，因为它不是有效的 JSON

---

### 4. parseAny 命名可能令人困惑

**问题**: `parseAny` 名称可能暗示解析任意类型，但实际上是自动类型检测

**选项**:
- 保留 `parseAny` (与 Jackson 的 `readValue` 类似)
- 改为 `parseAuto` (更明确)
- 添加别名 `autoParse`

**决策**: 保留 `parseAny`，添加 Javadoc 说明

---

### 5. parseTypedArray 性能问题

**问题**: 创建两次数组副本
```java
List<E> list = ObjectMapper.shared().readList(json, elementType);  // 内部创建 ArrayList
return list.toArray((E[]) Array.newInstance(elementType, 0));      // 再创建数组
```

**影响**: 对于大数组，性能较差

**建议**: 考虑直接解析到数组

---

### 6. writeCompact 方法可能是多余的

**问题**: `writeCompact` 等同于 `write`，存在意义不大

```java
public static String writeCompact(Object obj) {
    return write(obj, WriteConfig.DEFAULT);  // 完全等同于 write(obj)
}
```

**建议**: 移除或在文档中明确说明其存在原因（对称性）

---

### 7. 类型安全问题

**问题**: `parseTypedArray` 使用 `@SuppressWarnings("unchecked")`

```java
return list.toArray((E[]) java.lang.reflect.Array.newInstance(elementType, 0));
```

**分析**: 这是 Java 类型擦除的限制，无法避免

**缓解**: 在 Javadoc 中说明

---

## 第二轮审计 - 需要验证的问题

### A. ObjectMapper.readList/readSet/readMap 的实现

**问题**: 这些方法的实现可能不够高效

```java
// 当前实现
JSONArray array = parser.readArray();  // 先解析为 JSONArray
List<T> list = new ArrayList<>(array.size());  // 然后转换
for (int i = 0; i < array.size(); i++) {
    // 逐个转换
}
```

**建议**: 直接解析为目标类型，避免中间 JSONArray

---

### B. 错误消息一致性

**问题**: 需要验证所有方法抛出的异常类型和消息格式是否一致

**检查点**:
- `parse()` 抛出的异常类型
- `parseList()` 抛出的异常类型
- `parse()` vs `parseObject()` 异常行为

---

### C. 文档完整性

**问题**: 需要检查所有新方法的 Javadoc 是否

- [ ] 有清晰的参数说明
- [ ] 有返回值说明
- [ ] 有异常说明 (@throws)
- [ ] 有使用示例
- [ ] 有相关方法链接 (@see)

---

## 审计总结

### 必须修复
1. 为 parseList/parseSet/parseMap 添加 ParseConfig 参数支持

### 应该修复
2. 性能优化: 缓存 features() 返回的数组
3. 性能优化: parseTypedArray 直接解析
4. 错误处理: 空字符串应该抛出异常

### 可以延迟
5. writeCompact 方法移除或明确说明
6. parseAny 命名审查
7. ObjectMapper.readList 实现优化

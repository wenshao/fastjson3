# 从 Jackson 迁移到 fastjson3

> **注意**：本文档已拆分为更详细的版本特定指南：
>
> - [从 Jackson 2.x 迁移 →](from-jackson2.md)
> - [从 Jackson 3.x 迁移 →](from-jackson3.md)
>
> 请根据你的 Jackson 版本选择对应的文档。

---

## 快速对照

| Jackson | fastjson3 | 说明 |
|---------|-----------|------|
| `new ObjectMapper()` | `ObjectMapper.shared()` | fastjson3 不可变 |
| `@JsonProperty` | `@JSONField` 或保留 | **原生支持 Jackson 注解** |
| `TypeReference<List<T>>` | `TypeToken.listOf(T.class)` | 泛型处理 |

## 快速示例

```java
// ===== Jackson =====
ObjectMapper mapper = new ObjectMapper();
User user = mapper.readValue(json, User.class);

// ===== fastjson3 =====
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);
```

**fastjson3 原生支持 Jackson 注解，通常无需修改实体类！**

```java
public class User {
    @JsonProperty("user_name")     // Jackson 注解
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")  // Jackson 注解
    private Date birthday;

    @JsonIgnore                     // Jackson 注解
    private String password;
}
// fastjson3 直接支持上述所有注解
```

## 详细文档

请根据你的 Jackson 版本选择：

### Jackson 2.x 用户
[→ 查看完整迁移指南](from-jackson2.md)

- 完整注解映射表
- Feature 对照表
- 常用场景迁移
- Spring Boot 集成

### Jackson 3.x 用户
[→ 查看完整迁移指南](from-jackson3.md)

- ObjectMapper API 对照
- Stream API 迁移
- Record/Builder 模式
- 性能对比

## 相关文档

- [迁移检查清单](checklist.md)
- [常见问题](faq.md)
- [注解使用指南](../guides/annotations.md)

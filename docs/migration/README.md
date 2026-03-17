# 迁移指南

本文档帮助你从其他 JSON 库迁移到 fastjson3。

## 📋 快速导航

选择你的来源库：

| 来源库 | 迁移难度 | 兼容性 | 文档 |
|--------|----------|--------|------|
| fastjson 1.x | ⭐⭐ 中 | API 相似 | [from-fastjson1.md](from-fastjson1.md) |
| fastjson2 | ⭐ 低 | 高度兼容 | [from-fastjson2.md](from-fastjson2.md) |
| Jackson 2.x | ⭐⭐ 中 | 注解兼容 | [from-jackson2.md](from-jackson2.md) |
| Jackson 3.x | ⭐ 低 | API 相似 | [from-jackson3.md](from-jackson3.md) |
| Gson | ⭐⭐ 中 | 需要适应 | [from-gson.md](from-gson.md) |
| org.json | ⭐⭐ 中 | 完全重写 | [from-org-json.md](from-org-json.md) |

---

## 🔍 Jackson 2.x vs 3.x 迁移

不确定你的 Jackson 版本？

```bash
# Maven 项目检查
grep -r "jackson-databind" pom.xml

# Gradle 项目检查
grep -r "jackson-databind" build.gradle
```

| Jackson 版本 | 特点 | 迁移难度 |
|-------------|------|----------|
| **2.x** | 可变 ObjectMapper，`new ObjectMapper()` 创建 | ⭐⭐ 配置方式变化较大 |
| **3.x** | 不可变 ObjectMapper，`JsonMapper.builder()` 创建 | ⭐ API 设计与 fastjson3 相似 |

**注意**：
- Jackson 2.x 需要 Java 8+
- Jackson 3.x 需要 Java 17+
- fastjson3 需要 Java 17+

如果你使用 Jackson 2.x 且无法升级 Java，请继续使用 Jackson 2.x 或考虑 [fastjson2](from-fastjson2.md)。

---

## 🔍 fastjson 1.x vs 2.x vs 3.x 迁移

不确定你的 fastjson 版本？

```bash
# Maven 项目检查
grep -E "fastjson|fastjson2" pom.xml

# Gradle 项目检查
grep -E "fastjson|fastjson2" build.gradle
```

| fastjson 版本 | 包名 | Java 要求 | 迁移难度 |
|--------------|------|-----------|----------|
| **1.x** | `com.alibaba.fastjson` | Java 6+ | ⭐⭐ Feature 重命名，API 变化 |
| **2.x** | `com.alibaba.fastjson2` | Java 8+ | ⭐ 包名替换即可 |
| **3.x** | `com.alibaba.fastjson3` | Java 17+ | - |

### fastjson 版本对比

| 特性 | fastjson 1.x | fastjson2 | fastjson3 |
|------|-------------|-----------|-----------|
| **性能** | 基准 | ~1.5x | ~2x |
| **Java 版本** | Java 6+ | Java 8+ | Java 17+ |
| **核心 API** | `JSON.parseObject()` | `JSON.parseObject()` | `JSON.parseObject()` |
| **ObjectMapper** | ❌ | 部分 | 完整 |
| **Record 支持** | ❌ | ✅ | ✅ |
| **sealed class** | ❌ | 部分 | ✅ |
| **Jackson 注解** | ❌ | ✅ | ✅ |

### 迁移决策

```
当前使用 fastjson 1.x？
│
├─ 能升级到 Java 17？
│   ├─ 是 → 直接迁移到 fastjson3（推荐）
│   └─ 否 → 先迁移到 fastjson2（Java 8 兼容）
│
当前使用 fastjson2？
│
├─ 能升级到 Java 17？
│   ├─ 是 → 迁移到 fastjson3（包名替换即可）
│   └─ 否 → 继续使用 fastjson2
```

---

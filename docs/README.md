# fastjson3 文档索引 | Documentation Index

## 🎯 快速导航 | Quick Navigation

**我是新手** → 从 [start/00-5-minutes.md](start/00-5-minutes.md) 开始
**我要查 API** → 去 [api/](api/) 目录
**我要解决具体问题** → 看 [guides/](guides/) 目录
**我要深入优化** → 看 [advanced/](advanced/) 目录

---

## 📚 文档地图 | Document Map

### 📘 入门教程 | Getting Started

| 文档 | 描述 | 时间 |
|------|------|------|
| [00-5-minutes.md](start/00-5-minutes.md) | 5分钟快速上手 | 5 min |
| [01-basic-parse.md](start/01-basic-parse.md) | 解析 JSON 基础 | 10 min |
| [02-basic-serialize.md](start/02-basic-serialize.md) | 序列化基础 | 10 min |
| [03-common-issues.md](start/03-common-issues.md) | 新手常见问题 | 5 min |

### 📖 场景指南 | Guides

完整的场景教程，每个指南独立完整。

| 文档 | 描述 |
|------|------|
| [pojo.md](guides/pojo.md) | Java 对象与 JSON 互转完整指南 |
| [jsonpath.md](guides/jsonpath.md) | JSONPath 查询完整指南 |
| [validation.md](guides/validation.md) | JSON Schema 验证 |
| [spring-boot.md](guides/spring-boot.md) | Spring Boot 集成 |
| [performance.md](guides/performance.md) | 性能调优指南 |

### 📋 API 参考 | API Reference

快速查找，不包含教程内容。

| 类/接口 | 描述 |
|---------|------|
| [JSON.md](api/JSON.md) | 静态工具类 |
| [ObjectMapper.md](api/ObjectMapper.md) | 核心映射器 |
| [JSONObject.md](api/JSONObject.md) | 动态 JSON 对象 |
| [JSONArray.md](api/JSONArray.md) | 动态 JSON 数组 |
| [JSONPath.md](api/JSONPath.md) | JSONPath API |
| [annotations.md](api/annotations.md) | 注解参考 |
| [features.md](api/features.md) | 特性枚举 |
| [filters.md](api/filters.md) | 过滤器 API |

### 🔧 高级主题 | Advanced

| 文档 | 描述 |
|------|------|
| [custom-serializer.md](advanced/custom-serializer.md) | 自定义序列化器 |
| [modules.md](advanced/modules.md) | 模块系统 |
| [mixin.md](advanced/mixin.md) | Mixin 配置 |
| [security.md](advanced/security.md) | 安全配置 |
| [graalvm.md](advanced/graalvm.md) | GraalVM Native Image |
| [android.md](advanced/android.md) | Android 优化 |

### 🏗️ 深入原理 | Internals

架构和优化原理，适合深入理解。

| 文档 | 描述 |
|------|------|
| [architecture.md](internals/architecture.md) | 整体架构设计 |
| [optimization.md](internals/optimization.md) | 性能优化技术 |

### 📦 其他 | Others

| 文档 | 描述 |
|------|------|
| [cheat-sheet.md](cheat-sheet.md) | 速查表 - 一页纸常用代码 |
| [best-practices.md](best-practices.md) | 最佳实践 & 避坑指南 |
| [migration/](migration/) | 迁移指南 |
| [faq.md](faq.md) | 常见问题解答 |

### 💡 示例代码 | Samples

| 类别 | 描述 |
|------|------|
| [basic/](samples/basic/) | 基础示例 |
| [jsonpath/](samples/jsonpath/) | JSONPath 示例 |
| [performance/](samples/performance/) | 性能优化示例 |

**运行示例：**
```bash
cd docs/samples
./run.sh              # Linux/Mac
run.bat               # Windows
```

---

## 🔍 按功能查找 | Find by Feature

| 我想... | 去哪里 |
|---------|--------|
| 解析 JSON 字符串 | [start/01-basic-parse.md](start/01-basic-parse.md) |
| 转换为 JSON 字符串 | [start/02-basic-serialize.md](start/02-basic-serialize.md) |
| 处理日期 | [guides/pojo.md#日期处理](guides/pojo.md#日期处理) |
| 忽略字段 | [guides/pojo.md#忽略字段](guides/pojo.md#忽略字段) |
| 重命名字段 | [guides/pojo.md#jsonfield---字段级注解](guides/pojo.md#jsonfield---字段级注解) |
| 处理泛型 | [guides/pojo.md#泛型处理](guides/pojo.md#泛型处理) |
| 查询 JSON 路径 | [guides/jsonpath.md](guides/jsonpath.md) |
| 验证 JSON | [guides/validation.md](guides/validation.md) |
| 自定义序列化 | [advanced/custom-serializer.md](advanced/custom-serializer.md) |
| 提升性能 | [guides/performance.md](guides/performance.md) |
| 安全配置 | [advanced/security.md](advanced/security.md) |
| 从 fastjson2 迁移 | [migration/from-fastjson2.md](migration/from-fastjson2.md) |
| 从 Jackson 迁移 | [migration/from-jackson.md](migration/from-jackson.md) |
| 从 Gson 迁移 | [migration/from-gson.md](migration/from-gson.md) |
| 从 org.json 迁移 | [migration/from-org-json.md](migration/from-org-json.md) |

---

## 🔗 交叉引用索引 | Cross-Reference Index

### 核心类文档位置

| 类 | 入门文档 | 完整 API | 示例 |
|---|----------|----------|------|
| `JSON` | [start/00-5-minutes.md](start/00-5-minutes.md) | [api/JSON.md](api/JSON.md) | [samples/basic/](samples/basic/) |
| `ObjectMapper` | [start/02-basic-serialize.md](start/02-basic-serialize.md) | [api/ObjectMapper.md](api/ObjectMapper.md) | [samples/basic/](samples/basic/) |
| `JSONObject` | [start/01-basic-parse.md](start/01-basic-parse.md) | [api/JSONObject.md](api/JSONObject.md) | [samples/basic/](samples/basic/) |
| `JSONPath` | [guides/jsonpath.md](guides/jsonpath.md) | [api/JSONPath.md](api/JSONPath.md) | [samples/jsonpath/](samples/jsonpath/) |

### 概念文档位置

| 概念 | 入门 | 完整指南 |
|------|------|----------|
| 注解 | [start/02-basic-serialize.md](start/02-basic-serialize.md) | [api/annotations.md](api/annotations.md) |
| 特性配置 | [start/01-basic-parse.md](start/01-basic-parse.md) | [api/features.md](api/features.md) |
| 性能优化 | [guides/performance.md](guides/performance.md) | [internals/optimization.md](internals/optimization.md) |

---

## 🌐 Language / 语言

This documentation is primarily in **Chinese**. For English speakers, key APIs are language-agnostic, and code examples work the same.

English documentation contributions are welcome.

---

## 🤖 给大模型 | For LLMs

**大模型阅读本文档的建议：**

1. **先读这个文件** - 了解文档结构
2. **根据问题类型定位：**
   - API 问题 → `api/` 目录
   - 场景问题 → `guides/` 目录
   - 深入问题 → `advanced/` 或 `internals/`
3. **单一信息源** - 每个概念只在对应文档详细描述
4. **交叉链接** - 文档间有明确链接，避免重复

**文档结构说明：**
- `start/` - 新手入门，循序渐进
- `guides/` - 场景教程，每个独立完整
- `api/` - API 参考，快速查找
- `advanced/` - 高级配置和扩展
- `internals/` - 深入原理

---

*最后更新: 2026-03-22*

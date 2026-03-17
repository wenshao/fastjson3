# 整体架构设计

fastjson3 (core3) 的架构文档索引。

## 快速导航

| 主题 | 文档 | 说明 |
|------|------|------|
| 项目定位 | [positioning.md](positioning.md) | 目标和设计理念 |
| 核心架构 | [core.md](core.md) | ObjectMapper、SPI |
| 解析器架构 | [parser.md](parser.md) | sealed JSONParser |
| Creator SPI | [creator-spi.md](creator-spi.md) | 可插拔设计 |
| 字段处理 | [field-handling.md](field-handling.md) | FieldReader/Writer |
| 字段名匹配 | [field-matcher.md](field-matcher.md) | 哈希匹配 |
| 平台支持 | [platforms.md](platforms.md) | Android、GraalVM |
| 性能架构 | [performance.md](performance.md) | 优化策略 |
| 模块结构 | [modules.md](modules.md) | core3、core3-android |

## 按角色阅读

### 架构师/技术 Leader
- [项目定位](positioning.md)
- [核心架构](core.md)
- [设计决策记录](design-decisions.md)

### 库开发者
- [Creator SPI](creator-spi.md)
- [字段处理](field-handling.md)
- [字段名匹配](field-matcher.md)

### 性能工程师
- [性能架构](performance.md)
- [解析器架构](parser.md)

### 平台工程师
- [平台支持](platforms.md)
- [模块结构](modules.md)

## 架构图概览

### 核心组件

```
                          ObjectMapper (immutable, thread-safe)
                         ┌──────┴──────┐
                    ObjectReader    ObjectWriter
                    (interface)     (interface)
                         │               │
              ┌──────────┼──────────┐     ┌┴─────────────┐
           Reflection   ASM    Module  Reflection   ASM
           Creator    Creator         Creator     Creator
                         │                          │
                   @JVMOnly                   @JVMOnly
```

### 解析器层次

```
sealed JSONParser
├── JSONParser.Str         (String 输入)
├── JSONParser.UTF8        (byte[] 输入，主热路径)
└── JSONParser.CharArray   (char[] 输入)
```

## 设计原则

1. **不可变性** - ObjectMapper 构建后不可修改，线程安全
2. **可插拔** - Creator SPI 允许自定义序列化/反序列化策略
3. **平台适配** - @JVMOnly 注解标记 JVM 专有代码
4. **性能优先** - sealed class 启用 JIT 优化，SWAR 并行处理

## 相关文档

- [性能优化技术](../optimization/)
- [API 参考](../../api/)
- [最佳实践](../../best-practices.md)

# 性能优化技术

fastjson3 在性能优化上做了大量工作，本文档详细介绍这些技术。

> 💡 **阅读建议**：无论你是否使用 fastjson3，这些优化技术都能帮助你编写更高效的代码。

## 优化概览

| 优化技术 | 性能提升 | 难度 | 文档 |
|----------|----------|------|------|
| SWAR 并行处理 | ~10% | ⭐⭐⭐ | [swar.md](swar.md) |
| ASM 字节码生成 | ~10-20% | ⭐⭐⭐⭐ | [asm-bytecode.md](asm-bytecode.md) |
| 转移计算 | ~30-50% | ⭐⭐⭐ | [shift-computation.md](shift-computation.md) |
| 字面量 int 匹配 | ~30% | ⭐ | [literal-int-matching.md](literal-int-matching.md) |
| Unsafe 直接操作 | ~5% | ⭐⭐⭐ | [unsafe-memory.md](unsafe-memory.md) |
| 字段预编码 | ~5% | ⭐⭐ | [field-pre-encoding.md](field-pre-encoding.md) |
| 单遍检查复制 | ~8% | ⭐⭐⭐ | [single-pass-copy.md](single-pass-copy.md) |
| Type Tag 分派 | ~5% | ⭐⭐ | [type-tag-dispatch.md](type-tag-dispatch.md) |
| 缓冲区池化 | ~3% | ⭐⭐ | [buffer-pooling.md](buffer-pooling.md) |
| 融合容量检查 | ~3% | ⭐⭐ | [fused-capacity-check.md](fused-capacity-check.md) |
| 空白字符位掩码 | ~42% (skipWhitespace) | ⭐ | [whitespace-bitmask.md](whitespace-bitmask.md) |

## 按类别阅读

### CPU 优化
- [字面量 int 匹配](literal-int-matching.md) - 固定字符串单次比较
- [SWAR 并行处理](swar.md) - 用位运算代替分支
- [Type Tag 分派](type-tag-dispatch.md) - 避免多态调用开销
- [单遍检查复制](single-pass-copy.md) - 减少遍历次数

### 内存优化
- [缓冲区池化](buffer-pooling.md) - 复用缓冲区减少 GC
- [Unsafe 直接操作](unsafe-memory.md) - 减少边界检查
- [字段预编码](field-pre-encoding.md) - 预编码固定内容
- [融合容量检查](fused-capacity-check.md) - 合并容量检查

### 代码生成优化
- [ASM 字节码生成](asm-bytecode.md) - 动态生成专门代码
- [转移计算](shift-computation.md) - 计算从运行时转移到定义阶段

## 快速开始

**新手入门**：建议按顺序阅读
1. [字面量 int 匹配](literal-int-matching.md) - 最简单，效果显著
2. [字段预编码](field-pre-encoding.md) - 容易理解
3. [单遍检查复制](single-pass-copy.md) - 思路清晰
4. [缓冲区池化](buffer-pooling.md) - 常见技巧

**深入理解**：
1. [SWAR 并行处理](swar.md) - 位运算技巧
2. [Type Tag 分派](type-tag-dispatch.md) - JVM 优化原理
3. [转移计算](shift-computation.md) - 设计思想

**高级应用**：
1. [ASM 字节码生成](asm-bytecode.md) - 复杂但强大
2. [Unsafe 直接操作](unsafe-memory.md) - 底层操作

## 优化设计原则

总结 fastjson3 的优化思路：

1. **减少分支** - SWAR、Type Tag
2. **减少遍历** - 单遍检查复制
3. **减少调用** - 字段预编码、融合检查
4. **避免反射** - ASM 字节码生成
5. **内存复用** - 缓冲区池化
6. **快速路径** - 常见情况特殊处理
7. **转移计算** - 将计算从运行时转移到定义/CodeGen 阶段

## 参考资料

- [SWAR 技术](https://www.chessprogramming.org/SIMD_and_SWAR_Tricks)
- [Java Unsafe 指南](https://www.baeldung.com/java-unsafe)
- [ASM 官方文档](https://asm.ow2.io/)

## 相关文档

- [🏗️ 整体架构 →](../architecture.md)
- [📖 性能调优指南 →](../../guides/performance.md)

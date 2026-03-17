# 按难度阅读

## 初级优化 ⭐⭐

适合刚开始学习性能优化的开发者：

1. [缓冲区池化](buffer-pooling.md) - 复用对象减少 GC
2. [字段预编码](field-pre-encoding.md) - 预编码固定内容
3. [融合容量检查](fused-capacity-check.md) - 合并多次检查

## 中级优化 ⭐⭐⭐

需要一定的 JVM 和计算机基础：

1. [单遍检查复制](single-pass-copy.md) - 减少遍历次数
2. [Type Tag 分派](type-tag-dispatch.md) - 避免多态调用
3. [转移计算](shift-computation.md) - 设计思想

## 高级优化 ⭐⭐⭐⭐

需要深入理解 JVM 底层和字节码：

1. [SWAR 并行处理](swar.md) - 用位运算代替分支
2. [Unsafe 直接操作](unsafe-memory.md) - 绕过边界检查
3. [ASM 字节码生成](asm-bytecode.md) - 动态生成代码

## 学习路径建议

```
初级 → 中级 → 高级
  ↓      ↓       ↓
学会   深入    掌握
复用   原理    底层
```

[← 返回索引](README.md)

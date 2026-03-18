# 整体架构设计

fastjson3 的架构文档已按主题拆分为独立文件。

## 📁 架构文档索引

[📘 查看架构文档索引 →](architecture/)

## 项目定位

fastjson3 是 fastjson2 的下一代版本，目标：

- **Java 21+ baseline** - sealed class、pattern matching、switch expression、record patterns
- **性能超越** - 比 wast、fastjson2 更快
- **API 对标 Jackson 3** - 不可变 ObjectMapper、Builder 模式
- **多平台支持** - GraalVM Native Image、Android 8+

---

## 核心架构

```
                          ObjectMapper (immutable, thread-safe)
                         ┌──────┴──────┐
                    ObjectReader    ObjectWriter
                    (interface)     (interface)
                         │               │
              ┌──────────┼────────┐     ┌┴─────────────┐
           Reflection   ASM    Module  Reflection   ASM
           Creator    Creator         Creator     Creator
                         │                          │
                   @JVMOnly                   @JVMOnly
```

### 设计理念

1. **不可变性** - ObjectMapper 构建后不可修改，线程安全
2. **可插拔** - Creator SPI 允许自定义序列化/反序列化策略
3. **平台适配** - @JVMOnly 注解标记 JVM 专有代码

---

## 解析器架构

### sealed class 层次结构

```
sealed JSONParser
├── JSONParser.Str         (String 输入)
├── JSONParser.UTF8        (byte[] 输入，主热路径)
└── JSONParser.CharArray   (char[] 输入)
```

**为什么使用 sealed？**

- JIT 可以做 **devirtualization**（去虚化）
- 将虚方法调用优化为直接调用或内联
- 性能提升约 5-10%

### 生成器架构

```
sealed JSONGenerator
├── JSONGenerator.Char     (char[] 缓冲区，String 输出)
└── JSONGenerator.UTF8     (byte[] 缓冲区，byte[] 输出，主热路径)
```

---

## ObjectReaderProvider 机制

### 设计目标

在 ObjectMapper 层面配置 ASM/Reflection 策略，确保嵌套 POJO 使用相同的策略。

### 架构

```
                    ObjectReaderProvider (interface)
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
  AutoObjectReaderProvider  │   ASMObjectReaderProvider
    (自动选择 ASM/反射)      │      (强制 ASM)
                            │
                   ReflectObjectReaderProvider
                      (仅反射)

                    ↑ 继承自
    AbstractObjectReaderProvider (基类)
    ├─ readerCache (ConcurrentHashMap)
    ├─ classLoader (DynamicClassLoader)
    ├─ shared (boolean)
    └─ cleanup() 方法
```

### ThreadLocal 上下文传播

```
ThreadA                               ThreadB
    │                                     │
    │ context = provider.openContext()    │
    │ ┌─ ThreadLocal.set(provider) ───────┼──┐
    │ │                                   │  │
    │ │ createObjectReader(User)          │  │
    │ │   └─ CONTEXT.get() ───────────────┼──┼───→ ASM
    │ │                                   │  │
    │ │   createObjectReader(Client)      │  │
    │ │     └─ CONTEXT.get() ─────────────┼──┼───→ ASM
    │ │                                   │  │
    │ │ context.close()                   │  │
    │ └─ ThreadLocal.remove() ────────────┼──┘
    │                                     │
```

### SafeContext RAII 模式

```java
// 自动清理，避免 ThreadLocal 内存泄漏
try (SafeContext ctx = provider.openContext()) {
    // 嵌套类型创建，自动使用父级策略
    User user = mapper.readValue(json, User.class);
    // Client 和 Geo 也使用 ASM
}
```

### ReaderCreatorType 枚举

| 策略 | 说明 |
|------|------|
| AUTO | 自动选择：简单 POJO 用 ASM，复杂类型用反射 |
| ASM | 强制 ASM，递归应用到嵌套类型 |
| REFLECT | 仅反射，无字节码生成 |

### 配置方式

```java
// ObjectMapper 层面配置
ObjectMapper asmMapper = ObjectMapper.builder()
    .readerCreatorType(ReaderCreatorType.ASM)
    .build();

// 所有嵌套类型都使用 ASM
User user = asmMapper.readValue(json, User.class);
// User.client → ASM
// User.client.geo → ASM
```

### ClassLoader 卸载支持

```
ObjectMapper (per-instance)
    │
    ├─ DynamicClassLoader (one per ObjectMapper)
    │   ├─ WeakReference<Class<?>>
    │   └─ ReferenceQueue (自动清理)
    │
    └─ cleanup() 方法
        ├─ cacheVersion.incrementAndGet()
        ├─ readerCache.clear()
        └─ readerProvider.cleanup()
```

---

## Creator SPI

### 设计

```java
// ObjectReader/ObjectWriter 的创建策略是可插拔的
Function<Class<?>, ObjectReader<?>> readerCreator
Function<Class<?>, ObjectWriter<?>> writerCreator
```

### 默认实现

- **Reflection** - 使用反射，默认选择
- **ASM** - 运行时生成字节码，可选

### ASM 性能

| 操作 | 反射 | ASM | 提升 |
|------|------|-----|------|
| Read | 100% | 107% | +7% |
| Write | 100% | 100% | 持平 |

**Write 持平原因：** 反射 Writer 已通过 Unsafe 高度优化。

---

## 字段处理架构

### FieldReader (反序列化)

```
FieldReader
├── typeTag (int)           // 类型标签
├── fieldName (String)      // 字段名
├── fieldNameHashCode (long) // 预计算哈希
├── offset (long)           // Unsafe 偏移量
└── readXxx() 方法
```

**Type Tag 分派：**

```java
public final void readField(JSONParser parser, Object bean) {
    switch (typeTag) {  // 编译为跳转表
        case TYPE_STRING -> readString(parser, bean);
        case TYPE_INT -> readInt(parser, bean);
        // ...
    }
}
```

**避免 megamorphic callsite：**
- 多态子类 >3 种时，JIT 退化为间接跳转
- Type Tag + switch 保持单态调用点

### FieldWriter (序列化)

```
FieldWriter
├── typeTag (int)
├── nameLongs (long[])      // 预编码字段名
├── nameBytes (byte[])      // 预编码字段名
└── writeXxx() 方法
```

---

## 字段名匹配 (FieldNameMatcher)

### 两级策略

```
1. Byte Comparison (主路径)
   └── 预编码字段名为 byte[]
   └── first-byte dispatch
   └── 直接字节比较

2. Hash-based (回退)
   └── 对 String/char[] 或转义序列
   └── 三种哈希策略自动选择
```

### 三种哈希策略

| 策略 | 公式 | 适用场景 |
|------|------|----------|
| PLHV | `hash += byte` | 加法，默认 |
| BIHV | `hash = hash*31 + byte` | 位移，碰撞时 |
| PRHV | `hash = hash*31 + byte` | 质数乘法，再碰撞 |

**零碰撞保证：** 自动切换策略直到零碰撞。

---

## 平台支持

### GraalVM Native Image

**检测：**

```java
// static final boolean，JIT 常量折叠
public static final boolean NATIVE_IMAGE =
    System.getProperty("org.graalvm.nativeimage.imagecode") != null;
```

**降级策略：**

| 组件 | JVM | Native Image |
|------|-----|--------------|
| ASM Creator | 使用 | 回退到反射 |
| Unsafe 操作 | 使用 | 使用（GraalVM 支持） |
| String 内部操作 | 使用 | 回退到标准 API |

### Android

**@JVMOnly 注解：**

```java
@Retention(SOURCE)
@Target(TYPE)
@Documented
public @interface JVMOnly {}
```

**自动排除：**
- 所有 `internal/asm/*` (13 个类)
- ASM Creators
- DynamicClassLoader

**构建：**

```bash
mvn package                    # → fastjson3-3.0.0.jar (172KB, 62 classes)
mvn package -Pandroid          # → fastjson3-3.0.0-android.jar (111KB, 45 classes)
```

---

## 性能优化架构

### 优化层次

```
应用层
    │
    ├─ 复用 ObjectMapper
    ├─ 使用 byte[] 而非 String
    └─ 启用 ASM
    │
框架层
    │
    ├─ sealed class (JIT 优化)
    ├─ Type Tag 分派 (避免 megamorphic)
    ├─ 字段名预编码
    └─ 缓冲区池化
    │
实现层
    │
    ├─ SWAR (noEscape8)
    ├─ Unsafe 直接操作
    ├─ 单遍检查复制
    ├─ 融合容量检查
    └─ putLong 批量写入
```

### 关键技术

1. **SWAR** - 8 字节并行转义检测 (~10% 提升)
2. **ASM** - 运行时字节码生成 (~7% Read 提升)
3. **Unsafe** - 绕过边界检查 (~5% 提升)
4. **预编码** - 字段名 long[] (~5% 提升)
5. **池化** - 线程本地缓冲区 (~3% 提升)

**总计：** ~36% vs fastjson2 (UsersWriteUTF8)

---

## 模块结构

```
core3/src/main/java/com/alibaba/fastjson3/
├── JSON.java                      # 静态工具类
├── JSONParser.java                # sealed 解析器
├── JSONGenerator.java             # sealed 生成器
├── ObjectMapper.java              # 不可变映射器
├── JSONPath.java                  # JSONPath 引擎
├── JSONObject.java                # 动态对象
├── JSONArray.java                 # 动态数组
├── ReadFeature.java               # 解析特性
├── WriteFeature.java              # 序列化特性
├── annotation/
│   ├── JSONField.java
│   ├── JSONType.java
│   ├── JSONCreator.java
│   ├── NamingStrategy.java
│   └── JVMOnly.java
├── reader/
│   ├── FieldReader.java           # type tag dispatch
│   ├── FieldNameMatcher.java      # byte + hash 双策略
│   ├── ObjectReaderProvider.java        # Reader 创建策略接口
│   ├── AbstractObjectReaderProvider.java  # Provider 基类
│   ├── AutoObjectReaderProvider.java     # 自动 ASM/反射选择
│   ├── ASMObjectReaderProvider.java      # 强制 ASM 策略
│   ├── ReflectObjectReaderProvider.java  # 仅反射策略
│   ├── ReaderCreatorType.java           # 策略枚举 (AUTO/ASM/REFLECT)
│   ├── ObjectReaderCreator.java
│   └── ObjectReaderCreatorASM.java
├── writer/
│   ├── FieldWriter.java
│   ├── ObjectWriterCreator.java
│   └── ObjectWriterCreatorASM.java
├── modules/
│   ├── ObjectReaderModule.java
│   └── ObjectWriterModule.java
├── schema/
│   ├── JSONSchema.java
│   └── validator/
├── jsonpath/
│   ├── JSONPath.java
│   └── segments/
├── filter/
│   ├── NameFilter.java
│   ├── ValueFilter.java
│   └── PropertyFilter.java
├── util/
│   ├── JDKUtils.java              # Unsafe + 平台检测
│   ├── UnsafeAllocator.java
│   ├── BufferPool.java
│   ├── DynamicClassLoader.java    # ASM 类加载器，支持卸载
│   ├── Logger.java                # 内部诊断日志
│   └── VectorizedScanner.java
└── internal/
    └── asm/                        # @JVMOnly ASM 库
```

---

## 设计决策记录

### 为什么使用 sealed class？

1. **性能** - JIT 可以做 devirtualization
2. **可维护性** - 编译时检查所有子类
3. **可读性** - 清晰表达封闭层次结构

### 为什么使用 @JVMOnly 而非 Maven profiles？

1. **源码级标记** - 一目了然
2. **自动排除** - Android profile 自动扫描
3. **无需维护** - 新增类只需加注解

### 为什么用 Type Tag 而非多态？

1. **避免 megamorphic** - 超过 3 种类型性能下降
2. **内联友好** - switch 更容易被 JIT 优化
3. **内存效率** - 单个 int vs 对象引用

---

## 相关文档

- [⚡ 性能优化技术 →](optimization.md)
- [📖 序列化优化设计 →](../../core3/docs/serialization_optimization.md)
- [📖 设计历史 →](../design/api-design-evolution.md)

# 模块结构

fastjson3 的代码组织。

## 目录结构

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
│   ├── JSONField.java             # 字段注解
│   ├── JSONType.java              # 类注解
│   ├── JSONCreator.java           # 构造器注解
│   ├── NamingStrategy.java        # 命名策略
│   └── JVMOnly.java               # JVM 专有标记
├── reader/
│   ├── FieldReader.java           # type tag dispatch
│   ├── FieldNameMatcher.java      # byte + hash 双策略
│   ├── ObjectReader.java          # 反序列化接口
│   ├── ObjectReaderCreator.java   # SPI
│   └── ObjectReaderCreatorASM.java # @JVMOnly
├── writer/
│   ├── FieldWriter.java           # 预编码字段名
│   ├── ObjectWriter.java          # 序列化接口
│   ├── ObjectWriterCreator.java   # SPI
│   └── ObjectWriterCreatorASM.java # @JVMOnly
├── modules/
│   ├── ObjectReaderModule.java    # Reader 扩展模块
│   └── ObjectWriterModule.java    # Writer 扩展模块
├── schema/
│   ├── JSONSchema.java            # JSONSchema 接口
│   ├── JSONSchemaValidator.java   # Schema 验证器
│   └── validator/
│       ├── SchemaValidator.java
│       └── formats/
├── jsonpath/
│   ├── JSONPath.java              # JSONPath 核心接口
│   ├── segments/                  # 路径段实现
│   └── compiler/                  # 路径编译器
├── filter/
│   ├── Filter.java                # 过滤器接口
│   ├── NameFilter.java            # 字段名过滤
│   ├── ValueFilter.java           # 值过滤
│   └── PropertyFilter.java        # 属性过滤
├── util/
│   ├── JDKUtils.java              # Unsafe + 平台检测
│   ├── UnsafeAllocator.java       # Unsafe 分配器
│   ├── BufferPool.java            # 线程本地缓冲区
│   ├── DynamicClassLoader.java    # 动态类加载
│   └── VectorizedScanner.java     # 向量化扫描
└── internal/
    └── asm/                        # @JVMOnly ASM 库
```

## 模块说明

### 核心模块

| 模块 | 说明 |
|------|------|
| JSON.java | 静态工具类，parseObject, toJSONString 等 |
| ObjectMapper.java | 不可变映射器，线程安全 |
| JSONObject/JSONArray | 动态 JSON 对象/数组 |

### 解析/序列化

| 模块 | 说明 |
|------|------|
| JSONParser | sealed 解析器层次 |
| JSONGenerator | sealed 生成器层次 |
| reader/ | 反序列化相关 |
| writer/ | 序列化相关 |

### 扩展机制

| 模块 | 说明 |
|------|------|
| modules/ | 扩展模块 SPI |
| filter/ | 过滤器接口 |
| annotation/ | 注解定义 |

### 高级特性

| 模块 | 说明 |
|------|------|
| schema/ | JSON Schema 支持 |
| jsonpath/ | JSONPath 引擎 (RFC 9535) |
| util/ | 工具类和平台适配 |
| internal/ | 内部实现 (ASM 等) |

## Android 模块

```
core3-android/src/main/java/com/alibaba/fastjson3/
└── (排除 @JVMOnly 类)
```

Android 版本通过 Maven profile 自动排除：
- `internal/asm/*` (13 个类)
- ASM Creators
- DynamicClassLoader

## 相关文档

- [平台支持](platforms.md)
- [核心架构](core.md)

[← 返回索引](README.md)

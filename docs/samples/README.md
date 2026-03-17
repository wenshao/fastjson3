# fastjson3 示例代码

本目录包含 fastjson3 的可运行示例代码。

## 目录结构

```
samples/
├── basic/                    # 基础示例
│   ├── BasicExample.java
│   ├── AnnotationExample.java
│   └── GenericExample.java
├── jsonpath/                 # JSONPath 示例
│   ├── JSONPathExample.java
│   └── JSONPathRealWorldExample.java
├── performance/              # 性能优化示例
│   ├── PerformanceExample.java
│   └── HighPerformanceExample.java
└── spring-boot/              # Spring Boot 集成示例
```

## 编译和运行

### 使用 Maven

确保项目依赖已配置：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 编译示例

```bash
# 进入 samples 目录
cd samples

# 编译所有示例
javac -cp ../..:../../../* basic/*.java jsonpath/*.java performance/*.java

# 运行基础示例
java -cp ../..:../../../*:. com.alibaba.fastjson3.samples.basic.BasicExample

# 运行 JSONPath 示例
java -cp ../..:../../../*:. com.alibaba.fastjson3.samples.jsonpath.JSONPathExample
```

## 示例说明

### basic/ - 基础示例

| 示例 | 说明 |
|------|------|
| BasicExample | 基本的序列化和反序列化操作 |
| AnnotationExample | 注解使用：字段重命名、格式化、忽略等 |
| GenericExample | 泛型类型处理：List、Map、嵌套泛型 |

### jsonpath/ - JSONPath 示例

| 示例 | 说明 |
|------|------|
| JSONPathExample | JSONPath 基本语法和使用 |
| JSONPathRealWorldExample | 实际应用：日志分析、配置提取等 |

### performance/ - 性能示例

| 示例 | 说明 |
|------|------|
| PerformanceExample | 性能基准测试和最佳实践 |
| HighPerformanceExample | 高级优化技巧：ASM、缓存等 |

## 运行要求

- JDK 21+
- fastjson3 3.0.0 或更高版本

## 代码风格

示例代码遵循 fastjson3 的最佳实践：

- 复用 `ObjectMapper` 实例
- 使用 `byte[]` 处理 UTF-8 数据
- 预编译 `JSONPath`
- 正确的异常处理

## 相关文档

- [📖 入门教程](../start/)
- [📖 场景指南](../guides/)
- [⚡ 性能优化](../guides/performance.md)

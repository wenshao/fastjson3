# GraalVM Native Image 支持

fastjson3 对 GraalVM Native Image 的完整支持。

## 检测

```java
public final class JDKUtils {
    // static final boolean，JIT 常量折叠
    public static final boolean NATIVE_IMAGE =
        System.getProperty("org.graalvm.nativeimage.imagecode") != null;
}
```

## 降级策略

| 组件 | JVM | Native Image |
|------|-----|--------------|
| ASM Creator | 使用 | 回退到反射 |
| Unsafe 操作 | 使用 | 使用（GraalVM 支持） |
| String 内部操作 | 直接调用 | 回退到标准 API |

## 反射配置

### reflect-config.json

```json
{
  "reflect": [
    {
      "name": "com.alibaba.fastjson3.JSON",
      "allDeclaredFields": true,
      "allPublicMethods": true
    },
    {
      "name": "com.alibaba.fastjson3.JSONObject",
      "allDeclaredFields": true,
      "allPublicMethods": true
    },
    {
      "name": "com.alibaba.fastjson3.JSONArray",
      "allDeclaredFields": true,
      "allPublicMethods": true
    },
    {
      "name": "com.alibaba.fastjson3 ObjectMapper",
      "allDeclaredFields": true,
      "allPublicMethods": true
    }
  ]
}
```

## 资源配置

### resource-config.json

```json
{
  "resources": {
    "includes": [
      {
        "pattern": "\\.json$"
      }
    ]
  }
}
```

## 条件编译

### 自动降级

```java
// Native Image 环境自动使用反射 Creator
ObjectReaderCreator readerCreator;
if (JDKUtils.NATIVE_IMAGE) {
    readerCreator = new ObjectReaderCreatorReflective();
} else {
    readerCreator = new ObjectReaderCreatorASM();
}
```

### 手动配置

```java
// 强制使用反射（即使在 Native Image）
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreator(ObjectReaderCreatorReflective::new)
    .writerCreator(ObjectWriterCreatorReflective::new)
    .build();
```

## 构建 Native Image

### 基本命令

```bash
# 编译为 native executable
native-image -cp fastjson3.jar:app.jar \
  --enable-https \
  --report-unsupported-elements-at-runtime \
  MainClass
```

### 使用 GraalVM Tracing Agent

```bash
# 1. 在 JVM 上运行应用并收集元数据
java -agentlib:native-image-agent.jar=trace-output \
  -cp fastjson3.jar:app.jar \
  MainClass

# 2. 使用收集的元数据构建
native-image -H:ConfigurationFileDirectories=trace-output \
  -cp fastjson3.jar:app.jar \
  MainClass
```

## 性能考虑

### Native Image 性能

| 操作 | JVM JIT | Native Image |
|------|---------|--------------|
| 启动时间 | 慢 | 极快 |
| 峰值性能 | 高 | 高 |
| 内存占用 | 高 | 低 |
| 镜像大小 | - | 大 |

### 启动时间对比

```
JVM:     ~1-2秒（预热后达到峰值）
Native:  ~0.01秒（即达峰值）
```

## 常见问题

### Q: 为什么不能使用 ASM？

A: Native Image 不支持运行时字节码生成，需要回退到反射。

### Q: 性能会下降吗？

A: Read 性能下降 ~7%（无 ASM），但启动时间和内存占用大幅优化。

### Q: 如何提升 Native Image 性能？

A:
1. 使用反射配置文件
2. 使用 Tracing Agent 收集完整元数据
3. 考虑使用 Shared 库模式

## 示例项目

### Maven 配置

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
    <version>0.9.28</version>
    <configuration>
        <imageName>myapp</imageName>
        <mainClass>com.example.Main</mainClass>
        <buildArgs>
            <buildArg>-H:+ReportExceptionStackTraces</buildArg>
            <buildArg>-H:-DeleteLocalSymbols</buildArg>
            <buildArg>--enable-https</buildArg>
        </buildArgs>
    </configuration>
</plugin>
```

### 运行构建

```bash
mvn -Pnative native:compile
```

## 相关文档

- [Android 支持](android.md)
- [平台检测](positioning.md)
- [性能优化](../optimization/)

[← 返回索引](README.md)

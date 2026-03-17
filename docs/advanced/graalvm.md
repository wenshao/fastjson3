# GraalVM Native Image 配置指南

fastjson3 支持 GraalVM Native Image，需要适当的反射配置。

## 基本配置

### 反射配置

创建 `src/main/resources/META-INF/native-image/reflect-config.json`：

```json
{
  "reflect": [
    {
      "name": "com.example.User",
      "allDeclaredFields": true,
      "allPublicMethods": true,
      "allDeclaredConstructors": true
    },
    {
      "name": "com.example.Order",
      "allDeclaredFields": true,
      "allPublicMethods": true,
      "allDeclaredConstructors": true
    }
  ]
}
```

### 资源配置

创建 `src/main/resources/META-INF/native-image/resource-config.json`：

```json
{
  "resources": {
    "includes": [
      {
        "pattern": ".*\\.json$"
      }
    ]
  }
}
```

---

## 使用 Tracing Agent

Tracing Agent 可以自动生成配置文件。

### 运行应用并收集信息

```bash
# 运行应用，附加 tracing agent
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
     -jar your-application.jar

# 触发所有需要反射的功能
# Agent 会自动记录所有反射访问

# 关闭应用后，配置文件会自动生成
```

### 运行测试以触发反射

```bash
# 运行测试套件
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
     -cp target/classes:test-classes \
     org.junit.runner.JUnitCore com.example.TestSuite
```

---

## 手动配置

### 配置 POJO 类

对于每个需要序列化/反序列化的 POJO 类：

```json
{
  "name": "com.example.User",
  "allDeclaredFields": true,
  "allPublicMethods": true,
  "allDeclaredConstructors": true
}
```

### 配置集合类型

```json
{
  "name": "java.util.ArrayList",
  "allPublicMethods": true,
  "allDeclaredConstructors": true
},
{
  "name": "java.util.HashMap",
  "allPublicMethods": true,
  "allDeclaredConstructors": true
}
```

### 配置 fastjson3 内部类

```json
{
  "name": "com.alibaba.fastjson3.JSON",
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
}
```

---

## 构建配置

### native-image.properties

创建 `src/main/resources/META-INF/native-image/native-image.properties`：

```properties
# 类路径
Args = -H:ConfigurationFileDirectories=src/main/resources/META-INF/native-image/ \
       -H:ResourceConfigurationFiles=src/main/resources/META-INF/native-image/resource-config.json \
       -H:ReflectionConfigurationFiles=src/main/resources/META-INF/native-image/reflect-config.json

# 排除不需要的功能
Args = --no-fallback \
       --initialize-at-build-time=org.slf4j \
       --report-unsupported-elements-at-runtime
```

---

## 构建原生镜像

### 基本构建

```bash
# 使用 Maven
mvn -Pnative native:compile

# 使用 Gradle
./gradlew nativeCompile
```

### 完整构建命令

```bash
native-image \
  --no-fallback \
  -H:ConfigurationFileDirectories=src/main/resources/META-INF/native-image/ \
  -H:ResourceConfigurationFiles=src/main/resources/META-INF/native-image/resource-config.json \
  -H:ReflectionConfigurationFiles=src/main/resources/META-INF/native-image/reflect-config.json \
  -cp target/classes:target/dependency/* \
  com.example.Main
```

---

## 常见问题

### 类未注册

**错误：** `com.oracle.graal.pointsto.constraints.UnsupportedFeatureException: Reflective access to ...`

**解决：** 在 reflect-config.json 中添加该类。

### 字段未找到

**错误：** `java.lang.NoSuchFieldException: $fieldName`

**解决：** 确保 `allDeclaredFields: true`。

### 构造方法未找到

**错误：** `java.lang.NoSuchMethodException: ...<init>()`

**解决：** 确保配置了 `allDeclaredConstructors: true`。

---

## 平台检测

fastjson3 在 Native Image 中会自动检测并调整：

```java
// JDKUtils.NATIVE_IMAGE 在 Native Image 中为 true
if (JDKUtils.NATIVE_IMAGE) {
    // Native Image 特定逻辑
}
```

以下功能在 Native Image 中会自动调整：

| 功能 | JVM | Native Image |
|------|-----|--------------|
| ASM 生成 | 启用 | **禁用**（使用反射） |
| Unsafe 操作 | 启用 | 启用 |
| String 内部操作 | 启用 | **使用标准 API** |

---

## 性能优化

### 预注册类型

```java
@Configuration
public class NativeImageConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = ObjectMapper.builder()
            .readerCreator(ObjectReaderCreatorASM::createObjectReader)
            .writerCreator(ObjectWriterCreatorASM::createObjectWriter)
            .build();

        // 预热常用类型
        mapper.getObjectWriter(User.class);
        mapper.getObjectWriter(Order.class);
        mapper.getObjectReader(User.class);
        mapper.getObjectReader(Order.class);

        return mapper;
    }
}
```

### 配置文件分离

```bash
src/main/resources/META-INF/native-image/
├── reflect-config.json        # 反射配置
├── resource-config.json       # 资源配置
├── proxy-config.json          # 动态代理配置
├── serialization-config.json  # 序列化配置
└── native-image.properties    # 构建配置
```

---

## 完整示例

### reflect-config.json

```json
[
  {
    "name": "com.example.User",
    "queryAllDeclaredConstructors": true,
    "allDeclaredFields": true,
    "allPublicMethods": true
  },
  {
    "name": "com.alibaba.fastjson3.JSON",
    "allPublicMethods": true
  },
  {
    "name": "com.alibaba.fastjson3.JSONObject",
    "allDeclaredFields": true,
    "allPublicMethods": true
  },
  {
    "name": "java.util.ArrayList",
    "allPublicMethods": true,
    "allDeclaredConstructors": true
  },
  {
    "name": "java.util.HashMap",
    "allPublicMethods": true,
    "allDeclaredConstructors": true
  }
]
```

### 构建脚本

```bash
#!/bin/bash
# build-native.sh

# 设置配置目录
CONFIG_DIR="src/main/resources/META-INF/native-image"

# 构建原生镜像
native-image \
  --no-fallback \
  -H:Name=myapp \
  -H:ConfigurationFileDirectories=$CONFIG_DIR \
  -cp target/classes:target/dependency/* \
  com.example.Main

echo "Native image built successfully!"
```

---

## 测试

### 单元测试

```java
@Test
public void testUserSerialization() {
    User user = new User();
    user.setName("张三");

    String json = JSON.toJSONString(user);
    User parsed = JSON.parseObject(json, User.class);

    assertEquals("张三", parsed.getName());
}
```

### 集成测试

在原生镜像上运行相同的测试：

```bash
# 构建原生镜像
mvn -Pnative native:compile

# 运行测试
./target/myapp com.example.TestSuite
```

---

## 相关文档

- [GraalVM 官方文档](https://www.graalvm.org/latest/reference-manual/native-image/)
- [📖 Android 优化 →](android.md)
- [📖 性能调优 →](../guides/performance.md)

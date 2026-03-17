# 平台支持

fastjson3 对 GraalVM Native Image 和 Android 的支持。

## GraalVM Native Image

### 检测

```java
// static final boolean，JIT 常量折叠
public static final boolean NATIVE_IMAGE =
    System.getProperty("org.graalvm.nativeimage.imagecode") != null;
```

### 降级策略

| 组件 | JVM | Native Image |
|------|-----|--------------|
| ASM Creator | 使用 | 回退到反射 |
| Unsafe 操作 | 使用 | 使用（GraalVM 支持） |
| String 内部操作 | 使用 | 回退到标准 API |

### 配置

需要提供反射元数据：

```json
{
  "reflect": [
    {
      "name": "com.alibaba.fastjson3.JSON",
      "allDeclaredFields": true,
      "allPublicMethods": true
    }
  ],
  "resources": {
    "includes": [
      {
        "pattern": "\\.json$"
      }
    ]
  }
}
```

## Android

### @JVMOnly 注解

```java
@Retention(SOURCE)
@Target(TYPE)
@Documented
public @interface JVMOnly {
}
```

### 自动排除

所有标记为 `@JVMOnly` 的类在 Android 构建时自动排除：

```java
@JVMOnly
class ObjectReaderCreatorASM {
    // Android 构建时自动排除
}

@JVMOnly
class DynamicClassLoader {
    // Android 构建时自动排除
}
```

### 构建对比

```bash
# 构建 JVM 版本
mvn package
# → fastjson3-3.0.0.jar (172KB, 62 classes)

# 构建 Android 版本
mvn package -Pandroid
# → fastjson3-3.0.0-android.jar (111KB, 45 classes)
```

### Android Maven Profile

```xml
<profile>
    <id>android</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <excludes>
                        <!-- 排除 @JVMOnly 类 -->
                        <exclude>**/internal/asm/**</exclude>
                        <exclude>**/ObjectReaderCreatorASM.java</exclude>
                        <exclude>**/ObjectWriterCreatorASM.java</exclude>
                        <exclude>**/DynamicClassLoader.java</exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

## 平台检测

### JDKUtils

```java
public final class JDKUtils {
    // 平台检测常量
    public static final boolean ANDROID;
    public static final boolean NATIVE_IMAGE;
    public static final boolean UNSAFE_AVAILABLE;

    static {
        // Android 检测
        ANDROID = "Dalvik".equals(System.getProperty("java.vm.name"));

        // Native Image 检测
        NATIVE_IMAGE = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

        // Unsafe 检测
        UNSAFE_AVAILABLE = unsafeAvailable();
    }
}
```

### 条件启用

```java
// 根据平台选择 Creator
Function<Class<?>, ObjectReader<?>> readerCreator;
if (JDKUtils.ANDROID || JDKUtils.NATIVE_IMAGE) {
    readerCreator = new ObjectReaderCreatorReflective();
} else {
    readerCreator = new ObjectReaderCreatorASM();
}
```

## 相关文档

- [ASM 字节码生成](../optimization/asm-bytecode.md)
- [模块结构](modules.md)

[← 返回索引](README.md)

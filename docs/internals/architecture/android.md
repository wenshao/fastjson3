# Android 平台支持

fastjson3 对 Android 8+ 的完整支持。

## @JVMOnly 注解

```java
@Retention(SOURCE)
@Target(TYPE)
@Documented
public @interface JVMOnly {
}
```

标记为 `@JVMOnly` 的类在 Android 构建时自动排除。

## 自动排除的类

### ASM 相关

```
internal/asm/
├── ASM.java
├── ClassReader.java
├── ClassWriter.java
├── MethodWriter.java
├── (13 个 ASM 核心类)
```

### Creators

```
ObjectReaderCreatorASM.java
ObjectWriterCreatorASM.java
```

### 动态类加载

```
DynamicClassLoader.java
```

## Android Maven Profile

### 配置

```xml
<profile>
    <id>android</id>
    <activation>
        <property>
            <name>android.exclude</name>
            <value>true</value>
        </property>
    </activation>

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

## 构建对比

```bash
# 构建 JVM 版本
mvn package
# → fastjson3-3.0.0.jar (172KB, 62 classes)

# 构建 Android 版本
mvn package -Pandroid
# → fastjson3-3.0.0-android.jar (111KB, 45 classes)
```

**节省**：~35% 体积，~27% 类

## Android 降级策略

| 组件 | JVM 版本 | Android 版本 |
|------|----------|--------------|
| ASM Creator | 使用 | 回退到反射 |
| Unsafe 操作 | 使用 | 使用（Android 支持） |
| String 内部操作 | 直接调用 | 标准调用 |

## 平台检测

```java
public final class JDKUtils {
    public static final boolean ANDROID;

    static {
        // Android 检测
        ANDROID = "Dalvik".equals(System.getProperty("java.vm.name"));
    }
}
```

## 条件启用

```java
// 根据平台选择 Creator
ObjectReader<?> readerCreator;
if (JDKUtils.ANDROID) {
    readerCreator = new ObjectReaderCreatorReflective();
} else {
    readerCreator = new ObjectReaderCreatorASM();
}
```

## Gradle 依赖

```gradle
dependencies {
    implementation 'com.alibaba.fastjson3:fastjson3:3.0.0'
}
```

## ProGuard 配置

```proguard
# fastjson3
-keep class com.alibaba.fastjson3.** { *; }
-keepclassmembers class com.alibaba.fastjson3.** { *; }

# 如果使用注解
-keepattributes *Annotation*
```

## 性能特点

### Android 优化的特点

1. **反射优化** - Android 的反射比 JVM 快
2. **无 ASM 开销** - 启动更快
3. **体积小** - APK 体积减少

### 性能对比

| 操作 | Android | JVM+ASM |
|------|---------|---------|
| 对象序列化 | 100% | 107% |
| 对象反序列化 | 100% | 107% |

**注**：Android 使用反射仍能达到良好性能。

## 示例代码

### Android 项目中使用

```java
import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 解析 JSON
        String json = "{\"name\":\"张三\",\"age\":25}";
        User user = JSON.parseObject(json, User.class);

        // 序列化
        String output = JSON.toJSONString(user);
    }
}
```

## 相关文档

- [核心架构](core.md)
- [模块结构](modules.md)
- [GraalVM 支持](graalvm.md)

[← 返回索引](README.md)

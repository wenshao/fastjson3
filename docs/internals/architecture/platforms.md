# 平台支持架构

fastjson3 的多平台支持架构设计。

## 支持的平台

| 平台 | 状态 | 用户文档 |
|------|------|----------|
| JVM (Java 17+) | ✅ 完全支持 | - |
| Android 8+ | ✅ 独立构建 | [📘 Android 指南 →](../../advanced/android.md) |
| GraalVM Native Image | ✅ 支持 | [📘 GraalVM 指南 →](../../advanced/graalvm.md) |

## 架构设计

### @JVMOnly 注解

```java
@Retention(SOURCE)
@Target(TYPE)
@Documented
public @interface JVMOnly {
}
```

标记为 `@JVMOnly` 的类在非 JVM 平台自动排除。

### 平台检测

```java
public final class JDKUtils {
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

### 降级策略矩阵

| 组件 | JVM | Android | Native Image |
|------|-----|---------|--------------|
| ASM Creator | 使用 | 排除 | 回退到反射 |
| Unsafe 操作 | 使用 | 使用 | 使用 |
| 直接内存访问 | 使用 | 回退 | 回退 |

## 构建产物

### JVM 版本

```bash
mvn package
# → fastjson3-3.0.0.jar (172KB, 62 classes)
```

### Android 版本

```bash
mvn package -Pandroid
# → fastjson3-3.0.0-android.jar (111KB, 45 classes)
# 节省 ~35% 体积
```

## 相关文档

### 平台特定文档
- [Android 完整指南 →](../../advanced/android.md)
- [GraalVM Native Image 指南 →](../../advanced/graalvm.md)

### 架构相关
- [模块结构 →](modules.md)
- [设计决策 →](design-decisions.md)

[← 返回索引](README.md)

# 平台支持

fastjson3 对多平台的支持。

## 支持的平台

| 平台 | 状态 | 文档 |
|------|------|------|
| JVM (Java 17+) | ✅ 完全支持 | - |
| Android 8+ | ✅ 独立构建 | [android.md](android.md) |
| GraalVM Native Image | ✅ 支持 | [graalvm.md](graalvm.md) |

## 平台文档

### Android

- [Android 平台支持 →](android.md)
- @JVMOnly 注解
- Maven profile 配置
- ProGuard 配置
- 性能特点

### GraalVM Native Image

- [GraalVM 支持 →](graalvm.md)
- 反射配置
- 资源配置
- Tracing Agent 使用
- 构建命令

## 平台检测

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

## 降级策略

| 组件 | JVM | Android | Native Image |
|------|-----|---------|--------------|
| ASM Creator | 使用 | 回退到反射 | 回退到反射 |
| Unsafe 操作 | 使用 | 使用 | 使用 |
| String 内部 | 直接 | 标准 | 标准 |

## 相关文档

- [模块结构](modules.md)
- [设计决策](design-decisions.md)

[← 返回索引](README.md)

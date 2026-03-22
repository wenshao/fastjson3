# Android 优化指南

fastjson3 支持 Android 8+ (API 26+)，并提供专用 JAR 以减小体积。

## 系统要求

| 项目 | 要求 |
|------|------|
| 最低 Android 版本 | API 26 (Android 8.0) |
| 推荐版本 | API 28+ |
| Gradle 插件 | 8.1+ |
| Java 版本 | 17+ (D8 脱糖支持) |

---

## 添加依赖

### Gradle (Kotlin)

```kotlin
dependencies {
    implementation("com.alibaba:fastjson3:3.0.0:android@aar")
}
```

### Gradle (Groovy)

```gradle
dependencies {
    implementation 'com.alibaba:fastjson3:3.0.0:android@aar'
}
```

### Maven

```xml
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
    <classifier>android</classifier>
</dependency>
```

---

## Android 版本差异

### Android 版本 vs 普通 JAR

| 特性 | 普通 JAR | Android JAR |
|------|----------|------------|
| 文件大小 | ~400KB | ~340KB (-15%) |
| 类数量 | 148 类 | 128 类 |
| ASM 支持 | ✅ | ❌ |
| Vector API | ✅ | ❌ |
| 反射支持 | ✅ | ✅ |
| Unsafe 支持 | ✅ | ✅ |
| 性能 | 更高 (ASM) | 稍低 (反射) |

### 移除的组件

Android JAR 移除了以下 JVM 专用的组件：

- `internal/asm/*` (13 个类) - ASM 字节码操作
- `ObjectReaderCreatorASM` - ASM 读取器创建
- `ObjectWriterCreatorASM` - ASM 写入器创建
- `DynamicClassLoader` - 动态类加载

这些类标记为 `@JVMOnly` 注解，Android 构建会自动排除。

### 替换的组件

以下组件在 Android JAR 中使用优化实现：

- `JDKUtils` - Android 特定实现，禁用 Vector API 和快速字符串创建

---

## 构建机制

### @JVMOnly 注解

fastjson3 使用 `@JVMOnly` 注解标记需要在 Android 构建中排除的类：

```java
@com.alibaba.fastjson3.annotation.JVMOnly
public final class ASMUtils {
    // 这个类不会包含在 Android JAR 中
}
```

### @AndroidNative 注解

`@AndroidNative` 注解标记在 Android 上需要不同实现的类：

```java
@AndroidNative("Android version in core3-android module")
public final class JDKUtils {
    // 这个类会被 core3-android 中的实现替换
}
```

### 构建流程

```bash
# 构建 Android JAR
mvn clean package -Pandroid -pl core3

# 构建产物
# core3/target/fastjson3-3.0.0-SNAPSHOT-android.jar
```

构建过程：
1. 合并 `core3/src/main/java` 和 `core3-android/src/main/java` 源码
2. `@AndroidNative` 标记的类被 Android 版本覆盖
3. `@JVMOnly` 标记的类及其内部类从编译输出中删除
4. 生成带有 `android` classifier 的 JAR

---

## ProGuard/R8 配置

### 基本配置

```proguard
# fastjson3
-keep class com.alibaba.fastjson3.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# 如果使用注解
-keepclassmembers class * {
    @com.alibaba.fastjson3.annotation.JSONField <fields>;
    @com.alibaba.fastjson3.annotation.JSONType <class>;
}

# 如果使用泛型
-keepattributes Signature
```

### 完整配置

```proguard
# fastjson3 核心配置
-keep class com.alibaba.fastjson3.** { *; }
-dontwarn com.alibaba.fastjson3.**
-keep class sun.misc.Unsafe { *; }

# 注解相关
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.alibaba.fastjson3.annotation.JSONField <fields>;
    @com.alibaba.fastjson3.annotation.JSONType <class>;
    @com.alibaba.fastjson3.annotation.JSONCreator <methods>;
}

# 泛型支持
-keepattributes Signature
-keep class com.alibaba.fastjson3.TypeToken { *; }

# 枚举
-keepclassmembers enum com.alibaba.fastjson3.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
```

---

## 最佳实践

### 避免反射

在 Android 上，反射比 JVM 慢，建议使用注解明确配置：

```java
// ✅ 好：使用注解
@JSONType(naming = NamingStrategy.SnakeCase)
public class User {
    @JSONField(name = "user_id")
    private Long userId;

    @JSONField(name = "user_name")
    private String userName;
}

// ⚠️ 避免：依赖反射自动推断
```

### 使用字段访问

```java
// ✅ 好：字段访问
@JSONType(alphabetic = true)
public class Data {
    public String field1;
    public String field2;
}

// ⚠️ 避免：需要 getter/setter
```

### 复用 ObjectMapper

```java
// ✅ 好：单例
public class JsonHelper {
    private static final ObjectMapper MAPPER = ObjectMapper.shared();

    public static String toJSON(Object obj) {
        return MAPPER.writeValueAsString(obj);
    }
}

// ❌ 不好：每次创建
```

### 处理日期

```java
// ✅ 好：使用 Java Time API
@JSONField(format = "yyyy-MM-dd")
private LocalDate date;

@JSONField(format = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime dateTime;

// ⚠️ 避免：java.util.Date (需要时区处理)
```

---

## 性能优化

### 减少分配

```java
// ✅ 好：复用实例
private static final ObjectMapper MAPPER = ObjectMapper.shared();

// ✅ 好：使用 byte[]
byte[] bytes = JSON.toJSONBytes(obj);

// ❌ 不好：每次创建新实例
// ❌ 不好：使用 String（需要转换）
```

### 避免频繁类型转换

```java
// ✅ 好：直接使用需要的方法
String json = JSON.toJSONString(user);

// ❌ 不好：不必要的中间类型
JSONObject obj = JSON.parseObject(json);
String json = obj.toJSONString();
```

### 使用预编译 JSONPath

```java
// ✅ 好：预编译并复用
private static final JSONPath PATH = JSONPath.of("$.user.name");

// ❌ 不好：每次创建新实例
JSONPath path = JSONPath.of("$.user.name");
```

---

## 内存优化

### 避免内存泄漏

```java
// ✅ 好：使用静态实例
private static final ObjectMapper MAPPER = ObjectMapper.shared();

// ❌ 可能泄漏：持有 Activity 引用
private ObjectMapper mapper = ObjectMapper.builder().build();
```

### 及时释放

```java
// JSONObject/JSONArray 持有引用
JSONObject obj = JSON.parseObject(json);
// 使用完后
obj.clear();  // 释放内部引用
```

---

## 常见问题

### Multidex 支持

```gradle
android {
    defaultConfig {
        multiDexEnabled true
    }
}

dependencies {
    implementation 'androidx.multidex:multidex:2.0.1'
}
```

### Jack 编译器

fastjson3 不兼容 Jack 编译器（已废弃），使用 D8/R8。

### Java 8+ 特性

```gradle
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    // 启用脱糖
    coreLibraryDesugaringEnabled true
}

dependencies {
    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.4'
}
```

---

## 示例代码

### 基本序列化

```java
public class JsonHelper {
    private static final ObjectMapper MAPPER = ObjectMapper.shared();

    public static String toJSON(Object obj) {
        return MAPPER.writeValueAsString(obj);
    }

    public static byte[] toJSONBytes(Object obj) {
        return MAPPER.writeValueAsBytes(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return MAPPER.readValue(json, clazz);
    }

    public static <T> T fromJson(byte[] bytes, Class<T> clazz) {
        return MAPPER.readValue(bytes, clazz);
    }
}
```

### 数据类

```java
@JSONType(naming = NamingStrategy.SnakeCase)
public class User {
    @JSONField(name = "id")
    private Long userId;

    @JSONField(name = "name")
    private String userName;

    @JSONField(name = "created_at")
    private LocalDateTime createdAt;

    // getters & setters
}
```

### 在 ViewModel 中使用

```java
public class UserViewModel extends ViewModel {
    private static final ObjectMapper MAPPER = ObjectMapper.shared();

    private final MutableLiveData<User> user = new MutableLiveData<>();

    public void loadUser(String json) {
        User u = MAPPER.readValue(json, User.class);
        user.setValue(u);
    }

    public LiveData<User> getUser() {
        return user;
    }
}
```

---

## 测试

### 单元测试

```java
@Test
public void testSerialization() {
    User user = new User();
    user.setUserId(1L);
    user.setUserName("张三");

    String json = JSON.toJSONString(user);
    assertNotNull(json);
    assertTrue(json.contains("user_name"));
}

@Test
public void testDeserialization() {
    String json = "{\"id\":1,\"name\":\"张三\"}";
    User user = JSON.parseObject(json, User.class);
    assertEquals(Long.valueOf(1L), user.getUserId());
    assertEquals("张三", user.getUserName());
}
```

### Instrumented 测试

```java
@RunWith(AndroidJUnit4.class)
public class JsonTest {
    @Test
    public void testWithAndroidContext(Context context) {
        User user = new User();
        user.setUserName("测试");

        String json = JSON.toJSONString(user);
        User parsed = JSON.parseObject(json, User.class);

        assertEquals("测试", parsed.getUserName());
    }
}
```

---

## 检查清单

发布前检查：

- [ ] 使用 Android 专用 JAR
- [ ] 配置 ProGuard/R8 规则
- [ ] 使用注解而非反射
- [ ] 复用 ObjectMapper 实例
- [ ] 避免内存泄漏
- [ ] 测试序列化/反序列化
- [ ] 验证 JSON 输出格式
- [ ] 检查包大小影响

---

## 相关文档

- [GraalVM 配置 →](graalvm.md)
- [📖 性能调优 →](../guides/performance.md)
- [📖 POJO 序列化 →](../guides/pojo.md)
- [📖 注解说明 →](../api/annotations.md)

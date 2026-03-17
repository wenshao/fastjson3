# ASM 字节码生成

**性能提升**：~10-20% | **难度**：⭐⭐⭐⭐ | **适用场景**：反射替代

## 问题描述

Java 反射有性能开销：

```java
// ❌ 反射：每次调用都有开销
Field field = clazz.getDeclaredField("name");
field.setAccessible(true);
String value = (String) field.get(obj);  // 方法调用开销
```

**开销来源**：
- 方法调用虚表查找
- 参数校验
- 安全检查
- JIT 难以内联

## 解决方案：ASM 动态生成类

运行时生成专门的字节码类，直接访问字段：

```java
// ✅ ASM 生成的类（等价于）
public class ObjectWriter_User_1 implements ObjectWriter<User> {
    @Override
    public void write(JSONGenerator gen, Object object, long features) {
        User user = (User) object;
        gen.writeString(user.name);  // 直接字段访问，无反射
    }
}
```

**生成的字节码直接 `getfield`，JIT 可以完全内联。**

## 性能对比

| 方法 | 相对性能 | 说明 |
|------|----------|------|
| 反射 Reader | 100% | 基准 |
| ASM Reader | ~107% | **+7%** |
| 反射 Writer | 100% | 基准（已高度优化） |
| ASM Writer | ~100% | 持平 |

## 工作原理

### 1. 类生成时机

```java
// 首次请求时生成
ObjectWriter<?> writer = createObjectWriter(User.class);

// 检查缓存
ObjectWriter<?> cached = writerCache.get(User.class);
if (cached == null) {
    // 使用 ASM 生成
    cached = ASMUtils.createWriter(User.class);
    writerCache.put(User.class, cached);
}
return cached;
```

### 2. 生成的字节码示例

```java
// ASM 生成的 ObjectWriter
public class UserWriter implements ObjectWriter {
    public void write(JSONGenerator gen, Object object) {
        User user = (User) object;
        gen.writeFieldName("name");
        gen.writeString(user.name);      // 直接字段访问
        gen.writeFieldName("age");
        gen.writeInt(user.age);          // 直接字段访问
    }
}
```

对应的字节码（简化）：
```
ALOAD 1    // 加载 gen
LDC "name" // 常量
INVOKEVIRTUAL JSONGenerator.writeFieldName
ALOAD 2    // 加载 user
GETFIELD User.name    // 直接字段访问！
INVOKEVIRTUAL JSONGenerator.writeString
```

### 3. 字段访问对比

```java
// 反射方式（慢）
Field field = User.class.getDeclaredField("name");
field.setAccessible(true);
String value = (String) field.get(user);
// 字节码：invokevirtual Field.get (虚方法调用)

// ASM 生成方式（快）
String value = user.name;
// 字节码：getfield User.name (直接访问)
```

## ASM 基础

### 核心类

```java
import org.objectweb.asm.*;

// ClassWriter：生成类
ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

// MethodVisitor：生成方法
MethodVisitor mv = cw.visitMethod(
    ACC_PUBLIC,            // 访问修饰符
    "write",               // 方法名
    "(Ljava/lang/Object;)V", // 描述符
    null, null
);

// 生成字节码指令
mv.visitCode();
mv.visitVarInsn(ALOAD, 1);  // 加载参数
// ... 更多指令
mv.visitInsn(RETURN);
mv.visitMaxs(1, 1);
mv.visitEnd();
```

### 常用指令

| 指令 | 描述 | 示例 |
|------|------|------|
| `(ALOAD n)` | 加载引用变量到栈 | `ALOAD 0` 加载 this |
| `GETFIELD` | 获取实例字段 | `GETFIELD User.name` |
| `INVOKEVIRTUAL` | 调用实例方法 | `INVOKEVIRTUAL obj.method()` |
| `PUTFIELD` | 设置实例字段 | `PUTFIELD User.name` |

## 代码示例

### 简单的 ASM 生成

```java
public class ASMGenerator {
    public static byte[] generateWriter(Class<?> clazz) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // 定义类
        cw.visit(V17, ACC_PUBLIC, "GeneratedWriter", null, "java/lang/Object",
                 new String[]{"ObjectWriter"});

        // 生成 write 方法
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "write",
            "(LJSONGenerator;Ljava/lang/Object;)V", null, null);

        mv.visitCode();

        // 生成：User user = (User) object;
        mv.visitVarInsn(ALOAD, 2);
        mv.visitTypeInsn(CHECKCAST, "User");
        mv.visitVarInsn(ASTORE, 3);

        // 生成：gen.writeString(user.name);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitFieldInsn(GETFIELD, "User", "name", "Ljava/lang/String;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "JSONGenerator", "writeString",
                          "(Ljava/lang/String;)V", false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);  // 自动计算
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}
```

### 动态加载生成的类

```java
public class GeneratedClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}

// 使用
byte[] bytecode = ASMGenerator.generateWriter(User.class);
GeneratedClassLoader loader = new GeneratedClassLoader();
Class<?> writerClass = loader.defineClass("UserWriter", bytecode);
ObjectWriter writer = (ObjectWriter) writerClass.newInstance();
```

## 限制和注意事项

### Android 平台

Android 不支持 ASM，需要回退到反射：

```java
ObjectWriter<?> createWriter(Class<?> clazz) {
    if (JDKUtils.ANDROID) {
        return new ReflectWriter(clazz);  // 反射实现
    } else {
        return ASMUtils.createWriter(clazz);  // ASM 实现
    }
}
```

### GraalVM Native Image

需要配置反射元数据：

```json
{
  "-H:ConfigurationFileResources": "resources/reflect-config.json"
}
```

### 类加载器泄漏

生成的类需要正确管理类加载器：

```java
// ✅ 好：使用专用类加载器，可被 GC
ClassLoader tempLoader = new GeneratedClassLoader();

// ❌ 不好：使用线程上下文类加载器，可能导致泄漏
```

## 你可以学到什么

1. **反射瓶颈** - 高频调用时反射开销明显
2. **动态代码生成** - 运行时生成专门代码
3. **字节码优化** - `getfield` 比 `Method.invoke` 快
4. **JIT 友好** - 直接调用容易被内联

## 替代方案

如果不想使用 ASM：

### 1. MethodHandle（JDK 7+）

```java
MethodHandles.Lookup lookup = MethodHandles.lookup();
MethodHandle getter = lookup.findGetter(User.class, "name", String.class);
String value = (String) getter.invokeExact(user);
```

### 2. Lambda Metafactory（JDK 8+）

```java
MethodHandle mh = lookup.findVirtual(User.class, "getName",
    MethodType.methodType(String.class));
CallSite site = LambdaMetafactory.metafactory(
    lookup, "apply", MethodType.methodType(Function.class),
    mh.type(), mh, mh.type()
);
Function<User, String> getter = (Function<User, String>) site.getTarget().invoke();
```

### 3. VarHandle（JDK 9+）

```java
VarHandle handle = MethodHandles.lookup()
    .findVarHandle(User.class, "name", String.class);
String value = (String) handle.get(user);
```

## 参考资料

- [ASM 官方文档](https://asm.ow2.io/)
- [Java 字节码入门](https://docs.oracle.com/javase/specs/jvms/se8/html/)
- [JIT 编译优化](https://wiki.openjdk.org/display/HotSpot/JIT)

## 相关优化

- [转移计算](shift-computation.md) - CodeGen 阶段的优化
- [Type Tag 分派](type-tag-dispatch.md) - 避免多态调用

[← 返回索引](README.md)

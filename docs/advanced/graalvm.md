# GraalVM Native Image 指南

fastjson3 在 GraalVM native-image 下开箱可用。本指南说明如何：
- 注册你自己的 POJO（如何让库认识你的类）
- 在 CI 中烟雾测试
- 排查常见错误

## 前置条件

- GraalVM 24+（当前 schema 要求）。验证：`native-image --version`
- fastjson3 `3.0.0-SNAPSHOT` 或更新版本（更早版本的 metadata 是老 schema，GraalVM 24+ 会拒绝）

## 基本原理

fastjson3 自带的 `META-INF/native-image/reachability-metadata.json` 声明了库自己需要的反射钩子（`@JSONField` / `@JsonProperty` 注解、`String.value` / `String.coder` 字段、`sun.misc.Unsafe.theUnsafe`、`BufferPool$CacheItem` 等）。

**你需要额外注册的只有你自己的 POJO。** native-image 编译期会静态分析哪些类被反射访问，所以用户类也必须声明，否则运行时出 `NoSuchMethodException: Foo.<init>()` 或 `no suitable constructor found`。

## 注册你的 POJO

### 方式 A — 手写 reachability-metadata.json（推荐用于少量稳定 POJO）

在你项目的 `src/main/resources/META-INF/native-image/<groupId>/<artifactId>/` 目录下放一个 `reachability-metadata.json`：

```json
{
  "reflection": [
    {
      "type": "com.example.User",
      "unsafeAllocated": true,
      "allDeclaredFields": true,
      "methods": [
        { "name": "<init>", "parameterTypes": [] }
      ]
    }
  ]
}
```

要点：
- `unsafeAllocated: true` — 允许 fastjson3 的 Unsafe 字段直写路径构造实例（最快）。即便不开也能 work，只是退回到反射构造。
- `allDeclaredFields: true` — 所有字段都可被反射读写。
- `methods: [{ "name": "<init>", "parameterTypes": [] }]` — 显式保留无参构造器。
- `condition.typeReached` — 可选的条件触发；省略即无条件注册。

**schema 注意**：GraalVM 24+ 的 schema 用 `typeReached`（不是 `typeReachable`）、`allPublicMethods`（不是 `queryAllPublicMethods`）。见 [GraalVM 官方 schema](https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/ReachabilityMetadata.md)。

### 方式 B — 跑 native-image-agent 自动录制（推荐用于复杂/动态的 POJO 集合）

先在 JVM 上跑一遍典型工作负载，agent 会记录实际发生的反射访问：

```bash
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image/myapp \
     -jar target/myapp.jar

# 在此期间触发所有需要 JSON 解析/序列化的代码路径
# （跑你的单测、集成测试，或人工点一遍关键路径）

# 退出后 config 目录下会自动生成 reachability-metadata.json
```

这个文件可以直接提交，随后 `native-image` 编译会把它拉进去。

## 编译

```bash
native-image \
  --no-fallback \
  -cp "myapp.jar:deps/*" \
  -o myapp \
  com.example.Main
```

`--no-fallback` 强制失败而不是退回 JVM 模式——更容易定位缺失的配置。

在 Maven/Gradle 项目里用 [GraalVM native-maven-plugin](https://graalvm.github.io/native-build-tools/latest/maven-plugin.html)（或 Gradle 插件）来自动装配 classpath 和 metadata 目录。

## 烟雾测试脚本

仓库根目录有 `scripts/test-native-image.sh`：

```bash
./scripts/test-native-image.sh
```

它会：
1. 打 core3 主 jar
2. 编译 `NativeImageTest` 测试类
3. 给测试类写一份外挂 metadata（示范用户注册动作）
4. 调 `native-image` 编 binary
5. 跑 binary 验证 parse/write/list-parse 行为正确
6. 打印 JVM vs native 的 3 次冷启动时间对比

参考数字（GraalVM CE 25.0.2 / x86_64 16c）：

| 指标 | JVM | Native |
|---|---:|---:|
| 冷启动 | 114 ms | **2-3 ms**（~45×） |
| binary 大小 | — | 19.3 MB |
| 编译时间 | — | ~19 s |

## 运行时行为

fastjson3 在 native-image 下自动降级以下优化路径：

| 机制 | JVM | Native Image |
|---|---|---|
| ASM 字节码生成 | ✅ 启用 | ❌ 禁用，自动走 REFLECT 创建器 |
| `Unsafe` 直读字段 | ✅ 启用 | ✅ 启用 |
| `Unsafe.putObject(String, value)` fast path | ✅ 启用 | ❌ 禁用（SubstrateVM 限制），降级到 `new String(byte[], Charset)` |
| SWAR / Vector API string 扫描 | ✅ 启用 | ✅ 启用 |

用户代码无感 —— `JSON.parseObject` / `ObjectMapper.readValue` 在两种环境下返回同样的结果，只是吞吐量略有差异（REFLECT 路径通常比 ASM 路径慢 10-30%，取决于 POJO 形状）。

如果你想在代码里显式分支（比如跳过一些只在 JVM 有意义的启动期预热）：

```java
if (JDKUtils.NATIVE_IMAGE) {
    // native image 专用逻辑
}
```

## 常见错误

### `no suitable constructor found for com.example.Foo`

解析时抛这个 → `Foo` 没有被注册。在你的 `reachability-metadata.json` 里加一条方式 A 那样的条目，或者跑 agent 重录。

### `java.lang.NoSuchFieldException: <field>`

字段没被注册。确保你的 POJO 条目里有 `"allDeclaredFields": true` 而不是只声明了某几个字段。

### `Error parsing reflection configuration: 'typeReachable' condition can not be used with the latest schema`

老 schema 的 metadata 被现代 GraalVM（24+）加载了。把 `typeReachable` 改成 `typeReached`、`queryAllPublicMethods` 改成 `allPublicMethods`。

### `Warning: Could not register ... for reflection (build-time initialization required)`

有些类（Proxy、动态生成类等）需要在编译期初始化。加编译参数：

```
--initialize-at-build-time=com.example.SomeClass
```

### native-image 编译跑了很久才报错

正常情况下 fastjson3 相关的 binary 在 16c 机器上 20s 左右出结果。如果你的项目依赖多、代码大，可能需要 1-5 分钟。真卡住看 heap：`--gc=G1 -H:MaxHeapSize=8g` 等。

## 相关链接

- [GraalVM Reachability Metadata 规范](https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/ReachabilityMetadata.md)
- [Native Build Tools（Maven/Gradle 插件）](https://graalvm.github.io/native-build-tools/)
- fastjson3 自带 metadata：[`core3/src/main/resources/META-INF/native-image/reachability-metadata.json`](../../core3/src/main/resources/META-INF/native-image/reachability-metadata.json)
- 烟雾测试脚本：[`scripts/test-native-image.sh`](../../scripts/test-native-image.sh)
- [📖 Android 优化 →](android.md)
- [📖 性能调优 →](../guides/performance.md)

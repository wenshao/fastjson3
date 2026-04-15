package com.alibaba.fastjson3.samples.performance;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONPath;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.util.JDKUtils;

/**
 * 性能优化示例：演示 fastjson3 的性能最佳实践
 */
public class PerformanceExample {

    static class User {
        private String name;
        private int age;
        private String email;
        private boolean vip;
        private long balance;

        public User() {}

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        // getters & setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static void main(String[] args) {
        System.out.println("=== fastjson3 性能优化示例 ===\n");

        // 1. 复用 ObjectMapper 的重要性
        demonstrateMapperReuse();

        // 2. byte[] vs String 的性能差异
        demonstrateBytesVsString();

        // 3. 预编译 JSONPath 的性能
        demonstrateCompiledPath();

        // 4. 平台检测
        demonstratePlatformDetection();

        // 5. 安全配置
        demonstrateSafeConfiguration();
    }

    /**
     * 演示 1：复用 ObjectMapper 的重要性
     */
    private static void demonstrateMapperReuse() {
        System.out.println("1. 复用 ObjectMapper 的重要性");

        // ❌ 不好：每次创建新实例
        long start1 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            ObjectMapper mapper = ObjectMapper.builder().build();  // 每次创建
            mapper.writeValueAsString(new User("User" + i, 25));
        }
        long time1 = System.nanoTime() - start1;
        System.out.println("  每次创建: " + (time1 / 1_000_000) + " ms");

        // ✅ 好：复用共享实例
        ObjectMapper mapper = ObjectMapper.shared();
        long start2 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            mapper.writeValueAsString(new User("User" + i, 25));
        }
        long time2 = System.nanoTime() - start2;
        System.out.println("  复用实例: " + (time2 / 1_000_000) + " ms");
        System.out.println("  性能提升: " + (100.0 * (time1 - time2) / time1) + "%");
        System.out.println();
    }

    /**
     * 演示 2：byte[] vs String 的性能差异
     */
    private static void demonstrateBytesVsString() {
        System.out.println("2. byte[] vs String 性能差异");

        User user = new User("张三", 25);
        int iterations = 10000;

        // 字符串序列化
        long start1 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            JSON.toJSONString(user);
        }
        long time1 = System.nanoTime() - start1;

        // 字节数组序列化
        long start2 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            JSON.toJSONBytes(user);
        }
        long time2 = System.nanoTime() - start2;

        System.out.println("  toJSONString: " + (time1 / 1_000_000) + " ms");
        System.out.println("  toJSONBytes:  " + (time2 / 1_000_000) + " ms");
        System.out.println("  字节更快: " + (100.0 * (time1 - time2) / time1) + "%");
        System.out.println("  建议: 对于 UTF-8 数据优先使用 byte[]");
        System.out.println();
    }

    /**
     * 演示 3：预编译 JSONPath 的性能
     */
    private static void demonstrateCompiledPath() {
        System.out.println("3. 预编译 JSONPath 的性能");

        String json = """
            {"store":{"book":[{"price":10},{"price":20},{"price":30}]}}
            """;
        int iterations = 10000;

        // ❌ 不好：每次编译
        long start1 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            JSONPath path = JSONPath.of("$.store.book[*].price");
            path.extract(json, Object.class);
        }
        long time1 = System.nanoTime() - start1;

        // ✅ 好：预编译（复用同一个 JSONPath 实例）
        JSONPath path = JSONPath.of("$.store.book[*].price");
        long start2 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            path.extract(json, Object.class);
        }
        long time2 = System.nanoTime() - start2;

        System.out.println("  每次编译: " + (time1 / 1_000_000) + " ms");
        System.out.println("  预编译:   " + (time2 / 1_000_000) + " ms");
        System.out.println("  性能提升: " + (100.0 * (time1 - time2) / time1) + "%");
        System.out.println();
    }

    /**
     * 演示 4：平台检测
     */
    private static void demonstratePlatformDetection() {
        System.out.println("4. 平台检测");

        System.out.println("  NATIVE_IMAGE: " + JDKUtils.NATIVE_IMAGE);
        System.out.println("  UNSAFE_AVAILABLE: " + JDKUtils.UNSAFE_AVAILABLE);

        // GraalVM Native Image 会回退到反射
        if (JDKUtils.NATIVE_IMAGE) {
            System.out.println("  运行在 Native Image 中，ASM 不可用");
        } else {
            System.out.println("  运行在 JVM 中，ASM 可用");
        }
        System.out.println();
    }

    /**
     * 演示 5：安全配置
     */
    private static void demonstrateSafeConfiguration() {
        System.out.println("5. 安全配置");

        // ⚠️ 不安全配置（开发环境）
        System.out.println("  开发环境配置:");
        ObjectMapper devMapper = ObjectMapper.builder()
            .enableRead(
                com.alibaba.fastjson3.ReadFeature.AllowComments,
                com.alibaba.fastjson3.ReadFeature.AllowSingleQuotes
            )
            .enableWrite(com.alibaba.fastjson3.WriteFeature.PrettyFormat)
            .build();
        System.out.println("    - 允许注释: true");
        System.out.println("    - 允许单引号: true");
        System.out.println("    - 美化输出: true");

        // ✅ 安全配置（生产环境）
        System.out.println("\n  生产环境配置:");
        ObjectMapper prodMapper = ObjectMapper.builder()
            .disableRead(com.alibaba.fastjson3.ReadFeature.SupportAutoType)
            .disableRead(com.alibaba.fastjson3.ReadFeature.AllowComments)
            .enableRead(com.alibaba.fastjson3.ReadFeature.ErrorOnUnknownProperties)
            .build();
        System.out.println("    - AutoType: false");
        System.out.println("    - 注释: false");
        System.out.println("    - 严格模式: true");

        // 性能优化配置（AUTO provider 已是默认，JVM 上自动走 ASM 路径）
        System.out.println("\n  性能优化配置:");
        ObjectMapper perfMapper = ObjectMapper.builder()
            .enableWrite(com.alibaba.fastjson3.WriteFeature.OptimizedForAscii)
            .build();
        System.out.println("    - AUTO provider → ASM: 已默认启用");
        System.out.println("    - ASCII 优化: enabled");
    }

    /**
     * 性能测试辅助方法
     */
    private static void benchmark(String name, Runnable runnable, int iterations) {
        // 预热
        for (int i = 0; i < 1000; i++) {
            runnable.run();
        }

        // 测试
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            runnable.run();
        }
        long duration = System.nanoTime() - start;

        System.out.println(name + ": " + (duration / iterations) + " ns/op");
    }
}

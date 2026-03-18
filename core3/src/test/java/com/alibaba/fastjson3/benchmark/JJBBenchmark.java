package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.reader.ReaderCreatorType;

/**
 * JJB Parse User 场景性能测试
 * 测试嵌套 POJO 的解析性能，验证 ObjectReaderProvider context 机制是否引入性能开销
 */
public class JJBBenchmark {

    // 模拟 JJB 的嵌套 POJO 结构
    public static class Geo {
        public double lat;
        public double lng;
    }

    public static class Client {
        public long id;
        public int index;
        public String guid;
        public boolean isActive;
        public double balance;
        public int age;
        public String eyeColor;
        public String name;
        public String gender;
        public String company;
        public String email;
        public long phone;
        public String address;
        public String about;
        public String registered;
        public double latitude;
        public double longitude;
        public String[] tags;
        public Geo geo;  // 嵌套 POJO
    }

    public static class User {
        public long id;
        public int index;
        public String guid;
        public boolean isActive;
        public double balance;
        public int age;
        public String eyeColor;
        public String name;
        public String gender;
        public String company;
        public String email;
        public long phone;
        public String address;
        public String about;
        public String registered;
        public double latitude;
        public double longitude;
        public String[] tags;
        public Client client;  // 嵌套 POJO - 这是关键测试点
        public Geo geo;      // 嵌套 POJO
    }

    // 典型的 JJB User JSON
    private static final String USER_JSON = "{"
            + "\"id\":1,"
            + "\"index\":0,"
            + "\"guid\":\"test-guid\","
            + "\"isActive\":true,"
            + "\"balance\":1000.50,"
            + "\"age\":25,"
            + "\"eyeColor\":\"brown\","
            + "\"name\":\"Test User\","
            + "\"gender\":\"male\","
            + "\"company\":\"ACME\","
            + "\"email\":\"test@example.com\","
            + "\"phone\":1234567890,"
            + "\"address\":\"123 Main St\","
            + "\"about\":\"Test user\","
            + "\"registered\":\"2024-01-01\","
            + "\"latitude\":40.7128,"
            + "\"longitude\":-74.0060,"
            + "\"tags\":[\"tag1\",\"tag2\"],"
            + "\"client\":{"
            + "   \"id\":2,"
            + "   \"index\":1,"
            + "   \"guid\":\"client-guid\","
            + "   \"isActive\":true,"
            + "   \"balance\":500.25,"
            + "   \"age\":30,"
            + "   \"name\":\"Client\","
            + "   \"email\":\"client@example.com\","
            + "   \"geo\":{\"lat\":40.7,\"lng\":-74.0}"
            + "},"
            + "\"geo\":{\"lat\":40.7,\"lng\":-74.0}"
            + "}";

    private static final int WARMUP = 100_000;
    private static final int ITERATIONS = 1_000_000;

    public static void main(String[] args) throws Exception {
        System.out.println("==============================================");
        System.out.println("JJB Parse User 场景性能测试");
        System.out.println("==============================================");
        System.out.println("测试嵌套 POJO 解析性能");
        System.out.println("Warmup: " + WARMUP + ", Iterations: " + ITERATIONS);
        System.out.println();

        // Test 1: ASM Provider (with context mechanism)
        ObjectMapper asmMapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.ASM)
                .build();

        System.out.println("预热 ASM Provider...");
        for (int i = 0; i < WARMUP; i++) {
            asmMapper.readValue(USER_JSON, User.class);
        }

        System.out.println("测试 ASM Provider...");
        long asmStart = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            asmMapper.readValue(USER_JSON, User.class);
        }
        long asmTime = (System.nanoTime() - asmStart) / 1_000_000;
        System.out.printf("ASM:     %d ms (%d ops/sec)%n", asmTime, ITERATIONS * 1000 / asmTime);

        // 验证结果正确性
        User asmUser = asmMapper.readValue(USER_JSON, User.class);
        System.out.println("验证: user.name=" + asmUser.name + ", client.name=" +
            (asmUser.client != null ? asmUser.client.name : "null"));

        // Test 2: Reflect Provider
        ObjectMapper reflectMapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.REFLECT)
                .build();

        System.out.println();
        System.out.println("预热 Reflect Provider...");
        for (int i = 0; i < WARMUP; i++) {
            reflectMapper.readValue(USER_JSON, User.class);
        }

        System.out.println("测试 Reflect Provider...");
        long reflectStart = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            reflectMapper.readValue(USER_JSON, User.class);
        }
        long reflectTime = (System.nanoTime() - reflectStart) / 1_000_000;
        System.out.printf("Reflect: %d ms (%d ops/sec)%n", reflectTime, ITERATIONS * 1000 / reflectTime);

        User reflectUser = reflectMapper.readValue(USER_JSON, User.class);
        System.out.println("验证: user.name=" + reflectUser.name + ", client.name=" +
            (reflectUser.client != null ? reflectUser.client.name : "null"));

        // Test 3: AUTO Provider
        ObjectMapper autoMapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.AUTO)
                .build();

        System.out.println();
        System.out.println("预热 AUTO Provider...");
        for (int i = 0; i < WARMUP; i++) {
            autoMapper.readValue(USER_JSON, User.class);
        }

        System.out.println("测试 AUTO Provider...");
        long autoStart = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            autoMapper.readValue(USER_JSON, User.class);
        }
        long autoTime = (System.nanoTime() - autoStart) / 1_000_000;
        System.out.printf("AUTO:    %d ms (%d ops/sec)%n", autoTime, ITERATIONS * 1000 / autoTime);

        // Summary
        System.out.println();
        System.out.println("==============================================");
        System.out.println("性能对比:");
        System.out.printf("  ASM vs Reflect: %.2fx (%s)%n",
            (double) reflectTime / asmTime,
            reflectTime > asmTime ? "ASM 更快" : "Reflect 更快");
        System.out.printf("  ASM vs AUTO:    %.2fx (%s)%n",
            (double) autoTime / asmTime,
            autoTime > asmTime ? "ASM 更快" : "AUTO 更快");

        // 检查嵌套类型是否使用 ASM
        System.out.println();
        System.out.println("==============================================");
        System.out.println("嵌套类型 ObjectReader 检查:");
        checkReaderClass(asmMapper, "User", User.class);
        checkReaderClass(asmMapper, "Client", Client.class);
        checkReaderClass(asmMapper, "Geo", Geo.class);
    }

    private static void checkReaderClass(ObjectMapper mapper, String label, Class<?> type) {
        try {
            var reader = mapper.getObjectReader(type);
            String className = reader.getClass().getName();
            boolean isASM = className.contains("gen.OR_");
            System.out.printf("  %-10s: %s %s%n", label,
                isASM ? "✓ ASM" : "  Reflect",
                isASM ? "" : "(" + className + ")");
        } catch (Exception e) {
            System.out.printf("  %-10s: ERROR - %s%n", label, e.getMessage());
        }
    }
}

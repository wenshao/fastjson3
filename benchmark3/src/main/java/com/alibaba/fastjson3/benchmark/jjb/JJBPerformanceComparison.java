package com.alibaba.fastjson3.benchmark.jjb;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * fastjson2 vs fastjson3 JJB 场景性能对比测试
 *
 * 测试 User 和 Client 两个典型场景
 */
public class JJBPerformanceComparison {

    public static class Geo {
        public double lat = 40.7;
        public double lng = -74.0;
    }

    public static class Client {
        public long id = 2;
        public int index = 1;
        public String guid = "client-guid";
        public boolean isActive = true;
        public double balance = 500.25;
        public int age = 30;
        public String eyeColor = "blue";
        public String name = "Client";
        public String gender = "male";
        public String company = "ACME";
        public String[] emails = {"email1@example.com", "email2@example.com"};
        public long[] phones = {9876543210L, 1234567890L};
        public String address = "456 Oak St";
        public String about = "Client user";
        public String registered = "2024-01-01";
        public double latitude = 40.7;
        public double longitude = -74.0;
        public List<String> tags = new ArrayList<>();
        public Geo geo = new Geo();
    }

    public static class User {
        public long id = 1;
        public int index = 0;
        public String guid = "test-guid";
        public boolean isActive = true;
        public double balance = 1000.50;
        public int age = 25;
        public String eyeColor = "brown";
        public String name = "Test User";
        public String gender = "male";
        public String company = "ACME";
        public String email = "test@example.com";
        public String phone = "1234567890";
        public String address = "123 Main St";
        public String about = "Test user";
        public String registered = "2024-01-01";
        public double latitude = 40.7128;
        public double longitude = -74.0060;
        public String[] tags = {"tag1", "tag2", "premium"};
        public Geo geo = new Geo();
        public Client client = new Client();
    }

    private static final int WARMUP = 50_000;
    private static final int ITERATIONS = 1_000_000;

    public static void main(String[] args) throws Exception {
        System.out.println("==============================================");
        System.out.println("fastjson2 vs fastjson3 - JJB 场景性能对比");
        System.out.println("==============================================");
        System.out.println("Warmup: " + WARMUP + ", Iterations: " + ITERATIONS);
        System.out.println();

        // ==================== User 场景 ====================
        runUserBenchmark();

        System.out.println();
        System.out.println();

        // ==================== Client 场景 ====================
        runClientBenchmark();

        System.out.println();
        System.out.println("测试完成。");
    }

    private static void runUserBenchmark() {
        User user = new User();

        // fastjson2
        String json2 = com.alibaba.fastjson2.JSON.toJSONString(user);
        System.out.println("fastjson2 预热中...");
        for (int i = 0; i < WARMUP; i++) {
            com.alibaba.fastjson2.JSON.toJSONString(user);
        }

        System.out.println("fastjson2 User (20 fields + nested Client + Geo)...");
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            com.alibaba.fastjson2.JSON.toJSONString(user);
        }
        long time2 = (System.nanoTime() - start) / 1_000_000;
        long ops2 = ITERATIONS * 1000 / time2;

        // fastjson3
        ObjectMapper mapper3 = ObjectMapper.builder().build();
        System.out.println("fastjson3 预热中...");
        for (int i = 0; i < WARMUP; i++) {
            mapper3.writeValueAsString(user);
        }

        System.out.println("fastjson3 User (20 fields + nested Client + Geo)...");
        long start3 = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mapper3.writeValueAsString(user);
        }
        long time3 = (System.nanoTime() - start3) / 1_000_000;
        long ops3 = ITERATIONS * 1000 / time3;

        System.out.println();
        System.out.println("【User 场景】序列化性能对比:");
        System.out.println("─────────────────────────────────────────────");
        System.out.printf("%-20s %8d ms  %12d ops/sec  (100%%)%n", "fastjson2", time2, ops2);
        System.out.printf("%-20s %8d ms  %12d ops/sec  %5.1f%%%n", "fastjson3", time3, ops3, ops3 * 100.0 / ops2);
        System.out.printf("差异: %d ms, %+.1f%%\n", time3 - time2, (ops3 - ops2) * 100.0 / ops2);

        // 反序列化测试
        System.out.println();
        System.out.println("【User 场景】反序列化性能对比:");
        System.out.println("─────────────────────────────────────────────");

        // fastjson2 反序列化
        System.out.println("fastjson2 预热中...");
        for (int i = 0; i < WARMUP; i++) {
            com.alibaba.fastjson2.JSON.parseObject(json2, User.class);
        }

        System.out.println("fastjson2 User 反序列化...");
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            com.alibaba.fastjson2.JSON.parseObject(json2, User.class);
        }
        long parseTime2 = (System.nanoTime() - start) / 1_000_000;
        long parseOps2 = ITERATIONS * 1000 / parseTime2;

        // fastjson3 反序列化
        System.out.println("fastjson3 预热中...");
        for (int i = 0; i < WARMUP; i++) {
            mapper3.readValue(json2, User.class);
        }

        System.out.println("fastjson3 User 反序列化...");
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mapper3.readValue(json2, User.class);
        }
        long parseTime3 = (System.nanoTime() - start) / 1_000_000;
        long parseOps3 = ITERATIONS * 1000 / parseTime3;

        System.out.printf("%-20s %8d ms  %12d ops/sec  (100%%)%n", "fastjson2 反序列化", parseTime2, parseOps2);
        System.out.printf("%-20s %8d ms  %12d ops/sec  %5.1f%%%n", "fastjson3 反序列化", parseTime3, parseOps3, parseOps3 * 100.0 / parseOps2);
        System.out.printf("差异: %d ms, %+.1f%%\n", parseTime3 - parseTime2, (parseOps3 - parseOps2) * 100.0 / parseOps2);
    }

    private static void runClientBenchmark() {
        Client client = new Client();

        // fastjson2
        String json2 = com.alibaba.fastjson2.JSON.toJSONString(client);
        System.out.println("fastjson2 预热中...");
        for (int i = 0; i < WARMUP; i++) {
            com.alibaba.fastjson2.JSON.toJSONString(client);
        }

        System.out.println("fastjson2 Client (20 fields + nested Geo)...");
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            com.alibaba.fastjson2.JSON.toJSONString(client);
        }
        long time2 = (System.nanoTime() - start) / 1_000_000;
        long ops2 = ITERATIONS * 1000 / time2;

        // fastjson3
        ObjectMapper mapper3 = ObjectMapper.builder().build();
        System.out.println("fastjson3 预热中...");
        for (int i = 0; i < WARMUP; i++) {
            mapper3.writeValueAsString(client);
        }

        System.out.println("fastjson3 Client (20 fields + nested Geo)...");
        long start3 = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mapper3.writeValueAsString(client);
        }
        long time3 = (System.nanoTime() - start3) / 1_000_000;
        long ops3 = ITERATIONS * 1000 / time3;

        System.out.println();
        System.out.println("【Client 场景】序列化性能对比:");
        System.out.println("─────────────────────────────────────────────");
        System.out.printf("%-20s %8d ms  %12d ops/sec  (100%%)%n", "fastjson2", time2, ops2);
        System.out.printf("%-20s %8d ms  %12d ops/sec  %5.1f%%%n", "fastjson3", time3, ops3, ops3 * 100.0 / ops2);
        System.out.printf("差异: %d ms, %+.1f%%\n", time3 - time2, (ops3 - ops2) * 100.0 / ops2);

        // 反序列化测试
        System.out.println();
        System.out.println("【Client 场景】反序列化性能对比:");
        System.out.println("─────────────────────────────────────────────");

        // fastjson2 反序列化
        System.out.println("fastjson2 预热中...");
        for (int i = 0; i < WARMUP; i++) {
            com.alibaba.fastjson2.JSON.parseObject(json2, Client.class);
        }

        System.out.println("fastjson2 Client 反序列化...");
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            com.alibaba.fastjson2.JSON.parseObject(json2, Client.class);
        }
        long parseTime2 = (System.nanoTime() - start) / 1_000_000;
        long parseOps2 = ITERATIONS * 1000 / parseTime2;

        // fastjson3 反序列化
        System.out.println("fastjson3 预热中...");
        for (int i = 0; i < WARMUP; i++) {
            mapper3.readValue(json2, Client.class);
        }

        System.out.println("fastjson3 Client 反序列化...");
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mapper3.readValue(json2, Client.class);
        }
        long parseTime3 = (System.nanoTime() - start) / 1_000_000;
        long parseOps3 = ITERATIONS * 1000 / parseTime3;

        System.out.printf("%-20s %8d ms  %12d ops/sec  (100%%)%n", "fastjson2 反序列化", parseTime2, parseOps2);
        System.out.printf("%-20s %8d ms  %12d ops/sec  %5.1f%%%n", "fastjson3 反序列化", parseTime3, parseOps3, parseOps3 * 100.0 / parseOps2);
        System.out.printf("差异: %d ms, %+.1f%%\n", parseTime3 - parseTime2, (parseOps3 - parseOps2) * 100.0 / parseOps2);
    }
}

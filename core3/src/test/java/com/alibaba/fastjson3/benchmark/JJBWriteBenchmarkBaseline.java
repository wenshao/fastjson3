package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.ObjectMapper;

/**
 * JJB Write User 场景性能测试
 */
public class JJBWriteBenchmarkBaseline {

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
        public String email = "client@example.com";
        public long phone = 9876543210L;
        public String address = "456 Oak St";
        public String about = "Client user";
        public String registered = "2024-01-01";
        public double latitude = 40.7;
        public double longitude = -74.0;
        public String[] tags = {"tag3", "tag4"};
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
        public long phone = 1234567890L;
        public String address = "123 Main St";
        public String about = "Test user";
        public String registered = "2024-01-01";
        public double latitude = 40.7128;
        public double longitude = -74.0060;
        public String[] tags = {"tag1", "tag2"};
        public Client client = new Client();
        public Geo geo = new Geo();
    }

    private static final int WARMUP = 100_000;
    private static final int ITERATIONS = 1_000_000;

    public static void main(String[] args) throws Exception {
        System.out.println("==============================================");
        System.out.println("JJB Write User 场景性能测试 (BASELINE on main)");
        System.out.println("==============================================");
        System.out.println("Warmup: " + WARMUP + ", Iterations: " + ITERATIONS);
        System.out.println();

        User user = new User();

        // Baseline: default ObjectMapper
        ObjectMapper mapper = ObjectMapper.builder().build();

        System.out.println("预热...");
        for (int i = 0; i < WARMUP; i++) {
            mapper.writeValueAsString(user);
        }

        System.out.println("测试 User (20 fields + nested Client + Geo)...");
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mapper.writeValueAsString(user);
        }
        long time = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("User: %d ms (%d ops/sec)%n", time, ITERATIONS * 1000 / time);

        System.out.println();
        System.out.println("测试 Client (20 fields + nested Geo)...");
        Client client = new Client();
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mapper.writeValueAsString(client);
        }
        time = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("Client: %d ms (%d ops/sec)%n", time, ITERATIONS * 1000 / time);

        System.out.println();
        System.out.println("测试完成。");
    }
}

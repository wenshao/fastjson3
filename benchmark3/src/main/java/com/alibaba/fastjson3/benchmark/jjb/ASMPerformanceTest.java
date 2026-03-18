package com.alibaba.fastjson3.benchmark.jjb;

import com.alibaba.fastjson3.JSON;

/**
 * Simple performance test for JSON parsing.
 */
public class ASMPerformanceTest {
    public static void main(String[] args) throws Exception {
        // Load test data
        byte[] userBytes = loadResource("data/jjb/user.json");
        byte[] clientBytes = loadResource("data/jjb/client.json");

        // Verify data loaded
        System.out.println("=== Testing JSON Performance ===");
        System.out.println("User data size: " + userBytes.length + " bytes");
        System.out.println("Client data size: " + clientBytes.length + " bytes");
        System.out.println();
        for (int i = 0; i < 10000; i++) {
            JSON.parseObject(userBytes, Users.class);
            JSON.parseObject(clientBytes, Clients.class);
        }
        System.out.println("Warmup complete");
        System.out.println();

        // Test 1: Users Parse
        int iterations = 100000;
        long start, end;

        System.out.println("=== Users Parse (" + iterations + " iterations) ===");
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            JSON.parseObject(userBytes, Users.class);
        }
        end = System.nanoTime();
        long usersTime = (end - start) / 1000; // microseconds
        System.out.printf("Time: %,d us\n", usersTime);
        System.out.printf("Ops/s: %,.0f\n", iterations * 1000000000.0 / (usersTime * 1000));
        System.out.printf("Avg: %.3f us/op\n\n", (double) usersTime / iterations);

        // Test 2: Clients Parse
        System.out.println("=== Clients Parse (" + iterations + " iterations) ===");
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            JSON.parseObject(clientBytes, Clients.class);
        }
        end = System.nanoTime();
        long clientsTime = (end - start) / 1000;
        System.out.printf("Time: %,d us\n", clientsTime);
        System.out.printf("Ops/s: %,.0f\n", iterations * 1000000000.0 / (clientsTime * 1000));
        System.out.printf("Avg: %.3f us/op\n\n", (double) clientsTime / iterations);

        // Summary
        System.out.println("=== Summary ===");
        long totalTime = usersTime + clientsTime;
        System.out.printf("Total: %,d us\n", totalTime);
        System.out.printf("Total ops/s: %,.0f\n", 2L * iterations * 1000000000.0 / (totalTime * 1000));
    }

    private static byte[] loadResource(String path) {
        try (java.io.InputStream is = ASMPerformanceTest.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            return is.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }
}

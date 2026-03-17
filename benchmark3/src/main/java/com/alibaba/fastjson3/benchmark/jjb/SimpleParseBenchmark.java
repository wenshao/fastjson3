package com.alibaba.fastjson3.benchmark.jjb;

import java.io.InputStream;

/**
 * Simple performance comparison between fastjson2 and fastjson3 JJB parse benchmark.
 */
public class SimpleParseBenchmark {
    static byte[] utf8Bytes;

    static {
        try {
            InputStream is = SimpleParseBenchmark.class.getClassLoader().getResourceAsStream("data/jjb/client.json");
            utf8Bytes = is.readAllBytes();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("JJB Parse Benchmark: fastjson2 vs fastjson3");
        System.out.println("===============================================");
        System.out.println("Test file: client.json (" + utf8Bytes.length + " bytes)");
        System.out.println();

        final int WARMUP_ROUNDS = 5;
        final int TEST_ROUNDS = 10;
        final int ITERATIONS_PER_ROUND = 100_000;

        // Warmup
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            testFastjson2(ITERATIONS_PER_ROUND);
            testFastjson3(ITERATIONS_PER_ROUND);
        }
        System.out.println("Warmup complete.");
        System.out.println();

        // Test fastjson2
        System.out.println("--- Testing fastjson2 ---");
        long fj2Total = 0;
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testFastjson2(ITERATIONS_PER_ROUND);
            fj2Total += t;
            System.out.printf("  Round %2d: %,d ms (%,.1f ops/ms)%n", i+1, t, ITERATIONS_PER_ROUND/(double)t);
        }
        long fj2Avg = fj2Total / TEST_ROUNDS;
        double fj2OpsPerMs = ITERATIONS_PER_ROUND / (double) fj2Avg;
        System.out.printf("  Average: %,d ms (%,.1f ops/ms)%n%n", fj2Avg, fj2OpsPerMs);

        // Test fastjson3
        System.out.println("--- Testing fastjson3 ---");
        long fj3Total = 0;
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testFastjson3(ITERATIONS_PER_ROUND);
            fj3Total += t;
            System.out.printf("  Round %2d: %,d ms (%,.1f ops/ms)%n", i+1, t, ITERATIONS_PER_ROUND/(double)t);
        }
        long fj3Avg = fj3Total / TEST_ROUNDS;
        double fj3OpsPerMs = ITERATIONS_PER_ROUND / (double) fj3Avg;
        System.out.printf("  Average: %,d ms (%,.1f ops/ms)%n%n", fj3Avg, fj3OpsPerMs);

        // Summary
        System.out.println("===============================================");
        System.out.println("SUMMARY");
        System.out.println("===============================================");
        System.out.printf("fastjson2: %,.1f ops/ms%n", fj2OpsPerMs);
        System.out.printf("fastjson3: %,.1f ops/ms%n", fj3OpsPerMs);
        double improvement = ((fj3Avg - fj2Avg) / (double) fj2Avg) * 100;
        System.out.printf("fastjson3 is %.2f%% %s than fastjson2%n",
            Math.abs(improvement),
            improvement < 0 ? "faster" : "slower");
        System.out.println("===============================================");
    }

    static long testFastjson2(int iterations) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            com.alibaba.fastjson2.JSON.parseObject(utf8Bytes, Clients.class);
        }
        return System.currentTimeMillis() - start;
    }

    static long testFastjson3(int iterations) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            com.alibaba.fastjson3.JSON.parseObject(utf8Bytes, Clients.class);
        }
        return System.currentTimeMillis() - start;
    }
}

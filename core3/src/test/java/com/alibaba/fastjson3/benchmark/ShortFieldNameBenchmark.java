package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;

/**
 * Benchmark to evaluate short field name optimization potential.
 * Tests parsing JSON with many short field names (1-4 chars).
 */
public class ShortFieldNameBenchmark {

    // JSON with many short field names
    static final String JSON_SHORT_FIELDS;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        // Common short field names: id, name, age, x, y, z, a, b
        for (int i = 0; i < 1000; i++) {
            sb.append("\"id\":").append(i).append(",");
            sb.append("\"n\":\"user").append(i).append("\",");
            sb.append("\"a\":").append(25 + i % 50).append(",");
            sb.append("\"x\":").append(i * 1.5).append(",");
        }
        sb.append("\"end\":true}");
        JSON_SHORT_FIELDS = sb.toString();
    }

    static final byte[] JSON_BYTES = JSON_SHORT_FIELDS.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    static volatile long sink;

    static final int WARMUP_ROUNDS = 10;
    static final int TEST_ROUNDS = 10;
    static final int ITERATIONS = 1000;

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("Short Field Name Benchmark");
        System.out.println("===============================================");
        System.out.println("Data size: " + JSON_BYTES.length + " bytes");
        System.out.println("Contains ~4000 short field names (id, n, a, x)");
        System.out.println("Iterations per round: " + ITERATIONS);
        System.out.println();

        // Warmup
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            testParse();
        }
        System.out.println("Warmup complete.");
        System.out.println();

        // Run benchmark
        System.out.println("--- Running benchmark ---");
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;

        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testParse();
            totalTime += t;
            if (t < minTime) minTime = t;
            double opsMs = ITERATIONS * 1000.0 / t;
            System.out.printf("  Round %2d: %,d ms (%,.0f ops/s)%n", i+1, t, opsMs);
        }

        long avgTime = totalTime / TEST_ROUNDS;
        double avgOpsPerSec = ITERATIONS * 1000.0 / avgTime;

        System.out.println();
        System.out.println("===============================================");
        System.out.println("RESULTS");
        System.out.println("===============================================");
        System.out.printf("Average: %,d ms%n", avgTime);
        System.out.printf("Min:     %,d ms%n", minTime);
        System.out.printf("Throughput: %,.0f ops/s%n", avgOpsPerSec);
        System.out.println("===============================================");

        // Verify correctness
        JSONObject obj = JSON.parseObject(JSON_BYTES);
        System.out.println();
        System.out.println("Correctness check:");
        System.out.println("  id: " + obj.getIntValue("id"));
        System.out.println("  n: " + obj.getString("n"));
        System.out.println("  a: " + obj.getIntValue("a"));
        System.out.println("  x: " + obj.getDoubleValue("x"));

        System.out.println();
        System.out.println("Short field name optimization analysis:");
        System.out.println("  - Most field names are 1-2 characters");
        System.out.println("  - Unrolled 1-4 char paths could reduce loop overhead");
        System.out.println("  - Current 2-byte unrolled loop is already efficient");
    }

    static long testParse() {
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            JSONObject obj = JSON.parseObject(JSON_BYTES);
            sink = obj.size();
        }
        return (System.nanoTime() - start) / 1_000_000;
    }
}

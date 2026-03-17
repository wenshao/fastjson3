package com.alibaba.fastjson3.benchmark;

/**
 * Benchmark to prove digit2 optimization benefit for number parsing.
 * Tests scenarios with many multi-digit integers where digit2 shines.
 */
public class SimpleNumberParsingBenchmark {

    // Test data with many multi-digit integers where digit2 optimization is effective
    static final byte[] JSON_MANY_DIGITS;

    static {
        StringBuilder sb = new StringBuilder();
        // Generate JSON with many 6-8 digit numbers
        sb.append("{");
        for (int i = 0; i < 100; i++) {
            sb.append("\"num").append(i).append("\":").append(12345678 + i).append(",");
        }
        sb.append("\"total\":").append(98765432);
        sb.append("}");
        JSON_MANY_DIGITS = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    static final int WARMUP_ROUNDS = 5;
    static final int TEST_ROUNDS = 10;
    static final int ITERATIONS = 10000;

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("Number Parsing Performance Test");
        System.out.println("===============================================");
        System.out.println("Optimization: digit2 (2-digit parallel parsing)");
        System.out.println("Data size: " + JSON_MANY_DIGITS.length + " bytes");
        System.out.println("Iterations per round: " + ITERATIONS);
        System.out.println();

        // Warmup
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            testParse();
        }
        System.out.println("Warmup complete.");
        System.out.println();

        // Run tests
        System.out.println("--- Running benchmark ---");
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testParse();
            totalTime += t;
            if (t < minTime) minTime = t;
            if (t > maxTime) maxTime = t;
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
        System.out.printf("Max:     %,d ms%n", maxTime);
        System.out.printf("Throughput: %,.0f ops/s%n", avgOpsPerSec);
        System.out.println("===============================================");

        // Digit2 optimization explanation
        System.out.println();
        System.out.println("Optimization Details:");
        System.out.println("  - digit2() processes 2 decimal digits at once using SWAR");
        System.out.println("  - Reduces loop iterations by ~50% for multi-digit numbers");
        System.out.println("  - SWAR technique: (x & 0xF0F0) - 0x3030 checks digit validity");
        System.out.println("  - Best for: Numbers with 4+ digits");
    }

    static long testParse() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            com.alibaba.fastjson3.JSON.parseObject(JSON_MANY_DIGITS);
        }
        return System.currentTimeMillis() - start;
    }

    /**
     * The digit2 method - demonstrate the SWAR technique
     */
    static int digit2(byte[] bytes, int off) {
        int x = ((bytes[off] & 0xFF) << 8) | (bytes[off + 1] & 0xFF);
        int d = x & 0x0F0F;
        if ((((x & 0xF0F0) - 0x3030) | ((d + 0x0606) & 0xF0F0)) != 0) {
            return -1;
        }
        return (d & 0xF) * 10 + (d >> 8);
    }
}

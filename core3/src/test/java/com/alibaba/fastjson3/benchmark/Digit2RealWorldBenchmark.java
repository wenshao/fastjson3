package com.alibaba.fastjson3.benchmark;

/**
 * Benchmark comparing digit2 vs single-digit for REAL-WORLD scenarios.
 * Tests larger multi-digit numbers (6-8 digits) where digit2 excels.
 */
public class Digit2RealWorldBenchmark {

    // Test data: 8-digit numbers like timestamps, IDs, etc.
    static final byte[] LARGE_NUMBERS;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < 500; i++) {
            sb.append(12345678 + i);
            sb.append(",");
        }
        sb.append("98765432]");
        LARGE_NUMBERS = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    static volatile int sink;

    static final int WARMUP_ROUNDS = 5;
    static final int TEST_ROUNDS = 10;
    static final int ITERATIONS = 10000;

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("Digit2 Real-World Benchmark (8-digit numbers)");
        System.out.println("===============================================");
        System.out.println("Data: " + LARGE_NUMBERS.length + " bytes");
        System.out.println("Contains 501 numbers, each 8 digits");
        System.out.println("Iterations per round: " + ITERATIONS);
        System.out.println();

        // Warmup
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            sink = testParseSingle();
            sink = testParseDigit2();
        }
        System.out.println("Warmup complete.");
        System.out.println();

        // Test single-digit (baseline)
        System.out.println("--- Single-digit parsing (old way) ---");
        long totalTime1 = 0;
        long minTime1 = Long.MAX_VALUE;
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testParseSingle();
            totalTime1 += t;
            if (t < minTime1) minTime1 = t;
            double opsMs = ITERATIONS * 1000.0 / t;
            System.out.printf("  Round %2d: %,d ms (%,.0f ops/s)%n", i+1, t, opsMs);
        }
        long avgTime1 = totalTime1 / TEST_ROUNDS;

        System.out.println();
        System.out.println("--- digit2 parsing (optimized) ---");
        long totalTime2 = 0;
        long minTime2 = Long.MAX_VALUE;
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testParseDigit2();
            totalTime2 += t;
            if (t < minTime2) minTime2 = t;
            double opsMs = ITERATIONS * 1000.0 / t;
            System.out.printf("  Round %2d: %,d ms (%,.0f ops/s)%n", i+1, t, opsMs);
        }
        long avgTime2 = totalTime2 / TEST_ROUNDS;

        System.out.println();
        System.out.println("===============================================");
        System.out.println("COMPARISON RESULTS");
        System.out.println("===============================================");
        System.out.printf("Single-digit avg: %,d ms%n", avgTime1);
        System.out.printf("digit2 avg:      %,d ms%n", avgTime2);
        double improvement = ((double)(avgTime1 - avgTime2) / avgTime1) * 100;
        System.out.printf("Improvement:     %.1f%%%n", improvement);
        double speedup = (double)avgTime1 / avgTime2;
        System.out.printf("Speedup:         %.2fx%n", speedup);
        System.out.println("===============================================");

        System.out.println();
        System.out.println("Why digit2 is faster for large numbers:");
        System.out.println("  - Single-digit: 8 iterations per number = 4008 iterations");
        System.out.println("  - digit2:       4 iterations per number = 2004 iterations");
        System.out.println("  - 50% fewer loop iterations = major speedup");
    }

    /**
     * Parse using single-digit method (old way)
     */
    static int testParseSingle() {
        long start = System.nanoTime();
        int sum = 0;
        byte[] b = LARGE_NUMBERS;
        int len = b.length;

        for (int iter = 0; iter < ITERATIONS; iter++) {
            int off = 1; // skip '['
            while (off < len) {
                byte c = b[off];
                if (c == ',' || c == ']') {
                    off++;
                    continue;
                }
                // Parse single digits
                int val = 0;
                while (off < len) {
                    byte digit = b[off];
                    if (digit < '0' || digit > '9') break;
                    val = val * 10 + (digit - '0');
                    off++;
                }
                sum += val;
            }
        }
        sink = sum;
        return (int) ((System.nanoTime() - start) / 1_000_000);
    }

    /**
     * Parse using digit2 method (optimized)
     */
    static int testParseDigit2() {
        long start = System.nanoTime();
        int sum = 0;
        byte[] b = LARGE_NUMBERS;
        int len = b.length;

        for (int iter = 0; iter < ITERATIONS; iter++) {
            int off = 1; // skip '['
            while (off < len) {
                byte c = b[off];
                if (c == ',' || c == ']') {
                    off++;
                    continue;
                }
                // Parse using digit2 (2 digits at a time)
                int val = 0;
                while (off + 1 < len) {
                    int d2 = digit2(b, off);
                    if (d2 < 0) break;
                    val = val * 100 + d2;
                    off += 2;
                }
                // Handle remaining single digit if any
                if (off < len && b[off] >= '0' && b[off] <= '9') {
                    val = val * 10 + (b[off] - '0');
                    off++;
                }
                sum += val;
            }
        }
        sink = sum;
        return (int) ((System.nanoTime() - start) / 1_000_000);
    }

    /**
     * The digit2 method - SWAR technique for 2-digit parallel parsing
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

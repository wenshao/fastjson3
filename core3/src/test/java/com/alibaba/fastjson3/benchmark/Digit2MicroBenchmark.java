package com.alibaba.fastjson3.benchmark;

/**
 * Microbenchmark comparing digit2 vs single-digit parsing.
 * Tests ONLY the number parsing in isolation for fair comparison.
 */
public class Digit2MicroBenchmark {

    // Test data: array of digit pairs for parsing
    static final byte[] DIGITS;
    static final int[] EXPECTED;
    static {
        int len = 10000;
        byte[] b = new byte[len * 2];
        int[] exp = new int[len];
        for (int i = 0; i < len; i++) {
            b[i * 2] = '1';
            b[i * 2 + 1] = '2';
            exp[i] = 12;
        }
        DIGITS = b;
        EXPECTED = exp;
    }

    // Volatile result to prevent JIT optimization
    static volatile int sink;

    static final int WARMUP_ROUNDS = 5;
    static final int TEST_ROUNDS = 10;
    static final int ITERATIONS = 100000;

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("Digit2 Microbenchmark - Isolated Number Parsing");
        System.out.println("===============================================");
        System.out.println("Data: " + DIGITS.length + " bytes (" + EXPECTED.length + " numbers)");
        System.out.println("Iterations per round: " + ITERATIONS);
        System.out.println();

        // Warmup both
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            sink = testSingleDigit();
            sink = testDigit2();
        }
        System.out.println("Warmup complete.");
        System.out.println();

        // Test single-digit (baseline)
        System.out.println("--- Single-digit parsing (old way) ---");
        long totalTime1 = 0;
        long minTime1 = Long.MAX_VALUE;
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testSingleDigit();
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
            long t = testDigit2();
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

        // Verify correctness
        System.out.println();
        System.out.println("Correctness verification:");
        int sum1 = verifySingleDigit();
        int sum2 = verifyDigit2();
        System.out.println("  Single-digit sum: " + sum1);
        System.out.println("  digit2 sum:       " + sum2);
        System.out.println("  Match: " + (sum1 == sum2 ? "✓ PASS" : "✗ FAIL"));
    }

    /**
     * Baseline: Parse one digit at a time (the old way)
     */
    static int testSingleDigit() {
        long start = System.nanoTime();
        int sum = 0;
        for (int iter = 0; iter < ITERATIONS; iter++) {
            for (int i = 0; i < DIGITS.length; i += 2) {
                byte c1 = DIGITS[i];
                byte c2 = DIGITS[i + 1];
                // Single-digit parsing
                if (c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
                    int val = (c1 - '0') * 10 + (c2 - '0');
                    sum += val;
                }
            }
        }
        sink = sum; // Prevent dead code elimination
        return (int) ((System.nanoTime() - start) / 1_000_000);
    }

    /**
     * Optimized: Parse two digits at once using digit2
     */
    static int testDigit2() {
        long start = System.nanoTime();
        int sum = 0;
        for (int iter = 0; iter < ITERATIONS; iter++) {
            for (int i = 0; i < DIGITS.length; i += 2) {
                int d = digit2(DIGITS, i);
                if (d >= 0) {
                    sum += d;
                }
            }
        }
        sink = sum; // Prevent dead code elimination
        return (int) ((System.nanoTime() - start) / 1_000_000);
    }

    /**
     * The digit2 method - SWAR technique
     */
    static int digit2(byte[] bytes, int off) {
        int x = ((bytes[off] & 0xFF) << 8) | (bytes[off + 1] & 0xFF);
        int d = x & 0x0F0F;
        if ((((x & 0xF0F0) - 0x3030) | ((d + 0x0606) & 0xF0F0)) != 0) {
            return -1;
        }
        return (d & 0xF) * 10 + (d >> 8);
    }

    static int verifySingleDigit() {
        int sum = 0;
        for (int i = 0; i < DIGITS.length; i += 2) {
            byte c1 = DIGITS[i];
            byte c2 = DIGITS[i + 1];
            if (c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
                sum += (c1 - '0') * 10 + (c2 - '0');
            }
        }
        return sum;
    }

    static int verifyDigit2() {
        int sum = 0;
        for (int i = 0; i < DIGITS.length; i += 2) {
            int d = digit2(DIGITS, i);
            if (d >= 0) {
                sum += d;
            }
        }
        return sum;
    }
}

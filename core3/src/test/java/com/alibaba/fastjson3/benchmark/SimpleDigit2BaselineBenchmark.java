package com.alibaba.fastjson3.benchmark;

/**
 * Baseline benchmark WITHOUT digit2 optimization.
 * Uses traditional single-digit parsing to prove the benefit of digit2.
 */
public class SimpleDigit2BaselineBenchmark {

    // Same test data as SimpleNumberParsingBenchmark for fair comparison
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
        System.out.println("BASELINE: Single-digit parsing (no digit2)");
        System.out.println("===============================================");
        System.out.println("Data size: " + JSON_MANY_DIGITS.length + " bytes");
        System.out.println("Iterations per round: " + ITERATIONS);
        System.out.println();

        // Warmup
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            testParseBaseline();
        }
        System.out.println("Warmup complete.");
        System.out.println();

        // Run tests
        System.out.println("--- Running baseline benchmark ---");
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testParseBaseline();
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
        System.out.println("BASELINE RESULTS (no digit2)");
        System.out.println("===============================================");
        System.out.printf("Average: %,d ms%n", avgTime);
        System.out.printf("Min:     %,d ms%n", minTime);
        System.out.printf("Max:     %,d ms%n", maxTime);
        System.out.printf("Throughput: %,.0f ops/s%n", avgOpsPerSec);
        System.out.println("===============================================");
    }

    /**
     * Baseline: Use old single-digit parsing method
     * This simulates the old code before digit2 optimization
     */
    static long testParseBaseline() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            parseOldWay(JSON_MANY_DIGITS);
        }
        return System.currentTimeMillis() - start;
    }

    /**
     * Old-school single-digit number parsing.
     * Compare this with digit2 to see the performance difference.
     */
    static int parseIntOldWay(byte[] b, int off, int e) {
        int value = 0;
        // Single digit at a time - the OLD way
        while (off < e) {
            byte c = b[off];
            if (c < '0' || c > '9') break;
            value = value * 10 + (c - '0');
            off++;
        }
        return value;
    }

    /**
     * Simple JSON parser that uses old single-digit parsing.
     * Minimal implementation to isolate the number parsing performance.
     */
    static Object parseOldWay(byte[] bytes) {
        int off = 0;
        int e = bytes.length;

        // Skip whitespace
        while (off < e && bytes[off] == ' ') off++;
        if (off >= e) return null;

        if (bytes[off] == '{') {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            off++;
            while (off < e && bytes[off] != '}') {
                if (bytes[off] == ',') off++;
                if (bytes[off] == '"') {
                    off++; // skip quote
                    // Read field name
                    int nameStart = off;
                    while (off < e && bytes[off] != '"') off++;
                    // Skip name processing - just read the value
                    off++; // skip closing quote
                    if (off < e && bytes[off] == ':') off++;
                    // Read number using OLD method
                    int value = parseIntOldWay(bytes, off, e);
                    // Advance past number
                    while (off < e && bytes[off] >= '0' && bytes[off] <= '9') off++;
                } else {
                    off++;
                }
            }
            return map;
        }
        return null;
    }
}

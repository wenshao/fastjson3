package com.alibaba.fastjson3.benchmark;

/**
 * Simple performance test to compare whitespace detection methods without JMH dependency.
 */
public class SimpleWhitespacePerfTest {

    static char[] TEST_CHARS;
    static byte[] TEST_BYTES;
    static final int WARMUP_ROUNDS = 5;
    static final int TEST_ROUNDS = 10;

    // Old method: array lookup
    static final boolean[] WHITESPACE_ARRAY = new boolean[256];
    static {
        WHITESPACE_ARRAY[' '] = true;
        WHITESPACE_ARRAY['\t'] = true;
        WHITESPACE_ARRAY['\n'] = true;
        WHITESPACE_ARRAY['\r'] = true;
    }

    // New method: bit mask
    static final long SPACE = 1L | (1L << ' ') | (1L << '\n') | (1L << '\r') | (1L << '\f') | (1L << '\t') | (1L << '\b');

    static {
        try {
            // Try to load from classpath, fallback to inline data
            String json;
            try (var is = SimpleWhitespacePerfTest.class.getClassLoader().getResourceAsStream("data/large.json")) {
                if (is != null) {
                    json = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                } else {
                    throw new RuntimeException("Resource not found");
                }
            } catch (Exception e) {
                // Fallback to synthetic data
                json = createSyntheticJson();
            }

            // Repeat to make it larger for more meaningful benchmark
            StringBuilder sb = new StringBuilder(json.length() * 100);
            for (int i = 0; i < 100; i++) {
                sb.append(json);
            }
            TEST_CHARS = sb.toString().toCharArray();
            TEST_BYTES = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }

    private static String createSyntheticJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"users\": [\n");
        for (int i = 0; i < 100; i++) {
            sb.append("    {\"id\": ").append(i).append(", \"name\": \"User").append(i).append("\",\n");
            sb.append("     \"email\": \"user").append(i).append("@example.com\"}");
            if (i < 99) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"metadata\": {\n");
        sb.append("    \"version\": \"1.0\",\n");
        sb.append("    \"count\": 100\n");
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Whitespace Detection Performance Test");
        System.out.println("=".repeat(80));
        System.out.println("Test data size: " + TEST_CHARS.length + " chars");
        System.out.println("Warmup rounds: " + WARMUP_ROUNDS);
        System.out.println("Test rounds: " + TEST_ROUNDS);
        System.out.println();

        // Warmup
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            testArrayLookupChar();
            testBitMaskChar();
            testSkipWhitespaceArray();
            testSkipWhitespaceBitMask();
        }
        System.out.println("Warmup complete.");
        System.out.println();

        // Run tests
        System.out.println("-".repeat(80));
        System.out.println("Running benchmarks...");
        System.out.println("-".repeat(80));
        System.out.println();

        long arrayTime = 0, bitMaskTime = 0;
        long arraySkipTime = 0, bitMaskSkipTime = 0;

        // Simple char lookup test
        System.out.println("Test 1: Char array lookup (OLD method)");
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testArrayLookupChar();
            arrayTime += t;
            System.out.printf("  Round %d: %,d ns (%,.3f ms)%n", i+1, t, t/1_000_000.0);
        }
        long avgArray = arrayTime / TEST_ROUNDS;
        System.out.printf("  Average: %,d ns (%,.3f ms)%n%n", avgArray, avgArray/1_000_000.0);

        System.out.println("Test 2: Char bit mask (NEW method)");
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testBitMaskChar();
            bitMaskTime += t;
            System.out.printf("  Round %d: %,d ns (%,.3f ms)%n", i+1, t, t/1_000_000.0);
        }
        long avgBitMask = bitMaskTime / TEST_ROUNDS;
        System.out.printf("  Average: %,d ns (%,.3f ms)%n%n", avgBitMask, avgBitMask/1_000_000.0);

        double improvement1 = ((double)(avgArray - avgBitMask) / avgArray) * 100;
        System.out.printf(">>> Improvement: %.2f%%%n%n", improvement1);

        // Skip whitespace simulation test (more realistic)
        System.out.println("Test 3: Skip whitespace - Array lookup (OLD method)");
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testSkipWhitespaceArray();
            arraySkipTime += t;
            System.out.printf("  Round %d: %,d ns (%,.3f ms)%n", i+1, t, t/1_000_000.0);
        }
        long avgArraySkip = arraySkipTime / TEST_ROUNDS;
        System.out.printf("  Average: %,d ns (%,.3f ms)%n%n", avgArraySkip, avgArraySkip/1_000_000.0);

        System.out.println("Test 4: Skip whitespace - Bit mask (NEW method)");
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testSkipWhitespaceBitMask();
            bitMaskSkipTime += t;
            System.out.printf("  Round %d: %,d ns (%,.3f ms)%n", i+1, t, t/1_000_000.0);
        }
        long avgBitMaskSkip = bitMaskSkipTime / TEST_ROUNDS;
        System.out.printf("  Average: %,d ns (%,.3f ms)%n%n", avgBitMaskSkip, avgBitMaskSkip/1_000_000.0);

        double improvement2 = ((double)(avgArraySkip - avgBitMaskSkip) / avgArraySkip) * 100;
        System.out.printf(">>> Improvement: %.2f%%%n%n", improvement2);

        // Summary
        System.out.println("=".repeat(80));
        System.out.println("SUMMARY");
        System.out.println("=".repeat(80));
        System.out.printf("Simple char lookup:    %.2f%% faster%n", improvement1);
        System.out.printf("Skip whitespace:       %.2f%% faster%n", improvement2);
        System.out.printf("Memory usage:          8 bytes (bit mask) vs 256 bytes (array)%n");
        System.out.println("=".repeat(80));
    }

    static long testArrayLookupChar() {
        long start = System.nanoTime();
        int spaceCount = 0;
        for (int i = 0; i < TEST_CHARS.length; i++) {
            char ch = TEST_CHARS[i];
            if (ch <= ' ' && WHITESPACE_ARRAY[ch]) {
                spaceCount++;
            }
        }
        long end = System.nanoTime();
        // Consume result to prevent optimization
        if (spaceCount == -1) System.out.println(spaceCount);
        return end - start;
    }

    static long testBitMaskChar() {
        long start = System.nanoTime();
        int spaceCount = 0;
        for (int i = 0; i < TEST_CHARS.length; i++) {
            char ch = TEST_CHARS[i];
            if (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                spaceCount++;
            }
        }
        long end = System.nanoTime();
        if (spaceCount == -1) System.out.println(spaceCount);
        return end - start;
    }

    static long testSkipWhitespaceArray() {
        long start = System.nanoTime();
        int offset = 0;
        int totalNonWs = 0;
        final int end = TEST_BYTES.length;

        while (offset < end) {
            // Skip whitespace using array lookup
            while (offset < end) {
                byte c = TEST_BYTES[offset];
                if (c > ' ' || !WHITESPACE_ARRAY[c & 0xFF]) {
                    break;
                }
                offset++;
            }

            if (offset >= end) break;

            // Count non-whitespace
            totalNonWs++;
            offset++;

            // Simulate some pattern
            if (offset % 50 == 0) {
                // Reset pattern
            }
        }
        long endNano = System.nanoTime();
        if (totalNonWs == -1) System.out.println(totalNonWs);
        return endNano - start;
    }

    static long testSkipWhitespaceBitMask() {
        long start = System.nanoTime();
        int offset = 0;
        int totalNonWs = 0;
        final int end = TEST_BYTES.length;

        while (offset < end) {
            // Skip whitespace using bit mask
            while (offset < end) {
                byte c = TEST_BYTES[offset];
                if (c > ' ' || ((1L << (c & 0xFF)) & SPACE) == 0) {
                    break;
                }
                offset++;
            }

            if (offset >= end) break;

            // Count non-whitespace
            totalNonWs++;
            offset++;

            // Simulate some pattern
            if (offset % 50 == 0) {
                // Reset pattern
            }
        }
        long endNano = System.nanoTime();
        if (totalNonWs == -1) System.out.println(totalNonWs);
        return endNano - start;
    }
}

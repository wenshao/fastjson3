package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;

/**
 * Benchmark to test CHAR1_ESCAPED lookup table optimization.
 * Tests parsing JSON with many escape sequences.
 */
public class EscapeCharBenchmark {

    // JSON with many escape sequences
    static final String JSON_WITH_ESCAPES;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 500; i++) {
            sb.append("\"field").append(i).append("\":\"");
            // Add various escape sequences
            sb.append("Text\\nwith\\tescape\\r\\nsequences\\\\");
            sb.append("\"");
            if (i < 499) sb.append(",");
        }
        sb.append("}");
        JSON_WITH_ESCAPES = sb.toString();
    }

    // JSON without escapes (baseline)
    static final String JSON_NO_ESCAPES;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 500; i++) {
            sb.append("\"field").append(i).append("\":\"");
            sb.append("Text with normal content");
            sb.append("\"");
            if (i < 499) sb.append(",");
        }
        sb.append("}");
        JSON_NO_ESCAPES = sb.toString();
    }

    static volatile int sink;

    static final int WARMUP_ROUNDS = 10;
    static final int TEST_ROUNDS = 10;
    static final int ITERATIONS = 1000;

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("Escape Character Lookup Table Benchmark");
        System.out.println("===============================================");

        // Warmup
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            testParseNoEscapes();
            testParseWithEscapes();
        }
        System.out.println("Warmup complete.");
        System.out.println();

        // Test without escapes
        System.out.println("--- Test 1: JSON without escapes ---");
        long totalTime1 = 0;
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testParseNoEscapes();
            totalTime1 += t;
            double opsMs = ITERATIONS * 1000.0 / t;
            System.out.printf("  Round %2d: %,d ms (%,.0f ops/s)%n", i+1, t, opsMs);
        }
        long avgTime1 = totalTime1 / TEST_ROUNDS;

        // Test with escapes
        System.out.println();
        System.out.println("--- Test 2: JSON with escape sequences ---");
        long totalTime2 = 0;
        for (int i = 0; i < TEST_ROUNDS; i++) {
            long t = testParseWithEscapes();
            totalTime2 += t;
            double opsMs = ITERATIONS * 1000.0 / t;
            System.out.printf("  Round %2d: %,d ms (%,.0f ops/s)%n", i+1, t, opsMs);
        }
        long avgTime2 = totalTime2 / TEST_ROUNDS;

        System.out.println();
        System.out.println("===============================================");
        System.out.println("RESULTS");
        System.out.println("===============================================");
        System.out.printf("No escapes avg:   %,d ms (%,.0f ops/s)%n", avgTime1, ITERATIONS * 1000.0 / avgTime1);
        System.out.printf("With escapes avg: %,d ms (%,.0f ops/s)%n", avgTime2, ITERATIONS * 1000.0 / avgTime2);
        System.out.println("===============================================");

        // Verify correctness
        System.out.println();
        System.out.println("Correctness verification:");
        String testJson = "{\"field0\":\"Text\\nwith\\tescape\\r\\nsequences\\\\\"}";
        JSONObject obj = JSON.parseObject(testJson);
        String field0 = obj.getString("field0");
        System.out.println("  field0: " + field0.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r").replaceAll("\t", "\\\\t"));
        if (field0.contains("\n") && field0.contains("\t") && field0.contains("\\")) {
            System.out.println("  ✓ Escape sequences parsed correctly!");
        } else {
            System.out.println("  ✗ Escape sequence parsing issue!");
        }

        System.out.println();
        System.out.println("Optimization: CHAR1_ESCAPED lookup table");
        System.out.println("  - Replaces switch statement with array lookup");
        System.out.println("  - Faster for single-char escapes (\\n, \\t, \\r, etc.)");
        System.out.println("  - Benefits: Reduced branch misprediction");
    }

    static long testParseNoEscapes() {
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            JSONObject obj = JSON.parseObject(JSON_NO_ESCAPES);
            sink = obj.size();
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    static long testParseWithEscapes() {
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            JSONObject obj = JSON.parseObject(JSON_WITH_ESCAPES);
            sink = obj.size();
        }
        return (System.nanoTime() - start) / 1_000_000;
    }
}

package com.alibaba.fastjson3.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark to compare whitespace detection methods:
 * 1. Array lookup (old fastjson3 method)
 * 2. Bit mask (new fastjson3 method, same as fastjson2)
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class WhitespaceBenchmark {

    // Test data with realistic whitespace distribution
    static char[] TEST_CHARS;
    static byte[] TEST_BYTES;

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
            // Read a sample JSON file for realistic data
            String json = Files.readString(Paths.get("src/test/resources/data/large.json"));
            TEST_CHARS = json.toCharArray();
            TEST_BYTES = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Fallback to synthetic data if file not found
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                sb.append("  {\n\t\"key\": \r\n");
                sb.append(i);
                sb.append("\t}\n");
            }
            String json = sb.toString();
            TEST_CHARS = json.toCharArray();
            TEST_BYTES = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    // ========== OLD METHOD (Array Lookup) ==========

    @Benchmark
    public void arrayLookupChar(Blackhole bh) {
        int spaceCount = 0;
        for (int i = 0; i < TEST_CHARS.length; i++) {
            char ch = TEST_CHARS[i];
            if (ch <= ' ' && WHITESPACE_ARRAY[ch]) {
                spaceCount++;
            }
        }
        bh.consume(spaceCount);
    }

    @Benchmark
    public void arrayLookupByte(Blackhole bh) {
        int spaceCount = 0;
        for (int i = 0; i < TEST_BYTES.length; i++) {
            byte b = TEST_BYTES[i];
            int ch = b & 0xFF;
            if (ch <= ' ' && WHITESPACE_ARRAY[ch]) {
                spaceCount++;
            }
        }
        bh.consume(spaceCount);
    }

    // ========== NEW METHOD (Bit Mask) ==========

    @Benchmark
    public void bitMaskChar(Blackhole bh) {
        int spaceCount = 0;
        for (int i = 0; i < TEST_CHARS.length; i++) {
            char ch = TEST_CHARS[i];
            if (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                spaceCount++;
            }
        }
        bh.consume(spaceCount);
    }

    @Benchmark
    public void bitMaskByte(Blackhole bh) {
        int spaceCount = 0;
        for (int i = 0; i < TEST_BYTES.length; i++) {
            byte b = TEST_BYTES[i];
            int ch = b & 0xFF;
            if (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                spaceCount++;
            }
        }
        bh.consume(spaceCount);
    }

    // ========== Skip Whitespace Simulation (more realistic) ==========

    @Benchmark
    public int skipWhitespaceArray(Blackhole bh) {
        int offset = 0;
        int totalSkipped = 0;
        final int end = TEST_BYTES.length;

        while (offset < end) {
            // Skip whitespace
            while (offset < end) {
                byte c = TEST_BYTES[offset];
                if (c > ' ' || !WHITESPACE_ARRAY[c & 0xFF]) {
                    break;
                }
                offset++;
            }

            if (offset >= end) break;

            // Simulate finding a non-whitespace char
            totalSkipped++;
            offset++;

            // Simulate some processing, then find more whitespace
            if (offset % 100 == 0) {
                // Reset pattern
            }
        }
        bh.consume(totalSkipped);
        return totalSkipped;
    }

    @Benchmark
    public int skipWhitespaceBitMask(Blackhole bh) {
        int offset = 0;
        int totalSkipped = 0;
        final int end = TEST_BYTES.length;

        while (offset < end) {
            // Skip whitespace
            while (offset < end) {
                byte c = TEST_BYTES[offset];
                if (c > ' ' || ((1L << (c & 0xFF)) & SPACE) == 0) {
                    break;
                }
                offset++;
            }

            if (offset >= end) break;

            // Simulate finding a non-whitespace char
            totalSkipped++;
            offset++;

            // Simulate some processing, then find more whitespace
            if (offset % 100 == 0) {
                // Reset pattern
            }
        }
        bh.consume(totalSkipped);
        return totalSkipped;
    }

    // ========== Comparison with standard library ==========

    @Benchmark
    public void characterIsWhitespace(Blackhole bh) {
        int spaceCount = 0;
        for (int i = 0; i < TEST_CHARS.length; i++) {
            char ch = TEST_CHARS[i];
            if (Character.isWhitespace(ch)) {
                spaceCount++;
            }
        }
        bh.consume(spaceCount);
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(WhitespaceBenchmark.class.getName())
                .build();
        new Runner(options).run();
    }
}

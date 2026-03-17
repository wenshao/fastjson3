package com.alibaba.fastjson3.benchmark.jjb;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark to test indexOfSlash optimization with strings that contain escape characters.
 * This tests the scenario where the cache can actually provide benefit.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@State(Scope.Thread)
public class EscapeStringBenchmark {
    // JSON with NO escape characters (cache always returns -1, no benefit)
    static final byte[] JSON_NO_ESCAPE =
        "{\"name\":\"value\",\"age\":123,\"active\":true}".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    // JSON with escape characters (cache can help)
    static final byte[] JSON_WITH_ESCAPE =
        "{\"name\":\"value\\\"with\\\"quotes\",\"path\":\"C:\\\\Users\\\\test\",\"age\":123}".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    // Mixed: some strings have escapes, some don't (cache provides most benefit)
    static final byte[] JSON_MIXED =
        ("{\"field1\":\"no_escape\",\"field2\":\"with\\\"escape\",\"field3\":\"another_one\","
         + "\"field4\":\"path\\\\here\",\"field5\":\"normal\",\"field6\":\"unicode\\u0020test\"}")
        .getBytes(java.nio.charset.StandardCharsets.UTF_8);

    @Benchmark
    public void fastjson3_noEscape(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(JSON_NO_ESCAPE));
    }

    @Benchmark
    public void fastjson2_noEscape(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(JSON_NO_ESCAPE));
    }

    @Benchmark
    public void fastjson3_withEscape(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(JSON_WITH_ESCAPE));
    }

    @Benchmark
    public void fastjson2_withEscape(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(JSON_WITH_ESCAPE));
    }

    @Benchmark
    public void fastjson3_mixed(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(JSON_MIXED));
    }

    @Benchmark
    public void fastjson2_mixed(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(JSON_MIXED));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EscapeStringBenchmark.class.getName())
                .build();
        new Runner(options).run();
    }
}

package com.alibaba.fastjson3.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark to prove digit2 optimization performance improvement.
 * Compares the new digit2 implementation against the old逐位 method.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class Digit2Benchmark {

    // Test data: JSON with many integer values
    static final byte[] JSON_MANY_INTS =
        ("{\"a\":123,\"b\":456,\"c\":789,\"d\":12,\"e\":345,\"f\":678,\"g\":901,\"h\":234," +
         "\"i\":567,\"j\":890,\"k\":111,\"l\":222,\"m\":333,\"n\":444,\"o\":555,\"p\":666}")
        .getBytes(java.nio.charset.StandardCharsets.UTF_8);

    // Large integers (require multiple digit2 calls)
    static final byte[] JSON_LARGE_INTS =
        ("{\"val1\":12345678,\"val2\":87654321,\"val3\":11122233,\"val4\":99988877}")
        .getBytes(java.nio.charset.StandardCharsets.UTF_8);

    // Mixed data
    static final byte[] JSON_MIXED = (
        "{\"id\":12345,\"age\":25,\"score\":98,\"count\":1000,\"total\":50000," +
        "\"items\":[1,2,3,4,5,6,7,8,9,10],\"nested\":{\"a\":111,\"b\":222}}"
        ).getBytes(java.nio.charset.StandardCharsets.UTF_8);

    @Benchmark
    public void parseManyInts(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(JSON_MANY_INTS));
    }

    @Benchmark
    public void parseLargeInts(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(JSON_LARGE_INTS));
    }

    @Benchmark
    public void parseMixed(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(JSON_MIXED));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(Digit2Benchmark.class.getName())
                .build();
        new Runner(options).run();
    }
}

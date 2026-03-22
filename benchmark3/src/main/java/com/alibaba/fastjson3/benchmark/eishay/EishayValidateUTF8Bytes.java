package com.alibaba.fastjson3.benchmark.eishay;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Validate JSON without building object tree — pure syntax check.
 * Tests the fast-path validation code (no object allocation).
 */
public class EishayValidateUTF8Bytes {
    static byte[] utf8Bytes;
    static String str;

    static {
        try {
            InputStream is = EishayValidateUTF8Bytes.class.getClassLoader()
                    .getResourceAsStream("data/eishay/eishay_compact.json");
            utf8Bytes = is.readAllBytes();
            str = new String(utf8Bytes, StandardCharsets.UTF_8);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2_bytes(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.isValid(utf8Bytes));
    }

    @Benchmark
    public void fastjson3_bytes(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.isValid(utf8Bytes));
    }

    @Benchmark
    public void fastjson2_string(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.isValid(str));
    }

    @Benchmark
    public void fastjson3_string(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.isValid(str));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayValidateUTF8Bytes.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(2)
                .measurementIterations(3)
                .forks(2)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}

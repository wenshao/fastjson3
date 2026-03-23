package com.alibaba.fastjson3.benchmark.eishay;

import com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

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
 * Parse pretty-printed (formatted with whitespace) JSON from String.
 */
public class EishayParseStringPretty {
    static String str;
    static final ObjectMapper jackson = new ObjectMapper();
    static final Gson gson = new Gson();

    static {
        try {
            try (InputStream is = EishayParseStringPretty.class.getClassLoader()
                    .getResourceAsStream("data/eishay/eishay.json")) {
                str = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(str, MediaContent.class));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(str, MediaContent.class));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(jackson.readValue(str, MediaContent.class));
    }

    @Benchmark
    public void gson(Blackhole bh) {
        bh.consume(gson.fromJson(str, MediaContent.class));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(str, MediaContent.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayParseStringPretty.class.getName())
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

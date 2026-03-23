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
 * Parse pretty-printed (formatted with whitespace) JSON from UTF-8 bytes.
 */
public class EishayParseUTF8BytesPretty {
    static byte[] utf8Bytes;
    static final ObjectMapper jackson = new ObjectMapper();
    static final Gson gson = new Gson();

    static {
        try {
            // eishay.json is the pretty-printed version
            try (InputStream is = EishayParseUTF8BytesPretty.class.getClassLoader()
                    .getResourceAsStream("data/eishay/eishay.json")) {
                utf8Bytes = is.readAllBytes();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(utf8Bytes, MediaContent.class));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(utf8Bytes, MediaContent.class));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(jackson.readValue(utf8Bytes, MediaContent.class));
    }

    @Benchmark
    public void gson(Blackhole bh) {
        bh.consume(gson.fromJson(new String(utf8Bytes, StandardCharsets.UTF_8), MediaContent.class));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(utf8Bytes, MediaContent.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayParseUTF8BytesPretty.class.getName())
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

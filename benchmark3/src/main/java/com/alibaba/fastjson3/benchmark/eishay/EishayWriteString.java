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
import java.util.concurrent.TimeUnit;

public class EishayWriteString {
    static MediaContent mc;
    static final ObjectMapper jackson = new ObjectMapper();
    static final Gson gson = new Gson();
    static com.alibaba.fastjson3.ObjectWriter<MediaContent> reflectWriter;
    static com.alibaba.fastjson3.ObjectWriter<MediaContent> asmWriter;

    static {
        try {
            InputStream is = EishayWriteString.class.getClassLoader()
                    .getResourceAsStream("data/eishay/eishay_compact.json");
            byte[] bytes = is.readAllBytes();
            mc = com.alibaba.fastjson2.JSON.parseObject(bytes, MediaContent.class);
            reflectWriter = com.alibaba.fastjson3.writer.ObjectWriterCreator.createObjectWriter(MediaContent.class);
            asmWriter = com.alibaba.fastjson3.writer.ObjectWriterCreatorASM.createObjectWriter(MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.toJSONString(mc));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.toJSONString(mc));
    }

    @Benchmark
    public void fastjson3_reflect(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONGenerator gen = com.alibaba.fastjson3.JSONGenerator.of()) {
            reflectWriter.write(gen, mc, null, null, 0);
            bh.consume(gen.toString());
        }
    }

    @Benchmark
    public void fastjson3_asm(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONGenerator gen = com.alibaba.fastjson3.JSONGenerator.of()) {
            asmWriter.write(gen, mc, null, null, 0);
            bh.consume(gen.toString());
        }
    }

    /** Direct UTF8→Latin1 path, bypassing ObjectMapper overhead */
    @Benchmark
    public void fastjson3_utf8latin1(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONGenerator gen = com.alibaba.fastjson3.JSONGenerator.ofUTF8()) {
            gen.writeAny(mc);
            if (gen instanceof com.alibaba.fastjson3.JSONGenerator.UTF8 utf8) {
                String result = utf8.toStringLatin1();
                if (result != null) {
                    bh.consume(result);
                    return;
                }
            }
            bh.consume(gen.toString());
        }
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(jackson.writeValueAsString(mc));
    }

    @Benchmark
    public void gson(Blackhole bh) {
        bh.consume(gson.toJson(mc));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonString(mc));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteString.class.getName())
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

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

public class EishayWriteUTF8Bytes {
    static MediaContent mc;
    static final ObjectMapper jackson = new ObjectMapper();
    static final Gson gson = new Gson();
    static com.alibaba.fastjson3.ObjectWriter<MediaContent> reflectWriter;
    static com.alibaba.fastjson3.ObjectWriter<MediaContent> asmWriter;

    static {
        try {
            byte[] bytes;
            try (InputStream is = EishayWriteUTF8Bytes.class.getClassLoader()
                    .getResourceAsStream("data/eishay/eishay_compact.json")) {
                bytes = is.readAllBytes();
            }
            mc = com.alibaba.fastjson2.JSON.parseObject(bytes, MediaContent.class);
            reflectWriter = com.alibaba.fastjson3.writer.ObjectWriterCreator.createObjectWriter(MediaContent.class);
            asmWriter = com.alibaba.fastjson3.writer.ObjectWriterCreatorASM.createObjectWriter(MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.toJSONBytes(mc));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.toJSONBytes(mc));
    }

    @Benchmark
    public void fastjson3_reflect(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONGenerator gen = com.alibaba.fastjson3.JSONGenerator.ofUTF8()) {
            reflectWriter.write(gen, mc, null, null, 0);
            bh.consume(gen.toByteArray());
        }
    }

    @Benchmark
    public void fastjson3_asm(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONGenerator gen = com.alibaba.fastjson3.JSONGenerator.ofUTF8()) {
            asmWriter.write(gen, mc, null, null, 0);
            bh.consume(gen.toByteArray());
        }
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(jackson.writeValueAsBytes(mc));
    }

    @Benchmark
    public void gson(Blackhole bh) {
        bh.consume(gson.toJson(mc).getBytes(StandardCharsets.UTF_8));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonBytes(mc));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayWriteUTF8Bytes.class.getName())
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

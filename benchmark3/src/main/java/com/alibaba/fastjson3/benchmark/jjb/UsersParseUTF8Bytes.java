package com.alibaba.fastjson3.benchmark.jjb;

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

public class UsersParseUTF8Bytes {
    static final ObjectMapper jackson = new ObjectMapper();
    static final Gson gson = new Gson();
    static byte[] utf8Bytes;
    static com.alibaba.fastjson3.ObjectReader<Users> reflectReader;
    static com.alibaba.fastjson3.ObjectReader<Users> asmReader;

    static {
        try {
            try (InputStream is = UsersParseUTF8Bytes.class.getClassLoader().getResourceAsStream("data/jjb/user.json")) {
                utf8Bytes = is.readAllBytes();
            }
            reflectReader = com.alibaba.fastjson3.reader.ObjectReaderCreator.createObjectReader(Users.class);
            asmReader = com.alibaba.fastjson3.reader.ObjectReaderCreatorASM.createObjectReader(Users.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(utf8Bytes, Users.class));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(utf8Bytes, Users.class));
    }

    @Benchmark
    public void fastjson3_reflect(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONParser parser = com.alibaba.fastjson3.JSONParser.of(utf8Bytes)) {
            bh.consume(reflectReader.readObject(parser, null, null, 0));
        }
    }

    @Benchmark
    public void fastjson3_asm(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONParser parser = com.alibaba.fastjson3.JSONParser.of(utf8Bytes)) {
            bh.consume(asmReader.readObject(parser, null, null, 0));
        }
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(utf8Bytes, Users.class));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(jackson.readValue(utf8Bytes, Users.class));
    }

    @Benchmark
    public void gson(Blackhole bh) {
        bh.consume(gson.fromJson(new String(utf8Bytes, java.nio.charset.StandardCharsets.UTF_8), Users.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(UsersParseUTF8Bytes.class.getName())
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

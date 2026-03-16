package com.alibaba.fastjson3.benchmark.jjb;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 5)
@Fork(2)
@State(Scope.Thread)
public class JJBQuickBenchmark {
    static byte[] userBytes;
    static byte[] clientBytes;
    static Users user;
    static Clients client;

    static {
        try {
            InputStream is = JJBQuickBenchmark.class.getClassLoader().getResourceAsStream("data/jjb/user.json");
            userBytes = is.readAllBytes();
            is.close();

            is = JJBQuickBenchmark.class.getClassLoader().getResourceAsStream("data/jjb/client.json");
            clientBytes = is.readAllBytes();
            is.close();

            user = com.alibaba.fastjson2.JSON.parseObject(userBytes, Users.class);
            client = com.alibaba.fastjson2.JSON.parseObject(clientBytes, Clients.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    // ===== Parse Tests =====

    @Benchmark
    public void parseUser_fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(userBytes, Users.class));
    }

    @Benchmark
    public void parseUser_wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(userBytes, Users.class));
    }

    @Benchmark
    public void parseClient_fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(clientBytes, Clients.class));
    }

    @Benchmark
    public void parseClient_wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(clientBytes, Clients.class));
    }

    // ===== Write Tests =====

    @Benchmark
    public void writeUser_fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.toJSONBytes(user));
    }

    @Benchmark
    public void writeUser_wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonBytes(user));
    }

    @Benchmark
    public void writeClient_fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.toJSONBytes(client));
    }

    @Benchmark
    public void writeClient_wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonBytes(client));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(JJBQuickBenchmark.class.getName())
                .build();
        new Runner(options).run();
    }
}

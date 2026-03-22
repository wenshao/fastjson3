package com.alibaba.fastjson3.benchmark.eishay;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Run eishay benchmarks with profiler support for deep analysis.
 *
 * <pre>
 * # GC allocation profiling (shows bytes allocated per operation)
 * java -cp benchmark3.jar ...EishayProfileBenchmark gc
 *
 * # Stack profiling (shows hot methods)
 * java -cp benchmark3.jar ...EishayProfileBenchmark stack
 *
 * # Default: gc profiling, single thread, fastjson3 only
 * java -cp benchmark3.jar ...EishayProfileBenchmark
 * </pre>
 */
public class EishayProfileBenchmark {
    public static void main(String[] args) throws RunnerException {
        String profiler = args.length > 0 ? args[0] : "gc";
        String include = args.length > 1 ? args[1] : ".*Eishay.*.fastjson3$";

        System.out.println("Profiler: " + profiler + ", include: " + include);

        Options options = new OptionsBuilder()
                .include(include)
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(2)
                .measurementIterations(3)
                .forks(1)
                .threads(1)  // single thread for clean profiling
                .addProfiler(profiler)
                .build();
        new Runner(options).run();
    }
}

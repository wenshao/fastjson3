package com.alibaba.fastjson3.benchmark.eishay;

import com.alibaba.fastjson3.benchmark.BenchmarkConfig;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Run all eishay benchmarks.
 *
 * <pre>
 * java -cp benchmark3.jar ...EishayBenchmark                # standard, 16 threads
 * java -cp benchmark3.jar ...EishayBenchmark quick           # quick, skip reflect/asm
 * java -cp benchmark3.jar ...EishayBenchmark full            # full, high confidence
 * java -cp benchmark3.jar ...EishayBenchmark standard 1      # standard, single thread
 * </pre>
 */
public class EishayBenchmark {
    public static void main(String[] args) throws RunnerException {
        BenchmarkConfig config = BenchmarkConfig.parse(args);
        System.out.println("Eishay benchmark: " + config);

        OptionsBuilder builder = new OptionsBuilder();
        builder.include("com.alibaba.fastjson3.benchmark.eishay.Eishay")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(config.warmupIterations)
                .measurementIterations(config.measurementIterations)
                .forks(config.forks)
                .threads(config.threads);

        if (config.excludeInternalVariants) {
            builder.exclude(".*_reflect.*")
                    .exclude(".*_asm.*");
        }

        Options options = builder.build();
        new Runner(options).run();
    }
}

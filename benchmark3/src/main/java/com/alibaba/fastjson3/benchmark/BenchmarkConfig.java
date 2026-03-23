package com.alibaba.fastjson3.benchmark;

/**
 * Benchmark configuration profiles.
 *
 * <pre>
 * Profile     Warmup  Measure  Forks  Threads  Exclude           ~Time (91 methods)
 * ─────────────────────────────────────────────────────────────────────────────────
 * quick       1       2        1      16       reflect,asm       ~3 min
 * standard    2       3        2      16       (none)            ~15 min
 * full        3       5        3      16       (none)            ~45 min
 * </pre>
 *
 * <p>Usage from command line:</p>
 * <pre>
 * java -cp benchmark3.jar ...EishayBenchmark quick
 * java -cp benchmark3.jar ...EishayBenchmark full
 * java -cp benchmark3.jar ...EishayBenchmark standard 1    # single thread
 * </pre>
 */
public final class BenchmarkConfig {
    public final int warmupIterations;
    public final int measurementIterations;
    public final int forks;
    public final int threads;
    public final boolean excludeInternalVariants; // skip fastjson3_reflect and fastjson3_asm

    private BenchmarkConfig(int warmup, int measurement, int forks, int threads, boolean excludeInternal) {
        this.warmupIterations = warmup;
        this.measurementIterations = measurement;
        this.forks = forks;
        this.threads = threads;
        this.excludeInternalVariants = excludeInternal;
    }

    /**
     * Parse args: [profile] [threads]
     * <ul>
     *   <li>profile: quick | standard (default) | full</li>
     *   <li>threads: integer, default 16</li>
     * </ul>
     */
    public static BenchmarkConfig parse(String[] args) {
        String profile = "standard";
        int threads = 16;

        for (String arg : args) {
            switch (arg) {
                case "quick", "standard", "full" -> profile = arg;
                default -> {
                    try {
                        threads = Integer.parseInt(arg);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        return switch (profile) {
            case "quick" -> new BenchmarkConfig(1, 2, 1, threads, true);
            case "full" -> new BenchmarkConfig(3, 5, 3, threads, false);
            default -> new BenchmarkConfig(2, 3, 2, threads, false); // standard
        };
    }

    @Override
    public String toString() {
        return String.format("warmup=%d, measurement=%d, forks=%d, threads=%d, excludeInternal=%s",
                warmupIterations, measurementIterations, forks, threads, excludeInternalVariants);
    }
}

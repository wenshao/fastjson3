# fastjson3 Benchmark Raw Output — 3.0.0-SNAPSHOT-57caa76

Git commit: `57caa76`  

Raw JMH stdout from each machine. For the human-readable summary see the non-`_raw` companion file.

## aarch64 4c

**JDK**: 25.0.2  
**Arch**: aarch64  
**Cores**: 4  
**OS**: Debian GNU/Linux 12 (bookworm)  

```
=== STARTED 2026-04-13T15:32:49+08:00 ===
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson2

# Run progress: 0.00% complete, ETA 01:36:00
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6769.682 ops/ms
# Warmup Iteration   2: 6999.695 ops/ms
# Warmup Iteration   3: 7182.465 ops/ms
Iteration   1: 7187.837 ops/ms
Iteration   2: 7205.642 ops/ms
Iteration   3: 7196.552 ops/ms
Iteration   4: 7205.736 ops/ms
Iteration   5: 7209.496 ops/ms

# Run progress: 1.39% complete, ETA 01:35:48
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6858.515 ops/ms
# Warmup Iteration   2: 7181.360 ops/ms
# Warmup Iteration   3: 7356.262 ops/ms
Iteration   1: 7341.933 ops/ms
Iteration   2: 7353.558 ops/ms
Iteration   3: 7342.718 ops/ms
Iteration   4: 7370.773 ops/ms
Iteration   5: 7370.933 ops/ms

# Run progress: 2.78% complete, ETA 01:34:26
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6659.642 ops/ms
# Warmup Iteration   2: 7085.439 ops/ms
# Warmup Iteration   3: 7164.410 ops/ms
Iteration   1: 7117.381 ops/ms
Iteration   2: 7159.103 ops/ms
Iteration   3: 7161.966 ops/ms
Iteration   4: 7162.494 ops/ms
Iteration   5: 7172.282 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson2":
  7237.227 ±(99.9%) 96.521 ops/ms [Average]
  (min, avg, max) = (7117.381, 7237.227, 7370.933), stdev = 90.286
  CI (99.9%): [7140.706, 7333.748] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3

# Run progress: 4.17% complete, ETA 01:33:02
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6246.094 ops/ms
# Warmup Iteration   2: 6379.434 ops/ms
# Warmup Iteration   3: 6411.053 ops/ms
Iteration   1: 6393.567 ops/ms
Iteration   2: 6413.384 ops/ms
Iteration   3: 6407.816 ops/ms
Iteration   4: 6404.129 ops/ms
Iteration   5: 6395.769 ops/ms

# Run progress: 5.56% complete, ETA 01:31:41
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6187.681 ops/ms
# Warmup Iteration   2: 6379.129 ops/ms
# Warmup Iteration   3: 6396.892 ops/ms
Iteration   1: 6405.797 ops/ms
Iteration   2: 6413.213 ops/ms
Iteration   3: 6411.366 ops/ms
Iteration   4: 6409.490 ops/ms
Iteration   5: 6386.322 ops/ms

# Run progress: 6.94% complete, ETA 01:30:20
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6208.372 ops/ms
# Warmup Iteration   2: 6400.082 ops/ms
# Warmup Iteration   3: 6386.414 ops/ms
Iteration   1: 6360.260 ops/ms
Iteration   2: 6385.136 ops/ms
Iteration   3: 6370.448 ops/ms
Iteration   4: 6362.432 ops/ms
Iteration   5: 6379.507 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3":
  6393.242 ±(99.9%) 19.694 ops/ms [Average]
  (min, avg, max) = (6360.260, 6393.242, 6413.384), stdev = 18.421
  CI (99.9%): [6373.549, 6412.936] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson2

# Run progress: 8.33% complete, ETA 01:28:58
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4724.512 ops/ms
# Warmup Iteration   2: 4959.512 ops/ms
# Warmup Iteration   3: 5038.333 ops/ms
Iteration   1: 5047.458 ops/ms
Iteration   2: 5036.833 ops/ms
Iteration   3: 5030.523 ops/ms
Iteration   4: 5034.300 ops/ms
Iteration   5: 5036.652 ops/ms

# Run progress: 9.72% complete, ETA 01:27:37
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4584.733 ops/ms
# Warmup Iteration   2: 4882.442 ops/ms
# Warmup Iteration   3: 4934.216 ops/ms
Iteration   1: 4937.677 ops/ms
Iteration   2: 4938.520 ops/ms
Iteration   3: 4929.504 ops/ms
Iteration   4: 4926.142 ops/ms
Iteration   5: 4936.587 ops/ms

# Run progress: 11.11% complete, ETA 01:26:16
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4576.750 ops/ms
# Warmup Iteration   2: 4938.940 ops/ms
# Warmup Iteration   3: 4956.396 ops/ms
Iteration   1: 4973.231 ops/ms
Iteration   2: 4967.499 ops/ms
Iteration   3: 4966.954 ops/ms
Iteration   4: 4961.477 ops/ms
Iteration   5: 4964.501 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson2":
  4979.191 ±(99.9%) 48.052 ops/ms [Average]
  (min, avg, max) = (4926.142, 4979.191, 5047.458), stdev = 44.948
  CI (99.9%): [4931.139, 5027.242] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson3

# Run progress: 12.50% complete, ETA 01:24:55
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4922.851 ops/ms
# Warmup Iteration   2: 5190.020 ops/ms
# Warmup Iteration   3: 5187.673 ops/ms
Iteration   1: 5204.448 ops/ms
Iteration   2: 5200.273 ops/ms
Iteration   3: 5201.151 ops/ms
Iteration   4: 5171.912 ops/ms
Iteration   5: 5188.338 ops/ms

# Run progress: 13.89% complete, ETA 01:23:34
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4004.903 ops/ms
# Warmup Iteration   2: 4244.105 ops/ms
# Warmup Iteration   3: 4222.475 ops/ms
Iteration   1: 4235.290 ops/ms
Iteration   2: 4231.344 ops/ms
Iteration   3: 4240.409 ops/ms
Iteration   4: 4229.874 ops/ms
Iteration   5: 4229.896 ops/ms

# Run progress: 15.28% complete, ETA 01:22:13
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4177.774 ops/ms
# Warmup Iteration   2: 4298.383 ops/ms
# Warmup Iteration   3: 4332.850 ops/ms
Iteration   1: 4325.791 ops/ms
Iteration   2: 4334.338 ops/ms
Iteration   3: 4316.252 ops/ms
Iteration   4: 4321.569 ops/ms
Iteration   5: 4322.844 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson3":
  4583.582 ±(99.9%) 478.872 ops/ms [Average]
  (min, avg, max) = (4229.874, 4583.582, 5204.448), stdev = 447.937
  CI (99.9%): [4104.710, 5062.454] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson2

# Run progress: 16.67% complete, ETA 01:20:52
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3904.031 ops/ms
# Warmup Iteration   2: 3995.301 ops/ms
# Warmup Iteration   3: 4026.699 ops/ms
Iteration   1: 4035.341 ops/ms
Iteration   2: 4024.294 ops/ms
Iteration   3: 4027.199 ops/ms
Iteration   4: 4018.782 ops/ms
Iteration   5: 4028.732 ops/ms

# Run progress: 18.06% complete, ETA 01:19:31
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3817.140 ops/ms
# Warmup Iteration   2: 3939.011 ops/ms
# Warmup Iteration   3: 3936.505 ops/ms
Iteration   1: 3916.950 ops/ms
Iteration   2: 3936.706 ops/ms
Iteration   3: 3930.380 ops/ms
Iteration   4: 3939.915 ops/ms
Iteration   5: 3938.101 ops/ms

# Run progress: 19.44% complete, ETA 01:18:10
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3863.087 ops/ms
# Warmup Iteration   2: 3980.608 ops/ms
# Warmup Iteration   3: 3988.314 ops/ms
Iteration   1: 4008.493 ops/ms
Iteration   2: 3994.348 ops/ms
Iteration   3: 4001.921 ops/ms
Iteration   4: 4006.869 ops/ms
Iteration   5: 4000.309 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson2":
  3987.223 ±(99.9%) 44.864 ops/ms [Average]
  (min, avg, max) = (3916.950, 3987.223, 4035.341), stdev = 41.966
  CI (99.9%): [3942.359, 4032.086] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson3

# Run progress: 20.83% complete, ETA 01:16:49
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3534.902 ops/ms
# Warmup Iteration   2: 3600.579 ops/ms
# Warmup Iteration   3: 3608.451 ops/ms
Iteration   1: 3617.508 ops/ms
Iteration   2: 3623.068 ops/ms
Iteration   3: 3619.069 ops/ms
Iteration   4: 3616.277 ops/ms
Iteration   5: 3603.474 ops/ms

# Run progress: 22.22% complete, ETA 01:15:28
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3476.529 ops/ms
# Warmup Iteration   2: 3557.541 ops/ms
# Warmup Iteration   3: 3552.678 ops/ms
Iteration   1: 3548.148 ops/ms
Iteration   2: 3550.046 ops/ms
Iteration   3: 3551.840 ops/ms
Iteration   4: 3555.401 ops/ms
Iteration   5: 3546.300 ops/ms

# Run progress: 23.61% complete, ETA 01:14:07
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3418.807 ops/ms
# Warmup Iteration   2: 3537.404 ops/ms
# Warmup Iteration   3: 3550.687 ops/ms
Iteration   1: 3550.715 ops/ms
Iteration   2: 3550.108 ops/ms
Iteration   3: 3545.873 ops/ms
Iteration   4: 3549.865 ops/ms
Iteration   5: 3546.678 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson3":
  3571.625 ±(99.9%) 34.973 ops/ms [Average]
  (min, avg, max) = (3545.873, 3571.625, 3623.068), stdev = 32.714
  CI (99.9%): [3536.651, 3606.598] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson2

# Run progress: 25.00% complete, ETA 01:12:46
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3817.839 ops/ms
# Warmup Iteration   2: 3922.071 ops/ms
# Warmup Iteration   3: 3950.042 ops/ms
Iteration   1: 3933.300 ops/ms
Iteration   2: 3937.724 ops/ms
Iteration   3: 3944.750 ops/ms
Iteration   4: 3931.342 ops/ms
Iteration   5: 3927.110 ops/ms

# Run progress: 26.39% complete, ETA 01:11:25
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3765.870 ops/ms
# Warmup Iteration   2: 3883.935 ops/ms
# Warmup Iteration   3: 3915.924 ops/ms
Iteration   1: 3929.747 ops/ms
Iteration   2: 3932.586 ops/ms
Iteration   3: 3927.173 ops/ms
Iteration   4: 3940.100 ops/ms
Iteration   5: 3922.516 ops/ms

# Run progress: 27.78% complete, ETA 01:10:04
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3795.847 ops/ms
# Warmup Iteration   2: 3896.128 ops/ms
# Warmup Iteration   3: 3909.495 ops/ms
Iteration   1: 3913.560 ops/ms
Iteration   2: 3915.919 ops/ms
Iteration   3: 3927.167 ops/ms
Iteration   4: 3917.910 ops/ms
Iteration   5: 3919.012 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson2":
  3927.994 ±(99.9%) 9.707 ops/ms [Average]
  (min, avg, max) = (3913.560, 3927.994, 3944.750), stdev = 9.080
  CI (99.9%): [3918.287, 3937.701] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson3

# Run progress: 29.17% complete, ETA 01:08:43
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3438.088 ops/ms
# Warmup Iteration   2: 3477.962 ops/ms
# Warmup Iteration   3: 3494.608 ops/ms
Iteration   1: 3485.771 ops/ms
Iteration   2: 3468.234 ops/ms
Iteration   3: 3462.484 ops/ms
Iteration   4: 3466.779 ops/ms
Iteration   5: 3471.709 ops/ms

# Run progress: 30.56% complete, ETA 01:07:22
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3514.403 ops/ms
# Warmup Iteration   2: 3569.596 ops/ms
# Warmup Iteration   3: 3591.984 ops/ms
Iteration   1: 3589.502 ops/ms
Iteration   2: 3590.976 ops/ms
Iteration   3: 3590.771 ops/ms
Iteration   4: 3591.337 ops/ms
Iteration   5: 3587.351 ops/ms

# Run progress: 31.94% complete, ETA 01:06:01
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3434.746 ops/ms
# Warmup Iteration   2: 3514.169 ops/ms
# Warmup Iteration   3: 3527.712 ops/ms
Iteration   1: 3547.276 ops/ms
Iteration   2: 3545.680 ops/ms
Iteration   3: 3544.345 ops/ms
Iteration   4: 3535.389 ops/ms
Iteration   5: 3544.114 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson3":
  3534.781 ±(99.9%) 54.484 ops/ms [Average]
  (min, avg, max) = (3462.484, 3534.781, 3591.337), stdev = 50.964
  CI (99.9%): [3480.297, 3589.265] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson2

# Run progress: 33.33% complete, ETA 01:04:40
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6840.394 ops/ms
# Warmup Iteration   2: 7063.908 ops/ms
# Warmup Iteration   3: 7093.884 ops/ms
Iteration   1: 7094.944 ops/ms
Iteration   2: 7101.074 ops/ms
Iteration   3: 7097.102 ops/ms
Iteration   4: 7093.980 ops/ms
Iteration   5: 7084.228 ops/ms

# Run progress: 34.72% complete, ETA 01:03:19
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6753.755 ops/ms
# Warmup Iteration   2: 7093.857 ops/ms
# Warmup Iteration   3: 7101.574 ops/ms
Iteration   1: 7118.807 ops/ms
Iteration   2: 7095.123 ops/ms
Iteration   3: 7088.833 ops/ms
Iteration   4: 7089.765 ops/ms
Iteration   5: 7084.909 ops/ms

# Run progress: 36.11% complete, ETA 01:01:59
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6744.413 ops/ms
# Warmup Iteration   2: 7008.022 ops/ms
# Warmup Iteration   3: 7087.492 ops/ms
Iteration   1: 7095.287 ops/ms
Iteration   2: 7103.952 ops/ms
Iteration   3: 7091.104 ops/ms
Iteration   4: 7082.030 ops/ms
Iteration   5: 7073.983 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson2":
  7093.008 ±(99.9%) 11.206 ops/ms [Average]
  (min, avg, max) = (7073.983, 7093.008, 7118.807), stdev = 10.482
  CI (99.9%): [7081.802, 7104.214] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3

# Run progress: 37.50% complete, ETA 01:00:38
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4716.427 ops/ms
# Warmup Iteration   2: 4870.650 ops/ms
# Warmup Iteration   3: 4894.520 ops/ms
Iteration   1: 4877.769 ops/ms
Iteration   2: 4887.367 ops/ms
Iteration   3: 4870.586 ops/ms
Iteration   4: 4882.453 ops/ms
Iteration   5: 4880.182 ops/ms

# Run progress: 38.89% complete, ETA 00:59:17
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4735.956 ops/ms
# Warmup Iteration   2: 4928.344 ops/ms
# Warmup Iteration   3: 4929.185 ops/ms
Iteration   1: 4932.935 ops/ms
Iteration   2: 4943.587 ops/ms
Iteration   3: 4937.305 ops/ms
Iteration   4: 4933.440 ops/ms
Iteration   5: 4935.957 ops/ms

# Run progress: 40.28% complete, ETA 00:57:56
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6568.468 ops/ms
# Warmup Iteration   2: 6758.500 ops/ms
# Warmup Iteration   3: 6802.132 ops/ms
Iteration   1: 6784.662 ops/ms
Iteration   2: 6768.413 ops/ms
Iteration   3: 6785.557 ops/ms
Iteration   4: 6772.343 ops/ms
Iteration   5: 6770.004 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3":
  5530.837 ±(99.9%) 974.819 ops/ms [Average]
  (min, avg, max) = (4870.586, 5530.837, 6785.557), stdev = 911.847
  CI (99.9%): [4556.018, 6505.657] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson2

# Run progress: 41.67% complete, ETA 00:56:35
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4609.121 ops/ms
# Warmup Iteration   2: 4849.739 ops/ms
# Warmup Iteration   3: 4850.905 ops/ms
Iteration   1: 4865.697 ops/ms
Iteration   2: 4864.150 ops/ms
Iteration   3: 4875.630 ops/ms
Iteration   4: 4864.602 ops/ms
Iteration   5: 4868.372 ops/ms

# Run progress: 43.06% complete, ETA 00:55:14
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4480.221 ops/ms
# Warmup Iteration   2: 4746.031 ops/ms
# Warmup Iteration   3: 4794.783 ops/ms
Iteration   1: 4790.160 ops/ms
Iteration   2: 4796.702 ops/ms
Iteration   3: 4800.063 ops/ms
Iteration   4: 4786.643 ops/ms
Iteration   5: 4798.627 ops/ms

# Run progress: 44.44% complete, ETA 00:53:54
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4522.836 ops/ms
# Warmup Iteration   2: 4805.334 ops/ms
# Warmup Iteration   3: 4820.791 ops/ms
Iteration   1: 4830.192 ops/ms
Iteration   2: 4817.014 ops/ms
Iteration   3: 4829.369 ops/ms
Iteration   4: 4821.556 ops/ms
Iteration   5: 4829.775 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson2":
  4829.237 ±(99.9%) 33.662 ops/ms [Average]
  (min, avg, max) = (4786.643, 4829.237, 4875.630), stdev = 31.487
  CI (99.9%): [4795.575, 4862.899] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson3

# Run progress: 45.83% complete, ETA 00:52:33
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3931.044 ops/ms
# Warmup Iteration   2: 4020.247 ops/ms
# Warmup Iteration   3: 4097.356 ops/ms
Iteration   1: 4021.718 ops/ms
Iteration   2: 4030.001 ops/ms
Iteration   3: 4025.958 ops/ms
Iteration   4: 4029.313 ops/ms
Iteration   5: 4017.753 ops/ms

# Run progress: 47.22% complete, ETA 00:51:12
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5185.718 ops/ms
# Warmup Iteration   2: 5354.840 ops/ms
# Warmup Iteration   3: 5365.340 ops/ms
Iteration   1: 5370.565 ops/ms
Iteration   2: 5368.145 ops/ms
Iteration   3: 5374.248 ops/ms
Iteration   4: 5344.380 ops/ms
Iteration   5: 5360.426 ops/ms

# Run progress: 48.61% complete, ETA 00:49:51
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4138.470 ops/ms
# Warmup Iteration   2: 4270.097 ops/ms
# Warmup Iteration   3: 4296.856 ops/ms
Iteration   1: 4264.167 ops/ms
Iteration   2: 4270.963 ops/ms
Iteration   3: 4257.668 ops/ms
Iteration   4: 4256.979 ops/ms
Iteration   5: 4265.178 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson3":
  4550.497 ±(99.9%) 645.269 ops/ms [Average]
  (min, avg, max) = (4017.753, 4550.497, 5374.248), stdev = 603.585
  CI (99.9%): [3905.229, 5195.766] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson2

# Run progress: 50.00% complete, ETA 00:48:30
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6523.662 ops/ms
# Warmup Iteration   2: 6821.465 ops/ms
# Warmup Iteration   3: 6843.247 ops/ms
Iteration   1: 6837.829 ops/ms
Iteration   2: 6847.446 ops/ms
Iteration   3: 6842.294 ops/ms
Iteration   4: 6848.919 ops/ms
Iteration   5: 6834.006 ops/ms

# Run progress: 51.39% complete, ETA 00:47:10
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6653.659 ops/ms
# Warmup Iteration   2: 6872.199 ops/ms
# Warmup Iteration   3: 6900.010 ops/ms
Iteration   1: 6927.184 ops/ms
Iteration   2: 6921.694 ops/ms
Iteration   3: 6940.842 ops/ms
Iteration   4: 6950.496 ops/ms
Iteration   5: 6923.035 ops/ms

# Run progress: 52.78% complete, ETA 00:45:49
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6311.470 ops/ms
# Warmup Iteration   2: 6540.212 ops/ms
# Warmup Iteration   3: 6541.114 ops/ms
Iteration   1: 6557.101 ops/ms
Iteration   2: 6554.039 ops/ms
Iteration   3: 6553.993 ops/ms
Iteration   4: 6539.294 ops/ms
Iteration   5: 6551.700 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson2":
  6775.325 ±(99.9%) 180.282 ops/ms [Average]
  (min, avg, max) = (6539.294, 6775.325, 6950.496), stdev = 168.636
  CI (99.9%): [6595.043, 6955.607] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3

# Run progress: 54.17% complete, ETA 00:44:28
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6975.251 ops/ms
# Warmup Iteration   2: 7185.330 ops/ms
# Warmup Iteration   3: 7240.207 ops/ms
Iteration   1: 7249.078 ops/ms
Iteration   2: 7220.940 ops/ms
Iteration   3: 7242.139 ops/ms
Iteration   4: 7206.605 ops/ms
Iteration   5: 7220.079 ops/ms

# Run progress: 55.56% complete, ETA 00:43:07
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6707.719 ops/ms
# Warmup Iteration   2: 6831.049 ops/ms
# Warmup Iteration   3: 6862.780 ops/ms
Iteration   1: 6858.236 ops/ms
Iteration   2: 6819.182 ops/ms
Iteration   3: 6836.837 ops/ms
Iteration   4: 6879.754 ops/ms
Iteration   5: 6824.724 ops/ms

# Run progress: 56.94% complete, ETA 00:41:46
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6513.642 ops/ms
# Warmup Iteration   2: 6834.258 ops/ms
# Warmup Iteration   3: 6842.764 ops/ms
Iteration   1: 6812.827 ops/ms
Iteration   2: 6843.837 ops/ms
Iteration   3: 6824.903 ops/ms
Iteration   4: 6834.476 ops/ms
Iteration   5: 6850.496 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3":
  6968.274 ±(99.9%) 204.030 ops/ms [Average]
  (min, avg, max) = (6812.827, 6968.274, 7249.078), stdev = 190.850
  CI (99.9%): [6764.244, 7172.305] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson2

# Run progress: 58.33% complete, ETA 00:40:26
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7467.920 ops/ms
# Warmup Iteration   2: 7757.021 ops/ms
# Warmup Iteration   3: 7855.530 ops/ms
Iteration   1: 7823.372 ops/ms
Iteration   2: 7837.151 ops/ms
Iteration   3: 7819.872 ops/ms
Iteration   4: 7827.986 ops/ms
Iteration   5: 7808.048 ops/ms

# Run progress: 59.72% complete, ETA 00:39:05
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7329.328 ops/ms
# Warmup Iteration   2: 7534.221 ops/ms
# Warmup Iteration   3: 7616.829 ops/ms
Iteration   1: 7622.355 ops/ms
Iteration   2: 7601.492 ops/ms
Iteration   3: 7600.222 ops/ms
Iteration   4: 7589.653 ops/ms
Iteration   5: 7595.752 ops/ms

# Run progress: 61.11% complete, ETA 00:37:44
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7211.322 ops/ms
# Warmup Iteration   2: 7723.162 ops/ms
# Warmup Iteration   3: 7749.740 ops/ms
Iteration   1: 7752.856 ops/ms
Iteration   2: 7755.427 ops/ms
Iteration   3: 7741.033 ops/ms
Iteration   4: 7751.459 ops/ms
Iteration   5: 7746.161 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson2":
  7724.856 ±(99.9%) 102.321 ops/ms [Average]
  (min, avg, max) = (7589.653, 7724.856, 7837.151), stdev = 95.711
  CI (99.9%): [7622.535, 7827.177] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3

# Run progress: 62.50% complete, ETA 00:36:23
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6937.631 ops/ms
# Warmup Iteration   2: 7184.010 ops/ms
# Warmup Iteration   3: 7192.149 ops/ms
Iteration   1: 7171.371 ops/ms
Iteration   2: 7175.621 ops/ms
Iteration   3: 7174.185 ops/ms
Iteration   4: 7170.217 ops/ms
Iteration   5: 7149.202 ops/ms

# Run progress: 63.89% complete, ETA 00:35:02
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6987.873 ops/ms
# Warmup Iteration   2: 7221.018 ops/ms
# Warmup Iteration   3: 7210.189 ops/ms
Iteration   1: 7196.872 ops/ms
Iteration   2: 7224.647 ops/ms
Iteration   3: 7233.986 ops/ms
Iteration   4: 7222.645 ops/ms
Iteration   5: 7226.639 ops/ms

# Run progress: 65.28% complete, ETA 00:33:42
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6983.671 ops/ms
# Warmup Iteration   2: 7145.392 ops/ms
# Warmup Iteration   3: 7140.825 ops/ms
Iteration   1: 7137.282 ops/ms
Iteration   2: 7140.871 ops/ms
Iteration   3: 7140.188 ops/ms
Iteration   4: 7161.159 ops/ms
Iteration   5: 7116.564 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3":
  7176.097 ±(99.9%) 39.847 ops/ms [Average]
  (min, avg, max) = (7116.564, 7176.097, 7233.986), stdev = 37.273
  CI (99.9%): [7136.250, 7215.943] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson2

# Run progress: 66.67% complete, ETA 00:32:21
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1322.344 ops/ms
# Warmup Iteration   2: 1403.591 ops/ms
# Warmup Iteration   3: 1409.066 ops/ms
Iteration   1: 1406.819 ops/ms
Iteration   2: 1400.379 ops/ms
Iteration   3: 1405.931 ops/ms
Iteration   4: 1401.700 ops/ms
Iteration   5: 1405.728 ops/ms

# Run progress: 68.06% complete, ETA 00:31:00
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1277.990 ops/ms
# Warmup Iteration   2: 1358.492 ops/ms
# Warmup Iteration   3: 1366.380 ops/ms
Iteration   1: 1362.950 ops/ms
Iteration   2: 1364.750 ops/ms
Iteration   3: 1366.086 ops/ms
Iteration   4: 1366.007 ops/ms
Iteration   5: 1364.019 ops/ms

# Run progress: 69.44% complete, ETA 00:29:39
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1312.150 ops/ms
# Warmup Iteration   2: 1387.184 ops/ms
# Warmup Iteration   3: 1389.076 ops/ms
Iteration   1: 1391.225 ops/ms
Iteration   2: 1395.027 ops/ms
Iteration   3: 1391.771 ops/ms
Iteration   4: 1392.468 ops/ms
Iteration   5: 1390.715 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson2":
  1387.038 ±(99.9%) 18.351 ops/ms [Average]
  (min, avg, max) = (1362.950, 1387.038, 1406.819), stdev = 17.166
  CI (99.9%): [1368.687, 1405.390] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3

# Run progress: 70.83% complete, ETA 00:28:18
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1599.363 ops/ms
# Warmup Iteration   2: 1656.915 ops/ms
# Warmup Iteration   3: 1665.372 ops/ms
Iteration   1: 1671.443 ops/ms
Iteration   2: 1665.949 ops/ms
Iteration   3: 1667.907 ops/ms
Iteration   4: 1663.818 ops/ms
Iteration   5: 1656.731 ops/ms

# Run progress: 72.22% complete, ETA 00:26:57
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1613.320 ops/ms
# Warmup Iteration   2: 1653.346 ops/ms
# Warmup Iteration   3: 1661.043 ops/ms
Iteration   1: 1655.768 ops/ms
Iteration   2: 1661.498 ops/ms
Iteration   3: 1657.040 ops/ms
Iteration   4: 1658.186 ops/ms
Iteration   5: 1656.437 ops/ms

# Run progress: 73.61% complete, ETA 00:25:36
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1614.645 ops/ms
# Warmup Iteration   2: 1663.635 ops/ms
# Warmup Iteration   3: 1663.062 ops/ms
Iteration   1: 1657.347 ops/ms
Iteration   2: 1649.001 ops/ms
Iteration   3: 1652.158 ops/ms
Iteration   4: 1655.689 ops/ms
Iteration   5: 1657.750 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3":
  1659.115 ±(99.9%) 6.392 ops/ms [Average]
  (min, avg, max) = (1649.001, 1659.115, 1671.443), stdev = 5.979
  CI (99.9%): [1652.723, 1665.506] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson2

# Run progress: 75.00% complete, ETA 00:24:15
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1768.604 ops/ms
# Warmup Iteration   2: 1976.671 ops/ms
# Warmup Iteration   3: 1971.676 ops/ms
Iteration   1: 1978.497 ops/ms
Iteration   2: 1972.558 ops/ms
Iteration   3: 1979.952 ops/ms
Iteration   4: 1972.791 ops/ms
Iteration   5: 1973.774 ops/ms

# Run progress: 76.39% complete, ETA 00:22:55
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1860.734 ops/ms
# Warmup Iteration   2: 1947.276 ops/ms
# Warmup Iteration   3: 1959.226 ops/ms
Iteration   1: 1954.615 ops/ms
Iteration   2: 1937.121 ops/ms
Iteration   3: 1937.293 ops/ms
Iteration   4: 1938.341 ops/ms
Iteration   5: 1932.194 ops/ms

# Run progress: 77.78% complete, ETA 00:21:34
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1923.733 ops/ms
# Warmup Iteration   2: 2000.597 ops/ms
# Warmup Iteration   3: 2005.761 ops/ms
Iteration   1: 2003.555 ops/ms
Iteration   2: 2001.668 ops/ms
Iteration   3: 2002.977 ops/ms
Iteration   4: 2000.197 ops/ms
Iteration   5: 2002.948 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson2":
  1972.565 ±(99.9%) 28.763 ops/ms [Average]
  (min, avg, max) = (1932.194, 1972.565, 2003.555), stdev = 26.905
  CI (99.9%): [1943.803, 2001.328] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3

# Run progress: 79.17% complete, ETA 00:20:13
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2108.425 ops/ms
# Warmup Iteration   2: 2149.692 ops/ms
# Warmup Iteration   3: 2153.924 ops/ms
Iteration   1: 2152.405 ops/ms
Iteration   2: 2152.813 ops/ms
Iteration   3: 2151.776 ops/ms
Iteration   4: 2153.807 ops/ms
Iteration   5: 2151.476 ops/ms

# Run progress: 80.56% complete, ETA 00:18:52
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2097.343 ops/ms
# Warmup Iteration   2: 2155.240 ops/ms
# Warmup Iteration   3: 2148.430 ops/ms
Iteration   1: 2158.571 ops/ms
Iteration   2: 2151.636 ops/ms
Iteration   3: 2153.874 ops/ms
Iteration   4: 2158.162 ops/ms
Iteration   5: 2158.283 ops/ms

# Run progress: 81.94% complete, ETA 00:17:31
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2069.898 ops/ms
# Warmup Iteration   2: 2134.198 ops/ms
# Warmup Iteration   3: 2140.509 ops/ms
Iteration   1: 2135.489 ops/ms
Iteration   2: 2136.137 ops/ms
Iteration   3: 2140.221 ops/ms
Iteration   4: 2136.430 ops/ms
Iteration   5: 2146.156 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3":
  2149.149 ±(99.9%) 8.789 ops/ms [Average]
  (min, avg, max) = (2135.489, 2149.149, 2158.571), stdev = 8.222
  CI (99.9%): [2140.360, 2157.938] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson2

# Run progress: 83.33% complete, ETA 00:16:10
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1647.754 ops/ms
# Warmup Iteration   2: 1723.540 ops/ms
# Warmup Iteration   3: 1732.619 ops/ms
Iteration   1: 1724.315 ops/ms
Iteration   2: 1731.903 ops/ms
Iteration   3: 1731.811 ops/ms
Iteration   4: 1724.451 ops/ms
Iteration   5: 1725.444 ops/ms

# Run progress: 84.72% complete, ETA 00:14:49
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1582.931 ops/ms
# Warmup Iteration   2: 1667.921 ops/ms
# Warmup Iteration   3: 1676.315 ops/ms
Iteration   1: 1674.301 ops/ms
Iteration   2: 1678.710 ops/ms
Iteration   3: 1677.882 ops/ms
Iteration   4: 1668.997 ops/ms
Iteration   5: 1671.700 ops/ms

# Run progress: 86.11% complete, ETA 00:13:28
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1602.954 ops/ms
# Warmup Iteration   2: 1683.712 ops/ms
# Warmup Iteration   3: 1698.686 ops/ms
Iteration   1: 1693.445 ops/ms
Iteration   2: 1693.678 ops/ms
Iteration   3: 1693.365 ops/ms
Iteration   4: 1693.729 ops/ms
Iteration   5: 1685.011 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson2":
  1697.916 ±(99.9%) 24.838 ops/ms [Average]
  (min, avg, max) = (1668.997, 1697.916, 1731.903), stdev = 23.233
  CI (99.9%): [1673.078, 1722.754] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3

# Run progress: 87.50% complete, ETA 00:12:08
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1455.353 ops/ms
# Warmup Iteration   2: 1490.171 ops/ms
# Warmup Iteration   3: 1500.808 ops/ms
Iteration   1: 1494.507 ops/ms
Iteration   2: 1495.181 ops/ms
Iteration   3: 1491.975 ops/ms
Iteration   4: 1494.955 ops/ms
Iteration   5: 1493.070 ops/ms

# Run progress: 88.89% complete, ETA 00:10:47
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1453.847 ops/ms
# Warmup Iteration   2: 1499.347 ops/ms
# Warmup Iteration   3: 1498.269 ops/ms
Iteration   1: 1497.321 ops/ms
Iteration   2: 1500.876 ops/ms
Iteration   3: 1500.473 ops/ms
Iteration   4: 1503.251 ops/ms
Iteration   5: 1506.135 ops/ms

# Run progress: 90.28% complete, ETA 00:09:26
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1415.347 ops/ms
# Warmup Iteration   2: 1454.983 ops/ms
# Warmup Iteration   3: 1458.565 ops/ms
Iteration   1: 1457.919 ops/ms
Iteration   2: 1455.940 ops/ms
Iteration   3: 1460.052 ops/ms
Iteration   4: 1458.955 ops/ms
Iteration   5: 1453.560 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3":
  1484.278 ±(99.9%) 21.551 ops/ms [Average]
  (min, avg, max) = (1453.560, 1484.278, 1506.135), stdev = 20.159
  CI (99.9%): [1462.727, 1505.829] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson2

# Run progress: 91.67% complete, ETA 00:08:05
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1840.606 ops/ms
# Warmup Iteration   2: 1951.556 ops/ms
# Warmup Iteration   3: 1947.569 ops/ms
Iteration   1: 1949.832 ops/ms
Iteration   2: 1948.943 ops/ms
Iteration   3: 1954.839 ops/ms
Iteration   4: 1955.730 ops/ms
Iteration   5: 1953.698 ops/ms

# Run progress: 93.06% complete, ETA 00:06:44
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1893.178 ops/ms
# Warmup Iteration   2: 1967.796 ops/ms
# Warmup Iteration   3: 1968.627 ops/ms
Iteration   1: 1965.607 ops/ms
Iteration   2: 1971.529 ops/ms
Iteration   3: 1971.390 ops/ms
Iteration   4: 1970.370 ops/ms
Iteration   5: 1968.850 ops/ms

# Run progress: 94.44% complete, ETA 00:05:23
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1859.321 ops/ms
# Warmup Iteration   2: 1953.004 ops/ms
# Warmup Iteration   3: 1958.895 ops/ms
Iteration   1: 1954.435 ops/ms
Iteration   2: 1954.956 ops/ms
Iteration   3: 1957.523 ops/ms
Iteration   4: 1946.383 ops/ms
Iteration   5: 1949.421 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson2":
  1958.234 ±(99.9%) 9.490 ops/ms [Average]
  (min, avg, max) = (1946.383, 1958.234, 1971.529), stdev = 8.877
  CI (99.9%): [1948.743, 1967.724] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3

# Run progress: 95.83% complete, ETA 00:04:02
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2136.062 ops/ms
# Warmup Iteration   2: 2178.783 ops/ms
# Warmup Iteration   3: 2182.333 ops/ms
Iteration   1: 2183.545 ops/ms
Iteration   2: 2176.016 ops/ms
Iteration   3: 2184.663 ops/ms
Iteration   4: 2184.825 ops/ms
Iteration   5: 2189.101 ops/ms

# Run progress: 97.22% complete, ETA 00:02:41
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2126.668 ops/ms
# Warmup Iteration   2: 2178.948 ops/ms
# Warmup Iteration   3: 2165.346 ops/ms
Iteration   1: 2176.385 ops/ms
Iteration   2: 2177.044 ops/ms
Iteration   3: 2180.193 ops/ms
Iteration   4: 2178.813 ops/ms
Iteration   5: 2175.272 ops/ms

# Run progress: 98.61% complete, ETA 00:01:20
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2163.170 ops/ms
# Warmup Iteration   2: 2167.235 ops/ms
# Warmup Iteration   3: 2166.847 ops/ms
Iteration   1: 2169.298 ops/ms
Iteration   2: 2165.621 ops/ms
Iteration   3: 2173.101 ops/ms
Iteration   4: 2182.508 ops/ms
Iteration   5: 2174.106 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3":
  2178.033 ±(99.9%) 6.724 ops/ms [Average]
  (min, avg, max) = (2165.621, 2178.033, 2189.101), stdev = 6.289
  CI (99.9%): [2171.309, 2184.756] (assumes normal distribution)


# Run complete. Total time: 01:37:04

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

NOTE: Current JVM experimentally supports Compiler Blackholes, and they are in use. Please exercise
extra caution when trusting the results, look into the generated code to check the benchmark still
works, and factor in a small probability of new VM bugs. Additionally, while comparisons between
different JVMs are already problematic, the performance difference caused by different Blackhole
modes can be very significant. Please make sure you use the consistent Blackhole mode for comparisons.

Benchmark                                             Mode  Cnt     Score     Error   Units
c.a.f.b.eishay.EishayParseString.fastjson2           thrpt   15  7237.227 ±  96.521  ops/ms
c.a.f.b.eishay.EishayParseString.fastjson3           thrpt   15  6393.242 ±  19.694  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.fastjson2     thrpt   15  4979.191 ±  48.052  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.fastjson3     thrpt   15  4583.582 ± 478.872  ops/ms
c.a.f.b.eishay.EishayParseTreeString.fastjson2       thrpt   15  3987.223 ±  44.864  ops/ms
c.a.f.b.eishay.EishayParseTreeString.fastjson3       thrpt   15  3571.625 ±  34.973  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2    thrpt   15  3927.994 ±   9.707  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3    thrpt   15  3534.781 ±  54.484  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2        thrpt   15  7093.008 ±  11.206  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3        thrpt   15  5530.837 ± 974.819  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.fastjson2  thrpt   15  4829.237 ±  33.662  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.fastjson3  thrpt   15  4550.497 ± 645.269  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson2           thrpt   15  6775.325 ± 180.282  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson3           thrpt   15  6968.274 ± 204.030  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2        thrpt   15  7724.856 ± 102.321  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3        thrpt   15  7176.097 ±  39.847  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2          thrpt   15  1387.038 ±  18.351  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3          thrpt   15  1659.115 ±   6.392  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2          thrpt   15  1972.565 ±  28.763  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3          thrpt   15  2149.149 ±   8.789  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2            thrpt   15  1697.916 ±  24.838  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3            thrpt   15  1484.278 ±  21.551  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2            thrpt   15  1958.234 ±   9.490  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3            thrpt   15  2178.033 ±   6.724  ops/ms

Benchmark result is saved to /root/bench-fj3/results-pub/arm-57caa76/results.json
=== FINISHED 2026-04-13T17:09:54+08:00 ===
```

## x86_64 4c

**JDK**: 25.0.2  
**Arch**: x86_64  
**Cores**: 4  
**OS**: Debian GNU/Linux 13 (trixie)  

```
=== STARTED 2026-04-13T15:34:49+08:00 ===
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson2

# Run progress: 0.00% complete, ETA 01:36:00
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13100.995 ops/ms
# Warmup Iteration   2: 13368.905 ops/ms
# Warmup Iteration   3: 13421.037 ops/ms
Iteration   1: 13378.622 ops/ms
Iteration   2: 13424.669 ops/ms
Iteration   3: 13462.307 ops/ms
Iteration   4: 13457.780 ops/ms
Iteration   5: 13447.379 ops/ms

# Run progress: 1.39% complete, ETA 01:35:34
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13051.390 ops/ms
# Warmup Iteration   2: 13355.274 ops/ms
# Warmup Iteration   3: 13343.388 ops/ms
Iteration   1: 13344.092 ops/ms
Iteration   2: 13350.964 ops/ms
Iteration   3: 13279.930 ops/ms
Iteration   4: 13310.860 ops/ms
Iteration   5: 13324.358 ops/ms

# Run progress: 2.78% complete, ETA 01:34:12
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 12999.484 ops/ms
# Warmup Iteration   2: 13346.676 ops/ms
# Warmup Iteration   3: 13378.695 ops/ms
Iteration   1: 13380.676 ops/ms
Iteration   2: 13419.219 ops/ms
Iteration   3: 13385.264 ops/ms
Iteration   4: 13416.439 ops/ms
Iteration   5: 13471.738 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson2":
  13390.287 ±(99.9%) 63.315 ops/ms [Average]
  (min, avg, max) = (13279.930, 13390.287, 13471.738), stdev = 59.225
  CI (99.9%): [13326.971, 13453.602] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3

# Run progress: 4.17% complete, ETA 01:32:50
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10425.910 ops/ms
# Warmup Iteration   2: 10633.165 ops/ms
# Warmup Iteration   3: 10597.230 ops/ms
Iteration   1: 10602.976 ops/ms
Iteration   2: 10589.482 ops/ms
Iteration   3: 10600.085 ops/ms
Iteration   4: 10593.937 ops/ms
Iteration   5: 10574.981 ops/ms

# Run progress: 5.56% complete, ETA 01:31:28
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 9979.063 ops/ms
# Warmup Iteration   2: 10161.897 ops/ms
# Warmup Iteration   3: 10133.884 ops/ms
Iteration   1: 10147.696 ops/ms
Iteration   2: 10094.656 ops/ms
Iteration   3: 10035.873 ops/ms
Iteration   4: 10107.082 ops/ms
Iteration   5: 10094.700 ops/ms

# Run progress: 6.94% complete, ETA 01:30:09
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10285.784 ops/ms
# Warmup Iteration   2: 10470.304 ops/ms
# Warmup Iteration   3: 10485.370 ops/ms
Iteration   1: 10471.924 ops/ms
Iteration   2: 10519.002 ops/ms
Iteration   3: 10508.382 ops/ms
Iteration   4: 10470.042 ops/ms
Iteration   5: 10437.804 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3":
  10389.908 ±(99.9%) 237.292 ops/ms [Average]
  (min, avg, max) = (10035.873, 10389.908, 10602.976), stdev = 221.963
  CI (99.9%): [10152.616, 10627.200] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson2

# Run progress: 8.33% complete, ETA 01:28:48
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 9431.830 ops/ms
# Warmup Iteration   2: 9591.532 ops/ms
# Warmup Iteration   3: 9611.055 ops/ms
Iteration   1: 9617.102 ops/ms
Iteration   2: 9616.062 ops/ms
Iteration   3: 9638.735 ops/ms
Iteration   4: 9625.906 ops/ms
Iteration   5: 9617.464 ops/ms

# Run progress: 9.72% complete, ETA 01:27:27
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8828.185 ops/ms
# Warmup Iteration   2: 9242.938 ops/ms
# Warmup Iteration   3: 9266.220 ops/ms
Iteration   1: 9253.778 ops/ms
Iteration   2: 9247.100 ops/ms
Iteration   3: 9189.398 ops/ms
Iteration   4: 9250.182 ops/ms
Iteration   5: 9263.237 ops/ms

# Run progress: 11.11% complete, ETA 01:26:06
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8315.690 ops/ms
# Warmup Iteration   2: 8661.404 ops/ms
# Warmup Iteration   3: 8649.000 ops/ms
Iteration   1: 8606.746 ops/ms
Iteration   2: 8623.656 ops/ms
Iteration   3: 8609.262 ops/ms
Iteration   4: 8676.073 ops/ms
Iteration   5: 8679.891 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson2":
  9167.640 ±(99.9%) 448.986 ops/ms [Average]
  (min, avg, max) = (8606.746, 9167.640, 9638.735), stdev = 419.981
  CI (99.9%): [8718.654, 9616.625] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson3

# Run progress: 12.50% complete, ETA 01:24:46
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8704.724 ops/ms
# Warmup Iteration   2: 8914.081 ops/ms
# Warmup Iteration   3: 8960.593 ops/ms
Iteration   1: 8936.123 ops/ms
Iteration   2: 8948.420 ops/ms
Iteration   3: 8869.036 ops/ms
Iteration   4: 8923.349 ops/ms
Iteration   5: 8944.035 ops/ms

# Run progress: 13.89% complete, ETA 01:23:25
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8407.084 ops/ms
# Warmup Iteration   2: 8538.227 ops/ms
# Warmup Iteration   3: 8700.896 ops/ms
Iteration   1: 8701.292 ops/ms
Iteration   2: 8698.363 ops/ms
Iteration   3: 8703.665 ops/ms
Iteration   4: 8660.062 ops/ms
Iteration   5: 8669.473 ops/ms

# Run progress: 15.28% complete, ETA 01:22:04
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8408.711 ops/ms
# Warmup Iteration   2: 8715.229 ops/ms
# Warmup Iteration   3: 8758.960 ops/ms
Iteration   1: 8745.668 ops/ms
Iteration   2: 8765.091 ops/ms
Iteration   3: 8774.788 ops/ms
Iteration   4: 8737.045 ops/ms
Iteration   5: 8741.887 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson3":
  8787.886 ±(99.9%) 113.285 ops/ms [Average]
  (min, avg, max) = (8660.062, 8787.886, 8948.420), stdev = 105.967
  CI (99.9%): [8674.602, 8901.171] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson2

# Run progress: 16.67% complete, ETA 01:20:43
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7226.277 ops/ms
# Warmup Iteration   2: 7269.983 ops/ms
# Warmup Iteration   3: 7359.754 ops/ms
Iteration   1: 7333.813 ops/ms
Iteration   2: 7334.093 ops/ms
Iteration   3: 7345.682 ops/ms
Iteration   4: 7379.406 ops/ms
Iteration   5: 7375.033 ops/ms

# Run progress: 18.06% complete, ETA 01:19:22
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7329.639 ops/ms
# Warmup Iteration   2: 7389.818 ops/ms
# Warmup Iteration   3: 7461.635 ops/ms
Iteration   1: 7436.617 ops/ms
Iteration   2: 7450.986 ops/ms
Iteration   3: 7441.119 ops/ms
Iteration   4: 7450.812 ops/ms
Iteration   5: 7473.116 ops/ms

# Run progress: 19.44% complete, ETA 01:18:01
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7292.379 ops/ms
# Warmup Iteration   2: 7386.233 ops/ms
# Warmup Iteration   3: 7417.455 ops/ms
Iteration   1: 7459.980 ops/ms
Iteration   2: 7402.487 ops/ms
Iteration   3: 7409.857 ops/ms
Iteration   4: 7414.417 ops/ms
Iteration   5: 7405.518 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson2":
  7407.529 ±(99.9%) 48.966 ops/ms [Average]
  (min, avg, max) = (7333.813, 7407.529, 7473.116), stdev = 45.802
  CI (99.9%): [7358.564, 7456.495] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson3

# Run progress: 20.83% complete, ETA 01:16:39
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5229.869 ops/ms
# Warmup Iteration   2: 5302.995 ops/ms
# Warmup Iteration   3: 5298.516 ops/ms
Iteration   1: 5296.442 ops/ms
Iteration   2: 5290.585 ops/ms
Iteration   3: 5295.511 ops/ms
Iteration   4: 5287.326 ops/ms
Iteration   5: 5271.330 ops/ms

# Run progress: 22.22% complete, ETA 01:15:19
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5233.408 ops/ms
# Warmup Iteration   2: 5330.858 ops/ms
# Warmup Iteration   3: 5312.777 ops/ms
Iteration   1: 5307.533 ops/ms
Iteration   2: 5292.734 ops/ms
Iteration   3: 5305.307 ops/ms
Iteration   4: 5307.297 ops/ms
Iteration   5: 5309.941 ops/ms

# Run progress: 23.61% complete, ETA 01:13:59
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5187.720 ops/ms
# Warmup Iteration   2: 5278.277 ops/ms
# Warmup Iteration   3: 5285.399 ops/ms
Iteration   1: 5290.742 ops/ms
Iteration   2: 5271.615 ops/ms
Iteration   3: 5280.654 ops/ms
Iteration   4: 5277.435 ops/ms
Iteration   5: 5255.208 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson3":
  5289.311 ±(99.9%) 16.809 ops/ms [Average]
  (min, avg, max) = (5255.208, 5289.311, 5309.941), stdev = 15.723
  CI (99.9%): [5272.501, 5306.120] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson2

# Run progress: 25.00% complete, ETA 01:12:38
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7186.965 ops/ms
# Warmup Iteration   2: 7313.169 ops/ms
# Warmup Iteration   3: 7330.385 ops/ms
Iteration   1: 7268.212 ops/ms
Iteration   2: 7353.551 ops/ms
Iteration   3: 7358.659 ops/ms
Iteration   4: 7318.107 ops/ms
Iteration   5: 7354.942 ops/ms

# Run progress: 26.39% complete, ETA 01:11:17
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7298.421 ops/ms
# Warmup Iteration   2: 7394.028 ops/ms
# Warmup Iteration   3: 7424.170 ops/ms
Iteration   1: 7420.807 ops/ms
Iteration   2: 7378.233 ops/ms
Iteration   3: 7410.974 ops/ms
Iteration   4: 7423.309 ops/ms
Iteration   5: 7408.325 ops/ms

# Run progress: 27.78% complete, ETA 01:09:57
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7190.419 ops/ms
# Warmup Iteration   2: 7354.514 ops/ms
# Warmup Iteration   3: 7342.828 ops/ms
Iteration   1: 7350.693 ops/ms
Iteration   2: 7359.500 ops/ms
Iteration   3: 7362.485 ops/ms
Iteration   4: 7323.424 ops/ms
Iteration   5: 7348.758 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson2":
  7362.665 ±(99.9%) 44.878 ops/ms [Average]
  (min, avg, max) = (7268.212, 7362.665, 7423.309), stdev = 41.979
  CI (99.9%): [7317.787, 7407.544] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson3

# Run progress: 29.17% complete, ETA 01:08:36
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5269.544 ops/ms
# Warmup Iteration   2: 5342.836 ops/ms
# Warmup Iteration   3: 5347.060 ops/ms
Iteration   1: 5343.321 ops/ms
Iteration   2: 5334.984 ops/ms
Iteration   3: 5344.564 ops/ms
Iteration   4: 5352.199 ops/ms
Iteration   5: 5343.391 ops/ms

# Run progress: 30.56% complete, ETA 01:07:15
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5231.048 ops/ms
# Warmup Iteration   2: 5323.438 ops/ms
# Warmup Iteration   3: 5306.197 ops/ms
Iteration   1: 5317.490 ops/ms
Iteration   2: 5288.692 ops/ms
Iteration   3: 5289.614 ops/ms
Iteration   4: 5290.167 ops/ms
Iteration   5: 5305.675 ops/ms

# Run progress: 31.94% complete, ETA 01:05:54
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5268.772 ops/ms
# Warmup Iteration   2: 5350.837 ops/ms
# Warmup Iteration   3: 5298.664 ops/ms
Iteration   1: 5328.100 ops/ms
Iteration   2: 5328.274 ops/ms
Iteration   3: 5329.719 ops/ms
Iteration   4: 5331.753 ops/ms
Iteration   5: 5330.115 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson3":
  5323.871 ±(99.9%) 22.506 ops/ms [Average]
  (min, avg, max) = (5288.692, 5323.871, 5352.199), stdev = 21.053
  CI (99.9%): [5301.364, 5346.377] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson2

# Run progress: 33.33% complete, ETA 01:04:33
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13123.890 ops/ms
# Warmup Iteration   2: 13510.364 ops/ms
# Warmup Iteration   3: 13431.728 ops/ms
Iteration   1: 13499.463 ops/ms
Iteration   2: 13401.567 ops/ms
Iteration   3: 13517.221 ops/ms
Iteration   4: 13483.167 ops/ms
Iteration   5: 13473.041 ops/ms

# Run progress: 34.72% complete, ETA 01:03:13
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13209.824 ops/ms
# Warmup Iteration   2: 13612.001 ops/ms
# Warmup Iteration   3: 13619.362 ops/ms
Iteration   1: 13582.679 ops/ms
Iteration   2: 13608.209 ops/ms
Iteration   3: 13573.262 ops/ms
Iteration   4: 13616.027 ops/ms
Iteration   5: 13579.409 ops/ms

# Run progress: 36.11% complete, ETA 01:01:52
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 12928.255 ops/ms
# Warmup Iteration   2: 13249.250 ops/ms
# Warmup Iteration   3: 13053.837 ops/ms
Iteration   1: 13186.021 ops/ms
Iteration   2: 13225.287 ops/ms
Iteration   3: 13175.412 ops/ms
Iteration   4: 13123.817 ops/ms
Iteration   5: 13242.263 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson2":
  13419.123 ±(99.9%) 190.364 ops/ms [Average]
  (min, avg, max) = (13123.817, 13419.123, 13616.027), stdev = 178.067
  CI (99.9%): [13228.759, 13609.487] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3

# Run progress: 37.50% complete, ETA 01:00:31
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10270.839 ops/ms
# Warmup Iteration   2: 10514.766 ops/ms
# Warmup Iteration   3: 10627.752 ops/ms
Iteration   1: 10634.984 ops/ms
Iteration   2: 10614.414 ops/ms
Iteration   3: 10599.084 ops/ms
Iteration   4: 10569.138 ops/ms
Iteration   5: 10607.359 ops/ms

# Run progress: 38.89% complete, ETA 00:59:10
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10850.746 ops/ms
# Warmup Iteration   2: 10843.040 ops/ms
# Warmup Iteration   3: 10889.871 ops/ms
Iteration   1: 10999.661 ops/ms
Iteration   2: 10985.326 ops/ms
Iteration   3: 11015.447 ops/ms
Iteration   4: 10988.759 ops/ms
Iteration   5: 11010.251 ops/ms

# Run progress: 40.28% complete, ETA 00:57:50
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10626.395 ops/ms
# Warmup Iteration   2: 10762.077 ops/ms
# Warmup Iteration   3: 10815.918 ops/ms
Iteration   1: 10775.787 ops/ms
Iteration   2: 10765.010 ops/ms
Iteration   3: 10765.980 ops/ms
Iteration   4: 10780.586 ops/ms
Iteration   5: 10748.274 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3":
  10790.671 ±(99.9%) 180.167 ops/ms [Average]
  (min, avg, max) = (10569.138, 10790.671, 11015.447), stdev = 168.528
  CI (99.9%): [10610.504, 10970.838] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson2

# Run progress: 41.67% complete, ETA 00:56:29
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8910.539 ops/ms
# Warmup Iteration   2: 9474.232 ops/ms
# Warmup Iteration   3: 9468.818 ops/ms
Iteration   1: 9461.953 ops/ms
Iteration   2: 9506.971 ops/ms
Iteration   3: 9407.594 ops/ms
Iteration   4: 9498.175 ops/ms
Iteration   5: 9485.427 ops/ms

# Run progress: 43.06% complete, ETA 00:55:08
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 9291.742 ops/ms
# Warmup Iteration   2: 9609.974 ops/ms
# Warmup Iteration   3: 9605.394 ops/ms
Iteration   1: 9611.048 ops/ms
Iteration   2: 9587.534 ops/ms
Iteration   3: 9609.249 ops/ms
Iteration   4: 9566.266 ops/ms
Iteration   5: 9134.470 ops/ms

# Run progress: 44.44% complete, ETA 00:53:48
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8412.327 ops/ms
# Warmup Iteration   2: 8875.124 ops/ms
# Warmup Iteration   3: 8893.686 ops/ms
Iteration   1: 8901.596 ops/ms
Iteration   2: 8893.702 ops/ms
Iteration   3: 8870.301 ops/ms
Iteration   4: 8880.584 ops/ms
Iteration   5: 8895.844 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson2":
  9287.381 ±(99.9%) 334.787 ops/ms [Average]
  (min, avg, max) = (8870.301, 9287.381, 9611.048), stdev = 313.160
  CI (99.9%): [8952.594, 9622.168] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson3

# Run progress: 45.83% complete, ETA 00:52:27
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 9199.084 ops/ms
# Warmup Iteration   2: 9465.736 ops/ms
# Warmup Iteration   3: 9347.152 ops/ms
Iteration   1: 9358.242 ops/ms
Iteration   2: 9145.206 ops/ms
Iteration   3: 9166.257 ops/ms
Iteration   4: 9245.367 ops/ms
Iteration   5: 9325.248 ops/ms

# Run progress: 47.22% complete, ETA 00:51:06
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8686.032 ops/ms
# Warmup Iteration   2: 9076.782 ops/ms
# Warmup Iteration   3: 9068.734 ops/ms
Iteration   1: 9065.788 ops/ms
Iteration   2: 9142.654 ops/ms
Iteration   3: 9178.624 ops/ms
Iteration   4: 9125.552 ops/ms
Iteration   5: 9153.035 ops/ms

# Run progress: 48.61% complete, ETA 00:49:46
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8992.101 ops/ms
# Warmup Iteration   2: 9322.723 ops/ms
# Warmup Iteration   3: 9344.672 ops/ms
Iteration   1: 9336.817 ops/ms
Iteration   2: 9337.465 ops/ms
Iteration   3: 9272.767 ops/ms
Iteration   4: 9321.633 ops/ms
Iteration   5: 9328.515 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson3":
  9233.545 ±(99.9%) 104.503 ops/ms [Average]
  (min, avg, max) = (9065.788, 9233.545, 9358.242), stdev = 97.752
  CI (99.9%): [9129.042, 9338.047] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson2

# Run progress: 50.00% complete, ETA 00:48:25
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 14047.368 ops/ms
# Warmup Iteration   2: 14321.591 ops/ms
# Warmup Iteration   3: 14342.689 ops/ms
Iteration   1: 14341.868 ops/ms
Iteration   2: 14342.450 ops/ms
Iteration   3: 14335.860 ops/ms
Iteration   4: 14319.572 ops/ms
Iteration   5: 14356.750 ops/ms

# Run progress: 51.39% complete, ETA 00:47:04
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 11887.705 ops/ms
# Warmup Iteration   2: 12403.729 ops/ms
# Warmup Iteration   3: 12351.168 ops/ms
Iteration   1: 12366.322 ops/ms
Iteration   2: 12408.023 ops/ms
Iteration   3: 12515.111 ops/ms
Iteration   4: 12327.601 ops/ms
Iteration   5: 12485.184 ops/ms

# Run progress: 52.78% complete, ETA 00:45:44
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13438.805 ops/ms
# Warmup Iteration   2: 13525.562 ops/ms
# Warmup Iteration   3: 13733.784 ops/ms
Iteration   1: 13797.757 ops/ms
Iteration   2: 13746.746 ops/ms
Iteration   3: 13708.108 ops/ms
Iteration   4: 13726.023 ops/ms
Iteration   5: 13761.216 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson2":
  13502.573 ±(99.9%) 889.266 ops/ms [Average]
  (min, avg, max) = (12327.601, 13502.573, 14356.750), stdev = 831.820
  CI (99.9%): [12613.307, 14391.838] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3

# Run progress: 54.17% complete, ETA 00:44:23
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13682.383 ops/ms
# Warmup Iteration   2: 13857.496 ops/ms
# Warmup Iteration   3: 13939.992 ops/ms
Iteration   1: 13824.689 ops/ms
Iteration   2: 13929.624 ops/ms
Iteration   3: 13931.934 ops/ms
Iteration   4: 13962.557 ops/ms
Iteration   5: 13961.093 ops/ms

# Run progress: 55.56% complete, ETA 00:43:02
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13594.962 ops/ms
# Warmup Iteration   2: 13816.241 ops/ms
# Warmup Iteration   3: 13923.413 ops/ms
Iteration   1: 13834.665 ops/ms
Iteration   2: 13696.231 ops/ms
Iteration   3: 13664.809 ops/ms
Iteration   4: 13676.525 ops/ms
Iteration   5: 13642.649 ops/ms

# Run progress: 56.94% complete, ETA 00:41:42
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13691.627 ops/ms
# Warmup Iteration   2: 13881.483 ops/ms
# Warmup Iteration   3: 13857.276 ops/ms
Iteration   1: 13909.918 ops/ms
Iteration   2: 13935.121 ops/ms
Iteration   3: 13857.888 ops/ms
Iteration   4: 13904.187 ops/ms
Iteration   5: 13817.116 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3":
  13836.600 ±(99.9%) 121.956 ops/ms [Average]
  (min, avg, max) = (13642.649, 13836.600, 13962.557), stdev = 114.077
  CI (99.9%): [13714.645, 13958.556] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson2

# Run progress: 58.33% complete, ETA 00:40:21
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 17029.929 ops/ms
# Warmup Iteration   2: 17376.902 ops/ms
# Warmup Iteration   3: 17294.587 ops/ms
Iteration   1: 17361.690 ops/ms
Iteration   2: 17454.258 ops/ms
Iteration   3: 17435.708 ops/ms
Iteration   4: 17426.281 ops/ms
Iteration   5: 17427.161 ops/ms

# Run progress: 59.72% complete, ETA 00:39:00
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 17656.528 ops/ms
# Warmup Iteration   2: 17893.801 ops/ms
# Warmup Iteration   3: 18140.401 ops/ms
Iteration   1: 18058.795 ops/ms
Iteration   2: 18080.559 ops/ms
Iteration   3: 17976.101 ops/ms
Iteration   4: 18164.975 ops/ms
Iteration   5: 18238.821 ops/ms

# Run progress: 61.11% complete, ETA 00:37:40
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 17230.686 ops/ms
# Warmup Iteration   2: 17514.871 ops/ms
# Warmup Iteration   3: 17630.643 ops/ms
Iteration   1: 17567.950 ops/ms
Iteration   2: 17535.155 ops/ms
Iteration   3: 17532.732 ops/ms
Iteration   4: 17541.025 ops/ms
Iteration   5: 17525.388 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson2":
  17688.440 ±(99.9%) 335.245 ops/ms [Average]
  (min, avg, max) = (17361.690, 17688.440, 18238.821), stdev = 313.588
  CI (99.9%): [17353.195, 18023.685] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3

# Run progress: 62.50% complete, ETA 00:36:19
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 12821.895 ops/ms
# Warmup Iteration   2: 12905.789 ops/ms
# Warmup Iteration   3: 12852.782 ops/ms
Iteration   1: 12821.263 ops/ms
Iteration   2: 12825.682 ops/ms
Iteration   3: 12922.742 ops/ms
Iteration   4: 12938.426 ops/ms
Iteration   5: 12932.498 ops/ms

# Run progress: 63.89% complete, ETA 00:34:58
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 12417.849 ops/ms
# Warmup Iteration   2: 12534.279 ops/ms
# Warmup Iteration   3: 12559.155 ops/ms
Iteration   1: 12579.359 ops/ms
Iteration   2: 12497.771 ops/ms
Iteration   3: 12505.747 ops/ms
Iteration   4: 12455.508 ops/ms
Iteration   5: 12536.242 ops/ms

# Run progress: 65.28% complete, ETA 00:33:38
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13664.075 ops/ms
# Warmup Iteration   2: 13843.930 ops/ms
# Warmup Iteration   3: 13859.854 ops/ms
Iteration   1: 13813.591 ops/ms
Iteration   2: 13853.746 ops/ms
Iteration   3: 13897.582 ops/ms
Iteration   4: 13854.832 ops/ms
Iteration   5: 13864.205 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3":
  13086.613 ±(99.9%) 627.488 ops/ms [Average]
  (min, avg, max) = (12455.508, 13086.613, 13897.582), stdev = 586.952
  CI (99.9%): [12459.125, 13714.100] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson2

# Run progress: 66.67% complete, ETA 00:32:17
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2675.453 ops/ms
# Warmup Iteration   2: 2752.871 ops/ms
# Warmup Iteration   3: 2758.626 ops/ms
Iteration   1: 2752.505 ops/ms
Iteration   2: 2742.838 ops/ms
Iteration   3: 2744.566 ops/ms
Iteration   4: 2752.009 ops/ms
Iteration   5: 2749.968 ops/ms

# Run progress: 68.06% complete, ETA 00:30:56
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2680.373 ops/ms
# Warmup Iteration   2: 2760.715 ops/ms
# Warmup Iteration   3: 2748.464 ops/ms
Iteration   1: 2772.674 ops/ms
Iteration   2: 2766.758 ops/ms
Iteration   3: 2761.140 ops/ms
Iteration   4: 2778.897 ops/ms
Iteration   5: 2771.268 ops/ms

# Run progress: 69.44% complete, ETA 00:29:36
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2598.216 ops/ms
# Warmup Iteration   2: 2692.446 ops/ms
# Warmup Iteration   3: 2691.334 ops/ms
Iteration   1: 2680.049 ops/ms
Iteration   2: 2655.530 ops/ms
Iteration   3: 2671.342 ops/ms
Iteration   4: 2682.925 ops/ms
Iteration   5: 2685.201 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson2":
  2731.178 ±(99.9%) 45.792 ops/ms [Average]
  (min, avg, max) = (2655.530, 2731.178, 2778.897), stdev = 42.834
  CI (99.9%): [2685.386, 2776.970] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3

# Run progress: 70.83% complete, ETA 00:28:15
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2698.103 ops/ms
# Warmup Iteration   2: 2727.642 ops/ms
# Warmup Iteration   3: 2729.273 ops/ms
Iteration   1: 2714.093 ops/ms
Iteration   2: 2703.962 ops/ms
Iteration   3: 2719.070 ops/ms
Iteration   4: 2720.036 ops/ms
Iteration   5: 2720.916 ops/ms

# Run progress: 72.22% complete, ETA 00:26:54
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2674.620 ops/ms
# Warmup Iteration   2: 2702.377 ops/ms
# Warmup Iteration   3: 2709.543 ops/ms
Iteration   1: 2706.834 ops/ms
Iteration   2: 2707.243 ops/ms
Iteration   3: 2704.874 ops/ms
Iteration   4: 2702.398 ops/ms
Iteration   5: 2711.238 ops/ms

# Run progress: 73.61% complete, ETA 00:25:33
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2693.296 ops/ms
# Warmup Iteration   2: 2745.133 ops/ms
# Warmup Iteration   3: 2741.517 ops/ms
Iteration   1: 2726.584 ops/ms
Iteration   2: 2730.375 ops/ms
Iteration   3: 2721.231 ops/ms
Iteration   4: 2731.018 ops/ms
Iteration   5: 2733.783 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3":
  2716.910 ±(99.9%) 11.307 ops/ms [Average]
  (min, avg, max) = (2702.398, 2716.910, 2733.783), stdev = 10.576
  CI (99.9%): [2705.604, 2728.217] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson2

# Run progress: 75.00% complete, ETA 00:24:13
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2979.855 ops/ms
# Warmup Iteration   2: 3048.396 ops/ms
# Warmup Iteration   3: 3019.349 ops/ms
Iteration   1: 3029.243 ops/ms
Iteration   2: 3034.268 ops/ms
Iteration   3: 3037.207 ops/ms
Iteration   4: 3051.875 ops/ms
Iteration   5: 3022.908 ops/ms

# Run progress: 76.39% complete, ETA 00:22:52
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2977.950 ops/ms
# Warmup Iteration   2: 3069.468 ops/ms
# Warmup Iteration   3: 3079.409 ops/ms
Iteration   1: 3090.043 ops/ms
Iteration   2: 3084.437 ops/ms
Iteration   3: 3065.835 ops/ms
Iteration   4: 3081.497 ops/ms
Iteration   5: 3081.773 ops/ms

# Run progress: 77.78% complete, ETA 00:21:31
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3081.098 ops/ms
# Warmup Iteration   2: 3160.164 ops/ms
# Warmup Iteration   3: 3181.000 ops/ms
Iteration   1: 3175.062 ops/ms
Iteration   2: 3166.514 ops/ms
Iteration   3: 3133.649 ops/ms
Iteration   4: 3163.170 ops/ms
Iteration   5: 3172.473 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson2":
  3092.664 ±(99.9%) 59.481 ops/ms [Average]
  (min, avg, max) = (3022.908, 3092.664, 3175.062), stdev = 55.638
  CI (99.9%): [3033.183, 3152.144] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3

# Run progress: 79.17% complete, ETA 00:20:10
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3570.318 ops/ms
# Warmup Iteration   2: 3616.632 ops/ms
# Warmup Iteration   3: 3624.225 ops/ms
Iteration   1: 3625.833 ops/ms
Iteration   2: 3631.309 ops/ms
Iteration   3: 3609.999 ops/ms
Iteration   4: 3610.595 ops/ms
Iteration   5: 3585.046 ops/ms

# Run progress: 80.56% complete, ETA 00:18:50
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3691.209 ops/ms
# Warmup Iteration   2: 3786.018 ops/ms
# Warmup Iteration   3: 3771.855 ops/ms
Iteration   1: 3778.762 ops/ms
Iteration   2: 3767.651 ops/ms
Iteration   3: 3774.252 ops/ms
Iteration   4: 3773.028 ops/ms
Iteration   5: 3774.052 ops/ms

# Run progress: 81.94% complete, ETA 00:17:29
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3662.999 ops/ms
# Warmup Iteration   2: 3710.742 ops/ms
# Warmup Iteration   3: 3719.237 ops/ms
Iteration   1: 3728.595 ops/ms
Iteration   2: 3721.105 ops/ms
Iteration   3: 3707.820 ops/ms
Iteration   4: 3719.817 ops/ms
Iteration   5: 3710.145 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3":
  3701.201 ±(99.9%) 74.744 ops/ms [Average]
  (min, avg, max) = (3585.046, 3701.201, 3778.762), stdev = 69.916
  CI (99.9%): [3626.456, 3775.945] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson2

# Run progress: 83.33% complete, ETA 00:16:08
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2884.361 ops/ms
# Warmup Iteration   2: 2938.360 ops/ms
# Warmup Iteration   3: 2970.392 ops/ms
Iteration   1: 2942.352 ops/ms
Iteration   2: 2955.121 ops/ms
Iteration   3: 2951.511 ops/ms
Iteration   4: 2943.126 ops/ms
Iteration   5: 2953.424 ops/ms

# Run progress: 84.72% complete, ETA 00:14:48
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1092.167 ops/ms
# Warmup Iteration   2: 1111.381 ops/ms
# Warmup Iteration   3: 1106.564 ops/ms
Iteration   1: 1106.559 ops/ms
Iteration   2: 1108.663 ops/ms
Iteration   3: 1110.748 ops/ms
Iteration   4: 1116.412 ops/ms
Iteration   5: 1115.893 ops/ms

# Run progress: 86.11% complete, ETA 00:13:27
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2987.913 ops/ms
# Warmup Iteration   2: 3048.854 ops/ms
# Warmup Iteration   3: 3055.078 ops/ms
Iteration   1: 3039.914 ops/ms
Iteration   2: 3046.598 ops/ms
Iteration   3: 3039.463 ops/ms
Iteration   4: 3032.703 ops/ms
Iteration   5: 3046.116 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson2":
  2367.240 ±(99.9%) 983.351 ops/ms [Average]
  (min, avg, max) = (1106.559, 2367.240, 3046.598), stdev = 919.827
  CI (99.9%): [1383.889, 3350.591] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3

# Run progress: 87.50% complete, ETA 00:12:06
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2779.183 ops/ms
# Warmup Iteration   2: 2815.248 ops/ms
# Warmup Iteration   3: 2808.327 ops/ms
Iteration   1: 2793.348 ops/ms
Iteration   2: 2799.740 ops/ms
Iteration   3: 2804.568 ops/ms
Iteration   4: 2804.889 ops/ms
Iteration   5: 2818.381 ops/ms

# Run progress: 88.89% complete, ETA 00:10:45
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2714.940 ops/ms
# Warmup Iteration   2: 2599.059 ops/ms
# Warmup Iteration   3: 2705.911 ops/ms
Iteration   1: 2804.851 ops/ms
Iteration   2: 2799.422 ops/ms
Iteration   3: 2801.837 ops/ms
Iteration   4: 2802.142 ops/ms
Iteration   5: 2805.256 ops/ms

# Run progress: 90.28% complete, ETA 00:09:25
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2751.566 ops/ms
# Warmup Iteration   2: 2795.372 ops/ms
# Warmup Iteration   3: 2787.171 ops/ms
Iteration   1: 2772.382 ops/ms
Iteration   2: 2791.632 ops/ms
Iteration   3: 2793.107 ops/ms
Iteration   4: 2803.017 ops/ms
Iteration   5: 2791.112 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3":
  2799.046 ±(99.9%) 10.846 ops/ms [Average]
  (min, avg, max) = (2772.382, 2799.046, 2818.381), stdev = 10.146
  CI (99.9%): [2788.199, 2809.892] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson2

# Run progress: 91.67% complete, ETA 00:08:04
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3290.527 ops/ms
# Warmup Iteration   2: 3366.944 ops/ms
# Warmup Iteration   3: 3362.186 ops/ms
Iteration   1: 3349.545 ops/ms
Iteration   2: 3373.759 ops/ms
Iteration   3: 3356.842 ops/ms
Iteration   4: 3371.119 ops/ms
Iteration   5: 3368.517 ops/ms

# Run progress: 93.06% complete, ETA 00:06:43
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3209.833 ops/ms
# Warmup Iteration   2: 3356.718 ops/ms
# Warmup Iteration   3: 3354.606 ops/ms
Iteration   1: 3364.193 ops/ms
Iteration   2: 3350.749 ops/ms
Iteration   3: 3369.887 ops/ms
Iteration   4: 3362.717 ops/ms
Iteration   5: 3343.958 ops/ms

# Run progress: 94.44% complete, ETA 00:05:22
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3163.254 ops/ms
# Warmup Iteration   2: 3267.677 ops/ms
# Warmup Iteration   3: 3265.828 ops/ms
Iteration   1: 3267.820 ops/ms
Iteration   2: 3249.888 ops/ms
Iteration   3: 3259.996 ops/ms
Iteration   4: 3251.751 ops/ms
Iteration   5: 3248.885 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson2":
  3325.975 ±(99.9%) 55.910 ops/ms [Average]
  (min, avg, max) = (3248.885, 3325.975, 3373.759), stdev = 52.298
  CI (99.9%): [3270.065, 3381.885] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: <none>
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 16 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3

# Run progress: 95.83% complete, ETA 00:04:02
# Fork: 1 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3918.868 ops/ms
# Warmup Iteration   2: 3990.780 ops/ms
# Warmup Iteration   3: 3954.008 ops/ms
Iteration   1: 3952.587 ops/ms
Iteration   2: 3958.402 ops/ms
Iteration   3: 3960.217 ops/ms
Iteration   4: 3958.694 ops/ms
Iteration   5: 3958.115 ops/ms

# Run progress: 97.22% complete, ETA 00:02:41
# Fork: 2 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3879.936 ops/ms
# Warmup Iteration   2: 3961.286 ops/ms
# Warmup Iteration   3: 3953.459 ops/ms
Iteration   1: 3953.294 ops/ms
Iteration   2: 3958.849 ops/ms
Iteration   3: 3956.329 ops/ms
Iteration   4: 3937.111 ops/ms
Iteration   5: 3960.800 ops/ms

# Run progress: 98.61% complete, ETA 00:01:20
# Fork: 3 of 3
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3844.730 ops/ms
# Warmup Iteration   2: 3903.628 ops/ms
# Warmup Iteration   3: 3894.913 ops/ms
Iteration   1: 3890.511 ops/ms
Iteration   2: 3884.325 ops/ms
Iteration   3: 3905.250 ops/ms
Iteration   4: 3912.474 ops/ms
Iteration   5: 3915.556 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3":
  3937.501 ±(99.9%) 29.749 ops/ms [Average]
  (min, avg, max) = (3884.325, 3937.501, 3960.800), stdev = 27.827
  CI (99.9%): [3907.752, 3967.250] (assumes normal distribution)


# Run complete. Total time: 01:36:53

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

NOTE: Current JVM experimentally supports Compiler Blackholes, and they are in use. Please exercise
extra caution when trusting the results, look into the generated code to check the benchmark still
works, and factor in a small probability of new VM bugs. Additionally, while comparisons between
different JVMs are already problematic, the performance difference caused by different Blackhole
modes can be very significant. Please make sure you use the consistent Blackhole mode for comparisons.

Benchmark                                             Mode  Cnt      Score     Error   Units
c.a.f.b.eishay.EishayParseString.fastjson2           thrpt   15  13390.287 ±  63.315  ops/ms
c.a.f.b.eishay.EishayParseString.fastjson3           thrpt   15  10389.908 ± 237.292  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.fastjson2     thrpt   15   9167.640 ± 448.986  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.fastjson3     thrpt   15   8787.886 ± 113.285  ops/ms
c.a.f.b.eishay.EishayParseTreeString.fastjson2       thrpt   15   7407.529 ±  48.966  ops/ms
c.a.f.b.eishay.EishayParseTreeString.fastjson3       thrpt   15   5289.311 ±  16.809  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2    thrpt   15   7362.665 ±  44.878  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3    thrpt   15   5323.871 ±  22.506  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2        thrpt   15  13419.123 ± 190.364  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3        thrpt   15  10790.671 ± 180.167  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.fastjson2  thrpt   15   9287.381 ± 334.787  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.fastjson3  thrpt   15   9233.545 ± 104.503  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson2           thrpt   15  13502.573 ± 889.266  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson3           thrpt   15  13836.600 ± 121.956  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2        thrpt   15  17688.440 ± 335.245  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3        thrpt   15  13086.613 ± 627.488  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2          thrpt   15   2731.178 ±  45.792  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3          thrpt   15   2716.910 ±  11.307  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2          thrpt   15   3092.664 ±  59.481  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3          thrpt   15   3701.201 ±  74.744  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2            thrpt   15   2367.240 ± 983.351  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3            thrpt   15   2799.046 ±  10.846  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2            thrpt   15   3325.975 ±  55.910  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3            thrpt   15   3937.501 ±  29.749  ops/ms

Benchmark result is saved to /root/bench-fj3/results-pub/x86-57caa76/results.json
=== FINISHED 2026-04-13T17:11:43+08:00 ===
```


# fastjson3 Benchmark Raw Output — 3.0.0-SNAPSHOT-66a5e2a

Git commit: `66a5e2a`

Raw JMH stdout from each machine. For the human-readable summary see the non-`_raw` companion file: [`benchmark_3.0.0-SNAPSHOT-66a5e2a.md`](benchmark_3.0.0-SNAPSHOT-66a5e2a.md).

## aarch64 Neoverse N2 4c

**Host**: 172.16.1.231
**JDK**: Zulu 25.0.2+10-LTS
**Arch**: aarch64
**Kernel**: 6.1.0-42-arm64
**Cores**: 4
**JMH args**: `-f 3 -wi 3 -i 5 -w 2 -r 2` single-threaded
**Pattern**: `Eishay.*\.(fastjson2|fastjson3|fastjson3_asm|fastjson3_reflect|wast)`
**jar md5**: `2df686ace7d78c9da05bb0f6c54edc95`

```
### Host: root@172.16.1.231
### arch: aarch64
### kernel: 6.1.0-42-arm64
### openjdk version "25.0.2" 2026-01-20 LTS
### OpenJDK Runtime Environment Zulu25.32+21-CA (build 25.0.2+10-LTS)
### OpenJDK 64-Bit Server VM Zulu25.32+21-CA (build 25.0.2+10-LTS, mixed mode, sharing)
### jar md5: 2df686ace7d78c9da05bb0f6c54edc95
### cmd: java -XX:-UseCompressedOops --add-modules jdk.incubator.vector -jar benchmark3.jar Eishay.*\.(fastjson2|fastjson3|fastjson3_asm|fastjson3_reflect|wast) -f 3 -wi 3 -i 5 -w 2 -r 2

WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson2

# Run progress: 0.00% complete, ETA 00:30:24
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1662972.715 ops/s
# Warmup Iteration   2: 1917235.529 ops/s
# Warmup Iteration   3: 1933096.457 ops/s
Iteration   1: 1924898.863 ops/s
Iteration   2: 1873474.431 ops/s
Iteration   3: 1854849.908 ops/s
Iteration   4: 1891967.861 ops/s
Iteration   5: 1892128.812 ops/s

# Run progress: 0.88% complete, ETA 00:30:56
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1643810.971 ops/s
# Warmup Iteration   2: 1824361.982 ops/s
# Warmup Iteration   3: 1828192.734 ops/s
Iteration   1: 1833825.972 ops/s
Iteration   2: 1854649.851 ops/s
Iteration   3: 1781562.351 ops/s
Iteration   4: 1864758.663 ops/s
Iteration   5: 1877085.995 ops/s

# Run progress: 1.75% complete, ETA 00:30:37
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1679726.696 ops/s
# Warmup Iteration   2: 1808458.458 ops/s
# Warmup Iteration   3: 1843773.227 ops/s
Iteration   1: 1818721.498 ops/s
Iteration   2: 1867834.035 ops/s
Iteration   3: 1798096.503 ops/s
Iteration   4: 1862197.020 ops/s
Iteration   5: 1872325.799 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson2":
  1857891.837 ±(99.9%) 39736.766 ops/s [Average]
  (min, avg, max) = (1781562.351, 1857891.837, 1924898.863), stdev = 37169.796
  CI (99.9%): [1818155.071, 1897628.604] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3

# Run progress: 2.63% complete, ETA 00:30:20
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1219562.381 ops/s
# Warmup Iteration   2: 1251392.475 ops/s
# Warmup Iteration   3: 1258027.224 ops/s
Iteration   1: 1273785.898 ops/s
Iteration   2: 1258524.421 ops/s
Iteration   3: 1279431.733 ops/s
Iteration   4: 1279490.003 ops/s
Iteration   5: 1268829.046 ops/s

# Run progress: 3.51% complete, ETA 00:30:04
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1213865.064 ops/s
# Warmup Iteration   2: 1240936.244 ops/s
# Warmup Iteration   3: 1258812.768 ops/s
Iteration   1: 1251439.919 ops/s
Iteration   2: 1238870.524 ops/s
Iteration   3: 1257598.812 ops/s
Iteration   4: 1256643.561 ops/s
Iteration   5: 1235839.321 ops/s

# Run progress: 4.39% complete, ETA 00:29:47
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1216751.627 ops/s
# Warmup Iteration   2: 1300132.884 ops/s
# Warmup Iteration   3: 1274759.984 ops/s
Iteration   1: 1270560.575 ops/s
Iteration   2: 1252810.304 ops/s
Iteration   3: 1270222.136 ops/s
Iteration   4: 1299676.404 ops/s
Iteration   5: 1264337.134 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3":
  1263870.653 ±(99.9%) 17576.077 ops/s [Average]
  (min, avg, max) = (1235839.321, 1263870.653, 1299676.404), stdev = 16440.673
  CI (99.9%): [1246294.576, 1281446.730] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_asm

# Run progress: 5.26% complete, ETA 00:29:31
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1772262.389 ops/s
# Warmup Iteration   2: 1890963.982 ops/s
# Warmup Iteration   3: 1920827.902 ops/s
Iteration   1: 1905893.232 ops/s
Iteration   2: 1889746.258 ops/s
Iteration   3: 1856568.237 ops/s
Iteration   4: 1887889.327 ops/s
Iteration   5: 1847302.214 ops/s

# Run progress: 6.14% complete, ETA 00:29:14
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1774690.897 ops/s
# Warmup Iteration   2: 1848726.434 ops/s
# Warmup Iteration   3: 1888201.424 ops/s
Iteration   1: 1840500.167 ops/s
Iteration   2: 1847282.928 ops/s
Iteration   3: 1837631.621 ops/s
Iteration   4: 1812037.213 ops/s
Iteration   5: 1857306.951 ops/s

# Run progress: 7.02% complete, ETA 00:28:57
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1720480.619 ops/s
# Warmup Iteration   2: 1857420.232 ops/s
# Warmup Iteration   3: 1839029.413 ops/s
Iteration   1: 1851214.460 ops/s
Iteration   2: 1877209.905 ops/s
Iteration   3: 1803245.692 ops/s
Iteration   4: 1892264.763 ops/s
Iteration   5: 1888653.921 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_asm":
  1859649.793 ±(99.9%) 32257.714 ops/s [Average]
  (min, avg, max) = (1803245.692, 1859649.793, 1905893.232), stdev = 30173.886
  CI (99.9%): [1827392.078, 1891907.507] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_preconv_utf8

# Run progress: 7.89% complete, ETA 00:28:41
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1270160.208 ops/s
# Warmup Iteration   2: 1286617.185 ops/s
# Warmup Iteration   3: 1258915.287 ops/s
Iteration   1: 1297871.614 ops/s
Iteration   2: 1303534.091 ops/s
Iteration   3: 1282190.793 ops/s
Iteration   4: 1279332.037 ops/s
Iteration   5: 1276665.510 ops/s

# Run progress: 8.77% complete, ETA 00:28:24
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1282576.175 ops/s
# Warmup Iteration   2: 1330027.074 ops/s
# Warmup Iteration   3: 1262919.415 ops/s
Iteration   1: 1328587.713 ops/s
Iteration   2: 1330250.133 ops/s
Iteration   3: 1314532.028 ops/s
Iteration   4: 1324453.440 ops/s
Iteration   5: 1322652.059 ops/s

# Run progress: 9.65% complete, ETA 00:28:08
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1233057.877 ops/s
# Warmup Iteration   2: 1325042.792 ops/s
# Warmup Iteration   3: 1320329.513 ops/s
Iteration   1: 1271467.917 ops/s
Iteration   2: 1295690.063 ops/s
Iteration   3: 1244851.341 ops/s
Iteration   4: 1282528.328 ops/s
Iteration   5: 1262754.091 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_preconv_utf8":
  1294490.744 ±(99.9%) 27806.902 ops/s [Average]
  (min, avg, max) = (1244851.341, 1294490.744, 1330250.133), stdev = 26010.593
  CI (99.9%): [1266683.842, 1322297.646] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser

# Run progress: 10.53% complete, ETA 00:27:52
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1096017.366 ops/s
# Warmup Iteration   2: 1164706.807 ops/s
# Warmup Iteration   3: 1143908.170 ops/s
Iteration   1: 1160780.029 ops/s
Iteration   2: 1160347.603 ops/s
Iteration   3: 1165579.011 ops/s
Iteration   4: 1150808.078 ops/s
Iteration   5: 1154393.203 ops/s

# Run progress: 11.40% complete, ETA 00:27:35
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1115428.666 ops/s
# Warmup Iteration   2: 1153025.417 ops/s
# Warmup Iteration   3: 1124294.662 ops/s
Iteration   1: 1150691.151 ops/s
Iteration   2: 1096060.726 ops/s
Iteration   3: 1154548.804 ops/s
Iteration   4: 1145292.224 ops/s
Iteration   5: 1168648.770 ops/s

# Run progress: 12.28% complete, ETA 00:27:19
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1109932.104 ops/s
# Warmup Iteration   2: 1157711.883 ops/s
# Warmup Iteration   3: 1092203.630 ops/s
Iteration   1: 1133847.913 ops/s
Iteration   2: 1131186.323 ops/s
Iteration   3: 1167274.521 ops/s
Iteration   4: 1163232.135 ops/s
Iteration   5: 1137617.365 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser":
  1149353.857 ±(99.9%) 20247.555 ops/s [Average]
  (min, avg, max) = (1096060.726, 1149353.857, 1168648.770), stdev = 18939.575
  CI (99.9%): [1129106.302, 1169601.412] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.wast

# Run progress: 13.16% complete, ETA 00:27:02
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1568207.148 ops/s
# Warmup Iteration   2: 1671544.062 ops/s
# Warmup Iteration   3: 1662806.259 ops/s
Iteration   1: 1733844.725 ops/s
Iteration   2: 1696246.164 ops/s
Iteration   3: 1722849.450 ops/s
Iteration   4: 1700902.854 ops/s
Iteration   5: 1716139.867 ops/s

# Run progress: 14.04% complete, ETA 00:26:46
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1587246.266 ops/s
# Warmup Iteration   2: 1735385.752 ops/s
# Warmup Iteration   3: 1707945.830 ops/s
Iteration   1: 1716621.184 ops/s
Iteration   2: 1725141.638 ops/s
Iteration   3: 1719020.451 ops/s
Iteration   4: 1722587.361 ops/s
Iteration   5: 1727656.951 ops/s

# Run progress: 14.91% complete, ETA 00:26:29
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1552383.333 ops/s
# Warmup Iteration   2: 1717310.680 ops/s
# Warmup Iteration   3: 1723415.348 ops/s
Iteration   1: 1696910.319 ops/s
Iteration   2: 1728536.902 ops/s
Iteration   3: 1746334.981 ops/s
Iteration   4: 1742885.851 ops/s
Iteration   5: 1719482.166 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.wast":
  1721010.724 ±(99.9%) 15794.904 ops/s [Average]
  (min, avg, max) = (1696246.164, 1721010.724, 1746334.981), stdev = 14774.563
  CI (99.9%): [1705215.820, 1736805.629] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson2

# Run progress: 15.79% complete, ETA 00:26:13
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1095698.908 ops/s
# Warmup Iteration   2: 1256056.442 ops/s
# Warmup Iteration   3: 1276157.482 ops/s
Iteration   1: 1265656.893 ops/s
Iteration   2: 1266637.005 ops/s
Iteration   3: 1268054.951 ops/s
Iteration   4: 1270171.305 ops/s
Iteration   5: 1269749.029 ops/s

# Run progress: 16.67% complete, ETA 00:25:56
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1120829.604 ops/s
# Warmup Iteration   2: 1267403.709 ops/s
# Warmup Iteration   3: 1277197.559 ops/s
Iteration   1: 1278278.930 ops/s
Iteration   2: 1281682.607 ops/s
Iteration   3: 1284433.573 ops/s
Iteration   4: 1278271.540 ops/s
Iteration   5: 1273242.210 ops/s

# Run progress: 17.54% complete, ETA 00:25:40
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1115102.799 ops/s
# Warmup Iteration   2: 1274584.528 ops/s
# Warmup Iteration   3: 1263524.904 ops/s
Iteration   1: 1262329.699 ops/s
Iteration   2: 1283629.553 ops/s
Iteration   3: 1294100.909 ops/s
Iteration   4: 1270764.598 ops/s
Iteration   5: 1266422.030 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson2":
  1274228.322 ±(99.9%) 9463.228 ops/s [Average]
  (min, avg, max) = (1262329.699, 1274228.322, 1294100.909), stdev = 8851.909
  CI (99.9%): [1264765.095, 1283691.550] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson3

# Run progress: 18.42% complete, ETA 00:25:23
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1023918.264 ops/s
# Warmup Iteration   2: 1055013.252 ops/s
# Warmup Iteration   3: 1021679.857 ops/s
Iteration   1: 1030974.810 ops/s
Iteration   2: 1057272.922 ops/s
Iteration   3: 1072302.402 ops/s
Iteration   4: 1011415.787 ops/s
Iteration   5: 1036363.397 ops/s

# Run progress: 19.30% complete, ETA 00:25:06
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1013618.575 ops/s
# Warmup Iteration   2: 1079790.610 ops/s
# Warmup Iteration   3: 1065714.552 ops/s
Iteration   1: 1082024.323 ops/s
Iteration   2: 1081001.680 ops/s
Iteration   3: 1055409.351 ops/s
Iteration   4: 1075449.234 ops/s
Iteration   5: 1066721.060 ops/s

# Run progress: 20.18% complete, ETA 00:24:50
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 965788.991 ops/s
# Warmup Iteration   2: 1075182.668 ops/s
# Warmup Iteration   3: 1069617.588 ops/s
Iteration   1: 1069082.201 ops/s
Iteration   2: 1072223.862 ops/s
Iteration   3: 1068924.602 ops/s
Iteration   4: 1072958.807 ops/s
Iteration   5: 1022647.931 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson3":
  1058318.158 ±(99.9%) 23877.016 ops/s [Average]
  (min, avg, max) = (1011415.787, 1058318.158, 1082024.323), stdev = 22334.576
  CI (99.9%): [1034441.142, 1082195.174] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.wast

# Run progress: 21.05% complete, ETA 00:24:33
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1050291.964 ops/s
# Warmup Iteration   2: 1140161.495 ops/s
# Warmup Iteration   3: 1141468.433 ops/s
Iteration   1: 1145318.797 ops/s
Iteration   2: 1157612.890 ops/s
Iteration   3: 1141261.924 ops/s
Iteration   4: 1150303.437 ops/s
Iteration   5: 1138797.273 ops/s

# Run progress: 21.93% complete, ETA 00:24:17
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1058805.355 ops/s
# Warmup Iteration   2: 1151311.386 ops/s
# Warmup Iteration   3: 1148197.900 ops/s
Iteration   1: 1167182.795 ops/s
Iteration   2: 1170672.499 ops/s
Iteration   3: 1142955.694 ops/s
Iteration   4: 1163021.937 ops/s
Iteration   5: 1155275.057 ops/s

# Run progress: 22.81% complete, ETA 00:24:00
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1049587.500 ops/s
# Warmup Iteration   2: 1145496.617 ops/s
# Warmup Iteration   3: 1141279.089 ops/s
Iteration   1: 1147626.655 ops/s
Iteration   2: 1167254.968 ops/s
Iteration   3: 1142544.094 ops/s
Iteration   4: 1135208.145 ops/s
Iteration   5: 1171073.408 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.wast":
  1153073.972 ±(99.9%) 13191.230 ops/s [Average]
  (min, avg, max) = (1135208.145, 1153073.972, 1171073.408), stdev = 12339.085
  CI (99.9%): [1139882.742, 1166265.201] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson2

# Run progress: 23.68% complete, ETA 00:23:44
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 919728.907 ops/s
# Warmup Iteration   2: 984146.372 ops/s
# Warmup Iteration   3: 999537.126 ops/s
Iteration   1: 1004473.230 ops/s
Iteration   2: 1009502.905 ops/s
Iteration   3: 1013934.692 ops/s
Iteration   4: 979822.947 ops/s
Iteration   5: 990033.125 ops/s

# Run progress: 24.56% complete, ETA 00:23:27
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 935483.228 ops/s
# Warmup Iteration   2: 972898.358 ops/s
# Warmup Iteration   3: 946102.713 ops/s
Iteration   1: 953286.068 ops/s
Iteration   2: 987880.474 ops/s
Iteration   3: 981306.570 ops/s
Iteration   4: 998442.928 ops/s
Iteration   5: 996746.520 ops/s

# Run progress: 25.44% complete, ETA 00:23:11
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 887020.557 ops/s
# Warmup Iteration   2: 987016.458 ops/s
# Warmup Iteration   3: 995023.503 ops/s
Iteration   1: 983224.227 ops/s
Iteration   2: 965111.398 ops/s
Iteration   3: 977293.717 ops/s
Iteration   4: 992163.834 ops/s
Iteration   5: 987025.727 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson2":
  988016.557 ±(99.9%) 17124.959 ops/s [Average]
  (min, avg, max) = (953286.068, 988016.557, 1013934.692), stdev = 16018.697
  CI (99.9%): [970891.599, 1005141.516] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson3

# Run progress: 26.32% complete, ETA 00:22:54
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 879459.333 ops/s
# Warmup Iteration   2: 916406.036 ops/s
# Warmup Iteration   3: 916897.244 ops/s
Iteration   1: 919156.764 ops/s
Iteration   2: 919272.581 ops/s
Iteration   3: 930810.703 ops/s
Iteration   4: 930840.180 ops/s
Iteration   5: 926938.984 ops/s

# Run progress: 27.19% complete, ETA 00:22:38
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 879773.921 ops/s
# Warmup Iteration   2: 915669.518 ops/s
# Warmup Iteration   3: 922439.006 ops/s
Iteration   1: 921825.525 ops/s
Iteration   2: 912343.358 ops/s
Iteration   3: 916192.757 ops/s
Iteration   4: 919395.694 ops/s
Iteration   5: 919215.849 ops/s

# Run progress: 28.07% complete, ETA 00:22:21
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 868671.597 ops/s
# Warmup Iteration   2: 919159.637 ops/s
# Warmup Iteration   3: 922390.824 ops/s
Iteration   1: 922390.537 ops/s
Iteration   2: 924578.996 ops/s
Iteration   3: 922379.020 ops/s
Iteration   4: 926269.968 ops/s
Iteration   5: 923786.057 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson3":
  922359.798 ±(99.9%) 5448.622 ops/s [Average]
  (min, avg, max) = (912343.358, 922359.798, 930840.180), stdev = 5096.644
  CI (99.9%): [916911.176, 927808.420] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.wast

# Run progress: 28.95% complete, ETA 00:22:05
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
946247.113 ops/s
# Warmup Iteration   2: 985451.774 ops/s
# Warmup Iteration   3: 1028200.261 ops/s
Iteration   1: 1032686.274 ops/s
Iteration   2: 974651.377 ops/s
Iteration   3: 1003991.028 ops/s
Iteration   4: 1000268.474 ops/s
Iteration   5: 994880.748 ops/s

# Run progress: 29.82% complete, ETA 00:21:49
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1045508.429 ops/s
# Warmup Iteration   2: 1139120.067 ops/s
# Warmup Iteration   3: 1109454.428 ops/s
Iteration   1: 1140471.855 ops/s
Iteration   2: 1135525.183 ops/s
Iteration   3: 1139103.913 ops/s
Iteration   4: 1111035.380 ops/s
Iteration   5: 1099568.781 ops/s

# Run progress: 30.70% complete, ETA 00:21:32
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
897589.601 ops/s
# Warmup Iteration   2: 967110.292 ops/s
# Warmup Iteration   3: 983126.816 ops/s
Iteration   1: 979059.205 ops/s
Iteration   2: 982768.287 ops/s
Iteration   3: 966054.861 ops/s
Iteration   4: 961939.503 ops/s
Iteration   5: 965414.662 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.wast":
  1032494.635 ±(99.9%) 75669.136 ops/s [Average]
  (min, avg, max) = (961939.503, 1032494.635, 1140471.855), stdev = 70780.957
  CI (99.9%): [956825.499, 1108163.772] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson2

# Run progress: 31.58% complete, ETA 00:21:16
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 900917.581 ops/s
# Warmup Iteration   2: 972480.462 ops/s
# Warmup Iteration   3: 988038.078 ops/s
Iteration   1: 1014116.575 ops/s
Iteration   2: 984566.058 ops/s
Iteration   3: 1005413.873 ops/s
Iteration   4: 991892.575 ops/s
Iteration   5: 996524.237 ops/s

# Run progress: 32.46% complete, ETA 00:20:59
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 899852.540 ops/s
# Warmup Iteration   2: 1017046.729 ops/s
# Warmup Iteration   3: 986250.833 ops/s
Iteration   1: 963978.247 ops/s
Iteration   2: 981221.905 ops/s
Iteration   3: 945833.048 ops/s
Iteration   4: 951493.920 ops/s
Iteration   5: 1007140.204 ops/s

# Run progress: 33.33% complete, ETA 00:20:43
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 894783.778 ops/s
# Warmup Iteration   2: 957330.537 ops/s
# Warmup Iteration   3: 961030.679 ops/s
Iteration   1: 984816.052 ops/s
Iteration   2: 991125.588 ops/s
Iteration   3: 995558.284 ops/s
Iteration   4: 978644.057 ops/s
Iteration   5: 974900.475 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson2":
  984481.673 ±(99.9%) 20870.149 ops/s [Average]
  (min, avg, max) = (945833.048, 984481.673, 1014116.575), stdev = 19521.951
  CI (99.9%): [963611.524, 1005351.823] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson3

# Run progress: 34.21% complete, ETA 00:20:26
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 874127.499 ops/s
# Warmup Iteration   2: 916229.332 ops/s
# Warmup Iteration   3: 926730.600 ops/s
Iteration   1: 920022.916 ops/s
Iteration   2: 929480.776 ops/s
Iteration   3: 914845.948 ops/s
Iteration   4: 921424.407 ops/s
Iteration   5: 920448.098 ops/s

# Run progress: 35.09% complete, ETA 00:20:10
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 880679.933 ops/s
# Warmup Iteration   2: 927728.328 ops/s
# Warmup Iteration   3: 925573.248 ops/s
Iteration   1: 929865.210 ops/s
Iteration   2: 928441.328 ops/s
Iteration   3: 927844.312 ops/s
Iteration   4: 935489.556 ops/s
Iteration   5: 935077.263 ops/s

# Run progress: 35.96% complete, ETA 00:19:54
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 881302.047 ops/s
# Warmup Iteration   2: 933967.207 ops/s
# Warmup Iteration   3: 935055.644 ops/s
Iteration   1: 936980.309 ops/s
Iteration   2: 933875.036 ops/s
Iteration   3: 927899.220 ops/s
Iteration   4: 936218.455 ops/s
Iteration   5: 934402.767 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson3":
  928821.040 ±(99.9%) 7354.703 ops/s [Average]
  (min, avg, max) = (914845.948, 928821.040, 936980.309), stdev = 6879.594
  CI (99.9%): [921466.337, 936175.743] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.wast

# Run progress: 36.84% complete, ETA 00:19:37
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
909213.070 ops/s
# Warmup Iteration   2: 952852.575 ops/s
# Warmup Iteration   3: 968680.856 ops/s
Iteration   1: 978436.554 ops/s
Iteration   2: 970047.423 ops/s
Iteration   3: 941633.487 ops/s
Iteration   4: 970881.410 ops/s
Iteration   5: 962487.819 ops/s

# Run progress: 37.72% complete, ETA 00:19:21
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
903071.242 ops/s
# Warmup Iteration   2: 956892.897 ops/s
# Warmup Iteration   3: 951892.816 ops/s
Iteration   1: 967789.421 ops/s
Iteration   2: 982311.761 ops/s
Iteration   3: 976732.465 ops/s
Iteration   4: 964314.224 ops/s
Iteration   5: 955436.051 ops/s

# Run progress: 38.60% complete, ETA 00:19:04
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1052602.977 ops/s
# Warmup Iteration   2: 1105021.498 ops/s
# Warmup Iteration   3: 1115789.219 ops/s
Iteration   1: 1114605.145 ops/s
Iteration   2: 1104719.109 ops/s
Iteration   3: 1101788.829 ops/s
Iteration   4: 1112920.253 ops/s
Iteration   5: 1128795.032 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.wast":
  1015526.599 ±(99.9%) 76857.448 ops/s [Average]
  (min, avg, max) = (941633.487, 1015526.599, 1128795.032), stdev = 71892.504
  CI (99.9%): [938669.151, 1092384.046] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson2

# Run progress: 39.47% complete, ETA 00:18:48
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1655020.212 ops/s
# Warmup Iteration   2: 1824056.012 ops/s
# Warmup Iteration   3: 1841130.083 ops/s
Iteration   1: 1850108.891 ops/s
Iteration   2: 1841448.480 ops/s
Iteration   3: 1855390.235 ops/s
Iteration   4: 1841802.464 ops/s
Iteration   5: 1847111.876 ops/s

# Run progress: 40.35% complete, ETA 00:18:32
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1576298.696 ops/s
# Warmup Iteration   2: 1765120.866 ops/s
# Warmup Iteration   3: 1775008.791 ops/s
Iteration   1: 1812374.414 ops/s
Iteration   2: 1820101.823 ops/s
Iteration   3: 1758155.560 ops/s
Iteration   4: 1766649.852 ops/s
Iteration   5: 1820746.685 ops/s

# Run progress: 41.23% complete, ETA 00:18:15
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1590847.468 ops/s
# Warmup Iteration   2: 1798043.425 ops/s
# Warmup Iteration   3: 1768570.706 ops/s
Iteration   1: 1732204.861 ops/s
Iteration   2: 1783780.323 ops/s
Iteration   3: 1753577.866 ops/s
Iteration   4: 1782317.951 ops/s
Iteration   5: 1756568.369 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson2":
  1801489.310 ±(99.9%) 44534.262 ops/s [Average]
  (min, avg, max) = (1732204.861, 1801489.310, 1855390.235), stdev = 41657.377
  CI (99.9%): [1756955.048, 1846023.572] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3

# Run progress: 42.11% complete, ETA 00:17:59
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1257843.789 ops/s
# Warmup Iteration   2: 1304599.752 ops/s
# Warmup Iteration   3: 1304557.907 ops/s
Iteration   1: 1261490.511 ops/s
Iteration   2: 1301364.250 ops/s
Iteration   3: 1298611.265 ops/s
Iteration   4: 1280017.524 ops/s
Iteration   5: 1261319.737 ops/s

# Run progress: 42.98% complete, ETA 00:17:43
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1260714.974 ops/s
# Warmup Iteration   2: 1313279.693 ops/s
# Warmup Iteration   3: 1306654.673 ops/s
Iteration   1: 1321980.841 ops/s
Iteration   2: 1292355.170 ops/s
Iteration   3: 1264915.209 ops/s
Iteration   4: 1305873.614 ops/s
Iteration   5: 1316044.968 ops/s

# Run progress: 43.86% complete, ETA 00:17:26
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1261778.421 ops/s
# Warmup Iteration   2: 1311129.876 ops/s
# Warmup Iteration   3: 1299216.956 ops/s
Iteration   1: 1266012.860 ops/s
Iteration   2: 1304171.650 ops/s
Iteration   3: 1312058.191 ops/s
Iteration   4: 1277596.469 ops/s
Iteration   5: 1303728.834 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3":
  1291169.406 ±(99.9%) 22335.029 ops/s [Average]
  (min, avg, max) = (1261319.737, 1291169.406, 1321980.841), stdev = 20892.200
  CI (99.9%): [1268834.377, 1313504.435] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_asm

# Run progress: 44.74% complete, ETA 00:17:10
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2012529.347 ops/s
# Warmup Iteration   2: 2101469.242 ops/s
# Warmup Iteration   3: 2077732.872 ops/s
Iteration   1: 2059981.552 ops/s
Iteration   2: 2078132.239 ops/s
Iteration   3: 2101710.063 ops/s
Iteration   4: 2058473.567 ops/s
Iteration   5: 2073916.539 ops/s

# Run progress: 45.61% complete, ETA 00:16:54
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2050843.887 ops/s
# Warmup Iteration   2: 2098142.370 ops/s
# Warmup Iteration   3: 2096529.441 ops/s
Iteration   1: 2085799.253 ops/s
Iteration   2: 2068333.219 ops/s
Iteration   3: 2099321.632 ops/s
Iteration   4: 2114668.664 ops/s
Iteration   5: 2055132.715 ops/s

# Run progress: 46.49% complete, ETA 00:16:37
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2017469.007 ops/s
# Warmup Iteration   2: 2060374.387 ops/s
# Warmup Iteration   3: 1974483.269 ops/s
Iteration   1: 2066654.402 ops/s
Iteration   2: 2073781.680 ops/s
Iteration   3: 2042155.708 ops/s
Iteration   4: 2075068.650 ops/s
Iteration   5: 2091355.031 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_asm":
  2076298.994 ±(99.9%) 20871.289 ops/s [Average]
  (min, avg, max) = (2042155.708, 2076298.994, 2114668.664), stdev = 19523.016
  CI (99.9%): [2055427.706, 2097170.283] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_reflect

# Run progress: 47.37% complete, ETA 00:16:21
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1284047.599 ops/s
# Warmup Iteration   2: 1302584.305 ops/s
# Warmup Iteration   3: 1282579.121 ops/s
Iteration   1: 1305008.488 ops/s
Iteration   2: 1330537.402 ops/s
Iteration   3: 1350339.631 ops/s
Iteration   4: 1324152.187 ops/s
Iteration   5: 1320181.382 ops/s

# Run progress: 48.25% complete, ETA 00:16:05
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1299543.982 ops/s
# Warmup Iteration   2: 1339249.352 ops/s
# Warmup Iteration   3: 1307282.022 ops/s
Iteration   1: 1268096.799 ops/s
Iteration   2: 1319233.534 ops/s
Iteration   3: 1282842.764 ops/s
Iteration   4: 1319942.571 ops/s
Iteration   5: 1332660.315 ops/s

# Run progress: 49.12% complete, ETA 00:15:48
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1290392.638 ops/s
# Warmup Iteration   2: 1311887.584 ops/s
# Warmup Iteration   3: 1354957.625 ops/s
Iteration   1: 1332855.871 ops/s
Iteration   2: 1300627.598 ops/s
Iteration   3: 1337504.057 ops/s
Iteration   4: 1270791.323 ops/s
Iteration   5: 1345427.841 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_reflect":
  1316013.451 ±(99.9%) 27383.790 ops/s [Average]
  (min, avg, max) = (1268096.799, 1316013.451, 1350339.631), stdev = 25614.814
  CI (99.9%): [1288629.661, 1343397.240] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.wast

# Run progress: 50.00% complete, ETA 00:15:32
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1557906.192 ops/s
# Warmup Iteration   2: 1680618.735 ops/s
# Warmup Iteration   3: 1678468.141 ops/s
Iteration   1: 1683909.916 ops/s
Iteration   2: 1689392.453 ops/s
Iteration   3: 1709200.642 ops/s
Iteration   4: 1707778.254 ops/s
Iteration   5: 1644141.000 ops/s

# Run progress: 50.88% complete, ETA 00:15:16
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1522351.768 ops/s
# Warmup Iteration   2: 1626386.013 ops/s
# Warmup Iteration   3: 1657676.805 ops/s
Iteration   1: 1677634.417 ops/s
Iteration   2: 1678016.454 ops/s
Iteration   3: 1691599.428 ops/s
Iteration   4: 1654365.941 ops/s
Iteration   5: 1640533.611 ops/s

# Run progress: 51.75% complete, ETA 00:14:59
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1584179.571 ops/s
# Warmup Iteration   2: 1655281.317 ops/s
# Warmup Iteration   3: 1677533.284 ops/s
Iteration   1: 1703328.508 ops/s
Iteration   2: 1712367.736 ops/s
Iteration   3: 1693370.146 ops/s
Iteration   4: 1673153.965 ops/s
Iteration   5: 1640169.104 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.wast":
  1679930.772 ±(99.9%) 26766.816 ops/s [Average]
  (min, avg, max) = (1640169.104, 1679930.772, 1712367.736), stdev = 25037.696
  CI (99.9%): [1653163.955, 1706697.588] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson2

# Run progress: 52.63% complete, ETA 00:14:43
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1107187.337 ops/s
# Warmup Iteration   2: 1237554.820 ops/s
# Warmup Iteration   3: 1248776.754 ops/s
Iteration   1: 1249097.948 ops/s
Iteration   2: 1247856.757 ops/s
Iteration   3: 1245455.287 ops/s
Iteration   4: 1239178.989 ops/s
Iteration   5: 1246175.153 ops/s

# Run progress: 53.51% complete, ETA 00:14:27
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1113246.933 ops/s
# Warmup Iteration   2: 1249970.574 ops/s
# Warmup Iteration   3: 1252350.632 ops/s
Iteration   1: 1256229.005 ops/s
Iteration   2: 1257474.406 ops/s
Iteration   3: 1247794.490 ops/s
Iteration   4: 1259033.081 ops/s
Iteration   5: 1256135.649 ops/s

# Run progress: 54.39% complete, ETA 00:14:10
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1121949.652 ops/s
# Warmup Iteration   2: 1247452.602 ops/s
# Warmup Iteration   3: 1243604.766 ops/s
Iteration   1: 1259903.918 ops/s
Iteration   2: 1262544.312 ops/s
Iteration   3: 1251373.329 ops/s
Iteration   4: 1248873.946 ops/s
Iteration   5: 1260985.677 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson2":
  1252540.797 ±(99.9%) 7334.741 ops/s [Average]
  (min, avg, max) = (1239178.989, 1252540.797, 1262544.312), stdev = 6860.921
  CI (99.9%): [1245206.055, 1259875.538] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson3

# Run progress: 55.26% complete, ETA 00:13:54
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1044863.400 ops/s
# Warmup Iteration   2: 1091328.485 ops/s
# Warmup Iteration   3: 1082002.106 ops/s
Iteration   1: 1094855.012 ops/s
Iteration   2: 1085512.313 ops/s
Iteration   3: 1097556.724 ops/s
Iteration   4: 1097420.740 ops/s
Iteration   5: 1088558.590 ops/s

# Run progress: 56.14% complete, ETA 00:13:38
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1034534.342 ops/s
# Warmup Iteration   2: 1110175.116 ops/s
# Warmup Iteration   3: 1083994.498 ops/s
Iteration   1: 1111128.963 ops/s
Iteration   2: 1118926.200 ops/s
Iteration   3: 1120807.500 ops/s
Iteration   4: 1107156.838 ops/s
Iteration   5: 1120760.954 ops/s

# Run progress: 57.02% complete, ETA 00:13:21
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1024770.498 ops/s
# Warmup Iteration   2: 1073234.361 ops/s
# Warmup Iteration   3: 1085787.097 ops/s
Iteration   1: 1077873.587 ops/s
Iteration   2: 1096182.506 ops/s
Iteration   3: 1088924.511 ops/s
Iteration   4: 1096540.432 ops/s
Iteration   5: 1093129.932 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson3":
  1099688.987 ±(99.9%) 14175.985 ops/s [Average]
  (min, avg, max) = (1077873.587, 1099688.987, 1120807.500), stdev = 13260.225
  CI (99.9%): [1085513.002, 1113864.972] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.wast

# Run progress: 57.89% complete, ETA 00:13:05
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1011548.472 ops/s
# Warmup Iteration   2: 1097584.855 ops/s
# Warmup Iteration   3: 1097200.151 ops/s
Iteration   1: 1105501.500 ops/s
Iteration   2: 1093516.263 ops/s
Iteration   3: 1100178.391 ops/s
Iteration   4: 1104653.262 ops/s
Iteration   5: 1107643.529 ops/s

# Run progress: 58.77% complete, ETA 00:12:48
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1037884.435 ops/s
# Warmup Iteration   2: 1124688.551 ops/s
# Warmup Iteration   3: 1104789.561 ops/s
Iteration   1: 1119879.993 ops/s
Iteration   2: 1117628.228 ops/s
Iteration   3: 1127673.116 ops/s
Iteration   4: 1129776.309 ops/s
Iteration   5: 1127690.062 ops/s

# Run progress: 59.65% complete, ETA 00:12:32
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1033716.024 ops/s
# Warmup Iteration   2: 1121948.242 ops/s
# Warmup Iteration   3: 1119091.081 ops/s
Iteration   1: 1133781.493 ops/s
Iteration   2: 1108214.599 ops/s
Iteration   3: 1101532.384 ops/s
Iteration   4: 1124555.332 ops/s
Iteration   5: 1130125.989 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.wast":
  1115490.030 ±(99.9%) 13997.269 ops/s [Average]
  (min, avg, max) = (1093516.263, 1115490.030, 1133781.493), stdev = 13093.054
  CI (99.9%): [1101492.761, 1129487.299] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson2_bytes

# Run progress: 60.53% complete, ETA 00:12:16
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2096031.132 ops/s
# Warmup Iteration   2: 2255603.121 ops/s
# Warmup Iteration   3: 2269687.907 ops/s
Iteration   1: 2268573.647 ops/s
Iteration   2: 2270391.308 ops/s
Iteration   3: 2271948.777 ops/s
Iteration   4: 2278556.940 ops/s
Iteration   5: 2272514.593 ops/s

# Run progress: 61.40% complete, ETA 00:11:59
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2075332.071 ops/s
# Warmup Iteration   2: 2203653.057 ops/s
# Warmup Iteration   3: 2246437.104 ops/s
Iteration   1: 2249900.783 ops/s
Iteration   2: 2247295.528 ops/s
Iteration   3: 2245752.627 ops/s
Iteration   4: 2250284.881 ops/s
Iteration   5: 2254805.270 ops/s

# Run progress: 62.28% complete, ETA 00:11:43
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2089225.354 ops/s
# Warmup Iteration   2: 2258362.051 ops/s
# Warmup Iteration   3: 2261800.928 ops/s
Iteration   1: 2265012.429 ops/s
Iteration   2: 2266233.127 ops/s
Iteration   3: 2272039.779 ops/s
Iteration   4: 2260190.431 ops/s
Iteration   5: 2260421.243 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson2_bytes":
  2262261.424 ±(99.9%) 11232.134 ops/s [Average]
  (min, avg, max) = (2245752.627, 2262261.424, 2278556.940), stdev = 10506.545
  CI (99.9%): [2251029.290, 2273493.558] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson2_string

# Run progress: 63.16% complete, ETA 00:11:26
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2142648.047 ops/s
# Warmup Iteration   2: 2270170.320 ops/s
# Warmup Iteration   3: 2321305.615 ops/s
Iteration   1: 2323649.458 ops/s
Iteration   2: 2323238.685 ops/s
Iteration   3: 2314029.280 ops/s
Iteration   4: 2323671.150 ops/s
Iteration   5: 2317596.958 ops/s

# Run progress: 64.04% complete, ETA 00:11:10
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2144125.553 ops/s
# Warmup Iteration   2: 2273256.229 ops/s
# Warmup Iteration   3: 2336537.155 ops/s
Iteration   1: 2330328.383 ops/s
Iteration   2: 2341062.431 ops/s
Iteration   3: 2337325.684 ops/s
Iteration   4: 2337079.012 ops/s
Iteration   5: 2337093.584 ops/s

# Run progress: 64.91% complete, ETA 00:10:53
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2160854.025 ops/s
# Warmup Iteration   2: 2309570.569 ops/s
# Warmup Iteration   3: 2324429.685 ops/s
Iteration   1: 2321650.155 ops/s
Iteration   2: 2323653.214 ops/s
Iteration   3: 2320016.425 ops/s
Iteration   4: 2319344.114 ops/s
Iteration   5: 2319522.078 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson2_string":
  2325950.707 ±(99.9%) 9029.053 ops/s [Average]
  (min, avg, max) = (2314029.280, 2325950.707, 2341062.431), stdev = 8445.782
  CI (99.9%): [2316921.654, 2334979.761] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson3_bytes

# Run progress: 65.79% complete, ETA 00:10:37
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1962803.270 ops/s
# Warmup Iteration   2: 1989753.062 ops/s
# Warmup Iteration   3: 1974570.904 ops/s
Iteration   1: 1972528.712 ops/s
Iteration   2: 1983050.326 ops/s
Iteration   3: 1984977.570 ops/s
Iteration   4: 1985089.079 ops/s
Iteration   5: 1986350.573 ops/s

# Run progress: 66.67% complete, ETA 00:10:21
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1959259.895 ops/s
# Warmup Iteration   2: 1987765.183 ops/s
# Warmup Iteration   3: 1988994.799 ops/s
Iteration   1: 1992849.539 ops/s
Iteration   2: 1991518.031 ops/s
Iteration   3: 1988671.409 ops/s
Iteration   4: 1991252.375 ops/s
Iteration   5: 1994014.776 ops/s

# Run progress: 67.54% complete, ETA 00:10:04
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1964146.352 ops/s
# Warmup Iteration   2: 1989989.079 ops/s
# Warmup Iteration   3: 1992967.228 ops/s
Iteration   1: 1991788.838 ops/s
Iteration   2: 1992624.689 ops/s
Iteration   3: 1990644.614 ops/s
Iteration   4: 1992407.297 ops/s
Iteration   5: 1994303.960 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson3_bytes":
  1988804.786 ±(99.9%) 6124.488 ops/s [Average]
  (min, avg, max) = (1972528.712, 1988804.786, 1994303.960), stdev = 5728.849
  CI (99.9%): [1982680.298, 1994929.273] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson3_string

# Run progress: 68.42% complete, ETA 00:09:48
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1598841.080 ops/s
# Warmup Iteration   2: 1645913.245 ops/s
# Warmup Iteration   3: 1642384.199 ops/s
Iteration   1: 1640270.246 ops/s
Iteration   2: 1642107.330 ops/s
Iteration   3: 1643636.371 ops/s
Iteration   4: 1641966.043 ops/s
Iteration   5: 1640011.668 ops/s

# Run progress: 69.30% complete, ETA 00:09:31
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1647423.911 ops/s
# Warmup Iteration   2: 1672739.379 ops/s
# Warmup Iteration   3: 1667652.970 ops/s
Iteration   1: 1662621.230 ops/s
Iteration   2: 1667118.098 ops/s
Iteration   3: 1669187.824 ops/s
Iteration   4: 1642353.556 ops/s
Iteration   5: 1655539.368 ops/s

# Run progress: 70.18% complete, ETA 00:09:15
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1600712.924 ops/s
# Warmup Iteration   2: 1614935.966 ops/s
# Warmup Iteration   3: 1628126.821 ops/s
Iteration   1: 1624757.725 ops/s
Iteration   2: 1632125.256 ops/s
Iteration   3: 1631378.292 ops/s
Iteration   4: 1629013.055 ops/s
Iteration   5: 1622298.505 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson3_string":
  1642958.971 ±(99.9%) 15750.967 ops/s [Average]
  (min, avg, max) = (1622298.505, 1642958.971, 1669187.824), stdev = 14733.465
  CI (99.9%): [1627208.004, 1658709.939] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson2

# Run progress: 71.05% complete, ETA 00:08:59
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1771573.179 ops/s
# Warmup Iteration   2: 1871066.531 ops/s
# Warmup Iteration   3: 1863258.017 ops/s
Iteration   1: 1869227.990 ops/s
Iteration   2: 1848852.454 ops/s
Iteration   3: 1874143.911 ops/s
Iteration   4: 1876981.453 ops/s
Iteration   5: 1885904.376 ops/s

# Run progress: 71.93% complete, ETA 00:08:42
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1770997.903 ops/s
# Warmup Iteration   2: 1889840.995 ops/s
# Warmup Iteration   3: 1904528.564 ops/s
Iteration   1: 1875400.336 ops/s
Iteration   2: 1869151.935 ops/s
Iteration   3: 1862437.306 ops/s
Iteration   4: 1857704.120 ops/s
Iteration   5: 1890427.190 ops/s

# Run progress: 72.81% complete, ETA 00:08:26
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1753165.247 ops/s
# Warmup Iteration   2: 1898568.860 ops/s
# Warmup Iteration   3: 1886092.435 ops/s
Iteration   1: 1879429.220 ops/s
Iteration   2: 1877945.958 ops/s
Iteration   3: 1918559.862 ops/s
Iteration   4: 1900392.579 ops/s
Iteration   5: 1903824.532 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson2":
  1879358.881 ±(99.9%) 19559.301 ops/s [Average]
  (min, avg, max) = (1848852.454, 1879358.881, 1918559.862), stdev = 18295.782
  CI (99.9%): [1859799.581, 1898918.182] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3

# Run progress: 73.68% complete, ETA 00:08:10
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2102858.261 ops/s
# Warmup Iteration   2: 2184439.696 ops/s
# Warmup Iteration   3: 2183267.910 ops/s
Iteration   1: 2196204.756 ops/s
Iteration   2: 2199992.948 ops/s
Iteration   3: 2176524.716 ops/s
Iteration   4: 2179233.580 ops/s
Iteration   5: 2188102.712 ops/s

# Run progress: 74.56% complete, ETA 00:07:53
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2181661.817 ops/s
# Warmup Iteration   2: 2300497.507 ops/s
# Warmup Iteration   3: 2328629.432 ops/s
Iteration   1: 2338097.122 ops/s
Iteration   2: 2322477.100 ops/s
Iteration   3: 2329700.327 ops/s
Iteration   4: 2337384.095 ops/s
Iteration   5: 2338275.800 ops/s

# Run progress: 75.44% complete, ETA 00:07:37
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2207687.714 ops/s
# Warmup Iteration   2: 2277385.474 ops/s
# Warmup Iteration   3: 2255548.599 ops/s
Iteration   1: 2273628.639 ops/s
Iteration   2: 2276035.610 ops/s
Iteration   3: 2265458.460 ops/s
Iteration   4: 2266715.220 ops/s
Iteration   5: 2268944.797 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3":
  2263785.059 ±(99.9%) 66204.245 ops/s [Average]
  (min, avg, max) = (2176524.716, 2263785.059, 2338275.800), stdev = 61927.492
  CI (99.9%): [2197580.814, 2329989.304] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_asm

# Run progress: 76.32% complete, ETA 00:07:21
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2212845.235 ops/s
# Warmup Iteration   2: 2271915.261 ops/s
# Warmup Iteration   3: 2290179.664 ops/s
Iteration   1: 2287116.731 ops/s
Iteration   2: 2297228.959 ops/s
Iteration   3: 2287819.543 ops/s
Iteration   4: 2291105.148 ops/s
Iteration   5: 2299448.286 ops/s

# Run progress: 77.19% complete, ETA 00:07:05
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2306010.560 ops/s
# Warmup Iteration   2: 2403938.255 ops/s
# Warmup Iteration   3: 2429840.976 ops/s
Iteration   1: 2446131.345 ops/s
Iteration   2: 2454259.419 ops/s
Iteration   3: 2439882.850 ops/s
Iteration   4: 2451668.189 ops/s
Iteration   5: 2430246.044 ops/s

# Run progress: 78.07% complete, ETA 00:06:48
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2278992.771 ops/s
# Warmup Iteration   2: 2352124.587 ops/s
# Warmup Iteration   3: 2347489.383 ops/s
Iteration   1: 2378217.776 ops/s
Iteration   2: 2376107.011 ops/s
Iteration   3: 2353518.233 ops/s
Iteration   4: 2350697.859 ops/s
Iteration   5: 2381173.470 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_asm":
  2368308.057 ±(99.9%) 69418.601 ops/s [Average]
  (min, avg, max) = (2287116.731, 2368308.057, 2454259.419), stdev = 64934.202
  CI (99.9%): [2298889.457, 2437726.658] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_reflect

# Run progress: 78.95% complete, ETA 00:06:32
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2099436.626 ops/s
# Warmup Iteration   2: 2166699.615 ops/s
# Warmup Iteration   3: 2182203.864 ops/s
Iteration   1: 2163827.801 ops/s
Iteration   2: 2195634.668 ops/s
Iteration   3: 2166027.855 ops/s
Iteration   4: 2164315.329 ops/s
Iteration   5: 2162914.336 ops/s

# Run progress: 79.82% complete, ETA 00:06:16
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2142678.181 ops/s
# Warmup Iteration   2: 2249000.920 ops/s
# Warmup Iteration   3: 2242229.979 ops/s
Iteration   1: 2239651.157 ops/s
Iteration   2: 2232439.698 ops/s
Iteration   3: 2256644.810 ops/s
Iteration   4: 2245929.426 ops/s
Iteration   5: 2242531.503 ops/s

# Run progress: 80.70% complete, ETA 00:05:59
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2102571.681 ops/s
# Warmup Iteration   2: 2152802.716 ops/s
# Warmup Iteration   3: 2205807.576 ops/s
Iteration   1: 2204238.220 ops/s
Iteration   2: 2176762.502 ops/s
Iteration   3: 2188751.138 ops/s
Iteration   4: 2211434.630 ops/s
Iteration   5: 2181156.807 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_reflect":
  2202150.659 ±(99.9%) 36121.499 ops/s [Average]
  (min, avg, max) = (2162914.336, 2202150.659, 2256644.810), stdev = 33788.073
  CI (99.9%): [2166029.160, 2238272.158] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_utf8latin1

# Run progress: 81.58% complete, ETA 00:05:43
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2290923.408 ops/s
# Warmup Iteration   2: 2428076.371 ops/s
# Warmup Iteration   3: 2385758.534 ops/s
Iteration   1: 2403688.722 ops/s
Iteration   2: 2382025.255 ops/s
Iteration   3: 2427462.581 ops/s
Iteration   4: 2399765.437 ops/s
Iteration   5: 2407007.407 ops/s

# Run progress: 82.46% complete, ETA 00:05:27
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2258425.031 ops/s
# Warmup Iteration   2: 2367988.776 ops/s
# Warmup Iteration   3: 2439414.993 ops/s
Iteration   1: 2414616.468 ops/s
Iteration   2: 2466974.840 ops/s
Iteration   3: 2441122.517 ops/s
Iteration   4: 2454984.476 ops/s
Iteration   5: 2461831.122 ops/s

# Run progress: 83.33% complete, ETA 00:05:10
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2192242.616 ops/s
# Warmup Iteration   2: 2260523.301 ops/s
# Warmup Iteration   3: 2289018.426 ops/s
Iteration   1: 2239980.398 ops/s
Iteration   2: 2267719.257 ops/s
Iteration   3: 2273604.818 ops/s
Iteration   4: 2266157.807 ops/s
Iteration   5: 2259020.053 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_utf8latin1":
  2371064.077 ±(99.9%) 89756.874 ops/s [Average]
  (min, avg, max) = (2239980.398, 2371064.077, 2466974.840), stdev = 83958.636
  CI (99.9%): [2281307.203, 2460820.951] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.wast

# Run progress: 84.21% complete, ETA 00:04:54
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1535706.897 ops/s
# Warmup Iteration   2: 1605175.810 ops/s
# Warmup Iteration   3: 1612235.537 ops/s
Iteration   1: 1612921.926 ops/s
Iteration   2: 1614169.835 ops/s
Iteration   3: 1608458.529 ops/s
Iteration   4: 1609146.661 ops/s
Iteration   5: 1592477.774 ops/s

# Run progress: 85.09% complete, ETA 00:04:38
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1549834.413 ops/s
# Warmup Iteration   2: 1626847.046 ops/s
# Warmup Iteration   3: 1630497.737 ops/s
Iteration   1: 1621905.851 ops/s
Iteration   2: 1624119.665 ops/s
Iteration   3: 1639949.165 ops/s
Iteration   4: 1632934.243 ops/s
Iteration   5: 1625772.213 ops/s

# Run progress: 85.96% complete, ETA 00:04:21
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1545126.492 ops/s
# Warmup Iteration   2: 1652740.186 ops/s
# Warmup Iteration   3: 1662067.731 ops/s
Iteration   1: 1654580.195 ops/s
Iteration   2: 1649394.094 ops/s
Iteration   3: 1644840.569 ops/s
Iteration   4: 1644912.017 ops/s
Iteration   5: 1654949.452 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.wast":
  1628702.146 ±(99.9%) 20393.913 ops/s [Average]
  (min, avg, max) = (1592477.774, 1628702.146, 1654949.452), stdev = 19076.479
  CI (99.9%): [1608308.233, 1649096.059] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson2

# Run progress: 86.84% complete, ETA 00:04:05
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2073377.821 ops/s
# Warmup Iteration   2: 2186901.360 ops/s
# Warmup Iteration   3: 2212032.626 ops/s
Iteration   1: 2203388.291 ops/s
Iteration   2: 2189023.548 ops/s
Iteration   3: 2197302.713 ops/s
Iteration   4: 2185779.640 ops/s
Iteration   5: 2174254.423 ops/s

# Run progress: 87.72% complete, ETA 00:03:49
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2082642.428 ops/s
# Warmup Iteration   2: 2229599.627 ops/s
# Warmup Iteration   3: 2212886.199 ops/s
Iteration   1: 2212370.352 ops/s
Iteration   2: 2182992.207 ops/s
Iteration   3: 2206876.610 ops/s
Iteration   4: 2225668.545 ops/s
Iteration   5: 2212268.998 ops/s

# Run progress: 88.60% complete, ETA 00:03:32
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2057821.576 ops/s
# Warmup Iteration   2: 2183980.095 ops/s
# Warmup Iteration   3: 2176786.038 ops/s
Iteration   1: 2171229.255 ops/s
Iteration   2: 2191988.378 ops/s
Iteration   3: 2193526.595 ops/s
Iteration   4: 2204584.555 ops/s
Iteration   5: 2192850.733 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson2":
  2196273.656 ±(99.9%) 15910.608 ops/s [Average]
  (min, avg, max) = (2171229.255, 2196273.656, 2225668.545), stdev = 14882.792
  CI (99.9%): [2180363.049, 2212184.264] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3

# Run progress: 89.47% complete, ETA 00:03:16
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2273181.315 ops/s
# Warmup Iteration   2: 2333013.530 ops/s
# Warmup Iteration   3: 2360924.428 ops/s
Iteration   1: 2336343.675 ops/s
Iteration   2: 2370630.666 ops/s
Iteration   3: 2365820.419 ops/s
Iteration   4: 2349075.318 ops/s
Iteration   5: 2356581.639 ops/s

# Run progress: 90.35% complete, ETA 00:03:00
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2249837.348 ops/s
# Warmup Iteration   2: 2340644.566 ops/s
# Warmup Iteration   3: 2327962.300 ops/s
Iteration   1: 2334123.641 ops/s
Iteration   2: 2328997.755 ops/s
Iteration   3: 2334065.550 ops/s
Iteration   4: 2349859.216 ops/s
Iteration   5: 2331639.138 ops/s

# Run progress: 91.23% complete, ETA 00:02:43
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2280496.518 ops/s
# Warmup Iteration   2: 2359952.821 ops/s
# Warmup Iteration   3: 2385566.224 ops/s
Iteration   1: 2383268.821 ops/s
Iteration   2: 2387385.517 ops/s
Iteration   3: 2386949.127 ops/s
Iteration   4: 2381345.344 ops/s
Iteration   5: 2382397.369 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3":
  2358565.546 ±(99.9%) 23891.430 ops/s [Average]
  (min, avg, max) = (2328997.755, 2358565.546, 2387385.517), stdev = 22348.058
  CI (99.9%): [2334674.117, 2382456.976] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_asm

# Run progress: 92.11% complete, ETA 00:02:27
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2376463.623 ops/s
# Warmup Iteration   2: 2464955.177 ops/s
# Warmup Iteration   3: 2452798.792 ops/s
Iteration   1: 2456898.753 ops/s
Iteration   2: 2469855.873 ops/s
Iteration   3: 2487386.590 ops/s
Iteration   4: 2444548.691 ops/s
Iteration   5: 2442858.502 ops/s

# Run progress: 92.98% complete, ETA 00:02:11
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2283919.478 ops/s
# Warmup Iteration   2: 2319012.593 ops/s
# Warmup Iteration   3: 2405347.755 ops/s
Iteration   1: 2412509.426 ops/s
Iteration   2: 2404236.472 ops/s
Iteration   3: 2414677.170 ops/s
Iteration   4: 2384602.783 ops/s
Iteration   5: 2398500.071 ops/s

# Run progress: 93.86% complete, ETA 00:01:54
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2334080.253 ops/s
# Warmup Iteration   2: 2399786.876 ops/s
# Warmup Iteration   3: 2418757.564 ops/s
Iteration   1: 2433021.373 ops/s
Iteration   2: 2403957.711 ops/s
Iteration   3: 2422554.979 ops/s
Iteration   4: 2434290.744 ops/s
Iteration   5: 2415891.011 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_asm":
  2428386.010 ±(99.9%) 30184.962 ops/s [Average]
  (min, avg, max) = (2384602.783, 2428386.010, 2487386.590), stdev = 28235.032
  CI (99.9%): [2398201.048, 2458570.972] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_reflect

# Run progress: 94.74% complete, ETA 00:01:38
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2172676.574 ops/s
# Warmup Iteration   2: 2263355.486 ops/s
# Warmup Iteration   3: 2270257.847 ops/s
Iteration   1: 2278524.804 ops/s
Iteration   2: 2273637.071 ops/s
Iteration   3: 2266740.782 ops/s
Iteration   4: 2293199.341 ops/s
Iteration   5: 2281674.256 ops/s

# Run progress: 95.61% complete, ETA 00:01:21
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2171510.149 ops/s
# Warmup Iteration   2: 2246515.930 ops/s
# Warmup Iteration   3: 2284422.719 ops/s
Iteration   1: 2253312.965 ops/s
Iteration   2: 2257704.006 ops/s
Iteration   3: 2263464.112 ops/s
Iteration   4: 2267292.029 ops/s
Iteration   5: 2243134.988 ops/s

# Run progress: 96.49% complete, ETA 00:01:05
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2171758.682 ops/s
# Warmup Iteration   2: 2197169.981 ops/s
# Warmup Iteration   3: 2197627.547 ops/s
Iteration   1: 2223394.788 ops/s
Iteration   2: 2224454.201 ops/s
Iteration   3: 2220669.459 ops/s
Iteration   4: 2230184.820 ops/s
Iteration   5: 2232455.831 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_reflect":
  2253989.563 ±(99.9%) 25237.562 ops/s [Average]
  (min, avg, max) = (2220669.459, 2253989.563, 2293199.341), stdev = 23607.231
  CI (99.9%): [2228752.001, 2279227.125] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/zulu25.32.21-ca-jdk25.0.2-linux_aarch64/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.wast

# Run progress: 97.37% complete, ETA 00:00:49
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1710940.245 ops/s
# Warmup Iteration   2: 1803282.428 ops/s
# Warmup Iteration   3: 1808953.183 ops/s
Iteration   1: 1811440.207 ops/s
Iteration   2: 1811041.788 ops/s
Iteration   3: 1812601.797 ops/s
Iteration   4: 1814910.882 ops/s
Iteration   5: 1813005.361 ops/s

# Run progress: 98.25% complete, ETA 00:00:32
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1710165.502 ops/s
# Warmup Iteration   2: 1793996.349 ops/s
# Warmup Iteration   3: 1821220.853 ops/s
Iteration   1: 1807422.646 ops/s
Iteration   2: 1816480.335 ops/s
Iteration   3: 1813870.778 ops/s
Iteration   4: 1814177.813 ops/s
Iteration   5: 1815284.161 ops/s

# Run progress: 99.12% complete, ETA 00:00:16
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> false
1702958.386 ops/s
# Warmup Iteration   2: 1789946.119 ops/s
# Warmup Iteration   3: 1795376.739 ops/s
Iteration   1: 1806788.539 ops/s
Iteration   2: 1808437.158 ops/s
Iteration   3: 1808787.937 ops/s
Iteration   4: 1799959.256 ops/s
Iteration   5: 1801894.651 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.wast":
  1810406.887 ±(99.9%) 5192.925 ops/s [Average]
  (min, avg, max) = (1799959.256, 1810406.887, 1816480.335), stdev = 4857.466
  CI (99.9%): [1805213.962, 1815599.813] (assumes normal distribution)


# Run complete. Total time: 00:31:08

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

Benchmark                                  Mode  Cnt        Score       Error  Units
EishayParseString.fastjson2               thrpt   15  1857891.837 ± 39736.766  ops/s
EishayParseString.fastjson3               thrpt   15  1263870.653 ± 17576.077  ops/s
EishayParseString.fastjson3_asm           thrpt   15  1859649.793 ± 32257.714  ops/s
EishayParseString.fastjson3_preconv_utf8  thrpt   15  1294490.744 ± 27806.902  ops/s
EishayParseString.fastjson3_str_parser    thrpt   15  1149353.857 ± 20247.555  ops/s
EishayParseString.wast                    thrpt   15  1721010.724 ± 15794.904  ops/s
EishayParseStringPretty.fastjson2         thrpt   15  1274228.322 ±  9463.228  ops/s
EishayParseStringPretty.fastjson3         thrpt   15  1058318.158 ± 23877.016  ops/s
EishayParseStringPretty.wast              thrpt   15  1153073.972 ± 13191.230  ops/s
EishayParseTreeString.fastjson2           thrpt   15   988016.557 ± 17124.959  ops/s
EishayParseTreeString.fastjson3           thrpt   15   922359.798 ±  5448.622  ops/s
EishayParseTreeString.wast                thrpt   15  1032494.635 ± 75669.136  ops/s
EishayParseTreeUTF8Bytes.fastjson2        thrpt   15   984481.673 ± 20870.149  ops/s
EishayParseTreeUTF8Bytes.fastjson3        thrpt   15   928821.040 ±  7354.703  ops/s
EishayParseTreeUTF8Bytes.wast             thrpt   15  1015526.599 ± 76857.448  ops/s
EishayParseUTF8Bytes.fastjson2            thrpt   15  1801489.310 ± 44534.262  ops/s
EishayParseUTF8Bytes.fastjson3            thrpt   15  1291169.406 ± 22335.029  ops/s
EishayParseUTF8Bytes.fastjson3_asm        thrpt   15  2076298.994 ± 20871.289  ops/s
EishayParseUTF8Bytes.fastjson3_reflect    thrpt   15  1316013.451 ± 27383.790  ops/s
EishayParseUTF8Bytes.wast                 thrpt   15  1679930.772 ± 26766.816  ops/s
EishayParseUTF8BytesPretty.fastjson2      thrpt   15  1252540.797 ±  7334.741  ops/s
EishayParseUTF8BytesPretty.fastjson3      thrpt   15  1099688.987 ± 14175.985  ops/s
EishayParseUTF8BytesPretty.wast           thrpt   15  1115490.030 ± 13997.269  ops/s
EishayValidateUTF8Bytes.fastjson2_bytes   thrpt   15  2262261.424 ± 11232.134  ops/s
EishayValidateUTF8Bytes.fastjson2_string  thrpt   15  2325950.707 ±  9029.053  ops/s
EishayValidateUTF8Bytes.fastjson3_bytes   thrpt   15  1988804.786 ±  6124.488  ops/s
EishayValidateUTF8Bytes.fastjson3_string  thrpt   15  1642958.971 ± 15750.967  ops/s
EishayWriteString.fastjson2               thrpt   15  1879358.881 ± 19559.301  ops/s
EishayWriteString.fastjson3               thrpt   15  2263785.059 ± 66204.245  ops/s
EishayWriteString.fastjson3_asm           thrpt   15  2368308.057 ± 69418.601  ops/s
EishayWriteString.fastjson3_reflect       thrpt   15  2202150.659 ± 36121.499  ops/s
EishayWriteString.fastjson3_utf8latin1    thrpt   15  2371064.077 ± 89756.874  ops/s
EishayWriteString.wast                    thrpt   15  1628702.146 ± 20393.913  ops/s
EishayWriteUTF8Bytes.fastjson2            thrpt   15  2196273.656 ± 15910.608  ops/s
EishayWriteUTF8Bytes.fastjson3            thrpt   15  2358565.546 ± 23891.430  ops/s
EishayWriteUTF8Bytes.fastjson3_asm        thrpt   15  2428386.010 ± 30184.962  ops/s
EishayWriteUTF8Bytes.fastjson3_reflect    thrpt   15  2253989.563 ± 25237.562  ops/s
EishayWriteUTF8Bytes.wast                 thrpt   15  1810406.887 ±  5192.925  ops/s

Benchmark result is saved to /tmp/bench-xplat-root_172_16_1_231.json
```

## x86_64 AMD EPYC 9T95 4c

**Host**: 172.16.172.143
**JDK**: Zulu 25.0.2+10-LTS
**Arch**: x86_64
**Kernel**: 6.12.69+deb13-amd64
**Cores**: 4
**JMH args**: `-f 3 -wi 3 -i 5 -w 2 -r 2` single-threaded
**Pattern**: `Eishay.*\.(fastjson2|fastjson3|fastjson3_asm|fastjson3_reflect|wast)`
**jar md5**: `2df686ace7d78c9da05bb0f6c54edc95`

```
### Host: root@172.16.172.143
### arch: x86_64
### kernel: 6.12.69+deb13-amd64
### openjdk version "25.0.2" 2026-01-20 LTS
### OpenJDK Runtime Environment Zulu25.32+21-CA (build 25.0.2+10-LTS)
### OpenJDK 64-Bit Server VM Zulu25.32+21-CA (build 25.0.2+10-LTS, mixed mode, sharing)
### jar md5: 2df686ace7d78c9da05bb0f6c54edc95
### cmd: java -XX:-UseCompressedOops --add-modules jdk.incubator.vector -jar benchmark3.jar Eishay.*\.(fastjson2|fastjson3|fastjson3_asm|fastjson3_reflect|wast) -f 3 -wi 3 -i 5 -w 2 -r 2

WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson2

# Run progress: 0.00% complete, ETA 00:30:24
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3146580.348 ops/s
# Warmup Iteration   2: 3383325.323 ops/s
# Warmup Iteration   3: 3366496.023 ops/s
Iteration   1: 3371078.216 ops/s
Iteration   2: 3369888.150 ops/s
Iteration   3: 3368172.409 ops/s
Iteration   4: 3365834.047 ops/s
Iteration   5: 3370545.004 ops/s

# Run progress: 0.88% complete, ETA 00:30:39
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3161292.245 ops/s
# Warmup Iteration   2: 3401146.700 ops/s
# Warmup Iteration   3: 3375622.199 ops/s
Iteration   1: 3377403.033 ops/s
Iteration   2: 3373226.357 ops/s
Iteration   3: 3369876.358 ops/s
Iteration   4: 3374191.867 ops/s
Iteration   5: 3374220.238 ops/s

# Run progress: 1.75% complete, ETA 00:30:21
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3220603.869 ops/s
# Warmup Iteration   2: 3466369.624 ops/s
# Warmup Iteration   3: 3475143.295 ops/s
Iteration   1: 3457260.066 ops/s
Iteration   2: 3478371.400 ops/s
Iteration   3: 3374476.401 ops/s
Iteration   4: 3444540.983 ops/s
Iteration   5: 3476691.694 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson2":
  3396385.082 ±(99.9%) 46069.289 ops/s [Average]
  (min, avg, max) = (3365834.047, 3396385.082, 3478371.400), stdev = 43093.241
  CI (99.9%): [3350315.793, 3442454.370] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3

# Run progress: 2.63% complete, ETA 00:30:04
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2434006.748 ops/s
# Warmup Iteration   2: 2535102.233 ops/s
# Warmup Iteration   3: 2536172.168 ops/s
Iteration   1: 2538906.393 ops/s
Iteration   2: 2474808.864 ops/s
Iteration   3: 2539448.761 ops/s
Iteration   4: 2537189.341 ops/s
Iteration   5: 2536367.726 ops/s

# Run progress: 3.51% complete, ETA 00:29:48
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2442502.198 ops/s
# Warmup Iteration   2: 2518574.096 ops/s
# Warmup Iteration   3: 2512584.389 ops/s
Iteration   1: 2486858.781 ops/s
Iteration   2: 2512882.139 ops/s
Iteration   3: 2513665.680 ops/s
Iteration   4: 2515216.028 ops/s
Iteration   5: 2514789.856 ops/s

# Run progress: 4.39% complete, ETA 00:29:32
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2464349.957 ops/s
# Warmup Iteration   2: 2539154.692 ops/s
# Warmup Iteration   3: 2542976.878 ops/s
Iteration   1: 2543345.914 ops/s
Iteration   2: 2543090.265 ops/s
Iteration   3: 2541929.630 ops/s
Iteration   4: 2543476.143 ops/s
Iteration   5: 2510552.053 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3":
  2523501.838 ±(99.9%) 23204.899 ops/s [Average]
  (min, avg, max) = (2474808.864, 2523501.838, 2543476.143), stdev = 21705.877
  CI (99.9%): [2500296.939, 2546706.737] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_asm

# Run progress: 5.26% complete, ETA 00:29:16
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3484118.906 ops/s
# Warmup Iteration   2: 3617889.385 ops/s
# Warmup Iteration   3: 3614653.436 ops/s
Iteration   1: 3615318.617 ops/s
Iteration   2: 3611060.257 ops/s
Iteration   3: 3618728.230 ops/s
Iteration   4: 3513382.080 ops/s
Iteration   5: 3616787.951 ops/s

# Run progress: 6.14% complete, ETA 00:28:59
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3559865.722 ops/s
# Warmup Iteration   2: 3584544.699 ops/s
# Warmup Iteration   3: 3699507.501 ops/s
Iteration   1: 3660063.833 ops/s
Iteration   2: 3584884.851 ops/s
Iteration   3: 3579525.703 ops/s
Iteration   4: 3700749.590 ops/s
Iteration   5: 3683709.784 ops/s

# Run progress: 7.02% complete, ETA 00:28:43
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3536196.016 ops/s
# Warmup Iteration   2: 3670099.684 ops/s
# Warmup Iteration   3: 3665288.953 ops/s
Iteration   1: 3667501.283 ops/s
Iteration   2: 3668236.786 ops/s
Iteration   3: 3666944.992 ops/s
Iteration   4: 3656440.356 ops/s
Iteration   5: 3666620.109 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_asm":
  3633996.961 ±(99.9%) 52595.904 ops/s [Average]
  (min, avg, max) = (3513382.080, 3633996.961, 3700749.590), stdev = 49198.242
  CI (99.9%): [3581401.057, 3686592.866] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_preconv_utf8

# Run progress: 7.89% complete, ETA 00:28:27
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2585045.791 ops/s
# Warmup Iteration   2: 2648219.695 ops/s
# Warmup Iteration   3: 2662061.553 ops/s
Iteration   1: 2663548.642 ops/s
Iteration   2: 2662702.567 ops/s
Iteration   3: 2664237.222 ops/s
Iteration   4: 2662776.270 ops/s
Iteration   5: 2663508.766 ops/s

# Run progress: 8.77% complete, ETA 00:28:10
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2523140.072 ops/s
# Warmup Iteration   2: 2616625.831 ops/s
# Warmup Iteration   3: 2620190.775 ops/s
Iteration   1: 2619404.862 ops/s
Iteration   2: 2620770.290 ops/s
Iteration   3: 2612023.294 ops/s
Iteration   4: 2621003.436 ops/s
Iteration   5: 2621317.740 ops/s

# Run progress: 9.65% complete, ETA 00:27:54
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2577298.506 ops/s
# Warmup Iteration   2: 2641784.956 ops/s
# Warmup Iteration   3: 2639767.501 ops/s
Iteration   1: 2641780.320 ops/s
Iteration   2: 2640181.111 ops/s
Iteration   3: 2639232.576 ops/s
Iteration   4: 2638618.085 ops/s
Iteration   5: 2614270.134 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_preconv_utf8":
  2639025.021 ±(99.9%) 21512.174 ops/s [Average]
  (min, avg, max) = (2612023.294, 2639025.021, 2664237.222), stdev = 20122.501
  CI (99.9%): [2617512.847, 2660537.195] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser

# Run progress: 10.53% complete, ETA 00:27:38
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2139495.269 ops/s
# Warmup Iteration   2: 2202937.199 ops/s
# Warmup Iteration   3: 2202086.952 ops/s
Iteration   1: 2200750.229 ops/s
Iteration   2: 2200325.339 ops/s
Iteration   3: 2198390.400 ops/s
Iteration   4: 2199066.742 ops/s
Iteration   5: 2181216.204 ops/s

# Run progress: 11.40% complete, ETA 00:27:21
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2146079.989 ops/s
# Warmup Iteration   2: 2202759.669 ops/s
# Warmup Iteration   3: 2138622.829 ops/s
Iteration   1: 2189783.806 ops/s
Iteration   2: 2210032.351 ops/s
Iteration   3: 2170272.396 ops/s
Iteration   4: 2169437.555 ops/s
Iteration   5: 2199568.090 ops/s

# Run progress: 12.28% complete, ETA 00:27:05
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2145894.777 ops/s
# Warmup Iteration   2: 2212058.336 ops/s
# Warmup Iteration   3: 2216140.889 ops/s
Iteration   1: 2218555.807 ops/s
Iteration   2: 2216913.971 ops/s
Iteration   3: 2217762.138 ops/s
Iteration   4: 2219199.776 ops/s
Iteration   5: 2217295.069 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser":
  2200571.325 ±(99.9%) 18044.901 ops/s [Average]
  (min, avg, max) = (2169437.555, 2200571.325, 2219199.776), stdev = 16879.211
  CI (99.9%): [2182526.424, 2218616.225] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.wast

# Run progress: 13.16% complete, ETA 00:26:49
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2610202.544 ops/s
# Warmup Iteration   2: 3274305.421 ops/s
# Warmup Iteration   3: 3291969.176 ops/s
Iteration   1: 3296116.658 ops/s
Iteration   2: 3292668.375 ops/s
Iteration   3: 3290293.040 ops/s
Iteration   4: 3292326.761 ops/s
Iteration   5: 3276202.446 ops/s

# Run progress: 14.04% complete, ETA 00:26:32
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2652888.495 ops/s
# Warmup Iteration   2: 3244865.490 ops/s
# Warmup Iteration   3: 3264172.982 ops/s
Iteration   1: 3266659.016 ops/s
Iteration   2: 3258645.532 ops/s
Iteration   3: 3263733.184 ops/s
Iteration   4: 3268329.036 ops/s
Iteration   5: 3269910.183 ops/s

# Run progress: 14.91% complete, ETA 00:26:16
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2690610.074 ops/s
# Warmup Iteration   2: 3327385.837 ops/s
# Warmup Iteration   3: 3275971.944 ops/s
Iteration   1: 3276402.560 ops/s
Iteration   2: 3253710.376 ops/s
Iteration   3: 3286168.126 ops/s
Iteration   4: 3305236.401 ops/s
Iteration   5: 3322623.174 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.wast":
  3281268.325 ±(99.9%) 20204.933 ops/s [Average]
  (min, avg, max) = (3253710.376, 3281268.325, 3322623.174), stdev = 18899.707
  CI (99.9%): [3261063.392, 3301473.257] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson2

# Run progress: 15.79% complete, ETA 00:26:00
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2430433.975 ops/s
# Warmup Iteration   2: 2660129.635 ops/s
# Warmup Iteration   3: 2657085.734 ops/s
Iteration   1: 2656306.312 ops/s
Iteration   2: 2655973.108 ops/s
Iteration   3: 2654747.392 ops/s
Iteration   4: 2656581.380 ops/s
Iteration   5: 2656279.747 ops/s

# Run progress: 16.67% complete, ETA 00:25:43
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2409240.099 ops/s
# Warmup Iteration   2: 2633706.020 ops/s
# Warmup Iteration   3: 2631262.446 ops/s
Iteration   1: 2629908.099 ops/s
Iteration   2: 2630876.427 ops/s
Iteration   3: 2629201.862 ops/s
Iteration   4: 2629380.904 ops/s
Iteration   5: 2629653.621 ops/s

# Run progress: 17.54% complete, ETA 00:25:27
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2377500.531 ops/s
# Warmup Iteration   2: 2592493.176 ops/s
# Warmup Iteration   3: 2596914.962 ops/s
Iteration   1: 2597237.843 ops/s
Iteration   2: 2596322.213 ops/s
Iteration   3: 2595059.617 ops/s
Iteration   4: 2596234.647 ops/s
Iteration   5: 2595305.472 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson2":
  2627271.243 ±(99.9%) 27163.804 ops/s [Average]
  (min, avg, max) = (2595059.617, 2627271.243, 2656581.380), stdev = 25409.039
  CI (99.9%): [2600107.439, 2654435.047] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson3

# Run progress: 18.42% complete, ETA 00:25:11
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2042688.668 ops/s
# Warmup Iteration   2: 2139604.638 ops/s
# Warmup Iteration   3: 2141625.312 ops/s
Iteration   1: 2141258.062 ops/s
Iteration   2: 2124685.262 ops/s
Iteration   3: 2141951.120 ops/s
Iteration   4: 2142913.965 ops/s
Iteration   5: 2143301.078 ops/s

# Run progress: 19.30% complete, ETA 00:24:54
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2154131.125 ops/s
# Warmup Iteration   2: 2268795.022 ops/s
# Warmup Iteration   3: 2257590.243 ops/s
Iteration   1: 2259394.250 ops/s
Iteration   2: 2258659.658 ops/s
Iteration   3: 2261003.037 ops/s
Iteration   4: 2258328.293 ops/s
Iteration   5: 2257462.332 ops/s

# Run progress: 20.18% complete, ETA 00:24:38
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2122139.332 ops/s
# Warmup Iteration   2: 2215319.630 ops/s
# Warmup Iteration   3: 2227955.255 ops/s
Iteration   1: 2225443.718 ops/s
Iteration   2: 2224381.176 ops/s
Iteration   3: 2227506.903 ops/s
Iteration   4: 2225321.636 ops/s
Iteration   5: 2222495.326 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson3":
  2207607.054 ±(99.9%) 56162.372 ops/s [Average]
  (min, avg, max) = (2124685.262, 2207607.054, 2261003.037), stdev = 52534.317
  CI (99.9%): [2151444.683, 2263769.426] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.wast

# Run progress: 21.05% complete, ETA 00:24:22
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1971307.706 ops/s
# Warmup Iteration   2: 2422918.174 ops/s
# Warmup Iteration   3: 2423408.571 ops/s
Iteration   1: 2406675.147 ops/s
Iteration   2: 2399182.382 ops/s
Iteration   3: 2409734.708 ops/s
Iteration   4: 2421129.030 ops/s
Iteration   5: 2423124.701 ops/s

# Run progress: 21.93% complete, ETA 00:24:05
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1916470.385 ops/s
# Warmup Iteration   2: 2430543.492 ops/s
# Warmup Iteration   3: 2406898.407 ops/s
Iteration   1: 2471721.875 ops/s
Iteration   2: 2471559.858 ops/s
Iteration   3: 2467902.222 ops/s
Iteration   4: 2471664.693 ops/s
Iteration   5: 2468109.313 ops/s

# Run progress: 22.81% complete, ETA 00:23:49
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1905505.147 ops/s
# Warmup Iteration   2: 2346970.078 ops/s
# Warmup Iteration   3: 2381895.902 ops/s
Iteration   1: 2392442.307 ops/s
Iteration   2: 2398741.794 ops/s
Iteration   3: 2377122.388 ops/s
Iteration   4: 2391333.063 ops/s
Iteration   5: 2401421.079 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.wast":
  2424790.971 ±(99.9%) 37484.453 ops/s [Average]
  (min, avg, max) = (2377122.388, 2424790.971, 2471721.875), stdev = 35062.980
  CI (99.9%): [2387306.518, 2462275.424] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson2

# Run progress: 23.68% complete, ETA 00:23:33
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1844779.769 ops/s
# Warmup Iteration   2: 1947469.001 ops/s
# Warmup Iteration   3: 1951612.639 ops/s
Iteration   1: 1949938.053 ops/s
Iteration   2: 1947528.948 ops/s
Iteration   3: 1951682.499 ops/s
Iteration   4: 1949865.500 ops/s
Iteration   5: 1945888.710 ops/s

# Run progress: 24.56% complete, ETA 00:23:16
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1837404.067 ops/s
# Warmup Iteration   2: 1944302.096 ops/s
# Warmup Iteration   3: 1941049.355 ops/s
Iteration   1: 1939819.877 ops/s
Iteration   2: 1941584.723 ops/s
Iteration   3: 1943718.132 ops/s
Iteration   4: 1939550.148 ops/s
Iteration   5: 1941302.512 ops/s

# Run progress: 25.44% complete, ETA 00:23:00
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1843837.079 ops/s
# Warmup Iteration   2: 1945047.237 ops/s
# Warmup Iteration   3: 1947237.371 ops/s
Iteration   1: 1946972.744 ops/s
Iteration   2: 1946688.951 ops/s
Iteration   3: 1943732.801 ops/s
Iteration   4: 1943070.292 ops/s
Iteration   5: 1947120.828 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson2":
  1945230.981 ±(99.9%) 4032.412 ops/s [Average]
  (min, avg, max) = (1939550.148, 1945230.981, 1951682.499), stdev = 3771.920
  CI (99.9%): [1941198.569, 1949263.393] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson3

# Run progress: 26.32% complete, ETA 00:22:44
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1345331.873 ops/s
# Warmup Iteration   2: 1391652.636 ops/s
# Warmup Iteration   3: 1392743.824 ops/s
Iteration   1: 1393814.366 ops/s
Iteration   2: 1392851.462 ops/s
Iteration   3: 1393790.050 ops/s
Iteration   4: 1393737.242 ops/s
Iteration   5: 1392545.924 ops/s

# Run progress: 27.19% complete, ETA 00:22:27
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1341745.230 ops/s
# Warmup Iteration   2: 1392544.965 ops/s
# Warmup Iteration   3: 1393246.198 ops/s
Iteration   1: 1393447.604 ops/s
Iteration   2: 1392894.952 ops/s
Iteration   3: 1391906.073 ops/s
Iteration   4: 1348569.478 ops/s
Iteration   5: 1393642.243 ops/s

# Run progress: 28.07% complete, ETA 00:22:11
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1339998.024 ops/s
# Warmup Iteration   2: 1392014.037 ops/s
# Warmup Iteration   3: 1391390.091 ops/s
Iteration   1: 1391530.921 ops/s
Iteration   2: 1391167.320 ops/s
Iteration   3: 1393097.120 ops/s
Iteration   4: 1391963.978 ops/s
Iteration   5: 1392817.478 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson3":
  1389851.747 ±(99.9%) 12242.334 ops/s [Average]
  (min, avg, max) = (1348569.478, 1389851.747, 1393814.366), stdev = 11451.486
  CI (99.9%): [1377609.414, 1402094.081] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.wast

# Run progress: 28.95% complete, ETA 00:21:55
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1503035.604 ops/s
# Warmup Iteration   2: 1802835.578 ops/s
# Warmup Iteration   3: 1814189.437 ops/s
Iteration   1: 1803758.766 ops/s
Iteration   2: 1802075.506 ops/s
Iteration   3: 1772181.057 ops/s
Iteration   4: 1766047.572 ops/s
Iteration   5: 1797887.345 ops/s

# Run progress: 29.82% complete, ETA 00:21:38
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1484356.517 ops/s
# Warmup Iteration   2: 1815002.712 ops/s
# Warmup Iteration   3: 1826107.100 ops/s
Iteration   1: 1824389.880 ops/s
Iteration   2: 1816777.907 ops/s
Iteration   3: 1821828.151 ops/s
Iteration   4: 1823563.806 ops/s
Iteration   5: 1819810.012 ops/s

# Run progress: 30.70% complete, ETA 00:21:22
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1505618.004 ops/s
# Warmup Iteration   2: 1814664.416 ops/s
# Warmup Iteration   3: 1811218.991 ops/s
Iteration   1: 1815384.002 ops/s
Iteration   2: 1813387.901 ops/s
Iteration   3: 1815354.497 ops/s
Iteration   4: 1812589.540 ops/s
Iteration   5: 1813873.629 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.wast":
  1807927.305 ±(99.9%) 18757.778 ops/s [Average]
  (min, avg, max) = (1766047.572, 1807927.305, 1824389.880), stdev = 17546.038
  CI (99.9%): [1789169.526, 1826685.083] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson2

# Run progress: 31.58% complete, ETA 00:21:06
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1837615.808 ops/s
# Warmup Iteration   2: 1939267.794 ops/s
# Warmup Iteration   3: 1946769.922 ops/s
Iteration   1: 1942811.085 ops/s
Iteration   2: 1945289.792 ops/s
Iteration   3: 1943811.486 ops/s
Iteration   4: 1943009.874 ops/s
Iteration   5: 1945156.174 ops/s

# Run progress: 32.46% complete, ETA 00:20:50
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1802919.598 ops/s
# Warmup Iteration   2: 1906760.252 ops/s
# Warmup Iteration   3: 1911735.797 ops/s
Iteration   1: 1911366.037 ops/s
Iteration   2: 1907382.992 ops/s
Iteration   3: 1907954.199 ops/s
Iteration   4: 1910760.831 ops/s
Iteration   5: 1894412.667 ops/s

# Run progress: 33.33% complete, ETA 00:20:33
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1819457.709 ops/s
# Warmup Iteration   2: 1921074.358 ops/s
# Warmup Iteration   3: 1929845.733 ops/s
Iteration   1: 1930397.893 ops/s
Iteration   2: 1928219.789 ops/s
Iteration   3: 1925697.866 ops/s
Iteration   4: 1928331.873 ops/s
Iteration   5: 1930560.880 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson2":
  1926344.229 ±(99.9%) 17597.714 ops/s [Average]
  (min, avg, max) = (1894412.667, 1926344.229, 1945289.792), stdev = 16460.913
  CI (99.9%): [1908746.515, 1943941.944] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson3

# Run progress: 34.21% complete, ETA 00:20:17
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1334871.332 ops/s
# Warmup Iteration   2: 1388989.715 ops/s
# Warmup Iteration   3: 1396741.164 ops/s
Iteration   1: 1395934.675 ops/s
Iteration   2: 1396036.988 ops/s
Iteration   3: 1395493.551 ops/s
Iteration   4: 1396001.877 ops/s
Iteration   5: 1396008.307 ops/s

# Run progress: 35.09% complete, ETA 00:20:01
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1277818.619 ops/s
# Warmup Iteration   2: 1388161.034 ops/s
# Warmup Iteration   3: 1393774.308 ops/s
Iteration   1: 1393366.118 ops/s
Iteration   2: 1393953.240 ops/s
Iteration   3: 1393851.457 ops/s
Iteration   4: 1392770.489 ops/s
Iteration   5: 1393655.354 ops/s

# Run progress: 35.96% complete, ETA 00:19:44
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1334686.371 ops/s
# Warmup Iteration   2: 1382136.244 ops/s
# Warmup Iteration   3: 1390415.447 ops/s
Iteration   1: 1389481.560 ops/s
Iteration   2: 1394645.677 ops/s
Iteration   3: 1394352.241 ops/s
Iteration   4: 1393769.777 ops/s
Iteration   5: 1393922.317 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson3":
  1394216.242 ±(99.9%) 1819.092 ops/s [Average]
  (min, avg, max) = (1389481.560, 1394216.242, 1396036.988), stdev = 1701.580
  CI (99.9%): [1392397.150, 1396035.334] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.wast

# Run progress: 36.84% complete, ETA 00:19:28
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1442552.506 ops/s
# Warmup Iteration   2: 1755218.339 ops/s
# Warmup Iteration   3: 1773844.671 ops/s
Iteration   1: 1764017.098 ops/s
Iteration   2: 1771273.452 ops/s
Iteration   3: 1767129.724 ops/s
Iteration   4: 1767938.609 ops/s
Iteration   5: 1765193.787 ops/s

# Run progress: 37.72% complete, ETA 00:19:12
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1511441.138 ops/s
# Warmup Iteration   2: 1806144.169 ops/s
# Warmup Iteration   3: 1823936.440 ops/s
Iteration   1: 1822776.334 ops/s
Iteration   2: 1829348.155 ops/s
Iteration   3: 1821432.598 ops/s
Iteration   4: 1761750.849 ops/s
Iteration   5: 1818757.142 ops/s

# Run progress: 38.60% complete, ETA 00:18:56
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1505159.851 ops/s
# Warmup Iteration   2: 1822720.346 ops/s
# Warmup Iteration   3: 1818239.193 ops/s
Iteration   1: 1816163.302 ops/s
Iteration   2: 1811080.465 ops/s
Iteration   3: 1812460.983 ops/s
Iteration   4: 1820053.064 ops/s
Iteration   5: 1817349.240 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.wast":
  1797781.653 ±(99.9%) 28944.727 ops/s [Average]
  (min, avg, max) = (1761750.849, 1797781.653, 1829348.155), stdev = 27074.915
  CI (99.9%): [1768836.927, 1826726.380] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson2

# Run progress: 39.47% complete, ETA 00:18:39
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3107974.197 ops/s
# Warmup Iteration   2: 3367334.008 ops/s
# Warmup Iteration   3: 3362828.780 ops/s
Iteration   1: 3363159.739 ops/s
Iteration   2: 3354088.846 ops/s
Iteration   3: 3365871.750 ops/s
Iteration   4: 3364936.973 ops/s
Iteration   5: 3367301.654 ops/s

# Run progress: 40.35% complete, ETA 00:18:23
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3129237.592 ops/s
# Warmup Iteration   2: 3362734.723 ops/s
# Warmup Iteration   3: 3360671.897 ops/s
Iteration   1: 3365814.079 ops/s
Iteration   2: 3364031.223 ops/s
Iteration   3: 3363946.379 ops/s
Iteration   4: 3365330.757 ops/s
Iteration   5: 3363328.541 ops/s

# Run progress: 41.23% complete, ETA 00:18:07
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3173449.941 ops/s
# Warmup Iteration   2: 3432116.883 ops/s
# Warmup Iteration   3: 3420018.618 ops/s
Iteration   1: 3423224.334 ops/s
Iteration   2: 3416784.006 ops/s
Iteration   3: 3422781.427 ops/s
Iteration   4: 3421399.532 ops/s
Iteration   5: 3400045.120 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson2":
  3381469.624 ±(99.9%) 28406.805 ops/s [Average]
  (min, avg, max) = (3354088.846, 3381469.624, 3423224.334), stdev = 26571.743
  CI (99.9%): [3353062.819, 3409876.429] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3

# Run progress: 42.11% complete, ETA 00:17:51
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2541750.161 ops/s
# Warmup Iteration   2: 2603280.222 ops/s
# Warmup Iteration   3: 2611210.945 ops/s
Iteration   1: 2610872.135 ops/s
Iteration   2: 2608127.700 ops/s
Iteration   3: 2607817.612 ops/s
Iteration   4: 2613055.673 ops/s
Iteration   5: 2609706.284 ops/s

# Run progress: 42.98% complete, ETA 00:17:35
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2545704.364 ops/s
# Warmup Iteration   2: 2626756.498 ops/s
# Warmup Iteration   3: 2621875.468 ops/s
Iteration   1: 2623705.220 ops/s
Iteration   2: 2622839.398 ops/s
Iteration   3: 2624193.531 ops/s
Iteration   4: 2623392.706 ops/s
Iteration   5: 2624048.494 ops/s

# Run progress: 43.86% complete, ETA 00:17:18
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2541715.094 ops/s
# Warmup Iteration   2: 2628946.249 ops/s
# Warmup Iteration   3: 2536706.553 ops/s
Iteration   1: 2626912.125 ops/s
Iteration   2: 2624832.478 ops/s
Iteration   3: 2619866.611 ops/s
Iteration   4: 2622870.829 ops/s
Iteration   5: 2621536.128 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3":
  2618918.462 ±(99.9%) 7331.032 ops/s [Average]
  (min, avg, max) = (2607817.612, 2618918.462, 2626912.125), stdev = 6857.452
  CI (99.9%): [2611587.429, 2626249.494] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_asm

# Run progress: 44.74% complete, ETA 00:17:02
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3933666.399 ops/s
# Warmup Iteration   2: 4022135.835 ops/s
# Warmup Iteration   3: 4035310.535 ops/s
Iteration   1: 4015145.467 ops/s
Iteration   2: 4024848.308 ops/s
Iteration   3: 4037465.487 ops/s
Iteration   4: 4034814.114 ops/s
Iteration   5: 4031315.545 ops/s

# Run progress: 45.61% complete, ETA 00:16:46
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3936587.109 ops/s
# Warmup Iteration   2: 4034368.250 ops/s
# Warmup Iteration   3: 4053250.401 ops/s
Iteration   1: 4049246.193 ops/s
Iteration   2: 4049859.367 ops/s
Iteration   3: 4029170.294 ops/s
Iteration   4: 4022174.472 ops/s
Iteration   5: 4052913.523 ops/s

# Run progress: 46.49% complete, ETA 00:16:30
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3836570.328 ops/s
# Warmup Iteration   2: 3917452.376 ops/s
# Warmup Iteration   3: 3988076.129 ops/s
Iteration   1: 3992343.377 ops/s
Iteration   2: 3988853.728 ops/s
Iteration   3: 3992251.581 ops/s
Iteration   4: 3988219.676 ops/s
Iteration   5: 3943312.889 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_asm":
  4016795.602 ±(99.9%) 32401.651 ops/s [Average]
  (min, avg, max) = (3943312.889, 4016795.602, 4052913.523), stdev = 30308.524
  CI (99.9%): [3984393.951, 4049197.252] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_reflect

# Run progress: 47.37% complete, ETA 00:16:14
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2606013.803 ops/s
# Warmup Iteration   2: 2682477.125 ops/s
# Warmup Iteration   3: 2682399.748 ops/s
Iteration   1: 2682592.820 ops/s
Iteration   2: 2682746.625 ops/s
Iteration   3: 2680568.348 ops/s
Iteration   4: 2682805.564 ops/s
Iteration   5: 2683292.177 ops/s

# Run progress: 48.25% complete, ETA 00:15:57
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2609411.294 ops/s
# Warmup Iteration   2: 2673600.003 ops/s
# Warmup Iteration   3: 2685957.193 ops/s
Iteration   1: 2685500.630 ops/s
Iteration   2: 2683543.706 ops/s
Iteration   3: 2683545.627 ops/s
Iteration   4: 2685700.753 ops/s
Iteration   5: 2684488.510 ops/s

# Run progress: 49.12% complete, ETA 00:15:41
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2592297.887 ops/s
# Warmup Iteration   2: 2706034.464 ops/s
# Warmup Iteration   3: 2707992.671 ops/s
Iteration   1: 2706885.932 ops/s
Iteration   2: 2707260.960 ops/s
Iteration   3: 2706808.700 ops/s
Iteration   4: 2705195.082 ops/s
Iteration   5: 2693733.417 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_reflect":
  2690311.257 ±(99.9%) 11265.497 ops/s [Average]
  (min, avg, max) = (2680568.348, 2690311.257, 2707260.960), stdev = 10537.753
  CI (99.9%): [2679045.760, 2701576.753] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.wast

# Run progress: 50.00% complete, ETA 00:15:25
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2649130.548 ops/s
# Warmup Iteration   2: 3283716.346 ops/s
# Warmup Iteration   3: 3279643.968 ops/s
Iteration   1: 3280897.623 ops/s
Iteration   2: 3279110.010 ops/s
Iteration   3: 3276956.366 ops/s
Iteration   4: 3279442.057 ops/s
Iteration   5: 3281025.776 ops/s

# Run progress: 50.88% complete, ETA 00:15:09
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2612964.979 ops/s
# Warmup Iteration   2: 3208287.163 ops/s
# Warmup Iteration   3: 3228011.009 ops/s
Iteration   1: 3189052.657 ops/s
Iteration   2: 3186751.519 ops/s
Iteration   3: 3204117.526 ops/s
Iteration   4: 3213410.859 ops/s
Iteration   5: 3228744.884 ops/s

# Run progress: 51.75% complete, ETA 00:14:52
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2607765.498 ops/s
# Warmup Iteration   2: 3238659.560 ops/s
# Warmup Iteration   3: 3235846.964 ops/s
Iteration   1: 3233890.493 ops/s
Iteration   2: 3239259.704 ops/s
Iteration   3: 3238793.723 ops/s
Iteration   4: 3237433.995 ops/s
Iteration   5: 3234980.183 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.wast":
  3240257.825 ±(99.9%) 35490.250 ops/s [Average]
  (min, avg, max) = (3186751.519, 3240257.825, 3281025.776), stdev = 33197.602
  CI (99.9%): [3204767.575, 3275748.075] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson2

# Run progress: 52.63% complete, ETA 00:14:36
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2406393.859 ops/s
# Warmup Iteration   2: 2638630.878 ops/s
# Warmup Iteration   3: 2640925.859 ops/s
Iteration   1: 2641176.521 ops/s
Iteration   2: 2640499.787 ops/s
Iteration   3: 2640689.070 ops/s
Iteration   4: 2641646.679 ops/s
Iteration   5: 2641366.370 ops/s

# Run progress: 53.51% complete, ETA 00:14:20
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2328748.510 ops/s
# Warmup Iteration   2: 2554368.841 ops/s
# Warmup Iteration   3: 2550293.289 ops/s
Iteration   1: 2554319.585 ops/s
Iteration   2: 2553752.558 ops/s
Iteration   3: 2548138.891 ops/s
Iteration   4: 2547126.787 ops/s
Iteration   5: 2550263.202 ops/s

# Run progress: 54.39% complete, ETA 00:14:04
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2427563.027 ops/s
# Warmup Iteration   2: 2582183.314 ops/s
# Warmup Iteration   3: 2655003.936 ops/s
Iteration   1: 2655900.433 ops/s
Iteration   2: 2653991.824 ops/s
Iteration   3: 2640172.094 ops/s
Iteration   4: 2653182.583 ops/s
Iteration   5: 2655819.635 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson2":
  2614536.401 ±(99.9%) 50346.300 ops/s [Average]
  (min, avg, max) = (2547126.787, 2614536.401, 2655900.433), stdev = 47093.960
  CI (99.9%): [2564190.101, 2664882.701] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson3

# Run progress: 55.26% complete, ETA 00:13:47
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2199654.806 ops/s
# Warmup Iteration   2: 2332770.635 ops/s
# Warmup Iteration   3: 2320238.871 ops/s
Iteration   1: 2328449.742 ops/s
Iteration   2: 2324396.921 ops/s
Iteration   3: 2324152.537 ops/s
Iteration   4: 2322357.277 ops/s
Iteration   5: 2320366.749 ops/s

# Run progress: 56.14% complete, ETA 00:13:31
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2244433.748 ops/s
# Warmup Iteration   2: 2349131.900 ops/s
# Warmup Iteration   3: 2357307.755 ops/s
Iteration   1: 2353842.664 ops/s
Iteration   2: 2357666.782 ops/s
Iteration   3: 2354026.690 ops/s
Iteration   4: 2357486.146 ops/s
Iteration   5: 2350883.940 ops/s

# Run progress: 57.02% complete, ETA 00:13:15
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2220777.581 ops/s
# Warmup Iteration   2: 2314265.965 ops/s
# Warmup Iteration   3: 2314339.788 ops/s
Iteration   1: 2283178.404 ops/s
Iteration   2: 2307695.414 ops/s
Iteration   3: 2315460.870 ops/s
Iteration   4: 2314640.406 ops/s
Iteration   5: 2317230.842 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson3":
  2328789.026 ±(99.9%) 23212.139 ops/s [Average]
  (min, avg, max) = (2283178.404, 2328789.026, 2357666.782), stdev = 21712.650
  CI (99.9%): [2305576.886, 2352001.165] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.wast

# Run progress: 57.89% complete, ETA 00:12:59
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1951424.744 ops/s
# Warmup Iteration   2: 2376766.126 ops/s
# Warmup Iteration   3: 2331826.147 ops/s
Iteration   1: 2362524.795 ops/s
Iteration   2: 2366113.705 ops/s
Iteration   3: 2369299.433 ops/s
Iteration   4: 2365104.103 ops/s
Iteration   5: 2366215.580 ops/s

# Run progress: 58.77% complete, ETA 00:12:42
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1970503.533 ops/s
# Warmup Iteration   2: 2454932.011 ops/s
# Warmup Iteration   3: 2455483.058 ops/s
Iteration   1: 2454782.837 ops/s
Iteration   2: 2457305.140 ops/s
Iteration   3: 2456104.218 ops/s
Iteration   4: 2456428.639 ops/s
Iteration   5: 2455120.604 ops/s

# Run progress: 59.65% complete, ETA 00:12:26
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
1982751.074 ops/s
# Warmup Iteration   2: 2465601.774 ops/s
# Warmup Iteration   3: 2479673.796 ops/s
Iteration   1: 2470455.662 ops/s
Iteration   2: 2482721.481 ops/s
Iteration   3: 2482560.791 ops/s
Iteration   4: 2399575.407 ops/s
Iteration   5: 2482202.619 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.wast":
  2428434.334 ±(99.9%) 53271.296 ops/s [Average]
  (min, avg, max) = (2362524.795, 2428434.334, 2482721.481), stdev = 49830.004
  CI (99.9%): [2375163.038, 2481705.631] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson2_bytes

# Run progress: 60.53% complete, ETA 00:12:10
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3354855.615 ops/s
# Warmup Iteration   2: 3533153.315 ops/s
# Warmup Iteration   3: 3524483.181 ops/s
Iteration   1: 3525946.774 ops/s
Iteration   2: 3524116.958 ops/s
Iteration   3: 3525551.256 ops/s
Iteration   4: 3528480.459 ops/s
Iteration   5: 3534544.786 ops/s

# Run progress: 61.40% complete, ETA 00:11:54
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3304689.555 ops/s
# Warmup Iteration   2: 3530407.682 ops/s
# Warmup Iteration   3: 3518910.850 ops/s
Iteration   1: 3518189.834 ops/s
Iteration   2: 3519697.734 ops/s
Iteration   3: 3521632.131 ops/s
Iteration   4: 3521055.199 ops/s
Iteration   5: 3523158.008 ops/s

# Run progress: 62.28% complete, ETA 00:11:37
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3356598.179 ops/s
# Warmup Iteration   2: 3543330.656 ops/s
# Warmup Iteration   3: 3535293.080 ops/s
Iteration   1: 3536221.076 ops/s
Iteration   2: 3536374.401 ops/s
Iteration   3: 3533184.641 ops/s
Iteration   4: 3522055.973 ops/s
Iteration   5: 3529438.260 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson2_bytes":
  3526643.166 ±(99.9%) 6516.194 ops/s [Average]
  (min, avg, max) = (3518189.834, 3526643.166, 3536374.401), stdev = 6095.252
  CI (99.9%): [3520126.972, 3533159.360] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson2_string

# Run progress: 63.16% complete, ETA 00:11:21
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3361577.945 ops/s
# Warmup Iteration   2: 3570486.707 ops/s
# Warmup Iteration   3: 3547461.973 ops/s
Iteration   1: 3544795.450 ops/s
Iteration   2: 3542713.704 ops/s
Iteration   3: 3546154.165 ops/s
Iteration   4: 3433992.162 ops/s
Iteration   5: 3534248.781 ops/s

# Run progress: 64.04% complete, ETA 00:11:05
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3217146.248 ops/s
# Warmup Iteration   2: 3485867.702 ops/s
# Warmup Iteration   3: 3489816.993 ops/s
Iteration   1: 3486679.874 ops/s
Iteration   2: 3491462.360 ops/s
Iteration   3: 3490062.449 ops/s
Iteration   4: 3490448.641 ops/s
Iteration   5: 3487290.249 ops/s

# Run progress: 64.91% complete, ETA 00:10:49
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3321537.325 ops/s
# Warmup Iteration   2: 3568314.133 ops/s
# Warmup Iteration   3: 3552123.801 ops/s
Iteration   1: 3574763.053 ops/s
Iteration   2: 3492220.798 ops/s
Iteration   3: 3568505.945 ops/s
Iteration   4: 3570535.739 ops/s
Iteration   5: 3572961.062 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson2_string":
  3521788.962 ±(99.9%) 45726.982 ops/s [Average]
  (min, avg, max) = (3433992.162, 3521788.962, 3574763.053), stdev = 42773.047
  CI (99.9%): [3476061.980, 3567515.944] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson3_bytes

# Run progress: 65.79% complete, ETA 00:10:32
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3241103.833 ops/s
# Warmup Iteration   2: 3271112.272 ops/s
# Warmup Iteration   3: 3270669.656 ops/s
Iteration   1: 3271225.514 ops/s
Iteration   2: 3280559.681 ops/s
Iteration   3: 3279438.969 ops/s
Iteration   4: 3278927.197 ops/s
Iteration   5: 3280182.563 ops/s

# Run progress: 66.67% complete, ETA 00:10:16
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3242539.347 ops/s
# Warmup Iteration   2: 3278263.611 ops/s
# Warmup Iteration   3: 3278582.651 ops/s
Iteration   1: 3279513.284 ops/s
Iteration   2: 3283462.258 ops/s
Iteration   3: 3286271.570 ops/s
Iteration   4: 3291749.353 ops/s
Iteration   5: 3202444.579 ops/s

# Run progress: 67.54% complete, ETA 00:10:00
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3240096.169 ops/s
# Warmup Iteration   2: 3270376.837 ops/s
# Warmup Iteration   3: 3272033.474 ops/s
Iteration   1: 3268066.105 ops/s
Iteration   2: 3274701.083 ops/s
Iteration   3: 3272866.477 ops/s
Iteration   4: 3278121.537 ops/s
Iteration   5: 3274480.217 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson3_bytes":
  3273467.359 ±(99.9%) 21943.664 ops/s [Average]
  (min, avg, max) = (3202444.579, 3273467.359, 3291749.353), stdev = 20526.117
  CI (99.9%): [3251523.696, 3295411.023] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson3_string

# Run progress: 68.42% complete, ETA 00:09:43
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2541999.348 ops/s
# Warmup Iteration   2: 2585976.408 ops/s
# Warmup Iteration   3: 2603467.340 ops/s
Iteration   1: 2601688.516 ops/s
Iteration   2: 2599814.097 ops/s
Iteration   3: 2604946.593 ops/s
Iteration   4: 2604845.408 ops/s
Iteration   5: 2604953.750 ops/s

# Run progress: 69.30% complete, ETA 00:09:27
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2579933.342 ops/s
# Warmup Iteration   2: 2621358.095 ops/s
# Warmup Iteration   3: 2624014.814 ops/s
Iteration   1: 2623080.080 ops/s
Iteration   2: 2623390.572 ops/s
Iteration   3: 2626805.789 ops/s
Iteration   4: 2626909.621 ops/s
Iteration   5: 2627649.216 ops/s

# Run progress: 70.18% complete, ETA 00:09:11
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2578968.308 ops/s
# Warmup Iteration   2: 2620755.646 ops/s
# Warmup Iteration   3: 2622364.344 ops/s
Iteration   1: 2620763.578 ops/s
Iteration   2: 2620208.386 ops/s
Iteration   3: 2624310.850 ops/s
Iteration   4: 2625520.182 ops/s
Iteration   5: 2625606.719 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayValidateUTF8Bytes.fastjson3_string":
  2617366.224 ±(99.9%) 11342.910 ops/s [Average]
  (min, avg, max) = (2599814.097, 2617366.224, 2627649.216), stdev = 10610.165
  CI (99.9%): [2606023.314, 2628709.134] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson2

# Run progress: 71.05% complete, ETA 00:08:55
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3582497.471 ops/s
# Warmup Iteration   2: 3785451.918 ops/s
# Warmup Iteration   3: 3790482.388 ops/s
Iteration   1: 3781979.006 ops/s
Iteration   2: 3781298.426 ops/s
Iteration   3: 3781801.981 ops/s
Iteration   4: 3780659.437 ops/s
Iteration   5: 3783196.210 ops/s

# Run progress: 71.93% complete, ETA 00:08:39
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3606172.763 ops/s
# Warmup Iteration   2: 3807170.884 ops/s
# Warmup Iteration   3: 3791659.038 ops/s
Iteration   1: 3789676.756 ops/s
Iteration   2: 3793770.359 ops/s
Iteration   3: 3792193.065 ops/s
Iteration   4: 3788291.480 ops/s
Iteration   5: 3785708.968 ops/s

# Run progress: 72.81% complete, ETA 00:08:22
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3420216.991 ops/s
# Warmup Iteration   2: 3607292.293 ops/s
# Warmup Iteration   3: 3616983.632 ops/s
Iteration   1: 3600506.556 ops/s
Iteration   2: 3609015.476 ops/s
Iteration   3: 3614031.301 ops/s
Iteration   4: 3615464.731 ops/s
Iteration   5: 3614453.814 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson2":
  3727469.838 ±(99.9%) 91536.153 ops/s [Average]
  (min, avg, max) = (3600506.556, 3727469.838, 3793770.359), stdev = 85622.974
  CI (99.9%): [3635933.685, 3819005.991] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3

# Run progress: 73.68% complete, ETA 00:08:06
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5371154.082 ops/s
# Warmup Iteration   2: 5516595.254 ops/s
# Warmup Iteration   3: 5521619.522 ops/s
Iteration   1: 5522398.075 ops/s
Iteration   2: 5512691.294 ops/s
Iteration   3: 5520874.377 ops/s
Iteration   4: 5515072.791 ops/s
Iteration   5: 5525350.977 ops/s

# Run progress: 74.56% complete, ETA 00:07:50
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5291626.536 ops/s
# Warmup Iteration   2: 5494182.781 ops/s
# Warmup Iteration   3: 5489811.756 ops/s
Iteration   1: 5491605.461 ops/s
Iteration   2: 5469749.032 ops/s
Iteration   3: 5486067.329 ops/s
Iteration   4: 5485757.450 ops/s
Iteration   5: 5489359.042 ops/s

# Run progress: 75.44% complete, ETA 00:07:34
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5367413.430 ops/s
# Warmup Iteration   2: 5498415.311 ops/s
# Warmup Iteration   3: 5512848.018 ops/s
Iteration   1: 5511693.141 ops/s
Iteration   2: 5485470.157 ops/s
Iteration   3: 5506959.035 ops/s
Iteration   4: 5508600.077 ops/s
Iteration   5: 5513033.333 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3":
  5502978.771 ±(99.9%) 18005.114 ops/s [Average]
  (min, avg, max) = (5469749.032, 5502978.771, 5525350.977), stdev = 16841.995
  CI (99.9%): [5484973.657, 5520983.886] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_asm

# Run progress: 76.32% complete, ETA 00:07:18
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5241931.859 ops/s
# Warmup Iteration   2: 5379367.538 ops/s
# Warmup Iteration   3: 5165345.719 ops/s
Iteration   1: 5354151.140 ops/s
Iteration   2: 5344483.510 ops/s
Iteration   3: 5360582.306 ops/s
Iteration   4: 5357865.900 ops/s
Iteration   5: 5352727.222 ops/s

# Run progress: 77.19% complete, ETA 00:07:01
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5156639.828 ops/s
# Warmup Iteration   2: 5312100.484 ops/s
# Warmup Iteration   3: 5283683.198 ops/s
Iteration   1: 5286706.193 ops/s
Iteration   2: 5274740.223 ops/s
Iteration   3: 5281433.660 ops/s
Iteration   4: 5287282.747 ops/s
Iteration   5: 5276261.791 ops/s

# Run progress: 78.07% complete, ETA 00:06:45
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5273493.595 ops/s
# Warmup Iteration   2: 5522432.886 ops/s
# Warmup Iteration   3: 5436020.973 ops/s
Iteration   1: 5420167.254 ops/s
Iteration   2: 5427578.814 ops/s
Iteration   3: 5391575.060 ops/s
Iteration   4: 5421059.594 ops/s
Iteration   5: 5423249.355 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_asm":
  5350657.651 ±(99.9%) 61975.274 ops/s [Average]
  (min, avg, max) = (5274740.223, 5350657.651, 5427578.814), stdev = 57971.709
  CI (99.9%): [5288682.378, 5412632.925] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_reflect

# Run progress: 78.95% complete, ETA 00:06:29
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4704646.153 ops/s
# Warmup Iteration   2: 4889866.116 ops/s
# Warmup Iteration   3: 4767147.927 ops/s
Iteration   1: 4902194.907 ops/s
Iteration   2: 4893779.953 ops/s
Iteration   3: 4872738.498 ops/s
Iteration   4: 4890705.798 ops/s
Iteration   5: 4895436.532 ops/s

# Run progress: 79.82% complete, ETA 00:06:13
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4685308.855 ops/s
# Warmup Iteration   2: 4900826.703 ops/s
# Warmup Iteration   3: 4889693.888 ops/s
Iteration   1: 4884339.847 ops/s
Iteration   2: 4887349.368 ops/s
Iteration   3: 4884328.345 ops/s
Iteration   4: 4887620.256 ops/s
Iteration   5: 4885067.381 ops/s

# Run progress: 80.70% complete, ETA 00:05:57
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4675908.234 ops/s
# Warmup Iteration   2: 4907651.983 ops/s
# Warmup Iteration   3: 4855317.425 ops/s
Iteration   1: 4855099.104 ops/s
Iteration   2: 4848540.771 ops/s
Iteration   3: 4850513.621 ops/s
Iteration   4: 4856160.364 ops/s
Iteration   5: 4855155.823 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_reflect":
  4876602.038 ±(99.9%) 19702.176 ops/s [Average]
  (min, avg, max) = (4848540.771, 4876602.038, 4902194.907), stdev = 18429.428
  CI (99.9%): [4856899.862, 4896304.214] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_utf8latin1

# Run progress: 81.58% complete, ETA 00:05:40
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5281931.534 ops/s
# Warmup Iteration   2: 5416284.049 ops/s
# Warmup Iteration   3: 5438847.653 ops/s
Iteration   1: 5434275.308 ops/s
Iteration   2: 5450912.133 ops/s
Iteration   3: 5326526.850 ops/s
Iteration   4: 5419758.217 ops/s
Iteration   5: 5451478.132 ops/s

# Run progress: 82.46% complete, ETA 00:05:24
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5293509.335 ops/s
# Warmup Iteration   2: 5536978.633 ops/s
# Warmup Iteration   3: 5511112.952 ops/s
Iteration   1: 5487039.635 ops/s
Iteration   2: 5498407.750 ops/s
Iteration   3: 5499336.363 ops/s
Iteration   4: 5523612.684 ops/s
Iteration   5: 5521559.014 ops/s

# Run progress: 83.33% complete, ETA 00:05:08
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5186388.788 ops/s
# Warmup Iteration   2: 5316643.921 ops/s
# Warmup Iteration   3: 5315327.942 ops/s
Iteration   1: 5253285.482 ops/s
Iteration   2: 5296031.247 ops/s
Iteration   3: 5301434.629 ops/s
Iteration   4: 5304551.024 ops/s
Iteration   5: 5305590.480 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_utf8latin1":
  5404919.930 ±(99.9%) 102660.156 ops/s [Average]
  (min, avg, max) = (5253285.482, 5404919.930, 5523612.684), stdev = 96028.374
  CI (99.9%): [5302259.774, 5507580.086] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.wast

# Run progress: 84.21% complete, ETA 00:04:52
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2410567.499 ops/s
# Warmup Iteration   2: 2743897.514 ops/s
# Warmup Iteration   3: 2744884.644 ops/s
Iteration   1: 2745243.289 ops/s
Iteration   2: 2736710.027 ops/s
Iteration   3: 2687083.924 ops/s
Iteration   4: 2710963.709 ops/s
Iteration   5: 2714110.381 ops/s

# Run progress: 85.09% complete, ETA 00:04:36
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2384160.894 ops/s
# Warmup Iteration   2: 2692667.148 ops/s
# Warmup Iteration   3: 2748626.307 ops/s
Iteration   1: 2745168.377 ops/s
Iteration   2: 2747493.166 ops/s
Iteration   3: 2772308.625 ops/s
Iteration   4: 2745484.204 ops/s
Iteration   5: 2752471.946 ops/s

# Run progress: 85.96% complete, ETA 00:04:19
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2456585.450 ops/s
# Warmup Iteration   2: 2798778.339 ops/s
# Warmup Iteration   3: 2799911.246 ops/s
Iteration   1: 2808281.194 ops/s
Iteration   2: 2799419.495 ops/s
Iteration   3: 2800796.709 ops/s
Iteration   4: 2809728.950 ops/s
Iteration   5: 2812159.841 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.wast":
  2759161.589 ±(99.9%) 42489.563 ops/s [Average]
  (min, avg, max) = (2687083.924, 2759161.589, 2812159.841), stdev = 39744.764
  CI (99.9%): [2716672.026, 2801651.153] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson2

# Run progress: 86.84% complete, ETA 00:04:03
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4642230.402 ops/s
# Warmup Iteration   2: 4868893.742 ops/s
# Warmup Iteration   3: 5044024.482 ops/s
Iteration   1: 5046048.039 ops/s
Iteration   2: 5046277.843 ops/s
Iteration   3: 5048632.620 ops/s
Iteration   4: 5048357.033 ops/s
Iteration   5: 5044587.261 ops/s

# Run progress: 87.72% complete, ETA 00:03:47
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4720980.036 ops/s
# Warmup Iteration   2: 4998384.932 ops/s
# Warmup Iteration   3: 5009427.880 ops/s
Iteration   1: 5008598.414 ops/s
Iteration   2: 5010925.680 ops/s
Iteration   3: 5013053.842 ops/s
Iteration   4: 5012494.093 ops/s
Iteration   5: 5013187.649 ops/s

# Run progress: 88.60% complete, ETA 00:03:31
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4757407.079 ops/s
# Warmup Iteration   2: 5053445.543 ops/s
# Warmup Iteration   3: 5058302.659 ops/s
Iteration   1: 5055705.048 ops/s
Iteration   2: 5051915.454 ops/s
Iteration   3: 5056371.456 ops/s
Iteration   4: 5062840.008 ops/s
Iteration   5: 5049980.736 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson2":
  5037931.679 ±(99.9%) 21166.526 ops/s [Average]
  (min, avg, max) = (5008598.414, 5037931.679, 5062840.008), stdev = 19799.182
  CI (99.9%): [5016765.152, 5059098.205] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3

# Run progress: 89.47% complete, ETA 00:03:14
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5424832.232 ops/s
# Warmup Iteration   2: 5678252.773 ops/s
# Warmup Iteration   3: 5646637.287 ops/s
Iteration   1: 5643159.749 ops/s
Iteration   2: 5621334.306 ops/s
Iteration   3: 5645971.601 ops/s
Iteration   4: 5621051.550 ops/s
Iteration   5: 5635265.957 ops/s

# Run progress: 90.35% complete, ETA 00:02:58
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5363193.416 ops/s
# Warmup Iteration   2: 5589321.555 ops/s
# Warmup Iteration   3: 5574905.155 ops/s
Iteration   1: 5547671.047 ops/s
Iteration   2: 5570511.124 ops/s
Iteration   3: 5564526.332 ops/s
Iteration   4: 5567065.855 ops/s
Iteration   5: 5581141.185 ops/s

# Run progress: 91.23% complete, ETA 00:02:42
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5340122.428 ops/s
# Warmup Iteration   2: 5518536.032 ops/s
# Warmup Iteration   3: 5497899.331 ops/s
Iteration   1: 5539584.774 ops/s
Iteration   2: 5496358.487 ops/s
Iteration   3: 5353183.564 ops/s
Iteration   4: 5420929.423 ops/s
Iteration   5: 5533532.747 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3":
  5556085.847 ±(99.9%) 88199.613 ops/s [Average]
  (min, avg, max) = (5353183.564, 5556085.847, 5645971.601), stdev = 82501.973
  CI (99.9%): [5467886.234, 5644285.460] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_asm

# Run progress: 92.11% complete, ETA 00:02:26
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5155270.655 ops/s
# Warmup Iteration   2: 5291611.352 ops/s
# Warmup Iteration   3: 5276866.254 ops/s
Iteration   1: 5274589.985 ops/s
Iteration   2: 5275157.592 ops/s
Iteration   3: 5093356.031 ops/s
Iteration   4: 5273080.962 ops/s
Iteration   5: 5277930.608 ops/s

# Run progress: 92.98% complete, ETA 00:02:09
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5474206.320 ops/s
# Warmup Iteration   2: 5629022.304 ops/s
# Warmup Iteration   3: 5650431.485 ops/s
Iteration   1: 5643522.776 ops/s
Iteration   2: 5663496.257 ops/s
Iteration   3: 5661703.386 ops/s
Iteration   4: 5662532.164 ops/s
Iteration   5: 5659158.309 ops/s

# Run progress: 93.86% complete, ETA 00:01:53
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5522776.036 ops/s
# Warmup Iteration   2: 5668981.763 ops/s
# Warmup Iteration   3: 5728446.173 ops/s
Iteration   1: 5736653.565 ops/s
Iteration   2: 5695243.056 ops/s
Iteration   3: 5742881.855 ops/s
Iteration   4: 5732232.407 ops/s
Iteration   5: 5738019.032 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_asm":
  5541970.533 ±(99.9%) 244123.736 ops/s [Average]
  (min, avg, max) = (5093356.031, 5541970.533, 5742881.855), stdev = 228353.495
  CI (99.9%): [5297846.796, 5786094.269] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_reflect

# Run progress: 94.74% complete, ETA 00:01:37
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4740364.095 ops/s
# Warmup Iteration   2: 4984672.768 ops/s
# Warmup Iteration   3: 4998870.277 ops/s
Iteration   1: 4992885.088 ops/s
Iteration   2: 4998378.932 ops/s
Iteration   3: 4995999.954 ops/s
Iteration   4: 5003270.228 ops/s
Iteration   5: 5001284.306 ops/s

# Run progress: 95.61% complete, ETA 00:01:21
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4681051.999 ops/s
# Warmup Iteration   2: 4875036.332 ops/s
# Warmup Iteration   3: 4873434.843 ops/s
Iteration   1: 4878298.219 ops/s
Iteration   2: 4868963.990 ops/s
Iteration   3: 4870054.592 ops/s
Iteration   4: 4871721.819 ops/s
Iteration   5: 4877688.480 ops/s

# Run progress: 96.49% complete, ETA 00:01:04
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4787472.127 ops/s
# Warmup Iteration   2: 4986830.390 ops/s
# Warmup Iteration   3: 5059018.553 ops/s
Iteration   1: 5058855.153 ops/s
Iteration   2: 5060427.453 ops/s
Iteration   3: 5060972.566 ops/s
Iteration   4: 5058748.397 ops/s
Iteration   5: 5055363.107 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_reflect":
  4976860.819 ±(99.9%) 85564.334 ops/s [Average]
  (min, avg, max) = (4868963.990, 4976860.819, 5060972.566), stdev = 80036.931
  CI (99.9%): [4891296.485, 5062425.153] (assumes normal distribution)


# JMH version: 1.37
# VM version: JDK 25.0.2, OpenJDK 64-Bit Server VM, 25.0.2+10-LTS
# VM invoker: /root/Install/jdk25/bin/java
# VM options: -XX:-UseCompressedOops --add-modules=jdk.incubator.vector
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 3 iterations, 2 s each
# Measurement: 5 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.wast

# Run progress: 97.37% complete, ETA 00:00:48
# Fork: 1 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2935142.886 ops/s
# Warmup Iteration   2: 3384028.388 ops/s
# Warmup Iteration   3: 3306625.332 ops/s
Iteration   1: 3287626.861 ops/s
Iteration   2: 3284766.316 ops/s
Iteration   3: 3361282.825 ops/s
Iteration   4: 3379178.850 ops/s
Iteration   5: 3380008.176 ops/s

# Run progress: 98.25% complete, ETA 00:00:32
# Fork: 2 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2771342.865 ops/s
# Warmup Iteration   2: 3179654.664 ops/s
# Warmup Iteration   3: 3195553.461 ops/s
Iteration   1: 3223665.882 ops/s
Iteration   2: 3199401.047 ops/s
Iteration   3: 3204069.697 ops/s
Iteration   4: 3213065.515 ops/s
Iteration   5: 3205428.894 ops/s

# Run progress: 99.12% complete, ETA 00:00:16
# Fork: 3 of 3
WARNING: Using incubator modules: jdk.incubator.vector
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/tmp/benchmark3-xplat.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: # wast_json incubator.vector enabled -> true
2905701.983 ops/s
# Warmup Iteration   2: 3381992.428 ops/s
# Warmup Iteration   3: 3388602.626 ops/s
Iteration   1: 3385374.510 ops/s
Iteration   2: 3389885.540 ops/s
Iteration   3: 3385330.783 ops/s
Iteration   4: 3355567.198 ops/s
Iteration   5: 3385800.509 ops/s


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.wast":
  3309363.507 ±(99.9%) 85832.695 ops/s [Average]
  (min, avg, max) = (3199401.047, 3309363.507, 3389885.540), stdev = 80287.956
  CI (99.9%): [3223530.812, 3395196.202] (assumes normal distribution)


# Run complete. Total time: 00:30:52

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

Benchmark                                  Mode  Cnt        Score        Error  Units
EishayParseString.fastjson2               thrpt   15  3396385.082 ±  46069.289  ops/s
EishayParseString.fastjson3               thrpt   15  2523501.838 ±  23204.899  ops/s
EishayParseString.fastjson3_asm           thrpt   15  3633996.961 ±  52595.904  ops/s
EishayParseString.fastjson3_preconv_utf8  thrpt   15  2639025.021 ±  21512.174  ops/s
EishayParseString.fastjson3_str_parser    thrpt   15  2200571.325 ±  18044.901  ops/s
EishayParseString.wast                    thrpt   15  3281268.325 ±  20204.933  ops/s
EishayParseStringPretty.fastjson2         thrpt   15  2627271.243 ±  27163.804  ops/s
EishayParseStringPretty.fastjson3         thrpt   15  2207607.054 ±  56162.372  ops/s
EishayParseStringPretty.wast              thrpt   15  2424790.971 ±  37484.453  ops/s
EishayParseTreeString.fastjson2           thrpt   15  1945230.981 ±   4032.412  ops/s
EishayParseTreeString.fastjson3           thrpt   15  1389851.747 ±  12242.334  ops/s
EishayParseTreeString.wast                thrpt   15  1807927.305 ±  18757.778  ops/s
EishayParseTreeUTF8Bytes.fastjson2        thrpt   15  1926344.229 ±  17597.714  ops/s
EishayParseTreeUTF8Bytes.fastjson3        thrpt   15  1394216.242 ±   1819.092  ops/s
EishayParseTreeUTF8Bytes.wast             thrpt   15  1797781.653 ±  28944.727  ops/s
EishayParseUTF8Bytes.fastjson2            thrpt   15  3381469.624 ±  28406.805  ops/s
EishayParseUTF8Bytes.fastjson3            thrpt   15  2618918.462 ±   7331.032  ops/s
EishayParseUTF8Bytes.fastjson3_asm        thrpt   15  4016795.602 ±  32401.651  ops/s
EishayParseUTF8Bytes.fastjson3_reflect    thrpt   15  2690311.257 ±  11265.497  ops/s
EishayParseUTF8Bytes.wast                 thrpt   15  3240257.825 ±  35490.250  ops/s
EishayParseUTF8BytesPretty.fastjson2      thrpt   15  2614536.401 ±  50346.300  ops/s
EishayParseUTF8BytesPretty.fastjson3      thrpt   15  2328789.026 ±  23212.139  ops/s
EishayParseUTF8BytesPretty.wast           thrpt   15  2428434.334 ±  53271.296  ops/s
EishayValidateUTF8Bytes.fastjson2_bytes   thrpt   15  3526643.166 ±   6516.194  ops/s
EishayValidateUTF8Bytes.fastjson2_string  thrpt   15  3521788.962 ±  45726.982  ops/s
EishayValidateUTF8Bytes.fastjson3_bytes   thrpt   15  3273467.359 ±  21943.664  ops/s
EishayValidateUTF8Bytes.fastjson3_string  thrpt   15  2617366.224 ±  11342.910  ops/s
EishayWriteString.fastjson2               thrpt   15  3727469.838 ±  91536.153  ops/s
EishayWriteString.fastjson3               thrpt   15  5502978.771 ±  18005.114  ops/s
EishayWriteString.fastjson3_asm           thrpt   15  5350657.651 ±  61975.274  ops/s
EishayWriteString.fastjson3_reflect       thrpt   15  4876602.038 ±  19702.176  ops/s
EishayWriteString.fastjson3_utf8latin1    thrpt   15  5404919.930 ± 102660.156  ops/s
EishayWriteString.wast                    thrpt   15  2759161.589 ±  42489.563  ops/s
EishayWriteUTF8Bytes.fastjson2            thrpt   15  5037931.679 ±  21166.526  ops/s
EishayWriteUTF8Bytes.fastjson3            thrpt   15  5556085.847 ±  88199.613  ops/s
EishayWriteUTF8Bytes.fastjson3_asm        thrpt   15  5541970.533 ± 244123.736  ops/s
EishayWriteUTF8Bytes.fastjson3_reflect    thrpt   15  4976860.819 ±  85564.334  ops/s
EishayWriteUTF8Bytes.wast                 thrpt   15  3309363.507 ±  85832.695  ops/s

Benchmark result is saved to /tmp/bench-xplat-root_172_16_172_143.json
```

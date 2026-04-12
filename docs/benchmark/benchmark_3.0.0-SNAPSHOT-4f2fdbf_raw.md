# fastjson3 Benchmark Raw Output — 3.0.0-SNAPSHOT-4f2fdbf

Git commit: `4f2fdbf`  

Raw JMH stdout from each machine. For the human-readable summary see the non-`_raw` companion file.

## aarch64 4c

**JDK**: 25.0.2  
**Arch**: aarch64  
**Cores**: 4  
**OS**: Debian GNU/Linux 12 (bookworm)  

```
=== STARTED 2026-04-12T14:40:01+08:00 ===
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

# Run progress: 0.00% complete, ETA 01:42:40
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6216.372 ops/ms
# Warmup Iteration   2: 6450.294 ops/ms
# Warmup Iteration   3: 6551.985 ops/ms
Iteration   1: 6548.115 ops/ms
Iteration   2: 6560.494 ops/ms
Iteration   3: 6571.297 ops/ms
Iteration   4: 6577.337 ops/ms
Iteration   5: 6576.547 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson2":
  6566.758 ±(99.9%) 47.752 ops/ms [Average]
  (min, avg, max) = (6548.115, 6566.758, 6577.337), stdev = 12.401
  CI (99.9%): [6519.006, 6614.510] (assumes normal distribution)


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

# Run progress: 1.30% complete, ETA 01:42:32
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3856.302 ops/ms
# Warmup Iteration   2: 4013.600 ops/ms
# Warmup Iteration   3: 4033.207 ops/ms
Iteration   1: 4047.039 ops/ms
Iteration   2: 4036.178 ops/ms
Iteration   3: 4031.739 ops/ms
Iteration   4: 4011.616 ops/ms
Iteration   5: 4027.557 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3":
  4030.826 ±(99.9%) 49.911 ops/ms [Average]
  (min, avg, max) = (4011.616, 4030.826, 4047.039), stdev = 12.962
  CI (99.9%): [3980.915, 4080.737] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_preconv_utf8

# Run progress: 2.60% complete, ETA 01:41:10
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6097.235 ops/ms
# Warmup Iteration   2: 6293.150 ops/ms
# Warmup Iteration   3: 6290.456 ops/ms
Iteration   1: 6281.614 ops/ms
Iteration   2: 6293.522 ops/ms
Iteration   3: 6266.131 ops/ms
Iteration   4: 6281.204 ops/ms
Iteration   5: 6297.619 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_preconv_utf8":
  6284.018 ±(99.9%) 47.512 ops/ms [Average]
  (min, avg, max) = (6266.131, 6284.018, 6297.619), stdev = 12.339
  CI (99.9%): [6236.506, 6331.530] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser

# Run progress: 3.90% complete, ETA 01:39:47
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: <failure>

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_thrpt_jmhStub(EishayParseString_fastjson3_str_parser_jmhTest.java:121)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:84)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)




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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.gson

# Run progress: 5.19% complete, ETA 01:13:56
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1407.747 ops/ms
# Warmup Iteration   2: 1475.240 ops/ms
# Warmup Iteration   3: 1468.732 ops/ms
Iteration   1: 1468.713 ops/ms
Iteration   2: 1468.683 ops/ms
Iteration   3: 1470.355 ops/ms
Iteration   4: 1473.342 ops/ms
Iteration   5: 1472.298 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.gson":
  1470.678 ±(99.9%) 8.092 ops/ms [Average]
  (min, avg, max) = (1468.683, 1470.678, 1473.342), stdev = 2.102
  CI (99.9%): [1462.586, 1478.770] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.jackson

# Run progress: 6.49% complete, ETA 01:17:43
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1423.486 ops/ms
# Warmup Iteration   2: 1490.846 ops/ms
# Warmup Iteration   3: 1493.149 ops/ms
Iteration   1: 1491.512 ops/ms
Iteration   2: 1500.102 ops/ms
Iteration   3: 1499.838 ops/ms
Iteration   4: 1496.362 ops/ms
Iteration   5: 1498.330 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.jackson":
  1497.229 ±(99.9%) 13.574 ops/ms [Average]
  (min, avg, max) = (1491.512, 1497.229, 1500.102), stdev = 3.525
  CI (99.9%): [1483.655, 1510.804] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.wast

# Run progress: 7.79% complete, ETA 01:19:48
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4422.079 ops/ms
# Warmup Iteration   2: 4595.650 ops/ms
# Warmup Iteration   3: 4596.401 ops/ms
Iteration   1: 4605.971 ops/ms
Iteration   2: 4610.728 ops/ms
Iteration   3: 4632.661 ops/ms
Iteration   4: 4860.437 ops/ms
Iteration   5: 4865.480 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.wast":
  4715.055 ±(99.9%) 521.388 ops/ms [Average]
  (min, avg, max) = (4605.971, 4715.055, 4865.480), stdev = 135.403
  CI (99.9%): [4193.667, 5236.444] (assumes normal distribution)


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

# Run progress: 9.09% complete, ETA 01:20:54
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4410.819 ops/ms
# Warmup Iteration   2: 4655.246 ops/ms
# Warmup Iteration   3: 4708.995 ops/ms
Iteration   1: 4704.331 ops/ms
Iteration   2: 4680.067 ops/ms
Iteration   3: 4699.744 ops/ms
Iteration   4: 4699.025 ops/ms
Iteration   5: 4700.236 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson2":
  4696.681 ±(99.9%) 36.635 ops/ms [Average]
  (min, avg, max) = (4680.067, 4696.681, 4704.331), stdev = 9.514
  CI (99.9%): [4660.046, 4733.316] (assumes normal distribution)


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

# Run progress: 10.39% complete, ETA 01:21:24
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4004.479 ops/ms
# Warmup Iteration   2: 4181.277 ops/ms
# Warmup Iteration   3: 4184.039 ops/ms
Iteration   1: 4194.290 ops/ms
Iteration   2: 4188.124 ops/ms
Iteration   3: 4190.218 ops/ms
Iteration   4: 4182.566 ops/ms
Iteration   5: 4172.811 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson3":
  4185.602 ±(99.9%) 31.985 ops/ms [Average]
  (min, avg, max) = (4172.811, 4185.602, 4194.290), stdev = 8.307
  CI (99.9%): [4153.616, 4217.587] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.gson

# Run progress: 11.69% complete, ETA 01:21:29
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1296.324 ops/ms
# Warmup Iteration   2: 1347.265 ops/ms
# Warmup Iteration   3: 1349.289 ops/ms
Iteration   1: 1346.000 ops/ms
Iteration   2: 1351.709 ops/ms
Iteration   3: 1350.406 ops/ms
Iteration   4: 1351.042 ops/ms
Iteration   5: 1349.536 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.gson":
  1349.739 ±(99.9%) 8.620 ops/ms [Average]
  (min, avg, max) = (1346.000, 1349.739, 1351.709), stdev = 2.238
  CI (99.9%): [1341.119, 1358.358] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.jackson

# Run progress: 12.99% complete, ETA 01:21:16
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1270.967 ops/ms
# Warmup Iteration   2: 1336.617 ops/ms
# Warmup Iteration   3: 1332.695 ops/ms
Iteration   1: 1334.695 ops/ms
Iteration   2: 1334.782 ops/ms
Iteration   3: 1338.941 ops/ms
Iteration   4: 1337.707 ops/ms
Iteration   5: 1333.301 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.jackson":
  1335.885 ±(99.9%) 9.024 ops/ms [Average]
  (min, avg, max) = (1333.301, 1335.885, 1338.941), stdev = 2.343
  CI (99.9%): [1326.861, 1344.909] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.wast

# Run progress: 14.29% complete, ETA 01:20:51
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3735.118 ops/ms
# Warmup Iteration   2: 4000.302 ops/ms
# Warmup Iteration   3: 4016.285 ops/ms
Iteration   1: 4027.091 ops/ms
Iteration   2: 4030.999 ops/ms
Iteration   3: 4034.342 ops/ms
Iteration   4: 4191.732 ops/ms
Iteration   5: 4190.106 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.wast":
  4094.854 ±(99.9%) 337.833 ops/ms [Average]
  (min, avg, max) = (4027.091, 4094.854, 4191.732), stdev = 87.734
  CI (99.9%): [3757.020, 4432.687] (assumes normal distribution)


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

# Run progress: 15.58% complete, ETA 01:20:17
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3566.263 ops/ms
# Warmup Iteration   2: 3603.700 ops/ms
# Warmup Iteration   3: 3634.531 ops/ms
Iteration   1: 3641.219 ops/ms
Iteration   2: 3622.681 ops/ms
Iteration   3: 3620.437 ops/ms
Iteration   4: 3641.235 ops/ms
Iteration   5: 3630.148 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson2":
  3631.144 ±(99.9%) 38.051 ops/ms [Average]
  (min, avg, max) = (3620.437, 3631.144, 3641.235), stdev = 9.882
  CI (99.9%): [3593.093, 3669.195] (assumes normal distribution)


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

# Run progress: 16.88% complete, ETA 01:19:36
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3332.461 ops/ms
# Warmup Iteration   2: 3390.795 ops/ms
# Warmup Iteration   3: 3400.080 ops/ms
Iteration   1: 3408.514 ops/ms
Iteration   2: 3402.378 ops/ms
Iteration   3: 3415.099 ops/ms
Iteration   4: 3405.713 ops/ms
Iteration   5: 3406.313 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson3":
  3407.603 ±(99.9%) 18.220 ops/ms [Average]
  (min, avg, max) = (3402.378, 3407.603, 3415.099), stdev = 4.732
  CI (99.9%): [3389.383, 3425.824] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.gson

# Run progress: 18.18% complete, ETA 01:18:49
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1208.306 ops/ms
# Warmup Iteration   2: 1258.042 ops/ms
# Warmup Iteration   3: 1261.308 ops/ms
Iteration   1: 1264.490 ops/ms
Iteration   2: 1255.242 ops/ms
Iteration   3: 1254.069 ops/ms
Iteration   4: 1252.567 ops/ms
Iteration   5: 1254.326 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.gson":
  1256.139 ±(99.9%) 18.354 ops/ms [Average]
  (min, avg, max) = (1252.567, 1256.139, 1264.490), stdev = 4.767
  CI (99.9%): [1237.784, 1274.493] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.jackson

# Run progress: 19.48% complete, ETA 01:17:57
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1473.505 ops/ms
# Warmup Iteration   2: 1561.988 ops/ms
# Warmup Iteration   3: 1562.148 ops/ms
Iteration   1: 1563.762 ops/ms
Iteration   2: 1562.892 ops/ms
Iteration   3: 1561.209 ops/ms
Iteration   4: 1563.212 ops/ms
Iteration   5: 1563.300 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.jackson":
  1562.875 ±(99.9%) 3.781 ops/ms [Average]
  (min, avg, max) = (1561.209, 1562.875, 1563.762), stdev = 0.982
  CI (99.9%): [1559.094, 1566.656] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.wast

# Run progress: 20.78% complete, ETA 01:17:02
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3373.137 ops/ms
# Warmup Iteration   2: 3471.330 ops/ms
# Warmup Iteration   3: 3461.883 ops/ms
Iteration   1: 3463.867 ops/ms
Iteration   2: 3466.286 ops/ms
Iteration   3: 3468.877 ops/ms
Iteration   4: 3454.823 ops/ms
Iteration   5: 3454.470 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.wast":
  3461.665 ±(99.9%) 25.600 ops/ms [Average]
  (min, avg, max) = (3454.470, 3461.665, 3468.877), stdev = 6.648
  CI (99.9%): [3436.065, 3487.265] (assumes normal distribution)


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

# Run progress: 22.08% complete, ETA 01:16:04
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3499.347 ops/ms
# Warmup Iteration   2: 3594.618 ops/ms
# Warmup Iteration   3: 3593.029 ops/ms
Iteration   1: 3598.417 ops/ms
Iteration   2: 3592.660 ops/ms
Iteration   3: 3603.981 ops/ms
Iteration   4: 3586.041 ops/ms
Iteration   5: 3595.847 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson2":
  3595.389 ±(99.9%) 25.690 ops/ms [Average]
  (min, avg, max) = (3586.041, 3595.389, 3603.981), stdev = 6.672
  CI (99.9%): [3569.699, 3621.079] (assumes normal distribution)


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

# Run progress: 23.38% complete, ETA 01:15:03
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3347.544 ops/ms
# Warmup Iteration   2: 3455.535 ops/ms
# Warmup Iteration   3: 3452.511 ops/ms
Iteration   1: 3455.518 ops/ms
Iteration   2: 3451.047 ops/ms
Iteration   3: 3449.765 ops/ms
Iteration   4: 3459.756 ops/ms
Iteration   5: 3457.117 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson3":
  3454.640 ±(99.9%) 16.080 ops/ms [Average]
  (min, avg, max) = (3449.765, 3454.640, 3459.756), stdev = 4.176
  CI (99.9%): [3438.560, 3470.720] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.gson

# Run progress: 24.68% complete, ETA 01:14:01
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1222.313 ops/ms
# Warmup Iteration   2: 1262.830 ops/ms
# Warmup Iteration   3: 1261.995 ops/ms
Iteration   1: 1259.865 ops/ms
Iteration   2: 1261.150 ops/ms
Iteration   3: 1264.620 ops/ms
Iteration   4: 1262.376 ops/ms
Iteration   5: 1255.434 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.gson":
  1260.689 ±(99.9%) 13.172 ops/ms [Average]
  (min, avg, max) = (1255.434, 1260.689, 1264.620), stdev = 3.421
  CI (99.9%): [1247.517, 1273.861] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.jackson

# Run progress: 25.97% complete, ETA 01:12:56
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1592.432 ops/ms
# Warmup Iteration   2: 1666.530 ops/ms
# Warmup Iteration   3: 1673.794 ops/ms
Iteration   1: 1665.504 ops/ms
Iteration   2: 1664.395 ops/ms
Iteration   3: 1668.642 ops/ms
Iteration   4: 1673.676 ops/ms
Iteration   5: 1665.787 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.jackson":
  1667.601 ±(99.9%) 14.401 ops/ms [Average]
  (min, avg, max) = (1664.395, 1667.601, 1673.676), stdev = 3.740
  CI (99.9%): [1653.199, 1682.002] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.wast

# Run progress: 27.27% complete, ETA 01:11:50
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3294.800 ops/ms
# Warmup Iteration   2: 3342.240 ops/ms
# Warmup Iteration   3: 3334.107 ops/ms
Iteration   1: 3318.722 ops/ms
Iteration   2: 3323.396 ops/ms
Iteration   3: 3325.206 ops/ms
Iteration   4: 3330.249 ops/ms
Iteration   5: 3313.907 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.wast":
  3322.296 ±(99.9%) 24.054 ops/ms [Average]
  (min, avg, max) = (3313.907, 3322.296, 3330.249), stdev = 6.247
  CI (99.9%): [3298.242, 3346.350] (assumes normal distribution)


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

# Run progress: 28.57% complete, ETA 01:10:43
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6080.462 ops/ms
# Warmup Iteration   2: 6349.206 ops/ms
# Warmup Iteration   3: 6381.625 ops/ms
Iteration   1: 6387.207 ops/ms
Iteration   2: 6361.394 ops/ms
Iteration   3: 6361.356 ops/ms
Iteration   4: 6357.068 ops/ms
Iteration   5: 6374.883 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson2":
  6368.382 ±(99.9%) 48.050 ops/ms [Average]
  (min, avg, max) = (6357.068, 6368.382, 6387.207), stdev = 12.478
  CI (99.9%): [6320.331, 6416.432] (assumes normal distribution)


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

# Run progress: 29.87% complete, ETA 01:09:34
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4356.123 ops/ms
# Warmup Iteration   2: 4519.363 ops/ms
# Warmup Iteration   3: 4553.512 ops/ms
Iteration   1: 4535.350 ops/ms
Iteration   2: 4536.712 ops/ms
Iteration   3: 4528.636 ops/ms
Iteration   4: 4521.436 ops/ms
Iteration   5: 4526.718 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3":
  4529.770 ±(99.9%) 24.307 ops/ms [Average]
  (min, avg, max) = (4521.436, 4529.770, 4536.712), stdev = 6.312
  CI (99.9%): [4505.464, 4554.077] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_asm

# Run progress: 31.17% complete, ETA 01:08:25
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6091.990 ops/ms
# Warmup Iteration   2: 6227.684 ops/ms
# Warmup Iteration   3: 6267.780 ops/ms
Iteration   1: 6252.937 ops/ms
Iteration   2: 6285.266 ops/ms
Iteration   3: 6288.414 ops/ms
Iteration   4: 6279.548 ops/ms
Iteration   5: 6277.274 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_asm":
  6276.688 ±(99.9%) 53.899 ops/ms [Average]
  (min, avg, max) = (6252.937, 6276.688, 6288.414), stdev = 13.997
  CI (99.9%): [6222.789, 6330.586] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_reflect

# Run progress: 32.47% complete, ETA 01:07:14
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6320.903 ops/ms
# Warmup Iteration   2: 6447.806 ops/ms
# Warmup Iteration   3: 6524.583 ops/ms
Iteration   1: 6504.535 ops/ms
Iteration   2: 6490.710 ops/ms
Iteration   3: 6490.610 ops/ms
Iteration   4: 6478.480 ops/ms
Iteration   5: 6501.066 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_reflect":
  6493.080 ±(99.9%) 39.452 ops/ms [Average]
  (min, avg, max) = (6478.480, 6493.080, 6504.535), stdev = 10.245
  CI (99.9%): [6453.629, 6532.532] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.gson

# Run progress: 33.77% complete, ETA 01:06:03
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1378.915 ops/ms
# Warmup Iteration   2: 1443.217 ops/ms
# Warmup Iteration   3: 1453.516 ops/ms
Iteration   1: 1448.171 ops/ms
Iteration   2: 1449.252 ops/ms
Iteration   3: 1451.899 ops/ms
Iteration   4: 1450.959 ops/ms
Iteration   5: 1451.693 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.gson":
  1450.395 ±(99.9%) 6.244 ops/ms [Average]
  (min, avg, max) = (1448.171, 1450.395, 1451.899), stdev = 1.621
  CI (99.9%): [1444.151, 1456.639] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.jackson

# Run progress: 35.06% complete, ETA 01:04:51
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1492.121 ops/ms
# Warmup Iteration   2: 1560.093 ops/ms
# Warmup Iteration   3: 1557.173 ops/ms
Iteration   1: 1559.994 ops/ms
Iteration   2: 1562.368 ops/ms
Iteration   3: 1562.377 ops/ms
Iteration   4: 1560.525 ops/ms
Iteration   5: 1560.461 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.jackson":
  1561.145 ±(99.9%) 4.386 ops/ms [Average]
  (min, avg, max) = (1559.994, 1561.145, 1562.377), stdev = 1.139
  CI (99.9%): [1556.759, 1565.531] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.wast

# Run progress: 36.36% complete, ETA 01:03:38
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4312.114 ops/ms
# Warmup Iteration   2: 4471.308 ops/ms
# Warmup Iteration   3: 4529.641 ops/ms
Iteration   1: 4509.476 ops/ms
Iteration   2: 4516.180 ops/ms
Iteration   3: 4516.219 ops/ms
Iteration   4: 4729.486 ops/ms
Iteration   5: 4738.109 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.wast":
  4601.894 ±(99.9%) 463.928 ops/ms [Average]
  (min, avg, max) = (4509.476, 4601.894, 4738.109), stdev = 120.481
  CI (99.9%): [4137.966, 5065.822] (assumes normal distribution)


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

# Run progress: 37.66% complete, ETA 01:02:25
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4312.895 ops/ms
# Warmup Iteration   2: 4604.216 ops/ms
# Warmup Iteration   3: 4626.616 ops/ms
Iteration   1: 4617.453 ops/ms
Iteration   2: 4615.109 ops/ms
Iteration   3: 4613.670 ops/ms
Iteration   4: 4607.003 ops/ms
Iteration   5: 4635.845 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson2":
  4617.816 ±(99.9%) 41.589 ops/ms [Average]
  (min, avg, max) = (4607.003, 4617.816, 4635.845), stdev = 10.801
  CI (99.9%): [4576.227, 4659.405] (assumes normal distribution)


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

# Run progress: 38.96% complete, ETA 01:01:11
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3758.496 ops/ms
# Warmup Iteration   2: 3855.968 ops/ms
# Warmup Iteration   3: 3843.849 ops/ms
Iteration   1: 3833.755 ops/ms
Iteration   2: 3840.801 ops/ms
Iteration   3: 3848.593 ops/ms
Iteration   4: 3850.771 ops/ms
Iteration   5: 3836.535 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson3":
  3842.091 ±(99.9%) 28.533 ops/ms [Average]
  (min, avg, max) = (3833.755, 3842.091, 3850.771), stdev = 7.410
  CI (99.9%): [3813.558, 3870.624] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.gson

# Run progress: 40.26% complete, ETA 00:59:57
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1248.843 ops/ms
# Warmup Iteration   2: 1309.705 ops/ms
# Warmup Iteration   3: 1310.931 ops/ms
Iteration   1: 1308.368 ops/ms
Iteration   2: 1308.518 ops/ms
Iteration   3: 1310.257 ops/ms
Iteration   4: 1308.347 ops/ms
Iteration   5: 1306.751 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.gson":
  1308.448 ±(99.9%) 4.784 ops/ms [Average]
  (min, avg, max) = (1306.751, 1308.448, 1310.257), stdev = 1.242
  CI (99.9%): [1303.664, 1313.232] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.jackson

# Run progress: 41.56% complete, ETA 00:58:42
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1418.667 ops/ms
# Warmup Iteration   2: 1501.098 ops/ms
# Warmup Iteration   3: 1501.109 ops/ms
Iteration   1: 1499.875 ops/ms
Iteration   2: 1500.426 ops/ms
Iteration   3: 1499.623 ops/ms
Iteration   4: 1499.583 ops/ms
Iteration   5: 1500.178 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.jackson":
  1499.937 ±(99.9%) 1.395 ops/ms [Average]
  (min, avg, max) = (1499.583, 1499.937, 1500.426), stdev = 0.362
  CI (99.9%): [1498.542, 1501.332] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.wast

# Run progress: 42.86% complete, ETA 00:57:27
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3676.462 ops/ms
# Warmup Iteration   2: 3849.219 ops/ms
# Warmup Iteration   3: 3852.594 ops/ms
Iteration   1: 3857.313 ops/ms
Iteration   2: 3849.200 ops/ms
Iteration   3: 3871.949 ops/ms
Iteration   4: 4003.885 ops/ms
Iteration   5: 4002.824 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.wast":
  3917.034 ±(99.9%) 305.051 ops/ms [Average]
  (min, avg, max) = (3849.200, 3917.034, 4003.885), stdev = 79.221
  CI (99.9%): [3611.983, 4222.085] (assumes normal distribution)


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

# Run progress: 44.16% complete, ETA 00:56:12
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6482.830 ops/ms
# Warmup Iteration   2: 6743.116 ops/ms
# Warmup Iteration   3: 6758.237 ops/ms
Iteration   1: 6766.432 ops/ms
Iteration   2: 6742.955 ops/ms
Iteration   3: 6752.120 ops/ms
Iteration   4: 6745.036 ops/ms
Iteration   5: 6740.781 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson2":
  6749.465 ±(99.9%) 40.033 ops/ms [Average]
  (min, avg, max) = (6740.781, 6749.465, 6766.432), stdev = 10.396
  CI (99.9%): [6709.432, 6789.498] (assumes normal distribution)


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

# Run progress: 45.45% complete, ETA 00:54:57
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6294.270 ops/ms
# Warmup Iteration   2: 6443.578 ops/ms
# Warmup Iteration   3: 6475.428 ops/ms
Iteration   1: 6462.001 ops/ms
Iteration   2: 6465.569 ops/ms
Iteration   3: 6477.286 ops/ms
Iteration   4: 6456.579 ops/ms
Iteration   5: 6442.789 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3":
  6460.845 ±(99.9%) 48.637 ops/ms [Average]
  (min, avg, max) = (6442.789, 6460.845, 6477.286), stdev = 12.631
  CI (99.9%): [6412.207, 6509.482] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_asm

# Run progress: 46.75% complete, ETA 00:53:41
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4554.598 ops/ms
# Warmup Iteration   2: 4596.069 ops/ms
# Warmup Iteration   3: 4650.741 ops/ms
Iteration   1: 4652.308 ops/ms
Iteration   2: 4656.290 ops/ms
Iteration   3: 4659.393 ops/ms
Iteration   4: 4664.869 ops/ms
Iteration   5: 4652.480 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_asm":
  4657.068 ±(99.9%) 20.246 ops/ms [Average]
  (min, avg, max) = (4652.308, 4657.068, 4664.869), stdev = 5.258
  CI (99.9%): [4636.822, 4677.314] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_reflect

# Run progress: 48.05% complete, ETA 00:52:25
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4837.690 ops/ms
# Warmup Iteration   2: 4939.827 ops/ms
# Warmup Iteration   3: 4998.140 ops/ms
Iteration   1: 5018.033 ops/ms
Iteration   2: 5005.991 ops/ms
Iteration   3: 5017.613 ops/ms
Iteration   4: 5006.369 ops/ms
Iteration   5: 5013.626 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_reflect":
  5012.326 ±(99.9%) 22.603 ops/ms [Average]
  (min, avg, max) = (5005.991, 5012.326, 5018.033), stdev = 5.870
  CI (99.9%): [4989.723, 5034.930] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_utf8latin1

# Run progress: 49.35% complete, ETA 00:51:09
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6452.004 ops/ms
# Warmup Iteration   2: 6630.511 ops/ms
# Warmup Iteration   3: 6540.174 ops/ms
Iteration   1: 6518.346 ops/ms
Iteration   2: 6554.378 ops/ms
Iteration   3: 6545.892 ops/ms
Iteration   4: 6547.528 ops/ms
Iteration   5: 6530.808 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_utf8latin1":
  6539.390 ±(99.9%) 56.121 ops/ms [Average]
  (min, avg, max) = (6518.346, 6539.390, 6554.378), stdev = 14.574
  CI (99.9%): [6483.269, 6595.511] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.gson

# Run progress: 50.65% complete, ETA 00:49:52
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 848.290 ops/ms
# Warmup Iteration   2: 905.915 ops/ms
# Warmup Iteration   3: 906.682 ops/ms
Iteration   1: 906.122 ops/ms
Iteration   2: 907.199 ops/ms
Iteration   3: 908.167 ops/ms
Iteration   4: 907.793 ops/ms
Iteration   5: 906.541 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.gson":
  907.164 ±(99.9%) 3.265 ops/ms [Average]
  (min, avg, max) = (906.122, 907.164, 908.167), stdev = 0.848
  CI (99.9%): [903.899, 910.430] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.jackson

# Run progress: 51.95% complete, ETA 00:48:35
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2562.486 ops/ms
# Warmup Iteration   2: 2679.967 ops/ms
# Warmup Iteration   3: 2707.813 ops/ms
Iteration   1: 2703.953 ops/ms
Iteration   2: 2701.508 ops/ms
Iteration   3: 2705.735 ops/ms
Iteration   4: 2700.411 ops/ms
Iteration   5: 2693.813 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.jackson":
  2701.084 ±(99.9%) 17.578 ops/ms [Average]
  (min, avg, max) = (2693.813, 2701.084, 2705.735), stdev = 4.565
  CI (99.9%): [2683.506, 2718.662] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.wast

# Run progress: 53.25% complete, ETA 00:47:18
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4567.973 ops/ms
# Warmup Iteration   2: 4680.289 ops/ms
# Warmup Iteration   3: 4679.865 ops/ms
Iteration   1: 4680.463 ops/ms
Iteration   2: 4688.951 ops/ms
Iteration   3: 4729.519 ops/ms
Iteration   4: 5368.144 ops/ms
Iteration   5: 5370.357 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.wast":
  4967.487 ±(99.9%) 1414.063 ops/ms [Average]
  (min, avg, max) = (4680.463, 4967.487, 5370.357), stdev = 367.228
  CI (99.9%): [3553.424, 6381.549] (assumes normal distribution)


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

# Run progress: 54.55% complete, ETA 00:46:01
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7631.475 ops/ms
# Warmup Iteration   2: 7943.072 ops/ms
# Warmup Iteration   3: 7978.638 ops/ms
Iteration   1: 7958.986 ops/ms
Iteration   2: 7993.866 ops/ms
Iteration   3: 7994.361 ops/ms
Iteration   4: 7960.006 ops/ms
Iteration   5: 7964.079 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson2":
  7974.260 ±(99.9%) 70.177 ops/ms [Average]
  (min, avg, max) = (7958.986, 7974.260, 7994.361), stdev = 18.225
  CI (99.9%): [7904.082, 8044.437] (assumes normal distribution)


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

# Run progress: 55.84% complete, ETA 00:44:44
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6481.721 ops/ms
# Warmup Iteration   2: 6549.506 ops/ms
# Warmup Iteration   3: 6565.429 ops/ms
Iteration   1: 6568.502 ops/ms
Iteration   2: 6562.714 ops/ms
Iteration   3: 6545.970 ops/ms
Iteration   4: 6557.138 ops/ms
Iteration   5: 6551.168 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3":
  6557.098 ±(99.9%) 34.472 ops/ms [Average]
  (min, avg, max) = (6545.970, 6557.098, 6568.502), stdev = 8.952
  CI (99.9%): [6522.626, 6591.570] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_asm

# Run progress: 57.14% complete, ETA 00:43:26
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6420.013 ops/ms
# Warmup Iteration   2: 6548.107 ops/ms
# Warmup Iteration   3: 6582.293 ops/ms
Iteration   1: 6555.853 ops/ms
Iteration   2: 6586.891 ops/ms
Iteration   3: 6561.193 ops/ms
Iteration   4: 6567.068 ops/ms
Iteration   5: 6631.418 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_asm":
  6580.485 ±(99.9%) 118.594 ops/ms [Average]
  (min, avg, max) = (6555.853, 6580.485, 6631.418), stdev = 30.798
  CI (99.9%): [6461.891, 6699.078] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_reflect

# Run progress: 58.44% complete, ETA 00:42:09
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6448.427 ops/ms
# Warmup Iteration   2: 6603.251 ops/ms
# Warmup Iteration   3: 6656.770 ops/ms
Iteration   1: 6659.848 ops/ms
Iteration   2: 6659.776 ops/ms
Iteration   3: 6654.434 ops/ms
Iteration   4: 6652.420 ops/ms
Iteration   5: 6661.207 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_reflect":
  6657.537 ±(99.9%) 14.869 ops/ms [Average]
  (min, avg, max) = (6652.420, 6657.537, 6661.207), stdev = 3.861
  CI (99.9%): [6642.668, 6672.406] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.gson

# Run progress: 59.74% complete, ETA 00:40:51
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 844.009 ops/ms
# Warmup Iteration   2: 893.125 ops/ms
# Warmup Iteration   3: 892.981 ops/ms
Iteration   1: 892.403 ops/ms
Iteration   2: 892.884 ops/ms
Iteration   3: 892.878 ops/ms
Iteration   4: 894.985 ops/ms
Iteration   5: 892.495 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.gson":
  893.129 ±(99.9%) 4.082 ops/ms [Average]
  (min, avg, max) = (892.403, 893.129, 894.985), stdev = 1.060
  CI (99.9%): [889.047, 897.211] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.jackson

# Run progress: 61.04% complete, ETA 00:39:33
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2573.104 ops/ms
# Warmup Iteration   2: 2689.643 ops/ms
# Warmup Iteration   3: 2700.898 ops/ms
Iteration   1: 2697.319 ops/ms
Iteration   2: 2693.793 ops/ms
Iteration   3: 2702.039 ops/ms
Iteration   4: 2700.102 ops/ms
Iteration   5: 2700.547 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.jackson":
  2698.760 ±(99.9%) 12.550 ops/ms [Average]
  (min, avg, max) = (2693.793, 2698.760, 2702.039), stdev = 3.259
  CI (99.9%): [2686.210, 2711.310] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.wast

# Run progress: 62.34% complete, ETA 00:38:15
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5277.356 ops/ms
# Warmup Iteration   2: 5304.959 ops/ms
# Warmup Iteration   3: 5338.955 ops/ms
Iteration   1: 5343.244 ops/ms
Iteration   2: 5347.731 ops/ms
Iteration   3: 5393.396 ops/ms
Iteration   4: 6082.730 ops/ms
Iteration   5: 6066.073 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.wast":
  5646.635 ±(99.9%) 1505.728 ops/ms [Average]
  (min, avg, max) = (5343.244, 5646.635, 6082.730), stdev = 391.033
  CI (99.9%): [4140.907, 7152.363] (assumes normal distribution)


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

# Run progress: 63.64% complete, ETA 00:36:57
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1196.425 ops/ms
# Warmup Iteration   2: 1254.320 ops/ms
# Warmup Iteration   3: 1258.644 ops/ms
Iteration   1: 1247.857 ops/ms
Iteration   2: 1250.785 ops/ms
Iteration   3: 1251.908 ops/ms
Iteration   4: 1251.533 ops/ms
Iteration   5: 1253.748 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson2":
  1251.166 ±(99.9%) 8.271 ops/ms [Average]
  (min, avg, max) = (1247.857, 1251.166, 1253.748), stdev = 2.148
  CI (99.9%): [1242.896, 1259.437] (assumes normal distribution)


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

# Run progress: 64.94% complete, ETA 00:35:39
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1492.775 ops/ms
# Warmup Iteration   2: 1538.695 ops/ms
# Warmup Iteration   3: 1555.130 ops/ms
Iteration   1: 1558.930 ops/ms
Iteration   2: 1537.959 ops/ms
Iteration   3: 1538.083 ops/ms
Iteration   4: 1538.100 ops/ms
Iteration   5: 1531.990 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3":
  1541.012 ±(99.9%) 39.869 ops/ms [Average]
  (min, avg, max) = (1531.990, 1541.012, 1558.930), stdev = 10.354
  CI (99.9%): [1501.143, 1580.882] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3_asm

# Run progress: 66.23% complete, ETA 00:34:20
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1482.947 ops/ms
# Warmup Iteration   2: 1512.588 ops/ms
# Warmup Iteration   3: 1514.051 ops/ms
Iteration   1: 1514.888 ops/ms
Iteration   2: 1550.285 ops/ms
Iteration   3: 1529.688 ops/ms
Iteration   4: 1514.862 ops/ms
Iteration   5: 1521.522 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3_asm":
  1526.249 ±(99.9%) 56.809 ops/ms [Average]
  (min, avg, max) = (1514.862, 1526.249, 1550.285), stdev = 14.753
  CI (99.9%): [1469.440, 1583.058] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3_reflect

# Run progress: 67.53% complete, ETA 00:33:02
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1455.550 ops/ms
# Warmup Iteration   2: 1499.458 ops/ms
# Warmup Iteration   3: 1495.873 ops/ms
Iteration   1: 1502.175 ops/ms
Iteration   2: 1501.861 ops/ms
Iteration   3: 1511.299 ops/ms
Iteration   4: 1527.057 ops/ms
Iteration   5: 1507.040 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3_reflect":
  1509.886 ±(99.9%) 39.868 ops/ms [Average]
  (min, avg, max) = (1501.861, 1509.886, 1527.057), stdev = 10.354
  CI (99.9%): [1470.018, 1549.754] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.gson

# Run progress: 68.83% complete, ETA 00:31:43
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 374.640 ops/ms
# Warmup Iteration   2: 402.071 ops/ms
# Warmup Iteration   3: 401.922 ops/ms
Iteration   1: 402.106 ops/ms
Iteration   2: 402.558 ops/ms
Iteration   3: 402.921 ops/ms
Iteration   4: 402.015 ops/ms
Iteration   5: 402.417 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.gson":
  402.403 ±(99.9%) 1.402 ops/ms [Average]
  (min, avg, max) = (402.015, 402.403, 402.921), stdev = 0.364
  CI (99.9%): [401.001, 403.806] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.jackson

# Run progress: 70.13% complete, ETA 00:30:24
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 443.767 ops/ms
# Warmup Iteration   2: 470.115 ops/ms
# Warmup Iteration   3: 470.828 ops/ms
Iteration   1: 470.810 ops/ms
Iteration   2: 470.667 ops/ms
Iteration   3: 471.240 ops/ms
Iteration   4: 471.304 ops/ms
Iteration   5: 470.740 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.jackson":
  470.952 ±(99.9%) 1.143 ops/ms [Average]
  (min, avg, max) = (470.667, 470.952, 471.304), stdev = 0.297
  CI (99.9%): [469.809, 472.096] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.wast

# Run progress: 71.43% complete, ETA 00:29:06
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1260.259 ops/ms
# Warmup Iteration   2: 1325.624 ops/ms
# Warmup Iteration   3: 1330.060 ops/ms
Iteration   1: 1325.109 ops/ms
Iteration   2: 1325.824 ops/ms
Iteration   3: 1327.584 ops/ms
Iteration   4: 1351.297 ops/ms
Iteration   5: 1355.065 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.wast":
  1336.976 ±(99.9%) 57.299 ops/ms [Average]
  (min, avg, max) = (1325.109, 1336.976, 1355.065), stdev = 14.880
  CI (99.9%): [1279.676, 1394.275] (assumes normal distribution)


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

# Run progress: 72.73% complete, ETA 00:27:47
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1944.554 ops/ms
# Warmup Iteration   2: 2017.233 ops/ms
# Warmup Iteration   3: 2018.758 ops/ms
Iteration   1: 2021.230 ops/ms
Iteration   2: 2018.974 ops/ms
Iteration   3: 2018.122 ops/ms
Iteration   4: 2019.183 ops/ms
Iteration   5: 2019.858 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson2":
  2019.474 ±(99.9%) 4.472 ops/ms [Average]
  (min, avg, max) = (2018.122, 2019.474, 2021.230), stdev = 1.161
  CI (99.9%): [2015.002, 2023.946] (assumes normal distribution)


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

# Run progress: 74.03% complete, ETA 00:26:28
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2011.221 ops/ms
# Warmup Iteration   2: 2055.025 ops/ms
# Warmup Iteration   3: 2064.499 ops/ms
Iteration   1: 2055.152 ops/ms
Iteration   2: 2057.998 ops/ms
Iteration   3: 2052.342 ops/ms
Iteration   4: 2056.695 ops/ms
Iteration   5: 2056.832 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3":
  2055.804 ±(99.9%) 8.409 ops/ms [Average]
  (min, avg, max) = (2052.342, 2055.804, 2057.998), stdev = 2.184
  CI (99.9%): [2047.395, 2064.212] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3_asm

# Run progress: 75.32% complete, ETA 00:25:09
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1961.193 ops/ms
# Warmup Iteration   2: 1999.171 ops/ms
# Warmup Iteration   3: 2012.904 ops/ms
Iteration   1: 2008.877 ops/ms
Iteration   2: 2010.713 ops/ms
Iteration   3: 2010.334 ops/ms
Iteration   4: 2011.145 ops/ms
Iteration   5: 2012.884 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3_asm":
  2010.791 ±(99.9%) 5.576 ops/ms [Average]
  (min, avg, max) = (2008.877, 2010.791, 2012.884), stdev = 1.448
  CI (99.9%): [2005.215, 2016.367] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect

# Run progress: 76.62% complete, ETA 00:23:50
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2017.269 ops/ms
# Warmup Iteration   2: 2007.712 ops/ms
# Warmup Iteration   3: 2059.032 ops/ms
Iteration   1: 2055.716 ops/ms
Iteration   2: 2067.541 ops/ms
Iteration   3: 2063.093 ops/ms
Iteration   4: 2064.075 ops/ms
Iteration   5: 2059.753 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect":
  2062.035 ±(99.9%) 17.299 ops/ms [Average]
  (min, avg, max) = (2055.716, 2062.035, 2067.541), stdev = 4.493
  CI (99.9%): [2044.736, 2079.335] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.gson

# Run progress: 77.92% complete, ETA 00:22:31
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 242.312 ops/ms
# Warmup Iteration   2: 253.817 ops/ms
# Warmup Iteration   3: 253.907 ops/ms
Iteration   1: 253.869 ops/ms
Iteration   2: 254.254 ops/ms
Iteration   3: 254.209 ops/ms
Iteration   4: 254.601 ops/ms
Iteration   5: 254.242 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.gson":
  254.235 ±(99.9%) 1.000 ops/ms [Average]
  (min, avg, max) = (253.869, 254.235, 254.601), stdev = 0.260
  CI (99.9%): [253.235, 255.234] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.jackson

# Run progress: 79.22% complete, ETA 00:21:12
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 694.517 ops/ms
# Warmup Iteration   2: 738.764 ops/ms
# Warmup Iteration   3: 740.594 ops/ms
Iteration   1: 739.655 ops/ms
Iteration   2: 739.258 ops/ms
Iteration   3: 737.914 ops/ms
Iteration   4: 739.851 ops/ms
Iteration   5: 737.819 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.jackson":
  738.899 ±(99.9%) 3.725 ops/ms [Average]
  (min, avg, max) = (737.819, 738.899, 739.851), stdev = 0.967
  CI (99.9%): [735.174, 742.624] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.wast

# Run progress: 80.52% complete, ETA 00:19:53
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1752.656 ops/ms
# Warmup Iteration   2: 1797.821 ops/ms
# Warmup Iteration   3: 1806.588 ops/ms
Iteration   1: 1800.704 ops/ms
Iteration   2: 1799.566 ops/ms
Iteration   3: 1805.109 ops/ms
Iteration   4: 1846.633 ops/ms
Iteration   5: 1843.689 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.wast":
  1819.140 ±(99.9%) 91.902 ops/ms [Average]
  (min, avg, max) = (1799.566, 1819.140, 1846.633), stdev = 23.867
  CI (99.9%): [1727.238, 1911.042] (assumes normal distribution)


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

# Run progress: 81.82% complete, ETA 00:18:33
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1350.516 ops/ms
# Warmup Iteration   2: 1419.466 ops/ms
# Warmup Iteration   3: 1415.698 ops/ms
Iteration   1: 1418.778 ops/ms
Iteration   2: 1429.658 ops/ms
Iteration   3: 1424.319 ops/ms
Iteration   4: 1421.155 ops/ms
Iteration   5: 1425.505 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson2":
  1423.883 ±(99.9%) 16.071 ops/ms [Average]
  (min, avg, max) = (1418.778, 1423.883, 1429.658), stdev = 4.174
  CI (99.9%): [1407.812, 1439.954] (assumes normal distribution)


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

# Run progress: 83.12% complete, ETA 00:17:14
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1328.107 ops/ms
# Warmup Iteration   2: 1344.258 ops/ms
# Warmup Iteration   3: 1340.619 ops/ms
Iteration   1: 1339.675 ops/ms
Iteration   2: 1342.969 ops/ms
Iteration   3: 1339.060 ops/ms
Iteration   4: 1345.503 ops/ms
Iteration   5: 1344.874 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3":
  1342.416 ±(99.9%) 11.333 ops/ms [Average]
  (min, avg, max) = (1339.060, 1342.416, 1345.503), stdev = 2.943
  CI (99.9%): [1331.083, 1353.749] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3_asm

# Run progress: 84.42% complete, ETA 00:15:55
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1304.850 ops/ms
# Warmup Iteration   2: 1334.139 ops/ms
# Warmup Iteration   3: 1330.167 ops/ms
Iteration   1: 1334.963 ops/ms
Iteration   2: 1338.039 ops/ms
Iteration   3: 1337.366 ops/ms
Iteration   4: 1338.688 ops/ms
Iteration   5: 1338.687 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3_asm":
  1337.548 ±(99.9%) 5.951 ops/ms [Average]
  (min, avg, max) = (1334.963, 1337.548, 1338.688), stdev = 1.545
  CI (99.9%): [1331.597, 1343.499] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3_reflect

# Run progress: 85.71% complete, ETA 00:14:35
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1317.699 ops/ms
# Warmup Iteration   2: 1350.676 ops/ms
# Warmup Iteration   3: 1345.817 ops/ms
Iteration   1: 1345.179 ops/ms
Iteration   2: 1350.486 ops/ms
Iteration   3: 1345.528 ops/ms
Iteration   4: 1352.218 ops/ms
Iteration   5: 1351.986 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3_reflect":
  1349.080 ±(99.9%) 13.353 ops/ms [Average]
  (min, avg, max) = (1345.179, 1349.080, 1352.218), stdev = 3.468
  CI (99.9%): [1335.726, 1362.433] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.gson

# Run progress: 87.01% complete, ETA 00:13:16
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 389.819 ops/ms
# Warmup Iteration   2: 423.142 ops/ms
# Warmup Iteration   3: 424.930 ops/ms
Iteration   1: 424.949 ops/ms
Iteration   2: 425.017 ops/ms
Iteration   3: 425.963 ops/ms
Iteration   4: 424.586 ops/ms
Iteration   5: 424.492 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.gson":
  425.001 ±(99.9%) 2.246 ops/ms [Average]
  (min, avg, max) = (424.492, 425.001, 425.963), stdev = 0.583
  CI (99.9%): [422.756, 427.247] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.jackson

# Run progress: 88.31% complete, ETA 00:11:56
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 426.098 ops/ms
# Warmup Iteration   2: 455.626 ops/ms
# Warmup Iteration   3: 457.248 ops/ms
Iteration   1: 457.169 ops/ms
Iteration   2: 457.573 ops/ms
Iteration   3: 457.609 ops/ms
Iteration   4: 457.397 ops/ms
Iteration   5: 456.897 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.jackson":
  457.329 ±(99.9%) 1.146 ops/ms [Average]
  (min, avg, max) = (456.897, 457.329, 457.609), stdev = 0.298
  CI (99.9%): [456.183, 458.475] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.wast

# Run progress: 89.61% complete, ETA 00:10:37
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1240.226 ops/ms
# Warmup Iteration   2: 1309.358 ops/ms
# Warmup Iteration   3: 1316.600 ops/ms
Iteration   1: 1314.799 ops/ms
Iteration   2: 1314.076 ops/ms
Iteration   3: 1309.604 ops/ms
Iteration   4: 1328.214 ops/ms
Iteration   5: 1330.384 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.wast":
  1319.415 ±(99.9%) 35.700 ops/ms [Average]
  (min, avg, max) = (1309.604, 1319.415, 1330.384), stdev = 9.271
  CI (99.9%): [1283.715, 1355.116] (assumes normal distribution)


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

# Run progress: 90.91% complete, ETA 00:09:17
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1891.238 ops/ms
# Warmup Iteration   2: 1993.628 ops/ms
# Warmup Iteration   3: 1994.210 ops/ms
Iteration   1: 1997.331 ops/ms
Iteration   2: 2001.303 ops/ms
Iteration   3: 1999.769 ops/ms
Iteration   4: 1997.643 ops/ms
Iteration   5: 1998.641 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson2":
  1998.937 ±(99.9%) 6.278 ops/ms [Average]
  (min, avg, max) = (1997.331, 1998.937, 2001.303), stdev = 1.630
  CI (99.9%): [1992.660, 2005.215] (assumes normal distribution)


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

# Run progress: 92.21% complete, ETA 00:07:58
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2127.717 ops/ms
# Warmup Iteration   2: 2176.828 ops/ms
# Warmup Iteration   3: 2177.986 ops/ms
Iteration   1: 2188.460 ops/ms
Iteration   2: 2191.489 ops/ms
Iteration   3: 2192.437 ops/ms
Iteration   4: 2186.467 ops/ms
Iteration   5: 2190.000 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3":
  2189.771 ±(99.9%) 9.183 ops/ms [Average]
  (min, avg, max) = (2186.467, 2189.771, 2192.437), stdev = 2.385
  CI (99.9%): [2180.588, 2198.953] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3_asm

# Run progress: 93.51% complete, ETA 00:06:38
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2132.728 ops/ms
# Warmup Iteration   2: 2125.132 ops/ms
# Warmup Iteration   3: 2191.426 ops/ms
Iteration   1: 2196.268 ops/ms
Iteration   2: 2193.691 ops/ms
Iteration   3: 2193.992 ops/ms
Iteration   4: 2188.145 ops/ms
Iteration   5: 2182.630 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3_asm":
  2190.945 ±(99.9%) 21.279 ops/ms [Average]
  (min, avg, max) = (2182.630, 2190.945, 2196.268), stdev = 5.526
  CI (99.9%): [2169.666, 2212.224] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3_reflect

# Run progress: 94.81% complete, ETA 00:05:18
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2134.900 ops/ms
# Warmup Iteration   2: 2181.757 ops/ms
# Warmup Iteration   3: 2205.041 ops/ms
Iteration   1: 2201.104 ops/ms
Iteration   2: 2190.675 ops/ms
Iteration   3: 2189.530 ops/ms
Iteration   4: 2190.133 ops/ms
Iteration   5: 2189.527 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3_reflect":
  2192.194 ±(99.9%) 19.269 ops/ms [Average]
  (min, avg, max) = (2189.527, 2192.194, 2201.104), stdev = 5.004
  CI (99.9%): [2172.925, 2211.463] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.gson

# Run progress: 96.10% complete, ETA 00:03:59
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 230.937 ops/ms
# Warmup Iteration   2: 244.231 ops/ms
# Warmup Iteration   3: 244.754 ops/ms
Iteration   1: 244.737 ops/ms
Iteration   2: 244.672 ops/ms
Iteration   3: 243.968 ops/ms
Iteration   4: 244.472 ops/ms
Iteration   5: 244.656 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.gson":
  244.501 ±(99.9%) 1.209 ops/ms [Average]
  (min, avg, max) = (243.968, 244.501, 244.737), stdev = 0.314
  CI (99.9%): [243.292, 245.710] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.jackson

# Run progress: 97.40% complete, ETA 00:02:39
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 685.922 ops/ms
# Warmup Iteration   2: 713.054 ops/ms
# Warmup Iteration   3: 714.164 ops/ms
Iteration   1: 715.010 ops/ms
Iteration   2: 714.197 ops/ms
Iteration   3: 715.092 ops/ms
Iteration   4: 713.987 ops/ms
Iteration   5: 712.039 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.jackson":
  714.065 ±(99.9%) 4.747 ops/ms [Average]
  (min, avg, max) = (712.039, 714.065, 715.092), stdev = 1.233
  CI (99.9%): [709.318, 718.812] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.wast

# Run progress: 98.70% complete, ETA 00:01:19
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1778.943 ops/ms
# Warmup Iteration   2: 1817.659 ops/ms
# Warmup Iteration   3: 1832.157 ops/ms
Iteration   1: 1819.065 ops/ms
Iteration   2: 1819.396 ops/ms
Iteration   3: 1817.150 ops/ms
Iteration   4: 1860.206 ops/ms
Iteration   5: 1858.491 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.wast":
  1834.862 ±(99.9%) 86.170 ops/ms [Average]
  (min, avg, max) = (1817.150, 1834.862, 1860.206), stdev = 22.378
  CI (99.9%): [1748.692, 1921.032] (assumes normal distribution)


# Run complete. Total time: 01:42:24

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

Benchmark                                                 Mode  Cnt     Score      Error   Units
c.a.f.b.eishay.EishayParseString.fastjson2               thrpt    5  6566.758 ±   47.752  ops/ms
c.a.f.b.eishay.EishayParseString.fastjson3               thrpt    5  4030.826 ±   49.911  ops/ms
c.a.f.b.eishay.EishayParseString.fastjson3_preconv_utf8  thrpt    5  6284.018 ±   47.512  ops/ms
c.a.f.b.eishay.EishayParseString.gson                    thrpt    5  1470.678 ±    8.092  ops/ms
c.a.f.b.eishay.EishayParseString.jackson                 thrpt    5  1497.229 ±   13.574  ops/ms
c.a.f.b.eishay.EishayParseString.wast                    thrpt    5  4715.055 ±  521.388  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.fastjson2         thrpt    5  4696.681 ±   36.635  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.fastjson3         thrpt    5  4185.602 ±   31.985  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.gson              thrpt    5  1349.739 ±    8.620  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.jackson           thrpt    5  1335.885 ±    9.024  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.wast              thrpt    5  4094.854 ±  337.833  ops/ms
c.a.f.b.eishay.EishayParseTreeString.fastjson2           thrpt    5  3631.144 ±   38.051  ops/ms
c.a.f.b.eishay.EishayParseTreeString.fastjson3           thrpt    5  3407.603 ±   18.220  ops/ms
c.a.f.b.eishay.EishayParseTreeString.gson                thrpt    5  1256.139 ±   18.354  ops/ms
c.a.f.b.eishay.EishayParseTreeString.jackson             thrpt    5  1562.875 ±    3.781  ops/ms
c.a.f.b.eishay.EishayParseTreeString.wast                thrpt    5  3461.665 ±   25.600  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2        thrpt    5  3595.389 ±   25.690  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3        thrpt    5  3454.640 ±   16.080  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.gson             thrpt    5  1260.689 ±   13.172  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.jackson          thrpt    5  1667.601 ±   14.401  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast             thrpt    5  3322.296 ±   24.054  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2            thrpt    5  6368.382 ±   48.050  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3            thrpt    5  4529.770 ±   24.307  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm        thrpt    5  6276.688 ±   53.899  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect    thrpt    5  6493.080 ±   39.452  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.gson                 thrpt    5  1450.395 ±    6.244  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.jackson              thrpt    5  1561.145 ±    4.386  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.wast                 thrpt    5  4601.894 ±  463.928  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.fastjson2      thrpt    5  4617.816 ±   41.589  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.fastjson3      thrpt    5  3842.091 ±   28.533  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.gson           thrpt    5  1308.448 ±    4.784  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.jackson        thrpt    5  1499.937 ±    1.395  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.wast           thrpt    5  3917.034 ±  305.051  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson2               thrpt    5  6749.465 ±   40.033  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson3               thrpt    5  6460.845 ±   48.637  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson3_asm           thrpt    5  4657.068 ±   20.246  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson3_reflect       thrpt    5  5012.326 ±   22.603  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson3_utf8latin1    thrpt    5  6539.390 ±   56.121  ops/ms
c.a.f.b.eishay.EishayWriteString.gson                    thrpt    5   907.164 ±    3.265  ops/ms
c.a.f.b.eishay.EishayWriteString.jackson                 thrpt    5  2701.084 ±   17.578  ops/ms
c.a.f.b.eishay.EishayWriteString.wast                    thrpt    5  4967.487 ± 1414.063  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2            thrpt    5  7974.260 ±   70.177  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3            thrpt    5  6557.098 ±   34.472  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm        thrpt    5  6580.485 ±  118.594  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect    thrpt    5  6657.537 ±   14.869  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.gson                 thrpt    5   893.129 ±    4.082  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.jackson              thrpt    5  2698.760 ±   12.550  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast                 thrpt    5  5646.635 ± 1505.728  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2              thrpt    5  1251.166 ±    8.271  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3              thrpt    5  1541.012 ±   39.869  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm          thrpt    5  1526.249 ±   56.809  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect      thrpt    5  1509.886 ±   39.868  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.gson                   thrpt    5   402.403 ±    1.402  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.jackson                thrpt    5   470.952 ±    1.143  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                   thrpt    5  1336.976 ±   57.299  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2              thrpt    5  2019.474 ±    4.472  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3              thrpt    5  2055.804 ±    8.409  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm          thrpt    5  2010.791 ±    5.576  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect      thrpt    5  2062.035 ±   17.299  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.gson                   thrpt    5   254.235 ±    1.000  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.jackson                thrpt    5   738.899 ±    3.725  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                   thrpt    5  1819.140 ±   91.902  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2                thrpt    5  1423.883 ±   16.071  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3                thrpt    5  1342.416 ±   11.333  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm            thrpt    5  1337.548 ±    5.951  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect        thrpt    5  1349.080 ±   13.353  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.gson                     thrpt    5   425.001 ±    2.246  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.jackson                  thrpt    5   457.329 ±    1.146  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                     thrpt    5  1319.415 ±   35.700  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2                thrpt    5  1998.937 ±    6.278  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3                thrpt    5  2189.771 ±    9.183  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm            thrpt    5  2190.945 ±   21.279  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect        thrpt    5  2192.194 ±   19.269  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.gson                     thrpt    5   244.501 ±    1.209  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.jackson                  thrpt    5   714.065 ±    4.747  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                     thrpt    5  1834.862 ±   86.170  ops/ms

Benchmark result is saved to /root/bench-fj3/results/arm-172.16.1.231/results.json
=== FINISHED 2026-04-12T16:22:26+08:00 ===
```

## x86_64 4c

**JDK**: 25.0.2  
**Arch**: x86_64  
**Cores**: 4  
**OS**: Debian GNU/Linux 13 (trixie)  

```
=== STARTED 2026-04-12T14:42:01+08:00 ===
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

# Run progress: 0.00% complete, ETA 01:42:40
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13075.062 ops/ms
# Warmup Iteration   2: 13402.983 ops/ms
# Warmup Iteration   3: 13387.588 ops/ms
Iteration   1: 13329.977 ops/ms
Iteration   2: 13291.170 ops/ms
Iteration   3: 13343.264 ops/ms
Iteration   4: 13300.797 ops/ms
Iteration   5: 13266.966 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson2":
  13306.435 ±(99.9%) 117.637 ops/ms [Average]
  (min, avg, max) = (13266.966, 13306.435, 13343.264), stdev = 30.550
  CI (99.9%): [13188.798, 13424.071] (assumes normal distribution)


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

# Run progress: 1.30% complete, ETA 01:42:25
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8858.465 ops/ms
# Warmup Iteration   2: 9312.524 ops/ms
# Warmup Iteration   3: 9283.309 ops/ms
Iteration   1: 9275.434 ops/ms
Iteration   2: 9267.568 ops/ms
Iteration   3: 9175.537 ops/ms
Iteration   4: 9277.553 ops/ms
Iteration   5: 9223.528 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3":
  9243.924 ±(99.9%) 169.774 ops/ms [Average]
  (min, avg, max) = (9175.537, 9243.924, 9277.553), stdev = 44.090
  CI (99.9%): [9074.150, 9413.698] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_preconv_utf8

# Run progress: 2.60% complete, ETA 01:40:58
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10292.489 ops/ms
# Warmup Iteration   2: 10577.043 ops/ms
# Warmup Iteration   3: 10604.440 ops/ms
Iteration   1: 10603.617 ops/ms
Iteration   2: 10607.683 ops/ms
Iteration   3: 10651.364 ops/ms
Iteration   4: 10631.612 ops/ms
Iteration   5: 10580.412 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_preconv_utf8":
  10614.938 ±(99.9%) 105.062 ops/ms [Average]
  (min, avg, max) = (10580.412, 10614.938, 10651.364), stdev = 27.284
  CI (99.9%): [10509.876, 10720.000] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser

# Run progress: 3.90% complete, ETA 01:39:37
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: <failure>

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_thrpt_jmhStub(EishayParseString_fastjson3_str_parser_jmhTest.java:121)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:84)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)

java.lang.UnsupportedOperationException: reading com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent not yet supported without ObjectReader
	at com.alibaba.fastjson3.JSONParser.read(JSONParser.java:876)
	at com.alibaba.fastjson3.benchmark.eishay.EishayParseString.fastjson3_str_parser(EishayParseString.java:55)
	at com.alibaba.fastjson3.benchmark.eishay.jmh_generated.EishayParseString_fastjson3_str_parser_jmhTest.fastjson3_str_parser_Throughput(EishayParseString_fastjson3_str_parser_jmhTest.java:78)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:565)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:545)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:328)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1090)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:614)
	at java.base/java.lang.Thread.run(Thread.java:1474)




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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.gson

# Run progress: 5.19% complete, ETA 01:13:46
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3007.362 ops/ms
# Warmup Iteration   2: 3125.374 ops/ms
# Warmup Iteration   3: 3106.011 ops/ms
Iteration   1: 3090.119 ops/ms
Iteration   2: 3102.728 ops/ms
Iteration   3: 3096.063 ops/ms
Iteration   4: 3103.614 ops/ms
Iteration   5: 3095.058 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.gson":
  3097.517 ±(99.9%) 21.716 ops/ms [Average]
  (min, avg, max) = (3090.119, 3097.517, 3103.614), stdev = 5.640
  CI (99.9%): [3075.800, 3119.233] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.jackson

# Run progress: 6.49% complete, ETA 01:17:35
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3110.842 ops/ms
# Warmup Iteration   2: 3241.889 ops/ms
# Warmup Iteration   3: 3225.827 ops/ms
Iteration   1: 3225.978 ops/ms
Iteration   2: 3234.605 ops/ms
Iteration   3: 3237.939 ops/ms
Iteration   4: 3229.324 ops/ms
Iteration   5: 3222.342 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.jackson":
  3230.038 ±(99.9%) 24.316 ops/ms [Average]
  (min, avg, max) = (3222.342, 3230.038, 3237.939), stdev = 6.315
  CI (99.9%): [3205.722, 3254.354] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseString.wast

# Run progress: 7.79% complete, ETA 01:19:40
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10246.511 ops/ms
# Warmup Iteration   2: 10104.502 ops/ms
# Warmup Iteration   3: 10428.492 ops/ms
Iteration   1: 10411.725 ops/ms
Iteration   2: 10465.599 ops/ms
Iteration   3: 10428.226 ops/ms
Iteration   4: 11186.302 ops/ms
Iteration   5: 11188.388 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseString.wast":
  10736.048 ±(99.9%) 1588.154 ops/ms [Average]
  (min, avg, max) = (10411.725, 10736.048, 11188.388), stdev = 412.439
  CI (99.9%): [9147.894, 12324.202] (assumes normal distribution)


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

# Run progress: 9.09% complete, ETA 01:20:46
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 9384.127 ops/ms
# Warmup Iteration   2: 9618.152 ops/ms
# Warmup Iteration   3: 9532.184 ops/ms
Iteration   1: 9576.654 ops/ms
Iteration   2: 9560.602 ops/ms
Iteration   3: 9520.875 ops/ms
Iteration   4: 9458.844 ops/ms
Iteration   5: 9452.415 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson2":
  9513.878 ±(99.9%) 219.347 ops/ms [Average]
  (min, avg, max) = (9452.415, 9513.878, 9576.654), stdev = 56.964
  CI (99.9%): [9294.532, 9733.225] (assumes normal distribution)


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

# Run progress: 10.39% complete, ETA 01:21:16
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7332.060 ops/ms
# Warmup Iteration   2: 7474.491 ops/ms
# Warmup Iteration   3: 7505.071 ops/ms
Iteration   1: 7500.384 ops/ms
Iteration   2: 7470.587 ops/ms
Iteration   3: 7461.853 ops/ms
Iteration   4: 7504.271 ops/ms
Iteration   5: 7442.590 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.fastjson3":
  7475.937 ±(99.9%) 100.771 ops/ms [Average]
  (min, avg, max) = (7442.590, 7475.937, 7504.271), stdev = 26.170
  CI (99.9%): [7375.166, 7576.708] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.gson

# Run progress: 11.69% complete, ETA 01:21:22
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2869.085 ops/ms
# Warmup Iteration   2: 2912.903 ops/ms
# Warmup Iteration   3: 2877.477 ops/ms
Iteration   1: 2885.994 ops/ms
Iteration   2: 2895.528 ops/ms
Iteration   3: 2886.972 ops/ms
Iteration   4: 2879.188 ops/ms
Iteration   5: 2896.794 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.gson":
  2888.895 ±(99.9%) 28.082 ops/ms [Average]
  (min, avg, max) = (2879.188, 2888.895, 2896.794), stdev = 7.293
  CI (99.9%): [2860.813, 2916.977] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.jackson

# Run progress: 12.99% complete, ETA 01:21:10
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2672.931 ops/ms
# Warmup Iteration   2: 2795.376 ops/ms
# Warmup Iteration   3: 2801.119 ops/ms
Iteration   1: 2798.715 ops/ms
Iteration   2: 2802.370 ops/ms
Iteration   3: 2794.202 ops/ms
Iteration   4: 2796.409 ops/ms
Iteration   5: 2798.548 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.jackson":
  2798.049 ±(99.9%) 11.688 ops/ms [Average]
  (min, avg, max) = (2794.202, 2798.049, 2802.370), stdev = 3.035
  CI (99.9%): [2786.361, 2809.737] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.wast

# Run progress: 14.29% complete, ETA 01:20:45
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 9004.667 ops/ms
# Warmup Iteration   2: 9207.035 ops/ms
# Warmup Iteration   3: 9210.744 ops/ms
Iteration   1: 9108.589 ops/ms
Iteration   2: 9141.768 ops/ms
Iteration   3: 9172.825 ops/ms
Iteration   4: 9383.188 ops/ms
Iteration   5: 9385.523 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseStringPretty.wast":
  9238.378 ±(99.9%) 520.540 ops/ms [Average]
  (min, avg, max) = (9108.589, 9238.378, 9385.523), stdev = 135.183
  CI (99.9%): [8717.839, 9758.918] (assumes normal distribution)


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

# Run progress: 15.58% complete, ETA 01:20:11
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7317.954 ops/ms
# Warmup Iteration   2: 7322.032 ops/ms
# Warmup Iteration   3: 7333.034 ops/ms
Iteration   1: 7336.226 ops/ms
Iteration   2: 7359.557 ops/ms
Iteration   3: 7298.506 ops/ms
Iteration   4: 7319.396 ops/ms
Iteration   5: 7328.760 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson2":
  7328.489 ±(99.9%) 86.250 ops/ms [Average]
  (min, avg, max) = (7298.506, 7328.489, 7359.557), stdev = 22.399
  CI (99.9%): [7242.239, 7414.739] (assumes normal distribution)


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

# Run progress: 16.88% complete, ETA 01:19:30
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5183.838 ops/ms
# Warmup Iteration   2: 5187.363 ops/ms
# Warmup Iteration   3: 5252.639 ops/ms
Iteration   1: 5253.837 ops/ms
Iteration   2: 5252.084 ops/ms
Iteration   3: 5255.022 ops/ms
Iteration   4: 5265.721 ops/ms
Iteration   5: 5291.755 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.fastjson3":
  5263.684 ±(99.9%) 63.818 ops/ms [Average]
  (min, avg, max) = (5252.084, 5263.684, 5291.755), stdev = 16.573
  CI (99.9%): [5199.866, 5327.502] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.gson

# Run progress: 18.18% complete, ETA 01:18:43
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2755.175 ops/ms
# Warmup Iteration   2: 2840.948 ops/ms
# Warmup Iteration   3: 2839.015 ops/ms
Iteration   1: 2843.143 ops/ms
Iteration   2: 2847.678 ops/ms
Iteration   3: 2844.444 ops/ms
Iteration   4: 2852.698 ops/ms
Iteration   5: 2850.855 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.gson":
  2847.764 ±(99.9%) 15.675 ops/ms [Average]
  (min, avg, max) = (2843.143, 2847.764, 2852.698), stdev = 4.071
  CI (99.9%): [2832.089, 2863.438] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.jackson

# Run progress: 19.48% complete, ETA 01:17:52
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3277.291 ops/ms
# Warmup Iteration   2: 3369.847 ops/ms
# Warmup Iteration   3: 3362.795 ops/ms
Iteration   1: 3355.618 ops/ms
Iteration   2: 3352.057 ops/ms
Iteration   3: 3363.015 ops/ms
Iteration   4: 3351.564 ops/ms
Iteration   5: 3373.511 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.jackson":
  3359.153 ±(99.9%) 35.579 ops/ms [Average]
  (min, avg, max) = (3351.564, 3359.153, 3373.511), stdev = 9.240
  CI (99.9%): [3323.574, 3394.732] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.wast

# Run progress: 20.78% complete, ETA 01:16:56
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7258.443 ops/ms
# Warmup Iteration   2: 7220.754 ops/ms
# Warmup Iteration   3: 7244.256 ops/ms
Iteration   1: 7191.479 ops/ms
Iteration   2: 7216.764 ops/ms
Iteration   3: 7244.250 ops/ms
Iteration   4: 7225.782 ops/ms
Iteration   5: 7097.228 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeString.wast":
  7195.100 ±(99.9%) 223.026 ops/ms [Average]
  (min, avg, max) = (7097.228, 7195.100, 7244.250), stdev = 57.919
  CI (99.9%): [6972.075, 7418.126] (assumes normal distribution)


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

# Run progress: 22.08% complete, ETA 01:15:58
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7246.404 ops/ms
# Warmup Iteration   2: 7347.148 ops/ms
# Warmup Iteration   3: 7402.284 ops/ms
Iteration   1: 7397.026 ops/ms
Iteration   2: 7379.854 ops/ms
Iteration   3: 7347.741 ops/ms
Iteration   4: 7395.536 ops/ms
Iteration   5: 7401.204 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson2":
  7384.272 ±(99.9%) 84.589 ops/ms [Average]
  (min, avg, max) = (7347.741, 7384.272, 7401.204), stdev = 21.968
  CI (99.9%): [7299.683, 7468.861] (assumes normal distribution)


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

# Run progress: 23.38% complete, ETA 01:14:58
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5226.302 ops/ms
# Warmup Iteration   2: 5264.708 ops/ms
# Warmup Iteration   3: 5274.102 ops/ms
Iteration   1: 5272.509 ops/ms
Iteration   2: 5258.195 ops/ms
Iteration   3: 5262.862 ops/ms
Iteration   4: 5267.873 ops/ms
Iteration   5: 5241.421 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.fastjson3":
  5260.572 ±(99.9%) 46.106 ops/ms [Average]
  (min, avg, max) = (5241.421, 5260.572, 5272.509), stdev = 11.974
  CI (99.9%): [5214.466, 5306.678] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.gson

# Run progress: 24.68% complete, ETA 01:13:55
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2722.514 ops/ms
# Warmup Iteration   2: 2782.109 ops/ms
# Warmup Iteration   3: 2816.328 ops/ms
Iteration   1: 2797.578 ops/ms
Iteration   2: 2812.000 ops/ms
Iteration   3: 2812.287 ops/ms
Iteration   4: 2813.329 ops/ms
Iteration   5: 2806.653 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.gson":
  2808.369 ±(99.9%) 25.290 ops/ms [Average]
  (min, avg, max) = (2797.578, 2808.369, 2813.329), stdev = 6.568
  CI (99.9%): [2783.080, 2833.659] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.jackson

# Run progress: 25.97% complete, ETA 01:12:51
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3661.840 ops/ms
# Warmup Iteration   2: 3747.399 ops/ms
# Warmup Iteration   3: 3745.283 ops/ms
Iteration   1: 3742.964 ops/ms
Iteration   2: 3753.628 ops/ms
Iteration   3: 3752.624 ops/ms
Iteration   4: 3743.581 ops/ms
Iteration   5: 3762.025 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.jackson":
  3750.964 ±(99.9%) 30.484 ops/ms [Average]
  (min, avg, max) = (3742.964, 3750.964, 3762.025), stdev = 7.917
  CI (99.9%): [3720.480, 3781.449] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.wast

# Run progress: 27.27% complete, ETA 01:11:45
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 6916.851 ops/ms
# Warmup Iteration   2: 6910.067 ops/ms
# Warmup Iteration   3: 6940.670 ops/ms
Iteration   1: 6940.318 ops/ms
Iteration   2: 6875.888 ops/ms
Iteration   3: 6916.625 ops/ms
Iteration   4: 6882.409 ops/ms
Iteration   5: 6880.753 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseTreeUTF8Bytes.wast":
  6899.199 ±(99.9%) 108.229 ops/ms [Average]
  (min, avg, max) = (6875.888, 6899.199, 6940.318), stdev = 28.107
  CI (99.9%): [6790.970, 7007.428] (assumes normal distribution)


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

# Run progress: 28.57% complete, ETA 01:10:37
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 12931.161 ops/ms
# Warmup Iteration   2: 13235.096 ops/ms
# Warmup Iteration   3: 13148.942 ops/ms
Iteration   1: 13202.973 ops/ms
Iteration   2: 13247.341 ops/ms
Iteration   3: 13153.058 ops/ms
Iteration   4: 13229.093 ops/ms
Iteration   5: 13221.792 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson2":
  13210.851 ±(99.9%) 138.651 ops/ms [Average]
  (min, avg, max) = (13153.058, 13210.851, 13247.341), stdev = 36.007
  CI (99.9%): [13072.200, 13349.502] (assumes normal distribution)


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

# Run progress: 29.87% complete, ETA 01:09:29
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10350.340 ops/ms
# Warmup Iteration   2: 10591.695 ops/ms
# Warmup Iteration   3: 10630.092 ops/ms
Iteration   1: 10632.674 ops/ms
Iteration   2: 10612.189 ops/ms
Iteration   3: 10624.027 ops/ms
Iteration   4: 10547.278 ops/ms
Iteration   5: 10630.357 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3":
  10609.305 ±(99.9%) 136.979 ops/ms [Average]
  (min, avg, max) = (10547.278, 10609.305, 10632.674), stdev = 35.573
  CI (99.9%): [10472.326, 10746.284] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_asm

# Run progress: 31.17% complete, ETA 01:08:20
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10871.594 ops/ms
# Warmup Iteration   2: 11156.767 ops/ms
# Warmup Iteration   3: 11176.594 ops/ms
Iteration   1: 11121.857 ops/ms
Iteration   2: 11196.856 ops/ms
Iteration   3: 11248.109 ops/ms
Iteration   4: 11232.591 ops/ms
Iteration   5: 11243.548 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_asm":
  11208.592 ±(99.9%) 202.113 ops/ms [Average]
  (min, avg, max) = (11121.857, 11208.592, 11248.109), stdev = 52.488
  CI (99.9%): [11006.479, 11410.705] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_reflect

# Run progress: 32.47% complete, ETA 01:07:09
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 11015.479 ops/ms
# Warmup Iteration   2: 11195.948 ops/ms
# Warmup Iteration   3: 11218.464 ops/ms
Iteration   1: 11270.571 ops/ms
Iteration   2: 11161.130 ops/ms
Iteration   3: 11145.787 ops/ms
Iteration   4: 11134.840 ops/ms
Iteration   5: 11167.955 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.fastjson3_reflect":
  11176.057 ±(99.9%) 209.466 ops/ms [Average]
  (min, avg, max) = (11134.840, 11176.057, 11270.571), stdev = 54.398
  CI (99.9%): [10966.591, 11385.523] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.gson

# Run progress: 33.77% complete, ETA 01:05:58
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2879.722 ops/ms
# Warmup Iteration   2: 2975.464 ops/ms
# Warmup Iteration   3: 2943.414 ops/ms
Iteration   1: 2968.209 ops/ms
Iteration   2: 2971.156 ops/ms
Iteration   3: 2975.547 ops/ms
Iteration   4: 2981.370 ops/ms
Iteration   5: 2963.948 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.gson":
  2972.046 ±(99.9%) 25.853 ops/ms [Average]
  (min, avg, max) = (2963.948, 2972.046, 2981.370), stdev = 6.714
  CI (99.9%): [2946.193, 2997.899] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.jackson

# Run progress: 35.06% complete, ETA 01:04:46
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3290.691 ops/ms
# Warmup Iteration   2: 3401.935 ops/ms
# Warmup Iteration   3: 3392.710 ops/ms
Iteration   1: 3384.947 ops/ms
Iteration   2: 3367.372 ops/ms
Iteration   3: 3391.766 ops/ms
Iteration   4: 3392.548 ops/ms
Iteration   5: 3398.520 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.jackson":
  3387.030 ±(99.9%) 46.195 ops/ms [Average]
  (min, avg, max) = (3367.372, 3387.030, 3398.520), stdev = 11.997
  CI (99.9%): [3340.835, 3433.226] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.wast

# Run progress: 36.36% complete, ETA 01:03:33
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10366.910 ops/ms
# Warmup Iteration   2: 10814.882 ops/ms
# Warmup Iteration   3: 10678.189 ops/ms
Iteration   1: 10715.936 ops/ms
Iteration   2: 10731.902 ops/ms
Iteration   3: 10719.075 ops/ms
Iteration   4: 11029.000 ops/ms
Iteration   5: 11008.946 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8Bytes.wast":
  10840.972 ±(99.9%) 626.718 ops/ms [Average]
  (min, avg, max) = (10715.936, 10840.972, 11029.000), stdev = 162.757
  CI (99.9%): [10214.254, 11467.689] (assumes normal distribution)


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

# Run progress: 37.66% complete, ETA 01:02:20
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8985.053 ops/ms
# Warmup Iteration   2: 9652.315 ops/ms
# Warmup Iteration   3: 9608.513 ops/ms
Iteration   1: 9633.462 ops/ms
Iteration   2: 9649.918 ops/ms
Iteration   3: 9656.097 ops/ms
Iteration   4: 9624.777 ops/ms
Iteration   5: 9658.711 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson2":
  9644.593 ±(99.9%) 57.003 ops/ms [Average]
  (min, avg, max) = (9624.777, 9644.593, 9658.711), stdev = 14.804
  CI (99.9%): [9587.590, 9701.596] (assumes normal distribution)


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

# Run progress: 38.96% complete, ETA 01:01:07
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8588.572 ops/ms
# Warmup Iteration   2: 8929.841 ops/ms
# Warmup Iteration   3: 8895.516 ops/ms
Iteration   1: 8916.087 ops/ms
Iteration   2: 8979.341 ops/ms
Iteration   3: 8946.670 ops/ms
Iteration   4: 8938.478 ops/ms
Iteration   5: 8969.715 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.fastjson3":
  8950.058 ±(99.9%) 97.100 ops/ms [Average]
  (min, avg, max) = (8916.087, 8950.058, 8979.341), stdev = 25.217
  CI (99.9%): [8852.958, 9047.158] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.gson

# Run progress: 40.26% complete, ETA 00:59:53
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2638.592 ops/ms
# Warmup Iteration   2: 2688.950 ops/ms
# Warmup Iteration   3: 2679.096 ops/ms
Iteration   1: 2687.864 ops/ms
Iteration   2: 2689.566 ops/ms
Iteration   3: 2677.037 ops/ms
Iteration   4: 2677.811 ops/ms
Iteration   5: 2685.659 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.gson":
  2683.587 ±(99.9%) 22.338 ops/ms [Average]
  (min, avg, max) = (2677.037, 2683.587, 2689.566), stdev = 5.801
  CI (99.9%): [2661.250, 2705.925] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.jackson

# Run progress: 41.56% complete, ETA 00:58:38
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2914.105 ops/ms
# Warmup Iteration   2: 3009.405 ops/ms
# Warmup Iteration   3: 3008.664 ops/ms
Iteration   1: 2991.821 ops/ms
Iteration   2: 2999.267 ops/ms
Iteration   3: 3010.716 ops/ms
Iteration   4: 3015.341 ops/ms
Iteration   5: 3000.472 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.jackson":
  3003.524 ±(99.9%) 36.315 ops/ms [Average]
  (min, avg, max) = (2991.821, 3003.524, 3015.341), stdev = 9.431
  CI (99.9%): [2967.209, 3039.839] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.wast

# Run progress: 42.86% complete, ETA 00:57:23
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 7569.823 ops/ms
# Warmup Iteration   2: 7768.368 ops/ms
# Warmup Iteration   3: 7737.059 ops/ms
Iteration   1: 7752.809 ops/ms
Iteration   2: 7785.203 ops/ms
Iteration   3: 7731.808 ops/ms
Iteration   4: 7942.700 ops/ms
Iteration   5: 7974.108 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayParseUTF8BytesPretty.wast":
  7837.325 ±(99.9%) 433.975 ops/ms [Average]
  (min, avg, max) = (7731.808, 7837.325, 7974.108), stdev = 112.702
  CI (99.9%): [7403.350, 8271.301] (assumes normal distribution)


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

# Run progress: 44.16% complete, ETA 00:56:08
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 13420.717 ops/ms
# Warmup Iteration   2: 13695.326 ops/ms
# Warmup Iteration   3: 13713.586 ops/ms
Iteration   1: 13716.297 ops/ms
Iteration   2: 13755.211 ops/ms
Iteration   3: 13716.248 ops/ms
Iteration   4: 13728.400 ops/ms
Iteration   5: 13727.198 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson2":
  13728.671 ±(99.9%) 61.311 ops/ms [Average]
  (min, avg, max) = (13716.248, 13728.671, 13755.211), stdev = 15.922
  CI (99.9%): [13667.359, 13789.982] (assumes normal distribution)


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

# Run progress: 45.45% complete, ETA 00:54:53
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 11849.495 ops/ms
# Warmup Iteration   2: 12072.132 ops/ms
# Warmup Iteration   3: 12133.944 ops/ms
Iteration   1: 12119.545 ops/ms
Iteration   2: 12122.384 ops/ms
Iteration   3: 12100.574 ops/ms
Iteration   4: 12195.541 ops/ms
Iteration   5: 12196.021 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3":
  12146.813 ±(99.9%) 175.133 ops/ms [Average]
  (min, avg, max) = (12100.574, 12146.813, 12196.021), stdev = 45.481
  CI (99.9%): [11971.680, 12321.946] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_asm

# Run progress: 46.75% complete, ETA 00:53:37
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8210.686 ops/ms
# Warmup Iteration   2: 8377.398 ops/ms
# Warmup Iteration   3: 8359.055 ops/ms
Iteration   1: 8317.743 ops/ms
Iteration   2: 8357.932 ops/ms
Iteration   3: 8337.942 ops/ms
Iteration   4: 8350.475 ops/ms
Iteration   5: 8423.699 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_asm":
  8357.558 ±(99.9%) 153.965 ops/ms [Average]
  (min, avg, max) = (8317.743, 8357.558, 8423.699), stdev = 39.984
  CI (99.9%): [8203.594, 8511.523] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_reflect

# Run progress: 48.05% complete, ETA 00:52:21
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 8119.346 ops/ms
# Warmup Iteration   2: 8190.001 ops/ms
# Warmup Iteration   3: 8156.336 ops/ms
Iteration   1: 8054.022 ops/ms
Iteration   2: 8039.177 ops/ms
Iteration   3: 8143.843 ops/ms
Iteration   4: 8147.705 ops/ms
Iteration   5: 8213.059 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_reflect":
  8119.562 ±(99.9%) 278.211 ops/ms [Average]
  (min, avg, max) = (8039.177, 8119.562, 8213.059), stdev = 72.250
  CI (99.9%): [7841.351, 8397.772] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_utf8latin1

# Run progress: 49.35% complete, ETA 00:51:05
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 11498.506 ops/ms
# Warmup Iteration   2: 11541.772 ops/ms
# Warmup Iteration   3: 11672.200 ops/ms
Iteration   1: 11681.785 ops/ms
Iteration   2: 11632.870 ops/ms
Iteration   3: 11568.148 ops/ms
Iteration   4: 11657.236 ops/ms
Iteration   5: 11654.844 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.fastjson3_utf8latin1":
  11638.976 ±(99.9%) 166.423 ops/ms [Average]
  (min, avg, max) = (11568.148, 11638.976, 11681.785), stdev = 43.219
  CI (99.9%): [11472.554, 11805.399] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.gson

# Run progress: 50.65% complete, ETA 00:49:49
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1407.416 ops/ms
# Warmup Iteration   2: 1431.185 ops/ms
# Warmup Iteration   3: 1429.166 ops/ms
Iteration   1: 1427.490 ops/ms
Iteration   2: 1427.526 ops/ms
Iteration   3: 1427.957 ops/ms
Iteration   4: 1430.320 ops/ms
Iteration   5: 1430.270 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.gson":
  1428.713 ±(99.9%) 5.608 ops/ms [Average]
  (min, avg, max) = (1427.490, 1428.713, 1430.320), stdev = 1.456
  CI (99.9%): [1423.105, 1434.321] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.jackson

# Run progress: 51.95% complete, ETA 00:48:32
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5655.752 ops/ms
# Warmup Iteration   2: 5918.606 ops/ms
# Warmup Iteration   3: 5943.485 ops/ms
Iteration   1: 5950.615 ops/ms
Iteration   2: 5919.025 ops/ms
Iteration   3: 5958.925 ops/ms
Iteration   4: 5950.559 ops/ms
Iteration   5: 5984.652 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.jackson":
  5952.755 ±(99.9%) 90.358 ops/ms [Average]
  (min, avg, max) = (5919.025, 5952.755, 5984.652), stdev = 23.466
  CI (99.9%): [5862.397, 6043.114] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.wast

# Run progress: 53.25% complete, ETA 00:47:15
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 9873.366 ops/ms
# Warmup Iteration   2: 9972.912 ops/ms
# Warmup Iteration   3: 10043.600 ops/ms
Iteration   1: 10083.194 ops/ms
Iteration   2: 10096.683 ops/ms
Iteration   3: 10128.108 ops/ms
Iteration   4: 10937.841 ops/ms
Iteration   5: 10899.960 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteString.wast":
  10429.157 ±(99.9%) 1723.432 ops/ms [Average]
  (min, avg, max) = (10083.194, 10429.157, 10937.841), stdev = 447.570
  CI (99.9%): [8705.725, 12152.589] (assumes normal distribution)


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

# Run progress: 54.55% complete, ETA 00:45:58
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 17297.150 ops/ms
# Warmup Iteration   2: 17804.967 ops/ms
# Warmup Iteration   3: 17790.751 ops/ms
Iteration   1: 17755.128 ops/ms
Iteration   2: 17775.099 ops/ms
Iteration   3: 17770.455 ops/ms
Iteration   4: 17785.320 ops/ms
Iteration   5: 17834.895 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson2":
  17784.179 ±(99.9%) 116.925 ops/ms [Average]
  (min, avg, max) = (17755.128, 17784.179, 17834.895), stdev = 30.365
  CI (99.9%): [17667.254, 17901.104] (assumes normal distribution)


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

# Run progress: 55.84% complete, ETA 00:44:41
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 12030.303 ops/ms
# Warmup Iteration   2: 12217.122 ops/ms
# Warmup Iteration   3: 12241.362 ops/ms
Iteration   1: 12294.070 ops/ms
Iteration   2: 12292.565 ops/ms
Iteration   3: 12304.630 ops/ms
Iteration   4: 12259.700 ops/ms
Iteration   5: 12332.513 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3":
  12296.696 ±(99.9%) 100.753 ops/ms [Average]
  (min, avg, max) = (12259.700, 12296.696, 12332.513), stdev = 26.165
  CI (99.9%): [12195.943, 12397.449] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_asm

# Run progress: 57.14% complete, ETA 00:43:23
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 11905.300 ops/ms
# Warmup Iteration   2: 11994.464 ops/ms
# Warmup Iteration   3: 11953.189 ops/ms
Iteration   1: 11899.123 ops/ms
Iteration   2: 11910.766 ops/ms
Iteration   3: 12029.162 ops/ms
Iteration   4: 11945.772 ops/ms
Iteration   5: 11829.151 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_asm":
  11922.795 ±(99.9%) 281.098 ops/ms [Average]
  (min, avg, max) = (11829.151, 11922.795, 12029.162), stdev = 73.000
  CI (99.9%): [11641.696, 12203.893] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_reflect

# Run progress: 58.44% complete, ETA 00:42:06
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 11718.562 ops/ms
# Warmup Iteration   2: 11818.799 ops/ms
# Warmup Iteration   3: 11801.308 ops/ms
Iteration   1: 11823.546 ops/ms
Iteration   2: 11828.332 ops/ms
Iteration   3: 11868.398 ops/ms
Iteration   4: 11781.655 ops/ms
Iteration   5: 11832.304 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.fastjson3_reflect":
  11826.847 ±(99.9%) 118.867 ops/ms [Average]
  (min, avg, max) = (11781.655, 11826.847, 11868.398), stdev = 30.869
  CI (99.9%): [11707.980, 11945.714] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.gson

# Run progress: 59.74% complete, ETA 00:40:48
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1382.708 ops/ms
# Warmup Iteration   2: 1404.174 ops/ms
# Warmup Iteration   3: 1411.807 ops/ms
Iteration   1: 1412.571 ops/ms
Iteration   2: 1410.607 ops/ms
Iteration   3: 1409.866 ops/ms
Iteration   4: 1410.281 ops/ms
Iteration   5: 1411.436 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.gson":
  1410.952 ±(99.9%) 4.130 ops/ms [Average]
  (min, avg, max) = (1409.866, 1410.952, 1412.571), stdev = 1.073
  CI (99.9%): [1406.822, 1415.083] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.jackson

# Run progress: 61.04% complete, ETA 00:39:30
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 5837.819 ops/ms
# Warmup Iteration   2: 6015.949 ops/ms
# Warmup Iteration   3: 6001.676 ops/ms
Iteration   1: 5984.261 ops/ms
Iteration   2: 5962.767 ops/ms
Iteration   3: 5961.303 ops/ms
Iteration   4: 5976.407 ops/ms
Iteration   5: 6007.792 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.jackson":
  5978.506 ±(99.9%) 73.032 ops/ms [Average]
  (min, avg, max) = (5961.303, 5978.506, 6007.792), stdev = 18.966
  CI (99.9%): [5905.474, 6051.538] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.wast

# Run progress: 62.34% complete, ETA 00:38:12
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 10863.447 ops/ms
# Warmup Iteration   2: 10992.594 ops/ms
# Warmup Iteration   3: 10980.315 ops/ms
Iteration   1: 11082.928 ops/ms
Iteration   2: 11083.504 ops/ms
Iteration   3: 11052.727 ops/ms
Iteration   4: 12052.234 ops/ms
Iteration   5: 12024.753 ops/ms


Result "com.alibaba.fastjson3.benchmark.eishay.EishayWriteUTF8Bytes.wast":
  11459.229 ±(99.9%) 2037.103 ops/ms [Average]
  (min, avg, max) = (11052.727, 11459.229, 12052.234), stdev = 529.029
  CI (99.9%): [9422.126, 13496.332] (assumes normal distribution)


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

# Run progress: 63.64% complete, ETA 00:36:54
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2632.406 ops/ms
# Warmup Iteration   2: 2736.926 ops/ms
# Warmup Iteration   3: 2722.815 ops/ms
Iteration   1: 2707.235 ops/ms
Iteration   2: 2730.230 ops/ms
Iteration   3: 2728.879 ops/ms
Iteration   4: 2698.080 ops/ms
Iteration   5: 2713.415 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson2":
  2715.568 ±(99.9%) 53.497 ops/ms [Average]
  (min, avg, max) = (2698.080, 2715.568, 2730.230), stdev = 13.893
  CI (99.9%): [2662.071, 2769.065] (assumes normal distribution)


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

# Run progress: 64.94% complete, ETA 00:35:36
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2640.735 ops/ms
# Warmup Iteration   2: 2697.907 ops/ms
# Warmup Iteration   3: 2706.719 ops/ms
Iteration   1: 2708.788 ops/ms
Iteration   2: 2721.937 ops/ms
Iteration   3: 2714.793 ops/ms
Iteration   4: 2711.746 ops/ms
Iteration   5: 2709.674 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3":
  2713.387 ±(99.9%) 20.440 ops/ms [Average]
  (min, avg, max) = (2708.788, 2713.387, 2721.937), stdev = 5.308
  CI (99.9%): [2692.948, 2733.827] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3_asm

# Run progress: 66.23% complete, ETA 00:34:18
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2752.628 ops/ms
# Warmup Iteration   2: 2784.418 ops/ms
# Warmup Iteration   3: 2767.141 ops/ms
Iteration   1: 2782.971 ops/ms
Iteration   2: 2788.536 ops/ms
Iteration   3: 2790.458 ops/ms
Iteration   4: 2797.056 ops/ms
Iteration   5: 2795.336 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3_asm":
  2790.872 ±(99.9%) 21.631 ops/ms [Average]
  (min, avg, max) = (2782.971, 2790.872, 2797.056), stdev = 5.617
  CI (99.9%): [2769.241, 2812.502] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3_reflect

# Run progress: 67.53% complete, ETA 00:32:59
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2707.299 ops/ms
# Warmup Iteration   2: 2751.391 ops/ms
# Warmup Iteration   3: 2738.933 ops/ms
Iteration   1: 2757.299 ops/ms
Iteration   2: 2757.631 ops/ms
Iteration   3: 2756.897 ops/ms
Iteration   4: 2748.070 ops/ms
Iteration   5: 2747.274 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.fastjson3_reflect":
  2753.434 ±(99.9%) 20.308 ops/ms [Average]
  (min, avg, max) = (2747.274, 2753.434, 2757.631), stdev = 5.274
  CI (99.9%): [2733.126, 2773.742] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.gson

# Run progress: 68.83% complete, ETA 00:31:41
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 715.324 ops/ms
# Warmup Iteration   2: 745.005 ops/ms
# Warmup Iteration   3: 744.257 ops/ms
Iteration   1: 742.993 ops/ms
Iteration   2: 738.412 ops/ms
Iteration   3: 746.293 ops/ms
Iteration   4: 746.312 ops/ms
Iteration   5: 748.132 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.gson":
  744.428 ±(99.9%) 14.788 ops/ms [Average]
  (min, avg, max) = (738.412, 744.428, 748.132), stdev = 3.840
  CI (99.9%): [729.640, 759.217] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.jackson

# Run progress: 70.13% complete, ETA 00:30:22
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 829.768 ops/ms
# Warmup Iteration   2: 866.920 ops/ms
# Warmup Iteration   3: 866.525 ops/ms
Iteration   1: 864.820 ops/ms
Iteration   2: 866.123 ops/ms
Iteration   3: 867.245 ops/ms
Iteration   4: 866.490 ops/ms
Iteration   5: 866.371 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.jackson":
  866.210 ±(99.9%) 3.399 ops/ms [Average]
  (min, avg, max) = (864.820, 866.210, 867.245), stdev = 0.883
  CI (99.9%): [862.811, 869.609] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.wast

# Run progress: 71.43% complete, ETA 00:29:04
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2680.172 ops/ms
# Warmup Iteration   2: 2736.527 ops/ms
# Warmup Iteration   3: 2722.313 ops/ms
Iteration   1: 2722.726 ops/ms
Iteration   2: 2718.107 ops/ms
Iteration   3: 2721.227 ops/ms
Iteration   4: 2649.775 ops/ms
Iteration   5: 2520.095 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsParseUTF8Bytes.wast":
  2666.386 ±(99.9%) 336.430 ops/ms [Average]
  (min, avg, max) = (2520.095, 2666.386, 2722.726), stdev = 87.370
  CI (99.9%): [2329.957, 3002.816] (assumes normal distribution)


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

# Run progress: 72.73% complete, ETA 00:27:45
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2990.349 ops/ms
# Warmup Iteration   2: 3129.411 ops/ms
# Warmup Iteration   3: 3154.184 ops/ms
Iteration   1: 3135.424 ops/ms
Iteration   2: 3136.859 ops/ms
Iteration   3: 3130.654 ops/ms
Iteration   4: 3130.386 ops/ms
Iteration   5: 3150.285 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson2":
  3136.722 ±(99.9%) 31.201 ops/ms [Average]
  (min, avg, max) = (3130.386, 3136.722, 3150.285), stdev = 8.103
  CI (99.9%): [3105.520, 3167.923] (assumes normal distribution)


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

# Run progress: 74.03% complete, ETA 00:26:26
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3688.365 ops/ms
# Warmup Iteration   2: 3728.630 ops/ms
# Warmup Iteration   3: 3704.122 ops/ms
Iteration   1: 3733.046 ops/ms
Iteration   2: 3731.955 ops/ms
Iteration   3: 3734.218 ops/ms
Iteration   4: 3731.841 ops/ms
Iteration   5: 3731.065 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3":
  3732.425 ±(99.9%) 4.722 ops/ms [Average]
  (min, avg, max) = (3731.065, 3732.425, 3734.218), stdev = 1.226
  CI (99.9%): [3727.703, 3737.147] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3_asm

# Run progress: 75.32% complete, ETA 00:25:07
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3542.610 ops/ms
# Warmup Iteration   2: 3640.910 ops/ms
# Warmup Iteration   3: 3571.424 ops/ms
Iteration   1: 3605.045 ops/ms
Iteration   2: 3585.102 ops/ms
Iteration   3: 3548.903 ops/ms
Iteration   4: 3543.425 ops/ms
Iteration   5: 3536.831 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3_asm":
  3563.861 ±(99.9%) 114.217 ops/ms [Average]
  (min, avg, max) = (3536.831, 3563.861, 3605.045), stdev = 29.662
  CI (99.9%): [3449.644, 3678.078] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect

# Run progress: 76.62% complete, ETA 00:23:48
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3515.453 ops/ms
# Warmup Iteration   2: 3545.434 ops/ms
# Warmup Iteration   3: 3580.424 ops/ms
Iteration   1: 3572.294 ops/ms
Iteration   2: 3530.916 ops/ms
Iteration   3: 3509.595 ops/ms
Iteration   4: 3555.287 ops/ms
Iteration   5: 3571.044 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect":
  3547.827 ±(99.9%) 104.412 ops/ms [Average]
  (min, avg, max) = (3509.595, 3547.827, 3572.294), stdev = 27.115
  CI (99.9%): [3443.415, 3652.239] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.gson

# Run progress: 77.92% complete, ETA 00:22:29
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 404.070 ops/ms
# Warmup Iteration   2: 420.143 ops/ms
# Warmup Iteration   3: 421.187 ops/ms
Iteration   1: 420.263 ops/ms
Iteration   2: 413.103 ops/ms
Iteration   3: 419.607 ops/ms
Iteration   4: 418.789 ops/ms
Iteration   5: 421.186 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.gson":
  418.590 ±(99.9%) 12.286 ops/ms [Average]
  (min, avg, max) = (413.103, 418.590, 421.186), stdev = 3.191
  CI (99.9%): [406.304, 430.875] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.jackson

# Run progress: 79.22% complete, ETA 00:21:10
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1181.259 ops/ms
# Warmup Iteration   2: 1217.019 ops/ms
# Warmup Iteration   3: 1224.168 ops/ms
Iteration   1: 1220.242 ops/ms
Iteration   2: 1223.821 ops/ms
Iteration   3: 1221.219 ops/ms
Iteration   4: 1225.613 ops/ms
Iteration   5: 1221.059 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.jackson":
  1222.391 ±(99.9%) 8.652 ops/ms [Average]
  (min, avg, max) = (1220.242, 1222.391, 1225.613), stdev = 2.247
  CI (99.9%): [1213.739, 1231.043] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.wast

# Run progress: 80.52% complete, ETA 00:19:51
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3571.204 ops/ms
# Warmup Iteration   2: 3640.553 ops/ms
# Warmup Iteration   3: 3628.986 ops/ms
Iteration   1: 3646.127 ops/ms
Iteration   2: 3678.001 ops/ms
Iteration   3: 3670.222 ops/ms
Iteration   4: 3746.176 ops/ms
Iteration   5: 3737.954 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.ClientsWriteUTF8Bytes.wast":
  3695.696 ±(99.9%) 169.527 ops/ms [Average]
  (min, avg, max) = (3646.127, 3695.696, 3746.176), stdev = 44.026
  CI (99.9%): [3526.169, 3865.223] (assumes normal distribution)


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

# Run progress: 81.82% complete, ETA 00:18:32
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2830.508 ops/ms
# Warmup Iteration   2: 2918.893 ops/ms
# Warmup Iteration   3: 2944.004 ops/ms
Iteration   1: 2932.030 ops/ms
Iteration   2: 2917.951 ops/ms
Iteration   3: 2938.746 ops/ms
Iteration   4: 2936.823 ops/ms
Iteration   5: 2930.205 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson2":
  2931.151 ±(99.9%) 31.388 ops/ms [Average]
  (min, avg, max) = (2917.951, 2931.151, 2938.746), stdev = 8.151
  CI (99.9%): [2899.763, 2962.539] (assumes normal distribution)


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

# Run progress: 83.12% complete, ETA 00:17:13
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2787.073 ops/ms
# Warmup Iteration   2: 2815.915 ops/ms
# Warmup Iteration   3: 2803.243 ops/ms
Iteration   1: 2822.199 ops/ms
Iteration   2: 2808.237 ops/ms
Iteration   3: 2791.145 ops/ms
Iteration   4: 2815.443 ops/ms
Iteration   5: 2812.704 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3":
  2809.946 ±(99.9%) 44.921 ops/ms [Average]
  (min, avg, max) = (2791.145, 2809.946, 2822.199), stdev = 11.666
  CI (99.9%): [2765.025, 2854.867] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3_asm

# Run progress: 84.42% complete, ETA 00:15:54
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2775.748 ops/ms
# Warmup Iteration   2: 2787.439 ops/ms
# Warmup Iteration   3: 2753.810 ops/ms
Iteration   1: 2786.580 ops/ms
Iteration   2: 2835.432 ops/ms
Iteration   3: 2824.444 ops/ms
Iteration   4: 2826.747 ops/ms
Iteration   5: 2830.957 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3_asm":
  2820.832 ±(99.9%) 75.479 ops/ms [Average]
  (min, avg, max) = (2786.580, 2820.832, 2835.432), stdev = 19.602
  CI (99.9%): [2745.353, 2896.311] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3_reflect

# Run progress: 85.71% complete, ETA 00:14:34
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2755.214 ops/ms
# Warmup Iteration   2: 2798.110 ops/ms
# Warmup Iteration   3: 2790.995 ops/ms
Iteration   1: 2766.873 ops/ms
Iteration   2: 2784.871 ops/ms
Iteration   3: 2776.507 ops/ms
Iteration   4: 2795.730 ops/ms
Iteration   5: 2800.640 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.fastjson3_reflect":
  2784.924 ±(99.9%) 53.093 ops/ms [Average]
  (min, avg, max) = (2766.873, 2784.924, 2800.640), stdev = 13.788
  CI (99.9%): [2731.831, 2838.018] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.gson

# Run progress: 87.01% complete, ETA 00:13:15
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 783.124 ops/ms
# Warmup Iteration   2: 813.157 ops/ms
# Warmup Iteration   3: 820.031 ops/ms
Iteration   1: 818.286 ops/ms
Iteration   2: 819.351 ops/ms
Iteration   3: 824.033 ops/ms
Iteration   4: 820.896 ops/ms
Iteration   5: 819.855 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.gson":
  820.484 ±(99.9%) 8.453 ops/ms [Average]
  (min, avg, max) = (818.286, 820.484, 824.033), stdev = 2.195
  CI (99.9%): [812.032, 828.937] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.jackson

# Run progress: 88.31% complete, ETA 00:11:56
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 871.870 ops/ms
# Warmup Iteration   2: 903.797 ops/ms
# Warmup Iteration   3: 901.755 ops/ms
Iteration   1: 896.858 ops/ms
Iteration   2: 902.778 ops/ms
Iteration   3: 899.933 ops/ms
Iteration   4: 901.646 ops/ms
Iteration   5: 900.595 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.jackson":
  900.362 ±(99.9%) 8.611 ops/ms [Average]
  (min, avg, max) = (896.858, 900.362, 902.778), stdev = 2.236
  CI (99.9%): [891.751, 908.973] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.wast

# Run progress: 89.61% complete, ETA 00:10:36
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 2686.494 ops/ms
# Warmup Iteration   2: 2767.806 ops/ms
# Warmup Iteration   3: 2774.209 ops/ms
Iteration   1: 2782.299 ops/ms
Iteration   2: 2776.289 ops/ms
Iteration   3: 2785.572 ops/ms
Iteration   4: 2809.501 ops/ms
Iteration   5: 2804.286 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersParseUTF8Bytes.wast":
  2791.589 ±(99.9%) 55.756 ops/ms [Average]
  (min, avg, max) = (2776.289, 2791.589, 2809.501), stdev = 14.480
  CI (99.9%): [2735.834, 2847.345] (assumes normal distribution)


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

# Run progress: 90.91% complete, ETA 00:09:17
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3137.240 ops/ms
# Warmup Iteration   2: 3261.768 ops/ms
# Warmup Iteration   3: 3287.944 ops/ms
Iteration   1: 3283.647 ops/ms
Iteration   2: 3288.355 ops/ms
Iteration   3: 3283.267 ops/ms
Iteration   4: 3287.967 ops/ms
Iteration   5: 3269.917 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson2":
  3282.630 ±(99.9%) 28.836 ops/ms [Average]
  (min, avg, max) = (3269.917, 3282.630, 3288.355), stdev = 7.489
  CI (99.9%): [3253.795, 3311.466] (assumes normal distribution)


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

# Run progress: 92.21% complete, ETA 00:07:57
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3913.719 ops/ms
# Warmup Iteration   2: 3996.467 ops/ms
# Warmup Iteration   3: 3984.126 ops/ms
Iteration   1: 4007.411 ops/ms
Iteration   2: 3918.117 ops/ms
Iteration   3: 3991.804 ops/ms
Iteration   4: 3979.746 ops/ms
Iteration   5: 3991.242 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3":
  3977.664 ±(99.9%) 133.659 ops/ms [Average]
  (min, avg, max) = (3918.117, 3977.664, 4007.411), stdev = 34.711
  CI (99.9%): [3844.005, 4111.323] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3_asm

# Run progress: 93.51% complete, ETA 00:06:38
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 4008.160 ops/ms
# Warmup Iteration   2: 4000.157 ops/ms
# Warmup Iteration   3: 4027.506 ops/ms
Iteration   1: 4026.013 ops/ms
Iteration   2: 4021.044 ops/ms
Iteration   3: 4037.001 ops/ms
Iteration   4: 4033.849 ops/ms
Iteration   5: 4031.730 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3_asm":
  4029.927 ±(99.9%) 24.573 ops/ms [Average]
  (min, avg, max) = (4021.044, 4029.927, 4037.001), stdev = 6.382
  CI (99.9%): [4005.354, 4054.500] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3_reflect

# Run progress: 94.81% complete, ETA 00:05:18
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3822.082 ops/ms
# Warmup Iteration   2: 3867.846 ops/ms
# Warmup Iteration   3: 3838.730 ops/ms
Iteration   1: 3857.487 ops/ms
Iteration   2: 3868.675 ops/ms
Iteration   3: 3869.340 ops/ms
Iteration   4: 3860.689 ops/ms
Iteration   5: 3869.707 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.fastjson3_reflect":
  3865.180 ±(99.9%) 21.898 ops/ms [Average]
  (min, avg, max) = (3857.487, 3865.180, 3869.707), stdev = 5.687
  CI (99.9%): [3843.282, 3887.078] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.gson

# Run progress: 96.10% complete, ETA 00:03:58
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 365.615 ops/ms
# Warmup Iteration   2: 371.017 ops/ms
# Warmup Iteration   3: 371.313 ops/ms
Iteration   1: 371.172 ops/ms
Iteration   2: 371.674 ops/ms
Iteration   3: 368.189 ops/ms
Iteration   4: 370.127 ops/ms
Iteration   5: 372.008 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.gson":
  370.634 ±(99.9%) 5.932 ops/ms [Average]
  (min, avg, max) = (368.189, 370.634, 372.008), stdev = 1.540
  CI (99.9%): [364.702, 376.566] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.jackson

# Run progress: 97.40% complete, ETA 00:02:39
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 1270.371 ops/ms
# Warmup Iteration   2: 1312.669 ops/ms
# Warmup Iteration   3: 1311.867 ops/ms
Iteration   1: 1310.906 ops/ms
Iteration   2: 1313.486 ops/ms
Iteration   3: 1313.816 ops/ms
Iteration   4: 1296.725 ops/ms
Iteration   5: 1310.003 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.jackson":
  1308.987 ±(99.9%) 27.134 ops/ms [Average]
  (min, avg, max) = (1296.725, 1308.987, 1313.816), stdev = 7.047
  CI (99.9%): [1281.853, 1336.122] (assumes normal distribution)


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
# Benchmark: com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.wast

# Run progress: 98.70% complete, ETA 00:01:19
# Fork: 1 of 1
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/root/bench-fj3/benchmark3.jar)
WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
# Warmup Iteration   1: 3138.660 ops/ms
# Warmup Iteration   2: 3189.740 ops/ms
# Warmup Iteration   3: 3181.494 ops/ms
Iteration   1: 3182.600 ops/ms
Iteration   2: 3184.805 ops/ms
Iteration   3: 3196.721 ops/ms
Iteration   4: 3233.967 ops/ms
Iteration   5: 3224.697 ops/ms


Result "com.alibaba.fastjson3.benchmark.jjb.UsersWriteUTF8Bytes.wast":
  3204.558 ±(99.9%) 90.391 ops/ms [Average]
  (min, avg, max) = (3182.600, 3204.558, 3233.967), stdev = 23.474
  CI (99.9%): [3114.167, 3294.949] (assumes normal distribution)


# Run complete. Total time: 01:42:17

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

Benchmark                                                 Mode  Cnt      Score      Error   Units
c.a.f.b.eishay.EishayParseString.fastjson2               thrpt    5  13306.435 ±  117.637  ops/ms
c.a.f.b.eishay.EishayParseString.fastjson3               thrpt    5   9243.924 ±  169.774  ops/ms
c.a.f.b.eishay.EishayParseString.fastjson3_preconv_utf8  thrpt    5  10614.938 ±  105.062  ops/ms
c.a.f.b.eishay.EishayParseString.gson                    thrpt    5   3097.517 ±   21.716  ops/ms
c.a.f.b.eishay.EishayParseString.jackson                 thrpt    5   3230.038 ±   24.316  ops/ms
c.a.f.b.eishay.EishayParseString.wast                    thrpt    5  10736.048 ± 1588.154  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.fastjson2         thrpt    5   9513.878 ±  219.347  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.fastjson3         thrpt    5   7475.937 ±  100.771  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.gson              thrpt    5   2888.895 ±   28.082  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.jackson           thrpt    5   2798.049 ±   11.688  ops/ms
c.a.f.b.eishay.EishayParseStringPretty.wast              thrpt    5   9238.378 ±  520.540  ops/ms
c.a.f.b.eishay.EishayParseTreeString.fastjson2           thrpt    5   7328.489 ±   86.250  ops/ms
c.a.f.b.eishay.EishayParseTreeString.fastjson3           thrpt    5   5263.684 ±   63.818  ops/ms
c.a.f.b.eishay.EishayParseTreeString.gson                thrpt    5   2847.764 ±   15.675  ops/ms
c.a.f.b.eishay.EishayParseTreeString.jackson             thrpt    5   3359.153 ±   35.579  ops/ms
c.a.f.b.eishay.EishayParseTreeString.wast                thrpt    5   7195.100 ±  223.026  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2        thrpt    5   7384.272 ±   84.589  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3        thrpt    5   5260.572 ±   46.106  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.gson             thrpt    5   2808.369 ±   25.290  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.jackson          thrpt    5   3750.964 ±   30.484  ops/ms
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast             thrpt    5   6899.199 ±  108.229  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2            thrpt    5  13210.851 ±  138.651  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3            thrpt    5  10609.305 ±  136.979  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm        thrpt    5  11208.592 ±  202.113  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect    thrpt    5  11176.057 ±  209.466  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.gson                 thrpt    5   2972.046 ±   25.853  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.jackson              thrpt    5   3387.030 ±   46.195  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.wast                 thrpt    5  10840.972 ±  626.718  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.fastjson2      thrpt    5   9644.593 ±   57.003  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.fastjson3      thrpt    5   8950.058 ±   97.100  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.gson           thrpt    5   2683.587 ±   22.338  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.jackson        thrpt    5   3003.524 ±   36.315  ops/ms
c.a.f.b.eishay.EishayParseUTF8BytesPretty.wast           thrpt    5   7837.325 ±  433.975  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson2               thrpt    5  13728.671 ±   61.311  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson3               thrpt    5  12146.813 ±  175.133  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson3_asm           thrpt    5   8357.558 ±  153.965  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson3_reflect       thrpt    5   8119.562 ±  278.211  ops/ms
c.a.f.b.eishay.EishayWriteString.fastjson3_utf8latin1    thrpt    5  11638.976 ±  166.423  ops/ms
c.a.f.b.eishay.EishayWriteString.gson                    thrpt    5   1428.713 ±    5.608  ops/ms
c.a.f.b.eishay.EishayWriteString.jackson                 thrpt    5   5952.755 ±   90.358  ops/ms
c.a.f.b.eishay.EishayWriteString.wast                    thrpt    5  10429.157 ± 1723.432  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2            thrpt    5  17784.179 ±  116.925  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3            thrpt    5  12296.696 ±  100.753  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm        thrpt    5  11922.795 ±  281.098  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect    thrpt    5  11826.847 ±  118.867  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.gson                 thrpt    5   1410.952 ±    4.130  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.jackson              thrpt    5   5978.506 ±   73.032  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast                 thrpt    5  11459.229 ± 2037.103  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2              thrpt    5   2715.568 ±   53.497  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3              thrpt    5   2713.387 ±   20.440  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm          thrpt    5   2790.872 ±   21.631  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect      thrpt    5   2753.434 ±   20.308  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.gson                   thrpt    5    744.428 ±   14.788  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.jackson                thrpt    5    866.210 ±    3.399  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                   thrpt    5   2666.386 ±  336.430  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2              thrpt    5   3136.722 ±   31.201  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3              thrpt    5   3732.425 ±    4.722  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm          thrpt    5   3563.861 ±  114.217  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect      thrpt    5   3547.827 ±  104.412  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.gson                   thrpt    5    418.590 ±   12.286  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.jackson                thrpt    5   1222.391 ±    8.652  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                   thrpt    5   3695.696 ±  169.527  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2                thrpt    5   2931.151 ±   31.388  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3                thrpt    5   2809.946 ±   44.921  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm            thrpt    5   2820.832 ±   75.479  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect        thrpt    5   2784.924 ±   53.093  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.gson                     thrpt    5    820.484 ±    8.453  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.jackson                  thrpt    5    900.362 ±    8.611  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                     thrpt    5   2791.589 ±   55.756  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2                thrpt    5   3282.630 ±   28.836  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3                thrpt    5   3977.664 ±  133.659  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm            thrpt    5   4029.927 ±   24.573  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect        thrpt    5   3865.180 ±   21.898  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.gson                     thrpt    5    370.634 ±    5.932  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.jackson                  thrpt    5   1308.987 ±   27.134  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                     thrpt    5   3204.558 ±   90.391  ops/ms

Benchmark result is saved to /root/bench-fj3/results/x86-172.16.172.143/results.json
=== FINISHED 2026-04-12T16:24:19+08:00 ===
```


# fastjson3 3.0.0-SNAPSHOT-7e8da7e — Raw JMH Output

Companion to [`benchmark_3.0.0-SNAPSHOT-7e8da7e.md`](benchmark_3.0.0-SNAPSHOT-7e8da7e.md). One section per machine; each row's `Score ± Error` is `ops/ms` averaged over `Cnt = forks × measurement-iters`.

## x86_64 (`root@172.16.172.143`, 16c, Zulu 25.0.2, 16 threads)

`-wi 3 -i 5 -f 2 -t 16 -bm thrpt -tu ms`

```
Benchmark                                               Mode  Cnt      Score      Error   Units
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt   10  13296.832 ±  123.487  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt   10  14618.562 ±  211.989  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt   10  15277.718 ±   72.811  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt   10  11156.610 ±  158.604  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt   10  10811.074 ±  406.552  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2¹         thrpt   10  17839.963 ±  547.525  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt   10  20496.287 ±  848.981  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt   10  20001.106 ±  566.047  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt   10  17807.064 ±  260.254  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt   10  10448.483 ±  662.053  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt   10   2739.315 ±   44.644  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt   10   2686.869 ±   14.312  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt   10   2737.948 ±   74.360  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt   10   2704.357 ±   45.892  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt   10   2717.356 ±  246.106  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt   10   3145.453 ±  186.380  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt   10   3774.830 ±   22.501  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt   10   3829.315 ±   31.000  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt   10   3833.062 ±   56.957  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt   10   3556.923 ±  219.828  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt   10   2962.884 ±   37.664  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt   10   2657.077 ±   60.137  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt   10   2727.916 ±   24.877  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt   10   2724.901 ±    9.886  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt   10   2746.354 ±   41.648  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt   10   3261.973 ±   50.693  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt   10   4580.815 ±   37.322  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt   10   4610.380 ±  236.187  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt   10   4148.161 ±   41.908  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt   10   3083.204 ±   39.401  ops/ms
```

¹ EishayWriteUTF8Bytes.fastjson2 re-run in isolation; the combined-run measurement was `14469.991 ± 4429.457` (> 30% error bar — noisy neighbor on the box).

## aarch64 16c — Cortex-A76 (`root@172.16.1.231`, Zulu 25.0.2, 16 threads)

`-wi 3 -i 5 -f 2 -t 16 -bm thrpt -tu ms`

```
Benchmark                                               Mode  Cnt     Score      Error   Units
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt   10  6935.784 ±  108.256  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt   10  7760.618 ±   27.806  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt   10  7880.120 ±  271.127  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt   10  5761.598 ± 1365.116  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt   10  4850.104 ±  233.880  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt   10  8035.108 ±   58.095  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt   10  8622.748 ±  171.269  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt   10  9114.102 ±   93.946  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt   10  7799.012 ±   37.132  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt   10  5857.170 ±  697.613  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt   10  1333.945 ±   54.384  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt   10  1616.353 ±   23.183  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt   10  1637.717 ±    9.276  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt   10  1602.232 ±    6.292  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt   10  1421.781 ±   23.990  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt   10  1999.334 ±   16.558  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt   10  2213.143 ±   16.200  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt   10  2194.398 ±   50.078  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt   10  2218.308 ±   74.118  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt   10  1769.311 ±   61.618  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt   10  1548.242 ±   21.372  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt   10  1432.015 ±    7.081  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt   10  1431.502 ±   11.478  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt   10  1418.963 ±   12.544  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt   10  1460.839 ±   67.230  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt   10  1997.404 ±   12.502  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt   10  2408.652 ±   32.966  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt   10  2385.490 ±   15.737  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt   10  2210.889 ±   41.775  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt   10  1807.497 ±   33.088  ops/ms
```

## arm3 — OrangePi 5+ 8c (`arm3`, Zulu 25, 8 threads)

`-wi 3 -i 5 -f 2 -t 8 -bm thrpt -tu ms`

```
Benchmark                                               Mode  Cnt     Score     Error   Units
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt   10  5064.417 ± 423.217  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt   10  5273.389 ± 254.569  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt   10  5184.193 ± 172.391  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt   10  3096.170 ±  71.883  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt   10  3035.426 ±  97.438  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt   10  5569.513 ± 130.367  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt   10  6748.464 ± 283.899  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt   10  6375.812 ± 219.523  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt   10  5433.743 ± 117.376  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt   10  3647.738 ± 312.270  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt   10   830.801 ±   9.105  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt   10   945.629 ±  30.565  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt   10   940.363 ±  17.359  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt   10   939.790 ±  12.814  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt   10   869.429 ±  19.581  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt   10  1258.531 ±  27.963  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt   10  1373.163 ±  17.030  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt   10  1372.142 ±  19.929  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt   10  1310.754 ±  18.517  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt   10  1112.904 ±  19.700  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt   10   824.822 ±   9.073  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt   10   881.725 ±  15.793  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt   10   890.827 ±  15.538  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt   10   885.167 ±  12.138  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt   10   819.031 ±   9.449  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt   10  1232.167 ±   6.868  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt   10  1539.486 ±  23.312  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt   10  1547.263 ±  15.035  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt   10  1302.988 ±   9.011  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt   10  1107.391 ±  16.092  ops/ms
```

## arm4 — OrangePi 6+ 12c (`arm4`, Zulu 25, 12 threads)

`-wi 3 -i 5 -f 2 -t 12 -bm thrpt -tu ms`

```
Benchmark                                               Mode  Cnt      Score     Error   Units
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt   10  14231.805 ± 127.642  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt   10  16434.994 ± 147.450  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt   10  16694.729 ± 175.230  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt   10   8458.506 ± 195.200  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt   10   9203.047 ± 342.509  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt   10  17422.679 ± 200.471  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt   10  19167.425 ± 392.419  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt   10  19011.433 ± 377.536  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt   10  16504.280 ± 121.342  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt   10  10146.759 ± 908.495  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt   10   2412.386 ±  52.013  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt   10   2755.201 ±  50.831  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt   10   2793.179 ±   6.640  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt   10   2752.778 ±  13.175  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt   10   2622.393 ±  42.050  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt   10   3657.008 ±  21.217  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt   10   4018.013 ±  15.802  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt   10   3967.389 ±   8.551  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt   10   3880.234 ±  79.998  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt   10   3043.759 ± 173.109  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt   10   2825.074 ±  35.533  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt   10   2657.863 ±  17.331  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt   10   2647.023 ±   8.653  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt   10   2628.810 ±  17.750  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt   10   2477.716 ±  42.495  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt   10   3767.594 ±  46.956  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt   10   4268.527 ±   8.933  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt   10   4243.575 ±  21.216  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt   10   3845.200 ±  83.578  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt   10   3115.522 ± 120.436  ops/ms
```

## RISC-V 8c (`riscv`, OpenJDK 25+37, 4 threads, lighter `-wi 2 -i 3 -f 1`)

Only `fastjson2` + `fastjson3_asm` columns — per-fork startup is ~5× slower than aarch64, full library matrix locked the box.

```
Benchmark                                           Mode  Cnt     Score     Error   Units
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2      thrpt    3   608.830 ± 113.495  ops/ms
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm  thrpt    3   813.370 ±  73.680  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2      thrpt    3   853.990 ±  47.097  ops/ms
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm  thrpt    3  1234.000 ±  99.161  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2        thrpt    3   125.958 ±  18.243  ops/ms
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm    thrpt    3   137.841 ±   1.457  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2        thrpt    3   182.097 ±  27.930  ops/ms
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm    thrpt    3   240.383 ±  44.581  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2          thrpt    3   129.468 ±   3.667  ops/ms
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm      thrpt    3   126.628 ±  14.874  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2          thrpt    3   167.324 ±  57.657  ops/ms
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm      thrpt    3   296.583 ±  15.295  ops/ms
```

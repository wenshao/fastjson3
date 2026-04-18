# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-94414d5 — Raw JMH Output

Raw `thrpt` output from JMH, per machine.

JMH command: `java -jar target/benchmark3.jar -wi 2 -i 3 -f 2 -t $threads "((Eishay|Users|Clients)(Parse|Write)UTF8Bytes\.(fastjson2|fastjson3|wast)|EishayParseTree(String|UTF8Bytes)\.(fastjson2|fastjson3|wast))"`

Note: JMH's filter is substring-based, so `fastjson3` also matches `fastjson3_asm` and `fastjson3_reflect`.

## x86_64 (`root@172.16.172.143`, 16c)

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
c.a.f.b.eishay.EishayParseTreeString.fastjson2         thrpt    6   7294474.187 ±  230618.024  ops/s
c.a.f.b.eishay.EishayParseTreeString.fastjson3         thrpt    6   7929149.391 ±   81972.932  ops/s
c.a.f.b.eishay.EishayParseTreeString.wast              thrpt    6   7588537.004 ± 1371217.802  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2      thrpt    6   7318313.128 ±  401229.690  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3      thrpt    6   7873635.989 ±  148880.303  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast           thrpt    6   7779181.357 ± 1728684.849  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt    6  13466923.501 ±  259286.373  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt    6  14987006.953 ±  168983.756  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt    6  14803924.956 ±  357763.756  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt    6  11049910.597 ±  951984.831  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt    6  10814217.304 ±  280033.738  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt    6  17627049.652 ±  455802.163  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt    6  18512396.950 ± 1949630.692  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt    6  21219602.537 ± 1267988.978  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt    6  17768720.220 ±  387631.013  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt    6  10657409.869 ±  305655.808  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt    6   2765310.482 ±   23395.129  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt    6   2796709.421 ±  141717.629  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt    6   2848645.083 ±   27041.445  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt    6   2740525.832 ±   34377.058  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt    6   2767149.633 ±   74116.074  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt    6   3109583.333 ±   71570.847  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt    6   3803352.117 ±   34909.755  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt    6   3890249.167 ±   69404.922  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt    6   3960744.161 ±  173205.448  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt    6   3536479.440 ±   76335.427  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt    6   2999418.639 ±   61116.772  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt    6   2815125.778 ±   20911.868  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt    6   2815863.668 ±   91537.129  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt    6   2787174.123 ±   81328.559  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt    6   2710575.097 ±   81430.169  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt    6   3311537.031 ±  346724.256  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt    6   4675292.122 ±  187536.206  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt    6   4727798.914 ±  264103.486  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt    6   4118719.185 ±   40067.542  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt    6   3157339.825 ±  122281.160  ops/s
```

## aarch64 16c (`root@172.16.1.231`, Cortex-A76)

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
c.a.f.b.eishay.EishayParseTreeString.fastjson2         thrpt    6  3856462.740 ±  80862.868  ops/s
c.a.f.b.eishay.EishayParseTreeString.fastjson3         thrpt    6  4796170.312 ± 340842.168  ops/s
c.a.f.b.eishay.EishayParseTreeString.wast              thrpt    6  4068620.856 ± 654042.600  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2      thrpt    6  3790143.538 ±  70792.070  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3      thrpt    6  4863200.633 ±  40631.893  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast           thrpt    6  3539340.859 ± 167442.314  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt    6  6867656.133 ± 211036.674  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt    6  7508565.516 ± 125197.481  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt    6  7936999.314 ± 892861.285  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt    6  4896037.006 ± 578044.087  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt    6  4744254.481 ±  31965.107  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt    6  8031617.528 ± 318803.283  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt    6  8990474.560 ± 137801.006  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt    6  9072128.737 ± 161001.872  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt    6  8034789.948 ± 256821.357  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt    6  5267144.930 ± 921791.804  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt    6  1351053.313 ±  81657.093  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt    6  1754014.415 ± 212496.435  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt    6  1687872.330 ± 154380.143  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt    6  1595189.768 ± 208625.966  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt    6  1416068.201 ±  22039.003  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt    6  1990592.526 ±  52323.823  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt    6  2226752.699 ±  29158.416  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt    6  2192852.695 ±  92258.653  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt    6  2284716.572 ±  58059.386  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt    6  1827189.706 ±  25998.579  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt    6  1650725.915 ±  33514.465  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt    6  1649921.026 ± 240253.357  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt    6  1635764.997 ± 352879.828  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt    6  1543787.121 ±  32456.371  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt    6  1421466.073 ±  71574.907  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt    6  2004438.391 ±  11392.754  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt    6  2437583.381 ±  13907.443  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt    6  2414447.137 ±  31029.397  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt    6  2235619.853 ±  15134.500  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt    6  1822631.994 ±  19512.239  ops/s
```

## arm3 8c (OrangePi 5+, `orangepi5plus`)

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
c.a.f.b.eishay.EishayParseTreeString.fastjson2         thrpt    6  2641173.475 ± 221025.612  ops/s
c.a.f.b.eishay.EishayParseTreeString.fastjson3         thrpt    6  2837611.332 ± 145588.546  ops/s
c.a.f.b.eishay.EishayParseTreeString.wast              thrpt    6  2113926.526 ± 131033.655  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2      thrpt    6  2196110.721 ± 175674.656  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3      thrpt    6  2619005.842 ±  62387.265  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast           thrpt    6  2055112.548 ± 155063.655  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt    6  4419938.116 ± 116559.041  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt    6  4927991.786 ± 134396.615  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt    6  5080050.947 ± 165701.112  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt    6  3072980.987 ±  93198.849  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt    6  3010128.141 ±  90925.679  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt    6  5492487.394 ±  99861.969  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt    6  6973932.099 ± 680875.369  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt    6  6699079.485 ± 123582.747  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt    6  5517291.624 ± 151189.239  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt    6  3482297.806 ±  29363.444  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt    6   856344.547 ± 123302.635  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt    6  1026484.049 ±  70445.749  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt    6  1034567.150 ±  81924.392  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt    6   975968.772 ±  41045.005  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt    6   878932.576 ±  25813.843  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt    6  1238354.506 ±  15077.555  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt    6  1388367.414 ±  47984.520  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt    6  1394949.172 ±  35397.130  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt    6  1319995.366 ±  70172.643  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt    6  1130969.380 ±  40219.119  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt    6   890359.328 ± 168276.967  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt    6   984182.582 ± 102336.347  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt    6   971587.660 ±  63465.512  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt    6   904010.258 ±  41755.585  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt    6   814828.141 ±  21470.144  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt    6  1235147.990 ±  44937.650  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt    6  1533358.706 ±  97780.803  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt    6  1570396.113 ±  40919.369  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt    6  1321007.942 ±  14964.474  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt    6  1120336.209 ±  41617.256  ops/s
```

## arm4 12c (`orangepi6plus`)

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
c.a.f.b.eishay.EishayParseTreeString.fastjson2         thrpt    6   7160996.260 ±   90056.877  ops/s
c.a.f.b.eishay.EishayParseTreeString.fastjson3         thrpt    6   8199635.615 ±   69300.857  ops/s
c.a.f.b.eishay.EishayParseTreeString.wast              thrpt    6   6408569.469 ±  514372.173  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2      thrpt    6   6954745.057 ±   74427.672  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3      thrpt    6   8067624.490 ±   75066.467  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast           thrpt    6   6413132.216 ±   35108.882  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt    6  13967493.431 ±  189951.833  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt    6  16207351.523 ±   69121.913  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt    6  16616826.841 ±  327947.672  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt    6   8564929.640 ±  572719.825  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt    6   8894178.365 ±  512671.927  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt    6  16025950.207 ± 1064513.083  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt    6  16814149.588 ± 7017594.694  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt    6  19460420.199 ± 1528871.359  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt    6  15833720.929 ± 2442617.363  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt    6  10101043.105 ±  866106.171  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt    6   2473594.000 ±   14186.713  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt    6   3136280.058 ±   27113.027  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt    6   3187843.709 ±   21145.845  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt    6   2970960.300 ±   37671.755  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt    6   2630422.353 ±   24218.658  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt    6   3727014.510 ±  187283.316  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt    6   3943794.705 ±   43160.656  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt    6   3984881.003 ±   39378.100  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt    6   3877620.320 ±  128292.073  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt    6   3090280.091 ±   18957.346  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt    6   2500378.295 ±  127214.790  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt    6   3108263.126 ±   33340.203  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt    6   3088167.090 ±   93877.441  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt    6   2703260.755 ±   35791.598  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt    6   2485454.607 ±  193675.135  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt    6   3727415.894 ±  213583.494  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt    6   4301793.769 ±   61080.691  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt    6   4243125.422 ±   33264.936  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt    6   3915442.840 ±   30557.528  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt    6   3076917.032 ±   53711.587  ops/s
```

## RISC-V 8c (`orangepirv2`)

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
c.a.f.b.eishay.EishayParseTreeString.fastjson2         thrpt    6   611286.757 ±  23514.119  ops/s
c.a.f.b.eishay.EishayParseTreeString.fastjson3         thrpt    6   678927.466 ±  47662.810  ops/s
c.a.f.b.eishay.EishayParseTreeString.wast              thrpt    6   611724.166 ±  43986.895  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2      thrpt    6   575291.742 ±  30780.916  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3      thrpt    6   694086.073 ±  35818.094  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast           thrpt    6   589973.587 ±  29032.815  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt    6  1111382.051 ±  51667.997  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt    6  1330627.512 ±  33058.828  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt    6  1358927.246 ±  60552.843  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt    6   978550.975 ±  27323.356  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt    6   759425.665 ±  40948.121  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt    6  1600398.283 ± 134073.767  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt    6  2245997.762 ± 132761.294  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt    6  2145218.095 ± 122718.266  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt    6  1803798.001 ± 121582.249  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt    6   984312.974 ±  10655.484  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt    6   226869.534 ±  10699.104  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt    6   277118.354 ±  12075.510  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt    6   276796.714 ±  10240.541  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt    6   251613.334 ±   7650.376  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt    6   176320.409 ±   6506.839  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt    6   340877.215 ±  13344.191  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt    6   455103.542 ±  10277.199  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt    6   461270.841 ±  31925.734  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt    6   415415.979 ±  36290.520  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt    6   227893.992 ±   9424.744  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt    6   263351.240 ±  13231.498  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt    6   259553.672 ±  20114.163  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt    6   229979.952 ±   5398.832  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt    6   188596.159 ±   7417.956  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt    6   357044.519 ±  12983.360  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt    6   542646.016 ±  25021.119  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt    6   553382.715 ±  27414.398  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt    6   425134.637 ±  16202.132  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt    6   295157.491 ±  77835.180  ops/s
```


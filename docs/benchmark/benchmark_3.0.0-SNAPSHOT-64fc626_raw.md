# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-64fc626 — Raw JMH Output

Raw `-prof async` / `thrpt` output from JMH, per machine.

JMH command: `java -jar target/benchmark3.jar -wi 2 -i 3 -f 2 -t $threads "((Eishay|Users|Clients)(Parse|Write)UTF8Bytes\.(fastjson2|fastjson3|wast)|EishayParseTree(String|UTF8Bytes)\.(fastjson2|fastjson3|wast))"`

## x86_64 (`root@172.16.172.143`, 16c)

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
c.a.f.b.eishay.EishayParseTreeString.fastjson2         thrpt    6   7374727.211 ±  106583.681  ops/s
c.a.f.b.eishay.EishayParseTreeString.fastjson3         thrpt    6   7909115.831 ±  140835.080  ops/s
c.a.f.b.eishay.EishayParseTreeString.wast              thrpt    6   7275441.306 ±  270185.014  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2      thrpt    6   7397724.435 ±   71282.861  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3      thrpt    6   7905066.358 ±   61916.901  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast           thrpt    6   7083474.132 ±  219993.911  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt    6  13313332.954 ±  199121.947  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt    6  14782045.065 ± 1184965.006  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt    6  15043274.612 ±  347406.539  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt    6  11382423.109 ±  278669.338  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt    6  10579972.199 ±  144904.280  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt    6  17638874.714 ±  550896.985  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt    6  19342024.595 ± 2261892.100  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt    6  18455174.740 ±  267275.625  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt    6  16676614.617 ±   82744.356  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt    6  10166042.071 ±  801297.574  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt    6   2014264.402 ± 2314902.303  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt    6   2863376.271 ±  120641.514  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt    6   2724939.333 ±   55731.978  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt    6   2739212.060 ±   64111.303  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt    6   2843086.290 ±   64444.593  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt    6   3135937.025 ±   22595.348  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt    6   3934214.055 ±   87534.000  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt    6   3824744.175 ±   78321.513  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt    6   3926804.804 ±  225241.154  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt    6   3458520.449 ±   82327.015  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt    6   2932007.103 ±   57593.497  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt    6   2804773.824 ±   53696.504  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt    6   2809429.067 ±  174542.637  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt    6   2778199.078 ±   61492.275  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt    6   2747295.675 ±   46981.972  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt    6   3404198.191 ±  100777.412  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt    6   4656648.953 ±  260979.738  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt    6   4624124.448 ±  186006.698  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt    6   4140423.050 ±  283040.323  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt    6   3126919.250 ±  122146.060  ops/s
```

## aarch64 16c (`root@172.16.1.231`, Cortex-A76)

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
c.a.f.b.eishay.EishayParseTreeString.fastjson2         thrpt    6  3881820.757 ±   39675.960  ops/s
c.a.f.b.eishay.EishayParseTreeString.fastjson3         thrpt    6  4809924.569 ±  185005.376  ops/s
c.a.f.b.eishay.EishayParseTreeString.wast              thrpt    6  3686533.273 ±   79791.241  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2      thrpt    6  3841307.139 ±   98388.104  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3      thrpt    6  4808165.056 ±  140436.733  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast           thrpt    6  3571584.927 ±    6969.324  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt    6  6899041.555 ±   83279.686  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt    6  7497438.557 ±  340189.247  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt    6  7885796.395 ±  449289.549  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt    6  5935301.659 ± 2705266.660  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt    6  4741995.609 ±   28903.009  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt    6  7720004.853 ±  294983.121  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt    6  7916729.202 ±  205366.823  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt    6  8073294.331 ±  768730.833  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt    6  7360239.247 ±  260942.550  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt    6  5308927.849 ±  180631.482  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt    6  1335843.634 ±   32065.858  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt    6  1730554.647 ±   95642.237  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt    6  1677279.620 ±   88801.200  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt    6  1627692.181 ±   42573.822  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt    6  1419036.485 ±   78210.917  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt    6  2007308.038 ±   27280.223  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt    6  2223141.011 ±   24618.990  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt    6  2184767.109 ±   23527.026  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt    6  2219828.543 ±   94527.626  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt    6  1786682.264 ±    8377.036  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt    6  1524359.410 ±   49103.630  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt    6  1663378.061 ±  346032.600  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt    6  1587427.798 ±  310401.399  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt    6  1502269.427 ±   20720.263  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt    6  1446367.911 ±   39653.659  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt    6  2008212.921 ±   32895.827  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt    6  2437382.401 ±   47097.746  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt    6  2429479.205 ±   90036.736  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt    6  2161432.663 ±  150085.777  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt    6  1831642.486 ±   34288.991  ops/s
```

## arm3 8c (OrangePi 5+, `orangepi5plus`)

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
c.a.f.b.eishay.EishayParseTreeString.fastjson2         thrpt    6  2660642.371 ± 304650.325  ops/s
c.a.f.b.eishay.EishayParseTreeString.fastjson3         thrpt    6  2841888.229 ±  94497.281  ops/s
c.a.f.b.eishay.EishayParseTreeString.wast              thrpt    6  2091796.732 ± 144018.343  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2      thrpt    6  2265914.286 ± 133445.938  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3      thrpt    6  2638664.780 ±  53564.022  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast           thrpt    6  2055597.429 ± 193768.098  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt    6  4287712.170 ± 151336.579  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt    6  4904482.803 ± 365221.614  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt    6  5046305.539 ± 178122.222  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt    6  3058404.452 ±  64082.854  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt    6  3003429.452 ± 105052.613  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt    6  5500565.276 ± 301978.615  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt    6  6216391.868 ± 533405.529  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt    6  6053373.898 ± 360020.977  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt    6  5487923.538 ± 170569.786  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt    6  3535206.883 ±  97263.351  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt    6   822731.362 ±  38460.938  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt    6  1034074.337 ±  31745.547  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt    6  1036498.991 ±  15424.283  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt    6  1002939.502 ±  48286.846  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt    6   868290.679 ±  26576.544  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt    6  1240028.597 ±   4733.802  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt    6  1395105.074 ±  36735.158  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt    6  1392721.250 ±  45384.151  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt    6  1339815.955 ±  38625.873  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt    6  1125630.425 ±  25116.829  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt    6   927859.446 ±  78530.845  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt    6   948154.041 ±  20520.740  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt    6   969212.306 ± 110826.097  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt    6   901277.762 ±  34377.504  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt    6   826619.391 ±  12293.003  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt    6  1231429.575 ±  16989.160  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt    6  1584796.149 ±  40303.762  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt    6  1580078.579 ±  44577.171  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt    6  1298839.429 ± 103907.210  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt    6  1125191.537 ±  49868.919  ops/s
```

## arm4 12c (`orangepi6plus`)

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
c.a.f.b.eishay.EishayParseTreeString.fastjson2         thrpt    6   7158259.472 ± 109822.203  ops/s
c.a.f.b.eishay.EishayParseTreeString.fastjson3         thrpt    6   8134999.151 ±  87716.093  ops/s
c.a.f.b.eishay.EishayParseTreeString.wast              thrpt    6   6460356.530 ± 455806.703  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2      thrpt    6   6935514.815 ±  57368.774  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3      thrpt    6   8022826.106 ±  53627.081  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast           thrpt    6   6054090.426 ±  37477.252  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson2          thrpt    6  13598226.724 ± 415860.424  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt    6  15981577.200 ± 167120.353  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt    6  16482699.012 ±  74956.827  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt    6   8416998.264 ± 142422.453  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt    6   8790139.944 ± 164072.730  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt    6  17286598.644 ± 342976.476  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt    6  16926784.165 ± 500969.676  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt    6  17144708.245 ± 938055.804  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt    6  15222491.121 ± 349156.389  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt    6   9556214.011 ± 861647.518  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt    6   2446423.001 ±  56991.927  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt    6   3109842.068 ± 106907.906  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt    6   3141616.772 ±  26308.194  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt    6   2964241.272 ±  46104.136  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt    6   2666423.600 ±  77408.509  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt    6   3740439.652 ±  76929.109  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt    6   4008378.836 ±  33836.426  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt    6   3992711.857 ± 114879.324  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt    6   3986400.310 ±  17384.977  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt    6   3179965.111 ±  41001.552  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt    6   2645419.289 ± 587825.149  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt    6   3067162.101 ±  39232.865  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt    6   3087638.713 ±  70619.999  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt    6   2690696.448 ±  20273.861  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt    6   2496247.949 ± 241688.585  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt    6   3752149.323 ± 131259.550  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt    6   4289535.950 ± 100091.005  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt    6   4285313.381 ±  81530.363  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt    6   3886833.926 ± 177999.689  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt    6   3173872.373 ± 133802.329  ops/s
```

## RISC-V 8c (`orangepirv2`)

```
Benchmark                                                    Mode  Cnt        Score         Error  Units
c.a.f.b.eishay.EishayParseTreeString.fastjson2         thrpt    6   613922.290 ± 15771.510  ops/s
c.a.f.b.eishay.EishayParseTreeString.fastjson3         thrpt    6   682394.760 ± 36759.949  ops/s
c.a.f.b.eishay.EishayParseTreeString.wast              thrpt    6   618046.972 ± 29357.985  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson2      thrpt    6   581977.267 ± 23514.387  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.fastjson3      thrpt    6   670208.414 ± 16613.541  ops/s
c.a.f.b.eishay.EishayParseTreeUTF8Bytes.wast           thrpt    6   585899.851 ± 11860.924  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3          thrpt    6  1360177.825 ± 61367.886  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_asm      thrpt    6  1357865.478 ± 49062.359  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.fastjson3_reflect  thrpt    6   966603.459 ± 30626.051  ops/s
c.a.f.b.eishay.EishayParseUTF8Bytes.wast               thrpt    6   724022.866 ± 48298.367  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson2          thrpt    6  1631544.557 ± 55876.003  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3          thrpt    6  2102182.743 ±  7840.961  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_asm      thrpt    6  2075576.146 ±  4667.789  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.fastjson3_reflect  thrpt    6  1782308.755 ± 57753.035  ops/s
c.a.f.b.eishay.EishayWriteUTF8Bytes.wast               thrpt    6   999578.438 ± 59833.768  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson2            thrpt    6   223434.548 ±  9926.139  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3            thrpt    6   276139.173 ± 13195.904  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_asm        thrpt    6   279780.525 ± 10087.426  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.fastjson3_reflect    thrpt    6   252502.447 ±  7804.180  ops/s
c.a.f.b.jjb.ClientsParseUTF8Bytes.wast                 thrpt    6   173479.178 ± 16114.030  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson2            thrpt    6   316850.755 ± 51565.263  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3            thrpt    6   448939.616 ± 30021.692  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_asm        thrpt    6   447917.407 ± 17741.604  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.fastjson3_reflect    thrpt    6   412431.069 ± 51598.616  ops/s
c.a.f.b.jjb.ClientsWriteUTF8Bytes.wast                 thrpt    6   243033.310 ±  9970.700  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson2              thrpt    6   230238.475 ±  9188.879  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3              thrpt    6   264658.581 ± 13680.619  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_asm          thrpt    6   263132.984 ± 10163.006  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.fastjson3_reflect      thrpt    6   230384.216 ±  8357.735  ops/s
c.a.f.b.jjb.UsersParseUTF8Bytes.wast                   thrpt    6   187486.286 ±  6574.601  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson2              thrpt    6   355572.112 ± 13771.993  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3              thrpt    6   549831.195 ± 15487.552  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_asm          thrpt    6   533243.901 ± 13315.859  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.fastjson3_reflect      thrpt    6   440619.398 ± 20431.798  ops/s
c.a.f.b.jjb.UsersWriteUTF8Bytes.wast                   thrpt    6   302476.560 ± 77742.170  ops/s
```


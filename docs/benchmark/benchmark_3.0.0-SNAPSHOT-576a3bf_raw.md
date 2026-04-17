# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-576a3bf — Raw JMH Output

Paired raw data for [`benchmark_3.0.0-SNAPSHOT-576a3bf.md`](benchmark_3.0.0-SNAPSHOT-576a3bf.md).
Each section contains the JMH summary table for one machine, with error bars.


## x86_64 (172.16.172.143, 16c)

```
EishayParseUTF8Bytes.fastjson2 thrpt 6 13274181.898 ± 68340.331 ops/s
EishayParseUTF8Bytes.fastjson3_asm thrpt 6 14624440.931 ± 436562.630 ops/s
EishayParseUTF8Bytes.fastjson3_reflect thrpt 6 10903279.579 ± 183078.780 ops/s
EishayParseUTF8Bytes.fastjson3 thrpt 6 14899107.341 ± 251729.104 ops/s
EishayParseUTF8Bytes.gson thrpt 6 3035617.591 ± 40672.198 ops/s
EishayParseUTF8Bytes.jackson thrpt 6 3372048.058 ± 325005.125 ops/s
EishayParseUTF8BytesPretty.fastjson2 thrpt 6 9302722.084 ± 38166.863 ops/s
EishayParseUTF8BytesPretty.fastjson3 thrpt 6 11354063.149 ± 541312.705 ops/s
EishayParseUTF8BytesPretty.gson thrpt 6 2760599.699 ± 39933.310 ops/s
EishayParseUTF8BytesPretty.jackson thrpt 6 3074819.347 ± 34868.103 ops/s
EishayParseUTF8BytesPretty.wast thrpt 6 8917943.291 ± 164971.152 ops/s
EishayParseUTF8Bytes.wast thrpt 6 10653953.174 ± 475134.104 ops/s
EishayWriteUTF8Bytes.fastjson2 thrpt 6 17558073.820 ± 1005781.481 ops/s
EishayWriteUTF8Bytes.fastjson3_asm thrpt 6 17986319.841 ± 1494857.349 ops/s
EishayWriteUTF8Bytes.fastjson3_reflect thrpt 6 15474379.204 ± 2096533.803 ops/s
EishayWriteUTF8Bytes.fastjson3 thrpt 6 17719219.542 ± 1530198.955 ops/s
EishayWriteUTF8Bytes.gson thrpt 6 1413441.015 ± 30685.559 ops/s
EishayWriteUTF8Bytes.jackson thrpt 6 6102034.120 ± 220422.345 ops/s
EishayWriteUTF8Bytes.wast thrpt 6 10061762.496 ± 1224684.802 ops/s
ClientsParseUTF8Bytes.fastjson2 thrpt 6 2783247.642 ± 31765.564 ops/s
ClientsParseUTF8Bytes.fastjson3_asm thrpt 6 2826070.641 ± 49441.115 ops/s
ClientsParseUTF8Bytes.fastjson3_reflect thrpt 6 2744758.429 ± 107792.757 ops/s
ClientsParseUTF8Bytes.fastjson3 thrpt 6 2780200.526 ± 100062.491 ops/s
ClientsParseUTF8Bytes.gson thrpt 6 745167.448 ± 32507.587 ops/s
ClientsParseUTF8Bytes.jackson thrpt 6 883021.619 ± 30237.838 ops/s
ClientsParseUTF8Bytes.wast thrpt 6 2805557.205 ± 54219.794 ops/s
ClientsWriteUTF8Bytes.fastjson2 thrpt 6 3123848.519 ± 153542.829 ops/s
ClientsWriteUTF8Bytes.fastjson3_asm thrpt 6 3889797.983 ± 193691.168 ops/s
ClientsWriteUTF8Bytes.fastjson3_reflect thrpt 6 3905736.129 ± 22460.424 ops/s
ClientsWriteUTF8Bytes.fastjson3 thrpt 6 3801469.681 ± 94358.604 ops/s
ClientsWriteUTF8Bytes.gson thrpt 6 427719.333 ± 15918.943 ops/s
ClientsWriteUTF8Bytes.jackson thrpt 6 1223371.392 ± 18686.230 ops/s
ClientsWriteUTF8Bytes.wast thrpt 6 3572805.886 ± 78414.067 ops/s
UsersParseUTF8Bytes.fastjson2 thrpt 6 2042723.763 ± 2876436.101 ops/s
UsersParseUTF8Bytes.fastjson3_asm thrpt 6 2838512.105 ± 62991.207 ops/s
UsersParseUTF8Bytes.fastjson3_reflect thrpt 6 2778210.355 ± 101349.354 ops/s
UsersParseUTF8Bytes.fastjson3 thrpt 6 2817974.084 ± 49510.414 ops/s
UsersParseUTF8Bytes.gson thrpt 6 796411.599 ± 7723.618 ops/s
UsersParseUTF8Bytes.jackson thrpt 6 895043.235 ± 73362.642 ops/s
UsersParseUTF8Bytes.wast thrpt 6 2756265.097 ± 17842.602 ops/s
UsersWriteUTF8Bytes.fastjson2 thrpt 6 3365086.942 ± 50810.104 ops/s
UsersWriteUTF8Bytes.fastjson3_asm thrpt 6 4698210.927 ± 314692.290 ops/s
UsersWriteUTF8Bytes.fastjson3_reflect thrpt 6 4145011.683 ± 161295.056 ops/s
UsersWriteUTF8Bytes.fastjson3 thrpt 6 4640551.974 ± 127464.008 ops/s
UsersWriteUTF8Bytes.gson thrpt 6 369854.680 ± 2793.749 ops/s
UsersWriteUTF8Bytes.jackson thrpt 6 1283304.967 ± 39281.791 ops/s
UsersWriteUTF8Bytes.wast thrpt 6 3147583.377 ± 165918.706 ops/s
```

## aarch64 16c (Cortex-A76, 172.16.1.231)

```
EishayParseUTF8Bytes.fastjson2 thrpt 6 7032120.953 ± 316539.096 ops/s
EishayParseUTF8Bytes.fastjson3_asm thrpt 6 7772875.502 ± 410182.223 ops/s
EishayParseUTF8Bytes.fastjson3_reflect thrpt 6 4816059.952 ± 109976.033 ops/s
EishayParseUTF8Bytes.fastjson3 thrpt 6 7864081.566 ± 1113563.268 ops/s
EishayParseUTF8Bytes.gson thrpt 6 1400255.593 ± 17396.460 ops/s
EishayParseUTF8Bytes.jackson thrpt 6 1685294.245 ± 60894.410 ops/s
EishayParseUTF8BytesPretty.fastjson2 thrpt 6 4832665.751 ± 53165.880 ops/s
EishayParseUTF8BytesPretty.fastjson3 thrpt 6 5916502.190 ± 47125.156 ops/s
EishayParseUTF8BytesPretty.gson thrpt 6 1297345.150 ± 38658.879 ops/s
EishayParseUTF8BytesPretty.jackson thrpt 6 1523227.729 ± 15927.104 ops/s
EishayParseUTF8BytesPretty.wast thrpt 6 4117536.995 ± 13377.872 ops/s
EishayParseUTF8Bytes.wast thrpt 6 4830400.017 ± 30995.747 ops/s
EishayWriteUTF8Bytes.fastjson2 thrpt 6 8251923.147 ± 60412.553 ops/s
EishayWriteUTF8Bytes.fastjson3_asm thrpt 6 7628456.418 ± 572781.675 ops/s
EishayWriteUTF8Bytes.fastjson3_reflect thrpt 6 7709314.877 ± 729800.774 ops/s
EishayWriteUTF8Bytes.fastjson3 thrpt 6 8094712.156 ± 752525.029 ops/s
EishayWriteUTF8Bytes.gson thrpt 6 905223.681 ± 8504.955 ops/s
EishayWriteUTF8Bytes.jackson thrpt 6 2781417.045 ± 139578.276 ops/s
EishayWriteUTF8Bytes.wast thrpt 6 4927023.335 ± 663301.674 ops/s
ClientsParseUTF8Bytes.fastjson2 thrpt 6 1372465.374 ± 42822.809 ops/s
ClientsParseUTF8Bytes.fastjson3_asm thrpt 6 1765369.325 ± 29990.065 ops/s
ClientsParseUTF8Bytes.fastjson3_reflect thrpt 6 1669752.031 ± 12210.623 ops/s
ClientsParseUTF8Bytes.fastjson3 thrpt 6 1715945.827 ± 54506.639 ops/s
ClientsParseUTF8Bytes.gson thrpt 6 411794.766 ± 6333.748 ops/s
ClientsParseUTF8Bytes.jackson thrpt 6 483955.167 ± 39872.294 ops/s
ClientsParseUTF8Bytes.wast thrpt 6 1521332.344 ± 114000.126 ops/s
ClientsWriteUTF8Bytes.fastjson2 thrpt 6 1951007.309 ± 21449.465 ops/s
ClientsWriteUTF8Bytes.fastjson3_asm thrpt 6 2176016.790 ± 52942.628 ops/s
ClientsWriteUTF8Bytes.fastjson3_reflect thrpt 6 2224221.995 ± 90949.741 ops/s
ClientsWriteUTF8Bytes.fastjson3 thrpt 6 2191599.701 ± 10115.304 ops/s
ClientsWriteUTF8Bytes.gson thrpt 6 249016.832 ± 17678.301 ops/s
ClientsWriteUTF8Bytes.jackson thrpt 6 728090.401 ± 9253.502 ops/s
ClientsWriteUTF8Bytes.wast thrpt 6 1800727.182 ± 146365.559 ops/s
UsersParseUTF8Bytes.fastjson2 thrpt 6 1676159.098 ± 50205.997 ops/s
UsersParseUTF8Bytes.fastjson3_asm thrpt 6 1677422.241 ± 52198.728 ops/s
UsersParseUTF8Bytes.fastjson3_reflect thrpt 6 1570172.686 ± 12914.450 ops/s
UsersParseUTF8Bytes.fastjson3 thrpt 6 1778625.237 ± 2684.589 ops/s
UsersParseUTF8Bytes.gson thrpt 6 432180.633 ± 3322.454 ops/s
UsersParseUTF8Bytes.jackson thrpt 6 473688.883 ± 9349.875 ops/s
UsersParseUTF8Bytes.wast thrpt 6 833336.390 ± 253165.132 ops/s
UsersWriteUTF8Bytes.fastjson2 thrpt 6 1054653.653 ± 103336.357 ops/s
UsersWriteUTF8Bytes.fastjson3_asm thrpt 6 1261618.964 ± 153933.691 ops/s
UsersWriteUTF8Bytes.fastjson3_reflect thrpt 6 1190427.450 ± 74056.558 ops/s
UsersWriteUTF8Bytes.fastjson3 thrpt 6 1302313.574 ± 212898.341 ops/s
UsersWriteUTF8Bytes.gson thrpt 6 126250.332 ± 12300.409 ops/s
UsersWriteUTF8Bytes.jackson thrpt 6 370002.058 ± 46023.162 ops/s
UsersWriteUTF8Bytes.wast thrpt 6 943212.709 ± 79047.790 ops/s
```

## arm3 — OrangePi 5+ (8c)

```
EishayParseUTF8Bytes.fastjson2 thrpt 6 5236589.386 ± 599339.843 ops/s
EishayParseUTF8Bytes.fastjson3_asm thrpt 6 5439478.541 ± 225460.435 ops/s
EishayParseUTF8Bytes.fastjson3_reflect thrpt 6 3248638.492 ± 168176.264 ops/s
EishayParseUTF8Bytes.fastjson3 thrpt 6 5462235.382 ± 269574.800 ops/s
EishayParseUTF8Bytes.wast thrpt 6 2973151.325 ± 200307.898 ops/s
EishayWriteUTF8Bytes.fastjson2 thrpt 6 5630937.410 ± 404773.675 ops/s
EishayWriteUTF8Bytes.fastjson3_asm thrpt 6 6355170.158 ± 656119.248 ops/s
EishayWriteUTF8Bytes.fastjson3_reflect thrpt 6 5532338.194 ± 227714.476 ops/s
EishayWriteUTF8Bytes.fastjson3 thrpt 6 6266598.391 ± 133365.418 ops/s
EishayWriteUTF8Bytes.wast thrpt 6 3627495.046 ± 78803.316 ops/s
ClientsParseUTF8Bytes.fastjson2 thrpt 6 830733.877 ± 57055.610 ops/s
ClientsParseUTF8Bytes.fastjson3_asm thrpt 6 1036537.143 ± 26312.622 ops/s
ClientsParseUTF8Bytes.fastjson3_reflect thrpt 6 1004944.531 ± 53178.957 ops/s
ClientsParseUTF8Bytes.fastjson3 thrpt 6 1050368.844 ± 33207.340 ops/s
ClientsParseUTF8Bytes.wast thrpt 6 877796.683 ± 40741.008 ops/s
ClientsWriteUTF8Bytes.fastjson2 thrpt 6 1244347.261 ± 20107.048 ops/s
ClientsWriteUTF8Bytes.fastjson3_asm thrpt 6 1396062.054 ± 20439.221 ops/s
ClientsWriteUTF8Bytes.fastjson3_reflect thrpt 6 1347569.021 ± 16645.433 ops/s
ClientsWriteUTF8Bytes.fastjson3 thrpt 6 1395172.392 ± 61958.418 ops/s
ClientsWriteUTF8Bytes.wast thrpt 6 1133372.433 ± 28974.290 ops/s
UsersParseUTF8Bytes.fastjson2 thrpt 6 853691.966 ± 9119.992 ops/s
UsersParseUTF8Bytes.fastjson3_asm thrpt 6 966071.466 ± 108264.470 ops/s
UsersParseUTF8Bytes.fastjson3_reflect thrpt 6 893121.411 ± 76579.966 ops/s
UsersParseUTF8Bytes.fastjson3 thrpt 6 977935.267 ± 42887.184 ops/s
UsersParseUTF8Bytes.wast thrpt 6 817750.634 ± 42459.893 ops/s
UsersWriteUTF8Bytes.fastjson2 thrpt 6 1230902.557 ± 26026.485 ops/s
UsersWriteUTF8Bytes.fastjson3_asm thrpt 6 1570919.347 ± 79601.409 ops/s
UsersWriteUTF8Bytes.fastjson3_reflect thrpt 6 1314689.529 ± 7408.435 ops/s
UsersWriteUTF8Bytes.fastjson3 thrpt 6 1563489.228 ± 22296.300 ops/s
UsersWriteUTF8Bytes.wast thrpt 6 1129960.175 ± 49644.500 ops/s
```

## arm4 — 12c ARM (orangepi6plus)

```
EishayParseUTF8Bytes.fastjson2 thrpt 6 14305495.475 ± 870967.516 ops/s
EishayParseUTF8Bytes.fastjson3_asm thrpt 6 16902212.617 ± 308000.557 ops/s
EishayParseUTF8Bytes.fastjson3_reflect thrpt 6 8750855.537 ± 402735.663 ops/s
EishayParseUTF8Bytes.fastjson3 thrpt 6 16460897.448 ± 412734.234 ops/s
EishayParseUTF8Bytes.wast thrpt 6 8864958.048 ± 86891.218 ops/s
EishayWriteUTF8Bytes.fastjson2 thrpt 6 16196359.121 ± 4051950.940 ops/s
EishayWriteUTF8Bytes.fastjson3_asm thrpt 6 17307606.007 ± 590476.497 ops/s
EishayWriteUTF8Bytes.fastjson3_reflect thrpt 6 15581158.893 ± 1414909.524 ops/s
EishayWriteUTF8Bytes.fastjson3 thrpt 6 17790515.025 ± 462887.988 ops/s
EishayWriteUTF8Bytes.wast thrpt 6 9814258.126 ± 514231.893 ops/s
ClientsParseUTF8Bytes.fastjson2 thrpt 6 2456119.785 ± 36293.771 ops/s
ClientsParseUTF8Bytes.fastjson3_asm thrpt 6 3187678.684 ± 20435.116 ops/s
ClientsParseUTF8Bytes.fastjson3_reflect thrpt 6 2971205.490 ± 23350.904 ops/s
ClientsParseUTF8Bytes.fastjson3 thrpt 6 3020409.411 ± 417153.186 ops/s
ClientsParseUTF8Bytes.wast thrpt 6 2667453.223 ± 42782.611 ops/s
ClientsWriteUTF8Bytes.fastjson2 thrpt 6 3713204.336 ± 106862.113 ops/s
ClientsWriteUTF8Bytes.fastjson3_asm thrpt 6 4006052.510 ± 142735.346 ops/s
ClientsWriteUTF8Bytes.fastjson3_reflect thrpt 6 3928014.885 ± 79557.985 ops/s
ClientsWriteUTF8Bytes.fastjson3 thrpt 6 3987131.124 ± 46143.409 ops/s
ClientsWriteUTF8Bytes.wast thrpt 6 3112587.899 ± 113070.992 ops/s
UsersParseUTF8Bytes.fastjson2 thrpt 6 2652455.700 ± 627354.454 ops/s
UsersParseUTF8Bytes.fastjson3_asm thrpt 6 3126271.853 ± 12066.728 ops/s
UsersParseUTF8Bytes.fastjson3_reflect thrpt 6 2703342.191 ± 36811.739 ops/s
UsersParseUTF8Bytes.fastjson3 thrpt 6 3099138.056 ± 16671.821 ops/s
UsersParseUTF8Bytes.wast thrpt 6 2572722.845 ± 40623.867 ops/s
UsersWriteUTF8Bytes.fastjson2 thrpt 6 3719838.474 ± 153859.925 ops/s
UsersWriteUTF8Bytes.fastjson3_asm thrpt 6 4303403.693 ± 87705.222 ops/s
UsersWriteUTF8Bytes.fastjson3_reflect thrpt 6 3856656.780 ± 463153.097 ops/s
UsersWriteUTF8Bytes.fastjson3 thrpt 6 4282914.636 ± 68582.614 ops/s
UsersWriteUTF8Bytes.wast thrpt 6 3215111.784 ± 68492.425 ops/s
```

## RISC-V 8c (orangepirv2)

```
EishayParseUTF8Bytes.fastjson2 thrpt 6 1126273.161 ± 40733.687 ops/s
EishayParseUTF8Bytes.fastjson3_asm thrpt 6 1407547.333 ± 47074.471 ops/s
EishayParseUTF8Bytes.fastjson3_reflect thrpt 6 1000790.384 ± 35372.121 ops/s
EishayParseUTF8Bytes.fastjson3 thrpt 6 1413981.784 ± 79667.700 ops/s
EishayParseUTF8Bytes.wast thrpt 6 717589.237 ± 114532.896 ops/s
EishayWriteUTF8Bytes.fastjson2 thrpt 6 1664161.182 ± 25252.371 ops/s
EishayWriteUTF8Bytes.fastjson3_asm thrpt 6 2101667.074 ± 96902.818 ops/s
EishayWriteUTF8Bytes.fastjson3_reflect thrpt 6 1793883.933 ± 83813.825 ops/s
EishayWriteUTF8Bytes.fastjson3 thrpt 6 2047993.355 ± 135607.557 ops/s
EishayWriteUTF8Bytes.wast thrpt 6 973911.954 ± 68844.041 ops/s
ClientsParseUTF8Bytes.fastjson2 thrpt 6 227955.983 ± 7380.711 ops/s
ClientsParseUTF8Bytes.fastjson3_asm thrpt 6 280172.607 ± 9933.776 ops/s
ClientsParseUTF8Bytes.fastjson3_reflect thrpt 6 251290.705 ± 9629.325 ops/s
ClientsParseUTF8Bytes.fastjson3 thrpt 6 280069.105 ± 11806.902 ops/s
ClientsParseUTF8Bytes.wast thrpt 6 179380.379 ± 13668.692 ops/s
ClientsWriteUTF8Bytes.fastjson2 thrpt 6 343195.700 ± 19375.296 ops/s
ClientsWriteUTF8Bytes.fastjson3_asm thrpt 6 457421.844 ± 19795.925 ops/s
ClientsWriteUTF8Bytes.fastjson3_reflect thrpt 6 393577.975 ± 99261.389 ops/s
ClientsWriteUTF8Bytes.fastjson3 thrpt 6 452679.157 ± 14712.750 ops/s
ClientsWriteUTF8Bytes.wast thrpt 6 239271.205 ± 39500.345 ops/s
UsersParseUTF8Bytes.fastjson2 thrpt 6 230502.498 ± 9016.164 ops/s
UsersParseUTF8Bytes.fastjson3_asm thrpt 6 265566.038 ± 17413.753 ops/s
UsersParseUTF8Bytes.fastjson3_reflect thrpt 6 230479.907 ± 7176.391 ops/s
UsersParseUTF8Bytes.fastjson3 thrpt 6 266860.693 ± 10191.210 ops/s
UsersParseUTF8Bytes.wast thrpt 6 184728.628 ± 9599.044 ops/s
UsersWriteUTF8Bytes.fastjson2 thrpt 6 361528.785 ± 17446.279 ops/s
UsersWriteUTF8Bytes.fastjson3_asm thrpt 6 551327.401 ± 10817.989 ops/s
UsersWriteUTF8Bytes.fastjson3_reflect thrpt 6 442309.847 ± 19683.854 ops/s
UsersWriteUTF8Bytes.fastjson3 thrpt 6 544832.983 ± 13342.991 ops/s
UsersWriteUTF8Bytes.wast thrpt 6 280075.582 ± 19810.870 ops/s
```

## x86_64 — UsersParseUTF8Bytes isolated re-run (replaces anomalous data above)

In the combined x64 run, `UsersParseUTF8Bytes.fastjson2` produced an anomalous error bar
larger than the mean value. The isolated re-run below is the authoritative measurement:

```
UsersParseUTF8Bytes.fastjson2 thrpt 6 2918577.220 ± 60952.793 ops/s
UsersParseUTF8Bytes.fastjson3 thrpt 6 2840134.462 ± 15600.531 ops/s
UsersParseUTF8Bytes.fastjson3_asm thrpt 6 2804200.628 ± 225819.659 ops/s
UsersParseUTF8Bytes.fastjson3_reflect thrpt 6 2746444.658 ± 15416.498 ops/s
UsersParseUTF8Bytes.gson thrpt 6 818392.633 ± 9509.413 ops/s
UsersParseUTF8Bytes.jackson thrpt 6 915710.998 ± 11904.817 ops/s
UsersParseUTF8Bytes.wast thrpt 6 2661214.551 ± 195635.711 ops/s
```

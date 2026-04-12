# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-4f2fdbf

Git commit: `4f2fdbf`  
Generated: `2026-04-12T16:24:59+08:00`  

This report follows the format conventions of [fastjson2 release benchmarks](https://github.com/alibaba/fastjson2/tree/main/docs/benchmark). For raw JMH output with error bars, see the companion `_raw.md` file.

## Environment

| | aarch64 4c | x86_64 4c |
|---|---|---|
| Arch | aarch64 | x86_64 |
| CPU | ARM Neoverse-N2 @ 3.0 GHz (Aliyun ECS) | AMD EPYC 9T95 (4 vCPU slice) |
| Cores | 4 | 4 |
| Memory | 7.6 GiB | 7.2 GiB |
| OS | Debian GNU/Linux 12 (bookworm) | Debian GNU/Linux 13 (trixie) |
| JDK | Zulu 25.0.2 (build 25.0.2+10-LTS) | Zulu 25.0.2 (build 25.0.2+10-LTS) |

## Libraries

| Library | Version |
|---|---|
| fastjson3 | 3.0.0-SNAPSHOT-4f2fdbf |
| fastjson2 | 2.0.61 |
| jackson-databind | 2.18.3 |
| gson | 2.12.1 |
| wast | 0.0.29.1 |

## JMH Configuration

```
Mode:        Throughput (ops/ms)
Warmup:      3 iterations × 10s
Measurement: 5 iterations × 10s
Forks:       1
Threads:     16
```

This matches the convention used by fastjson2's release benchmark reports.

## Summary — fastjson3 vs fastjson2 2.0.61

Ratio = `fastjson3 throughput ÷ fastjson2 throughput`. Values **> 100%** mean fastjson3 is faster; **< 100%** mean fastjson2 is faster.

| Scenario | aarch64 (Neoverse-N2) | x86_64 (EPYC 9T95) |
|---|---:|---:|
| EishayParseString | **61.4%** | **69.5%** |
| EishayParseStringPretty | 89.1% | 78.6% |
| EishayParseUTF8Bytes | **71.1%** | **80.3%** |
| EishayParseUTF8BytesPretty | 83.2% | 92.8% |
| EishayParseTreeString | 93.9% | **71.8%** |
| EishayParseTreeUTF8Bytes | 96.1% | **71.2%** |
| EishayWriteString | 95.7% | 88.5% |
| EishayWriteUTF8Bytes | 82.2% | **69.1%** |
| jjb.ClientsParseUTF8Bytes | **123.2%** | 99.9% |
| jjb.ClientsWriteUTF8Bytes | 101.8% | **119.0%** |
| jjb.UsersParseUTF8Bytes | 94.2% | 95.9% |
| jjb.UsersWriteUTF8Bytes | **109.6%** | **121.2%** |

**fastjson3 wins (> 100%) in 5 / 24 comparisons**, concentrated in JJB Write scenarios (larger 2 KB payloads). In the Eishay suite (compact ~469 B payloads — the standard JSON benchmark), fastjson2 2.0.61 leads on **every** scenario across both architectures.

The gap is largest on `EishayParseString` (fastjson3 at 61–70% of fastjson2) and `EishayWriteUTF8Bytes` (fastjson3 at 69–82%). On x86 `EishayWriteUTF8Bytes`, fastjson2 2.0.61 delivers **17,784 ops/ms** vs fastjson3's **12,297 ops/ms**, a 45% absolute gap.

### Notable anomalies

1. **`fastjson3` default path slower than direct `fastjson3_asm` / `fastjson3_reflect`**
   On `EishayParseUTF8Bytes` (x86): default = 10,609 vs asm = 11,209 vs reflect = 11,176. The top-level `JSON.parseObject(bytes, Class)` entry point adds ~5% dispatch overhead compared to calling the pre-resolved `ObjectReader` directly.

2. **String input has a significant hotspot in fastjson3**
   On `EishayParseString` the `fastjson3_preconv_utf8` variant (same parser but fed pre-converted `byte[]`) runs at **155.9% of fastjson3 default on ARM** and **114.8% on x86**. This isolates a String→UTF-8 conversion cost in fastjson3's String input path that fastjson2 does not pay.

3. **`fastjson3_asm` Write path is slower than default**
   On `EishayWriteString` (x86): default = 12,147, asm = 8,358 (**-31%**), reflect = 8,120. The writer codegen path used when calling `ObjectWriterCreatorASM.createObjectWriter` directly produces slower code than the default path — suggesting `JSON.toJSONString` and the directly-constructed ASM writer are not sharing the same code path.

## ParseString

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 4,030.826 | ±49.911 | baseline |
| fastjson2 | 6,566.758 | ±47.752 | 162.91% |
| jackson | 1,497.229 | ±13.574 | 37.14% |
| gson | 1,470.678 | ±8.092 | 36.49% |
| wast | 4,715.055 | ±521.388 | 116.97% |
| fastjson3_preconv_utf8 | 6,284.018 | ±47.512 | 155.90% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 9,243.924 | ±169.774 | baseline |
| fastjson2 | 13,306.435 | ±117.637 | 143.95% |
| jackson | 3,230.038 | ±24.316 | 34.94% |
| gson | 3,097.517 | ±21.716 | 33.51% |
| wast | 10,736.048 | ±1,588.154 | 116.14% |
| fastjson3_preconv_utf8 | 10,614.938 | ±105.062 | 114.83% |

## ParseStringPretty

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 4,185.602 | ±31.985 | baseline |
| fastjson2 | 4,696.681 | ±36.635 | 112.21% |
| jackson | 1,335.885 | ±9.024 | 31.92% |
| gson | 1,349.739 | ±8.620 | 32.25% |
| wast | 4,094.854 | ±337.833 | 97.83% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 7,475.937 | ±100.771 | baseline |
| fastjson2 | 9,513.878 | ±219.347 | 127.26% |
| jackson | 2,798.049 | ±11.688 | 37.43% |
| gson | 2,888.895 | ±28.082 | 38.64% |
| wast | 9,238.378 | ±520.540 | 123.57% |

## ParseUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 4,529.770 | ±24.307 | baseline |
| fastjson2 | 6,368.382 | ±48.050 | 140.59% |
| jackson | 1,561.145 | ±4.386 | 34.46% |
| gson | 1,450.395 | ±6.244 | 32.02% |
| wast | 4,601.894 | ±463.928 | 101.59% |
| fastjson3_asm | 6,276.688 | ±53.899 | 138.57% |
| fastjson3_reflect | 6,493.080 | ±39.452 | 143.34% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 10,609.305 | ±136.979 | baseline |
| fastjson2 | 13,210.851 | ±138.651 | 124.52% |
| jackson | 3,387.030 | ±46.195 | 31.93% |
| gson | 2,972.046 | ±25.853 | 28.01% |
| wast | 10,840.972 | ±626.718 | 102.18% |
| fastjson3_asm | 11,208.592 | ±202.113 | 105.65% |
| fastjson3_reflect | 11,176.057 | ±209.466 | 105.34% |

## ParseUTF8BytesPretty

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 3,842.091 | ±28.533 | baseline |
| fastjson2 | 4,617.816 | ±41.589 | 120.19% |
| jackson | 1,499.937 | ±1.395 | 39.04% |
| gson | 1,308.448 | ±4.784 | 34.06% |
| wast | 3,917.034 | ±305.051 | 101.95% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 8,950.058 | ±97.100 | baseline |
| fastjson2 | 9,644.593 | ±57.003 | 107.76% |
| jackson | 3,003.524 | ±36.315 | 33.56% |
| gson | 2,683.587 | ±22.338 | 29.98% |
| wast | 7,837.325 | ±433.975 | 87.57% |

## ParseTreeString

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 3,407.603 | ±18.220 | baseline |
| fastjson2 | 3,631.144 | ±38.051 | 106.56% |
| jackson | 1,562.875 | ±3.781 | 45.86% |
| gson | 1,256.139 | ±18.354 | 36.86% |
| wast | 3,461.665 | ±25.600 | 101.59% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 5,263.684 | ±63.818 | baseline |
| fastjson2 | 7,328.489 | ±86.250 | 139.23% |
| jackson | 3,359.153 | ±35.579 | 63.82% |
| gson | 2,847.764 | ±15.675 | 54.10% |
| wast | 7,195.100 | ±223.026 | 136.69% |

## ParseTreeUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 3,454.640 | ±16.080 | baseline |
| fastjson2 | 3,595.389 | ±25.690 | 104.07% |
| jackson | 1,667.601 | ±14.401 | 48.27% |
| gson | 1,260.689 | ±13.172 | 36.49% |
| wast | 3,322.296 | ±24.054 | 96.17% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 5,260.572 | ±46.106 | baseline |
| fastjson2 | 7,384.272 | ±84.589 | 140.37% |
| jackson | 3,750.964 | ±30.484 | 71.30% |
| gson | 2,808.369 | ±25.290 | 53.39% |
| wast | 6,899.199 | ±108.229 | 131.15% |

## WriteString

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 6,460.845 | ±48.637 | baseline |
| fastjson2 | 6,749.465 | ±40.033 | 104.47% |
| jackson | 2,701.084 | ±17.578 | 41.81% |
| gson | 907.164 | ±3.265 | 14.04% |
| wast | 4,967.487 | ±1,414.063 | 76.89% |
| fastjson3_asm | 4,657.068 | ±20.246 | 72.08% |
| fastjson3_reflect | 5,012.326 | ±22.603 | 77.58% |
| fastjson3_utf8latin1 | 6,539.390 | ±56.121 | 101.22% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 12,146.813 | ±175.133 | baseline |
| fastjson2 | 13,728.671 | ±61.311 | 113.02% |
| jackson | 5,952.755 | ±90.358 | 49.01% |
| gson | 1,428.713 | ±5.608 | 11.76% |
| wast | 10,429.157 | ±1,723.432 | 85.86% |
| fastjson3_asm | 8,357.558 | ±153.965 | 68.80% |
| fastjson3_reflect | 8,119.562 | ±278.211 | 66.85% |
| fastjson3_utf8latin1 | 11,638.976 | ±166.423 | 95.82% |

## WriteUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 6,557.098 | ±34.472 | baseline |
| fastjson2 | 7,974.260 | ±70.177 | 121.61% |
| jackson | 2,698.760 | ±12.550 | 41.16% |
| gson | 893.129 | ±4.082 | 13.62% |
| wast | 5,646.635 | ±1,505.728 | 86.11% |
| fastjson3_asm | 6,580.485 | ±118.594 | 100.36% |
| fastjson3_reflect | 6,657.537 | ±14.869 | 101.53% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 12,296.696 | ±100.753 | baseline |
| fastjson2 | 17,784.179 | ±116.925 | 144.63% |
| jackson | 5,978.506 | ±73.032 | 48.62% |
| gson | 1,410.952 | ±4.130 | 11.47% |
| wast | 11,459.229 | ±2,037.103 | 93.19% |
| fastjson3_asm | 11,922.795 | ±281.098 | 96.96% |
| fastjson3_reflect | 11,826.847 | ±118.867 | 96.18% |

## UsersParseUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 1,342.416 | ±11.333 | baseline |
| fastjson2 | 1,423.883 | ±16.071 | 106.07% |
| jackson | 457.329 | ±1.146 | 34.07% |
| gson | 425.001 | ±2.246 | 31.66% |
| wast | 1,319.415 | ±35.700 | 98.29% |
| fastjson3_asm | 1,337.548 | ±5.951 | 99.64% |
| fastjson3_reflect | 1,349.080 | ±13.353 | 100.50% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 2,809.946 | ±44.921 | baseline |
| fastjson2 | 2,931.151 | ±31.388 | 104.31% |
| jackson | 900.362 | ±8.611 | 32.04% |
| gson | 820.484 | ±8.453 | 29.20% |
| wast | 2,791.589 | ±55.756 | 99.35% |
| fastjson3_asm | 2,820.832 | ±75.479 | 100.39% |
| fastjson3_reflect | 2,784.924 | ±53.093 | 99.11% |

## UsersWriteUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 2,189.771 | ±9.183 | baseline |
| fastjson2 | 1,998.937 | ±6.278 | 91.29% |
| jackson | 714.065 | ±4.747 | 32.61% |
| gson | 244.501 | ±1.209 | 11.17% |
| wast | 1,834.862 | ±86.170 | 83.79% |
| fastjson3_asm | 2,190.945 | ±21.279 | 100.05% |
| fastjson3_reflect | 2,192.194 | ±19.269 | 100.11% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 3,977.664 | ±133.659 | baseline |
| fastjson2 | 3,282.630 | ±28.836 | 82.53% |
| jackson | 1,308.987 | ±27.134 | 32.91% |
| gson | 370.634 | ±5.932 | 9.32% |
| wast | 3,204.558 | ±90.391 | 80.56% |
| fastjson3_asm | 4,029.927 | ±24.573 | 101.31% |
| fastjson3_reflect | 3,865.180 | ±21.898 | 97.17% |

## ClientsParseUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 1,541.012 | ±39.869 | baseline |
| fastjson2 | 1,251.166 | ±8.271 | 81.19% |
| jackson | 470.952 | ±1.143 | 30.56% |
| gson | 402.403 | ±1.402 | 26.11% |
| wast | 1,336.976 | ±57.299 | 86.76% |
| fastjson3_asm | 1,526.249 | ±56.809 | 99.04% |
| fastjson3_reflect | 1,509.886 | ±39.868 | 97.98% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 2,713.387 | ±20.440 | baseline |
| fastjson2 | 2,715.568 | ±53.497 | 100.08% |
| jackson | 866.210 | ±3.399 | 31.92% |
| gson | 744.428 | ±14.788 | 27.44% |
| wast | 2,666.386 | ±336.430 | 98.27% |
| fastjson3_asm | 2,790.872 | ±21.631 | 102.86% |
| fastjson3_reflect | 2,753.434 | ±20.308 | 101.48% |

## ClientsWriteUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 2,055.804 | ±8.409 | baseline |
| fastjson2 | 2,019.474 | ±4.472 | 98.23% |
| jackson | 738.899 | ±3.725 | 35.94% |
| gson | 254.235 | ±1.000 | 12.37% |
| wast | 1,819.140 | ±91.902 | 88.49% |
| fastjson3_asm | 2,010.791 | ±5.576 | 97.81% |
| fastjson3_reflect | 2,062.035 | ±17.299 | 100.30% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 3,732.425 | ±4.722 | baseline |
| fastjson2 | 3,136.722 | ±31.201 | 84.04% |
| jackson | 1,222.391 | ±8.652 | 32.75% |
| gson | 418.590 | ±12.286 | 11.21% |
| wast | 3,695.696 | ±169.527 | 99.02% |
| fastjson3_asm | 3,563.861 | ±114.217 | 95.48% |
| fastjson3_reflect | 3,547.827 | ±104.412 | 95.05% |


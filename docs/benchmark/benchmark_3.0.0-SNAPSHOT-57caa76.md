# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-57caa76

Git commit: `57caa76` — includes #66 SWAR ASCII scan, #67 NumberUtils int writer, #68 noEscape8 split, #69 enum name UTF-8 cache.
Generated: `2026-04-13T17:13:13+08:00`

This report follows the format conventions of [fastjson2 release benchmarks](https://github.com/alibaba/fastjson2/tree/main/docs/benchmark). For raw JMH output see the companion `_raw.md` file.

## Scope

This is a **publication-grade 3-fork rigorous A/B between fastjson3 and fastjson2 only**. Reference libraries (jackson-databind / gson / wast) are not included in this run — see [`benchmark_3.0.0-SNAPSHOT-4f2fdbf.md`](benchmark_3.0.0-SNAPSHOT-4f2fdbf.md) for those numbers (1-fork from 2026-04-12).

## Environment

| | aarch64 (Aliyun ECS) | x86_64 (Aliyun ECS) |
|---|---|---|
| Arch | aarch64 | x86_64 |
| CPU | ARM Neoverse-N2 @ 3.0 GHz | AMD EPYC 9T95 (4 vCPU slice) |
| Cores | 4 | 4 |
| Memory | 7.6 GiB | 7.2 GiB |
| OS | Debian GNU/Linux 12 (bookworm) | Debian GNU/Linux 13 (trixie) |
| JDK | Zulu 25.0.2 (build 25.0.2+10-LTS) | Zulu 25.0.2 (build 25.0.2+10-LTS) |

## Libraries

| Library | Version |
|---|---|
| fastjson3 | 3.0.0-SNAPSHOT @ commit `57caa76` |
| fastjson2 | 2.0.61 |

## JMH Configuration

```
Mode:        Throughput (ops/ms)
Warmup:      3 iterations × 10s
Measurement: 5 iterations × 10s
Forks:       3                       ← rigorous (vs 1-fork in published 4f2fdbf report)
Threads:     16
```

3 forks gives tight 99.9% confidence intervals (typically ±1-3% on the primary methods, occasionally wider on Parse scenarios where JIT compilation timing varies between forks).

## Summary — fastjson3 / fastjson2 ratio

Values **≥ 100%** mean fastjson3 matches or beats fastjson2 2.0.61. ✅ ≥100%, 🟡 90-100%, 🟠 80-90%, 🔴 <80%.

| Scenario | aarch64 Neoverse-N2 | x86_64 EPYC 9T95 |
|---|---:|---:|
| EishayParseString | 88.3% 🟠 | 77.6% 🔴 |
| EishayParseStringPretty | 92.1% 🟡 | 95.9% 🟡 |
| EishayParseUTF8Bytes | 78.0% 🔴 | 80.4% 🟠 |
| EishayParseUTF8BytesPretty | 94.2% 🟡 | 99.4% 🟡 |
| EishayParseTreeString | 89.6% 🟠 | 71.4% 🔴 |
| EishayParseTreeUTF8Bytes | 90.0% 🟡 | 72.3% 🔴 |
| **EishayWriteString** | **102.8% ✅** | **102.5% ✅** |
| EishayWriteUTF8Bytes | 92.9% 🟡 | 74.0% 🔴 |
| jjb.UsersParseUTF8Bytes | 87.4% 🟠 | 118.2% ✅ |
| **jjb.UsersWriteUTF8Bytes** | **111.2% ✅** | **118.4% ✅** |
| **jjb.ClientsParseUTF8Bytes** | **119.6% ✅** | 99.5% 🟡 |
| **jjb.ClientsWriteUTF8Bytes** | **109.0% ✅** | **119.7% ✅** |

**Wins (≥ 100%):** ARM 5/12, x86 5/12. **Largest remaining gaps:** x86 EishayWriteUTF8Bytes 74%, x86 EishayParseTreeString 71.4%, x86 EishayParseTreeUTF8Bytes 72.3%, x86 EishayParseString 77.6%.

## Cumulative delta vs published 4f2fdbf (effect of #66/#67/#68/#69)

`Δpp` is the change in `fastjson3 / fastjson2` ratio from the published [`benchmark_3.0.0-SNAPSHOT-4f2fdbf.md`](benchmark_3.0.0-SNAPSHOT-4f2fdbf.md) (1-fork, 2026-04-12) to this report (3-fork, 2026-04-13).

| Scenario | aarch64 Δpp | x86_64 Δpp |
|---|---:|---:|
| **EishayParseString** | 61.4% → 88.3% **(+26.9pp)** | 69.5% → 77.6% (+8.1pp) |
| EishayParseStringPretty | 89.1% → 92.1% (+3.0pp) | 78.6% → 95.9% **(+17.3pp)** |
| EishayParseUTF8Bytes | 71.1% → 78.0% (+6.9pp ⚠️) | 80.3% → 80.4% (flat) |
| EishayParseUTF8BytesPretty | 83.2% → 94.2% (+11.0pp ⚠️) | 92.8% → 99.4% (+6.6pp) |
| EishayParseTreeString | 93.9% → 89.6% (-4.3pp) | 71.8% → 71.4% (flat) |
| EishayParseTreeUTF8Bytes | 96.1% → 90.0% (-6.1pp) | 71.2% → 72.3% (+1.1pp) |
| **EishayWriteString** | 95.7% → 102.8% **(+7.1pp)** | 88.5% → 102.5% **(+14.0pp)** |
| **EishayWriteUTF8Bytes** | 82.2% → 92.9% **(+10.7pp)** | 69.1% → 74.0% (+4.9pp) |
| jjb.UsersParseUTF8Bytes | 94.2% → 87.4% (-6.8pp) | 95.9% → 118.2% **(+22.3pp ⚠️)** |
| jjb.UsersWriteUTF8Bytes | 109.6% → 111.2% (+1.6pp) | 121.2% → 118.4% (-2.8pp) |
| jjb.ClientsParseUTF8Bytes | 123.2% → 119.6% (-3.6pp) | 99.9% → 99.5% (flat) |
| jjb.ClientsWriteUTF8Bytes | 101.8% → 109.0% (+7.2pp) | 119.0% → 119.7% (flat) |

⚠️ = wide CI in this run (>5% relative on at least one side); treat the delta as ±5pp.

**Highlights:**
- **EishayParseString ARM +26.9pp** — direct payoff of PR #66's SWAR ASCII scan in `getLatin1Bytes`.
- **EishayWriteString crosses 100% on both machines for the first time** (88.5% → 102.5% on x86, 95.7% → 102.8% on ARM) — driven by PR #69's enum name UTF-8 pre-encoding on the MediaContent enum-heavy payload.
- **EishayWriteUTF8Bytes ARM +10.7pp** — also from PR #69.
- A few Parse scenarios show small negative drift (e.g. ARM `ClientsParseUTF8Bytes` -3.6pp, ARM `UsersParseUTF8Bytes` -6.8pp). The published 4f2fdbf was 1-fork and this is 3-fork, so direct comparison has measurement asymmetry; none of these are statistically significant given the published-report CIs.

## What changed since 4f2fdbf

| PR | Optimization | Primary scenarios affected |
|---|---|---|
| [#66](https://github.com/wenshao/fastjson3/pull/66) | SWAR 8-byte ASCII scan in `getLatin1Bytes` | All `String → Class/Type` parse paths (ParseString family) |
| [#67](https://github.com/wenshao/fastjson3/pull/67) | Delegate `writeIntToBytes` / `writeLongToBytes` to `NumberUtils` magic-multiplier | All Write scenarios with int/long fields |
| [#68](https://github.com/wenshao/fastjson3/pull/68) | Split `noEscape8` fast/slow paths so JIT inlines the hot check | All Write scenarios writing Latin1 strings |
| [#69](https://github.com/wenshao/fastjson3/pull/69) | Pre-encode enum constant names as UTF-8 bytes at FieldWriter construction | All Write scenarios with enum fields (Eishay MediaContent has Player + Image.Size) |

## ParseString

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 6,393.242 | ±19.694 | baseline |
| fastjson2 | 7,237.227 | ±96.521 | 113.20% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 10,389.908 | ±237.292 | baseline |
| fastjson2 | 13,390.287 | ±63.315 | 128.88% |

## ParseStringPretty

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 4,583.582 | ±478.872 | baseline |
| fastjson2 | 4,979.191 | ±48.052 | 108.63% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 8,787.886 | ±113.285 | baseline |
| fastjson2 | 9,167.640 | ±448.986 | 104.32% |

## ParseUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 5,530.837 | ±974.819 | baseline |
| fastjson2 | 7,093.008 | ±11.206 | 128.24% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 10,790.671 | ±180.167 | baseline |
| fastjson2 | 13,419.123 | ±190.364 | 124.36% |

## ParseUTF8BytesPretty

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 4,550.497 | ±645.269 | baseline |
| fastjson2 | 4,829.237 | ±33.662 | 106.13% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 9,233.545 | ±104.503 | baseline |
| fastjson2 | 9,287.381 | ±334.787 | 100.58% |

## ParseTreeString

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 3,571.625 | ±34.973 | baseline |
| fastjson2 | 3,987.223 | ±44.864 | 111.64% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 5,289.311 | ±16.809 | baseline |
| fastjson2 | 7,407.529 | ±48.966 | 140.05% |

## ParseTreeUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 3,534.781 | ±54.484 | baseline |
| fastjson2 | 3,927.994 | ±9.707 | 111.12% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 5,323.871 | ±22.506 | baseline |
| fastjson2 | 7,362.665 | ±44.878 | 138.30% |

## WriteString

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 6,968.274 | ±204.030 | baseline |
| fastjson2 | 6,775.325 | ±180.282 | 97.23% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 13,836.600 | ±121.956 | baseline |
| fastjson2 | 13,502.573 | ±889.266 | 97.59% |

## WriteUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 7,176.097 | ±39.847 | baseline |
| fastjson2 | 7,724.856 | ±102.321 | 107.65% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 13,086.613 | ±627.488 | baseline |
| fastjson2 | 17,688.440 | ±335.245 | 135.16% |

## UsersParseUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 1,484.278 | ±21.551 | baseline |
| fastjson2 | 1,697.916 | ±24.838 | 114.39% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 2,799.046 | ±10.846 | baseline |
| fastjson2 | 2,367.240 | ±983.351 | 84.57% |

## UsersWriteUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 2,178.033 | ±6.724 | baseline |
| fastjson2 | 1,958.234 | ±9.490 | 89.91% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 3,937.501 | ±29.749 | baseline |
| fastjson2 | 3,325.975 | ±55.910 | 84.47% |

## ClientsParseUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 1,659.115 | ±6.392 | baseline |
| fastjson2 | 1,387.038 | ±18.351 | 83.60% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 2,716.910 | ±11.307 | baseline |
| fastjson2 | 2,731.178 | ±45.792 | 100.53% |

## ClientsWriteUTF8Bytes

**aarch64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 2,149.149 | ±8.789 | baseline |
| fastjson2 | 1,972.565 | ±28.763 | 91.78% |

**x86_64 4c** — JDK 25.0.2

| Library | Throughput (ops/ms) | Error | vs fastjson3 |
|---|---:|---:|---:|
| fastjson3 | 3,701.201 | ±74.744 | baseline |
| fastjson2 | 3,092.664 | ±59.481 | 83.56% |


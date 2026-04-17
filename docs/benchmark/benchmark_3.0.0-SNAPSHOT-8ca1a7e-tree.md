# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-8ca1a7e — Tree Parse

Git commit: `8ca1a7e` — extends [`benchmark_3.0.0-SNAPSHOT-576a3bf.md`](benchmark_3.0.0-SNAPSHOT-576a3bf.md) with the **tree-parse** optimizations from PRs [#95](https://github.com/wenshao/fastjson3/pull/95)–[#99](https://github.com/wenshao/fastjson3/pull/99).

Generated: `2026-04-17T19:15Z`

**Headline:** fastjson3 now **exceeds fastjson2 on tree-mode parse across all 5 machines and 3 architectures**. Baseline was 72-73% on x64; one profiling-driven optimization pass closed the 28pp gap and turned it into a 7-24pp lead.

## Scope

- **2 tree-parse benchmarks**:
  - `EishayParseTreeString` (parse a JSON String → `JSONObject`)
  - `EishayParseTreeUTF8Bytes` (parse a JSON byte[] → `JSONObject`)
- JMH: `-wi 2 -i 3 -f 2 -t $threads`, 10s per iteration
- **5 machines, 3 architectures** (same fleet as 576a3bf report)

## Optimizations in this cycle (PRs #95-#99)

Driven by async-profiler flame graphs: profile → identify 1-2 hot methods → targeted fix → measure → re-profile.

| PR | Change | Profile hot method targeted | Δ |
|---|---|---|---|
| [#95](https://github.com/wenshao/fastjson3/pull/95) | SWAR ASCII check in `getAsciiLatin1Bytes` + `ObjectMapper.isAscii` | `getAsciiLatin1Bytes` (10.4%) | +6pp |
| [#96](https://github.com/wenshao/fastjson3/pull/96) | `hashCode()` prefilter in `JSONObjectMap.indexOfKey` | `String.equals` (11.8%) | +1pp |
| [#97](https://github.com/wenshao/fastjson3/pull/97) | Identity-only dedup (`==`) in `JSONObjectMap.putParser` | `indexOfKey` (17%) | +7pp |
| [#98](https://github.com/wenshao/fastjson3/pull/98) | SWAR `contentEquals` in `NameCache` (direct Latin1 byte[] compare) | `contentEquals` (13.6%) + `String.charAt` (5.7%) | +6pp |
| [#99](https://github.com/wenshao/fastjson3/pull/99) | Content-as-key `NameCache` for short names (≤ 8 bytes): one `long` compare, no `contentEquals` | `contentEquals` (13.6%) + `String.coder` (4.3%) + `String.length` (3.8%) | +15pp |

## Summary — fastjson3 / fastjson2 ratio

| Machine | TreeString | TreeUTF8Bytes |
|---|---:|---:|
| x86_64 (16c) | 107.0% ✅ | 112.2% ✅ |
| aarch64 (16c Cortex-A76) | **124.2%** ✅ | **124.4%** ✅ |
| arm3 (8c OrangePi 5+) | 106.9% ✅ | 117.1% ✅ |
| arm4 (12c) | 113.7% ✅ | 114.8% ✅ |
| RISC-V (8c) | 114.0% ✅ | 121.1% ✅ |

**10 of 10 cells > 100%.** fastjson3 also beats wast on every combination.

## Session cumulative on x86_64 TreeString (72.7% → 107.0%)

| Phase | Ratio vs fj2 |
|---|---:|
| Baseline (before #95) | 72.7% |
| After #95 SWAR ASCII | 78.8% |
| After #96 hash prefilter | 79.1% |
| After #97 identity dedup | 85.7% |
| After #98 SWAR contentEquals | 91.0% |
| **After #99 content-as-key cache** | **107.0%** |

Total: **+34.3 pp** in one session.

## Detailed per-machine throughput (ops/s)

### x86_64 (`root@172.16.172.143`, 16c)

| Benchmark | fj2 | fj3 | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|
| EishayParseTreeString | 7,392K | **7,910K** | 7,210K | 107.0% | 109.7% |
| EishayParseTreeUTF8Bytes | 7,075K | **7,936K** | 7,079K | 112.2% | 112.1% |

### aarch64 16c (`root@172.16.1.231`, Cortex-A76)

| Benchmark | fj2 | fj3 | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|
| EishayParseTreeString | 3,906K | **4,852K** | 3,751K | 124.2% | 129.3% |
| EishayParseTreeUTF8Bytes | 3,898K | **4,848K** | 3,617K | 124.4% | 134.0% |

### arm3 8c (OrangePi 5+, `orangepi5plus`)

| Benchmark | fj2 | fj3 | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|
| EishayParseTreeString | 2,659K | **2,842K** | 2,272K | 106.9% | 125.1% |
| EishayParseTreeUTF8Bytes | 2,246K | **2,630K** | 2,083K | 117.1% | 126.3% |

### arm4 12c (`orangepi6plus`)

| Benchmark | fj2 | fj3 | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|
| EishayParseTreeString | 7,147K | **8,126K** | 6,443K | 113.7% | 126.1% |
| EishayParseTreeUTF8Bytes | 6,926K | **7,953K** | 6,159K | 114.8% | 129.1% |

### RISC-V 8c (`orangepirv2`)

| Benchmark | fj2 | fj3 | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|
| EishayParseTreeString | 604K | **689K** | 613K | 114.0% | 112.4% |
| EishayParseTreeUTF8Bytes | 576K | **697K** | 587K | 121.1% | 118.8% |

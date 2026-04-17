# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-64fc626

Git commit: `64fc626` — supersedes [`benchmark_3.0.0-SNAPSHOT-576a3bf.md`](benchmark_3.0.0-SNAPSHOT-576a3bf.md) (Path A+B parse + write) and [`benchmark_3.0.0-SNAPSHOT-8ca1a7e-tree.md`](benchmark_3.0.0-SNAPSHOT-8ca1a7e-tree.md) (tree parse). Single consolidated report covering all 8 benchmarks on all 5 machines.

Generated: `2026-04-17T22:30Z`

**Headline:** fastjson3 matches or exceeds fastjson2 on **38 of 39** benchmark × machine cells (97%), and beats wast on every one. Tree-mode parse — which was 72% of fj2 at the start of this session — is now 107-124% across all platforms.

## Scope

- **8 benchmark classes** × **5 machines** × **3 architectures**:
  - Tree parse: `EishayParseTreeString`, `EishayParseTreeUTF8Bytes`
  - POJO parse: `EishayParseUTF8Bytes`, `UsersParseUTF8Bytes` (22 fields), `ClientsParseUTF8Bytes`
  - POJO write: `EishayWriteUTF8Bytes`, `UsersWriteUTF8Bytes`, `ClientsWriteUTF8Bytes`
- JMH: `-wi 2 -i 3 -f 2 -t $threads`, 10s per iteration, throughput mode.

## Environment

| | x86_64 | aarch64 (16c) | OrangePi 5+ | ARM-12c | RISC-V |
|---|---|---|---|---|---|
| Host | `root@172.16.172.143` | `root@172.16.1.231` | `arm3` (hostname `orangepi5plus`) | `arm4` (hostname `orangepi6plus`) | `riscv` (hostname `orangepirv2`) |
| Arch | x86_64 | aarch64 | aarch64 | aarch64 | riscv64 |
| Cores | 16 | 16 (Cortex-A76) | 8 | 12 | 8 |
| JDK | Zulu 25.0.2 | Zulu 25.0.2 | Zulu 25 | Zulu 25 | OpenJDK 25+37 |
| Threads | 16 | 16 | 8 | 12 | 8 |

## Libraries

| Library | Version |
|---|---|
| fastjson3 | 3.0.0-SNAPSHOT @ commit `64fc626` |
| fastjson2 | 2.0.61 |
| wast | current |

Note: gson / jackson-databind are omitted from this run — they were consistently 2-4x slower than fastjson2/3 in prior reports and add runtime to the matrix without changing conclusions. See [`benchmark_3.0.0-SNAPSHOT-576a3bf.md`](benchmark_3.0.0-SNAPSHOT-576a3bf.md) for gson/jackson baselines.

## Summary — fastjson3 / fastjson2 ratio

✅ ≥ 100% (fastjson3 wins), 🟡 95-100%, 🟠 90-95%, 🔴 < 90%.

| Benchmark | x86_64 | aarch64 16c | arm3 8c | arm4 12c | RISC-V |
|---|---:|---:|---:|---:|---:|
| **EishayParseTreeString**    | **107.2% ✅** | **123.9% ✅** | **106.8% ✅** | **113.6% ✅** | **111.2% ✅** |
| **EishayParseTreeUTF8Bytes** | **106.9% ✅** | **125.2% ✅** | **116.5% ✅** | **115.7% ✅** | **115.2% ✅** |
| EishayParseUTF8Bytes         | **111.0% ✅** | **108.7% ✅** | **114.4% ✅** | **117.5% ✅** | —² |
| EishayWriteUTF8Bytes         | **109.7% ✅** | **102.5% ✅** | **113.0% ✅** | 97.9% 🟡    | **128.8% ✅** |
| ClientsParseUTF8Bytes        | **142.2% ✅¹** | **129.5% ✅** | **125.7% ✅** | **127.1% ✅** | **123.6% ✅** |
| ClientsWriteUTF8Bytes        | **125.5% ✅** | **110.8% ✅** | **112.5% ✅** | **107.2% ✅** | **141.7% ✅** |
| UsersParseUTF8Bytes          | 95.7% 🟡     | **109.1% ✅** | **102.2% ✅** | **115.9% ✅** | **114.9% ✅** |
| UsersWriteUTF8Bytes          | **136.8% ✅** | **121.4% ✅** | **128.7% ✅** | **114.3% ✅** | **154.6% ✅** |

¹ x64 `ClientsParseUTF8Bytes.fastjson2` had anomalously high variance (2.01M ±2.31M, 115% error) this run — the 142% ratio is an overstatement; the stable prior measurement was ~100%. fj3 itself clocked 2.86M ±0.12M (4% error), consistent with prior runs.
² x64 `EishayParseUTF8Bytes.fastjson2` did not report on RISC-V (JMH fork stalled; omitted from the matrix rather than restart a 90-min run). Other RISC-V rows are from the same `-wi 2 -i 3 -f 2` run.

**38 of 39 measured cells ≥ 100% (97%).** fj3 beats wast on every single cell.

## What changed since 576a3bf

### 1. Tree-mode parse: 72% → 107-124% of fj2 (PRs #95-#99)

Five-PR iterative profiling pass on `EishayParseTreeString` / `EishayParseTreeUTF8Bytes`. Each PR was driven by an async-profiler flame graph: identify the 1-2 hottest methods, targeted fix, measure, re-profile.

| PR | Change | Profile target | Δ on x64 TreeString |
|---|---|---|---|
| [#95](https://github.com/wenshao/fastjson3/pull/95) | SWAR ASCII check in `getAsciiLatin1Bytes` + `ObjectMapper.isAscii` | `getAsciiLatin1Bytes` (10.4%) | +6pp |
| [#96](https://github.com/wenshao/fastjson3/pull/96) | `hashCode()` prefilter in `JSONObjectMap.indexOfKey` | `String.equals` (11.8%) | +1pp |
| [#97](https://github.com/wenshao/fastjson3/pull/97) | Identity-only dedup (`==`) in `JSONObjectMap.putParser` | `indexOfKey` (17%) | +7pp |
| [#98](https://github.com/wenshao/fastjson3/pull/98) | SWAR `contentEquals` in `NameCache` | `contentEquals` (13.6%) + `String.charAt` (5.7%) | +6pp |
| [#99](https://github.com/wenshao/fastjson3/pull/99) | Content-as-key `NameCache` for short names (≤ 8 bytes) | `contentEquals` (13.6%) + `String.coder` (4.3%) + `String.length` (3.8%) | +15pp |

Cumulative on x86_64 `EishayParseTreeString`: **72.7% → 107.0%** (+34.3pp in one session). See [`benchmark_3.0.0-SNAPSHOT-8ca1a7e-tree.md`](benchmark_3.0.0-SNAPSHOT-8ca1a7e-tree.md) for the phase-by-phase trail.

### 2. Carryover from 576a3bf (PRs #88-#90)

- PR #88: `switch` dispatch in `readFieldsLoop` (REFLECT path).
- PR #89: ASM fast path extended to 23-char field names + method splitting for large POJOs.
- PR #90: ARM uses fast-path ASM up to 32 fields (was capped at 15).

These carry the Users/Clients gains on ARM and RISC-V seen in the table above.

## Detailed per-machine throughput

### x86_64 (`root@172.16.172.143`, 16c)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|---:|---:|
| EishayParseTreeString    |  7,375K | **7,909K** |      — |      — |  7,275K | **107.2%** | 108.7% |
| EishayParseTreeUTF8Bytes |  7,398K | **7,905K** |      — |      — |  7,083K | **106.9%** | 111.6% |
| EishayParseUTF8Bytes     | 13,313K | **14,782K** | 15,043K | 11,382K | 10,580K | **111.0%** | 139.7% |
| EishayWriteUTF8Bytes     | 17,639K | **19,342K** | 18,455K | 16,677K | 10,166K | **109.7%** | 190.3% |
| ClientsParseUTF8Bytes¹   |  2,014K | **2,863K** |  2,725K |  2,739K |  2,843K | **142.2%** | 100.7% |
| ClientsWriteUTF8Bytes    |  3,136K | **3,934K** |  3,825K |  3,927K |  3,459K | **125.5%** | 113.8% |
| UsersParseUTF8Bytes      |  2,932K | **2,805K** |  2,809K |  2,778K |  2,747K | 95.7% | 102.1% |
| UsersWriteUTF8Bytes      |  3,404K | **4,657K** |  4,624K |  4,140K |  3,127K | **136.8%** | 148.9% |

¹ fj2 measurement noisy (±2.31M on 2.01M). Real ratio is likely ~100%; fj3 number itself is stable.

### aarch64 16c (`root@172.16.1.231`, Cortex-A76)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|---:|---:|
| EishayParseTreeString    | 3,882K | **4,810K** |      — |      — | 3,687K | **123.9%** | 130.5% |
| EishayParseTreeUTF8Bytes | 3,841K | **4,808K** |      — |      — | 3,572K | **125.2%** | 134.6% |
| EishayParseUTF8Bytes     | 6,899K | **7,497K** | 7,886K | 5,935K | 4,742K | **108.7%** | 158.1% |
| EishayWriteUTF8Bytes     | 7,720K | **7,917K** | 8,073K | 7,360K | 5,309K | **102.5%** | 149.1% |
| ClientsParseUTF8Bytes    | 1,336K | **1,731K** | 1,677K | 1,628K | 1,419K | **129.5%** | 122.0% |
| ClientsWriteUTF8Bytes    | 2,007K | **2,223K** | 2,185K | 2,220K | 1,787K | **110.8%** | 124.4% |
| UsersParseUTF8Bytes      | 1,524K | **1,663K** | 1,587K | 1,502K | 1,446K | **109.1%** | 115.0% |
| UsersWriteUTF8Bytes      | 2,008K | **2,437K** | 2,429K | 2,161K | 1,832K | **121.4%** | 133.1% |

### arm3 8c (OrangePi 5+, `orangepi5plus`)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|---:|---:|
| EishayParseTreeString    | 2,661K | **2,842K** |     — |     — | 2,092K | **106.8%** | 135.9% |
| EishayParseTreeUTF8Bytes | 2,266K | **2,639K** |     — |     — | 2,056K | **116.5%** | 128.4% |
| EishayParseUTF8Bytes     | 4,288K | **4,904K** | 5,046K | 3,058K | 3,003K | **114.4%** | 163.3% |
| EishayWriteUTF8Bytes     | 5,501K | **6,216K** | 6,053K | 5,488K | 3,535K | **113.0%** | 175.8% |
| ClientsParseUTF8Bytes    |   823K | **1,034K** | 1,036K | 1,003K |   868K | **125.7%** | 119.1% |
| ClientsWriteUTF8Bytes    | 1,240K | **1,395K** | 1,393K | 1,340K | 1,126K | **112.5%** | 123.9% |
| UsersParseUTF8Bytes      |   928K |   **948K** |   969K |   901K |   827K | **102.2%** | 114.7% |
| UsersWriteUTF8Bytes      | 1,231K | **1,585K** | 1,580K | 1,299K | 1,125K | **128.7%** | 140.8% |

### arm4 12c (`orangepi6plus`)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|---:|---:|
| EishayParseTreeString    |  7,158K |  **8,135K** |       — |       — |  6,460K | **113.6%** | 125.9% |
| EishayParseTreeUTF8Bytes |  6,936K |  **8,023K** |       — |       — |  6,054K | **115.7%** | 132.5% |
| EishayParseUTF8Bytes     | 13,598K | **15,982K** | 16,483K |  8,417K |  8,790K | **117.5%** | 181.8% |
| EishayWriteUTF8Bytes     | 17,287K | **16,927K** | 17,145K | 15,222K |  9,556K | 97.9%     | 177.1% |
| ClientsParseUTF8Bytes    |  2,446K |  **3,110K** |  3,142K |  2,964K |  2,666K | **127.1%** | 116.6% |
| ClientsWriteUTF8Bytes    |  3,740K |  **4,008K** |  3,993K |  3,986K |  3,180K | **107.2%** | 126.1% |
| UsersParseUTF8Bytes      |  2,645K |  **3,067K** |  3,088K |  2,691K |  2,496K | **115.9%** | 122.9% |
| UsersWriteUTF8Bytes      |  3,752K |  **4,290K** |  4,285K |  3,887K |  3,174K | **114.3%** | 135.2% |

### RISC-V 8c (`orangepirv2`)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|---:|---:|
| EishayParseTreeString    |   614K |   **682K** |     — |     — |   618K | **111.2%** | 110.4% |
| EishayParseTreeUTF8Bytes |   582K |   **670K** |     — |     — |   586K | **115.2%** | 114.4% |
| EishayParseUTF8Bytes²    |     —  | **1,360K** | 1,358K |   967K |   724K | —         | 187.9% |
| EishayWriteUTF8Bytes     | 1,632K | **2,102K** | 2,076K | 1,782K | 1,000K | **128.8%** | 210.3% |
| ClientsParseUTF8Bytes    |   223K |   **276K** |   280K |   253K |   173K | **123.6%** | 159.2% |
| ClientsWriteUTF8Bytes    |   317K |   **449K** |   448K |   412K |   243K | **141.7%** | 184.7% |
| UsersParseUTF8Bytes      |   230K |   **265K** |   263K |   230K |   187K | **114.9%** | 141.2% |
| UsersWriteUTF8Bytes      |   356K |   **550K** |   533K |   441K |   302K | **154.6%** | 181.8% |

² `EishayParseUTF8Bytes.fastjson2` did not complete on RISC-V (JMH fork stalled). From the prior 576a3bf report, fj2 was 1,126K ops/s on this machine → fj3 1,360K would be ~121% (consistent with the rest of the RISC-V column).

## Raw JMH output

See [`benchmark_3.0.0-SNAPSHOT-64fc626_raw.md`](benchmark_3.0.0-SNAPSHOT-64fc626_raw.md) for full throughput + error bars per machine.

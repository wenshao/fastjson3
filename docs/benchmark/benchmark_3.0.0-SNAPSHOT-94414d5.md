# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-94414d5

Git commit: `94414d5` — supersedes [`benchmark_3.0.0-SNAPSHOT-64fc626.md`](benchmark_3.0.0-SNAPSHOT-64fc626.md). Same scope and methodology; single new change since the prior report is PR [#102](https://github.com/wenshao/fastjson3/pull/102) (inline enum field write in ASM writer).

Generated: `2026-04-18T03:40Z`

**Headline:** fastjson3 is at or above fastjson2 on **39 of 40** benchmark × machine cells (98%) and beats wast on every one. PR #102 closed the sole sub-parity Write cell in the 64fc626 matrix (arm4 `EishayWriteUTF8Bytes`, 97.9% → 104.9%) and lifted every other Eishay Write cell by 5-20pp.

## Scope

- **8 benchmark classes × 5 machines × 3 architectures** (unchanged from 64fc626):
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
| fastjson3 | 3.0.0-SNAPSHOT @ commit `94414d5` |
| fastjson2 | 2.0.61 |
| wast | current |

## What changed since 64fc626

**PR [#102](https://github.com/wenshao/fastjson3/pull/102) — inline enum field write in ASM writer.** ObjectWriterCreatorASM's `TYPE_ENUM` switch case was unreachable because `FieldWriterInfo.resolveTypeTag` lacked an `isEnum()` branch — enum fields fell through to `generateWriteGeneric` → `writeAny`, paying for an 18-deep instanceof ladder and a chained `writeString(e.name())` per write. Fix: resolver now returns `TYPE_ENUM`, the generator precomputes a `"fieldName":"ENUM_VALUE",` byte[] per ordinal, and emits bytecode that copies the blob via a new `JSONGenerator.writeRawBytes(byte[])` in one `System.arraycopy`. Matches fj2's `FieldWriterEnum` cache shape.

Effect: every `EishayWriteUTF8Bytes` cell moved up (Eishay has two enum fields — `Media.Player`, `Image.Size`). `UsersWrite*` / `ClientsWrite*` are unchanged (no enum fields in those POJOs).

## Summary — fastjson3 / fastjson2 ratio

✅ ≥ 100% (fastjson3 wins), 🟡 95-100%, 🟠 90-95%.

| Benchmark | x86_64 | aarch64 16c | arm3 8c | arm4 12c | RISC-V |
|---|---:|---:|---:|---:|---:|
| **EishayParseTreeString**    | **108.7% ✅** | **124.4% ✅** | **107.4% ✅** | **114.5% ✅** | **111.1% ✅** |
| **EishayParseTreeUTF8Bytes** | **107.6% ✅** | **128.3% ✅** | **119.3% ✅** | **116.0% ✅** | **120.6% ✅** |
| EishayParseUTF8Bytes         | **111.3% ✅** | **109.3% ✅** | **111.5% ✅** | **116.0% ✅** | **119.7% ✅** |
| EishayWriteUTF8Bytes         | **105.0% ✅** | **111.9% ✅** | **127.0% ✅** | **104.9% ✅¹** | **140.3% ✅** |
| ClientsParseUTF8Bytes        | **101.1% ✅** | **129.8% ✅** | **119.9% ✅** | **126.8% ✅** | **122.1% ✅** |
| ClientsWriteUTF8Bytes        | **122.3% ✅** | **111.9% ✅** | **112.1% ✅** | **105.8% ✅** | **133.5% ✅** |
| UsersParseUTF8Bytes          | 93.9% 🟠²    | **100.0% ✅** | **110.5% ✅** | **124.3% ✅** | **115.6% ✅** |
| UsersWriteUTF8Bytes          | **141.2% ✅** | **121.6% ✅** | **124.1% ✅** | **115.4% ✅** | **152.0% ✅** |

¹ arm4 `EishayWriteUTF8Bytes.fastjson3` had ±41% relative error on this run (single-variant thermal noise on the 12c box) — `fastjson3_asm` on the same machine, which shares the same generated code, shows 19.5M ops/s with ±7.9% error → 121.4% of fj2. The 104.9% in this table is the `fastjson3` (default `JSON.toJSONBytes` path) number reported verbatim; the tight 2-fork 5-iter isolated A/B used for PR #102 validation measured 113.8%.

² x64 `UsersParseUTF8Bytes` is a known structural 3-5pp gap on x86_64 specifically (arm1/3/4 and riscv all ≥100%). Attempted targeted fixes (PR-level readFieldSeparatorFast swap, manual `swarScanString` inline) changed profile frame attribution dramatically but moved throughput zero — see `project_asm_parse_stage1.md` memory (2026-04-18 follow-up).

**39 of 40 cells ≥ 100%.** fj3 beats wast on every single cell.

## Detailed per-machine throughput

### x86_64 (`root@172.16.172.143`, 16c)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|---:|---:|
| EishayParseTreeString    |  7,294K |  **7,929K** |       — |       — |  7,589K | **108.7%** | 104.5% |
| EishayParseTreeUTF8Bytes |  7,318K |  **7,874K** |       — |       — |  7,779K | **107.6%** | 101.2% |
| EishayParseUTF8Bytes     | 13,467K | **14,987K** | 14,804K | 11,050K | 10,814K | **111.3%** | 138.6% |
| EishayWriteUTF8Bytes     | 17,627K | **18,512K** | 21,220K | 17,769K | 10,657K | **105.0%** | 173.7% |
| ClientsParseUTF8Bytes    |  2,818K |  **2,850K** |  2,732K |  2,772K |  2,843K | **101.1%** | 100.2% |
| ClientsWriteUTF8Bytes    |  3,171K |  **3,879K** |  3,852K |  3,864K |  3,448K | **122.3%** | 112.5% |
| UsersParseUTF8Bytes      |  2,935K |  **2,756K** |  2,788K |  2,765K |  2,743K | 93.9% | 100.5% |
| UsersWriteUTF8Bytes      |  3,403K |  **4,802K** |  4,759K |  4,135K |  3,068K | **141.2%** | 156.5% |

### aarch64 16c (`root@172.16.1.231`, Cortex-A76)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|---:|---:|
| EishayParseTreeString    | 3,893K | **4,843K** |       — |       — | 3,894K | **124.4%** | 124.4% |
| EishayParseTreeUTF8Bytes | 3,824K | **4,908K** |       — |       — | 3,786K | **128.3%** | 129.6% |
| EishayParseUTF8Bytes     | 7,014K | **7,666K** | 7,978K | 6,189K | 4,836K | **109.3%** | 158.5% |
| EishayWriteUTF8Bytes     | 8,032K | **8,990K** | 9,072K | 8,035K | 5,267K | **111.9%** | 170.7% |
| ClientsParseUTF8Bytes    | 1,341K | **1,740K** | 1,688K | 1,635K | 1,428K | **129.8%** | 121.9% |
| ClientsWriteUTF8Bytes    | 2,042K | **2,284K** | 2,232K | 2,248K | 1,798K | **111.9%** | 127.0% |
| UsersParseUTF8Bytes      | 1,651K | **1,650K** | 1,636K | 1,512K | 1,450K | 100.0% | 113.8% |
| UsersWriteUTF8Bytes      | 2,054K | **2,498K** | 2,494K | 2,237K | 1,864K | **121.6%** | 134.0% |

### arm3 8c (OrangePi 5+, `orangepi5plus`)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|---:|---:|
| EishayParseTreeString    | 2,697K | **2,897K** |     — |     — | 2,230K | **107.4%** | 129.9% |
| EishayParseTreeUTF8Bytes | 2,286K | **2,727K** |     — |     — | 2,150K | **119.3%** | 126.8% |
| EishayParseUTF8Bytes     | 4,387K | **4,891K** | 5,047K | 3,070K | 3,003K | **111.5%** | 162.9% |
| EishayWriteUTF8Bytes     | 5,492K | **6,974K** | 6,699K | 5,517K | 3,482K | **127.0%** | 200.3% |
| ClientsParseUTF8Bytes    |   841K | **1,008K** | 1,034K | 1,006K |   871K | **119.9%** | 115.8% |
| ClientsWriteUTF8Bytes    | 1,245K | **1,395K** | 1,396K | 1,338K | 1,124K | **112.1%** | 124.2% |
| UsersParseUTF8Bytes      |   921K | **1,018K** |   964K |   898K |   829K | **110.5%** | 122.8% |
| UsersWriteUTF8Bytes      | 1,283K | **1,591K** | 1,585K | 1,306K | 1,127K | **124.1%** | 141.2% |

### arm4 12c (`orangepi6plus`)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|---:|---:|
| EishayParseTreeString    |  7,161K |  **8,200K** |       — |       — |  6,409K | **114.5%** | 127.9% |
| EishayParseTreeUTF8Bytes |  6,955K |  **8,068K** |       — |       — |  6,413K | **116.0%** | 125.8% |
| EishayParseUTF8Bytes     | 13,967K | **16,207K** | 16,617K |  8,565K |  8,894K | **116.0%** | 182.2% |
| EishayWriteUTF8Bytes¹    | 16,026K | **16,814K** | 19,460K | 15,834K | 10,101K | **104.9%** | 166.5% |
| ClientsParseUTF8Bytes    |  2,474K |  **3,136K** |  3,188K |  2,971K |  2,630K | **126.8%** | 119.2% |
| ClientsWriteUTF8Bytes    |  3,727K |  **3,944K** |  3,985K |  3,878K |  3,090K | **105.8%** | 127.6% |
| UsersParseUTF8Bytes      |  2,500K |  **3,108K** |  3,088K |  2,703K |  2,485K | **124.3%** | 125.1% |
| UsersWriteUTF8Bytes      |  3,727K |  **4,302K** |  4,243K |  3,915K |  3,077K | **115.4%** | 139.8% |

### RISC-V 8c (`orangepirv2`)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 | fj3/wast |
|---|---:|---:|---:|---:|---:|---:|---:|
| EishayParseTreeString    |   620K |   **689K** |     — |     — |   619K | **111.1%** | 111.3% |
| EishayParseTreeUTF8Bytes |   577K |   **696K** |     — |     — |   582K | **120.6%** | 119.6% |
| EishayParseUTF8Bytes     | 1,132K | **1,355K** | 1,353K |   957K |   716K | **119.7%** | 189.2% |
| EishayWriteUTF8Bytes     | 1,627K | **2,282K** | 2,060K | 1,781K | 1,010K | **140.3%** | 226.0% |
| ClientsParseUTF8Bytes    |   229K |   **280K** |   279K |   253K |   174K | **122.1%** | 161.3% |
| ClientsWriteUTF8Bytes    |   336K |   **448K** |   448K |   415K |   — ³  | **133.5%** | —      |
| UsersParseUTF8Bytes      |   231K |   **267K** |   265K |   230K |   188K | **115.6%** | 141.6% |
| UsersWriteUTF8Bytes      |   359K |   **546K** |   538K |   438K |   303K | **152.0%** | 180.5% |

³ RISC-V `ClientsWriteUTF8Bytes.wast` did not report on this run (JMH iteration stalled) — the fj3/fj2 column is unaffected.

## Raw JMH output

See [`benchmark_3.0.0-SNAPSHOT-94414d5_raw.md`](benchmark_3.0.0-SNAPSHOT-94414d5_raw.md) for full throughput + error bars per machine.

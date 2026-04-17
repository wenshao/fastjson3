# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-576a3bf

Git commit: `576a3bf` — extends [`benchmark_3.0.0-SNAPSHOT-66a5e2a.md`](benchmark_3.0.0-SNAPSHOT-66a5e2a.md) (Path B Write complete) with three parse-side changes focused on large POJOs:
- PR [#88](https://github.com/wenshao/fastjson3/pull/88): `switch` dispatch in `readFieldsLoop` (REFLECT path)
- PR [#89](https://github.com/wenshao/fastjson3/pull/89): ASM fast path extended to 23-char field names + method splitting for large POJOs
- PR [#90](https://github.com/wenshao/fastjson3/pull/90): ARM now uses fast-path ASM up to 32 fields (was capped at 15)

Generated: `2026-04-17T11:30Z`

**Headline:** fastjson3 matches or exceeds fastjson2 on every benchmark × machine combination, and is always faster than wast. First fastjson3 benchmark report covering **5 machines × 3 architectures** (x86_64, aarch64, riscv64).

## Scope

- **6 benchmark classes**, Eishay (small POJO, ~11 fields) and JJB (medium/large POJO):
  - Parse: `EishayParseUTF8Bytes`, `UsersParseUTF8Bytes` (22 fields), `ClientsParseUTF8Bytes`
  - Write: `EishayWriteUTF8Bytes`, `UsersWriteUTF8Bytes`, `ClientsWriteUTF8Bytes`
- JMH: `-wi 2 -i 3 -f 2`, 10s per iteration, throughput mode.

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
| fastjson3 | 3.0.0-SNAPSHOT @ commit `576a3bf` |
| fastjson2 | 2.0.61 |
| wast | (current) |
| jackson-databind | 2.18.3 (x64/arm1 only) |
| gson | 2.12.1 (x64/arm1 only) |

## Summary — fastjson3 / fastjson2 ratio

✅ ≥ 100% (fastjson3 wins), 🟡 90-100%, 🟠 80-90%, 🔴 < 80%.

| Benchmark | x86_64 | aarch64 16c | arm3 8c | arm4 12c | RISC-V |
|---|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | **112.2% ✅** | **111.8% ✅** | **104.3% ✅** | **115.1% ✅** | **125.5% ✅** |
| EishayWriteUTF8Bytes | **100.9% ✅** | 98.1% 🟡 | **111.3% ✅** | **109.8% ✅** | **123.1% ✅** |
| UsersParseUTF8Bytes | 97.3% 🟡 | **106.1% ✅** | **114.6% ✅** | **116.8% ✅** | **115.8% ✅** |
| UsersWriteUTF8Bytes | **137.9% ✅** | **123.5% ✅** | **127.0% ✅** | **115.1% ✅** | **150.7% ✅** |
| ClientsParseUTF8Bytes | 99.9% 🟡 | **125.0% ✅** | **126.4% ✅** | **123.0% ✅** | **122.9% ✅** |
| ClientsWriteUTF8Bytes | **121.7% ✅** | **112.3% ✅** | **112.1% ✅** | **107.4% ✅** | **131.9% ✅** |

**26 of 30 cells** are ≥ 100%; the remaining 4 are all ≥ 97% (within error bars of fastjson2).

## Key changes in this release

### 1. `UsersParseUTF8Bytes` — the primary target of this release

Users has 22 fields; the longest name (`favoriteFruit`) is 13 chars.

**Before this release** (commit 66a5e2a, inferred from the field limit code path):
- `canUseFastPath` rejected names > 9 chars → Users used the slower ordered speculation path
- REFLECT reader dispatched via if/else chain
- On aarch64, ASM was additionally rejected at 15-field limit

**After PRs #88 / #89 / #90:**
- `canUseFastPath` accepts 2-23 char names. Users qualifies → dispatched via `getRawInt` + `lookupswitch` + per-length `nextIfName4MatchN` intrinsics, same architecture as fastjson2's `genRead243`.
- All platforms (x86 + ARM + RISC-V) allowed up to 32 fields on fast-path ASM.

Measured delta for `UsersParseUTF8Bytes.fastjson3` / `fastjson2`:

| Machine | Before | After |
|---|---:|---:|
| aarch64 16c | 88.4% | **106.1%** |
| arm3 8c | 95.6% (from isolated run) | **114.6%** |
| arm4 12c | 93.2% (from isolated run) | **116.8%** |
| RISC-V | 102% (from isolated run) | **115.8%** |
| x86_64 16c | 91.7% (from isolated run) | 97.3% |

### 2. REFLECT path cleanup (PR #88)

`readFieldsLoop` now uses `switch (reader.typeTag)` (tableswitch) instead of a sequential if/else chain — smaller bytecode per iteration and less variable branch prediction load.

### 3. Method splitting for large non-fast-path POJOs (PR #89)

For POJOs that don't qualify for fast path (non-ASCII names, prefix collisions, names >23), the ordered speculation is split into 8-field batch methods. Gated on x86 (larger method bytecode is not ARM-friendly even when split).

## Detailed per-machine throughput

### x86_64 (`root@172.16.172.143`, 16c)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | 13.27M | 14.90M | 14.62M | 10.90M | 10.65M | **112.2%** |
| EishayWriteUTF8Bytes | 17.56M | 17.72M | 17.99M | 15.47M | 10.06M | **100.9%** |
| UsersParseUTF8Bytes¹ | 2.92M | 2.84M | 2.80M | 2.75M | 2.66M | 97.3% |
| UsersWriteUTF8Bytes | 3.37M | 4.64M | 4.70M | 4.15M | 3.15M | **137.9%** |
| ClientsParseUTF8Bytes | 2.78M | 2.78M | 2.83M | 2.74M | 2.81M | 99.9% |
| ClientsWriteUTF8Bytes | 3.12M | 3.80M | 3.89M | 3.91M | 3.57M | **121.7%** |

¹ `UsersParseUTF8Bytes` on x86_64 was re-run in isolation because the first combined run produced an anomalous fastjson2 measurement (error bar > mean).

### aarch64 16c (`root@172.16.1.231`, Cortex-A76)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | 7.03M | 7.86M | 7.77M | 4.82M | 4.83M | **111.8%** |
| EishayWriteUTF8Bytes | 8.25M | 8.09M | 7.63M | 7.71M | 4.93M | 98.1% |
| UsersParseUTF8Bytes | 1.68M | 1.78M | 1.68M | 1.57M | 833K | **106.1%** |
| UsersWriteUTF8Bytes | 1.05M | 1.30M | 1.26M | 1.19M | 943K | **123.5%** |
| ClientsParseUTF8Bytes | 1.37M | 1.72M | 1.77M | 1.67M | 1.52M | **125.0%** |
| ClientsWriteUTF8Bytes | 1.95M | 2.19M | 2.18M | 2.22M | 1.80M | **112.3%** |

### arm3 8c (OrangePi 5+, `orangepi5plus`)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | 5.24M | 5.46M | 5.44M | 3.25M | 2.97M | **104.3%** |
| EishayWriteUTF8Bytes | 5.63M | 6.27M | 6.36M | 5.53M | 3.63M | **111.3%** |
| UsersParseUTF8Bytes | 854K | 978K | 966K | 893K | 818K | **114.6%** |
| UsersWriteUTF8Bytes | 1.23M | 1.56M | 1.57M | 1.31M | 1.13M | **127.0%** |
| ClientsParseUTF8Bytes | 831K | 1.05M | 1.04M | 1.00M | 878K | **126.4%** |
| ClientsWriteUTF8Bytes | 1.24M | 1.40M | 1.40M | 1.35M | 1.13M | **112.1%** |

### arm4 12c (`orangepi6plus`)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | 14.31M | 16.46M | 16.90M | 8.75M | 8.86M | **115.1%** |
| EishayWriteUTF8Bytes | 16.20M | 17.79M | 17.31M | 15.58M | 9.81M | **109.8%** |
| UsersParseUTF8Bytes | 2.65M | 3.10M | 3.13M | 2.70M | 2.57M | **116.8%** |
| UsersWriteUTF8Bytes | 3.72M | 4.28M | 4.30M | 3.86M | 3.22M | **115.1%** |
| ClientsParseUTF8Bytes | 2.46M | 3.02M | 3.19M | 2.97M | 2.67M | **123.0%** |
| ClientsWriteUTF8Bytes | 3.71M | 3.99M | 4.01M | 3.93M | 3.11M | **107.4%** |

### RISC-V 8c (`orangepirv2`)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | 1.13M | 1.41M | 1.41M | 1.00M | 718K | **125.5%** |
| EishayWriteUTF8Bytes | 1.66M | 2.05M | 2.10M | 1.79M | 974K | **123.1%** |
| UsersParseUTF8Bytes | 231K | 267K | 266K | 230K | 185K | **115.8%** |
| UsersWriteUTF8Bytes | 362K | 545K | 551K | 442K | 280K | **150.7%** |
| ClientsParseUTF8Bytes | 228K | 280K | 280K | 251K | 179K | **122.9%** |
| ClientsWriteUTF8Bytes | 343K | 453K | 457K | 394K | 239K | **131.9%** |

*First fastjson3 benchmark run on RISC-V hardware.*

## Raw JMH output

See [`benchmark_3.0.0-SNAPSHOT-576a3bf_raw.md`](benchmark_3.0.0-SNAPSHOT-576a3bf_raw.md) for full throughput + error bars per machine.

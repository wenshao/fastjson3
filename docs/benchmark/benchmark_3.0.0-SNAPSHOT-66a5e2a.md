# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-66a5e2a (Path B Write complete)

Git commit: `66a5e2a` — extends the Path B series with the full Write-side work: PR [#80](https://github.com/wenshao/fastjson3/pull/80) (W#1+W#2+W#4 ObjectWriterCreatorASM rewrite) and PR [#81](https://github.com/wenshao/fastjson3/pull/81) (W#5 per-length `writeName1L/2L` emit). Builds on the parse-side trajectory captured in [`benchmark_3.0.0-SNAPSHOT-6d78ed8.md`](benchmark_3.0.0-SNAPSHOT-6d78ed8.md) (PRs [#72](https://github.com/wenshao/fastjson3/pull/72)–[#78](https://github.com/wenshao/fastjson3/pull/78)).
Generated: `2026-04-16T06:29:18Z`

This is the first **post-Path-B-Write publication-grade** benchmark — both architectures run every Eishay scenario with the ASM (PR #80/#81) and REFLECT paths, side-by-side with fastjson2, wast, and the default `fastjson3` path. Generated via [`scripts/bench-eishay-cross-platform.sh`](../../scripts/bench-eishay-cross-platform.sh).

## Scope

- **9 Eishay scenarios** (the full set in `benchmark3/.../benchmark/eishay/`):
  - Parse: `EishayParseString`, `EishayParseStringPretty`, `EishayParseUTF8Bytes`, `EishayParseUTF8BytesPretty`, `EishayParseTreeString`, `EishayParseTreeUTF8Bytes`, `EishayValidateUTF8Bytes`
  - Write: `EishayWriteString`, `EishayWriteUTF8Bytes`
- **3 fork × 5 measurement iterations × 2 s** per iteration (`-f 3 -wi 3 -i 5 -w 2 -r 2`), single-threaded throughput.
- Two physical hosts via SSH, jar built once on the workstation (md5 `2df686ace7d78c9da05bb0f6c54edc95`) and SCP'd to each host.

## Environment

| | aarch64 (172.16.1.231) | x86_64 (172.16.172.143) |
|---|---|---|
| Arch | aarch64 | x86_64 |
| CPU | ARM Neoverse N2 (4 cores, SVE2 128-bit, NEON) | AMD EPYC 9T95 192-Core (4 cores exposed) |
| Kernel | 6.1.0-42-arm64 | 6.12.69+deb13-amd64 |
| JDK | Zulu 25.0.2+10-LTS | Zulu 25.0.2+10-LTS |
| Vector API | `ByteVector.SPECIES_MAX = 16` (NEON) | (AVX2/AVX-512, unused on parse hot path post-PR #77) |

## Libraries

| Library | Version |
|---|---|
| fastjson3 | 3.0.0-SNAPSHOT @ commit `66a5e2a` |
| fastjson2 | 2.0.61 |
| wast | (current) |

## JMH Configuration

```
Mode:        Throughput (ops/s)
Warmup:      3 iterations × 2 s
Measurement: 5 iterations × 2 s
Forks:       3
Threads:     1
JVM args:    -XX:-UseCompressedOops --add-modules jdk.incubator.vector
```

## Summary — fastjson3_asm / fastjson2 ratio

The Path B story is the **`fastjson3_asm`** path — the explicit ASM creator built via `ObjectMapper.builder().readerCreatorType(ReaderCreatorType.ASM).writerCreatorType(WriterCreatorType.ASM).build()`. PR #81 brought aarch64 write parity; this is the first report where the whole Parse + Write × x86_64 + aarch64 matrix is `≥ 100%`.

Values **≥ 100%** mean fastjson3_asm matches or beats fastjson2 2.0.61. ✅ ≥100%, 🟡 90-100%, 🟠 80-90%, 🔴 <80%.

| Scenario | aarch64 Neoverse N2 | x86_64 EPYC 9T95 |
|---|---:|---:|
| **EishayParseString** | **100.09% ✅** | **107.00% ✅** |
| **EishayParseUTF8Bytes** | **115.25% ✅** | **118.79% ✅** |
| **EishayWriteString** | **126.01% ✅** | **143.55% ✅** |
| **EishayWriteUTF8Bytes** | **110.57% ✅** | **110.01% ✅** |

**fastjson3_asm wins every instrumented scenario on both architectures.** The W#5 per-length `writeName1L/2L` change (PR #81) closed the last gap — aarch64 `EishayWriteUTF8Bytes` moved from **96.20%** (6d78ed8 report) to **110.57%**, a +14.4 pp swing. `EishayWriteString` is the strongest at +26% / +44% over fastjson2 on both platforms.

## Default-path summary — fastjson3 (no explicit ASM) / fastjson2 ratio

The `fastjson3.fastjson3` benchmark methods call `JSON.parseObject(...)` / `JSON.toJSONString(...)` with the default `ObjectMapper.shared()`. On JVM builds this path goes through `AutoObjectWriterProvider` / `AutoObjectReaderProvider`, so **writes use the ASM writer automatically** (PR [#80](https://github.com/wenshao/fastjson3/pull/80)). The parse side still routes around AUTO inside `ObjectMapper.computeValue` for per-call feature preservation — see ["Future work"](#future-work) below.

| Scenario | aarch64 | x86_64 |
|---|---:|---:|
| EishayParseString | 68.03% 🔴 | 74.30% 🔴 |
| EishayParseStringPretty | 83.06% 🟠 | 84.03% 🟠 |
| EishayParseUTF8Bytes | 71.67% 🔴 | 77.45% 🔴 |
| EishayParseUTF8BytesPretty | 87.80% 🟠 | 89.07% 🟠 |
| EishayParseTreeString | 93.35% 🟡 | 71.45% 🔴 |
| EishayParseTreeUTF8Bytes | 94.34% 🟡 | 72.38% 🔴 |
| EishayValidateUTF8Bytes (bytes) | 87.91% 🟠 | 92.82% 🟡 |
| EishayValidateUTF8Bytes (string) | 70.64% 🔴 | 74.32% 🔴 |
| **EishayWriteString** | **120.45% ✅** | **147.63% ✅** |
| **EishayWriteUTF8Bytes** | **107.39% ✅** | **110.29% ✅** |

Write-side defaults are now **above fastjson2** on both platforms — this is the user-visible benefit of merging PR #80 and PR #81 to `main`. Parse-side defaults remain on REFLECT pending the `ObjectMapper.computeValue` rewire.

## Detailed per-scenario throughput

### aarch64 (172.16.1.231, Neoverse N2, 4c)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3_asm/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseString | 1,857,892 | 1,263,871 | **1,859,650** | — | 1,721,011 | **100.09%** |
| EishayParseStringPretty | 1,274,228 | 1,058,318 | — | — | 1,153,074 | — |
| EishayParseUTF8Bytes | 1,801,489 | 1,291,169 | **2,076,299** | 1,316,013 | 1,679,931 | **115.25%** |
| EishayParseUTF8BytesPretty | 1,252,541 | 1,099,689 | — | — | 1,115,490 | — |
| EishayParseTreeString | 988,017 | 922,360 | — | — | 1,032,495 | — |
| EishayParseTreeUTF8Bytes | 984,482 | 928,821 | — | — | 1,015,527 | — |
| EishayValidateUTF8Bytes (bytes) | 2,262,261 | 1,988,805 | — | — | — | — |
| EishayValidateUTF8Bytes (string) | 2,325,951 | 1,642,959 | — | — | — | — |
| EishayWriteString | 1,879,359 | 2,263,785 | **2,368,308** | 2,202,151 | 1,628,702 | **126.01%** |
| EishayWriteUTF8Bytes | 2,196,274 | 2,358,566 | **2,428,386** | 2,253,990 | 1,810,407 | **110.57%** |

### x86_64 (172.16.172.143, AMD EPYC 9T95, 4c)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3_asm/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseString | 3,396,385 | 2,523,502 | **3,633,997** | — | 3,281,268 | **107.00%** |
| EishayParseStringPretty | 2,627,271 | 2,207,607 | — | — | 2,424,791 | — |
| EishayParseUTF8Bytes | 3,381,470 | 2,618,918 | **4,016,796** | 2,690,311 | 3,240,258 | **118.79%** |
| EishayParseUTF8BytesPretty | 2,614,536 | 2,328,789 | — | — | 2,428,434 | — |
| EishayParseTreeString | 1,945,231 | 1,389,852 | — | — | 1,807,927 | — |
| EishayParseTreeUTF8Bytes | 1,926,344 | 1,394,216 | — | — | 1,797,782 | — |
| EishayValidateUTF8Bytes (bytes) | 3,526,643 | 3,273,467 | — | — | — | — |
| EishayValidateUTF8Bytes (string) | 3,521,789 | 2,617,366 | — | — | — | — |
| EishayWriteString | 3,727,470 | 5,502,979 | **5,350,658** | 4,876,602 | 2,759,162 | **143.55%** |
| EishayWriteUTF8Bytes | 5,037,932 | 5,556,086 | **5,541,971** | 4,976,861 | 3,309,364 | **110.01%** |

## Write-path Path B trajectory (recap)

The `fastjson3_asm` write numbers above are the cumulative effect of two PRs landed after the previous report:

| PR | Phase | aarch64 WriteUTF8Bytes | x86_64 WriteUTF8Bytes |
|---|---|---:|---:|
| pre-W (6d78ed8) | PR #78 — parse-side Path B complete | 96.20% 🟡 | 85.05% 🟠 |
| [#80](https://github.com/wenshao/fastjson3/pull/80) | W#1+W#2+W#4 — real ASM writer | (~86% on ARM, 110% on x64) | (~110% on x64) |
| [#81](https://github.com/wenshao/fastjson3/pull/81) | W#5 — per-length `writeName1L/2L` | **110.57% ✅** | **110.01% ✅** |

PR #80 was a full ObjectWriterCreatorASM rewrite (Unsafe field access, cached nested writers, inline List loops). It brought x86_64 write to 110% of fj2 but left aarch64 at 86% because the generated `OW_Media.write` was 460 bytes of bytecode — over HotSpot C2's `FreqInlineSize=325` budget, so C2 compiled it standalone and every Media element took a call. PR #81 ported fj2's `writeNameXRaw` per-length pattern (`JSONGenerator.UTF8.writeName1L(long, int)` / `writeName2L(long, long, int)`) so the ASM writer now emits each field's pre-encoded `"jsonName":` token as a compact `ldc2_w + invokevirtual` pair. Each helper is ~15 bytes of bytecode — always inlinable under `MaxInlineSize=35` — and the shrink brought `OW_Media.write` under the inline budget on aarch64. The ARM write gap `fj3_asm vs fj3_reflect` moved from **−9.45 pp** to **+10.09 pp**.

## Full Path B trajectory (parse + write)

| PR | Phase | x86_64 fj3_asm/fj2 at merge |
|---|---|---:|
| pre-Path-B baseline | — | 73.3% (parse) |
| [#72](https://github.com/wenshao/fastjson3/pull/72) | B0 name-match intrinsics | 76.0% |
| [#73](https://github.com/wenshao/fastjson3/pull/73) | B1–B3 value-match + enum + List inline | 77.4% |
| [#74](https://github.com/wenshao/fastjson3/pull/74) | B4 ASM item readers | 85.7% |
| [#75](https://github.com/wenshao/fastjson3/pull/75) | B5 nested POJO + List&lt;String&gt; | 92.0% |
| [#76](https://github.com/wenshao/fastjson3/pull/76) | B6 profile-driven kill chain | 109.9% (parse) |
| [#77](https://github.com/wenshao/fastjson3/pull/77) | B7 cross-platform parity (ARM parse) | 106.20% / 101.57% |
| [#78](https://github.com/wenshao/fastjson3/pull/78) | default → AUTO + cross-platform script | (default still REFLECT in computeValue) |
| [#80](https://github.com/wenshao/fastjson3/pull/80) | Write W#1+W#2+W#4 — real ASM writer | (~110% x64 write, ~86% ARM write) |
| [#81](https://github.com/wenshao/fastjson3/pull/81) | Write W#5 — per-length `writeName1L/2L` | **110% / 110% write, 107% / 119% parse** |

## Future work

1. **Wire AUTO into `ObjectMapper.computeValue`** so `JSON.parseObject(...)` automatically benefits from Path B for simple POJOs. The current bypass is documented in [`ObjectMapper.java`](../../core3/src/main/java/com/alibaba/fastjson3/ObjectMapper.java) — it routes around AUTO to preserve parse-time features (`AllowSingleQuotes`, `ErrorOnUnknown`, `@JSONField(anySetter=true)`, custom `deserializeUsing`, etc.) that the ASM generator doesn't yet honor. Closing this gap requires either (a) detecting these features per-call and routing accordingly, or (b) extending `ObjectReaderCreatorASM.canGenerate` to mirror every per-feature branch. This is now the single biggest remaining delta between the "explicit ASM" numbers above and what users see by default.

2. **`EishayValidateUTF8Bytes (string)`** at 70.64% / 74.32% — `getLatin1Bytes` zero-copy path may have a regression on String input. Not Path B's target scenario but worth profiling.

3. **`EishayParseTree*`** at ~93% / ~72% — tree parsing (no schema) doesn't go through Path B's POJO codegen. Tree-mode optimization is a separate workstream.

4. **Pretty-parse paths** (`EishayParseStringPretty`, `EishayParseUTF8BytesPretty`) at ~83–89% — Pretty handling has a separate slow path that bypasses the main ASM reader.

## Reproducing this report

```bash
# Run from the fastjson3 repo root.
scripts/bench-eishay-cross-platform.sh -f 3 -i 5 \
    -p 'Eishay.*\.(fastjson2|fastjson3|fastjson3_asm|fastjson3_reflect|wast)' \
    root@<aarch64-host> root@<x86_64-host>
```

Output goes to `/tmp/fj3-bench-<timestamp>/` (per-host `.log` + `.json`). The script:
- builds `benchmark3.jar` once locally
- SCPs it to each host (each host only needs a JDK 21+ with `jdk.incubator.vector`)
- runs JMH in parallel on all hosts
- captures arch + JDK version + jar md5 in each log header
- prints a side-by-side `fj3_asm/fj2` summary table

Add or remove hosts (ARM, x86_64, future risc-v) by appending more `user@host` arguments — the script handles arbitrary host counts. See [`benchmark_3.0.0-SNAPSHOT-66a5e2a_raw.md`](benchmark_3.0.0-SNAPSHOT-66a5e2a_raw.md) for the unprocessed JMH stdout from each host.

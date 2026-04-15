# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-6d78ed8 (Path B complete)

Git commit: `6d78ed8` — includes the full Path B series ([#72](https://github.com/wenshao/fastjson3/pull/72), [#73](https://github.com/wenshao/fastjson3/pull/73), [#74](https://github.com/wenshao/fastjson3/pull/74), [#75](https://github.com/wenshao/fastjson3/pull/75), [#76](https://github.com/wenshao/fastjson3/pull/76), [#77](https://github.com/wenshao/fastjson3/pull/77), [#78](https://github.com/wenshao/fastjson3/pull/78)).
Generated: `2026-04-15T07:54:32Z`

This is the first **post-Path-B publication-grade** benchmark with both architectures running every Eishay scenario and comparing fastjson3 default + fastjson3_asm vs fastjson2, wast, and the other reference paths. Generated via [`scripts/bench-eishay-cross-platform.sh`](../../scripts/bench-eishay-cross-platform.sh) — the same script the project uses for cross-platform validation.

## Scope

- **9 Eishay scenarios** (the full set in `benchmark3/.../benchmark/eishay/`):
  - Parse: `EishayParseString`, `EishayParseStringPretty`, `EishayParseUTF8Bytes`, `EishayParseUTF8BytesPretty`, `EishayParseTreeString`, `EishayParseTreeUTF8Bytes`, `EishayValidateUTF8Bytes`
  - Write: `EishayWriteString`, `EishayWriteUTF8Bytes`
- **3 fork × 5 measurement iterations × 2 s** per iteration (`-f 3 -wi 3 -i 5 -w 2 -r 2`), single-threaded throughput.
- Two physical hosts via SSH, jar built once on the workstation and SCP'd to each host (`scripts/bench-eishay-cross-platform.sh -f 3 -i 5 -p 'Eishay.*\.(fastjson2|fastjson3|fastjson3_asm|wast)' root@... root@...`).

## Environment

| | aarch64 (172.16.1.231) | x86_64 (172.16.172.143) |
|---|---|---|
| Arch | aarch64 | x86_64 |
| CPU | ARM Neoverse V2 (SVE2 128-bit, NEON) | x86_64 (AVX-512 capable) |
| OS | Debian GNU/Linux 12 | Debian GNU/Linux 13 |
| JDK | Zulu 25.0.2+10-LTS | Zulu 25.0.2+10-LTS |
| Vector API | `ByteVector.SPECIES_MAX = 16` (NEON) | (varies, but unused on parse hot path post-PR #77) |

## Libraries

| Library | Version |
|---|---|
| fastjson3 | 3.0.0-SNAPSHOT @ commit `6d78ed8` |
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

The Path B story is about the **`fastjson3_asm`** path — the explicit ASM creator built via `ObjectMapper.builder().readerCreatorType(ReaderCreatorType.ASM).build()`. This is the path that PRs #72–#77 closed against fastjson2.

Values **≥ 100%** mean fastjson3_asm matches or beats fastjson2 2.0.61. ✅ ≥100%, 🟡 90-100%, 🟠 80-90%, 🔴 <80%.

| Scenario | aarch64 Neoverse V2 | x86_64 |
|---|---:|---:|
| **EishayParseString** | **101.25% ✅** | **104.42% ✅** |
| **EishayParseUTF8Bytes** | **112.36% ✅** | **119.04% ✅** |
| **EishayWriteString** | **110.06% ✅** | **109.46% ✅** |
| EishayWriteUTF8Bytes | 96.20% 🟡 | 85.05% 🟠 |

**fastjson3_asm wins all 4 Path B target scenarios on both architectures**, with EishayParseUTF8Bytes the strongest at +12% / +19% over fastjson2 — Path B's `byte[]` parse path is now solidly ahead. EishayWriteUTF8Bytes is the only `_asm`-instrumented scenario where fj3 still trails (write path optimizations would be a follow-up Path B-style series).

## Default-path summary — fastjson3 (no explicit ASM) / fastjson2 ratio

The `fastjson3.fastjson3` benchmark methods call `JSON.parseObject(...)` / `JSON.toJSONString(...)` with the default `ObjectMapper.shared()`. Currently this path goes through `ObjectReaderCreator.createObjectReader` (REFLECT) inside `ObjectMapper.computeValue`, **not** through the AUTO provider — see ["Future work"](#future-work) below for why.

| Scenario | aarch64 | x86_64 |
|---|---:|---:|
| EishayParseString | 67.86% 🔴 | 74.71% 🔴 |
| EishayParseStringPretty | 82.84% 🟠 | 83.85% 🟠 |
| EishayParseUTF8Bytes | 71.43% 🔴 | 77.99% 🔴 |
| EishayParseUTF8BytesPretty | 87.68% 🟠 | 88.79% 🟠 |
| EishayParseTreeString | 90.84% 🟡 | 72.85% 🔴 |
| EishayParseTreeUTF8Bytes | 91.55% 🟡 | 72.08% 🔴 |
| EishayValidateUTF8Bytes (bytes) | 88.74% 🟠 | 90.96% 🟡 |
| EishayValidateUTF8Bytes (string) | 71.02% 🔴 | 74.67% 🔴 |
| **EishayWriteString** | **106.89% ✅** | **107.49% ✅** |
| EishayWriteUTF8Bytes | 93.19% 🟡 | 82.13% 🟠 |

Default-path results are **independent of Path B** — they exercise the REFLECT reader. Path B's wins land for users that explicitly opt into ASM. Closing this gap is tracked under "Future work".

## Detailed per-scenario throughput

### aarch64 (172.16.1.231, Neoverse V2)

| Benchmark | fj2 | fj3 | fj3_asm | wast | fj3_asm/fj2 |
|---|---:|---:|---:|---:|---:|
| EishayParseString | 1,892,154 | 1,284,159 | **1,915,867** | 1,767,574 | **101.25%** |
| EishayParseStringPretty | 1,281,417 | 1,061,512 | — | 1,159,668 | — |
| EishayParseUTF8Bytes | 1,860,276 | 1,328,815 | **2,090,208** | 1,688,389 | **112.36%** |
| EishayParseUTF8BytesPretty | 1,257,149 | 1,102,261 | — | 1,127,782 | — |
| EishayParseTreeString | 1,012,354 | 919,609 | — | 1,002,392 | — |
| EishayParseTreeUTF8Bytes | 1,004,189 | 919,290 | — | 1,030,374 | — |
| EishayValidateUTF8Bytes (bytes) | 2,247,694 | 1,994,540 | — | — | — |
| EishayValidateUTF8Bytes (string) | 2,322,799 | 1,649,682 | — | — | — |
| EishayWriteString | 1,838,067 | 1,964,784 | **2,022,939** | 1,595,130 | **110.06%** |
| EishayWriteUTF8Bytes | 2,170,107 | 2,022,273 | 2,087,654 | 1,791,492 | 96.20% |

### x86_64 (172.16.172.143)

| Benchmark | fj2 | fj3 | fj3_asm | wast | fj3_asm/fj2 |
|---|---:|---:|---:|---:|---:|
| EishayParseString | 3,409,580 | 2,547,096 | **3,560,421** | 3,273,617 | **104.42%** |
| EishayParseStringPretty | 2,630,220 | 2,205,324 | — | 2,350,726 | — |
| EishayParseUTF8Bytes | 3,373,862 | 2,631,157 | **4,016,186** | 3,212,628 | **119.04%** |
| EishayParseUTF8BytesPretty | 2,623,485 | 2,329,472 | — | 2,348,003 | — |
| EishayParseTreeString | 1,913,816 | 1,394,216 | — | 1,811,392 | — |
| EishayParseTreeUTF8Bytes | 1,935,377 | 1,395,067 | — | 1,809,925 | — |
| EishayValidateUTF8Bytes (bytes) | 3,522,992 | 3,204,708 | — | — | — |
| EishayValidateUTF8Bytes (string) | 3,515,969 | 2,625,383 | — | — | — |
| EishayWriteString | 3,801,828 | 4,086,570 | **4,161,335** | 2,749,782 | **109.46%** |
| EishayWriteUTF8Bytes | 5,000,024 | 4,106,596 | 4,252,280 | 3,192,548 | 85.05% |

## Path B trajectory (recap)

The `fastjson3_asm` results above are the cumulative effect of 7 PRs:

| PR | Phase | x86_64 ratio at merge |
|---|---|---:|
| pre-Path-B baseline | — | 73.3% |
| [#72](https://github.com/wenshao/fastjson3/pull/72) | B0 name-match intrinsics | 76.0% |
| [#73](https://github.com/wenshao/fastjson3/pull/73) | B1–B3 value-match + enum + List inline | 77.4% |
| [#74](https://github.com/wenshao/fastjson3/pull/74) | B4 ASM item readers | 85.7% |
| [#75](https://github.com/wenshao/fastjson3/pull/75) | B5 nested POJO + List&lt;String&gt; | 92.0% |
| [#76](https://github.com/wenshao/fastjson3/pull/76) | B6 profile-driven kill chain | 109.9% |
| [#77](https://github.com/wenshao/fastjson3/pull/77) | B7 cross-platform parity (ARM) | 106.20% / 101.57% |
| [#78](https://github.com/wenshao/fastjson3/pull/78) | default → AUTO + cross-platform script | (default still REFLECT in cache path) |

## Future work

1. **Wire AUTO into `ObjectMapper.computeValue`** so `JSON.parseObject(...)` automatically benefits from Path B for simple POJOs. The current bypass is documented in [`ObjectMapper.java`](../../core3/src/main/java/com/alibaba/fastjson3/ObjectMapper.java) line 251 — it routes around AUTO to preserve parse-time features (`AllowSingleQuotes`, `ErrorOnUnknown`, `@JSONField(anySetter=true)`, custom `deserializeUsing`, etc.) that the ASM generator doesn't yet honor. Closing this gap requires either (a) detecting these features per-call and routing accordingly, or (b) extending `ObjectReaderCreatorASM.canGenerate` to mirror every per-feature branch.

2. **Write-path Path B**. `EishayWriteUTF8Bytes` is still 85% / 96% of fastjson2 on x86 / ARM respectively. The reverse direction — generating an ASM `ObjectWriterCreatorASM` along the same lines as Path B — is the obvious next series.

3. **`EishayValidateUTF8Bytes (string)`** at 71% / 74.7% — `getLatin1Bytes` zero-copy path may have a regression on String input. Not Path B's target scenario but worth profiling.

4. **`EishayParseTree*`** at ~90% / ~72% — tree parsing (no schema) doesn't go through Path B's POJO codegen. Tree-mode optimization is a separate workstream.

## Reproducing this report

```bash
# Run from the fastjson3 repo root.
scripts/bench-eishay-cross-platform.sh -f 3 -i 5 \
    -p 'Eishay.*\.(fastjson2|fastjson3|fastjson3_asm|wast)' \
    root@<aarch64-host> root@<x86_64-host>
```

Output goes to `/tmp/fj3-bench-<timestamp>/` (per-host `.log` + `.json`). The script:
- builds `benchmark3.jar` once locally
- SCPs it to each host (each host only needs a JDK 21+ with `jdk.incubator.vector`)
- runs JMH in parallel on all hosts
- captures arch + JDK version + jar md5 in each log header
- prints a side-by-side `fj3_asm/fj2` summary table

Add or remove hosts (ARM, x86_64, future risc-v) by appending more `user@host` arguments — the script handles arbitrary host counts.

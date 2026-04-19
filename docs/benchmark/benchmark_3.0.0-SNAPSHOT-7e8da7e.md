# fastjson3 Benchmark Report — 3.0.0-SNAPSHOT-7e8da7e

Git commit: `7e8da7e` — extends [`benchmark_3.0.0-SNAPSHOT-94414d5.md`](benchmark_3.0.0-SNAPSHOT-94414d5.md) with the parse-side reader work that landed since PR #102 (writer enum) and the ASM generator bug-fix that surfaced during it:

- PR [#108](https://github.com/wenshao/fastjson3/pull/108): docs — README stats refresh + `graalvm.md` rewrite for current schema
- PR [#109](https://github.com/wenshao/fastjson3/pull/109): clear error message for Record with missing primitive field
- PR [#110](https://github.com/wenshao/fastjson3/pull/110): honour `TypeReference<List|Set|Map<X>>` at top level
- PR [#111](https://github.com/wenshao/fastjson3/pull/111): include `line:col` in `JSONException` location
- PR [#112](https://github.com/wenshao/fastjson3/pull/112): nested generic, inheritance (`Child extends Parent<Bean>`), wildcard, ParameterizedType POJO support
- PR [#113](https://github.com/wenshao/fastjson3/pull/113): JSON-path breadcrumbs in parse error messages (`(path: outer.field[N].child)`)
- PR [#114](https://github.com/wenshao/fastjson3/pull/114): demote >15-field beans to batched ASM path + extract `readFieldsFallback`
- PR [#115](https://github.com/wenshao/fastjson3/pull/115): native-image smoke-test coverage + JVM-side ASM large-bean regression test that exposed and fixed a `VerifyError` PR #114 introduced

Generated: `2026-04-19T13:30Z`

**Headline:** fastjson3 wins or ties on 25 of 30 cells (5 machines × 6 benchmarks). The 5 sub-100% cells are 4× `UsersParseUTF8Bytes` (x86_64 92.1%, aarch64 16c 92.5%, arm4 93.7%, RISC-V 97.8%) plus 1× `ClientsParseUTF8Bytes` on x86_64 (99.96% — within error of parity). `UsersParseUTF8Bytes` on arm3 sits at 108%, the only above-parity cell on that benchmark; the rest is the persistent ASM-vs-fastjson2 22-field gap tagged "do not retry" in PR #102's session, structural and not yet closed.

## Scope

- **6 benchmark classes**, Eishay (small POJO, ~11 fields) and JJB (medium/large POJOs):
  - Parse: `EishayParseUTF8Bytes`, `UsersParseUTF8Bytes` (22 fields), `ClientsParseUTF8Bytes`
  - Write: `EishayWriteUTF8Bytes`, `UsersWriteUTF8Bytes`, `ClientsWriteUTF8Bytes`
- JMH: `-wi 3 -i 5 -f 2`, 10s per iteration, throughput mode (`-bm thrpt -tu ms`).
- RISC-V: lighter `-wi 2 -i 3 -f 1` config and only the `fastjson2` + `fastjson3_asm` columns (per-fork startup is ~5× slower than aarch64; full matrix locked the box).

## Environment

| | x86_64 | aarch64 (16c) | OrangePi 5+ | OrangePi 6+ | RISC-V |
|---|---|---|---|---|---|
| Host | `root@172.16.172.143` | `root@172.16.1.231` | `arm3` (`orangepi5plus`) | `arm4` (`orangepi6plus`) | `riscv` (`orangepirv2`) |
| Arch | x86_64 | aarch64 | aarch64 | aarch64 | riscv64 |
| Cores | 16 | 16 (Cortex-A76) | 8 | 12 | 8 |
| JDK | Zulu 25.0.2 | Zulu 25.0.2 | Zulu 25 | Zulu 25 | OpenJDK 25+37 |
| Threads | 16 | 16 | 8 | 12 | 4 |

## Libraries

| Library | Version |
|---|---|
| fastjson3 | 3.0.0-SNAPSHOT @ commit `7e8da7e` |
| fastjson2 | 2.0.61 |
| wast | (current, x86/aarch64 only) |

## Summary — fastjson3_asm / fastjson2 ratio

✅ ≥ 100% (fastjson3 wins), 🟡 90–100%, 🟠 80–90%, 🔴 < 80%.

| Benchmark | x86_64 | aarch64 16c | arm3 8c | arm4 12c | RISC-V 8c |
|---|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | **114.9% ✅** | **113.6% ✅** | **102.4% ✅** | **117.3% ✅** | **133.6% ✅** |
| EishayWriteUTF8Bytes¹ | **112.1% ✅** | **113.4% ✅** | **114.5% ✅** | **109.1% ✅** | **144.5% ✅** |
| UsersParseUTF8Bytes | 92.1% 🟡 | 92.5% 🟡 | **108.0% ✅** | 93.7% 🟡 | 97.8% 🟡 |
| UsersWriteUTF8Bytes | **141.3% ✅** | **119.4% ✅** | **125.6% ✅** | **112.6% ✅** | **177.2% ✅** |
| ClientsParseUTF8Bytes | 99.96% 🟡 | **122.8% ✅** | **113.1% ✅** | **115.8% ✅** | **109.4% ✅** |
| ClientsWriteUTF8Bytes | **121.7% ✅** | **109.7% ✅** | **109.0% ✅** | **108.5% ✅** | **131.9% ✅** |

¹ x86_64 EishayWrite was re-run in isolation because the first combined run produced an anomalous fastjson2 measurement (`17840 ±548` clean re-run vs `14470 ±4429` first run, > 30% error bar — the box scheduled a noisy neighbor at the same time).

**25 of 30 cells ≥ 100%.** Sub-parity breakdown:
- `UsersParseUTF8Bytes` — 4 of 5 machines below parity (92–98%); arm3 the lone outlier at 108%, presumably because the OrangePi 5+'s A76 big.LITTLE cores hit a different JIT cliff than the bigger aarch64 box.
- `ClientsParseUTF8Bytes` on x86_64 — 99.96%, within error of parity.

## Key changes since the previous report (`94414d5`)

### Reader correctness + ergonomics (PR #109–#113, #115)

Five PRs ship as a usability bundle. None move performance materially, but each closes a documented gap that surfaced in a usability audit:

- **`Record` with missing primitive field** (#109) — pre-fix, the canonical-constructor invocation surfaced as JDK-internals NPE; now a clean `JSONException: cannot construct record User: required int field 'age' is missing or null`.
- **`TypeReference<List|Set|Map<X>>` at top level** (#110) — pre-fix, Jackson/Gson migration code threw `no ObjectReader registered for type: java.util.List<User>`. Now works including nested `Map<String, List<User>>`.
- **`line:col` in JSONException location** (#111) — every `at offset N` now reads `at offset N (line L, col C)`.
- **Full generic support** (#112) — nested collection/map fields (`List<List<T>>`, `Map<String, List<T>>`), inheritance (`Child extends Parent<Bean>` with TypeVariable resolution), wildcards (`List<? extends Bean>`), `Set<E>`/`Map<K,V>` fields with typed key conversion, and parameterized POJO via `TypeReference<Box<Bean>>`. 23 new tests in `NestedGenericFieldTest`.
- **JSON-path breadcrumbs in parse errors** (#113) — `expected '{' at offset 42 (path: teams.blue.members[0].age)`. Side effect: `int age` field receiving JSON `"bad"` used to leak a raw `ClassCastException` — now surfaces as a `JSONException` with the failing field's path. 12 new tests in `ParseErrorPathTest`.

### ASM generator architectural cleanup (PR #114 + bug fix #115)

Refactor of `ObjectReaderCreatorASM` for prefix-unique > 15-field beans:

- `useFastPath` helper centralizes the fast-path decision (combines existing prefix-uniqueness with a `FAST_PATH_INLINE_BUDGET_FIELDS = 15` cap).
- Beans above the cap fall into the non-fast-path's already-existing batched emission (`readFieldsBatch0/1/...`).
- `genericLoopTop`'s ~900 byte body extracted into a private `readFieldsFallback` method.

For `jjb.Users.User` (22 fields): `readObjectUTF8` shrinks from 2151 → 241 bytes — comfortably under C2's `FreqInlineSize=325`.

**Important caveat.** PR #114 originally claimed +5% on aarch64 Users. Re-bench after PR #115 fixed the underlying `VerifyError` (that PR #114 itself introduced — `generateReadFieldsBatches` never called `visitMaxs`, so the loaded class failed verification and `createObjectReader`'s silent catch returned the REFLECT fallback) shows the structural change is **perf-neutral**:

| Arch | pre-PR #114 fj3_asm | post-PR #115 fj3_asm | Δ |
|---|---:|---:|---:|
| x86_64 16c | 2730 ±16 | 2734 ±16 | +0.1% noise |
| aarch64 16c | 1427 ±22 | 1440 ±7 | +0.9% noise |

The architectural cleanup stands; the perf claim was a measurement artifact. Lesson: silent catch-and-fallback hides VerifyErrors as "fallback chose REFLECT" — indistinguishable in bench output. PR #115 added `AsmLargeBeanTest` whose first assertion (`generated.getName().startsWith("…gen.")`) catches this regression class.

### Native-image (PR #115)

Smoke-test (`scripts/test-native-image.sh`) extended from 4 minimal checks to 12 covering all PR #110–#114 features end-to-end in a native binary. Cold start preserved at 2-3 ms native vs 133 ms JVM (≈50× faster), 19.5 MB binary on GraalVM CE 25.0.2.

## Detailed per-machine throughput

### x86_64 (`root@172.16.172.143`, 16c, Zulu 25.0.2, 16 threads)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3_asm/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | 13.30M | 14.62M | 15.28M | 11.16M | 10.81M | **114.9% ✅** |
| EishayWriteUTF8Bytes¹ | 17.84M | 20.50M | 20.00M | 17.81M | 10.45M | **112.1% ✅** |
| UsersParseUTF8Bytes | 2.96M | 2.66M | 2.73M | 2.72M | 2.75M | 92.1% 🟡 |
| UsersWriteUTF8Bytes | 3.26M | 4.58M | 4.61M | 4.15M | 3.08M | **141.3% ✅** |
| ClientsParseUTF8Bytes | 2.74M | 2.69M | 2.74M | 2.70M | 2.72M | 99.96% 🟡 |
| ClientsWriteUTF8Bytes | 3.15M | 3.78M | 3.83M | 3.83M | 3.56M | **121.7% ✅** |

### aarch64 16c — Cortex-A76 (`root@172.16.1.231`, Zulu 25.0.2, 16 threads)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3_asm/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | 6.94M | 7.76M | 7.88M | 5.76M | 4.85M | **113.6% ✅** |
| EishayWriteUTF8Bytes | 8.04M | 8.62M | 9.11M | 7.80M | 5.86M | **113.4% ✅** |
| UsersParseUTF8Bytes | 1.55M | 1.43M | 1.43M | 1.42M | 1.46M | 92.5% 🟡 |
| UsersWriteUTF8Bytes | 2.00M | 2.41M | 2.39M | 2.21M | 1.81M | **119.4% ✅** |
| ClientsParseUTF8Bytes | 1.33M | 1.62M | 1.64M | 1.60M | 1.42M | **122.8% ✅** |
| ClientsWriteUTF8Bytes | 2.00M | 2.21M | 2.19M | 2.22M | 1.77M | **109.7% ✅** |

### arm3 — OrangePi 5+ 8c (`arm3`, Zulu 25, 8 threads)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3_asm/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | 5.06M | 5.27M | 5.18M | 3.10M | 3.04M | **102.4% ✅** |
| EishayWriteUTF8Bytes | 5.57M | 6.75M | 6.38M | 5.43M | 3.65M | **114.5% ✅** |
| UsersParseUTF8Bytes | 0.825M | 0.882M | 0.891M | 0.885M | 0.819M | **108.0% ✅** |
| UsersWriteUTF8Bytes | 1.23M | 1.54M | 1.55M | 1.30M | 1.11M | **125.6% ✅** |
| ClientsParseUTF8Bytes | 0.831M | 0.946M | 0.940M | 0.940M | 0.869M | **113.1% ✅** |
| ClientsWriteUTF8Bytes | 1.26M | 1.37M | 1.37M | 1.31M | 1.11M | **109.0% ✅** |

### arm4 — OrangePi 6+ 12c (`arm4`, Zulu 25, 12 threads)

| Benchmark | fj2 | fj3 | fj3_asm | fj3_reflect | wast | fj3_asm/fj2 |
|---|---:|---:|---:|---:|---:|---:|
| EishayParseUTF8Bytes | 14.23M | 16.43M | 16.69M | 8.46M | 9.20M | **117.3% ✅** |
| EishayWriteUTF8Bytes | 17.42M | 19.17M | 19.01M | 16.50M | 10.15M | **109.1% ✅** |
| UsersParseUTF8Bytes | 2.83M | 2.66M | 2.65M | 2.63M | 2.48M | 93.7% 🟡 |
| UsersWriteUTF8Bytes | 3.77M | 4.27M | 4.24M | 3.85M | 3.12M | **112.6% ✅** |
| ClientsParseUTF8Bytes | 2.41M | 2.76M | 2.79M | 2.75M | 2.62M | **115.8% ✅** |
| ClientsWriteUTF8Bytes | 3.66M | 4.02M | 3.97M | 3.88M | 3.04M | **108.5% ✅** |

### RISC-V 8c (`riscv`, OpenJDK 25+37, 4 threads, lighter `-wi 2 -i 3 -f 1`)

| Benchmark | fj2 | fj3_asm | fj3_asm/fj2 |
|---|---:|---:|---:|
| EishayParseUTF8Bytes | 0.609M | 0.813M | **133.6% ✅** |
| EishayWriteUTF8Bytes | 0.854M | 1.234M | **144.5% ✅** |
| UsersParseUTF8Bytes | 0.129M | 0.127M | 97.8% 🟡 |
| UsersWriteUTF8Bytes | 0.167M | 0.297M | **177.2% ✅** |
| ClientsParseUTF8Bytes | 0.126M | 0.138M | **109.4% ✅** |
| ClientsWriteUTF8Bytes | 0.182M | 0.240M | **131.9% ✅** |

## Open: persistent UsersParseUTF8Bytes gap

Four of five machines show fastjson3_asm at 92–98% on Users — the 22-field bean. arm3 is the exception at 108%. This is the same gap PR #102's session (2026-04-17) tagged "stuck at ~95-97% structurally, do not retry". 2026-04-18 follow-up tried two targeted fixes (`readFieldSeparatorFast` substitution; manual inline of `swarScanString`); both were profile-attribution wins with zero throughput delta. PR #114's batched-path refactor confirms perf-neutrality. Closing this last cell would require either (a) a `readStringValueFastAndSep()` fusion that updates all ASM emit sites, or (b) profile drill-down for fundamentally new optimization lanes — neither of which has yielded measurable progress in three independent attempts. Recorded as a known structural ceiling for now.

## Reproducibility

```bash
# Build and ship to all 5 machines
cd benchmark3 && mvn package -DskipTests
for h in root@172.16.172.143 root@172.16.1.231 arm3 arm4 riscv; do
  ssh "$h" "rm -rf /tmp/bench_pub && mkdir -p /tmp/bench_pub/target"
  scp target/benchmark3.jar "$h:/tmp/bench_pub/target/benchmark3.jar"
  tar -czf - src/main/resources/data/ | ssh "$h" "cd /tmp/bench_pub && tar -xzf -"
done

# Run on each (threads = $(nproc))
ssh root@172.16.172.143 "cd /tmp/bench_pub && /root/Install/jdk25/bin/java -jar target/benchmark3.jar \
  -wi 3 -i 5 -f 2 -t 16 -bm thrpt -tu ms \
  '(Eishay|Users|Clients)(Parse|Write)UTF8Bytes\\.(fastjson2|fastjson3|fastjson3_asm|fastjson3_reflect|wast)\$'"
```

Raw JMH output (`Score ± Error` per row): see [`benchmark_3.0.0-SNAPSHOT-7e8da7e_raw.md`](benchmark_3.0.0-SNAPSHOT-7e8da7e_raw.md).

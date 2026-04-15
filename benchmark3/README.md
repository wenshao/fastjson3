# Benchmark Module (benchmark3)

JMH-based JSON performance benchmarks comparing fastjson3 against fastjson2, jackson, gson, and wast.

## Quick Start

```bash
cd benchmark3

# Build
mvn package -q -DskipTests

# Run (auto-captures environment, saves structured results)
./run-benchmark.sh eishay quick        # ~3 min, fast check
./run-benchmark.sh jjb                 # ~15 min, standard
./run-benchmark.sh all full            # ~45 min, publication quality

# Compare results across machines
./compare-results.sh
```

## Profiles

| Profile | Warmup | Measure | Forks | Skip reflect/asm | ~Time |
|---------|--------|---------|-------|-------------------|-------|
| `quick` | 1 | 2 | 1 | Yes | ~3 min |
| `standard` | 2 | 3 | 2 | No | ~15 min |
| `full` | 3 | 5 | 3 | No | ~45 min |

Single-thread variant: `./run-benchmark.sh eishay standard 1`

## Benchmark Suites

### Eishay (MediaContent)

Classic JSON benchmark based on MediaContent data (~469 bytes compact).

| Class | Operation | Input | Competitors |
|-------|-----------|-------|-------------|
| `EishayParseUTF8Bytes` | Parse | byte[] → MediaContent | fj2, fj3, fj3_reflect, fj3_asm, jackson, gson, wast |
| `EishayWriteUTF8Bytes` | Write | MediaContent → byte[] | same |
| `EishayParseString` | Parse | String → MediaContent | fj2, fj3, jackson, gson, wast |
| `EishayWriteString` | Write | MediaContent → String | same |
| `EishayParseUTF8BytesPretty` | Parse | Pretty byte[] → MediaContent | fj2, fj3, jackson, gson, wast |
| `EishayParseStringPretty` | Parse | Pretty String → MediaContent | same |
| `EishayParseTreeString` | Parse | String → JSONObject | fj2, fj3, jackson, gson, wast |
| `EishayParseTreeUTF8Bytes` | Parse | byte[] → JSONObject | same |

### JJB (Users + Clients)

Real-world benchmark with complex nested data (~2KB each).

| Class | Operation | Data |
|-------|-----------|------|
| `UsersParseUTF8Bytes` | Parse | Users (22 fields, List<Friend>) |
| `UsersWriteUTF8Bytes` | Write | Users |
| `ClientsParseUTF8Bytes` | Parse | Clients (long[], String[], List<Partner>) |
| `ClientsWriteUTF8Bytes` | Write | Clients |
| `ClientsParseCNBytes` | Parse | Clients with Chinese content |
| `ClientsWriteCNBytes` | Write | Clients with Chinese content |

## Competitors

| Library | Version | Notes |
|---------|---------|-------|
| fastjson3 | 3.0.0-SNAPSHOT | This project |
| fastjson2 | 2.0.29 | Previous generation |
| jackson | 2.18.3 | Industry standard |
| gson | 2.12.1 | Google JSON |
| wast | 0.0.29.1 | High-performance library |

## Result Collection

Results are automatically saved by `run-benchmark.sh` to:

```
results/
├── 2026-03-22-server1/
│   ├── env.json              # Machine metadata (CPU, cores, memory, JDK, OS)
│   ├── eishay.json           # JMH JSON output
│   ├── jjb.json              # JMH JSON output
│   ├── eishay.log            # Full console output
│   ├── jjb.log
│   └── summary.txt           # Human-readable summary
├── 2026-03-22-laptop/
│   └── ...
```

### Cross-Machine Comparison

```bash
# Compare all result directories
./compare-results.sh

# Compare specific runs
./compare-results.sh results/2026-03-20-server1 results/2026-03-21-laptop
```

Output:
```
Machine              2026-03-22-server1   2026-03-22-laptop
CPU                  AMD EPYC 7R13       Apple M2 Pro
Cores                16                   12
JDK                  25                   21.0.1

Benchmark (ops/ms)                         server1              laptop
──────────────────────────────────────── ──────────────────── ────────────────────
eishay.EishayParseUTF8Bytes.fastjson3          5645.2               3210.1
eishay.EishayParseUTF8Bytes.jackson            1234.5                987.3
...
```

## Latest Results

Per-release reports are published under the top-level [`docs/benchmark/`](../docs/benchmark/) directory, following the fastjson2 release benchmark convention.

| Date | Machines | Report |
|------|----------|--------|
| 2026-04-16 | aarch64 (Neoverse-N2) + x86_64 (EPYC 9T95), JDK 25 | [`benchmark_3.0.0-SNAPSHOT-66a5e2a.md`](../docs/benchmark/benchmark_3.0.0-SNAPSHOT-66a5e2a.md) — Path B Write complete (PR #80 + #81) |
| 2026-04-15 | aarch64 (Neoverse-N2) + x86_64 (EPYC 9T95), JDK 25 | [`benchmark_3.0.0-SNAPSHOT-6d78ed8.md`](../docs/benchmark/benchmark_3.0.0-SNAPSHOT-6d78ed8.md) — Path B parse complete |
| 2026-04-12 | aarch64 (Neoverse-N2) + x86_64 (EPYC 9T95), JDK 25 | [`benchmark_3.0.0-SNAPSHOT-4f2fdbf.md`](../docs/benchmark/benchmark_3.0.0-SNAPSHOT-4f2fdbf.md) |

## Manual Usage

```bash
# Run specific benchmark class
java -jar target/benchmark3.jar EishayParseUTF8Bytes

# Run with JMH profiler
java -jar target/benchmark3.jar "EishayParseUTF8Bytes.fastjson3" -prof stack

# Custom JMH options
java -jar target/benchmark3.jar -wi 3 -i 5 -f 2 -t 8 EishayParseUTF8Bytes

# Output JSON for external analysis
java -jar target/benchmark3.jar EishayParseUTF8Bytes -rf json -rff results.json
```

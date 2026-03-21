# Benchmark Module (benchmark3)

JMH-based JSON performance benchmarks for fastjson3.

## JJB Benchmark

Java JSON Benchmark (JJB) is the primary benchmark suite for measuring parse and write performance.

### Benchmark Classes

| Class | Description | Config |
|-------|-------------|--------|
| `JJBBenchmark` | Main entry point, Users/Clients parse+write UTF8 | 16 threads, 2 warmup, 3 iterations |
| `JJBQuickBenchmark` | Quick comparison: fastjson3 vs wast | Single thread |
| `JJBSimpleBenchmark` | Simplified benchmark for CI | - |
| `JJBPerformanceComparison` | Detailed performance comparison | - |
| `QuickCompare` | Fast comparison tool | - |

### Data Type Benchmarks

| Class | Operation | Data Type |
|-------|-----------|-----------|
| `UsersParseUTF8Bytes` | Parse | Users (UTF-8 bytes) |
| `UsersWriteUTF8Bytes` | Write | Users (UTF-8 bytes) |
| `ClientsParseUTF8Bytes` | Parse | Clients (UTF-8 bytes) |
| `ClientsWriteUTF8Bytes` | Write | Clients (UTF-8 bytes) |
| `ClientsParseCNBytes` | Parse | Clients with Chinese (UTF-8 bytes) |
| `ClientsWriteCNBytes` | Write | Clients with Chinese (UTF-8 bytes) |
| `UserParseUTF8Bytes` | Parse | Single User (UTF-8 bytes) |

### Other Benchmarks

| Class | Description |
|-------|-------------|
| `EscapeStringBenchmark` | String escaping performance |
| `UserProviderBenchmark` | Provider-based user data |
| `QuickParseBenchmark` | Quick parse tests |
| `SimpleParseBenchmark` | Simple parse tests |

## Test Data

Located in `src/main/resources/data/jjb/`:

| File | Size | Description |
|------|------|-------------|
| `user.json` | 2.1K | Users list with 22 fields per user |
| `client.json` | 2.2K | Clients with `long[]`, `String[]` arrays |
| `client_cn.json` | 2.2K | Clients with Chinese content |

### Data Models

**Users** (`com.alibaba.fastjson3.benchmark.jjb.Users`)
- `List<User> users`
- User fields: id, index, guid, isActive, balance, picture, age, eyeColor, name, gender, company, email, phone, address, about, registered, latitude, longitude, tags, friends, greeting, favoriteFruit
- Nested: `List<Friend>` (id, name)

**Clients** (`com.alibaba.fastjson3.benchmark.jjb.Clients`)
- `List<Client> clients`
- Client fields: id (long), index, guid, isActive, balance (double), picture, age, eyeColor, name, gender, company, emails (String[]), phones (long[]), address, about, registered, latitude, longitude, tags, partners
- Nested: `List<Partner>` (id, name, since)

## Competitors

The benchmarks compare fastjson3 against:
- **wast** (`io.github.wycst.wast.json`) - Wycst JSON library (current: 0.0.29.1)
- **fastjson2** (`com.alibaba.fastjson2`) - fastjson2 (current: 2.0.29)

## Benchmark Results

Results are stored in `results/` directory by date.

### Latest Results

| Date | File | Benchmarks |
|------|------|------------|
| 2026-03-21 | [results/2026-03-21.md](results/2026-03-21.md) | ClientsParse/WriteUTF8Bytes, UserParse/WriteUTF8Bytes |

### Summary (2026-03-21)

**Parse 场景 - fastjson3 领先**

| Benchmark | fastjson3 vs fastjson2 | fastjson3 vs wast |
|-----------|------------------------|-------------------|
| ClientsParseUTF8Bytes | **+98-102%** | **+8-10%** |
| UserParseUTF8Bytes | **+131-144%** | **+28-35%** |

**Write 场景 - wast 领先**

| Benchmark | fastjson3 vs fastjson2 | fastjson3 vs wast |
|-----------|------------------------|-------------------|
| ClientsWriteUTF8Bytes | **+154%** | **+9%** |
| UsersWriteUTF8Bytes | **+162%** | **+32%** |

**Key Findings:**
- **Parse:** fastjson3 > wast > fastjson2
- **Write:** fastjson3 > wast > fastjson2
- fastjson3 在 Parse 和 Write 场景均全面领先
- Write 场景超越 wast 9-32%

## Running Benchmarks

### Run All JJB Benchmarks
```bash
java -cp benchmark3.jar com.alibaba.fastjson3.benchmark.jjb.JJBBenchmark
```

### Run Quick Comparison
```bash
java -cp benchmark3.jar com.alibaba.fastjson3.benchmark.jjb.JJBQuickBenchmark
```

### Run with Maven
```bash
mvn package -pl benchmark3 -DskipTests
java -jar benchmark3/target/benchmark3.jar JJBBenchmark
```

### Custom JMH Options
```bash
java -jar benchmark3/target/benchmarks.jar -wi 3 -i 5 -f 2 -t 8 JJBBenchmark
```

Options:
- `-wi N` - Warmup iterations
- `-i N` - Measurement iterations
- `-f N` - Forks
- `-t N` - Threads

### Run with Profiler
```bash
java -jar benchmark3/target/benchmark3.jar "ClientsParseUTF8Bytes.fastjson3" -prof stack
```

## Adding New Benchmarks

1. Create test data in `src/main/resources/data/jjb/`
2. Create data model class in `com.alibaba.fastjson3.benchmark.jjb`
3. Create benchmark class with `@Benchmark` methods
4. Register in `JJBBenchmark.java` if needed

Example benchmark method:
```java
@Benchmark
public void parseXxx_fastjson3(Blackhole bh) {
    bh.consume(com.alibaba.fastjson3.JSON.parseObject(bytes, Xxx.class));
}
```

## Recording Results

When running new benchmarks, record results in `results/YYYY-MM-DD.md`:

```markdown
# Benchmark Results - YYYY-MM-DD

## Test Environment
| Item | Value |
|------|-------|
| JDK | ... |
| ... | ... |

## BenchmarkName
...
```

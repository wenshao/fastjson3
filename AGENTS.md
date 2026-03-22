# core3 Agent Guidelines

## Project Overview

fastjson3 core3 is a high-performance JSON library for Java 21+, targeting JVM, GraalVM native-image, and Android 8+.

## Architecture Rules

- **sealed classes**: `JSONParser` and `JSONGenerator` are sealed. Inner classes use short names (`UTF8`, `Str`, `Char`, `CharArray`) — no redundant prefixes.
- **Creator SPI**: ObjectReader/ObjectWriter creation is pluggable via `Function<Class<?>, ObjectReader/Writer<?>>` on `ObjectMapper.Builder`. Default is reflection; ASM is opt-in.
- **@JVMOnly**: Classes annotated with `@com.alibaba.fastjson3.annotation.JVMOnly` are excluded from the Android JAR. Only applies to whole classes, not methods. All `internal/asm/*`, ASM Creators, and `DynamicClassLoader` are `@JVMOnly`.
- **Platform detection**: `JDKUtils.NATIVE_IMAGE` and `JDKUtils.UNSAFE_AVAILABLE` are `static final boolean` constants. JIT constant-folds them, so guarded branches have zero runtime cost.

## Code Conventions

- Shared code (non-`@JVMOnly`) must NOT import `@JVMOnly` classes directly. Use SPI/Function references instead.
- Unsafe operations in `JDKUtils` must always have fallback paths (except `*Direct` methods, which callers gate behind `UNSAFE_AVAILABLE`).
- String internal manipulation (`createAsciiString`, `createLatin1String`) must be guarded by `!NATIVE_IMAGE`.
- New JVM-only classes: add `@JVMOnly` annotation — the Android build profile auto-excludes them.

## Build

```bash
mvn test -pl core3                     # Run all tests (1327 tests)
mvn package -pl core3                  # Full JAR (JVM, 62 classes)
mvn package -pl core3 -Pandroid        # Android JAR (45 classes, 35% smaller)
mvn package -pl core3 -DskipTests      # Skip tests for faster builds
```

## Key Files

| File | Purpose |
|------|---------|
| `JSONParser.java` | Sealed parser (1800+ lines, core parsing logic) |
| `JSONGenerator.java` | Sealed generator (1300+ lines, core serialization) |
| `ObjectMapper.java` | Immutable mapper, central API entry point |
| `JDKUtils.java` | Platform detection, Unsafe ops, String internals |
| `FieldReader.java` / `FieldWriter.java` | Type-tag dispatch field handlers |
| `FieldNameMatcher.java` | Byte-comparison + hash-based field matching |
| `ObjectReaderCreatorASM.java` | @JVMOnly — ASM bytecode reader generation |
| `ObjectWriterCreatorASM.java` | @JVMOnly — ASM bytecode writer generation |

## Benchmark

See `benchmark3/README.md` for details.

**Quick Reference:**
- `JJBBenchmark` — Main JMH benchmark entry (parse/write Users & Clients, 16 threads)
- `JJBQuickBenchmark` — Quick comparison fastjson3 vs wast
- Test data: `benchmark3/src/main/resources/data/jjb/` (user.json, client.json, client_cn.json)
- Run: `java -cp benchmark3.jar com.alibaba.fastjson3.benchmark.jjb.JJBBenchmark`

## Design Docs

- `docs/internals/architecture.md` — Overall architecture, platform support, module structure
- `docs/internals/optimization/` — Performance optimization techniques (SWAR, putLong, etc.)

## User Documentation

- `docs/README.md` — Documentation index
- `docs/start/` — Getting started tutorials
- `docs/api/` — API reference
- `docs/guides/` — Topic guides (JSONPath, validation, performance, etc.)
- `docs/advanced/` — Advanced topics
- `docs/migration/` — Migration guides
- `docs/samples/` — Runnable code samples
- `docs/api/quick-reference.md` — Consolidated API index (best entry point for AI)

## Documentation Quality

When modifying documentation, run `./scripts/check-docs.sh` to verify accuracy.
The script checks for common errors:
- Non-existent enum values, annotation parameters, builder methods
- Wrong class references (JSONReader/JSONWriter don't exist in fastjson3)
- Wrong JSONGenerator method names (use startObject not writeStartObject)
- Broken internal links

Key rules for documentation:
- Use `ReadFeature` / `WriteFeature`, NOT `JSONReader.Feature` / `JSONWriter.Feature`
- Use `addMixIn` (capital I), NOT `addMixin`
- Use `enableRead` / `enableWrite`, NOT `enableReader` / `readFeatures`
- `JSONSchema.parseSchema(String)`, NOT `JSONSchema.of(String)` (of() only takes JSONObject)
- No `.dateFormat()` or `.namingStrategy()` on ObjectMapper.builder() — use annotations instead
- `@JSONField` has NO `unwrapped`, `raw`, `using`, or `writeEnumUsingName` parameters
- `@JSONType` has NO `serializer`, `deserializer`, `format`, or `ignore` (boolean) parameters

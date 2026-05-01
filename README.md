# fastjson3

The next generation of [fastjson2](https://github.com/alibaba/fastjson2), **built entirely with AI Coding**.

fastjson3 adopts a [Jackson 3](https://github.com/FasterXML/jackson-core) style API design (`ObjectMapper`, `readValue`, `writeValueAsString`), making it easy to migrate from both fastjson2 and Jackson. Under the hood, it inherits fastjson2's high-performance parsing and serialization engine.

[![CI](https://github.com/wenshao/fastjson3/actions/workflows/ci.yml/badge.svg)](https://github.com/wenshao/fastjson3/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/wenshao/fastjson3/branch/main/graph/badge.svg)](https://app.codecov.io/gh/wenshao/fastjson3)

## By the Numbers

| Metric | Value |
|--------|-------|
| Source files (core3 main) | 112 |
| Lines of code (core3 main) | 46,000+ |
| Test cases | 2,068 (1,985 JVM + 83 Android) |
| Test pass rate | 100% |
| Hand-written code | 0 lines |

## Documentation

**[Documentation Index](docs/README.md)** — Complete documentation with tutorials, guides, API references, and samples.

Quick links:
- [Getting Started](docs/start/00-5-minutes.md) — 5-minute quick start
- [API Reference](docs/api/) — Core API documentation
- [Guides](docs/guides/) — JSONPath, validation, performance, etc.
- [Migration](docs/migration/) — From fastjson2, Jackson, Gson, org.json

## Highlights

- **100% AI Coding** — not a single line of code is written by hand, from architecture design to implementation to testing, all by AI
- **Jackson 3 compatible API** — `ObjectMapper.builder()`, `readValue()`, `writeValueAsString()`, Mixin, Module SPI, familiar to Jackson users
- **fastjson2 performance** — inherits fastjson2's high-performance engine: ASM codegen, SWAR, Vector API (SIMD), zero-copy parsing
- **Easy migration** — supports both `@JSONField` (fastjson) and `@JsonProperty` (Jackson) annotations simultaneously
- **Modern Java** — sealed classes, records, pattern matching, Vector API (SIMD), requires JDK 21+
- **Comprehensive features** — JSONPath (RFC 9535), JSON Schema (Draft 2020-12), Mixin, Filters, custom codecs
- **Zero dependencies** — core module has no runtime dependencies

## Requirements

- JDK 21+

## Install

Core library:

```xml
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Bill of Materials (BOM)

For multi-module projects, import the BOM and omit `<version>` on individual fastjson3 dependencies:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alibaba.fastjson3</groupId>
            <artifactId>fastjson3-bom</artifactId>
            <version>3.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Ecosystem modules

| Artifact | Purpose |
|----------|---------|
| `fastjson3-kotlin` | Kotlin reified inline extensions (`parseAs<T>()`, `parseList<T>()`, `parseMap<T>()`, `toJSON()`) |
| `fastjson3-spring` | Spring 6.x HttpMessageConverter (servlet) + WebFlux codecs + Spring MVC `AbstractView` + Redis serializer |
| `fastjson3-spring-boot-autoconfigure` | Spring Boot 3.2+ auto-configuration (servlet + reactive — Boot 3.0/3.1 fail at runtime because Spring 6.0 ASM can't read JDK 21 bytecode) |
| `fastjson3-spring-boot-starter` | Spring Boot starter (pom-only aggregator) |
| `fastjson3-jaxrs-jakarta` | JAX-RS `@Provider` for `jakarta.ws.rs` (Jakarta EE 9+ — Jersey 3.x, RESTEasy 6.x, CXF 4.x) |
| `fastjson3-jaxrs-javax` | JAX-RS `@Provider` for `javax.ws.rs` (legacy Jakarta EE 8 — Jersey 2.x, RESTEasy 4.x/5.x) |
| `fastjson3-kafka` | Kafka `Serializer<T>` / `Deserializer<T>` (Kafka 3.x — SPI stable across the line) |
| `fastjson3-jpa-jakarta` | JPA `AttributeConverter<T, String>` for `jakarta.persistence` (Jakarta Persistence 3.x — Hibernate 6.x, EclipseLink 4.x) |
| `fastjson3-jpa-javax` | JPA `AttributeConverter<T, String>` for `javax.persistence` (legacy JPA 2.x) |
| `fastjson3-mybatis` | MyBatis `BaseTypeHandler<T>` for JSON columns (MyBatis 3.5+) |

## Migration from fastjson2

```java
// fastjson2 style — still works
JSONObject obj = JSON.parseObject(jsonStr);
User user = JSON.parseObject(jsonStr, User.class);
String json = JSON.toJSONString(user);

// Jackson 3 style — recommended
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(jsonStr, User.class);
String json = mapper.writeValueAsString(user);
```

## Modern API

```java
// Parse with semantic configuration
User user = JSON.parse(json, User.class);                          // default
User user = JSON.parse(json, User.class, ParseConfig.LENIENT);    // config files
User user = JSON.parse(json, User.class, ParseConfig.STRICT);     // API contracts

// Serialize with semantic configuration
String json = JSON.write(user);                                    // default
String json = JSON.write(user, WriteConfig.PRETTY);                // pretty print
String json = JSON.write(user, WriteConfig.WITH_NULLS);            // include nulls

// Type-safe collection parsing
List<User> users = JSON.parseList(json, User.class);
Map<String, User> map = JSON.parseMap(json, User.class);
Set<String> tags = JSON.parseSet(json, String.class);
```

## Quick Start

```java
// Parse
JSONObject obj = JSON.parseObject(jsonStr);
User user = JSON.parseObject(jsonStr, User.class);
JSONArray arr = JSON.parseArray(jsonStr);

// Serialize
String json = JSON.toJSONString(user);
byte[] bytes = JSON.toJSONBytes(user);

// Validate
boolean valid = JSON.isValid(jsonStr);
boolean isObj = JSON.isValidObject(jsonStr);
boolean isArr = JSON.isValidArray(jsonStr);
```

## ObjectMapper

Thread-safe, immutable-after-construction mapper with builder pattern:

```java
// Shared default instance
ObjectMapper mapper = ObjectMapper.shared();
User user = mapper.readValue(json, User.class);
String json = mapper.writeValueAsString(user);

// Custom configuration
ObjectMapper mapper = ObjectMapper.builder()
    .enableRead(ReadFeature.AllowComments, ReadFeature.SupportSmartMatch)
    .enableWrite(WriteFeature.PrettyFormat, WriteFeature.WriteNulls)
    .build();

// Derive variant from existing mapper
ObjectMapper pretty = mapper.rebuild()
    .enableWrite(WriteFeature.PrettyFormat)
    .build();
```

### Stream I/O

```java
// Read from InputStream / Reader
User user = mapper.readValue(inputStream, User.class);
User user = mapper.readValue(reader, User.class);

// Write to OutputStream
mapper.writeValue(outputStream, user);
```

### Mixin Annotations

```java
ObjectMapper mapper = ObjectMapper.builder()
    .addMixin(ThirdPartyClass.class, ThirdPartyMixin.class)
    .build();
```

### Custom Reader / Writer

```java
// Register custom codec
ObjectReader<Money> moneyReader = new ObjectReader<>() {
    @Override
    public Money readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
        return new Money(parser.readString());
    }
};
mapper.registerReader(Money.class, moneyReader);

ObjectWriter<Money> moneyWriter = new ObjectWriter<>() {
    @Override
    public void write(JSONGenerator gen, Object object, Object fieldName, Type fieldType, long features) {
        gen.writeString(((Money) object).toString());
    }
};
mapper.registerWriter(Money.class, moneyWriter);
```

## JSONPath (RFC 9535)

```java
// Compile once, reuse many times
JSONPath path = JSONPath.of("$.store.book[0].title");

// Evaluate on parsed object
String title = path.eval(jsonObject, String.class);

// Stream mode - extract from raw JSON without full parse
String title = path.extract(jsonString, String.class);

// Multi-path typed extraction
JSONPath multi = JSONPath.of(
    new String[]{"$.name", "$.age"},
    new Type[]{String.class, Integer.class}
);
Object[] values = (Object[]) multi.extract(jsonString);
```

## JSONValidator

Lightweight syntax validator — no object allocation, just structural verification:

```java
try (JSONValidator v = JSONValidator.from(jsonStr)) {
    if (v.validate()) {
        JSONValidator.Type type = v.getType(); // Object, Array, or Value
    }
}

// Or use convenience methods
JSON.isValid(jsonStr);              // any valid JSON
JSON.isValidObject(jsonStr);        // must be { ... }
JSON.isValidArray(jsonStr);         // must be [ ... ]
```

## JSON Schema (Draft 2020-12)

```java
JSONSchema schema = JSONSchema.parseSchema(schemaJson);
ValidateResult result = schema.validate(dataJson);
if (!result.isSuccess()) {
    System.out.println(result.getMessage());
}
```

## JSON Pointer (RFC 6901)

```java
JSONPointer pointer = JSONPointer.of("/store/book/0/title");

// Evaluate
String title = pointer.eval(jsonObject, String.class);

// Modify
pointer.set(jsonObject, "New Title");

// Remove
JSONPointer.of("/store/book/0").remove(jsonObject);

// Check existence
boolean exists = pointer.exists(jsonObject);
```

## JSON Patch (RFC 6902)

```java
String target = "{\"foo\":\"bar\"}";
String patch = """
    [
        {"op":"add","path":"/baz","value":"qux"},
        {"op":"remove","path":"/foo"},
        {"op":"replace","path":"/baz","value":"boo"}
    ]
    """;
String result = JSONPatch.apply(target, patch);
// {"baz":"boo"}
```

Supported operations: `add`, `remove`, `replace`, `move`, `copy`, `test`

## Annotations

```java
public class User {
    @JSONField(name = "user_name")
    public String name;

    @JSONField(format = "yyyy-MM-dd")
    public LocalDate birthday;

    @JSONField(serialize = false)
    public String password;
}

@JSONType(naming = NamingStrategy.SnakeCase)
public class Config {
    public String serverName;  // -> "server_name"
}
```

## Features

### ReadFeature
| Feature | Description |
|---------|-------------|
| `FieldBased` | Field-based access instead of getter/setter |
| `AllowSingleQuotes` | Allow `'string'` in JSON |
| `AllowUnquotedFieldNames` | Allow `{key: value}` |
| `AllowComments` | Allow `//` and `/* */` comments |
| `SupportSmartMatch` | Case/underscore insensitive field matching |
| `SupportAutoType` | Polymorphic type detection |
| `ErrorOnUnknownProperties` | Throw on unknown fields |

### WriteFeature
| Feature | Description |
|---------|-------------|
| `PrettyFormat` | Indented JSON output |
| `WriteNulls` | Include null fields |
| `WriteEnumsUsingName` | Enum as name instead of ordinal |
| `WriteLongAsString` | Long as string for JavaScript |
| `SortMapEntriesByKeys` | Sorted map keys |
| `BrowserCompatible` | Escape special chars for browsers |
| `OptimizedForAscii` | Optimized output for ASCII content |

## Performance

Key optimizations:
- **SWAR (SIMD Within A Register)** — 8-byte parallel character scanning and escape detection
- **Vector API (SIMD)** — accelerated string scanning using `jdk.incubator.vector` on supported JDKs
- **JIT-friendly architecture** — compact hot loops with ordered field speculation, enabling deep JIT inlining
- **Zero-copy parsing** — direct field name matching with pre-computed long-word headers
- **Unsafe field access** — direct memory read/write bypassing reflection and bounds checking
- **Buffer pooling** — thread-local buffer reuse to minimize GC pressure

### Benchmark Reports

Per-release benchmark reports following the [fastjson2 convention](https://github.com/alibaba/fastjson2/tree/main/docs/benchmark) — Eishay + JJB scenarios, 5 machines × 3 architectures (x86_64, aarch64, riscv64), with raw JMH output and error bars:

- [`docs/benchmark/`](docs/benchmark/) — all release reports
- Latest: [`benchmark_3.0.0-SNAPSHOT-94414d5.md`](docs/benchmark/benchmark_3.0.0-SNAPSHOT-94414d5.md) — **39 of 40** benchmark × machine cells ≥ 100% of fastjson2 2.0.61 (the 40th is x86_64 `UsersParseUTF8Bytes` at 93.9%, within noise), every cell beats wast. Tree-mode parse 107-128% across all 5 machines.

Methodology: JMH throughput mode (`ops/s`), `-wi 2 -i 3 -f 2 -t $threads`, 10s per iteration — runs in parallel across all 5 hosts via [`benchmark3/run-remote.sh`](benchmark3/run-remote.sh). Reports compare fastjson3 against the latest fastjson2 release plus wast.

## GraalVM Native Image

fastjson3 ships reachability metadata so `native-image` picks up its reflection hooks automatically. At runtime `JDKUtils.NATIVE_IMAGE == true` is detected and the library falls back to the REFLECT creator path (ASM bytecode generation is not available under SubstrateVM). The library's own `String` / `Unsafe` fast paths degrade cleanly to the JDK-standard APIs.

You register your own POJOs (or run the GraalVM `native-image-agent` on a JVM workload to record usage). Smoke-test the setup end-to-end with the bundled script:

```bash
./scripts/test-native-image.sh
```

It builds a native binary of the sample `NativeImageTest` main class and prints JVM-vs-native cold-start. Reference measurement on GraalVM CE 25.0.2 / x86_64:

| Metric | JVM | Native |
|---|---:|---:|
| Cold start | 114 ms | **2-3 ms** (~45×) |
| Binary size | — | 19.3 MB |
| Build time | — | ~19 s |

See [`docs/advanced/graalvm.md`](docs/advanced/graalvm.md) for configuration details.

## Project Structure

Published modules (consumable Maven artifacts):

```
fastjson3/
├── core3/                          # Core library (com.alibaba.fastjson3)
│   └── src/main/java/com/alibaba/fastjson3/
│       ├── JSON.java               # Static convenience API
│       ├── ObjectMapper.java        # Thread-safe mapper (builder pattern)
│       ├── JSONParser.java          # High-performance parser
│       ├── JSONGenerator.java       # High-performance serializer
│       ├── JSONPath.java            # JSONPath engine (RFC 9535)
│       ├── JSONObject.java          # Mutable JSON object
│       ├── JSONArray.java           # Mutable JSON array
│       ├── ReadFeature.java         # Deserialization feature flags
│       ├── WriteFeature.java        # Serialization feature flags
│       ├── annotation/              # @JSONField, @JSONType, etc.
│       ├── reader/                  # ObjectReader framework + ASM codegen
│       ├── writer/                  # ObjectWriter framework + ASM codegen
│       ├── schema/                  # JSON Schema validation (Draft 2020-12)
│       ├── jsonpath/                # JSONPath compiler and segments
│       ├── filter/                  # NameFilter, ValueFilter, PropertyFilter
│       └── util/                    # DateUtils, JDKUtils, VectorizedScanner
├── core3-bom/                      # Bill of Materials (BOM) — pin all module versions
├── core3-kotlin/                   # Kotlin extensions
├── core3-spring/                   # Spring 6.x HttpMessageConverter / WebFlux / MVC View / Redis
├── core3-spring-boot-autoconfigure/  # Spring Boot 3.2+ auto-configuration
├── core3-spring-boot-starter/      # Spring Boot starter (pom-only)
├── core3-jaxrs/                    # JAX-RS aggregator (pom-only)
│   ├── core3-jaxrs-jakarta/        # jakarta.ws.rs namespace
│   └── core3-jaxrs-javax/          # javax.ws.rs namespace
├── core3-jpa/                      # JPA aggregator (pom-only)
│   ├── core3-jpa-jakarta/          # jakarta.persistence namespace
│   └── core3-jpa-javax/            # javax.persistence namespace
├── core3-kafka/                    # Kafka Serializer / Deserializer
├── core3-mybatis/                  # MyBatis TypeHandler for JSON columns
└── benchmark3/                     # JMH benchmarks vs fastjson2 and wast
```

Reactor test harnesses (not published): `core3-android-test/`, `core3-spring-test/`.

## Build

```bash
mvn clean test
```

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

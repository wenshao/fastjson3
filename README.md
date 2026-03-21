# fastjson3

The next generation of [fastjson2](https://github.com/alibaba/fastjson2), **built entirely with AI Coding**.

fastjson3 adopts a [Jackson 3](https://github.com/FasterXML/jackson-core) style API design (`ObjectMapper`, `readValue`, `writeValueAsString`), making it easy to migrate from both fastjson2 and Jackson. Under the hood, it inherits fastjson2's high-performance parsing and serialization engine.

[![CI](https://github.com/wenshao/fastjson3/actions/workflows/ci.yml/badge.svg)](https://github.com/wenshao/fastjson3/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/wenshao/fastjson3/branch/main/graph/badge.svg)](https://app.codecov.io/gh/wenshao/fastjson3)

## By the Numbers

| Metric | Value |
|--------|-------|
| Source files | 83 |
| Lines of code | 22,000+ |
| Test cases | 1,327 |
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
mapper.registerReader(Money.class, (parser, type, features) -> {
    return new Money(parser.readString());
});

mapper.registerWriter(Money.class, (gen, obj, features) -> {
    gen.writeString(obj.toString());
});
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
JSONPath.TypedMultiPath multi = JSONPath.typedMulti()
    .path("$.name", String.class)
    .path("$.age", Integer.class)
    .build();
Object[] values = multi.extract(jsonString);
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
JSONSchema schema = JSONSchema.of(schemaJson);
ValidateResult result = schema.validate(dataJson);
if (!result.isSuccess()) {
    System.out.println(result.getMessage());
}
```

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
- **ASM code generation** — runtime-generated bytecode for serialization/deserialization, avoiding reflection overhead
- **Vector API (SIMD)** — accelerated string scanning using `jdk.incubator.vector`
- **Zero-copy parsing** — direct field name matching with pre-computed hash tables
- **Buffer pooling** — thread-local buffer reuse to minimize GC pressure

### Benchmark Results (JDK 25, 16 threads)

| Operation | fastjson2 | fastjson3 | wast | fastjson3 vs fastjson2 |
|-----------|-----------|-----------|------|----------------------|
| Parse Client | 1,921 ops/ms | **5,645 ops/ms** | 4,899 ops/ms | **+193.7%** |
| Parse User | 907 ops/ms | **5,331 ops/ms** | 4,675 ops/ms | **+487.5%** |
| Write Client | 3,091 ops/ms | **7,864 ops/ms** | 7,217 ops/ms | **+154.3%** |
| Write User | 3,225 ops/ms | **8,446 ops/ms** | 6,378 ops/ms | **+161.9%** |

> fastjson3 在 Parse 和 Write 场景下均全面领先，Write 场景超越 wast 9-32%

## Project Structure

```
fastjson3/
├── core3/                          # Core library
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
└── benchmark3/                     # JMH benchmarks vs fastjson2 and wast
```

## Build

```bash
mvn clean test
```

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

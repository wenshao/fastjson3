# fastjson3-spring

Spring Web + WebFlux + Redis JSON adapters for [fastjson3](https://github.com/wenshao/fastjson3). Single artifact, three entry points:

| Entry point | Class | Use case |
|---|---|---|
| Spring Web (servlet) | `Fastjson3HttpMessageConverter` | `@RestController` + `@RequestBody` / `ResponseEntity` round-trip |
| Spring WebFlux (reactive) | `Fastjson3JsonDecoder` + `Fastjson3JsonEncoder` | `Mono` / `Flux` reactive endpoints |
| Spring Data Redis | `Fastjson3RedisSerializer` + `GenericFastjson3RedisSerializer` | `RedisTemplate` value serializer for JSON cache |

Drop-in replacement for Jackson's `MappingJackson2HttpMessageConverter` (servlet), `Jackson2JsonDecoder` / `Jackson2JsonEncoder` (reactive), and `Jackson2JsonRedisSerializer` (Redis).

## Requirements

- **JDK 21+** (matches the fastjson3 project baseline)
- **Spring 6.x / Spring Boot 3.x** (`spring-web` declared as `provided` — the consumer brings its own version)

## Install

```xml
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3-spring</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

> `spring-web` is declared `provided + optional` so this artifact doesn't drag a Spring version onto consumers. Bring your own — `spring-boot-starter-web` provides it transitively for Boot apps, or declare `org.springframework:spring-web` directly for plain Spring apps. Compiled against Spring 6.0; runtime works against any 6.x.

## Usage

### Spring Boot 3 — register as a `@Bean`

```java
import com.alibaba.fastjson3.spring.Fastjson3HttpMessageConverter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@SpringBootApplication(exclude = JacksonAutoConfiguration.class)
public class App {
    @Configuration
    public static class WebConfig implements WebMvcConfigurer {
        @Bean
        public Fastjson3HttpMessageConverter fastjson3HttpMessageConverter() {
            return new Fastjson3HttpMessageConverter();
        }

        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            // Insert at position 0 so fastjson3 wins over the default Jackson
            // converter (if Jackson isn't already excluded).
            converters.add(0, fastjson3HttpMessageConverter());
        }
    }
}
```

### Spring WebFlux — register codecs

```java
import com.alibaba.fastjson3.spring.codec.Fastjson3JsonDecoder;
import com.alibaba.fastjson3.spring.codec.Fastjson3JsonEncoder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer cfg) {
        cfg.defaultCodecs().jackson2JsonDecoder(new Fastjson3JsonDecoder());
        cfg.defaultCodecs().jackson2JsonEncoder(new Fastjson3JsonEncoder());
    }
}
```

### Spring Data Redis — register serializer

```java
import com.alibaba.fastjson3.spring.data.redis.Fastjson3RedisSerializer;
import com.alibaba.fastjson3.spring.data.redis.GenericFastjson3RedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    // Strongly-typed cache: User stored as JSON; reconstruction Class<User>-driven.
    @Bean
    public RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, User> t = new RedisTemplate<>();
        t.setConnectionFactory(cf);
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new Fastjson3RedisSerializer<>(User.class));
        return t;
    }

    // Polymorphic cache: serialized JSON carries @type; today round-trips
    // to JSONObject because ObjectMapper.Builder doesn't yet expose an
    // AutoTypeFilter hook (tracked as fj3 core follow-up). Use
    // Fastjson3RedisSerializer<T> when typed reconstruction matters.
    @Bean
    public RedisTemplate<String, Object> genericRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> t = new RedisTemplate<>();
        t.setConnectionFactory(cf);
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new GenericFastjson3RedisSerializer());
        return t;
    }
}
```

### Custom `ObjectMapper`

Pass a configured mapper to apply per-instance settings (modules, mapSupplier, listSupplier, etc.):

```java
ObjectMapper mapper = ObjectMapper.builder()
        .mapSupplier(java.util.concurrent.ConcurrentHashMap::new)
        .addReaderModule(new MyDomainModule())
        .build();
return new Fastjson3HttpMessageConverter(mapper);    // servlet
return new Fastjson3JsonDecoder(mapper);             // WebFlux decode
return new Fastjson3JsonEncoder(mapper);             // WebFlux encode
return new Fastjson3RedisSerializer<>(User.class, mapper); // Redis typed
return new GenericFastjson3RedisSerializer(mapper);  // Redis generic
```

## Behavior

| Concern | Behavior |
|---|---|
| Media types | `application/json` + `application/*+json` |
| Default charset | UTF-8 (RFC 8259) |
| Request `Content-Type;charset=...` | Honored — body decoded through declared charset (e.g. GBK, ISO-8859-1) |
| Generic types | Read / write via `GenericHttpMessageConverter` (`List<User>`, `Map<K,V>`, generic records) |
| `String` return | Not hijacked — `StringHttpMessageConverter` handles |
| `byte[]` return | Not hijacked — `ByteArrayHttpMessageConverter` handles |
| `Resource` return | Not hijacked — `ResourceHttpMessageConverter` handles |
| Malformed JSON | Wrapped as `HttpMessageNotReadableException` → Spring 400 |

## Test coverage

- 20 servlet unit tests (`Fastjson3HttpMessageConverterTest`): `supports()` exclusions, generic type handling, charset honoring, error wrapping, generic write Content-Type, `StreamingHttpOutputMessage` support
- 22 reactive unit tests (`Fastjson3JsonCodecTest`): Decoder + Encoder canDecode/canEncode, exclusion of `String` / `byte[]` / `ByteBuffer` / `Resource`, NDJSON not advertised, `Mono` / `Flux` round-trip with multi-buffer join semantics, generic `List<T>` via `ResolvableType`, `DecodingException` wrapping, custom `ObjectMapper`, empty `getStreamingMediaTypes`
- 22 Redis unit tests (`Fastjson3RedisSerializerTest`): typed `Fastjson3RedisSerializer<T>` round-trip POJO + nested generic field, null / empty bytes contract, malformed payload → `SerializationException`, custom `ObjectMapper`, null-arg ctor rejection, `getTargetType()` returns concrete `T`, 16-thread × 200-iter concurrency, >10KB payload; `GenericFastjson3RedisSerializer` writes `@type`, current JSONObject fallback (autotype filter pending fj3 core), Map / List value round-trip
- 40 end-to-end MockMvc integration tests in `core3-spring-test` covering POJO / record / `Object` / `JSONObject` / `JSONArray` round-trip, ControllerAdvice error responses, header roundtrip, deep nesting, 16-thread concurrency, etc.

## Migrating from `fastjson2:extension-spring6`

Users moving from fj2's `Fastjson2Decoder` / `Fastjson2Encoder` swap the configuration handle from `FastJsonConfig` to `ObjectMapper`:

| fastjson2 | fastjson3 |
|---|---|
| `new Fastjson2Decoder()` / `Fastjson2Encoder()` | `new Fastjson3JsonDecoder()` / `Fastjson3JsonEncoder()` |
| `new Fastjson2Decoder(new FastJsonConfig())` | `new Fastjson3JsonDecoder(ObjectMapper.shared())` |
| `FastJsonConfig.setReaderFeatures(...)` | `ObjectMapper.builder().enableRead(...).build()` |
| `FastJsonConfig.setWriterFeatures(...)` | `ObjectMapper.builder().enableWrite(...).build()` |
| `FastJsonConfig.setReaderFilters(...)` / `setWriterFilters(...)` | `ObjectMapper.builder().addReaderModule(...)` / `addPropertyFilter(...)` |
| `FastJsonConfig.setDateFormat("yyyy-MM-dd")` | _(open gap on `ObjectMapper.Builder` — track as fj3 core follow-up)_ |
| `MimeType...` ctor varargs | unchanged — same shape |

Behavioral upgrades vs fj2 (you opt into automatically):

- `canDecode` / `canEncode` exclude `String` / `CharSequence` / `byte[]` / `ByteBuffer` / `Resource` so dedicated codecs handle them
- `decode(Publisher<DataBuffer>, ...)` joins buffers before parse (matches multi-chunk HTTP body shape)
- Implements `HttpMessageDecoder<Object>` / `HttpMessageEncoder<Object>` — server-side hint propagation
- `getStreamingMediaTypes()` returns empty (no `application/x-ndjson` until line-framing lands) — fj2 ships ndjson in default mime types but emits unframed bytes

## Related artifacts

- `com.alibaba.fastjson3:fastjson3` — core JSON parsing / writing

# fastjson3-spring

Spring Web + WebFlux + Redis JSON adapters for [fastjson3](https://github.com/wenshao/fastjson3). Single artifact, four entry points:

| Entry point | Class | Use case |
|---|---|---|
| Spring Web (servlet) | `Fastjson3HttpMessageConverter` | `@RestController` + `@RequestBody` / `ResponseEntity` round-trip |
| Spring MVC `View` | `Fastjson3JsonView` | `ModelAndView` / `ContentNegotiatingViewResolver` JSON rendering (legacy MVC) |
| Spring WebFlux (reactive) | `Fastjson3JsonDecoder` + `Fastjson3JsonEncoder` | `Mono` / `Flux` reactive endpoints |
| Spring Data Redis | `Fastjson3RedisSerializer` + `GenericFastjson3RedisSerializer` | `RedisTemplate` value serializer for JSON cache |

Drop-in replacement for Jackson's `MappingJackson2HttpMessageConverter` (servlet), `MappingJackson2JsonView` (legacy view resolver), `Jackson2JsonDecoder` / `Jackson2JsonEncoder` (reactive), and `Jackson2JsonRedisSerializer` (Redis).

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

### Spring Boot 3 — drop in the starter (recommended)

For Boot apps, use `fastjson3-spring-boot-starter` — it auto-registers the servlet converter and reactive codecs based on what's on the classpath, so you don't need any `@Configuration`:

```xml
<dependency>
    <groupId>com.alibaba.fastjson3</groupId>
    <artifactId>fastjson3-spring-boot-starter</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

The starter is a pom-only aggregator that pulls `fastjson3-spring-boot-autoconfigure` (the actual `@AutoConfiguration` classes), which in turn pulls `fastjson3-spring` (the converter / codec implementations). **It does not pull `spring-boot-starter-web` or `spring-boot-starter-webflux`** — by design, so the starter never forces a Spring or Boot version onto consumers. You declare those alongside the starter (any standard Boot app already does):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>      <!-- or -webflux -->
</dependency>
```

Auto-config triggers:

- **Servlet** (`spring-boot-starter-web` present): registers `Fastjson3HttpMessageConverter` ahead of Jackson via Boot's `HttpMessageConverters` discovery.
- **Reactive** (`spring-boot-starter-webflux` present): registers `Fastjson3JsonDecoder` + `Fastjson3JsonEncoder` and a `CodecCustomizer` at `Ordered.LOWEST_PRECEDENCE` that wires them into `ServerCodecConfigurer.defaultCodecs()`, replacing Boot's default Jackson codecs (Boot's Jackson customizer is at `@Order(0)`; ours runs after — last writer wins).
- **Redis**: not auto-configured — declare your `RedisTemplate` and serializer beans manually (the typed/generic choice is too opinionated to default).

User-supplied beans of the same types short-circuit auto-registration via `@ConditionalOnMissingBean` — pass a configured `ObjectMapper` by declaring your own `Fastjson3HttpMessageConverter` bean.

#### Configuration properties

| Property | Default | Description |
|---|---|---|
| `spring.fastjson3.date-format` | _(unset → `ObjectMapper.shared()`)_ | Default date/time format applied at write time to typed POJO Date / Temporal fields without an explicit `@JSONField(format=...)` override. Mirrors `spring.jackson.date-format`. Recognized values: `millis` / `unixtime` / `iso8601`, the five fast-path patterns (`yyyy-MM-dd`, `yyyyMMdd`, `yyyy-MM-dd HH:mm`, `yyyy-MM-dd HH:mm:ss`, `yyyyMMddHHmmss`), or any `DateTimeFormatter` pattern. |

```yaml
spring:
  fastjson3:
    date-format: yyyy-MM-dd HH:mm:ss
```

The property pipes through both `Fastjson3HttpMessageConverter` (servlet) and `Fastjson3JsonEncoder` / `Fastjson3JsonDecoder` (reactive), so a single setting covers both stacks.

If you want only the auto-config classes without the starter alias (e.g. you maintain your own dependency aggregation), depend on `fastjson3-spring-boot-autoconfigure` directly — same content, no aggregator layer.

### Spring Boot 3 — register as a `@Bean` (manual)

If you don't want the starter, register the converter yourself:

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

### Spring MVC `View` — register Fastjson3JsonView

For legacy Spring MVC apps that return `ModelAndView` and resolve view names through `ContentNegotiatingViewResolver` or similar. Modern `@RestController` apps should use `Fastjson3HttpMessageConverter` (above) instead.

```java
import com.alibaba.fastjson3.spring.view.Fastjson3JsonView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import java.util.List;

@Configuration
public class ViewConfig {
    @Bean
    public ContentNegotiatingViewResolver viewResolver() {
        ContentNegotiatingViewResolver r = new ContentNegotiatingViewResolver();
        Fastjson3JsonView view = new Fastjson3JsonView();
        view.setExtractValueFromSingleKeyModel(true); // emit array/object directly when model has one entry
        r.setDefaultViews(List.of(view));
        return r;
    }
}
```

Knobs (mirror fastjson2's `FastJsonJsonView`): `setRenderedAttributes` (whitelist of model keys), `setDisableCaching` (default true; emits `Pragma` / `Cache-Control` / `Expires` headers), `setExtractValueFromSingleKeyModel` (un-wrap the map when there's a single entry), `setUpdateContentLength`.

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

    // Polymorphic cache: serialized JSON carries @type; deserialization
    // round-trips to JSONObject by design — fastjson3 does not do raw
    // @type-driven Class.forName (fj2 CVE-2017-18349 mitigation). Use
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
- 28 reactive unit tests (`Fastjson3JsonCodecTest`): Decoder + Encoder canDecode/canEncode, exclusion of `String` / `byte[]` / `ByteBuffer` / `Resource`, `Mono` / `Flux` round-trip with multi-buffer join semantics, generic `List<T>` via `ResolvableType`, `DecodingException` wrapping, custom `ObjectMapper`, NDJSON streaming (advertise `application/x-ndjson`, per-element `\n` framing on `Flux<T>` + `Mono<T>`, charset-parameter media type still routes streaming, `application/stream+json` not routed to framing, non-streaming JSON unaffected)
- 22 Redis unit tests (`Fastjson3RedisSerializerTest`): typed `Fastjson3RedisSerializer<T>` round-trip POJO + nested generic field, null / empty bytes contract, malformed payload → `SerializationException`, custom `ObjectMapper`, null-arg ctor rejection, `getTargetType()` returns concrete `T`, 16-thread × 200-iter concurrency, >10KB payload; `GenericFastjson3RedisSerializer` writes `@type`, JSONObject reconstruction by design (fj3 does not do raw `@type` class loading), Map / List value round-trip
- 17 view unit tests (`Fastjson3JsonViewTest`): basic JSON body, `renderedAttributes` whitelist, `BindingResult` filter, `extractValueFromSingleKeyModel` un-wrap (map + list), cache header emission and suppression, `updateContentLength`, custom `ObjectMapper`, null-arg ctor rejection, default content type, CJK / UTF-8 round-trip, null-value-in-model serialization, 16-thread × 100-iter concurrent render
- 40 end-to-end MockMvc integration tests in `core3-spring-test` covering POJO / record / `Object` / `JSONObject` / `JSONArray` round-trip, ControllerAdvice error responses, header roundtrip, deep nesting, 16-thread concurrency, etc.

## Migrating from fastjson2 Spring extensions

Users moving from fj2's `Fastjson2Decoder` / `Fastjson2Encoder` (WebFlux) and `FastJsonRedisSerializer` (Redis) swap the configuration handle from `FastJsonConfig` to `ObjectMapper`:

| fastjson2 | fastjson3 |
|---|---|
| `new Fastjson2Decoder()` / `Fastjson2Encoder()` | `new Fastjson3JsonDecoder()` / `Fastjson3JsonEncoder()` |
| `new Fastjson2Decoder(new FastJsonConfig())` | `new Fastjson3JsonDecoder(ObjectMapper.shared())` |
| `FastJsonConfig.setReaderFeatures(...)` | `ObjectMapper.builder().enableRead(...).build()` |
| `FastJsonConfig.setWriterFeatures(...)` | `ObjectMapper.builder().enableWrite(...).build()` |
| `FastJsonConfig.setReaderFilters(...)` / `setWriterFilters(...)` | `ObjectMapper.builder().addReaderModule(...)` / `addPropertyFilter(...)` |
| `FastJsonConfig.setDateFormat("yyyy-MM-dd")` | `ObjectMapper.builder().dateFormat("yyyy-MM-dd").build()` — applies to typed POJO fields, untyped `Map<String, Date>` values, and `writeAny(Object)` callers via a single hook in `JSONGenerator.writeAny` plus the per-type `BuiltinCodecs` writers. Recognizes the fj2 special tokens `"millis"` / `"unixtime"` / `"iso8601"` plus 5 fast-path patterns hand-rolled at the byte level. Field-level `@JSONField(format=...)` always wins over the mapper-level default. |
| `MimeType...` ctor varargs | unchanged — same shape |
| `FastJsonRedisSerializer<>(User.class)` (typed) | `Fastjson3RedisSerializer<>(User.class)` |
| `GenericFastJsonRedisSerializer` (autotype) | `GenericFastjson3RedisSerializer` — read side returns `JSONObject` by design; for typed reconstruction use `Fastjson3RedisSerializer<T>` or `@JSONType(seeAlso = ...)` |
| `new GenericFastJsonRedisSerializer(new String[]{"com.foo."})` (allowlist ctor) | _not ported_ — fj3 never resolves `@type` to `Class.forName`, so allowlists have nothing to gate. Use `Fastjson3RedisSerializer<T>` or `@JSONType(seeAlso = ...)` for typed polymorphism. |
| `serializer.setFastJsonConfig(cfg)` (post-construction) | _not supported_ — `ObjectMapper` is an immutable config carrier. Pass it to the ctor, or rebuild and reassign the bean. |
| `fastJsonConfig.setCharset(...)` | _no equivalent needed_ — fj3 emits UTF-8 unconditionally. (Matches fj2 actual behavior: `FastJsonRedisSerializer.serialize()` calls `JSON.toJSONBytes(...)`, which ignores `FastJsonConfig.charset` on the JSON path.) |
| `FastJsonJSONBRedisSerializer` / `GenericFastJsonJSONBRedisSerializer` | _not yet ported_ — JSONB Redis variants are out of scope for this artifact; track separately. |

Behavioral upgrades vs fj2 (you opt into automatically):

- `canDecode` / `canEncode` exclude `String` / `CharSequence` / `byte[]` / `ByteBuffer` / `Resource` so dedicated codecs handle them
- `decode(Publisher<DataBuffer>, ...)` joins buffers before parse (matches multi-chunk HTTP body shape)
- Implements `HttpMessageDecoder<Object>` / `HttpMessageEncoder<Object>` — server-side hint propagation
- `getStreamingMediaTypes()` advertises `application/x-ndjson` and the encoder frames each element with a trailing `\n` byte. fj2's WebFlux encoder does not advertise NDJSON at all, so clients sending `Accept: application/x-ndjson` to a fj2 endpoint get the non-streaming JSON-array fallback (`[v1,v2,...]`) — well-formed JSON but wrong streaming protocol. fj3 closes that gap. (Spring 5.x's deprecated `application/stream+json` is intentionally NOT advertised; clients should migrate to NDJSON.)
- `Fastjson3RedisSerializer<T>.getTargetType()` returns concrete `T` — Spring `RedisCache.canSerialize(type)` works without manual override (fj2 leaves it at `Object.class`)

## Related artifacts

- `com.alibaba.fastjson3:fastjson3` — core JSON parsing / writing

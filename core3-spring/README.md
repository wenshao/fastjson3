# fastjson3-spring

Spring Web + WebFlux JSON adapters for [fastjson3](https://github.com/wenshao/fastjson3). Single artifact, two entry points:

| Entry point | Class | Use case |
|---|---|---|
| Spring Web (servlet) | `Fastjson3HttpMessageConverter` | `@RestController` + `@RequestBody` / `ResponseEntity` round-trip |
| Spring WebFlux (reactive) | `Fastjson3JsonDecoder` + `Fastjson3JsonEncoder` | `Mono` / `Flux` reactive endpoints |

Drop-in replacement for Jackson's `MappingJackson2HttpMessageConverter` (servlet) and `Jackson2JsonDecoder` / `Jackson2JsonEncoder` (reactive).

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
- 14 reactive unit tests (`Fastjson3JsonCodecTest`): Decoder + Encoder canDecode/canEncode, `Mono` / `Flux` round-trip, generic `List<T>` via `ResolvableType`, `DecodingException` wrapping, custom `ObjectMapper`
- 40 end-to-end MockMvc integration tests in `core3-spring-test` covering POJO / record / `Object` / `JSONObject` / `JSONArray` round-trip, ControllerAdvice error responses, header roundtrip, deep nesting, 16-thread concurrency, etc.

## Related artifacts

- `com.alibaba.fastjson3:fastjson3` — core JSON parsing / writing
- _Planned_: `com.alibaba.fastjson3:fastjson3-spring-redis` — `Fastjson3RedisSerializer` for Spring Data Redis

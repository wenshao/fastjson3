package com.alibaba.fastjson3.spring;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectMapper;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Spring {@link org.springframework.http.converter.HttpMessageConverter}
 * implementation that routes (de)serialization through fastjson3's
 * {@link ObjectMapper}. Drop-in replacement for Jackson's
 * {@code MappingJackson2HttpMessageConverter} when the application wants
 * fastjson3 in front of Spring's web layer.
 *
 * <p><b>Usage</b> (Spring Boot 3 / Spring Web 6):</p>
 * <pre>{@code
 * @Configuration
 * public class WebConfig implements WebMvcConfigurer {
 *     @Bean
 *     public Fastjson3HttpMessageConverter fastjson3HttpMessageConverter() {
 *         return new Fastjson3HttpMessageConverter();
 *     }
 *
 *     @Override
 *     public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
 *         converters.add(0, fastjson3HttpMessageConverter());
 *     }
 * }
 * }</pre>
 *
 * <p>To bypass Spring Boot's Jackson auto-configuration entirely, also
 * exclude {@code JacksonAutoConfiguration.class} on the Boot application.</p>
 *
 * <p><b>Coverage</b>:</p>
 * <ul>
 *   <li>Media types {@code application/json} + {@code application/*+json}.</li>
 *   <li>Generic types ({@code List<User>}, {@code Map<String, V>}, generic
 *       records) via {@link GenericHttpMessageConverter}.</li>
 *   <li>Charset honoring: request bodies in GBK / ISO-8859-1 / etc. decode
 *       through the declared {@code Content-Type;charset=…} parameter
 *       (defaults to UTF-8 per RFC 8259).</li>
 *   <li>Doesn't hijack {@link String} / {@code byte[]} /
 *       {@link Resource} returns — those keep their dedicated converters.</li>
 *   <li>Reader/writer SPI honored via the underlying {@link ObjectMapper}.
 *       Pass a configured {@code ObjectMapper} to the two-arg constructor
 *       to wire {@code addReaderModule} / {@code mapSupplier} / etc.</li>
 * </ul>
 */
public class Fastjson3HttpMessageConverter
        extends AbstractHttpMessageConverter<Object>
        implements GenericHttpMessageConverter<Object> {
    private final ObjectMapper mapper;

    public Fastjson3HttpMessageConverter() {
        this(ObjectMapper.shared());
    }

    public Fastjson3HttpMessageConverter(ObjectMapper mapper) {
        super(StandardCharsets.UTF_8,
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json"));
        this.mapper = mapper;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        // Don't hijack types that are owned by other dedicated converters:
        //   - String / CharSequence  -> StringHttpMessageConverter
        //   - byte[]                 -> ByteArrayHttpMessageConverter
        //   - Resource (file/stream) -> ResourceHttpMessageConverter
        // Without this exclusion, returning a String from a @RestController
        // would be JSON-quoted and a byte[] would parse as JSON instead of
        // streaming through.
        if (clazz == byte[].class) {
            return false;
        }
        if (CharSequence.class.isAssignableFrom(clazz)) {
            return false;
        }
        if (Resource.class.isAssignableFrom(clazz)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        // Mirror the Class-typed path's supports() check so the generic
        // entry doesn't bypass our String / byte[] / Resource exclusions.
        // Without this, a generic-typed @RequestBody declared as
        // List<String> with `application/json` would route to fastjson3
        // instead of StringHttpMessageConverter where Spring put the
        // String element handling.
        Class<?> clazz = (type instanceof Class<?> c) ? c : null;
        if (clazz != null && !supports(clazz)) {
            return false;
        }
        return canRead(mediaType);
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        if (clazz != null && !supports(clazz)) {
            return false;
        }
        return canWrite(mediaType);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        if (type == null) {
            // No declared type — Spring's contract allows null; defer to the
            // Class-only path. Fall back to Object for the runtime decision.
            return readInternal(Object.class, inputMessage);
        }
        Type resolvedType = (contextClass != null)
                ? GenericTypeResolver.resolveType(type, contextClass)
                : type;
        java.nio.charset.Charset charset = resolveCharset(inputMessage);
        try {
            if (charset == StandardCharsets.UTF_8) {
                return mapper.readValue(inputMessage.getBody(), resolvedType);
            }
            // Non-UTF-8 charset declared on the request — fastjson3's reader
            // path is UTF-8 only, so transcode bytes through String to avoid
            // silent mojibake when a client posts e.g. GBK or ISO-8859-1.
            String json = new String(inputMessage.getBody().readAllBytes(), charset);
            return mapper.readValue(json, resolvedType);
        } catch (JSONException e) {
            throw new HttpMessageNotReadableException("JSON parse error: " + e.getMessage(), e, inputMessage);
        }
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        java.nio.charset.Charset charset = resolveCharset(inputMessage);
        try {
            if (charset == StandardCharsets.UTF_8) {
                return mapper.readValue(inputMessage.getBody(), clazz);
            }
            String json = new String(inputMessage.getBody().readAllBytes(), charset);
            return mapper.readValue(json, clazz);
        } catch (JSONException e) {
            throw new HttpMessageNotReadableException("JSON parse error: " + e.getMessage(), e, inputMessage);
        }
    }

    /**
     * Honor the request's {@code Content-Type: ...; charset=...} parameter
     * so a body in GBK / ISO-8859-1 / etc. is decoded with the declared
     * charset rather than assumed UTF-8. fastjson3's byte-oriented reader
     * is UTF-8 only, so non-UTF-8 charsets transcode through {@code String}.
     * Defaults to UTF-8 when no charset parameter is present (RFC 8259).
     *
     * <p><b>Note</b>: HTTP semantics trust the header, so a body whose
     * actual encoding doesn't match the declared {@code charset} parameter
     * decodes successfully but to wrong characters (e.g. UTF-8 bytes
     * decoded as GBK). This matches Jackson's behavior when an
     * {@code InputStreamReader} is opened with the declared charset.
     * Spring rejects unparseable / unknown charset names upstream
     * (415 Unsupported Media Type) before our converter sees them.</p>
     */
    private static java.nio.charset.Charset resolveCharset(HttpInputMessage inputMessage) {
        MediaType contentType = inputMessage.getHeaders().getContentType();
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        }
        return StandardCharsets.UTF_8;
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        try {
            byte[] bytes = mapper.writeValueAsBytes(o);
            outputMessage.getBody().write(bytes);
            outputMessage.getBody().flush();
        } catch (JSONException e) {
            throw new HttpMessageNotWritableException("JSON write error: " + e.getMessage(), e);
        }
    }

    @Override
    public void write(Object o, Type type, MediaType contentType, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        writeInternal(o, outputMessage);
    }
}

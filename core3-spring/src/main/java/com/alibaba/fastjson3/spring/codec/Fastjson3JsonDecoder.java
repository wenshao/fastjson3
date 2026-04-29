package com.alibaba.fastjson3.spring.codec;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageDecoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

/**
 * Spring WebFlux {@link org.springframework.core.codec.Decoder} that routes
 * deserialization through fastjson3's {@link ObjectMapper}. Drop-in
 * replacement for the default Jackson-backed
 * {@code Jackson2JsonDecoder} when the application wants fastjson3 in
 * front of the reactive web layer.
 *
 * <p><b>Usage</b> (Spring WebFlux 6.x):</p>
 * <pre>{@code
 * @Configuration
 * public class WebFluxConfig implements WebFluxConfigurer {
 *     @Override
 *     public void configureHttpMessageCodecs(ServerCodecConfigurer cfg) {
 *         cfg.defaultCodecs().jackson2JsonDecoder(new Fastjson3JsonDecoder());
 *         cfg.defaultCodecs().jackson2JsonEncoder(new Fastjson3JsonEncoder());
 *     }
 * }
 * }</pre>
 *
 * <p>Buffers the entire request payload in memory before parsing — same
 * as Jackson's default and consistent with fastjson3's UTF-8 byte reader,
 * which can't accept arbitrary {@link DataBuffer} chunks lazily.</p>
 *
 * @see Fastjson3JsonEncoder
 */
public final class Fastjson3JsonDecoder
        extends AbstractDecoder<Object>
        implements HttpMessageDecoder<Object> {
    private static final int BUFFER_SIZE = 65536;

    private final ObjectMapper mapper;

    public Fastjson3JsonDecoder() {
        this(ObjectMapper.shared(),
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json"));
    }

    public Fastjson3JsonDecoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mimeTypes == null || mimeTypes.length == 0
                ? new MimeType[]{MediaType.APPLICATION_JSON,
                        new MediaType("application", "*+json")}
                : mimeTypes);
        this.mapper = mapper;
    }

    @Override
    public boolean canDecode(@NonNull final ResolvableType elementType,
                             @Nullable final MimeType mimeType) {
        // Don't hijack types owned by dedicated codecs:
        //   - String / CharSequence  -> StringDecoder
        //   - byte[] / ByteBuffer    -> ByteArrayDecoder / ByteBufferDecoder
        //   - Resource               -> ResourceDecoder
        // Without this filter a `Mono<String>` @RequestBody at content-type
        // application/json would arrive as a JSON-quoted String parsed by us
        // instead of the raw String the user expected.
        Class<?> clazz = elementType.resolve();
        if (clazz != null && isExcludedType(clazz)) {
            return false;
        }
        return super.canDecode(elementType, mimeType);
    }

    @Override
    @NonNull
    public Flux<Object> decode(@NonNull final Publisher<DataBuffer> inputStream,
                               @NonNull final ResolvableType elementType,
                               @Nullable final MimeType mimeType,
                               @Nullable final Map<String, Object> hints) {
        // Defer to single-value Mono semantics. The Flux entry point on
        // AbstractDecoder is contract-required but for application/json
        // 1 buffer ≠ 1 value (large bodies arrive in multiple chunks);
        // join the publisher and decode once, surfacing a single-element
        // Flux. Stream-of-objects use cases (NDJSON / line-delimited)
        // need a tokenizer — tracked as follow-up.
        return decodeToMono(inputStream, elementType, mimeType, hints).flux();
    }

    @Override
    @Nullable
    public Object decode(@NonNull final DataBuffer buffer,
                         @NonNull final ResolvableType targetType,
                         @Nullable final MimeType mimeType,
                         @Nullable final Map<String, Object> hints)
            throws DecodingException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                InputStream in = buffer.asInputStream()) {
            byte[] buf = new byte[BUFFER_SIZE];
            for (;;) {
                int len = in.read(buf);
                if (len == -1) {
                    break;
                }
                if (len > 0) {
                    os.write(buf, 0, len);
                }
            }
            Object value = mapper.readValue(os.toByteArray(), targetType.getType());
            logValue(value, hints);
            return value;
        } catch (JSONException ex) {
            throw new DecodingException("JSON parse error: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new DecodingException("I/O error while reading input message", ex);
        } finally {
            DataBufferUtils.release(buffer);
        }
    }

    @Override
    @NonNull
    public Mono<Object> decodeToMono(
            @NonNull final Publisher<DataBuffer> inputStream,
            @NonNull final ResolvableType elementType,
            @Nullable final MimeType mimeType,
            @Nullable final Map<String, Object> hints) {
        return DataBufferUtils.join(inputStream)
                .flatMap(buffer -> Mono.justOrEmpty(decode(buffer, elementType, mimeType, hints)));
    }

    @Override
    @NonNull
    public Map<String, Object> getDecodeHints(@NonNull final ResolvableType actualType,
                                              @NonNull final ResolvableType elementType,
                                              @NonNull final ServerHttpRequest request,
                                              @NonNull final ServerHttpResponse response) {
        // No HTTP-aware hints today; Jackson uses this to surface
        // controller method-level annotations to the codec. Returning an
        // empty map keeps the Spring contract satisfied without coupling
        // fastjson3 to Spring's annotation API.
        return Collections.emptyMap();
    }

    private static boolean isExcludedType(final Class<?> clazz) {
        if (clazz == byte[].class || clazz == ByteBuffer.class) {
            return true;
        }
        if (CharSequence.class.isAssignableFrom(clazz)) {
            return true;
        }
        return Resource.class.isAssignableFrom(clazz);
    }

    private void logValue(@Nullable final Object value,
                          @Nullable final Map<String, Object> hints) {
        if (!Hints.isLoggingSuppressed(hints)) {
            LogFormatUtils.traceDebug(logger, traceOn -> {
                String formatted = LogFormatUtils.formatValue(value, !traceOn);
                return Hints.getLogPrefix(hints) + "Decoded [" + formatted + "]";
            });
        }
    }
}

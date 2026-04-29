package com.alibaba.fastjson3.spring.codec;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public final class Fastjson3JsonDecoder extends AbstractDecoder<Object> {
    private static final int BUFFER_SIZE = 65536;

    private final ObjectMapper mapper;

    public Fastjson3JsonDecoder() {
        this(ObjectMapper.shared(),
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json"),
                new MediaType("application", "x-ndjson"));
    }

    public Fastjson3JsonDecoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mimeTypes == null || mimeTypes.length == 0
                ? new MimeType[]{MediaType.APPLICATION_JSON,
                        new MediaType("application", "*+json")}
                : mimeTypes);
        this.mapper = mapper;
    }

    @Override
    @NonNull
    public Flux<Object> decode(@NonNull final Publisher<DataBuffer> inputStream,
                               @NonNull final ResolvableType elementType,
                               @Nullable final MimeType mimeType,
                               @Nullable final Map<String, Object> hints) {
        return Flux.from(inputStream)
                .mapNotNull(buffer -> decode(buffer, elementType, mimeType, hints));
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

package com.alibaba.fastjson3.spring.codec;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Spring WebFlux {@link org.springframework.core.codec.Encoder} that routes
 * serialization through fastjson3's {@link ObjectMapper}. Pairs with
 * {@link Fastjson3JsonDecoder} as the encoder side of the WebFlux JSON
 * codec replacement.
 *
 * <p><b>Usage</b>: see {@link Fastjson3JsonDecoder}.</p>
 *
 * <p>Each emitted value materializes to a {@code byte[]} via
 * {@code mapper.writeValueAsBytes(value)} and copies into a single
 * {@link DataBuffer}. For very large responses {@code mapper
 * .writeValue(OutputStream, Object)} would be preferable, but fastjson3's
 * current writer surface is byte-array oriented; lazy streaming output is
 * a follow-up.</p>
 *
 * @see Fastjson3JsonDecoder
 */
public final class Fastjson3JsonEncoder
        extends AbstractEncoder<Object>
        implements HttpMessageEncoder<Object> {
    private final ObjectMapper mapper;

    public Fastjson3JsonEncoder() {
        this(ObjectMapper.shared(),
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json"));
    }

    public Fastjson3JsonEncoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mimeTypes == null || mimeTypes.length == 0
                ? new MimeType[]{MediaType.APPLICATION_JSON,
                        new MediaType("application", "*+json")}
                : mimeTypes);
        this.mapper = mapper;
    }

    @Override
    public boolean canEncode(@NonNull final ResolvableType elementType,
                             @Nullable final MimeType mimeType) {
        // See decoder canDecode comment — same exclusions apply on the
        // write side so a controller returning a String / byte[] / Resource
        // routes to the dedicated encoder, not us.
        Class<?> clazz = elementType.resolve();
        if (clazz != null && isExcludedType(clazz)) {
            return false;
        }
        return super.canEncode(elementType, mimeType);
    }

    @Override
    @NonNull
    public Flux<DataBuffer> encode(@NonNull final Publisher<?> inputStream,
                                   @NonNull final DataBufferFactory bufferFactory,
                                   @NonNull final ResolvableType elementType,
                                   @Nullable final MimeType mimeType,
                                   @Nullable final Map<String, Object> hints) {
        return Flux.from(inputStream)
                .map(value -> encodeValue(value, bufferFactory, elementType, mimeType, hints));
    }

    @Override
    @NonNull
    public DataBuffer encodeValue(@Nullable final Object value,
                                  @NonNull final DataBufferFactory bufferFactory,
                                  @NonNull final ResolvableType valueType,
                                  @Nullable final MimeType mimeType,
                                  @Nullable final Map<String, Object> hints) {
        try {
            byte[] bytes = mapper.writeValueAsBytes(value);
            DataBuffer buffer = bufferFactory.allocateBuffer(bytes.length)
                    .write(bytes, 0, bytes.length);
            Hints.touchDataBuffer(buffer, hints, logger);
            return buffer;
        } catch (JSONException ex) {
            throw new EncodingException("JSON write error: " + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            // Buffer allocation OOM, custom mapper failures, etc. — wrap so
            // the reactive pipeline surfaces a deterministic Spring exception.
            throw new EncodingException(
                    "Failed to encode JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    @NonNull
    public List<MediaType> getStreamingMediaTypes() {
        // Stream-of-objects (NDJSON / application/stream+json) needs
        // per-element line separator emission. fastjson3's encoder
        // currently emits one JSON value per buffer with no framing,
        // so declaring streaming media types would route ill-formed
        // bytes to the wire. Return empty until tokenizer-style
        // framing lands as a follow-up.
        return Collections.emptyList();
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
}

package com.alibaba.fastjson3.solon;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectMapper;
import org.noear.solon.core.convert.Converter;
import org.noear.solon.core.handle.Context;
import org.noear.solon.serialization.EntityStringSerializer;
import org.noear.solon.serialization.SerializerNames;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Solon {@link EntityStringSerializer} backed by fastjson3.
 *
 * <p>Serializes response bodies as JSON via {@link ObjectMapper#writeValueAsString}
 * and deserializes request bodies via {@link ObjectMapper#readValue}.
 *
 * <p>Triggered when the request/response is marked as JSON (matching the
 * standard {@code application/json} content type or carrying the Solon
 * {@link SerializerNames#AT_JSON} annotation).
 *
 * <p>Custom encoders for specific types can be registered via
 * {@link #addEncoder(Class, Converter)} — the serializer applies the converter
 * before delegating to fastjson3.
 */
public class Fastjson3StringSerializer implements EntityStringSerializer {
    private static final String MIME_TYPE = "application/json";

    private final ObjectMapper mapper;
    // ConcurrentHashMap because addEncoder is a public API; it could be
    // called after request threads start serving, even if the canonical
    // pattern is to register during plugin start.
    private final Map<Class<?>, Converter<?, Object>> encoders = new ConcurrentHashMap<>();

    public Fastjson3StringSerializer() {
        this(ObjectMapper.shared());
    }

    public Fastjson3StringSerializer(ObjectMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    @Override
    public String name() {
        return SerializerNames.AT_JSON;
    }

    @Override
    public String mimeType() {
        return MIME_TYPE;
    }

    @Override
    public Class<String> dataType() {
        return String.class;
    }

    @Override
    public boolean matched(Context ctx, String mime) {
        if (mime == null) {
            return false;
        }
        // Match application/json (with optional ; charset=...), the +json
        // suffix anchored at end-of-subtype (or before parameters), and
        // Solon's @json marker constant. Avoid bare `contains("+json")`
        // which would match `text/x-anything+json-ish` — anchor the match.
        return mime.contains(MIME_TYPE)
                || mime.endsWith("+json")
                || mime.contains("+json;")
                || mime.contains("+json ")
                || SerializerNames.AT_JSON.equalsIgnoreCase(mime);
    }

    @Override
    public String serialize(Object source) throws IOException {
        try {
            Object value = applyEncoder(source);
            return mapper.writeValueAsString(value);
        } catch (JSONException ex) {
            throw new IOException("JSON serialize error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Object deserialize(String data, Type type) throws IOException {
        // isBlank covers null + empty + whitespace-only bodies (e.g. Solon
        // routing a request with `\n` body should not surface as a parse
        // error — return null and let the caller decide).
        if (data == null || data.isBlank()) {
            return null;
        }
        try {
            // Prefer the Class<T> overload for the UTF-8 fast path.
            if (type instanceof Class<?>) {
                return mapper.readValue(data, (Class<?>) type);
            }
            return mapper.readValue(data, type);
        } catch (JSONException ex) {
            throw new IOException("JSON deserialize error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void serializeToBody(Context ctx, Object data) throws IOException {
        ctx.contentType(MIME_TYPE);
        ctx.output(serialize(data));
    }

    @Override
    public Object deserializeFromBody(Context ctx, Type type) throws IOException {
        String body = ctx.body();
        return deserialize(body, type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <T> void addEncoder(Class<T> clazz, Converter<T, Object> converter) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz must not be null");
        }
        if (converter == null) {
            throw new IllegalArgumentException("converter must not be null");
        }
        encoders.put(clazz, (Converter) converter);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object applyEncoder(Object source) {
        if (source == null) {
            return null;
        }
        Converter encoder = encoders.get(source.getClass());
        if (encoder != null) {
            return encoder.convert(source);
        }
        return source;
    }
}

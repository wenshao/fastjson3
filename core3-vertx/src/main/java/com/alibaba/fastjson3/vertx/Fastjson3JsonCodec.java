package com.alibaba.fastjson3.vertx;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectMapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Vert.x {@link JsonCodec} backed by fastjson3. Wired in via
 * {@link Fastjson3JsonFactory} through Vert.x's
 * {@code io.vertx.core.spi.JsonFactory} ServiceLoader contract.
 *
 * <p>When this artifact is on the classpath and the {@code JsonFactory}
 * service file is present in {@code META-INF/services}, Vert.x will pick
 * fastjson3 as the JSON engine for {@code Json.encode}, {@code Json.decode},
 * {@code JsonObject}, {@code JsonArray}, and HTTP body conversions
 * (when {@code application/json}).
 *
 * <p>Backed by {@link ObjectMapper#shared()}.
 */
public class Fastjson3JsonCodec implements JsonCodec {
    private final ObjectMapper mapper;

    public Fastjson3JsonCodec() {
        this(ObjectMapper.shared());
    }

    public Fastjson3JsonCodec(ObjectMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
    }

    @Override
    public <T> T fromString(String json, Class<T> clazz) throws DecodeException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (JSONException ex) {
            throw new DecodeException("Failed to decode JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    public <T> T fromBuffer(Buffer json, Class<T> clazz) throws DecodeException {
        if (json == null || json.length() == 0) {
            return null;
        }
        try {
            return mapper.readValue(json.getBytes(), clazz);
        } catch (JSONException ex) {
            throw new DecodeException("Failed to decode JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromValue(Object json, Class<T> toValueType) {
        if (json == null) {
            return null;
        }
        if (toValueType.isInstance(json)) {
            return (T) json;
        }
        // Fall back to round-trip through string for shape conversions
        // (e.g. Map -> POJO, List -> POJO collection).
        try {
            return mapper.readValue(mapper.writeValueAsString(json), toValueType);
        } catch (JSONException ex) {
            throw new DecodeException("Failed to convert value: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String toString(Object object, boolean pretty) throws EncodeException {
        try {
            if (pretty) {
                return mapper.writer()
                        .with(com.alibaba.fastjson3.WriteFeature.PrettyFormat)
                        .writeValueAsString(object);
            }
            return mapper.writeValueAsString(object);
        } catch (JSONException ex) {
            throw new EncodeException("Failed to encode JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Buffer toBuffer(Object object, boolean pretty) throws EncodeException {
        try {
            byte[] bytes;
            if (pretty) {
                bytes = mapper.writer()
                        .with(com.alibaba.fastjson3.WriteFeature.PrettyFormat)
                        .writeValueAsBytes(object);
            } else {
                bytes = mapper.writeValueAsBytes(object);
            }
            return Buffer.buffer(bytes);
        } catch (JSONException ex) {
            throw new EncodeException("Failed to encode JSON: " + ex.getMessage(), ex);
        }
    }
}

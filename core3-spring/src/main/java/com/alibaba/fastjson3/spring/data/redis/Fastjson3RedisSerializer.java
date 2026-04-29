package com.alibaba.fastjson3.spring.data.redis;

import com.alibaba.fastjson3.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Spring Data Redis {@link RedisSerializer} that routes serialization through
 * fastjson3's {@link ObjectMapper}. Drop-in replacement for fastjson2's
 * {@code FastJsonRedisSerializer} or Jackson's
 * {@code Jackson2JsonRedisSerializer} when the application wants fastjson3
 * for cache values stored as JSON.
 *
 * <p><b>Usage</b> (Spring Data Redis 3.x):</p>
 * <pre>{@code
 * @Bean
 * public RedisTemplate<String, User> userRedisTemplate(
 *         RedisConnectionFactory connectionFactory) {
 *     RedisTemplate<String, User> template = new RedisTemplate<>();
 *     template.setConnectionFactory(connectionFactory);
 *     template.setKeySerializer(new StringRedisSerializer());
 *     template.setValueSerializer(new Fastjson3RedisSerializer<>(User.class));
 *     return template;
 * }
 * }</pre>
 *
 * <p>Pass a configured {@link ObjectMapper} to the two-arg constructor for
 * per-instance settings (modules, mapSupplier, listSupplier, features).
 * Each value is fully typed by the {@code Class<T>} declaration; for
 * polymorphic / unknown-type cache values use
 * {@link GenericFastjson3RedisSerializer} instead.</p>
 *
 * @param <T> the target value type
 */
public class Fastjson3RedisSerializer<T> implements RedisSerializer<T> {
    private final Class<T> type;
    private final ObjectMapper mapper;

    public Fastjson3RedisSerializer(Class<T> type) {
        this(type, ObjectMapper.shared());
    }

    public Fastjson3RedisSerializer(Class<T> type, ObjectMapper mapper) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.type = type;
        this.mapper = mapper;
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        try {
            return mapper.writeValueAsBytes(value);
        } catch (RuntimeException ex) {
            // Catch broader than JSONException — adversarial input can
            // surface as NPE / ClassCast / IAE inside the writer; users
            // expect a SerializationException either way.
            throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(bytes, type);
        } catch (RuntimeException ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Class<?> getTargetType() {
        // Used by Spring's RedisCache.canSerialize() and similar
        // type-gated callers. Default returns Object.class, which would
        // make canSerialize(User.class) report false on a typed
        // User-targeted serializer. Return the concrete bound type.
        return type;
    }
}

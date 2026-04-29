package com.alibaba.fastjson3.spring.data.redis;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ReadFeature;
import com.alibaba.fastjson3.WriteFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Spring Data Redis {@link RedisSerializer} that handles arbitrary
 * {@link Object} values by writing the runtime type as an
 * {@code @type} discriminator (write side) and deserializing it back
 * through fastjson3's auto-type machinery (read side). Use when the
 * cache value's static type is {@code Object} or polymorphic.
 *
 * <p>By default uses an {@link ObjectMapper} configured with
 * {@link WriteFeature#WriteClassName} +
 * {@link ReadFeature#SupportAutoType}.</p>
 *
 * <p><b>Security note</b>: enabling {@code SupportAutoType} on attacker-
 * controlled JSON allows instantiation of arbitrary registered classes
 * (gadget chain risk). Only use this serializer for cache values your
 * application produces; never for cache values written by external
 * systems unless you also configure an autotype allow-list.</p>
 *
 * <p><b>Usage</b>:</p>
 * <pre>{@code
 * @Bean
 * public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
 *     RedisTemplate<String, Object> template = new RedisTemplate<>();
 *     template.setConnectionFactory(cf);
 *     template.setKeySerializer(new StringRedisSerializer());
 *     template.setValueSerializer(new GenericFastjson3RedisSerializer());
 *     return template;
 * }
 * }</pre>
 */
public class GenericFastjson3RedisSerializer implements RedisSerializer<Object> {
    private final ObjectMapper mapper;

    /**
     * Default constructor — wires {@link WriteFeature#WriteClassName} +
     * {@link ReadFeature#SupportAutoType} on a fresh ObjectMapper.
     *
     * <p><b>Current behavior</b>: serialized JSON carries an {@code @type}
     * discriminator, but fastjson3's {@code ObjectMapper.Builder} does not
     * yet expose an {@code AutoTypeFilter} hook (the
     * {@link com.alibaba.fastjson3.filter.AutoTypeFilter} class is declared
     * but not wired into the parser path). Result: deserialization of an
     * unknown root type falls back to
     * {@link com.alibaba.fastjson3.JSONObject} or
     * {@link com.alibaba.fastjson3.JSONArray} rather than the original POJO.
     * For strongly-typed cache values prefer
     * {@link Fastjson3RedisSerializer} with the concrete {@code Class<T>}.
     * Tracked as a fj3 core follow-up; once the builder hook lands this
     * serializer will gain typed-reconstruction support transparently.</p>
     */
    public GenericFastjson3RedisSerializer() {
        this(ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteClassName)
                .enableRead(ReadFeature.SupportAutoType)
                .build());
    }

    /**
     * Caller-provided mapper. The caller is responsible for enabling
     * {@code WriteClassName} on the writer side and {@code SupportAutoType}
     * on the reader side; otherwise polymorphic deserialization will fail.
     */
    public GenericFastjson3RedisSerializer(ObjectMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
    }

    @Override
    public byte[] serialize(Object value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        try {
            return mapper.writeValueAsBytes(value);
        } catch (RuntimeException ex) {
            throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(bytes, Object.class);
        } catch (RuntimeException ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}

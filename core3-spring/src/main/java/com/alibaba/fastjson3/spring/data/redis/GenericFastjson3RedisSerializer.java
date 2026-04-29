package com.alibaba.fastjson3.spring.data.redis;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ReadFeature;
import com.alibaba.fastjson3.WriteFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Spring Data Redis {@link RedisSerializer} for arbitrary
 * {@link Object} values. Writes the runtime type as an {@code @type}
 * discriminator on the write side. Use when the cache value's static
 * type is {@code Object} or polymorphic.
 *
 * <p>By default uses an {@link ObjectMapper} configured with
 * {@link WriteFeature#WriteClassName}.</p>
 *
 * <p><b>Read-side behavior — by design</b>: fastjson3 does not perform
 * {@code @type}-driven {@link Class#forName(String)} loading. This is a
 * deliberate departure from fastjson2's {@code SupportAutoType} mode,
 * which was the source of CVE-2017-18349 and subsequent gadget-chain
 * CVEs. Result: deserialization of an unknown root type yields
 * {@link com.alibaba.fastjson3.JSONObject} or
 * {@link com.alibaba.fastjson3.JSONArray} rather than the original POJO.
 * The {@code @type} field is preserved in the JSON payload but is not
 * used for instantiation. fastjson3's
 * {@link ReadFeature#SupportAutoType} flag is a deprecated no-op.</p>
 *
 * <p>For typed reconstruction prefer {@link Fastjson3RedisSerializer}
 * with a concrete {@code Class<T>}, or model the hierarchy with
 * {@code @JSONType(seeAlso = ...)} on a sealed type.</p>
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
     * Default constructor — wires {@link WriteFeature#WriteClassName} on
     * a fresh {@link ObjectMapper}. Read side uses default behavior. See
     * the class Javadoc for the design rationale.
     */
    public GenericFastjson3RedisSerializer() {
        this(ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteClassName)
                .build());
    }

    /**
     * Caller-provided mapper. The caller is responsible for enabling
     * {@link WriteFeature#WriteClassName} on the writer side. Note that
     * {@link ReadFeature#SupportAutoType} is a deprecated no-op in
     * fastjson3 and has no effect on read-side reconstruction.
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

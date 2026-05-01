package com.alibaba.fastjson3.kafka;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * Kafka {@link Deserializer} that decodes record values from JSON via fastjson3.
 *
 * <p>The target type can be supplied two ways:
 * <ol>
 *   <li>Constructor injection — preferred when the consumer is wired
 *       programmatically:
 *       <pre>{@code
 *         new Fastjson3KafkaDeserializer<>(MyEvent.class)
 *       }</pre>
 *   </li>
 *   <li>{@code configure()}-based — use the no-arg constructor and supply the
 *       fully-qualified class name via Kafka consumer properties:
 *       <pre>{@code
 *         props.put(Fastjson3KafkaDeserializer.VALUE_DEFAULT_TYPE,
 *                   "com.example.MyEvent");
 *         props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
 *                   Fastjson3KafkaDeserializer.class.getName());
 *       }</pre>
 *       Or {@link #KEY_DEFAULT_TYPE} for keys.
 *   </li>
 * </ol>
 *
 * <p>Malformed JSON or class-loading failures are wrapped in
 * {@link SerializationException} per Kafka conventions.
 *
 * @param <T> the value type to deserialize
 */
public class Fastjson3KafkaDeserializer<T> implements Deserializer<T> {
    /** Config key: fully-qualified class name to deserialize record values into. */
    public static final String VALUE_DEFAULT_TYPE = "fastjson3.kafka.value.default.type";
    /** Config key: fully-qualified class name to deserialize record keys into. */
    public static final String KEY_DEFAULT_TYPE = "fastjson3.kafka.key.default.type";

    private final ObjectMapper mapper;
    private Class<T> targetType;

    public Fastjson3KafkaDeserializer() {
        this(ObjectMapper.shared(), null);
    }

    public Fastjson3KafkaDeserializer(Class<T> targetType) {
        this(ObjectMapper.shared(), targetType);
    }

    public Fastjson3KafkaDeserializer(ObjectMapper mapper, Class<T> targetType) {
        this.mapper = mapper == null ? ObjectMapper.shared() : mapper;
        this.targetType = targetType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (targetType != null) {
            return;
        }
        Object cfg = configs.get(isKey ? KEY_DEFAULT_TYPE : VALUE_DEFAULT_TYPE);
        if (cfg == null) {
            return;
        }
        String className = cfg instanceof Class ? ((Class<?>) cfg).getName() : cfg.toString();
        try {
            targetType = (Class<T>) Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new SerializationException(
                    "Cannot resolve target type for fastjson3 deserializer: " + className, e);
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        if (targetType == null) {
            throw new SerializationException(
                    "Target type not configured for Fastjson3KafkaDeserializer; "
                            + "pass Class<T> to the constructor or set "
                            + VALUE_DEFAULT_TYPE + " / " + KEY_DEFAULT_TYPE);
        }
        try {
            return mapper.readValue(data, targetType);
        } catch (JSONException e) {
            throw new SerializationException("Failed to deserialize JSON for topic " + topic, e);
        }
    }
}

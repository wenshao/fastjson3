package com.alibaba.fastjson3.kafka;

import com.alibaba.fastjson3.Fastjson3MapperHolder;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Kafka {@link Deserializer} that decodes record values from JSON via fastjson3.
 *
 * <p>The target type can be supplied three ways:
 * <ol>
 *   <li>Constructor with {@code Class<T>} — non-generic types:
 *       <pre>{@code
 *         new Fastjson3KafkaDeserializer<>(MyEvent.class)
 *       }</pre>
 *   </li>
 *   <li>Constructor with {@code TypeReference<T>} — generic / parameterized
 *       types (batch payloads, generic envelopes):
 *       <pre>{@code
 *         new Fastjson3KafkaDeserializer<>(new TypeReference<List<MyEvent>>() {})
 *       }</pre>
 *   </li>
 *   <li>{@code configure()}-based — use the no-arg constructor and supply the
 *       fully-qualified class name via Kafka consumer properties (Class only;
 *       parameterized types not expressible as a string config value):
 *       <pre>{@code
 *         props.put(Fastjson3KafkaDeserializer.VALUE_DEFAULT_TYPE,
 *                   "com.example.MyEvent");
 *         props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
 *                   Fastjson3KafkaDeserializer.class.getName());
 *       }</pre>
 *       Or {@link #KEY_DEFAULT_TYPE} for keys. <b>The class-name config must
 *       come from trusted application code, not network input</b> — it is
 *       resolved via {@code Class.forName}, so untrusted values can target
 *       any class on the classpath. Constructor injection is preferred.
 *   </li>
 * </ol>
 *
 * <p>Constructor-supplied target type wins over {@code configure()}-supplied
 * value when both are set.
 *
 * <p><b>Custom mapper note</b>: when Kafka instantiates the deserializer via
 * the {@code VALUE_DESERIALIZER_CLASS_CONFIG} property, Kafka uses the no-arg
 * constructor and {@link ObjectMapper#shared()} is used. The Spring-managed
 * {@code fastjson3ObjectMapper} bean does not propagate to deserializers
 * instantiated outside the Spring container. To inject a configured mapper,
 * instantiate the deserializer manually and pass it to the consumer factory:
 * <pre>{@code
 *   var de = new Fastjson3KafkaDeserializer<>(MyEvent.class, myCustomMapper);
 *   new DefaultKafkaConsumerFactory<>(props, keyDeserializer, de);
 * }</pre>
 *
 * <p>Malformed JSON or class-loading failures are wrapped in
 * {@link SerializationException} per Kafka conventions.
 *
 * <p><b>Thread safety</b>: {@code configure()} must be called once before any
 * {@code deserialize()} call. The {@code targetType} field is {@code volatile}
 * to provide the happens-before edge for shared-instance topologies (Spring
 * Kafka, Kafka Streams).
 *
 * @param <T> the value type to deserialize
 */
public class Fastjson3KafkaDeserializer<T> implements Deserializer<T> {
    /** Config key: fully-qualified class name to deserialize record values into. */
    public static final String VALUE_DEFAULT_TYPE = "fastjson3.kafka.value.default.type";
    /** Config key: fully-qualified class name to deserialize record keys into. */
    public static final String KEY_DEFAULT_TYPE = "fastjson3.kafka.key.default.type";

    private final ObjectMapper mapper;
    private volatile Type targetType;

    public Fastjson3KafkaDeserializer() {
        this((Type) null, Fastjson3MapperHolder.get());
    }

    public Fastjson3KafkaDeserializer(Class<T> targetType) {
        this((Type) targetType, Fastjson3MapperHolder.get());
    }

    public Fastjson3KafkaDeserializer(TypeReference<T> targetType) {
        this(unwrap(targetType), Fastjson3MapperHolder.get());
    }

    public Fastjson3KafkaDeserializer(Class<T> targetType, ObjectMapper mapper) {
        this((Type) targetType, mapper);
    }

    public Fastjson3KafkaDeserializer(TypeReference<T> targetType, ObjectMapper mapper) {
        this(unwrap(targetType), mapper);
    }

    private Fastjson3KafkaDeserializer(Type targetType, ObjectMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
        this.targetType = targetType;
    }

    private static Type unwrap(TypeReference<?> ref) {
        if (ref == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        return ref.getType();
    }

    @Override
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
            // initialize=false: defer static-initializer side effects; the class
            // is used only as a type token for fastjson3 deserialization.
            targetType = Class.forName(
                    className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new SerializationException(
                    "Cannot resolve target type for fastjson3 deserializer: " + className, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        Type t = targetType;
        if (t == null) {
            throw new SerializationException(
                    "Target type not configured for Fastjson3KafkaDeserializer; "
                            + "pass Class<T>/TypeReference<T> to the constructor or set "
                            + VALUE_DEFAULT_TYPE + " / " + KEY_DEFAULT_TYPE);
        }
        try {
            // Prefer the Class<T> overload when targetType is a non-generic
            // class — fj3's readValue(byte[], Class<T>) reaches the UTF-8
            // fast path (readObjectUTF8) that the Type overload skips.
            if (t instanceof Class<?>) {
                return (T) mapper.readValue(data, (Class<?>) t);
            }
            return mapper.readValue(data, t);
        } catch (RuntimeException e) {
            throw new SerializationException("Failed to deserialize JSON for topic " + topic, e);
        }
    }
}

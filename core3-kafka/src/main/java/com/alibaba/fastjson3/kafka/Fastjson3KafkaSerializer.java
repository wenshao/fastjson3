package com.alibaba.fastjson3.kafka;

import com.alibaba.fastjson3.Fastjson3MapperHolder;
import com.alibaba.fastjson3.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Kafka {@link Serializer} that encodes record values as JSON via fastjson3.
 *
 * <p>Produces UTF-8 JSON bytes through {@link ObjectMapper#writeValueAsBytes(Object)}.
 * Pair with {@link Fastjson3KafkaDeserializer} on the consumer side.
 *
 * <p>Wiring example:
 * <pre>{@code
 *   props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
 *             Fastjson3KafkaSerializer.class.getName());
 * }</pre>
 *
 * <p>Defaults to {@link ObjectMapper#shared()}; pass a configured mapper through
 * the constructor to customize features.
 *
 * @param <T> the value type to serialize
 */
public class Fastjson3KafkaSerializer<T> implements Serializer<T> {
    private final ObjectMapper mapper;

    public Fastjson3KafkaSerializer() {
        this(Fastjson3MapperHolder.get());
    }

    public Fastjson3KafkaSerializer(ObjectMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }
        return mapper.writeValueAsBytes(data);
    }
}

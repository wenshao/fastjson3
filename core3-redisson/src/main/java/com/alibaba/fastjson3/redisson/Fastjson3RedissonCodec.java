package com.alibaba.fastjson3.redisson;

import com.alibaba.fastjson3.Fastjson3MapperHolder;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Redisson {@link org.redisson.client.codec.Codec} backed by fastjson3.
 * Encodes Redis values as UTF-8 JSON, decodes back into the configured target
 * type. Pair with Redisson's {@code RBucket}, {@code RMap}, {@code RList}, etc.
 *
 * <p>Wiring:
 * <pre>{@code
 *   Config config = new Config();
 *   config.useSingleServer().setAddress("redis://localhost:6379");
 *   config.setCodec(new Fastjson3RedissonCodec(MyEvent.class));
 *   RedissonClient redisson = Redisson.create(config);
 * }</pre>
 *
 * <p>Or per-bucket:
 * <pre>{@code
 *   RBucket<MyEvent> bucket = redisson.getBucket("key", new Fastjson3RedissonCodec<>(MyEvent.class));
 * }</pre>
 *
 * <p>Constructor variants:
 * <ul>
 *   <li>{@link #Fastjson3RedissonCodec(Class)} — non-generic types</li>
 *   <li>{@link #Fastjson3RedissonCodec(TypeReference)} — generic types</li>
 *   <li>{@link #Fastjson3RedissonCodec(Class, ObjectMapper)} — custom mapper</li>
 *   <li>{@link #Fastjson3RedissonCodec(TypeReference, ObjectMapper)} — custom mapper, generic type</li>
 * </ul>
 *
 * <p>Defaults to {@link ObjectMapper#shared()}; pass a configured mapper through
 * the constructor to customize features.
 *
 * @param <T> the value type
 */
public class Fastjson3RedissonCodec<T> extends BaseCodec {
    private final ObjectMapper mapper;
    private final Type targetType;
    private final Encoder encoder;
    private final Decoder<Object> decoder;

    public Fastjson3RedissonCodec(Class<T> targetType) {
        this(targetType, Fastjson3MapperHolder.get());
    }

    public Fastjson3RedissonCodec(TypeReference<T> targetType) {
        this(unwrap(targetType), Fastjson3MapperHolder.get());
    }

    public Fastjson3RedissonCodec(Class<T> targetType, ObjectMapper mapper) {
        this((Type) targetType, mapper);
    }

    public Fastjson3RedissonCodec(TypeReference<T> targetType, ObjectMapper mapper) {
        this(unwrap(targetType), mapper);
    }

    private Fastjson3RedissonCodec(Type targetType, ObjectMapper mapper) {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
        this.targetType = targetType;
        this.encoder = this::encode;
        this.decoder = this::decode;
    }

    private static Type unwrap(TypeReference<?> ref) {
        if (ref == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        return ref.getType();
    }

    private ByteBuf encode(Object in) throws IOException {
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        try {
            try (ByteBufOutputStream os = new ByteBufOutputStream(out)) {
                // mapper.writeValue(os, ...) currently materializes a byte[]
                // internally then writes — same shape as writeValueAsBytes +
                // os.write(bytes). Use the OutputStream form anyway so this
                // becomes zero-copy automatically when fj3 grows a streaming
                // JSONGenerator-to-OutputStream path.
                mapper.writeValue(os, in);
            }
            return out;
        } catch (Throwable ex) {
            // Release on any failure (RuntimeException from fj3, IOException
            // from ByteBufOutputStream.close, OOM via netty allocator, etc.)
            // before propagating — otherwise the buf leaks.
            out.release();
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException("JSON write error: " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Object decode(ByteBuf buf, State state) throws IOException {
        try (ByteBufInputStream is = new ByteBufInputStream(buf)) {
            // Prefer the Class<T> overload for the UTF-8 fast path when targetType
            // is a non-generic class.
            if (targetType instanceof Class<?>) {
                return mapper.readValue(is, (Class<?>) targetType);
            }
            return mapper.readValue(is, targetType);
        } catch (RuntimeException ex) {
            throw new IOException("JSON parse error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    /**
     * Map keys go through Redisson's {@link StringCodec} so {@code RMap<String, T>}
     * keys round-trip raw (not JSON-quoted). This matches the convention used by
     * Redisson's bundled {@code JsonJacksonCodec} and prevents key mismatches
     * with code paths that use other codecs (e.g. {@code StringCodec.INSTANCE}
     * directly, or RMap operations from a different client).
     */
    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return StringCodec.INSTANCE.getMapKeyDecoder();
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return StringCodec.INSTANCE.getMapKeyEncoder();
    }
}

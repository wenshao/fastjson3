package com.alibaba.fastjson3.grpc;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import io.grpc.MethodDescriptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * gRPC {@link MethodDescriptor.Marshaller} that encodes/decodes message bodies
 * as JSON via fastjson3. Useful for gRPC-JSON transcoding scenarios where the
 * wire format is JSON instead of protobuf — e.g. {@code grpc-json} proxies,
 * server reflection over gRPC-Web, or hand-rolled {@code MethodDescriptor}s
 * that swap the marshaller layer.
 *
 * <p>Wiring example:
 * <pre>{@code
 *   MethodDescriptor.<MyRequest, MyResponse>newBuilder()
 *       .setType(MethodDescriptor.MethodType.UNARY)
 *       .setFullMethodName("acme.MyService/Call")
 *       .setRequestMarshaller(new Fastjson3JsonMarshaller<>(MyRequest.class))
 *       .setResponseMarshaller(new Fastjson3JsonMarshaller<>(MyResponse.class))
 *       .build();
 * }</pre>
 *
 * <p>Constructor variants:
 * <ul>
 *   <li>{@link #Fastjson3JsonMarshaller(Class)} — non-generic types</li>
 *   <li>{@link #Fastjson3JsonMarshaller(TypeReference)} — generic types</li>
 *   <li>{@link #Fastjson3JsonMarshaller(Class, ObjectMapper)} — custom mapper</li>
 *   <li>{@link #Fastjson3JsonMarshaller(TypeReference, ObjectMapper)} — custom mapper, generic type</li>
 * </ul>
 *
 * <p>Defaults to {@link ObjectMapper#shared()}; pass a configured mapper through
 * the constructor to customize features.
 *
 * @param <T> the message type
 */
public class Fastjson3JsonMarshaller<T> implements MethodDescriptor.Marshaller<T> {
    private final ObjectMapper mapper;
    private final Type targetType;

    public Fastjson3JsonMarshaller(Class<T> targetType) {
        this(targetType, ObjectMapper.shared());
    }

    public Fastjson3JsonMarshaller(TypeReference<T> targetType) {
        this(unwrap(targetType), ObjectMapper.shared());
    }

    public Fastjson3JsonMarshaller(Class<T> targetType, ObjectMapper mapper) {
        this((Type) targetType, mapper);
    }

    public Fastjson3JsonMarshaller(TypeReference<T> targetType, ObjectMapper mapper) {
        this(unwrap(targetType), mapper);
    }

    private Fastjson3JsonMarshaller(Type targetType, ObjectMapper mapper) {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.targetType = targetType;
        this.mapper = mapper;
    }

    private static Type unwrap(TypeReference<?> ref) {
        if (ref == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        return ref.getType();
    }

    @Override
    public InputStream stream(T value) {
        return new ByteArrayInputStream(mapper.writeValueAsBytes(value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T parse(InputStream stream) {
        // Prefer the Class<T> overload for the UTF-8 fast path when targetType
        // is a non-generic class (matches mybatis/jpa/kafka pattern).
        Type t = targetType;
        if (t instanceof Class<?>) {
            return (T) mapper.readValue(stream, (Class<?>) t);
        }
        return mapper.readValue(stream, t);
    }
}

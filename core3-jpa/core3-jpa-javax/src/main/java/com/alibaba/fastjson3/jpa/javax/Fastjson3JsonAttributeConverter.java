package com.alibaba.fastjson3.jpa.javax;

import com.alibaba.fastjson3.Fastjson3MapperHolder;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;

import javax.persistence.AttributeConverter;

import java.lang.reflect.Type;

/**
 * Generic JPA {@link AttributeConverter} that round-trips an entity attribute
 * through a JSON column (typically declared as {@code TEXT} / {@code VARCHAR(...)}
 * / {@code JSON} on the database side).
 *
 * <p>This artifact targets the legacy {@code javax.persistence} namespace
 * (JPA 2.x). For Jakarta Persistence 3.0+ ({@code jakarta.persistence}) use
 * {@code fastjson3-jpa-jakarta} instead.
 *
 * <p>Subclass with the concrete attribute type — fastjson3 cannot infer
 * {@code T} at runtime from the parent class's type parameter (erasure on
 * this generic abstract base), so subclasses must pass a {@code Class<T>}
 * or {@code TypeReference<T>} to the constructor:
 * <pre>{@code
 *   @Converter(autoApply = false)
 *   public class TagsConverter extends Fastjson3JsonAttributeConverter<List<String>> {
 *       public TagsConverter() {
 *           super(new TypeReference<List<String>>() {});
 *       }
 *   }
 * }</pre>
 *
 * <p>Constructor variants:
 * <ul>
 *   <li>{@link #Fastjson3JsonAttributeConverter(Class)} — non-generic types</li>
 *   <li>{@link #Fastjson3JsonAttributeConverter(TypeReference)} — generic types</li>
 *   <li>{@link #Fastjson3JsonAttributeConverter(Class, ObjectMapper)} — custom mapper</li>
 *   <li>{@link #Fastjson3JsonAttributeConverter(TypeReference, ObjectMapper)} — custom mapper, generic type</li>
 * </ul>
 *
 * <p><b>Empty-string handling</b>: {@code convertToEntityAttribute("")} returns
 * {@code null}, accommodating databases that canonicalize empty TEXT to {@code ""}
 * rather than {@code NULL}.
 *
 * <p><b>Mapper resolution</b>: Hibernate instantiates the subclass via the
 * no-arg constructor, so the mapper is whatever the subclass passes to
 * {@code super(...)} — which defaults to {@link Fastjson3MapperHolder#get()}.
 * In a Spring Boot app the holder is populated by
 * {@code Fastjson3ObjectMapperAutoConfiguration} with the resolved
 * {@link ObjectMapper} bean, so {@code spring.fastjson3.*} settings
 * propagate to JPA-managed converters. Outside Spring the holder defaults
 * to {@link ObjectMapper#shared()} unless the application explicitly calls
 * {@link Fastjson3MapperHolder#set(ObjectMapper)} at startup.
 * <b>Ordering caveat</b>: the holder is published when the Spring
 * {@link ObjectMapper} bean finishes initializing; if your
 * {@code EntityManagerFactory} does not transitively depend on the
 * {@link ObjectMapper} bean, declare {@code @DependsOn} pointing at the
 * actual mapper bean name on it ({@code "fastjson3ObjectMapper"} for the
 * auto-config default, or your own bean's name when you supply a custom
 * mapper) so JPA bootstrap reads the configured mapper rather than the
 * default. To pin a specific mapper regardless of context, hardcode it in
 * the subclass.
 *
 * @param <T> the entity attribute type
 */
public abstract class Fastjson3JsonAttributeConverter<T> implements AttributeConverter<T, String> {
    private final ObjectMapper mapper;
    private final Type targetType;

    protected Fastjson3JsonAttributeConverter(Class<T> targetType) {
        this(targetType, Fastjson3MapperHolder.get());
    }

    protected Fastjson3JsonAttributeConverter(TypeReference<T> targetType) {
        this(unwrap(targetType), Fastjson3MapperHolder.get());
    }

    protected Fastjson3JsonAttributeConverter(Class<T> targetType, ObjectMapper mapper) {
        this((Type) targetType, mapper);
    }

    protected Fastjson3JsonAttributeConverter(TypeReference<T> targetType, ObjectMapper mapper) {
        this(unwrap(targetType), mapper);
    }

    /**
     * Advanced escape hatch: pass a {@link Type} computed at runtime.
     */
    protected Fastjson3JsonAttributeConverter(Type targetType, ObjectMapper mapper) {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
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
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }
        return mapper.writeValueAsString(attribute);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        // Prefer the Class<T> overload when targetType is a non-generic class
        // — fj3's readValue(String, Class<T>) reaches the UTF-8 fast path
        // (readObjectUTF8) that the Type overload skips.
        Type t = targetType;
        if (t instanceof Class<?>) {
            return (T) mapper.readValue(dbData, (Class<?>) t);
        }
        return mapper.readValue(dbData, t);
    }
}

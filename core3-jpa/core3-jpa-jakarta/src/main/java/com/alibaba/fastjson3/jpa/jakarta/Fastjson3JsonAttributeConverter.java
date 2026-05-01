package com.alibaba.fastjson3.jpa.jakarta;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import jakarta.persistence.AttributeConverter;

import java.lang.reflect.Type;

/**
 * Generic JPA {@link AttributeConverter} that round-trips an entity attribute
 * through a JSON column (typically declared as {@code TEXT} / {@code VARCHAR(...)}
 * / {@code JSON} on the database side).
 *
 * <p>Subclass with the concrete attribute type — fastjson3 cannot infer
 * {@code T} at runtime from the parent class's type parameter (erasure on
 * this generic abstract base), so subclasses must pass a {@code Class<T>}
 * or {@code Type} to the constructor:
 * <pre>{@code
 *   @Converter(autoApply = false)
 *   public class TagsConverter extends Fastjson3JsonAttributeConverter<List<String>> {
 *       public TagsConverter() {
 *           super(new TypeReference<List<String>>() {});
 *       }
 *   }
 *
 *   @Entity
 *   public class Article {
 *       @Convert(converter = TagsConverter.class)
 *       private List<String> tags;
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
 * rather than {@code NULL}. If your schema distinguishes the two, override
 * {@link #convertToEntityAttribute(String)}.
 *
 * @param <T> the entity attribute type
 */
public abstract class Fastjson3JsonAttributeConverter<T> implements AttributeConverter<T, String> {
    private final ObjectMapper mapper;
    private final Type targetType;

    protected Fastjson3JsonAttributeConverter(Class<T> targetType) {
        this(targetType, ObjectMapper.shared());
    }

    protected Fastjson3JsonAttributeConverter(TypeReference<T> targetType) {
        this(unwrap(targetType), ObjectMapper.shared());
    }

    protected Fastjson3JsonAttributeConverter(Class<T> targetType, ObjectMapper mapper) {
        this((Type) targetType, mapper);
    }

    protected Fastjson3JsonAttributeConverter(TypeReference<T> targetType, ObjectMapper mapper) {
        this(unwrap(targetType), mapper);
    }

    /**
     * Advanced escape hatch: pass a {@link Type} computed at runtime (e.g.
     * resolved from a field via reflection on the enclosing entity class).
     * For simple cases prefer {@link #Fastjson3JsonAttributeConverter(Class)}
     * or {@link #Fastjson3JsonAttributeConverter(TypeReference)}.
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
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return mapper.readValue(dbData, targetType);
    }
}

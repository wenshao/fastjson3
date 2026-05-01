package com.alibaba.fastjson3.jpa.jakarta;

import com.alibaba.fastjson3.ObjectMapper;
import jakarta.persistence.AttributeConverter;

import java.lang.reflect.Type;

/**
 * Generic JPA {@link AttributeConverter} that round-trips an entity attribute
 * through a JSON column (typically declared as {@code TEXT} / {@code VARCHAR(...)}
 * / {@code JSON} on the database side).
 *
 * <p>Subclass with the concrete attribute type — JPA cannot read the type
 * argument directly because of erasure:
 * <pre>{@code
 *   @Converter(autoApply = false)
 *   public class TagsConverter extends Fastjson3JsonAttributeConverter<List<String>> {
 *       public TagsConverter() {
 *           super(new TypeReference<List<String>>() {}.getType());
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
 * <p>For non-generic types use the {@link #Fastjson3JsonAttributeConverter(Class)}
 * constructor.
 *
 * <p>Defaults to {@link ObjectMapper#shared()}; pass a configured mapper through
 * the protected constructor to customize features.
 *
 * @param <T> the entity attribute type
 */
public abstract class Fastjson3JsonAttributeConverter<T> implements AttributeConverter<T, String> {
    private final ObjectMapper mapper;
    private final Type targetType;

    protected Fastjson3JsonAttributeConverter(Class<T> targetType) {
        this(ObjectMapper.shared(), targetType);
    }

    protected Fastjson3JsonAttributeConverter(Type targetType) {
        this(ObjectMapper.shared(), targetType);
    }

    protected Fastjson3JsonAttributeConverter(ObjectMapper mapper, Type targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        this.mapper = mapper == null ? ObjectMapper.shared() : mapper;
        this.targetType = targetType;
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

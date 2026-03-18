package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;

import java.lang.reflect.Type;

/**
 * Provider for creating {@link ObjectReader} instances.
 * Allows customization of the reader creation strategy (ASM vs Reflection).
 *
 * @see ReaderCreatorType
 */
public interface ObjectReaderProvider {

    /**
     * Get or create an ObjectReader for the given type.
     *
     * @param type the target type
     * @param <T>  the type parameter
     * @return the ObjectReader instance
     */
    <T> ObjectReader<T> getObjectReader(Class<T> type);

    /**
     * Get or create an ObjectReader for the given type.
     *
     * @param type the target type
     * @param <T>  the type parameter
     * @return the ObjectReader instance
     */
    @SuppressWarnings("unchecked")
    default <T> ObjectReader<T> getObjectReader(Type type) {
        if (type instanceof Class) {
            return getObjectReader((Class<T>) type);
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    /**
     * Get the creator type strategy used by this provider.
     */
    ReaderCreatorType getCreatorType();

    /**
     * Create a new provider with the specified creator type.
     *
     * @param creatorType the creator type strategy
     * @return a new provider instance
     */
    static ObjectReaderProvider of(ReaderCreatorType creatorType) {
        return switch (creatorType) {
            case AUTO -> AutoObjectReaderProvider.INSTANCE;
            case ASM -> ASMObjectReaderProvider.INSTANCE;
            case REFLECT -> ReflectObjectReaderProvider.INSTANCE;
        };
    }

    /**
     * Get the default provider (uses AUTO strategy).
     */
    static ObjectReaderProvider defaultProvider() {
        return AutoObjectReaderProvider.INSTANCE;
    }

    // ==================== Default Implementations ====================

    /**
     * Provider that automatically chooses between ASM and reflection.
     */
    AutoObjectReaderProvider AUTO = AutoObjectReaderProvider.INSTANCE;

    /**
     * Provider that prefers ASM generation.
     */
    ASMObjectReaderProvider ASM = ASMObjectReaderProvider.INSTANCE;

    /**
     * Provider that uses only reflection.
     */
    ReflectObjectReaderProvider REFLECT = ReflectObjectReaderProvider.INSTANCE;
}

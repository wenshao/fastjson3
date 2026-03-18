package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ObjectReaderProvider that prefers ASM bytecode generation.
 * Will recursively generate ASM readers for nested types when possible.
 */
public final class ASMObjectReaderProvider implements ObjectReaderProvider {

    public static final ASMObjectReaderProvider INSTANCE = new ASMObjectReaderProvider();

    private final ConcurrentMap<Class<?>, ObjectReader<?>> readerCache = new ConcurrentHashMap<>();

    private ASMObjectReaderProvider() {
    }

    @Override
    public ReaderCreatorType getCreatorType() {
        return ReaderCreatorType.ASM;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectReader<T> getObjectReader(Class<T> type) {
        return (ObjectReader<T>) readerCache.computeIfAbsent(type, t ->
            createRecursiveReader(t, new java.util.HashSet<>())
        );
    }

    /**
     * Recursively create ASM reader for the type and all nested types.
     *
     * @param type        the target type
     * @param creating     set of types currently being created (to prevent infinite recursion)
     * @param <T>         the type parameter
     * @return the ObjectReader instance
     */
    @SuppressWarnings("unchecked")
    private <T> ObjectReader<T> createRecursiveReader(Class<T> type, java.util.Set<Class<?>> creating) {
        // Prevent infinite recursion
        if (!creating.add(type)) {
            // Circular dependency detected, use reflection as fallback
            return ObjectReaderCreator.createObjectReader(type);
        }

        try {
            // First, try to create ASM reader
            ObjectReader<T> asmReader = ObjectReaderCreatorASM.createObjectReader(type);

            // Pre-create readers for nested field types (not collections/maps)
            // This helps warm up the cache for deeply nested structures
            prewarmNestedTypes(type, creating);

            return asmReader;
        } catch (Throwable e) {
            // ASM generation failed, fall back to reflection
            return ObjectReaderCreator.createObjectReader(type);
        } finally {
            creating.remove(type);
        }
    }

    /**
     * Pre-warm the cache for nested field types.
     * Only processes direct field types that are simple POJOs.
     */
    private void prewarmNestedTypes(Class<?> type, java.util.Set<Class<?>> creating) {
        // Get field readers to find nested types
        ObjectReaderCreator.FieldReaderCollection collection = ObjectReaderCreator.collectFieldReaders(type);
        if (collection.fieldReaders == null) {
            return;
        }

        for (FieldReader fieldReader : collection.fieldReaders) {
            Class<?> fieldType = fieldReader.fieldClass;
            // Skip: primitives, String, arrays, collections, maps
            if (shouldSkipType(fieldType)) {
                continue;
            }

            // Recursively create reader for nested POJO type
            if (!readerCache.containsKey(fieldType)) {
                try {
                    createRecursiveReader(fieldType, creating);
                } catch (Throwable ignored) {
                    // Ignore prewarm failures
                }
            }
        }
    }

    private boolean shouldSkipType(Class<?> type) {
        return type.isPrimitive()
            || type == String.class
            || type == Boolean.class
            || type == Character.class
            || type == Byte.class
            || type == Short.class
            || type == Integer.class
            || type == Long.class
            || type == Float.class
            || type == Double.class
            || type.isArray()
            || Iterable.class.isAssignableFrom(type)
            || java.util.Map.class.isAssignableFrom(type);
    }
}

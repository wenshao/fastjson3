package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.util.Logger;

/**
 * ObjectReaderProvider that prefers ASM bytecode generation.
 * Will recursively generate ASM readers for nested types when possible.
 */
@com.alibaba.fastjson3.annotation.JVMOnly
public final class ASMObjectReaderProvider extends AbstractObjectReaderProvider {

    public static final ASMObjectReaderProvider INSTANCE = new ASMObjectReaderProvider();

    private static final Logger.CategoryLogger LOG = Logger.category("ASMProvider");

    public ASMObjectReaderProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader to use for ASM-generated classes,
     *                    or null to use the shared instance
     */
    public ASMObjectReaderProvider(com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public ReaderCreatorType getCreatorType() {
        return ReaderCreatorType.ASM;
    }

    @Override
    protected ObjectReader<?> createReader(Class<?> type) {
        // Check built-in codecs first (UUID, Duration, Period, etc.)
        com.alibaba.fastjson3.ObjectReader<?> builtin = com.alibaba.fastjson3.BuiltinCodecs.getReader(type);
        if (builtin != null) {
            return builtin;
        }
        return createRecursiveReader(type, java.util.concurrent.ConcurrentHashMap.newKeySet());
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
        return createRecursiveReader(type, creating, 0);
    }

    /**
     * Recursively create ASM reader for the type and all nested types.
     *
     * @param type        the target type
     * @param creating     concurrent set of types currently being created (to prevent infinite recursion)
     * @param depth       current recursion depth (to prevent stack overflow)
     * @param <T>         the type parameter
     * @return the ObjectReader instance
     */
    @SuppressWarnings("unchecked")
    private <T> ObjectReader<T> createRecursiveReader(Class<T> type, java.util.Set<Class<?>> creating, int depth) {
        // Maximum recursion depth to prevent stack overflow
        // 100 levels should be more than enough for any real-world POJO structure
        final int MAX_DEPTH = 100;
        if (depth > MAX_DEPTH) {
            // Too deep, fall back to reflection for this branch
            return ObjectReaderCreator.createObjectReader(type);
        }

        // Prevent infinite recursion (circular references)
        if (!creating.add(type)) {
            // Circular dependency detected, use reflection as fallback
            return ObjectReaderCreator.createObjectReader(type);
        }

        try {
            // First, try to create ASM reader
            ObjectReader<T> asmReader = ObjectReaderCreatorASM.createObjectReader(type);

            // Pre-create readers for nested field types (not collections/maps)
            // This helps warm up the cache for deeply nested structures
            prewarmNestedTypes(type, creating, depth);

            return asmReader;
        } catch (Throwable e) {
            // ASM generation failed, fall back to reflection
            LOG.debug(() -> "ASM generation failed for " + type.getName() + ", using reflection: " + e.getMessage());
            return ObjectReaderCreator.createObjectReader(type);
        } finally {
            creating.remove(type);
        }
    }

    /**
     * Pre-warm the cache for nested field types.
     * Only processes direct field types that are simple POJOs.
     */
    private void prewarmNestedTypes(Class<?> type, java.util.Set<Class<?>> creating, int depth) {
        // Get field readers to find nested types
        ObjectReaderCreator.FieldReaderCollection collection = ObjectReaderCreator.collectFieldReaders(type);

        for (FieldReader fieldReader : collection.fieldReaders) {
            Class<?> fieldType = fieldReader.fieldClass;
            // Skip: primitives, String, arrays, collections, maps
            if (shouldSkipType(fieldType)) {
                continue;
            }

            // Recursively create reader for nested POJO type
            if (!readerCache.containsKey(fieldType)) {
                try {
                    createRecursiveReader(fieldType, creating, depth + 1);
                } catch (Throwable e) {
                    // Ignore prewarm failures - will be created on-demand
                    LOG.debug(() -> "Prewarm failed for " + fieldType.getName() + ": " + e.getMessage());
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

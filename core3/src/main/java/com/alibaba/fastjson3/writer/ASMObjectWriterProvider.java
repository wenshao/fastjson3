package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;
import com.alibaba.fastjson3.util.Logger;

/**
 * ObjectWriterProvider that prefers ASM bytecode generation.
 */
@com.alibaba.fastjson3.annotation.JVMOnly
public final class ASMObjectWriterProvider extends AbstractObjectWriterProvider {

    public static final ASMObjectWriterProvider INSTANCE = new ASMObjectWriterProvider();

    private static final Logger.CategoryLogger LOG = Logger.category("ASMWriterProvider");

    public ASMObjectWriterProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader to use for ASM-generated classes,
     *                    or null to use the shared instance
     */
    public ASMObjectWriterProvider(com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public WriterCreatorType getCreatorType() {
        return WriterCreatorType.ASM;
    }

    @Override
    protected ObjectWriter<?> createWriter(Class<?> type) {
        // Check built-in codecs first (UUID, Duration, Period, etc.)
        com.alibaba.fastjson3.ObjectWriter<?> builtin = com.alibaba.fastjson3.BuiltinCodecs.getWriter(type);
        if (builtin != null) {
            return builtin;
        }

        // Primitive types don't have ObjectWriters - they're handled inline
        if (type.isPrimitive() && type != void.class) {
            return null;
        }

        // Skip interfaces and abstract classes - they don't have constructors
        if (type.isInterface() || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            return null;
        }

        return createRecursiveWriter(type, java.util.concurrent.ConcurrentHashMap.newKeySet());
    }

    /**
     * Recursively create ASM writer for the type and all nested types.
     *
     * @param type        the target type
     * @param creating     set of types currently being created (to prevent infinite recursion)
     * @param <T>         the type parameter
     * @return the ObjectWriter instance
     */
    @SuppressWarnings("unchecked")
    private <T> ObjectWriter<T> createRecursiveWriter(Class<T> type, java.util.Set<Class<?>> creating) {
        return createRecursiveWriter(type, creating, 0);
    }

    /**
     * Recursively create ASM writer for the type and all nested types.
     *
     * @param type        the target type
     * @param creating     concurrent set of types currently being created (to prevent infinite recursion)
     * @param depth       current recursion depth (to prevent stack overflow)
     * @param <T>         the type parameter
     * @return the ObjectWriter instance
     */
    @SuppressWarnings("unchecked")
    private <T> ObjectWriter<T> createRecursiveWriter(Class<T> type, java.util.Set<Class<?>> creating, int depth) {
        // Maximum recursion depth to prevent stack overflow
        // 100 levels should be more than enough for any real-world POJO structure
        final int MAX_DEPTH = 100;
        if (depth > MAX_DEPTH) {
            // Too deep, fall back to reflection for this branch
            return ObjectWriterCreator.createObjectWriter(type);
        }

        // Prevent infinite recursion (circular references)
        if (!creating.add(type)) {
            // Circular dependency detected, use reflection as fallback
            return ObjectWriterCreator.createObjectWriter(type);
        }

        try {
            // First, try to create ASM writer
            ObjectWriter<T> asmWriter = ObjectWriterCreatorASM.createObjectWriter(type);

            // Pre-create writers for nested field types (not collections/maps)
            // This helps warm up the cache for deeply nested structures
            prewarmNestedTypes(type, creating, depth);

            return asmWriter;
        } catch (Throwable e) {
            // ASM generation failed, fall back to reflection
            LOG.debug(() -> "ASM generation failed for " + type.getName() + ", using reflection: " + e.getMessage());
            return ObjectWriterCreator.createObjectWriter(type);
        } finally {
            creating.remove(type);
        }
    }

    /**
     * Pre-warm the cache for nested field types.
     * Only processes direct field types that are simple POJOs.
     */
    private void prewarmNestedTypes(Class<?> type, java.util.Set<Class<?>> creating, int depth) {
        // Get declared fields to find nested types
        for (java.lang.reflect.Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            // Skip static, transient, and synthetic fields
            if (java.lang.reflect.Modifier.isStatic(modifiers)
                    || java.lang.reflect.Modifier.isTransient(modifiers)
                    || field.isSynthetic()) {
                continue;
            }

            Class<?> fieldType = field.getType();
            // Skip: primitives, String, arrays, collections, maps
            if (shouldSkipType(fieldType)) {
                continue;
            }

            // Recursively create and cache writer for nested POJO type
            if (!writerCache.containsKey(fieldType)) {
                try {
                    ObjectWriter<?> writer = createRecursiveWriter(fieldType, creating, depth + 1);
                    if (writer != null) {
                        writerCache.put(fieldType, writer);
                    }
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

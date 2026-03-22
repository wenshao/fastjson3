package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.util.JDKUtils;
import com.alibaba.fastjson3.util.Logger;

/**
 * ObjectReaderProvider that automatically chooses between ASM and reflection.
 *
 * <p>Decision logic:</p>
 * <ul>
 *   <li>Native-image: always use reflection</li>
 *   <li>Android: always use reflection (no ASM support)</li>
 *   <li>Simple POJOs (public, concrete, no schema): try ASM first, fallback to reflection</li>
 *   <li>Complex types (records, sealed classes): use reflection</li>
 * </ul>
 */
public final class AutoObjectReaderProvider extends AbstractObjectReaderProvider {

    private static final Logger.CategoryLogger LOG = Logger.category("AutoProvider");

    // Check if ASM is available at runtime
    private static final boolean ASM_AVAILABLE;
    static {
        boolean available;
        try {
            // Check if ObjectReaderCreatorASM class exists
            Class.forName("com.alibaba.fastjson3.reader.ObjectReaderCreatorASM");
            // On non-Android builds, JDKUtils doesn't have ANDROID field
            // Assume available if ObjectReaderCreatorASM exists
            available = true;
        } catch (ClassNotFoundException e) {
            // ObjectReaderCreatorASM not available (Android build or stripped)
            available = false;
        }
        ASM_AVAILABLE = available && !JDKUtils.NATIVE_IMAGE;
    }

    public static final AutoObjectReaderProvider INSTANCE = new AutoObjectReaderProvider();

    public AutoObjectReaderProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader to use for ASM-generated classes,
     *                    or null to use the shared instance
     */
    public AutoObjectReaderProvider(com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public ReaderCreatorType getCreatorType() {
        return ReaderCreatorType.AUTO;
    }

    @Override
    protected ObjectReader<?> createReader(Class<?> type) {
        // Check built-in codecs first (UUID, Duration, Period, etc.)
        com.alibaba.fastjson3.ObjectReader<?> builtin = com.alibaba.fastjson3.BuiltinCodecs.getReader(type);
        if (builtin != null) {
            return builtin;
        }

        // Primitive types don't have ObjectReaders - they're handled inline
        // Return null so the parser handles them directly via typeTag dispatch
        if (type.isPrimitive() && type != void.class) {
            return null;
        }

        // Skip interfaces, abstract classes, and enums - they don't have accessible constructors
        // Enum deserialization is handled inline by FieldReader
        if (type.isInterface() || type.isEnum()
                || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            return null;
        }

        // For simple POJOs, try ASM first (if available)
        if (ASM_AVAILABLE && isSimplePOJO(type)) {
            try {
                return ObjectReaderCreatorASM.createObjectReader(type);
            } catch (Throwable e) {
                // ASM generation failed, fall back to reflection
                LOG.debug(() -> "ASM generation failed for " + type.getName() + ", using reflection: " + e.getMessage());
            }
        }

        // Default to reflection
        return ObjectReaderCreator.createObjectReader(type);
    }

    /**
     * Check if the type is a simple POJO that can benefit from ASM optimization.
     */
    private static boolean isSimplePOJO(Class<?> type) {
        if (type.isInterface() || type.isArray() || type.isEnum()
                || type.isPrimitive() || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            return false;
        }

        // Check if class is accessible for ASM
        // 1. Public classes are always accessible
        if (!java.lang.reflect.Modifier.isPublic(type.getModifiers())) {
            // 2. Static inner classes of public classes are also accessible
            // 3. Non-public static member classes (like benchmark classes) can be accessed
            if (!java.lang.reflect.Modifier.isStatic(type.getModifiers())) {
                // Non-public non-static class - not accessible
                return false;
            }
        }

        // Skip types with special JSON configuration
        com.alibaba.fastjson3.annotation.JSONType jsonType = type.getAnnotation(
                com.alibaba.fastjson3.annotation.JSONType.class);
        if (jsonType != null && !jsonType.schema().isEmpty()) {
            return false;
        }
        return true;
    }
}

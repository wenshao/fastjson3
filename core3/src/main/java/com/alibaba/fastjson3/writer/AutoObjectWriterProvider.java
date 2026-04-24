package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;
import com.alibaba.fastjson3.util.JDKUtils;
import com.alibaba.fastjson3.util.Logger;

/**
 * ObjectWriterProvider that automatically chooses between ASM and reflection.
 *
 * <p>Decision logic:</p>
 * <ul>
 *   <li>Native-image: always use reflection</li>
 *   <li>Android: always use reflection (no ASM support)</li>
 *   <li>Simple POJOs (public, concrete, no schema): try ASM first, fallback to reflection</li>
 *   <li>Complex types (records, sealed classes): use reflection</li>
 * </ul>
 */
public final class AutoObjectWriterProvider extends AbstractObjectWriterProvider {

    private static final Logger.CategoryLogger LOG = Logger.category("AutoWriterProvider");

    // Check if ASM is available at runtime
    private static final boolean ASM_AVAILABLE;
    static {
        boolean available;
        try {
            // Check if ObjectWriterCreatorASM class exists
            Class.forName("com.alibaba.fastjson3.writer.ObjectWriterCreatorASM");
            // On non-Android builds, JDKUtils doesn't have ANDROID field
            // Assume available if ObjectWriterCreatorASM exists
            available = true;
        } catch (ClassNotFoundException e) {
            // ObjectWriterCreatorASM not available (Android build or stripped)
            available = false;
        }
        ASM_AVAILABLE = available && !JDKUtils.NATIVE_IMAGE;
    }

    public static final AutoObjectWriterProvider INSTANCE = new AutoObjectWriterProvider();

    public AutoObjectWriterProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader to use for ASM-generated classes,
     *                    or null to use the shared instance
     */
    public AutoObjectWriterProvider(com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public WriterCreatorType getCreatorType() {
        return WriterCreatorType.AUTO;
    }

    @Override
    protected ObjectWriter<?> createWriter(Class<?> type) {
        // Check built-in codecs first (UUID, Duration, Period, etc.)
        com.alibaba.fastjson3.ObjectWriter<?> builtin = com.alibaba.fastjson3.BuiltinCodecs.getWriter(type);
        if (builtin != null) {
            return builtin;
        }

        // Primitive types don't have ObjectWriters - they're handled inline
        // Return null so the serializer handles them directly via typeTag dispatch
        if (type.isPrimitive() && type != void.class) {
            return null;
        }

        // Skip interfaces and abstract classes - they don't have constructors
        if (type.isInterface() || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            return null;
        }

        // For simple POJOs, try ASM first (if available)
        if (ASM_AVAILABLE && isSimplePOJO(type) && !hasEnumFieldWithValueAccessor(type)) {
            try {
                return ObjectWriterCreatorASM.createObjectWriter(type);
            } catch (Throwable e) {
                // ASM generation failed, fall back to reflection
                LOG.debug(() -> "ASM generation failed for " + type.getName() + ", using reflection: " + e.getMessage());
            }
        }

        // Default to reflection
        return ObjectWriterCreator.createObjectWriter(type);
    }

    /**
     * Check whether any non-static enum field's type declares a
     * {@code @JSONField(value=true)} getter (or Jackson {@code @JsonValue}).
     * When yes, the ASM writer's TYPE_ENUM fast path — which bakes
     * {@code enum.name()} bytes per ordinal — would emit the constant
     * name instead of the value-method result. The reflect path's
     * per-field {@code findValueWriter} lookup (ObjectWriterCreator
     * line ~481) handles this correctly, so route to it.
     */
    private static boolean hasEnumFieldWithValueAccessor(Class<?> type) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())
                        || java.lang.reflect.Modifier.isTransient(f.getModifiers())) {
                    continue;
                }
                Class<?> ft = f.getType();
                if (ft.isEnum() && hasValueAnnotatedMethod(ft)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasValueAnnotatedMethod(Class<?> enumType) {
        for (java.lang.reflect.Method m : enumType.getMethods()) {
            if (m.getDeclaringClass() == Object.class || m.getParameterCount() != 0
                    || java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                continue;
            }
            com.alibaba.fastjson3.annotation.JSONField jf =
                    m.getAnnotation(com.alibaba.fastjson3.annotation.JSONField.class);
            if (jf != null && jf.value()) {
                return true;
            }
        }
        return false;
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

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
        // Enum-typed POJO fields are deserialized inline by FieldReader;
        // top-level enum and collection element enum deserialization is not yet supported
        if (type.isInterface() || type.isEnum()
                || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            return null;
        }

        // For simple POJOs, try ASM first (if available and type doesn't
        // use features the ASM reader doesn't support).
        // IMPORTANT: all guards must live HERE, not in ObjectReaderCreatorASM,
        // because GraalVM's JIT is sensitive to ANY bytecode change in that
        // class — even adding dead-code checks to canGenerate() perturbs the
        // tiered compilation cascade for the generated reader classes.
        if (ASM_AVAILABLE && isSimplePOJO(type)
                && !hasAnySetter(type)
                && !hasBuiltinCodecField(type)
                && !hasUnwrappedField(type)
                && countSerializableFields(type) <= 15) {
            try {
                return ObjectReaderCreatorASM.createObjectReader(type);
            } catch (Throwable e) {
                // ASM generation failed, fall back to reflection
                LOG.debug(() -> "ASM generation failed for " + type.getName() + ", using reflection: " + e.getMessage());
            }
        }

        // Default to reflection (handles anySetter, BuiltinCodecs fields,
        // @JSONField(unwrapped=true), etc.)
        return ObjectReaderCreator.createObjectReader(type);
    }

    /**
     * Check if any field (on this class or a superclass) carries
     * {@code @JSONField(unwrapped=true)}. The ASM reader's generated dispatch
     * emits the outer's direct FieldReader list only and has no hook for the
     * inner-routing side table populated by {@code expandUnwrappedField}, so
     * beans that declare unwrapped must fall back to reflection until ASM
     * gains native support.
     */
    private static boolean hasUnwrappedField(Class<?> type) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field field : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                com.alibaba.fastjson3.annotation.JSONField jf =
                        field.getAnnotation(com.alibaba.fastjson3.annotation.JSONField.class);
                if (jf != null && jf.unwrapped()) {
                    return true;
                }
            }
            // Setter-side: @JSONField(unwrapped=true) on a 1-arg method also triggers
            // the unwrapped-expansion path and isn't reflected in the FieldReader array.
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) || m.getParameterCount() != 1) {
                    continue;
                }
                com.alibaba.fastjson3.annotation.JSONField jf =
                        m.getAnnotation(com.alibaba.fastjson3.annotation.JSONField.class);
                if (jf != null && jf.unwrapped()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the type has a {@code @JSONField(anySetter=true)} method.
     * The ASM reader silently skips unknown fields instead of routing them
     * to the any-setter.
     */
    private static boolean hasAnySetter(Class<?> type) {
        for (java.lang.reflect.Method method : type.getMethods()) {
            if (java.lang.reflect.Modifier.isStatic(method.getModifiers())
                    || method.getParameterCount() != 2) {
                continue;
            }
            if (method.getParameterTypes()[0] != String.class) {
                continue;
            }
            com.alibaba.fastjson3.annotation.JSONField jf =
                    method.getAnnotation(com.alibaba.fastjson3.annotation.JSONField.class);
            if (jf != null && jf.anySetter()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if any field uses a BuiltinCodecs type (URI, Path, Optional,
     * Duration, etc.) that the ASM reader's generic-field delegate path
     * can't handle correctly.
     */
    private static boolean hasBuiltinCodecField(Class<?> type) {
        for (java.lang.reflect.Field field : type.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    || java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (com.alibaba.fastjson3.BuiltinCodecs.getReader(field.getType()) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count non-static, non-transient declared fields. POJOs with &gt; 15
     * fields produce a readObjectUTF8 &gt; 2000 bytes — far over C2's
     * FreqInlineSize=325 budget. On aarch64 the REFLECT reader's compact
     * readFieldsLoop actually outperforms the huge ASM method because the
     * REFLECT entry point (11 bytes) inlines while the ASM entry (2000+
     * bytes) can't. Threshold of 15 keeps Eishay-class POJOs (≤ 13 fields)
     * on the fast ASM path.
     */
    private static int countSerializableFields(Class<?> type) {
        int count = 0;
        for (java.lang.reflect.Field field : type.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    || java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            count++;
        }
        return count;
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

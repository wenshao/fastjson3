package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.util.JDKUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ObjectReaderProvider that automatically chooses between ASM and reflection.
 *
 * <p>Decision logic:</p>
 * <ul>
 *   <li>Native-image: always use reflection</li>
 *   <li>Simple POJOs (public, concrete, no schema): try ASM first, fallback to reflection</li>
 *   <li>Complex types (records, sealed classes): use reflection</li>
 * </ul>
 */
public final class AutoObjectReaderProvider implements ObjectReaderProvider {

    public static final AutoObjectReaderProvider INSTANCE = new AutoObjectReaderProvider();

    private final ConcurrentMap<Class<?>, ObjectReader<?>> readerCache = new ConcurrentHashMap<>();

    private AutoObjectReaderProvider() {
    }

    @Override
    public ReaderCreatorType getCreatorType() {
        return ReaderCreatorType.AUTO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectReader<T> getObjectReader(Class<T> type) {
        return (ObjectReader<T>) readerCache.computeIfAbsent(type, t -> {
            // Native-image doesn't support runtime bytecode generation
            if (JDKUtils.NATIVE_IMAGE) {
                return ObjectReaderCreator.createObjectReader(t);
            }

            // For simple POJOs, try ASM first
            if (isSimplePOJO(t)) {
                try {
                    return ObjectReaderCreatorASM.createObjectReader(t);
                } catch (Throwable ignored) {
                    // Fall through to reflection
                }
            }

            // Default to reflection
            return ObjectReaderCreator.createObjectReader(t);
        });
    }

    /**
     * Check if the type is a simple POJO that can benefit from ASM optimization.
     */
    private static boolean isSimplePOJO(Class<?> type) {
        if (type.isInterface() || type.isArray() || type.isEnum()
                || type.isPrimitive() || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            return false;
        }
        if (!java.lang.reflect.Modifier.isPublic(type.getModifiers())) {
            return false;
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

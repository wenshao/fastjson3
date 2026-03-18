package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.util.DynamicClassLoader;

/**
 * ObjectReaderProvider that uses reflection.
 * (Android version - ASM not available).
 */
public final class AutoObjectReaderProvider
        extends AbstractObjectReaderProvider {
    /** Singleton instance. */
    public static final AutoObjectReaderProvider INSTANCE =
            new AutoObjectReaderProvider();

    /** Default constructor. */
    public AutoObjectReaderProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader (ignored, kept for API consistency)
     */
    public AutoObjectReaderProvider(
            final DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public ReaderCreatorType getCreatorType() {
        return ReaderCreatorType.AUTO;
    }

    @Override
    protected ObjectReader<?> createReader(final Class<?> type) {
        // Skip interfaces and abstract classes
        if (type.isInterface()
                || java.lang.reflect.Modifier.isAbstract(
                        type.getModifiers())) {
            return null;
        }

        // On Android, always use reflection (ASM not available)
        return ObjectReaderCreator.createObjectReader(type);
    }
}

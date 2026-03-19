package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;

/**
 * ObjectWriterProvider that uses reflection.
 * (Android version - ASM not available).
 */
public final class AutoObjectWriterProvider
        extends AbstractObjectWriterProvider {
    /** Singleton instance. */
    public static final AutoObjectWriterProvider INSTANCE =
            new AutoObjectWriterProvider();

    /** Default constructor. */
    public AutoObjectWriterProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader (ignored, kept for API consistency)
     */
    public AutoObjectWriterProvider(
            final com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public WriterCreatorType getCreatorType() {
        return WriterCreatorType.AUTO;
    }

    @Override
    protected ObjectWriter<?> createWriter(final Class<?> type) {
        // Skip interfaces and abstract classes
        if (type.isInterface()
                || java.lang.reflect.Modifier.isAbstract(
                        type.getModifiers())) {
            return null;
        }

        // On Android, always use reflection (ASM not available)
        return ObjectWriterCreator.createObjectWriter(type);
    }
}

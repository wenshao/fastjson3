package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;

/**
 * ObjectWriterProvider that uses only reflection (Android version).
 */
public final class ReflectObjectWriterProvider
        extends AbstractObjectWriterProvider {
    /** Singleton instance. */
    public static final ReflectObjectWriterProvider INSTANCE =
            new ReflectObjectWriterProvider();

    /** Default constructor. */
    public ReflectObjectWriterProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader (ignored, kept for API)
     */
    public ReflectObjectWriterProvider(
            final com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public WriterCreatorType getCreatorType() {
        return WriterCreatorType.REFLECT;
    }

    @Override
    protected ObjectWriter<?> createWriter(final Class<?> type) {
        return ObjectWriterCreator.createObjectWriter(type);
    }
}

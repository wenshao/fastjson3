package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;

/**
 * ObjectWriterProvider that uses only reflection.
 * No ASM bytecode generation.
 */
public final class ReflectObjectWriterProvider extends AbstractObjectWriterProvider {

    public static final ReflectObjectWriterProvider INSTANCE = new ReflectObjectWriterProvider();

    public ReflectObjectWriterProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader (ignored for reflection provider, kept for API consistency)
     */
    public ReflectObjectWriterProvider(com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public WriterCreatorType getCreatorType() {
        return WriterCreatorType.REFLECT;
    }

    @Override
    protected ObjectWriter<?> createWriter(Class<?> type) {
        // Check built-in codecs first (UUID, Duration, Period, etc.)
        com.alibaba.fastjson3.ObjectWriter<?> builtin = com.alibaba.fastjson3.BuiltinCodecs.getWriter(type);
        if (builtin != null) {
            return builtin;
        }
        return ObjectWriterCreator.createObjectWriter(type);
    }
}

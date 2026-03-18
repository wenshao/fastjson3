package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;

/**
 * ObjectReaderProvider that uses only reflection.
 * No ASM bytecode generation.
 */
public final class ReflectObjectReaderProvider extends AbstractObjectReaderProvider {

    public static final ReflectObjectReaderProvider INSTANCE = new ReflectObjectReaderProvider();

    public ReflectObjectReaderProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader (ignored for reflection provider, kept for API consistency)
     */
    public ReflectObjectReaderProvider(com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public ReaderCreatorType getCreatorType() {
        return ReaderCreatorType.REFLECT;
    }

    @Override
    protected ObjectReader<?> createReader(Class<?> type) {
        // Check built-in codecs first (UUID, Duration, Period, etc.)
        com.alibaba.fastjson3.ObjectReader<?> builtin = com.alibaba.fastjson3.BuiltinCodecs.getReader(type);
        if (builtin != null) {
            return builtin;
        }
        return ObjectReaderCreator.createObjectReader(type);
    }
}

package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.util.DynamicClassLoader;
import com.alibaba.fastjson3.util.Logger;

/**
 * Android-compatible stub for ASMObjectWriterProvider.
 * On Android, ASM is not available, so this falls back to AUTO.
 */
public final class ASMObjectWriterProvider
        extends AbstractObjectWriterProvider {
    /** Singleton instance. */
    public static final ASMObjectWriterProvider INSTANCE =
            new ASMObjectWriterProvider();

    /** Logger for ASM writer provider. */
    private static final Logger.CategoryLogger LOG =
            Logger.category("ASMWriterProvider");

    /** Default constructor. */
    public ASMObjectWriterProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader (ignored, kept for API)
     */
    public ASMObjectWriterProvider(final DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public WriterCreatorType getCreatorType() {
        return WriterCreatorType.ASM;
    }

    @Override
    protected com.alibaba.fastjson3.ObjectWriter<?> createWriter(
            final Class<?> type) {
        // On Android, ASM is not available, delegate to AUTO
        LOG.debug("ASM not available on Android for "
                + type.getName() + ", using reflection");
        return AutoObjectWriterProvider.INSTANCE.createWriter(type);
    }
}

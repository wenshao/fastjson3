package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.util.DynamicClassLoader;
import com.alibaba.fastjson3.util.Logger;

/**
 * Android-compatible stub for ASMObjectReaderProvider.
 * On Android, ASM is not available, so this falls back to AUTO (reflection).
 */
public final class ASMObjectReaderProvider
        extends AbstractObjectReaderProvider {
    /** Singleton instance. */
    public static final ASMObjectReaderProvider INSTANCE =
            new ASMObjectReaderProvider();

    /** Logger for ASM provider. */
    private static final Logger.CategoryLogger LOG =
            Logger.category("ASMProvider");

    /** Default constructor. */
    public ASMObjectReaderProvider() {
        super(null);
    }

    /**
     * Create a provider with a specific classloader.
     *
     * @param classLoader the classloader (ignored, kept for API consistency)
     */
    public ASMObjectReaderProvider(final DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public ReaderCreatorType getCreatorType() {
        return ReaderCreatorType.ASM;
    }

    @Override
    protected ObjectReader<?> createReader(final Class<?> type) {
        // On Android, ASM is not available, delegate to AUTO (reflection)
        LOG.debug("ASM not available on Android for "
                + type.getName() + ", using reflection");
        return AutoObjectReaderProvider.INSTANCE.getObjectReader(type);
    }
}

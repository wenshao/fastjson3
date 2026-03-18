package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.util.Logger;

/**
 * Android-compatible stub for ASMObjectReaderProvider.
 * On Android, ASM is not available, so this falls back to AUTO (reflection).
 */
public final class ASMObjectReaderProvider extends AbstractObjectReaderProvider {
    public static final ASMObjectReaderProvider INSTANCE = new ASMObjectReaderProvider();

    private static final Logger.CategoryLogger LOG = Logger.category("ASMProvider");

    public ASMObjectReaderProvider() {
        super(null);
    }

    public ASMObjectReaderProvider(com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public ReaderCreatorType getCreatorType() {
        return ReaderCreatorType.ASM;
    }

    @Override
    protected ObjectReader<?> createReader(Class<?> type) {
        // On Android, ASM is not available, delegate to AUTO (reflection)
        LOG.debug(() -> "ASM not available on Android for " + type.getName() + ", using reflection");
        return AutoObjectReaderProvider.INSTANCE.createReader(type);
    }
}

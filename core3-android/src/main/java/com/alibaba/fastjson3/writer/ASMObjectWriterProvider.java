package com.alibaba.fastjson3.writer;

/**
 * Android-compatible stub for ASMObjectWriterProvider.
 * On Android, ASM is not available, so this falls back to AUTO (reflection).
 */
public final class ASMObjectWriterProvider extends AbstractObjectWriterProvider {
    public static final ASMObjectWriterProvider INSTANCE = new ASMObjectWriterProvider();

    private static final com.alibaba.fastjson3.util.Logger.CategoryLogger LOG =
            com.alibaba.fastjson3.util.Logger.category("ASMWriterProvider");

    public ASMObjectWriterProvider() {
        super(null);
    }

    public ASMObjectWriterProvider(com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public WriterCreatorType getCreatorType() {
        return WriterCreatorType.ASM;
    }

    @Override
    protected com.alibaba.fastjson3.ObjectWriter<?> createWriter(Class<?> type) {
        // On Android, ASM is not available, delegate to AUTO (reflection)
        LOG.debug(() -> "ASM not available on Android for " + type.getName() + ", using reflection");
        return AutoObjectWriterProvider.INSTANCE.createWriter(type);
    }
}

package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;

/**
 * Android-compatible stub for ObjectWriterCreatorASM.
 * On Android, ASM is not available, so all methods throw LinkageError
 * to trigger fallback to reflection in ObjectMapper.
 */
public final class ObjectWriterCreatorASM {
    /**
     * Create an ObjectWriter using ASM (not available on Android).
     *
     * @param type the target type
     * @return never returns, always throws LinkageError
     * @throws LinkageError always, as ASM is not available on Android
     */
    public static ObjectWriter<?> createObjectWriter(final Class<?> type) {
        throw new LinkageError(
                "ASM is not available on Android, use reflection instead");
    }

    /** Private constructor to prevent instantiation. */
    private ObjectWriterCreatorASM() {
        throw new LinkageError("ASM is not available on Android");
    }
}

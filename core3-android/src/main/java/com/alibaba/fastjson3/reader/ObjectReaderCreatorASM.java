package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;

/**
 * Android-compatible stub for ObjectReaderCreatorASM.
 * On Android, ASM is not available, so all methods throw LinkageError
 * to trigger fallback to reflection in providers.
 */
public final class ObjectReaderCreatorASM {
    /**
     * Create an ObjectReader using ASM (not available on Android).
     *
     * @param <T> the type parameter
     * @param type the target type
     * @return never returns, always throws LinkageError
     * @throws LinkageError always, as ASM is not available on Android
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectReader<T> createObjectReader(
            final Class<T> type) {
        throw new LinkageError(
                "ASM is not available on Android, use reflection instead");
    }

    /** Private constructor to prevent instantiation. */
    private ObjectReaderCreatorASM() {
        throw new LinkageError("ASM is not available on Android");
    }
}

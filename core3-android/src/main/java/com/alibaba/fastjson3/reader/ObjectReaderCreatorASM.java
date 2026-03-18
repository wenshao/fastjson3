package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;

/**
 * Android-compatible stub for ObjectReaderCreatorASM.
 * On Android, ASM is not available, so all methods throw LinkageError
 * to trigger fallback to reflection in providers.
 */
public final class ObjectReaderCreatorASM {
    @SuppressWarnings("unchecked")
    public static <T> ObjectReader<T> createObjectReader(Class<T> type) {
        throw new LinkageError("ASM is not available on Android, use reflection instead");
    }

    private ObjectReaderCreatorASM() {
        throw new LinkageError("ASM is not available on Android");
    }
}

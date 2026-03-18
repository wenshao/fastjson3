package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectReader;

/**
 * Android-compatible stub for ObjectReaderCreatorASM.
 * On Android, ASM is not available, so all methods throw exceptions
 * indicating that reflection should be used instead.
 */
public final class ObjectReaderCreatorASM {
    @SuppressWarnings("unchecked")
    public static <T> ObjectReader<T> createObjectReader(Class<T> type) {
        throw new JSONException("ASM is not available on Android, use reflection instead");
    }

    private ObjectReaderCreatorASM() {
        throw new UnsupportedOperationException("ASM is not available on Android");
    }
}

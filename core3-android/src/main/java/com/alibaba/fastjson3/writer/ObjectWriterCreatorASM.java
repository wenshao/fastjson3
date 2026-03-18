package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectWriter;

/**
 * Android-compatible stub for ObjectWriterCreatorASM.
 * On Android, ASM is not available, so all methods throw exceptions
 * indicating that reflection should be used instead.
 */
public final class ObjectWriterCreatorASM {
    public static ObjectWriter<?> createObjectWriter(Class<?> type) {
        throw new JSONException("ASM is not available on Android, use reflection instead");
    }

    private ObjectWriterCreatorASM() {
        throw new UnsupportedOperationException("ASM is not available on Android");
    }
}

package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;

/**
 * Android-compatible stub for ObjectWriterCreatorASM.
 * On Android, ASM is not available, so all methods throw LinkageError
 * to trigger fallback to reflection in ObjectMapper.
 */
public final class ObjectWriterCreatorASM {
    public static ObjectWriter<?> createObjectWriter(Class<?> type) {
        throw new LinkageError("ASM is not available on Android, use reflection instead");
    }

    private ObjectWriterCreatorASM() {
        throw new LinkageError("ASM is not available on Android");
    }
}

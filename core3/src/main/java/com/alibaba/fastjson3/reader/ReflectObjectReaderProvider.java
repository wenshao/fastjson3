package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ObjectReaderProvider that uses only reflection.
 * No ASM bytecode generation.
 */
public final class ReflectObjectReaderProvider implements ObjectReaderProvider {

    public static final ReflectObjectReaderProvider INSTANCE = new ReflectObjectReaderProvider();

    private final ConcurrentMap<Class<?>, ObjectReader<?>> readerCache = new ConcurrentHashMap<>();

    private ReflectObjectReaderProvider() {
    }

    @Override
    public ReaderCreatorType getCreatorType() {
        return ReaderCreatorType.REFLECT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectReader<T> getObjectReader(Class<T> type) {
        return (ObjectReader<T>) readerCache.computeIfAbsent(type, t ->
            ObjectReaderCreator.createObjectReader(t)
        );
    }
}

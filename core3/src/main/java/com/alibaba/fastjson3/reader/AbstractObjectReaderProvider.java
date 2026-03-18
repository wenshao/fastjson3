package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base class for ObjectReaderProvider implementations.
 * Handles common functionality like caching, classloader management, and cleanup.
 */
public abstract class AbstractObjectReaderProvider implements ObjectReaderProvider {

    protected final ConcurrentMap<Class<?>, ObjectReader<?>> readerCache = new ConcurrentHashMap<>();
    protected final com.alibaba.fastjson3.util.DynamicClassLoader classLoader;
    protected final boolean shared;

    protected AbstractObjectReaderProvider(com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader
            : com.alibaba.fastjson3.util.DynamicClassLoader.getSharedInstance();
        this.shared = (this.classLoader == com.alibaba.fastjson3.util.DynamicClassLoader.getSharedInstance());
    }

    @Override
    public void cleanup() {
        // No-op for shared singleton - don't clear shared resources
        if (shared) {
            return;
        }
        readerCache.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectReader<T> getObjectReader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return (ObjectReader<T>) readerCache.computeIfAbsent(type, this::createReader);
    }

    /**
     * Create an ObjectReader for the given type.
     * Subclasses must implement this method.
     */
    protected abstract ObjectReader<?> createReader(Class<?> type);
}

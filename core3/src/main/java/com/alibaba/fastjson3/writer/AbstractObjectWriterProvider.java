package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base class for ObjectWriterProvider implementations.
 * Handles common functionality like caching, classloader management, and cleanup.
 */
public abstract class AbstractObjectWriterProvider implements ObjectWriterProvider {

    protected final ConcurrentMap<Class<?>, ObjectWriter<?>> writerCache = new ConcurrentHashMap<>();
    protected final com.alibaba.fastjson3.util.DynamicClassLoader classLoader;
    protected final boolean shared;

    protected AbstractObjectWriterProvider(com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
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
        writerCache.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectWriter<T> getObjectWriter(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return (ObjectWriter<T>) writerCache.computeIfAbsent(type, this::createWriter);
    }

    /**
     * Create an ObjectWriter for the given type.
     * Subclasses must implement this method.
     */
    protected abstract ObjectWriter<?> createWriter(Class<?> type);
}

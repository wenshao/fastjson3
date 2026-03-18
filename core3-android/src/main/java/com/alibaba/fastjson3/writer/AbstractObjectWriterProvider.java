package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base class for ObjectWriterProvider implementations (Android version).
 */
public abstract class AbstractObjectWriterProvider
        implements ObjectWriterProvider {
    /** Cache of ObjectWriter instances by type. */
    private final ConcurrentMap<Class<?>, ObjectWriter<?>> writerCache =
            new ConcurrentHashMap<>();
    /** ClassLoader for generating dynamic classes. */
    private final com.alibaba.fastjson3.util.DynamicClassLoader loader;
    /** Whether this provider is using the shared ClassLoader. */
    private final boolean shared;

    protected AbstractObjectWriterProvider(
            final com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        this.loader = classLoader != null ? classLoader
                : com.alibaba.fastjson3.util.DynamicClassLoader
                        .getSharedInstance();
        this.shared = (this.loader
                == com.alibaba.fastjson3.util.DynamicClassLoader
                        .getSharedInstance());
    }

    /**
     * Get the ClassLoader used by this provider.
     *
     * @return the ClassLoader
     */
    protected com.alibaba.fastjson3.util.DynamicClassLoader getClassLoader() {
        return loader;
    }

    @Override
    public final void cleanup() {
        // No-op for shared singleton
        if (shared) {
            return;
        }
        writerCache.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> ObjectWriter<T> getObjectWriter(final Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return (ObjectWriter<T>) writerCache.computeIfAbsent(
                type, this::createWriter);
    }

    /**
     * Create an ObjectWriter for the given type.
     * Subclasses must implement this method.
     *
     * @param type the type to create writer for
     * @return the ObjectWriter or null if cannot create
     */
    protected abstract ObjectWriter<?> createWriter(Class<?> type);
}

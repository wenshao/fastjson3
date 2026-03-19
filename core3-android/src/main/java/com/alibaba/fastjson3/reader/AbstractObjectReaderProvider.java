package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.util.DynamicClassLoader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base class for ObjectReaderProvider implementations (Android version).
 */
public abstract class AbstractObjectReaderProvider
        implements ObjectReaderProvider {
    /** Cache of ObjectReader instances by type. */
    private final ConcurrentMap<Class<?>, ObjectReader<?>> readerCache =
            new ConcurrentHashMap<>();
    /** ClassLoader for generating dynamic classes. */
    private final DynamicClassLoader loader;
    /** Whether this provider is using the shared ClassLoader. */
    private final boolean shared;

    protected AbstractObjectReaderProvider(
            final DynamicClassLoader classLoader) {
        this.loader = classLoader != null ? classLoader
                : DynamicClassLoader.getSharedInstance();
        this.shared = (this.loader
                == DynamicClassLoader.getSharedInstance());
    }

    /**
     * Get the ClassLoader used by this provider.
     *
     * @return the ClassLoader
     */
    protected DynamicClassLoader getClassLoader() {
        return loader;
    }

    @Override
    public final <T> ObjectReader<T> getObjectReader(final Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return (ObjectReader<T>) readerCache.computeIfAbsent(
                type, this::createReader);
    }

    /**
     * Create a new ObjectReader for the given type.
     * Subclasses must implement this method.
     *
     * @param type the type to create reader for
     * @return the ObjectReader or null if cannot create
     */
    protected abstract ObjectReader<?> createReader(Class<?> type);

    /**
     * Cleanup resources when this provider is no longer needed.
     * Only clears cache if this is a per-instance provider (not shared).
     */
    public void cleanup() {
        if (!shared) {
            readerCache.clear();
        }
    }
}

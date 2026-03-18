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

    /**
     * RAII-style context guard that ensures ThreadLocal cleanup.
     * Always call close() in a finally block.
     *
     * <p><b>Nested contexts:</b> When opening a nested context, the previous
     * context value is saved and restored on close, allowing proper nesting
     * of contexts from different providers.</p>
     */
    final class SafeContext implements AutoCloseable {
        /** Whether the context has been closed. */
        private boolean closed;
        /** The previous context provider to restore. */
        private final ObjectReaderProvider previousContext;

        SafeContext(final ObjectReaderProvider previous) {
            this.previousContext = previous;
        }

        @Override
        public void close() {
            if (!closed) {
                ObjectReaderProvider.CONTEXT.set(previousContext);
                closed = true;
            }
        }
    }
}

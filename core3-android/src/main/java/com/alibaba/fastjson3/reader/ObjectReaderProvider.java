package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.ObjectReader;

import java.util.function.Supplier;

/**
 * Provider for creating {@link ObjectReader} instances.
 * Allows customization of the reader creation strategy (ASM vs Reflection).
 *
 * @see ReaderCreatorType
 */
public interface ObjectReaderProvider {
    /**
     * Thread-local context for the current ObjectReaderProvider.
     */
    ThreadLocal<ObjectReaderProvider> CONTEXT = new ThreadLocal<>();

    /**
     * Set this provider as the context for nested ObjectReader creation.
     *
     * <p><b>Important:</b> Always call close() in a finally block:</p>
     *
     * @return the SafeContext for managing the provider context
     */
    default SafeContext openContext() {
        ObjectReaderProvider previous = CONTEXT.get();
        CONTEXT.set(this);
        return new SafeContext(previous);
    }

    /**
     * Execute an action with this provider as the context.
     *
     * @param <T> the result type
     * @param action the action to execute
     * @return the result of the action
     */
    default <T> T withContext(Supplier<T> action) {
        SafeContext ctx = openContext();
        try {
            return action.get();
        } finally {
            ctx.close();
        }
    }

    /**
     * Get the current context provider, or null if none is set.
     *
     * @return the current context provider
     */
    static ObjectReaderProvider getContext() {
        return CONTEXT.get();
    }

    /**
     * RAII-style context guard that ensures ThreadLocal cleanup.
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
                if (previousContext != null) {
                    CONTEXT.set(previousContext);
                } else {
                    CONTEXT.remove();
                }
                closed = true;
            }
        }
    }

    /**
     * Get or create an ObjectReader for the given type.
     *
     * @param <T> the type parameter
     * @param type the target type
     * @return the ObjectReader instance
     */
    <T> ObjectReader<T> getObjectReader(Class<T> type);

    /**
     * Get the creator type strategy.
     *
     * @return the creator type
     */
    ReaderCreatorType getCreatorType();

    /**
     * Cleanup resources when this provider is no longer needed.
     */
    default void cleanup() {
    }

    /**
     * Get the default provider (uses REFLECT strategy on Android).
     */
    static ObjectReaderProvider defaultProvider() {
        return ReflectObjectReaderProvider.INSTANCE;
    }
}

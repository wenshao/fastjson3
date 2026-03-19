package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;

import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * Provider for creating {@link ObjectWriter} instances.
 * Allows customization of the writer creation strategy (ASM vs Reflection).
 *
 * @see WriterCreatorType
 */
public interface ObjectWriterProvider {

    /**
     * Thread-local context for the current ObjectWriterProvider.
     *
     * <p><b>Memory leak prevention:</b> Always use try-finally or try-with-resources
     * pattern to ensure the ThreadLocal is cleared. The SafeContext wrapper guarantees
     * cleanup via its close() method.</p>
     *
     * <p><b>Note:</b> Using strong reference (not WeakReference) because:
     * <ul>
     *   <li>WeakReference could be cleared during action execution, breaking nested type creation</li>
     *   <li>The SafeContext pattern ensures cleanup via finally block</li>
     *   <li>ThreadLocal is only set during short-lived withContext() calls</li>
     * </ul></p>
     */
    ThreadLocal<ObjectWriterProvider> CONTEXT = new ThreadLocal<>();

    /**
     * Set this provider as the context for nested ObjectWriter creation.
     *
     * <p><b>Important:</b> Always call close() in a finally block:</p>
     * <pre>
     *     try (SafeContext ctx = provider.openContext()) {
     *         // nested operations that need this provider
     *         result = action.get();
     *     }
     *     // or
     *     SafeContext ctx = provider.openContext();
     *     try {
     *         // nested operations that need this provider
     *         result = action.get();
     *     } finally {
     *         ctx.close();
     *     }
     * </pre>
     *
     * <p><b>Nested contexts:</b> This method supports nested contexts by saving
     * and restoring the previous context value.</p>
     *
     * @return a SafeContext that must be closed when done
     */
    default SafeContext openContext() {
        ObjectWriterProvider previous = CONTEXT.get();
        CONTEXT.set(this);
        return new SafeContext(previous);
    }

    /**
     * Execute an action with this provider as the context.
     * The context is automatically cleared after the action completes.
     *
     * @param action the action to execute
     * @param <T>    the result type
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
     */
    static ObjectWriterProvider getContext() {
        return CONTEXT.get();
    }

    /**
     * RAII-style context guard that ensures ThreadLocal cleanup.
     * Always call close() in a finally block.
     *
     * <p><b>Nested contexts:</b> When opening a nested context, the previous
     * context value is saved and restored on close, allowing proper nesting
     * of contexts from different providers.</p>
     */
    class SafeContext implements AutoCloseable {
        private boolean closed;
        private final ObjectWriterProvider previous;

        SafeContext(final ObjectWriterProvider previousContext) {
            this.previous = previousContext;
        }

        @Override
        public void close() {
            if (!closed) {
                if (previous != null) {
                    CONTEXT.set(previous);
                } else {
                    CONTEXT.remove();
                }
                closed = true;
            }
        }
    }

    /**
     * Get or create an ObjectWriter for the given type.
     *
     * @param type the target type
     * @param <T>  the type parameter
     * @return the ObjectWriter instance
     */
    <T> ObjectWriter<T> getObjectWriter(Class<T> type);

    /**
     * Get or create an ObjectWriter for the given type.
     *
     * @param type the target type
     * @param <T>  the type parameter
     * @return the ObjectWriter instance
     */
    @SuppressWarnings("unchecked")
    default <T> ObjectWriter<T> getObjectWriter(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (type instanceof Class) {
            return getObjectWriter((Class<T>) type);
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    /**
     * Get the creator type strategy used by this provider.
     */
    WriterCreatorType getCreatorType();

    /**
     * Clean up resources to support ClassLoader unloading.
     *
     * <p>Clears all cached ObjectWriter instances and generated ASM classes.
     * Call this method when the provider is no longer needed to allow
     * the ClassLoader to be garbage collected.</p>
     *
     * <p>For shared providers (AUTO, ASM, REFLECT singletons), this method
     * is a no-op to avoid affecting other users. For provider instances
     * created per-ObjectMapper, this releases all resources.</p>
     */
    void cleanup();

    /**
     * Create a new provider with the specified creator type.
     *
     * @param creatorType the creator type strategy
     * @return a new provider instance
     */
    static ObjectWriterProvider of(WriterCreatorType creatorType) {
        return switch (creatorType) {
            case AUTO -> AutoObjectWriterProvider.INSTANCE;
            case ASM -> ASMObjectWriterProvider.INSTANCE;
            case REFLECT -> ReflectObjectWriterProvider.INSTANCE;
        };
    }

    /**
     * Get the default provider (uses AUTO strategy).
     */
    static ObjectWriterProvider defaultProvider() {
        return AutoObjectWriterProvider.INSTANCE;
    }
}

package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;

import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * Provider for creating {@link ObjectWriter} instances.
 * Allows customization of the writer creation strategy.
 *
 * @see WriterCreatorType
 */
public interface ObjectWriterProvider {
    /**
     * Thread-local context for the current ObjectWriterProvider.
     *
     * <p><b>Memory leak prevention:</b> Always use try-finally
     * or try-with-resources pattern to ensure the ThreadLocal
     * is cleared. The SafeContext wrapper guarantees cleanup
     * via its close() method.</p>
     *
     * <p><b>Note:</b> Using strong reference (not WeakReference):</p>
     * <ul>
     *   <li>WeakReference could be cleared during action execution</li>
     *   <li>The SafeContext pattern ensures cleanup via finally</li>
     *   <li>ThreadLocal is only set during short-lived calls</li>
     * </ul>
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
     * </pre>
     *
     * <p><b>Nested contexts:</b> This method supports nested contexts
     * by saving and restoring the previous context value.</p>
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
     * @param <T> the result type
     * @return the result of the action
     */
    default <T> T withContext(final Supplier<T> action) {
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
     * @return the current context provider or null
     */
    static ObjectWriterProvider getContext() {
        return CONTEXT.get();
    }

    /**
     * RAII-style context guard that ensures ThreadLocal cleanup.
     * Always call close() in a finally block.
     *
     * <p><b>Nested contexts:</b> When opening a nested context,
     * the previous context value is saved and restored on close.</p>
     */
    final class SafeContext implements AutoCloseable {
        /** Whether the context has been closed. */
        private boolean closed;
        /** The previous context provider to restore. */
        private final ObjectWriterProvider previousContext;

        SafeContext(final ObjectWriterProvider previous) {
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
     * Get or create an ObjectWriter for the given type.
     *
     * @param <T> the type parameter
     * @param type the target type
     * @return the ObjectWriter instance
     */
    <T> ObjectWriter<T> getObjectWriter(Class<T> type);

    /**
     * Get or create an ObjectWriter for the given type.
     *
     * @param <T> the type parameter
     * @param type the target type
     * @return the ObjectWriter instance
     */
    @SuppressWarnings("unchecked")
    default <T> ObjectWriter<T> getObjectWriter(final Type type) {
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
     *
     * @return the creator type
     */
    WriterCreatorType getCreatorType();

    /**
     * Clean up resources to support ClassLoader unloading.
     *
     * <p>Clears all cached ObjectWriter instances and generated ASM classes.
     * Call this method when the provider is no longer needed to allow
     * the ClassLoader to be garbage collected.</p>
     *
     * <p>For shared providers (AUTO, ASM, REFLECT singletons),
     * this method is a no-op.</p>
     */
    void cleanup();

    /**
     * Create a new provider with the specified creator type.
     *
     * @param creatorType the creator type strategy
     * @return a new provider instance
     */
    static ObjectWriterProvider of(final WriterCreatorType creatorType) {
        return switch (creatorType) {
            case AUTO -> AutoObjectWriterProvider.INSTANCE;
            case ASM -> {
                // On Android, ASM is not available, use AUTO instead
                try {
                    Class.forName("com.alibaba.fastjson3.internal.asm.ASMUtils");
                    // If we get here, we're on JVM with ASM available
                    yield new ASMObjectWriterProvider();
                } catch (final ClassNotFoundException e) {
                    // Android or restricted environment
                    yield AutoObjectWriterProvider.INSTANCE;
                }
            }
            case REFLECT -> ReflectObjectWriterProvider.INSTANCE;
        };
    }

    /**
     * Get the default provider (uses AUTO strategy).
     *
     * @return the default provider
     */
    static ObjectWriterProvider defaultProvider() {
        return AutoObjectWriterProvider.INSTANCE;
    }
}

package com.alibaba.fastjson3;

/**
 * Process-global accessor for the "default" {@link ObjectMapper} that
 * ecosystem integrations use when no explicit mapper is passed at
 * construction time.
 *
 * <p>Defaults to {@link ObjectMapper#shared()}. Frameworks that want to wire
 * a configured mapper as the process-wide default — typically Spring Boot's
 * {@code Fastjson3ObjectMapperAutoConfiguration} — call
 * {@link #set(ObjectMapper)} during startup.
 *
 * <p>Module no-arg constructors (Kafka serializer, JPA converter, JAX-RS
 * provider, MyBatis type handler, ...) read this holder via {@link #get()}.
 * That way a single setup of {@code spring.fastjson3.date-format=iso8601}
 * (or any other {@code ObjectMapper.Builder} configuration) propagates to
 * framework-managed converters that bypass the Spring container — JPA
 * converters instantiated by Hibernate via {@code @Convert(converter=...)},
 * MyBatis type handlers instantiated via {@code TypeHandlerRegistry},
 * Kafka deserializers instantiated via {@code VALUE_DESERIALIZER_CLASS_CONFIG},
 * JAX-RS providers picked up by classpath scan, etc.
 *
 * <p><b>Lifecycle</b>: install once at app startup, before any module's
 * no-arg constructor runs. The field is {@code volatile} for visibility
 * across threads. Re-installing later is allowed but produces inconsistent
 * state across already-instantiated converters — don't.
 *
 * <p><b>Test isolation</b>: tests that mutate the holder must call
 * {@link #reset()} in a tear-down hook. Otherwise the configured mapper
 * leaks into other tests in the same JVM.
 */
public final class Fastjson3MapperHolder {
    private static volatile ObjectMapper instance = ObjectMapper.shared();

    private Fastjson3MapperHolder() {
    }

    /**
     * Return the current process-default {@link ObjectMapper}. Initially
     * {@link ObjectMapper#shared()}; replaced by {@link #set(ObjectMapper)}.
     */
    public static ObjectMapper get() {
        return instance;
    }

    /**
     * Install a new process-default mapper. Called by Boot auto-config or
     * other framework startup hooks.
     *
     * @throws IllegalArgumentException if {@code mapper} is null
     */
    public static void set(ObjectMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        instance = mapper;
    }

    /**
     * Reset to {@link ObjectMapper#shared()}. Test-only.
     */
    public static void reset() {
        instance = ObjectMapper.shared();
    }
}

package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.Fastjson3MapperHolder;
import com.alibaba.fastjson3.ObjectMapper;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that exposes a single {@link ObjectMapper} bean
 * configured from {@link Fastjson3Properties}, shared across the servlet
 * converter and reactive codec auto-configurations. Mirrors Jackson's
 * {@code JacksonAutoConfiguration$JacksonObjectMapperConfiguration} —
 * users override the entire serialization chain by declaring their own
 * {@link ObjectMapper} bean.
 *
 * <p><b>Why a shared bean</b>: prior to this configuration, each
 * autoconfig built its own {@code ObjectMapper} from
 * {@code Fastjson3Properties.buildObjectMapper()} when a {@code dateFormat}
 * was set. With reactive (decoder + encoder) on the classpath that's
 * three identical mapper instances — non-trivial heap footprint per app.
 * Extracting the mapper as a bean collapses to one instance and gives
 * users the canonical override pattern.</p>
 *
 * <p><b>Why install into {@link Fastjson3MapperHolder}</b>: Spring DI
 * cannot reach the ecosystem converters that frameworks instantiate
 * themselves — Hibernate {@code @Convert(converter=Class)}, MyBatis
 * {@code TypeHandlerRegistry}, Kafka {@code VALUE_DESERIALIZER_CLASS_CONFIG}
 * (FQCN string), Jersey {@code @Provider} classpath scan, Vert.x
 * {@code JsonFactory} ServiceLoader, Retrofit programmatic factory,
 * gRPC per-method marshaller. Each of those calls {@code Fastjson3MapperHolder.get()}
 * in its no-arg ctor, so installing the Spring-resolved mapper into the
 * holder is what propagates a Spring user's {@code spring.fastjson3.*}
 * configuration (or their own {@code ObjectMapper} bean) to converters
 * Spring never sees.</p>
 */
@AutoConfiguration
@ConditionalOnClass(ObjectMapper.class)
@EnableConfigurationProperties(Fastjson3Properties.class)
public class Fastjson3ObjectMapperAutoConfiguration {
    /**
     * Shared {@link ObjectMapper} bean. Replaced by a user-supplied
     * {@code ObjectMapper} bean of any name (the
     * {@link ConditionalOnMissingBean} matches by type). When the user
     * provides their own mapper, {@code spring.fastjson3.date-format}
     * has no effect — the user is responsible for any desired format
     * configuration on their mapper.
     *
     * <p>When no property is set and no user mapper exists, returns
     * {@link ObjectMapper#shared()} — alloc-equivalent to the
     * pre-property no-arg constructor path; no per-app mapper allocation.</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper fastjson3ObjectMapper(Fastjson3Properties properties) {
        return properties.buildObjectMapper();
    }

    /**
     * Publishes the resolved {@link ObjectMapper} — ours or a user override —
     * into {@link Fastjson3MapperHolder} as the JVM-wide default for
     * framework-instantiated converters that bypass Spring DI.
     *
     * <p>Uses {@link SmartInitializingSingleton} so installation runs after
     * all singleton beans are constructed but before
     * {@code ContextRefreshedEvent} fires — early enough for
     * {@code @PostConstruct} hooks (e.g. JPA {@code EntityManagerFactory}
     * bootstrap) to see the configured mapper.</p>
     */
    @Bean
    SmartInitializingSingleton fastjson3MapperHolderInstaller(ObjectMapper fastjson3ObjectMapper) {
        return () -> Fastjson3MapperHolder.set(fastjson3ObjectMapper);
    }
}

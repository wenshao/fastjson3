package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.geojson.Fastjson3GeoJsonModule;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

/**
 * Auto-configuration that registers {@link Fastjson3GeoJsonModule} readers
 * and writers on the {@link ObjectMapper} bean exposed by
 * {@link Fastjson3ObjectMapperAutoConfiguration}.
 *
 * <p>Triggered when both {@link Fastjson3GeoJsonModule} and
 * {@link GeoJsonPoint} are on the classpath — i.e. the user has pulled in
 * {@code fastjson3-geojson} <em>and</em> some module that brings
 * {@code spring-data-mongodb} (typically
 * {@code spring-boot-starter-data-mongodb}).</p>
 *
 * <p>Registration runs as a {@link BeanPostProcessor} keyed on the bean
 * name {@code "fastjson3ObjectMapper"} — the auto-config default mapper.
 * That has two effects:</p>
 * <ul>
 *   <li><b>User-supplied mappers stay untouched.</b> When a user defines
 *       their own {@link ObjectMapper} bean (with any name), Spring Boot's
 *       {@link org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean}
 *       suppresses the auto-config bean — no bean named
 *       {@code "fastjson3ObjectMapper"} exists and the registrar skips
 *       silently. This keeps any {@code ObjectWriter}/{@code ObjectReader}
 *       the user already registered on their own mapper from being
 *       clobbered. To opt into geojson on a user mapper, call
 *       {@link Fastjson3GeoJsonModule#register(ObjectMapper)} explicitly.</li>
 *   <li><b>Registration co-locates with the holder install.</b> Both this
 *       BPP and {@code Fastjson3ObjectMapperAutoConfiguration}'s
 *       holder-installer BPP fire on the same trigger
 *       ({@code postProcessAfterInitialization} of the
 *       {@code "fastjson3ObjectMapper"} bean), in either order — both
 *       mutate the same mutable mapper instance, so the final state is
 *       deterministic regardless of BPP iteration order.</li>
 * </ul>
 *
 * <p>The bean is always a fresh per-app instance
 * ({@link Fastjson3Properties#buildObjectMapper()} no longer returns
 * {@link ObjectMapper#shared()}), so this mutation stays local to the
 * Spring context and never poisons the JVM-global shared mapper.</p>
 *
 * <p>To disable this auto-configuration entirely, set
 * {@code spring.autoconfigure.exclude=…Fastjson3GeoJsonAutoConfiguration}.</p>
 */
@AutoConfiguration(after = Fastjson3ObjectMapperAutoConfiguration.class)
@ConditionalOnClass({Fastjson3GeoJsonModule.class, GeoJsonPoint.class})
public class Fastjson3GeoJsonAutoConfiguration {
    /** The bean name targeted by the geojson registrar. */
    static final String AUTOCONFIG_MAPPER_BEAN_NAME = "fastjson3ObjectMapper";

    /**
     * BPP that registers GeoJSON readers/writers on the auto-config's
     * {@link ObjectMapper} bean immediately after it finishes initialization.
     * Filters by exact bean name so user-supplied mappers (which suppress
     * the auto-config via {@code @ConditionalOnMissingBean}) are untouched.
     *
     * <p>Declared {@code static} per Spring's {@link BeanPostProcessor}
     * contract — early phase BPPs must be instantiable without the rest of
     * the configuration class fully initialized.</p>
     */
    @Bean
    static BeanPostProcessor fastjson3GeoJsonRegistrar() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (AUTOCONFIG_MAPPER_BEAN_NAME.equals(beanName) && bean instanceof ObjectMapper) {
                    Fastjson3GeoJsonModule.register((ObjectMapper) bean);
                }
                return bean;
            }
        };
    }
}

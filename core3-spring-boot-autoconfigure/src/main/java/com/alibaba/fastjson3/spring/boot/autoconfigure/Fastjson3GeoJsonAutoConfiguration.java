package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.geojson.Fastjson3GeoJsonModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

/**
 * Auto-configuration that registers {@link Fastjson3GeoJsonModule} readers
 * and writers on the auto-config-built {@link ObjectMapper} bean exposed
 * by {@link Fastjson3ObjectMapperAutoConfiguration}.
 *
 * <p>Triggered when both {@link Fastjson3GeoJsonModule} and
 * {@link GeoJsonPoint} are on the classpath — i.e. the user has pulled in
 * {@code fastjson3-geojson} <em>and</em> some module that brings
 * {@code spring-data-mongodb} (typically
 * {@code spring-boot-starter-data-mongodb}).</p>
 *
 * <p>Registration runs as a {@link Fastjson3MapperCustomizer} that the
 * core auto-config applies during construction of its
 * {@code fastjson3ObjectMapper} bean. That has two consequences:</p>
 * <ul>
 *   <li><b>User-supplied mappers stay untouched.</b> When the user
 *       provides their own {@link ObjectMapper} bean (any name —
 *       including {@code "fastjson3ObjectMapper"}), Spring Boot's
 *       {@link org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean}
 *       suppresses the auto-config bean factory entirely, so customizers
 *       never run. Any {@code ObjectWriter}/{@code ObjectReader} the user
 *       has pre-registered on their own mapper is preserved. To opt into
 *       GeoJSON on a user mapper, call
 *       {@link Fastjson3GeoJsonModule#register(ObjectMapper)} explicitly
 *       inside the user's {@code @Bean} factory.</li>
 *   <li><b>Registration happens during the auto-config mapper's
 *       construction</b>, before the
 *       {@link Fastjson3ObjectMapperAutoConfiguration} holder-installer
 *       BPP runs and before any framework converter (JPA, Kafka, ...)
 *       captures the mapper reference — so framework converters see the
 *       GeoJSON-registered mapper deterministically.</li>
 * </ul>
 *
 * <p>The bean is always a fresh per-app instance
 * ({@link Fastjson3Properties#buildObjectMapper()} no longer returns
 * {@link ObjectMapper#shared()}), so registration stays local to the
 * Spring context and never poisons the JVM-global shared mapper.</p>
 *
 * <p>To disable this auto-configuration entirely, set
 * {@code spring.autoconfigure.exclude=…Fastjson3GeoJsonAutoConfiguration}.</p>
 */
@AutoConfiguration(after = Fastjson3ObjectMapperAutoConfiguration.class)
@ConditionalOnClass({Fastjson3GeoJsonModule.class, GeoJsonPoint.class})
public class Fastjson3GeoJsonAutoConfiguration {
    /**
     * Customizer that installs the GeoJSON readers/writers on the
     * auto-config {@link ObjectMapper} bean during its construction.
     */
    @Bean
    Fastjson3MapperCustomizer fastjson3GeoJsonCustomizer() {
        return Fastjson3GeoJsonModule::register;
    }
}

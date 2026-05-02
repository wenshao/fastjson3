package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.geojson.Fastjson3GeoJsonModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
 * <p>Registration runs during bean factory initialization (eagerly inside
 * the registrar's {@code @Bean} factory method), so the readers/writers
 * land on the mapper before
 * {@link Fastjson3ObjectMapperAutoConfiguration}'s
 * {@link org.springframework.beans.factory.SmartInitializingSingleton}
 * publishes the mapper into {@link com.alibaba.fastjson3.Fastjson3MapperHolder}.
 * Holder consumers (Hibernate, MyBatis, Kafka, Jersey, Vert.x, Retrofit,
 * gRPC) therefore see GeoJSON support automatically.</p>
 *
 * <p>Users wanting a different module-registration policy can either:</p>
 * <ul>
 *   <li>Provide their own {@link ObjectMapper} bean already configured with
 *       the registrations they want — this auto-config still runs, but
 *       repeated registration on the user mapper is a no-op for the same
 *       writer/reader pair, so it is safe.</li>
 *   <li>Disable this auto-configuration via
 *       {@code spring.autoconfigure.exclude=…Fastjson3GeoJsonAutoConfiguration}.</li>
 * </ul>
 */
@AutoConfiguration(after = Fastjson3ObjectMapperAutoConfiguration.class)
@ConditionalOnClass({Fastjson3GeoJsonModule.class, GeoJsonPoint.class})
@ConditionalOnBean(ObjectMapper.class)
public class Fastjson3GeoJsonAutoConfiguration {
    /**
     * Eagerly registers the GeoJSON readers/writers on the resolved
     * {@link ObjectMapper} bean. The marker return value pins the
     * registration into the bean lifecycle so Spring won't garbage-collect
     * the registrar before {@code SmartInitializingSingleton} runs.
     */
    @Bean
    Fastjson3GeoJsonRegistrar fastjson3GeoJsonRegistrar(ObjectMapper fastjson3ObjectMapper) {
        Fastjson3GeoJsonModule.register(fastjson3ObjectMapper);
        return new Fastjson3GeoJsonRegistrar();
    }

    /** Marker bean — see {@link #fastjson3GeoJsonRegistrar}. */
    public static final class Fastjson3GeoJsonRegistrar {
    }
}

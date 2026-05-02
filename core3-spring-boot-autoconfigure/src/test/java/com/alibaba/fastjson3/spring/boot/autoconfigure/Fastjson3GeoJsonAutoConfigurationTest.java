package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.Fastjson3MapperHolder;
import com.alibaba.fastjson3.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link Fastjson3GeoJsonAutoConfiguration} registers the
 * {@code Fastjson3GeoJsonModule} writers on the auto-config
 * {@link ObjectMapper} bean and that the registration also propagates
 * through {@link Fastjson3MapperHolder} to consumers that bypass Spring DI.
 */
class Fastjson3GeoJsonAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    Fastjson3ObjectMapperAutoConfiguration.class,
                    Fastjson3GeoJsonAutoConfiguration.class));

    @AfterEach
    void resetHolder() {
        Fastjson3MapperHolder.reset();
    }

    @Test
    void registersGeoJsonModule_onAutoConfigMapper() {
        runner.run(ctx -> {
            ObjectMapper mapper = ctx.getBean(ObjectMapper.class);
            // Pre-registration the auto-config mapper would emit GeoJsonPoint
            // via the default Spring-data Point writer (which fastjson3 has
            // none for, so it would fall back to bean serialization with x/y).
            // Post-registration we expect the canonical GeoJSON shape.
            String json = mapper.writeValueAsString(new GeoJsonPoint(1.0, 2.0));
            assertThat(json)
                    .as("GeoJsonPoint must serialize to canonical {type, coordinates}")
                    .contains("\"type\":\"Point\"")
                    .contains("\"coordinates\":[1.0,2.0]");
        });
    }

    @Test
    void registrationVisibleViaHolder() {
        runner.run(ctx -> {
            // The holder mapper is the same instance Spring resolved, so the
            // GeoJSON writer registration is observable through the path
            // framework-instantiated converters use.
            String json = Fastjson3MapperHolder.get()
                    .writeValueAsString(new GeoJsonPoint(3.0, 4.0));
            assertThat(json).contains("\"type\":\"Point\"")
                    .contains("\"coordinates\":[3.0,4.0]");
        });
    }

    @Test
    void customMapperGetsRegistration() {
        // With spring.fastjson3.date-format set, the auto-config builds
        // a distinct (non-shared) mapper. Verify geojson registration
        // still lands on the resolved mapper bean — not just on shared.
        runner.withPropertyValues("spring.fastjson3.date-format=yyyy-MM-dd")
                .run(ctx -> {
                    ObjectMapper bean = ctx.getBean(ObjectMapper.class);
                    assertThat(bean.getDateFormat()).isEqualTo("yyyy-MM-dd");
                    assertThat(bean.writeValueAsString(new GeoJsonPoint(7.0, 8.0)))
                            .contains("\"type\":\"Point\"")
                            .contains("\"coordinates\":[7.0,8.0]");
                });
    }
}

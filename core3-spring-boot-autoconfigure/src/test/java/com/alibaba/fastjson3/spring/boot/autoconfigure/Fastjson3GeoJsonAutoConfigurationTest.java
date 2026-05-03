package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.Fastjson3MapperHolder;
import com.alibaba.fastjson3.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
            assertThat(mapper)
                    .as("auto-config mapper must be a fresh instance, not shared() — "
                            + "registration on shared would mutate the JVM-global mapper")
                    .isNotSameAs(ObjectMapper.shared());
            // Post-registration we expect the canonical GeoJSON shape:
            // ONLY {type, coordinates} — the default bean serializer
            // would also emit x/y fields, so their absence proves the
            // custom writer is firing.
            String json = mapper.writeValueAsString(new GeoJsonPoint(1.0, 2.0));
            assertThat(json)
                    .as("GeoJsonPoint must serialize to canonical {type, coordinates}")
                    .contains("\"type\":\"Point\"")
                    .contains("\"coordinates\":[1.0,2.0]")
                    .doesNotContain("\"x\":")
                    .doesNotContain("\"y\":");
        });
    }

    @Test
    void registrationVisibleViaHolder() {
        runner.run(ctx -> {
            ObjectMapper bean = ctx.getBean(ObjectMapper.class);
            // Holder must publish the fresh, geojson-mutated bean — not
            // the default ObjectMapper.shared(). If the BeanPostProcessor
            // installer (PR #171) failed to fire, holder would still be
            // shared() and this assertion would fail.
            assertThat(Fastjson3MapperHolder.get())
                    .as("holder must hold the geojson-registered fresh instance")
                    .isSameAs(bean)
                    .isNotSameAs(ObjectMapper.shared());
            // Strong assertion: doesNotContain("\"x\":") proves the geojson
            // writer fired (default bean serializer would emit x/y).
            String json = Fastjson3MapperHolder.get()
                    .writeValueAsString(new GeoJsonPoint(3.0, 4.0));
            assertThat(json)
                    .contains("\"type\":\"Point\"")
                    .contains("\"coordinates\":[3.0,4.0]")
                    .doesNotContain("\"x\":")
                    .doesNotContain("\"y\":");
        });
    }

    @Test
    void userSuppliedMapper_isNotMutated() {
        // When the user provides their own ObjectMapper bean,
        // @ConditionalOnMissingBean suppresses the auto-config @Bean
        // factory entirely — customizers run inside that factory, so
        // the user's bean is untouched.
        runner.withUserConfiguration(UserMapperConfig.class).run(ctx -> {
            ObjectMapper bean = ctx.getBean(ObjectMapper.class);
            assertThat(bean).isSameAs(UserMapperConfig.USER);
            String json = bean.writeValueAsString(new GeoJsonPoint(11.0, 12.0));
            assertThat(json)
                    .as("user-supplied mapper must keep default bean serialization "
                            + "— customizers must not run on user beans")
                    .contains("\"x\":11.0")
                    .contains("\"y\":12.0");
        });
    }

    @Test
    void userSuppliedMapperWithCanonicalName_isNotMutated() {
        // R3 audit edge case: user names their bean
        // "fastjson3ObjectMapper" — the canonical auto-config name.
        // @ConditionalOnMissingBean still suppresses our @Bean factory
        // (matches by type), so customizers don't run on the user's
        // bean even though it shares the canonical name. A bean-name
        // -keyed BPP fix would have failed here; the customizer pattern
        // sidesteps the question entirely.
        runner.withUserConfiguration(CanonicalNameUserMapperConfig.class).run(ctx -> {
            ObjectMapper bean = ctx.getBean(ObjectMapper.class);
            assertThat(bean).isSameAs(CanonicalNameUserMapperConfig.CANONICAL);
            String json = bean.writeValueAsString(new GeoJsonPoint(13.0, 14.0));
            assertThat(json)
                    .as("user bean named 'fastjson3ObjectMapper' must still be "
                            + "untouched — customizers fire inside the auto-config "
                            + "factory, not against arbitrary beans of the same name")
                    .contains("\"x\":13.0")
                    .contains("\"y\":14.0");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class UserMapperConfig {
        static final ObjectMapper USER = ObjectMapper.builder().build();

        @Bean
        ObjectMapper userMapper() {
            return USER;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CanonicalNameUserMapperConfig {
        static final ObjectMapper CANONICAL = ObjectMapper.builder().build();

        @Bean(name = "fastjson3ObjectMapper")
        ObjectMapper fastjson3ObjectMapper() {
            return CANONICAL;
        }
    }

    @Test
    void customMapperGetsRegistration() {
        // With spring.fastjson3.date-format set, the auto-config builds
        // a distinct (non-shared) mapper. Verify geojson registration
        // still lands on the resolved mapper bean and the date-format
        // setting is preserved (registration is purely additive).
        runner.withPropertyValues("spring.fastjson3.date-format=yyyy-MM-dd")
                .run(ctx -> {
                    ObjectMapper bean = ctx.getBean(ObjectMapper.class);
                    assertThat(bean.getDateFormat()).isEqualTo("yyyy-MM-dd");
                    assertThat(bean.writeValueAsString(new GeoJsonPoint(7.0, 8.0)))
                            .contains("\"type\":\"Point\"")
                            .contains("\"coordinates\":[7.0,8.0]");
                });
    }

    @Test
    void sharedMapperUnaffected() {
        // The buildObjectMapper "always fresh" contract guarantees the
        // JVM-global ObjectMapper.shared() never sees geojson registration,
        // even after the auto-config has run. Pin the boundary — if a
        // future change re-introduces the shared-singleton optimization,
        // this test fails.
        //
        // Note: GeoJsonPoint inherently has 'type', 'coordinates', 'x', 'y'
        // bean properties, so the default fastjson3 bean serializer emits
        // all four. The custom geojson writer collapses to {type,
        // coordinates} ONLY. Assert on the presence of bean-only fields
        // (x, y) — they're absent from the canonical writer's output but
        // present in the default bean serializer's output.
        runner.run(ctx -> {
            ctx.getBean(ObjectMapper.class);
            String sharedJson = ObjectMapper.shared()
                    .writeValueAsString(new GeoJsonPoint(9.0, 10.0));
            assertThat(sharedJson)
                    .as("ObjectMapper.shared() must keep default bean serialization "
                            + "(x/y fields present) — geojson writer registration "
                            + "would suppress those")
                    .contains("\"x\":9.0")
                    .contains("\"y\":10.0");
        });
    }
}

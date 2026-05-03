package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.Fastjson3MapperHolder;
import com.alibaba.fastjson3.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link Fastjson3ObjectMapperAutoConfiguration} publishes the
 * resolved {@link ObjectMapper} bean — auto-config default or user override —
 * into {@link Fastjson3MapperHolder} so framework-instantiated converters
 * (Hibernate {@code @Convert}, MyBatis, Kafka, Jersey, Vert.x, Retrofit, gRPC)
 * pick up the same mapper Spring beans see.
 */
class Fastjson3ObjectMapperAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Fastjson3ObjectMapperAutoConfiguration.class));

    @AfterEach
    void resetHolder() {
        // Reset the JVM-global holder so leakage from one test cannot
        // mask a missed installation in the next.
        Fastjson3MapperHolder.reset();
    }

    @Test
    void installsAutoConfigMapper_intoHolder() {
        // Force a non-shared mapper via date-format. Without it, the bean
        // would be ObjectMapper.shared() and the holder default would be
        // ObjectMapper.shared() — assertion would pass even if the
        // installer never fired. Setting the property forces a distinct
        // instance, so the assertion proves the installer ran.
        runner.withPropertyValues("spring.fastjson3.date-format=yyyy-MM-dd")
                .run(ctx -> {
                    ObjectMapper bean = ctx.getBean(ObjectMapper.class);
                    assertThat(bean).isNotSameAs(ObjectMapper.shared());
                    assertThat(Fastjson3MapperHolder.get())
                            .as("holder must hold the same instance Spring resolved")
                            .isSameAs(bean);
                });
    }

    @Test
    void installsUserMapper_intoHolder() {
        runner.withUserConfiguration(UserMapperConfig.class).run(ctx -> {
            assertThat(ctx.getBean(ObjectMapper.class)).isSameAs(UserMapperConfig.USER);
            assertThat(Fastjson3MapperHolder.get())
                    .as("user-supplied ObjectMapper must override the holder default")
                    .isSameAs(UserMapperConfig.USER);
        });
    }

    @Test
    void appliesDateFormatProperty_toHolderMapper() {
        runner.withPropertyValues("spring.fastjson3.date-format=yyyy-MM-dd")
                .run(ctx -> {
                    // Verify holder is the SAME instance as the bean (not a
                    // separately-built mapper that happens to share the same
                    // dateFormat) — pins the cross-bean propagation contract.
                    ObjectMapper bean = ctx.getBean(ObjectMapper.class);
                    ObjectMapper holder = Fastjson3MapperHolder.get();
                    assertThat(holder).isSameAs(bean);
                    assertThat(holder.getDateFormat()).isEqualTo("yyyy-MM-dd");
                });
    }

    @Test
    void resolvesUserMapperByType_notByName() {
        // Spring resolves the BeanPostProcessor's installer dependency by
        // type, not by parameter name. A user-supplied bean named anything
        // (here: 'myCustomMapper') still gets installed.
        runner.withUserConfiguration(NamedUserMapperConfig.class).run(ctx -> {
            assertThat(ctx.getBeanNamesForType(ObjectMapper.class))
                    .as("only the user mapper bean is resolved, not the auto-config default")
                    .containsExactly("myCustomMapper");
            assertThat(Fastjson3MapperHolder.get()).isSameAs(NamedUserMapperConfig.NAMED);
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
    static class NamedUserMapperConfig {
        static final ObjectMapper NAMED = ObjectMapper.builder().build();

        @Bean(name = "myCustomMapper")
        ObjectMapper myCustomMapper() {
            return NAMED;
        }
    }
}

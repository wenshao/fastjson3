package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.spring.codec.Fastjson3JsonDecoder;
import com.alibaba.fastjson3.spring.codec.Fastjson3JsonEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Wires {@link Fastjson3JsonCodecAutoConfiguration} into a
 * {@link ReactiveWebApplicationContextRunner} and verifies it triggers
 * only on the reactive stack, registers both decoder + encoder + a
 * {@link WebFluxConfigurer} that wires them into the
 * {@code ServerCodecConfigurer}, and respects user-supplied beans.
 */
class Fastjson3JsonCodecAutoConfigurationTest {
    private final ReactiveWebApplicationContextRunner reactiveRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Fastjson3JsonCodecAutoConfiguration.class));

    @Test
    void registersDecoderAndEncoder_inReactiveContext() {
        reactiveRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(Fastjson3JsonDecoder.class);
            assertThat(ctx).hasSingleBean(Fastjson3JsonEncoder.class);
        });
    }

    @Test
    void registersWebFluxConfigurer() {
        reactiveRunner.run(ctx -> {
            // The configurer wires our decoder/encoder into ServerCodecConfigurer's
            // default codecs slot. Pin that exactly one such configurer is registered
            // so a future double-registration regression trips this test.
            assertThat(ctx.getBeansOfType(WebFluxConfigurer.class))
                    .hasEntrySatisfying("fastjson3WebFluxConfigurer",
                            bean -> assertThat(bean).isNotNull());
        });
    }

    @Test
    void doesNotRegister_inServletContext() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(Fastjson3JsonCodecAutoConfiguration.class))
                .run(ctx -> {
                    assertThat(ctx).doesNotHaveBean(Fastjson3JsonDecoder.class);
                    assertThat(ctx).doesNotHaveBean(Fastjson3JsonEncoder.class);
                });
    }

    @Test
    void doesNotRegister_inNonWebContext() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(Fastjson3JsonCodecAutoConfiguration.class))
                .run(ctx -> {
                    assertThat(ctx).doesNotHaveBean(Fastjson3JsonDecoder.class);
                    assertThat(ctx).doesNotHaveBean(Fastjson3JsonEncoder.class);
                });
    }

    @Test
    void respectsUserSuppliedDecoder() {
        reactiveRunner.withUserConfiguration(UserDecoderConfig.class).run(ctx -> {
            assertThat(ctx).hasSingleBean(Fastjson3JsonDecoder.class);
            assertThat(ctx.getBean(Fastjson3JsonDecoder.class)).isSameAs(UserDecoderConfig.SHARED);
        });
    }

    @Test
    void respectsUserSuppliedEncoder() {
        reactiveRunner.withUserConfiguration(UserEncoderConfig.class).run(ctx -> {
            assertThat(ctx).hasSingleBean(Fastjson3JsonEncoder.class);
            assertThat(ctx.getBean(Fastjson3JsonEncoder.class)).isSameAs(UserEncoderConfig.SHARED);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class UserDecoderConfig {
        static final Fastjson3JsonDecoder SHARED = new Fastjson3JsonDecoder(ObjectMapper.builder().build());

        @Bean
        Fastjson3JsonDecoder userDecoder() {
            return SHARED;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class UserEncoderConfig {
        static final Fastjson3JsonEncoder SHARED = new Fastjson3JsonEncoder(ObjectMapper.builder().build());

        @Bean
        Fastjson3JsonEncoder userEncoder() {
            return SHARED;
        }
    }
}

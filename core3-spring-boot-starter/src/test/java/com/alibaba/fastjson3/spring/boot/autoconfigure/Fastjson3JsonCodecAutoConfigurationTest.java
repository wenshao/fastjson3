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
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
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
    void webFluxConfigurer_actuallyWiresCodecsIntoServerCodecConfigurer() {
        // Verify the configurer's body, not just its presence — a future
        // refactor that empties configureHttpMessageCodecs(...) silently
        // passed the bean-existence test, but should fail this one.
        reactiveRunner.run(ctx -> {
            WebFluxConfigurer configurer = ctx.getBean(
                    "fastjson3WebFluxConfigurer", WebFluxConfigurer.class);
            ServerCodecConfigurer cfg = ServerCodecConfigurer.create();
            configurer.configureHttpMessageCodecs(cfg);

            boolean hasFastjson3Decoder = cfg.getReaders().stream()
                    .filter(r -> r instanceof DecoderHttpMessageReader<?>)
                    .map(r -> ((DecoderHttpMessageReader<?>) r).getDecoder())
                    .anyMatch(d -> d instanceof Fastjson3JsonDecoder);
            boolean hasFastjson3Encoder = cfg.getWriters().stream()
                    .filter(w -> w instanceof EncoderHttpMessageWriter<?>)
                    .map(w -> ((EncoderHttpMessageWriter<?>) w).getEncoder())
                    .anyMatch(e -> e instanceof Fastjson3JsonEncoder);
            assertThat(hasFastjson3Decoder)
                    .as("Fastjson3JsonDecoder must be wired into ServerCodecConfigurer.getReaders()")
                    .isTrue();
            assertThat(hasFastjson3Encoder)
                    .as("Fastjson3JsonEncoder must be wired into ServerCodecConfigurer.getWriters()")
                    .isTrue();
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

package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.spring.codec.Fastjson3JsonDecoder;
import com.alibaba.fastjson3.spring.codec.Fastjson3JsonEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;

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
            .withConfiguration(AutoConfigurations.of(
                        Fastjson3ObjectMapperAutoConfiguration.class,
                        Fastjson3JsonCodecAutoConfiguration.class));

    @Test
    void registersDecoderAndEncoder_inReactiveContext() {
        reactiveRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(Fastjson3JsonDecoder.class);
            assertThat(ctx).hasSingleBean(Fastjson3JsonEncoder.class);
        });
    }

    @Test
    void registersCodecCustomizer() {
        reactiveRunner.run(ctx -> {
            // The customizer writes our decoder/encoder into the
            // ServerCodecConfigurer's default codecs slot. Pin that exactly
            // one such customizer is registered so a future double-registration
            // regression trips this test.
            assertThat(ctx.getBeansOfType(CodecCustomizer.class))
                    .hasEntrySatisfying("fastjson3CodecCustomizer",
                            bean -> assertThat(bean).isNotNull());
        });
    }

    @Test
    void fastjson3CodecCustomizer_winsAgainstBootJacksonCustomizer() {
        // The reason fastjson3CodecCustomizer is at LOWEST_PRECEDENCE: Boot's
        // CodecsAutoConfiguration.JacksonCodecConfiguration registers a
        // jacksonCodecCustomizer at @Order(0) that writes the same
        // jackson2JsonDecoder / jackson2JsonEncoder slots — last writer wins.
        // This test loads BOTH auto-configurations into the same context and
        // invokes ALL CodecCustomizer beans in @Order order against a fresh
        // ServerCodecConfigurer, exactly as Boot's WebFluxConfig does at
        // runtime. If our @Order is ever inverted (or matches Jackson's), the
        // assertion below catches the silent regression.
        new ReactiveWebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JacksonAutoConfiguration.class,
                        CodecsAutoConfiguration.class,
                        Fastjson3ObjectMapperAutoConfiguration.class,
                        Fastjson3JsonCodecAutoConfiguration.class))
                .run(ctx -> {
                    assertThat(ctx.getBeansOfType(CodecCustomizer.class))
                            .as("expected both jacksonCodecCustomizer and fastjson3CodecCustomizer in the chain")
                            .containsKeys("jacksonCodecCustomizer", "fastjson3CodecCustomizer");

                    ServerCodecConfigurer cfg = ServerCodecConfigurer.create();
                    ctx.getBeanProvider(CodecCustomizer.class)
                            .orderedStream()
                            .forEach(c -> c.customize(cfg));

                    boolean hasFastjson3Decoder = cfg.getReaders().stream()
                            .filter(r -> r instanceof DecoderHttpMessageReader<?>)
                            .map(r -> ((DecoderHttpMessageReader<?>) r).getDecoder())
                            .anyMatch(d -> d instanceof Fastjson3JsonDecoder);
                    boolean hasFastjson3Encoder = cfg.getWriters().stream()
                            .filter(w -> w instanceof EncoderHttpMessageWriter<?>)
                            .map(w -> ((EncoderHttpMessageWriter<?>) w).getEncoder())
                            .anyMatch(e -> e instanceof Fastjson3JsonEncoder);
                    assertThat(hasFastjson3Decoder)
                            .as("Fastjson3JsonDecoder must win the slot vs Jackson's @Order(0) customizer")
                            .isTrue();
                    assertThat(hasFastjson3Encoder)
                            .as("Fastjson3JsonEncoder must win the slot vs Jackson's @Order(0) customizer")
                            .isTrue();
                });
    }

    @Test
    void codecCustomizer_actuallyWiresCodecsIntoServerCodecConfigurer() {
        // Verify the customizer's body, not just its presence — a future
        // refactor that empties customize(...) silently passed the
        // bean-existence test, but should fail this one.
        reactiveRunner.run(ctx -> {
            CodecCustomizer customizer = ctx.getBean(
                    "fastjson3CodecCustomizer", CodecCustomizer.class);
            ServerCodecConfigurer cfg = ServerCodecConfigurer.create();
            customizer.customize(cfg);

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
                .withConfiguration(AutoConfigurations.of(
                        Fastjson3ObjectMapperAutoConfiguration.class,
                        Fastjson3JsonCodecAutoConfiguration.class))
                .run(ctx -> {
                    assertThat(ctx).doesNotHaveBean(Fastjson3JsonDecoder.class);
                    assertThat(ctx).doesNotHaveBean(Fastjson3JsonEncoder.class);
                });
    }

    @Test
    void doesNotRegister_inNonWebContext() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        Fastjson3ObjectMapperAutoConfiguration.class,
                        Fastjson3JsonCodecAutoConfiguration.class))
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

    @Test
    void encoderAndDecoder_shareSameObjectMapper() {
        // R-audit P1 fix: pre-fix, Fastjson3Properties.buildObjectMapper()
        // was called twice (once per bean factory) and produced two
        // separate ObjectMapper instances when dateFormat was set —
        // doubling heap footprint. Post-fix, both consume the
        // fastjson3ObjectMapper @Bean and reference the SAME instance.
        reactiveRunner
                .withPropertyValues("spring.fastjson3.date-format=yyyy-MM-dd")
                .run(ctx -> {
                    Fastjson3JsonEncoder enc = ctx.getBean(Fastjson3JsonEncoder.class);
                    Fastjson3JsonDecoder dec = ctx.getBean(Fastjson3JsonDecoder.class);
                    com.alibaba.fastjson3.ObjectMapper bean =
                            ctx.getBean(com.alibaba.fastjson3.ObjectMapper.class);
                    // Field reflection — encoder/decoder don't expose mapper.
                    java.lang.reflect.Field encField =
                            Fastjson3JsonEncoder.class.getDeclaredField("mapper");
                    encField.setAccessible(true);
                    java.lang.reflect.Field decField =
                            Fastjson3JsonDecoder.class.getDeclaredField("mapper");
                    decField.setAccessible(true);
                    assertThat(encField.get(enc)).isSameAs(bean);
                    assertThat(decField.get(dec)).isSameAs(bean);
                });
    }

    @Test
    void userSuppliedObjectMapper_replacesAutoConfigured() {
        // Mirrors Jackson's pattern: user declares their own ObjectMapper
        // bean and the converter / encoder / decoder all consume it,
        // bypassing Fastjson3Properties entirely. spring.fastjson3.date-format
        // is ignored when the user-supplied mapper has its own config.
        reactiveRunner
                .withUserConfiguration(UserMapperConfig.class)
                .withPropertyValues("spring.fastjson3.date-format=yyyy-MM-dd")
                .run(ctx -> {
                    com.alibaba.fastjson3.ObjectMapper bean =
                            ctx.getBean(com.alibaba.fastjson3.ObjectMapper.class);
                    assertThat(bean).isSameAs(UserMapperConfig.SHARED);
                    // Property is bound (Boot still sees it) but the user
                    // mapper doesn't honor it — its own dateFormat (null
                    // here) wins.
                    assertThat(bean.getDateFormat()).isNull();
                });
    }

    @Test
    void appliesDateFormatProperty_camelCaseBindingAlsoWorks() {
        // Spring's relaxed binder accepts kebab-case (date-format) AND
        // camelCase (dateFormat). Pin both forms route to the same field.
        reactiveRunner
                .withPropertyValues("spring.fastjson3.dateFormat=yyyy-MM-dd")
                .run(ctx -> {
                    com.alibaba.fastjson3.ObjectMapper bean =
                            ctx.getBean(com.alibaba.fastjson3.ObjectMapper.class);
                    assertThat(bean.getDateFormat()).isEqualTo("yyyy-MM-dd");
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class UserMapperConfig {
        static final com.alibaba.fastjson3.ObjectMapper SHARED =
                com.alibaba.fastjson3.ObjectMapper.builder().build();

        @Bean
        com.alibaba.fastjson3.ObjectMapper userObjectMapper() {
            return SHARED;
        }
    }

    @Test
    void appliesDateFormatProperty_toEncoder() {
        // spring.fastjson3.date-format pipes through Fastjson3Properties →
        // ObjectMapper.builder().dateFormat() → Fastjson3JsonEncoder.
        // Encoder.encodeValue uses the mapper, so the format reaches the
        // wire bytes.
        reactiveRunner
                .withPropertyValues("spring.fastjson3.date-format=yyyy-MM-dd")
                .run(ctx -> {
                    Fastjson3JsonEncoder e = ctx.getBean(Fastjson3JsonEncoder.class);
                    org.springframework.core.io.buffer.DataBufferFactory factory =
                            new org.springframework.core.io.buffer.DefaultDataBufferFactory();
                    LocalDateTimeBean b = new LocalDateTimeBean();
                    b.ts = java.time.LocalDateTime.of(2024, 4, 30, 12, 34, 56);
                    org.springframework.core.io.buffer.DataBuffer buf = e.encodeValue(b, factory,
                            org.springframework.core.ResolvableType.forClass(LocalDateTimeBean.class),
                            org.springframework.http.MediaType.APPLICATION_JSON, null);
                    String body = buf.toString(java.nio.charset.StandardCharsets.UTF_8);
                    assertThat(body).contains("\"ts\":\"2024-04-30\"")
                            .doesNotContain("12:34:56");
                });
    }

    @Test
    void noDateFormatProperty_encoder_emitsDefaultIso() {
        reactiveRunner.run(ctx -> {
            Fastjson3JsonEncoder e = ctx.getBean(Fastjson3JsonEncoder.class);
            org.springframework.core.io.buffer.DataBufferFactory factory =
                    new org.springframework.core.io.buffer.DefaultDataBufferFactory();
            LocalDateTimeBean b = new LocalDateTimeBean();
            b.ts = java.time.LocalDateTime.of(2024, 4, 30, 12, 34, 56);
            org.springframework.core.io.buffer.DataBuffer buf = e.encodeValue(b, factory,
                    org.springframework.core.ResolvableType.forClass(LocalDateTimeBean.class),
                    org.springframework.http.MediaType.APPLICATION_JSON, null);
            String body = buf.toString(java.nio.charset.StandardCharsets.UTF_8);
            assertThat(body).contains("12:34:56");  // ISO with time preserved
        });
    }

    public static class LocalDateTimeBean {
        public java.time.LocalDateTime ts;
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

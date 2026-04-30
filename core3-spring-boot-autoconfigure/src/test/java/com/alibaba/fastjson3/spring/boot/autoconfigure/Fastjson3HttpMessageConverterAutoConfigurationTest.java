package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.spring.Fastjson3HttpMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Wires {@link Fastjson3HttpMessageConverterAutoConfiguration} into a
 * {@link WebApplicationContextRunner} (servlet) and verifies it triggers
 * only on the servlet stack, respects user-supplied beans, and lands in
 * the {@link HttpMessageConverters} chain at position 0 (ahead of any
 * default Jackson converter Boot would add).
 */
class Fastjson3HttpMessageConverterAutoConfigurationTest {
    private final WebApplicationContextRunner servletRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    Fastjson3ObjectMapperAutoConfiguration.class,
                    Fastjson3HttpMessageConverterAutoConfiguration.class,
                    HttpMessageConvertersAutoConfiguration.class));

    @Test
    void registersConverter_inServletContext() {
        servletRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(Fastjson3HttpMessageConverter.class);
        });
    }

    @Test
    void converter_strictlyPrecedesJackson_inChain() {
        servletRunner.run(ctx -> {
            HttpMessageConverters chain = ctx.getBean(HttpMessageConverters.class);
            List<HttpMessageConverter<?>> converters = chain.getConverters();
            // Pin "fastjson3 strictly precedes any Jackson-flavored
            // HttpMessageConverter" — stronger than "fastjson3 is the
            // first JSON-capable converter", which would also pass with
            // a Jackson converter sitting at index 1.
            int fjIdx = -1;
            int jacksonIdx = -1;
            for (int i = 0; i < converters.size(); i++) {
                HttpMessageConverter<?> c = converters.get(i);
                if (c instanceof Fastjson3HttpMessageConverter) {
                    if (fjIdx == -1) {
                        fjIdx = i;
                    }
                } else if (c.getClass().getName().contains("MappingJackson")) {
                    if (jacksonIdx == -1) {
                        jacksonIdx = i;
                    }
                }
            }
            assertThat(fjIdx).as("fastjson3 converter must be in the chain").isGreaterThanOrEqualTo(0);
            // Jackson may be absent from the test context (no transitive
            // jackson-databind on the classpath); only enforce ordering
            // when both are present.
            if (jacksonIdx >= 0) {
                assertThat(fjIdx)
                        .as("fastjson3 (idx %d) must precede Jackson (idx %d)", fjIdx, jacksonIdx)
                        .isLessThan(jacksonIdx);
            }
        });
    }

    @Test
    void doesNotRegister_inReactiveContext() {
        new ReactiveWebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        Fastjson3ObjectMapperAutoConfiguration.class,
                        Fastjson3HttpMessageConverterAutoConfiguration.class))
                .run(ctx -> assertThat(ctx).doesNotHaveBean(Fastjson3HttpMessageConverter.class));
    }

    @Test
    void doesNotRegister_inNonWebContext() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        Fastjson3ObjectMapperAutoConfiguration.class,
                        Fastjson3HttpMessageConverterAutoConfiguration.class))
                .run(ctx -> assertThat(ctx).doesNotHaveBean(Fastjson3HttpMessageConverter.class));
    }

    @Test
    void respectsUserSuppliedConverter() {
        servletRunner.withUserConfiguration(UserConverterConfig.class).run(ctx -> {
            assertThat(ctx).hasSingleBean(Fastjson3HttpMessageConverter.class);
            assertThat(ctx.getBean(Fastjson3HttpMessageConverter.class))
                    .isSameAs(UserConverterConfig.SHARED);
            // Close the loop on the documented "drop in a custom
            // ObjectMapper" workflow — verify the user's bean is what
            // Boot actually wires into the converter chain.
            HttpMessageConverters chain = ctx.getBean(HttpMessageConverters.class);
            assertThat(chain.getConverters()).contains(UserConverterConfig.SHARED);
        });
    }

    @Test
    void appliesDateFormatProperty() {
        // spring.fastjson3.date-format pipes through Fastjson3Properties →
        // ObjectMapper.builder().dateFormat() → Fastjson3HttpMessageConverter.
        // Verify by exercising the converter on a Bean with a LocalDateTime
        // field; mapper-level format must be observed in the bytes written.
        servletRunner
                .withPropertyValues("spring.fastjson3.date-format=yyyy-MM-dd")
                .run(ctx -> {
                    Fastjson3HttpMessageConverter c =
                            ctx.getBean(Fastjson3HttpMessageConverter.class);
                    org.springframework.mock.http.MockHttpOutputMessage out =
                            new org.springframework.mock.http.MockHttpOutputMessage();
                    LocalDateTimeBean b = new LocalDateTimeBean();
                    b.ts = java.time.LocalDateTime.of(2024, 4, 30, 12, 34, 56);
                    c.write(b, org.springframework.http.MediaType.APPLICATION_JSON, out);
                    String body = out.getBodyAsString();
                    assertThat(body).contains("\"ts\":\"2024-04-30\"")
                            .doesNotContain("12:34:56");
                });
    }

    @Test
    void noDateFormatProperty_keepsSharedMapperBehavior() {
        // No property set → Fastjson3Properties.buildObjectMapper returns
        // ObjectMapper.shared() so the converter behaves identically to
        // pre-property no-arg constructor (default ISO emit, time component
        // preserved on a LocalDateTime).
        servletRunner.run(ctx -> {
            Fastjson3HttpMessageConverter c =
                    ctx.getBean(Fastjson3HttpMessageConverter.class);
            org.springframework.mock.http.MockHttpOutputMessage out =
                    new org.springframework.mock.http.MockHttpOutputMessage();
            LocalDateTimeBean b = new LocalDateTimeBean();
            b.ts = java.time.LocalDateTime.of(2024, 4, 30, 12, 34, 56);
            c.write(b, org.springframework.http.MediaType.APPLICATION_JSON, out);
            String body = out.getBodyAsString();
            assertThat(body).contains("12:34:56");  // ISO with time preserved
        });
    }

    public static class LocalDateTimeBean {
        public java.time.LocalDateTime ts;
    }

    @Configuration(proxyBeanMethods = false)
    static class UserConverterConfig {
        static final Fastjson3HttpMessageConverter SHARED =
                new Fastjson3HttpMessageConverter(ObjectMapper.builder().build());

        @Bean
        Fastjson3HttpMessageConverter userConverter() {
            return SHARED;
        }
    }
}

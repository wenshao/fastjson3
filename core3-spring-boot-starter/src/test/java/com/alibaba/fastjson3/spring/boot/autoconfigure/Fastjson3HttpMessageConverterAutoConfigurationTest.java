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
                    Fastjson3HttpMessageConverterAutoConfiguration.class,
                    HttpMessageConvertersAutoConfiguration.class));

    @Test
    void registersConverter_inServletContext() {
        servletRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(Fastjson3HttpMessageConverter.class);
        });
    }

    @Test
    void converterIsFirstInChain_aheadOfJackson() {
        servletRunner.run(ctx -> {
            HttpMessageConverters chain = ctx.getBean(HttpMessageConverters.class);
            List<HttpMessageConverter<?>> converters = chain.getConverters();
            // Spring Boot orders user-defined HttpMessageConverter beans
            // ahead of its defaults; pin that the fastjson3 converter is
            // the JSON handler the chain reaches first.
            HttpMessageConverter<?> first = converters.stream()
                    .filter(c -> c.canWrite(java.util.HashMap.class, org.springframework.http.MediaType.APPLICATION_JSON))
                    .findFirst()
                    .orElseThrow();
            assertThat(first).isInstanceOf(Fastjson3HttpMessageConverter.class);
        });
    }

    @Test
    void doesNotRegister_inReactiveContext() {
        new ReactiveWebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        Fastjson3HttpMessageConverterAutoConfiguration.class))
                .run(ctx -> assertThat(ctx).doesNotHaveBean(Fastjson3HttpMessageConverter.class));
    }

    @Test
    void doesNotRegister_inNonWebContext() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        Fastjson3HttpMessageConverterAutoConfiguration.class))
                .run(ctx -> assertThat(ctx).doesNotHaveBean(Fastjson3HttpMessageConverter.class));
    }

    @Test
    void respectsUserSuppliedConverter() {
        servletRunner.withUserConfiguration(UserConverterConfig.class).run(ctx -> {
            assertThat(ctx).hasSingleBean(Fastjson3HttpMessageConverter.class);
            assertThat(ctx.getBean(Fastjson3HttpMessageConverter.class))
                    .isSameAs(UserConverterConfig.SHARED);
        });
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

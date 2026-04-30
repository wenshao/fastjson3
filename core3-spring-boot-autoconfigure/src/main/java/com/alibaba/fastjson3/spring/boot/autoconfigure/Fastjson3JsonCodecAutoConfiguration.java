package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.spring.codec.Fastjson3JsonDecoder;
import com.alibaba.fastjson3.spring.codec.Fastjson3JsonEncoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Auto-configuration that registers fastjson3 reactive codecs
 * ({@link Fastjson3JsonDecoder} + {@link Fastjson3JsonEncoder}) and a
 * {@link CodecCustomizer} that writes them into the
 * {@code ServerCodecConfigurer.defaultCodecs().jackson2JsonDecoder() /
 * jackson2JsonEncoder()} slots, replacing Jackson's default codecs.
 *
 * <p>Triggered when:</p>
 * <ul>
 *   <li>{@link Fastjson3JsonDecoder} is on the classpath (always true
 *       through this starter)</li>
 *   <li>{@link WebFluxConfigurer} is on the classpath (reactive stack)</li>
 *   <li>The application context is a reactive web application</li>
 * </ul>
 *
 * <p><b>Why a {@link CodecCustomizer} instead of a
 * {@link WebFluxConfigurer}</b>: Boot's
 * {@code CodecsAutoConfiguration.JacksonCodecConfiguration} ships a
 * {@code @Order(0)} {@code jacksonCodecCustomizer} bean that writes the
 * same {@code jackson2JsonDecoder} / {@code jackson2JsonEncoder} slots —
 * last writer wins. Boot iterates customizers via {@code orderedStream()},
 * so registering ours at {@link Ordered#LOWEST_PRECEDENCE} guarantees we
 * run after Jackson's and our codecs end up active. A bare
 * {@code WebFluxConfigurer} cannot make that guarantee because its
 * relative order against Boot's own {@code WebFluxConfig} (which is the
 * one that fans out the customizer chain) is implementation-defined.</p>
 *
 * <p>Users wanting a custom {@link com.alibaba.fastjson3.ObjectMapper}
 * declare their own {@code Fastjson3JsonDecoder} / {@code Fastjson3JsonEncoder}
 * beans; this auto-configuration steps aside via
 * {@link ConditionalOnMissingBean}, and the user-supplied codecs are picked
 * up by the registered customizer.</p>
 */
@AutoConfiguration(after = {JacksonAutoConfiguration.class, CodecsAutoConfiguration.class,
        Fastjson3ObjectMapperAutoConfiguration.class})
@ConditionalOnClass({Fastjson3JsonDecoder.class, WebFluxConfigurer.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class Fastjson3JsonCodecAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public Fastjson3JsonDecoder fastjson3JsonDecoder(ObjectMapper fastjson3ObjectMapper) {
        // Single shared mapper across decoder + encoder — see
        // Fastjson3ObjectMapperAutoConfiguration.
        return new Fastjson3JsonDecoder(fastjson3ObjectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public Fastjson3JsonEncoder fastjson3JsonEncoder(ObjectMapper fastjson3ObjectMapper) {
        return new Fastjson3JsonEncoder(fastjson3ObjectMapper);
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public CodecCustomizer fastjson3CodecCustomizer(
            Fastjson3JsonDecoder decoder, Fastjson3JsonEncoder encoder) {
        return cfg -> {
            cfg.defaultCodecs().jackson2JsonDecoder(decoder);
            cfg.defaultCodecs().jackson2JsonEncoder(encoder);
        };
    }
}

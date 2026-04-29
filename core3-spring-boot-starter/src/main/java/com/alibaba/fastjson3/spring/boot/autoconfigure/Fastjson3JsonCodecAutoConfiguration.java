package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.spring.codec.Fastjson3JsonDecoder;
import com.alibaba.fastjson3.spring.codec.Fastjson3JsonEncoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Auto-configuration that registers fastjson3 reactive codecs
 * ({@link Fastjson3JsonDecoder} + {@link Fastjson3JsonEncoder}) and a
 * {@link WebFluxConfigurer} that wires them into the
 * {@link ServerCodecConfigurer} default codecs slot, replacing Jackson's
 * default JSON decoder / encoder.
 *
 * <p>Triggered when:</p>
 * <ul>
 *   <li>{@link Fastjson3JsonDecoder} is on the classpath (always true
 *       through this starter)</li>
 *   <li>{@link WebFluxConfigurer} is on the classpath (reactive stack)</li>
 *   <li>The application context is a reactive web application</li>
 * </ul>
 *
 * <p>Users wanting a custom {@link com.alibaba.fastjson3.ObjectMapper}
 * declare their own {@code Fastjson3JsonDecoder} / {@code Fastjson3JsonEncoder}
 * beans; this auto-configuration steps aside via
 * {@link ConditionalOnMissingBean}, and the user-supplied codecs are picked
 * up by the registered configurer.</p>
 */
@AutoConfiguration
@ConditionalOnClass({Fastjson3JsonDecoder.class, WebFluxConfigurer.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class Fastjson3JsonCodecAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public Fastjson3JsonDecoder fastjson3JsonDecoder() {
        return new Fastjson3JsonDecoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public Fastjson3JsonEncoder fastjson3JsonEncoder() {
        return new Fastjson3JsonEncoder();
    }

    @Bean
    public WebFluxConfigurer fastjson3WebFluxConfigurer(
            Fastjson3JsonDecoder decoder, Fastjson3JsonEncoder encoder) {
        return new WebFluxConfigurer() {
            @Override
            public void configureHttpMessageCodecs(ServerCodecConfigurer cfg) {
                cfg.defaultCodecs().jackson2JsonDecoder(decoder);
                cfg.defaultCodecs().jackson2JsonEncoder(encoder);
            }
        };
    }
}

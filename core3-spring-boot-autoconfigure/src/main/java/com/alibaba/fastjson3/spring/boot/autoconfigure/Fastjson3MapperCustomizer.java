package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;

/**
 * Callback applied to the auto-config-built {@link ObjectMapper} bean
 * during construction. Mirrors Spring Boot's
 * {@code Jackson2ObjectMapperBuilderCustomizer} pattern.
 *
 * <p>Customizers fire ONLY against the auto-config mapper produced by
 * {@code Fastjson3ObjectMapperAutoConfiguration.fastjson3ObjectMapper()}.
 * When the user supplies their own {@link ObjectMapper} bean (suppressing
 * the auto-config via {@code @ConditionalOnMissingBean}), customizers are
 * never invoked — the user takes full responsibility for mapper
 * configuration. This keeps any {@code ObjectWriter} / {@code ObjectReader}
 * the user already registered on their own mapper from being clobbered,
 * even when the user names their bean {@code fastjson3ObjectMapper}.
 *
 * <p>Multiple customizers can be declared as separate {@code @Bean}s; they
 * are applied in {@link org.springframework.core.annotation.Order} order.
 *
 * <p>Module auto-configurations register their setup via this interface
 * — see {@code Fastjson3GeoJsonAutoConfiguration} for the canonical
 * example.
 */
@FunctionalInterface
public interface Fastjson3MapperCustomizer {
    /**
     * Apply customizations to the auto-config {@link ObjectMapper} bean.
     *
     * @param mapper the mapper instance under construction
     */
    void customize(ObjectMapper mapper);
}

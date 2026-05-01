package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that exposes a single {@link ObjectMapper} bean
 * configured from {@link Fastjson3Properties}, shared across the servlet
 * converter and reactive codec auto-configurations. Mirrors Jackson's
 * {@code JacksonAutoConfiguration$JacksonObjectMapperConfiguration} —
 * users override the entire serialization chain by declaring their own
 * {@link ObjectMapper} bean.
 *
 * <p><b>Why a shared bean</b>: prior to this configuration, each
 * autoconfig built its own {@code ObjectMapper} from
 * {@code Fastjson3Properties.buildObjectMapper()} when a {@code dateFormat}
 * was set. With reactive (decoder + encoder) on the classpath that's
 * three identical mapper instances — non-trivial heap footprint per app.
 * Extracting the mapper as a bean collapses to one instance and gives
 * users the canonical override pattern.</p>
 */
@AutoConfiguration
@ConditionalOnClass(ObjectMapper.class)
@EnableConfigurationProperties(Fastjson3Properties.class)
public class Fastjson3ObjectMapperAutoConfiguration {
    /**
     * Shared {@link ObjectMapper} bean. Replaced by a user-supplied
     * {@code ObjectMapper} bean of any name (the
     * {@link ConditionalOnMissingBean} matches by type). When the user
     * provides their own mapper, {@code spring.fastjson3.date-format}
     * has no effect — the user is responsible for any desired format
     * configuration on their mapper.
     *
     * <p>When no property is set and no user mapper exists, returns
     * {@link ObjectMapper#shared()} — alloc-equivalent to the
     * pre-property no-arg constructor path; no per-app mapper allocation.</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper fastjson3ObjectMapper(Fastjson3Properties properties) {
        return properties.buildObjectMapper();
    }
}

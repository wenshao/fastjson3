package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.spring.Fastjson3HttpMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration that registers {@link Fastjson3HttpMessageConverter} as
 * a bean in a servlet web context. Boot's
 * {@code HttpMessageConvertersAutoConfiguration} collects all
 * {@code HttpMessageConverter} beans via an {@code ObjectProvider} and ranks
 * user-supplied beans ahead of its built-in defaults — so the bean we
 * register here lands ahead of the default Jackson converter for
 * {@code application/json} + {@code application/*+json}.
 *
 * <p>Triggered when:</p>
 * <ul>
 *   <li>{@link Fastjson3HttpMessageConverter} is on the classpath
 *       (i.e. {@code fastjson3-spring} is a dependency — always true through
 *       this starter)</li>
 *   <li>{@link WebMvcConfigurer} is on the classpath (servlet stack)</li>
 *   <li>The application context is a servlet web application</li>
 *   <li>No user-supplied {@link Fastjson3HttpMessageConverter} bean exists</li>
 * </ul>
 *
 * <p>Users wanting a custom {@link com.alibaba.fastjson3.ObjectMapper} simply
 * declare their own {@code Fastjson3HttpMessageConverter} bean — this
 * auto-configuration steps aside via {@link ConditionalOnMissingBean}.</p>
 */
@AutoConfiguration(after = JacksonAutoConfiguration.class)
@ConditionalOnClass({Fastjson3HttpMessageConverter.class, WebMvcConfigurer.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(Fastjson3Properties.class)
public class Fastjson3HttpMessageConverterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public Fastjson3HttpMessageConverter fastjson3HttpMessageConverter(Fastjson3Properties properties) {
        // Build the converter against the property-configured ObjectMapper.
        // When no property is set, Fastjson3Properties.buildObjectMapper()
        // returns ObjectMapper.shared() so this path is alloc-equivalent
        // to the pre-property no-arg constructor.
        return new Fastjson3HttpMessageConverter(properties.buildObjectMapper());
    }
}

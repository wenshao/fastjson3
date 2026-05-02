package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * External configuration for the fastjson3 Spring Boot starter,
 * mirroring the pattern of {@code spring.jackson.*}.
 *
 * <p>All properties are optional; with none set, the starter still
 * builds a fresh per-app {@link ObjectMapper} that behaves identically
 * to {@link ObjectMapper#shared()} for serialization but is a distinct
 * instance — so add-on auto-configurations (e.g.
 * {@code Fastjson3GeoJsonAutoConfiguration}) can register
 * readers/writers locally on it without mutating the JVM-global
 * {@link ObjectMapper#shared()} instance.</p>
 *
 * <p><b>Properties</b>:</p>
 * <ul>
 *   <li>{@code spring.fastjson3.date-format} — default date/time
 *       format for write side, applied to typed POJO Date / Temporal
 *       fields without an explicit {@code @JSONField(format=...)}
 *       override. Recognized values match
 *       {@link ObjectMapper.Builder#dateFormat(String)}: {@code "millis"},
 *       {@code "unixtime"}, {@code "iso8601"}, the five fast-path
 *       patterns ({@code "yyyy-MM-dd"} / {@code "yyyyMMdd"} /
 *       {@code "yyyy-MM-dd HH:mm"} / {@code "yyyy-MM-dd HH:mm:ss"} /
 *       {@code "yyyyMMddHHmmss"}), or any {@link java.time.format.DateTimeFormatter}
 *       pattern.</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "spring.fastjson3")
public class Fastjson3Properties {
    /**
     * Default date/time format applied at write time. {@code null} or
     * empty means "no format" — emit per the natural ISO shape of each
     * Temporal type. Mirrors {@code spring.jackson.date-format}.
     */
    private String dateFormat;

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Build an {@link ObjectMapper} reflecting the configured properties.
     * Always returns a fresh instance (never {@link ObjectMapper#shared()}),
     * so module auto-configurations that register readers/writers on the
     * resolved bean — {@code Fastjson3GeoJsonAutoConfiguration} being the
     * first such case — cannot accidentally mutate the JVM-global shared
     * mapper. The per-app allocation cost is one mapper instance.
     */
    ObjectMapper buildObjectMapper() {
        ObjectMapper.Builder builder = ObjectMapper.builder();
        if (dateFormat != null && !dateFormat.isEmpty()) {
            builder.dateFormat(dateFormat);
        }
        return builder.build();
    }
}

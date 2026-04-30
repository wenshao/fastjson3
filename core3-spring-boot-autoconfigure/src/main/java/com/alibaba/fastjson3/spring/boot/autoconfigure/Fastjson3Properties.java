package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * External configuration for the fastjson3 Spring Boot starter,
 * mirroring the pattern of {@code spring.jackson.*}.
 *
 * <p>All properties are optional; when unset, the starter uses
 * {@link ObjectMapper#shared()} for the registered converter / codec
 * beans (the pre-property behavior).</p>
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
     * Returns {@link ObjectMapper#shared()} when no property is set, so
     * users with no {@code spring.fastjson3.*} config keep the default
     * shared mapper (no per-app allocation overhead).
     */
    ObjectMapper buildObjectMapper() {
        if (dateFormat == null || dateFormat.isEmpty()) {
            return ObjectMapper.shared();
        }
        return ObjectMapper.builder()
                .dateFormat(dateFormat)
                .build();
    }
}

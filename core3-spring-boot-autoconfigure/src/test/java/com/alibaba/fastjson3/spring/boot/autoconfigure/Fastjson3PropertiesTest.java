package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit-level tests for {@link Fastjson3Properties#buildObjectMapper()}.
 * Pins the alloc-equivalence claim: when no {@code dateFormat} is set
 * the helper returns the shared {@link ObjectMapper#shared()} instance,
 * so a no-property starter pays no per-app mapper allocation cost.
 */
class Fastjson3PropertiesTest {
    @Test
    void buildObjectMapper_null_returnsShared() {
        Fastjson3Properties p = new Fastjson3Properties();
        // dateFormat field defaults to null; no setter call needed.
        assertSame(ObjectMapper.shared(), p.buildObjectMapper(),
                "no dateFormat should yield the shared mapper to avoid per-app allocation");
    }

    @Test
    void buildObjectMapper_empty_returnsShared() {
        // Empty string from `spring.fastjson3.date-format=` is treated
        // identically to null — both fall back to the shared mapper.
        Fastjson3Properties p = new Fastjson3Properties();
        p.setDateFormat("");
        assertSame(ObjectMapper.shared(), p.buildObjectMapper());
    }

    @Test
    void buildObjectMapper_dateFormat_returnsConfiguredMapper() {
        Fastjson3Properties p = new Fastjson3Properties();
        p.setDateFormat("yyyy-MM-dd");
        ObjectMapper m = p.buildObjectMapper();
        assertNotNull(m);
        assertNotSame(ObjectMapper.shared(), m,
                "configured dateFormat should produce a fresh mapper, not the shared one");
        // Configured mapper exposes the format via its public accessor.
        org.junit.jupiter.api.Assertions.assertEquals("yyyy-MM-dd", m.getDateFormat());
    }
}

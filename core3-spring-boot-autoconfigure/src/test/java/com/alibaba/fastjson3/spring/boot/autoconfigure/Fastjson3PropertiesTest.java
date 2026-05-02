package com.alibaba.fastjson3.spring.boot.autoconfigure;

import com.alibaba.fastjson3.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Unit-level tests for {@link Fastjson3Properties#buildObjectMapper()}.
 *
 * <p>Pins the "always fresh" contract: even with no {@code dateFormat}
 * set, {@code buildObjectMapper()} returns a distinct instance — never
 * {@link ObjectMapper#shared()}. This is what lets module
 * auto-configurations (geojson, etc.) register readers/writers on the
 * resolved bean without mutating the JVM-global shared mapper.</p>
 */
class Fastjson3PropertiesTest {
    @Test
    void buildObjectMapper_null_returnsFreshInstance() {
        Fastjson3Properties p = new Fastjson3Properties();
        // dateFormat field defaults to null; no setter call needed.
        ObjectMapper m = p.buildObjectMapper();
        assertNotNull(m);
        assertNotSame(ObjectMapper.shared(), m,
                "no dateFormat must still yield a fresh mapper — module autoconfigs "
                        + "register readers/writers on the bean and must not mutate shared()");
    }

    @Test
    void buildObjectMapper_empty_returnsFreshInstance() {
        // Empty string from `spring.fastjson3.date-format=` is treated
        // identically to null — both yield a fresh mapper.
        Fastjson3Properties p = new Fastjson3Properties();
        p.setDateFormat("");
        assertNotSame(ObjectMapper.shared(), p.buildObjectMapper());
    }

    @Test
    void buildObjectMapper_consecutiveCalls_returnDistinctInstances() {
        Fastjson3Properties p = new Fastjson3Properties();
        // Each call allocates fresh — pins the mutable-bean isolation
        // contract: two Boot apps in the same JVM (rare, e.g. shared
        // tomcat) get independent mappers.
        assertNotSame(p.buildObjectMapper(), p.buildObjectMapper());
    }

    @Test
    void buildObjectMapper_dateFormat_returnsConfiguredMapper() {
        Fastjson3Properties p = new Fastjson3Properties();
        p.setDateFormat("yyyy-MM-dd");
        ObjectMapper m = p.buildObjectMapper();
        assertNotNull(m);
        assertNotSame(ObjectMapper.shared(), m,
                "configured dateFormat should produce a fresh mapper, not the shared one");
        assertEquals("yyyy-MM-dd", m.getDateFormat());
    }
}

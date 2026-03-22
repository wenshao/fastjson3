package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.filter.PropertyPreFilter;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for remaining P0 serialization capabilities.
 */
class P0RemainingTest {

    // ==================== IgnoreNonFieldGetter ====================

    public static class BeanWithExtraGetter {
        private String name = "test";

        public String getName() { return name; }

        // This getter has no backing field — should be ignored with IgnoreNonFieldGetter
        public String getComputed() { return name.toUpperCase(); }
    }

    @Test
    void ignoreNonFieldGetter() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.IgnoreNonFieldGetter)
                .build();
        String json = mapper.writeValueAsString(new BeanWithExtraGetter());
        assertTrue(json.contains("\"name\""), json);
        assertFalse(json.contains("\"computed\""), "Should skip getComputed(): " + json);
    }

    public static class BeanWithAnnotatedGetter {
        private String name = "test";
        public String getName() { return name; }

        @com.alibaba.fastjson3.annotation.JSONField(name = "computed")
        public String getComputed() { return name.toUpperCase(); }
    }

    @Test
    void ignoreNonFieldGetter_withAnnotation_keeps() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.IgnoreNonFieldGetter)
                .build();
        String json = mapper.writeValueAsString(new BeanWithAnnotatedGetter());
        assertTrue(json.contains("\"name\""), json);
        assertTrue(json.contains("\"computed\""), "Annotated getter should be kept: " + json);
    }

    // ==================== WriteDateAsMillis ====================

    @Test
    void writeDateAsMillis() {
        Date date = new Date(1700000000000L);
        String json = JSON.toJSONString(date, WriteFeature.WriteDateAsMillis);
        assertEquals("1700000000000", json);
    }

    @Test
    void writeDateAsMillis_inObject() {
        var obj = new JSONObject();
        obj.put("time", new Date(1700000000000L));
        String json = JSON.toJSONString(obj, WriteFeature.WriteDateAsMillis);
        assertTrue(json.contains("1700000000000"), json);
    }

    // ==================== WriteMapNullValue ====================

    @Test
    void writeMapNullValue_enabled() {
        var map = new LinkedHashMap<String, Object>();
        map.put("a", "hello");
        map.put("b", null);
        map.put("c", 42);
        String json = JSON.toJSONString(map, WriteFeature.WriteMapNullValue);
        assertTrue(json.contains("\"b\":null"), "Should include null entry: " + json);
    }

    @Test
    void writeMapNullValue_default_includesNull() {
        var map = new LinkedHashMap<String, Object>();
        map.put("a", "hello");
        map.put("b", null);
        map.put("c", 42);
        String json = JSON.toJSONString(map);
        // Default: null Map values ARE written (backward compatible)
        assertTrue(json.contains("\"b\":null"), "Default should include null entry: " + json);
    }

    // ==================== PropertyPreFilter ====================

    public static class User {
        public String name = "John";
        public String email = "john@example.com";
        public int age = 30;
    }

    @Test
    void propertyPreFilter_excludeByName() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addPropertyPreFilter((source, name) -> !"email".equals(name))
                .build();
        String json = mapper.writeValueAsString(new User());
        assertTrue(json.contains("\"name\""), json);
        assertTrue(json.contains("\"age\""), json);
        assertFalse(json.contains("\"email\""), "email should be filtered: " + json);
    }

    @Test
    void propertyPreFilter_includeOnlySpecific() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addPropertyPreFilter((source, name) -> "name".equals(name))
                .build();
        String json = mapper.writeValueAsString(new User());
        assertTrue(json.contains("\"name\""), json);
        assertFalse(json.contains("\"email\""), json);
        assertFalse(json.contains("\"age\""), json);
    }

    // ==================== @JSONField(deserializeFeatures) annotation exists ====================

    @Test
    void deserializeFeatures_annotationExists() throws Exception {
        var method = JSONField.class.getDeclaredMethod("deserializeFeatures");
        assertNotNull(method);
        assertEquals(ReadFeature[].class, method.getReturnType());
    }

    // ==================== AutoTypeFilter interface exists ====================

    @Test
    void autoTypeFilter_acceptNames() {
        var filter = com.alibaba.fastjson3.filter.AutoTypeFilter.acceptNames("com.alibaba.");
        // Should accept classes under com.alibaba.*
        Class<?> result = filter.apply("com.alibaba.fastjson3.JSONObject", null);
        assertNotNull(result);
        assertEquals(JSONObject.class, result);

        // Should deny other classes
        Class<?> denied = filter.apply("com.evil.Exploit", null);
        assertNull(denied);
    }
}

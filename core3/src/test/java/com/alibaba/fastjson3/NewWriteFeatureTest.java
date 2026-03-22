package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the 9 new WriteFeatures added for fastjson2 compatibility.
 */
class NewWriteFeatureTest {

    // ==================== PrettyFormatWith2Space ====================

    @Test
    void prettyFormat2Space() {
        var obj = new JSONObject();
        obj.put("a", 1);
        String json = JSON.toJSONString(obj, WriteFeature.PrettyFormatWith2Space);
        assertTrue(json.contains("\n  \"a\""), "Should have 2-space indent: " + json);
    }

    // ==================== PrettyFormatWith4Space ====================

    @Test
    void prettyFormat4Space() {
        var obj = new JSONObject();
        obj.put("a", 1);
        String json = JSON.toJSONString(obj, WriteFeature.PrettyFormatWith4Space);
        assertTrue(json.contains("\n    \"a\""), "Should have 4-space indent: " + json);
        assertFalse(json.contains("\n  \"a\":"), "Should NOT have 2-space indent");
    }

    @Test
    void prettyFormat4Space_nested() {
        var inner = new JSONObject();
        inner.put("b", 2);
        var outer = new JSONObject();
        outer.put("a", inner);
        String json = JSON.toJSONString(outer, WriteFeature.PrettyFormatWith4Space);
        assertTrue(json.contains("\n        \"b\""), "Nested should have 8-space indent: " + json);
    }

    // ==================== UseSingleQuotes ====================

    @Test
    void useSingleQuotes() {
        var obj = new JSONObject();
        obj.put("name", "test");
        String json = JSON.toJSONString(obj, WriteFeature.UseSingleQuotes);
        assertTrue(json.contains("'name'"), "Key should use single quotes: " + json);
        assertTrue(json.contains("'test'"), "Value should use single quotes: " + json);
        assertFalse(json.contains("\""), "Should not contain double quotes: " + json);
    }

    @Test
    void useSingleQuotes_withEmbeddedSingleQuote() {
        var obj = new JSONObject();
        obj.put("msg", "it's");
        String json = JSON.toJSONString(obj, WriteFeature.UseSingleQuotes);
        assertTrue(json.contains("\\'"), "Embedded single quote should be escaped: " + json);
    }

    // ==================== WriteFloatSpecialAsString ====================

    @Test
    void floatNaN_asString() {
        String json = JSON.toJSONString(Float.NaN, WriteFeature.WriteFloatSpecialAsString);
        assertEquals("\"NaN\"", json);
    }

    @Test
    void floatInfinity_asString() {
        String json = JSON.toJSONString(Float.POSITIVE_INFINITY, WriteFeature.WriteFloatSpecialAsString);
        assertEquals("\"Infinity\"", json);
    }

    @Test
    void doubleNaN_asString() {
        String json = JSON.toJSONString(Double.NaN, WriteFeature.WriteFloatSpecialAsString);
        assertEquals("\"NaN\"", json);
    }

    @Test
    void doubleNaN_default_asNull() {
        String json = JSON.toJSONString(Float.NaN);
        assertEquals("null", json);
    }

    // ==================== IgnoreErrorGetter ====================

    public static class ErrorBean {
        public String getName() { return "ok"; }
        public String getBroken() { throw new RuntimeException("boom"); }
    }

    @Test
    void ignoreErrorGetter() {
        ErrorBean bean = new ErrorBean();
        String json = JSON.toJSONString(bean, WriteFeature.IgnoreErrorGetter);
        assertTrue(json.contains("\"name\""), "Should contain name: " + json);
        assertTrue(json.contains("\"ok\""), "Should contain ok: " + json);
    }

    @Test
    void errorGetter_default_throws() {
        ErrorBean bean = new ErrorBean();
        assertThrows(JSONException.class, () -> JSON.toJSONString(bean));
    }

    // ==================== IgnoreNoneSerializable ====================

    public static class NonSerializableType {
        public int value = 42;
    }

    public static class ContainerWithNonSerializable implements Serializable {
        private String name = "test";
        private NonSerializableType data = new NonSerializableType();

        public String getName() { return name; }
        public NonSerializableType getData() { return data; }
    }

    @Test
    void ignoreNoneSerializable() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.IgnoreNoneSerializable)
                .build();
        ContainerWithNonSerializable bean = new ContainerWithNonSerializable();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\""), "Should contain Serializable field: " + json);
        assertFalse(json.contains("\"data\""), "Should skip non-Serializable field: " + json);
    }

    // ==================== ErrorOnNoneSerializable ====================

    @Test
    void errorOnNoneSerializable() {
        assertThrows(JSONException.class, () -> {
            ObjectMapper mapper = ObjectMapper.builder()
                    .enableWrite(WriteFeature.ErrorOnNoneSerializable)
                    .build();
            mapper.writeValueAsString(new ContainerWithNonSerializable());
        });
    }

    // ==================== WritePairAsJavaBean ====================

    @Test
    void writePairAsJavaBean() {
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("hello", 42);
        String json = JSON.toJSONString(entry, WriteFeature.WritePairAsJavaBean);
        assertTrue(json.contains("\"key\""), "Should have 'key' field: " + json);
        assertTrue(json.contains("\"hello\""), "Should have key value: " + json);
        assertTrue(json.contains("\"value\""), "Should have 'value' field: " + json);
        assertTrue(json.contains("42"), "Should have value: " + json);
    }

    // ==================== WriteNonStringKeyAsString ====================

    @Test
    void writeNonStringKeyAsString() {
        // Default behavior already converts keys via String.valueOf
        var map = new java.util.LinkedHashMap<>();
        map.put(123, "val");
        String json = JSON.toJSONString(map, WriteFeature.WriteNonStringKeyAsString);
        assertTrue(json.contains("\"123\""), "Integer key should be string: " + json);
    }
}

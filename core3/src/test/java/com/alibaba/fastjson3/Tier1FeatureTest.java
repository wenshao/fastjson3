package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Tier 1 feature completeness.
 */
class Tier1FeatureTest {

    // ==================== NotWriteSetClassName ====================

    @Test
    void notWriteSetClassName() {
        var set = new java.util.HashSet<String>();
        set.add("a");
        String json = JSON.toJSONString(set, WriteFeature.WriteClassName, WriteFeature.NotWriteSetClassName);
        assertFalse(json.contains("@type"), "HashSet should NOT have @type: " + json);
    }

    // ==================== NotWriteNumberClassName ====================

    @Test
    void notWriteNumberClassName() {
        Integer num = 42;
        String json = JSON.toJSONString(num, WriteFeature.WriteClassName, WriteFeature.NotWriteNumberClassName);
        // Numbers are written as primitives, not objects with @type
        assertFalse(json.contains("@type"), json);
    }

    // ==================== WriteThrowableClassName ====================

    @Test
    void writeThrowableClassName() {
        Exception ex = new IllegalArgumentException("bad input");
        String json = JSON.toJSONString(ex, WriteFeature.WriteThrowableClassName);
        assertTrue(json.contains("\"@type\""), "Should have @type: " + json);
        assertTrue(json.contains("IllegalArgumentException"), "Should have class name: " + json);
    }

    @Test
    void throwable_noClassName_byDefault() {
        Exception ex = new RuntimeException("test");
        String json = JSON.toJSONString(ex);
        assertFalse(json.contains("@type"), "Should NOT have @type by default: " + json);
    }

    // ==================== UnquoteFieldName ====================

    @Test
    void unquoteFieldName() {
        var obj = new JSONObject();
        obj.put("name", "test");
        obj.put("age", 30);
        String json = JSON.toJSONString(obj, WriteFeature.UnquoteFieldName);
        assertTrue(json.contains("name:"), "Field name should be unquoted: " + json);
        assertTrue(json.contains("age:"), json);
        // Values should still be quoted
        assertTrue(json.contains("\"test\""), "String values should still be quoted: " + json);
    }

    // ==================== LargeObject ====================

    @Test
    void largeObject_featureExists() {
        assertNotNull(WriteFeature.LargeObject);
        // Just verify the feature exists and can be used
        long mask = WriteFeature.of(WriteFeature.LargeObject);
        assertTrue(mask > 0);
    }

    // ==================== JSON.parseObject(Reader) ====================

    @Test
    void parseObject_reader() {
        StringReader reader = new StringReader("{\"name\":\"test\",\"age\":30}");
        JSONObject obj = JSON.parseObject(reader, JSONObject.class);
        // parseObject(Reader, Class) should work
    }

    public static class SimpleBean {
        public String name;
        public int age;
    }

    @Test
    void parseObject_reader_typed() {
        StringReader reader = new StringReader("{\"name\":\"test\",\"age\":30}");
        SimpleBean bean = JSON.parseObject(reader, SimpleBean.class);
        assertEquals("test", bean.name);
        assertEquals(30, bean.age);
    }

    // ==================== JSON.parseObject(InputStream) to JSONObject ====================

    @Test
    void parseObject_inputStream_jsonObject() {
        byte[] bytes = "{\"key\":\"value\"}".getBytes();
        var in = new java.io.ByteArrayInputStream(bytes);
        JSONObject obj = JSON.parseObject(in);
        assertNotNull(obj);
        assertEquals("value", obj.getString("key"));
    }

    // ==================== JSONObject.getOffsetDateTime ====================

    @Test
    void jsonObject_getOffsetDateTime() {
        JSONObject obj = new JSONObject();
        obj.put("dt", "2023-11-14T10:30:00+08:00");
        java.time.OffsetDateTime odt = obj.getOffsetDateTime("dt");
        assertNotNull(odt);
        assertEquals(2023, odt.getYear());
    }

    // ==================== JSONObject.getBytes ====================

    @Test
    void jsonObject_getBytes_base64() {
        String encoded = Base64.getEncoder().encodeToString("hello".getBytes());
        JSONObject obj = new JSONObject();
        obj.put("data", encoded);
        byte[] decoded = obj.getBytes("data");
        assertArrayEquals("hello".getBytes(), decoded);
    }

    @Test
    void jsonObject_getBytes_null() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getBytes("missing"));
    }

    // ==================== ReadFeature new values ====================

    @Test
    void readFeature_tier1_allExist() {
        assertNotNull(ReadFeature.SupportClassForName);
        assertNotNull(ReadFeature.UseDefaultConstructorAsPossible);
        assertNotNull(ReadFeature.ErrorOnNotSupportAutoType);
        assertNotNull(ReadFeature.IgnoreAutoTypeNotMatch);
        assertNotNull(ReadFeature.IgnoreNullPropertyValue);
        assertNotNull(ReadFeature.IgnoreCheckClose);
        assertNotNull(ReadFeature.UseBigIntegerForInts);
        assertNotNull(ReadFeature.UseDoubleForDecimals);
        assertNotNull(ReadFeature.DisableSingleQuote);
        assertNotNull(ReadFeature.DisableStringArrayUnwrapping);
        assertNotNull(ReadFeature.IgnoreNoneSerializable);
        assertNotNull(ReadFeature.ErrorOnNoneSerializable);
        assertNotNull(ReadFeature.NonStringKeyAsString);
        // All unique masks
        assertEquals(13, Long.bitCount(ReadFeature.of(
                ReadFeature.SupportClassForName,
                ReadFeature.UseDefaultConstructorAsPossible,
                ReadFeature.ErrorOnNotSupportAutoType,
                ReadFeature.IgnoreAutoTypeNotMatch,
                ReadFeature.IgnoreNullPropertyValue,
                ReadFeature.IgnoreCheckClose,
                ReadFeature.UseBigIntegerForInts,
                ReadFeature.UseDoubleForDecimals,
                ReadFeature.DisableSingleQuote,
                ReadFeature.DisableStringArrayUnwrapping,
                ReadFeature.IgnoreNoneSerializable,
                ReadFeature.ErrorOnNoneSerializable,
                ReadFeature.NonStringKeyAsString
        )));
    }
}

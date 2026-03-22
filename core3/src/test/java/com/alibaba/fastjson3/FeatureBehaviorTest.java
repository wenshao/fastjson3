package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ReadFeature/WriteFeature behavior (not just enum values).
 * Inspired by fastjson2 features/ directory and jackson feature tests.
 * Only tests features that are actually implemented.
 */
class FeatureBehaviorTest {

    // ==================== UseBigDecimalForDoubles ====================

    @Test
    void useBigDecimalForDoubles_scalarParse() {
        Object result = JSON.parse("1.2", ReadFeature.UseBigDecimalForDoubles);
        assertInstanceOf(BigDecimal.class, result);
        assertEquals(new BigDecimal("1.2"), result);
    }

    @Test
    void useBigDecimalForDoubles_inObjectParse() {
        Object result = JSON.parse("{\"price\":9.99}", ReadFeature.UseBigDecimalForDoubles);
        assertInstanceOf(JSONObject.class, result);
        JSONObject obj = (JSONObject) result;
        assertInstanceOf(BigDecimal.class, obj.get("price"));
        assertEquals(new BigDecimal("9.99"), obj.get("price"));
    }

    @Test
    void useBigDecimalForDoubles_preservesPrecision() {
        Object result = JSON.parse("0.1234567890123456789", ReadFeature.UseBigDecimalForDoubles);
        assertInstanceOf(BigDecimal.class, result);
        assertEquals(new BigDecimal("0.1234567890123456789"), result);
    }

    @Test
    void useBigDecimalForDoubles_inArrayParse() {
        Object result = JSON.parse("[1.1, 2.2]", ReadFeature.UseBigDecimalForDoubles);
        assertInstanceOf(JSONArray.class, result);
        JSONArray arr = (JSONArray) result;
        assertInstanceOf(BigDecimal.class, arr.get(0));
        assertInstanceOf(BigDecimal.class, arr.get(1));
    }

    @Test
    void useBigDecimalForDoubles_integerStaysInt() {
        Object result = JSON.parse("{\"a\":42,\"b\":1.5}", ReadFeature.UseBigDecimalForDoubles);
        JSONObject obj = (JSONObject) result;
        // Integer should remain Integer
        assertInstanceOf(Integer.class, obj.get("a"));
        // Float should become BigDecimal
        assertInstanceOf(BigDecimal.class, obj.get("b"));
    }

    @Test
    void defaultDouble_losesTypePrecision() {
        Object result = JSON.parse("0.1234567890123456789");
        assertInstanceOf(Double.class, result);
    }

    // ==================== AllowSingleQuotes ====================

    @Test
    void allowSingleQuotes_stringValue() {
        Object result = JSON.parse("'test'", ReadFeature.AllowSingleQuotes);
        assertEquals("test", result);
    }

    @Test
    void allowSingleQuotes_objectKeys() {
        Object result = JSON.parse("{'a':1,'b':2}", ReadFeature.AllowSingleQuotes);
        assertInstanceOf(JSONObject.class, result);
        JSONObject obj = (JSONObject) result;
        assertEquals(1, obj.getIntValue("a"));
        assertEquals(2, obj.getIntValue("b"));
    }

    @Test
    void allowSingleQuotes_nestedObject() {
        Object result = JSON.parse(
                "{'list':['x','y'],'obj':{'k':'v'}}",
                ReadFeature.AllowSingleQuotes);
        JSONObject obj = (JSONObject) result;
        JSONArray list = obj.getJSONArray("list");
        assertEquals(2, list.size());
        assertEquals("x", list.getString(0));
        assertEquals("v", obj.getJSONObject("obj").getString("k"));
    }

    @Test
    void allowSingleQuotes_mixedQuotes() {
        Object result = JSON.parse("{'a':\"double\"}", ReadFeature.AllowSingleQuotes);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("double", ((JSONObject) result).getString("a"));
    }

    @Test
    void allowSingleQuotes_empty() {
        Object result = JSON.parse("''", ReadFeature.AllowSingleQuotes);
        assertEquals("", result);
    }

    @Test
    void allowSingleQuotes_withEscape() {
        Object result = JSON.parse("'it\\'s a test'", ReadFeature.AllowSingleQuotes);
        assertEquals("it's a test", result);
    }

    // ==================== WriteNulls ====================

    public static class NullableBean {
        public String name;
        public Integer count;
    }

    @Test
    void writeNulls_includesNullFields() {
        NullableBean bean = new NullableBean();
        bean.name = null;
        bean.count = null;
        String json = JSON.toJSONString(bean, WriteFeature.WriteNulls);
        assertTrue(json.contains("null"), "Null fields should be present: " + json);
    }

    @Test
    void writeNulls_defaultOmitsNull() {
        NullableBean bean = new NullableBean();
        bean.name = null;
        bean.count = 5;
        String json = JSON.toJSONString(bean);
        assertFalse(json.contains("\"name\""), "Null name should be omitted: " + json);
        assertTrue(json.contains("\"count\":5"), json);
    }

    // ==================== WriteNullStringAsEmpty ====================

    @Test
    void writeNullStringAsEmpty() {
        NullableBean bean = new NullableBean();
        bean.name = null;
        bean.count = 5;
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNullStringAsEmpty)
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"\""), "Null string should be empty: " + json);
    }

    // ==================== WriteNullNumberAsZero ====================

    @Test
    void writeNullNumberAsZero() {
        NullableBean bean = new NullableBean();
        bean.count = null;
        bean.name = "test";
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNullNumberAsZero)
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"count\":0"), "Null number should be 0: " + json);
    }

    // ==================== WriteNullBooleanAsFalse ====================

    public static class BoolBean {
        public String name;
        public Boolean active;
    }

    @Test
    void writeNullBooleanAsFalse() {
        BoolBean bean = new BoolBean();
        bean.name = "test";
        bean.active = null;
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNullBooleanAsFalse)
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"active\":false"), "Null bool should be false: " + json);
    }

    // ==================== WriteNullListAsEmpty ====================

    public static class ListBean {
        public String name;
        public java.util.List<String> items;
    }

    @Test
    void writeNullListAsEmpty() {
        ListBean bean = new ListBean();
        bean.name = "test";
        bean.items = null;
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNullListAsEmpty)
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"items\":[]"), "Null list should be []: " + json);
    }

    // ==================== WriteBigDecimalAsPlain ====================

    @Test
    void writeBigDecimalAsPlain_direct() {
        BigDecimal value = new BigDecimal("1.23E10");
        String json = JSON.toJSONString(value, WriteFeature.WriteBigDecimalAsPlain);
        assertFalse(json.contains("E"), "Should be plain: " + json);
        assertEquals("12300000000", json);
    }

    @Test
    void writeBigDecimalAsPlain_small() {
        BigDecimal value = new BigDecimal("1.5E-5");
        String json = JSON.toJSONString(value, WriteFeature.WriteBigDecimalAsPlain);
        assertFalse(json.contains("E"), "Should be plain: " + json);
        assertEquals("0.000015", json);
    }

    @Test
    void writeBigDecimalAsPlain_inObject() {
        JSONObject obj = new JSONObject();
        obj.put("val", new BigDecimal("1.23E5"));
        String json = obj.toJSONString(WriteFeature.WriteBigDecimalAsPlain);
        assertFalse(json.contains("E"), "Should be plain: " + json);
        assertTrue(json.contains("123000"), json);
    }

    // ==================== PrettyFormat ====================

    @Test
    void prettyFormat_containsNewlines() {
        JSONObject obj = new JSONObject();
        obj.put("a", 1);
        String json = obj.toJSONString(WriteFeature.PrettyFormat);
        assertTrue(json.contains("\n"), "Pretty format should have newlines: " + json);
    }

    @Test
    void prettyFormat_containsIndentation() {
        JSONObject obj = new JSONObject();
        obj.put("a", 1);
        obj.put("b", 2);
        String json = obj.toJSONString(WriteFeature.PrettyFormat);
        assertTrue(json.contains("  ") || json.contains("\t"),
                "Pretty format should have indentation: " + json);
    }

    // ==================== Feature combinations ====================

    @Test
    void useBigDecimalForDoubles_withMultipleValues() {
        Object result = JSON.parse(
                "{\"a\":1.1,\"b\":2.2,\"c\":3}",
                ReadFeature.UseBigDecimalForDoubles);
        JSONObject obj = (JSONObject) result;
        assertInstanceOf(BigDecimal.class, obj.get("a"));
        assertInstanceOf(BigDecimal.class, obj.get("b"));
        assertInstanceOf(Integer.class, obj.get("c")); // integer stays
    }

    @Test
    void writeNullsCombined() {
        NullableBean bean = new NullableBean();
        // all null
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(
                        WriteFeature.WriteNullStringAsEmpty,
                        WriteFeature.WriteNullNumberAsZero
                )
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"\""), "String empty: " + json);
        assertTrue(json.contains("\"count\":0"), "Number zero: " + json);
    }
}

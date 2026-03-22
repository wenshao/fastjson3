package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for type coercion edge cases during deserialization.
 * Inspired by jackson CoerceXxxTest, fastjson2 type conversion tests.
 */
class TypeCoercionTest {

    // ==================== String → Number coercion ====================

    @Test
    void stringToInt() {
        JSONObject obj = JSON.parseObject("{\"val\":\"42\"}");
        assertEquals(42, obj.getIntValue("val"));
    }

    @Test
    void stringToLong() {
        JSONObject obj = JSON.parseObject("{\"val\":\"9999999999\"}");
        assertEquals(9999999999L, obj.getLongValue("val"));
    }

    @Test
    void stringToDouble() {
        JSONObject obj = JSON.parseObject("{\"val\":\"3.14\"}");
        assertEquals(3.14, obj.getDoubleValue("val"), 1e-10);
    }

    @Test
    void stringToBigDecimal() {
        JSONObject obj = JSON.parseObject("{\"val\":\"123.456\"}");
        assertEquals(0, new BigDecimal("123.456").compareTo(obj.getBigDecimal("val")));
    }

    @Test
    void stringToBigInteger() {
        JSONObject obj = JSON.parseObject("{\"val\":\"99999999999999999999\"}");
        assertEquals(new BigInteger("99999999999999999999"), obj.getBigInteger("val"));
    }

    @Test
    void stringToBoolean() {
        JSONObject obj = JSON.parseObject("{\"val\":\"true\"}");
        assertTrue(obj.getBooleanValue("val"));
    }

    @Test
    void stringToBoolean_false() {
        JSONObject obj = JSON.parseObject("{\"val\":\"false\"}");
        assertFalse(obj.getBooleanValue("val"));
    }

    // ==================== Number → String coercion ====================

    @Test
    void intToString() {
        JSONObject obj = JSON.parseObject("{\"val\":42}");
        assertEquals("42", obj.getString("val"));
    }

    @Test
    void longToString() {
        JSONObject obj = JSON.parseObject("{\"val\":9999999999}");
        assertEquals("9999999999", obj.getString("val"));
    }

    @Test
    void doubleToString() {
        JSONObject obj = JSON.parseObject("{\"val\":3.14}");
        String str = obj.getString("val");
        assertTrue(str.startsWith("3.14"), "Expected '3.14' prefix: " + str);
    }

    @Test
    void boolToString() {
        JSONObject obj = JSON.parseObject("{\"val\":true}");
        assertEquals("true", obj.getString("val"));
    }

    // ==================== Number ↔ Number coercion ====================

    @Test
    void intToLong() {
        JSONObject obj = JSON.parseObject("{\"val\":42}");
        assertEquals(42L, obj.getLongValue("val"));
    }

    @Test
    void intToDouble() {
        JSONObject obj = JSON.parseObject("{\"val\":42}");
        assertEquals(42.0, obj.getDoubleValue("val"), 1e-10);
    }

    @Test
    void intToBigDecimal() {
        JSONObject obj = JSON.parseObject("{\"val\":42}");
        assertEquals(0, new BigDecimal("42").compareTo(obj.getBigDecimal("val")));
    }

    @Test
    void longToInt_withinRange() {
        JSONObject obj = JSON.parseObject("{\"val\":42}");
        assertEquals(42, obj.getIntValue("val"));
    }

    @Test
    void doubleToInt() {
        JSONObject obj = JSON.parseObject("{\"val\":42.0}");
        assertEquals(42, obj.getIntValue("val"));
    }

    @Test
    void doubleToLong() {
        JSONObject obj = JSON.parseObject("{\"val\":42.0}");
        assertEquals(42L, obj.getLongValue("val"));
    }

    // ==================== Boolean coercion ====================

    @Test
    void boolToInt_throws() {
        // Boolean → Int coercion not supported — should throw
        JSONObject obj = JSON.parseObject("{\"val\":true}");
        assertThrows(JSONException.class, () -> obj.getIntValue("val"));
    }

    @Test
    void intToBoolean_nonZero() {
        JSONObject obj = JSON.parseObject("{\"val\":1}");
        assertTrue(obj.getBooleanValue("val"));
    }

    @Test
    void intToBoolean_zero() {
        JSONObject obj = JSON.parseObject("{\"val\":0}");
        assertFalse(obj.getBooleanValue("val"));
    }

    // ==================== Null coercion ====================

    @Test
    void nullToInt_defaultZero() {
        JSONObject obj = JSON.parseObject("{\"val\":null}");
        assertEquals(0, obj.getIntValue("val"));
    }

    @Test
    void nullToLong_defaultZero() {
        JSONObject obj = JSON.parseObject("{\"val\":null}");
        assertEquals(0L, obj.getLongValue("val"));
    }

    @Test
    void nullToDouble_defaultZero() {
        JSONObject obj = JSON.parseObject("{\"val\":null}");
        assertEquals(0.0, obj.getDoubleValue("val"));
    }

    @Test
    void nullToBoolean_defaultFalse() {
        JSONObject obj = JSON.parseObject("{\"val\":null}");
        assertFalse(obj.getBooleanValue("val"));
    }

    @Test
    void nullToString_null() {
        JSONObject obj = JSON.parseObject("{\"val\":null}");
        assertNull(obj.getString("val"));
    }

    @Test
    void nullToBigDecimal_null() {
        JSONObject obj = JSON.parseObject("{\"val\":null}");
        assertNull(obj.getBigDecimal("val"));
    }

    @Test
    void nullToBigInteger_null() {
        JSONObject obj = JSON.parseObject("{\"val\":null}");
        assertNull(obj.getBigInteger("val"));
    }

    // ==================== Missing key coercion ====================

    @Test
    void missingKey_intDefault() {
        JSONObject obj = JSON.parseObject("{}");
        assertEquals(0, obj.getIntValue("missing"));
    }

    @Test
    void missingKey_longDefault() {
        JSONObject obj = JSON.parseObject("{}");
        assertEquals(0L, obj.getLongValue("missing"));
    }

    @Test
    void missingKey_boolDefault() {
        JSONObject obj = JSON.parseObject("{}");
        assertFalse(obj.getBooleanValue("missing"));
    }

    @Test
    void missingKey_stringNull() {
        JSONObject obj = JSON.parseObject("{}");
        assertNull(obj.getString("missing"));
    }

    // ==================== JSONArray coercion ====================

    @Test
    void arrayElement_intToString() {
        JSONArray arr = JSON.parseArray("[1,2,3]");
        assertEquals("1", arr.getString(0));
    }

    @Test
    void arrayElement_stringToInt() {
        JSONArray arr = JSON.parseArray("[\"42\"]");
        assertEquals(42, arr.getIntValue(0));
    }

    @Test
    void arrayElement_null() {
        JSONArray arr = JSON.parseArray("[null]");
        assertNull(arr.getString(0));
        assertEquals(0, arr.getIntValue(0));
    }

    // ==================== POJO field type coercion (number → field) ====================

    public static class IntBean {
        public int value;
    }

    @Test
    void intToIntField() {
        IntBean bean = JSON.parseObject("{\"value\":42}", IntBean.class);
        assertEquals(42, bean.value);
    }

    public static class LongBean {
        public long value;
    }

    @Test
    void longToLongField() {
        LongBean bean = JSON.parseObject("{\"value\":9999999999}", LongBean.class);
        assertEquals(9999999999L, bean.value);
    }

    public static class BoolBean {
        public boolean value;
    }

    @Test
    void boolToBoolField() {
        BoolBean bean = JSON.parseObject("{\"value\":true}", BoolBean.class);
        assertTrue(bean.value);
    }

    public static class DoubleBean {
        public double value;
    }

    @Test
    void doubleToDoubleField() {
        DoubleBean bean = JSON.parseObject("{\"value\":3.14}", DoubleBean.class);
        assertEquals(3.14, bean.value, 1e-10);
    }

    // Note: BigDecimal fields in POJOs are not yet supported by the POJO reader.
    // BigDecimal values should be accessed via JSONObject.getBigDecimal() instead.

    // ==================== Edge cases ====================

    @Test
    void emptyString_getIntValue() {
        JSONObject obj = JSON.parseObject("{\"val\":\"\"}");
        // Empty string coerces to 0 via getIntValue
        assertEquals(0, obj.getIntValue("val"));
    }

    @Test
    void nestedObject_getString() {
        JSONObject obj = JSON.parseObject("{\"val\":{\"a\":1}}");
        String str = obj.getString("val");
        // Should return JSON representation
        assertNotNull(str);
        assertTrue(str.contains("a"), "Should serialize nested: " + str);
    }

    @Test
    void nestedArray_getString() {
        JSONObject obj = JSON.parseObject("{\"val\":[1,2,3]}");
        String str = obj.getString("val");
        assertNotNull(str);
        assertTrue(str.contains("1"), "Should serialize nested: " + str);
    }

    // ==================== Typed getter consistency ====================

    @Test
    void getterConsistency_intAccessors() {
        JSONObject obj = JSON.parseObject("{\"val\":42}");
        assertEquals(42, obj.getIntValue("val"));
        assertEquals(42L, obj.getLongValue("val"));
        assertEquals(42.0, obj.getDoubleValue("val"), 1e-10);
        assertEquals("42", obj.getString("val"));
    }

    @Test
    void getterConsistency_doubleAccessors() {
        JSONObject obj = JSON.parseObject("{\"val\":3.14}");
        assertEquals(3, obj.getIntValue("val"));
        assertEquals(3L, obj.getLongValue("val"));
        assertEquals(3.14, obj.getDoubleValue("val"), 1e-10);
    }

    @Test
    void getterConsistency_boolAccessors() {
        JSONObject obj = JSON.parseObject("{\"val\":true}");
        assertTrue(obj.getBooleanValue("val"));
        assertEquals("true", obj.getString("val"));
    }
}

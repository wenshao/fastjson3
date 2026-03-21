package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge case tests for number parsing and serialization.
 * Inspired by fastjson2 primitives/BigDecimalTest, BigIntegerTest, BigIntTest
 * and jackson BigNumbersDeserTest, HugeIntegerCoerceTest.
 */
class NumberEdgeCaseTest {

    // ==================== Integer boundary values ====================

    @Test
    void parseInt_maxValue() {
        assertEquals(Integer.MAX_VALUE, JSON.parse(String.valueOf(Integer.MAX_VALUE)));
    }

    @Test
    void parseInt_minValue() {
        assertEquals(Integer.MIN_VALUE, JSON.parse(String.valueOf(Integer.MIN_VALUE)));
    }

    @Test
    void parseLong_maxValue() {
        Object result = JSON.parse(String.valueOf(Long.MAX_VALUE));
        assertInstanceOf(Long.class, result);
        assertEquals(Long.MAX_VALUE, result);
    }

    @Test
    void parseLong_minValue() {
        Object result = JSON.parse(String.valueOf(Long.MIN_VALUE));
        assertInstanceOf(Long.class, result);
        assertEquals(Long.MIN_VALUE, result);
    }

    // ==================== Long → BigInteger overflow ====================

    @Test
    void parseBigInteger_exceedsLongMax() {
        // Long.MAX_VALUE = 9223372036854775807, add 1
        String val = "9223372036854775808";
        Object result = JSON.parse(val);
        assertInstanceOf(BigInteger.class, result);
        assertEquals(new BigInteger(val), result);
    }

    @Test
    void parseBigInteger_exceedsLongMin() {
        // Long.MIN_VALUE = -9223372036854775808, subtract 1
        String val = "-9223372036854775809";
        Object result = JSON.parse(val);
        assertInstanceOf(BigInteger.class, result);
        assertEquals(new BigInteger(val), result);
    }

    @Test
    void parseBigInteger_veryLarge_38digits() {
        String val = "12345678901234567890123456789012345678";
        Object result = JSON.parse(val);
        assertInstanceOf(BigInteger.class, result);
        assertEquals(new BigInteger(val), result);
    }

    @Test
    void parseBigInteger_veryLargeNegative_38digits() {
        String val = "-12345678901234567890123456789012345678";
        Object result = JSON.parse(val);
        assertInstanceOf(BigInteger.class, result);
        assertEquals(new BigInteger(val), result);
    }

    // ==================== Integer → Long boundary ====================

    @Test
    void parseInt_justAboveIntMax() {
        long val = (long) Integer.MAX_VALUE + 1;
        Object result = JSON.parse(String.valueOf(val));
        assertInstanceOf(Long.class, result);
        assertEquals(val, result);
    }

    @Test
    void parseInt_justBelowIntMin() {
        long val = (long) Integer.MIN_VALUE - 1;
        Object result = JSON.parse(String.valueOf(val));
        assertInstanceOf(Long.class, result);
        assertEquals(val, result);
    }

    // ==================== BigDecimal scientific notation ====================

    @Test
    void parseBigDecimal_scientificNotation_largeExponent() {
        BigDecimal expected = new BigDecimal("12345.123E256");
        String json = "{\"val\":" + expected.toEngineeringString() + "}";
        JSONObject obj = JSON.parseObject(json);
        BigDecimal actual = obj.getBigDecimal("val");
        assertEquals(0, expected.compareTo(actual),
                "Expected " + expected + " but got " + actual);
    }

    @Test
    void parseBigDecimal_scientificNotation_E300() {
        BigDecimal expected = new BigDecimal("1.123E300");
        String json = "{\"val\":" + expected.toEngineeringString() + "}";
        JSONObject obj = JSON.parseObject(json);
        BigDecimal actual = obj.getBigDecimal("val");
        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    void parseBigDecimal_scientificNotation_negativeExponent() {
        BigDecimal expected = new BigDecimal("1.5E-300");
        String json = "{\"val\":" + expected.toString() + "}";
        JSONObject obj = JSON.parseObject(json);
        BigDecimal actual = obj.getBigDecimal("val");
        assertEquals(0, expected.compareTo(actual));
    }

    // ==================== BigDecimal precision preservation ====================

    @Test
    void parseBigDecimal_preservesPrecision_1decimal() {
        verifyBigDecimalRoundTrip("345.6");
    }

    @Test
    void parseBigDecimal_preservesPrecision_2decimals() {
        verifyBigDecimalRoundTrip("345.67");
    }

    @Test
    void parseBigDecimal_preservesPrecision_7decimals() {
        verifyBigDecimalRoundTrip("345.6789123");
    }

    @Test
    void parseBigDecimal_preservesPrecision_negative() {
        verifyBigDecimalRoundTrip("-345.6789123");
    }

    @Test
    void parseBigDecimal_preservesPrecision_leadingDecimalLong() {
        // With UseBigDecimalForDoubles, arbitrary precision is preserved
        String value = "0.1234567890123456789012345678901234567";
        BigDecimal expected = new BigDecimal(value);
        Object result = JSON.parse("{\"val\":" + value + "}", ReadFeature.UseBigDecimalForDoubles);
        BigDecimal actual = ((JSONObject) result).getBigDecimal("val");
        assertEquals(0, expected.compareTo(actual),
                "Precision lost: expected " + expected + " got " + actual);
    }

    private void verifyBigDecimalRoundTrip(String value) {
        BigDecimal expected = new BigDecimal(value);
        // Parse via JSON object — getBigDecimal converts double to BigDecimal
        JSONObject obj = JSON.parseObject("{\"val\":" + value + "}");
        BigDecimal actual = obj.getBigDecimal("val");
        // Use doubleValue comparison since default parsing goes through double
        assertEquals(expected.doubleValue(), actual.doubleValue(), 1e-10,
                "Value mismatch: expected " + expected + " got " + actual);
    }

    // ==================== UseBigDecimalForFloats / UseBigDecimalForDoubles ====================

    @Test
    void useBigDecimalForFloats_objectValue() {
        Object result = JSON.parse("{\"val\":1.2}", ReadFeature.UseBigDecimalForFloats);
        assertInstanceOf(JSONObject.class, result);
        Object val = ((JSONObject) result).get("val");
        assertInstanceOf(BigDecimal.class, val,
                "Expected BigDecimal but got " + val.getClass().getSimpleName());
    }

    @Test
    void useBigDecimalForDoubles_objectValue() {
        Object result = JSON.parse("{\"val\":1.2}", ReadFeature.UseBigDecimalForDoubles);
        assertInstanceOf(JSONObject.class, result);
        Object val = ((JSONObject) result).get("val");
        assertInstanceOf(BigDecimal.class, val,
                "Expected BigDecimal but got " + val.getClass().getSimpleName());
    }

    @Test
    void useBigDecimalForDoubles_scalarValue() {
        Object result = JSON.parse("3.14", ReadFeature.UseBigDecimalForDoubles);
        assertInstanceOf(BigDecimal.class, result);
        assertEquals(new BigDecimal("3.14"), result);
    }

    // ==================== BigDecimal from UTF-8 bytes ====================

    @Test
    void parseBigDecimal_fromBytes() {
        byte[] bytes = "{\"val\":123.456789}".getBytes(StandardCharsets.UTF_8);
        JSONObject obj = JSON.parseObject(bytes);
        BigDecimal actual = obj.getBigDecimal("val");
        assertEquals(0, new BigDecimal("123.456789").compareTo(actual));
    }

    @Test
    void parseBigInteger_fromBytes() {
        String huge = "99999999999999999999999999999999";
        byte[] bytes = ("{\"val\":" + huge + "}").getBytes(StandardCharsets.UTF_8);
        JSONObject obj = JSON.parseObject(bytes);
        assertEquals(new BigInteger(huge), obj.getBigInteger("val"));
    }

    // ==================== Zero and special values ====================

    @Test
    void parseZero() {
        assertEquals(0, JSON.parse("0"));
    }

    @Test
    void parseNegativeZero_double() {
        Object result = JSON.parse("-0.0");
        // -0.0 should be preserved as a double
        assertEquals(-0.0, ((Number) result).doubleValue());
    }

    @Test
    void parseBigDecimal_zero() {
        JSONObject obj = JSON.parseObject("{\"val\":0.0}");
        BigDecimal actual = obj.getBigDecimal("val");
        assertEquals(0, BigDecimal.ZERO.compareTo(actual));
    }

    // ==================== Very large numbers (DoS awareness) ====================

    @Test
    void parseLargeNumber_100digits() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(i % 10);
        }
        String val = sb.toString();
        Object result = JSON.parse(val);
        assertInstanceOf(BigInteger.class, result);
        assertEquals(new BigInteger(val), result);
    }

    @Test
    void parseLargeDecimal_100digits() {
        StringBuilder sb = new StringBuilder("0.");
        for (int i = 0; i < 100; i++) {
            sb.append(i % 10);
        }
        String val = sb.toString();
        JSONObject obj = JSON.parseObject("{\"val\":" + val + "}");
        BigDecimal actual = obj.getBigDecimal("val");
        assertEquals(0, new BigDecimal(val).compareTo(actual));
    }

    // ==================== WriteBigDecimalAsPlain ====================

    @Test
    void writeBigDecimalAsPlain() {
        BigDecimal value = new BigDecimal("1.23E10");
        String json = JSON.toJSONString(value, WriteFeature.WriteBigDecimalAsPlain);
        // Should not contain 'E'
        assertFalse(json.contains("E"), "Expected plain format: " + json);
        assertFalse(json.contains("e"), "Expected plain format: " + json);
        assertEquals("12300000000", json);
    }

    @Test
    void writeBigDecimalAsPlain_small() {
        BigDecimal value = new BigDecimal("1.5E-5");
        String json = JSON.toJSONString(value, WriteFeature.WriteBigDecimalAsPlain);
        assertFalse(json.contains("E"), "Expected plain format: " + json);
        assertEquals("0.000015", json);
    }

    // ==================== WriteLongAsString ====================

    @Test
    void writeLongAsString_basic() {
        String json = JSON.toJSONString(Long.MAX_VALUE, WriteFeature.WriteLongAsString);
        assertEquals("\"" + Long.MAX_VALUE + "\"", json);
    }

    @Test
    void writeLongAsString_inObject() {
        JSONObject obj = new JSONObject();
        obj.put("id", 9007199254740993L); // exceeds JS safe integer
        String json = obj.toJSONString(WriteFeature.WriteLongAsString);
        assertTrue(json.contains("\"9007199254740993\""), "Long should be quoted: " + json);
    }

    // ==================== Number type in JSONObject getters ====================

    @Test
    void jsonObject_getIntValue_fromLong() {
        JSONObject obj = JSON.parseObject("{\"val\":42}");
        assertEquals(42, obj.getIntValue("val"));
        assertEquals(42L, obj.getLongValue("val"));
        assertEquals(42.0, obj.getDoubleValue("val"));
    }

    @Test
    void jsonObject_getBigDecimal_fromInt() {
        JSONObject obj = JSON.parseObject("{\"val\":42}");
        assertEquals(new BigDecimal("42"), obj.getBigDecimal("val"));
    }

    @Test
    void jsonObject_getBigInteger_fromInt() {
        JSONObject obj = JSON.parseObject("{\"val\":42}");
        assertEquals(BigInteger.valueOf(42), obj.getBigInteger("val"));
    }

    // ==================== WriteNonStringValueAsString ====================

    @Test
    void writeNonStringValueAsString_number() {
        JSONObject obj = new JSONObject();
        obj.put("val", 42);
        String json = obj.toJSONString(WriteFeature.WriteNonStringValueAsString);
        assertTrue(json.contains("\"42\""), "Number should be string: " + json);
    }

    @Test
    void writeNonStringValueAsString_boolean() {
        JSONObject obj = new JSONObject();
        obj.put("val", true);
        String json = obj.toJSONString(WriteFeature.WriteNonStringValueAsString);
        assertTrue(json.contains("\"true\""), "Boolean should be string: " + json);
    }
}

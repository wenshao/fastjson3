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

    // ==================== Decimal round-trip (double-level precision) ====================

    @Test
    void parseDecimal_preservesDoublePrecision_1decimal() {
        verifyDoubleRoundTrip("345.6");
    }

    @Test
    void parseDecimal_preservesDoublePrecision_2decimals() {
        verifyDoubleRoundTrip("345.67");
    }

    @Test
    void parseDecimal_preservesDoublePrecision_7decimals() {
        verifyDoubleRoundTrip("345.6789123");
    }

    @Test
    void parseDecimal_preservesDoublePrecision_negative() {
        verifyDoubleRoundTrip("-345.6789123");
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

    /**
     * Verifies that a decimal value survives a JSON round-trip at double-level precision.
     * Default parsing goes through double, so this comparison uses doubleValue() rather than
     * full BigDecimal precision.
     */
    private void verifyDoubleRoundTrip(String value) {
        BigDecimal expected = new BigDecimal(value);
        // Parse via JSON object — getBigDecimal converts double to BigDecimal
        JSONObject obj = JSON.parseObject("{\"val\":" + value + "}");
        BigDecimal actual = obj.getBigDecimal("val");
        // Use doubleValue comparison since default parsing goes through double
        assertEquals(expected.doubleValue(), actual.doubleValue(), 1e-10,
                "Value mismatch: expected " + expected + " got " + actual);
    }

    // ==================== UseBigDecimalForDoubles ====================

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
        // -0.0 should be preserved as a double; use bit comparison since -0.0 == 0.0 in Java
        double value = ((Number) result).doubleValue();
        assertEquals(Double.doubleToRawLongBits(-0.0), Double.doubleToRawLongBits(value));
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
        // Start with a non-zero digit to ensure valid large integer
        sb.append('1');
        for (int i = 1; i < 100; i++) {
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
        // Default parsing goes through double which loses precision for 100-digit decimals.
        // Compare using doubleValue() to verify the value is approximately correct.
        JSONObject obj = JSON.parseObject("{\"val\":" + val + "}");
        BigDecimal actual = obj.getBigDecimal("val");
        BigDecimal expected = new BigDecimal(val);
        assertEquals(expected.doubleValue(), actual.doubleValue(), 1e-15,
                "Value mismatch: expected " + expected + " got " + actual);
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
        BigDecimal actual = obj.getBigDecimal("val");
        assertEquals(0, new BigDecimal("42").compareTo(actual),
                "Expected 42 but got " + actual);
    }

    @Test
    void jsonObject_getBigInteger_fromInt() {
        JSONObject obj = JSON.parseObject("{\"val\":42}");
        assertEquals(BigInteger.valueOf(42), obj.getBigInteger("val"));
    }

    // ==================== Top-level BigDecimal / BigInteger target ====================

    @Test
    void topLevelBigDecimal_fromFloatLiteral() {
        // parseObject("3.14", BigDecimal.class) used to throw
        // ClassCastException: Double cannot be cast to BigDecimal
        BigDecimal bd = JSON.parseObject("3.14", BigDecimal.class);
        assertEquals(new BigDecimal("3.14"), bd);
    }

    @Test
    void topLevelBigDecimal_preservesPrecision() {
        // Round-tripping via Double would truncate to 15–17 significant digits.
        BigDecimal bd = JSON.parseObject("3.141592653589793238462643383279", BigDecimal.class);
        assertEquals(new BigDecimal("3.141592653589793238462643383279"), bd);
    }

    @Test
    void topLevelBigDecimal_null() {
        assertNull(JSON.parseObject("null", BigDecimal.class));
    }

    @Test
    void topLevelBigInteger_fromInt() {
        assertEquals(BigInteger.valueOf(42), JSON.parseObject("42", BigInteger.class));
    }

    @Test
    void topLevelBigInteger_fromLargeInt() {
        BigInteger bi = JSON.parseObject("12345678901234567890", BigInteger.class);
        assertEquals(new BigInteger("12345678901234567890"), bi);
    }

    @Test
    void topLevelBigInteger_truncatesFloat() {
        // Match Jackson tolerance: 3.14 on a BigInteger target rounds down to 3.
        BigInteger bi = JSON.parseObject("3.14", BigInteger.class);
        assertEquals(BigInteger.valueOf(3), bi);
    }

    // ==================== Record / POJO component BigDecimal + BigInteger ====================

    public record MoneyRec(BigDecimal price, BigInteger txId) {
    }

    public static class MoneyPojo {
        public BigDecimal price;
        public BigInteger txId;
    }

    @Test
    void recordBigDecimalBigIntegerFields() {
        // Record canonical-ctor path; used to throw "argument type mismatch"
        // because convertValue produced a Double for a BigDecimal component.
        MoneyRec r = JSON.parseObject(
                "{\"price\":99.95,\"txId\":12345678901234567890}", MoneyRec.class);
        assertEquals(new BigDecimal("99.95"), r.price);
        assertEquals(new BigInteger("12345678901234567890"), r.txId);
    }

    @Test
    void pojoBigDecimalBigIntegerFields() {
        MoneyPojo p = JSON.parseObject(
                "{\"price\":99.95,\"txId\":12345678901234567890}", MoneyPojo.class);
        assertEquals(new BigDecimal("99.95"), p.price);
        assertEquals(new BigInteger("12345678901234567890"), p.txId);
    }

    @Test
    void pojoBigDecimalFromStringValue() {
        // "99.95" as a quoted string still produces the decimal value — common
        // in APIs that return money amounts as strings to dodge JS precision.
        MoneyPojo p = JSON.parseObject("{\"price\":\"99.95\",\"txId\":\"42\"}", MoneyPojo.class);
        assertEquals(new BigDecimal("99.95"), p.price);
        assertEquals(BigInteger.valueOf(42), p.txId);
    }

    // ==================== In-parser dispatch accepts quoted strings ====================
    // Round-1 audit: the read(Class) fast path called readBigDecimalLiteral
    // directly and threw on quoted input, while BuiltinCodecs accepted it —
    // different behaviour for the same input depending on entry point. The
    // parseObject variant with ReadFeature overrides uses the in-parser path.

    @Test
    void topLevelBigDecimalFromQuotedStringWithFeature() {
        BigDecimal bd = JSON.parseObject("\"3.14\"", BigDecimal.class,
                ReadFeature.UseBigDecimalForDoubles);
        assertEquals(new BigDecimal("3.14"), bd);
    }

    @Test
    void topLevelBigDecimalFromQuotedStringDefault() {
        BigDecimal bd = JSON.parseObject("\"99.95\"", BigDecimal.class);
        assertEquals(new BigDecimal("99.95"), bd);
    }

    @Test
    void topLevelBigIntegerFromQuotedString() {
        BigInteger bi = JSON.parseObject("\"12345678901234567890\"", BigInteger.class);
        assertEquals(new BigInteger("12345678901234567890"), bi);
    }

    @Test
    void topLevelBigIntegerFromQuotedFractionalString() {
        // Jackson-style tolerance: "3.14" on a BigInteger target truncates to 3.
        BigInteger bi = JSON.parseObject("\"3.14\"", BigInteger.class);
        assertEquals(BigInteger.valueOf(3), bi);
    }

    // Round-2 audit: BuiltinCodecs previously duplicated the quote-peek with
    // an unguarded charAt, so whitespace-only input blew past end() with
    // AIOOBE, and single-quote bypassed the AllowSingleQuotes feature check.

    @Test
    void whitespaceOnlyInputRaisesCleanJsonException() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parseObject("   ", BigDecimal.class));
        assertTrue(ex.getMessage().toLowerCase().contains("unexpected")
                        || ex.getMessage().toLowerCase().contains("number"),
                "expected clean number-parse error, got: " + ex.getMessage());
    }

    @Test
    void singleQuoteInputRejectedWithoutAllowSingleQuotesFeature() {
        // readString rejects single quotes without the feature; BuiltinCodecs
        // must not bypass that gate with its own quote-peek.
        assertThrows(Exception.class,
                () -> JSON.parseObject("'3.14'", BigDecimal.class));
    }

    // Round-4: NFE/ArithmeticException must not leak as non-JSONException
    // (violates framework contract). Plus a digit cap on BigInteger
    // toBigInteger expansion to block DoS amplification where 16-byte
    // input -> ~400 KB bignum.

    @Test
    void bigDecimalMalformedQuotedInputRaisesJsonException() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parseObject("\"abc\"", BigDecimal.class));
        assertTrue(ex.getMessage().contains("abc")
                        || ex.getMessage().toLowerCase().contains("invalid"),
                ex.getMessage());
    }

    @Test
    void bigDecimalNaNQuotedRaisesJsonException() {
        assertThrows(JSONException.class,
                () -> JSON.parseObject("\"NaN\"", BigDecimal.class));
    }

    @Test
    void bigIntegerMalformedQuotedInputRaisesJsonException() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parseObject("\"abc\"", BigInteger.class));
        assertTrue(ex.getMessage().contains("abc")
                        || ex.getMessage().toLowerCase().contains("invalid"),
                ex.getMessage());
    }

    @Test
    void bigIntegerHugeExponentRejectedToBlockDoSAmplification() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parseObject("1e1000000", BigInteger.class));
        assertTrue(ex.getMessage().contains("digits")
                        || ex.getMessage().toLowerCase().contains("exceeds"),
                ex.getMessage());
    }

    @Test
    void bigIntegerHugeExponentFromQuotedStringRejected() {
        assertThrows(JSONException.class,
                () -> JSON.parseObject("\"1e1000000\"", BigInteger.class));
    }

    @Test
    void bigIntegerReasonableExponentStillAccepted() {
        // 100 digits — well under the 4096 cap. Covers legitimate big-int use.
        BigInteger bi = JSON.parseObject("1e100", BigInteger.class);
        assertEquals(new BigInteger("1" + "0".repeat(100)), bi);
    }

    public static class BigDecPojo {
        public BigDecimal price;
    }

    public static class BigIntPojo {
        public BigInteger count;
    }

    @Test
    void pojoBigDecimalMalformedStringRaisesJsonException() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"price\":\"abc\"}", BigDecPojo.class));
        assertTrue(ex.getMessage().contains("abc")
                        || ex.getMessage().toLowerCase().contains("invalid"),
                ex.getMessage());
    }

    @Test
    void pojoBigIntegerHugeExponentStringRejected() {
        assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"count\":\"1e1000000\"}", BigIntPojo.class));
    }

}

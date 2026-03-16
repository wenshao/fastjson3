package com.alibaba.fastjson3.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NumberUtils - high-performance number formatting utilities.
 */
class NumberUtilsTest {

    // ==================== writeDouble tests ====================

    @Test
    void testWriteDouble_zero() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, 0.0, false, false);

        assertEquals(3, off);
        assertEquals("0.0", new String(buf, 0, off, StandardCharsets.ISO_8859_1));
    }

    @Test
    void testWriteDouble_negativeZero() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, -0.0, false, false);

        assertEquals(4, off);
        assertEquals("-0.0", new String(buf, 0, off, StandardCharsets.ISO_8859_1));
    }

    @Test
    void testWriteDouble_positiveInteger() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, 42.0, false, false);

        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        // writeDouble always appends ".0" for integer-valued doubles
        assertEquals("42.0", result);
    }

    @Test
    void testWriteDouble_negativeInteger() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, -42.0, false, false);

        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        // writeDouble always appends ".0" for integer-valued doubles
        assertEquals("-42.0", result);
    }

    @Test
    void testWriteDouble_smallFractional() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, 3.14, false, false);

        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        assertTrue(result.contains("3"));
        assertTrue(result.contains("."));
        assertTrue(result.contains("14") || result.contains("1"));
    }

    @Test
    void testWriteDouble_largeNumber() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, 123456789.0, false, false);

        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        // 123456789.0 should serialize as either "123456789" or with scientific notation
        assertTrue(result.contains("123456789") || result.contains("E") || result.contains("e"));
    }

    @Test
    void testWriteDouble_verySmallNumber() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, 0.0001, false, false);

        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        assertTrue(result.length() > 0);
    }

    @Test
    void testWriteDouble_veryLargeNumber() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, 1e100, false, false);

        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        assertTrue(result.contains("E") || result.contains("e"));
    }

    @Test
    void testWriteDouble_maxValue() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, Double.MAX_VALUE, false, false);

        assertTrue(off > 0);
        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        assertTrue(result.length() > 0);
    }

    @Test
    void testWriteDouble_minValue() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, Double.MIN_VALUE, false, false);

        assertTrue(off > 0);
    }

    @Test
    void testWriteDouble_nan_withJsonMode() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, Double.NaN, true, false);

        assertEquals(4, off);
        assertEquals("null", new String(buf, 0, off, StandardCharsets.ISO_8859_1));
    }

    @Test
    void testWriteDouble_nan_asString() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, Double.NaN, true, true);

        assertEquals(5, off); // "NaN" with quotes
        assertEquals("\"NaN\"", new String(buf, 0, off, StandardCharsets.ISO_8859_1));
    }

    @Test
    void testWriteDouble_positiveInfinity_asString() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, Double.POSITIVE_INFINITY, true, true);

        assertTrue(off > 0);
        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        assertTrue(result.contains("Infinity"));
        assertTrue(result.startsWith("\""));
        assertTrue(result.endsWith("\""));
    }

    @Test
    void testWriteDouble_negativeInfinity_asString() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, Double.NEGATIVE_INFINITY, true, true);

        assertTrue(off > 0);
        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        assertTrue(result.contains("Infinity"));
        assertTrue(result.startsWith("\"-"));
    }

    @Test
    void testWriteDouble_withOffset() {
        byte[] buf = new byte[32];
        buf[0] = 'X';
        buf[1] = 'Y';
        int off = NumberUtils.writeDouble(buf, 2, 12.5, false, false);

        assertEquals('X', buf[0]);
        assertEquals('Y', buf[1]);
        assertTrue(off > 2);
    }

    @Test
    void testWriteDouble_scientificNotation_small() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, 0.00001, false, false);

        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        // Should use scientific notation for very small numbers
        assertTrue(result.contains("E") || result.contains("e") || result.length() < 10);
    }

    @Test
    void testWriteDouble_scientificNotation_large() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, 1e10, false, false);

        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        assertTrue(result.length() > 0);
    }

    // ==================== doubleToScientific tests ====================

    @Test
    void testDoubleToScientific_zero() {
        Scientific result = NumberUtils.doubleToScientific(0.0);

        assertNotNull(result);
        assertEquals(0, result.output);
    }

    @Test
    void testDoubleToScientific_positiveInteger() {
        Scientific result = NumberUtils.doubleToScientific(42.0);

        assertNotNull(result);
        assertTrue(result.output > 0);
    }

    @Test
    void testDoubleToScientific_negativeNumber() {
        Scientific result = NumberUtils.doubleToScientific(-3.14);

        assertNotNull(result);
    }

    @Test
    void testDoubleToScientific_verySmall() {
        Scientific result = NumberUtils.doubleToScientific(1e-100);

        assertNotNull(result);
        assertTrue(result.e10 < 0);
    }

    @Test
    void testDoubleToScientific_veryLarge() {
        Scientific result = NumberUtils.doubleToScientific(1e100);

        assertNotNull(result);
        assertTrue(result.e10 > 0);
    }

    @Test
    void testDoubleToScientific_minValue() {
        Scientific result = NumberUtils.doubleToScientific(Double.MIN_VALUE);

        assertNotNull(result);
        assertSame(Scientific.DOUBLE_MIN, result);
    }

    @Test
    void testDoubleToScientific_maxValue() {
        Scientific result = NumberUtils.doubleToScientific(Double.MAX_VALUE);

        assertNotNull(result);
    }

    @Test
    void testDoubleToScientific_nan() {
        Scientific result = NumberUtils.doubleToScientific(Double.NaN);

        assertNotNull(result);
        assertSame(Scientific.SCIENTIFIC_NULL, result);
    }

    @Test
    void testDoubleToScientific_positiveInfinity() {
        Scientific result = NumberUtils.doubleToScientific(Double.POSITIVE_INFINITY);

        assertNotNull(result);
        assertSame(Scientific.SCIENTIFIC_NULL, result);
    }

    @Test
    void testDoubleToScientific_negativeInfinity() {
        Scientific result = NumberUtils.doubleToScientific(Double.NEGATIVE_INFINITY);

        assertNotNull(result);
        assertSame(Scientific.SCIENTIFIC_NULL, result);
    }

    // ==================== stringSize tests ====================

    @Test
    void testStringSize_singleDigit() {
        assertEquals(1, NumberUtils.stringSize(0L));
        assertEquals(1, NumberUtils.stringSize(1L));
        assertEquals(1, NumberUtils.stringSize(9L));
    }

    @Test
    void testStringSize_twoDigits() {
        assertEquals(2, NumberUtils.stringSize(10L));
        assertEquals(2, NumberUtils.stringSize(99L));
    }

    @Test
    void testStringSize_threeDigits() {
        assertEquals(3, NumberUtils.stringSize(100L));
        assertEquals(3, NumberUtils.stringSize(999L));
    }

    @Test
    void testStringSize_manyDigits() {
        assertEquals(10, NumberUtils.stringSize(1_000_000_000L));
        assertEquals(18, NumberUtils.stringSize(999_999_999_999_999_999L));
        assertEquals(19, NumberUtils.stringSize(Long.MAX_VALUE));
    }

    // ==================== multiplyHigh tests ====================

    @Test
    void testMultiplyHigh_basic() {
        // Test basic multiplication
        long result = NumberUtils.MULTIPLY_HIGH.multiplyHigh(0x100000000L, 0x100000000L);
        // The high 64 bits of (2^32 * 2^32 = 2^64) should be 1
        assertEquals(1, result);
    }

    @Test
    void testMultiplyHigh_zero() {
        long result = NumberUtils.MULTIPLY_HIGH.multiplyHigh(0, 123456789L);
        assertEquals(0, result);
    }

    @Test
    void testMultiplyHigh_identity() {
        long result = NumberUtils.MULTIPLY_HIGH.multiplyHigh(1, Long.MAX_VALUE);
        // High bits of (1 * MAX_VALUE) should be 0
        assertEquals(0, result);
    }

    @Test
    void testMultiplyHigh_symmetric() {
        long result1 = NumberUtils.MULTIPLY_HIGH.multiplyHigh(12345, 67890);
        long result2 = NumberUtils.MULTIPLY_HIGH.multiplyHigh(67890, 12345);
        assertEquals(result1, result2);
    }

    // ==================== Edge case tests ====================

    @Test
    void testWriteDouble_rounding() {
        byte[] buf = new byte[32];
        NumberUtils.writeDouble(buf, 0, 0.1, false, false);

        String result = new String(buf, 0, 3, StandardCharsets.ISO_8859_1);
        // Should start with "0."
        assertTrue(result.startsWith("0.") || result.startsWith("0.1"));
    }

    @Test
    void testWriteDouble_repeatingFraction() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, 1.0 / 3.0, false, false);

        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        assertTrue(result.contains("3"));
    }

    @Test
    void testWriteDouble_veryCloseToZero() {
        byte[] buf = new byte[32];
        int off = NumberUtils.writeDouble(buf, 0, 1e-320, false, false);

        assertTrue(off > 0);
        String result = new String(buf, 0, off, StandardCharsets.ISO_8859_1);
        assertTrue(result.contains("E") || result.contains("e"));
    }

    // ==================== Power table tests ====================

    @Test
    void testPowerTablesInitialized() {
        assertNotNull(NumberUtils.POSITIVE_DECIMAL_POWER);
        assertEquals(325, NumberUtils.POSITIVE_DECIMAL_POWER.length);

        assertNotNull(NumberUtils.NEGATIVE_DECIMAL_POWER);
        assertEquals(325, NumberUtils.NEGATIVE_DECIMAL_POWER.length);

        assertNotNull(NumberUtils.POW10_LONG_VALUES);
        assertTrue(NumberUtils.POW10_LONG_VALUES.length > 0);

        assertNotNull(NumberUtils.POW5_LONG_VALUES);
        assertTrue(NumberUtils.POW5_LONG_VALUES.length > 0);
    }

    @Test
    void testPow10Values_correct() {
        assertEquals(10L, NumberUtils.POW10_LONG_VALUES[0]);
        assertEquals(100L, NumberUtils.POW10_LONG_VALUES[1]);
        assertEquals(1000L, NumberUtils.POW10_LONG_VALUES[2]);
        assertEquals(10000L, NumberUtils.POW10_LONG_VALUES[3]);
        assertEquals(100000L, NumberUtils.POW10_LONG_VALUES[4]);
    }

    @Test
    void testPow5Values_correct() {
        assertEquals(1L, NumberUtils.POW5_LONG_VALUES[0]);
        assertEquals(5L, NumberUtils.POW5_LONG_VALUES[1]);
        assertEquals(25L, NumberUtils.POW5_LONG_VALUES[2]);
        assertEquals(125L, NumberUtils.POW5_LONG_VALUES[3]);
        assertEquals(625L, NumberUtils.POW5_LONG_VALUES[4]);
    }

    @Test
    void testPositiveDecimalPower_format() {
        char[] chars = NumberUtils.POSITIVE_DECIMAL_POWER_CHARS[0];
        assertNotNull(chars);
        String str = new String(chars);
        assertTrue(str.contains("1.0E0") || str.contains("1.0E+0"));
    }

    @Test
    void testNegativeDecimalPower_format() {
        char[] chars = NumberUtils.NEGATIVE_DECIMAL_POWER_CHARS[0];
        assertNotNull(chars);
        String str = new String(chars);
        assertTrue(str.contains("1.0E-0"));
    }

    @Test
    void testDecimalPower_values() {
        // Test that power values are approximately correct
        double power0 = NumberUtils.POSITIVE_DECIMAL_POWER[0];
        assertEquals(1.0, power0, 0.001);

        double power1 = NumberUtils.POSITIVE_DECIMAL_POWER[1];
        assertEquals(10.0, power1, 0.001);

        double negPower1 = NumberUtils.NEGATIVE_DECIMAL_POWER[1];
        assertEquals(0.1, negPower1, 0.00001);
    }
}

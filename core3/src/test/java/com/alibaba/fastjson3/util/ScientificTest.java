package com.alibaba.fastjson3.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Scientific - scientific notation representation for number formatting.
 */
class ScientificTest {

    // ==================== Constructor tests ====================

    @Test
    void testConstructor_standard() {
        Scientific s = new Scientific(12345L, 5, 2);

        assertEquals(12345L, s.output);
        assertEquals(5, s.count);
        assertEquals(2, s.e10);
        assertFalse(s.b);
    }

    @Test
    void testConstructor_special() {
        Scientific s = new Scientific(-5, true);

        assertEquals(-5, s.e10);
        assertTrue(s.b);
        assertEquals(0, s.output);
        assertEquals(0, s.count);
    }

    @Test
    void testConstructor_zeroE10() {
        Scientific s = new Scientific(0, true);

        assertEquals(0, s.e10);
        assertTrue(s.b);
        assertEquals(0, s.output);
        assertEquals(0, s.count);
    }

    @Test
    void testConstructor_positiveE10() {
        Scientific s = new Scientific(10, true);

        assertEquals(10, s.e10);
        assertTrue(s.b);
    }

    // ==================== Constants tests ====================

    @Test
    void testSCIENTIFIC_NULL_constant() {
        assertSame(Scientific.SCIENTIFIC_NULL, Scientific.SCIENTIFIC_NULL);
        assertTrue(Scientific.SCIENTIFIC_NULL.b);
        assertEquals(0, Scientific.SCIENTIFIC_NULL.output);
        assertEquals(0, Scientific.SCIENTIFIC_NULL.count);
    }

    @Test
    void testZERO_constant() {
        assertSame(Scientific.ZERO, Scientific.ZERO);
        assertEquals(0, Scientific.ZERO.output);
        assertEquals(3, Scientific.ZERO.count);
        assertEquals(0, Scientific.ZERO.e10);
        assertFalse(Scientific.ZERO.b);
    }

    @Test
    void testNEGATIVE_ZERO_constant() {
        assertSame(Scientific.NEGATIVE_ZERO, Scientific.NEGATIVE_ZERO);
        assertEquals(0, Scientific.NEGATIVE_ZERO.output);
        assertEquals(3, Scientific.NEGATIVE_ZERO.count);
        assertEquals(0, Scientific.NEGATIVE_ZERO.e10);
        assertFalse(Scientific.NEGATIVE_ZERO.b);
    }

    @Test
    void testDOUBLE_MIN_constant() {
        assertSame(Scientific.DOUBLE_MIN, Scientific.DOUBLE_MIN);
        assertEquals(49, Scientific.DOUBLE_MIN.output);
        assertEquals(2, Scientific.DOUBLE_MIN.count);
        assertEquals(-324, Scientific.DOUBLE_MIN.e10);
        assertFalse(Scientific.DOUBLE_MIN.b);
    }

    // ==================== toString tests ====================

    @Test
    void testToString_SCIENTIFIC_NULL() {
        assertEquals("null", Scientific.SCIENTIFIC_NULL.toString());
    }

    @Test
    void testToString_ZERO() {
        assertEquals("0.0", Scientific.ZERO.toString());
    }

    @Test
    void testToString_NEGATIVE_ZERO() {
        assertEquals("-0.0", Scientific.NEGATIVE_ZERO.toString());
    }

    @Test
    void testToString_specialTrue() {
        Scientific s = new Scientific(5, true);
        assertEquals("1e5", s.toString());
    }

    @Test
    void testToString_specialNegative() {
        Scientific s = new Scientific(-3, true);
        assertEquals("1e-3", s.toString());
    }

    @Test
    void testToString_standard() {
        Scientific s = new Scientific(12345L, 5, 2);
        assertEquals("12345|2", s.toString());
    }

    @Test
    void testToString_zeroE10Standard() {
        Scientific s = new Scientific(100L, 3, 0);
        assertEquals("100|0", s.toString());
    }

    @Test
    void testToString_negativeE10() {
        Scientific s = new Scientific(500L, 1, -2);
        assertEquals("500|-2", s.toString());
    }

    // ==================== Equality tests ====================

    @Test
    void testEquality_sameValues() {
        Scientific s1 = new Scientific(123L, 3, 1);
        Scientific s2 = new Scientific(123L, 3, 1);

        // Scientific doesn't override equals(), so this uses reference equality
        assertNotSame(s1, s2);
    }

    @Test
    void testEquality_constantsAreSingletons() {
        assertSame(Scientific.SCIENTIFIC_NULL, Scientific.SCIENTIFIC_NULL);
        assertSame(Scientific.ZERO, Scientific.ZERO);
        assertSame(Scientific.NEGATIVE_ZERO, Scientific.NEGATIVE_ZERO);
        assertSame(Scientific.DOUBLE_MIN, Scientific.DOUBLE_MIN);
    }

    @Test
    void testEquality_ZERO_and_NEGATIVE_ZERO_different() {
        // ZERO and NEGATIVE_ZERO are different constant instances
        assertNotSame(Scientific.ZERO, Scientific.NEGATIVE_ZERO);
    }

    // ==================== Field immutability tests ====================

    @Test
    void testFields_arePublicFinal() {
        // All fields should be publicly accessible
        Scientific s = new Scientific(100L, 3, 1);

        assertEquals(100L, s.output);
        assertEquals(3, s.count);
        assertEquals(1, s.e10);
        assertFalse(s.b);
    }

    // ==================== Edge case tests ====================

    @Test
    void testConstructor_largeOutput() {
        long largeValue = Long.MAX_VALUE;
        Scientific s = new Scientific(largeValue, 19, 18);

        assertEquals(largeValue, s.output);
        assertEquals(19, s.count);
        assertEquals(18, s.e10);
    }

    @Test
    void testConstructor_negativeE10() {
        Scientific s = new Scientific(5L, 1, -100);

        assertEquals(-100, s.e10);
        assertEquals(5L, s.output);
        assertEquals(1, s.count);
    }

    @Test
    void testConstructor_largeE10() {
        Scientific s = new Scientific(1L, 1, 308);

        assertEquals(308, s.e10);
    }

    @Test
    void testConstructor_special_largeE10() {
        Scientific s = new Scientific(308, true);

        assertEquals(308, s.e10);
        assertTrue(s.b);
        assertEquals("1e308", s.toString());
    }

    @Test
    void testConstructor_special_smallE10() {
        Scientific s = new Scientific(-324, true);

        assertEquals(-324, s.e10);
        assertTrue(s.b);
        assertEquals("1e-324", s.toString());
    }

    // ==================== toString edge cases ====================

    @Test
    void testToString_specialVeryLargeE10() {
        Scientific s = new Scientific(1000, true);
        assertEquals("1e1000", s.toString());
    }

    @Test
    void testToString_specialVerySmallE10() {
        Scientific s = new Scientific(-1000, true);
        assertEquals("1e-1000", s.toString());
    }

    @Test
    void testToString_standardZeroOutput() {
        Scientific s = new Scientific(0L, 0, 5);
        assertEquals("0|5", s.toString());
    }

    // ==================== Semantic correctness tests ====================

    @Test
    void testZERO_semantic() {
        // ZERO represents the value 0.0
        assertEquals(0, Scientific.ZERO.output);
        assertEquals(0, Scientific.ZERO.e10);
        assertEquals("0.0", Scientific.ZERO.toString());
    }

    @Test
    void testNEGATIVE_ZERO_semantic() {
        // NEGATIVE_ZERO represents the value -0.0
        assertEquals(0, Scientific.NEGATIVE_ZERO.output);
        assertEquals(0, Scientific.NEGATIVE_ZERO.e10);
        assertEquals("-0.0", Scientific.NEGATIVE_ZERO.toString());
    }

    @Test
    void testDOUBLE_MIN_semantic() {
        // DOUBLE_MIN represents Double.MIN_VALUE (4.9E-324)
        // The output=49, count=2, e10=-324 represents "49" * 10^-324 * 10^-2 = 4.9E-324
        assertEquals(49, Scientific.DOUBLE_MIN.output);
        assertEquals(2, Scientific.DOUBLE_MIN.count);
        assertEquals(-324, Scientific.DOUBLE_MIN.e10);
        assertEquals("49|-324", Scientific.DOUBLE_MIN.toString());
    }

    @Test
    void testSCIENTIFIC_NULL_semantic() {
        // SCIENTIFIC_NULL represents null/NaN/Infinity values
        assertTrue(Scientific.SCIENTIFIC_NULL.b);
        assertEquals("null", Scientific.SCIENTIFIC_NULL.toString());
    }
}

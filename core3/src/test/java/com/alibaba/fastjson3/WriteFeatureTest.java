package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WriteFeature - serialization feature flags.
 */
class WriteFeatureTest {

    // ==================== Enum values tests ====================

    @Test
    void testEnumValues_allPresent() {
        WriteFeature[] values = WriteFeature.values();

        assertEquals(29, values.length);
    }

    @Test
    void testEnumValue_ordinals() {
        assertEquals(0, WriteFeature.FieldBased.ordinal());
        assertEquals(1, WriteFeature.PrettyFormat.ordinal());
        assertEquals(2, WriteFeature.WriteNulls.ordinal());
        assertEquals(3, WriteFeature.WriteNullListAsEmpty.ordinal());
        assertEquals(4, WriteFeature.WriteNullStringAsEmpty.ordinal());
        assertEquals(5, WriteFeature.WriteNullNumberAsZero.ordinal());
        assertEquals(6, WriteFeature.WriteNullBooleanAsFalse.ordinal());
        assertEquals(7, WriteFeature.WriteEnumsUsingName.ordinal());
        assertEquals(8, WriteFeature.WriteEnumUsingToString.ordinal());
        assertEquals(9, WriteFeature.WriteClassName.ordinal());
        assertEquals(10, WriteFeature.SortMapEntriesByKeys.ordinal());
        assertEquals(11, WriteFeature.EscapeNoneAscii.ordinal());
        assertEquals(12, WriteFeature.WriteBigDecimalAsPlain.ordinal());
        assertEquals(13, WriteFeature.WriteLongAsString.ordinal());
        assertEquals(14, WriteFeature.WriteByteArrayAsBase64.ordinal());
        assertEquals(15, WriteFeature.BeanToArray.ordinal());
        assertEquals(16, WriteFeature.ReferenceDetection.ordinal());
        assertEquals(17, WriteFeature.BrowserCompatible.ordinal());
        assertEquals(18, WriteFeature.WriteNonStringValueAsString.ordinal());
        assertEquals(19, WriteFeature.OptimizedForAscii.ordinal());
    }

    // ==================== Mask tests ====================

    @Test
    void testMask_uniquePowerOfTwo() {
        WriteFeature[] values = WriteFeature.values();
        long[] masks = new long[values.length];

        for (int i = 0; i < values.length; i++) {
            masks[i] = values[i].mask;
            // Each mask should be a power of 2
            assertTrue(Long.bitCount(masks[i]) == 1,
                    "Mask should have exactly 1 bit set: " + values[i] + " = " + masks[i]);
        }

        // All masks should be unique
        Arrays.sort(masks);
        for (int i = 1; i < masks.length; i++) {
            assertNotEquals(masks[i - 1], masks[i], "Masks should be unique");
        }
    }

    @Test
    void testMask_correctBitPosition() {
        assertEquals(1L << 0, WriteFeature.FieldBased.mask);
        assertEquals(1L << 1, WriteFeature.PrettyFormat.mask);
        assertEquals(1L << 2, WriteFeature.WriteNulls.mask);
        assertEquals(1L << 3, WriteFeature.WriteNullListAsEmpty.mask);
        assertEquals(1L << 4, WriteFeature.WriteNullStringAsEmpty.mask);
        assertEquals(1L << 5, WriteFeature.WriteNullNumberAsZero.mask);
        assertEquals(1L << 6, WriteFeature.WriteNullBooleanAsFalse.mask);
        assertEquals(1L << 7, WriteFeature.WriteEnumsUsingName.mask);
        assertEquals(1L << 8, WriteFeature.WriteEnumUsingToString.mask);
        assertEquals(1L << 9, WriteFeature.WriteClassName.mask);
        assertEquals(1L << 10, WriteFeature.SortMapEntriesByKeys.mask);
        assertEquals(1L << 11, WriteFeature.EscapeNoneAscii.mask);
        assertEquals(1L << 12, WriteFeature.WriteBigDecimalAsPlain.mask);
        assertEquals(1L << 13, WriteFeature.WriteLongAsString.mask);
        assertEquals(1L << 14, WriteFeature.WriteByteArrayAsBase64.mask);
        assertEquals(1L << 15, WriteFeature.BeanToArray.mask);
        assertEquals(1L << 16, WriteFeature.ReferenceDetection.mask);
        assertEquals(1L << 17, WriteFeature.BrowserCompatible.mask);
        assertEquals(1L << 18, WriteFeature.WriteNonStringValueAsString.mask);
        assertEquals(1L << 19, WriteFeature.OptimizedForAscii.mask);
    }

    // ==================== of() tests ====================

    @Test
    void testOf_empty() {
        long mask = WriteFeature.of();

        assertEquals(0L, mask);
    }

    @Test
    void testOf_singleFeature() {
        long mask = WriteFeature.of(WriteFeature.PrettyFormat);

        assertEquals(WriteFeature.PrettyFormat.mask, mask);
    }

    @Test
    void testOf_multipleFeatures() {
        long mask = WriteFeature.of(
                WriteFeature.PrettyFormat,
                WriteFeature.WriteNulls,
                WriteFeature.EscapeNoneAscii
        );

        long expected = WriteFeature.PrettyFormat.mask
                | WriteFeature.WriteNulls.mask
                | WriteFeature.EscapeNoneAscii.mask;
        assertEquals(expected, mask);
    }

    @Test
    void testOf_allFeatures() {
        long mask = WriteFeature.of(WriteFeature.values());

        // All 29 features should have their bits set
        assertEquals(29, Long.bitCount(mask));
    }

    @Test
    void testOf_duplicateFeatures() {
        // Duplicates should be ORed together, resulting in same mask
        long mask1 = WriteFeature.of(WriteFeature.PrettyFormat);
        long mask2 = WriteFeature.of(WriteFeature.PrettyFormat, WriteFeature.PrettyFormat);

        assertEquals(mask1, mask2);
    }

    // ==================== valuesFrom() tests ====================

    @Test
    void testValuesFrom_zeroMask() {
        WriteFeature[] features = WriteFeature.valuesFrom(0L);

        assertNotNull(features);
        assertEquals(0, features.length);
    }

    @Test
    void testValuesFrom_singleFeature() {
        long mask = WriteFeature.PrettyFormat.mask;
        WriteFeature[] features = WriteFeature.valuesFrom(mask);

        assertEquals(1, features.length);
        assertEquals(WriteFeature.PrettyFormat, features[0]);
    }

    @Test
    void testValuesFrom_multipleFeatures() {
        long mask = WriteFeature.of(
                WriteFeature.PrettyFormat,
                WriteFeature.WriteNulls,
                WriteFeature.EscapeNoneAscii
        );
        WriteFeature[] features = WriteFeature.valuesFrom(mask);

        assertEquals(3, features.length);
        assertTrue(Arrays.asList(features).contains(WriteFeature.PrettyFormat));
        assertTrue(Arrays.asList(features).contains(WriteFeature.WriteNulls));
        assertTrue(Arrays.asList(features).contains(WriteFeature.EscapeNoneAscii));
    }

    @Test
    void testValuesFrom_allFeatures() {
        long mask = WriteFeature.of(WriteFeature.values());
        WriteFeature[] features = WriteFeature.valuesFrom(mask);

        assertEquals(29, features.length);
        // Verify order matches ordinal
        assertEquals(WriteFeature.FieldBased, features[0]);
        assertEquals(WriteFeature.PrettyFormat, features[1]);
        assertEquals(WriteFeature.WriteNulls, features[2]);
        assertEquals(WriteFeature.OptimizedForAscii, features[19]);
        assertEquals(WriteFeature.WriteNonStringKeyAsString, features[28]);
    }

    @Test
    void testValuesFrom_invalidBits() {
        // Mask with bits that don't correspond to any feature
        long mask = 1L << 35; // Bit 35 is beyond our features
        WriteFeature[] features = WriteFeature.valuesFrom(mask);

        assertEquals(0, features.length);
    }

    @Test
    void testValuesFrom_mixedValidInvalid() {
        // Mix of valid and invalid bits
        long mask = WriteFeature.PrettyFormat.mask | (1L << 35);
        WriteFeature[] features = WriteFeature.valuesFrom(mask);

        assertEquals(1, features.length);
        assertEquals(WriteFeature.PrettyFormat, features[0]);
    }

    // ==================== Round-trip tests ====================

    @Test
    void testRoundTrip_ofAndValuesFrom() {
        WriteFeature[] original = {
                WriteFeature.PrettyFormat,
                WriteFeature.WriteNulls,
                WriteFeature.EscapeNoneAscii,
                WriteFeature.BrowserCompatible
        };

        long mask = WriteFeature.of(original);
        WriteFeature[] restored = WriteFeature.valuesFrom(mask);

        assertEquals(original.length, restored.length);
        for (WriteFeature f : original) {
            assertTrue(Arrays.asList(restored).contains(f));
        }
    }

    @Test
    void testRoundTrip_empty() {
        long mask = WriteFeature.of();
        WriteFeature[] restored = WriteFeature.valuesFrom(mask);

        assertEquals(0, restored.length);
    }

    // ==================== Feature semantic tests ====================

    @Test
    void testFeature_semanticNames() {
        assertEquals("FieldBased", WriteFeature.FieldBased.name());
        assertEquals("PrettyFormat", WriteFeature.PrettyFormat.name());
        assertEquals("WriteNulls", WriteFeature.WriteNulls.name());
        assertEquals("WriteNullListAsEmpty", WriteFeature.WriteNullListAsEmpty.name());
        assertEquals("WriteNullStringAsEmpty", WriteFeature.WriteNullStringAsEmpty.name());
        assertEquals("WriteNullNumberAsZero", WriteFeature.WriteNullNumberAsZero.name());
        assertEquals("WriteNullBooleanAsFalse", WriteFeature.WriteNullBooleanAsFalse.name());
        assertEquals("WriteEnumsUsingName", WriteFeature.WriteEnumsUsingName.name());
        assertEquals("WriteEnumUsingToString", WriteFeature.WriteEnumUsingToString.name());
        assertEquals("WriteClassName", WriteFeature.WriteClassName.name());
        assertEquals("SortMapEntriesByKeys", WriteFeature.SortMapEntriesByKeys.name());
        assertEquals("EscapeNoneAscii", WriteFeature.EscapeNoneAscii.name());
        assertEquals("WriteBigDecimalAsPlain", WriteFeature.WriteBigDecimalAsPlain.name());
        assertEquals("WriteLongAsString", WriteFeature.WriteLongAsString.name());
        assertEquals("WriteByteArrayAsBase64", WriteFeature.WriteByteArrayAsBase64.name());
        assertEquals("BeanToArray", WriteFeature.BeanToArray.name());
        assertEquals("ReferenceDetection", WriteFeature.ReferenceDetection.name());
        assertEquals("BrowserCompatible", WriteFeature.BrowserCompatible.name());
        assertEquals("WriteNonStringValueAsString", WriteFeature.WriteNonStringValueAsString.name());
        assertEquals("OptimizedForAscii", WriteFeature.OptimizedForAscii.name());
    }

    // ==================== Combining features tests ====================

    @Test
    void testCombiningFeatures_withBitwiseOr() {
        long mask = WriteFeature.PrettyFormat.mask | WriteFeature.WriteNulls.mask;

        assertTrue((mask & WriteFeature.PrettyFormat.mask) != 0);
        assertTrue((mask & WriteFeature.WriteNulls.mask) != 0);
        assertFalse((mask & WriteFeature.EscapeNoneAscii.mask) != 0);
    }

    @Test
    void testCheckingFeatureEnabled() {
        long mask = WriteFeature.of(
                WriteFeature.PrettyFormat,
                WriteFeature.WriteNulls
        );

        assertTrue((mask & WriteFeature.PrettyFormat.mask) != 0);
        assertTrue((mask & WriteFeature.WriteNulls.mask) != 0);
        assertFalse((mask & WriteFeature.EscapeNoneAscii.mask) != 0);
        assertFalse((mask & WriteFeature.BrowserCompatible.mask) != 0);
    }

    // ==================== Feature category tests ====================

    @Test
    void testFeatures_nullHandlingCategory() {
        // Features that control null handling
        WriteFeature[] nullHandling = {
                WriteFeature.WriteNulls,
                WriteFeature.WriteNullListAsEmpty,
                WriteFeature.WriteNullStringAsEmpty,
                WriteFeature.WriteNullNumberAsZero,
                WriteFeature.WriteNullBooleanAsFalse
        };

        for (WriteFeature f : nullHandling) {
            assertNotNull(f.name());
            assertTrue(f.mask > 0);
        }
    }

    @Test
    void testFeatures_formattingCategory() {
        // Features that control output formatting
        WriteFeature[] formatting = {
                WriteFeature.PrettyFormat,
                WriteFeature.SortMapEntriesByKeys,
                WriteFeature.EscapeNoneAscii,
                WriteFeature.BrowserCompatible
        };

        for (WriteFeature f : formatting) {
            assertNotNull(f.name());
            assertTrue(f.mask > 0);
        }
    }

    @Test
    void testFeatures_typeHandlingCategory() {
        // Features that control type handling
        WriteFeature[] typeHandling = {
                WriteFeature.WriteEnumsUsingName,
                WriteFeature.WriteEnumUsingToString,
                WriteFeature.WriteClassName,
                WriteFeature.WriteBigDecimalAsPlain,
                WriteFeature.WriteLongAsString,
                WriteFeature.WriteByteArrayAsBase64
        };

        for (WriteFeature f : typeHandling) {
            assertNotNull(f.name());
            assertTrue(f.mask > 0);
        }
    }

    @Test
    void testFeatures_structureCategory() {
        // Features that control output structure
        WriteFeature[] structure = {
                WriteFeature.BeanToArray,
                WriteFeature.ReferenceDetection
        };

        for (WriteFeature f : structure) {
            assertNotNull(f.name());
            assertTrue(f.mask > 0);
        }
    }

    // ==================== Edge case tests ====================

    @Test
    void testAllMasks_nonZero() {
        for (WriteFeature f : WriteFeature.values()) {
            assertTrue(f.mask != 0, f.name() + " mask should be non-zero");
        }
    }

    @Test
    void testNoMaskOverflow() {
        // Ensure masks fit in a long (no overflow)
        for (WriteFeature f : WriteFeature.values()) {
            assertTrue(f.mask > 0, f.name() + " mask should be positive");
        }
    }

    @Test
    void testOrdinalConsistency() {
        // Verify ordinal matches expected bit position
        for (WriteFeature f : WriteFeature.values()) {
            assertEquals(1L << f.ordinal(), f.mask,
                    f.name() + " ordinal doesn't match mask bit position");
        }
    }

    // ==================== Write feature combinations ====================

    @Test
    void testPrettyFormat_withWriteNulls() {
        // Common combination for readable output
        long mask = WriteFeature.of(WriteFeature.PrettyFormat, WriteFeature.WriteNulls);

        assertTrue((mask & WriteFeature.PrettyFormat.mask) != 0);
        assertTrue((mask & WriteFeature.WriteNulls.mask) != 0);
    }

    @Test
    void testBrowserCompatible_withEscapeNoneAscii() {
        // Common combination for web output
        long mask = WriteFeature.of(WriteFeature.BrowserCompatible, WriteFeature.EscapeNoneAscii);

        assertTrue((mask & WriteFeature.BrowserCompatible.mask) != 0);
        assertTrue((mask & WriteFeature.EscapeNoneAscii.mask) != 0);
    }

    // ==================== POJO-based feature behavior tests ====================

    @Test
    void nullAsDefaultValue_pojo() {
        NullBean bean = new NullBean();
        String json = JSON.toJSONString(bean, WriteFeature.NullAsDefaultValue, WriteFeature.FieldBased);
        // null String -> "", null Number -> 0, null Boolean -> false, null List -> []
        assertTrue(json.contains("\"name\":\"\""), "null String should be empty: " + json);
        assertTrue(json.contains("\"count\":0"), "null Number should be 0: " + json);
        assertTrue(json.contains("\"flag\":false"), "null Boolean should be false: " + json);
        assertTrue(json.contains("\"items\":[]"), "null List should be []: " + json);
    }

    public static class NullBean {
        public String name;
        public Integer count;
        public Boolean flag;
        public java.util.List<String> items;
    }

    @Test
    void notWriteEmptyArray_pojo() {
        EmptyArrayBean bean = new EmptyArrayBean();
        bean.name = "test";
        bean.items = new java.util.ArrayList<>();
        String json = JSON.toJSONString(bean, WriteFeature.NotWriteEmptyArray, WriteFeature.FieldBased);
        assertFalse(json.contains("\"items\""), "empty list should be skipped: " + json);
        assertTrue(json.contains("\"name\""), "non-empty should be present: " + json);
    }

    public static class EmptyArrayBean {
        public String name;
        public java.util.List<String> items;
    }
}

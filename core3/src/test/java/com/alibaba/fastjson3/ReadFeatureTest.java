package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ReadFeature - deserialization feature flags.
 */
class ReadFeatureTest {

    // ==================== Enum values tests ====================

    @Test
    void testEnumValues_allPresent() {
        ReadFeature[] values = ReadFeature.values();

        assertEquals(37, values.length);
    }

    @Test
    void testEnumValue_ordinals() {
        assertEquals(0, ReadFeature.FieldBased.ordinal());
        assertEquals(1, ReadFeature.AllowSingleQuotes.ordinal());
        assertEquals(2, ReadFeature.AllowUnquotedFieldNames.ordinal());
        assertEquals(3, ReadFeature.AllowComments.ordinal());
        assertEquals(4, ReadFeature.UseBigDecimalForFloats.ordinal());
        assertEquals(5, ReadFeature.UseBigDecimalForDoubles.ordinal());
        assertEquals(6, ReadFeature.TrimString.ordinal());
        assertEquals(7, ReadFeature.ErrorOnUnknownProperties.ordinal());
        assertEquals(8, ReadFeature.ErrorOnNullForPrimitives.ordinal());
        assertEquals(9, ReadFeature.SupportSmartMatch.ordinal());
        assertEquals(10, ReadFeature.SupportAutoType.ordinal());
        assertEquals(11, ReadFeature.InitStringFieldAsEmpty.ordinal());
        assertEquals(12, ReadFeature.NullOnError.ordinal());
        assertEquals(13, ReadFeature.SupportArrayToBean.ordinal());
        assertEquals(14, ReadFeature.EmptyStringAsNull.ordinal());
        assertEquals(15, ReadFeature.DuplicateKeyValueAsArray.ordinal());
        assertEquals(16, ReadFeature.Base64StringAsByteArray.ordinal());
    }

    // ==================== Mask tests ====================

    @Test
    void testMask_uniquePowerOfTwo() {
        ReadFeature[] values = ReadFeature.values();
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
        assertEquals(1L << 0, ReadFeature.FieldBased.mask);
        assertEquals(1L << 1, ReadFeature.AllowSingleQuotes.mask);
        assertEquals(1L << 2, ReadFeature.AllowUnquotedFieldNames.mask);
        assertEquals(1L << 3, ReadFeature.AllowComments.mask);
        assertEquals(1L << 4, ReadFeature.UseBigDecimalForFloats.mask);
        assertEquals(1L << 5, ReadFeature.UseBigDecimalForDoubles.mask);
        assertEquals(1L << 6, ReadFeature.TrimString.mask);
        assertEquals(1L << 7, ReadFeature.ErrorOnUnknownProperties.mask);
        assertEquals(1L << 8, ReadFeature.ErrorOnNullForPrimitives.mask);
        assertEquals(1L << 9, ReadFeature.SupportSmartMatch.mask);
        assertEquals(1L << 10, ReadFeature.SupportAutoType.mask);
        assertEquals(1L << 11, ReadFeature.InitStringFieldAsEmpty.mask);
        assertEquals(1L << 12, ReadFeature.NullOnError.mask);
        assertEquals(1L << 13, ReadFeature.SupportArrayToBean.mask);
        assertEquals(1L << 14, ReadFeature.EmptyStringAsNull.mask);
        assertEquals(1L << 15, ReadFeature.DuplicateKeyValueAsArray.mask);
        assertEquals(1L << 16, ReadFeature.Base64StringAsByteArray.mask);
    }

    // ==================== of() tests ====================

    @Test
    void testOf_empty() {
        long mask = ReadFeature.of();

        assertEquals(0L, mask);
    }

    @Test
    void testOf_singleFeature() {
        long mask = ReadFeature.of(ReadFeature.AllowSingleQuotes);

        assertEquals(ReadFeature.AllowSingleQuotes.mask, mask);
    }

    @Test
    void testOf_multipleFeatures() {
        long mask = ReadFeature.of(
                ReadFeature.AllowSingleQuotes,
                ReadFeature.AllowComments,
                ReadFeature.TrimString
        );

        long expected = ReadFeature.AllowSingleQuotes.mask
                | ReadFeature.AllowComments.mask
                | ReadFeature.TrimString.mask;
        assertEquals(expected, mask);
    }

    @Test
    void testOf_allFeatures() {
        long mask = ReadFeature.of(ReadFeature.values());

        // All 17 features should have their bits set
        assertEquals(37, Long.bitCount(mask));
    }

    @Test
    void testOf_duplicateFeatures() {
        // Duplicates should be ORed together, resulting in same mask
        long mask1 = ReadFeature.of(ReadFeature.AllowSingleQuotes);
        long mask2 = ReadFeature.of(ReadFeature.AllowSingleQuotes, ReadFeature.AllowSingleQuotes);

        assertEquals(mask1, mask2);
    }

    // ==================== valuesFrom() tests ====================

    @Test
    void testValuesFrom_zeroMask() {
        ReadFeature[] features = ReadFeature.valuesFrom(0L);

        assertNotNull(features);
        assertEquals(0, features.length);
    }

    @Test
    void testValuesFrom_singleFeature() {
        long mask = ReadFeature.AllowComments.mask;
        ReadFeature[] features = ReadFeature.valuesFrom(mask);

        assertEquals(1, features.length);
        assertEquals(ReadFeature.AllowComments, features[0]);
    }

    @Test
    void testValuesFrom_multipleFeatures() {
        long mask = ReadFeature.of(
                ReadFeature.AllowSingleQuotes,
                ReadFeature.AllowComments,
                ReadFeature.TrimString
        );
        ReadFeature[] features = ReadFeature.valuesFrom(mask);

        assertEquals(3, features.length);
        assertTrue(Arrays.asList(features).contains(ReadFeature.AllowSingleQuotes));
        assertTrue(Arrays.asList(features).contains(ReadFeature.AllowComments));
        assertTrue(Arrays.asList(features).contains(ReadFeature.TrimString));
    }

    @Test
    void testValuesFrom_allFeatures() {
        long mask = ReadFeature.of(ReadFeature.values());
        ReadFeature[] features = ReadFeature.valuesFrom(mask);

        assertEquals(37, features.length);
        // Verify order matches ordinal
        assertEquals(ReadFeature.FieldBased, features[0]);
        assertEquals(ReadFeature.AllowSingleQuotes, features[1]);
        assertEquals(ReadFeature.AllowComments, features[3]);
        assertEquals(ReadFeature.Base64StringAsByteArray, features[16]);
    }

    @Test
    void testValuesFrom_invalidBits() {
        // Mask with bits that don't correspond to any feature
        long mask = 1L << 50; // Bit 20 is beyond our features
        ReadFeature[] features = ReadFeature.valuesFrom(mask);

        assertEquals(0, features.length);
    }

    @Test
    void testValuesFrom_mixedValidInvalid() {
        // Mix of valid and invalid bits
        long mask = ReadFeature.AllowSingleQuotes.mask | (1L << 50);
        ReadFeature[] features = ReadFeature.valuesFrom(mask);

        assertEquals(1, features.length);
        assertEquals(ReadFeature.AllowSingleQuotes, features[0]);
    }

    // ==================== Round-trip tests ====================

    @Test
    void testRoundTrip_ofAndValuesFrom() {
        ReadFeature[] original = {
                ReadFeature.AllowSingleQuotes,
                ReadFeature.AllowComments,
                ReadFeature.TrimString,
                ReadFeature.ErrorOnUnknownProperties
        };

        long mask = ReadFeature.of(original);
        ReadFeature[] restored = ReadFeature.valuesFrom(mask);

        assertEquals(original.length, restored.length);
        for (ReadFeature f : original) {
            assertTrue(Arrays.asList(restored).contains(f));
        }
    }

    @Test
    void testRoundTrip_empty() {
        long mask = ReadFeature.of();
        ReadFeature[] restored = ReadFeature.valuesFrom(mask);

        assertEquals(0, restored.length);
    }

    // ==================== Feature semantic tests ====================

    @Test
    void testFeature_semanticNames() {
        // Verify feature names match their documentation
        assertEquals("FieldBased", ReadFeature.FieldBased.name());
        assertEquals("AllowSingleQuotes", ReadFeature.AllowSingleQuotes.name());
        assertEquals("AllowUnquotedFieldNames", ReadFeature.AllowUnquotedFieldNames.name());
        assertEquals("AllowComments", ReadFeature.AllowComments.name());
        assertEquals("UseBigDecimalForFloats", ReadFeature.UseBigDecimalForFloats.name());
        assertEquals("UseBigDecimalForDoubles", ReadFeature.UseBigDecimalForDoubles.name());
        assertEquals("TrimString", ReadFeature.TrimString.name());
        assertEquals("ErrorOnUnknownProperties", ReadFeature.ErrorOnUnknownProperties.name());
        assertEquals("ErrorOnNullForPrimitives", ReadFeature.ErrorOnNullForPrimitives.name());
        assertEquals("SupportSmartMatch", ReadFeature.SupportSmartMatch.name());
        assertEquals("SupportAutoType", ReadFeature.SupportAutoType.name());
        assertEquals("InitStringFieldAsEmpty", ReadFeature.InitStringFieldAsEmpty.name());
        assertEquals("NullOnError", ReadFeature.NullOnError.name());
        assertEquals("SupportArrayToBean", ReadFeature.SupportArrayToBean.name());
        assertEquals("EmptyStringAsNull", ReadFeature.EmptyStringAsNull.name());
        assertEquals("DuplicateKeyValueAsArray", ReadFeature.DuplicateKeyValueAsArray.name());
        assertEquals("Base64StringAsByteArray", ReadFeature.Base64StringAsByteArray.name());
    }

    // ==================== Combining features tests ====================

    @Test
    void testCombiningFeatures_withBitwiseOr() {
        long mask = ReadFeature.AllowSingleQuotes.mask | ReadFeature.AllowComments.mask;

        assertTrue((mask & ReadFeature.AllowSingleQuotes.mask) != 0);
        assertTrue((mask & ReadFeature.AllowComments.mask) != 0);
        assertFalse((mask & ReadFeature.TrimString.mask) != 0);
    }

    @Test
    void testCheckingFeatureEnabled() {
        long mask = ReadFeature.of(
                ReadFeature.AllowSingleQuotes,
                ReadFeature.AllowComments
        );

        assertTrue((mask & ReadFeature.AllowSingleQuotes.mask) != 0);
        assertTrue((mask & ReadFeature.AllowComments.mask) != 0);
        assertFalse((mask & ReadFeature.TrimString.mask) != 0);
        assertFalse((mask & ReadFeature.ErrorOnUnknownProperties.mask) != 0);
    }

    // ==================== Feature category tests ====================

    @Test
    void testFeatures_lexicalCategory() {
        // Features that control JSON syntax
        ReadFeature[] lexical = {
                ReadFeature.AllowSingleQuotes,
                ReadFeature.AllowUnquotedFieldNames,
                ReadFeature.AllowComments
        };

        for (ReadFeature f : lexical) {
            assertNotNull(f.name());
            assertTrue(f.mask > 0);
        }
    }

    @Test
    void testFeatures_typeConversionCategory() {
        // Features that control type conversion
        ReadFeature[] typeConversion = {
                ReadFeature.UseBigDecimalForFloats,
                ReadFeature.UseBigDecimalForDoubles,
                ReadFeature.Base64StringAsByteArray
        };

        for (ReadFeature f : typeConversion) {
            assertNotNull(f.name());
            assertTrue(f.mask > 0);
        }
    }

    @Test
    void testFeatures_errorHandlingCategory() {
        // Features that control error handling
        ReadFeature[] errorHandling = {
                ReadFeature.ErrorOnUnknownProperties,
                ReadFeature.ErrorOnNullForPrimitives,
                ReadFeature.NullOnError
        };

        for (ReadFeature f : errorHandling) {
            assertNotNull(f.name());
            assertTrue(f.mask > 0);
        }
    }

    @Test
    void testFeatures_stringHandlingCategory() {
        // Features that control string handling
        ReadFeature[] stringHandling = {
                ReadFeature.TrimString,
                ReadFeature.InitStringFieldAsEmpty,
                ReadFeature.EmptyStringAsNull
        };

        for (ReadFeature f : stringHandling) {
            assertNotNull(f.name());
            assertTrue(f.mask > 0);
        }
    }

    // ==================== Edge case tests ====================

    @Test
    void testAllMasks_nonZero() {
        for (ReadFeature f : ReadFeature.values()) {
            assertTrue(f.mask != 0, f.name() + " mask should be non-zero");
        }
    }

    @Test
    void testNoMaskOverflow() {
        // Ensure masks fit in a long (no overflow)
        for (ReadFeature f : ReadFeature.values()) {
            assertTrue(f.mask > 0, f.name() + " mask should be positive");
        }
    }

    @Test
    void testOrdinalConsistency() {
        // Verify ordinal matches expected bit position
        for (ReadFeature f : ReadFeature.values()) {
            assertEquals(1L << f.ordinal(), f.mask,
                    f.name() + " ordinal doesn't match mask bit position");
        }
    }
}

package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ParseConfig - semantic configuration presets for JSON parsing.
 */
class ParseConfigTest {

    @Test
    void testEnumValues() {
        // Verify all enum values exist
        ParseConfig[] values = ParseConfig.values();
        assertEquals(4, values.length);

        assertTrue(contains(values, ParseConfig.DEFAULT));
        assertTrue(contains(values, ParseConfig.LENIENT));
        assertTrue(contains(values, ParseConfig.STRICT));
        assertTrue(contains(values, ParseConfig.API));
    }

    @Test
    void testValueOf() {
        assertEquals(ParseConfig.DEFAULT, ParseConfig.valueOf("DEFAULT"));
        assertEquals(ParseConfig.LENIENT, ParseConfig.valueOf("LENIENT"));
        assertEquals(ParseConfig.STRICT, ParseConfig.valueOf("STRICT"));
        assertEquals(ParseConfig.API, ParseConfig.valueOf("API"));
    }

    @Test
    void testValueOf_invalid() {
        assertThrows(IllegalArgumentException.class, () -> ParseConfig.valueOf("INVALID"));
    }

    // ==================== DEFAULT ====================

    @Test
    void testDefault_features() {
        ReadFeature[] features = ParseConfig.DEFAULT.features();

        assertNotNull(features);
        assertEquals(0, features.length);
    }

    @Test
    void testDefault_mask() {
        long mask = ParseConfig.DEFAULT.mask();

        assertEquals(0L, mask);
    }

    // ==================== LENIENT ====================

    @Test
    void testLenient_features() {
        ReadFeature[] features = ParseConfig.LENIENT.features();

        assertNotNull(features);
        assertEquals(4, features.length);
    }

    @Test
    void testLenient_containsAllowComments() {
        ReadFeature[] features = ParseConfig.LENIENT.features();

        assertTrue(contains(features, ReadFeature.AllowComments));
    }

    @Test
    void testLenient_containsAllowSingleQuotes() {
        ReadFeature[] features = ParseConfig.LENIENT.features();

        assertTrue(contains(features, ReadFeature.AllowSingleQuotes));
    }

    @Test
    void testLenient_containsAllowUnquotedFieldNames() {
        ReadFeature[] features = ParseConfig.LENIENT.features();

        assertTrue(contains(features, ReadFeature.AllowUnquotedFieldNames));
    }

    @Test
    void testLenient_containsSupportSmartMatch() {
        ReadFeature[] features = ParseConfig.LENIENT.features();

        assertTrue(contains(features, ReadFeature.SupportSmartMatch));
    }

    @Test
    void testLenient_mask() {
        long mask = ParseConfig.LENIENT.mask();

        assertNotEquals(0L, mask);
    }

    @Test
    void testLenient_maskEqualsFeaturesMask() {
        long configMask = ParseConfig.LENIENT.mask();
        long featureMask = ReadFeature.of(ParseConfig.LENIENT.features());

        assertEquals(configMask, featureMask);
    }

    // ==================== STRICT ====================

    @Test
    void testStrict_features() {
        ReadFeature[] features = ParseConfig.STRICT.features();

        assertNotNull(features);
        assertEquals(2, features.length);
    }

    @Test
    void testStrict_containsErrorOnUnknownProperties() {
        ReadFeature[] features = ParseConfig.STRICT.features();

        assertTrue(contains(features, ReadFeature.ErrorOnUnknownProperties));
    }

    @Test
    void testStrict_containsErrorOnNullForPrimitives() {
        ReadFeature[] features = ParseConfig.STRICT.features();

        assertTrue(contains(features, ReadFeature.ErrorOnNullForPrimitives));
    }

    @Test
    void testStrict_mask() {
        long mask = ParseConfig.STRICT.mask();

        assertNotEquals(0L, mask);
    }

    @Test
    void testStrict_maskEqualsFeaturesMask() {
        long configMask = ParseConfig.STRICT.mask();
        long featureMask = ReadFeature.of(ParseConfig.STRICT.features());

        assertEquals(configMask, featureMask);
    }

    // ==================== API ====================

    @Test
    void testApi_features() {
        ReadFeature[] features = ParseConfig.API.features();

        assertNotNull(features);
        assertEquals(3, features.length);
    }

    @Test
    void testApi_containsErrorOnUnknownProperties() {
        ReadFeature[] features = ParseConfig.API.features();

        assertTrue(contains(features, ReadFeature.ErrorOnUnknownProperties));
    }

    @Test
    void testApi_containsErrorOnNullForPrimitives() {
        ReadFeature[] features = ParseConfig.API.features();

        assertTrue(contains(features, ReadFeature.ErrorOnNullForPrimitives));
    }

    @Test
    void testApi_containsUseBigDecimalForDoubles() {
        ReadFeature[] features = ParseConfig.API.features();

        assertTrue(contains(features, ReadFeature.UseBigDecimalForDoubles));
    }

    @Test
    void testApi_mask() {
        long mask = ParseConfig.API.mask();

        assertNotEquals(0L, mask);
    }

    @Test
    void testApi_maskEqualsFeaturesMask() {
        long configMask = ParseConfig.API.mask();
        long featureMask = ReadFeature.of(ParseConfig.API.features());

        assertEquals(configMask, featureMask);
    }

    // ==================== Feature immutability ====================

    @Test
    void testFeatures_returnsDefensiveCopy() {
        ReadFeature[] features1 = ParseConfig.LENIENT.features();
        ReadFeature[] features2 = ParseConfig.LENIENT.features();

        // Should return different array instances
        assertNotSame(features1, features2);

        // Modifying one should not affect the other
        features1[0] = null;
        assertNotNull(features2[0]);
    }

    // ==================== Mask comparisons ====================

    @Test
    void testMasks_areDistinct() {
        long defaultMask = ParseConfig.DEFAULT.mask();
        long lenientMask = ParseConfig.LENIENT.mask();
        long strictMask = ParseConfig.STRICT.mask();
        long apiMask = ParseConfig.API.mask();

        // All masks should be different
        assertNotEquals(defaultMask, lenientMask);
        assertNotEquals(defaultMask, strictMask);
        assertNotEquals(defaultMask, apiMask);
        assertNotEquals(lenientMask, strictMask);
        assertNotEquals(lenientMask, apiMask);
        assertNotEquals(strictMask, apiMask);
    }

    @Test
    void testDefaultMask_isZero() {
        // DEFAULT should have no features enabled
        assertEquals(0L, ParseConfig.DEFAULT.mask());
    }

    @Test
    void testStrictAndApi_masksOverlap() {
        // API includes STRICT features plus one more
        long strictMask = ParseConfig.STRICT.mask();
        long apiMask = ParseConfig.API.mask();

        // API mask should include all STRICT bits
        assertTrue((strictMask & apiMask) == strictMask);
    }

    // ==================== Integration tests ====================

    @Test
    void testParse_withConfig() {
        String json = "{\"name\":\"test\",\"value\":42}";

        // Test with DEFAULT config
        TestBean bean1 = JSON.parse(json, TestBean.class, ParseConfig.DEFAULT);
        assertEquals("test", bean1.name);
        assertEquals(42, bean1.value);

        // Test with LENIENT config (should work the same for valid JSON)
        TestBean bean2 = JSON.parse(json, TestBean.class, ParseConfig.LENIENT);
        assertEquals("test", bean2.name);
        assertEquals(42, bean2.value);
    }

    @Test
    void testParse_withByteArray_andConfig() {
        String jsonStr = "{\"name\":\"test\",\"value\":42}";
        byte[] json = jsonStr.getBytes();

        TestBean bean = JSON.parse(json, TestBean.class, ParseConfig.DEFAULT);
        assertEquals("test", bean.name);
        assertEquals(42, bean.value);
    }

    // ==================== Helper methods ====================

    private boolean contains(ReadFeature[] array, ReadFeature feature) {
        for (ReadFeature f : array) {
            if (f == feature) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(ParseConfig[] array, ParseConfig config) {
        for (ParseConfig c : array) {
            if (c == config) {
                return true;
            }
        }
        return false;
    }

    // ==================== Test bean ====================

    static class TestBean {
        public String name;
        public int value;
        public double doubleValue;
    }
}

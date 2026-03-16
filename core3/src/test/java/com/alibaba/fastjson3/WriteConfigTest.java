package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WriteConfig - semantic configuration presets for JSON serialization.
 */
class WriteConfigTest {

    @Test
    void testEnumValues() {
        // Verify all enum values exist
        WriteConfig[] values = WriteConfig.values();
        assertEquals(4, values.length);

        assertTrue(contains(values, WriteConfig.DEFAULT));
        assertTrue(contains(values, WriteConfig.PRETTY));
        assertTrue(contains(values, WriteConfig.WITH_NULLS));
        assertTrue(contains(values, WriteConfig.PRETTY_WITH_NULLS));
    }

    @Test
    void testValueOf() {
        assertEquals(WriteConfig.DEFAULT, WriteConfig.valueOf("DEFAULT"));
        assertEquals(WriteConfig.PRETTY, WriteConfig.valueOf("PRETTY"));
        assertEquals(WriteConfig.WITH_NULLS, WriteConfig.valueOf("WITH_NULLS"));
        assertEquals(WriteConfig.PRETTY_WITH_NULLS, WriteConfig.valueOf("PRETTY_WITH_NULLS"));
    }

    @Test
    void testValueOf_invalid() {
        assertThrows(IllegalArgumentException.class, () -> WriteConfig.valueOf("INVALID"));
    }

    // ==================== DEFAULT ====================

    @Test
    void testDefault_features() {
        WriteFeature[] features = WriteConfig.DEFAULT.features();

        assertNotNull(features);
        assertEquals(0, features.length);
    }

    @Test
    void testDefault_mask() {
        long mask = WriteConfig.DEFAULT.mask();

        assertEquals(0L, mask);
    }

    // ==================== PRETTY ====================

    @Test
    void testPretty_features() {
        WriteFeature[] features = WriteConfig.PRETTY.features();

        assertNotNull(features);
        assertEquals(1, features.length);
    }

    @Test
    void testPretty_containsPrettyFormat() {
        WriteFeature[] features = WriteConfig.PRETTY.features();

        assertEquals(WriteFeature.PrettyFormat, features[0]);
    }

    @Test
    void testPretty_mask() {
        long mask = WriteConfig.PRETTY.mask();

        assertNotEquals(0L, mask);
    }

    @Test
    void testPretty_maskEqualsFeaturesMask() {
        long configMask = WriteConfig.PRETTY.mask();
        long featureMask = WriteFeature.of(WriteConfig.PRETTY.features());

        assertEquals(configMask, featureMask);
    }

    // ==================== WITH_NULLS ====================

    @Test
    void testWithNulls_features() {
        WriteFeature[] features = WriteConfig.WITH_NULLS.features();

        assertNotNull(features);
        assertEquals(1, features.length);
    }

    @Test
    void testWithNulls_containsWriteNulls() {
        WriteFeature[] features = WriteConfig.WITH_NULLS.features();

        assertEquals(WriteFeature.WriteNulls, features[0]);
    }

    @Test
    void testWithNulls_mask() {
        long mask = WriteConfig.WITH_NULLS.mask();

        assertNotEquals(0L, mask);
    }

    @Test
    void testWithNulls_maskEqualsFeaturesMask() {
        long configMask = WriteConfig.WITH_NULLS.mask();
        long featureMask = WriteFeature.of(WriteConfig.WITH_NULLS.features());

        assertEquals(configMask, featureMask);
    }

    // ==================== PRETTY_WITH_NULLS ====================

    @Test
    void testPrettyWithNulls_features() {
        WriteFeature[] features = WriteConfig.PRETTY_WITH_NULLS.features();

        assertNotNull(features);
        assertEquals(2, features.length);
    }

    @Test
    void testPrettyWithNulls_containsPrettyFormat() {
        WriteFeature[] features = WriteConfig.PRETTY_WITH_NULLS.features();

        assertTrue(contains(features, WriteFeature.PrettyFormat));
    }

    @Test
    void testPrettyWithNulls_containsWriteNulls() {
        WriteFeature[] features = WriteConfig.PRETTY_WITH_NULLS.features();

        assertTrue(contains(features, WriteFeature.WriteNulls));
    }

    @Test
    void testPrettyWithNulls_mask() {
        long mask = WriteConfig.PRETTY_WITH_NULLS.mask();

        assertNotEquals(0L, mask);
    }

    @Test
    void testPrettyWithNulls_maskEqualsFeaturesMask() {
        long configMask = WriteConfig.PRETTY_WITH_NULLS.mask();
        long featureMask = WriteFeature.of(WriteConfig.PRETTY_WITH_NULLS.features());

        assertEquals(configMask, featureMask);
    }

    // ==================== Feature immutability ====================

    @Test
    void testFeatures_returnsDefensiveCopy() {
        WriteFeature[] features1 = WriteConfig.PRETTY_WITH_NULLS.features();
        WriteFeature[] features2 = WriteConfig.PRETTY_WITH_NULLS.features();

        // Should return different array instances
        assertNotSame(features1, features2);

        // Modifying one should not affect the other
        features1[0] = null;
        assertNotNull(features2[0]);
    }

    // ==================== Mask comparisons ====================

    @Test
    void testMasks_areDistinct() {
        long defaultMask = WriteConfig.DEFAULT.mask();
        long prettyMask = WriteConfig.PRETTY.mask();
        long withNullsMask = WriteConfig.WITH_NULLS.mask();
        long prettyWithNullsMask = WriteConfig.PRETTY_WITH_NULLS.mask();

        // All non-default masks should be different
        assertNotEquals(defaultMask, prettyMask);
        assertNotEquals(defaultMask, withNullsMask);
        assertNotEquals(defaultMask, prettyWithNullsMask);
        assertNotEquals(prettyMask, withNullsMask);
        assertNotEquals(prettyMask, prettyWithNullsMask);
        assertNotEquals(withNullsMask, prettyWithNullsMask);
    }

    @Test
    void testDefaultMask_isZero() {
        // DEFAULT should have no features enabled
        assertEquals(0L, WriteConfig.DEFAULT.mask());
    }

    @Test
    void testPrettyWithNullsMaskCombination() {
        // PRETTY_WITH_NULLS should combine PRETTY and WITH_NULLS
        long prettyMask = WriteConfig.PRETTY.mask();
        long withNullsMask = WriteConfig.WITH_NULLS.mask();
        long prettyWithNullsMask = WriteConfig.PRETTY_WITH_NULLS.mask();

        // The combined mask should equal OR of individual masks
        assertEquals(prettyMask | withNullsMask, prettyWithNullsMask);
    }

    // ==================== Integration tests ====================

    @Test
    void testWrite_withDefault() {
        TestBean bean = new TestBean();
        bean.name = "test";
        bean.value = 42;
        bean.nullValue = null;

        String json = JSON.write(bean, WriteConfig.DEFAULT);

        // DEFAULT should not include nulls
        assertTrue(json.contains("test"));
        assertTrue(json.contains("42"));
        assertFalse(json.contains("nullValue"));
        assertFalse(json.contains("null"));
    }

    @Test
    void testWrite_withPretty() {
        TestBean bean = new TestBean();
        bean.name = "test";
        bean.value = 42;

        String json = JSON.write(bean, WriteConfig.PRETTY);

        // PRETTY should format with indentation
        assertTrue(json.contains("\n"));
        assertTrue(json.contains("test"));
    }

    @Test
    void testWrite_withNulls() {
        TestBean bean = new TestBean();
        bean.name = "test";
        bean.value = 42;
        bean.nullValue = null;

        String json = JSON.write(bean, WriteConfig.WITH_NULLS);

        // WITH_NULLS should include null values
        assertTrue(json.contains("nullValue"));
        assertTrue(json.contains("null"));
    }

    @Test
    void testWrite_withPrettyWithNulls() {
        TestBean bean = new TestBean();
        bean.name = "test";
        bean.value = 42;
        bean.nullValue = null;

        String json = JSON.write(bean, WriteConfig.PRETTY_WITH_NULLS);

        // PRETTY_WITH_NULLS should format and include nulls
        assertTrue(json.contains("\n"));
        assertTrue(json.contains("nullValue"));
        assertTrue(json.contains("null"));
    }

    @Test
    void testWrite_defaultIsCompact() {
        TestBean bean = new TestBean();
        bean.name = "test";
        bean.value = 42;
        bean.another = "value";

        String defaultJson = JSON.write(bean, WriteConfig.DEFAULT);
        String prettyJson = JSON.write(bean, WriteConfig.PRETTY);

        // Default should be more compact than pretty
        assertTrue(defaultJson.length() < prettyJson.length());
        // Default should not have newlines
        assertFalse(defaultJson.contains("\n"));
    }

    @Test
    void testWrite_prettyFormatConsistent() {
        TestBean bean = new TestBean();
        bean.name = "test";
        bean.value = 42;

        String json1 = JSON.write(bean, WriteConfig.PRETTY);
        String json2 = JSON.write(bean, WriteConfig.PRETTY);

        // Multiple calls should produce the same result
        assertEquals(json1, json2);
    }

    @Test
    void testWrite_withNullsIncludesAllNulls() {
        TestBean bean = new TestBean();
        bean.name = null;
        bean.value = 0;
        bean.nullValue = null;
        bean.another = null;

        String json = JSON.write(bean, WriteConfig.WITH_NULLS);

        // Should include all null fields
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"nullValue\""));
        assertTrue(json.contains("\"another\""));
    }

    // ==================== Helper methods ====================

    private boolean contains(WriteFeature[] array, WriteFeature feature) {
        for (WriteFeature f : array) {
            if (f == feature) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(WriteConfig[] array, WriteConfig config) {
        for (WriteConfig c : array) {
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
        public String nullValue;
        public String another;
    }
}

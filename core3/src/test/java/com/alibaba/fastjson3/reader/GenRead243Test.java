package com.alibaba.fastjson3.reader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for genRead243 algorithm prefix grouping.
 */
public class GenRead243Test {

    static class TestBean {
        public String id;
        public String name;
        public String age;
        public String email;
        public String city;
    }

    @Test
    public void testComputeFieldNamePrefix() {
        // Test prefix computation for various field names

        // 2-char field: "id" -> ['"', 'i', 'd', '"']
        int idPrefix = ObjectReaderCreatorASM.computeFieldNamePrefix("id");
        assertEquals(0x22696422, idPrefix, "id prefix should be 0x22696422");

        // 3-char field: "age" -> ['"', 'a', 'g', 'e']
        int agePrefix = ObjectReaderCreatorASM.computeFieldNamePrefix("age");
        assertEquals(0x22616765, agePrefix, "age prefix should be 0x22616765");

        // 4-char field: "name" -> ['"', 'n', 'a', 'm']
        int namePrefix = ObjectReaderCreatorASM.computeFieldNamePrefix("name");
        assertEquals(0x226e616d, namePrefix, "name prefix should be 0x226e616d");

        // Too short
        int xPrefix = ObjectReaderCreatorASM.computeFieldNamePrefix("x");
        assertEquals(0, xPrefix, "single char field should return 0");

        // Too long
        String longName = "a".repeat(44);
        int longPrefix = ObjectReaderCreatorASM.computeFieldNamePrefix(longName);
        assertEquals(0, longPrefix, "too long field should return 0");
    }

    @Test
    public void testBuildPrefixGroupInfo() {
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("name", null, String.class, String.class, 1, null, false, null, null),
            new FieldReader("age", null, String.class, String.class, 2, null, false, null, null),
            new FieldReader("email", null, String.class, String.class, 3, null, false, null, null),
        };

        ObjectReaderCreatorASM.PrefixGroupInfo info =
            ObjectReaderCreatorASM.buildPrefixGroupInfo(fieldReaders, 0, 0);

        assertTrue(info.enabled, "Should be enabled for valid field names");
        assertEquals(4, info.prefixes.length, "Should have 4 prefix groups");

        // Print for debugging
        ObjectReaderCreatorASM.printPrefixGroupInfo(info);
    }

    @Test
    public void testPrefixGrouping() {
        // Test that fields with similar prefixes are grouped together
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("id2", null, String.class, String.class, 1, null, false, null, null),
            new FieldReader("name", null, String.class, String.class, 2, null, false, null, null),
            new FieldReader("age", null, String.class, String.class, 3, null, false, null, null),
        };

        ObjectReaderCreatorASM.PrefixGroupInfo info =
            ObjectReaderCreatorASM.buildPrefixGroupInfo(fieldReaders, 0, 0);

        assertTrue(info.enabled, "Should be enabled");

        // "id" and "id2" should have different prefixes
        // "id" prefix = 0x22696422
        // "id2" prefix = 0x22696432
        int idPrefix = ObjectReaderCreatorASM.computeFieldNamePrefix("id");
        int id2Prefix = ObjectReaderCreatorASM.computeFieldNamePrefix("id2");

        assertNotEquals(idPrefix, id2Prefix, "id and id2 should have different prefixes");
    }

    @Test
    public void testPrefixGroupInfoWithConstraints() {
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("x", null, String.class, String.class, 1, null, false, null, null),  // too short
        };

        // Should fail because "x" is only 1 character
        ObjectReaderCreatorASM.PrefixGroupInfo info =
            ObjectReaderCreatorASM.buildPrefixGroupInfo(fieldReaders, 0, 0);

        // Since there's a field that doesn't meet constraints, enabled should be false
        // or it should still process valid fields
        assertFalse(info.enabled, "Should be disabled when constraints not met");
        assertEquals(0, info.prefixes.length, "No prefix groups when constraints not met");
    }

    @Test
    public void testPrefixGroupInfoWithAlternateNames() {
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", new String[]{"ID", "Id"}, String.class, String.class, 0, null, false, null, null),
            new FieldReader("name", null, String.class, String.class, 1, null, false, null, null),
        };

        ObjectReaderCreatorASM.PrefixGroupInfo info =
            ObjectReaderCreatorASM.buildPrefixGroupInfo(fieldReaders, 0, 0);

        assertTrue(info.enabled, "Should be enabled");

        // Should have more prefix groups due to alternate names
        assertTrue(info.prefixes.length >= 3, "Should have at least 3 groups (id, ID, Id, name)");
    }
}

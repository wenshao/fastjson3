package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSONParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Prefix243Matcher (genRead243 algorithm from fastjson2).
 */
public class Prefix243MatcherTest {

    static class TestBean {
        public String id;
        public String name;
        public String age;
        public String email;
        public String phone;
        public String city;
        public String state;
        public String zip;
        public String country;
    }

    @Test
    public void testBuildMatcher() {
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("name", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("age", null, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNotNull(matcher, "Matcher should be built for valid field names");
        assertTrue(matcher.isValid(), "Matcher should be valid");
        assertTrue(matcher.getGroupCount() > 0, "Should have at least one prefix group");
    }

    @Test
    public void testBuildMatcherWithInvalidLengths() {
        // Field name too long (> 43 chars)
        String longName = "a".repeat(44);
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader(longName, null, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNull(matcher, "Matcher should be null for field names exceeding length limit");
    }

    @Test
    public void testBuildMatcherWithShortNames() {
        // Single char field name (min is 2)
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("x", null, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNull(matcher, "Matcher should be null for field names below length minimum");
    }

    @Test
    public void testMatchSimpleField() {
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("name", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("age", null, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNotNull(matcher);

        // Test matching "id"
        byte[] json = "\"id\":".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        FieldReader matched = matcher.match(json, 1, json.length);
        assertEquals("id", matched.fieldName);
    }

    @Test
    public void testMatchMultipleFields() {
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("name", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("age", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("email", null, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNotNull(matcher);

        // Test matching each field
        String[] fields = {"id", "name", "age", "email"};
        for (String field : fields) {
            byte[] json = ("\"" + field + "\":").getBytes(java.nio.charset.StandardCharsets.UTF_8);
            FieldReader matched = matcher.match(json, 1, json.length);
            assertEquals(field, matched.fieldName, "Should match " + field);
        }
    }

    @Test
    public void testMatchWithAlternateNames() {
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", new String[]{"ID", "Id"}, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNotNull(matcher);

        // Test primary name
        byte[] json1 = "\"id\":".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        FieldReader matched1 = matcher.match(json1, 1, json1.length);
        assertEquals("id", matched1.fieldName);

        // Test alternate name
        byte[] json2 = "\"ID\":".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        FieldReader matched2 = matcher.match(json2, 1, json2.length);
        assertEquals("id", matched2.fieldName); // Returns reader, not the matched alias
    }

    @Test
    public void testNoMatch() {
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("name", null, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNotNull(matcher);

        // Test non-matching field
        byte[] json = "\"unknown\":".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        FieldReader matched = matcher.match(json, 1, json.length);
        assertNull(matched, "Should return null for unknown field");
    }

    @Test
    public void testUTF8Integration() {
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("name", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("age", null, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNotNull(matcher);

        // Test with just the first field for simplicity
        byte[] jsonBytes = "\"id\":\"123\"".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        try (JSONParser parser = JSONParser.of(jsonBytes)) {
            assertTrue(parser instanceof JSONParser.UTF8, "Should be UTF8 parser");

            JSONParser.UTF8 utf8Parser = (JSONParser.UTF8) parser;

            // Read "id" field using Prefix243Matcher
            FieldReader idField = utf8Parser.readFieldNameMatch243(matcher);
            assertNotNull(idField, "Should match 'id' field");
            assertEquals("id", idField.fieldName);

            String idValue = utf8Parser.readString();
            assertEquals("123", idValue);
        }
    }

    @Test
    public void testPrefixGrouping() {
        // Test that fields with same 4-byte prefix are grouped together
        // "abX" and "abY" share prefix '"', 'a', 'b', first char of X/Y
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("aba", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("abb", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("abc", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("name", null, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNotNull(matcher);

        // Should have multiple prefix groups
        assertTrue(matcher.getGroupCount() >= 2, "Should have at least 2 prefix groups");
    }

    @Test
    public void testTwoCharFieldNames() {
        // Test 2-character field names (edge case)
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("ab", null, String.class, String.class, 0, null, false, null, null),
            new FieldReader("cd", null, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNotNull(matcher);

        // Test matching 2-char field
        byte[] json = "\"ab\":".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        FieldReader matched = matcher.match(json, 1, json.length);
        assertEquals("ab", matched.fieldName);
    }

    @Test
    public void testPrefixCalculation() {
        // Test that prefix calculation is consistent
        FieldReader[] fieldReaders = new FieldReader[] {
            new FieldReader("id", null, String.class, String.class, 0, null, false, null, null),
        };

        Prefix243Matcher matcher = Prefix243Matcher.build(fieldReaders);
        assertNotNull(matcher);

        // For "id", the prefix should be: '"', 'i', 'd', '"' = 0x22696422
        // The JSON input is: "id":
        byte[] json = "\"id\":".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // At runtime, after skipping opening quote, off=1
        // The prefix read is: json[0], json[1], json[2], json[3] = '"', 'i', 'd', '"'
        int runtimePrefix = ((json[0] & 0xFF) << 24)
                          | ((json[1] & 0xFF) << 16)
                          | ((json[2] & 0xFF) << 8)
                          | (json[3] & 0xFF);

        // Debug: print prefix values
        System.err.println("JSON bytes: " + Arrays.toString(json));
        System.err.println("Runtime prefix: 0x" + Integer.toHexString(runtimePrefix));

        // Binary search should find it
        int idx = matcher.binarySearchPrefix(runtimePrefix);
        assertTrue(idx >= 0, "Should find prefix group, got idx=" + idx + ", runtimePrefix=0x" + Integer.toHexString(runtimePrefix));
    }
}

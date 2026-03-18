package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for DirectField supporting methods in JSONParser.UTF8.
 *
 * <p>DirectField optimization uses 4-byte prefix + lookupswitch for O(1) field access.</p>
 */
public class DirectFieldParserTest {

    @Test
    public void testReadInt4() {
        String json = "{\"id\":123}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(bytes)) {
            assertTrue(parser instanceof JSONParser.UTF8, "Should be UTF8 parser");
            JSONParser.UTF8 utf8 = (JSONParser.UTF8) parser;

            // Position at '{', advance to 'i' (after '"')
            utf8.offset = 1; // Skip '{'
            utf8.offset = 1; // Skip '"'

            // Read "id" as 4-byte prefix: "id" = ['"', 'i', 'd', '"'] -> 0x22696422
            int prefix = utf8.readInt4(utf8.offset);
            assertEquals(0x22696422, prefix, "Prefix for 'id' should be 0x22696422");

            // Test with exact offset
            int prefix2 = utf8.readInt4(1);  // Position at '"', reads '"', 'i', 'd', '"'
            assertEquals(0x22696422, prefix2);
        }
    }

    @Test
    public void testCheckFieldName() {
        String json = "{\"id\":123}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(bytes)) {
            JSONParser.UTF8 utf8 = (JSONParser.UTF8) parser;

            // Position after opening quote of "id"
            utf8.offset = 2; // '{"', '"' -> position at 'i'

            assertTrue(utf8.checkFieldName(utf8.offset, "id"), "Should match 'id'");
            assertFalse(utf8.checkFieldName(utf8.offset, "name"), "Should not match 'name'");
        }
    }

    @Test
    public void testCheckFieldNameLength() {
        String json = "{\"id\":123}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(bytes)) {
            JSONParser.UTF8 utf8 = (JSONParser.UTF8) parser;

            utf8.offset = 2; // Position at 'i' in "id"

            assertTrue(utf8.checkFieldNameLength(utf8.offset, 2), "Should have enough bytes for 'id'");
            assertFalse(utf8.checkFieldNameLength(utf8.offset, 10), "Should not have enough bytes for 10-char name");
        }
    }

    @Test
    public void testDirectFieldMatching() {
        // Test the complete DirectField flow
        String json = "{\"id\":123,\"name\":\"test\",\"age\":25}";

        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        try (JSONParser parser = JSONParser.of(bytes)) {
            JSONParser.UTF8 utf8 = (JSONParser.UTF8) parser;

            // Skip '{'
            utf8.offset = 1;

            // Test "id" field
            // Prefix for "id" = 0x22696422
            utf8.offset = 2; // Position at 'i'
            int idPrefix = utf8.readInt4(utf8.offset - 1); // Include quote
            assertEquals(0x22696422, idPrefix);

            assertTrue(utf8.checkFieldName(utf8.offset, "id"), "Should match 'id'");
        }
    }
}

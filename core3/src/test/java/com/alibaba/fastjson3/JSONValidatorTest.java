package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class JSONValidatorTest {
    // ==================== JSONValidator.from(String) ====================

    @Test
    public void testValidObject() {
        try (JSONValidator v = JSONValidator.from("{\"name\":\"John\",\"age\":30}")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Object, v.getType());
        }
    }

    @Test
    public void testValidArray() {
        try (JSONValidator v = JSONValidator.from("[1,2,3]")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Array, v.getType());
        }
    }

    @Test
    public void testValidString() {
        try (JSONValidator v = JSONValidator.from("\"hello\"")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Value, v.getType());
        }
    }

    @Test
    public void testValidNumber() {
        try (JSONValidator v = JSONValidator.from("42")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Value, v.getType());
        }
    }

    @Test
    public void testValidTrue() {
        try (JSONValidator v = JSONValidator.from("true")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Value, v.getType());
        }
    }

    @Test
    public void testValidFalse() {
        try (JSONValidator v = JSONValidator.from("false")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Value, v.getType());
        }
    }

    @Test
    public void testValidNull() {
        try (JSONValidator v = JSONValidator.from("null")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Value, v.getType());
        }
    }

    @Test
    public void testEmptyObject() {
        try (JSONValidator v = JSONValidator.from("{}")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Object, v.getType());
        }
    }

    @Test
    public void testEmptyArray() {
        try (JSONValidator v = JSONValidator.from("[]")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Array, v.getType());
        }
    }

    @Test
    public void testWhitespace() {
        try (JSONValidator v = JSONValidator.from("  \t\n { \"a\" : 1 }  ")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Object, v.getType());
        }
    }

    // ==================== Invalid JSON ====================

    @Test
    public void testInvalidTrailingContent() {
        try (JSONValidator v = JSONValidator.from("{} extra")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidUnclosedObject() {
        try (JSONValidator v = JSONValidator.from("{\"a\":1")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidUnclosedArray() {
        try (JSONValidator v = JSONValidator.from("[1,2")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidUnclosedString() {
        try (JSONValidator v = JSONValidator.from("\"hello")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidKeyword_tru() {
        try (JSONValidator v = JSONValidator.from("tru")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidKeyword_trux() {
        try (JSONValidator v = JSONValidator.from("trux")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidKeyword_fals() {
        try (JSONValidator v = JSONValidator.from("fals")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidKeyword_falsX() {
        try (JSONValidator v = JSONValidator.from("falsX")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidKeyword_nul() {
        try (JSONValidator v = JSONValidator.from("nul")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidKeyword_nulx() {
        try (JSONValidator v = JSONValidator.from("nulx")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidBareWord() {
        try (JSONValidator v = JSONValidator.from("abc")) {
            assertFalse(v.validate());
        }
    }

    // ==================== Number edge cases ====================

    @Test
    public void testValidNegativeNumber() {
        try (JSONValidator v = JSONValidator.from("-1")) {
            assertTrue(v.validate());
        }
    }

    @Test
    public void testValidFloat() {
        try (JSONValidator v = JSONValidator.from("3.14")) {
            assertTrue(v.validate());
        }
    }

    @Test
    public void testValidScientific() {
        try (JSONValidator v = JSONValidator.from("1e10")) {
            assertTrue(v.validate());
        }
    }

    @Test
    public void testValidScientificNegExp() {
        try (JSONValidator v = JSONValidator.from("1.5e-3")) {
            assertTrue(v.validate());
        }
    }

    @Test
    public void testValidZero() {
        try (JSONValidator v = JSONValidator.from("0")) {
            assertTrue(v.validate());
        }
    }

    @Test
    public void testValidZeroFraction() {
        try (JSONValidator v = JSONValidator.from("0.5")) {
            assertTrue(v.validate());
        }
    }

    @Test
    public void testInvalidLeadingZero() {
        // JSON spec: leading zeros not allowed (except "0" alone)
        try (JSONValidator v = JSONValidator.from("01")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidMinusAlone() {
        try (JSONValidator v = JSONValidator.from("-")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidDotOnly() {
        try (JSONValidator v = JSONValidator.from("1.")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidExpNoDigit() {
        try (JSONValidator v = JSONValidator.from("1e")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testInvalidPlusPrefix() {
        try (JSONValidator v = JSONValidator.from("+1")) {
            assertFalse(v.validate());
        }
    }

    // ==================== Null/empty input ====================

    @Test
    public void testNullString() {
        try (JSONValidator v = JSONValidator.from((String) null)) {
            assertFalse(v.validate());
            assertNull(v.getType());
        }
    }

    @Test
    public void testEmptyString() {
        try (JSONValidator v = JSONValidator.from("")) {
            assertFalse(v.validate());
            assertNull(v.getType());
        }
    }

    @Test
    public void testNullBytes() {
        try (JSONValidator v = JSONValidator.fromUtf8(null)) {
            assertFalse(v.validate());
            assertNull(v.getType());
        }
    }

    @Test
    public void testEmptyBytes() {
        try (JSONValidator v = JSONValidator.fromUtf8(new byte[0])) {
            assertFalse(v.validate());
            assertNull(v.getType());
        }
    }

    // ==================== fromUtf8 ====================

    @Test
    public void testFromUtf8Valid() {
        byte[] bytes = "{\"k\":\"v\"}".getBytes(StandardCharsets.UTF_8);
        try (JSONValidator v = JSONValidator.fromUtf8(bytes)) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Object, v.getType());
        }
    }

    @Test
    public void testFromUtf8Invalid() {
        byte[] bytes = "{broken".getBytes(StandardCharsets.UTF_8);
        try (JSONValidator v = JSONValidator.fromUtf8(bytes)) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testFromUtf8Array() {
        byte[] bytes = "[1,2,3]".getBytes(StandardCharsets.UTF_8);
        try (JSONValidator v = JSONValidator.fromUtf8(bytes)) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Array, v.getType());
        }
    }

    // ==================== Cached result ====================

    @Test
    public void testValidateCachedResult() {
        try (JSONValidator v = JSONValidator.from("{\"a\":1}")) {
            assertTrue(v.validate());
            assertTrue(v.validate()); // second call returns cached
        }
    }

    @Test
    public void testGetTypeCallsValidate() {
        try (JSONValidator v = JSONValidator.from("[1]")) {
            // getType() should trigger validate() internally
            assertEquals(JSONValidator.Type.Array, v.getType());
        }
    }

    // ==================== Nested structures ====================

    @Test
    public void testDeepNesting() {
        try (JSONValidator v = JSONValidator.from("{\"a\":{\"b\":{\"c\":[1,[2,[3]]]}}}")) {
            assertTrue(v.validate());
            assertEquals(JSONValidator.Type.Object, v.getType());
        }
    }

    // ==================== JSON.isValid ====================

    @Test
    public void testJsonIsValidString() {
        assertTrue(JSON.isValid("{\"a\":1}"));
        assertTrue(JSON.isValid("[1,2,3]"));
        assertTrue(JSON.isValid("\"hello\""));
        assertTrue(JSON.isValid("42"));
        assertTrue(JSON.isValid("true"));
        assertTrue(JSON.isValid("false"));
        assertTrue(JSON.isValid("null"));
    }

    @Test
    public void testJsonIsValidStringInvalid() {
        assertFalse(JSON.isValid((String) null));
        assertFalse(JSON.isValid(""));
        assertFalse(JSON.isValid("{broken"));
        assertFalse(JSON.isValid("[1,2,"));
        assertFalse(JSON.isValid("trux"));
        assertFalse(JSON.isValid("nulx"));
        assertFalse(JSON.isValid("01"));
    }

    @Test
    public void testJsonIsValidBytes() {
        assertTrue(JSON.isValid("{}".getBytes(StandardCharsets.UTF_8)));
        assertTrue(JSON.isValid("[]".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValid((byte[]) null));
        assertFalse(JSON.isValid(new byte[0]));
        assertFalse(JSON.isValid("{bad".getBytes(StandardCharsets.UTF_8)));
    }

    // ==================== JSON.isValidObject ====================

    @Test
    public void testIsValidObject() {
        assertTrue(JSON.isValidObject("{}"));
        assertTrue(JSON.isValidObject("{\"a\":1}"));
        assertTrue(JSON.isValidObject("  \t{} "));
        assertFalse(JSON.isValidObject("[]"));
        assertFalse(JSON.isValidObject("\"str\""));
        assertFalse(JSON.isValidObject("123"));
        assertFalse(JSON.isValidObject((String) null));
        assertFalse(JSON.isValidObject(""));
        assertFalse(JSON.isValidObject("{bad"));
    }

    @Test
    public void testIsValidObjectBytes() {
        assertTrue(JSON.isValidObject("{}".getBytes(StandardCharsets.UTF_8)));
        assertTrue(JSON.isValidObject("{\"a\":1}".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidObject("[]".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidObject((byte[]) null));
        assertFalse(JSON.isValidObject(new byte[0]));
    }

    // ==================== JSON.isValidArray ====================

    @Test
    public void testIsValidArray() {
        assertTrue(JSON.isValidArray("[]"));
        assertTrue(JSON.isValidArray("[1,2,3]"));
        assertTrue(JSON.isValidArray("  \t[] "));
        assertFalse(JSON.isValidArray("{}"));
        assertFalse(JSON.isValidArray("\"str\""));
        assertFalse(JSON.isValidArray("123"));
        assertFalse(JSON.isValidArray((String) null));
        assertFalse(JSON.isValidArray(""));
        assertFalse(JSON.isValidArray("[bad"));
    }

    @Test
    public void testIsValidArrayBytes() {
        assertTrue(JSON.isValidArray("[]".getBytes(StandardCharsets.UTF_8)));
        assertTrue(JSON.isValidArray("[1,2,3]".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidArray("{}".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidArray((byte[]) null));
        assertFalse(JSON.isValidArray(new byte[0]));
    }

    // ==================== Trailing comma ====================

    @Test
    public void testTrailingCommaObject() {
        try (JSONValidator v = JSONValidator.from("{\"a\":1,}")) {
            assertFalse(v.validate());
        }
    }

    @Test
    public void testTrailingCommaArray() {
        try (JSONValidator v = JSONValidator.from("[1,2,]")) {
            assertFalse(v.validate());
        }
    }

    // ==================== String escapes ====================

    @Test
    public void testStringEscapes() {
        assertTrue(JSON.isValid("\"hello\\nworld\""));
        assertTrue(JSON.isValid("\"tab\\there\""));
        assertTrue(JSON.isValid("\"unicode\\u0041\""));
        assertTrue(JSON.isValid("\"escaped\\\"quote\""));
    }

    // ==================== Complex valid JSON ====================

    @Test
    public void testComplexValid() {
        String json = """
                {
                    "users": [
                        {"name": "Alice", "age": 30, "active": true},
                        {"name": "Bob", "age": null, "active": false}
                    ],
                    "count": 2,
                    "version": "1.0"
                }
                """;
        assertTrue(JSON.isValid(json));
        assertTrue(JSON.isValidObject(json));
        assertFalse(JSON.isValidArray(json));
    }
}

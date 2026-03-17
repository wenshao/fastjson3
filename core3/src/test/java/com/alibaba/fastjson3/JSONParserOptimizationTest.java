package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JSONParser optimization features.
 * Tests digit2() and CHAR1_ESCAPED functionality.
 */
class JSONParserOptimizationTest {

    @Test
    void testDigit2ValidDigits() {
        // digit2 returns: (high_digit * 10) + low_digit
        // For "12": high='1'=1, low='2'=2, result = 1*10 + 2 = 12
        byte[] input = {'1', '2'};
        assertEquals(12, JSONParser.digit2(input, 0));

        // For "99": high='9'=9, low='9'=9, result = 9*10 + 9 = 99
        byte[] input2 = {'9', '9'};
        assertEquals(99, JSONParser.digit2(input2, 0));

        // For "01": high='0'=0, low='1'=1, result = 0*10 + 1 = 1
        byte[] input3 = {'0', '1'};
        assertEquals(1, JSONParser.digit2(input3, 0));

        // For "00": result = 0*10 + 0 = 0
        byte[] input4 = {'0', '0'};
        assertEquals(0, JSONParser.digit2(input4, 0));
    }

    @Test
    void testDigit2InvalidDigits() {
        // Non-ASCII characters
        byte[] input = {'a', 'b'};
        assertEquals(-1, JSONParser.digit2(input, 0));

        // Mixed valid/invalid
        byte[] input2 = {'1', 'a'};
        assertEquals(-1, JSONParser.digit2(input2, 0));

        // Above '9'
        byte[] input3 = {'5', ':'};
        assertEquals(-1, JSONParser.digit2(input3, 0));
    }

    @Test
    void testDigit2AllDigitPairs() {
        // Test all combinations of '0'-'9'
        for (char d1 = '0'; d1 <= '9'; d1++) {
            for (char d2 = '0'; d2 <= '9'; d2++) {
                byte[] input = {(byte) d1, (byte) d2};
                // digit2 returns: (high_digit * 10) + low_digit
                // where high_digit = bytes[off] (d1) and low_digit = bytes[off+1] (d2)
                int expected = (d1 - '0') * 10 + (d2 - '0');
                int result = JSONParser.digit2(input, 0);
                if (result != expected) {
                    fail("Failed for " + d1 + d2 + ": expected " + expected + " but got " + result);
                }
            }
        }
    }

    @Test
    void testChar1EscapedTable() {
        // Test that CHAR1_ESCAPED table is properly initialized
        // Access via reflection since it's private
        try {
            java.lang.reflect.Field field = JSONParser.class.getDeclaredField("CHAR1_ESCAPED");
            field.setAccessible(true);
            int[] char1Escaped = (int[]) field.get(null);

            // Test common escape sequences
            assertEquals('\n', char1Escaped['n']);
            assertEquals('\t', char1Escaped['t']);
            assertEquals('\r', char1Escaped['r']);
            assertEquals('\b', char1Escaped['b']);
            assertEquals('\f', char1Escaped['f']);
            assertEquals('"', char1Escaped['"']);
            assertEquals('\\', char1Escaped['\\']);

            // Test unmapped characters return -1
            assertEquals(-1, char1Escaped['x']);
            assertEquals(-1, char1Escaped['z']);

            // Test that self-mapping entries were removed (code review fix)
            // These should be -1 (unmapped) not mapping to themselves
            assertEquals(-1, char1Escaped['#'], "# should not be in CHAR1_ESCAPED");
            assertEquals(-1, char1Escaped['&'], "& should not be in CHAR1_ESCAPED");
            assertEquals(-1, char1Escaped['['], "[ should not be in CHAR1_ESCAPED");
            assertEquals(-1, char1Escaped[']'], "] should not be in CHAR1_ESCAPED");
            assertEquals(-1, char1Escaped['@'], "@ should not be in CHAR1_ESCAPED");
            assertEquals(-1, char1Escaped[' '], "space should not be in CHAR1_ESCAPED");

        } catch (Exception e) {
            fail("Failed to access CHAR1_ESCAPED: " + e.getMessage());
        }
    }

    @Test
    void testParseIntWithDigit2() {
        // Test the optimized readIntDirect method
        JSONParser parser = JSONParser.of("{\"value\":12345678}");
        JSONObject obj = parser.readObject();
        assertEquals(12345678, obj.getIntValue("value"));
    }

    @Test
    void testParseIntLargeNumber() {
        // Test parsing large integers that benefit from digit2
        JSONParser parser = JSONParser.of("{\"id\":98765432,\"count\":1000000}");
        JSONObject obj = parser.readObject();
        assertEquals(98765432, obj.getIntValue("id"));
        assertEquals(1000000, obj.getIntValue("count"));
    }

    @Test
    void testParseIntMaxValue() {
        // Test Integer.MAX_VALUE (10 digits)
        JSONParser parser = JSONParser.of("{\"max\":2147483647}");
        JSONObject obj = parser.readObject();
        assertEquals(Integer.MAX_VALUE, obj.getIntValue("max"));
    }

    @Test
    void testParseIntMinValue() {
        // Test Integer.MIN_VALUE
        JSONParser parser = JSONParser.of("{\"min\":-2147483648}");
        JSONObject obj = parser.readObject();
        assertEquals(Integer.MIN_VALUE, obj.getIntValue("min"));
    }

    @Test
    void testEscapeSequences() {
        // Test CHAR1_ESCAPED lookup in actual parsing
        JSONParser parser = JSONParser.of("{\"text\":\"line1\\nline2\\ttab\\rcarriage\"}");
        JSONObject obj = parser.readObject();
        String text = obj.getString("text");
        assertTrue(text.contains("\n"));
        assertTrue(text.contains("\t"));
        assertTrue(text.contains("\r"));
        assertEquals("line1\nline2\ttab\rcarriage", text);
    }

    @Test
    void testEscapeBackslash() {
        JSONParser parser = JSONParser.of("{\"path\":\"C:\\\\Users\\\\test\"}");
        JSONObject obj = parser.readObject();
        assertEquals("C:\\Users\\test", obj.getString("path"));
    }

    @Test
    void testEscapeQuote() {
        JSONParser parser = JSONParser.of("{\"msg\":\"He said \\\"hello\\\"\"}");
        JSONObject obj = parser.readObject();
        assertEquals("He said \"hello\"", obj.getString("msg"));
    }

    @Test
    void testAllStandardEscapes() {
        // Test all standard escape sequences
        String json = "{\"a\":\"\\0\",\"b\":\"\\b\",\"t\":\"\\t\",\"n\":\"\\n\",\"v\":\"\\u000b\"," +
                      "\"f\":\"\\f\",\"r\":\"\\r\"}";
        JSONParser parser = JSONParser.of(json);
        JSONObject obj = parser.readObject();
        assertEquals(0, obj.getString("a").charAt(0));
        assertEquals('\b', obj.getString("b").charAt(0));
        assertEquals('\t', obj.getString("t").charAt(0));
        assertEquals('\n', obj.getString("n").charAt(0));
        assertEquals('\u000b', obj.getString("v").charAt(0));
        assertEquals('\f', obj.getString("f").charAt(0));
        assertEquals('\r', obj.getString("r").charAt(0));
    }

    @Test
    void testDigit2InMultiDigitNumber() {
        // Test that digit2 correctly handles multi-digit numbers
        JSONParser parser = JSONParser.of("{\"a\":1234,\"b\":5678,\"c\":9999}");
        JSONObject obj = parser.readObject();
        assertEquals(1234, obj.getIntValue("a"));
        assertEquals(5678, obj.getIntValue("b"));
        assertEquals(9999, obj.getIntValue("c"));
    }

    @Test
    void testParseIntEdgeCases() {
        // Test 1-digit numbers (digit2 not used)
        JSONParser parser = JSONParser.of("{\"a\":1,\"b\":9}");
        JSONObject obj = parser.readObject();
        assertEquals(1, obj.getIntValue("a"));
        assertEquals(9, obj.getIntValue("b"));

        // Test 10-digit number (boundary case)
        parser = JSONParser.of("{\"x\":1234567890}");
        obj = parser.readObject();
        assertEquals(1234567890L, obj.getLongValue("x"));
    }

    @Test
    void testParseIntWithDigit2Utf8Path() {
        // Test the optimized readIntDirect method via UTF-8 byte[] path
        // This exercises JSONParser.UTF8.readIntDirect() where digit2 optimization lives
        byte[] jsonBytes = "{\"value\":12345678}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        JSONParser parser = JSONParser.of(jsonBytes);
        JSONObject obj = parser.readObject();
        assertEquals(12345678, obj.getIntValue("value"));
    }

    @Test
    void testParseIntLargeNumberUtf8Path() {
        // Test parsing large integers via UTF-8 byte[] path
        byte[] jsonBytes = "{\"id\":98765432,\"count\":1000000}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        JSONParser parser = JSONParser.of(jsonBytes);
        JSONObject obj = parser.readObject();
        assertEquals(98765432, obj.getIntValue("id"));
        assertEquals(1000000, obj.getIntValue("count"));
    }

    @Test
    void testParseIntMaxValueUtf8Path() {
        // Test Integer.MAX_VALUE via UTF-8 byte[] path
        byte[] jsonBytes = "{\"max\":2147483647}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        JSONParser parser = JSONParser.of(jsonBytes);
        JSONObject obj = parser.readObject();
        assertEquals(Integer.MAX_VALUE, obj.getIntValue("max"));
    }

    @Test
    void testParseIntMinValueUtf8Path() {
        // Test Integer.MIN_VALUE via UTF-8 byte[] path
        byte[] jsonBytes = "{\"min\":-2147483648}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        JSONParser parser = JSONParser.of(jsonBytes);
        JSONObject obj = parser.readObject();
        assertEquals(Integer.MIN_VALUE, obj.getIntValue("min"));
    }

    @Test
    void testDigit2InMultiDigitNumberUtf8Path() {
        // Test that digit2 correctly handles multi-digit numbers via UTF-8 byte[] path
        byte[] jsonBytes = "{\"a\":1234,\"b\":5678,\"c\":9999}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        JSONParser parser = JSONParser.of(jsonBytes);
        JSONObject obj = parser.readObject();
        assertEquals(1234, obj.getIntValue("a"));
        assertEquals(5678, obj.getIntValue("b"));
        assertEquals(9999, obj.getIntValue("c"));
    }

    @Test
    void testParseIntOverflowFallback() {
        // Values that exceed Integer.MAX_VALUE (2147483647) should fall back to long/readNumber
        // and return a truncated int value via intValue()
        // 9999999999 = 9 * 10^9 > MAX_INT; readNumber().intValue() wraps as per Number contract
        byte[] jsonBytes = "{\"a\":9999999999,\"b\":3000000000}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        JSONParser parser = JSONParser.of(jsonBytes);
        JSONObject obj = parser.readObject();
        // getLongValue should return the correct long regardless
        assertEquals(9999999999L, obj.getLongValue("a"));
        assertEquals(3000000000L, obj.getLongValue("b"));
    }

    @Test
    void testParseIntOverflowBoundaryUtf8() {
        // MAX_INT + 1 = 2147483648 — must not corrupt parser state
        byte[] jsonBytes = "{\"x\":2147483648,\"y\":42}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        JSONParser parser = JSONParser.of(jsonBytes);
        JSONObject obj = parser.readObject();
        // 'y' must still parse correctly (parser state not corrupted by overflow in 'x')
        assertEquals(42, obj.getIntValue("y"));
        assertEquals(2147483648L, obj.getLongValue("x"));
    }
}

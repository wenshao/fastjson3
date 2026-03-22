package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge case tests for string parsing and encoding.
 * Inspired by fastjson2 JSONWriterUTF8Test, JSONWriterUTF16Test, JSONReaderUTF8Test,
 * EscapeNoneAsciiTest, and jackson StringConstraintsTest.
 */
class StringEncodingEdgeTest {

    // ==================== Escape sequences ====================

    @Test
    void parseAllEscapeSequences() {
        String json = "\"\\r\\n\\t\\f\\b\\\\\\/\\\"\"";
        Object result = JSON.parse(json);
        assertEquals("\r\n\t\f\b\\/\"", result);
    }

    @Test
    void parseUnicodeEscape_basic() {
        assertEquals("\u4e2d\u6587", JSON.parse("\"\\u4e2d\\u6587\""));
    }

    @Test
    void parseUnicodeEscape_uppercase() {
        assertEquals("\u4E2D", JSON.parse("\"\\u4E2D\""));
    }

    @Test
    void parseUnicodeEscape_nullChar() {
        String result = (String) JSON.parse("\"\\u0000\"");
        assertEquals(1, result.length());
        assertEquals('\0', result.charAt(0));
    }

    // ==================== Control characters — write then round-trip ====================

    @Test
    void writeAndParseControlChar_tab() {
        String input = "\t";
        String json = JSON.toJSONString(input);
        assertTrue(json.contains("\\t"), "Tab should be escaped: " + json);
        assertEquals(input, JSON.parseObject(json, String.class));
    }

    @Test
    void writeAndParseControlChar_newline() {
        String input = "\n";
        String json = JSON.toJSONString(input);
        assertTrue(json.contains("\\n"), "Newline should be escaped: " + json);
        assertEquals(input, JSON.parseObject(json, String.class));
    }

    @Test
    void writeAndParseControlChar_carriageReturn() {
        String input = "\r";
        String json = JSON.toJSONString(input);
        assertTrue(json.contains("\\r"), "CR should be escaped: " + json);
        assertEquals(input, JSON.parseObject(json, String.class));
    }

    @Test
    void writeAndParseControlChar_backspace() {
        String input = "\b";
        String json = JSON.toJSONString(input);
        assertTrue(json.contains("\\b"), "Backspace should be escaped: " + json);
        assertEquals(input, JSON.parseObject(json, String.class));
    }

    @Test
    void writeAndParseControlChar_formFeed() {
        String input = "\f";
        String json = JSON.toJSONString(input);
        assertTrue(json.contains("\\f"), "Form feed should be escaped: " + json);
        assertEquals(input, JSON.parseObject(json, String.class));
    }

    @Test
    void writeAndParseQuoteAndBackslash() {
        String input = "\"\\";
        String json = JSON.toJSONString(input);
        assertTrue(json.contains("\\\""), "Quote should be escaped");
        assertTrue(json.contains("\\\\"), "Backslash should be escaped");
        assertEquals(input, JSON.parseObject(json, String.class));
    }

    // ==================== CJK / Unicode characters ====================

    @Test
    void parseCJK_direct() {
        JSONObject obj = JSON.parseObject("{\"name\":\"\u4e2d\u56fd\"}");
        assertEquals("\u4e2d\u56fd", obj.getString("name"));
    }

    @Test
    void parseCJK_fromUTF8Bytes() {
        String json = "{\"name\":\"\u4e2d\u56fd\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        JSONObject obj = JSON.parseObject(bytes);
        assertEquals("\u4e2d\u56fd", obj.getString("name"));
    }

    @Test
    void writeCJK_roundTrip() {
        String original = "\u4e2d\u56fd\u4eba\u6c11";
        String json = JSON.toJSONString(original);
        String parsed = JSON.parseObject(json, String.class);
        assertEquals(original, parsed);
    }

    // ==================== Emoji / Surrogate pairs ====================

    @Test
    void parseEmoji_smiley() {
        String emoji = "\uD83D\uDE0D"; // 😍
        String json = "{\"emoji\":\"" + emoji + "\"}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals(emoji, obj.getString("emoji"));
    }

    @Test
    void parseEmoji_fromUTF8Bytes() {
        String emoji = "\uD83D\uDE0D"; // 😍
        byte[] bytes = ("{\"emoji\":\"" + emoji + "\"}").getBytes(StandardCharsets.UTF_8);
        JSONObject obj = JSON.parseObject(bytes);
        assertEquals(emoji, obj.getString("emoji"));
    }

    @Test
    void writeEmoji_roundTrip() {
        String text = "Hello \uD83D\uDE0D World \uD83D\uDC4C";
        String json = JSON.toJSONString(text);
        String parsed = JSON.parseObject(json, String.class);
        assertEquals(text, parsed);
    }

    @Test
    void parseMixedUnicode_ascii_cjk_emoji() {
        String mixed = "abcdef1234567890\u4e2d\u56fd\u00a9\u00ae\u00a3\uD83D\uDE0D\uD83D\uDC4C";
        String json = JSON.toJSONString(mixed);
        String parsed = JSON.parseObject(json, String.class);
        assertEquals(mixed, parsed);
    }

    @Test
    void writeMultipleEmoji_roundTrip() {
        // 😍🚀👌🎍
        String text = "\uD83D\uDE0D\uD83D\uDE80\uD83D\uDC4C\uD83C\uDF8D";
        String json = JSON.toJSONString(text);
        String parsed = JSON.parseObject(json, String.class);
        assertEquals(text, parsed);
    }

    // ==================== Large strings ====================

    @Test
    void parseLargeString_2048chars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2048; i++) {
            sb.append('a');
        }
        String large = sb.toString();
        String json = "\"" + large + "\"";
        assertEquals(large, JSON.parse(json));
    }

    @Test
    void parseLargeString_withUnicode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("Hello\u4e2d\u56fd");
        }
        String large = sb.toString();
        String json = JSON.toJSONString(large);
        String parsed = JSON.parseObject(json, String.class);
        assertEquals(large, parsed);
    }

    @Test
    void parseLargeString_withEmoji() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("text\uD83D\uDE0D");
        }
        String large = sb.toString();
        String json = JSON.toJSONString(large);
        String parsed = JSON.parseObject(json, String.class);
        assertEquals(large, parsed);
    }

    // ==================== Empty and special strings ====================

    @Test
    void parseEmptyString() {
        assertEquals("", JSON.parse("\"\""));
    }

    @Test
    void parseSingleCharString() {
        assertEquals("a", JSON.parse("\"a\""));
    }

    @Test
    void parseStringWithOnlySpaces() {
        assertEquals("   ", JSON.parse("\"   \""));
    }

    @Test
    void parseStringWithSlash() {
        assertEquals("a/b", JSON.parse("\"a/b\""));
        assertEquals("a/b", JSON.parse("\"a\\/b\""));
    }

    // ==================== String keys with special chars ====================

    @Test
    void parseFieldNameWithCJK() {
        JSONObject obj = JSON.parseObject("{\"\\u540d\\u524d\":\"value\"}");
        assertEquals("value", obj.getString("\u540d\u524d"));
    }

    @Test
    void parseFieldNameWithSpecialChars() {
        JSONObject obj = JSON.parseObject("{\"key with spaces\":1,\"key\\\"quote\":2}");
        assertEquals(1, obj.getIntValue("key with spaces"));
        assertEquals(2, obj.getIntValue("key\"quote"));
    }

    // ==================== AllowSingleQuotes feature ====================

    @Test
    void allowSingleQuotes_string() {
        Object result = JSON.parse("'hello'", ReadFeature.AllowSingleQuotes);
        assertEquals("hello", result);
    }

    @Test
    void allowSingleQuotes_object() {
        Object result = JSON.parse("{'name':'test','age':25}", ReadFeature.AllowSingleQuotes);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("test", ((JSONObject) result).getString("name"));
    }

    @Test
    void allowSingleQuotes_nested() {
        Object result = JSON.parse("{'list':['a','b'],'obj':{'k':'v'}}", ReadFeature.AllowSingleQuotes);
        assertInstanceOf(JSONObject.class, result);
        JSONObject obj = (JSONObject) result;
        assertEquals(2, obj.getJSONArray("list").size());
        assertEquals("v", obj.getJSONObject("obj").getString("k"));
    }

    @Test
    void allowSingleQuotes_withEscape() {
        Object result = JSON.parse("'it\\'s a test'", ReadFeature.AllowSingleQuotes);
        assertEquals("it's a test", result);
    }

    // ==================== UTF-8 byte input parsing ====================

    @Test
    void parseUTF8_latin1Range() {
        String text = "\u00a9\u00ae\u00e9\u00fc"; // ©®éü
        byte[] bytes = ("{\"text\":\"" + text + "\"}").getBytes(StandardCharsets.UTF_8);
        JSONObject obj = JSON.parseObject(bytes);
        assertEquals(text, obj.getString("text"));
    }

    @Test
    void parseUTF8_multibyte_2byte() {
        String text = "\u00e9\u00fc\u00f1"; // éüñ
        String json = JSON.toJSONString(text);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        String parsed = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), String.class);
        assertEquals(text, parsed);
    }

    @Test
    void parseUTF8_multibyte_3byte() {
        String text = "\u4e2d\u6587\u6d4b\u8bd5";
        byte[] bytes = ("{\"t\":\"" + text + "\"}").getBytes(StandardCharsets.UTF_8);
        JSONObject obj = JSON.parseObject(bytes);
        assertEquals(text, obj.getString("t"));
    }

    @Test
    void parseUTF8_multibyte_4byte() {
        String text = "\uD83D\uDE00\uD83D\uDE01"; // 😀😁
        byte[] bytes = ("{\"e\":\"" + text + "\"}").getBytes(StandardCharsets.UTF_8);
        JSONObject obj = JSON.parseObject(bytes);
        assertEquals(text, obj.getString("e"));
    }

    // ==================== String round-trip consistency ====================

    @Test
    void roundTrip_stringVsBytes() {
        String original = "Hello \u4e2d\u56fd \uD83D\uDE0D World! \r\n\t \"quoted\" \\back";
        String json = JSON.toJSONString(original);

        // Parse from String
        String fromString = JSON.parseObject(json, String.class);
        // Parse from UTF-8 bytes
        JSONObject wrapper = JSON.parseObject(("{\"v\":" + json + "}").getBytes(StandardCharsets.UTF_8));
        String fromBytes = wrapper.getString("v");

        assertEquals(original, fromString);
        assertEquals(original, fromBytes);
    }
}

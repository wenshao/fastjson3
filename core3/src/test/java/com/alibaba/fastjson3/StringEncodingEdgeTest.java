package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @Test
    void escapeNoneAscii_singleChar() {
        // EscapeNoneAscii escapes non-ASCII chars to \\uXXXX sequences
        String input = "\u4e2d";
        String json = JSON.toJSONString(input, WriteFeature.EscapeNoneAscii);
        assertTrue(json.contains("\\u4e2d"), "CJK char should be escaped to unicode sequence: " + json);
        // Round-trip should still work
        String parsed = JSON.parseObject(json, String.class);
        assertEquals(input, parsed);
    }

    @Test
    void escapeNoneAscii_mixedWithAscii() {
        // EscapeNoneAscii escapes non-ASCII chars to \\uXXXX sequences
        String input = "Hello \u4e2d\u56fd World";
        String json = JSON.toJSONString(input, WriteFeature.EscapeNoneAscii);
        assertTrue(json.contains("Hello"), "ASCII should be preserved");
        assertTrue(json.contains("World"), "ASCII should be preserved");
        assertTrue(json.contains("\\u"), "Non-ASCII should be escaped");
        // Round-trip
        String parsed = JSON.parseObject(json, String.class);
        assertEquals(input, parsed);
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

    // ==================== Latin-1 high-byte UTF-8 output ====================

    @Test
    void writeBytes_latin1HighBytes_roundTrip() {
        // Latin-1 chars U+0080-U+00FF must be encoded as 2-byte UTF-8 in toJSONBytes output.
        // Previously, noEscape4 incorrectly passed these through as single bytes (invalid UTF-8).
        // Test includes boundary values U+0080 and U+00FF and a mix of mid-range chars.
        String text = "\u0080\u00e9\u00fc\u00f1\u00e0\u00ff"; // U+0080, éüñà, U+00FF
        byte[] jsonBytes = JSON.toJSONBytes(text);
        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        assertEquals(text, JSON.parseObject(json, String.class), "Round-trip for Latin-1 high bytes");
    }

    @Test
    void writeBytes_latin1HighBytes_after8AlignedChunk() {
        // 8 ASCII bytes trigger the 8-byte UNSAFE fast path; 4 Latin-1 high bytes follow in
        // the tail and must be 2-byte UTF-8 encoded (regression for noEscape4 bug).
        String text = "AAAAAAAA\u00e9\u00fc\u00f1\u00e0"; // 8 ASCII + 4 Latin-1 high
        byte[] jsonBytes = JSON.toJSONBytes(text);
        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        assertEquals(text, JSON.parseObject(json, String.class), "Round-trip for tail Latin-1 bytes");
    }

    // ==================== Lone surrogate handling (UTF-8 byte path) ====================
    // RFC 3629 \u00a73 forbids isolated surrogate code points in UTF-8. The byte
    // generator must replace lone surrogates with U+FFFD when serialising.

    @Test
    void writeBytes_loneHighSurrogate_emitsReplacementChar() {
        String input = "x" + (char) 0xD800 + "y";
        byte[] bytes = JSON.toJSONBytes(input);
        // Expected: "x" + \xEF\xBF\xBD (U+FFFD) + "y" wrapped in quotes
        assertArrayEquals(
                new byte[]{'"', 'x', (byte) 0xEF, (byte) 0xBF, (byte) 0xBD, 'y', '"'},
                bytes,
                "Lone high surrogate in value must emit U+FFFD");
    }

    @Test
    void writeBytes_loneLowSurrogate_emitsReplacementChar() {
        String input = "x" + (char) 0xDC00 + "y";
        byte[] bytes = JSON.toJSONBytes(input);
        // Pre-fix this emitted CESU-8 bytes 0xED 0xB0 0x80 (invalid UTF-8).
        assertArrayEquals(
                new byte[]{'"', 'x', (byte) 0xEF, (byte) 0xBF, (byte) 0xBD, 'y', '"'},
                bytes,
                "Lone low surrogate in value must emit U+FFFD");
    }

    @Test
    void writeBytes_pairedSurrogates_emitsFourByteUtf8() {
        // U+1F600 (\ud83d\ude00) round-trips correctly via 4-byte UTF-8.
        String input = "x" + new String(Character.toChars(0x1F600)) + "y";
        byte[] bytes = JSON.toJSONBytes(input);
        assertArrayEquals(
                new byte[]{'"', 'x', (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80, 'y', '"'},
                bytes);
        assertEquals(input, JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), String.class));
    }

    @Test
    void writeBytes_loneLowSurrogateInFieldName_emitsReplacementChar() {
        // Default writeName path (quoted) \u2014 first of the two writeName
        // branches the fix touches. See the UnquoteFieldName variant below
        // for the second branch.
        String key = "a" + (char) 0xDC00 + "b";
        JSONObject obj = new JSONObject();
        obj.put(key, "v");
        byte[] bytes = JSON.toJSONBytes(obj);
        // Validate output decodes cleanly as UTF-8 (no CESU-8 bytes).
        String decoded = new String(bytes, StandardCharsets.UTF_8);
        JSONObject reparsed = JSON.parseObject(decoded);
        assertTrue(reparsed.containsKey("a\ufffdb"));
        assertEquals("v", reparsed.get("a\ufffdb"));
    }

    @Test
    void writeBytes_loneLowSurrogateInFieldName_unquoteFieldName() {
        // Second writeName branch \u2014 gated by WriteFeature.UnquoteFieldName.
        // The fix touched both branches identically; this exercises the
        // one the default-feature test above doesn't reach.
        String key = "a" + (char) 0xDC00 + "b";
        JSONObject obj = new JSONObject();
        obj.put(key, "v");
        byte[] bytes = JSON.toJSONBytes(obj, WriteFeature.UnquoteFieldName);
        // Should contain U+FFFD bytes, not CESU-8 ED B0 80.
        assertEquals(-1, indexOfBytes(bytes, new byte[]{(byte) 0xED, (byte) 0xB0, (byte) 0x80}),
                "must not emit CESU-8 surrogate bytes");
        assertTrue(indexOfBytes(bytes, new byte[]{(byte) 0xEF, (byte) 0xBF, (byte) 0xBD}) >= 0,
                "must emit U+FFFD replacement");
    }

    @Test
    void writeBytes_fieldWriterEncodeNameBytes_usesReplacementChar() {
        // FieldWriter.encodeNameBytes pre-encodes `@JSONField(name="...")`
        // at FieldWriter init time. Pre-fix it routed through Java's default
        // `String.getBytes(UTF_8)` which substitutes `?` (0x3F) for lone
        // surrogates \u2014 inconsistent with the value path's U+FFFD behaviour.
        // Reverse audit P2: produce identical wire bytes regardless of
        // whether a lone surrogate appears in name vs value.
        SurrogateNameBean b = new SurrogateNameBean();
        b.x = 1;
        byte[] bytes = JSON.toJSONBytes(b);
        // Field name was encoded by FieldWriter.encodeNameBytes once at init.
        // Pre-fix: `78 3f 79` (`x?y`); post-fix: `78 ef bf bd 79` (`x` + U+FFFD + `y`).
        assertTrue(indexOfBytes(bytes, new byte[]{(byte) 0xEF, (byte) 0xBF, (byte) 0xBD}) >= 0,
                "name must emit U+FFFD via FieldWriter.encodeNameBytes");
        // Also confirm no lingering `?` substitute inside the quoted name.
        assertEquals(-1, indexOfBytes(bytes, new byte[]{'?'}),
                "must not contain JDK default replacement '?'");
    }

    static class SurrogateNameBean {
        // Java strings can hold lone surrogates as char[]; the annotation's
        // String constant pool can therefore carry an isolated 0xDC00.
        @com.alibaba.fastjson3.annotation.JSONField(name = "x\uDC00y")
        public int x;
    }

    private static int indexOfBytes(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0; i + needle.length <= haystack.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    @Test
    void charGeneratorToByteArray_loneSurrogate_emitsReplacementChar() {
        // JSONGenerator.Char buffers chars; toByteArray() converts the
        // accumulated chars to UTF-8 bytes. Without an explicit encoder
        // configuration, JDK's default String.getBytes(UTF_8) substitutes
        // `?` (0x3F) for lone surrogates — diverges from the UTF8
        // generator's U+FFFD policy on the same input.
        try (com.alibaba.fastjson3.JSONGenerator g = com.alibaba.fastjson3.JSONGenerator.of()) {
            g.writeString("x" + (char) 0xDC00 + "y");
            byte[] bytes = g.toByteArray();
            assertEquals(-1, indexOfBytes(bytes, new byte[]{'?'}),
                    "Char gen toByteArray must not substitute '?'");
            assertTrue(indexOfBytes(bytes, new byte[]{(byte) 0xEF, (byte) 0xBF, (byte) 0xBD}) >= 0,
                    "Char gen toByteArray must emit U+FFFD for lone surrogate");
        }
    }
}

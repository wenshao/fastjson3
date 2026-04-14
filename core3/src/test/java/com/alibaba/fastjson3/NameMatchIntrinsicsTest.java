package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the fastjson2-compatible name-match intrinsics on {@link JSONParser.UTF8}.
 *
 * <p>These methods have a pre-check contract: {@code this.offset} must be positioned
 * at the first character of the field name (one past the opening quote), and the
 * caller has already validated the leading 4 bytes via a switch on {@link
 * JSONParser.UTF8#getRawInt()}. Tests here simulate that contract by manually
 * positioning offset after the opening quote.</p>
 */
class NameMatchIntrinsicsTest {
    /** Build a UTF8 parser from raw bytes (public JSONParser API). */
    private static JSONParser.UTF8 parser(byte[] bytes) {
        return (JSONParser.UTF8) JSONParser.of(bytes);
    }

    /**
     * Position {@code this.offset} at the first char of the field name inside
     * the JSON text. Uses reflection to set the protected offset field directly.
     */
    private static void seekToName(JSONParser.UTF8 p, byte[] bytes, String name) throws Exception {
        byte[] target = ("\"" + name + "\"").getBytes(StandardCharsets.UTF_8);
        int idx = indexOf(bytes, target);
        assertTrue(idx >= 0, "name '" + name + "' not found in: " + new String(bytes));
        Field f = JSONParser.class.getDeclaredField("offset");
        f.setAccessible(true);
        f.setInt(p, idx + 1);   // skip opening quote
    }

    private static int indexOf(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static int getOffset(JSONParser p) throws Exception {
        Field f = JSONParser.class.getDeclaredField("offset");
        f.setAccessible(true);
        return f.getInt(p);
    }

    // ---------- getRawInt / getRawLong ----------

    @Test
    void rawInt_readsNativeOrderInt() throws Exception {
        byte[] b = "\"abcd\":1".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "abcd");
        int raw = p.getRawInt();
        // Native-order int from bytes {'a','b','c','d'}
        int expected = com.alibaba.fastjson3.util.JDKUtils.getIntDirect(b, 1);
        assertEquals(expected, raw);
    }

    @Test
    void rawLong_readsNativeOrderLong() throws Exception {
        byte[] b = "\"abcdefgh\":1".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "abcdefgh");
        long raw = p.getRawLong();
        long expected = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(b, 1);
        assertEquals(expected, raw);
    }

    // ---------- Match2 ----------

    @Test
    void match2_success() throws Exception {
        byte[] b = "\"ab\":42".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "ab");
        assertTrue(p.nextIfName4Match2());
        // offset should now point at '4' (start of "42")
        assertEquals((byte) '4', b[getOffset(p)]);
    }

    @Test
    void match2_skipsTrailingWhitespace() throws Exception {
        byte[] b = "\"ab\":   42".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "ab");
        assertTrue(p.nextIfName4Match2());
        assertEquals((byte) '4', b[getOffset(p)]);
    }

    @Test
    void match2_failsOnMissingColon() throws Exception {
        byte[] b = "\"ab\",42".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "ab");
        int preOff = getOffset(p);
        assertFalse(p.nextIfName4Match2());
        assertEquals(preOff, getOffset(p), "offset must not advance on failure");
    }

    // ---------- Match3 ----------

    @Test
    void match3_success() throws Exception {
        byte[] b = "\"uri\":42".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "uri");
        assertTrue(p.nextIfName4Match3());
        assertEquals((byte) '4', b[getOffset(p)]);
    }

    @Test
    void match3_failsOnMissingClosingQuote() throws Exception {
        // Corrupt the bytes: put X where closing quote should be
        byte[] b = "\"uriX:42".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        Field f = JSONParser.class.getDeclaredField("offset");
        f.setAccessible(true);
        f.setInt(p, 1);   // first char of name
        assertFalse(p.nextIfName4Match3());
    }

    // ---------- Match4 ----------

    @Test
    void match4_success() throws Exception {
        byte[] b = "\"size\":99".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "size");
        assertTrue(p.nextIfName4Match4((byte) 'e'));
        assertEquals((byte) '9', b[getOffset(p)]);
    }

    @Test
    void match4_failsOnWrongC4() throws Exception {
        byte[] b = "\"size\":99".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "size");
        int preOff = getOffset(p);
        assertFalse(p.nextIfName4Match4((byte) 'X'));
        assertEquals(preOff, getOffset(p));
    }

    // ---------- Match5 ----------

    @Test
    void match5_success() throws Exception {
        byte[] b = "\"media\":1".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "media");
        // name1 = last 4 bytes of "dia\"" as native-order int
        int name1 = com.alibaba.fastjson3.util.JDKUtils.getIntDirect(b, 4);  // bytes[3..6] = d,i,a,"
        assertTrue(p.nextIfName4Match5(name1));
        assertEquals((byte) '1', b[getOffset(p)]);
    }

    @Test
    void match5_failsOnWrongConstant() throws Exception {
        byte[] b = "\"media\":1".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "media");
        assertFalse(p.nextIfName4Match5(0xDEADBEEF));
    }

    // ---------- Match6 ----------

    @Test
    void match6_success() throws Exception {
        byte[] b = "\"height\":1".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "height");
        // Match6: offset+8, reads int at offset+3 (= last 4 of "ight"), then checks bytes[offset+8-2]='"', bytes[offset+8-1]=':'
        // But actually looking at the impl: off=offset+8, reads int at off-5 = offset+3
        int name1 = com.alibaba.fastjson3.util.JDKUtils.getIntDirect(b, 4);  // bytes[3..6] = i,g,h,t
        assertTrue(p.nextIfName4Match6(name1));
        assertEquals((byte) '1', b[getOffset(p)]);
    }

    // ---------- Match7 ----------

    @Test
    void match7_success() throws Exception {
        byte[] b = "\"bitrate\":1".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "bitrate");
        // off = offset+9, reads int at off-6 = offset+3
        int name1 = com.alibaba.fastjson3.util.JDKUtils.getIntDirect(b, 4);  // bytes[3..6] = t,r,a,t
        assertTrue(p.nextIfName4Match7(name1));
        assertEquals((byte) '1', b[getOffset(p)]);
    }

    // ---------- Match8 ----------

    @Test
    void match8_success() throws Exception {
        byte[] b = "\"duration\":1".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "duration");
        // off = offset+10, reads int at off-7 = offset+3, c8 = bytes[off-3] = bytes[offset+7] = 'n'
        int name1 = com.alibaba.fastjson3.util.JDKUtils.getIntDirect(b, 4);  // bytes[3..6] = r,a,t,i
        assertTrue(p.nextIfName4Match8(name1, (byte) 'n'));
        assertEquals((byte) '1', b[getOffset(p)]);
    }

    // ---------- Match9 ----------

    @Test
    void match9_success() throws Exception {
        byte[] b = "\"copyright\":1".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "copyright");
        // off = offset+11, reads long at off-8 = offset+3 (bytes[3..10] = p,y,r,i,g,h,t,")
        long name1 = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(b, 4);
        assertTrue(p.nextIfName4Match9(name1));
        assertEquals((byte) '1', b[getOffset(p)]);
    }

    @Test
    void match9_failsOnWrongLong() throws Exception {
        byte[] b = "\"copyright\":1".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "copyright");
        int preOff = getOffset(p);
        assertFalse(p.nextIfName4Match9(0x1122334455667788L));
        assertEquals(preOff, getOffset(p));
    }

    // ---------- Near-end boundary ----------

    @Test
    void match5_failsGracefullyAtEndOfBuffer() throws Exception {
        // Only "\"abcde\": (no value) — advance would hit end
        byte[] b = "\"abcde\":".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToName(p, b, "abcde");
        int name1 = com.alibaba.fastjson3.util.JDKUtils.getIntDirect(b, 4);
        // off = 1 + 7 = 8 = end. off > end is false (== not >), so passes. But then ws loop at off==end exits, offset=end.
        assertTrue(p.nextIfName4Match5(name1));
        assertEquals(b.length, getOffset(p));
    }
}

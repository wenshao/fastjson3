package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the fastjson2-compatible value-match intrinsics on {@link JSONParser.UTF8}.
 *
 * <p>These methods have a pre-check contract: {@code this.offset} must be positioned
 * at the first content character of a JSON string value (one past the opening quote),
 * and the caller's generator has already validated the leading 4/8-byte prefix via a
 * switch on {@link JSONParser.UTF8#getRawInt()} / {@link JSONParser.UTF8#getRawLong()}.
 * Tests here simulate that contract by manually positioning offset after the opening
 * quote of the value being matched.</p>
 */
class ValueMatchIntrinsicsTest {
    private static JSONParser.UTF8 parser(byte[] bytes) {
        return (JSONParser.UTF8) JSONParser.of(bytes);
    }

    /** Position {@code this.offset} at the first content char of the given value string. */
    private static void seekToValue(JSONParser.UTF8 p, byte[] bytes, String value) throws Exception {
        byte[] target = ("\"" + value + "\"").getBytes(StandardCharsets.UTF_8);
        int idx = indexOf(bytes, target);
        assertTrue(idx >= 0, "value '" + value + "' not found in: " + new String(bytes));
        setOffset(p, idx + 1);
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

    private static void setOffset(JSONParser p, int off) throws Exception {
        Field f = JSONParser.class.getDeclaredField("offset");
        f.setAccessible(true);
        f.setInt(p, off);
    }

    // ---------- Match2 ----------

    @Test
    void match2_commaAdvancesPastCommaAndWs() throws Exception {
        byte[] b = "\"ab\",\"next\"".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "ab");
        assertTrue(p.nextIfValue4Match2());
        assertEquals((byte) '"', b[getOffset(p)]);
    }

    @Test
    void match2_commaSkipsTrailingWhitespace() throws Exception {
        byte[] b = "\"ab\",   \"next\"".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "ab");
        assertTrue(p.nextIfValue4Match2());
        assertEquals((byte) '"', b[getOffset(p)]);
    }

    @Test
    void match2_closeBraceLeavesOffsetAtTerminator() throws Exception {
        byte[] b = "\"ab\"}".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "ab");
        assertTrue(p.nextIfValue4Match2());
        assertEquals((byte) '}', b[getOffset(p)]);
    }

    @Test
    void match2_closeBracketLeavesOffsetAtTerminator() throws Exception {
        byte[] b = "\"ab\"]".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "ab");
        assertTrue(p.nextIfValue4Match2());
        assertEquals((byte) ']', b[getOffset(p)]);
    }

    @Test
    void match2_failsOnMissingSeparator() throws Exception {
        byte[] b = "\"ab\" x".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "ab");
        int preOff = getOffset(p);
        assertFalse(p.nextIfValue4Match2());
        assertEquals(preOff, getOffset(p), "offset must not advance on failure");
    }

    @Test
    void match2_failsOnBufferTooShort() throws Exception {
        // content bytes present but no separator byte available
        byte[] b = "\"ab\"".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        setOffset(p, 1);
        assertFalse(p.nextIfValue4Match2());
    }

    // ---------- Match3 ----------

    @Test
    void match3_success() throws Exception {
        byte[] b = "\"abc\",42".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "abc");
        assertTrue(p.nextIfValue4Match3());
        assertEquals((byte) '4', b[getOffset(p)]);
    }

    @Test
    void match3_failsOnMissingClosingQuote() throws Exception {
        // Corrupt the closing quote: "abcX,42" — first 4 bytes are "abc but no closing
        byte[] b = "\"abcX,42".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        setOffset(p, 1);
        int preOff = getOffset(p);
        assertFalse(p.nextIfValue4Match3());
        assertEquals(preOff, getOffset(p));
    }

    @Test
    void match3_closeBrace() throws Exception {
        byte[] b = "\"xyz\"}".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "xyz");
        assertTrue(p.nextIfValue4Match3());
        assertEquals((byte) '}', b[getOffset(p)]);
    }

    // ---------- Match4 ----------

    @Test
    void match4_success() throws Exception {
        byte[] b = "\"male\",true".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "male");
        assertTrue(p.nextIfValue4Match4((byte) 'e'));
        assertEquals((byte) 't', b[getOffset(p)]);
    }

    @Test
    void match4_failsOnWrongC4() throws Exception {
        byte[] b = "\"male\",true".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "male");
        assertFalse(p.nextIfValue4Match4((byte) 'X'));
    }

    // ---------- Match5 ----------

    @Test
    void match5_success() throws Exception {
        byte[] b = "\"apple\"]".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "apple");
        assertTrue(p.nextIfValue4Match5((byte) 'l', (byte) 'e'));
        assertEquals((byte) ']', b[getOffset(p)]);
    }

    @Test
    void match5_failsOnWrongC5() throws Exception {
        byte[] b = "\"apple\"]".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "apple");
        assertFalse(p.nextIfValue4Match5((byte) 'l', (byte) 'X'));
    }

    // ---------- Match6 ----------

    @Test
    void match6_success() throws Exception {
        byte[] b = "\"orange\",null".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "orange");
        // name1 covers content[3..5] ('n','g','e') + closing '"'
        byte[] tail4 = new byte[]{'n', 'g', 'e', '"'};
        int name1 = com.alibaba.fastjson3.util.JDKUtils.getIntDirect(tail4, 0);
        assertTrue(p.nextIfValue4Match6(name1));
        assertEquals((byte) 'n', b[getOffset(p)]);
    }

    @Test
    void match6_failsOnWrongName1() throws Exception {
        byte[] b = "\"orange\",null".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "orange");
        assertFalse(p.nextIfValue4Match6(0));
    }

    // ---------- Match7 ----------

    @Test
    void match7_success() throws Exception {
        byte[] b = "\"BARBARA\"]".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "BARBARA");
        // name1 covers content[3..6] ('B','A','R','A')
        byte[] tail4 = new byte[]{'B', 'A', 'R', 'A'};
        int name1 = com.alibaba.fastjson3.util.JDKUtils.getIntDirect(tail4, 0);
        assertTrue(p.nextIfValue4Match7(name1));
        assertEquals((byte) ']', b[getOffset(p)]);
    }

    // ---------- Match8 ----------

    @Test
    void match8_success() throws Exception {
        byte[] b = "\"LARGE123\",42".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "LARGE123");
        byte[] tail4 = new byte[]{'G', 'E', '1', '2'};
        int name1 = com.alibaba.fastjson3.util.JDKUtils.getIntDirect(tail4, 0);
        assertTrue(p.nextIfValue4Match8(name1, (byte) '3'));
        assertEquals((byte) '4', b[getOffset(p)]);
    }

    // ---------- Match9 ----------

    @Test
    void match9_success() throws Exception {
        byte[] b = "\"ALPHABETA\",1".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "ALPHABETA");
        byte[] tail4 = new byte[]{'H', 'A', 'B', 'E'};
        int name1 = com.alibaba.fastjson3.util.JDKUtils.getIntDirect(tail4, 0);
        assertTrue(p.nextIfValue4Match9(name1, (byte) 'T', (byte) 'A'));
        assertEquals((byte) '1', b[getOffset(p)]);
    }

    // ---------- Match10 ----------

    @Test
    void match10_success() throws Exception {
        byte[] b = "\"HORIZONTAL\"}".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "HORIZONTAL");
        // name1 long covers content[3..9] ('I','Z','O','N','T','A','L') + closing '"'
        byte[] tail8 = new byte[]{'I', 'Z', 'O', 'N', 'T', 'A', 'L', '"'};
        long name1 = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(tail8, 0);
        assertTrue(p.nextIfValue4Match10(name1));
        assertEquals((byte) '}', b[getOffset(p)]);
    }

    @Test
    void match10_failsOnWrongLong() throws Exception {
        byte[] b = "\"HORIZONTAL\"}".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "HORIZONTAL");
        assertFalse(p.nextIfValue4Match10(0L));
    }

    // ---------- Match11 ----------

    @Test
    void match11_success() throws Exception {
        byte[] b = "\"DIAGNOSTIC1\",2".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        seekToValue(p, b, "DIAGNOSTIC1");
        // name1 long covers content[3..10] ('G','N','O','S','T','I','C','1')
        byte[] tail8 = new byte[]{'G', 'N', 'O', 'S', 'T', 'I', 'C', '1'};
        long name1 = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(tail8, 0);
        assertTrue(p.nextIfValue4Match11(name1));
        assertEquals((byte) '2', b[getOffset(p)]);
    }

    @Test
    void match11_failsOnMissingClosingQuote() throws Exception {
        byte[] b = "\"DIAGNOSTIC1X,2".getBytes(StandardCharsets.UTF_8);
        JSONParser.UTF8 p = parser(b);
        setOffset(p, 1);
        byte[] tail8 = new byte[]{'G', 'N', 'O', 'S', 'T', 'I', 'C', '1'};
        long name1 = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(tail8, 0);
        assertFalse(p.nextIfValue4Match11(name1));
    }
}

package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every parse throwsite now reports {@code offset N (line L, col C)} so users
 * don't have to count bytes by hand to locate a malformed token, especially
 * in multi-line JSON documents.
 */
public class ParseErrorLocationTest {

    @Test
    public void singleLineErrorIncludesLineCol() {
        JSONException ex = assertThrows(JSONException.class, () -> JSON.parse("{bad}"));
        assertTrue(ex.getMessage().contains("line 1, col 2"),
                "single-line error should point at col 2; got: " + ex.getMessage());
    }

    @Test
    public void multiLineErrorComputesRealLine() {
        // '"' expected; bare identifier 'bad' starts at line 2 col 10.
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("{\n  \"name\":bad}"));
        assertTrue(ex.getMessage().contains("line 2"),
                "should report line 2; got: " + ex.getMessage());
    }

    @Test
    public void crlfLineBreakCountedOnce() {
        // CRLF is one logical line break — don't double-count.
        // Error on line 3 col 1 (well, end of '}' expected).
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("{\r\n  \"a\":1,\r\n  \"b\":\r\n}"));
        String msg = ex.getMessage();
        assertTrue(msg.contains("line 4"),
                "CRLF should advance line same as LF; got: " + msg);
    }

    @Test
    public void offsetStillPresent() {
        // Keep the legacy offset too — existing tooling / grep patterns
        // that look for "offset N" must keep working.
        JSONException ex = assertThrows(JSONException.class, () -> JSON.parse("{bad}"));
        assertTrue(ex.getMessage().matches(".*offset \\d+.*"),
                "offset should still be present; got: " + ex.getMessage());
    }

    @Test
    public void arrayErrorIncludesLineCol() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("[1,2,\n3,\nbad]"));
        assertTrue(ex.getMessage().contains("line 3"),
                "should land on line 3; got: " + ex.getMessage());
    }
}

package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JSON Pointer (RFC 6901).
 */
class JSONPointerTest {
    private static final String RFC_EXAMPLE = """
            {
                "foo": ["bar", "baz"],
                "": 0,
                "a/b": 1,
                "c%d": 2,
                "e^f": 3,
                "g|h": 4,
                "i\\\\j": 5,
                "k\\"l": 6,
                " ": 7,
                "m~n": 8
            }
            """;

    @Test
    void testRootPointer() {
        JSONObject obj = JSON.parseObject("{\"a\":1}");
        Object result = JSONPointer.of("").eval(obj);
        assertSame(obj, result);
    }

    @Test
    void testSimpleProperty() {
        JSONObject obj = JSON.parseObject("{\"foo\":42}");
        assertEquals(42, JSONPointer.of("/foo").eval(obj));
    }

    @Test
    void testNestedProperty() {
        JSONObject obj = JSON.parseObject("{\"a\":{\"b\":{\"c\":\"deep\"}}}");
        assertEquals("deep", JSONPointer.of("/a/b/c").eval(obj));
    }

    @Test
    void testArrayIndex() {
        JSONObject obj = JSON.parseObject("{\"foo\":[\"bar\",\"baz\"]}");
        assertEquals("bar", JSONPointer.of("/foo/0").eval(obj));
        assertEquals("baz", JSONPointer.of("/foo/1").eval(obj));
    }

    // RFC 6901 Section 5 — Example
    @Test
    void testRFCSection5() {
        JSONObject obj = JSON.parseObject(RFC_EXAMPLE);
        // ""     -> whole document
        assertSame(obj, JSONPointer.of("").eval(obj));
        // "/foo" -> ["bar","baz"]
        assertTrue(JSONPointer.of("/foo").eval(obj) instanceof JSONArray);
        // "/foo/0" -> "bar"
        assertEquals("bar", JSONPointer.of("/foo/0").eval(obj));
        // "/"    -> 0
        assertEquals(0, JSONPointer.of("/").eval(obj));
        // "/a~1b" -> 1  (~1 = /)
        assertEquals(1, JSONPointer.of("/a~1b").eval(obj));
        // "/c%d" -> 2
        assertEquals(2, JSONPointer.of("/c%d").eval(obj));
        // "/e^f" -> 3
        assertEquals(3, JSONPointer.of("/e^f").eval(obj));
        // "/g|h" -> 4
        assertEquals(4, JSONPointer.of("/g|h").eval(obj));
        // "/i\\j" -> 5
        assertEquals(5, JSONPointer.of("/i\\j").eval(obj));
        // "/k\"l" -> 6
        assertEquals(6, JSONPointer.of("/k\"l").eval(obj));
        // "/ " -> 7
        assertEquals(7, JSONPointer.of("/ ").eval(obj));
        // "/m~0n" -> 8  (~0 = ~)
        assertEquals(8, JSONPointer.of("/m~0n").eval(obj));
    }

    @Test
    void testEscapeUnescape() {
        assertEquals("a/b", JSONPointer.unescape("a~1b"));
        assertEquals("m~n", JSONPointer.unescape("m~0n"));
        assertEquals("a~1b", JSONPointer.escape("a/b"));
        assertEquals("m~0n", JSONPointer.escape("m~n"));
    }

    @Test
    void testSet() {
        JSONObject obj = JSON.parseObject("{\"a\":{\"b\":1}}");
        JSONPointer.of("/a/b").set(obj, 2);
        assertEquals(2, JSONPointer.of("/a/b").eval(obj));
    }

    @Test
    void testSetArrayAppend() {
        JSONObject obj = JSON.parseObject("{\"arr\":[1,2]}");
        JSONPointer.of("/arr/-").set(obj, 3);
        assertEquals(3, ((JSONArray) JSONPointer.of("/arr").eval(obj)).size());
    }

    @Test
    void testRemove() {
        JSONObject obj = JSON.parseObject("{\"a\":1,\"b\":2}");
        JSONPointer.of("/a").remove(obj);
        assertFalse(obj.containsKey("a"));
        assertTrue(obj.containsKey("b"));
    }

    @Test
    void testExists() {
        JSONObject obj = JSON.parseObject("{\"a\":{\"b\":1}}");
        assertTrue(JSONPointer.of("/a").exists(obj));
        assertTrue(JSONPointer.of("/a/b").exists(obj));
        assertFalse(JSONPointer.of("/c").exists(obj));
        assertFalse(JSONPointer.of("/a/c").exists(obj));
    }

    @Test
    void testInvalidPointer() {
        assertThrows(JSONException.class, () -> JSONPointer.of("invalid"));
    }

    @Test
    void testLeadingZeroIndex() {
        JSONObject obj = JSON.parseObject("{\"arr\":[1,2,3]}");
        // eval returns null for invalid paths (including leading-zero indices)
        assertNull(JSONPointer.of("/arr/01").eval(obj));
        // parseIndex itself still throws for leading zeros
        assertThrows(JSONException.class, () -> JSONPointer.parseIndex("01", 3));
    }

    @Test
    void testTypedEval() {
        JSONObject obj = JSON.parseObject("{\"name\":\"Alice\",\"age\":30}");
        assertEquals("Alice", JSONPointer.of("/name").eval(obj, String.class));
        assertEquals(30, JSONPointer.of("/age").eval(obj, Integer.class));
    }

    @Test
    void testEqualsAndHashCode() {
        JSONPointer p1 = JSONPointer.of("/a/b");
        JSONPointer p2 = JSONPointer.of("/a/b");
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void testNonExistentPath() {
        JSONObject obj = JSON.parseObject("{\"a\":1}");
        assertNull(JSONPointer.of("/b").eval(obj));
    }
}

package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JSON Patch (RFC 6902).
 */
class JSONPatchTest {

    // ==================== add ====================

    @Test
    void testAddObjectMember() {
        String result = JSONPatch.apply(
                "{\"foo\":\"bar\"}",
                "[{\"op\":\"add\",\"path\":\"/baz\",\"value\":\"qux\"}]");
        JSONObject obj = JSON.parseObject(result);
        assertEquals("bar", obj.getString("foo"));
        assertEquals("qux", obj.getString("baz"));
    }

    @Test
    void testAddArrayElement() {
        String result = JSONPatch.apply(
                "{\"foo\":[\"bar\",\"baz\"]}",
                "[{\"op\":\"add\",\"path\":\"/foo/1\",\"value\":\"qux\"}]");
        JSONArray foo = JSON.parseObject(result).getJSONArray("foo");
        assertEquals(3, foo.size());
        assertEquals("bar", foo.getString(0));
        assertEquals("qux", foo.getString(1));
        assertEquals("baz", foo.getString(2));
    }

    @Test
    void testAddArrayEnd() {
        String result = JSONPatch.apply(
                "{\"foo\":[1,2]}",
                "[{\"op\":\"add\",\"path\":\"/foo/-\",\"value\":3}]");
        JSONArray foo = JSON.parseObject(result).getJSONArray("foo");
        assertEquals(3, foo.size());
        assertEquals(3, foo.getIntValue(2));
    }

    @Test
    void testAddNestedMember() {
        String result = JSONPatch.apply(
                "{\"foo\":{\"bar\":1}}",
                "[{\"op\":\"add\",\"path\":\"/foo/baz\",\"value\":2}]");
        assertEquals(2, JSON.parseObject(result).getJSONObject("foo").getIntValue("baz"));
    }

    @Test
    void testAddReplaceRoot() {
        String result = JSONPatch.apply(
                "{\"foo\":1}",
                "[{\"op\":\"add\",\"path\":\"\",\"value\":{\"bar\":2}}]");
        JSONObject obj = JSON.parseObject(result);
        assertEquals(2, obj.getIntValue("bar"));
        assertFalse(obj.containsKey("foo"));
    }

    // ==================== remove ====================

    @Test
    void testRemoveObjectMember() {
        String result = JSONPatch.apply(
                "{\"foo\":\"bar\",\"baz\":\"qux\"}",
                "[{\"op\":\"remove\",\"path\":\"/baz\"}]");
        JSONObject obj = JSON.parseObject(result);
        assertEquals("bar", obj.getString("foo"));
        assertFalse(obj.containsKey("baz"));
    }

    @Test
    void testRemoveArrayElement() {
        String result = JSONPatch.apply(
                "{\"foo\":[\"bar\",\"qux\",\"baz\"]}",
                "[{\"op\":\"remove\",\"path\":\"/foo/1\"}]");
        JSONArray foo = JSON.parseObject(result).getJSONArray("foo");
        assertEquals(2, foo.size());
        assertEquals("bar", foo.getString(0));
        assertEquals("baz", foo.getString(1));
    }

    // ==================== replace ====================

    @Test
    void testReplaceValue() {
        String result = JSONPatch.apply(
                "{\"foo\":\"bar\",\"baz\":\"qux\"}",
                "[{\"op\":\"replace\",\"path\":\"/baz\",\"value\":\"boo\"}]");
        assertEquals("boo", JSON.parseObject(result).getString("baz"));
    }

    @Test
    void testReplaceArrayElement() {
        String result = JSONPatch.apply(
                "{\"foo\":[1,2,3]}",
                "[{\"op\":\"replace\",\"path\":\"/foo/1\",\"value\":99}]");
        assertEquals(99, JSON.parseObject(result).getJSONArray("foo").getIntValue(1));
    }

    @Test
    void testReplaceNonExistent() {
        assertThrows(JSONException.class, () -> JSONPatch.apply(
                "{\"foo\":1}",
                "[{\"op\":\"replace\",\"path\":\"/bar\",\"value\":2}]"));
    }

    // ==================== move ====================

    @Test
    void testMoveObjectMember() {
        String result = JSONPatch.apply(
                "{\"foo\":{\"bar\":\"baz\",\"waldo\":\"fred\"},\"qux\":{\"corge\":\"grault\"}}",
                "[{\"op\":\"move\",\"from\":\"/foo/waldo\",\"path\":\"/qux/thud\"}]");
        JSONObject obj = JSON.parseObject(result);
        assertFalse(obj.getJSONObject("foo").containsKey("waldo"));
        assertEquals("fred", obj.getJSONObject("qux").getString("thud"));
    }

    @Test
    void testMoveArrayElement() {
        String result = JSONPatch.apply(
                "{\"foo\":[\"all\",\"grass\",\"cows\",\"eat\"]}",
                "[{\"op\":\"move\",\"from\":\"/foo/1\",\"path\":\"/foo/3\"}]");
        JSONArray foo = JSON.parseObject(result).getJSONArray("foo");
        assertEquals("all", foo.getString(0));
        assertEquals("cows", foo.getString(1));
        assertEquals("eat", foo.getString(2));
        assertEquals("grass", foo.getString(3));
    }

    // ==================== copy ====================

    @Test
    void testCopyObjectMember() {
        String result = JSONPatch.apply(
                "{\"foo\":{\"bar\":\"baz\"}}",
                "[{\"op\":\"copy\",\"from\":\"/foo/bar\",\"path\":\"/foo/qux\"}]");
        JSONObject foo = JSON.parseObject(result).getJSONObject("foo");
        assertEquals("baz", foo.getString("bar"));
        assertEquals("baz", foo.getString("qux"));
    }

    @Test
    void testCopyIsDeep() {
        String result = JSONPatch.apply(
                "{\"a\":{\"b\":[1,2]}}",
                "[{\"op\":\"copy\",\"from\":\"/a\",\"path\":\"/c\"}]");
        JSONObject obj = JSON.parseObject(result);
        // Verify top-level objects are independent
        assertNotSame(obj.getJSONObject("a"), obj.getJSONObject("c"));
        // Verify nested structures are also independent (deep copy, not shallow)
        assertNotSame(obj.getJSONObject("a").getJSONArray("b"), obj.getJSONObject("c").getJSONArray("b"));
        // Modify the nested array in original; copy should be unaffected
        obj.getJSONObject("a").getJSONArray("b").add(3);
        assertEquals(3, obj.getJSONObject("a").getJSONArray("b").size());
        assertEquals(2, obj.getJSONObject("c").getJSONArray("b").size());
    }

    // ==================== test ====================

    @Test
    void testTestSuccess() {
        assertDoesNotThrow(() -> JSONPatch.apply(
                "{\"foo\":\"bar\"}",
                "[{\"op\":\"test\",\"path\":\"/foo\",\"value\":\"bar\"}]"));
    }

    @Test
    void testTestFailure() {
        assertThrows(JSONException.class, () -> JSONPatch.apply(
                "{\"foo\":\"bar\"}",
                "[{\"op\":\"test\",\"path\":\"/foo\",\"value\":\"baz\"}]"));
    }

    @Test
    void testTestNull() {
        assertDoesNotThrow(() -> JSONPatch.apply(
                "{\"foo\":null}",
                "[{\"op\":\"test\",\"path\":\"/foo\",\"value\":null}]"));
    }

    @Test
    void testTestNestedObject() {
        assertDoesNotThrow(() -> JSONPatch.apply(
                "{\"foo\":{\"bar\":1}}",
                "[{\"op\":\"test\",\"path\":\"/foo\",\"value\":{\"bar\":1}}]"));
    }

    // ==================== Multiple operations ====================

    @Test
    void testMultipleOperations() {
        String target = "{\"a\":1,\"b\":2}";
        String patch = """
                [
                    {"op":"remove","path":"/b"},
                    {"op":"add","path":"/c","value":3},
                    {"op":"replace","path":"/a","value":10}
                ]
                """;
        JSONObject obj = JSON.parseObject(JSONPatch.apply(target, patch));
        assertEquals(10, obj.getIntValue("a"));
        assertFalse(obj.containsKey("b"));
        assertEquals(3, obj.getIntValue("c"));
    }

    // ==================== Error cases ====================

    @Test
    void testUnknownOp() {
        assertThrows(JSONException.class, () -> JSONPatch.apply(
                "{}", "[{\"op\":\"unknown\",\"path\":\"/a\"}]"));
    }

    @Test
    void testMissingOp() {
        assertThrows(JSONException.class, () -> JSONPatch.apply(
                "{}", "[{\"path\":\"/a\"}]"));
    }

    @Test
    void testMissingPath() {
        assertThrows(JSONException.class, () -> JSONPatch.apply(
                "{}", "[{\"op\":\"add\",\"value\":1}]"));
    }

    @Test
    void testMissingValue() {
        assertThrows(JSONException.class, () -> JSONPatch.apply(
                "{}", "[{\"op\":\"add\",\"path\":\"/a\"}]"));
    }

    @Test
    void testMissingFrom() {
        assertThrows(JSONException.class, () -> JSONPatch.apply(
                "{\"a\":1}", "[{\"op\":\"move\",\"path\":\"/b\"}]"));
    }

    // ==================== RFC 6902 Appendix A examples ====================

    @Test
    void testRFCAppendixA1_AddObjectMember() {
        String result = JSONPatch.apply(
                "{\"foo\":\"bar\"}",
                "[{\"op\":\"add\",\"path\":\"/baz\",\"value\":\"qux\"}]");
        JSONObject obj = JSON.parseObject(result);
        assertEquals("bar", obj.getString("foo"));
        assertEquals("qux", obj.getString("baz"));
    }

    @Test
    void testRFCAppendixA2_AddArrayElement() {
        String result = JSONPatch.apply(
                "{\"foo\":[\"bar\",\"baz\"]}",
                "[{\"op\":\"add\",\"path\":\"/foo/1\",\"value\":\"qux\"}]");
        JSONArray foo = JSON.parseObject(result).getJSONArray("foo");
        assertEquals("bar", foo.get(0));
        assertEquals("qux", foo.get(1));
        assertEquals("baz", foo.get(2));
    }

    @Test
    void testRFCAppendixA3_RemoveObjectMember() {
        String result = JSONPatch.apply(
                "{\"baz\":\"qux\",\"foo\":\"bar\"}",
                "[{\"op\":\"remove\",\"path\":\"/baz\"}]");
        JSONObject obj = JSON.parseObject(result);
        assertFalse(obj.containsKey("baz"));
        assertEquals("bar", obj.getString("foo"));
    }

    @Test
    void testRFCAppendixA5_ReplacingValue() {
        String result = JSONPatch.apply(
                "{\"baz\":\"qux\",\"foo\":\"bar\"}",
                "[{\"op\":\"replace\",\"path\":\"/baz\",\"value\":\"boo\"}]");
        JSONObject obj = JSON.parseObject(result);
        assertEquals("boo", obj.getString("baz"));
        assertEquals("bar", obj.getString("foo"));
    }

    @Test
    void testRFCAppendixA6_MovingValue() {
        String result = JSONPatch.apply(
                "{\"foo\":{\"bar\":\"baz\",\"waldo\":\"fred\"},\"qux\":{\"corge\":\"grault\"}}",
                "[{\"op\":\"move\",\"from\":\"/foo/waldo\",\"path\":\"/qux/thud\"}]");
        JSONObject obj = JSON.parseObject(result);
        assertEquals("fred", obj.getJSONObject("qux").getString("thud"));
    }

    @Test
    void testRFCAppendixA8_TestingValueSuccess() {
        assertDoesNotThrow(() -> JSONPatch.apply(
                "{\"baz\":\"qux\",\"foo\":[\"a\",2,\"c\"]}",
                "[{\"op\":\"test\",\"path\":\"/baz\",\"value\":\"qux\"},{\"op\":\"test\",\"path\":\"/foo/1\",\"value\":2}]"));
    }

    @Test
    void testRFCAppendixA9_TestingValueError() {
        assertThrows(JSONException.class, () -> JSONPatch.apply(
                "{\"baz\":\"qux\"}",
                "[{\"op\":\"test\",\"path\":\"/baz\",\"value\":\"bar\"}]"));
    }

    @Test
    void testRFCAppendixA10_AddNestedMember() {
        String result = JSONPatch.apply(
                "{\"foo\":\"bar\"}",
                "[{\"op\":\"add\",\"path\":\"/child\",\"value\":{\"grandchild\":{}}}]");
        JSONObject obj = JSON.parseObject(result);
        assertNotNull(obj.getJSONObject("child").getJSONObject("grandchild"));
    }

    @Test
    void testRFCAppendixA14_TildeEscaping() {
        String result = JSONPatch.apply(
                "{\"/\":9,\"~1\":10}",
                "[{\"op\":\"test\",\"path\":\"/~01\",\"value\":10}]");
        // ~01 → ~1 (key is "~1"), test op verifies value equals 10
        JSONObject obj = JSON.parseObject(result);
        assertEquals(10, obj.getIntValue("~1"));
        assertEquals(9, obj.getIntValue("/"));
    }

    @Test
    void testRFCAppendixA15_ComparingStringsAndNumbers() {
        assertThrows(JSONException.class, () -> JSONPatch.apply(
                "{\"/\":9,\"~1\":10}",
                "[{\"op\":\"test\",\"path\":\"/~01\",\"value\":\"10\"}]"));
    }
}

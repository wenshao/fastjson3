package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JSON Merge Patch (RFC 7396).
 *
 * @see <a href="https://tools.ietf.org/html/rfc7396">RFC 7396</a>
 */
class MergePatchTest {

    // RFC 7396 Section 3 — Example test cases

    @Test
    void testAddField() {
        String result = JSON.mergePatch("{\"a\":1}", "{\"b\":2}");
        JSONObject obj = JSON.parseObject(result);
        assertEquals(1, obj.getIntValue("a"));
        assertEquals(2, obj.getIntValue("b"));
    }

    @Test
    void testRemoveField() {
        String result = JSON.mergePatch("{\"a\":1,\"b\":2}", "{\"a\":null}");
        JSONObject obj = JSON.parseObject(result);
        assertFalse(obj.containsKey("a"));
        assertEquals(2, obj.getIntValue("b"));
    }

    @Test
    void testReplaceField() {
        String result = JSON.mergePatch("{\"a\":1}", "{\"a\":2}");
        JSONObject obj = JSON.parseObject(result);
        assertEquals(2, obj.getIntValue("a"));
    }

    @Test
    void testNestedMerge() {
        String target = "{\"a\":{\"b\":1,\"c\":2}}";
        String patch = "{\"a\":{\"b\":null,\"d\":3}}";
        String result = JSON.mergePatch(target, patch);
        JSONObject obj = JSON.parseObject(result);
        JSONObject a = obj.getJSONObject("a");
        assertFalse(a.containsKey("b"));
        assertEquals(2, a.getIntValue("c"));
        assertEquals(3, a.getIntValue("d"));
    }

    @Test
    void testReplaceObjectWithScalar() {
        String result = JSON.mergePatch("{\"a\":{\"b\":1}}", "{\"a\":\"string\"}");
        JSONObject obj = JSON.parseObject(result);
        assertEquals("string", obj.getString("a"));
    }

    @Test
    void testReplaceScalarWithObject() {
        String result = JSON.mergePatch("{\"a\":\"string\"}", "{\"a\":{\"b\":1}}");
        JSONObject obj = JSON.parseObject(result);
        assertEquals(1, obj.getJSONObject("a").getIntValue("b"));
    }

    @Test
    void testReplaceArrayWithScalar() {
        String result = JSON.mergePatch("{\"a\":[1,2]}", "{\"a\":3}");
        JSONObject obj = JSON.parseObject(result);
        assertEquals(3, obj.getIntValue("a"));
    }

    @Test
    void testPatchIsScalar() {
        // When patch is not an object, it replaces the whole target
        String result = JSON.mergePatch("{\"a\":1}", "42");
        assertEquals("42", result);
    }

    @Test
    void testPatchIsString() {
        String result = JSON.mergePatch("{\"a\":1}", "\"hello\"");
        assertEquals("\"hello\"", result);
    }

    @Test
    void testNullTarget() {
        String result = JSON.mergePatch(null, "{\"a\":1}");
        JSONObject obj = JSON.parseObject(result);
        assertEquals(1, obj.getIntValue("a"));
    }

    @Test
    void testEmptyPatch() {
        String result = JSON.mergePatch("{\"a\":1}", "{}");
        JSONObject obj = JSON.parseObject(result);
        assertEquals(1, obj.getIntValue("a"));
    }

    @Test
    void testEmptyTarget() {
        String result = JSON.mergePatch("{}", "{\"a\":1}");
        JSONObject obj = JSON.parseObject(result);
        assertEquals(1, obj.getIntValue("a"));
    }

    // RFC 7396 Appendix A — Full test vector
    @Test
    void testRFCAppendixA() {
        String target = """
            {"title":"Goodbye!","author":{"givenName":"John","familyName":"Doe"},"tags":["example","sample"],"content":"This will be unchanged"}
            """;
        String patch = """
            {"title":"Hello!","phoneNumber":"+01-123-456-7890","author":{"familyName":null},"tags":["example"]}
            """;
        String result = JSON.mergePatch(target, patch);
        JSONObject obj = JSON.parseObject(result);

        assertEquals("Hello!", obj.getString("title"));
        assertEquals("+01-123-456-7890", obj.getString("phoneNumber"));
        assertEquals("This will be unchanged", obj.getString("content"));

        JSONObject author = obj.getJSONObject("author");
        assertEquals("John", author.getString("givenName"));
        assertFalse(author.containsKey("familyName"));

        JSONArray tags = obj.getJSONArray("tags");
        assertEquals(1, tags.size());
        assertEquals("example", tags.getString(0));
    }

    // Object-based API
    @Test
    void testObjectMergePatch() {
        JSONObject target = new JSONObject();
        target.put("a", 1);
        target.put("b", 2);

        JSONObject patch = new JSONObject();
        patch.put("b", null);
        patch.put("c", 3);

        Object result = JSON.mergePatch(target, patch);
        assertTrue(result instanceof JSONObject);
        JSONObject obj = (JSONObject) result;
        assertEquals(1, obj.getIntValue("a"));
        assertFalse(obj.containsKey("b"));
        assertEquals(3, obj.getIntValue("c"));
    }
}

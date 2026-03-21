package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for deep nesting handling and structural edge cases.
 * Inspired by fastjson2 read/DeepTest and jackson DeepJsonParsingTest.
 */
class DeepNestingTest {

    // ==================== Deep array nesting ====================

    @Test
    void deepNestedArray_100levels() {
        int depth = 100;
        String json = buildNestedArray(depth);
        Object result = JSON.parse(json);
        assertNotNull(result);
        // Verify structure: drill down to innermost
        Object current = result;
        for (int i = 0; i < depth - 1; i++) {
            assertInstanceOf(JSONArray.class, current,
                    "Level " + i + " should be JSONArray");
            JSONArray arr = (JSONArray) current;
            assertEquals(1, arr.size());
            current = arr.get(0);
        }
    }

    @Test
    void deepNestedArray_500levels() {
        int depth = 500;
        String json = buildNestedArray(depth);
        Object result = JSON.parse(json);
        assertNotNull(result);
    }

    @Test
    void deepNestedArray_roundTrip() {
        int depth = 100;
        String json = buildNestedArray(depth);
        Object result = JSON.parse(json);
        String serialized = JSON.toJSONString(result);
        // Parse again and verify equality
        Object result2 = JSON.parse(serialized);
        assertEquals(JSON.toJSONString(result), JSON.toJSONString(result2));
    }

    // ==================== Deep object nesting ====================

    @Test
    void deepNestedObject_100levels() {
        int depth = 100;
        String json = buildNestedObject(depth);
        Object result = JSON.parse(json);
        assertNotNull(result);
        // Drill down
        Object current = result;
        for (int i = 0; i < depth - 1; i++) {
            assertInstanceOf(JSONObject.class, current,
                    "Level " + i + " should be JSONObject");
            current = ((JSONObject) current).get("val");
        }
    }

    @Test
    void deepNestedObject_500levels() {
        int depth = 500;
        String json = buildNestedObject(depth);
        Object result = JSON.parse(json);
        assertNotNull(result);
    }

    @Test
    void deepNestedObject_roundTrip() {
        int depth = 100;
        String json = buildNestedObject(depth);
        Object result = JSON.parse(json);
        String serialized = JSON.toJSONString(result);
        Object result2 = JSON.parse(serialized);
        assertEquals(JSON.toJSONString(result), JSON.toJSONString(result2));
    }

    // ==================== Deep nesting from bytes ====================

    @Test
    void deepNestedArray_fromBytes() {
        int depth = 100;
        String json = buildNestedArray(depth);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        JSONArray result = JSON.parseArray(new String(bytes, StandardCharsets.UTF_8));
        assertNotNull(result);
    }

    @Test
    void deepNestedObject_fromBytes() {
        int depth = 100;
        String json = buildNestedObject(depth);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        JSONObject result = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8));
        assertNotNull(result);
    }

    // ==================== Mixed nesting ====================

    @Test
    void mixedNesting_objectInArray() {
        int depth = 50;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("[{\"v\":");
        }
        sb.append("42");
        for (int i = 0; i < depth; i++) {
            sb.append("}]");
        }
        Object result = JSON.parse(sb.toString());
        assertNotNull(result);
    }

    @Test
    void mixedNesting_arrayInObject() {
        int depth = 50;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("{\"v\":[");
        }
        sb.append("42");
        for (int i = 0; i < depth; i++) {
            sb.append("]}");
        }
        Object result = JSON.parse(sb.toString());
        assertNotNull(result);
    }

    // ==================== Large flat structures ====================

    @Test
    void largeArray_10000elements() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 10000; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        JSONArray arr = JSON.parseArray(sb.toString());
        assertEquals(10000, arr.size());
        assertEquals(0, arr.getIntValue(0));
        assertEquals(9999, arr.getIntValue(9999));
    }

    @Test
    void largeObject_1000fields() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"field_").append(i).append("\":").append(i);
        }
        sb.append("}");
        JSONObject obj = JSON.parseObject(sb.toString());
        assertEquals(1000, obj.size());
        assertEquals(0, obj.getIntValue("field_0"));
        assertEquals(999, obj.getIntValue("field_999"));
    }

    // ==================== Duplicate keys ====================

    @Test
    void duplicateKeys_lastWins() {
        JSONObject obj = JSON.parseObject("{\"a\":1,\"a\":2}");
        assertEquals(2, obj.getIntValue("a"));
    }

    @Test
    void duplicateKeys_asArray() {
        Object result = JSON.parse("{\"a\":1,\"a\":2}", ReadFeature.DuplicateKeyValueAsArray);
        assertInstanceOf(JSONObject.class, result);
        JSONObject obj = (JSONObject) result;
        Object val = obj.get("a");
        assertInstanceOf(JSONArray.class, val,
                "Duplicate keys should merge into array, got: " + val.getClass().getSimpleName());
        JSONArray arr = (JSONArray) val;
        assertEquals(2, arr.size());
    }

    // ==================== AllowComments ====================

    @Test
    void allowComments_lineComment() {
        String json = """
                {
                    // this is a comment
                    "name": "test"
                }
                """;
        Object result = JSON.parse(json, ReadFeature.AllowComments);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("test", ((JSONObject) result).getString("name"));
    }

    @Test
    void allowComments_blockComment() {
        String json = """
                {
                    /* block comment */
                    "name": "test"
                }
                """;
        Object result = JSON.parse(json, ReadFeature.AllowComments);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("test", ((JSONObject) result).getString("name"));
    }

    @Test
    void allowComments_inArray() {
        String json = """
                [
                    // comment before element
                    1,
                    /* inline */ 2
                ]
                """;
        Object result = JSON.parse(json, ReadFeature.AllowComments);
        assertInstanceOf(JSONArray.class, result);
        assertEquals(2, ((JSONArray) result).size());
    }

    @Test
    void allowComments_withCJK() {
        String json = """
                {
                    // 这是中文注释
                    "name": "test"
                }
                """;
        Object result = JSON.parse(json, ReadFeature.AllowComments);
        assertEquals("test", ((JSONObject) result).getString("name"));
    }

    // ==================== Helpers ====================

    private String buildNestedArray(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append('[');
        }
        sb.append(']');
        for (int i = 1; i < depth; i++) {
            sb.append(']');
        }
        return sb.toString();
    }

    private String buildNestedObject(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("{\"val\":");
        }
        sb.append("{}");
        for (int i = 0; i < depth; i++) {
            sb.append('}');
        }
        return sb.toString();
    }
}

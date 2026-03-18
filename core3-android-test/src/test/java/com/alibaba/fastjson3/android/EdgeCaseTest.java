package com.alibaba.fastjson3.android;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONObject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for edge cases and error handling.
 */
public class EdgeCaseTest {

    @Test
    public void testEmptyObject() {
        JSONObject obj = JSON.parseObject("{}");
        assertEquals(0, obj.size());
    }

    @Test
    public void testEmptyArray() {
        JSONArray arr = JSON.parseArray("[]");
        assertEquals(0, arr.size());
    }

    @Test
    public void testWhitespace() {
        JSONObject obj = JSON.parseObject("   {   \"a\"   :   1   }   ");
        assertEquals(1, obj.size());
        assertEquals(1, obj.getIntValue("a"));
    }

    @Test
    public void testNullLiteral() {
        JSONObject obj = JSON.parseObject("{\"value\":null}");
        assertTrue(obj.containsKey("value"));
        assertNull(obj.get("value"));
    }

    @Test
    public void testBooleanValues() {
        JSONObject obj = JSON.parseObject("{\"t\":true,\"f\":false}");
        assertEquals(true, obj.getBooleanValue("t"));
        assertEquals(false, obj.getBooleanValue("f"));
    }

    @Test
    public void testNumericString() {
        JSONObject obj = JSON.parseObject("{\"num\":\"123\"}");
        assertEquals("123", obj.getString("num"));
        assertEquals(123, obj.getIntValue("num"));
    }

    @Test
    public void testLargeNumber() {
        String json = "{\"value\":9007199254740991}";  // Number.MAX_SAFE_INTEGER
        JSONObject obj = JSON.parseObject(json);
        assertEquals(9007199254740991L, obj.getLongValue("value"));
    }

    @Test
    public void testNegativeNumber() {
        JSONObject obj = JSON.parseObject("{\"value\":-42}");
        assertEquals(-42, obj.getIntValue("value"));
    }

    @Test
    public void testFloatNumber() {
        JSONObject obj = JSON.parseObject("{\"value\":3.14}");
        assertEquals(3.14, obj.getDoubleValue("value"), 0.001);
    }

    @Test
    public void testScientificNotation() {
        JSONObject obj = JSON.parseObject("{\"value\":1.23e10}");
        assertEquals(1.23e10, obj.getDoubleValue("value"), 1e6);
    }

    @Test
    public void testEscapedCharacters() {
        String json = "{\"text\":\"Hello\\nWorld\\t!\"}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals("Hello\nWorld\t!", obj.getString("text"));
    }

    @Test
    public void testUnicodeEscape() {
        String json = "{\"text\":\"\\u4e2d\\u6587\"}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals("中文", obj.getString("text"));
    }

    @Test
    public void testDeeplyNested() {
        String json = "{\"a\":{\"b\":{\"c\":{\"d\":1}}}}";
        JSONObject obj = JSON.parseObject(json);
        JSONObject a = obj.getJSONObject("a");
        assertNotNull(a);
        JSONObject b = a.getJSONObject("b");
        assertNotNull(b);
        JSONObject c = b.getJSONObject("c");
        assertNotNull(c);
        assertEquals(1, c.getIntValue("d"));
    }

    @Test
    public void testLongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String json = "{\"text\":\"" + sb + "\"}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals(1000, obj.getString("text").length());
    }

    @Test
    public void testManyFields() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < 100; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"field").append(i).append("\":").append(i);
        }
        sb.append("}");

        JSONObject obj = JSON.parseObject(sb.toString());
        assertEquals(100, obj.size());
        assertEquals(0, obj.getIntValue("field0"));
        assertEquals(99, obj.getIntValue("field99"));
    }

    @Test
    public void testManyArrayElements() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 100; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");

        JSONArray arr = JSON.parseArray(sb.toString());
        assertEquals(100, arr.size());
        assertEquals(0, arr.getIntValue(0));
        assertEquals(99, arr.getIntValue(99));
    }

    @Test
    public void testSpecialCharactersInString() {
        String json = "{\"text\":\"Hello \\\"World\\\"!\"}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals("Hello \"World\"!", obj.getString("text"));
    }

    @Test
    public void testSlashInString() {
        String json = "{\"path\":\"C:\\\\Users\\\\test\"}";
        JSONObject obj = JSON.parseObject(json);
        assertEquals("C:\\Users\\test", obj.getString("path"));
    }

    @Test
    public void testInvalidJsonThrowsException() {
        assertThrows(JSONException.class, () -> {
            JSON.parseObject("{invalid}");
        });
    }

    @Test
    public void testTrailingCommaNotSupported() {
        // Standard JSON doesn't allow trailing commas
        assertThrows(Exception.class, () -> {
            JSON.parseObject("{\"a\":1,}");
        });
    }

    @Test
    public void testCommentNotSupported() {
        // Standard JSON doesn't allow comments
        assertThrows(Exception.class, () -> {
            JSON.parseObject("{\"a\":1 /* comment */}");
        });
    }
}

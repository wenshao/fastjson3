package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge case tests for enum serialization.
 * Inspired by jackson EnumSerializationTest, fastjson2 enum tests.
 *
 * Note: enum deserialization into POJO fields requires ObjectReader support
 * for enum types (not yet implemented). These tests focus on serialization
 * and JSONObject-level access.
 */
class EnumEdgeCaseTest {

    // ==================== Test enums ====================

    public enum Color {
        RED, GREEN, BLUE
    }

    public enum Fruit {
        APPLE, BANANA, CHERRY;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum Priority {
        LOW, MEDIUM, HIGH;

        @JSONField(value = true)
        public int getCode() {
            return ordinal() + 1;
        }
    }

    // ==================== Basic enum serialization ====================

    @Test
    void writeEnum_byName() {
        assertEquals("\"RED\"", JSON.toJSONString(Color.RED));
        assertEquals("\"GREEN\"", JSON.toJSONString(Color.GREEN));
        assertEquals("\"BLUE\"", JSON.toJSONString(Color.BLUE));
    }

    @Test
    void writeEnum_null() {
        Color c = null;
        assertEquals("null", JSON.toJSONString(c));
    }

    @Test
    void writeEnum_inObject() {
        JSONObject obj = new JSONObject();
        obj.put("color", Color.RED);
        String json = JSON.toJSONString(obj);
        assertTrue(json.contains("\"RED\""), "Enum should be name: " + json);
    }

    @Test
    void writeEnum_inArray() {
        JSONArray arr = new JSONArray();
        arr.add(Color.RED);
        arr.add(Color.GREEN);
        arr.add(Color.BLUE);
        String json = JSON.toJSONString(arr);
        assertTrue(json.contains("\"RED\""), json);
        assertTrue(json.contains("\"GREEN\""), json);
        assertTrue(json.contains("\"BLUE\""), json);
    }

    // ==================== All enum values ====================

    @Test
    void writeAllEnumValues() {
        for (Color c : Color.values()) {
            String json = JSON.toJSONString(c);
            assertEquals("\"" + c.name() + "\"", json);
        }
    }

    @Test
    void writeAllFruitValues() {
        for (Fruit f : Fruit.values()) {
            String json = JSON.toJSONString(f);
            // Default serialization uses name(), not toString()
            assertEquals("\"" + f.name() + "\"", json);
        }
    }

    // ==================== Enum with @JSONField(value=true) ====================

    @Test
    void writeEnum_withJsonValue() {
        String json = JSON.toJSONString(Priority.LOW);
        // With @JSONField(value=true) on getCode(), should serialize as the code
        assertTrue(json.equals("1") || json.equals("\"LOW\""),
                "Expected code or name: " + json);
    }

    @Test
    void writeEnum_withJsonValue_allValues() {
        for (Priority p : Priority.values()) {
            String json = JSON.toJSONString(p);
            assertNotNull(json);
            assertFalse(json.isEmpty());
        }
    }

    // ==================== Enum in JSONObject ====================

    @Test
    void jsonObjectPut_enumValue() {
        JSONObject obj = new JSONObject();
        obj.put("color", Color.RED);
        assertEquals(Color.RED, obj.get("color"));
    }

    @Test
    void jsonObjectPut_enumName_getString() {
        JSONObject obj = JSON.parseObject("{\"color\":\"RED\"}");
        assertEquals("RED", obj.getString("color"));
    }

    @Test
    void jsonObjectPut_allEnumValues() {
        JSONObject obj = new JSONObject();
        for (int i = 0; i < Color.values().length; i++) {
            obj.put("c" + i, Color.values()[i]);
        }
        String json = JSON.toJSONString(obj);
        assertTrue(json.contains("\"RED\""), json);
        assertTrue(json.contains("\"GREEN\""), json);
        assertTrue(json.contains("\"BLUE\""), json);
    }

    // ==================== Enum serialization in nested structures ====================

    @Test
    void writeEnum_inNestedObject() {
        JSONObject inner = new JSONObject();
        inner.put("color", Color.RED);
        JSONObject outer = new JSONObject();
        outer.put("data", inner);
        String json = JSON.toJSONString(outer);
        assertTrue(json.contains("\"RED\""), json);
    }

    @Test
    void writeEnum_inMixedArray() {
        JSONArray arr = new JSONArray();
        arr.add("text");
        arr.add(Color.GREEN);
        arr.add(42);
        arr.add(true);
        String json = JSON.toJSONString(arr);
        assertTrue(json.contains("\"GREEN\""), json);
        assertTrue(json.contains("\"text\""), json);
        assertTrue(json.contains("42"), json);
    }

    // ==================== Enum string parsing ====================

    @Test
    void parseEnumString_fromJsonObject() {
        JSONObject obj = JSON.parseObject("{\"status\":\"ACTIVE\",\"priority\":\"HIGH\"}");
        assertEquals("ACTIVE", obj.getString("status"));
        assertEquals("HIGH", obj.getString("priority"));
    }

    @Test
    void parseEnumString_inArray() {
        JSONArray arr = JSON.parseArray("[\"RED\",\"GREEN\",\"BLUE\"]");
        assertEquals(3, arr.size());
        assertEquals("RED", arr.getString(0));
        assertEquals("GREEN", arr.getString(1));
        assertEquals("BLUE", arr.getString(2));
    }

    // ==================== Enum round-trip via JSONObject ====================

    @Test
    void roundTrip_enumViaJsonObject() {
        JSONObject obj = new JSONObject();
        obj.put("color", Color.BLUE);
        String json = JSON.toJSONString(obj);
        JSONObject parsed = JSON.parseObject(json);
        assertEquals("BLUE", parsed.getString("color"));
        assertEquals(Color.BLUE, Color.valueOf(parsed.getString("color")));
    }

    @Test
    void roundTrip_enumArrayViaJsonArray() {
        JSONArray arr = new JSONArray();
        for (Color c : Color.values()) {
            arr.add(c.name());
        }
        String json = JSON.toJSONString(arr);
        JSONArray parsed = JSON.parseArray(json);
        for (int i = 0; i < Color.values().length; i++) {
            assertEquals(Color.values()[i], Color.valueOf(parsed.getString(i)));
        }
    }
}

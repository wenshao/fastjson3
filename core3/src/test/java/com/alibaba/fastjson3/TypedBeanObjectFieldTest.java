package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regressions for typed Bean / record fields declared as
 * {@code Object}. The auto-built reflection POJO reader produced
 * a bare {@code new java.lang.Object()} for object input (silent
 * data loss) and threw {@code "expected '{'"} for any non-object
 * literal — array, string, number, boolean — making
 * {@code Object} fields effectively unusable for heterogeneous
 * JSON shapes.
 *
 * <p>The fix routes {@code Object} field types through the same
 * {@code readAny()} + {@code convertValue} fallback already used
 * for {@code JSONObject} / {@code JSONArray} field types, so
 * {@code Object} accepts any JSON value:
 * <ul>
 *   <li>{@code {…}} → {@link JSONObject}</li>
 *   <li>{@code […]} → {@link JSONArray}</li>
 *   <li>{@code "…"} → {@link String}</li>
 *   <li>number → boxed numeric ({@link Integer}, etc.)</li>
 *   <li>{@code true}/{@code false} → {@link Boolean}</li>
 *   <li>{@code null} → {@code null}</li>
 * </ul>
 */
class TypedBeanObjectFieldTest {
    public static class Bean {
        public Object payload;
    }

    @Test
    void objectField_jsonObject_yieldsJSONObject() {
        Bean b = ObjectMapper.shared().readValue(
                "{\"payload\":{\"x\":1,\"y\":\"hi\"}}", Bean.class);
        assertInstanceOf(JSONObject.class, b.payload);
        JSONObject p = (JSONObject) b.payload;
        assertEquals(1, p.get("x"));
        assertEquals("hi", p.get("y"));
    }

    @Test
    void objectField_jsonArray_yieldsJSONArray() {
        Bean b = ObjectMapper.shared().readValue(
                "{\"payload\":[1,2,3]}", Bean.class);
        assertInstanceOf(JSONArray.class, b.payload);
        JSONArray p = (JSONArray) b.payload;
        assertEquals(3, p.size());
        assertEquals(1, p.get(0));
        assertEquals(3, p.get(2));
    }

    @Test
    void objectField_string_yieldsString() {
        Bean b = ObjectMapper.shared().readValue("{\"payload\":\"hello\"}", Bean.class);
        assertEquals("hello", b.payload);
    }

    @Test
    void objectField_number_yieldsBoxedNumber() {
        Bean b = ObjectMapper.shared().readValue("{\"payload\":42}", Bean.class);
        // Number boxing matches readAny semantics; pin both class and value.
        assertEquals(42, ((Number) b.payload).intValue());
    }

    @Test
    void objectField_boolean_yieldsBoolean() {
        Bean b = ObjectMapper.shared().readValue("{\"payload\":true}", Bean.class);
        assertEquals(Boolean.TRUE, b.payload);
    }

    @Test
    void objectField_null_yieldsNull() {
        Bean b = ObjectMapper.shared().readValue("{\"payload\":null}", Bean.class);
        assertNull(b.payload);
    }

    public static class HolderWithList {
        public List<Object> items;
    }

    @Test
    void listOfObject_mixedShapes_eachItemHasNativeType() {
        HolderWithList h = ObjectMapper.shared().readValue(
                "{\"items\":[{\"a\":1},[1,2],\"s\",42,true,null]}", HolderWithList.class);
        assertNotNull(h.items);
        assertEquals(6, h.items.size());
        assertInstanceOf(JSONObject.class, h.items.get(0));
        assertInstanceOf(JSONArray.class, h.items.get(1));
        assertEquals("s", h.items.get(2));
        assertEquals(42, ((Number) h.items.get(3)).intValue());
        assertEquals(Boolean.TRUE, h.items.get(4));
        assertNull(h.items.get(5));
    }

    public static class HolderWithMap {
        public Map<String, Object> data;
    }

    @Test
    void mapOfObject_mixedValueShapes_eachValueHasNativeType() {
        HolderWithMap h = ObjectMapper.shared().readValue(
                "{\"data\":{\"o\":{\"x\":1},\"a\":[1,2],\"s\":\"v\",\"n\":7,\"b\":false}}",
                HolderWithMap.class);
        assertNotNull(h.data);
        assertInstanceOf(JSONObject.class, h.data.get("o"));
        assertInstanceOf(JSONArray.class, h.data.get("a"));
        assertEquals("v", h.data.get("s"));
        assertEquals(7, ((Number) h.data.get("n")).intValue());
        assertEquals(Boolean.FALSE, h.data.get("b"));
    }

    public record RecordWithObject(Object payload) {
    }

    @Test
    void recordObjectField_jsonObject_yieldsJSONObject() {
        RecordWithObject r = ObjectMapper.shared().readValue(
                "{\"payload\":{\"k\":\"v\"}}", RecordWithObject.class);
        assertInstanceOf(JSONObject.class, r.payload());
        assertEquals("v", ((JSONObject) r.payload()).get("k"));
    }

    @Test
    void recordObjectField_jsonArray_yieldsJSONArray() {
        RecordWithObject r = ObjectMapper.shared().readValue(
                "{\"payload\":[\"x\",\"y\"]}", RecordWithObject.class);
        assertInstanceOf(JSONArray.class, r.payload());
        assertEquals(2, ((JSONArray) r.payload()).size());
    }

    @Test
    void objectField_honorsMapSupplier() throws Exception {
        ObjectMapper mapper = ObjectMapper.builder()
                .mapSupplier(ConcurrentHashMap::new)
                .build();
        Bean b = mapper.readValue("{\"payload\":{\"x\":1}}", Bean.class);
        assertInstanceOf(JSONObject.class, b.payload);
        java.lang.reflect.Field inner = JSONObject.class.getDeclaredField("innerMap");
        inner.setAccessible(true);
        assertInstanceOf(ConcurrentHashMap.class, inner.get(b.payload));
    }
}

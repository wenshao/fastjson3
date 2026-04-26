package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regressions for JSONObject.class / JSONArray.class routing in
 * ObjectMapper.readValue. Without the short-circuit in
 * {@code createReaderInternal} these calls fall into the auto-created
 * reflection POJO reader, which (a) rejects arrays outright with
 * {@code JSONException("expected '{'")} and (b) silently produces an empty
 * JSONObject (no fields) for object input.
 */
class JSONObjectArrayClassReadTest {

    @Test
    void readValue_jsonObjectClass_populates() {
        ObjectMapper mapper = ObjectMapper.shared();
        JSONObject obj = mapper.readValue("{\"a\":1,\"b\":2}", JSONObject.class);
        assertNotNull(obj);
        assertEquals(1, obj.get("a"));
        assertEquals(2, obj.get("b"));
    }

    @Test
    void readValue_jsonArrayClass_populates() {
        ObjectMapper mapper = ObjectMapper.shared();
        JSONArray arr = mapper.readValue("[1,2,3]", JSONArray.class);
        assertNotNull(arr);
        assertEquals(3, arr.size());
        assertEquals(1, arr.get(0));
        assertEquals(3, arr.get(2));
    }

    @Test
    void readValue_jsonArrayClass_bytesInput() {
        ObjectMapper mapper = ObjectMapper.shared();
        byte[] bytes = "[\"a\",\"b\"]".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        JSONArray arr = mapper.readValue(bytes, JSONArray.class);
        assertNotNull(arr);
        assertEquals(2, arr.size());
        assertEquals("a", arr.get(0));
    }

    @Test
    void readValue_jsonObjectClass_bytesInput() {
        ObjectMapper mapper = ObjectMapper.shared();
        byte[] bytes = "{\"x\":42}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        JSONObject obj = mapper.readValue(bytes, JSONObject.class);
        assertNotNull(obj);
        assertEquals(42, obj.get("x"));
    }

    @Test
    void readValue_jsonObjectClass_honorsMapSupplier() throws Exception {
        ObjectMapper mapper = ObjectMapper.builder()
                .mapSupplier(ConcurrentHashMap::new)
                .build();
        JSONObject obj = mapper.readValue("{\"a\":1}", JSONObject.class);
        Field f = JSONObject.class.getDeclaredField("innerMap");
        f.setAccessible(true);
        assertInstanceOf(ConcurrentHashMap.class, f.get(obj));
    }

    @Test
    void readValue_jsonArrayClass_honorsListSupplier() throws Exception {
        ObjectMapper mapper = ObjectMapper.builder()
                .listSupplier(LinkedList::new)
                .build();
        JSONArray arr = mapper.readValue("[1,2,3]", JSONArray.class);
        Field f = JSONArray.class.getDeclaredField("innerList");
        f.setAccessible(true);
        assertInstanceOf(LinkedList.class, f.get(arr));
    }

    @Test
    void readValue_jsonArrayClass_nestedObjects() {
        ObjectMapper mapper = ObjectMapper.shared();
        JSONArray arr = mapper.readValue("[{\"a\":1},{\"b\":2}]", JSONArray.class);
        assertEquals(2, arr.size());
        assertInstanceOf(JSONObject.class, arr.get(0));
        assertEquals(1, ((JSONObject) arr.get(0)).get("a"));
    }

    @Test
    void readValue_jsonObjectClass_nestedArray() {
        ObjectMapper mapper = ObjectMapper.shared();
        JSONObject obj = mapper.readValue("{\"items\":[1,2,3]}", JSONObject.class);
        assertInstanceOf(JSONArray.class, obj.get("items"));
        assertEquals(3, ((JSONArray) obj.get("items")).size());
    }
}

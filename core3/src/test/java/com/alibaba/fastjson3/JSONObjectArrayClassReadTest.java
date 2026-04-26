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

    public static class BeanWithJsonNodes {
        public JSONObject payload;
        public JSONArray items;
    }

    @Test
    void readValue_typedBean_jsonObjectField_populated() {
        ObjectMapper mapper = ObjectMapper.shared();
        BeanWithJsonNodes bean = mapper.readValue(
                "{\"payload\":{\"a\":1,\"b\":2},\"items\":[10,20]}", BeanWithJsonNodes.class);
        assertNotNull(bean.payload);
        assertEquals(1, bean.payload.get("a"));
        assertEquals(2, bean.payload.get("b"));
        assertNotNull(bean.items);
        assertEquals(2, bean.items.size());
        assertEquals(10, bean.items.get(0));
        assertEquals(20, bean.items.get(1));
    }

    public record RecordWithJsonNodes(JSONObject meta, JSONArray tags) {
    }

    @Test
    void readValue_typedRecord_jsonNodeFields_populated() {
        ObjectMapper mapper = ObjectMapper.shared();
        RecordWithJsonNodes r = mapper.readValue(
                "{\"meta\":{\"k\":\"v\"},\"tags\":[\"x\",\"y\"]}", RecordWithJsonNodes.class);
        assertNotNull(r.meta());
        assertEquals("v", r.meta().get("k"));
        assertNotNull(r.tags());
        assertEquals(2, r.tags().size());
        assertEquals("x", r.tags().get(0));
    }

    public static class BeanWithListOfJsonObject {
        public java.util.List<JSONObject> entries;
    }

    @Test
    void readValue_listOfJSONObjectField_populated_reflectionPath() {
        ObjectMapper mapper = ObjectMapper.builder()
                .readerProvider(new com.alibaba.fastjson3.reader.ReflectObjectReaderProvider())
                .build();
        BeanWithListOfJsonObject bean = mapper.readValue(
                "{\"entries\":[{\"a\":1},{\"b\":2}]}", BeanWithListOfJsonObject.class);
        assertNotNull(bean.entries);
        assertEquals(2, bean.entries.size());
        assertEquals(1, bean.entries.get(0).get("a"));
        assertEquals(2, bean.entries.get(1).get("b"));
    }

    @Test
    void readValue_listOfJSONObjectField_populated() {
        ObjectMapper mapper = ObjectMapper.shared();
        BeanWithListOfJsonObject bean = mapper.readValue(
                "{\"entries\":[{\"a\":1},{\"b\":2}]}", BeanWithListOfJsonObject.class);
        assertNotNull(bean.entries);
        assertEquals(2, bean.entries.size());
        assertEquals(1, bean.entries.get(0).get("a"));
        assertEquals(2, bean.entries.get(1).get("b"));
    }

    public static class BeanWithMapOfJsonArray {
        public java.util.Map<String, JSONArray> buckets;
    }

    @Test
    void readValue_mapOfJSONArrayField_populated() {
        ObjectMapper mapper = ObjectMapper.shared();
        BeanWithMapOfJsonArray bean = mapper.readValue(
                "{\"buckets\":{\"a\":[1,2],\"b\":[3,4]}}", BeanWithMapOfJsonArray.class);
        assertNotNull(bean.buckets);
        assertEquals(2, bean.buckets.size());
        assertEquals(2, bean.buckets.get("a").size());
        assertEquals(1, bean.buckets.get("a").get(0));
        assertEquals(4, bean.buckets.get("b").get(1));
    }
}

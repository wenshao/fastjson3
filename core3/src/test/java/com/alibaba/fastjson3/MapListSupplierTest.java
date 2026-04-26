package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Per-mapper {@code Map} / {@code List} supplier overrides — fastjson2-compat
 * {@code ObjectSupplier}/{@code ArraySupplier} pattern. The supplier-built
 * collection becomes the JSONObject/JSONArray's backing storage; downstream
 * Map/List API calls delegate to it.
 */
class MapListSupplierTest {

    private static Object innerMap(JSONObject obj) throws Exception {
        Field f = JSONObject.class.getDeclaredField("innerMap");
        f.setAccessible(true);
        return f.get(obj);
    }

    private static Object innerList(JSONArray arr) throws Exception {
        Field f = JSONArray.class.getDeclaredField("innerList");
        f.setAccessible(true);
        return f.get(arr);
    }

    @Test
    void mapSupplier_concurrentHashMapBacking() throws Exception {
        ObjectMapper mapper = ObjectMapper.builder()
                .mapSupplier(ConcurrentHashMap::new)
                .build();
        Object obj = mapper.readValue("{\"a\":1,\"b\":2}");
        assertInstanceOf(JSONObject.class, obj);
        assertInstanceOf(ConcurrentHashMap.class, innerMap((JSONObject) obj));
        assertEquals(1, ((JSONObject) obj).get("a"));
    }

    @Test
    void mapSupplier_treeMapBacking() throws Exception {
        ObjectMapper mapper = ObjectMapper.builder()
                .mapSupplier(TreeMap::new)
                .build();
        Object obj = mapper.readValue("{\"c\":3,\"a\":1,\"b\":2}");
        assertInstanceOf(JSONObject.class, obj);
        Object backing = innerMap((JSONObject) obj);
        assertInstanceOf(TreeMap.class, backing);
        // TreeMap orders keys; first key should be "a" not "c"
        assertEquals("a", ((Map<?, ?>) backing).keySet().iterator().next());
    }

    @Test
    void listSupplier_linkedListBacking() throws Exception {
        ObjectMapper mapper = ObjectMapper.builder()
                .listSupplier(LinkedList::new)
                .build();
        Object obj = mapper.readValue("[1,2,3]");
        assertInstanceOf(JSONArray.class, obj);
        assertInstanceOf(LinkedList.class, innerList((JSONArray) obj));
        assertEquals(3, ((JSONArray) obj).size());
    }

    @Test
    void bothSuppliers_appliedTogether() throws Exception {
        ObjectMapper mapper = ObjectMapper.builder()
                .mapSupplier(ConcurrentHashMap::new)
                .listSupplier(LinkedList::new)
                .build();
        Object obj = mapper.readValue("{\"items\":[1,2]}");
        assertInstanceOf(JSONObject.class, obj);
        assertInstanceOf(ConcurrentHashMap.class, innerMap((JSONObject) obj));
        Object items = ((JSONObject) obj).get("items");
        assertInstanceOf(JSONArray.class, items);
        assertInstanceOf(LinkedList.class, innerList((JSONArray) items));
    }

    @Test
    void defaultMapper_usesInternalStorage() throws Exception {
        // Sanity: shared mapper has no supplier — JSONObject uses optimized
        // JSONObjectMap, JSONArray uses inherited ArrayList (innerList=null).
        Object obj = JSON.parse("{\"a\":[1]}");
        assertInstanceOf(JSONObject.class, obj);
        Object backing = innerMap((JSONObject) obj);
        // JSONObjectMap is package-private; just verify it's NOT a user-typical map.
        assertNotEquals(ConcurrentHashMap.class, backing.getClass());
        assertNotEquals(TreeMap.class, backing.getClass());
        Object inner = ((JSONObject) obj).get("a");
        assertInstanceOf(JSONArray.class, inner);
        assertNull(innerList((JSONArray) inner)); // inherited ArrayList
    }

    @Test
    void mapSupplier_perMapperIndependent() throws Exception {
        // Two mappers with different suppliers don't interfere.
        ObjectMapper concurrent = ObjectMapper.builder()
                .mapSupplier(ConcurrentHashMap::new)
                .build();
        ObjectMapper tree = ObjectMapper.builder()
                .mapSupplier(TreeMap::new)
                .build();
        assertInstanceOf(ConcurrentHashMap.class, innerMap((JSONObject) concurrent.readValue("{}")));
        assertInstanceOf(TreeMap.class, innerMap((JSONObject) tree.readValue("{}")));
    }

    // Typed-read POJO with Map<String,Object> field: supplier propagation
    // through the typed reader path is currently best-effort. fj2's behavior
    // historically only guarantees supplier honor for untyped JSON.parse —
    // the typed-bean field path goes through reflection-based field setters
    // that use their own Map factory. v1 of this feature ships untyped-only;
    // a follow-up can extend.

    // Round-1 audit P0: ArrayList methods that walk elementData directly
    // (equals/hashCode/forEach/stream/sort/etc.) silently saw the empty
    // inherited storage when innerList was set. Pin each as a regression.

    @Test
    void supplierBackedJSONArray_equalsAndHashCode() {
        ObjectMapper mapper = ObjectMapper.builder()
                .listSupplier(LinkedList::new)
                .build();
        JSONArray arr = (JSONArray) mapper.readValue("[1,2,3]");
        assertEquals(java.util.Arrays.asList(1, 2, 3), arr);
        assertEquals(java.util.Arrays.asList(1, 2, 3).hashCode(), arr.hashCode());
        assertTrue(arr.contains(2));
        assertTrue(arr.containsAll(java.util.Arrays.asList(1, 3)));
    }

    @Test
    void supplierBackedJSONArray_forEachAndStream() {
        ObjectMapper mapper = ObjectMapper.builder()
                .listSupplier(LinkedList::new)
                .build();
        JSONArray arr = (JSONArray) mapper.readValue("[10,20,30]");
        long sum = 0;
        for (Object o : arr) {
            sum += ((Number) o).longValue();
        }
        assertEquals(60, sum);
        assertEquals(60L, arr.stream().mapToLong(o -> ((Number) o).longValue()).sum());
    }

    @Test
    void supplierBackedJSONArray_clone() {
        ObjectMapper mapper = ObjectMapper.builder()
                .listSupplier(LinkedList::new)
                .build();
        JSONArray arr = (JSONArray) mapper.readValue("[1,2,3]");
        JSONArray clone = (JSONArray) arr.clone();
        assertEquals(arr, clone);
        // Clone uses default ArrayList backing (not the supplier's LinkedList);
        // it preserves equality but not the same backing identity.
        assertEquals(3, clone.size());
    }
}

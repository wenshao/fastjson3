package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regressions for {@code mapper.readValue(json, RawInterface.class)}
 * (Map / List / Set / Collection / Iterable). Previously these all threw
 * {@code "cannot deserialize interface ..."} because no ObjectReader is
 * registered and no default fallback existed. The fix routes raw interface
 * targets through the same generic readers that {@code TypeReference<...>}
 * uses, matching fastjson2's {@code ObjectReaderImpl{Map,List}} mapping.
 */
class RawMapListClassReadTest {

    @Test
    void mapClass_returnsLinkedHashMap() {
        Map<?, ?> m = ObjectMapper.shared().readValue("{\"a\":1,\"b\":2}", Map.class);
        assertEquals(LinkedHashMap.class, m.getClass());
        assertEquals(2, m.size());
        assertEquals(1, m.get("a"));
        assertEquals(2, m.get("b"));
    }

    @Test
    void listClass_returnsArrayList() {
        List<?> l = ObjectMapper.shared().readValue("[1,2,3]", List.class);
        assertEquals(ArrayList.class, l.getClass());
        assertEquals(3, l.size());
        assertEquals(1, l.get(0));
        assertEquals(3, l.get(2));
    }

    @Test
    void setClass_returnsLinkedHashSet_preservesOrder() {
        Set<?> s = ObjectMapper.shared().readValue("[3,1,2,1]", Set.class);
        assertEquals(LinkedHashSet.class, s.getClass());
        // LinkedHashSet preserves insertion order while deduplicating
        assertEquals(3, s.size());
        assertTrue(s.containsAll(java.util.Arrays.asList(1, 2, 3)));
    }

    @Test
    void collectionClass_returnsArrayList() {
        Collection<?> c = ObjectMapper.shared().readValue("[\"a\",\"b\"]", Collection.class);
        assertEquals(ArrayList.class, c.getClass());
        assertEquals(2, c.size());
    }

    @Test
    void iterableClass_returnsArrayList() {
        Iterable<?> it = ObjectMapper.shared().readValue("[10,20]", Iterable.class);
        assertEquals(ArrayList.class, it.getClass());
        java.util.Iterator<?> i = it.iterator();
        assertEquals(10, i.next());
        assertEquals(20, i.next());
        assertFalse(i.hasNext());
    }

    @Test
    void mapClass_byteInput() {
        byte[] bytes = "{\"k\":\"v\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Map<?, ?> m = ObjectMapper.shared().readValue(bytes, Map.class);
        assertEquals("v", m.get("k"));
    }

    @Test
    void listClass_byteInput() {
        byte[] bytes = "[true,false]".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        List<?> l = ObjectMapper.shared().readValue(bytes, List.class);
        assertEquals(Boolean.TRUE, l.get(0));
        assertEquals(Boolean.FALSE, l.get(1));
    }

    @Test
    void mapClass_nullLiteral() {
        Map<?, ?> m = ObjectMapper.shared().readValue("null", Map.class);
        assertNull(m);
    }

    @Test
    void listClass_nullLiteral() {
        List<?> l = ObjectMapper.shared().readValue("null", List.class);
        assertNull(l);
    }

    @Test
    void mapClass_emptyObject() {
        Map<?, ?> m = ObjectMapper.shared().readValue("{}", Map.class);
        assertNotNull(m);
        assertTrue(m.isEmpty());
    }

    @Test
    void listClass_emptyArray() {
        List<?> l = ObjectMapper.shared().readValue("[]", List.class);
        assertNotNull(l);
        assertTrue(l.isEmpty());
    }

    @Test
    void mapClass_doesNotApplyMapSupplier() {
        // Per-mapper mapSupplier is for JSONObject backing storage — it does
        // not redirect raw Map.class requests. Pin this to keep the docs
        // truthful.
        ObjectMapper mapper = ObjectMapper.builder()
                .mapSupplier(ConcurrentHashMap::new)
                .build();
        Map<?, ?> m = mapper.readValue("{\"a\":1}", Map.class);
        assertEquals(LinkedHashMap.class, m.getClass());
        assertNotEquals(ConcurrentHashMap.class, m.getClass());
    }

    @Test
    void abstractMap_returnsLinkedHashMap() {
        Map<?, ?> m = ObjectMapper.shared().readValue("{\"a\":1}", AbstractMap.class);
        assertEquals(LinkedHashMap.class, m.getClass());
        assertEquals(1, m.get("a"));
    }

    @Test
    void arrayList_returnsArrayList() {
        Object r = ObjectMapper.shared().readValue("[1,2,3]", ArrayList.class);
        assertEquals(ArrayList.class, r.getClass());
        assertEquals(3, ((List<?>) r).size());
    }

    @Test
    void abstractCollectionAndAbstractList_returnArrayList() {
        Object r1 = ObjectMapper.shared().readValue("[1,2]", java.util.AbstractCollection.class);
        Object r2 = ObjectMapper.shared().readValue("[1,2]", java.util.AbstractList.class);
        assertEquals(ArrayList.class, r1.getClass());
        assertEquals(ArrayList.class, r2.getClass());
    }

    @Test
    void abstractSet_returnsLinkedHashSet() {
        Object r = ObjectMapper.shared().readValue("[1,2,3]", java.util.AbstractSet.class);
        assertEquals(LinkedHashSet.class, r.getClass());
        assertEquals(3, ((Set<?>) r).size());
    }

    @Test
    void hashSet_returnsLinkedHashSet_assignableToHashSet() {
        // LinkedHashSet extends HashSet, so the result is castable to HashSet
        // (caller's declared field/var type) while preserving insertion order.
        Object r = ObjectMapper.shared().readValue("[3,1,2]", java.util.HashSet.class);
        assertInstanceOf(java.util.HashSet.class, r);
        assertEquals(LinkedHashSet.class, r.getClass());
        assertEquals(3, ((Set<?>) r).size());
    }

    @Test
    void linkedHashSet_returnsLinkedHashSet() {
        Object r = ObjectMapper.shared().readValue("[3,1,2]", LinkedHashSet.class);
        assertEquals(LinkedHashSet.class, r.getClass());
    }

    // Specific concrete impls outside the abstract-class fallback set
    // (ConcurrentHashMap.class, TreeMap.class, LinkedList.class, Stack.class,
    // CopyOnWriteArrayList.class, etc.) are NOT covered — they fall through
    // to the regular ObjectReader path which currently produces empty or
    // broken results for several of them. Tracked as a separate follow-up
    // in the same family as the JSONObject.class / JSONArray.class fix.
}

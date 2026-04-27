package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.AbstractSequentialList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regressions for {@code mapper.readValue(json, X.class)} where X is a
 * specific concrete or non-raw container interface — TreeMap,
 * ConcurrentHashMap, LinkedList, Stack, CopyOnWriteArrayList, etc.
 *
 * <p>Pre-fix, these targets either threw {@code "expected '{'"} (the
 * auto-built reflection POJO reader assumed a {@code {…}} field-by-field
 * layout) or silently produced an empty container (the POJO reader
 * succeeded in instantiating concrete maps but found no fields to
 * populate). Same defect family as PRs #139–#142, but for the long
 * tail of concrete impls fastjson2 already covered.
 *
 * <p>The fix routes each declared type to a dedicated factory through
 * the new {@code readTypedMap} / {@code readTypedList} / {@code readTypedSet}
 * helpers in {@link com.alibaba.fastjson3.JSONParser}, mirroring fastjson2's
 * {@code ObjectReaderImpl{Map,List}.of(...)} default mappings:
 *
 * <pre>
 *   ConcurrentMap, ConcurrentHashMap                          → ConcurrentHashMap
 *   ConcurrentNavigableMap, ConcurrentSkipListMap              → ConcurrentSkipListMap
 *   SortedMap, NavigableMap, TreeMap                           → TreeMap
 *   HashMap                                                    → HashMap
 *   LinkedList, Queue, Deque, AbstractSequentialList           → LinkedList
 *   Vector / Stack / CopyOnWriteArrayList                      → as declared
 *   SortedSet, NavigableSet, TreeSet                           → TreeSet
 *   CopyOnWriteArraySet                                        → CopyOnWriteArraySet
 * </pre>
 */
class ConcreteCollectionClassReadTest {

    // ---- Map family ----

    @Test
    void hashMap() {
        HashMap<?, ?> m = ObjectMapper.shared().readValue("{\"a\":1,\"b\":2}", HashMap.class);
        assertEquals(HashMap.class, m.getClass());
        assertEquals(2, m.size());
        assertEquals(1, m.get("a"));
    }

    @Test
    void treeMap_keysSorted() {
        TreeMap<?, ?> m = ObjectMapper.shared().readValue("{\"c\":3,\"a\":1,\"b\":2}", TreeMap.class);
        assertEquals(TreeMap.class, m.getClass());
        assertEquals("a", m.firstKey());
        assertEquals("c", m.lastKey());
    }

    @Test
    void sortedMapAndNavigableMap_routeToTreeMap() {
        Object sm = ObjectMapper.shared().readValue("{\"b\":2,\"a\":1}", SortedMap.class);
        assertEquals(TreeMap.class, sm.getClass());
        Object nm = ObjectMapper.shared().readValue("{\"b\":2,\"a\":1}", NavigableMap.class);
        assertEquals(TreeMap.class, nm.getClass());
    }

    @Test
    void concurrentHashMap_andConcurrentMapInterface() {
        ConcurrentHashMap<?, ?> chm = ObjectMapper.shared()
                .readValue("{\"a\":1,\"b\":2}", ConcurrentHashMap.class);
        assertEquals(ConcurrentHashMap.class, chm.getClass());
        assertEquals(2, chm.size());

        Object cm = ObjectMapper.shared().readValue("{\"a\":1}", ConcurrentMap.class);
        assertEquals(ConcurrentHashMap.class, cm.getClass());
    }

    @Test
    void concurrentSkipListMap_andConcurrentNavigableMapInterface() {
        ConcurrentSkipListMap<?, ?> csm = ObjectMapper.shared()
                .readValue("{\"c\":3,\"a\":1,\"b\":2}", ConcurrentSkipListMap.class);
        assertEquals(ConcurrentSkipListMap.class, csm.getClass());
        assertEquals("a", csm.firstKey());

        Object cnm = ObjectMapper.shared().readValue("{\"a\":1}", ConcurrentNavigableMap.class);
        assertEquals(ConcurrentSkipListMap.class, cnm.getClass());
    }

    // ---- List family ----

    @Test
    void linkedList_andQueueDequeAbstractSequentialList() {
        LinkedList<?> ll = ObjectMapper.shared().readValue("[1,2,3]", LinkedList.class);
        assertEquals(LinkedList.class, ll.getClass());
        assertEquals(3, ll.size());

        Object q = ObjectMapper.shared().readValue("[1,2]", Queue.class);
        assertEquals(LinkedList.class, q.getClass());
        Object d = ObjectMapper.shared().readValue("[1,2]", Deque.class);
        assertEquals(LinkedList.class, d.getClass());
        Object asl = ObjectMapper.shared().readValue("[1,2]", AbstractSequentialList.class);
        assertEquals(LinkedList.class, asl.getClass());
    }

    @Test
    void vector() {
        Vector<?> v = ObjectMapper.shared().readValue("[\"x\",\"y\"]", Vector.class);
        assertEquals(Vector.class, v.getClass());
        assertEquals(2, v.size());
        assertEquals("x", v.get(0));
    }

    @Test
    void stack() {
        Stack<?> s = ObjectMapper.shared().readValue("[1,2,3]", Stack.class);
        assertEquals(Stack.class, s.getClass());
        assertEquals(3, s.size());
        assertEquals(3, s.peek());
    }

    @Test
    void copyOnWriteArrayList() {
        CopyOnWriteArrayList<?> cowal = ObjectMapper.shared()
                .readValue("[1,2,3]", CopyOnWriteArrayList.class);
        assertEquals(CopyOnWriteArrayList.class, cowal.getClass());
        assertEquals(3, cowal.size());
    }

    // ---- Set family ----

    @Test
    void treeSet_sortedAndDeduped() {
        TreeSet<?> ts = ObjectMapper.shared().readValue("[3,1,2,1]", TreeSet.class);
        assertEquals(TreeSet.class, ts.getClass());
        assertEquals(3, ts.size());
        assertEquals(1, ts.first());
        assertEquals(3, ts.last());
    }

    @Test
    void sortedSetAndNavigableSet_routeToTreeSet() {
        Object ss = ObjectMapper.shared().readValue("[3,1,2]", SortedSet.class);
        assertEquals(TreeSet.class, ss.getClass());
        Object ns = ObjectMapper.shared().readValue("[3,1,2]", NavigableSet.class);
        assertEquals(TreeSet.class, ns.getClass());
    }

    @Test
    void copyOnWriteArraySet() {
        CopyOnWriteArraySet<?> cowas = ObjectMapper.shared()
                .readValue("[1,2,3]", CopyOnWriteArraySet.class);
        assertEquals(CopyOnWriteArraySet.class, cowas.getClass());
        assertEquals(3, cowas.size());
    }

    // ---- Cross-cutting: nullability, byte input, mapSupplier scope ----

    @Test
    void nullLiteralAcrossFamily() {
        assertNull(ObjectMapper.shared().readValue("null", LinkedList.class));
        assertNull(ObjectMapper.shared().readValue("null", TreeMap.class));
        assertNull(ObjectMapper.shared().readValue("null", TreeSet.class));
        assertNull(ObjectMapper.shared().readValue("null", ConcurrentHashMap.class));
    }

    @Test
    void byteInput_treeMap() {
        byte[] bytes = "{\"b\":2,\"a\":1}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        TreeMap<?, ?> tm = ObjectMapper.shared().readValue(bytes, TreeMap.class);
        assertEquals(TreeMap.class, tm.getClass());
        assertEquals("a", tm.firstKey());
    }

    @Test
    void byteInput_concurrentHashMap() {
        byte[] bytes = "{\"x\":42}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ConcurrentHashMap<?, ?> chm = ObjectMapper.shared().readValue(bytes, ConcurrentHashMap.class);
        assertEquals(42, chm.get("x"));
    }

    @Test
    void typedMapClass_doesNotApplyMapSupplier() {
        // Per-mapper mapSupplier targets JSONObject backing storage. A typed
        // ConcurrentHashMap.class request gets a real ConcurrentHashMap; the
        // supplier's intended JSONObject backing is unrelated.
        ObjectMapper mapper = ObjectMapper.builder()
                .mapSupplier(() -> new java.util.LinkedHashMap<>())
                .build();
        ConcurrentHashMap<?, ?> chm = mapper.readValue("{\"a\":1}", ConcurrentHashMap.class);
        assertEquals(ConcurrentHashMap.class, chm.getClass());
    }

    // ---- Empty containers ----

    @Test
    void emptyContainersAcrossFamily() {
        assertTrue(ObjectMapper.shared().readValue("{}", TreeMap.class).isEmpty());
        assertTrue(ObjectMapper.shared().readValue("{}", ConcurrentHashMap.class).isEmpty());
        assertTrue(ObjectMapper.shared().readValue("[]", LinkedList.class).isEmpty());
        assertTrue(ObjectMapper.shared().readValue("[]", Stack.class).isEmpty());
        assertTrue(ObjectMapper.shared().readValue("[]", TreeSet.class).isEmpty());
    }

    // ---- ASM provider parity ----

    @Test
    void asmProvider_linkedList() {
        ObjectMapper m = ObjectMapper.builder()
                .readerProvider(new com.alibaba.fastjson3.reader.ASMObjectReaderProvider())
                .build();
        LinkedList<?> ll = m.readValue("[1,2]", LinkedList.class);
        assertEquals(LinkedList.class, ll.getClass());
        assertEquals(2, ll.size());
    }

    @Test
    void asmProvider_treeMap() {
        ObjectMapper m = ObjectMapper.builder()
                .readerProvider(new com.alibaba.fastjson3.reader.ASMObjectReaderProvider())
                .build();
        TreeMap<?, ?> tm = m.readValue("{\"b\":2,\"a\":1}", TreeMap.class);
        assertEquals(TreeMap.class, tm.getClass());
        assertEquals("a", tm.firstKey());
    }

    // Note: typed Bean / record fields declared as concrete container impls
    // (`Bean { TreeMap map; }` / `Bean { LinkedList list; }` etc.) are a
    // pre-existing defect on main — the field is silently populated with a
    // JSONObject / JSONArray from readAny() rather than a real instance of
    // the declared concrete class. Verified on commit a458d5d (PR #141 head)
    // and 9954808 (PR #142 head). Field-level routing requires extending the
    // FieldReader.convertValue / coerceListToArray bridges to materialise a
    // real concrete-impl instance from the readAny() result. Tracked as a
    // separate follow-up; this PR's scope is the entry-point readValue path.

    // ---- TypeReference<concrete> parity ----

    // Round-2 audit P0: TypeReference<TreeMap<String, Object>> previously
    // returned a LinkedHashMap because read(Type)'s ParameterizedType branch
    // delegated all Map raws to readGenericMap. The same dispatch table now
    // applies, so TypeReference parity matches the Class-typed entry-point.

    @Test
    void typeRef_treeMap_yieldsTreeMap() {
        TreeMap<String, Object> tm = ObjectMapper.shared().readValue(
                "{\"c\":3,\"a\":1,\"b\":2}",
                new TypeReference<TreeMap<String, Object>>() {});
        assertEquals(TreeMap.class, tm.getClass());
        assertEquals("a", tm.firstKey());
        assertEquals("c", tm.lastKey());
    }

    @Test
    void typeRef_concurrentHashMap_yieldsConcurrentHashMap() {
        ConcurrentHashMap<String, Object> chm = ObjectMapper.shared().readValue(
                "{\"x\":1}",
                new TypeReference<ConcurrentHashMap<String, Object>>() {});
        assertEquals(ConcurrentHashMap.class, chm.getClass());
        assertEquals(1, chm.get("x"));
    }

    @Test
    void typeRef_linkedList_yieldsLinkedList() {
        LinkedList<Integer> ll = ObjectMapper.shared().readValue(
                "[1,2,3]",
                new TypeReference<LinkedList<Integer>>() {});
        assertEquals(LinkedList.class, ll.getClass());
        assertEquals(3, ll.size());
        assertEquals(Integer.valueOf(1), ll.getFirst());
    }

    @Test
    void typeRef_stack_yieldsStack() {
        Stack<Integer> s = ObjectMapper.shared().readValue(
                "[1,2,3]",
                new TypeReference<Stack<Integer>>() {});
        assertEquals(Stack.class, s.getClass());
        assertEquals(3, s.peek());
    }

    @Test
    void typeRef_treeSet_sorted() {
        TreeSet<Integer> ts = ObjectMapper.shared().readValue(
                "[3,1,2,1]",
                new TypeReference<TreeSet<Integer>>() {});
        assertEquals(TreeSet.class, ts.getClass());
        assertEquals(3, ts.size());
        assertEquals(Integer.valueOf(1), ts.first());
    }

    @Test
    void typeRef_rawList_unchanged_arrayList() {
        // Generic List / Set / Map TypeReferences still go to the generic
        // default — pin the negative case so future drift is caught.
        java.util.List<Integer> l = ObjectMapper.shared().readValue(
                "[1,2]", new TypeReference<java.util.List<Integer>>() {});
        assertEquals(java.util.ArrayList.class, l.getClass());
    }

    // ---- SPI override still wins ----

    @Test
    void registerReader_treeMap_takesPrecedence() {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        ObjectReader<TreeMap> custom =
                (parser, fieldType, fieldName, features) -> {
                    calls.incrementAndGet();
                    TreeMap<String, Object> tm = new TreeMap<>();
                    tm.put("__intercepted", Boolean.TRUE);
                    parser.readObject();
                    return tm;
                };
        ObjectMapper mapper = ObjectMapper.shared();
        mapper.registerReader(TreeMap.class, custom);
        try {
            TreeMap<?, ?> tm = mapper.readValue("{\"x\":1}", TreeMap.class);
            assertEquals(Boolean.TRUE, tm.get("__intercepted"));
            assertTrue(calls.get() >= 1);
        } finally {
            mapper.unregisterReader(TreeMap.class);
        }
    }
}

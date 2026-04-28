package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drift guard for the two parallel "parser-handled types" lists:
 * {@link ObjectReaderCreator#isParserHandled} (provider-level)
 * and {@code ObjectMapper.isParserShortCircuitClass}
 * (mapper-level). They must classify every class identically — any
 * one-sided update (forgetting to add a new entry to one of the two
 * lists) is a latent bug where the SAME class behaves differently
 * across the {@code .readerCreator(...)} / {@code .readerProvider(...)} /
 * default mapper paths.
 */
class ParserHandledHelpersAlignmentTest {

    /**
     * Tree-shape parser-handled set (the smaller list): types whose typed
     * reads always go through the parser's {@code readObject} /
     * {@code readArray} / {@code readAny} / generic-container path. Field
     * declarations of these types should fall through to {@code readAny()}
     * + {@code convertValue} rather than synthesizing an auto-built POJO
     * reader.
     */
    private static final Class<?>[] PARSER_HANDLED = {
            JSONObject.class, JSONArray.class, Object.class,
            Map.class, AbstractMap.class,
            List.class, Collection.class, Iterable.class,
            ArrayList.class, AbstractCollection.class, AbstractList.class,
            Set.class, AbstractSet.class, HashSet.class, LinkedHashSet.class,
    };

    /**
     * Concrete impls / non-raw interfaces that are dispatched at the
     * parser-entry layer (provider-level + ObjectMapper-level), but NOT at
     * the field-level. Field declarations like {@code Bean { TreeMap map; }}
     * still synthesize a typed POJO reader so the field gets a real
     * {@link TreeMap}, not a {@code JSONObject}.
     */
    private static final Class<?>[] DISPATCH_ONLY = {
            HashMap.class, TreeMap.class,
            java.util.SortedMap.class, java.util.NavigableMap.class,
            java.util.concurrent.ConcurrentMap.class, ConcurrentHashMap.class,
            java.util.concurrent.ConcurrentNavigableMap.class,
            java.util.concurrent.ConcurrentSkipListMap.class,
            LinkedList.class, java.util.Queue.class, java.util.Deque.class,
            java.util.AbstractSequentialList.class,
            java.util.Vector.class, java.util.Stack.class,
            java.util.concurrent.CopyOnWriteArrayList.class,
            TreeSet.class, java.util.SortedSet.class, java.util.NavigableSet.class,
            java.util.concurrent.CopyOnWriteArraySet.class,
    };

    private static final Class<?>[] NOT_DISPATCHED = {
            String.class, Integer.class, Long.class, Boolean.class,
            // concrete impls that are NOT in any list — niche subclasses
            // we deliberately leave out (no fj2 default mapping).
            java.util.WeakHashMap.class, java.util.IdentityHashMap.class,
            java.util.PriorityQueue.class, java.util.ArrayDeque.class,
            java.util.EnumSet.class,
            // arbitrary user POJO
            ParserHandledHelpersAlignmentTest.class,
            // primitive arrays / parameterized roots — not in any list
            int[].class, String[].class,
    };

    @Test
    void parserHandledSet_pinned() throws Exception {
        // PARSER_HANDLED entries are recognised by isParserHandled (the
        // small list used at field-level + provider-level for tree-shape
        // types). isParserDispatched is a strict superset, so it MUST also
        // recognise them.
        Method handled = ObjectReaderCreator.class.getDeclaredMethod("isParserHandled", Class.class);
        handled.setAccessible(true);
        Method dispatched = ObjectReaderCreator.class.getDeclaredMethod("isParserDispatched", Class.class);
        dispatched.setAccessible(true);
        Method mapper = ObjectMapper.class.getDeclaredMethod("isParserShortCircuitClass", Class.class);
        mapper.setAccessible(true);

        for (Class<?> c : PARSER_HANDLED) {
            assertTrue((boolean) handled.invoke(null, c),
                    "isParserHandled missed: " + c.getName());
            assertTrue((boolean) dispatched.invoke(null, c),
                    "isParserDispatched missed: " + c.getName());
            assertTrue((boolean) mapper.invoke(null, c),
                    "isParserShortCircuitClass missed: " + c.getName());
        }
    }

    @Test
    void parserDispatchedOnly_pinned() throws Exception {
        // DISPATCH_ONLY entries are concrete impls / non-raw interfaces. They
        // are dispatched at parser-entry (isParserDispatched +
        // isParserShortCircuitClass) but NOT at field-level
        // (isParserHandled). A field declared as TreeMap should still get a
        // real TreeMap, not a JSONObject from readAny.
        Method handled = ObjectReaderCreator.class.getDeclaredMethod("isParserHandled", Class.class);
        handled.setAccessible(true);
        Method dispatched = ObjectReaderCreator.class.getDeclaredMethod("isParserDispatched", Class.class);
        dispatched.setAccessible(true);
        Method mapper = ObjectMapper.class.getDeclaredMethod("isParserShortCircuitClass", Class.class);
        mapper.setAccessible(true);

        for (Class<?> c : DISPATCH_ONLY) {
            assertFalse((boolean) handled.invoke(null, c),
                    "isParserHandled wrongly tree-shape: " + c.getName());
            assertTrue((boolean) dispatched.invoke(null, c),
                    "isParserDispatched missed: " + c.getName());
            assertTrue((boolean) mapper.invoke(null, c),
                    "isParserShortCircuitClass missed: " + c.getName());
        }
    }

    @Test
    void notDispatched_pinned() throws Exception {
        Method handled = ObjectReaderCreator.class.getDeclaredMethod("isParserHandled", Class.class);
        handled.setAccessible(true);
        Method dispatched = ObjectReaderCreator.class.getDeclaredMethod("isParserDispatched", Class.class);
        dispatched.setAccessible(true);
        Method mapper = ObjectMapper.class.getDeclaredMethod("isParserShortCircuitClass", Class.class);
        mapper.setAccessible(true);

        for (Class<?> c : NOT_DISPATCHED) {
            assertFalse((boolean) handled.invoke(null, c),
                    "isParserHandled wrongly handles: " + c.getName());
            assertFalse((boolean) dispatched.invoke(null, c),
                    "isParserDispatched wrongly handles: " + c.getName());
            assertFalse((boolean) mapper.invoke(null, c),
                    "isParserShortCircuitClass wrongly handles: " + c.getName());
        }
    }

}

package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
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

    private static final Class<?>[] HANDLED = {
            JSONObject.class, JSONArray.class, Object.class,
            Map.class, AbstractMap.class,
            List.class, Collection.class, Iterable.class,
            ArrayList.class, AbstractCollection.class, AbstractList.class,
            Set.class, AbstractSet.class, HashSet.class, LinkedHashSet.class,
    };

    private static final Class<?>[] NOT_HANDLED = {
            String.class, Integer.class, Long.class, Boolean.class,
            // concrete map/list/set impls that are NOT in either list — they
            // belong to a separate follow-up.
            ConcurrentHashMap.class, TreeMap.class, HashMap.class,
            LinkedList.class, TreeSet.class,
            // arbitrary user POJO
            ParserHandledHelpersAlignmentTest.class,
            // primitive arrays / parameterized roots — not in either list
            int[].class, String[].class,
    };

    @Test
    void bothHelpersAgreeOnHandledClasses() throws Exception {
        Method creator = ObjectReaderCreator.class.getDeclaredMethod("isParserHandled", Class.class);
        creator.setAccessible(true);
        Method mapper = ObjectMapper.class.getDeclaredMethod("isParserShortCircuitClass", Class.class);
        mapper.setAccessible(true);

        for (Class<?> c : HANDLED) {
            boolean creatorSays = (boolean) creator.invoke(null, c);
            boolean mapperSays = (boolean) mapper.invoke(null, c);
            assertTrue(creatorSays,
                    "ObjectReaderCreator.isParserHandled missed: " + c.getName());
            assertTrue(mapperSays,
                    "ObjectMapper.isParserShortCircuitClass missed: " + c.getName());
            assertEquals(creatorSays, mapperSays,
                    "alignment broken for: " + c.getName());
        }
    }

    @Test
    void bothHelpersAgreeOnNonHandledClasses() throws Exception {
        Method creator = ObjectReaderCreator.class.getDeclaredMethod("isParserHandled", Class.class);
        creator.setAccessible(true);
        Method mapper = ObjectMapper.class.getDeclaredMethod("isParserShortCircuitClass", Class.class);
        mapper.setAccessible(true);

        for (Class<?> c : NOT_HANDLED) {
            boolean creatorSays = invoke(creator, c);
            boolean mapperSays = invoke(mapper, c);
            assertFalse(creatorSays,
                    "ObjectReaderCreator.isParserHandled wrongly handles: " + c.getName());
            assertFalse(mapperSays,
                    "ObjectMapper.isParserShortCircuitClass wrongly handles: " + c.getName());
            assertEquals(creatorSays, mapperSays,
                    "alignment broken for: " + c.getName());
        }
    }

    private static boolean invoke(Method m, Class<?> arg)
            throws IllegalAccessException, InvocationTargetException {
        return (boolean) m.invoke(null, arg);
    }
}

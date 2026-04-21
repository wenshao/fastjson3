package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the Jackson-style top-level generic-collection idiom:
 * <pre>
 * JSON.parseObject(json, new TypeReference&lt;List&lt;User&gt;&gt;() {})
 * JSON.parseObject(json, new TypeReference&lt;Map&lt;K, V&gt;&gt;() {})
 * JSON.parseObject(json, new TypeReference&lt;Set&lt;E&gt;&gt;() {})
 * </pre>
 *
 * <p>Pre-fix: {@code JSON.parseObject(json, TypeReference&lt;List&lt;X&gt;&gt;)} went
 * through {@code JSONParser.read(Type)} which tried to resolve an
 * {@code ObjectReader} for the raw {@code ParameterizedType} and threw
 * {@code "no ObjectReader registered for type: java.util.List&lt;X&gt;"}. Users had
 * to switch to {@link JSON#parseList(String, Class)} / {@link JSON#parseMap}
 * / {@link JSON#parseSet}, breaking the migration story from Jackson / Gson.
 */
public class TypeReferenceGenericTest {

    public record User(String name, int age) {}

    public enum Role { ADMIN, USER }

    @Test
    public void listOfRecord() {
        List<User> users = JSON.parseObject(
                "[{\"name\":\"a\",\"age\":1},{\"name\":\"b\",\"age\":2}]",
                new TypeReference<List<User>>() {});
        assertEquals(2, users.size());
        assertEquals("a", users.get(0).name());
        assertEquals(2, users.get(1).age());
    }

    @Test
    public void listOfString() {
        List<String> list = JSON.parseObject("[\"a\",\"b\"]", new TypeReference<List<String>>() {});
        assertEquals(List.of("a", "b"), list);
    }

    @Test
    public void setOfStringDeduplicates() {
        Set<String> set = JSON.parseObject("[\"a\",\"b\",\"a\"]", new TypeReference<Set<String>>() {});
        assertEquals(Set.of("a", "b"), set);
        assertInstanceOf(LinkedHashSet.class, set, "preserves insertion order for predictable iteration");
    }

    @Test
    public void mapStringToRecord() {
        Map<String, User> byName = JSON.parseObject(
                "{\"alice\":{\"name\":\"Alice\",\"age\":1},\"bob\":{\"name\":\"Bob\",\"age\":2}}",
                new TypeReference<Map<String, User>>() {});
        assertEquals(2, byName.size());
        assertEquals("Alice", byName.get("alice").name());
        assertInstanceOf(LinkedHashMap.class, byName);
    }

    @Test
    public void mapIntegerKey() {
        Map<Integer, User> byId = JSON.parseObject(
                "{\"1\":{\"name\":\"a\",\"age\":1},\"2\":{\"name\":\"b\",\"age\":2}}",
                new TypeReference<Map<Integer, User>>() {});
        assertEquals("a", byId.get(1).name());
        assertEquals("b", byId.get(2).name());
    }

    @Test
    public void mapLongKey() {
        Map<Long, String> m = JSON.parseObject("{\"100\":\"x\"}",
                new TypeReference<Map<Long, String>>() {});
        assertEquals("x", m.get(100L));
    }

    @Test
    public void mapEnumKey() {
        Map<Role, Integer> counts = JSON.parseObject("{\"ADMIN\":3,\"USER\":7}",
                new TypeReference<Map<Role, Integer>>() {});
        assertEquals(3, counts.get(Role.ADMIN));
        assertEquals(7, counts.get(Role.USER));
    }

    @Test
    public void nestedMapOfListOfRecord() {
        Map<String, List<User>> nested = JSON.parseObject(
                "{\"team1\":[{\"name\":\"a\",\"age\":1},{\"name\":\"b\",\"age\":2}],\"team2\":[]}",
                new TypeReference<Map<String, List<User>>>() {});
        assertEquals(2, nested.size());
        assertEquals(2, nested.get("team1").size());
        assertTrue(nested.get("team2").isEmpty());
    }

    @Test
    public void nestedListOfListOfInt() {
        List<List<Integer>> matrix = JSON.parseObject(
                "[[1,2,3],[4,5,6]]",
                new TypeReference<List<List<Integer>>>() {});
        assertEquals(List.of(1, 2, 3), matrix.get(0));
        assertEquals(List.of(4, 5, 6), matrix.get(1));
    }

    @Test
    public void nullInputReturnsNull() {
        assertNull(JSON.parseObject("null", new TypeReference<List<User>>() {}));
        assertNull(JSON.parseObject("null", new TypeReference<Map<String, User>>() {}));
        assertNull(JSON.parseObject("null", new TypeReference<Set<String>>() {}));
    }

    @Test
    public void emptyCollectionsParse() {
        assertTrue(JSON.parseObject("[]", new TypeReference<List<User>>() {}).isEmpty());
        assertTrue(JSON.parseObject("[]", new TypeReference<Set<String>>() {}).isEmpty());
        assertTrue(JSON.parseObject("{}", new TypeReference<Map<String, User>>() {}).isEmpty());
    }

    /** Byte[] input path shares the same {@link JSONParser#read(java.lang.reflect.Type)} dispatch. */
    @Test
    public void byteInputSupportsTypeReference() {
        byte[] bytes = "[{\"name\":\"a\",\"age\":1}]".getBytes();
        List<User> users = JSON.parseObject(bytes, new TypeReference<List<User>>() {}.getType());
        assertEquals(1, users.size());
    }

    // ==================== null elements / values in typed collections ====================
    // Round-3 usability audit finding F2: null element in List<String> / value in
    // Map<String,String> used to throw "expected quote" at the null token because
    // the typed-target dispatch called readString() directly, with no null guard.

    @Test
    public void typedStringListAcceptsNullElements() {
        List<String> list = JSON.parseObject("[null,\"a\",null]", new TypeReference<List<String>>() {});
        assertEquals(3, list.size());
        assertNull(list.get(0));
        assertEquals("a", list.get(1));
        assertNull(list.get(2));
    }

    @Test
    public void typedStringMapAcceptsNullValues() {
        Map<String, String> m = JSON.parseObject("{\"a\":null,\"b\":\"x\",\"c\":null}",
                new TypeReference<Map<String, String>>() {});
        assertEquals(3, m.size());
        assertNull(m.get("a"));
        assertEquals("x", m.get("b"));
        assertNull(m.get("c"));
    }

    @Test
    public void typedStringSetAcceptsNullElements() {
        Set<String> s = JSON.parseObject("[\"a\",null,\"b\"]", new TypeReference<Set<String>>() {});
        assertEquals(3, s.size());
        assertTrue(s.contains(null));
        assertTrue(s.contains("a"));
    }

    @Test
    public void typedBoxedNumberListAcceptsNullElements() {
        // Same null-literal courtesy extended to boxed wrappers.
        List<Integer> li = JSON.parseObject("[1,null,3]", new TypeReference<List<Integer>>() {});
        assertEquals(java.util.Arrays.asList(1, null, 3), li);

        List<Long> ll = JSON.parseObject("[null,2]", new TypeReference<List<Long>>() {});
        assertNull(ll.get(0));
        assertEquals(2L, ll.get(1));

        List<Boolean> lb = JSON.parseObject("[true,null,false]", new TypeReference<List<Boolean>>() {});
        assertEquals(java.util.Arrays.asList(true, null, false), lb);
    }

    // Round-4: the BuiltinCodecs String-family readers (UUID, Date/time,
    // URI, Path, …) called readString() unconditionally — null elements
    // in a typed collection of those types threw. Fix at the codec layer
    // (readString → readNullableString).

    @Test
    public void typedLocalDateTimeListAcceptsNullElements() {
        List<java.time.LocalDateTime> ldts = JSON.parseObject(
                "[\"2026-04-20T10:00:00\",null,\"2026-04-21T11:00:00\"]",
                new TypeReference<List<java.time.LocalDateTime>>() {});
        assertEquals(3, ldts.size());
        assertNull(ldts.get(1));
        assertEquals(java.time.LocalDateTime.parse("2026-04-20T10:00:00"), ldts.get(0));
    }

    @Test
    public void typedUuidListAcceptsNullElements() {
        List<java.util.UUID> uuids = JSON.parseObject(
                "[\"00000000-0000-0000-0000-000000000001\",null]",
                new TypeReference<List<java.util.UUID>>() {});
        assertEquals(2, uuids.size());
        assertNull(uuids.get(1));
    }

    @Test
    public void typedInstantListAcceptsNullElements() {
        List<java.time.Instant> ins = JSON.parseObject(
                "[null,\"2026-04-20T10:00:00Z\"]",
                new TypeReference<List<java.time.Instant>>() {});
        assertEquals(2, ins.size());
        assertNull(ins.get(0));
        assertNotNull(ins.get(1));
    }

    @Test
    public void typedBigDecimalListAcceptsNullElements() {
        // Focus: null element no longer throws. Element-type fidelity is
        // tracked by a separate PR; here we only assert null + non-null arity.
        List<java.math.BigDecimal> bds = JSON.parseObject(
                "[1.5,null,2.5]", new TypeReference<List<java.math.BigDecimal>>() {});
        assertEquals(3, bds.size());
        assertNull(bds.get(1));
        assertNotNull(bds.get(0));
    }

    @Test
    public void typedBigIntegerListAcceptsNullElements() {
        List<java.math.BigInteger> bis = JSON.parseObject(
                "[42,null]", new TypeReference<List<java.math.BigInteger>>() {});
        assertEquals(2, bis.size());
        assertNotNull(bis.get(0));
        assertNull(bis.get(1));
    }

    @Test
    public void topLevelBigDecimalNull() {
        assertNull(JSON.parseObject("null", java.math.BigDecimal.class));
    }

    @Test
    public void topLevelBigIntegerNull() {
        assertNull(JSON.parseObject("null", java.math.BigInteger.class));
    }

    @Test
    public void topLevelJSONObjectNull() {
        assertNull(JSON.parseObject("null", JSONObject.class));
    }

    @Test
    public void topLevelJSONArrayNull() {
        assertNull(JSON.parseObject("null", JSONArray.class));
    }
}

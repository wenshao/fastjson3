package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONCreator;
import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Parallel reader/writer round-trip harness. For each fixture, asserts:
 * <ol>
 *   <li>{@code write(parse(write(x)))} equals {@code write(x)} — the
 *       re-serialize-idempotent form, which catches asymmetries without
 *       requiring user POJOs to implement {@code equals}.</li>
 *   <li>Same via the byte[] path.</li>
 * </ol>
 * Each fixture targets a specific asymmetry pattern the audit rounds
 * surfaced (covariant-return bridges, record-component unwrap, anyGetter
 * + type discriminator, mix-in factory body, nested generics, null in
 * typed collection). A new writer-side feature that forgets to update the
 * reader (or vice versa) fails the second round trip here.
 */
public class RoundTripHarnessTest {
    static final ObjectMapper SHARED = ObjectMapper.shared();

    record Fixture<T>(String name, T instance, Class<T> type, ObjectMapper mapper) {
        static <T> Fixture<T> of(String name, T instance, Class<T> type) {
            return new Fixture<>(name, instance, type, SHARED);
        }

        static <T> Fixture<T> of(String name, T instance, Class<T> type, ObjectMapper mapper) {
            return new Fixture<>(name, instance, type, mapper);
        }
    }

    @TestFactory
    Stream<DynamicTest> stringRoundTripIdempotent() {
        return fixtures().stream().map(this::stringRoundTrip);
    }

    @TestFactory
    Stream<DynamicTest> bytesRoundTripIdempotent() {
        return fixtures().stream().map(this::bytesRoundTrip);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private DynamicTest stringRoundTrip(Fixture<?> f) {
        return dynamicTest(f.name() + " [string]", () -> {
            String first = f.mapper().writeValueAsString(f.instance());
            Object back = f.mapper().readValue(first, (Class) f.type());
            assertNotNull(back, "parse returned null for " + f.name() + ": " + first);
            String second = f.mapper().writeValueAsString(back);
            assertEquals(first, second,
                    "re-serialize must match — reader/writer asymmetry for " + f.name());
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private DynamicTest bytesRoundTrip(Fixture<?> f) {
        return dynamicTest(f.name() + " [bytes]", () -> {
            byte[] first = f.mapper().writeValueAsBytes(f.instance());
            Object back = f.mapper().readValue(first, (Class) f.type());
            assertNotNull(back, "parse returned null for " + f.name());
            byte[] second = f.mapper().writeValueAsBytes(back);
            assertEquals(new String(first, java.nio.charset.StandardCharsets.UTF_8),
                    new String(second, java.nio.charset.StandardCharsets.UTF_8),
                    "re-serialize (bytes) must match — asymmetry for " + f.name());
        });
    }

    // ==================== Fixtures ====================

    static List<Fixture<?>> fixtures() {
        List<Fixture<?>> list = new ArrayList<>();

        // --- Baseline sanity ---
        list.add(Fixture.of("record/primitives",
                new UserRecord("alice", 30), UserRecord.class));
        list.add(Fixture.of("pojo/scalars",
                Scalars.of("bob", 42, true), Scalars.class));

        // --- Collections (no field-level generics needed) ---
        StringList sl = new StringList();
        sl.items.add("a");
        sl.items.add("b");
        list.add(Fixture.of("pojo/list-of-string", sl, StringList.class));

        IntMap im = new IntMap();
        im.counts.put("x", 1);
        im.counts.put("y", 2);
        list.add(Fixture.of("pojo/map-string-int", im, IntMap.class));

        // --- Nested generics (PR #112) ---
        NestedGeneric ng = new NestedGeneric();
        ng.matrix.add(List.of(1, 2));
        ng.matrix.add(List.of(3, 4));
        list.add(Fixture.of("pojo/list-of-list", ng, NestedGeneric.class));

        GenericParent<Inner> gp = new GenericChild();
        gp.value = new Inner("nested");
        list.add(Fixture.of("generic/parent-child", (GenericChild) gp, GenericChild.class));

        // --- Null in typed collection (PR #122) ---
        list.add(Fixture.of("pojo/list-with-null-element",
                nullElementHolder(), NullElementHolder.class));

        // --- @JSONField(unwrapped=true) writer+reader symmetry (PR #125) ---
        UnwrapPojo up = new UnwrapPojo();
        up.outer = "O";
        up.inner = new UnwrapInner();
        up.inner.a = "A";
        up.inner.b = 7;
        list.add(Fixture.of("pojo/unwrapped-field", up, UnwrapPojo.class));

        list.add(Fixture.of("record/unwrapped-component",
                new UnwrapRecord(new UnwrapInner("A2", 8), "O2"),
                UnwrapRecord.class));

        // --- Covariant-return + Jackson mix-in anyGetter (PR #124 R7) ---
        CovariantTarget ct = new CovariantTarget();
        ObjectMapper covariantMapper = ObjectMapper.builder()
                .useJacksonAnnotation(true)
                .addMixIn(CovariantTarget.class, CovariantMixIn.class)
                .build();
        list.add(Fixture.of("jackson-anygetter/covariant-bridge",
                ct, CovariantTarget.class, covariantMapper));

        // --- anyGetter + anySetter round-trip (no discriminator collision) ---
        AnyGetterPojo ag = new AnyGetterPojo();
        ag.id = "X";
        ag.extras.put("k", 1);
        ag.extras.put("color", "red");
        list.add(Fixture.of("anygetter/with-anysetter",
                ag, AnyGetterPojo.class));

        // KNOWN GAP — not yet in the harness:
        //   * anyGetter + @JSONType(typeName=) / WriteClassName + anySetter:
        //     the reader currently routes "@type" to the anySetter, so
        //     re-write emits "@type" twice. Writer fix (PR #124 R8) is
        //     correct on first write; reader needs a symmetric guard.
        //     Add a fixture here once the reader is fixed.

        // --- @JSONCreator factory method (PR #123) ---
        list.add(Fixture.of("factory/static-of",
                Money.ofCents(2599), Money.class));

        // --- @JSONCreator factory via mix-in (PR #123 R8) ---
        ObjectMapper factoryMixInMapper = ObjectMapper.builder()
                .addMixIn(MoneyTarget.class, MoneyMixIn.class)
                .build();
        MoneyTarget mt = new MoneyTarget();
        mt.cents = 999;
        list.add(Fixture.of("factory/mixin-metadata",
                mt, MoneyTarget.class, factoryMixInMapper));

        return list;
    }

    // ==================== Fixture types ====================

    public record UserRecord(String name, int age) {}

    public static class Scalars {
        public String name;
        public int count;
        public boolean flag;

        public static Scalars of(String n, int c, boolean f) {
            Scalars s = new Scalars();
            s.name = n;
            s.count = c;
            s.flag = f;
            return s;
        }
    }

    public static class StringList {
        public List<String> items = new ArrayList<>();
    }

    public static class IntMap {
        public Map<String, Integer> counts = new LinkedHashMap<>();
    }

    public static class NestedGeneric {
        public List<List<Integer>> matrix = new ArrayList<>();
    }

    public static class GenericParent<T> {
        public T value;
    }

    public static class GenericChild extends GenericParent<Inner> {
    }

    public record Inner(String label) {}

    public static class NullElementHolder {
        public List<String> tags = new ArrayList<>();
    }

    static NullElementHolder nullElementHolder() {
        NullElementHolder h = new NullElementHolder();
        h.tags.add("a");
        h.tags.add(null);
        h.tags.add("c");
        return h;
    }

    // --- Unwrap fixtures ---
    public static class UnwrapInner {
        public String a;
        public int b;

        public UnwrapInner() {}

        public UnwrapInner(String a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    public static class UnwrapPojo {
        public String outer;

        @JSONField(unwrapped = true)
        public UnwrapInner inner;
    }

    public record UnwrapRecord(@JSONField(unwrapped = true) UnwrapInner inner, String outer) {}

    // --- Covariant return + mix-in anyGetter ---
    public static class CovariantTarget {
        public String id = "T";

        public LinkedHashMap<String, Object> getExtras() {
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            m.put("x", 1);
            return m;
        }
    }

    public abstract static class CovariantMixIn {
        @com.fasterxml.jackson.annotation.JsonAnyGetter
        public abstract LinkedHashMap<String, Object> getExtras();
    }

    // --- anyGetter + anySetter pair (round-trippable shape) ---
    public static class AnyGetterPojo {
        public String id;
        private final LinkedHashMap<String, Object> extras = new LinkedHashMap<>();

        @JSONField(anyGetter = true)
        public Map<String, Object> getExtras() {
            return extras;
        }

        @JSONField(anySetter = true)
        public void putExtra(String key, Object value) {
            extras.put(key, value);
        }
    }

    // --- Factory method ---
    public static class Money {
        public long cents;

        @JSONCreator
        public static Money ofCents(@JSONField(name = "cents") long cents) {
            Money m = new Money();
            m.cents = cents;
            return m;
        }
    }

    // --- Factory via mix-in (body on target, annotation on mix-in) ---
    public static class MoneyTarget {
        public long cents;

        public static MoneyTarget ofCents(long cents) {
            MoneyTarget m = new MoneyTarget();
            m.cents = cents;
            return m;
        }
    }

    public abstract static class MoneyMixIn {
        @JSONCreator
        public static MoneyTarget ofCents(@JSONField(name = "cents") long cents) {
            return null;
        }
    }

    // ==================== equality helpers (unused, kept for reference) ====================
    @SuppressWarnings("unused")
    private static boolean deepEquals(Object a, Object b) {
        return Objects.equals(a, b);
    }
}

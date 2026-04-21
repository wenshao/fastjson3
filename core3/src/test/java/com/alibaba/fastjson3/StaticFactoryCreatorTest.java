package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONCreator;
import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jackson-style static factory methods annotated with {@code @JSONCreator} (and
 * {@code @JsonCreator} under {@code useJacksonAnnotation=true}) must build the
 * instance via the factory rather than falling through to constructor resolution.
 * Round-3 usability audit F7.
 */
public class StaticFactoryCreatorTest {

    public static class Money {
        private final long cents;

        private Money(long cents) {
            this.cents = cents;
        }

        @JSONCreator
        public static Money of(@JSONField(name = "cents") long cents) {
            return new Money(cents);
        }

        public long cents() {
            return cents;
        }
    }

    @Test
    public void singleArgStaticFactory() {
        Money m = JSON.parseObject("{\"cents\":5000}", Money.class);
        assertNotNull(m);
        assertEquals(5000L, m.cents());
    }

    public static class Range {
        private final int min;
        private final int max;

        private Range(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @JSONCreator
        public static Range create(@JSONField(name = "min") int min,
                                   @JSONField(name = "max") int max) {
            return new Range(min, max);
        }

        public int min() {
            return min;
        }

        public int max() {
            return max;
        }
    }

    @Test
    public void multiArgStaticFactory() {
        Range r = JSON.parseObject("{\"min\":3,\"max\":17}", Range.class);
        assertEquals(3, r.min());
        assertEquals(17, r.max());
    }

    @Test
    public void multiArgStaticFactoryReversedOrder() {
        // Payload key order doesn't match parameter order — name-based routing
        // must still fill the slots correctly via the fieldReader matcher.
        Range r = JSON.parseObject("{\"max\":20,\"min\":5}", Range.class);
        assertEquals(5, r.min());
        assertEquals(20, r.max());
    }

    public static class Validated {
        final int v;
        private Validated(int v) { this.v = v; }

        @JSONCreator
        public static Validated of(@JSONField(name = "v") int v) {
            if (v < 0) {
                throw new IllegalArgumentException("negative value: " + v);
            }
            return new Validated(v);
        }
    }

    @Test
    public void factoryThrowsExceptionIsUnwrapped() {
        // InvocationTargetException from the factory should surface the
        // underlying cause, not the reflection wrapper.
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"v\":-1}", Validated.class));
        assertTrue(ex.getMessage().contains("negative value"),
                "expected cause message 'negative value' in: " + ex.getMessage());
    }

    public static class ParameterNamesOverride {
        final String tag;
        private ParameterNamesOverride(String tag) { this.tag = tag; }

        // parameterNames on the annotation — no @JSONField on the param,
        // override still wins over the parameter's compiled name.
        @JSONCreator(parameterNames = {"label"})
        public static ParameterNamesOverride make(String notLabel) {
            return new ParameterNamesOverride(notLabel);
        }
    }

    @Test
    public void parameterNamesOverride() {
        ParameterNamesOverride p = JSON.parseObject("{\"label\":\"x\"}",
                ParameterNamesOverride.class);
        assertEquals("x", p.tag);
    }

    // ==================== Static factory preferred over ctor ====================

    public static class FactoryVsCtor {
        public int fromCtor;
        public int fromFactory;

        public FactoryVsCtor() {
            this.fromCtor = 1;
        }

        @JSONCreator
        public static FactoryVsCtor make(@JSONField(name = "x") int x) {
            FactoryVsCtor f = new FactoryVsCtor();
            f.fromCtor = 0;
            f.fromFactory = x;
            return f;
        }
    }

    @Test
    public void staticFactoryTakesPrecedenceOverCtor() {
        // Class has both a default ctor AND a @JSONCreator static factory.
        // Resolution should pick the factory, skipping the no-arg ctor.
        FactoryVsCtor f = JSON.parseObject("{\"x\":42}", FactoryVsCtor.class);
        assertEquals(0, f.fromCtor, "ctor should not have run");
        assertEquals(42, f.fromFactory);
    }

    // ==================== Factory declared on an interface ====================

    public interface Currency {
        @JSONCreator
        static Currency of(@JSONField(name = "code") String code) {
            return new USD(code);
        }
    }

    public static class USD implements Currency {
        public String code;

        public USD() {
        }

        public USD(String code) {
            this.code = code;
        }
    }

    @Test
    public void staticFactoryOnInterfaceReachable() {
        // The interface / abstract guard in createObjectReader used to
        // short-circuit BEFORE the factory lookup, making a valid
        // @JSONCreator on an interface unreachable (Jackson supports this).
        Currency c = JSON.parseObject("{\"code\":\"USD\"}", Currency.class);
        assertInstanceOf(USD.class, c);
        assertEquals("USD", ((USD) c).code);
    }

    // ==================== Multiple @JSONCreator factories: ambiguous ====================

    public static class MultiCreator {
        public int x;
        public String y;

        @JSONCreator
        public static MultiCreator byInt(@JSONField(name = "x") int x) {
            MultiCreator m = new MultiCreator();
            m.x = x;
            return m;
        }

        @JSONCreator
        public static MultiCreator byString(@JSONField(name = "y") String y) {
            MultiCreator m = new MultiCreator();
            m.y = y;
            return m;
        }
    }

    @Test
    public void multipleAnnotatedFactoriesRejected() {
        // Declared-method order is JVM-unspecified; silently picking first
        // found would produce non-deterministic behaviour across runs.
        // ObjectMapper's auto-create path swallows the creator exception as
        // "no ObjectReader registered" — call the creator directly so the
        // targeted error surfaces unchanged.
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(MultiCreator.class));
        assertTrue(ex.getMessage().contains("multiple"), ex.getMessage());
        assertTrue(ex.getMessage().contains("byInt")
                        && ex.getMessage().contains("byString"),
                "expected both method names in: " + ex.getMessage());
    }

    // ==================== @JSONField(unwrapped=true) on factory param rejected ====================

    public static class UnwrappedInner {
        public String city;
    }

    public static class UnwrappedFactoryHost {
        public UnwrappedInner addr;
        public String name;

        @JSONCreator
        public static UnwrappedFactoryHost make(
                @JSONField(unwrapped = true) UnwrappedInner addr,
                @JSONField(name = "name") String name) {
            UnwrappedFactoryHost h = new UnwrappedFactoryHost();
            h.addr = addr;
            h.name = name;
            return h;
        }
    }

    @Test
    public void unwrappedOnFactoryParamRejected() {
        // Factory path doesn't wire up scratch-inner machinery; silently
        // dropping the annotation would lose inner-field routing.
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(UnwrappedFactoryHost.class));
        assertTrue(ex.getMessage().contains("unwrapped"), ex.getMessage());
        assertTrue(ex.getMessage().contains("not supported"), ex.getMessage());
    }

    // ==================== Ambiguity across class + mix-in ====================

    public static class HostClassFactory {
        public int v;

        @JSONCreator
        public static HostClassFactory fromClass(@JSONField(name = "v") int v) {
            HostClassFactory h = new HostClassFactory();
            h.v = v;
            return h;
        }
    }

    public static class HostMixInFactory {
        @JSONCreator
        public static HostClassFactory fromMixIn(@JSONField(name = "v") int v) {
            HostClassFactory h = new HostClassFactory();
            h.v = v;
            return h;
        }
    }

    @Test
    public void crossHostFactoryAmbiguityRejected() {
        // One factory on the target class, another on a mix-in — neither is
        // objectively preferable. The first round of the fix only detected
        // ambiguity within a single host; this test ensures class + mix-in
        // competitors also surface a diagnostic.
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(HostClassFactory.class, HostMixInFactory.class));
        assertTrue(ex.getMessage().contains("multiple"), ex.getMessage());
        assertTrue(ex.getMessage().contains("fromClass")
                        && ex.getMessage().contains("fromMixIn"),
                "expected both method names in: " + ex.getMessage());
    }

    // ==================== Record + @JSONCreator factory ====================

    public record ValidatedRecord(int v) {
        @JSONCreator
        public static ValidatedRecord of(@JSONField(name = "v") int v) {
            if (v < 0) {
                throw new IllegalArgumentException("negative value: " + v);
            }
            return new ValidatedRecord(v);
        }
    }

    @Test
    public void recordFactoryTakesPrecedenceOverCanonicalCtor() {
        // Round-4 audit: record short-circuit ran before the factory lookup,
        // so a record with a validating @JSONCreator static factory silently
        // bypassed the validation. Now the factory lookup runs first and
        // records honour it like any other POJO.
        ValidatedRecord ok = JSON.parseObject("{\"v\":42}", ValidatedRecord.class);
        assertEquals(42, ok.v());

        // Factory's validation fires instead of the canonical ctor:
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> JSON.parseObject("{\"v\":-1}", ValidatedRecord.class));
        Throwable cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        assertTrue(cause.getMessage().contains("negative"), cause.getMessage());
    }

    // ==================== Generic factory with method-scoped TypeVariable ====================

    public static final class Box<T> {
        public T value;

        private Box(T v) {
            this.value = v;
        }

        @JSONCreator
        public static <T> Box<T> of(@JSONField(name = "value") T value) {
            return new Box<>(value);
        }
    }

    public static class Address {
        public String city;
    }

    @Test
    public void genericFactoryResolvesMethodTypeVariableFromTypeReference() {
        // Round-4 audit: `<T> Box<T> of(T v)` used to collapse T to Object
        // because TypeUtils.resolve walks only class-level TypeVariables.
        // The factory now unifies its return type against the caller's
        // TypeReference to bind method-scoped T.
        Box<Address> box = JSON.parseObject("{\"value\":{\"city\":\"SF\"}}",
                new TypeReference<Box<Address>>() {});
        assertNotNull(box.value);
        assertInstanceOf(Address.class, box.value,
                "expected Address, got " + (box.value == null ? "null" : box.value.getClass().getName()));
        assertEquals("SF", box.value.city);
    }

    // Round-5: bounded TypeVariable `<T extends Bean>` erases paramTypes[i]
    // to Bean.class, not Object.class — the R4 narrowing guard only matched
    // Object.class so fieldClass stayed Bean even when the target bound T
    // to a more specific SubBean, dropping SubBean-only fields.

    public static class Bean {
        public String name;
    }

    public static class SubBean extends Bean {
        public int age;
    }

    public static final class BoundedBox<T extends Bean> {
        public T value;

        private BoundedBox(T v) {
            this.value = v;
        }

        @JSONCreator
        public static <T extends Bean> BoundedBox<T> of(@JSONField(name = "value") T value) {
            return new BoundedBox<>(value);
        }
    }

    @Test
    public void boundedTypeVariableResolvesToActualSubtype() {
        BoundedBox<SubBean> box = JSON.parseObject(
                "{\"value\":{\"name\":\"n\",\"age\":3}}",
                new TypeReference<BoundedBox<SubBean>>() {});
        assertNotNull(box.value);
        assertInstanceOf(SubBean.class, box.value,
                "expected SubBean, got " + (box.value == null ? "null" : box.value.getClass().getName()));
        assertEquals("n", box.value.name);
        // Subtype-only field must survive the narrowing:
        assertEquals(3, ((SubBean) box.value).age);
    }

    // Round-6: F-bounded TypeVariable `<T extends Comparable<T>>` caused
    // infinite recursion in TypeUtils.resolve — T's bound is Comparable<T>,
    // resolving Comparable's type args recurses back into T → StackOverflow.
    // Idiomatic Jackson-migration pattern (Enum<E> shape). Fix: visited
    // set breaks cycles by falling back to the raw bound.

    public static final class RecurBox<T extends Comparable<T>> {
        public T value;

        private RecurBox(T v) {
            this.value = v;
        }

        @JSONCreator
        public static <T extends Comparable<T>> RecurBox<T> of(@JSONField(name = "value") T value) {
            return new RecurBox<>(value);
        }
    }

    @Test
    public void fBoundedTypeVariableDoesNotStackOverflow() {
        // Pre-fix: StackOverflowError after ~1024 frames.
        // Post-fix: raw-bound erasure (Comparable) kicks in on cycle.
        RecurBox<?> box = JSON.parseObject("{\"value\":\"hello\"}", RecurBox.class);
        assertNotNull(box);
    }

    // ==================== R8: mix-in factory is metadata-only ====================

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
            // Pre-fix: invoked directly and returned null.
            return null;
        }
    }

    @Test
    public void mixInFactoryBodyNotInvokedTargetMethodIsCalled() {
        // Mix-in annotations are metadata — the mix-in's factory body is
        // typically a stub. Pre-fix: factory.invoke(null, values) ran the
        // mix-in's `return null` and produced a silent null result. Post-fix:
        // the resolver swaps to the same-signature method on the target class.
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(MoneyTarget.class, MoneyMixIn.class)
                .build();
        MoneyTarget m = mapper.readValue("{\"cents\":1234}", MoneyTarget.class);
        assertNotNull(m, "mix-in factory must delegate to target's method, not invoke mix-in body");
        assertEquals(1234L, m.cents);
    }

    public abstract static class OrphanMixIn {
        @JSONCreator
        public static MoneyTarget createMismatch(@JSONField(name = "v") long v) {
            // Pre-fix path ran this body and returned null to the user — silent
            // data loss. Post-fix we detect the orphan during reader creation.
            return null;
        }
    }

    @Test
    public void mixInFactoryWithoutTargetCounterpartDoesNotInvokeStub() {
        // Target has no createMismatch(long), so we can't route through it.
        // The direct ObjectReaderCreator call surfaces the diagnostic; the
        // mapper catches and falls back to default reflection-based handling
        // without running the mix-in's stub body (pre-existing swallow pattern
        // documented in R7/R8 reports).
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator.createObjectReader(
                        MoneyTarget.class, OrphanMixIn.class));
        assertTrue(ex.getMessage().contains("createMismatch")
                        || ex.getMessage().contains("no matching static method"),
                ex.getMessage());
    }
}

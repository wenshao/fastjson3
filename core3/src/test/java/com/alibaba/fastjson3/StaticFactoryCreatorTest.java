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
}

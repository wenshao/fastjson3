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
}

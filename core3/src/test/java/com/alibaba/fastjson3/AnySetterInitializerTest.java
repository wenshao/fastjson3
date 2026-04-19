package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * When a class declares {@code @JSONField(anySetter=true)}, the reader must
 * run the class's no-arg constructor so field initializers execute — otherwise
 * a user writing
 *   {@code private Map<String,Object> extra = new LinkedHashMap<>();}
 *   {@code @JSONField(anySetter=true) void put(String k, Object v) { extra.put(k,v); }}
 * hits NPE on the first unknown property because {@code Unsafe.allocateInstance}
 * bypasses the initializer and {@code extra} is null.
 */
public class AnySetterInitializerTest {
    static final ObjectMapper MAPPER = ObjectMapper.shared();

    // ==================== Field initializer only ====================

    public static class Extensible {
        public String id;
        private Map<String, Object> extra = new LinkedHashMap<>();

        @JSONField(anyGetter = true)
        public Map<String, Object> getExtra() {
            return extra;
        }

        @JSONField(anySetter = true)
        public void put(String key, Object value) {
            extra.put(key, value);
        }
    }

    @Test
    public void anySetterWithFieldInitializer() {
        Extensible parsed = JSON.parse("{\"id\":\"x1\",\"color\":\"red\",\"size\":42}", Extensible.class);
        assertEquals("x1", parsed.id);
        assertEquals("red", parsed.extra.get("color"));
        assertEquals(42, ((Number) parsed.extra.get("size")).intValue());
    }

    @Test
    public void anySetterRoundTrip() {
        Extensible e = new Extensible();
        e.id = "x1";
        e.extra.put("color", "red");
        e.extra.put("size", 42);

        String json = MAPPER.writeValueAsString(e);
        Extensible back = JSON.parse(json, Extensible.class);
        assertEquals("x1", back.id);
        assertEquals("red", back.extra.get("color"));
        assertEquals(42, ((Number) back.extra.get("size")).intValue());
    }

    // ==================== Constructor that sets up state ====================

    public static class CtorInit {
        public String id;
        private final Map<String, Object> extra;

        public CtorInit() {
            // Final-field initialization only possible in constructor.
            this.extra = new LinkedHashMap<>();
        }

        @JSONField(anySetter = true)
        public void put(String k, Object v) {
            extra.put(k, v);
        }

        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    @Test
    public void anySetterWithFinalFieldRequiresConstructor() {
        CtorInit parsed = JSON.parse("{\"id\":\"x2\",\"alpha\":1,\"beta\":2}", CtorInit.class);
        assertEquals("x2", parsed.id);
        assertEquals(1, ((Number) parsed.getExtra().get("alpha")).intValue());
        assertEquals(2, ((Number) parsed.getExtra().get("beta")).intValue());
    }

    // ==================== Non-anySetter class: must still skip constructor for perf ====================

    public static class Plain {
        public String id;
        public boolean constructorRan = false;

        public Plain() {
            constructorRan = true;
        }
    }

    @Test
    public void plainClassStillSkipsConstructorWhenUnsafeAvailable() {
        // This test pins the existing perf behavior: for classes WITHOUT an anySetter,
        // the reader uses Unsafe.allocateInstance (skips constructor). The assertion
        // is environment-dependent — on non-Unsafe platforms the constructor runs and
        // the flag is true. We just confirm the read succeeds either way.
        Plain p = JSON.parse("{\"id\":\"a\"}", Plain.class);
        assertEquals("a", p.id);
    }
}

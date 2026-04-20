package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnyGetterSetterTest {
    static final ObjectMapper MAPPER = ObjectMapper.shared();

    // ==================== @JSONField(anyGetter=true) ====================

    public static class AnyGetterBean {
        private String name;
        private final Map<String, Object> extra = new LinkedHashMap<>();

        public AnyGetterBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JSONField(anyGetter = true)
        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    @Test
    public void testAnyGetterSerialize() {
        AnyGetterBean bean = new AnyGetterBean();
        bean.setName("test");
        bean.extra.put("dynamicKey", "dynamicValue");
        bean.extra.put("count", 42);

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"test\""), json);
        assertTrue(json.contains("\"dynamicKey\":\"dynamicValue\""), json);
        assertTrue(json.contains("\"count\":42"), json);
    }

    @Test
    public void testAnyGetterEmpty() {
        AnyGetterBean bean = new AnyGetterBean();
        bean.setName("test");
        // extra is empty

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"test\""), json);
        // Should not crash, just no extra fields
    }

    // ==================== @JSONField(anySetter=true) ====================

    public static class AnySetterBean {
        private String name;
        private Map<String, Object> extra;

        public AnySetterBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JSONField(anySetter = true)
        public void putExtra(String key, Object value) {
            if (extra == null) {
                extra = new LinkedHashMap<>();
            }
            extra.put(key, value);
        }

        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    @Test
    public void testAnySetterDeserialize() {
        AnySetterBean bean = MAPPER.readValue(
                "{\"name\":\"test\",\"unknown1\":\"val1\",\"unknown2\":42}", AnySetterBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.getName());
        assertEquals("val1", bean.getExtra().get("unknown1"));
        assertEquals(42, bean.getExtra().get("unknown2"));
    }

    @Test
    public void testAnySetterFromBytes() {
        byte[] json = "{\"name\":\"test\",\"extra_key\":\"extra_val\"}"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        AnySetterBean bean = MAPPER.readValue(json, AnySetterBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.getName());
        assertEquals("extra_val", bean.getExtra().get("extra_key"));
    }

    // ==================== Combined anyGetter + anySetter ====================

    public static class DynamicBean {
        private String id;
        private Map<String, Object> properties;

        public DynamicBean() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @JSONField(anyGetter = true)
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JSONField(anySetter = true)
        public void setProperty(String key, Object value) {
            if (properties == null) {
                properties = new LinkedHashMap<>();
            }
            properties.put(key, value);
        }
    }

    @Test
    public void testRoundtripDynamicProperties() {
        DynamicBean original = new DynamicBean();
        original.setId("123");
        original.setProperty("color", "red");
        original.setProperty("size", 42);

        String json = MAPPER.writeValueAsString(original);
        assertTrue(json.contains("\"id\":\"123\""), json);
        assertTrue(json.contains("\"color\":\"red\""), json);

        DynamicBean parsed = MAPPER.readValue(json, DynamicBean.class);
        assertEquals("123", parsed.getId());
        assertEquals("red", parsed.getProperties().get("color"));
        assertEquals(42, parsed.getProperties().get("size"));
    }

    // ==================== anySetter takes priority over ErrorOnUnknownProperties ====================

    @Test
    public void testAnySetterOverridesErrorOnUnknown() {
        ObjectMapper strictMapper = ObjectMapper.builder()
                .enableRead(ReadFeature.ErrorOnUnknownProperties)
                .build();

        // Without anySetter, unknown props would throw
        // With anySetter, unknown props are routed to anySetter
        AnySetterBean bean = strictMapper.readValue(
                "{\"name\":\"test\",\"unknown\":\"val\"}", AnySetterBean.class);
        assertEquals("val", bean.getExtra().get("unknown"));
    }

    // ==================== Jackson @JsonAnyGetter ====================
    // Round-3 usability audit F5: the writer emitted BOTH the getter's nested
    // {"extras":{...}} slot AND the flattened inner keys, producing duplicate
    // payload. The getter must be suppressed when @JsonAnyGetter is in play,
    // mirroring the existing @JSONField(anyGetter=true) branch.

    public static class JacksonAnyGetterBean {
        public String id = "X";
        private final Map<String, Object> extras = new LinkedHashMap<>();

        public JacksonAnyGetterBean() {
            extras.put("k1", "v1");
            extras.put("k2", 99);
        }

        @com.fasterxml.jackson.annotation.JsonAnyGetter
        public Map<String, Object> getExtras() {
            return extras;
        }
    }

    @Test
    public void jacksonAnyGetterSuppressesWrappingField() {
        ObjectMapper jacksonMapper = ObjectMapper.builder()
                .useJacksonAnnotation(true)
                .build();
        String json = jacksonMapper.writeValueAsString(new JacksonAnyGetterBean());
        // Must NOT contain the getter's property name as a top-level key.
        assertFalse(json.contains("\"extras\":"),
                "getter name must be suppressed under @JsonAnyGetter: " + json);
        // Must contain the flattened inner keys.
        assertTrue(json.contains("\"k1\":\"v1\""), json);
        assertTrue(json.contains("\"k2\":99"), json);
        assertTrue(json.contains("\"id\":\"X\""), json);
    }

    @Test
    public void jacksonAnyGetterIgnoredWithoutOptIn() {
        // Without useJacksonAnnotation, @JsonAnyGetter is not interpreted —
        // the getter produces a regular "extras":{...} slot. Documenting the
        // opt-in contract so the suppression test above isn't misread.
        String json = JSON.toJSONString(new JacksonAnyGetterBean());
        assertTrue(json.contains("\"extras\":"), json);
    }

    // Round-1 audit: @JsonAnyGetter on a non-Map getter must NOT suppress
    // the field. findAnyGetterMethod rejects non-Map returns, so suppressing
    // at the getter-collection step too would silently drop the field.

    public static class NonMapAnyGetterBean {
        public String id = "Z";
        @com.fasterxml.jackson.annotation.JsonAnyGetter
        public String getLabel() {
            return "labelval";
        }
    }

    @Test
    public void nonMapJacksonAnyGetterDoesNotSuppressField() {
        ObjectMapper jacksonMapper = ObjectMapper.builder()
                .useJacksonAnnotation(true)
                .build();
        String json = jacksonMapper.writeValueAsString(new NonMapAnyGetterBean());
        assertTrue(json.contains("\"id\":\"Z\""), json);
        // Non-Map @JsonAnyGetter is a user error; field must still serialise
        // as a regular getter rather than vanish.
        assertTrue(json.contains("\"label\":\"labelval\""),
                "non-Map @JsonAnyGetter must not suppress the field: " + json);
    }

    // Round-2 audit: the fj3-native @JSONField(anyGetter=true) branch had
    // the same latent drop-data bug that round 1 fixed on the Jackson branch.

    public static class NonMapNativeAnyGetterBean {
        public String id = "N";

        @JSONField(anyGetter = true)
        public String getLabel() {
            return "nativeval";
        }
    }

    @Test
    public void nonMapNativeAnyGetterDoesNotSuppressField() {
        String json = MAPPER.writeValueAsString(new NonMapNativeAnyGetterBean());
        assertTrue(json.contains("\"id\":\"N\""), json);
        assertTrue(json.contains("\"label\":\"nativeval\""),
                "non-Map @JSONField(anyGetter=true) must not suppress the field: " + json);
    }

    // Round-4: record path had no @JsonAnyGetter wiring — silent no-op.

    public record JacksonAnyGetterRecord(String id,
                                         @com.fasterxml.jackson.annotation.JsonAnyGetter Map<String, Object> extras) {}

    @Test
    public void recordJacksonAnyGetterFlattens() {
        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("k1", "v1");
        extras.put("k2", 99);
        JacksonAnyGetterRecord r = new JacksonAnyGetterRecord("X", extras);

        ObjectMapper jackson = ObjectMapper.builder().useJacksonAnnotation(true).build();
        String json = jackson.writeValueAsString(r);
        assertFalse(json.contains("\"extras\":"),
                "record accessor must be suppressed under @JsonAnyGetter: " + json);
        assertTrue(json.contains("\"id\":\"X\""), json);
        assertTrue(json.contains("\"k1\":\"v1\""), json);
        assertTrue(json.contains("\"k2\":99"), json);
    }

    public record NativeAnyGetterRecord(String id,
                                        @JSONField(anyGetter = true) Map<String, Object> extras) {}

    @Test
    public void recordNativeAnyGetterFlattens() {
        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("color", "red");
        NativeAnyGetterRecord r = new NativeAnyGetterRecord("R", extras);

        String json = MAPPER.writeValueAsString(r);
        assertFalse(json.contains("\"extras\":"), json);
        assertTrue(json.contains("\"id\":\"R\""), json);
        assertTrue(json.contains("\"color\":\"red\""), json);
    }

    // Round-4: public field + @JsonAnyGetter getter same-named produced
    // a duplicate slot; field loop now honours the getter suppression.

    public static class PublicFieldWithAnyGetter {
        public String id = "F";
        public Map<String, Object> extras = new LinkedHashMap<>();

        public PublicFieldWithAnyGetter() {
            extras.put("k", "v");
        }

        @com.fasterxml.jackson.annotation.JsonAnyGetter
        public Map<String, Object> getExtras() {
            return extras;
        }
    }

    @Test
    public void publicFieldWithJacksonAnyGetterNoDuplicateSlot() {
        ObjectMapper jackson = ObjectMapper.builder().useJacksonAnnotation(true).build();
        String json = jackson.writeValueAsString(new PublicFieldWithAnyGetter());
        assertFalse(json.contains("\"extras\":"),
                "field loop must honour the getter suppression: " + json);
        assertTrue(json.contains("\"k\":\"v\""), json);
        assertTrue(json.contains("\"id\":\"F\""), json);
    }

    // Round-4: mix-in @JsonAnyGetter not recognised.

    public static class MixInTarget {
        public String id = "M";
        public Map<String, Object> extras = new LinkedHashMap<>();

        public MixInTarget() {
            extras.put("x", 1);
        }

        public Map<String, Object> getExtras() {
            return extras;
        }
    }

    public abstract static class MixInSource {
        @com.fasterxml.jackson.annotation.JsonAnyGetter
        public abstract Map<String, Object> getExtras();
    }

    @Test
    public void mixInJacksonAnyGetterRecognised() {
        ObjectMapper jackson = ObjectMapper.builder()
                .useJacksonAnnotation(true)
                .addMixIn(MixInTarget.class, MixInSource.class)
                .build();
        String json = jackson.writeValueAsString(new MixInTarget());
        assertFalse(json.contains("\"extras\":"),
                "mix-in @JsonAnyGetter must be recognised: " + json);
        assertTrue(json.contains("\"x\":1"), json);
        assertTrue(json.contains("\"id\":\"M\""), json);
    }

    // Round-4: two @JsonAnyGetter methods — reject at construction.

    public static class TwoAnyGetters {
        public String id = "T";

        @com.fasterxml.jackson.annotation.JsonAnyGetter
        public Map<String, Object> extrasA() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("a", 1);
            return m;
        }

        @com.fasterxml.jackson.annotation.JsonAnyGetter
        public Map<String, Object> extrasB() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("b", 2);
            return m;
        }
    }

    @Test
    public void multipleAnyGetterMethodsRejected() {
        ObjectMapper jackson = ObjectMapper.builder().useJacksonAnnotation(true).build();
        Throwable ex = assertThrows(RuntimeException.class,
                () -> jackson.writeValueAsString(new TwoAnyGetters()));
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        assertTrue(root.getMessage().contains("multiple"),
                "expected 'multiple' in: " + root.getMessage());
        assertTrue(root.getMessage().contains("extrasA") && root.getMessage().contains("extrasB"),
                "expected both method names in: " + root.getMessage());
    }

    // Round-5: mix-in inheritance for Jackson @JsonAnyGetter. Previous
    // `mixIn.getDeclaredMethod` missed the annotation when it lived on a
    // parent mix-in class — a common Jackson pattern:
    //   class UserMixIn extends BaseMixIn
    //   BaseMixIn: @JsonAnyGetter public abstract Map<...> getExtras();

    public abstract static class BaseMixInWithAnyGetter {
        @com.fasterxml.jackson.annotation.JsonAnyGetter
        public abstract Map<String, Object> getExtras();
    }

    public abstract static class UserMixInExtendsBase extends BaseMixInWithAnyGetter {
    }

    public static class InheritedMixInTarget {
        public String name = "bob";
        public Map<String, Object> extras = new LinkedHashMap<>();

        public InheritedMixInTarget() {
            extras.put("k", "v");
        }

        public Map<String, Object> getExtras() {
            return extras;
        }
    }

    @Test
    public void mixInJacksonAnyGetterInheritedFromParent() {
        ObjectMapper jackson = ObjectMapper.builder()
                .useJacksonAnnotation(true)
                .addMixIn(InheritedMixInTarget.class, UserMixInExtendsBase.class)
                .build();
        String json = jackson.writeValueAsString(new InheritedMixInTarget());
        assertFalse(json.contains("\"extras\":"),
                "inherited mix-in @JsonAnyGetter must be recognised: " + json);
        assertTrue(json.contains("\"k\":\"v\""), json);
        assertTrue(json.contains("\"name\":\"bob\""), json);
    }
}

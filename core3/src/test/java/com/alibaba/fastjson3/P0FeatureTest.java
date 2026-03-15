package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for P0 feature implementations:
 * 1. WriteNullXxxAsYyy
 * 2. PrettyFormat
 * 3. ReferenceDetection (circular reference)
 * 4. JSONException offset
 * 5. @JSONField(unwrapped) removed
 */
public class P0FeatureTest {

    // ==================== WriteNullXxxAsYyy ====================

    public static class NullFieldBean {
        public String name;
        public Integer age;
        public List<String> tags;
        public Boolean active;
        public Double score;
    }

    @Test
    public void testWriteNullStringAsEmpty() {
        NullFieldBean bean = new NullFieldBean();
        bean.name = null;
        bean.age = 25;

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNullStringAsEmpty)
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"\""), "null string should be written as empty: " + json);
        assertTrue(json.contains("\"age\":25"), json);
        // tags, active, score are null and not WriteNulls, so should be omitted
        assertFalse(json.contains("\"tags\""), json);
    }

    @Test
    public void testWriteNullListAsEmpty() {
        NullFieldBean bean = new NullFieldBean();
        bean.tags = null;
        bean.name = "test";

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNullListAsEmpty)
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"tags\":[]"), "null list should be written as []: " + json);
    }

    @Test
    public void testWriteNullNumberAsZero() {
        NullFieldBean bean = new NullFieldBean();
        bean.age = null;
        bean.score = null;
        bean.name = "test";

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNullNumberAsZero)
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"age\":0"), "null int should be written as 0: " + json);
        assertTrue(json.contains("\"score\":0"), "null double should be written as 0: " + json);
    }

    @Test
    public void testWriteNullBooleanAsFalse() {
        NullFieldBean bean = new NullFieldBean();
        bean.active = null;
        bean.name = "test";

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNullBooleanAsFalse)
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"active\":false"), "null boolean should be written as false: " + json);
    }

    @Test
    public void testWriteNullsCombined() {
        NullFieldBean bean = new NullFieldBean();
        // all fields null

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(
                        WriteFeature.WriteNullStringAsEmpty,
                        WriteFeature.WriteNullListAsEmpty,
                        WriteFeature.WriteNullNumberAsZero,
                        WriteFeature.WriteNullBooleanAsFalse
                )
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"\""), json);
        assertTrue(json.contains("\"tags\":[]"), json);
        assertTrue(json.contains("\"age\":0"), json);
        assertTrue(json.contains("\"score\":0"), json);
        assertTrue(json.contains("\"active\":false"), json);
    }

    @Test
    public void testWriteNullsOverridesSpecific() {
        // WriteNulls takes precedence — when WriteNulls is on, null is written as null
        NullFieldBean bean = new NullFieldBean();
        bean.name = null;

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNulls, WriteFeature.WriteNullStringAsEmpty)
                .build();
        String json = mapper.writeValueAsString(bean);
        // WriteNulls is checked first, so null is written as null
        assertTrue(json.contains("\"name\":null"), "WriteNulls should take precedence: " + json);
    }

    // ==================== PrettyFormat ====================

    @Test
    public void testPrettyFormatObjectMapper() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.PrettyFormat)
                .build();
        JSONObject obj = new JSONObject();
        obj.put("name", "test");
        obj.put("age", 25);
        String json = mapper.writeValueAsString(obj);
        assertTrue(json.contains("\n"), "pretty format should have newlines: " + json);
        assertTrue(json.contains("  "), "pretty format should have indentation: " + json);
        assertTrue(json.contains(": "), "pretty format should have space after colon: " + json);
    }

    @Test
    public void testPrettyFormatEmptyObject() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.PrettyFormat)
                .build();
        String json = mapper.writeValueAsString(JSON.object());
        assertEquals("{}", json);
    }

    @Test
    public void testPrettyFormatNested() {
        JSONObject obj = JSON.object();
        obj.put("name", "test");
        obj.put("address", JSON.object("city", "Beijing"));

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.PrettyFormat)
                .build();
        String json = mapper.writeValueAsString(obj);
        assertTrue(json.contains("    \"city\""), "nested object should have 4 spaces indent: " + json);
    }

    @Test
    public void testPrettyFormatArray() {
        JSONArray arr = JSON.array(1, 2, 3);

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.PrettyFormat)
                .build();
        String json = mapper.writeValueAsString(arr);
        assertTrue(json.contains("[\n"), json);
        assertTrue(json.contains("  1"), json);
    }

    @Test
    public void testPrettyFormatStaticAPI() {
        String json = JSON.toJSONString(JSON.object("key", "value"), WriteFeature.PrettyFormat);
        assertTrue(json.contains("\n"), json);
        assertTrue(json.contains(": "), json);
    }

    @Test
    public void testPrettyFormatValueWriter() {
        ObjectMapper mapper = ObjectMapper.shared();
        String json = mapper.writer()
                .with(WriteFeature.PrettyFormat)
                .writeValueAsString(JSON.object("k", "v"));
        assertTrue(json.contains("\n"), json);
    }

    @Test
    public void testPrettyFormatStringWithQuotes() {
        String json = JSON.toJSONString(
                JSON.object("msg", "he said \"hello\""),
                WriteFeature.PrettyFormat
        );
        // Escaped quotes inside string should not break pretty printing
        assertTrue(json.contains("\"he said \\\"hello\\\"\""), json);
        // Should still have proper formatting
        assertTrue(json.contains("\n"), json);
    }

    // ==================== ReferenceDetection ====================

    public static class SelfRefBean {
        public String name;
        public SelfRefBean self;
    }

    @Test
    public void testCircularReferenceDetected() {
        SelfRefBean bean = new SelfRefBean();
        bean.name = "a";
        bean.self = bean; // circular!

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.ReferenceDetection)
                .build();

        assertThrows(JSONException.class, () -> mapper.writeValueAsString(bean));
    }

    @Test
    public void testNoCircularWithoutDetection() {
        SelfRefBean bean = new SelfRefBean();
        bean.name = "a";
        SelfRefBean other = new SelfRefBean();
        other.name = "b";
        bean.self = other; // not circular

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.ReferenceDetection)
                .build();

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"a\""), json);
        assertTrue(json.contains("\"name\":\"b\""), json);
    }

    @Test
    public void testCircularReferenceViaValueWriter() {
        SelfRefBean bean = new SelfRefBean();
        bean.name = "a";
        bean.self = bean;

        ObjectMapper mapper = ObjectMapper.shared();
        assertThrows(JSONException.class, () ->
                mapper.writer().with(WriteFeature.ReferenceDetection).writeValueAsString(bean));
    }

    @Test
    public void testCircularReferenceViaValueWriterBytes() {
        SelfRefBean bean = new SelfRefBean();
        bean.name = "a";
        bean.self = bean;

        ObjectMapper mapper = ObjectMapper.shared();
        assertThrows(JSONException.class, () ->
                mapper.writer().with(WriteFeature.ReferenceDetection).writeValueAsBytes(bean));
    }

    @Test
    public void testCircularReferenceJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("name", "test");
        obj.put("self", obj); // circular!

        assertThrows(JSONException.class, () ->
                JSON.toJSONString(obj, WriteFeature.ReferenceDetection));
    }

    @Test
    public void testCircularReferenceJSONArray() {
        JSONArray arr = new JSONArray();
        arr.add("test");
        arr.add(arr); // circular!

        assertThrows(JSONException.class, () ->
                JSON.toJSONString(arr, WriteFeature.ReferenceDetection));
    }

    @Test
    public void testCircularReferenceMutual() {
        SelfRefBean a = new SelfRefBean();
        SelfRefBean b = new SelfRefBean();
        a.name = "a";
        a.self = b;
        b.name = "b";
        b.self = a; // A→B→A

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.ReferenceDetection)
                .build();
        assertThrows(JSONException.class, () -> mapper.writeValueAsString(a));
    }

    public static class GenericSelfRef {
        public String name;
        public Object self; // declared as Object, not typed
    }

    @Test
    public void testCircularReferenceGenericField() {
        GenericSelfRef bean = new GenericSelfRef();
        bean.name = "a";
        bean.self = bean; // circular via Object field

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.ReferenceDetection)
                .build();
        assertThrows(JSONException.class, () -> mapper.writeValueAsString(bean));
    }

    public static class BeanWithContainers {
        public String name;
        public Object map;
        public Object list;
    }

    @Test
    public void testReferenceDetectionNoFalsePositiveOnContainers() {
        // Map/List via Object field should NOT cause false circular reference
        BeanWithContainers bean = new BeanWithContainers();
        bean.name = "test";
        bean.map = java.util.Map.of("k", "v");
        bean.list = java.util.List.of(1, 2, 3);

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.ReferenceDetection)
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"test\""), json);
        assertTrue(json.contains("\"k\":\"v\""), json);
    }

    public static class BeanWithSerializableField {
        public String name;
        public java.io.Serializable data; // non-container declared type, may hold Map at runtime
    }

    @Test
    public void testReferenceDetectionNoFalsePositiveOnPolymorphicContainer() {
        BeanWithSerializableField bean = new BeanWithSerializableField();
        bean.name = "test";
        bean.data = new java.util.HashMap<>(java.util.Map.of("key", "value"));

        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.ReferenceDetection)
                .build();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"test\""), json);
        assertTrue(json.contains("\"key\":\"value\""), json);
    }

    // ==================== JSONException offset ====================

    @Test
    public void testJSONExceptionOffset() {
        JSONException ex = new JSONException("error at position", 42);
        assertEquals(42, ex.getOffset());
        assertEquals("error at position", ex.getMessage());
    }

    @Test
    public void testJSONExceptionDefaultOffset() {
        JSONException ex = new JSONException("error");
        assertEquals(-1, ex.getOffset());
    }

    // ==================== @JSONField(unwrapped) removed ====================

    @Test
    public void testUnwrappedAttributeRemoved() throws Exception {
        // Verify the unwrapped attribute no longer exists on @JSONField
        var methods = JSONField.class.getDeclaredMethods();
        for (var method : methods) {
            assertNotEquals("unwrapped", method.getName(),
                    "@JSONField.unwrapped should be removed (not yet implemented)");
        }
    }
}

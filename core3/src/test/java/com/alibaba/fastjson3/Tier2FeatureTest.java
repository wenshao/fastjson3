package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;
import com.alibaba.fastjson3.filter.BeforeFilter;
import com.alibaba.fastjson3.filter.AfterFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Tier 2 features (medium/low complexity).
 */
class Tier2FeatureTest {

    // ==================== 1. @JSONType(typeKey/typeName) write ====================

    @JSONType(typeName = "dog", typeKey = "kind")
    public static class Dog {
        public String name = "Rex";
    }

    @Test
    void writeClassName_withTypeName() {
        Dog dog = new Dog();
        String json = JSON.toJSONString(dog, WriteFeature.WriteClassName);
        assertTrue(json.contains("\"kind\""), "Should use typeKey 'kind': " + json);
        assertTrue(json.contains("\"dog\""), "Should use typeName 'dog': " + json);
        assertFalse(json.contains("Tier2FeatureTest"), "Should NOT use class name: " + json);
    }

    @JSONType(typeName = "cat")
    public static class Cat {
        public String name = "Whiskers";
    }

    @Test
    void writeClassName_typeNameOnly() {
        Cat cat = new Cat();
        String json = JSON.toJSONString(cat, WriteFeature.WriteClassName);
        assertTrue(json.contains("\"@type\""), "Should default to @type key: " + json);
        assertTrue(json.contains("\"cat\""), "Should use typeName: " + json);
    }

    @Test
    void writeClassName_noAnnotation_usesClassName() {
        var obj = new JSONObject();
        obj.put("x", 1);
        // JSONObject won't go through ReflectObjectWriter, but a plain POJO will
        String json = JSON.toJSONString(new SimplePojo(), WriteFeature.WriteClassName);
        assertTrue(json.contains("SimplePojo"), "Should use class name: " + json);
    }

    public static class SimplePojo {
        public int x = 1;
    }

    // ==================== 2. BeforeFilter ====================

    @Test
    void beforeFilter() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addBeforeFilter((generator, object) -> {
                    generator.writeName("_version");
                    generator.writeInt32(2);
                })
                .build();
        String json = mapper.writeValueAsString(new SimplePojo());
        assertTrue(json.contains("\"_version\""), "Should have injected field: " + json);
        assertTrue(json.contains("2"), json);
    }

    // ==================== 3. AfterFilter ====================

    @Test
    void afterFilter() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addAfterFilter((generator, object) -> {
                    generator.writeName("_ts");
                    generator.writeInt64(1700000000000L);
                })
                .build();
        String json = mapper.writeValueAsString(new SimplePojo());
        assertTrue(json.contains("\"_ts\""), "Should have appended field: " + json);
        assertTrue(json.contains("1700000000000"), json);
    }

    @Test
    void beforeAndAfterFilter_combined() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addBeforeFilter((g, o) -> { g.writeName("_start"); g.writeBool(true); })
                .addAfterFilter((g, o) -> { g.writeName("_end"); g.writeBool(true); })
                .build();
        String json = mapper.writeValueAsString(new SimplePojo());
        assertTrue(json.contains("\"_start\""), json);
        assertTrue(json.contains("\"_end\""), json);
        // _start should come before x, _end after x
        int startIdx = json.indexOf("_start");
        int xIdx = json.indexOf("\"x\"");
        int endIdx = json.indexOf("_end");
        assertTrue(startIdx < xIdx, "_start should be before x: " + json);
        assertTrue(xIdx < endIdx, "_end should be after x: " + json);
    }

    // ==================== 4. ContextValueFilter interface ====================

    @Test
    void contextValueFilter_exists() {
        com.alibaba.fastjson3.filter.ContextValueFilter filter =
                (fieldClass, fieldName, source, name, value) -> value;
        assertNotNull(filter);
    }

    // ==================== 5. @JSONField(deserializeFeatures) ====================

    public static class TrimBean {
        @JSONField(deserializeFeatures = ReadFeature.TrimString)
        public String name;
        public String other;
    }

    @Test
    void deserializeFeatures_annotationExists() throws Exception {
        var method = JSONField.class.getDeclaredMethod("deserializeFeatures");
        assertEquals(ReadFeature[].class, method.getReturnType());
    }

    @Test
    void fieldReader_fieldFeatures_field() {
        // Verify FieldReader has fieldFeatures field
        var fr = new com.alibaba.fastjson3.reader.FieldReader(
                "test", null, String.class, String.class, 0, null, false, null, null,
                null, null, null, ReadFeature.of(ReadFeature.TrimString));
        assertEquals(ReadFeature.TrimString.mask, fr.fieldFeatures);
    }

    // ==================== 6. JSONObject.toJavaObject(TypeReference) ====================

    @Test
    void jsonObject_toJavaObject_typeReference() {
        JSONObject obj = JSON.parseObject("{\"name\":\"test\",\"x\":1}");
        SimplePojo pojo = obj.toJavaObject(new TypeReference<SimplePojo>() {});
        assertEquals(1, pojo.x);
    }

    // ==================== Filter interface completeness ====================

    @Test
    void allFilterInterfaces_exist() {
        assertNotNull(com.alibaba.fastjson3.filter.PropertyFilter.class);
        assertNotNull(com.alibaba.fastjson3.filter.PropertyPreFilter.class);
        assertNotNull(com.alibaba.fastjson3.filter.ValueFilter.class);
        assertNotNull(com.alibaba.fastjson3.filter.NameFilter.class);
        assertNotNull(com.alibaba.fastjson3.filter.LabelFilter.class);
        assertNotNull(com.alibaba.fastjson3.filter.AutoTypeFilter.class);
        assertNotNull(com.alibaba.fastjson3.filter.BeforeFilter.class);
        assertNotNull(com.alibaba.fastjson3.filter.AfterFilter.class);
        assertNotNull(com.alibaba.fastjson3.filter.ContextValueFilter.class);
    }
}

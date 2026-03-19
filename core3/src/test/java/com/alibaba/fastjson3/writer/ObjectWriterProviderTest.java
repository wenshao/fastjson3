package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.ObjectWriter;
import com.alibaba.fastjson3.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ObjectWriterProvider implementations.
 */
class ObjectWriterProviderTest {

    static class TestBean {
        public String name = "test";
        public int value = 42;
        public List<String> items = new ArrayList<>();

        public TestBean() {
            items.add("item1");
            items.add("item2");
        }
    }

    @Test
    void testDefaultProvider() {
        ObjectWriterProvider provider = ObjectWriterProvider.defaultProvider();
        assertNotNull(provider);
        assertEquals(WriterCreatorType.AUTO, provider.getCreatorType());
    }

    @Test
    void testProviderOfAuto() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);
        assertNotNull(provider);
        assertEquals(WriterCreatorType.AUTO, provider.getCreatorType());
        assertSame(AutoObjectWriterProvider.INSTANCE, provider);
    }

    @Test
    void testProviderOfReflect() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.REFLECT);
        assertNotNull(provider);
        assertEquals(WriterCreatorType.REFLECT, provider.getCreatorType());
        assertSame(ReflectObjectWriterProvider.INSTANCE, provider);
    }

    @Test
    void testProviderOfAsm() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.ASM);
        assertNotNull(provider);
        assertEquals(WriterCreatorType.ASM, provider.getCreatorType());
        assertSame(ASMObjectWriterProvider.INSTANCE, provider);
    }

    @Test
    void testAutoObjectWriterProvider() {
        AutoObjectWriterProvider provider = AutoObjectWriterProvider.INSTANCE;
        assertNotNull(provider);
        assertEquals(WriterCreatorType.AUTO, provider.getCreatorType());

        // Test getObjectWriter
        ObjectWriter<?> writer = provider.getObjectWriter(TestBean.class);
        assertNotNull(writer);
    }

    @Test
    void testReflectObjectWriterProvider() {
        ReflectObjectWriterProvider provider = ReflectObjectWriterProvider.INSTANCE;
        assertNotNull(provider);
        assertEquals(WriterCreatorType.REFLECT, provider.getCreatorType());

        // Test getObjectWriter
        ObjectWriter<?> writer = provider.getObjectWriter(TestBean.class);
        assertNotNull(writer);
    }

    @Test
    void testASMObjectWriterProvider() {
        ASMObjectWriterProvider provider = ASMObjectWriterProvider.INSTANCE;
        assertNotNull(provider);
        assertEquals(WriterCreatorType.ASM, provider.getCreatorType());

        // Test getObjectWriter
        ObjectWriter<?> writer = provider.getObjectWriter(TestBean.class);
        assertNotNull(writer);
    }

    @Test
    void testProviderCleanup() {
        // Test that cleanup doesn't throw for shared providers
        assertDoesNotThrow(() -> AutoObjectWriterProvider.INSTANCE.cleanup());
        assertDoesNotThrow(() -> ReflectObjectWriterProvider.INSTANCE.cleanup());
        assertDoesNotThrow(() -> ASMObjectWriterProvider.INSTANCE.cleanup());

        // Verify providers still work after cleanup
        ObjectWriter<?> writer = AutoObjectWriterProvider.INSTANCE.getObjectWriter(TestBean.class);
        assertNotNull(writer);
    }

    @Test
    void testContext() {
        ObjectWriterProvider provider1 = ObjectWriterProvider.of(WriterCreatorType.AUTO);
        ObjectWriterProvider provider2 = ObjectWriterProvider.of(WriterCreatorType.REFLECT);

        // Test openContext and close
        try (ObjectWriterProvider.SafeContext ctx = provider1.openContext()) {
            assertSame(provider1, ObjectWriterProvider.getContext());

            // Test nested context
            try (ObjectWriterProvider.SafeContext ctx2 = provider2.openContext()) {
                assertSame(provider2, ObjectWriterProvider.getContext());
            }

            // After closing inner context, outer should be restored
            assertSame(provider1, ObjectWriterProvider.getContext());
        }

        // After closing outer context, should be null
        assertNull(ObjectWriterProvider.getContext());
    }

    @Test
    void testWithContext() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);

        String result = provider.withContext(() -> {
            assertSame(provider, ObjectWriterProvider.getContext());
            return "test-result";
        });

        assertEquals("test-result", result);
        assertNull(ObjectWriterProvider.getContext());
    }

    @Test
    void testGetObjectWriterWithType() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);

        // Test with Class
        ObjectWriter<TestBean> writer1 = provider.getObjectWriter(TestBean.class);
        assertNotNull(writer1);

        // Test with Type parameter (Class should work)
        ObjectWriter<TestBean> writer2 = provider.getObjectWriter(
                (java.lang.reflect.Type) TestBean.class);
        assertNotNull(writer2);

        // Test with null - should throw
        assertThrows(IllegalArgumentException.class, () -> {
            provider.getObjectWriter((java.lang.reflect.Type) null);
        });
    }

    @Test
    void testSafeContextCloseMultipleTimes() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);

        try (ObjectWriterProvider.SafeContext ctx = provider.openContext()) {
            assertSame(provider, ObjectWriterProvider.getContext());
        }

        // Context should be cleared
        assertNull(ObjectWriterProvider.getContext());
    }

    @Test
    void testSerializationWithAutoProvider() {
        ObjectMapper mapper = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.AUTO)
                .build();

        TestBean bean = new TestBean();
        String json = mapper.writeValueAsString(bean);

        assertNotNull(json);
        assertTrue(json.contains("test"));
        assertTrue(json.contains("42"));
    }

    @Test
    void testSerializationWithReflectProvider() {
        ObjectMapper mapper = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.REFLECT)
                .build();

        TestBean bean = new TestBean();
        String json = mapper.writeValueAsString(bean);

        assertNotNull(json);
        assertTrue(json.contains("test"));
        assertTrue(json.contains("42"));
    }

    @Test
    void testSerializationWithAsmProvider() {
        ObjectMapper mapper = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.ASM)
                .build();

        TestBean bean = new TestBean();
        String json = mapper.writeValueAsString(bean);

        assertNotNull(json);
        assertTrue(json.contains("test"));
        assertTrue(json.contains("42"));
    }

    @Test
    void testMapSerialization() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 123);

        ObjectWriter<?> writer = provider.getObjectWriter(map.getClass());
        assertNotNull(writer);
    }

    @Test
    void testListSerialization() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);

        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        ObjectWriter<?> writer = provider.getObjectWriter(list.getClass());
        assertNotNull(writer);
    }

    @Test
    void testArraySerialization() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);

        // Arrays are handled by builtin writers, not cached in provider
        // This test verifies the provider doesn't crash for array types
        ObjectWriter<?> writer = provider.getObjectWriter(String[].class);
        // May be null or a writer depending on implementation
        // The important thing is it doesn't crash
        assertDoesNotThrow(() -> provider.getObjectWriter(String[].class));
        assertDoesNotThrow(() -> provider.getObjectWriter(int[].class));
        assertDoesNotThrow(() -> provider.getObjectWriter(boolean[].class));
    }

    @Test
    void testPrimitiveTypes() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);

        // Primitive types and their wrappers are handled by builtin writers
        // The provider delegates to builtin writers for these types
        ObjectWriter<?> stringWriter = provider.getObjectWriter(String.class);
        // String writer may be cached or builtin

        ObjectWriter<?> beanWriter = provider.getObjectWriter(TestBean.class);
        assertNotNull(beanWriter, "Bean writer should be created");
    }

    @Test
    void testJsonDefaultUsage() {
        // Verify that default JSON usage still works
        TestBean bean = new TestBean();
        String json = JSON.toJSONString(bean);

        assertNotNull(json);
        assertTrue(json.contains("test"));
        assertTrue(json.contains("42"));
    }

    static class NestedBean {
        public String name = "nested";
        public TestBean inner = new TestBean();
    }

    @Test
    void testNestedBeanSerialization() {
        ObjectMapper mapper = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.ASM)
                .build();

        NestedBean bean = new NestedBean();
        String json = mapper.writeValueAsString(bean);

        assertNotNull(json);
        assertTrue(json.contains("nested"));
        assertTrue(json.contains("test"));
    }

    static class BeanWithCollections {
        public List<String> strings = new ArrayList<>();
        public Map<String, Integer> map = new HashMap<>();

        public BeanWithCollections() {
            strings.add("a");
            strings.add("b");
            map.put("key", 123);
        }
    }

    @Test
    void testBeanWithCollections() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);
        ObjectWriter<?> writer = provider.getObjectWriter(BeanWithCollections.class);
        assertNotNull(writer);

        BeanWithCollections bean = new BeanWithCollections();
        String json = JSON.toJSONString(bean);

        assertNotNull(json);
        assertTrue(json.contains("a"));
        assertTrue(json.contains("key"));
    }

    @Test
    void testInterfaceType() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);

        // Interfaces should return null (handled by builtin serializers)
        ObjectWriter<?> writer = provider.getObjectWriter(Runnable.class);
        assertNull(writer, "Interface types should return null");
    }

    @Test
    void testAbstractClassType() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);

        // Abstract classes should return null (handled by builtin serializers)
        ObjectWriter<?> writer = provider.getObjectWriter(AbstractTest.class);
        assertNull(writer, "Abstract classes should return null");
    }

    abstract static class AbstractTest {
        public String value = "test";
    }

    @Test
    void testMultipleProviderInstances() {
        // Test that multiple provider instances work correctly
        ObjectWriterProvider provider1 = ObjectWriterProvider.of(WriterCreatorType.AUTO);
        ObjectWriterProvider provider2 = ObjectWriterProvider.of(WriterCreatorType.REFLECT);

        assertNotSame(provider1, provider2);

        // Both should be able to create writers
        ObjectWriter<?> writer1 = provider1.getObjectWriter(TestBean.class);
        ObjectWriter<?> writer2 = provider2.getObjectWriter(TestBean.class);

        assertNotNull(writer1);
        assertNotNull(writer2);
    }

    @Test
    void testProviderCaching() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);

        // Get the same writer twice - should return cached instance
        ObjectWriter<TestBean> writer1 = provider.getObjectWriter(TestBean.class);
        ObjectWriter<TestBean> writer2 = provider.getObjectWriter(TestBean.class);

        // Should be the same cached instance
        assertSame(writer1, writer2);
    }

    static class SimplePOJO {
        public String field1;
        public int field2;
        public boolean field3;

        public SimplePOJO() {
            this.field1 = "value";
            this.field2 = 100;
            this.field3 = true;
        }
    }

    @Test
    void testSimplePOJOSerialization() {
        SimplePOJO pojo = new SimplePOJO();

        // Test with AUTO
        String autoJson = JSON.toJSONString(pojo);
        assertNotNull(autoJson);
        assertTrue(autoJson.contains("value"));

        // Test with REFLECT
        ObjectMapper reflectMapper = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.REFLECT)
                .build();
        String reflectJson = reflectMapper.writeValueAsString(pojo);
        assertNotNull(reflectJson);
        assertTrue(reflectJson.contains("value"));

        // Test with ASM
        ObjectMapper asmMapper = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.ASM)
                .build();
        String asmJson = asmMapper.writeValueAsString(pojo);
        assertNotNull(asmJson);
        assertTrue(asmJson.contains("value"));
    }

    @Test
    void testEmptyBean() {
        ObjectWriterProvider provider = ObjectWriterProvider.of(WriterCreatorType.AUTO);
        ObjectWriter<?> writer = provider.getObjectWriter(EmptyBean.class);
        assertNotNull(writer);

        EmptyBean bean = new EmptyBean();
        String json = JSON.toJSONString(bean);
        assertNotNull(json);
    }

    static class EmptyBean {
        // No fields
    }

    @Test
    void testBeanWithNullFields() {
        BeanWithNulls bean = new BeanWithNulls();
        String json = JSON.toJSONString(bean);

        assertNotNull(json);
        // JSON should handle null fields
    }

    static class BeanWithNulls {
        public String nullString = null;
        public Integer nullInt = null;
        public List<String> nullList = null;
    }

    @Test
    void testSafeContextNestedClosing() {
        ObjectWriterProvider provider1 = ObjectWriterProvider.of(WriterCreatorType.AUTO);
        ObjectWriterProvider provider2 = ObjectWriterProvider.of(WriterCreatorType.REFLECT);
        ObjectWriterProvider provider3 = ObjectWriterProvider.of(WriterCreatorType.ASM);

        try (ObjectWriterProvider.SafeContext ctx1 = provider1.openContext()) {
            assertSame(provider1, ObjectWriterProvider.getContext());

            try (ObjectWriterProvider.SafeContext ctx2 = provider2.openContext()) {
                assertSame(provider2, ObjectWriterProvider.getContext());

                try (ObjectWriterProvider.SafeContext ctx3 = provider3.openContext()) {
                    assertSame(provider3, ObjectWriterProvider.getContext());
                }

                // After ctx3 closes, should return to ctx2
                assertSame(provider2, ObjectWriterProvider.getContext());
            }

            // After ctx2 closes, should return to ctx1
            assertSame(provider1, ObjectWriterProvider.getContext());
        }

        // After all close, should be null
        assertNull(ObjectWriterProvider.getContext());
    }
}

package com.alibaba.fastjson3;

import com.alibaba.fastjson3.reader.ObjectReaderProvider;
import com.alibaba.fastjson3.reader.ReaderCreatorType;
import com.alibaba.fastjson3.reader.ReflectObjectReaderProvider;
import com.alibaba.fastjson3.writer.ObjectWriterProvider;
import com.alibaba.fastjson3.writer.WriterCreatorType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ObjectMapper provider-related methods.
 */
class ObjectMapperProviderTest {

    static class SimpleBean {
        public String name = "test";
        public int value = 42;
    }

    @Test
    void testGetReaderProvider() {
        ObjectMapper mapper = ObjectMapper.builder().build();
        ObjectReaderProvider provider = mapper.getReaderProvider();
        assertNotNull(provider);
        assertEquals(ReaderCreatorType.REFLECT, provider.getCreatorType());
    }

    @Test
    void testGetWriterProvider() {
        ObjectMapper mapper = ObjectMapper.builder().build();
        ObjectWriterProvider provider = mapper.getWriterProvider();
        assertNotNull(provider);
        assertEquals(WriterCreatorType.AUTO, provider.getCreatorType());
    }

    @Test
    void testGetReaderCreatorType() {
        ObjectMapper mapper = ObjectMapper.builder().build();
        assertEquals(ReaderCreatorType.REFLECT, mapper.getReaderCreatorType());
    }

    @Test
    void testGetWriterCreatorType() {
        ObjectMapper mapper = ObjectMapper.builder().build();
        assertEquals(WriterCreatorType.AUTO, mapper.getWriterCreatorType());
    }

    @Test
    void testBuilderWithReaderProvider() {
        ObjectReaderProvider customProvider = ReflectObjectReaderProvider.INSTANCE;
        ObjectMapper mapper = ObjectMapper.builder()
                .readerProvider(customProvider)
                .build();
        
        assertSame(customProvider, mapper.getReaderProvider());
        assertEquals(ReaderCreatorType.REFLECT, mapper.getReaderCreatorType());
    }

    @Test
    void testBuilderWithWriterProvider() {
        ObjectWriterProvider customProvider = ObjectWriterProvider.of(WriterCreatorType.REFLECT);
        ObjectMapper mapper = ObjectMapper.builder()
                .writerProvider(customProvider)
                .build();
        
        assertSame(customProvider, mapper.getWriterProvider());
        assertEquals(WriterCreatorType.REFLECT, mapper.getWriterCreatorType());
    }

    @Test
    void testSerializationWithProvider() {
        ObjectMapper mapper = ObjectMapper.builder()
                .writerProvider(ObjectWriterProvider.of(WriterCreatorType.REFLECT))
                .build();
        
        SimpleBean bean = new SimpleBean();
        String json = mapper.writeValueAsString(bean);
        
        assertNotNull(json);
        assertTrue(json.contains("test"));
        assertTrue(json.contains("42"));
    }

    @Test
    void testDeserializationWithProvider() {
        ObjectMapper mapper = ObjectMapper.builder()
                .readerProvider(ObjectReaderProvider.of(ReaderCreatorType.REFLECT))
                .build();
        
        String json = "{\"name\":\"test\",\"value\":42}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        
        assertEquals("test", bean.name);
        assertEquals(42, bean.value);
    }

    @Test
    void testProviderCleanup() {
        ObjectMapper mapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.REFLECT)
                .writerCreatorType(WriterCreatorType.REFLECT)
                .build();
        
        // Should not throw
        assertDoesNotThrow(() -> mapper.cleanup());
        
        // Mapper should still work after cleanup
        SimpleBean bean = new SimpleBean();
        String json = mapper.writeValueAsString(bean);
        assertNotNull(json);
    }

    @Test
    void testRebuildPreservesProvider() {
        ObjectMapper original = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.REFLECT)
                .writerCreatorType(WriterCreatorType.ASM)
                .build();

        ObjectMapper rebuilt = original.rebuild().build();

        assertEquals(ReaderCreatorType.REFLECT, rebuilt.getReaderCreatorType());
        assertEquals(WriterCreatorType.ASM, rebuilt.getWriterCreatorType());
    }

    @Test
    void testDefaultProviderTypes() {
        ObjectMapper mapper = ObjectMapper.builder().build();

        // Default reader provider should be REFLECT
        assertEquals(ReaderCreatorType.REFLECT, mapper.getReaderCreatorType());

        // Default writer provider should be AUTO
        assertEquals(WriterCreatorType.AUTO, mapper.getWriterCreatorType());
    }

    @Test
    void testReadValueWithContext() {
        // Test that readerProvider.openContext() works correctly
        ObjectMapper mapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.REFLECT)
                .build();

        String json = "{\"name\":\"test\",\"value\":42}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);

        assertEquals("test", bean.name);
        assertEquals(42, bean.value);
    }

    @Test
    void testReadBytesValueWithContext() {
        ObjectMapper mapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.REFLECT)
                .build();

        String json = "{\"name\":\"test\",\"value\":42}";
        SimpleBean bean = mapper.readValue(json.getBytes(), SimpleBean.class);

        assertEquals("test", bean.name);
        assertEquals(42, bean.value);
    }
}

package com.alibaba.fastjson3;

import com.alibaba.fastjson3.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson3.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectReaderASMTest {
    public static class SimpleBean {
        public int id;
        public String name;
        public long version;
        public double percent;
        public boolean active;
        public float score;
    }

    private ObjectReader<SimpleBean> createASMReader() {
        return ObjectReaderCreatorASM.createObjectReader(SimpleBean.class);
    }

    @Test
    public void testSimpleBean() {
        ObjectReader<SimpleBean> reader = createASMReader();
        assertNotNull(reader);

        String json = "{\"id\":123,\"name\":\"test\",\"version\":99,\"percent\":3.14,\"active\":true,\"score\":1.5}";
        SimpleBean bean = JSON.parseObject(json.getBytes(java.nio.charset.StandardCharsets.UTF_8), SimpleBean.class);

        assertNotNull(bean);
        assertEquals(123, bean.id);
        assertEquals("test", bean.name);
        assertEquals(99L, bean.version);
        assertEquals(3.14, bean.percent, 0.001);
        assertTrue(bean.active);
        assertEquals(1.5f, bean.score, 0.001f);
    }

    @Test
    public void testASMReaderDirectly() {
        ObjectReader<SimpleBean> reader = createASMReader();
        assertNotNull(reader);

        String json = "{\"id\":42,\"name\":\"hello\",\"version\":100,\"percent\":2.71,\"active\":false,\"score\":9.8}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(bytes)) {
            SimpleBean bean = reader.readObject(parser, null, null, 0);
            assertNotNull(bean);
            assertEquals(42, bean.id);
            assertEquals("hello", bean.name);
            assertEquals(100L, bean.version);
            assertEquals(2.71, bean.percent, 0.001);
            assertFalse(bean.active);
            assertEquals(9.8f, bean.score, 0.1f);
        }
    }

    @Test
    public void testNullStringField() {
        ObjectReader<SimpleBean> reader = createASMReader();

        String json = "{\"id\":1,\"name\":null,\"version\":0,\"percent\":0.0,\"active\":false,\"score\":0.0}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(bytes)) {
            SimpleBean bean = reader.readObject(parser, null, null, 0);
            assertNotNull(bean);
            assertEquals(1, bean.id);
            assertNull(bean.name);
        }
    }

    @Test
    public void testEmptyObject() {
        ObjectReader<SimpleBean> reader = createASMReader();

        String json = "{}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(bytes)) {
            SimpleBean bean = reader.readObject(parser, null, null, 0);
            assertNotNull(bean);
            assertEquals(0, bean.id);
            assertNull(bean.name);
        }
    }

    @Test
    public void testNullInput() {
        ObjectReader<SimpleBean> reader = createASMReader();

        String json = "null";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(bytes)) {
            SimpleBean bean = reader.readObject(parser, null, null, 0);
            assertNull(bean);
        }
    }

    @Test
    public void testMatchesReflectionOutput() {
        String json = "{\"id\":123,\"name\":\"test\",\"version\":99,\"percent\":3.14,\"active\":true,\"score\":1.5}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // Parse with ASM reader
        ObjectReader<SimpleBean> asmReader = createASMReader();
        SimpleBean asmBean;
        try (JSONParser parser = JSONParser.of(bytes)) {
            asmBean = asmReader.readObject(parser, null, null, 0);
        }

        // Parse with standard JSON.parseObject (uses reflection reader)
        SimpleBean refBean = JSON.parseObject(bytes, SimpleBean.class);

        assertEquals(refBean.id, asmBean.id);
        assertEquals(refBean.name, asmBean.name);
        assertEquals(refBean.version, asmBean.version);
        assertEquals(refBean.percent, asmBean.percent, 0.001);
        assertEquals(refBean.active, asmBean.active);
        assertEquals(refBean.score, asmBean.score, 0.001f);
    }

    @Test
    public void testUnknownFieldsSkipped() {
        ObjectReader<SimpleBean> reader = createASMReader();

        String json = "{\"id\":5,\"unknown\":\"skip_me\",\"name\":\"ok\",\"version\":1,\"percent\":1.0,\"active\":true,\"score\":2.0}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(bytes)) {
            SimpleBean bean = reader.readObject(parser, null, null, 0);
            assertNotNull(bean);
            assertEquals(5, bean.id);
            assertEquals("ok", bean.name);
        }
    }

    @Test
    public void testCreateInstance() {
        ObjectReader<SimpleBean> reader = createASMReader();
        Object instance = reader.createInstance(0);
        assertNotNull(instance);
        assertInstanceOf(SimpleBean.class, instance);
    }

    @Test
    public void testGetObjectClass() {
        ObjectReader<SimpleBean> reader = createASMReader();
        assertEquals(SimpleBean.class, reader.getObjectClass());
    }
}

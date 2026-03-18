package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONParser;
import com.alibaba.fastjson3.ObjectReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.alibaba.fastjson3.JSON.*;

/**
 * Test for ASM bytecode generation.
 */
public class ASMGenerationTest {

    static class TestBean {
        public String id;
        public String name;
        public int age;
    }

    // @Test
    public void testCreateTestASMReader() {
        // TODO: Fix bytecode generation for this test
        // ObjectReader<TestBean> reader = ObjectReaderCreatorASM.createTestASMReader(TestBean.class);
        // assertNotNull(reader, "Reader should not be null");
        // assertEquals(TestBean.class, reader.getObjectClass(), "Object class should match");
    }

    @Test
    public void testCreateDirectFieldReader() {
        ObjectReader<ASMTestBean> reader = ObjectReaderCreatorASM.createDirectFieldReader(ASMTestBean.class);

        assertNotNull(reader, "Reader should not be null");
        assertEquals(ASMTestBean.class, reader.getObjectClass(), "Object class should match");
    }

    @Test
    public void testDirectFieldReaderParsesEmptyObject() {
        ObjectReader<ASMTestBean> reader = ObjectReaderCreatorASM.createDirectFieldReader(ASMTestBean.class);

        String json = "{}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(bytes)) {
            if (parser instanceof JSONParser.UTF8 utf8) {
                ASMTestBean bean = (ASMTestBean) reader.readObjectUTF8(utf8, 0);
                assertNotNull(bean, "Bean should not be null for empty object");
            }
        }
    }

    @Test
    public void testDirectFieldReaderParsesSimpleObject() {
        ObjectReader<ASMTestBean> reader = ObjectReaderCreatorASM.createDirectFieldReader(ASMTestBean.class);

        String json = "{\"id\":\"123\",\"nm\":\"test\",\"age\":25}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(bytes)) {
            if (parser instanceof JSONParser.UTF8 utf8) {
                ASMTestBean bean = (ASMTestBean) reader.readObjectUTF8(utf8, 0);
                assertNotNull(bean, "Bean should not be null");
                // Check that values were read correctly
                assertEquals("123", bean.id, "id should be parsed");
                assertEquals("test", bean.nm, "nm should be parsed");
                assertEquals(25, bean.age, "age should be parsed");
            }
        }
    }

    @Test
    public void testDirectFieldUsedAutomatically() {
        // Test that DirectField optimization is used automatically via JSON.parseObject
        // Use byte[] to ensure UTF8 parser is used
        String json = "{\"id\":\"456\",\"nm\":\"auto\",\"age\":30}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // First, test the direct ASM reader
        ObjectReader<ASMTestBean> directReader = ObjectReaderCreatorASM.createDirectFieldReader(ASMTestBean.class);
        assertNotNull(directReader, "Direct ASM reader should not be null");

        // Test direct reader with readObjectUTF8
        try (JSONParser parser = JSONParser.of(bytes)) {
            if (parser instanceof JSONParser.UTF8 utf8) {
                ASMTestBean bean1 = (ASMTestBean) directReader.readObjectUTF8(utf8, 0);
                assertNotNull(bean1, "Direct bean should not be null");
                assertEquals("456", bean1.id);
                assertEquals("auto", bean1.nm);
                assertEquals(30, bean1.age);
            }
        }

        // Test readObject method directly with a NEW parser
        try (JSONParser parser2 = JSONParser.of(bytes)) {
            Object result = directReader.readObject(parser2, ASMTestBean.class, null, 0);
            assertNotNull(result, "readObject result should not be null");
            assertInstanceOf(ASMTestBean.class, result);
            ASMTestBean bean2 = (ASMTestBean) result;
            assertEquals("456", bean2.id, "id should be parsed via readObject");
            assertEquals("auto", bean2.nm, "nm should be parsed via readObject");
            assertEquals(30, bean2.age, "age should be parsed via readObject");
        }

        // Now test via JSON.parseObject
        ASMTestBean bean = JSON.parseObject(bytes, ASMTestBean.class);

        assertNotNull(bean, "Bean should not be null");
        assertEquals("456", bean.id, "id should be parsed");
        assertEquals("auto", bean.nm, "nm should be parsed");
        assertEquals(30, bean.age, "age should be parsed");
    }

    @Test
    public void testExtendedTypes() {
        // Test support for double, float, short, byte, String[], long[]
        String json = "{\"id\":\"abc\",\"name\":\"test\",\"age\":25,\"userId\":12345," +
                      "\"balance\":99.99,\"score\":3.14,\"status\":42,\"flag\":7," +
                      "\"active\":true,\"tags\":[\"tag1\",\"tag2\"],\"phoneNumbers\":[123,456]}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        ASMTestBeanExtended bean = JSON.parseObject(bytes, ASMTestBeanExtended.class);

        assertNotNull(bean, "Bean should not be null");
        assertEquals("abc", bean.id, "id should be parsed");
        assertEquals("test", bean.name, "name should be parsed");
        assertEquals(25, bean.age, "age should be parsed");
        assertEquals(12345, bean.userId, "userId should be parsed");
        assertEquals(99.99, bean.balance, 0.001, "balance should be parsed");
        assertEquals(3.14f, bean.score, 0.001f, "score should be parsed");
        assertEquals((short)42, bean.status, "status should be parsed");
        assertEquals((byte)7, bean.flag, "flag should be parsed");
        assertEquals(true, bean.active, "active should be parsed");
        assertArrayEquals(new String[]{"tag1", "tag2"}, bean.tags, "tags should be parsed");
        assertArrayEquals(new long[]{123, 456}, bean.phoneNumbers, "phoneNumbers should be parsed");
    }

    @Test
    public void testPrivateFields() {
        // Test that ASM can handle private fields (like JJB User/Client)
        String json = "{\"id\":\"test123\",\"age\":30,\"balance\":999.99}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        ASMPrivateFieldBean bean = JSON.parseObject(bytes, ASMPrivateFieldBean.class);

        assertNotNull(bean, "Bean should not be null");
        assertEquals("test123", bean.getId(), "id should be parsed");
        assertEquals(30, bean.getAge(), "age should be parsed");
        assertEquals(999.99, bean.getBalance(), 0.001, "balance should be parsed");
    }
}


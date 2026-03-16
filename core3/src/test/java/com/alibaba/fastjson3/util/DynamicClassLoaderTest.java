package com.alibaba.fastjson3.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DynamicClassLoader - runtime bytecode loading for ASM-generated classes.
 */
class DynamicClassLoaderTest {

    @Test
    void testGetInstance() {
        DynamicClassLoader instance = DynamicClassLoader.getInstance();

        assertNotNull(instance);
        assertSame(instance, DynamicClassLoader.getInstance());
    }

    @Test
    void testConstructor_withParent() {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        DynamicClassLoader loader = new DynamicClassLoader(parent);

        assertNotNull(loader);
    }

    @Test
    void testConstructor_default() {
        DynamicClassLoader loader = new DynamicClassLoader();

        assertNotNull(loader);
    }

    @Test
    void testLoadClass_existingJDKClass() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> stringClass = loader.loadClass("java.lang.String");

        assertEquals(String.class, stringClass);
    }

    @Test
    void testLoadClass_fastjson3Class() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> jsonClass = loader.loadClass("com.alibaba.fastjson3.JSON");

        assertNotNull(jsonClass);
        assertEquals("com.alibaba.fastjson3.JSON", jsonClass.getName());
    }

    @Test
    void testLoadClass_fromMapping() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        // These classes should be in the preloaded mapping
        Class<?> objectClass = loader.loadClass("java.lang.Object");
        assertEquals(Object.class, objectClass);

        Class<?> listClass = loader.loadClass("java.util.List");
        assertEquals(java.util.List.class, listClass);

        Class<?> hashMapClass = loader.loadClass("java.util.HashMap");
        assertEquals(java.util.HashMap.class, hashMapClass);
    }

    @Test
    void testLoadClass_primitiveWrappers() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> integerClass = loader.loadClass("java.lang.Integer");
        assertEquals(Integer.class, integerClass);

        Class<?> longClass = loader.loadClass("java.lang.Long");
        assertEquals(Long.class, longClass);

        Class<?> doubleClass = loader.loadClass("java.lang.Double");
        assertEquals(Double.class, doubleClass);

        Class<?> booleanClass = loader.loadClass("java.lang.Boolean");
        assertEquals(Boolean.class, booleanClass);
    }

    @Test
    void testLoadClass_mathClasses() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> bigIntClass = loader.loadClass("java.math.BigInteger");
        assertEquals(java.math.BigInteger.class, bigIntClass);

        Class<?> bigDecClass = loader.loadClass("java.math.BigDecimal");
        assertEquals(java.math.BigDecimal.class, bigDecClass);
    }

    @Test
    void testLoadClass_coreTypes() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> collectionClass = loader.loadClass("java.util.Collection");
        assertEquals(java.util.Collection.class, collectionClass);

        Class<?> mapClass = loader.loadClass("java.util.Map");
        assertEquals(java.util.Map.class, mapClass);

        Class<?> setClass = loader.loadClass("java.util.Set");
        assertEquals(java.util.Set.class, setClass);
    }

    @Test
    void testLoadClass_exceptionClasses() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> exceptionClass = loader.loadClass("java.lang.Exception");
        assertEquals(Exception.class, exceptionClass);
    }

    @Test
    void testLoadClass_reflectionClasses() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> fieldClass = loader.loadClass("java.lang.reflect.Field");
        assertEquals(java.lang.reflect.Field.class, fieldClass);

        Class<?> methodClass = loader.loadClass("java.lang.reflect.Method");
        assertEquals(java.lang.reflect.Method.class, methodClass);

        Class<?> typeClass = loader.loadClass("java.lang.reflect.Type");
        assertEquals(java.lang.reflect.Type.class, typeClass);
    }

    @Test
    void testLoadClass_fastjsonException() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> exceptionClass = loader.loadClass("com.alibaba.fastjson3.JSONException");

        assertNotNull(exceptionClass);
        assertEquals("com.alibaba.fastjson3.JSONException", exceptionClass.getName());
    }

    @Test
    void testLoadClass_objectMapper() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> mapperClass = loader.loadClass("com.alibaba.fastjson3.ObjectMapper");

        assertNotNull(mapperClass);
        assertEquals("com.alibaba.fastjson3.ObjectMapper", mapperClass.getName());
    }

    @Test
    void testLoadClass_objectReader() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> readerClass = loader.loadClass("com.alibaba.fastjson3.ObjectReader");

        assertNotNull(readerClass);
        assertEquals("com.alibaba.fastjson3.ObjectReader", readerClass.getName());
    }

    @Test
    void testLoadClass_objectWriter() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> writerClass = loader.loadClass("com.alibaba.fastjson3.ObjectWriter");

        assertNotNull(writerClass);
        assertEquals("com.alibaba.fastjson3.ObjectWriter", writerClass.getName());
    }

    @Test
    void testLoadClass_jsonContainerClasses() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> objectClass = loader.loadClass("com.alibaba.fastjson3.JSONObject");
        assertEquals("com.alibaba.fastjson3.JSONObject", objectClass.getName());

        Class<?> arrayClass = loader.loadClass("com.alibaba.fastjson3.JSONArray");
        assertEquals("com.alibaba.fastjson3.JSONArray", arrayClass.getName());
    }

    @Test
    void testLoadClass_fieldClasses() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        Class<?> fieldReaderClass = loader.loadClass("com.alibaba.fastjson3.reader.FieldReader");
        assertNotNull(fieldReaderClass);

        Class<?> fieldWriterClass = loader.loadClass("com.alibaba.fastjson3.writer.FieldWriter");
        assertNotNull(fieldWriterClass);
    }

    @Test
    void testLoadClass_notFound() {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        assertThrows(ClassNotFoundException.class, () -> {
            loader.loadClass("com.nonexistent.Class");
        });
    }

    @Test
    void testLoadClass_withResolve() throws ClassNotFoundException {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        // Test with resolve = true
        Class<?> clazz = loader.loadClass("java.lang.String", true);
        assertEquals(String.class, clazz);

        // Test with resolve = false
        clazz = loader.loadClass("java.lang.String", false);
        assertEquals(String.class, clazz);
    }

    @Test
    void testLoadClass_fromContextClassLoader() {
        // This test verifies that the class loader can fall back to context class loader
        DynamicClassLoader loader = DynamicClassLoader.getInstance();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

        assertNotNull(contextLoader);

        // The loader should be able to load classes from the context loader
        assertDoesNotThrow(() -> {
            loader.loadClass("java.lang.String");
        });
    }

    @Test
    void testLoadClass_byteArrayInvalid() {
        DynamicClassLoader loader = DynamicClassLoader.getInstance();

        byte[] invalidClass = new byte[]{0x00, 0x01, 0x02, 0x03};

        assertThrows(ClassFormatError.class, () -> {
            loader.loadClass("Invalid", invalidClass, 0, invalidClass.length);
        });
    }
}

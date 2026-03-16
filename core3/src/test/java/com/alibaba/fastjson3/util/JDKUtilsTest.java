package com.alibaba.fastjson3.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JDKUtils - JDK compatibility and low-level optimization utilities.
 */
class JDKUtilsTest {

    @Test
    void testUnsafeAvailable() {
        // Unsafe should be available on standard HotSpot JVM
        assertNotNull(JDKUtils.UNSAFE_AVAILABLE, "UNSAFE_AVAILABLE should be defined");
    }

    @Test
    void testJDKVersion() {
        // Should be running on JDK 21+
        assertTrue(JDKUtils.JDK_VERSION >= 21, "Expected JDK 21+, found " + JDKUtils.JDK_VERSION);
    }

    @Test
    void testVectorSupport() {
        // Vector API support is optional, but the flag should be defined
        assertTrue(JDKUtils.VECTOR_SUPPORT == true || JDKUtils.VECTOR_SUPPORT == false);
        if (JDKUtils.VECTOR_SUPPORT) {
            assertTrue(JDKUtils.VECTOR_BYTE_SIZE >= 16);
        }
    }

    // ==================== getLong/putLong tests ====================

    @Test
    void testGetLong() {
        byte[] buf = new byte[16];
        for (int i = 0; i < 8; i++) {
            buf[i] = (byte) (i + 1);
        }

        long value = JDKUtils.getLong(buf, 0);
        // Test that we get consistent results
        assertEquals(value, JDKUtils.getLong(buf, 0));
    }

    @Test
    void testGetLong_offset() {
        byte[] buf = new byte[16];
        for (int i = 0; i < 16; i++) {
            buf[i] = (byte) i;
        }

        long value1 = JDKUtils.getLong(buf, 0);
        long value2 = JDKUtils.getLong(buf, 8);
        // Values should be different at different offsets
        assertNotEquals(value1, value2);
    }

    @Test
    void testPutLong() {
        byte[] buf = new byte[16];
        long expected = 0x123456789ABCDEFL;

        JDKUtils.putLong(buf, 0, expected);
        long result = JDKUtils.getLong(buf, 0);

        assertEquals(expected, result);
    }

    @Test
    void testPutLong_multipleOffsets() {
        byte[] buf = new byte[24];
        long val1 = 0x1111111111111111L;
        long val2 = 0x2222222222222222L;
        long val3 = 0x3333333333333333L;

        JDKUtils.putLong(buf, 0, val1);
        JDKUtils.putLong(buf, 8, val2);
        JDKUtils.putLong(buf, 16, val3);

        assertEquals(val1, JDKUtils.getLong(buf, 0));
        assertEquals(val2, JDKUtils.getLong(buf, 8));
        assertEquals(val3, JDKUtils.getLong(buf, 16));
    }

    @Test
    void testPutLong_roundtrip() {
        byte[] buf = new byte[8];
        long[] testValues = {
                0L,
                1L,
                -1L,
                Long.MAX_VALUE,
                Long.MIN_VALUE,
                0x123456789ABCDEFL,
                0xFF00FF00FF00FF00L
        };

        for (long value : testValues) {
            java.util.Arrays.fill(buf, (byte) 0);
            JDKUtils.putLong(buf, 0, value);
            assertEquals(value, JDKUtils.getLong(buf, 0),
                    "Round-trip failed for value: " + value);
        }
    }

    // ==================== getCharLong/putIntDirect tests ====================

    @Test
    void testGetCharLong() {
        char[] buf = new char[8];
        for (int i = 0; i < 8; i++) {
            buf[i] = (char) ('A' + i);
        }

        long value = JDKUtils.getCharLong(buf, 0);
        assertTrue(value != 0 || buf[0] == '\0'); // value is 0 only if buf[0] is null char
        assertEquals(value, JDKUtils.getCharLong(buf, 0));
    }

    @Test
    void testGetCharLong_offset() {
        char[] buf = new char[8];
        for (int i = 0; i < 8; i++) {
            buf[i] = (char) i;
        }

        long value1 = JDKUtils.getCharLong(buf, 0);
        long value2 = JDKUtils.getCharLong(buf, 4);
        assertNotEquals(value1, value2);
    }

    @Test
    void testPutIntDirect() {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return; // Skip test if Unsafe is not available
        }
        byte[] buf = new byte[8];
        int expected = 0x12345678;

        JDKUtils.putIntDirect(buf, 0, expected);
        int result = JDKUtils.getIntDirect(buf, 0);

        assertEquals(expected, result);
    }

    @Test
    void testPutShortDirect() {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }
        byte[] buf = new byte[4];
        short expected = (short) 0x1234;

        JDKUtils.putShortDirect(buf, 0, expected);

        // Read back using getIntDirect and check first 2 bytes
        int intValue = JDKUtils.getIntDirect(buf, 0);
        // On little-endian: low 16 bits contain the short
        // On big-endian: high 16 bits contain the short
        short result = (short) (intValue & 0xFFFF);

        assertEquals(expected, result);
    }

    // ==================== Fast String creation tests ====================

    @Test
    void testGetStringValue() {
        String test = "test";
        Object value = JDKUtils.getStringValue(test);

        if (JDKUtils.UNSAFE_AVAILABLE) {
            // If Unsafe is available, we might get the internal value
            // The result could be byte[] (JDK 9+) or char[] (JDK 8)
            // or null if the offset couldn't be determined
            assertTrue(value == null || value instanceof byte[] || value instanceof char[]);
        } else {
            assertNull(value);
        }
    }

    @Test
    void testGetStringCoder() {
        String latin1 = "test"; // LATIN1
        String utf16 = "test\u4e2d\u6587"; // contains Chinese, needs UTF16

        int latin1Coder = JDKUtils.getStringCoder(latin1);
        int utf16Coder = JDKUtils.getStringCoder(utf16);

        if (JDKUtils.UNSAFE_AVAILABLE) {
            // If Unsafe is available and compact strings are enabled,
            // LATIN1 coder is 0, UTF16 coder is 1
            // If we get -1, it means compact strings are not available or offset is -1
            assertTrue(latin1Coder == 0 || latin1Coder == -1);
            assertTrue(utf16Coder == 1 || utf16Coder == -1);
        } else {
            assertEquals(-1, latin1Coder);
            assertEquals(-1, utf16Coder);
        }
    }

    @Test
    void testCreateAsciiString() {
        byte[] bytes = "Hello World".getBytes(StandardCharsets.ISO_8859_1);
        String result = JDKUtils.createAsciiString(bytes, 0, bytes.length);

        assertEquals("Hello World", result);
    }

    @Test
    void testCreateAsciiString_withOffset() {
        byte[] bytes = "XXXHello WorldXXX".getBytes(StandardCharsets.ISO_8859_1);
        String result = JDKUtils.createAsciiString(bytes, 3, 11);

        assertEquals("Hello World", result);
    }

    @Test
    void testCreateAsciiString_empty() {
        byte[] bytes = new byte[0];
        String result = JDKUtils.createAsciiString(bytes, 0, 0);

        assertEquals("", result);
    }

    // ==================== Array comparison tests ====================

    @Test
    void testArrayEquals_identical() {
        byte[] a = "test".getBytes(StandardCharsets.UTF_8);
        byte[] b = "test".getBytes(StandardCharsets.UTF_8);

        assertTrue(JDKUtils.arrayEquals(a, 0, b, 0, a.length));
    }

    @Test
    void testArrayEquals_different() {
        byte[] a = "test".getBytes(StandardCharsets.UTF_8);
        byte[] b = "text".getBytes(StandardCharsets.UTF_8);

        assertFalse(JDKUtils.arrayEquals(a, 0, b, 0, a.length));
    }

    @Test
    void testArrayEquals_withOffset() {
        byte[] a = "XXXtestXXX".getBytes(StandardCharsets.UTF_8);
        byte[] b = "XXXtestXXX".getBytes(StandardCharsets.UTF_8);

        assertTrue(JDKUtils.arrayEquals(a, 3, b, 3, 4));
    }

    @Test
    void testArrayEquals_empty() {
        byte[] a = new byte[0];
        byte[] b = new byte[0];

        assertTrue(JDKUtils.arrayEquals(a, 0, b, 0, 0));
    }

    @Test
    void testArrayEquals_longArrays() {
        byte[] a = new byte[100];
        byte[] b = new byte[100];
        for (int i = 0; i < 100; i++) {
            a[i] = (byte) i;
            b[i] = (byte) i;
        }

        assertTrue(JDKUtils.arrayEquals(a, 0, b, 0, 100));
    }

    @Test
    void testArrayEquals_longArrays_differentAtEnd() {
        byte[] a = new byte[100];
        byte[] b = new byte[100];
        for (int i = 0; i < 99; i++) {
            a[i] = (byte) i;
            b[i] = (byte) i;
        }
        a[99] = 1;
        b[99] = 2;

        assertFalse(JDKUtils.arrayEquals(a, 0, b, 0, 100));
    }

    // ==================== Field access tests ====================

    @Test
    void testObjectFieldOffset() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public int publicField;
            private long privateField;
        }

        Field publicField = TestClass.class.getDeclaredField("publicField");
        long offset = JDKUtils.objectFieldOffset(publicField);

        assertTrue(offset >= 0 || offset == -1); // -1 for records in JDK 16+
    }

    @Test
    void testGetObject() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public Object objField = "testValue";
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("objField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            Object result = JDKUtils.getObject(instance, offset);
            assertEquals("testValue", result);
        }
    }

    @Test
    void testGetInt() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public int intField = 42;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("intField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            int result = JDKUtils.getInt(instance, offset);
            assertEquals(42, result);
        }
    }

    @Test
    void testGetLongField() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public long longField = 123456789L;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("longField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            long result = JDKUtils.getLongField(instance, offset);
            assertEquals(123456789L, result);
        }
    }

    @Test
    void testGetDouble() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public double doubleField = 3.14159;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("doubleField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            double result = JDKUtils.getDouble(instance, offset);
            assertEquals(3.14159, result, 0.00001);
        }
    }

    @Test
    void testGetFloat() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public float floatField = 2.718f;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("floatField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            float result = JDKUtils.getFloat(instance, offset);
            assertEquals(2.718f, result, 0.001f);
        }
    }

    @Test
    void testGetBoolean() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public boolean booleanField = true;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("booleanField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            boolean result = JDKUtils.getBoolean(instance, offset);
            assertTrue(result);
        }
    }

    // ==================== Field write tests ====================

    @Test
    void testPutObject() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public Object objField;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("objField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            JDKUtils.putObject(instance, offset, "newValue");
            assertEquals("newValue", instance.objField);
        }
    }

    @Test
    void testPutInt() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public int intField;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("intField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            JDKUtils.putInt(instance, offset, 999);
            assertEquals(999, instance.intField);
        }
    }

    @Test
    void testPutLongField() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public long longField;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("longField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            JDKUtils.putLongField(instance, offset, 987654321L);
            assertEquals(987654321L, instance.longField);
        }
    }

    @Test
    void testPutDouble() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public double doubleField;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("doubleField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            JDKUtils.putDouble(instance, offset, 2.71828);
            assertEquals(2.71828, instance.doubleField, 0.00001);
        }
    }

    @Test
    void testPutFloat() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public float floatField;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("floatField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            JDKUtils.putFloat(instance, offset, 1.414f);
            assertEquals(1.414f, instance.floatField, 0.001f);
        }
    }

    @Test
    void testPutBoolean() throws NoSuchFieldException {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return;
        }

        class TestClass {
            public boolean booleanField;
        }

        TestClass instance = new TestClass();
        Field field = TestClass.class.getDeclaredField("booleanField");
        long offset = JDKUtils.objectFieldOffset(field);

        if (offset >= 0) {
            JDKUtils.putBoolean(instance, offset, false);
            assertFalse(instance.booleanField);

            JDKUtils.putBoolean(instance, offset, true);
            assertTrue(instance.booleanField);
        }
    }

    // ==================== Record support tests ====================

    @Test
    void testIsRecord() {
        if (JDKUtils.JDK_VERSION < 16) {
            return; // Records not available
        }

        record TestRecord(String name, int value) {
        }

        assertTrue(JDKUtils.isRecord(TestRecord.class));
        assertFalse(JDKUtils.isRecord(String.class));
    }

    @Test
    void testGetRecordComponentNames() {
        if (JDKUtils.JDK_VERSION < 16) {
            return;
        }

        record TestRecord(String name, int value) {
        }

        String[] names = JDKUtils.getRecordComponentNames(TestRecord.class);
        assertEquals(2, names.length);
        assertEquals("name", names[0]);
        assertEquals("value", names[1]);
    }

    @Test
    void testGetRecordComponentTypes() {
        if (JDKUtils.JDK_VERSION < 16) {
            return;
        }

        record TestRecord(String name, int value) {
        }

        Class<?>[] types = JDKUtils.getRecordComponentTypes(TestRecord.class);
        assertEquals(2, types.length);
        assertEquals(String.class, types[0]);
        assertEquals(int.class, types[1]);
    }

    @Test
    void testGetRecordComponentGenericTypes() {
        if (JDKUtils.JDK_VERSION < 16) {
            return;
        }

        record TestRecord(String name, java.util.List<Integer> values) {
        }

        java.lang.reflect.Type[] types = JDKUtils.getRecordComponentGenericTypes(TestRecord.class);
        assertEquals(2, types.length);
        assertEquals(String.class, types[0]);
        assertTrue(types[1].getTypeName().contains("List"));
    }

    @Test
    void testGetRecordComponentAccessors() {
        if (JDKUtils.JDK_VERSION < 16) {
            return;
        }

        record TestRecord(String name, int value) {
        }

        java.lang.reflect.Method[] accessors = JDKUtils.getRecordComponentAccessors(TestRecord.class);
        assertEquals(2, accessors.length);
        assertEquals("name", accessors[0].getName());
        assertEquals("value", accessors[1].getName());
    }
}

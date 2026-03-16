package com.alibaba.fastjson3.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UnsafeAllocator - fast object instantiation bypassing constructor.
 */
class UnsafeAllocatorTest {

    @Test
    void testAllocateInstance_simpleClass() throws InstantiationException {
        TestClass obj = UnsafeAllocator.allocateInstance(TestClass.class);

        assertNotNull(obj);
        // Constructor should NOT have been called
        assertFalse(obj.constructorCalled);
        // Fields should have default values
        assertEquals(0, obj.intValue);
        assertEquals(0, obj.longValue);
        assertFalse(obj.booleanValue);
        assertNull(obj.stringValue);
    }

    @Test
    void testAllocateInstanceUnchecked() {
        TestClass obj = UnsafeAllocator.allocateInstanceUnchecked(TestClass.class);

        assertNotNull(obj);
        assertFalse(obj.constructorCalled);
        assertEquals(0, obj.intValue);
        assertNull(obj.stringValue);
    }

    @Test
    void testAllocateInstance_withDefaultValues() {
        TestClass obj = UnsafeAllocator.allocateInstanceUnchecked(TestClass.class);

        assertEquals(0, obj.intValue);
        assertEquals(0L, obj.longValue);
        assertEquals(0.0, obj.doubleValue, 0.0);
        assertEquals('\0', obj.charValue);
        assertFalse(obj.booleanValue);
        assertNull(obj.stringValue);
        assertNull(obj.objectValue);
    }

    @Test
    void testAllocateInstance_multipleInstances() {
        TestClass obj1 = UnsafeAllocator.allocateInstanceUnchecked(TestClass.class);
        TestClass obj2 = UnsafeAllocator.allocateInstanceUnchecked(TestClass.class);

        assertNotNull(obj1);
        assertNotNull(obj2);
        assertNotSame(obj1, obj2);
    }

    @Test
    void testAllocateInstance_canSetFieldsAfterAllocation() {
        TestClass obj = UnsafeAllocator.allocateInstanceUnchecked(TestClass.class);

        obj.intValue = 42;
        obj.stringValue = "test";
        obj.booleanValue = true;

        assertEquals(42, obj.intValue);
        assertEquals("test", obj.stringValue);
        assertTrue(obj.booleanValue);
    }

    @Test
    void testAllocateInstanceUnchecked_exception() {
        // This should not throw checked exception, but wrap it in RuntimeException
        assertThrows(RuntimeException.class, () -> {
            UnsafeAllocator.allocateInstanceUnchecked(int.class);
        });
    }

    @Test
    void testAllocateInstance_withFinalField() {
        ClassWithFinal obj = UnsafeAllocator.allocateInstanceUnchecked(ClassWithFinal.class);

        assertNotNull(obj);
        // Final field should have default value (0) since constructor wasn't called
        assertEquals(0, obj.finalValue);
    }

    @Test
    void testAllocateInstance_withParentClass() {
        ChildClass obj = UnsafeAllocator.allocateInstanceUnchecked(ChildClass.class);

        assertNotNull(obj);
        assertFalse(obj.childConstructorCalled);
        assertFalse(obj.parentConstructorCalled);
        assertEquals(0, obj.childValue);
        assertEquals(0, obj.parentValue);
    }

    // Note: Skip tests for String and Integer allocation as they can cause JVM crashes
    // when using Unsafe.allocateInstance() on certain JDK versions

    // ==================== Test classes ====================

    static class TestClass {
        public int intValue;
        public long longValue;
        public double doubleValue;
        public char charValue;
        public boolean booleanValue;
        public String stringValue;
        public Object objectValue;
        public boolean constructorCalled;

        public TestClass() {
            constructorCalled = true;
            intValue = 999; // Should not be set when using Unsafe
            stringValue = "default";
        }
    }

    static final class ClassWithFinal {
        public final int finalValue;

        public ClassWithFinal() {
            finalValue = 42;
        }
    }

    static class ParentClass {
        public int parentValue;
        public boolean parentConstructorCalled;

        public ParentClass() {
            parentConstructorCalled = true;
            parentValue = 100;
        }
    }

    static class ChildClass extends ParentClass {
        public int childValue;
        public boolean childConstructorCalled;

        public ChildClass() {
            super(); // Would normally call parent constructor
            childConstructorCalled = true;
            childValue = 200;
        }
    }
}

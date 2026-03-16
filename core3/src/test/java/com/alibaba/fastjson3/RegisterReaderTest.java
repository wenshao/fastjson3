package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test runtime registration of ObjectReader/ObjectWriter and its interaction
 * with ClassValue cache.
 */
public class RegisterReaderTest {

    static class TestType {
        public String value;
    }

    @Test
    public void testRegisterReaderOverridesClassValue() {
        ObjectMapper mapper = ObjectMapper.shared();

        // First: get reader (will use ClassValue and cache the result)
        TestType obj1 = mapper.readValue("{\"value\":\"original\"}", TestType.class);
        assertEquals("original", obj1.value);

        // Register a custom reader that modifies the value
        ObjectReader<TestType> customReader = (parser, fieldType, fieldName, features) -> {
            TestType obj = new TestType();
            obj.value = "CUSTOM:" + parser.readString();
            return obj;
        };
        mapper.registerReader(TestType.class, customReader);

        // Second: should use custom reader from readerCache (not ClassValue)
        TestType obj2 = mapper.readValue("\"after-register\"", TestType.class);
        assertEquals("CUSTOM:after-register", obj2.value);
    }

    @Test
    public void testRegisterReaderTakesPrecedence() {
        ObjectMapper mapper = ObjectMapper.shared();

        // Register custom reader BEFORE any lookup
        ObjectReader<TestType> customReader = (parser, fieldType, fieldName, features) -> {
            // Consume parser input to avoid issues if validation is added later
            parser.readString();
            TestType obj = new TestType();
            obj.value = "PRE-REGISTERED";
            return obj;
        };
        mapper.registerReader(TestType.class, customReader);

        // First lookup should use registered reader (not ClassValue)
        TestType obj = mapper.readValue("\"test\"", TestType.class);
        assertEquals("PRE-REGISTERED", obj.value);
    }

    @Test
    public void testRegisterWriterOverridesClassValue() {
        ObjectMapper mapper = ObjectMapper.shared();

        // First: get writer (will use ClassValue)
        String json1 = mapper.writeValueAsString(new TestType());
        assertNotNull(json1);

        // Register a custom writer
        ObjectWriter<TestType> customWriter = (generator, object, fieldName, fieldType, features) -> {
            generator.writeString("CUSTOM_WRITER");
        };
        mapper.registerWriter(TestType.class, customWriter);

        // Second: should use custom writer
        String json2 = mapper.writeValueAsString(new TestType());
        assertEquals("\"CUSTOM_WRITER\"", json2);
    }
}

package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the 7 P0 serialization capabilities for fastjson2 migration.
 */
class P0SerializationTest {

    // ==================== 1. WriteClassName ====================

    public static class Animal {
        public String name;
    }

    public static class Dog extends Animal {
        public String breed;
    }

    @Test
    void writeClassName_basic() {
        Dog dog = new Dog();
        dog.name = "Rex";
        dog.breed = "Labrador";
        String json = JSON.toJSONString(dog, WriteFeature.WriteClassName);
        assertTrue(json.contains("\"@type\""), "Should contain @type: " + json);
        assertTrue(json.contains("P0SerializationTest$Dog"), "Should contain class name: " + json);
        assertTrue(json.contains("\"name\""), json);
        assertTrue(json.contains("\"breed\""), json);
    }

    // ==================== 2. NotWriteRootClassName ====================

    @Test
    void notWriteRootClassName() {
        Dog dog = new Dog();
        dog.name = "Rex";
        String json = JSON.toJSONString(dog, WriteFeature.WriteClassName, WriteFeature.NotWriteRootClassName);
        assertFalse(json.contains("@type"), "Root object should NOT have @type: " + json);
    }

    // ==================== 3. NotWriteHashMapArrayListClassName ====================

    @Test
    void notWriteHashMapArrayListClassName() {
        var map = new java.util.HashMap<String, Object>();
        map.put("key", "value");
        String json = JSON.toJSONString(map, WriteFeature.WriteClassName, WriteFeature.NotWriteHashMapArrayListClassName);
        assertFalse(json.contains("@type"), "HashMap should NOT have @type: " + json);
    }

    // ==================== 4. @JSONField(unwrapped) ====================

    public static class Address {
        public String street = "123 Main St";
        public String city = "Springfield";
    }

    public static class UserWithAddress {
        public String name = "John";
        @JSONField(unwrapped = true)
        public Address address = new Address();
    }

    @Test
    void unwrapped_flattenNestedObject() {
        UserWithAddress user = new UserWithAddress();
        String json = JSON.toJSONString(user);
        assertTrue(json.contains("\"name\""), json);
        assertTrue(json.contains("\"street\""), "Nested street should be flattened: " + json);
        assertTrue(json.contains("\"city\""), "Nested city should be flattened: " + json);
        assertFalse(json.contains("\"address\""), "Should NOT have address wrapper: " + json);
    }

    @Test
    void unwrapped_nullValue() {
        UserWithAddress user = new UserWithAddress();
        user.address = null;
        String json = JSON.toJSONString(user);
        assertTrue(json.contains("\"name\""), json);
        assertFalse(json.contains("\"address\""), "Null unwrapped should be silent: " + json);
    }

    // ==================== 5. @JSONField(serializeFeatures) ====================

    public static class MixedFeatures {
        public String visible = "yes";

        @JSONField(serializeFeatures = WriteFeature.WriteNulls)
        public String nullable = null;

        public String otherNull = null;
    }

    @Test
    void serializeFeatures_fieldLevel() {
        MixedFeatures bean = new MixedFeatures();
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"visible\""), json);
        assertTrue(json.contains("\"nullable\":null"), "Field with WriteNulls should include null: " + json);
        assertFalse(json.contains("\"otherNull\""), "Global default should skip null: " + json);
    }

    // ==================== 6. Throwable/Exception serialization ====================

    @Test
    void throwable_serialization() {
        Exception ex = new RuntimeException("test error");
        String json = JSON.toJSONString(ex);
        assertTrue(json.contains("\"message\""), "Should have message: " + json);
        assertTrue(json.contains("\"test error\""), "Should contain error text: " + json);
        assertTrue(json.contains("\"stackTrace\""), "Should have stackTrace: " + json);
    }

    @Test
    void throwable_withCause() {
        Exception cause = new IllegalArgumentException("root cause");
        Exception ex = new RuntimeException("wrapper", cause);
        String json = JSON.toJSONString(ex);
        assertTrue(json.contains("\"cause\""), "Should have cause: " + json);
        assertTrue(json.contains("\"root cause\""), "Should contain cause message: " + json);
    }

    // ==================== 7. @JSONType(serializer=...) ====================

    public static class CustomAnimalWriter implements ObjectWriter<CustomAnimal> {
        @Override
        public void write(JSONGenerator generator, Object object,
                          Object fieldName, java.lang.reflect.Type fieldType, long features) {
            CustomAnimal a = (CustomAnimal) object;
            generator.writeString(a.species + ":" + a.name);
        }
    }

    @JSONType(serializer = CustomAnimalWriter.class)
    public static class CustomAnimal {
        public String species = "dog";
        public String name = "Rex";
    }

    @Test
    void jsonType_serializer() {
        CustomAnimal animal = new CustomAnimal();
        String json = JSON.toJSONString(animal);
        assertEquals("\"dog:Rex\"", json);
    }

    // ==================== Bonus: Currency / Locale / TimeZone ====================

    @Test
    void currency_serialization() {
        String json = JSON.toJSONString(java.util.Currency.getInstance("USD"));
        assertEquals("\"USD\"", json);
    }

    @Test
    void locale_serialization() {
        String json = JSON.toJSONString(java.util.Locale.US);
        assertEquals("\"en_US\"", json);
    }

    @Test
    void timeZone_serialization() {
        String json = JSON.toJSONString(java.util.TimeZone.getTimeZone("UTC"));
        assertEquals("\"UTC\"", json);
    }
}

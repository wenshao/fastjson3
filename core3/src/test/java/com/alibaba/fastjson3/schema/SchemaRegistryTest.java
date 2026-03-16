package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SchemaRegistry - named schema registration and resolution.
 */
class SchemaRegistryTest {

    private SchemaRegistry registry;

    @BeforeEach
    void setUp() {
        registry = SchemaRegistry.getInstance();
        registry.clear();
    }

    @AfterEach
    void tearDown() {
        registry.clear();
    }

    @Test
    void testGetInstance() {
        SchemaRegistry instance = SchemaRegistry.getInstance();

        assertNotNull(instance);
        assertSame(instance, SchemaRegistry.getInstance());
    }

    @Test
    void testRegister_andResolve() {
        JSONObject schemaObj = new JSONObject();
        schemaObj.put("type", "string");
        JSONSchema schema = JSONSchema.of(schemaObj);

        registry.register("mySchema", schema);

        JSONSchema resolved = registry.resolve("mySchema");

        assertSame(schema, resolved);
    }

    @Test
    void testRegister_anySchema() {
        registry.register("anySchema", Any.INSTANCE);

        assertSame(Any.INSTANCE, registry.resolve("anySchema"));
    }

    @Test
    void testRegister_nullName() {
        assertDoesNotThrow(() -> registry.register(null, Any.INSTANCE));
    }

    @Test
    void testRegister_nullSchema() {
        assertDoesNotThrow(() -> registry.register("test", null));
    }

    @Test
    void testResolve_nonExistent() {
        JSONSchema resolved = registry.resolve("nonexistent");

        assertNull(resolved);
    }

    @Test
    void testResolve_null() {
        JSONSchema resolved = registry.resolve(null);

        assertNull(resolved);
    }

    @Test
    void testUnregister() {
        registry.register("temp", Any.INSTANCE);

        assertSame(Any.INSTANCE, registry.resolve("temp"));

        registry.unregister("temp");

        assertNull(registry.resolve("temp"));
    }

    @Test
    void testUnregister_null() {
        assertDoesNotThrow(() -> registry.unregister(null));
    }

    @Test
    void testUnregister_nonExistent() {
        assertDoesNotThrow(() -> registry.unregister("nonexistent"));
    }

    @Test
    void testClear() {
        registry.register("schema1", Any.INSTANCE);
        registry.register("schema2", Any.INSTANCE);
        registry.register("schema3", Any.INSTANCE);

        assertNotNull(registry.resolve("schema1"));
        assertNotNull(registry.resolve("schema2"));
        assertNotNull(registry.resolve("schema3"));

        registry.clear();

        assertNull(registry.resolve("schema1"));
        assertNull(registry.resolve("schema2"));
        assertNull(registry.resolve("schema3"));
    }

    @Test
    void testMultipleRegistrations_sameName() {
        JSONObject obj1 = new JSONObject();
        obj1.put("type", "string");
        JSONSchema schema1 = JSONSchema.of(obj1);

        JSONObject obj2 = new JSONObject();
        obj2.put("type", "number");
        JSONSchema schema2 = JSONSchema.of(obj2);

        registry.register("shared", schema1);
        registry.register("shared", schema2); // Overwrites

        assertSame(schema2, registry.resolve("shared"));
        assertNotSame(schema1, registry.resolve("shared"));
    }

    @Test
    void testConcurrentRegistration() throws InterruptedException {
        int threadCount = 10;
        int schemasPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < schemasPerThread; j++) {
                    String name = "schema_" + threadId + "_" + j;
                    registry.register(name, Any.INSTANCE);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all schemas are registered
        int foundCount = 0;
        for (int i = 0; i < threadCount; i++) {
            for (int j = 0; j < schemasPerThread; j++) {
                String name = "schema_" + i + "_" + j;
                if (registry.resolve(name) != null) {
                    foundCount++;
                }
            }
        }

        assertEquals(threadCount * schemasPerThread, foundCount);
    }

    @Test
    void testRegisterComplexSchemas() {
        // Create various schema types
        JSONObject stringObj = new JSONObject();
        stringObj.put("type", "string");
        JSONSchema stringSchema = JSONSchema.of(stringObj);

        JSONObject numberObj = new JSONObject();
        numberObj.put("type", "number");
        JSONSchema numberSchema = JSONSchema.of(numberObj);

        JSONObject boolObj = new JSONObject();
        boolObj.put("type", "boolean");
        JSONSchema booleanSchema = JSONSchema.of(boolObj);

        JSONObject objectObj = new JSONObject();
        objectObj.put("type", "object");
        JSONSchema objectSchema = JSONSchema.of(objectObj);

        registry.register("stringSchema", stringSchema);
        registry.register("numberSchema", numberSchema);
        registry.register("booleanSchema", booleanSchema);
        registry.register("objectSchema", objectSchema);

        assertSame(stringSchema, registry.resolve("stringSchema"));
        assertSame(numberSchema, registry.resolve("numberSchema"));
        assertSame(booleanSchema, registry.resolve("booleanSchema"));
        assertSame(objectSchema, registry.resolve("objectSchema"));
    }

    @Test
    void testRegisterWithSpecialCharacters() {
        String[] specialNames = {
                "schema-with-dash",
                "schema_with_underscore",
                "schema.with.dots",
                "schema/with/slash",
                "schema:with:colon"
        };

        for (String name : specialNames) {
            registry.register(name, Any.INSTANCE);
            assertSame(Any.INSTANCE, registry.resolve(name));
        }
    }

    @Test
    void testUnregisterAndReRegister() {
        JSONObject obj1 = new JSONObject();
        obj1.put("const", "value1");
        JSONSchema schema1 = JSONSchema.of(obj1);

        JSONObject obj2 = new JSONObject();
        obj2.put("const", "value2");
        JSONSchema schema2 = JSONSchema.of(obj2);

        registry.register("test", schema1);
        assertSame(schema1, registry.resolve("test"));

        registry.unregister("test");
        assertNull(registry.resolve("test"));

        registry.register("test", schema2);
        assertSame(schema2, registry.resolve("test"));
    }

    @Test
    void testCaseSensitiveNames() {
        registry.register("MySchema", Any.INSTANCE);
        registry.register("myschema", Any.INSTANCE);
        registry.register("MYSCHEMA", Any.INSTANCE);

        // All three should be distinct
        assertNotNull(registry.resolve("MySchema"));
        assertNotNull(registry.resolve("myschema"));
        assertNotNull(registry.resolve("MYSCHEMA"));

        // Verify they're different entries (all map to Any.INSTANCE, so we can't distinguish by value)
        registry.unregister("MySchema");
        assertNull(registry.resolve("MySchema"));
        assertNotNull(registry.resolve("myschema"));
        assertNotNull(registry.resolve("MYSCHEMA"));
    }

    @Test
    void testEmptyName() {
        registry.register("", Any.INSTANCE);

        // Empty string should be a valid key
        assertSame(Any.INSTANCE, registry.resolve(""));
    }

    @Test
    void testUnicodeName() {
        String unicodeName = "模式-スキーマ-Schema";
        registry.register(unicodeName, Any.INSTANCE);

        assertSame(Any.INSTANCE, registry.resolve(unicodeName));
    }

    @Test
    void testRegisterNestedSchema() {
        // Create a nested object schema
        JSONObject nestedObj = new JSONObject();
        nestedObj.put("type", "object");
        nestedObj.put("additionalProperties", true);
        JSONSchema nestedSchema = JSONSchema.of(nestedObj);

        JSONObject parentObj = new JSONObject();
        parentObj.put("type", "object");
        parentObj.put("additionalProperties", false);
        JSONSchema parentSchema = JSONSchema.of(parentObj);

        registry.register("nested", nestedSchema);
        registry.register("parent", parentSchema);

        assertSame(nestedSchema, registry.resolve("nested"));
        assertSame(parentSchema, registry.resolve("parent"));
    }

    @Test
    void testRegisterArraySchema() {
        JSONObject arrayObj = new JSONObject();
        arrayObj.put("type", "array");
        arrayObj.put("items", true);
        JSONSchema arraySchema = JSONSchema.of(arrayObj);

        registry.register("arraySchema", arraySchema);

        assertSame(arraySchema, registry.resolve("arraySchema"));
    }
}

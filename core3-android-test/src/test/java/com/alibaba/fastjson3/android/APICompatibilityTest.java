package com.alibaba.fastjson3.android;

import com.alibaba.fastjson3.*;
import com.alibaba.fastjson3.filter.PropertyFilter;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Android API compatibility tests — verifies new JSON.java methods work
 * on the Android JAR (reflection-only, no ASM, no Vector API).
 */
public class APICompatibilityTest {

    public static class User {
        public String name;
        public int age;
        public User() {}
        public User(String name, int age) { this.name = name; this.age = age; }
    }

    private static final String USER_JSON = "{\"name\":\"Alice\",\"age\":30}";
    private static final String ARRAY_JSON = "[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}]";

    // ==================== toJSON / toJavaObject ====================

    @Test
    void testToJSON() {
        User user = new User("Alice", 30);
        Object result = JSON.toJSON(user);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("Alice", ((JSONObject) result).getString("name"));
    }

    @Test
    void testToJavaObject() {
        JSONObject obj = JSON.parseObject(USER_JSON);
        User user = JSON.toJavaObject(obj, User.class);
        assertEquals("Alice", user.name);
        assertEquals(30, user.age);
    }

    // ==================== copy / copyTo ====================

    @Test
    void testCopy() {
        User original = new User("Alice", 30);
        User copy = JSON.copy(original);
        assertNotSame(original, copy);
        assertEquals("Alice", copy.name);
        assertEquals(30, copy.age);
    }

    @Test
    void testCopyTo() {
        User user = new User("Alice", 30);
        User copy = JSON.copyTo(user, User.class);
        assertNotSame(user, copy);
        assertEquals("Alice", copy.name);
    }

    // ==================== config / isEnabled ====================

    @Test
    void testConfig() {
        JSON.config(ReadFeature.AllowSingleQuotes, false);
        try {
            JSON.config(ReadFeature.AllowSingleQuotes);
            assertTrue(JSON.isEnabled(ReadFeature.AllowSingleQuotes));
            JSONObject obj = JSON.parseObject("{'a':1}");
            assertEquals(1, obj.getIntValue("a"));
        } finally {
            JSON.config(ReadFeature.AllowSingleQuotes, false);
        }
    }

    @Test
    void testConfigWrite() {
        JSON.config(WriteFeature.PrettyFormat, false);
        try {
            JSON.config(WriteFeature.PrettyFormat);
            assertTrue(JSON.isEnabled(WriteFeature.PrettyFormat));
            String json = JSON.toJSONString(JSON.object("a", 1));
            assertTrue(json.contains("\n"));
        } finally {
            JSON.config(WriteFeature.PrettyFormat, false);
        }
    }

    // ==================== register / unregister ====================

    public static class Tag {
        public String value;
        public Tag() {}
        public Tag(String v) { this.value = v; }
    }

    @Test
    void testRegisterAndUnregister() {
        JSON.register(Tag.class, new ObjectReader<Tag>() {
            @Override
            public Tag readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
                parser.readAny();
                return new Tag("custom");
            }
        });
        try {
            assertEquals("custom", JSON.parseObject("{\"value\":\"x\"}", Tag.class).value);
        } finally {
            JSON.unregister(Tag.class);
        }
        assertEquals("original", JSON.parseObject("{\"value\":\"original\"}", Tag.class).value);
    }

    // ==================== writeTo ====================

    @Test
    void testWriteTo() {
        User user = new User("Alice", 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int written = JSON.writeTo(out, user);
        assertTrue(written > 0);
        String json = out.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("Alice"));
    }

    // ==================== Filter serialization ====================

    @Test
    void testPropertyFilter() {
        User user = new User("Alice", 30);
        PropertyFilter filter = (source, name, value) -> !"age".equals(name);
        String json = JSON.toJSONString(user, filter);
        assertTrue(json.contains("Alice"));
        assertFalse(json.contains("age"));
    }

    // ==================== TypeReference ====================

    @Test
    void testTypeReference() {
        // TypeReference with simple class works on Android reflection path
        User user = JSON.parseObject(USER_JSON, new TypeReference<User>() {});
        assertEquals("Alice", user.name);
    }

    // ==================== parseList / parseSet / parseMap ====================

    @Test
    void testParseList() {
        List<User> users = JSON.parseList(ARRAY_JSON, User.class);
        assertEquals(2, users.size());
    }

    @Test
    void testParseSet() {
        Set<String> set = JSON.parseSet("[\"a\",\"b\",\"a\"]", String.class);
        assertEquals(2, set.size());
    }

    @Test
    void testParseMap() {
        Map<String, Object> map = JSON.parseMap("{\"a\":1,\"b\":2}", Object.class);
        assertEquals(2, map.size());
    }

    // ==================== JSONPath ====================

    @Test
    void testEval() {
        String json = "{\"store\":{\"book\":[{\"title\":\"Hello\"}]}}";
        String title = JSON.eval(json, "$.store.book[0].title", String.class);
        assertEquals("Hello", title);
    }

    // ==================== object() / array() ====================

    @Test
    void testObjectFactory() {
        JSONObject obj = JSON.object("key", "value");
        assertEquals("value", obj.getString("key"));
    }

    @Test
    void testArrayFactory() {
        JSONArray arr = JSON.array(1, 2, 3);
        assertEquals(3, arr.size());
    }

    // ==================== parse from byte[] ====================

    @Test
    void testParseBytes() {
        byte[] bytes = USER_JSON.getBytes(StandardCharsets.UTF_8);
        Object result = JSON.parse(bytes);
        assertInstanceOf(JSONObject.class, result);
    }

    @Test
    void testParseObjectBytes() {
        byte[] bytes = USER_JSON.getBytes(StandardCharsets.UTF_8);
        User user = JSON.parseObject(bytes, User.class);
        assertEquals("Alice", user.name);
    }

    @Test
    void testParseArrayBytes() {
        byte[] bytes = ARRAY_JSON.getBytes(StandardCharsets.UTF_8);
        List<User> users = JSON.parseArray(bytes, User.class);
        assertEquals(2, users.size());
    }

    // ==================== mergePatch ====================

    @Test
    void testMergePatch() {
        String result = JSON.mergePatch("{\"a\":1,\"b\":2}", "{\"b\":null,\"c\":3}");
        JSONObject obj = JSON.parseObject(result);
        assertEquals(1, obj.getIntValue("a"));
        assertNull(obj.get("b"));
        assertEquals(3, obj.getIntValue("c"));
    }

    // ==================== isValid ====================

    @Test
    void testIsValid() {
        assertTrue(JSON.isValid(USER_JSON));
        assertFalse(JSON.isValid("{invalid"));
        assertTrue(JSON.isValidObject(USER_JSON));
        assertTrue(JSON.isValidArray("[1,2,3]"));
    }
}

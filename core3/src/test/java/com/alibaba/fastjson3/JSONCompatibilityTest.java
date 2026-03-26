package com.alibaba.fastjson3;

import com.alibaba.fastjson3.filter.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for fastjson2 API compatibility methods added to JSON class.
 */
public class JSONCompatibilityTest {
    // ==================== Test Data ====================

    public static class User {
        public String name;
        public int age;

        public User() {}
        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    private static final String USER_JSON = "{\"name\":\"Alice\",\"age\":30}";
    private static final String ARRAY_JSON = "[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}]";
    private static final byte[] USER_BYTES = USER_JSON.getBytes(StandardCharsets.UTF_8);
    private static final byte[] ARRAY_BYTES = ARRAY_JSON.getBytes(StandardCharsets.UTF_8);

    // ==================== Parse Overloads ====================

    @Test
    public void test_parse_bytes() {
        Object result = JSON.parse(USER_BYTES);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("Alice", ((JSONObject) result).getString("name"));
    }

    @Test
    public void test_parse_bytes_null() {
        assertNull(JSON.parse((byte[]) null));
        assertNull(JSON.parse(new byte[0]));
    }

    @Test
    public void test_parse_bytes_features() {
        // Single-quoted JSON requires AllowSingleQuotes
        byte[] singleQuoteJson = "{'name':'Alice'}".getBytes(StandardCharsets.UTF_8);
        Object result = JSON.parse(singleQuoteJson, ReadFeature.AllowSingleQuotes);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("Alice", ((JSONObject) result).getString("name"));
    }

    @Test
    public void test_parse_inputStream() {
        InputStream in = new ByteArrayInputStream(USER_BYTES);
        Object result = JSON.parse(in);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("Alice", ((JSONObject) result).getString("name"));
    }

    @Test
    public void test_parse_chars() {
        char[] chars = USER_JSON.toCharArray();
        Object result = JSON.parse(chars);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("Alice", ((JSONObject) result).getString("name"));
    }

    @Test
    public void test_parse_chars_null() {
        assertNull(JSON.parse((char[]) null));
        assertNull(JSON.parse(new char[0]));
    }

    // ==================== parseObject Overloads ====================

    @Test
    public void test_parseObject_bytes_type() {
        User user = JSON.parseObject(USER_BYTES, (Type) User.class);
        assertEquals("Alice", user.name);
        assertEquals(30, user.age);
    }

    @Test
    public void test_parseObject_bytes_type_features() {
        byte[] json = "{'name':'Alice','age':30}".getBytes(StandardCharsets.UTF_8);
        User user = JSON.parseObject(json, (Type) User.class, ReadFeature.AllowSingleQuotes);
        assertEquals("Alice", user.name);
    }

    @Test
    public void test_parseObject_string_type_features() {
        String json = "{'name':'Alice','age':30}";
        User user = JSON.parseObject(json, (Type) User.class, ReadFeature.AllowSingleQuotes);
        assertEquals("Alice", user.name);
    }

    @Test
    public void test_parseObject_chars() {
        JSONObject obj = JSON.parseObject(USER_JSON.toCharArray());
        assertEquals("Alice", obj.getString("name"));
    }

    @Test
    public void test_parseObject_chars_null() {
        assertNull(JSON.parseObject((char[]) null));
    }

    @Test
    public void test_parseObject_chars_typed() {
        User user = JSON.parseObject(USER_JSON.toCharArray(), User.class);
        assertEquals("Alice", user.name);
    }

    @Test
    public void test_parseObject_reader_no_type() {
        Reader reader = new StringReader(USER_JSON);
        JSONObject obj = JSON.parseObject(reader);
        assertEquals("Alice", obj.getString("name"));
        assertEquals(30, obj.getIntValue("age"));
    }

    // ==================== parseArray Overloads ====================

    @Test
    public void test_parseArray_string_type_features() {
        String json = "[{'name':'Alice','age':30}]";
        List<User> users = JSON.parseArray(json, User.class, ReadFeature.AllowSingleQuotes);
        assertEquals(1, users.size());
        assertEquals("Alice", users.get(0).name);
    }

    @Test
    public void test_parseArray_bytes_type() {
        List<User> users = JSON.parseArray(ARRAY_BYTES, User.class);
        assertEquals(2, users.size());
        assertEquals("Alice", users.get(0).name);
        assertEquals("Bob", users.get(1).name);
    }

    @Test
    public void test_parseArray_bytes_type_features() {
        byte[] json = "[{'name':'Alice'}]".getBytes(StandardCharsets.UTF_8);
        List<User> users = JSON.parseArray(json, User.class, ReadFeature.AllowSingleQuotes);
        assertEquals(1, users.size());
        assertEquals("Alice", users.get(0).name);
    }

    @Test
    public void test_parseArray_inputStream() {
        InputStream in = new ByteArrayInputStream(ARRAY_BYTES);
        JSONArray arr = JSON.parseArray(in);
        assertEquals(2, arr.size());
    }

    @Test
    public void test_parseArray_reader() {
        Reader reader = new StringReader(ARRAY_JSON);
        JSONArray arr = JSON.parseArray(reader);
        assertEquals(2, arr.size());
    }

    @Test
    public void test_parseArray_inputStream_typed() {
        InputStream in = new ByteArrayInputStream(ARRAY_BYTES);
        List<User> users = JSON.parseArray(in, User.class);
        assertEquals(2, users.size());
        assertEquals("Alice", users.get(0).name);
    }

    // ==================== Serialize with Filters ====================

    @Test
    public void test_toJSONString_with_propertyFilter() {
        User user = new User("Alice", 30);
        PropertyFilter filter = (source, name, value) -> !"age".equals(name);
        String json = JSON.toJSONString(user, filter);
        assertFalse(json.contains("age"));
        assertTrue(json.contains("Alice"));
    }

    @Test
    public void test_toJSONString_with_nameFilter() {
        User user = new User("Alice", 30);
        NameFilter filter = (source, name, value) -> name.toUpperCase();
        String json = JSON.toJSONString(user, filter);
        assertTrue(json.contains("NAME"));
        assertTrue(json.contains("AGE"));
    }

    @Test
    public void test_toJSONString_with_valueFilter() {
        User user = new User("Alice", 30);
        ValueFilter filter = (source, name, value) -> {
            if ("name".equals(name)) return "***";
            return value;
        };
        String json = JSON.toJSONString(user, filter);
        assertTrue(json.contains("***"));
        assertFalse(json.contains("Alice"));
    }

    @Test
    public void test_toJSONBytes_with_filters() {
        User user = new User("Alice", 30);
        PropertyFilter filter = (source, name, value) -> !"age".equals(name);
        byte[] bytes = JSON.toJSONBytes(user, new Filter[]{filter});
        String json = new String(bytes, StandardCharsets.UTF_8);
        assertFalse(json.contains("age"));
        assertTrue(json.contains("Alice"));
    }

    // ==================== writeTo OutputStream ====================

    @Test
    public void test_writeTo() throws Exception {
        User user = new User("Alice", 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int written = JSON.writeTo(out, user);
        assertTrue(written > 0);
        String json = out.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("Alice"));
        assertEquals(written, out.size());
    }

    @Test
    public void test_writeTo_null() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int written = JSON.writeTo(out, null);
        assertEquals(4, written); // "null"
        assertEquals("null", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void test_writeTo_with_features() throws Exception {
        User user = new User("Alice", 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out, user, WriteFeature.PrettyFormat);
        String json = out.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("\n")); // pretty formatted
    }

    @Test
    public void test_writeTo_with_filters() throws Exception {
        User user = new User("Alice", 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PropertyFilter filter = (source, name, value) -> !"age".equals(name);
        JSON.writeTo(out, user, new Filter[]{filter});
        String json = out.toString(StandardCharsets.UTF_8);
        assertFalse(json.contains("age"));
    }

    // ==================== Conversion ====================

    @Test
    public void test_toJSON() {
        User user = new User("Alice", 30);
        Object result = JSON.toJSON(user);
        assertInstanceOf(JSONObject.class, result);
        JSONObject obj = (JSONObject) result;
        assertEquals("Alice", obj.getString("name"));
        assertEquals(30, obj.getIntValue("age"));
    }

    @Test
    public void test_toJSON_null() {
        assertNull(JSON.toJSON(null));
    }

    @Test
    public void test_toJSON_passthrough() {
        JSONObject obj = new JSONObject();
        assertSame(obj, JSON.toJSON(obj));

        JSONArray arr = new JSONArray();
        assertSame(arr, JSON.toJSON(arr));

        assertSame("hello", JSON.toJSON("hello"));
        assertEquals(42, JSON.toJSON(42));
        assertEquals(true, JSON.toJSON(true));
    }

    @Test
    public void test_toJavaObject() {
        JSONObject obj = JSON.parseObject(USER_JSON);
        User user = JSON.toJavaObject(obj, User.class);
        assertEquals("Alice", user.name);
        assertEquals(30, user.age);
    }

    @Test
    public void test_toJavaObject_null() {
        assertNull(JSON.toJavaObject(null, User.class));
    }

    @Test
    public void test_toJavaObject_sameType() {
        User original = new User("Alice", 30);
        User result = JSON.toJavaObject(original, User.class);
        assertSame(original, result); // same instance when already correct type
    }

    @Test
    public void test_copy() {
        User original = new User("Alice", 30);
        User copy = JSON.copy(original);
        assertNotSame(original, copy);
        assertEquals(original.name, copy.name);
        assertEquals(original.age, copy.age);
    }

    @Test
    public void test_copy_null() {
        assertNull(JSON.copy(null));
    }

    @Test
    public void test_copyTo() {
        User user = new User("Alice", 30);
        User copy = JSON.copyTo(user, User.class);
        assertNotSame(user, copy);
        assertEquals("Alice", copy.name);
        assertEquals(30, copy.age);
    }

    @Test
    public void test_copyTo_null() {
        assertNull(JSON.copyTo(null, User.class));
    }

    // ==================== Global Registration ====================

    public static class SpecialUser {
        public String tag;
        public SpecialUser() {}
        public SpecialUser(String tag) { this.tag = tag; }
    }

    @Test
    public void test_register_objectReader() {
        // Register a custom reader for SpecialUser (not User, to avoid contaminating other tests)
        JSON.register(SpecialUser.class, new ObjectReader<SpecialUser>() {
            @Override
            public SpecialUser readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
                parser.readAny(); // consume input
                return new SpecialUser("Custom");
            }
        });

        SpecialUser user = JSON.parseObject("{\"tag\":\"original\"}", SpecialUser.class);
        assertEquals("Custom", user.tag);
    }

    // ==================== Global Configuration ====================

    @Test
    public void test_config_readFeature() {
        assertFalse(JSON.isEnabled(ReadFeature.AllowComments));
        JSON.config(ReadFeature.AllowComments);
        assertTrue(JSON.isEnabled(ReadFeature.AllowComments));

        // Disable
        JSON.config(ReadFeature.AllowComments, false);
        assertFalse(JSON.isEnabled(ReadFeature.AllowComments));
    }

    @Test
    public void test_config_writeFeature() {
        assertFalse(JSON.isEnabled(WriteFeature.PrettyFormat));
        JSON.config(WriteFeature.PrettyFormat);
        assertTrue(JSON.isEnabled(WriteFeature.PrettyFormat));

        // Disable
        JSON.config(WriteFeature.PrettyFormat, false);
        assertFalse(JSON.isEnabled(WriteFeature.PrettyFormat));
    }

    // ==================== Validation ====================

    @Test
    public void test_isValid_chars() {
        assertTrue(JSON.isValid(USER_JSON.toCharArray()));
        assertFalse(JSON.isValid("{invalid".toCharArray()));
        assertFalse(JSON.isValid((char[]) null));
        assertFalse(JSON.isValid(new char[0]));
    }

    @Test
    public void test_isValidObject_chars() {
        assertTrue(JSON.isValidObject(USER_JSON.toCharArray()));
        assertFalse(JSON.isValidObject("[1,2]".toCharArray()));
    }

    @Test
    public void test_isValidArray_chars() {
        assertTrue(JSON.isValidArray("[1,2,3]".toCharArray()));
        assertFalse(JSON.isValidArray(USER_JSON.toCharArray()));
    }

    // ==================== config() effectiveness ====================

    @Test
    public void test_config_affects_parsing() {
        JSON.config(ReadFeature.AllowSingleQuotes, false);
        try {
            JSON.config(ReadFeature.AllowSingleQuotes);
            // Single-quoted JSON should work when AllowSingleQuotes is globally enabled
            JSONObject obj = JSON.parseObject("{'a':1}");
            assertEquals(1, obj.getIntValue("a"));
        } finally {
            JSON.config(ReadFeature.AllowSingleQuotes, false);
        }
    }

    @Test
    public void test_config_affects_parseObject_typed() {
        JSON.config(ReadFeature.AllowSingleQuotes, false);
        try {
            JSON.config(ReadFeature.AllowSingleQuotes);
            User user = JSON.parseObject("{'name':'Alice','age':30}", User.class);
            assertEquals("Alice", user.name);
        } finally {
            JSON.config(ReadFeature.AllowSingleQuotes, false);
        }
    }

    @Test
    public void test_config_affects_serialization() {
        JSON.config(WriteFeature.PrettyFormat, false);
        try {
            JSON.config(WriteFeature.PrettyFormat);
            String json = JSON.toJSONString(JSON.object("a", 1));
            assertTrue(json.contains("\n"));
        } finally {
            JSON.config(WriteFeature.PrettyFormat, false);
        }
    }

    @Test
    public void test_config_affects_toJSONBytes() {
        JSON.config(WriteFeature.PrettyFormat, false);
        try {
            JSON.config(WriteFeature.PrettyFormat);
            byte[] bytes = JSON.toJSONBytes(JSON.object("a", 1));
            String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(json.contains("\n"));
        } finally {
            JSON.config(WriteFeature.PrettyFormat, false);
        }
    }

    // ==================== unregister ====================

    @Test
    public void test_unregister() {
        // Register a custom reader
        JSON.register(SpecialUser.class, new ObjectReader<SpecialUser>() {
            @Override
            public SpecialUser readObject(JSONParser parser, java.lang.reflect.Type fieldType,
                                          Object fieldName, long features) {
                parser.readAny();
                return new SpecialUser("Custom");
            }
        });
        assertEquals("Custom", JSON.parseObject("{\"tag\":\"x\"}", SpecialUser.class).tag);

        // Unregister
        JSON.unregister(SpecialUser.class);
        // Should now use default deserialization
        SpecialUser user = JSON.parseObject("{\"tag\":\"original\"}", SpecialUser.class);
        assertEquals("original", user.tag);
    }

    @Test
    public void test_register_null_unregisters() {
        JSON.register(SpecialUser.class, (ObjectReader<?>) null);
        // Should use default deserialization (no NPE)
        SpecialUser user = JSON.parseObject("{\"tag\":\"test\"}", SpecialUser.class);
        assertEquals("test", user.tag);
    }
}

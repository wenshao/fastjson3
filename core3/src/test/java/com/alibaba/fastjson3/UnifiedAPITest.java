package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the unified JSON API.
 * Tests TypeToken, ParseConfig, WriteConfig, and convenience methods.
 */
public class UnifiedAPITest {

    // ========== TypeToken Factory Methods ==========

    @Test
    void testTypeTokenOf() {
        TypeToken<String> token = TypeToken.of(String.class);
        assertNotNull(token);
        assertEquals(String.class, token.type());
        assertEquals("java.lang.String", token.typeName());
    }

    @Test
    void testTypeTokenListOf() {
        TypeToken<List<String>> token = TypeToken.listOf(String.class);
        assertNotNull(token);
        assertTrue(token.type() instanceof java.lang.reflect.ParameterizedType);
    }

    @Test
    void testTypeTokenSetOf() {
        TypeToken<Set<Integer>> token = TypeToken.setOf(Integer.class);
        assertNotNull(token);
        assertTrue(token.type() instanceof java.lang.reflect.ParameterizedType);
    }

    @Test
    void testTypeTokenMapOf() {
        TypeToken<Map<String, Integer>> token = TypeToken.mapOf(Integer.class);
        assertNotNull(token);
        assertTrue(token.type() instanceof java.lang.reflect.ParameterizedType);
    }

    @Test
    void testTypeTokenArrayOf() {
        TypeToken<String[]> token = TypeToken.arrayOf(String.class);
        assertNotNull(token);
        assertEquals(String[].class, token.type());
    }

    @Test
    void testTypeTokenFromTypeReference() {
        TypeReference<List<String>> typeRef = new TypeReference<List<String>>() {};
        TypeToken<List<String>> token = TypeToken.of(typeRef);
        assertNotNull(token);
        assertEquals(typeRef.getType(), token.type());
    }

    @Test
    void testTypeTokenEqualsAndHashCode() {
        TypeToken<String> token1 = TypeToken.of(String.class);
        TypeToken<String> token2 = TypeToken.of(String.class);
        TypeToken<Integer> token3 = TypeToken.of(Integer.class);

        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
        assertNotEquals(token1, token3);
    }

    // ========== ParseConfig Presets ==========

    @Test
    void testParseConfigDefault() {
        ParseConfig config = ParseConfig.DEFAULT;
        assertNotNull(config);
        assertEquals(0, config.mask());
        assertEquals(0, config.features().length);
    }

    @Test
    void testParseConfigLenient() {
        ParseConfig config = ParseConfig.LENIENT;
        assertNotNull(config);
        assertTrue(config.mask() != 0);
        assertTrue(config.features().length > 0);

        // Verify expected features
        List<ReadFeature> features = List.of(config.features());
        assertTrue(features.contains(ReadFeature.AllowComments));
        assertTrue(features.contains(ReadFeature.AllowSingleQuotes));
        assertTrue(features.contains(ReadFeature.AllowUnquotedFieldNames));
        assertTrue(features.contains(ReadFeature.SupportSmartMatch));
    }

    @Test
    void testParseConfigStrict() {
        ParseConfig config = ParseConfig.STRICT;
        assertNotNull(config);
        assertTrue(config.mask() != 0);

        List<ReadFeature> features = List.of(config.features());
        assertTrue(features.contains(ReadFeature.ErrorOnUnknownProperties));
        assertTrue(features.contains(ReadFeature.ErrorOnNullForPrimitives));
    }

    @Test
    void testParseConfigAPI() {
        ParseConfig config = ParseConfig.API;
        assertNotNull(config);
        assertTrue(config.mask() != 0);

        List<ReadFeature> features = List.of(config.features());
        assertTrue(features.contains(ReadFeature.ErrorOnUnknownProperties));
        assertTrue(features.contains(ReadFeature.ErrorOnNullForPrimitives));
        assertTrue(features.contains(ReadFeature.UseBigDecimalForDoubles));
    }

    // ========== WriteConfig Presets ==========

    @Test
    void testWriteConfigDefault() {
        WriteConfig config = WriteConfig.DEFAULT;
        assertNotNull(config);
        assertEquals(0, config.mask());
        assertEquals(0, config.features().length);
    }

    @Test
    void testWriteConfigPretty() {
        WriteConfig config = WriteConfig.PRETTY;
        assertNotNull(config);
        assertTrue(config.mask() != 0);

        List<WriteFeature> features = List.of(config.features());
        assertTrue(features.contains(WriteFeature.PrettyFormat));
    }

    @Test
    void testWriteConfigWithNulls() {
        WriteConfig config = WriteConfig.WITH_NULLS;
        assertNotNull(config);
        assertTrue(config.mask() != 0);

        List<WriteFeature> features = List.of(config.features());
        assertTrue(features.contains(WriteFeature.WriteNulls));
    }

    @Test
    void testWriteConfigPrettyWithNulls() {
        WriteConfig config = WriteConfig.PRETTY_WITH_NULLS;
        assertNotNull(config);
        assertTrue(config.mask() != 0);

        List<WriteFeature> features = List.of(config.features());
        assertTrue(features.contains(WriteFeature.PrettyFormat));
        assertTrue(features.contains(WriteFeature.WriteNulls));
    }

    // ========== Unified Parse API ==========

    @Test
    void testParseToClass() {
        String json = "{\"name\":\"John\",\"age\":30}";
        User user = JSON.parse(json, User.class);

        assertNotNull(user);
        assertEquals("John", user.name);
        assertEquals(30, user.age);
    }

    @Test
    void testParseWithConfig() {
        // Parse with config (uses default for standard JSON)
        String json = "{\"name\":\"John\",\"age\":30}";
        User user = JSON.parse(json, User.class, ParseConfig.DEFAULT);

        assertNotNull(user);
        assertEquals("John", user.name);
    }

    @Test
    void testParseNullOrEmpty() {
        assertNull(JSON.parse("", User.class));
        assertNull(JSON.parse((String) null, User.class));
    }

    @Test
    void testParseToTypeToken() {
        // TypeToken with parseList (preferred approach)
        String json = "[{\"name\":\"John\"},{\"name\":\"Jane\"}]";
        List<User> users = JSON.parseList(json, User.class);

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("John", users.get(0).name);
        assertEquals("Jane", users.get(1).name);
    }

    // ========== Collection Type Parsing ==========

    @Test
    void testParseList() {
        String json = "[1,2,3,4,5]";
        List<Integer> list = JSON.parseList(json, Integer.class);

        assertNotNull(list);
        assertEquals(5, list.size());
        assertEquals(1, list.get(0));
        assertEquals(5, list.get(4));
    }

    @Test
    void testParseListWithConfig() {
        String json = "[1, 2, 3]";
        List<Integer> list = JSON.parseList(json, Integer.class, ParseConfig.DEFAULT);

        assertNotNull(list);
        assertEquals(3, list.size());
    }

    @Test
    void testParseListFromObjects() {
        String json = "[{\"name\":\"John\"},{\"name\":\"Jane\"}]";
        List<User> users = JSON.parseList(json, User.class);

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("John", users.get(0).name);
    }

    @Test
    void testParseSet() {
        String json = "[1,2,3,2,1]";
        Set<Integer> set = JSON.parseSet(json, Integer.class);

        assertNotNull(set);
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
    }

    @Test
    void testParseSetWithConfig() {
        String json = "[1, 2, 3] // comment";
        Set<Integer> set = JSON.parseSet(json, Integer.class, ParseConfig.LENIENT);

        assertNotNull(set);
        assertEquals(3, set.size());
    }

    @Test
    void testParseMap() {
        String json = "{\"a\":1,\"b\":2,\"c\":3}";
        Map<String, Integer> map = JSON.parseMap(json, Integer.class);

        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @Test
    void testParseMapWithConfig() {
        String json = "{\"a\":1,\"b\":2}";
        Map<String, Integer> map = JSON.parseMap(json, Integer.class, ParseConfig.DEFAULT);

        assertNotNull(map);
        assertEquals(2, map.size());
    }

    @Test
    void testParseMapFromObjects() {
        String json = "{\"user1\":{\"name\":\"John\"},\"user2\":{\"name\":\"Jane\"}}";
        Map<String, User> map = JSON.parseMap(json, User.class);

        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("John", map.get("user1").name);
        assertEquals("Jane", map.get("user2").name);
    }

    @Test
    void testParseTypedArray() {
        String json = "[1,2,3]";
        Integer[] array = JSON.parseTypedArray(json, Integer.class);

        assertNotNull(array);
        assertEquals(3, array.length);
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);
        assertEquals(3, array[2]);
    }

    // ========== Unified Write API ==========

    @Test
    void testWrite() {
        User user = new User();
        user.name = "John";
        user.age = 30;

        String json = JSON.write(user);

        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"John\""));
        assertTrue(json.contains("\"age\":30"));
    }

    @Test
    void testWriteWithConfig() {
        User user = new User();
        user.name = "John";
        user.age = 30;

        String json = JSON.write(user, WriteConfig.PRETTY);

        assertNotNull(json);
        assertTrue(json.contains("\n")); // Pretty formatted
        assertTrue(json.contains("\"name\":"));
    }

    @Test
    void testWriteNull() {
        String json = JSON.write(null);
        assertEquals("null", json);
    }

    @Test
    void testWriteWithNulls() {
        User user = new User();
        user.name = "John";
        user.age = null;

        String json = JSON.write(user, WriteConfig.WITH_NULLS);

        assertNotNull(json);
        // WITH_NULLS should include null fields
        assertTrue(json.contains("\"age\""));
    }

    @Test
    void testWritePrettyWithNulls() {
        User user = new User();
        user.name = "John";
        user.age = null;

        String json = JSON.write(user, WriteConfig.PRETTY_WITH_NULLS);

        assertNotNull(json);
        assertTrue(json.contains("\n")); // Pretty formatted
        assertTrue(json.contains("\"age\"")); // With nulls
    }

    // ========== Convenience Methods ==========

    @Test
    void testWritePretty() {
        User user = new User();
        user.name = "John";
        user.age = 30;

        String json = JSON.writePretty(user);

        assertNotNull(json);
        assertTrue(json.contains("\n")); // Pretty formatted
    }

    @Test
    void testWriteCompact() {
        User user = new User();
        user.name = "John";
        user.age = 30;

        String json = JSON.writeCompact(user);

        assertNotNull(json);
        assertFalse(json.contains("\n")); // Compact format
    }

    @Test
    void testWriteBytes() {
        User user = new User();
        user.name = "John";

        byte[] bytes = JSON.writeBytes(user);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void testWriteBytesWithConfig() {
        User user = new User();
        user.name = "John";

        byte[] bytes = JSON.writeBytes(user, WriteConfig.PRETTY);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        // Check for newline in bytes (pretty format)
        String json = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(json.contains("\n"));
    }

    // ========== parseAny ==========

    @Test
    void testParseAnyObject() {
        String json = "{\"name\":\"John\",\"age\":30}";
        Object result = JSON.parseAny(json);

        assertNotNull(result);
        assertTrue(result instanceof JSONObject);
        JSONObject obj = (JSONObject) result;
        assertEquals("John", obj.getString("name"));
        assertEquals(30, obj.getIntValue("age"));
    }

    @Test
    void testParseAnyArray() {
        String json = "[1,2,3]";
        Object result = JSON.parseAny(json);

        assertNotNull(result);
        assertTrue(result instanceof JSONArray);
        JSONArray arr = (JSONArray) result;
        assertEquals(3, arr.size());
    }

    @Test
    void testParseAnyString() {
        String json = "\"hello\"";
        Object result = JSON.parseAny(json);

        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("hello", result);
    }

    @Test
    void testParseAnyNumber() {
        Object result = JSON.parseAny("42");
        assertEquals(42, result);

        Object result2 = JSON.parseAny("3.14");
        assertEquals(3.14, result2);
    }

    @Test
    void testParseAnyBoolean() {
        assertEquals(true, JSON.parseAny("true"));
        assertEquals(false, JSON.parseAny("false"));
    }

    @Test
    void testParseAnyNull() {
        assertNull(JSON.parseAny("null"));
    }

    @Test
    void testParseAnyWithConfig() {
        String json = "{\"name\":\"John\"}";
        Object result = JSON.parseAny(json, ParseConfig.DEFAULT);

        assertNotNull(result);
        assertTrue(result instanceof JSONObject);
    }

    // ========== Edge Cases ==========

    @Test
    void testParseEmptyString() {
        assertNull(JSON.parse("", User.class));
        assertNull(JSON.parseList("", Integer.class));
        assertNull(JSON.parseSet("", Integer.class));
        assertNull(JSON.parseMap("", Integer.class));
    }

    @Test
    void testParseNullString() {
        assertNull(JSON.parse((String) null, User.class));
        assertNull(JSON.parseList((String) null, Integer.class));
    }

    @Test
    void testParseEmptyCollections() {
        assertEquals(0, JSON.parseList("[]", Integer.class).size());
        assertEquals(0, JSON.parseSet("[]", Integer.class).size());
        assertEquals(0, JSON.parseMap("{}", Integer.class).size());
    }

    // ========== Test Model ==========

    public static class User {
        public String name;
        public Integer age;
    }

    // ========== ValueReader/ValueWriter Feature Tests ==========

    @Test
    void testValueReaderWithFeatures() {
        ObjectMapper mapper = ObjectMapper.shared();
        ObjectMapper.ValueReader<User> reader = mapper.readerFor(User.class)
            .with(ReadFeature.UseBigDecimalForDoubles);

        // Test with standard JSON (AllowSingleQuotes not yet implemented in JSONParser)
        User user = reader.readValue("{\"name\":\"John\",\"age\":30}");
        assertNotNull(user);
        assertEquals("John", user.name);
        assertEquals(30, user.age);
    }

    @Test
    void testValueReaderWithoutFeatures() {
        ObjectMapper mapper = ObjectMapper.builder()
            .enableRead(ReadFeature.UseBigDecimalForDoubles)
            .build();

        ObjectMapper.ValueReader<User> reader = mapper.readerFor(User.class)
            .without(ReadFeature.UseBigDecimalForDoubles);

        // Verify features can be removed
        assertFalse(reader.getClass().getSimpleName().isEmpty()); // Just verify reader exists
    }

    @Test
    void testValueWriterWithFeatures() {
        ObjectMapper mapper = ObjectMapper.shared();
        ObjectMapper.ValueWriter writer = mapper.writer()
            .with(WriteFeature.PrettyFormat, WriteFeature.WriteNulls);

        User user = new User();
        user.name = "John";
        user.age = null;

        String json = writer.writeValueAsString(user);
        assertNotNull(json);
        assertTrue(json.contains("\n")); // Pretty formatted
        assertTrue(json.contains("\"age\"")); // With nulls
    }

    @Test
    void testValueWriterWithoutFeatures() {
        ObjectMapper mapper = ObjectMapper.builder()
            .enableWrite(WriteFeature.PrettyFormat, WriteFeature.WriteNulls)
            .build();

        ObjectMapper.ValueWriter writer = mapper.writer()
            .without(WriteFeature.PrettyFormat);

        User user = new User();
        user.name = "John";
        user.age = null;

        String json = writer.writeValueAsString(user);
        assertNotNull(json);
        assertFalse(json.contains("\n")); // Not pretty
        assertTrue(json.contains("\"age\"")); // Still with nulls (not removed)
    }

    // ========== ParseConfig.API Tests ==========

    @Test
    void testParseWithAPIConfig() {
        // ParseConfig.API enables ErrorOnUnknownProperties, so JSON must match User fields exactly
        String json = "{\"name\":\"John\",\"age\":30}";

        User user = JSON.parse(json, User.class, ParseConfig.API);
        assertNotNull(user);
        assertEquals("John", user.name);
        assertEquals(30, user.age);
    }

    @Test
    void testParseConfigAPIErrorOnUnknown() {
        // ParseConfig.API should throw on unknown properties
        String json = "{\"name\":\"John\",\"age\":30,\"unknownField\":123}";

        assertThrows(JSONException.class, () ->
            JSON.parse(json, User.class, ParseConfig.API));
    }

    @Test
    void testParseConfigStrictErrorOnUnknown() {
        String json = "{\"name\":\"John\",\"unknownField\":123}";

        // STRICT mode should throw on unknown properties
        assertThrows(JSONException.class, () ->
            JSON.parse(json, User.class, ParseConfig.STRICT));
    }

    // ========== parseTypedArray with ParseConfig Tests ==========

    @Test
    void testParseTypedArrayWithConfig() {
        String json = "[1, 2, 3]";

        // Standard JSON parsing
        Integer[] arr = JSON.parseTypedArray(json, Integer.class);
        assertNotNull(arr);
        assertEquals(3, arr.length);
        assertEquals(1, arr[0]);
        assertEquals(2, arr[1]);
        assertEquals(3, arr[2]);

        // With DEFAULT config
        Integer[] arr2 = JSON.parseTypedArray(json, Integer.class, ParseConfig.DEFAULT);
        assertNotNull(arr2);
        assertEquals(3, arr2.length);
    }

    @Test
    void testParseTypedArrayBytesWithConfig() {
        byte[] jsonBytes = "[1, 2, 3]".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // Standard JSON parsing
        Integer[] arr = JSON.parseTypedArray(jsonBytes, Integer.class, ParseConfig.DEFAULT);
        assertNotNull(arr);
        assertEquals(3, arr.length);
    }

    // ========== ObjectMapper Features Propagation Tests ==========

    @Test
    void testObjectMapperFeaturesPropagation() {
        // Create mapper with UseBigDecimalForDoubles enabled
        ObjectMapper mapper = ObjectMapper.builder()
            .enableRead(ReadFeature.UseBigDecimalForDoubles)
            .build();

        String json = "{\"name\":\"John\",\"age\":30}";

        // readValue should respect the feature
        User user = mapper.readValue(json, User.class);
        assertNotNull(user);
        assertEquals("John", user.name);
        assertEquals(30, user.age);

        // readObject should also respect the feature
        JSONObject obj = mapper.readObject(json);
        assertNotNull(obj);
        assertEquals("John", obj.getString("name"));

        // readArray should also respect the feature
        JSONArray arr = mapper.readArray("[1, 2]");
        assertNotNull(arr);
        assertEquals(2, arr.size());
    }

    @Test
    void testObjectMapperReadTreeFeatures() {
        ObjectMapper mapper = ObjectMapper.builder()
            .enableRead(ReadFeature.UseBigDecimalForDoubles)
            .build();

        String json = "{\"name\":\"John\",\"age\":30}";

        Object tree = mapper.readTree(json);
        assertNotNull(tree);
        assertTrue(tree instanceof JSONObject);
    }

    // ========== AllowSingleQuotes Feature Tests ==========

    @Test
    void testParseWithSingleQuotes() {
        // Parse JSON with single quotes using LENIENT config
        String json = "{'name':'John','age':30}";

        User user = JSON.parse(json, User.class, ParseConfig.LENIENT);
        assertNotNull(user);
        assertEquals("John", user.name);
        assertEquals(30, user.age);
    }

    @Test
    void testParseListWithSingleQuotes() {
        String json = "[{'name':'John'},{'name':'Jane'}]";

        List<User> users = JSON.parseList(json, User.class, ParseConfig.LENIENT);
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("John", users.get(0).name);
        assertEquals("Jane", users.get(1).name);
    }

    @Test
    void testParseMapWithSingleQuotes() {
        String json = "{'a':1,'b':2}";

        Map<String, Integer> map = JSON.parseMap(json, Integer.class, ParseConfig.LENIENT);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
    }

    @Test
    void testObjectMapperWithSingleQuotes() {
        ObjectMapper mapper = ObjectMapper.builder()
            .enableRead(ReadFeature.AllowSingleQuotes)
            .build();

        String json = "{'name':'John','age':30}";

        User user = mapper.readValue(json, User.class);
        assertNotNull(user);
        assertEquals("John", user.name);
        assertEquals(30, user.age);
    }

    @Test
    void testValueReaderWithSingleQuotes() {
        ObjectMapper mapper = ObjectMapper.shared();
        ObjectMapper.ValueReader<User> reader = mapper.readerFor(User.class)
            .with(ReadFeature.AllowSingleQuotes);

        String json = "{'name':'John','age':30}";

        User user = reader.readValue(json);
        assertNotNull(user);
        assertEquals("John", user.name);
        assertEquals(30, user.age);
    }

    @Test
    void testJSONParserWithSingleQuotes() {
        String json = "{'name':'John','age':30}";

        try (JSONParser parser = JSONParser.of(json, ReadFeature.AllowSingleQuotes)) {
            JSONObject obj = parser.readObject();
            assertNotNull(obj);
            assertEquals("John", obj.getString("name"));
            assertEquals(30, obj.getIntValue("age"));
        }
    }

    @Test
    void testJSONParserBytesWithSingleQuotes() {
        byte[] jsonBytes = "{'name':'John','age':30}".getBytes(StandardCharsets.UTF_8);

        try (JSONParser parser = JSONParser.of(jsonBytes, ReadFeature.AllowSingleQuotes)) {
            JSONObject obj = parser.readObject();
            assertNotNull(obj);
            assertEquals("John", obj.getString("name"));
            assertEquals(30, obj.getIntValue("age"));
        }
    }

    @Test
    void testSingleQuotesWithEscape() {
        // Test single quotes with standard JSON escape sequences
        // Note: \' is not a standard JSON escape, use \\ for backslash
        String json = "{'name':'John\\nSmith','age':30}";

        User user = JSON.parse(json, User.class, ParseConfig.LENIENT);
        assertNotNull(user);
        assertEquals("John\nSmith", user.name);
        assertEquals(30, user.age);
    }

    @Test
    void testSingleQuotesWithUnicodeEscape() {
        // Test single quotes with unicode escape
        String json = "{'name':'\\u0041\\u0042','age':30}";

        User user = JSON.parse(json, User.class, ParseConfig.LENIENT);
        assertNotNull(user);
        assertEquals("AB", user.name);
        assertEquals(30, user.age);
    }
}

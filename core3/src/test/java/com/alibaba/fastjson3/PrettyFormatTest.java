package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for WriteFeature.PrettyFormat.
 */
public class PrettyFormatTest {

    // ========== Simple objects ==========

    @Test
    void testSimpleObject() {
        var obj = new JSONObject();
        obj.put("name", "John");
        obj.put("age", 30);
        String result = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
        // JSONObject preserves insertion order
        String expected = "{\n  \"name\": \"John\",\n  \"age\": 30\n}";
        assertEquals(expected, result);
    }

    @Test
    void testEmptyObject() {
        String result = JSON.toJSONString(new JSONObject(), WriteFeature.PrettyFormat);
        assertEquals("{}", result);
    }

    @Test
    void testEmptyArray() {
        String result = JSON.toJSONString(new JSONArray(), WriteFeature.PrettyFormat);
        assertEquals("[]", result);
    }

    // ========== Arrays ==========

    @Test
    void testSimpleArray() {
        var arr = new JSONArray();
        arr.add("a");
        arr.add("b");
        arr.add("c");
        String result = JSON.toJSONString(arr, WriteFeature.PrettyFormat);
        String expected = "[\n  \"a\",\n  \"b\",\n  \"c\"\n]";
        assertEquals(expected, result);
    }

    @Test
    void testIntArray() {
        var arr = new JSONArray();
        arr.add(1);
        arr.add(2);
        arr.add(3);
        String result = JSON.toJSONString(arr, WriteFeature.PrettyFormat);
        String expected = "[\n  1,\n  2,\n  3\n]";
        assertEquals(expected, result);
    }

    // ========== Nested structures ==========

    @Test
    void testNestedObject() {
        var inner = new JSONObject();
        inner.put("city", "NYC");
        var outer = new JSONObject();
        outer.put("address", inner);
        String result = JSON.toJSONString(outer, WriteFeature.PrettyFormat);
        String expected = "{\n  \"address\": {\n    \"city\": \"NYC\"\n  }\n}";
        assertEquals(expected, result);
    }

    @Test
    void testObjectWithArray() {
        var arr = new JSONArray();
        arr.add("x");
        arr.add("y");
        var obj = new JSONObject();
        obj.put("items", arr);
        String result = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
        String expected = "{\n  \"items\": [\n    \"x\",\n    \"y\"\n  ]\n}";
        assertEquals(expected, result);
    }

    @Test
    void testArrayOfObjects() {
        var obj1 = new JSONObject();
        obj1.put("id", 1);
        var obj2 = new JSONObject();
        obj2.put("id", 2);
        var arr = new JSONArray();
        arr.add(obj1);
        arr.add(obj2);
        String result = JSON.toJSONString(arr, WriteFeature.PrettyFormat);
        String expected = "[\n  {\n    \"id\": 1\n  },\n  {\n    \"id\": 2\n  }\n]";
        assertEquals(expected, result);
    }

    // ========== POJO serialization ==========

    public static class User {
        private String name;
        private int age;
        private List<String> tags;

        public User() {}
        public User(String name, int age, List<String> tags) {
            this.name = name;
            this.age = age;
            this.tags = tags;
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    @Test
    void testPojoWithList() {
        var user = new User("Alice", 25, List.of("dev", "admin"));
        String result = JSON.toJSONString(user, WriteFeature.PrettyFormat);
        // Verify structure: contains newlines and proper indentation
        assertTrue(result.contains("\"name\": \"Alice\""));
        assertTrue(result.contains("\"age\": 25"));
        assertTrue(result.contains("\"tags\": [\n"));
        assertTrue(result.contains("    \"dev\""));
        assertTrue(result.contains("    \"admin\""));
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
        // Verify it's valid JSON that roundtrips
        User parsed = JSON.parseObject(result, User.class);
        assertEquals("Alice", parsed.getName());
        assertEquals(25, parsed.getAge());
        assertEquals(List.of("dev", "admin"), parsed.getTags());
    }

    // ========== POJO with nested POJO ==========

    public static class Address {
        private String city;
        private String zip;
        public Address() {}
        public Address(String city, String zip) { this.city = city; this.zip = zip; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }
    }

    public static class Person {
        private String name;
        private Address address;
        public Person() {}
        public Person(String name, Address address) { this.name = name; this.address = address; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }
    }

    @Test
    void testNestedPojo() {
        var person = new Person("Bob", new Address("NYC", "10001"));
        String result = JSON.toJSONString(person, WriteFeature.PrettyFormat);
        assertTrue(result.contains("\"address\": {"));
        assertTrue(result.contains("    \"city\": \"NYC\""));
        assertTrue(result.contains("    \"zip\": \"10001\""));
        // Roundtrip
        Person parsed = JSON.parseObject(result, Person.class);
        assertEquals("Bob", parsed.getName());
        assertEquals("NYC", parsed.getAddress().getCity());
    }

    // ========== UTF-8 byte[] output ==========

    @Test
    void testPrettyBytes() {
        var obj = new JSONObject();
        obj.put("key", "value");
        byte[] bytes = JSON.toJSONBytes(obj, WriteFeature.PrettyFormat);
        String result = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        String expected = "{\n  \"key\": \"value\"\n}";
        assertEquals(expected, result);
    }

    @Test
    void testPrettyBytesNestedArray() {
        var arr = new JSONArray();
        arr.add(1);
        arr.add(2);
        var obj = new JSONObject();
        obj.put("nums", arr);
        byte[] bytes = JSON.toJSONBytes(obj, WriteFeature.PrettyFormat);
        String result = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(result.contains("\"nums\": [\n    1,\n    2\n  ]"));
    }

    // ========== Compact mode unaffected ==========

    @Test
    void testCompactModeUnchanged() {
        var obj = new JSONObject();
        obj.put("a", 1);
        obj.put("b", "hello");
        String compact = JSON.toJSONString(obj);
        assertFalse(compact.contains("\n"));
        assertFalse(compact.contains("  "));
        // No space after colon
        assertTrue(compact.contains("\"a\":1") || compact.contains("\"b\":\"hello\""));
    }

    // ========== Special values ==========

    @Test
    void testNullValues() {
        var obj = new JSONObject();
        obj.put("a", null);
        obj.put("b", "ok");
        // Without WriteNulls, null fields are omitted
        String result = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
        assertTrue(result.contains("\"b\": \"ok\""));
    }

    @Test
    void testNullValuesWithWriteNulls() {
        var obj = new JSONObject();
        obj.put("a", null);
        obj.put("b", "ok");
        String result = JSON.toJSONString(obj, WriteFeature.PrettyFormat, WriteFeature.WriteNulls);
        assertTrue(result.contains("\"a\": null"));
        assertTrue(result.contains("\"b\": \"ok\""));
    }

    @Test
    void testBooleanValues() {
        var obj = new JSONObject();
        obj.put("yes", true);
        obj.put("no", false);
        String result = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
        assertTrue(result.contains("\"yes\": true"));
        assertTrue(result.contains("\"no\": false"));
    }

    @Test
    void testDoubleValues() {
        var obj = new JSONObject();
        obj.put("pi", 3.14);
        String result = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
        assertTrue(result.contains("\"pi\": 3.14"));
    }

    // ========== Collection / Map via writeAny ==========

    @Test
    void testMapPretty() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", 1);
        map.put("y", 2);
        String result = JSON.toJSONString(map, WriteFeature.PrettyFormat);
        assertEquals("{\n  \"x\": 1,\n  \"y\": 2\n}", result);
    }

    @Test
    void testListPretty() {
        List<String> list = List.of("hello", "world");
        String result = JSON.toJSONString(list, WriteFeature.PrettyFormat);
        assertEquals("[\n  \"hello\",\n  \"world\"\n]", result);
    }

    // ========== ObjectMapper API ==========

    @Test
    void testMapperPretty() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.PrettyFormat)
                .build();
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("k", "v");
        String result = mapper.writeValueAsString(obj);
        assertEquals("{\n  \"k\": \"v\"\n}", result);
    }

    // ========== Deeply nested ==========

    @Test
    void testDeeplyNested() {
        var inner = new JSONObject();
        inner.put("val", 42);
        var mid = new JSONObject();
        mid.put("inner", inner);
        var outer = new JSONObject();
        outer.put("mid", mid);

        String result = JSON.toJSONString(outer, WriteFeature.PrettyFormat);
        String expected = "{\n  \"mid\": {\n    \"inner\": {\n      \"val\": 42\n    }\n  }\n}";
        assertEquals(expected, result);
    }

    // ========== Mixed nested ==========

    @Test
    void testComplexMixed() {
        var obj = new JSONObject();
        obj.put("name", "test");
        obj.put("count", 3);
        var items = new JSONArray();
        var item1 = new JSONObject();
        item1.put("id", 1);
        item1.put("tags", new JSONArray(List.of("a", "b")));
        items.add(item1);
        obj.put("items", items);

        String result = JSON.toJSONString(obj, WriteFeature.PrettyFormat);
        // Verify structure
        assertTrue(result.startsWith("{\n"));
        assertTrue(result.endsWith("\n}"));
        assertTrue(result.contains("  \"name\": \"test\""));
        assertTrue(result.contains("  \"items\": [\n"));
        assertTrue(result.contains("    {\n"));
        assertTrue(result.contains("      \"id\": 1"));
        assertTrue(result.contains("      \"tags\": [\n"));
        assertTrue(result.contains("        \"a\""));

        // Roundtrip
        JSONObject parsed = JSON.parseObject(result);
        assertEquals("test", parsed.getString("name"));
        assertEquals(3, parsed.getIntValue("count"));
    }

    // ========== POJO with List<POJO> ==========

    public static class Team {
        private String name;
        private List<User> members;
        public Team() {}
        public Team(String name, List<User> members) { this.name = name; this.members = members; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<User> getMembers() { return members; }
        public void setMembers(List<User> members) { this.members = members; }
    }

    @Test
    void testPojoWithListOfPojos() {
        var team = new Team("dev", List.of(
                new User("Alice", 25, null),
                new User("Bob", 30, null)
        ));
        String result = JSON.toJSONString(team, WriteFeature.PrettyFormat);
        assertTrue(result.contains("\"members\": [\n"));
        assertTrue(result.contains("      \"name\": \"Alice\""));
        assertTrue(result.contains("      \"name\": \"Bob\""));
        // Verify it roundtrips as JSON
        JSONObject parsed = JSON.parseObject(result);
        assertEquals("dev", parsed.getString("name"));
        JSONArray parsedMembers = parsed.getJSONArray("members");
        assertEquals(2, parsedMembers.size());
    }

    // ========== Single value (no container) ==========

    @Test
    void testSingleString() {
        String result = JSON.toJSONString("hello", WriteFeature.PrettyFormat);
        assertEquals("\"hello\"", result);
    }

    @Test
    void testSingleInt() {
        String result = JSON.toJSONString(42, WriteFeature.PrettyFormat);
        assertEquals("42", result);
    }

    // ========== Large array (buffer overflow regression test) ==========

    @Test
    void testLargeArrayBytes() {
        // Tests that UTF8 path handles arrays exceeding SAFE_MARGIN (512 bytes)
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            list.add(i);
        }
        // Nest inside objects to increase indent level
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("level1", Map.of("level2", Map.of("level3", Map.of("data", list))));
        byte[] bytes = JSON.toJSONBytes(nested, WriteFeature.PrettyFormat);
        String result = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        // Verify structure
        assertTrue(result.contains("        \"data\": [\n"));
        assertTrue(result.contains("          0,\n"));
        assertTrue(result.contains("          199\n"));
        // Verify roundtrip
        JSONObject parsed = JSON.parseObject(result);
        assertNotNull(parsed);
    }

    @Test
    void testLargeArrayString() {
        // Tests that Char path handles large arrays
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            list.add(i);
        }
        String result = JSON.toJSONString(list, WriteFeature.PrettyFormat);
        assertTrue(result.startsWith("[\n  0,\n"));
        assertTrue(result.endsWith("199\n]"));
        // Roundtrip
        JSONArray parsed = JSON.parseArray(result);
        assertEquals(200, parsed.size());
        assertEquals(0, parsed.getIntValue(0));
        assertEquals(199, parsed.getIntValue(199));
    }
}

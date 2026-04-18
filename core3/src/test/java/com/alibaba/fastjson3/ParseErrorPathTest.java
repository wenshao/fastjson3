package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Breadcrumb path attached to {@link JSONException} when structured parse fails.
 * Path is built innermost-first during stack unwind at each field / element /
 * entry read site, and formatted outermost-first in {@code getMessage()}.
 */
public class ParseErrorPathTest {

    public static class Address {
        public String city;
        public int zipcode;
    }

    public static class User {
        public String name;
        public int age;
        public Address address;
    }

    public static class Team {
        public String name;
        public List<User> members;
    }

    public static class Org {
        public String id;
        public Map<String, Team> teams;
    }

    // ==================== Single-level nested POJO field ====================

    @Test
    public void nestedPojoField() {
        // zipcode is int; "abc" forces a parse error inside address.
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"name\":\"Alice\",\"age\":30,"
                        + "\"address\":{\"city\":\"SF\",\"zipcode\":\"abc\"}}", User.class));
        assertNotNull(e.getPath());
        // Innermost first: [zipcode, address]
        assertEquals("zipcode", e.getPath().get(0));
        assertEquals("address", e.getPath().get(1));
        assertTrue(e.getMessage().contains("address.zipcode"),
                "message should include 'address.zipcode': " + e.getMessage());
    }

    // ==================== List element index ====================

    @Test
    public void listElementIndexPath() {
        // members[1].age is the bad field.
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"name\":\"red\","
                        + "\"members\":[{\"name\":\"a\",\"age\":1},"
                        + "{\"name\":\"b\",\"age\":\"bad\"}]}", Team.class));
        assertNotNull(e.getPath());
        assertTrue(e.getMessage().contains("members[1].age"),
                "message should include 'members[1].age': " + e.getMessage());
    }

    // ==================== Map value ====================

    @Test
    public void mapValuePath() {
        // teams.blue.members[0].age is the bad field
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"id\":\"org1\",\"teams\":{"
                        + "\"red\":{\"name\":\"red\",\"members\":[]},"
                        + "\"blue\":{\"name\":\"blue\",\"members\":[{\"name\":\"x\",\"age\":\"bad\"}]}}}",
                        Org.class));
        assertNotNull(e.getPath());
        assertTrue(e.getMessage().contains("teams.blue.members[0].age"),
                "message should include 'teams.blue.members[0].age': " + e.getMessage());
    }

    // ==================== Direct TypeReference: List<User> ====================

    @Test
    public void typeReferenceListElementPath() {
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("[{\"name\":\"a\",\"age\":1},"
                        + "{\"name\":\"b\",\"age\":\"bad\"}]",
                        new TypeReference<List<User>>() {}));
        assertTrue(e.getMessage().contains("[1].age"),
                "message should include '[1].age': " + e.getMessage());
    }

    // ==================== Nested list: List<List<User>> ====================

    @Test
    public void nestedListPath() {
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("[[{\"name\":\"a\",\"age\":1}],"
                        + "[{\"name\":\"b\",\"age\":\"bad\"}]]",
                        new TypeReference<List<List<User>>>() {}));
        assertTrue(e.getMessage().contains("[1][0].age"),
                "message should include '[1][0].age': " + e.getMessage());
    }

    // ==================== TypeReference<Map<String, User>> ====================

    @Test
    public void typeReferenceMapValuePath() {
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"alice\":{\"name\":\"a\",\"age\":1},"
                        + "\"bob\":{\"name\":\"b\",\"age\":\"bad\"}}",
                        new TypeReference<Map<String, User>>() {}));
        assertTrue(e.getMessage().contains("bob.age"),
                "message should include 'bob.age': " + e.getMessage());
    }

    // ==================== Record ====================

    public record RecordUser(String name, int age, Address address) {}

    @Test
    public void recordFieldPath() {
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"name\":\"a\",\"age\":1,"
                        + "\"address\":{\"city\":\"SF\",\"zipcode\":\"bad\"}}",
                        RecordUser.class));
        assertTrue(e.getMessage().contains("address.zipcode"),
                "record message should include 'address.zipcode': " + e.getMessage());
    }

    // ==================== No path when failure is top-level ====================

    @Test
    public void topLevelFailureNoPath() {
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("not json", User.class));
        // Top-level failures have no path — getPath() is null.
        if (e.getPath() != null) {
            assertTrue(e.getPath().isEmpty(),
                    "top-level failures should have empty path, got: " + e.getPath());
        }
        // getMessage() should not add "(path: ...)" for empty/no path
        assertTrue(!e.getMessage().contains("(path:"),
                "top-level error should not include '(path:': " + e.getMessage());
    }

    // ==================== Raw message vs formatted message ====================

    @Test
    public void rawMessageExcludesPath() {
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"name\":\"a\",\"age\":1,"
                        + "\"address\":{\"city\":\"SF\",\"zipcode\":\"bad\"}}", User.class));
        // Raw message is the original throw-site text, without the "(path: ...)" suffix.
        assertTrue(!e.getRawMessage().contains("(path:"),
                "rawMessage must not include '(path:': " + e.getRawMessage());
        assertTrue(e.getMessage().contains("(path:"),
                "getMessage must include '(path:': " + e.getMessage());
    }

    // ==================== Type mismatch inside a POJO — was CCE pre-fix ====================

    @Test
    public void intFieldWithStringValue() {
        // Pre-fix: leaked a raw ClassCastException with no path context.
        // Post-fix: JSONException wraps the CCE with the failing field's path.
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"name\":\"a\",\"age\":\"bad\"}", User.class));
        assertTrue(e.getMessage().contains("age"),
                "message should include 'age': " + e.getMessage());
    }

    // ==================== Null rawMessage with path — regression guard ====================

    @Test
    public void getMessageTolerantOfNullRawMessage() {
        // Throwable allows a null message; getMessage() must not NPE when a
        // path has been attached.
        JSONException e = new JSONException(null);
        e.prependPath("field");
        String formatted = e.getMessage();
        assertNotNull(formatted);
        assertTrue(formatted.contains("(path: field)"),
                "null raw + path should still format: " + formatted);
    }

    // ==================== Typed Map key conversion carries the key path ====================

    @Test
    public void mapIntegerKeyConversionFailureHasPath() {
        // "abc" can't be parsed as Integer; previously the key-conversion
        // throw site ran outside the path-tagging try/catch, so the breadcrumb
        // was missing on Map<Integer, ?> conversion failures.
        JSONException e = assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"abc\":1}", new TypeReference<Map<Integer, Integer>>() {}));
        assertNotNull(e.getPath(), "map integer key conversion should attach path");
        assertEquals("abc", e.getPath().get(0));
        assertTrue(e.getMessage().contains("abc"),
                "message should include failing key 'abc': " + e.getMessage());
    }
}

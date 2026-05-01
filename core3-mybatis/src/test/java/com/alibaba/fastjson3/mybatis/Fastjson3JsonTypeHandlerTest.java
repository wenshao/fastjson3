package com.alibaba.fastjson3.mybatis;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Fastjson3JsonTypeHandlerTest {
    public static class Profile {
        public String name;
        public int age;

        public Profile() {
        }

        public Profile(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    public static class ProfileJsonTypeHandler extends Fastjson3JsonTypeHandler<Profile> {
        public ProfileJsonTypeHandler() {
            super(Profile.class);
        }
    }

    public static class TagsJsonTypeHandler extends Fastjson3JsonTypeHandler<List<String>> {
        public TagsJsonTypeHandler() {
            super(new TypeReference<List<String>>() {
            });
        }
    }

    private static Connection conn;

    @BeforeAll
    static void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:fj3-mybatis;DB_CLOSE_DELAY=-1");
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS docs (id INT PRIMARY KEY, payload VARCHAR(4000))");
        }
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    @Test
    void simpleTypeRoundTrip() throws SQLException {
        ProfileJsonTypeHandler handler = new ProfileJsonTypeHandler();
        Profile src = new Profile("alice", 30);
        try (PreparedStatement ins = conn.prepareStatement(
                "MERGE INTO docs (id, payload) VALUES (1, ?)")) {
            handler.setNonNullParameter(ins, 1, src, null);
            ins.executeUpdate();
        }
        try (PreparedStatement sel = conn.prepareStatement(
                "SELECT payload FROM docs WHERE id = 1")) {
            try (ResultSet rs = sel.executeQuery()) {
                rs.next();
                Profile back = handler.getNullableResult(rs, "payload");
                assertEquals("alice", back.name);
                assertEquals(30, back.age);
            }
        }
    }

    @Test
    void parameterizedTypeRoundTripByColumnIndex() throws SQLException {
        TagsJsonTypeHandler handler = new TagsJsonTypeHandler();
        List<String> tags = Arrays.asList("java", "json");
        try (PreparedStatement ins = conn.prepareStatement(
                "MERGE INTO docs (id, payload) VALUES (2, ?)")) {
            handler.setNonNullParameter(ins, 1, tags, null);
            ins.executeUpdate();
        }
        try (PreparedStatement sel = conn.prepareStatement(
                "SELECT payload FROM docs WHERE id = 2")) {
            try (ResultSet rs = sel.executeQuery()) {
                rs.next();
                List<String> back = handler.getNullableResult(rs, 1);
                assertEquals(tags, back);
            }
        }
    }

    @Test
    void callableStatementOverload() throws SQLException {
        // Stored-proc OUT parameter columns route through
        // getNullableResult(CallableStatement, int) — the third overload
        // BaseTypeHandler declares. H2's stored-proc OUT-param dance is
        // heavy, so use a Proxy-based CallableStatement that returns a
        // fixed JSON string for cs.getString(int) and drives the parse
        // path the production code takes.
        ProfileJsonTypeHandler handler = new ProfileJsonTypeHandler();
        String fixedJson = "{\"name\":\"c\",\"age\":1}";
        CallableStatement cs = (CallableStatement) java.lang.reflect.Proxy.newProxyInstance(
                CallableStatement.class.getClassLoader(),
                new Class<?>[]{CallableStatement.class},
                (p, m, a) -> {
                    if ("getString".equals(m.getName()) && a.length == 1
                            && a[0] instanceof Integer && (Integer) a[0] == 1) {
                        return fixedJson;
                    }
                    if ("wasNull".equals(m.getName())) {
                        return false;
                    }
                    return null;
                });
        Profile back = handler.getNullableResult(cs, 1);
        assertEquals("c", back.name);
        assertEquals(1, back.age);
    }

    @Test
    void callableStatementOverloadNullColumn() throws SQLException {
        ProfileJsonTypeHandler handler = new ProfileJsonTypeHandler();
        CallableStatement cs = (CallableStatement) java.lang.reflect.Proxy.newProxyInstance(
                CallableStatement.class.getClassLoader(),
                new Class<?>[]{CallableStatement.class},
                (p, m, a) -> null);
        // cs.getString(...) returns null on a SQL NULL — handler returns null
        assertNull(handler.getNullableResult(cs, 1));
    }

    @Test
    void nullColumnProducesNull() throws SQLException {
        ProfileJsonTypeHandler handler = new ProfileJsonTypeHandler();
        try (PreparedStatement ins = conn.prepareStatement(
                "MERGE INTO docs (id, payload) VALUES (3, NULL)")) {
            ins.executeUpdate();
        }
        try (PreparedStatement sel = conn.prepareStatement(
                "SELECT payload FROM docs WHERE id = 3")) {
            try (ResultSet rs = sel.executeQuery()) {
                rs.next();
                assertNull(handler.getNullableResult(rs, "payload"));
            }
        }
    }

    @Test
    void emptyStringColumnProducesNull() throws SQLException {
        ProfileJsonTypeHandler handler = new ProfileJsonTypeHandler();
        try (PreparedStatement ins = conn.prepareStatement(
                "MERGE INTO docs (id, payload) VALUES (4, '')")) {
            ins.executeUpdate();
        }
        try (PreparedStatement sel = conn.prepareStatement(
                "SELECT payload FROM docs WHERE id = 4")) {
            try (ResultSet rs = sel.executeQuery()) {
                rs.next();
                assertNull(handler.getNullableResult(rs, "payload"));
            }
        }
    }

    @Test
    void customMapper() throws SQLException {
        ObjectMapper m = ObjectMapper.builder().build();
        Fastjson3JsonTypeHandler<Profile> handler =
                new Fastjson3JsonTypeHandler<Profile>(Profile.class, m) {
                };
        try (PreparedStatement ins = conn.prepareStatement(
                "MERGE INTO docs (id, payload) VALUES (5, ?)")) {
            handler.setNonNullParameter(ins, 1, new Profile("zoe", 88), null);
            ins.executeUpdate();
        }
        try (PreparedStatement sel = conn.prepareStatement(
                "SELECT payload FROM docs WHERE id = 5")) {
            try (ResultSet rs = sel.executeQuery()) {
                rs.next();
                Profile back = handler.getNullableResult(rs, "payload");
                assertEquals("zoe", back.name);
            }
        }
    }

    @Test
    void nullClassTargetTypeRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3JsonTypeHandler<Profile>((Class<Profile>) null) {
                });
    }

    @Test
    void nullTypeReferenceRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3JsonTypeHandler<Profile>((TypeReference<Profile>) null) {
                });
    }

    @Test
    void nullMapperRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3JsonTypeHandler<Profile>(Profile.class, null) {
                });
    }
}

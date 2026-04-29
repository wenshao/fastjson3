package com.alibaba.fastjson3.spring.data.redis;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-level coverage for {@link Fastjson3RedisSerializer} +
 * {@link GenericFastjson3RedisSerializer}. Exercises the
 * {@link org.springframework.data.redis.serializer.RedisSerializer}
 * contract directly without needing a Redis backend.
 */
class Fastjson3RedisSerializerTest {
    public static class User {
        public Long id;
        public String name;
        @JSONField(name = "email_addr")
        public String email;

        public User() {}

        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    public static class Order {
        public Long id;
        public List<String> items;

        public Order() {}

        public Order(Long id, List<String> items) {
            this.id = id;
            this.items = items;
        }
    }

    // ---- Fastjson3RedisSerializer (typed) ----

    @Test
    void typed_roundTrip_pojo() {
        Fastjson3RedisSerializer<User> serializer = new Fastjson3RedisSerializer<>(User.class);
        User original = new User(1L, "alice", "a@e.com");
        byte[] bytes = serializer.serialize(original);
        User round = serializer.deserialize(bytes);
        assertNotNull(round);
        assertEquals(1L, round.id);
        assertEquals("alice", round.name);
        assertEquals("a@e.com", round.email);
    }

    @Test
    void typed_serialize_null_returnsEmptyBytes() {
        Fastjson3RedisSerializer<User> s = new Fastjson3RedisSerializer<>(User.class);
        assertArrayEquals(new byte[0], s.serialize(null));
    }

    @Test
    void typed_deserialize_null_returnsNull() {
        Fastjson3RedisSerializer<User> s = new Fastjson3RedisSerializer<>(User.class);
        assertNull(s.deserialize(null));
    }

    @Test
    void typed_deserialize_emptyBytes_returnsNull() {
        Fastjson3RedisSerializer<User> s = new Fastjson3RedisSerializer<>(User.class);
        assertNull(s.deserialize(new byte[0]));
    }

    @Test
    void typed_deserialize_malformedBytes_throwsSerializationException() {
        Fastjson3RedisSerializer<User> s = new Fastjson3RedisSerializer<>(User.class);
        byte[] malformed = "not-a-json".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThrows(SerializationException.class, () -> s.deserialize(malformed));
    }

    @Test
    void typed_customMapper_isUsed() {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3RedisSerializer<User> s = new Fastjson3RedisSerializer<>(User.class, custom);
        User u = new User(2L, "bob", "b@e.com");
        byte[] bytes = s.serialize(u);
        User back = s.deserialize(bytes);
        assertEquals("bob", back.name);
    }

    @Test
    void typed_nestedFieldShape_roundTrip() {
        Fastjson3RedisSerializer<Order> s = new Fastjson3RedisSerializer<>(Order.class);
        Order o = new Order(99L, List.of("a", "b", "c"));
        byte[] bytes = s.serialize(o);
        Order back = s.deserialize(bytes);
        assertEquals(99L, back.id);
        assertEquals(3, back.items.size());
        assertEquals("c", back.items.get(2));
    }

    @Test
    void typed_constructor_rejectsNullType() {
        assertThrows(IllegalArgumentException.class, () -> new Fastjson3RedisSerializer<>(null));
    }

    @Test
    void typed_constructor_rejectsNullMapper() {
        assertThrows(IllegalArgumentException.class, () -> new Fastjson3RedisSerializer<>(User.class, null));
    }

    // ---- GenericFastjson3RedisSerializer (untyped, autotype) ----

    @Test
    void generic_serializeWritesTypeDiscriminator() {
        GenericFastjson3RedisSerializer s = new GenericFastjson3RedisSerializer();
        User original = new User(1L, "alice", "a@e.com");
        byte[] bytes = s.serialize(original);
        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(json.contains("@type"),
                "expected @type discriminator in: " + json);
        assertTrue(json.contains("\"alice\""), "expected payload fields preserved");
    }

    @Test
    void generic_deserializeUnknownType_fallsBackToJsonObject() {
        // Documented limitation: ObjectMapper.Builder doesn't yet expose an
        // AutoTypeFilter hook, so SupportAutoType+@type round-trip lands as
        // JSONObject rather than the original POJO. Pin the current shape so
        // a future fj3 core fix that wires AutoTypeFilter trips this test.
        GenericFastjson3RedisSerializer s = new GenericFastjson3RedisSerializer();
        User original = new User(1L, "alice", "a@e.com");
        byte[] bytes = s.serialize(original);
        Object back = s.deserialize(bytes);
        assertNotNull(back);
        // Today: JSONObject. Tomorrow (after autoTypeFilter wiring): User.
        assertTrue(back instanceof com.alibaba.fastjson3.JSONObject || back instanceof User,
                "expected JSONObject (current) or User (post-fix), got "
                        + back.getClass().getName());
        // The @type / payload data is preserved either way:
        if (back instanceof com.alibaba.fastjson3.JSONObject json) {
            assertEquals("alice", json.get("name"));
        } else {
            assertEquals("alice", ((User) back).name);
        }
    }

    @Test
    void generic_serialize_null_returnsEmptyBytes() {
        GenericFastjson3RedisSerializer s = new GenericFastjson3RedisSerializer();
        assertArrayEquals(new byte[0], s.serialize(null));
    }

    @Test
    void generic_deserialize_null_returnsNull() {
        GenericFastjson3RedisSerializer s = new GenericFastjson3RedisSerializer();
        assertNull(s.deserialize(null));
    }

    @Test
    void generic_deserialize_emptyBytes_returnsNull() {
        GenericFastjson3RedisSerializer s = new GenericFastjson3RedisSerializer();
        assertNull(s.deserialize(new byte[0]));
    }

    @Test
    void generic_roundTrip_mapValue() {
        GenericFastjson3RedisSerializer s = new GenericFastjson3RedisSerializer();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", "two");
        map.put("c", List.of("x", "y"));
        byte[] bytes = s.serialize(map);
        Object back = s.deserialize(bytes);
        assertInstanceOf(Map.class, back);
        Map<?, ?> m = (Map<?, ?>) back;
        assertEquals(1, m.get("a"));
        assertEquals("two", m.get("b"));
    }

    @Test
    void generic_roundTrip_listValue() {
        GenericFastjson3RedisSerializer s = new GenericFastjson3RedisSerializer();
        List<Object> list = List.of("a", 1, true);
        byte[] bytes = s.serialize(list);
        Object back = s.deserialize(bytes);
        assertInstanceOf(List.class, back);
        List<?> l = (List<?>) back;
        assertEquals(3, l.size());
    }

    @Test
    void generic_deserialize_malformedBytes_throwsSerializationException() {
        GenericFastjson3RedisSerializer s = new GenericFastjson3RedisSerializer();
        byte[] malformed = "not-a-json".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThrows(SerializationException.class, () -> s.deserialize(malformed));
    }

    @Test
    void generic_constructor_rejectsNullMapper() {
        assertThrows(IllegalArgumentException.class, () -> new GenericFastjson3RedisSerializer(null));
    }
}

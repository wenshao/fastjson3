package com.alibaba.fastjson3.kafka;

import com.alibaba.fastjson3.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Fastjson3KafkaTest {
    public static class Event {
        public String id;
        public int value;

        public Event() {
        }

        public Event(String id, int value) {
            this.id = id;
            this.value = value;
        }
    }

    @Test
    void serializerProducesUtf8Json() {
        Fastjson3KafkaSerializer<Event> ser = new Fastjson3KafkaSerializer<>();
        byte[] bytes = ser.serialize("topic-x", new Event("e1", 42));
        String json = new String(bytes);
        assertTrue(json.contains("\"id\":\"e1\""));
        assertTrue(json.contains("\"value\":42"));
    }

    @Test
    void serializerNullPassThrough() {
        Fastjson3KafkaSerializer<Event> ser = new Fastjson3KafkaSerializer<>();
        assertNull(ser.serialize("topic-x", null));
    }

    @Test
    void deserializerWithConstructorType() {
        Fastjson3KafkaSerializer<Event> ser = new Fastjson3KafkaSerializer<>();
        Fastjson3KafkaDeserializer<Event> de = new Fastjson3KafkaDeserializer<>(Event.class);
        Event src = new Event("e2", 7);
        Event dst = de.deserialize("topic-x", ser.serialize("topic-x", src));
        assertEquals("e2", dst.id);
        assertEquals(7, dst.value);
    }

    @Test
    void deserializerWithConfigureValueType() {
        Fastjson3KafkaDeserializer<Event> de = new Fastjson3KafkaDeserializer<>();
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(Fastjson3KafkaDeserializer.VALUE_DEFAULT_TYPE, Event.class.getName());
        de.configure(cfg, false);

        Fastjson3KafkaSerializer<Event> ser = new Fastjson3KafkaSerializer<>();
        Event dst = de.deserialize("topic-x", ser.serialize("topic-x", new Event("e3", 9)));
        assertEquals("e3", dst.id);
    }

    @Test
    void deserializerWithConfigureKeyType() {
        Fastjson3KafkaDeserializer<String> de = new Fastjson3KafkaDeserializer<>();
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(Fastjson3KafkaDeserializer.KEY_DEFAULT_TYPE, String.class.getName());
        de.configure(cfg, true);
        // serialize "abc" -> "\"abc\""
        byte[] bytes = "\"abc\"".getBytes();
        assertEquals("abc", de.deserialize("topic-x", bytes));
    }

    @Test
    void deserializerAcceptsClassValueInConfig() {
        Fastjson3KafkaDeserializer<Event> de = new Fastjson3KafkaDeserializer<>();
        Map<String, Object> cfg = new HashMap<>();
        // configure() may receive a Class object directly when set programmatically
        cfg.put(Fastjson3KafkaDeserializer.VALUE_DEFAULT_TYPE, Event.class);
        de.configure(cfg, false);

        Fastjson3KafkaSerializer<Event> ser = new Fastjson3KafkaSerializer<>();
        Event dst = de.deserialize("t", ser.serialize("t", new Event("c", 1)));
        assertEquals("c", dst.id);
    }

    @Test
    void deserializerNullPassThrough() {
        Fastjson3KafkaDeserializer<Event> de = new Fastjson3KafkaDeserializer<>(Event.class);
        assertNull(de.deserialize("topic-x", null));
    }

    @Test
    void deserializerMissingTypeThrows() {
        Fastjson3KafkaDeserializer<Event> de = new Fastjson3KafkaDeserializer<>();
        // Neither constructor nor configure() set a type.
        de.configure(new HashMap<>(), false);
        assertThrows(SerializationException.class,
                () -> de.deserialize("topic-x", "{}".getBytes()));
    }

    @Test
    void deserializerUnknownClassThrows() {
        Fastjson3KafkaDeserializer<Event> de = new Fastjson3KafkaDeserializer<>();
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(Fastjson3KafkaDeserializer.VALUE_DEFAULT_TYPE, "no.such.Class");
        assertThrows(SerializationException.class, () -> de.configure(cfg, false));
    }

    @Test
    void deserializerMalformedJsonWrapped() {
        Fastjson3KafkaDeserializer<Event> de = new Fastjson3KafkaDeserializer<>(Event.class);
        assertThrows(SerializationException.class,
                () -> de.deserialize("topic-x", "not json".getBytes()));
    }

    @Test
    void configureLeavesConstructorTypeAlone() {
        Fastjson3KafkaDeserializer<Event> de = new Fastjson3KafkaDeserializer<>(Event.class);
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(Fastjson3KafkaDeserializer.VALUE_DEFAULT_TYPE, "no.such.Class");
        // Constructor type wins; configure() must not overwrite or fail
        de.configure(cfg, false);
        Fastjson3KafkaSerializer<Event> ser = new Fastjson3KafkaSerializer<>();
        Event dst = de.deserialize("t", ser.serialize("t", new Event("ok", 1)));
        assertEquals("ok", dst.id);
    }

    @Test
    void customMapperUsed() {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3KafkaSerializer<Event> ser = new Fastjson3KafkaSerializer<>(custom);
        byte[] bytes = ser.serialize("t", new Event("x", 2));
        assertTrue(bytes.length > 0);
    }
}

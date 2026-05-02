package com.alibaba.fastjson3.solon;

import com.alibaba.fastjson3.Fastjson3MapperHolder;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.noear.solon.serialization.SerializerNames;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Fastjson3StringSerializerTest {
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
    void serializeRoundTrip() throws IOException {
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        String json = s.serialize(new Event("e1", 42));
        assertTrue(json.contains("\"id\":\"e1\""));
        assertTrue(json.contains("\"value\":42"));

        Event back = (Event) s.deserialize(json, Event.class);
        assertEquals("e1", back.id);
        assertEquals(42, back.value);
    }

    @Test
    void deserializeWithGenericType() throws IOException {
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        String json = s.serialize(Arrays.asList(new Event("a", 1), new Event("b", 2)));
        @SuppressWarnings("unchecked")
        List<Event> back = (List<Event>) s.deserialize(json,
                new TypeReference<List<Event>>() {
                }.getType());
        assertEquals(2, back.size());
        assertEquals("a", back.get(0).id);
        assertEquals(2, back.get(1).value);
    }

    @Test
    void serializerMetadata() {
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        assertEquals(SerializerNames.AT_JSON, s.name());
        assertEquals("application/json", s.mimeType());
        assertEquals(String.class, s.dataType());
    }

    @Test
    void matchedRecognizesJsonMimeTypes() {
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        assertTrue(s.matched(null, "application/json"));
        assertTrue(s.matched(null, "application/json; charset=UTF-8"));
        assertTrue(s.matched(null, "application/vnd.acme+json"));
        assertTrue(s.matched(null, SerializerNames.AT_JSON));

        assertFalse(s.matched(null, "application/xml"));
        assertFalse(s.matched(null, "text/plain"));
        assertFalse(s.matched(null, null));
    }

    @Test
    void nullDataDeserializesToNull() throws IOException {
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        assertNull(s.deserialize(null, Event.class));
        assertNull(s.deserialize("", Event.class));
    }

    @Test
    void customMapperUsed() throws IOException {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3StringSerializer s = new Fastjson3StringSerializer(custom);
        assertEquals(custom, s.getMapper());
        assertTrue(s.serialize(new Event("c", 3)).contains("\"id\":\"c\""));
    }

    @Test
    void nullMapperRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Fastjson3StringSerializer(null));
    }

    @Test
    void addEncoderApplied() throws IOException {
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        // Convert Event to a String "id-value" before serializing
        s.addEncoder(Event.class, src -> src.id + "-" + src.value);
        String json = s.serialize(new Event("e", 7));
        // String result is JSON-encoded (quoted)
        assertEquals("\"e-7\"", json);
    }

    @Test
    void addEncoderRejectsNullClass() {
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        assertThrows(IllegalArgumentException.class, () -> s.addEncoder(null, src -> src));
    }

    @Test
    void addEncoderRejectsNullConverter() {
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        assertThrows(IllegalArgumentException.class, () -> s.addEncoder(Event.class, null));
    }

    @Test
    void malformedJsonWrappedAsIoException() {
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        assertThrows(IOException.class, () -> s.deserialize("not json", Event.class));
    }

    @Test
    void entityConverterMappingsContainsAtJson() {
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        Fastjson3EntityConverter c = new Fastjson3EntityConverter(s);
        String[] mappings = c.mappings();
        assertEquals(1, mappings.length);
        assertEquals(SerializerNames.AT_JSON, mappings[0]);
    }

    @AfterEach
    void resetHolder() {
        // Don't leak custom mappers into sibling tests in this JVM.
        Fastjson3MapperHolder.reset();
    }

    @Test
    void noArgCtorReadsFromHolder() {
        // Pin the load-bearing PR #171 contract for this module
        // specifically: `Fastjson3StringSerializer`'s no-arg ctor reads
        // `Fastjson3MapperHolder.get()` instead of `ObjectMapper.shared()`.
        //
        // The other 11 ecosystem modules (jaxrs jakarta+javax, jpa
        // jakarta+javax, kafka serializer + deserializer, mybatis, vertx,
        // retrofit, grpc, redisson) each have their own independent no-arg
        // ctor calling the holder. None exposes a public mapper accessor,
        // so this test does NOT prove their behavior — it only structurally
        // backstops the pattern, paired with grep-level confirmation in
        // PR #171's cross-module audit.
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3MapperHolder.set(custom);
        Fastjson3StringSerializer s = new Fastjson3StringSerializer();
        assertSame(custom, s.getMapper(),
                "no-arg ctor must read mapper from Fastjson3MapperHolder, not ObjectMapper.shared()");
    }
}

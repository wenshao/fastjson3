package com.alibaba.fastjson3.vertx;

import com.alibaba.fastjson3.ObjectMapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Fastjson3JsonCodecTest {
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
    void stringRoundTrip() {
        Fastjson3JsonCodec codec = new Fastjson3JsonCodec();
        String json = codec.toString(new Event("e1", 42), false);
        assertTrue(json.contains("\"id\":\"e1\""));
        assertTrue(json.contains("\"value\":42"));

        Event back = codec.fromString(json, Event.class);
        assertEquals("e1", back.id);
        assertEquals(42, back.value);
    }

    @Test
    void bufferRoundTrip() {
        Fastjson3JsonCodec codec = new Fastjson3JsonCodec();
        Buffer buf = codec.toBuffer(new Event("e2", 7), false);
        assertNotNull(buf);

        Event back = codec.fromBuffer(buf, Event.class);
        assertEquals("e2", back.id);
        assertEquals(7, back.value);
    }

    @Test
    void prettyFormat() {
        Fastjson3JsonCodec codec = new Fastjson3JsonCodec();
        String compact = codec.toString(new Event("p", 1), false);
        String pretty = codec.toString(new Event("p", 1), true);
        // Pretty version is longer and has newlines/indentation
        assertTrue(pretty.length() > compact.length());
        assertTrue(pretty.contains("\n"));
    }

    @Test
    void fromValueRoundTripsEvenWhenAlreadyTargetType() {
        // Matches Jackson's ObjectMapper.convertValue contract: always
        // round-trip through JSON, never identity-passthrough. Mutating the
        // result must not affect the source.
        Fastjson3JsonCodec codec = new Fastjson3JsonCodec();
        Event src = new Event("s", 9);
        Event back = codec.fromValue(src, Event.class);
        // Same shape, different instance
        assertEquals("s", back.id);
        assertEquals(9, back.value);
        org.junit.jupiter.api.Assertions.assertNotSame(src, back);
    }

    @Test
    void fromValueRoundTripFromMap() {
        Fastjson3JsonCodec codec = new Fastjson3JsonCodec();
        Map<String, Object> src = new HashMap<>();
        src.put("id", "m");
        src.put("value", 11);
        Event back = codec.fromValue(src, Event.class);
        assertEquals("m", back.id);
        assertEquals(11, back.value);
    }

    @Test
    void nullStringReturnsNull() {
        Fastjson3JsonCodec codec = new Fastjson3JsonCodec();
        assertNull(codec.fromString(null, Event.class));
        assertNull(codec.fromString("", Event.class));
    }

    @Test
    void nullBufferReturnsNull() {
        Fastjson3JsonCodec codec = new Fastjson3JsonCodec();
        assertNull(codec.fromBuffer(null, Event.class));
        assertNull(codec.fromBuffer(Buffer.buffer(), Event.class));
    }

    @Test
    void nullValueReturnsNull() {
        Fastjson3JsonCodec codec = new Fastjson3JsonCodec();
        assertNull(codec.fromValue(null, Event.class));
    }

    @Test
    void malformedJsonThrowsDecodeException() {
        Fastjson3JsonCodec codec = new Fastjson3JsonCodec();
        assertThrows(DecodeException.class, () -> codec.fromString("not json", Event.class));
        assertThrows(DecodeException.class,
                () -> codec.fromBuffer(Buffer.buffer("not json"), Event.class));
    }

    @Test
    void factoryMetadata() {
        // SPI contract: order() = 100 (wins over Jackson default 0),
        // codec() returns a non-null Fastjson3JsonCodec.
        Fastjson3JsonFactory factory = new Fastjson3JsonFactory();
        assertNotNull(factory.codec());
        assertEquals(100, factory.order());
        org.junit.jupiter.api.Assertions.assertInstanceOf(Fastjson3JsonCodec.class, factory.codec());
    }

    @Test
    void serviceLoaderRegistration() throws Exception {
        // Verify META-INF/services/io.vertx.core.spi.JsonFactory references
        // our factory class — drift between rename + service file is silent.
        java.io.InputStream resource = Fastjson3JsonCodecTest.class
                .getResourceAsStream("/META-INF/services/io.vertx.core.spi.JsonFactory");
        assertNotNull(resource, "ServiceLoader registration file missing");
        try (resource) {
            String content = new String(resource.readAllBytes()).trim();
            assertEquals("com.alibaba.fastjson3.vertx.Fastjson3JsonFactory", content);
        }
    }

    @Test
    void nullMapperRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Fastjson3JsonCodec(null));
    }

    @Test
    void customMapperUsed() {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3JsonCodec codec = new Fastjson3JsonCodec(custom);
        String json = codec.toString(new Event("c", 2), false);
        assertTrue(json.contains("\"id\":\"c\""));
    }
}

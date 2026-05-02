package com.alibaba.fastjson3.redisson;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.redisson.client.handler.State;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Fastjson3RedissonCodecTest {
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
    @SuppressWarnings("unchecked")
    void roundTripWithClass() throws IOException {
        Fastjson3RedissonCodec<Event> codec = new Fastjson3RedissonCodec<>(Event.class);
        ByteBuf encoded = codec.getValueEncoder().encode(new Event("e1", 42));
        try {
            String json = encoded.toString(io.netty.util.CharsetUtil.UTF_8);
            assertTrue(json.contains("\"id\":\"e1\""));
            assertTrue(json.contains("\"value\":42"));

            ByteBuf forDecode = Unpooled.wrappedBuffer(json.getBytes());
            Event dst = (Event) codec.getValueDecoder().decode(forDecode, new State());
            assertEquals("e1", dst.id);
            assertEquals(42, dst.value);
        } finally {
            encoded.release();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void roundTripWithTypeReference() throws IOException {
        Fastjson3RedissonCodec<List<Event>> codec = new Fastjson3RedissonCodec<>(
                new TypeReference<List<Event>>() {
                });
        List<Event> src = Arrays.asList(new Event("a", 1), new Event("b", 2));
        ByteBuf encoded = codec.getValueEncoder().encode(src);
        try {
            ByteBuf forDecode = Unpooled.wrappedBuffer(encoded.toString(io.netty.util.CharsetUtil.UTF_8).getBytes());
            List<Event> dst = (List<Event>) codec.getValueDecoder().decode(forDecode, new State());
            assertEquals(2, dst.size());
            assertEquals("a", dst.get(0).id);
            assertEquals(2, dst.get(1).value);
        } finally {
            encoded.release();
        }
    }

    @Test
    void customMapper() throws IOException {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3RedissonCodec<Event> codec = new Fastjson3RedissonCodec<>(Event.class, custom);
        ByteBuf encoded = codec.getValueEncoder().encode(new Event("c", 3));
        try {
            assertTrue(encoded.toString(io.netty.util.CharsetUtil.UTF_8).contains("\"id\":\"c\""));
        } finally {
            encoded.release();
        }
    }

    @Test
    void nullClassRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3RedissonCodec<Event>((Class<Event>) null));
    }

    @Test
    void nullTypeReferenceRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3RedissonCodec<Event>((TypeReference<Event>) null));
    }

    @Test
    void nullMapperRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3RedissonCodec<>(Event.class, null));
    }

    @Test
    void malformedJsonWrappedAsIoException() {
        Fastjson3RedissonCodec<Event> codec = new Fastjson3RedissonCodec<>(Event.class);
        ByteBuf bad = Unpooled.wrappedBuffer("not json".getBytes());
        assertThrows(IOException.class, () -> codec.getValueDecoder().decode(bad, new State()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapKeyEncoderDelegatesToStringCodec() throws IOException {
        // Locks in the RMap<String, T> key compatibility fix: map keys must
        // wire raw (not JSON-quoted), matching Redisson's StringCodec
        // convention so keys are interoperable across codecs.
        Fastjson3RedissonCodec<Event> codec = new Fastjson3RedissonCodec<>(Event.class);
        ByteBuf encoded = codec.getMapKeyEncoder().encode("foo");
        try {
            // StringCodec writes raw UTF-8 with no JSON quotes
            String wire = encoded.toString(io.netty.util.CharsetUtil.UTF_8);
            assertEquals("foo", wire);
        } finally {
            encoded.release();
        }

        ByteBuf forDecode = Unpooled.wrappedBuffer("foo".getBytes());
        Object decoded = codec.getMapKeyDecoder().decode(forDecode, new State());
        assertEquals("foo", decoded);
    }

    @Test
    void mapKeyEncoderIsStringCodecInstance() {
        // Confirm we delegate to Redisson's StringCodec rather than rolling
        // our own — guards against silent regression if the override is
        // accidentally removed.
        Fastjson3RedissonCodec<Event> codec = new Fastjson3RedissonCodec<>(Event.class);
        org.redisson.client.codec.StringCodec stringCodec = org.redisson.client.codec.StringCodec.INSTANCE;
        org.junit.jupiter.api.Assertions.assertSame(
                stringCodec.getMapKeyEncoder(), codec.getMapKeyEncoder());
        org.junit.jupiter.api.Assertions.assertSame(
                stringCodec.getMapKeyDecoder(), codec.getMapKeyDecoder());
    }
}

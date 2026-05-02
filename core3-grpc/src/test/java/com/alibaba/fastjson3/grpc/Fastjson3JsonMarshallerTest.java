package com.alibaba.fastjson3.grpc;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Fastjson3JsonMarshallerTest {
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
    void roundTripWithClass() throws Exception {
        Fastjson3JsonMarshaller<Event> m = new Fastjson3JsonMarshaller<>(Event.class);
        Event src = new Event("e1", 42);
        try (InputStream in = m.stream(src)) {
            Event dst = m.parse(in);
            assertEquals("e1", dst.id);
            assertEquals(42, dst.value);
        }
    }

    @Test
    void roundTripWithTypeReference() throws Exception {
        Fastjson3JsonMarshaller<List<Event>> m = new Fastjson3JsonMarshaller<>(
                new TypeReference<List<Event>>() {
                });
        List<Event> src = Arrays.asList(new Event("a", 1), new Event("b", 2));
        try (InputStream in = m.stream(src)) {
            List<Event> dst = m.parse(in);
            assertEquals(2, dst.size());
            assertEquals("a", dst.get(0).id);
            assertEquals(2, dst.get(1).value);
        }
    }

    @Test
    void customMapper() throws Exception {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3JsonMarshaller<Event> m = new Fastjson3JsonMarshaller<>(Event.class, custom);
        try (InputStream in = m.stream(new Event("c", 3))) {
            Event dst = m.parse(in);
            assertEquals("c", dst.id);
        }
    }

    @Test
    void nullClassRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3JsonMarshaller<Event>((Class<Event>) null));
    }

    @Test
    void nullTypeReferenceRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3JsonMarshaller<Event>((TypeReference<Event>) null));
    }

    @Test
    void nullMapperRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3JsonMarshaller<>(Event.class, null));
    }
}

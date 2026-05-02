package com.alibaba.fastjson3.retrofit;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.jupiter.api.Test;
import retrofit2.Converter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Fastjson3RetrofitConverterFactoryTest {
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
    void requestBodyRoundTrip() throws IOException {
        Fastjson3RetrofitConverterFactory factory = Fastjson3RetrofitConverterFactory.create();
        Converter<Event, RequestBody> req = (Converter<Event, RequestBody>)
                factory.requestBodyConverter(Event.class, new Annotation[0], new Annotation[0], null);
        RequestBody body = req.convert(new Event("e1", 42));
        assertEquals("application/json; charset=utf-8", body.contentType().toString());

        Buffer sink = new Buffer();
        body.writeTo(sink);
        String json = sink.readUtf8();
        assertTrue(json.contains("\"id\":\"e1\""));
        assertTrue(json.contains("\"value\":42"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void responseBodyRoundTripWithClass() throws IOException {
        Fastjson3RetrofitConverterFactory factory = Fastjson3RetrofitConverterFactory.create();
        Converter<ResponseBody, Event> resp = (Converter<ResponseBody, Event>)
                factory.responseBodyConverter(Event.class, new Annotation[0], null);
        ResponseBody body = ResponseBody.create(
                MediaType.parse("application/json"), "{\"id\":\"e1\",\"value\":42}");
        Event dst = resp.convert(body);
        assertEquals("e1", dst.id);
        assertEquals(42, dst.value);
    }

    @Test
    @SuppressWarnings("unchecked")
    void responseBodyRoundTripWithGenericType() throws IOException {
        Fastjson3RetrofitConverterFactory factory = Fastjson3RetrofitConverterFactory.create();
        Type listOfEvent = new TypeReference<List<Event>>() {
        }.getType();
        Converter<ResponseBody, List<Event>> resp = (Converter<ResponseBody, List<Event>>)
                factory.responseBodyConverter(listOfEvent, new Annotation[0], null);
        ResponseBody body = ResponseBody.create(
                MediaType.parse("application/json"),
                "[{\"id\":\"a\",\"value\":1},{\"id\":\"b\",\"value\":2}]");
        List<Event> dst = resp.convert(body);
        assertEquals(2, dst.size());
        assertEquals("a", dst.get(0).id);
        assertEquals(2, dst.get(1).value);
    }

    @Test
    void customMapper() {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3RetrofitConverterFactory factory = Fastjson3RetrofitConverterFactory.create(custom);
        Converter<?, RequestBody> req = factory.requestBodyConverter(
                Event.class, new Annotation[0], new Annotation[0], null);
        assertNotNull(req);
    }

    @Test
    void nullMapperRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Fastjson3RetrofitConverterFactory.create(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void malformedJsonWrappedAsIoException() {
        Fastjson3RetrofitConverterFactory factory = Fastjson3RetrofitConverterFactory.create();
        Converter<ResponseBody, Event> resp = (Converter<ResponseBody, Event>)
                factory.responseBodyConverter(Event.class, new Annotation[0], null);
        ResponseBody body = ResponseBody.create(MediaType.parse("application/json"), "not json");
        assertThrows(IOException.class, () -> resp.convert(body));
    }
}

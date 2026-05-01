package com.alibaba.fastjson3.jaxrs.jakarta;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Fastjson3ProviderTest {
    static class User {
        public String name;
        public int age;

        public User() {
        }

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    void writeAndReadRoundTrip() throws IOException {
        Fastjson3Provider provider = new Fastjson3Provider();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        User src = new User("alice", 30);
        provider.writeTo(src, User.class, User.class, null,
                MediaType.APPLICATION_JSON_TYPE, null, out);
        String json = out.toString("UTF-8");
        assertTrue(json.contains("\"name\":\"alice\""));
        assertTrue(json.contains("\"age\":30"));

        @SuppressWarnings({"rawtypes", "unchecked"})
        User dst = (User) provider.readFrom(
                (Class) User.class, User.class, null,
                MediaType.APPLICATION_JSON_TYPE, null,
                new ByteArrayInputStream(out.toByteArray()));
        assertEquals("alice", dst.name);
        assertEquals(30, dst.age);
    }

    @Test
    void parameterizedTypeReadViaGenericType() throws IOException {
        // Resources returning List<User> hand the provider a ParameterizedType
        // for genericType. The reader must prefer it over the raw type.
        Fastjson3Provider provider = new Fastjson3Provider();
        Type listOfUser = new TypeReference<List<User>>() {
        }.getType();
        String json = "[{\"name\":\"a\",\"age\":1},{\"name\":\"b\",\"age\":2}]";

        @SuppressWarnings({"rawtypes", "unchecked"})
        List<User> dst = (List<User>) provider.readFrom(
                (Class) List.class, listOfUser, null,
                MediaType.APPLICATION_JSON_TYPE, null,
                new ByteArrayInputStream(json.getBytes()));
        assertEquals(2, dst.size());
        assertEquals("a", dst.get(0).name);
        assertEquals(2, dst.get(1).age);
    }

    @Test
    void mediaTypeMatching() {
        Fastjson3Provider provider = new Fastjson3Provider();
        // standard json
        assertTrue(provider.isReadable(User.class, User.class, null, MediaType.APPLICATION_JSON_TYPE));
        assertTrue(provider.isWriteable(User.class, User.class, null, MediaType.APPLICATION_JSON_TYPE));
        // +json suffix (e.g. application/hal+json)
        assertTrue(provider.isReadable(User.class, User.class, null,
                MediaType.valueOf("application/hal+json")));
        // null mediaType — accept (some clients omit Content-Type)
        assertTrue(provider.isReadable(User.class, User.class, null, null));
        // unrelated types rejected
        assertFalse(provider.isReadable(User.class, User.class, null,
                MediaType.valueOf("application/xml")));
        assertFalse(provider.isWriteable(User.class, User.class, null,
                MediaType.valueOf("text/plain")));
        // legacy non-JSON subtypes rejected (fj3 narrows vs. fj2)
        assertFalse(provider.isReadable(User.class, User.class, null,
                MediaType.valueOf("application/javascript")));
        assertFalse(provider.isReadable(User.class, User.class, null,
                MediaType.valueOf("application/x-www-form-urlencoded")));
    }

    @Test
    void rejectsStreamAndPrimitiveTypes() {
        Fastjson3Provider provider = new Fastjson3Provider();
        // Read side excludes raw streams + primitive-shaped types
        // (those are owned by JAX-RS native body readers)
        assertFalse(provider.isReadable(InputStream.class, InputStream.class, null,
                MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isReadable(Reader.class, Reader.class, null,
                MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isReadable(String.class, String.class, null,
                MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isReadable(byte[].class, byte[].class, null,
                MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isReadable(Integer.class, Integer.class, null,
                MediaType.APPLICATION_JSON_TYPE));
        // Write side excludes raw streams + JAX-RS Response/StreamingOutput +
        // primitive-shaped types (avoids re-quoting pre-encoded JSON strings)
        assertFalse(provider.isWriteable(StreamingOutput.class, StreamingOutput.class, null,
                MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isWriteable(Response.class, Response.class, null,
                MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isWriteable(String.class, String.class, null,
                MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isWriteable(byte[].class, byte[].class, null,
                MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    void allowListGatesTypes() {
        Fastjson3Provider provider = new Fastjson3Provider(ObjectMapper.shared(), new Class<?>[]{User.class});
        assertTrue(provider.isReadable(User.class, User.class, null, MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isReadable(java.util.Date.class, java.util.Date.class, null,
                MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    void allowListIsDefensivelyCopied() {
        Class<?>[] arr = {User.class};
        Fastjson3Provider provider = new Fastjson3Provider(ObjectMapper.shared(), arr);
        // mutate caller's array — provider must not be affected
        arr[0] = java.util.Date.class;
        assertTrue(provider.isReadable(User.class, User.class, null, MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    void usesProvidedMapper() throws IOException {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3Provider provider = new Fastjson3Provider(custom);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        provider.writeTo(new User("bob", 40), User.class, User.class, null,
                MediaType.APPLICATION_JSON_TYPE, null, out);
        assertTrue(out.toString().contains("bob"));
    }

    @Test
    void nullMapperRejected() {
        // Aligns with Fastjson3RedisSerializer / Fastjson3JsonView precedent.
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3Provider((ObjectMapper) null));
        assertThrows(IllegalArgumentException.class,
                () -> new Fastjson3Provider(null, new Class<?>[]{User.class}));
    }

    @Test
    void invalidJsonWrappedAsBadRequest() {
        Fastjson3Provider provider = new Fastjson3Provider();
        ByteArrayInputStream junk = new ByteArrayInputStream("not json".getBytes());
        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> provider.readFrom((Class) User.class, User.class, null,
                        MediaType.APPLICATION_JSON_TYPE, null, junk));
        // 400, not 500 — parse error is a client problem
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                ex.getResponse().getStatus());
    }
}

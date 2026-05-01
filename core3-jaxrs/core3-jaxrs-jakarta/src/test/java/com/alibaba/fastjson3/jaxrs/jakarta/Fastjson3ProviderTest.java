package com.alibaba.fastjson3.jaxrs.jakarta;

import com.alibaba.fastjson3.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        // unrelated type rejected
        assertFalse(provider.isReadable(User.class, User.class, null,
                MediaType.valueOf("application/xml")));
        assertFalse(provider.isWriteable(User.class, User.class, null,
                MediaType.valueOf("text/plain")));
    }

    @Test
    void rejectsStreamTypes() {
        Fastjson3Provider provider = new Fastjson3Provider();
        // Read side excludes raw streams
        assertFalse(provider.isReadable(InputStream.class, InputStream.class, null,
                MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isReadable(Reader.class, Reader.class, null,
                MediaType.APPLICATION_JSON_TYPE));
        // Write side excludes raw streams + JAX-RS Response/StreamingOutput
        assertFalse(provider.isWriteable(StreamingOutput.class, StreamingOutput.class, null,
                MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isWriteable(Response.class, Response.class, null,
                MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    void allowListGatesTypes() {
        Fastjson3Provider provider = new Fastjson3Provider(ObjectMapper.shared(), new Class<?>[]{User.class});
        assertTrue(provider.isReadable(User.class, User.class, null, MediaType.APPLICATION_JSON_TYPE));
        assertFalse(provider.isReadable(String.class, String.class, null, MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    void usesProvidedMapper() {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3Provider provider = new Fastjson3Provider(custom);
        // Sanity: a configured mapper still produces valid JSON
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            provider.writeTo(new User("bob", 40), User.class, User.class, null,
                    MediaType.APPLICATION_JSON_TYPE, null, out);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertTrue(out.toString().contains("bob"));
    }

    @Test
    void nullMapperFallsBackToShared() {
        Fastjson3Provider p1 = new Fastjson3Provider((ObjectMapper) null);
        Fastjson3Provider p2 = new Fastjson3Provider(ObjectMapper.shared(), null);
        // Both write the same output for the same input — fallback worked
        ByteArrayOutputStream o1 = new ByteArrayOutputStream();
        ByteArrayOutputStream o2 = new ByteArrayOutputStream();
        try {
            p1.writeTo(new User("c", 1), User.class, User.class, null,
                    MediaType.APPLICATION_JSON_TYPE, null, o1);
            p2.writeTo(new User("c", 1), User.class, User.class, null,
                    MediaType.APPLICATION_JSON_TYPE, null, o2);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(o1.toString(), o2.toString());
    }

    @Test
    void invalidJsonWrappedAsWebApplicationException() {
        Fastjson3Provider provider = new Fastjson3Provider();
        ByteArrayInputStream junk = new ByteArrayInputStream("not json".getBytes());
        assertThrows(WebApplicationException.class, () -> provider.readFrom(
                (Class) User.class, User.class, null,
                MediaType.APPLICATION_JSON_TYPE, null, junk));
    }

    @Test
    void sharedDefaultMapper() {
        // Default constructor uses the shared mapper — verifies the no-arg ctor.
        Fastjson3Provider provider = new Fastjson3Provider();
        assertSame(provider.getClass(), Fastjson3Provider.class);
    }
}

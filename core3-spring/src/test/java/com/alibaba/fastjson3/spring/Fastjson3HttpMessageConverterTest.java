package com.alibaba.fastjson3.spring;

import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-level coverage for {@link Fastjson3HttpMessageConverter} using
 * Spring's MockHttpInputMessage / MockHttpOutputMessage. Verifies the
 * converter contract directly — no Spring Boot context needed. Heavier
 * end-to-end MockMvc / @RestController coverage lives in
 * {@code core3-spring-test}.
 */
class Fastjson3HttpMessageConverterTest {
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

    private final Fastjson3HttpMessageConverter conv = new Fastjson3HttpMessageConverter();

    // ---- supports() exclusions ----

    @Test
    void supports_excludesString() {
        assertFalse(conv.canRead(String.class, MediaType.APPLICATION_JSON));
        assertFalse(conv.canWrite(String.class, MediaType.APPLICATION_JSON));
    }

    @Test
    void supports_excludesByteArray() {
        assertFalse(conv.canRead(byte[].class, MediaType.APPLICATION_JSON));
        assertFalse(conv.canWrite(byte[].class, MediaType.APPLICATION_JSON));
    }

    @Test
    void supports_excludesResource() {
        assertFalse(conv.canRead(ByteArrayResource.class, MediaType.APPLICATION_JSON));
        assertFalse(conv.canWrite(Resource.class, MediaType.APPLICATION_JSON));
    }

    @Test
    void supports_acceptsPojo() {
        assertTrue(conv.canRead(User.class, MediaType.APPLICATION_JSON));
        assertTrue(conv.canWrite(User.class, MediaType.APPLICATION_JSON));
    }

    @Test
    void supports_acceptsJsonObjectAndArray() {
        assertTrue(conv.canRead(JSONObject.class, MediaType.APPLICATION_JSON));
        assertTrue(conv.canRead(JSONArray.class, MediaType.APPLICATION_JSON));
    }

    @Test
    void supports_rejectsNonJsonMediaType() {
        assertFalse(conv.canRead(User.class, MediaType.APPLICATION_XML));
        assertFalse(conv.canWrite(User.class, MediaType.TEXT_HTML));
    }

    @Test
    void supports_acceptsApplicationStarPlusJson() {
        MediaType vendor = MediaType.parseMediaType("application/vnd.example+json");
        assertTrue(conv.canRead(User.class, vendor));
        assertTrue(conv.canWrite(User.class, vendor));
    }

    // ---- read happy path ----

    @Test
    void read_pojo_utf8() throws Exception {
        String body = "{\"id\":1,\"name\":\"alice\",\"email_addr\":\"a@e.com\"}";
        HttpInputMessage in = new MockHttpInputMessage(body.getBytes(StandardCharsets.UTF_8));
        ((MockHttpInputMessage) in).getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Object result = conv.read(User.class, in);
        assertInstanceOf(User.class, result);
        User u = (User) result;
        assertEquals(1L, u.id);
        assertEquals("alice", u.name);
        assertEquals("a@e.com", u.email);
    }

    @Test
    void read_genericList_throughGenericMessageConverter() throws Exception {
        String body = "[{\"id\":1,\"name\":\"a\",\"email_addr\":\"a@e.com\"},"
                + "{\"id\":2,\"name\":\"b\",\"email_addr\":\"b@e.com\"}]";
        HttpInputMessage in = new MockHttpInputMessage(body.getBytes(StandardCharsets.UTF_8));
        ((MockHttpInputMessage) in).getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Use MethodParameter to extract the generic List<User> type from a
        // sample method, mirroring how Spring resolves @RequestBody types.
        Method m = SampleMethods.class.getDeclaredMethod("acceptUsers", List.class);
        Type t = m.getGenericParameterTypes()[0];
        Object result = conv.read(t, SampleMethods.class, in);
        assertInstanceOf(List.class, result);
        List<?> list = (List<?>) result;
        assertEquals(2, list.size());
        assertInstanceOf(User.class, list.get(0));
        assertEquals("a", ((User) list.get(0)).name);
    }

    public static class SampleMethods {
        @SuppressWarnings("unused")
        public void acceptUsers(List<User> users) {}
        @SuppressWarnings("unused")
        public void acceptMap(Map<String, Integer> map) {}
    }

    @Test
    void read_genericMap() throws Exception {
        String body = "{\"a\":1,\"b\":2}";
        HttpInputMessage in = new MockHttpInputMessage(body.getBytes(StandardCharsets.UTF_8));
        ((MockHttpInputMessage) in).getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Method m = SampleMethods.class.getDeclaredMethod("acceptMap", Map.class);
        Type t = m.getGenericParameterTypes()[0];
        Object result = conv.read(t, SampleMethods.class, in);
        assertInstanceOf(Map.class, result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
    }

    // ---- read charset honoring ----

    @Test
    void read_gbkCharset_chineseRoundTrip() throws Exception {
        Charset gbk = Charset.forName("GBK");
        byte[] bytes = "{\"id\":1,\"name\":\"张三\",\"email_addr\":\"z@e.com\"}".getBytes(gbk);
        HttpInputMessage in = new MockHttpInputMessage(bytes);
        ((MockHttpInputMessage) in).getHeaders().setContentType(MediaType.parseMediaType("application/json;charset=GBK"));
        User u = (User) conv.read(User.class, in);
        assertEquals("张三", u.name);
    }

    @Test
    void read_iso88591Charset_specialCharRoundTrip() throws Exception {
        byte[] bytes = "{\"id\":1,\"name\":\"café\",\"email_addr\":\"c@e.com\"}"
                .getBytes(StandardCharsets.ISO_8859_1);
        HttpInputMessage in = new MockHttpInputMessage(bytes);
        ((MockHttpInputMessage) in).getHeaders().setContentType(MediaType.parseMediaType("application/json;charset=ISO-8859-1"));
        User u = (User) conv.read(User.class, in);
        assertEquals("café", u.name);
    }

    // ---- read error wrapping ----

    @Test
    void read_malformedJson_wrapsAsHttpMessageNotReadable() {
        byte[] body = "{\"id\":1,\"name\":".getBytes(StandardCharsets.UTF_8);
        HttpInputMessage in = new MockHttpInputMessage(body);
        ((MockHttpInputMessage) in).getHeaders().setContentType(MediaType.APPLICATION_JSON);
        assertThrows(HttpMessageNotReadableException.class, () -> conv.read(User.class, in));
    }

    @Test
    void read_nullType_fallsBackToObject() throws Exception {
        // Spring contract allows null Type — guard returns Object via readInternal.
        byte[] body = "{\"id\":1,\"name\":\"a\",\"email_addr\":\"a@e.com\"}".getBytes(StandardCharsets.UTF_8);
        HttpInputMessage in = new MockHttpInputMessage(body);
        ((MockHttpInputMessage) in).getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Object result = conv.read((Type) null, null, in);
        assertNotNull(result);
        // Object.class returns a JSONObject for `{...}` per fastjson3 routing.
        assertInstanceOf(JSONObject.class, result);
    }

    // ---- write happy path ----

    @Test
    void write_pojo_emitsJsonBytes() throws Exception {
        MockHttpOutputMessage out = new MockHttpOutputMessage();
        conv.write(new User(1L, "alice", "a@e.com"), MediaType.APPLICATION_JSON, out);
        String body = out.getBodyAsString();
        assertTrue(body.contains("\"id\":1"));
        assertTrue(body.contains("\"name\":\"alice\""));
        assertTrue(body.contains("\"email_addr\":\"a@e.com\""));
    }

    @Test
    void write_jsonObject_passThrough() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("k", 1);
        obj.put("n", "v");
        MockHttpOutputMessage out = new MockHttpOutputMessage();
        conv.write(obj, MediaType.APPLICATION_JSON, out);
        String body = out.getBodyAsString();
        assertTrue(body.contains("\"k\":1"));
        assertTrue(body.contains("\"n\":\"v\""));
    }

    @Test
    void write_setsContentType() throws Exception {
        MockHttpOutputMessage out = new MockHttpOutputMessage();
        conv.write(new User(1L, "a", "a@e.com"), MediaType.APPLICATION_JSON, out);
        // setDefaultCharset(UTF_8) on the converter widens the published
        // Content-Type to include charset=UTF-8.
        MediaType ct = out.getHeaders().getContentType();
        assertNotNull(ct);
        assertTrue(ct.isCompatibleWith(MediaType.APPLICATION_JSON));
        assertEquals(StandardCharsets.UTF_8, ct.getCharset());
    }

    @Test
    void genericWrite_setsContentType() throws Exception {
        // Regression: pre-fix the GenericHttpMessageConverter.write(Object,
        // Type, MediaType, HttpOutputMessage) override called writeInternal
        // directly and skipped addDefaultHeaders, so generic return values
        // (List<User>, Map<K,V>, etc.) ended up without negotiated
        // Content-Type / charset headers. Now both Class-typed and
        // generic-typed write paths flow through addDefaultHeaders.
        MockHttpOutputMessage out = new MockHttpOutputMessage();
        conv.write(new User(1L, "a", "a@e.com"), (Type) User.class, MediaType.APPLICATION_JSON, out);
        MediaType ct = out.getHeaders().getContentType();
        assertNotNull(ct);
        assertTrue(ct.isCompatibleWith(MediaType.APPLICATION_JSON));
        assertEquals(StandardCharsets.UTF_8, ct.getCharset());
    }

    @Test
    void genericWrite_supportsStreamingHttpOutputMessage() throws Exception {
        // Regression: pre-fix the generic write override skipped Spring's
        // StreamingHttpOutputMessage branch that delays body emission
        // until output-message commit (used by async / filter-chain
        // scenarios). After the super.write() delegation, streaming output
        // is honored: the captured "body" callback runs once, on demand.
        java.util.concurrent.atomic.AtomicInteger callbackCount = new java.util.concurrent.atomic.AtomicInteger();
        java.io.ByteArrayOutputStream realOut = new java.io.ByteArrayOutputStream();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        org.springframework.http.StreamingHttpOutputMessage streaming = new org.springframework.http.StreamingHttpOutputMessage() {
            org.springframework.http.StreamingHttpOutputMessage.Body body;

            @Override
            public java.io.OutputStream getBody() {
                return realOut;
            }

            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                return headers;
            }

            @Override
            public void setBody(org.springframework.http.StreamingHttpOutputMessage.Body callback) {
                this.body = callback;
                callbackCount.incrementAndGet();
            }
        };

        conv.write(new User(1L, "a", "a@e.com"), (Type) User.class, MediaType.APPLICATION_JSON, streaming);

        // setBody callback registered exactly once (deferred body emission).
        assertEquals(1, callbackCount.get(),
                "StreamingHttpOutputMessage.setBody must run exactly once for the deferred body");
        // Headers still populated synchronously.
        assertNotNull(headers.getContentType());
    }

    // ---- custom mapper constructor ----

    @Test
    void customMapperConstructor_isUsed() throws Exception {
        ObjectMapper customMapper = ObjectMapper.builder().build();
        Fastjson3HttpMessageConverter c = new Fastjson3HttpMessageConverter(customMapper);
        byte[] body = "{\"id\":1,\"name\":\"a\",\"email_addr\":\"a@e.com\"}".getBytes(StandardCharsets.UTF_8);
        HttpInputMessage in = new MockHttpInputMessage(body);
        ((MockHttpInputMessage) in).getHeaders().setContentType(MediaType.APPLICATION_JSON);
        User u = (User) c.read(User.class, in);
        assertEquals("a", u.name);
    }
}

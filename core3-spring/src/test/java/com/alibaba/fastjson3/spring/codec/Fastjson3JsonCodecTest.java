package com.alibaba.fastjson3.spring.codec;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-level coverage for {@link Fastjson3JsonDecoder} +
 * {@link Fastjson3JsonEncoder} using Spring's
 * {@link DefaultDataBufferFactory} + Reactor StepVerifier. Verifies the
 * Decoder/Encoder contract directly, no full WebFlux server needed.
 */
class Fastjson3JsonCodecTest {
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

    private final DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
    private final Fastjson3JsonDecoder decoder = new Fastjson3JsonDecoder();
    private final Fastjson3JsonEncoder encoder = new Fastjson3JsonEncoder();

    private DataBuffer buffer(String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return factory.wrap(bytes);
    }

    private String bufferToString(DataBuffer b) {
        byte[] bytes = new byte[b.readableByteCount()];
        b.read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // ---- Decoder ----

    @Test
    void decoder_canDecode_application_json() {
        assertTrue(decoder.canDecode(ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void decoder_canDecode_vendorPlusJson() {
        MediaType vendor = MediaType.parseMediaType("application/vnd.example+json");
        assertTrue(decoder.canDecode(ResolvableType.forClass(User.class), vendor));
    }

    @Test
    void decoder_cannotDecode_xml() {
        assertFalse(decoder.canDecode(ResolvableType.forClass(User.class), MediaType.APPLICATION_XML));
    }

    @Test
    void decoder_excludesString() {
        // String / CharSequence / byte[] / Resource owned by dedicated codecs.
        assertFalse(decoder.canDecode(ResolvableType.forClass(String.class), MediaType.APPLICATION_JSON));
        assertFalse(decoder.canDecode(ResolvableType.forClass(CharSequence.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void decoder_excludesByteArrayAndByteBuffer() {
        assertFalse(decoder.canDecode(ResolvableType.forClass(byte[].class), MediaType.APPLICATION_JSON));
        assertFalse(decoder.canDecode(ResolvableType.forClass(java.nio.ByteBuffer.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void decoder_excludesResource() {
        assertFalse(decoder.canDecode(ResolvableType.forClass(org.springframework.core.io.Resource.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void decoder_doesNotAdvertise_ndjson() {
        // NDJSON support requires per-line tokenization which we don't
        // currently implement; advertising it would route ill-framed bytes.
        MediaType ndjson = MediaType.parseMediaType("application/x-ndjson");
        assertFalse(decoder.canDecode(ResolvableType.forClass(User.class), ndjson));
    }

    @Test
    void decoder_decodeToMono_pojo() {
        DataBuffer buf = buffer("{\"id\":1,\"name\":\"alice\",\"email_addr\":\"a@e.com\"}");
        Mono<Object> mono = decoder.decodeToMono(Mono.just(buf),
                ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON, null);
        StepVerifier.create(mono)
                .assertNext(o -> {
                    User u = (User) o;
                    assertEquals(1L, u.id);
                    assertEquals("alice", u.name);
                    assertEquals("a@e.com", u.email);
                })
                .verifyComplete();
    }

    @Test
    void decoder_flux_joinsBuffersIntoSingleValue() {
        // Decode contract: Flux entry point now joins buffers (matches
        // production HTTP behavior where one body arrives in chunks).
        // Splitting the input across two buffers must still decode to a
        // single User, not two (broken) Users.
        DataBuffer b1 = buffer("{\"id\":1,\"name\":\"al");
        DataBuffer b2 = buffer("ice\",\"email_addr\":\"a@e.com\"}");
        Flux<Object> flux = decoder.decode(Flux.just(b1, b2),
                ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON, null);
        StepVerifier.create(flux)
                .assertNext(o -> assertEquals("alice", ((User) o).name))
                .verifyComplete();
    }

    @Test
    void decoder_genericListType_throughResolvableType() {
        String body = "[{\"id\":1,\"name\":\"a\",\"email_addr\":\"a@e.com\"},"
                + "{\"id\":2,\"name\":\"b\",\"email_addr\":\"b@e.com\"}]";
        DataBuffer buf = buffer(body);
        ResolvableType listType = ResolvableType.forClassWithGenerics(List.class, User.class);
        Mono<Object> mono = decoder.decodeToMono(Mono.just(buf),
                listType, MediaType.APPLICATION_JSON, null);
        StepVerifier.create(mono)
                .assertNext(o -> {
                    @SuppressWarnings("unchecked")
                    List<User> list = (List<User>) o;
                    assertEquals(2, list.size());
                    assertEquals("a", list.get(0).name);
                    assertEquals("b", list.get(1).name);
                })
                .verifyComplete();
    }

    @Test
    void decoder_malformedJson_wrapsAsDecodingException() {
        DataBuffer buf = buffer("{\"id\":1,\"name\":");
        assertThrows(DecodingException.class, () ->
                decoder.decode(buf, ResolvableType.forClass(User.class), null, null));
    }

    // ---- Encoder ----

    @Test
    void encoder_canEncode_application_json() {
        assertTrue(encoder.canEncode(ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void encoder_cannotEncode_xml() {
        assertFalse(encoder.canEncode(ResolvableType.forClass(User.class), MediaType.APPLICATION_XML));
    }

    @Test
    void encoder_excludesString() {
        assertFalse(encoder.canEncode(ResolvableType.forClass(String.class), MediaType.APPLICATION_JSON));
        assertFalse(encoder.canEncode(ResolvableType.forClass(CharSequence.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void encoder_excludesByteArrayAndByteBuffer() {
        assertFalse(encoder.canEncode(ResolvableType.forClass(byte[].class), MediaType.APPLICATION_JSON));
        assertFalse(encoder.canEncode(ResolvableType.forClass(java.nio.ByteBuffer.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void encoder_excludesResource() {
        assertFalse(encoder.canEncode(ResolvableType.forClass(org.springframework.core.io.Resource.class), MediaType.APPLICATION_JSON));
    }

    @Test
    void encoder_streamingMediaTypes_emptyByDefault() {
        assertTrue(encoder.getStreamingMediaTypes().isEmpty(),
                "NDJSON / stream+json need framing — return empty until tokenizer-style support lands");
    }

    @Test
    void encoder_encodeValue_pojo() {
        DataBuffer buf = encoder.encodeValue(new User(1L, "alice", "a@e.com"),
                factory, ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON, null);
        String body = bufferToString(buf);
        assertTrue(body.contains("\"id\":1"));
        assertTrue(body.contains("\"name\":\"alice\""));
        assertTrue(body.contains("\"email_addr\":\"a@e.com\""));
    }

    @Test
    void encoder_encode_fluxPerElement() {
        Flux<User> users = Flux.just(
                new User(1L, "a", "a@e.com"),
                new User(2L, "b", "b@e.com"));
        Flux<DataBuffer> buffers = encoder.encode(users, factory,
                ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON, null);
        StepVerifier.create(buffers)
                .assertNext(b -> {
                    String s = bufferToString(b);
                    assertTrue(s.contains("\"name\":\"a\""));
                })
                .assertNext(b -> {
                    String s = bufferToString(b);
                    assertTrue(s.contains("\"name\":\"b\""));
                })
                .verifyComplete();
    }

    @Test
    void encoder_supportsRepeatableEncoding() {
        // Encoding the same value twice produces identical bytes
        // (same shape as repeated POST body emission for retry).
        User u = new User(1L, "alice", "a@e.com");
        String first = bufferToString(encoder.encodeValue(u, factory,
                ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON, null));
        String second = bufferToString(encoder.encodeValue(u, factory,
                ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON, null));
        assertEquals(first, second);
    }

    // ---- Custom mapper round-trip ----

    @Test
    void customMapper_isUsedByBothCodecs() {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3JsonDecoder d = new Fastjson3JsonDecoder(custom);
        Fastjson3JsonEncoder e = new Fastjson3JsonEncoder(custom);
        User u = new User(1L, "alice", "a@e.com");
        DataBuffer buf = e.encodeValue(u, factory,
                ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON, null);
        Object decoded = d.decode(buf, ResolvableType.forClass(User.class),
                MediaType.APPLICATION_JSON, null);
        assertInstanceOf(User.class, decoded);
        assertEquals("alice", ((User) decoded).name);
    }

    // ---- Round-trip via Mono ----

    @Test
    void monoRoundTrip_pojo() {
        User u = new User(7L, "carol", "c@e.com");
        Flux<DataBuffer> encoded = encoder.encode(Mono.just(u), factory,
                ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON, null);
        Mono<Object> decoded = decoder.decodeToMono(encoded,
                ResolvableType.forClass(User.class), MediaType.APPLICATION_JSON, null);
        StepVerifier.create(decoded)
                .assertNext(o -> {
                    User back = (User) o;
                    assertEquals(7L, back.id);
                    assertEquals("carol", back.name);
                    assertEquals("c@e.com", back.email);
                })
                .verifyComplete();
    }
}

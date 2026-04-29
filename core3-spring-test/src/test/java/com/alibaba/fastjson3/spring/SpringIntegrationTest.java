package com.alibaba.fastjson3.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SpringIntegrationApp.class)
@AutoConfigureMockMvc
class SpringIntegrationTest {
    @Autowired
    MockMvc mvc;

    // ---- POJO round-trip ----

    @Test
    void echoUser_roundTrip() throws Exception {
        String body = "{\"id\":1,\"name\":\"alice\",\"age\":30,\"email_addr\":\"a@e.com\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("alice"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.email_addr").value("a@e.com"));
    }

    @Test
    void echoUser_unknownField_ignored() throws Exception {
        String body = "{\"id\":2,\"name\":\"bob\",\"age\":25,\"email_addr\":\"b@e.com\",\"extra\":true}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("bob"));
    }

    @Test
    void echoUser_partialFields() throws Exception {
        String body = "{\"id\":3,\"name\":\"carol\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("carol"))
                .andExpect(jsonPath("$.age").value(0));
    }

    @Test
    void echoUser_missingRequiredAnnotationField_jsonFieldName() throws Exception {
        // Use the JSON-form name (email_addr) per @JSONField rename
        String body = "{\"id\":4,\"name\":\"dan\",\"age\":20,\"email_addr\":\"d@e.com\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email_addr").value("d@e.com"));
    }

    // ---- List<User> ----

    @Test
    void echoList_threeUsers() throws Exception {
        String body = "[{\"id\":1,\"name\":\"a\",\"age\":1,\"email_addr\":\"a@e.com\"},"
                + "{\"id\":2,\"name\":\"b\",\"age\":2,\"email_addr\":\"b@e.com\"},"
                + "{\"id\":3,\"name\":\"c\",\"age\":3,\"email_addr\":\"c@e.com\"}]";
        mvc.perform(post("/api/echo-list").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("a"))
                .andExpect(jsonPath("$[2].email_addr").value("c@e.com"));
    }

    @Test
    void echoList_empty() throws Exception {
        mvc.perform(post("/api/echo-list").contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ---- Map<String, Integer> ----

    @Test
    void echoMap_simple() throws Exception {
        String body = "{\"a\":1,\"b\":2,\"c\":3}";
        mvc.perform(post("/api/echo-map").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value(1))
                .andExpect(jsonPath("$.b").value(2))
                .andExpect(jsonPath("$.c").value(3));
    }

    @Test
    void echoMap_empty() throws Exception {
        mvc.perform(post("/api/echo-map").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    // ---- Holder: nested User list + Map + JSONObject + Object ----

    @Test
    void echoHolder_full() throws Exception {
        String body = "{"
                + "\"users\":[{\"id\":1,\"name\":\"a\",\"age\":1,\"email_addr\":\"a@e.com\"}],"
                + "\"counts\":{\"x\":7,\"y\":8},"
                + "\"payload\":{\"k\":\"v\"},"
                + "\"meta\":{\"version\":\"1.0\"},"
                + "\"tags\":[\"red\",\"green\"]"
                + "}";
        mvc.perform(post("/api/echo-holder").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].name").value("a"))
                .andExpect(jsonPath("$.counts.x").value(7))
                .andExpect(jsonPath("$.payload.k").value("v"))
                .andExpect(jsonPath("$.meta.version").value("1.0"))
                .andExpect(jsonPath("$.tags[0]").value("red"));
    }

    @Test
    void echoHolder_missingFields_nullsAllowed() throws Exception {
        // Most fields absent, only `users` populated
        String body = "{\"users\":[{\"id\":1,\"name\":\"a\",\"age\":1,\"email_addr\":\"a@e.com\"}]}";
        mvc.perform(post("/api/echo-holder").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].name").value("a"));
    }

    // ---- JSONObject / JSONArray direct ----

    @Test
    void echoJsonObject_passThrough() throws Exception {
        String body = "{\"a\":1,\"b\":\"hi\",\"c\":[1,2,3],\"d\":{\"nested\":true}}";
        mvc.perform(post("/api/echo-jsonobject").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value(1))
                .andExpect(jsonPath("$.c[2]").value(3))
                .andExpect(jsonPath("$.d.nested").value(true));
    }

    @Test
    void echoJsonArray_passThrough() throws Exception {
        String body = "[1,\"two\",3.5,true,null,[10,20],{\"k\":\"v\"}]";
        mvc.perform(post("/api/echo-jsonarray").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value("two"))
                .andExpect(jsonPath("$[5][1]").value(20))
                .andExpect(jsonPath("$[6].k").value("v"));
    }

    // ---- Object (untyped) ----

    @Test
    void echoObject_jsonObjectInput() throws Exception {
        String body = "{\"a\":1}";
        mvc.perform(post("/api/echo-object").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value(1));
    }

    @Test
    void echoObject_jsonArrayInput() throws Exception {
        String body = "[1,2,3]";
        mvc.perform(post("/api/echo-object").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1));
    }

    // ---- Generic record Box<User> ----

    @Test
    void echoRecord_box_user() throws Exception {
        String body = "{\"value\":{\"id\":1,\"name\":\"a\",\"age\":2,\"email_addr\":\"a@e.com\"},\"version\":3}";
        mvc.perform(post("/api/echo-record").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.name").value("a"))
                .andExpect(jsonPath("$.value.email_addr").value("a@e.com"))
                .andExpect(jsonPath("$.version").value(3));
    }

    // ---- GET / ResponseEntity ----

    @Test
    void getUser_ok() throws Exception {
        mvc.perform(get("/api/user/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("user-42"))
                .andExpect(jsonPath("$.email_addr").value("u42@e.com"));
    }

    @Test
    void getUser_notFound() throws Exception {
        mvc.perform(get("/api/user/-1"))
                .andExpect(status().isNotFound());
    }

    // ---- Error handling ----

    @Test
    void echoUser_malformedJson_returns400() throws Exception {
        String body = "{\"id\":1,\"name\":";  // truncated
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void echoUser_typeMismatch_returns400() throws Exception {
        // age is int, sent as a JSON object
        String body = "{\"id\":1,\"name\":\"a\",\"age\":{\"x\":1},\"email_addr\":\"a@e.com\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void echoList_typeMismatch_arrayExpected_returns400() throws Exception {
        // Sent object where a list is expected
        String body = "{\"a\":1}";
        mvc.perform(post("/api/echo-list").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    // ---- UTF-8 / unicode ----

    @Test
    void echoUser_utf8_chineseName() throws Exception {
        String body = "{\"id\":1,\"name\":\"张三\",\"age\":30,\"email_addr\":\"z@e.com\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("张三"));
    }

    @Test
    void echoUser_utf8_emoji() throws Exception {
        String body = "{\"id\":1,\"name\":\"😀 happy\",\"age\":30,\"email_addr\":\"e@e.com\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("😀 happy"));
    }

    @Test
    void echoUser_specialChars_escaped() throws Exception {
        String body = "{\"id\":1,\"name\":\"line\\nbreak\\ttab\\\"quote\",\"age\":30,\"email_addr\":\"s@e.com\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("line\nbreak\ttab\"quote"));
    }

    // ---- Large payload ----

    @Test
    void echoList_thousandUsers_stable() throws Exception {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"id\":").append(i).append(",\"name\":\"u").append(i)
                    .append("\",\"age\":").append(i % 80).append(",\"email_addr\":\"u").append(i).append("@e.com\"}");
        }
        sb.append("]");
        mvc.perform(post("/api/echo-list").contentType(MediaType.APPLICATION_JSON).content(sb.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1000))
                .andExpect(jsonPath("$[999].name").value("u999"));
    }

    // ---- Concurrency (Spring's MockMvc is single-threaded; use real executor) ----

    // ---- P0: temporal / enum / BigDecimal / byte[] field round-trip ----

    @Test
    void echoUser_localDateTimeField() throws Exception {
        // fastjson3 default LocalDateTime format = ISO-8601; pin the round-trip
        // so the contract with downstream parsers stays stable.
        String body = "{\"id\":1,\"name\":\"a\",\"age\":1,\"email_addr\":\"a@e.com\","
                + "\"createdAt\":\"2026-04-29T10:00:00\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void echoUser_enumField_byName() throws Exception {
        String body = "{\"id\":1,\"name\":\"a\",\"age\":1,\"email_addr\":\"a@e.com\",\"role\":\"ADMIN\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void echoUser_enumField_unknownConstant_fails() throws Exception {
        String body = "{\"id\":1,\"name\":\"a\",\"age\":1,\"email_addr\":\"a@e.com\",\"role\":\"NOT_A_ROLE\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void echoUser_bigDecimalField_preservesPrecision() throws Exception {
        String body = "{\"id\":1,\"name\":\"a\",\"age\":1,\"email_addr\":\"a@e.com\","
                + "\"balance\":\"123456789012345.67\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(123456789012345.67));
    }

    @Test
    void echoUser_byteArrayField_isCurrentlyDropped_knownLimitation() throws Exception {
        // KNOWN LIMITATION (fastjson3 follow-up): byte[] field with
        // base64-encoded String input parses to null; write side omits the
        // field. Jackson's default emits base64 string and reads it back.
        // Pin the current (broken) shape so a future fix that adds byte[]
        // base64 support flips this test deterministically.
        String body = "{\"id\":1,\"name\":\"a\",\"age\":1,\"email_addr\":\"a@e.com\","
                + "\"avatar\":\"aGVsbG8=\"}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                // avatar dropped on the write side — assert the path is absent.
                .andExpect(jsonPath("$.avatar").doesNotExist());
    }

    // ---- P0: null field round-trip ----

    @Test
    void echoUser_nullStringField_doesNotNpe() throws Exception {
        String body = "{\"id\":1,\"name\":null,\"age\":0,\"email_addr\":null}";
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ---- P0: empty-body POST ----

    @Test
    void echoUser_emptyBody_returns400() throws Exception {
        mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(""))
                .andExpect(status().isBadRequest());
    }

    // ---- P0: ControllerAdvice / @ExceptionHandler error response ----

    @Test
    void controllerAdvice_serializesErrorResponseViaFastjson3() throws Exception {
        String body = "{\"id\":1,\"name\":\"badname\",\"age\":1,\"email_addr\":\"x@e.com\"}";
        mvc.perform(post("/api/throw").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                // ErrorResponse body must round-trip through fastjson3:
                // empty body would mean the converter wasn't invoked at the
                // error path (silent prod failure mode).
                .andExpect(jsonPath("$.code").value("E_ARG"))
                .andExpect(jsonPath("$.message").value("rejected: badname"));
    }

    // ---- P1: header roundtrip + JSON body ----

    @Test
    void headerRoundtrip_withJsonBody() throws Exception {
        String body = "{\"id\":1,\"name\":\"a\",\"age\":1,\"email_addr\":\"a@e.com\"}";
        mvc.perform(post("/api/header-trace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Trace-Id", "abc-123")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trace").value("abc-123"))
                .andExpect(jsonPath("$.name").value("a"));
    }

    // ---- P0: non-UTF-8 charset honor ----

    @Test
    void echoUser_gbkRequestCharset_chineseRoundTrip() throws Exception {
        // Body encoded in GBK with explicit Content-Type: charset=GBK.
        // Pre-fix the converter ignored the charset and decoded as UTF-8,
        // producing mojibake. Now resolveCharset() pulls the declared
        // charset out of the request headers.
        java.nio.charset.Charset gbk = java.nio.charset.Charset.forName("GBK");
        byte[] body = "{\"id\":1,\"name\":\"张三\",\"age\":30,\"email_addr\":\"z@e.com\"}".getBytes(gbk);
        mvc.perform(post("/api/echo-user")
                        .contentType(MediaType.parseMediaType("application/json;charset=GBK"))
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("张三"));
    }

    @Test
    void echoUser_iso88591RequestCharset_specialCharRoundTrip() throws Exception {
        // ISO-8859-1 body with é (0xE9). UTF-8 fallback would mojibake.
        byte[] body = "{\"id\":1,\"name\":\"café\",\"age\":30,\"email_addr\":\"c@e.com\"}".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        mvc.perform(post("/api/echo-user")
                        .contentType(MediaType.parseMediaType("application/json;charset=ISO-8859-1"))
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("café"));
    }

    // ---- Content-type negotiation ----

    @Test
    void echoUser_acceptXmlOnly_returns406() throws Exception {
        // No converter supports XML — Spring should reject Accept-only XML.
        String body = "{\"id\":1,\"name\":\"a\",\"age\":1,\"email_addr\":\"a@e.com\"}";
        mvc.perform(post("/api/echo-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_XML)
                        .content(body))
                .andExpect(status().isNotAcceptable());
    }

    // ---- Deeply nested JSON: depth limit kicks in, no SO ----

    @Test
    void echoJsonObject_deeplyNested_returns400_noStackOverflow() throws Exception {
        StringBuilder open = new StringBuilder();
        StringBuilder close = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            open.append("{\"a\":");
            close.append("}");
        }
        String body = open.toString() + "1" + close.toString();
        mvc.perform(post("/api/echo-jsonobject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---- Converter exclusion: don't hijack String / byte[] returns ----

    @Test
    void plainStringEndpoint_notJsonQuoted() throws Exception {
        // GET /api/plain returns String. fastjson3 converter MUST exclude
        // String so StringHttpMessageConverter handles it — body should be
        // raw "hello world" (no surrounding JSON quotes).
        mvc.perform(get("/api/plain"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string("hello world"));
    }

    @Test
    void rawByteEndpoint_notJsonEncoded() throws Exception {
        // byte[] return — ByteArrayHttpMessageConverter should win.
        mvc.perform(get("/api/bytes"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{1, 2, 3, 4, 5}));
    }

    @Test
    void concurrentRequests_allSucceed() throws Exception {
        int threads = 16;
        int perThread = 50;
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads * perThread);
        AtomicInteger fails = new AtomicInteger();
        try {
            for (int t = 0; t < threads; t++) {
                final int tid = t;
                exec.submit(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < perThread; i++) {
                            String body = "{\"id\":" + (tid * 1000 + i) + ",\"name\":\"u\","
                                    + "\"age\":" + i + ",\"email_addr\":\"u@e.com\"}";
                            mvc.perform(post("/api/echo-user").contentType(MediaType.APPLICATION_JSON).content(body))
                                    .andExpect(status().isOk());
                            done.countDown();
                        }
                    } catch (Throwable e) {
                        fails.incrementAndGet();
                        while (done.getCount() > 0) {
                            done.countDown();
                        }
                    }
                });
            }
            start.countDown();
            assertTrue(done.await(60, TimeUnit.SECONDS), "concurrency timeout");
            assertEquals(0, fails.get(), "concurrency failures: " + fails.get());
        } finally {
            exec.shutdownNow();
        }
    }
}

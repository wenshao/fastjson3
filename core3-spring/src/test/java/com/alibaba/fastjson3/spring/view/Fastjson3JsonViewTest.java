package com.alibaba.fastjson3.spring.view;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindingResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit-level coverage for {@link Fastjson3JsonView}. Drives the view
 * through Spring's {@code MockHttpServletRequest} / {@code MockHttpServletResponse}
 * so the tests don't need a servlet container.
 */
class Fastjson3JsonViewTest {
    @Test
    void render_writesJsonBody() throws Exception {
        Fastjson3JsonView view = new Fastjson3JsonView();
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("id", 1);
        model.put("name", "alice");

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(model, req, resp);

        // Spring appends ;charset=UTF-8 to the content type during render —
        // assert prefix instead of exact match.
        assertTrue(resp.getContentType().startsWith("application/json"));
        assertEquals("UTF-8", resp.getCharacterEncoding());
        JSONObject body = JSON.parseObject(resp.getContentAsString());
        assertEquals(1, body.get("id"));
        assertEquals("alice", body.get("name"));
    }

    @Test
    void render_filtersByRenderedAttributes() throws Exception {
        Fastjson3JsonView view = new Fastjson3JsonView();
        view.setRenderedAttributes(Set.of("id"));
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("id", 1);
        model.put("secret", "hidden");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(model, new MockHttpServletRequest(), resp);

        JSONObject body = JSON.parseObject(resp.getContentAsString());
        assertEquals(1, body.get("id"));
        assertFalse(body.containsKey("secret"),
                "secret must be filtered out by renderedAttributes");
    }

    @Test
    void render_skipsBindingResult() throws Exception {
        Fastjson3JsonView view = new Fastjson3JsonView();
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("id", 1);
        model.put("errors", new StubBindingResult());

        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(model, new MockHttpServletRequest(), resp);

        JSONObject body = JSON.parseObject(resp.getContentAsString());
        assertEquals(1, body.get("id"));
        assertFalse(body.containsKey("errors"),
                "BindingResult instances must be filtered from the model");
    }

    @Test
    void render_extractValueFromSingleKeyModel_unwrapsMap() throws Exception {
        // Default (extract = false): emits {"data": {...}}
        // With extract = true on a single-key model: emits {...} directly.
        Fastjson3JsonView view = new Fastjson3JsonView();
        view.setExtractValueFromSingleKeyModel(true);
        Map<String, Object> model = new LinkedHashMap<>();
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("name", "alice");
        model.put("data", inner);

        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(model, new MockHttpServletRequest(), resp);

        JSONObject body = JSON.parseObject(resp.getContentAsString());
        assertEquals("alice", body.get("name"),
                "single-key model should be un-wrapped to the inner value");
        assertFalse(body.containsKey("data"),
                "wrapper key should be gone after extractValueFromSingleKeyModel");
    }

    @Test
    void render_extractValueFromSingleKeyModel_doesNotUnwrapMultiKey() throws Exception {
        Fastjson3JsonView view = new Fastjson3JsonView();
        view.setExtractValueFromSingleKeyModel(true);
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("a", 1);
        model.put("b", 2);

        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(model, new MockHttpServletRequest(), resp);

        JSONObject body = JSON.parseObject(resp.getContentAsString());
        assertEquals(1, body.get("a"));
        assertEquals(2, body.get("b"));
    }

    @Test
    void render_disableCaching_default_emitsCacheHeaders() throws Exception {
        Fastjson3JsonView view = new Fastjson3JsonView();
        // disableCaching defaults to true.
        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(Map.of("k", 1), new MockHttpServletRequest(), resp);

        assertEquals("no-cache", resp.getHeader("Pragma"));
        assertEquals("no-cache, no-store, max-age=0", resp.getHeader("Cache-Control"));
        assertNotNull(resp.getHeader("Expires"));
    }

    @Test
    void render_disableCaching_false_omitsCacheHeaders() throws Exception {
        Fastjson3JsonView view = new Fastjson3JsonView();
        view.setDisableCaching(false);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(Map.of("k", 1), new MockHttpServletRequest(), resp);

        assertEquals(null, resp.getHeader("Pragma"));
        assertEquals(null, resp.getHeader("Cache-Control"));
    }

    @Test
    void render_updateContentLength_setsHeader() throws Exception {
        Fastjson3JsonView view = new Fastjson3JsonView();
        view.setUpdateContentLength(true);
        Map<String, Object> model = Map.of("name", "alice");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(model, new MockHttpServletRequest(), resp);

        int len = resp.getContentAsString().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        assertEquals(len, resp.getContentLength());
    }

    @Test
    void render_updateContentLength_default_doesNotSet() throws Exception {
        Fastjson3JsonView view = new Fastjson3JsonView();
        // default updateContentLength = false
        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(Map.of("k", 1), new MockHttpServletRequest(), resp);

        // The view did not explicitly set the Content-Length header.
        assertNull(resp.getHeader("Content-Length"));
    }

    @Test
    void render_listModel_extracted() throws Exception {
        // Common ModelAndView pattern: Model.addAttribute("users", List.of(...))
        // with extractValueFromSingleKeyModel=true emits a top-level JSON array.
        Fastjson3JsonView view = new Fastjson3JsonView();
        view.setExtractValueFromSingleKeyModel(true);
        Map<String, Object> model = Map.of("users", List.of("alice", "bob"));

        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(model, new MockHttpServletRequest(), resp);

        String body = resp.getContentAsString();
        assertTrue(body.startsWith("["),
                "single-key list model should render as a top-level array, got: " + body);
        assertTrue(body.contains("alice"));
        assertTrue(body.contains("bob"));
    }

    @Test
    void customMapper_isUsed() throws Exception {
        // Pass a distinct mapper instance and verify the view uses it for
        // serialization (round-trip the data through the custom mapper).
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3JsonView view = new Fastjson3JsonView(custom);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(Map.of("k", "v"), new MockHttpServletRequest(), resp);

        JSONObject body = JSON.parseObject(resp.getContentAsString());
        assertEquals("v", body.get("k"));
    }

    @Test
    void constructor_rejectsNullMapper() {
        assertThrows(IllegalArgumentException.class, () -> new Fastjson3JsonView(null));
    }

    @Test
    void contentType_defaultsToApplicationJson() {
        Fastjson3JsonView view = new Fastjson3JsonView();
        assertEquals("application/json", view.getContentType());
    }

    @Test
    void render_emptyRenderedAttributes_treatsAsRenderAll() throws Exception {
        // Edge case: setting an empty (not null) renderedAttributes should
        // mean "render everything", matching fj2 semantics.
        Fastjson3JsonView view = new Fastjson3JsonView();
        view.setRenderedAttributes(Set.of());
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("a", 1);
        model.put("b", 2);

        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(model, new MockHttpServletRequest(), resp);

        JSONObject body = JSON.parseObject(resp.getContentAsString());
        assertEquals(1, body.get("a"));
        assertEquals(2, body.get("b"));
    }

    @Test
    void render_unicode_emitsUtf8() throws Exception {
        // CJK characters must round-trip verbatim through the UTF-8 byte path.
        Fastjson3JsonView view = new Fastjson3JsonView();
        Map<String, Object> model = Map.of("name", "测试用户", "city", "北京");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(model, new MockHttpServletRequest(), resp);

        byte[] body = resp.getContentAsByteArray();
        String decoded = new String(body, java.nio.charset.StandardCharsets.UTF_8);
        JSONObject parsed = JSON.parseObject(decoded);
        assertEquals("测试用户", parsed.get("name"));
        assertEquals("北京", parsed.get("city"));
    }

    @Test
    void render_nullValue_inModel_serializesAsNull() throws Exception {
        // Pin current write policy: null map values land in JSON as `null`
        // rather than being skipped. Catches a future writer-feature flip.
        Fastjson3JsonView view = new Fastjson3JsonView();
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("id", 1);
        model.put("optional", null);

        MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(model, new MockHttpServletRequest(), resp);

        String body = resp.getContentAsString();
        assertTrue(body.contains("\"optional\":null"),
                "expected null value preserved as JSON null, got: " + body);
    }

    @Test
    void render_threadSafe_underConcurrentRender() throws Exception {
        // Spring caches View beans; a single instance fields concurrent
        // requests. Mirror the Redis serializer's 16-thread × 100-iter
        // pattern to pin we don't mutate shared state during render.
        Fastjson3JsonView view = new Fastjson3JsonView();
        int threads = 16;
        int per = 100;
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(threads);
        java.util.concurrent.atomic.AtomicInteger fails = new java.util.concurrent.atomic.AtomicInteger();
        try {
            for (int t = 0; t < threads; t++) {
                final int tid = t;
                pool.submit(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < per; i++) {
                            String name = "u-" + tid + "-" + i;
                            MockHttpServletResponse resp = new MockHttpServletResponse();
                            view.render(Map.of("name", name), new MockHttpServletRequest(), resp);
                            JSONObject body = JSON.parseObject(resp.getContentAsString());
                            if (!name.equals(body.get("name"))) {
                                fails.incrementAndGet();
                            }
                        }
                    } catch (Throwable e) {
                        fails.incrementAndGet();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertTrue(done.await(30, java.util.concurrent.TimeUnit.SECONDS),
                    "concurrency timeout");
            assertEquals(0, fails.get(),
                    "concurrent render had " + fails.get() + " failures");
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * Minimal {@link BindingResult} stub so {@code render_skipsBindingResult}
     * can verify the filter without pulling in spring-validation infrastructure.
     */
    private static class StubBindingResult implements BindingResult {
        @Override public Object getTarget() { return null; }
        @Override public Map<String, Object> getModel() { return Map.of(); }
        @Override public Object getRawFieldValue(String field) { return null; }
        @Override public java.beans.PropertyEditor findEditor(String field, Class<?> valueType) { return null; }
        @Override public PropertyEditorRegistry getPropertyEditorRegistry() { return null; }
        @Override public String[] resolveMessageCodes(String errorCode) { return new String[0]; }
        @Override public String[] resolveMessageCodes(String errorCode, String field) { return new String[0]; }
        @Override public void addError(org.springframework.validation.ObjectError error) { }
        @Override public String getObjectName() { return ""; }
        @Override public void setNestedPath(String nestedPath) { }
        @Override public String getNestedPath() { return ""; }
        @Override public void pushNestedPath(String subPath) { }
        @Override public void popNestedPath() { }
        @Override public void reject(String errorCode) { }
        @Override public void reject(String errorCode, String defaultMessage) { }
        @Override public void reject(String errorCode, Object[] errorArgs, String defaultMessage) { }
        @Override public void rejectValue(String field, String errorCode) { }
        @Override public void rejectValue(String field, String errorCode, String defaultMessage) { }
        @Override public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) { }
        @Override public void addAllErrors(org.springframework.validation.Errors errors) { }
        @Override public boolean hasErrors() { return false; }
        @Override public int getErrorCount() { return 0; }
        @Override public java.util.List<org.springframework.validation.ObjectError> getAllErrors() { return List.of(); }
        @Override public boolean hasGlobalErrors() { return false; }
        @Override public int getGlobalErrorCount() { return 0; }
        @Override public java.util.List<org.springframework.validation.ObjectError> getGlobalErrors() { return List.of(); }
        @Override public org.springframework.validation.ObjectError getGlobalError() { return null; }
        @Override public boolean hasFieldErrors() { return false; }
        @Override public int getFieldErrorCount() { return 0; }
        @Override public java.util.List<org.springframework.validation.FieldError> getFieldErrors() { return List.of(); }
        @Override public org.springframework.validation.FieldError getFieldError() { return null; }
        @Override public boolean hasFieldErrors(String field) { return false; }
        @Override public int getFieldErrorCount(String field) { return 0; }
        @Override public java.util.List<org.springframework.validation.FieldError> getFieldErrors(String field) { return List.of(); }
        @Override public org.springframework.validation.FieldError getFieldError(String field) { return null; }
        @Override public Object getFieldValue(String field) { return null; }
        @Override public Class<?> getFieldType(String field) { return null; }
    }
}

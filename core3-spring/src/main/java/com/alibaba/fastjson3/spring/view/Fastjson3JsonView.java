package com.alibaba.fastjson3.spring.view;

import com.alibaba.fastjson3.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.AbstractView;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Spring MVC {@link AbstractView} that renders a model map as JSON via
 * fastjson3's {@link ObjectMapper}. Drop-in replacement for fastjson2's
 * {@code FastJsonJsonView} — same {@code renderedAttributes},
 * {@code disableCaching}, {@code extractValueFromSingleKeyModel}, and
 * {@code updateContentLength} knobs.
 *
 * <p>Use this when the application returns {@code ModelAndView} objects
 * via {@code ViewResolver} chains (the legacy Spring MVC pattern). Modern
 * {@code @RestController} / {@code @ResponseBody} apps should use
 * {@link com.alibaba.fastjson3.spring.Fastjson3HttpMessageConverter}
 * instead.</p>
 *
 * <p><b>Usage</b>:</p>
 * <pre>{@code
 * @Bean
 * public ContentNegotiatingViewResolver viewResolver() {
 *     ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
 *     resolver.setDefaultViews(List.of(new Fastjson3JsonView()));
 *     return resolver;
 * }
 * }</pre>
 *
 * <p>Pass a configured {@link ObjectMapper} to the two-arg constructor for
 * per-instance settings.</p>
 */
public class Fastjson3JsonView extends AbstractView {
    private final ObjectMapper mapper;

    private Set<String> renderedAttributes;
    private boolean disableCaching = true;
    private boolean extractValueFromSingleKeyModel;
    private boolean updateContentLength;

    public Fastjson3JsonView() {
        this(ObjectMapper.shared());
    }

    public Fastjson3JsonView(ObjectMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
        setContentType(MediaType.APPLICATION_JSON_VALUE);
        setExposePathVariables(false);
    }

    /**
     * Set the attributes (model keys) the view should render. Other
     * attributes are skipped. Null / empty means render all attributes.
     */
    public void setRenderedAttributes(Set<String> renderedAttributes) {
        this.renderedAttributes = renderedAttributes;
    }

    public boolean isExtractValueFromSingleKeyModel() {
        return extractValueFromSingleKeyModel;
    }

    /**
     * If true and the filtered model contains a single key, render the
     * value alone (un-wrapping the map). Useful when the view should emit
     * a JSON object / array directly rather than {@code {"key": ...}}.
     */
    public void setExtractValueFromSingleKeyModel(boolean extractValueFromSingleKeyModel) {
        this.extractValueFromSingleKeyModel = extractValueFromSingleKeyModel;
    }

    /**
     * Disables caching of the generated JSON response (sets {@code Pragma},
     * {@code Cache-Control}, {@code Expires} headers). Default {@code true}.
     */
    public void setDisableCaching(boolean disableCaching) {
        this.disableCaching = disableCaching;
    }

    /**
     * Compute and set the {@code Content-Length} header on the response
     * before writing the body. Default {@code false}.
     */
    public void setUpdateContentLength(boolean updateContentLength) {
        this.updateContentLength = updateContentLength;
    }

    @Override
    protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
        setResponseContentType(request, response);
        // fj3 emits UTF-8 unconditionally — matches RFC 8259 and the rest
        // of the fastjson3-spring artifacts.
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (disableCaching) {
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache, no-store, max-age=0");
            response.addDateHeader("Expires", 1L);
        }
    }

    @Override
    protected void renderMergedOutputModel(
            Map<String, Object> model,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        Object value = filterModel(model);
        byte[] bytes = mapper.writeValueAsBytes(value);
        if (updateContentLength) {
            response.setContentLength(bytes.length);
        }
        ServletOutputStream out = response.getOutputStream();
        out.write(bytes);
        out.flush();
    }

    /**
     * Filters out undesired attributes from the given model. Default
     * implementation removes {@link BindingResult} entries and entries
     * whose keys are not in {@link #setRenderedAttributes(Set)} (when set).
     */
    protected Object filterModel(Map<String, Object> model) {
        // LinkedHashMap preserves insertion order — important when the
        // caller assembled the model in a meaningful order.
        Map<String, Object> result = new LinkedHashMap<>(model.size());
        Set<String> attributes = !CollectionUtils.isEmpty(renderedAttributes)
                ? renderedAttributes
                : model.keySet();
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            if (!(entry.getValue() instanceof BindingResult)
                    && attributes.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        if (extractValueFromSingleKeyModel && result.size() == 1) {
            return result.values().iterator().next();
        }
        return result;
    }
}

package com.alibaba.fastjson3.jaxrs.jakarta;

import com.alibaba.fastjson3.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * JAX-RS {@link MessageBodyReader} / {@link MessageBodyWriter} provider that
 * delegates JSON serialization to fastjson3.
 *
 * <p>Auto-discovered by JAX-RS implementations (Jersey 3+, RESTEasy 6+, CXF 4+)
 * via the {@link Provider} annotation. Manual registration:
 * <pre>{@code
 *   ResourceConfig config = new ResourceConfig();
 *   config.register(Fastjson3Provider.class);
 * }</pre>
 *
 * <p>Backed by a single {@link ObjectMapper}; defaults to {@link ObjectMapper#shared()}.
 * Pass a configured mapper through {@link #Fastjson3Provider(ObjectMapper)} to
 * customize features.
 *
 * <p><b>Custom mapper note</b>: when JAX-RS auto-discovery instantiates this
 * provider via the {@code @Provider} classpath scan, the no-arg constructor
 * runs and the shared mapper is used. The {@code spring.fastjson3.*}
 * properties wired by {@code Fastjson3HttpMessageConverterAutoConfiguration}
 * customize the Spring MVC converter only — they do not propagate here.
 * To inject a configured mapper, register an instance manually:
 * <pre>{@code
 *   resourceConfig.register(new Fastjson3Provider(myCustomMapper));
 * }</pre>
 *
 * <p><b>String / byte[] passthrough</b>: this provider does not hijack
 * {@code String} or {@code byte[]} — these route through JAX-RS's
 * built-in body readers/writers unchanged so resources returning a
 * pre-encoded JSON {@code String} or raw bytes pass through verbatim
 * (mirrors Spring's {@code Fastjson3HttpMessageConverter}). To route
 * those types through fastjson3, declare them on an explicit
 * {@code allowList}. Primitive wrappers ({@code Long}, {@code Boolean},
 * etc.) ARE serialized as JSON: {@code @Produces(application/json)
 * public Long getCount()} returns a numeric body.
 */
@Provider
@Consumes({MediaType.APPLICATION_JSON, "application/*+json"})
@Produces({MediaType.APPLICATION_JSON, "application/*+json"})
public class Fastjson3Provider
        implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
    private static final Class<?>[] DEFAULT_UNREADABLES = {
            InputStream.class, Reader.class,
            String.class, byte[].class
    };
    private static final Class<?>[] DEFAULT_UNWRITABLES = {
            InputStream.class, OutputStream.class, Writer.class,
            StreamingOutput.class, Response.class,
            String.class, byte[].class
    };

    private final ObjectMapper mapper;
    private final Class<?>[] allowList;

    public Fastjson3Provider() {
        this(ObjectMapper.shared(), null);
    }

    public Fastjson3Provider(ObjectMapper mapper) {
        this(mapper, null);
    }

    public Fastjson3Provider(ObjectMapper mapper, Class<?>[] allowList) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
        this.allowList = allowList == null ? null : allowList.clone();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return matchesMediaType(mediaType)
                && !isAssignableFromAny(type, DEFAULT_UNREADABLES)
                && isAllowed(type);
    }

    @Override
    public Object readFrom(
            Class<Object> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream
    ) throws IOException, WebApplicationException {
        try {
            return mapper.readValue(entityStream, genericType != null ? genericType : type);
        } catch (RuntimeException ex) {
            // JSONException + any unexpected NPE/AIOOBE/IAE from adversarial input
            throw new WebApplicationException(ex.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return matchesMediaType(mediaType)
                && !isAssignableFromAny(type, DEFAULT_UNWRITABLES)
                && isAllowed(type);
    }

    @Override
    public void writeTo(
            Object object,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream
    ) throws IOException, WebApplicationException {
        entityStream.write(mapper.writeValueAsBytes(object));
    }

    private static boolean matchesMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }
        String subtype = mediaType.getSubtype();
        if (subtype == null) {
            return false;
        }
        return "json".equalsIgnoreCase(subtype) || subtype.endsWith("+json");
    }

    private static boolean isAssignableFromAny(Class<?> type, Class<?>[] classes) {
        if (type == null) {
            return false;
        }
        for (Class<?> cls : classes) {
            if (cls.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowed(Class<?> type) {
        if (allowList == null) {
            return true;
        }
        for (Class<?> cls : allowList) {
            if (cls == type) {
                return true;
            }
        }
        return false;
    }
}

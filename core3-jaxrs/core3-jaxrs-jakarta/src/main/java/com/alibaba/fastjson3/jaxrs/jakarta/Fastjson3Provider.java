package com.alibaba.fastjson3.jaxrs.jakarta;

import com.alibaba.fastjson3.JSONException;
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

import java.io.ByteArrayOutputStream;
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
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class Fastjson3Provider
        implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
    private static final Class<?>[] DEFAULT_UNREADABLES = {InputStream.class, Reader.class};
    private static final Class<?>[] DEFAULT_UNWRITABLES = {
            InputStream.class, OutputStream.class, Writer.class,
            StreamingOutput.class, Response.class
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
        this.mapper = mapper == null ? ObjectMapper.shared() : mapper;
        this.allowList = allowList;
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
            return mapper.readValue(readAll(entityStream), genericType != null ? genericType : type);
        } catch (JSONException ex) {
            throw new WebApplicationException(ex);
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
        try {
            entityStream.write(mapper.writeValueAsBytes(object));
        } catch (JSONException ex) {
            throw new WebApplicationException(ex);
        }
    }

    private static boolean matchesMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }
        String subtype = mediaType.getSubtype();
        if (subtype == null) {
            return true;
        }
        return "json".equalsIgnoreCase(subtype)
                || subtype.endsWith("+json")
                || "javascript".equals(subtype)
                || "x-javascript".equals(subtype)
                || "x-json".equals(subtype);
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

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }
}

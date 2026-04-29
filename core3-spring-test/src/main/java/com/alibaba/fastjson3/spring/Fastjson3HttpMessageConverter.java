package com.alibaba.fastjson3.spring;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectMapper;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Minimal Spring {@link org.springframework.http.converter.HttpMessageConverter}
 * implementation that routes (de)serialization through fastjson3's
 * {@link ObjectMapper}. Designed for the {@code fastjson3-spring-test}
 * integration suite — exercises real HTTP round-trip semantics without
 * pulling in a full extension module.
 *
 * <p>Reads / writes UTF-8 JSON only; {@code application/json}
 * + {@code application/*+json} media types. Generic types (e.g.
 * {@code List<User>}) are handled via {@link GenericHttpMessageConverter}.</p>
 */
public class Fastjson3HttpMessageConverter
        extends AbstractHttpMessageConverter<Object>
        implements GenericHttpMessageConverter<Object> {
    private final ObjectMapper mapper;

    public Fastjson3HttpMessageConverter() {
        this(ObjectMapper.shared());
    }

    public Fastjson3HttpMessageConverter(ObjectMapper mapper) {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
        this.mapper = mapper;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return canRead(mediaType);
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return canWrite(mediaType);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        Type resolvedType = type;
        if (contextClass != null) {
            resolvedType = GenericTypeResolver.resolveType(type, contextClass);
        }
        try {
            return mapper.readValue(inputMessage.getBody(), resolvedType);
        } catch (JSONException e) {
            throw new HttpMessageNotReadableException("JSON parse error: " + e.getMessage(), e, inputMessage);
        }
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        try {
            return mapper.readValue(inputMessage.getBody(), clazz);
        } catch (JSONException e) {
            throw new HttpMessageNotReadableException("JSON parse error: " + e.getMessage(), e, inputMessage);
        }
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        try {
            byte[] bytes = mapper.writeValueAsBytes(o);
            outputMessage.getBody().write(bytes);
            outputMessage.getBody().flush();
        } catch (JSONException e) {
            throw new HttpMessageNotWritableException("JSON write error: " + e.getMessage(), e);
        }
    }

    @Override
    public void write(Object o, Type type, MediaType contentType, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        writeInternal(o, outputMessage);
    }
}

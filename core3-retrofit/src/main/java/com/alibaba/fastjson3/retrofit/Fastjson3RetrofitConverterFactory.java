package com.alibaba.fastjson3.retrofit;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Retrofit 2 {@link Converter.Factory} backed by fastjson3.
 *
 * <p>Wiring:
 * <pre>{@code
 *   Retrofit retrofit = new Retrofit.Builder()
 *       .baseUrl("https://api.example.com/")
 *       .addConverterFactory(Fastjson3RetrofitConverterFactory.create())
 *       .build();
 * }</pre>
 *
 * <p>Or with a configured mapper:
 * <pre>{@code
 *   ObjectMapper mapper = ObjectMapper.builder()
 *       .enableRead(ReadFeature.SupportSmartMatch)
 *       .build();
 *   Retrofit retrofit = new Retrofit.Builder()
 *       .addConverterFactory(Fastjson3RetrofitConverterFactory.create(mapper))
 *       .build();
 * }</pre>
 *
 * <p>Wire format is {@code application/json; charset=utf-8}.
 *
 * <p>Defaults to {@link ObjectMapper#shared()}; pass a configured mapper through
 * {@link #create(ObjectMapper)} to customize features.
 */
public class Fastjson3RetrofitConverterFactory extends Converter.Factory {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final ObjectMapper mapper;

    private Fastjson3RetrofitConverterFactory(ObjectMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
    }

    public static Fastjson3RetrofitConverterFactory create() {
        return new Fastjson3RetrofitConverterFactory(ObjectMapper.shared());
    }

    public static Fastjson3RetrofitConverterFactory create(ObjectMapper mapper) {
        return new Fastjson3RetrofitConverterFactory(mapper);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type,
            Annotation[] annotations,
            Retrofit retrofit) {
        return new ResponseBodyConverter<>(mapper, type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            Type type,
            Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations,
            Retrofit retrofit) {
        return new RequestBodyConverter<>(mapper);
    }

    static final class ResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private final ObjectMapper mapper;
        private final Type type;

        ResponseBodyConverter(ObjectMapper mapper, Type type) {
            this.mapper = mapper;
            this.type = type;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T convert(ResponseBody value) throws IOException {
            try {
                // Prefer the Class<T> overload for the UTF-8 fast path when type
                // is a non-generic class (matches mybatis/jpa/kafka pattern).
                if (type instanceof Class<?>) {
                    return (T) mapper.readValue(value.bytes(), (Class<?>) type);
                }
                return mapper.readValue(value.bytes(), type);
            } catch (JSONException ex) {
                throw new IOException("JSON parse error: " + ex.getMessage(), ex);
            } finally {
                value.close();
            }
        }
    }

    static final class RequestBodyConverter<T> implements Converter<T, RequestBody> {
        private final ObjectMapper mapper;

        RequestBodyConverter(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public RequestBody convert(T value) throws IOException {
            try {
                return RequestBody.create(MEDIA_TYPE, mapper.writeValueAsBytes(value));
            } catch (JSONException ex) {
                throw new IOException("JSON write error: " + ex.getMessage(), ex);
            }
        }
    }
}

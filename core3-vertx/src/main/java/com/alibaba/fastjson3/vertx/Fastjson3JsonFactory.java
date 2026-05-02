package com.alibaba.fastjson3.vertx;

import io.vertx.core.spi.JsonFactory;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Vert.x {@link JsonFactory} service that contributes a fastjson3-backed
 * {@link Fastjson3JsonCodec}. Registered automatically via
 * {@code META-INF/services/io.vertx.core.spi.JsonFactory} when this artifact
 * is on the classpath.
 *
 * <p>Vert.x picks the {@link JsonFactory} with the highest {@link #order()}.
 * The default Jackson factory shipped with vertx-core uses {@code 0}; this
 * factory uses {@code 100} so fastjson3 wins by default. To opt back to
 * Jackson, exclude this artifact from the classpath, or register a
 * higher-priority {@link JsonFactory} via your own
 * {@code META-INF/services/io.vertx.core.spi.JsonFactory}.
 */
public class Fastjson3JsonFactory implements JsonFactory {
    @Override
    public int order() {
        return 100;
    }

    @Override
    public JsonCodec codec() {
        return new Fastjson3JsonCodec();
    }
}

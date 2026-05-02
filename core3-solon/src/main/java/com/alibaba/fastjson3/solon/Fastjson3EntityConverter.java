package com.alibaba.fastjson3.solon;

import org.noear.solon.serialization.AbstractStringEntityConverter;
import org.noear.solon.serialization.SerializerNames;

/**
 * Solon {@link AbstractStringEntityConverter} backed by fastjson3.
 *
 * <p>Bridges Solon's request/response handling with the
 * {@link Fastjson3StringSerializer}: converts JSON request bodies into
 * controller method parameters, and writes JSON responses for handlers
 * annotated with {@code @Json} or returning JSON-shaped types.
 */
public class Fastjson3EntityConverter extends AbstractStringEntityConverter<Fastjson3StringSerializer> {
    public Fastjson3EntityConverter(Fastjson3StringSerializer serializer) {
        super(serializer);
    }

    @Override
    public String[] mappings() {
        return new String[]{SerializerNames.AT_JSON};
    }
}

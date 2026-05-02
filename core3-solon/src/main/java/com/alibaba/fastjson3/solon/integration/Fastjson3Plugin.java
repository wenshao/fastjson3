package com.alibaba.fastjson3.solon.integration;

import com.alibaba.fastjson3.solon.Fastjson3EntityConverter;
import com.alibaba.fastjson3.solon.Fastjson3StringSerializer;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.serialization.SerializerNames;

/**
 * Solon plugin that registers fastjson3 as the JSON serializer + entity
 * converter for the Solon framework.
 *
 * <p>Auto-discovered via {@code META-INF/solon/com.alibaba.fastjson3.
 * fastjson3-solon-plugin.properties} when this artifact is on the classpath.
 *
 * <p>Registers:
 * <ul>
 *   <li>{@link Fastjson3StringSerializer} as the {@code @json} serializer
 *       (replaces Solon's default JSON engine — typically snack3)</li>
 *   <li>{@link Fastjson3EntityConverter} on the entity-converter chain for
 *       request body deserialization + response body rendering</li>
 * </ul>
 */
public class Fastjson3Plugin implements Plugin {
    @Override
    public void start(AppContext context) {
        Fastjson3StringSerializer serializer = new Fastjson3StringSerializer();
        context.wrapAndPut(Fastjson3StringSerializer.class, serializer);
        context.app().serializers().register(SerializerNames.AT_JSON, serializer);

        Fastjson3EntityConverter entityConverter = new Fastjson3EntityConverter(serializer);
        context.wrapAndPut(Fastjson3EntityConverter.class, entityConverter);
        context.app().chains().addEntityConverter(entityConverter);
    }
}

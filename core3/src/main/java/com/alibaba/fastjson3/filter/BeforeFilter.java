package com.alibaba.fastjson3.filter;

import com.alibaba.fastjson3.JSONGenerator;

/**
 * Filter called before an object's fields are written during serialization.
 * Use to inject extra properties at the beginning of the object.
 *
 * <pre>
 * BeforeFilter filter = (generator, object) -&gt; {
 *     generator.writeName("_version");
 *     generator.writeInt32(1);
 * };
 * </pre>
 */
@FunctionalInterface
public interface BeforeFilter {
    /**
     * Called after startObject() but before any field is written.
     *
     * @param generator the JSON generator to write extra properties to
     * @param object    the object being serialized
     */
    void writeBefore(JSONGenerator generator, Object object);
}

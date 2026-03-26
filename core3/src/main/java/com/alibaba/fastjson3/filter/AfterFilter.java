package com.alibaba.fastjson3.filter;

import com.alibaba.fastjson3.JSONGenerator;

/**
 * Filter called after an object's fields are written during serialization.
 * Use to append extra properties at the end of the object.
 *
 * <pre>
 * AfterFilter filter = (generator, object) -&gt; {
 *     generator.writeName("_timestamp");
 *     generator.writeInt64(System.currentTimeMillis());
 * };
 * </pre>
 */
@FunctionalInterface
public interface AfterFilter extends Filter {
    /**
     * Called after all fields are written but before endObject().
     *
     * @param generator the JSON generator to write extra properties to
     * @param object    the object being serialized
     */
    void writeAfter(JSONGenerator generator, Object object);
}

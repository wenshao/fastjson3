package com.alibaba.fastjson3.filter;

import java.lang.reflect.Type;

/**
 * Value filter with access to field context (type, annotations, etc.).
 *
 * <pre>
 * ContextValueFilter filter = (fieldClass, fieldName, source, name, value) -&gt; {
 *     if (fieldClass == String.class &amp;&amp; "password".equals(name)) {
 *         return "***";
 *     }
 *     return value;
 * };
 * </pre>
 */
@FunctionalInterface
public interface ContextValueFilter extends Filter {
    /**
     * Transform a property value with access to field context.
     *
     * @param fieldClass the declared type of the field
     * @param fieldName  the field name (may differ from JSON name after NameFilter)
     * @param source     the object being serialized
     * @param name       the JSON property name
     * @param value      the property value
     * @return the transformed value to write
     */
    Object apply(Class<?> fieldClass, String fieldName, Object source, String name, Object value);
}

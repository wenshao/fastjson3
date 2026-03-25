package com.alibaba.fastjson3.filter;

/**
 * Pre-filter that controls whether a property should be serialized,
 * without needing to read the property value.
 *
 * <p>More efficient than {@link PropertyFilter} when the decision is based
 * solely on the source object and property name (no value access needed).</p>
 *
 * <pre>
 * PropertyPreFilter filter = (source, name) -&gt; !"internalField".equals(name);
 * </pre>
 */
@FunctionalInterface
public interface PropertyPreFilter extends Filter {
    /**
     * @param source the object being serialized
     * @param name   the property name
     * @return true to include this property, false to exclude
     */
    boolean apply(Object source, String name);
}

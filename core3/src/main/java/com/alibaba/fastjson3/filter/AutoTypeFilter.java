package com.alibaba.fastjson3.filter;

/**
 * Filter for controlling which types are allowed during AutoType deserialization.
 *
 * <p>When {@link com.alibaba.fastjson3.ReadFeature#SupportAutoType} is enabled,
 * JSON containing {@code "@type":"com.example.MyClass"} will attempt to deserialize
 * as that class. This filter controls which class names are accepted.</p>
 *
 * <pre>
 * AutoTypeFilter filter = AutoTypeFilter.acceptNames("com.myapp.");
 * ObjectMapper mapper = ObjectMapper.builder()
 *     .enableRead(ReadFeature.SupportAutoType)
 *     .autoTypeFilter(filter)
 *     .build();
 * </pre>
 */
@FunctionalInterface
public interface AutoTypeFilter extends Filter {
    /**
     * Check if the given type name is accepted for AutoType deserialization.
     *
     * @param typeName    the fully qualified class name from {@code @type}
     * @param expectClass the expected class (field declared type), or null
     * @return the resolved Class if accepted, or null to deny
     */
    Class<?> apply(String typeName, Class<?> expectClass);

    /**
     * Create a filter that accepts type names matching any of the given prefixes.
     */
    static AutoTypeFilter acceptNames(String... prefixes) {
        return (typeName, expectClass) -> {
            for (String prefix : prefixes) {
                if (typeName.startsWith(prefix)) {
                    try {
                        return Class.forName(typeName);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                }
            }
            return null;
        };
    }

    /**
     * Create a filter that accepts any of the given classes and their subtypes.
     */
    static AutoTypeFilter acceptClasses(Class<?>... classes) {
        return (typeName, expectClass) -> {
            try {
                Class<?> cls = Class.forName(typeName);
                for (Class<?> accepted : classes) {
                    if (accepted.isAssignableFrom(cls)) {
                        return cls;
                    }
                }
            } catch (ClassNotFoundException e) {
                // not found = deny
            }
            return null;
        };
    }
}

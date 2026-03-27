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
     * Create a filter that accepts type names matching any of the given package prefixes.
     *
     * <p>Prefixes should end with {@code "."} to match a specific package.
     * For example, {@code "com.myapp."} matches {@code "com.myapp.User"} but
     * NOT {@code "com.myappEvil.Payload"} (package boundary enforced).</p>
     *
     * <p><strong>Security note:</strong> Prefer {@link #acceptClasses(Class[])} for
     * tighter control. This method allows any class within the matched package.</p>
     */
    static AutoTypeFilter acceptNames(String... prefixes) {
        return (typeName, expectClass) -> {
            if (typeName == null || typeName.isEmpty()) {
                return null;
            }
            for (String prefix : prefixes) {
                if (typeName.startsWith(prefix)) {
                    // Package boundary check: if prefix ends with '.', the match is exact.
                    // If prefix does NOT end with '.', the next char must be '.' or end-of-string,
                    // preventing "com.example" from matching "com.exampleEvil.Payload".
                    if (!prefix.endsWith(".") && typeName.length() > prefix.length()
                            && typeName.charAt(prefix.length()) != '.') {
                        continue; // not an exact package boundary match
                    }
                    try {
                        // initialize=false: prevent static initializer execution for untrusted types
                        return Class.forName(typeName, false, Thread.currentThread().getContextClassLoader());
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
     *
     * <p><strong>Security note:</strong> Uses {@code Class.forName(name, false, classLoader)}
     * to prevent static initializer execution on untrusted class names.</p>
     */
    static AutoTypeFilter acceptClasses(Class<?>... classes) {
        return (typeName, expectClass) -> {
            try {
                // initialize=false: prevent static initializer execution for untrusted types
                Class<?> cls = Class.forName(typeName, false, Thread.currentThread().getContextClassLoader());
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

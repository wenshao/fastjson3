package com.alibaba.fastjson3;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Type token for capturing generic type information at runtime.
 * Provides a more concise alternative to {@link TypeReference} for common generic types.
 *
 * <h3>Comparison with TypeReference:</h3>
 * <ul>
 *   <li><b>TypeToken</b>: Uses static factory methods, cleaner syntax</li>
 *   <li><b>TypeReference</b>: Uses anonymous subclass, supports arbitrary complex types</li>
 *   <li>For simple generics, prefer TypeToken</li>
 *   <li>For complex nested types, combine TypeReference with TypeToken.of()</li>
 * </ul>
 *
 * <h3>Usage examples:</h3>
 * <pre>
 * // Simple type
 * TypeToken&lt;User&gt; userType = TypeToken.of(User.class);
 * User user = JSON.parse(json, userType);
 *
 * // Collection types
 * TypeToken&lt;List&lt;User&gt;&gt; listType = TypeToken.listOf(User.class);
 * List&lt;User&gt; users = JSON.parse(json, listType);
 *
 * TypeToken&lt;Set&lt;User&gt;&gt; setType = TypeToken.setOf(User.class);
 * Set&lt;User&gt; users = JSON.parse(json, setType);
 *
 * // Map types (JSON object keys are always Strings)
 * TypeToken&lt;Map&lt;String, User&gt;&gt; mapType = TypeToken.mapOf(User.class);
 * Map&lt;String, User&gt; userMap = JSON.parse(json, mapType);
 *
 * // Complex nested types - combine with TypeReference
 * TypeToken&lt;Map&lt;String, List&lt;User&gt;&gt;&gt; complexType =
 *     TypeToken.of(new TypeReference&lt;Map&lt;String, List&lt;User&gt;&gt;&gt;() {});
 * Map&lt;String, List&lt;User&gt;&gt; data = JSON.parse(json, complexType);
 *
 * // Array types
 * TypeToken&lt;User[]&gt; arrayType = TypeToken.arrayOf(User.class);
 * User[] users = JSON.parse(json, arrayType);
 * </pre>
 *
 * <h3>Thread safety:</h3>
 * TypeToken is immutable and thread-safe. Instances can be safely shared across threads.
 * Consider caching frequently used TypeToken instances as static final constants.
 *
 * @param <T> the referenced type
 * @see TypeReference
 * @see JSON#parse(String, Class)
 * @see JSON#parse(String, TypeToken)
 */
public final class TypeToken<T> {
    private final Type type;

    private TypeToken(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.type = type;
    }

    /**
     * Returns the underlying {@link Type} represented by this token.
     *
     * @return the Type object
     */
    public Type type() {
        return type;
    }

    /**
     * Returns the type name for debugging purposes.
     *
     * @return the type name
     */
    public String typeName() {
        return type.getTypeName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TypeToken<?>)) {
            return false;
        }
        TypeToken<?> other = (TypeToken<?>) obj;
        return type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return "TypeToken<" + type.getTypeName() + ">";
    }

    // ==================== Static factory methods ====================

    /**
     * Creates a TypeToken for a simple class type.
     *
     * <pre>
     * TypeToken&lt;User&gt; token = TypeToken.of(User.class);
     * </pre>
     *
     * @param type the class type
     * @param <T>   the type parameter
     * @return a TypeToken for the given class
     */
    public static <T> TypeToken<T> of(Class<T> type) {
        return new TypeToken<>(type);
    }

    /**
     * Creates a TypeToken from any {@link Type}.
     * Use this for custom types or when you have a Type object from reflection.
     *
     * <pre>
     * Type type = new TypeReference&lt;List&lt;User&gt;&gt;() {}.getType();
     * TypeToken&lt;List&lt;User&gt;&gt; token = TypeToken.of(type);
     * </pre>
     *
     * @param type the type
     * @param <T>   the type parameter
     * @return a TypeToken for the given type
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeToken<T> of(Type type) {
        return new TypeToken<>(type);
    }

    /**
     * Creates a TypeToken from a {@link TypeReference}.
     * This allows combining TypeReference's flexibility with TypeToken's API.
     *
     * <pre>
     * TypeToken&lt;Map&lt;String, List&lt;User&gt;&gt;&gt; token =
     *     TypeToken.of(new TypeReference&lt;Map&lt;String, List&lt;User&gt;&gt;&gt;() {});
     * </pre>
     *
     * @param ref the type reference
     * @param <T>  the type parameter
     * @return a TypeToken for the type represented by the reference
     */
    public static <T> TypeToken<T> of(TypeReference<T> ref) {
        return new TypeToken<>(ref.getType());
    }

    /**
     * Creates a TypeToken for {@code List<T>}.
     *
     * <pre>
     * TypeToken&lt;List&lt;User&gt;&gt; token = TypeToken.listOf(User.class);
     * </pre>
     *
     * @param elementType the list element type
     * @param <E>          the element type parameter
     * @return a TypeToken for List&lt;E&gt;
     */
    public static <E> TypeToken<List<E>> listOf(Class<E> elementType) {
        TypeToken<?> token = new TypeToken<>(new ParameterizedTypeImpl(List.class, elementType));
        @SuppressWarnings("unchecked")
        TypeToken<List<E>> result = (TypeToken<List<E>>) token;
        return result;
    }

    /**
     * Creates a TypeToken for {@code Set<T>}.
     *
     * <pre>
     * TypeToken&lt;Set&lt;User&gt;&gt; token = TypeToken.setOf(User.class);
     * </pre>
     *
     * @param elementType the set element type
     * @param <E>          the element type parameter
     * @return a TypeToken for Set&lt;E&gt;
     */
    public static <E> TypeToken<Set<E>> setOf(Class<E> elementType) {
        TypeToken<?> token = new TypeToken<>(new ParameterizedTypeImpl(Set.class, elementType));
        @SuppressWarnings("unchecked")
        TypeToken<Set<E>> result = (TypeToken<Set<E>>) token;
        return result;
    }

    /**
     * Creates a TypeToken for {@code Map<String, V>}.
     * <p>Note: JSON object keys are always strings, so the key type is fixed to String.
     *
     * <pre>
     * TypeToken&lt;Map&lt;String, User&gt;&gt; token = TypeToken.mapOf(User.class);
     * </pre>
     *
     * @param valueType the map value type
     * @param <V>        the value type parameter
     * @return a TypeToken for Map&lt;String, V&gt;
     */
    public static <V> TypeToken<Map<String, V>> mapOf(Class<V> valueType) {
        TypeToken<?> token = new TypeToken<>(
                new ParameterizedTypeImpl(Map.class, String.class, valueType));
        @SuppressWarnings("unchecked")
        TypeToken<Map<String, V>> result = (TypeToken<Map<String, V>>) token;
        return result;
    }

    /**
     * Creates a TypeToken for {@code T[]} (Java array).
     *
     * <pre>
     * TypeToken&lt;User[]&gt; token = TypeToken.arrayOf(User.class);
     * </pre>
     *
     * @param elementType the array element type
     * @param <E>          the element type parameter
     * @return a TypeToken for E[]
     */
    public static <E> TypeToken<E[]> arrayOf(Class<E> elementType) {
        // Create a zero-length array to get the generic array type
        Object array = Array.newInstance(elementType, 0);
        TypeToken<?> token = new TypeToken<>(array.getClass());
        @SuppressWarnings("unchecked")
        TypeToken<E[]> result = (TypeToken<E[]>) token;
        return result;
    }

    // ==================== Internal implementation ====================

    /**
     * Internal implementation of ParameterizedType for generic types.
     */
    private static final class ParameterizedTypeImpl implements ParameterizedType {
        private final Class<?> rawType;
        private final Type[] typeArguments;

        ParameterizedTypeImpl(Class<?> rawType, Class<?>... typeArguments) {
            this.rawType = rawType;
            this.typeArguments = typeArguments.clone();
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ParameterizedType)) {
                return false;
            }
            ParameterizedType other = (ParameterizedType) obj;
            return rawType.equals(other.getRawType())
                    && java.util.Arrays.equals(typeArguments, other.getActualTypeArguments());
        }

        @Override
        public int hashCode() {
            int result = rawType.hashCode();
            result = 31 * result + java.util.Arrays.hashCode(typeArguments);
            return result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(rawType.getSimpleName());
            if (typeArguments.length > 0) {
                sb.append("<");
                for (int i = 0; i < typeArguments.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    Type arg = typeArguments[i];
                    if (arg instanceof Class) {
                        sb.append(((Class<?>) arg).getSimpleName());
                    } else {
                        sb.append(arg.getTypeName());
                    }
                }
                sb.append(">");
            }
            return sb.toString();
        }
    }
}

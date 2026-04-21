package com.alibaba.fastjson3.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

/**
 * Type utilities for reflective generic-type resolution during ObjectReader construction.
 * Substitutes {@link TypeVariable}s against a concrete context class, normalizes
 * {@link WildcardType} to its upper bound, and erases any {@link Type} to its raw class.
 */
public final class TypeUtils {
    private TypeUtils() {
    }

    /**
     * Erase a generic {@link Type} to its raw {@link Class}.
     * Returns {@code Object.class} for unresolved {@link TypeVariable}s and
     * unbounded {@link WildcardType}s.
     */
    public static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?> cls) {
            return cls;
        }
        if (type instanceof ParameterizedType pt) {
            return getRawClass(pt.getRawType());
        }
        if (type instanceof GenericArrayType gat) {
            Class<?> comp = getRawClass(gat.getGenericComponentType());
            return java.lang.reflect.Array.newInstance(comp, 0).getClass();
        }
        if (type instanceof TypeVariable<?> tv) {
            Type[] bounds = tv.getBounds();
            return bounds.length > 0 ? getRawClass(bounds[0]) : Object.class;
        }
        if (type instanceof WildcardType wt) {
            Type[] upper = wt.getUpperBounds();
            return upper.length > 0 ? getRawClass(upper[0]) : Object.class;
        }
        return Object.class;
    }

    /**
     * Resolve a reflected {@code Type} against a concrete context class.
     * <p>Substitutes any {@link TypeVariable} whose declaring site is visible
     * through {@code contextClass}'s generic supertype chain with the actual type
     * argument. Normalizes {@link WildcardType} to its upper bound. Recurses into
     * {@link ParameterizedType} and {@link GenericArrayType}.
     *
     * <p>Example: given {@code class Parent<T> { T value; }} and
     * {@code class Child extends Parent<Bean> {}}, resolving
     * {@code Parent.getDeclaredField("value").getGenericType()} against
     * {@code Child.class} returns {@code Bean.class}.
     */
    public static Type resolve(Type type, Class<?> contextClass) {
        if (type == null || contextClass == null) {
            return type;
        }
        return resolve(type, buildBindings(contextClass));
    }

    /**
     * Resolve {@code type} against a context type that may be either a {@link Class}
     * or a {@link ParameterizedType}. For a {@code ParameterizedType} like
     * {@code Parent<Bean>}, the raw class's type parameters are bound to the actual
     * type arguments, on top of the usual superclass-chain bindings. This supports
     * {@code TypeReference<Parent<Bean>>}-style top-level reads of parameterized POJOs.
     */
    public static Type resolve(Type type, Type contextType) {
        if (type == null || contextType == null) {
            return type;
        }
        Class<?> contextClass = getRawClass(contextType);
        Map<TypeVariable<?>, Type> bindings = buildBindings(contextClass);
        if (contextType instanceof ParameterizedType pt) {
            addParameterizedBindings(pt, bindings);
        }
        return resolve(type, bindings);
    }

    /**
     * Produce the {@code TypeVariable → actualTypeArgument} bindings carried by a
     * {@link ParameterizedType}. Used by the ObjectReader creator to stamp generic
     * parameters onto a reader built from the raw class.
     */
    public static Map<TypeVariable<?>, Type> bindingsFromParameterizedType(ParameterizedType pt) {
        Map<TypeVariable<?>, Type> bindings = new HashMap<>();
        addParameterizedBindings(pt, bindings);
        return bindings;
    }

    private static void addParameterizedBindings(ParameterizedType pt, Map<TypeVariable<?>, Type> bindings) {
        if (!(pt.getRawType() instanceof Class<?> raw)) {
            return;
        }
        TypeVariable<?>[] params = raw.getTypeParameters();
        Type[] args = pt.getActualTypeArguments();
        int n = Math.min(params.length, args.length);
        for (int i = 0; i < n; i++) {
            bindings.put(params[i], args[i]);
        }
    }

    private static Type resolve(Type type, Map<TypeVariable<?>, Type> bindings) {
        return resolve(type, bindings, null);
    }

    /**
     * Inner recursion carries a {@code visited} set to detect F-bounded
     * TypeVariable cycles. Without it, a factory / field typed
     * {@code <T extends Comparable<T>>} or {@code <E extends Enum<E>>}
     * triggers infinite recursion: resolve(T) → bound = Comparable&lt;T&gt; →
     * recurse into Comparable's type args → back to T → StackOverflowError.
     * When a TypeVariable is already on the resolution stack, fall back to
     * its raw bound's erasure to break the cycle.
     */
    private static Type resolve(Type type, Map<TypeVariable<?>, Type> bindings,
                                 java.util.Set<TypeVariable<?>> visited) {
        if (type instanceof Class<?>) {
            return type;
        }
        if (type instanceof TypeVariable<?> tv) {
            if (visited != null && visited.contains(tv)) {
                // Cycle: erase to the first bound's raw class. Comparable<T> → Comparable.
                Type[] bounds = tv.getBounds();
                return bounds.length > 0 ? getRawClass(bounds[0]) : Object.class;
            }
            Type bound = bindings.get(tv);
            if (bound == null || bound.equals(tv)) {
                Type[] bounds = tv.getBounds();
                if (bounds.length == 0) {
                    return Object.class;
                }
                java.util.Set<TypeVariable<?>> newVisited = visited == null
                        ? new java.util.HashSet<>() : visited;
                newVisited.add(tv);
                try {
                    return resolve(bounds[0], bindings, newVisited);
                } finally {
                    newVisited.remove(tv);
                }
            }
            // Recurse in case the bound is itself a TypeVariable or parameterized.
            java.util.Set<TypeVariable<?>> newVisited = visited == null
                    ? new java.util.HashSet<>() : visited;
            newVisited.add(tv);
            try {
                return resolve(bound, bindings, newVisited);
            } finally {
                newVisited.remove(tv);
            }
        }
        if (type instanceof WildcardType wt) {
            Type[] upper = wt.getUpperBounds();
            return upper.length > 0 ? resolve(upper[0], bindings, visited) : Object.class;
        }
        if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            Type[] resolved = new Type[args.length];
            boolean changed = false;
            for (int i = 0; i < args.length; i++) {
                resolved[i] = resolve(args[i], bindings, visited);
                if (resolved[i] != args[i]) {
                    changed = true;
                }
            }
            if (!changed) {
                return pt;
            }
            return new ResolvedParameterizedType(pt.getRawType(), resolved, pt.getOwnerType());
        }
        if (type instanceof GenericArrayType gat) {
            Type comp = resolve(gat.getGenericComponentType(), bindings, visited);
            if (comp instanceof Class<?> c) {
                return java.lang.reflect.Array.newInstance(c, 0).getClass();
            }
            return new ResolvedGenericArrayType(comp);
        }
        return type;
    }

    /**
     * Build {@code TypeVariable → Type} bindings by walking {@code cls}'s
     * generic superclass and interface chain. A {@code TypeVariable} declared
     * on any ancestor {@code A<T>} is mapped to whatever concrete type
     * {@code cls} (or an intermediate subclass) supplies for {@code T}.
     */
    public static Map<TypeVariable<?>, Type> buildBindings(Class<?> cls) {
        Map<TypeVariable<?>, Type> bindings = new HashMap<>();
        collectBindings(cls, bindings);
        // Resolve chains: if T → U and U → String, collapse to T → String.
        for (Map.Entry<TypeVariable<?>, Type> e : bindings.entrySet()) {
            Type current = e.getValue();
            while (current instanceof TypeVariable<?> tv && bindings.containsKey(tv)
                    && !bindings.get(tv).equals(tv)) {
                Type next = bindings.get(tv);
                if (next == current) {
                    break;
                }
                current = next;
            }
            e.setValue(current);
        }
        return bindings;
    }

    private static void collectBindings(Class<?> cls, Map<TypeVariable<?>, Type> bindings) {
        if (cls == null || cls == Object.class) {
            return;
        }
        Type superType = cls.getGenericSuperclass();
        Class<?> superRaw = cls.getSuperclass();
        if (superType instanceof ParameterizedType pt && superRaw != null) {
            bindParameters(superRaw.getTypeParameters(), pt.getActualTypeArguments(), bindings);
        }
        collectBindings(superRaw, bindings);
        Type[] genericInterfaces = cls.getGenericInterfaces();
        Class<?>[] interfaces = cls.getInterfaces();
        for (int i = 0; i < genericInterfaces.length; i++) {
            Type it = genericInterfaces[i];
            if (it instanceof ParameterizedType pt) {
                bindParameters(interfaces[i].getTypeParameters(), pt.getActualTypeArguments(), bindings);
            }
            collectBindings(interfaces[i], bindings);
        }
    }

    private static void bindParameters(TypeVariable<?>[] params, Type[] args, Map<TypeVariable<?>, Type> bindings) {
        int n = Math.min(params.length, args.length);
        for (int i = 0; i < n; i++) {
            bindings.putIfAbsent(params[i], args[i]);
        }
    }

    /**
     * Internal {@link ParameterizedType} implementation used when
     * {@link #resolve(Type, Class)} substitutes at least one type argument.
     */
    private record ResolvedParameterizedType(Type rawType, Type[] actualTypeArguments, Type ownerType)
            implements ParameterizedType {
        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(rawType.getTypeName()).append('<');
            for (int i = 0; i < actualTypeArguments.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(actualTypeArguments[i].getTypeName());
            }
            sb.append('>');
            return sb.toString();
        }
    }

    private record ResolvedGenericArrayType(Type genericComponentType) implements GenericArrayType {
        @Override
        public Type getGenericComponentType() {
            return genericComponentType;
        }

        @Override
        public String toString() {
            return genericComponentType.getTypeName() + "[]";
        }
    }
}

package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;
import com.alibaba.fastjson3.WriteFeature;
import com.alibaba.fastjson3.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Creates {@link ObjectWriter} instances for arbitrary POJO classes via reflection.
 * Inspects getter methods, public fields, and annotations ({@link JSONField}, {@link JSONType})
 * to build a sorted list of {@link FieldWriter}s that drive serialization.
 */
public final class ObjectWriterCreator {
    private ObjectWriterCreator() {
    }

    /**
     * Create an ObjectWriter for the given type by introspecting its getters, fields, and annotations.
     *
     * @param type the class to create a writer for
     * @return a new ObjectWriter that serializes instances of the given type
     */
    public static <T> ObjectWriter<T> createObjectWriter(Class<T> type) {
        return createObjectWriter(type, null, false);
    }

    public static <T> ObjectWriter<T> createObjectWriter(Class<T> type, Class<?> mixIn) {
        return createObjectWriter(type, mixIn, false);
    }

    public static <T> ObjectWriter<T> createObjectWriter(Class<T> type, Class<?> mixIn, boolean useJacksonAnnotation) {
        return createObjectWriter(type, mixIn, useJacksonAnnotation, 0);
    }

    public static <T> ObjectWriter<T> createObjectWriter(Class<T> type, Class<?> mixIn,
                                                          boolean useJacksonAnnotation, long writeFeatures) {
        return createObjectWriter(type, mixIn, useJacksonAnnotation, writeFeatures, null);
    }

    /**
     * @param ancestorMixIns the full mix-in map from the owning ObjectMapper, used
     *     to resolve an ancestor's effective {@code @JSONType} annotation when the
     *     annotation lives on a mix-in rather than the real class. {@code null} is
     *     equivalent to an empty map — ancestor-mix-in discovery is then skipped.
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectWriter<T> createObjectWriter(Class<T> type, Class<?> mixIn,
                                                          boolean useJacksonAnnotation, long writeFeatures,
                                                          java.util.Map<Class<?>, Class<?>> ancestorMixIns) {
        // Check @JSONType(serializer=...) for class-level custom writer
        JSONType jsonType = type.getAnnotation(JSONType.class);
        if (jsonType != null && jsonType.serializer() != Void.class) {
            Class<?> serializerClass = jsonType.serializer();
            if (!com.alibaba.fastjson3.ObjectWriter.class.isAssignableFrom(serializerClass)) {
                throw new com.alibaba.fastjson3.JSONException(
                        "@JSONType(serializer=" + serializerClass.getName()
                                + ") does not implement ObjectWriter");
            }
            try {
                return (ObjectWriter<T>) serializerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new com.alibaba.fastjson3.JSONException(
                        "cannot instantiate serializer: " + serializerClass.getName(), e);
            }
        }

        if (com.alibaba.fastjson3.util.JDKUtils.isRecord(type)) {
            // Note: writeFeatures (IgnoreNoneSerializable etc.) not yet applied to Record fields.
            // Record fields are always final components — Serializable check is less relevant.
            return createRecordWriter(type, mixIn, useJacksonAnnotation, ancestorMixIns);
        }

        return createPojoWriter(type, mixIn, useJacksonAnnotation, writeFeatures, ancestorMixIns);
    }

    private static <T> ObjectWriter<T> createRecordWriter(Class<T> type, Class<?> mixIn, boolean useJacksonAnnotation,
                                                           java.util.Map<Class<?>, Class<?>> ancestorMixIns) {
        JSONType jsonType = type.getAnnotation(JSONType.class);
        NamingStrategy naming = jsonType != null ? jsonType.naming() : NamingStrategy.NoneStrategy;
        Set<String> includes = jsonType != null && jsonType.includes().length > 0 ? Set.of(jsonType.includes()) : Set.of();
        Set<String> ignores = jsonType != null && jsonType.ignores().length > 0 ? Set.of(jsonType.ignores()) : Set.of();
        boolean alphabetic = jsonType == null || jsonType.alphabetic();
        Inclusion typeInclusion = jsonType != null ? jsonType.inclusion() : Inclusion.DEFAULT;

        // Jackson class-level annotations as fallback
        if (useJacksonAnnotation) {
            JacksonAnnotationSupport.BeanInfo jackson = JacksonAnnotationSupport.getBeanInfo(type);
            if (jackson != null) {
                if (naming == NamingStrategy.NoneStrategy && jackson.naming() != null) {
                    naming = jackson.naming();
                }
                if (ignores.isEmpty() && jackson.ignoreProperties() != null) {
                    ignores = Set.of(jackson.ignoreProperties());
                }
                if (jsonType == null && jackson.alphabetic() != null) {
                    alphabetic = jackson.alphabetic();
                }
                if (typeInclusion == Inclusion.DEFAULT && jackson.inclusion() != null) {
                    typeInclusion = jackson.inclusion();
                }
            }
        }

        String[] componentNames = com.alibaba.fastjson3.util.JDKUtils.getRecordComponentNames(type);
        java.lang.reflect.Method[] accessors = com.alibaba.fastjson3.util.JDKUtils.getRecordComponentAccessors(type);

        // Records are field bags just like POJOs on the write side — the
        // anyGetter lookup that createPojoWriter does must also run here or
        // a record accessor annotated @JsonAnyGetter becomes a silent no-op.
        Method anyGetterMethod = findAnyGetterMethod(type, mixIn, useJacksonAnnotation);

        Map<String, FieldWriter> writerMap = new LinkedHashMap<>();
        for (int i = 0; i < componentNames.length; i++) {
            String propertyName = componentNames[i];
            java.lang.reflect.Method accessor = accessors[i];

            if (!includes.isEmpty() && !includes.contains(propertyName)) {
                continue;
            }
            if (ignores.contains(propertyName)) {
                continue;
            }

            // Skip the accessor adopted as anyGetter — its Map is emitted
            // inline by the anyGetter wrapper. Without this skip, the record
            // writer emits both a nested {"extras":{…}} slot AND the flattened
            // inline keys, duplicating the payload (same shape PR #124 fixed
            // for createPojoWriter).
            if (anyGetterMethod != null && accessor.equals(anyGetterMethod)) {
                continue;
            }

            JSONField jsonField = accessor.getAnnotation(JSONField.class);
            // Check mixin for annotation
            if (jsonField == null && mixIn != null) {
                jsonField = findMixInAnnotation(mixIn, accessor, propertyName, JSONField.class);
            }
            // Also check field-level annotation
            if (jsonField == null) {
                try {
                    java.lang.reflect.Field f = type.getDeclaredField(propertyName);
                    jsonField = f.getAnnotation(JSONField.class);
                } catch (NoSuchFieldException ignored) {
                }
            }
            // Check mixin field annotation
            if (jsonField == null && mixIn != null) {
                jsonField = findMixInFieldAnnotation(mixIn, propertyName, JSONField.class);
            }
            if (jsonField != null && !jsonField.serialize()) {
                continue;
            }

            // Jackson field annotations as fallback
            JacksonAnnotationSupport.FieldInfo jacksonField = null;
            if (jsonField == null && useJacksonAnnotation) {
                jacksonField = JacksonAnnotationSupport.getFieldInfo(accessor.getAnnotations());
                if (jacksonField == null) {
                    try {
                        java.lang.reflect.Field f = type.getDeclaredField(propertyName);
                        jacksonField = JacksonAnnotationSupport.getFieldInfo(f.getAnnotations());
                    } catch (NoSuchFieldException ignored) {
                    }
                }
                if (jacksonField != null && jacksonField.noSerialize()) {
                    continue;
                }
            }

            String jsonName;
            int ordinal = 0;
            if (jsonField != null && !jsonField.name().isEmpty()) {
                jsonName = jsonField.name();
            } else if (jacksonField != null && !jacksonField.name().isEmpty()) {
                jsonName = jacksonField.name();
            } else {
                jsonName = applyNamingStrategy(propertyName, naming);
            }
            if (jsonField != null) {
                ordinal = jsonField.ordinal();
            } else if (jacksonField != null) {
                ordinal = jacksonField.ordinal();
            }

            // Resolve inclusion: field-level overrides type-level
            Inclusion fieldInclusion;
            if (jsonField != null && jsonField.inclusion() != Inclusion.DEFAULT) {
                fieldInclusion = jsonField.inclusion();
            } else if (jacksonField != null && jacksonField.inclusion() != null) {
                fieldInclusion = jacksonField.inclusion();
            } else {
                fieldInclusion = typeInclusion;
            }

            // Prefer backing field for Unsafe direct access
            java.lang.reflect.Field backingField = null;
            try {
                backingField = type.getDeclaredField(propertyName);
                backingField.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }

            if (backingField != null) {
                writerMap.put(propertyName, createFieldWriterForField(
                        jsonName, ordinal, accessor.getGenericReturnType(), accessor.getReturnType(), backingField, fieldInclusion
                ));
            } else {
                accessor.setAccessible(true);
                writerMap.put(propertyName, createFieldWriterForGetter(
                        jsonName, ordinal, accessor.getGenericReturnType(), accessor.getReturnType(), accessor, fieldInclusion
                ));
            }
        }

        List<FieldWriter> fieldWriters = new ArrayList<>(writerMap.values());
        if (alphabetic) {
            Collections.sort(fieldWriters);
        }
        FieldWriter[] writers = fieldWriters.toArray(new FieldWriter[0]);

        String typeName = (jsonType != null && !jsonType.typeName().isEmpty()) ? jsonType.typeName() : null;
        String typeKey = (jsonType != null && !jsonType.typeKey().isEmpty()) ? jsonType.typeKey() : null;
        if (typeKey == null && useJacksonAnnotation) {
            JacksonAnnotationSupport.BeanInfo jacksonBean = findJacksonBeanInfoWithTypeKey(type);
            if (jacksonBean != null) {
                typeKey = jacksonBean.typeKey();
                if (typeName == null) {
                    typeName = resolveJacksonTypeName(type, jacksonBean);
                }
            }
        }
        if (typeName == null || typeKey == null) {
            JSONType ancestor = findSeeAlsoAncestor(type, ancestorMixIns);
            if (ancestor != null) {
                if (typeKey == null) {
                    typeKey = !ancestor.typeKey().isEmpty() ? ancestor.typeKey() : "@type";
                }
                if (typeName == null) {
                    typeName = type.getSimpleName();
                }
            }
        }
        return buildObjectWriter(writers, anyGetterMethod, typeName, typeKey);
    }

    private static <T> ObjectWriter<T> createPojoWriter(Class<T> type, Class<?> mixIn, boolean useJacksonAnnotation) {
        return createPojoWriter(type, mixIn, useJacksonAnnotation, 0, null);
    }

    private static <T> ObjectWriter<T> createPojoWriter(Class<T> type, Class<?> mixIn,
                                                         boolean useJacksonAnnotation, long writeFeatures,
                                                         java.util.Map<Class<?>, Class<?>> ancestorMixIns) {
        JSONType jsonType = type.getAnnotation(JSONType.class);
        NamingStrategy naming = jsonType != null ? jsonType.naming() : NamingStrategy.NoneStrategy;
        Set<String> includes = jsonType != null && jsonType.includes().length > 0 ? Set.of(jsonType.includes()) : Set.of();
        Set<String> ignores = jsonType != null && jsonType.ignores().length > 0 ? Set.of(jsonType.ignores()) : Set.of();
        String[] orders = jsonType != null ? jsonType.orders() : new String[0];
        boolean alphabetic = jsonType == null || jsonType.alphabetic();
        Inclusion typeInclusion = jsonType != null ? jsonType.inclusion() : Inclusion.DEFAULT;

        // Jackson class-level annotations as fallback (only when @JSONType not present or doesn't specify)
        if (useJacksonAnnotation) {
            JacksonAnnotationSupport.BeanInfo jackson = JacksonAnnotationSupport.getBeanInfo(type);
            if (jackson != null) {
                if (naming == NamingStrategy.NoneStrategy && jackson.naming() != null) {
                    naming = jackson.naming();
                }
                if (ignores.isEmpty() && jackson.ignoreProperties() != null) {
                    ignores = Set.of(jackson.ignoreProperties());
                }
                if (orders.length == 0 && jackson.propertyOrder() != null) {
                    orders = jackson.propertyOrder();
                }
                if (jsonType == null && jackson.alphabetic() != null) {
                    alphabetic = jackson.alphabetic();
                }
                if (typeInclusion == Inclusion.DEFAULT && jackson.inclusion() != null) {
                    typeInclusion = jackson.inclusion();
                }
            }
        }

        // Collect field writers keyed by property name to deduplicate getter vs field
        Map<String, FieldWriter> writerMap = new LinkedHashMap<>();
        // Property names suppressed by the getter loop (e.g., anyGetter methods)
        // so the public-field fallback below doesn't pick them up — a public
        // `Map<String,Object> extras` paired with `@JsonAnyGetter getExtras()`
        // would otherwise produce both a nested "extras":{...} slot AND the
        // flattened inline keys.
        Set<String> suppressedNames = new java.util.HashSet<>();

        // 0. Check for @JSONField(value = true) — single-value serialization
        ObjectWriter<T> valueWriter = findValueWriter(type, mixIn, useJacksonAnnotation);
        if (valueWriter != null) {
            return valueWriter;
        }

        // 1. Inspect getter methods
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            if (method.getParameterCount() != 0) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            String methodName = method.getName();
            String propertyName = extractPropertyName(methodName, method.getReturnType());
            if (propertyName == null) {
                continue;
            }

            JSONField jsonField = method.getAnnotation(JSONField.class);
            // Check mixin for getter annotation
            if (jsonField == null && mixIn != null) {
                jsonField = findMixInAnnotation(mixIn, method, propertyName, JSONField.class);
            }
            if (jsonField != null && !jsonField.serialize()) {
                continue;
            }
            // Skip anyGetter methods — they're handled separately by
            // findAnyGetterMethod / the anyGetter-aware writer wrapper. Without
            // this branch, a getter like `@JsonAnyGetter Map<String,Object>
            // getExtras()` would produce both a nested `"extras":{…}` slot AND
            // flattened inline keys, duplicating the payload.
            //
            // Map-return gate: findAnyGetterMethod requires Map-typed return
            // to adopt the getter as an anyGetter. A non-Map @anyGetter is a
            // user error; suppressing it here would silently drop the field.
            // Fall through to the normal getter path in that case — the
            // annotation becomes a no-op, and the value keeps serialising.
            // jsonField may already be the mix-in annotation here (line above
            // promotes it from mixIn when the direct method annotation is null),
            // so the anyGetter check covers both cases in one branch.
            if (jsonField != null && jsonField.anyGetter()
                    && java.util.Map.class.isAssignableFrom(method.getReturnType())) {
                suppressedNames.add(propertyName);
                continue;
            }
            if (useJacksonAnnotation
                    && (isJacksonAnyGetter(method)
                            || (mixIn != null && findMixInJacksonAnyGetter(mixIn, method)))
                    && java.util.Map.class.isAssignableFrom(method.getReturnType())) {
                suppressedNames.add(propertyName);
                continue;
            }

            // Jackson field annotations as fallback (only when @JSONField not found)
            JacksonAnnotationSupport.FieldInfo jacksonField = null;
            if (jsonField == null && useJacksonAnnotation) {
                jacksonField = JacksonAnnotationSupport.getFieldInfo(method.getAnnotations());
                if (jacksonField != null && jacksonField.noSerialize()) {
                    continue;
                }
            }

            String jsonName;
            int ordinal = 0;

            if (jsonField != null && !jsonField.name().isEmpty()) {
                jsonName = jsonField.name();
            } else if (jacksonField != null && !jacksonField.name().isEmpty()) {
                jsonName = jacksonField.name();
            } else {
                jsonName = applyNamingStrategy(propertyName, naming);
            }

            if (jsonField != null) {
                ordinal = jsonField.ordinal();
            } else if (jacksonField != null) {
                ordinal = jacksonField.ordinal();
            }

            // Check field-level annotation as well (for the corresponding field)
            if (jsonField == null && jacksonField == null) {
                try {
                    Field f = findDeclaredField(type, propertyName);
                    if (f != null) {
                        JSONField fieldAnnotation = f.getAnnotation(JSONField.class);
                        // Also check mixin field
                        if (fieldAnnotation == null && mixIn != null) {
                            fieldAnnotation = findMixInFieldAnnotation(mixIn, propertyName, JSONField.class);
                        }
                        if (fieldAnnotation != null) {
                            if (!fieldAnnotation.serialize()) {
                                continue;
                            }
                            if (!fieldAnnotation.name().isEmpty()) {
                                jsonName = fieldAnnotation.name();
                            }
                            ordinal = fieldAnnotation.ordinal();
                        } else if (useJacksonAnnotation) {
                            // Check Jackson annotations on backing field
                            jacksonField = JacksonAnnotationSupport.getFieldInfo(f.getAnnotations());
                            if (jacksonField != null) {
                                if (jacksonField.noSerialize()) {
                                    continue;
                                }
                                if (!jacksonField.name().isEmpty()) {
                                    jsonName = jacksonField.name();
                                }
                                ordinal = jacksonField.ordinal();
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // field lookup failure is not fatal
                }
            }

            // Lookup backing field once — reused for inclusion resolution and Unsafe offset
            Field backingField = findDeclaredField(type, propertyName);

            // IgnoreNonFieldGetter: skip getters without a backing field
            if (backingField == null && (writeFeatures & WriteFeature.IgnoreNonFieldGetter.mask) != 0
                    && jsonField == null) { // explicit @JSONField overrides
                continue;
            }

            // Resolve inclusion: field-level overrides type-level
            Inclusion fieldInclusion = typeInclusion;
            if (jsonField != null && jsonField.inclusion() != Inclusion.DEFAULT) {
                fieldInclusion = jsonField.inclusion();
            } else if (jacksonField != null && jacksonField.inclusion() != null) {
                fieldInclusion = jacksonField.inclusion();
            }
            if (backingField != null) {
                JSONField fAnn = backingField.getAnnotation(JSONField.class);
                if (fAnn == null && mixIn != null) {
                    fAnn = findMixInFieldAnnotation(mixIn, propertyName, JSONField.class);
                }
                if (fAnn != null && fAnn.inclusion() != Inclusion.DEFAULT) {
                    fieldInclusion = fAnn.inclusion();
                }
            }

            // Resolve format, custom writer, label, field features, and unwrapped
            String format = resolveFormat(jsonField, jacksonField, backingField, mixIn, useJacksonAnnotation);
            ObjectWriter<?> customWriter = resolveSerializeUsing(jsonField, backingField, mixIn, jacksonField);
            String label = resolveLabel(jsonField, backingField, mixIn);
            long fieldFeatures = resolveFieldFeatures(jsonField);
            boolean unwrapped = jsonField != null && jsonField.unwrapped();

            // Prefer backing field for Unsafe direct access (avoids Method.invoke overhead)
            Type fieldType = method.getGenericReturnType();
            Class<?> fieldClass = method.getReturnType();
            if (customWriter == null && fieldClass.isEnum()) {
                // Enum type declares @JSONField(value=true) / @JsonValue on one of its methods —
                // route the field through that single-value writer instead of TYPE_ENUM's
                // ordinal-based name cache.
                customWriter = findValueWriter(fieldClass, null, useJacksonAnnotation);
            }
            // Detect List<T> element type for specialized List serialization
            // Skip specialization when field has custom features/unwrapped (not supported by ofList)
            if (List.class.isAssignableFrom(fieldClass) && fieldFeatures == 0 && !unwrapped
                    && format == null && customWriter == null) {
                Class<?> elemClass = extractListElementClass(fieldType);
                if (elemClass != null) {
                    if (backingField != null) {
                        backingField.setAccessible(true);
                        writerMap.put(propertyName, FieldWriter.ofList(
                                jsonName, ordinal, fieldType, fieldClass, elemClass, backingField, fieldInclusion));
                    } else {
                        method.setAccessible(true);
                        writerMap.put(propertyName, FieldWriter.ofList(
                                jsonName, ordinal, fieldType, fieldClass, elemClass, method, fieldInclusion));
                    }
                    continue;
                }
            }
            if (backingField != null) {
                backingField.setAccessible(true);
                writerMap.put(propertyName, FieldWriter.ofField(
                        jsonName, ordinal, fieldType, fieldClass,
                        backingField, fieldInclusion, format, customWriter, label,
                        fieldFeatures, unwrapped
                ));
            } else {
                method.setAccessible(true);
                writerMap.put(propertyName, FieldWriter.ofGetter(
                        jsonName, ordinal, fieldType, fieldClass,
                        method, fieldInclusion, format, customWriter, label,
                        fieldFeatures, unwrapped
                ));
            }
        }

        // 2. Inspect public fields as fallback (only if no getter already found)
        for (Field field : type.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            String propertyName = field.getName();
            if (writerMap.containsKey(propertyName) || suppressedNames.contains(propertyName)) {
                continue;
            }

            JSONField jsonField = field.getAnnotation(JSONField.class);
            // Check mixin for field annotation
            if (jsonField == null && mixIn != null) {
                jsonField = findMixInFieldAnnotation(mixIn, propertyName, JSONField.class);
            }
            if (jsonField != null && !jsonField.serialize()) {
                continue;
            }

            // Jackson field annotations as fallback
            JacksonAnnotationSupport.FieldInfo jacksonField = null;
            if (jsonField == null && useJacksonAnnotation) {
                jacksonField = JacksonAnnotationSupport.getFieldInfo(field.getAnnotations());
                if (jacksonField != null && jacksonField.noSerialize()) {
                    continue;
                }
            }

            String jsonName;
            int ordinal = 0;

            if (jsonField != null && !jsonField.name().isEmpty()) {
                jsonName = jsonField.name();
            } else if (jacksonField != null && !jacksonField.name().isEmpty()) {
                jsonName = jacksonField.name();
            } else {
                jsonName = applyNamingStrategy(propertyName, naming);
            }

            if (jsonField != null) {
                ordinal = jsonField.ordinal();
            } else if (jacksonField != null) {
                ordinal = jacksonField.ordinal();
            }

            // Resolve inclusion: field-level overrides type-level
            Inclusion fieldInclusion;
            if (jsonField != null && jsonField.inclusion() != Inclusion.DEFAULT) {
                fieldInclusion = jsonField.inclusion();
            } else if (jacksonField != null && jacksonField.inclusion() != null) {
                fieldInclusion = jacksonField.inclusion();
            } else {
                fieldInclusion = typeInclusion;
            }

            String format = resolveFormat(jsonField, jacksonField, null, mixIn, useJacksonAnnotation);
            ObjectWriter<?> customWriter = resolveSerializeUsing(jsonField, null, mixIn, jacksonField);
            String label = resolveLabel(jsonField, null, mixIn);
            long fieldFeatures = resolveFieldFeatures(jsonField);
            boolean unwrapped = jsonField != null && jsonField.unwrapped();

            Class<?> publicFieldClass = field.getType();
            if (customWriter == null && publicFieldClass.isEnum()) {
                customWriter = findValueWriter(publicFieldClass, null, useJacksonAnnotation);
            }

            field.setAccessible(true);
            writerMap.put(propertyName, FieldWriter.ofField(
                    jsonName, ordinal, field.getGenericType(), field.getType(), field,
                    fieldInclusion, format, customWriter, label, fieldFeatures, unwrapped
            ));
        }

        // 2.5. Apply Serializable checks (pre-computed at writer creation time — zero runtime cost)
        boolean ignoreNonSerializable = (writeFeatures & WriteFeature.IgnoreNoneSerializable.mask) != 0;
        boolean errorOnNonSerializable = (writeFeatures & WriteFeature.ErrorOnNoneSerializable.mask) != 0;
        if (ignoreNonSerializable || errorOnNonSerializable) {
            var it = writerMap.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                Class<?> fc = entry.getValue().fieldClass;
                if (fc.isPrimitive() || fc == String.class || java.io.Serializable.class.isAssignableFrom(fc)) {
                    continue;
                }
                if (errorOnNonSerializable) {
                    throw new com.alibaba.fastjson3.JSONException(
                            "field '" + entry.getKey() + "' type " + fc.getName()
                                    + " does not implement Serializable");
                }
                it.remove(); // ignoreNonSerializable
            }
        }

        // 3. Apply includes/ignores filters
        if (!includes.isEmpty()) {
            writerMap.keySet().retainAll(includes);
        }
        if (!ignores.isEmpty()) {
            writerMap.keySet().removeAll(ignores);
        }

        // 4. Sort: apply order hints, then ordinal, then alphabetic
        List<FieldWriter> fieldWriters = new ArrayList<>(writerMap.values());

        if (orders.length > 0) {
            Map<String, Integer> orderMap = new HashMap<>();
            for (int i = 0; i < orders.length; i++) {
                orderMap.put(orders[i], i);
            }
            fieldWriters.sort((a, b) -> {
                int oa = orderMap.getOrDefault(a.getFieldName(), Integer.MAX_VALUE);
                int ob = orderMap.getOrDefault(b.getFieldName(), Integer.MAX_VALUE);
                int cmp = Integer.compare(oa, ob);
                if (cmp != 0) {
                    return cmp;
                }
                return a.compareTo(b);
            });
        } else if (alphabetic) {
            Collections.sort(fieldWriters);
        }

        FieldWriter[] writers = fieldWriters.toArray(new FieldWriter[0]);

        // Scan for @JSONField(anyGetter=true) — separate pass over ALL methods
        Method anyGetterMethod = findAnyGetterMethod(type, mixIn, useJacksonAnnotation);

        // Pre-compute @JSONType(typeName/typeKey) — zero cost if not annotated
        String typeName = (jsonType != null && !jsonType.typeName().isEmpty()) ? jsonType.typeName() : null;
        String typeKey = (jsonType != null && !jsonType.typeKey().isEmpty()) ? jsonType.typeKey() : null;
        if (typeKey == null && useJacksonAnnotation) {
            JacksonAnnotationSupport.BeanInfo jacksonBean = findJacksonBeanInfoWithTypeKey(type);
            if (jacksonBean != null) {
                typeKey = jacksonBean.typeKey();
                if (typeName == null) {
                    typeName = resolveJacksonTypeName(type, jacksonBean);
                }
            }
        }
        if (typeName == null || typeKey == null) {
            JSONType ancestor = findSeeAlsoAncestor(type, ancestorMixIns);
            if (ancestor != null) {
                if (typeKey == null) {
                    typeKey = !ancestor.typeKey().isEmpty() ? ancestor.typeKey() : "@type";
                }
                if (typeName == null) {
                    typeName = type.getSimpleName();
                }
            }
        }

        return buildObjectWriter(writers, anyGetterMethod, typeName, typeKey);
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectWriter<T> buildObjectWriter(FieldWriter[] writers, Method anyGetterMethod,
                                                          String typeName, String typeKey) {
        if (anyGetterMethod == null) {
            // Concrete final class — JIT can devirtualize + inline write() for nested objects
            return (ObjectWriter<T>) new ReflectObjectWriter(writers, typeName, typeKey);
        }
        // Lambda fallback for anyGetter (rare)
        return (generator, object, fieldName, fieldType, features) -> {
            generator.startObject();
            writeFields(generator, writers, object, features);
            try {
                java.util.Map<?, ?> extra = (java.util.Map<?, ?>) anyGetterMethod.invoke(object);
                if (extra != null) {
                    for (java.util.Map.Entry<?, ?> e : extra.entrySet()) {
                        generator.writeName(String.valueOf(e.getKey()));
                        generator.writeAny(e.getValue());
                    }
                }
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw new com.alibaba.fastjson3.JSONException("anyGetter error", e.getTargetException());
            } catch (Exception e) {
                throw new com.alibaba.fastjson3.JSONException("anyGetter error", e);
            }
            generator.endObject();
        };
    }

    static void writeFields(com.alibaba.fastjson3.JSONGenerator generator, FieldWriter[] writers, Object object, long features) {
        // Static path: bypass virtual dispatch for UTF8 and Char generators
        if (!generator.hasFilters() && generator.labelFilter == null) {
            if (!generator.isPretty()) {
                if (generator instanceof com.alibaba.fastjson3.JSONGenerator.UTF8 utf8) {
                    com.alibaba.fastjson3.JSONGenerator.writeFieldsStaticUTF8NoFrame(utf8, writers, object, features);
                    return;
                }
                if (generator instanceof com.alibaba.fastjson3.JSONGenerator.Char charGen) {
                    com.alibaba.fastjson3.JSONGenerator.writeFieldsStaticChar(charGen, writers, object, features);
                    return;
                }
            }
            for (FieldWriter fw : writers) {
                fw.writeField(generator, object, features);
            }
        } else {
            com.alibaba.fastjson3.filter.LabelFilter lf = generator.labelFilter;
            for (FieldWriter fw : writers) {
                if (lf != null && fw.label != null && !lf.apply(fw.label)) continue;
                if (generator.hasFilters()) {
                    fw.writeFieldFiltered(generator, object, features,
                            generator.propertyPreFilters, generator.propertyFilters,
                            generator.valueFilters, generator.nameFilters);
                } else {
                    fw.writeField(generator, object, features);
                }
            }
        }
    }

    /**
     * Concrete ObjectWriter for reflection-created POJOs.
     * Using a final class (not lambda) enables JIT to devirtualize the write() call
     * at megamorphic call sites (e.g., FieldWriter.writeListObject's elemWriter.write loop).
     */
    public static final class ReflectObjectWriter implements com.alibaba.fastjson3.ObjectWriter<Object> {
        public final FieldWriter[] writers;
        private final int estimatedSize;
        // Pre-computed from @JSONType — null if not annotated (zero overhead)
        final String typeName;  // @JSONType(typeName="Dog") → "Dog"
        final String typeKey;   // @JSONType(typeKey="type") → "type", default "@type"

        ReflectObjectWriter(FieldWriter[] writers) {
            this(writers, null, null);
        }

        ReflectObjectWriter(FieldWriter[] writers, String typeName, String typeKey) {
            this.writers = writers;
            this.estimatedSize = writers.length * 32 + 16;
            this.typeName = typeName;
            this.typeKey = typeKey;
        }

        @Override
        public void write(com.alibaba.fastjson3.JSONGenerator generator, Object object,
                          Object fieldName, java.lang.reflect.Type fieldType, long features) {
            // Pre-allocate capacity for the entire object
            generator.ensureCapacityPublic(estimatedSize);
            // Track depth for NotWriteRootClassName (depth 0 = root)
            int depth = generator.getWriteDepth();
            generator.incrementDepth();
            try {
                generator.startObject();
                // Write type discriminator: annotation-driven (@JSONType/@JsonTypeInfo)
                // always writes; WriteClassName feature respects skip rules
                if (typeName != null || typeKey != null) {
                    String key = typeKey != null ? typeKey : "@type";
                    String name = typeName != null ? typeName : object.getClass().getName();
                    generator.writeName(key);
                    generator.writeString(name);
                } else if (generator.writeClassName) {
                    Class<?> objectClass = object.getClass();
                    boolean skip = (generator.notWriteRootClassName && depth == 0)
                            || (generator.notWriteHashMapArrayListClassName && isCommonContainerType(objectClass))
                            || (generator.notWriteSetClassName && java.util.Set.class.isAssignableFrom(objectClass))
                            || (generator.notWriteNumberClassName && Number.class.isAssignableFrom(objectClass));
                    if (!skip) {
                        generator.writeName("@type");
                        generator.writeString(objectClass.getName());
                    }
                }
                // Before filters
                if (generator.beforeFilters != null) {
                    for (var bf : generator.beforeFilters) {
                        bf.writeBefore(generator, object);
                    }
                }
                writeFields(generator, writers, object, features);
                // After filters
                if (generator.afterFilters != null) {
                    for (var af : generator.afterFilters) {
                        af.writeAfter(generator, object);
                    }
                }
                generator.endObject();
            } finally {
                generator.decrementDepth();
            }
        }

        private static boolean isCommonContainerType(Class<?> cls) {
            return cls == java.util.HashMap.class || cls == java.util.ArrayList.class
                    || cls == java.util.LinkedHashMap.class;
        }
    }

    /**
     * Extract the property name from a getter method name.
     * Supports getXxx and isXxx (for boolean) patterns.
     *
     * @return the property name with first letter lowercased, or null if not a getter
     */
    private static String extractPropertyName(String methodName, Class<?> returnType) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2
                && (returnType == boolean.class || returnType == Boolean.class)) {
            return decapitalize(methodName.substring(2));
        }
        return null;
    }

    /**
     * Decapitalize a string following JavaBeans conventions.
     * If the first two characters are both uppercase, return as-is (e.g., "URL" stays "URL").
     */
    private static String decapitalize(String name) {
        if (name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Apply a naming strategy to convert a Java property name to a JSON field name.
     */
    static String applyNamingStrategy(String name, NamingStrategy strategy) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (strategy == NamingStrategy.NoneStrategy || strategy == NamingStrategy.CamelCase) {
            return name;
        } else if (strategy == NamingStrategy.PascalCase) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else if (strategy == NamingStrategy.SnakeCase) {
            return camelToSeparated(name, '_', false);
        } else if (strategy == NamingStrategy.UpperSnakeCase) {
            return camelToSeparated(name, '_', true);
        } else if (strategy == NamingStrategy.KebabCase) {
            return camelToSeparated(name, '-', false);
        } else if (strategy == NamingStrategy.UpperKebabCase) {
            return camelToSeparated(name, '-', true);
        }
        return name;
    }

    /**
     * Convert camelCase to a separated format (snake_case, kebab-case, etc.).
     *
     * @param name      the camelCase name
     * @param separator the separator character ('_' or '-')
     * @param upper     whether to uppercase the result
     */
    private static String camelToSeparated(String name, char separator, boolean upper) {
        var sb = new StringBuilder(name.length() + 4);
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    sb.append(separator);
                }
                sb.append(upper ? ch : Character.toLowerCase(ch));
            } else {
                sb.append(upper ? Character.toUpperCase(ch) : ch);
            }
        }
        return sb.toString();
    }

    private static FieldWriter createFieldWriterForGetter(
            String jsonName, int ordinal, Type fieldType, Class<?> fieldClass, Method method
    ) {
        return createFieldWriterForGetter(jsonName, ordinal, fieldType, fieldClass, method, Inclusion.DEFAULT);
    }

    private static FieldWriter createFieldWriterForGetter(
            String jsonName, int ordinal, Type fieldType, Class<?> fieldClass, Method method,
            Inclusion inclusion
    ) {
        if (List.class.isAssignableFrom(fieldClass)) {
            Class<?> elemClass = extractListElementClass(fieldType);
            if (elemClass != null) {
                return FieldWriter.ofList(jsonName, ordinal, fieldType, fieldClass, elemClass, method, inclusion);
            }
        }
        return FieldWriter.ofGetter(jsonName, ordinal, fieldType, fieldClass, method, inclusion);
    }

    private static FieldWriter createFieldWriterForField(
            String jsonName, int ordinal, Type fieldType, Class<?> fieldClass, Field field
    ) {
        return createFieldWriterForField(jsonName, ordinal, fieldType, fieldClass, field, Inclusion.DEFAULT);
    }

    private static FieldWriter createFieldWriterForField(
            String jsonName, int ordinal, Type fieldType, Class<?> fieldClass, Field field,
            Inclusion inclusion
    ) {
        if (List.class.isAssignableFrom(fieldClass)) {
            Class<?> elemClass = extractListElementClass(fieldType);
            if (elemClass != null) {
                return FieldWriter.ofList(jsonName, ordinal, fieldType, fieldClass, elemClass, field, inclusion);
            }
        }
        return FieldWriter.ofField(jsonName, ordinal, fieldType, fieldClass, field, inclusion);
    }

    private static Class<?> extractListElementClass(Type fieldType) {
        if (fieldType instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?> elemClass) {
                return elemClass;
            }
        }
        return null;
    }

    /**
     * Find a declared field by name, searching up the class hierarchy.
     */
    private static Field findDeclaredField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectWriter<T> createSingleValueWriter(Method valueMethod) {
        return (ObjectWriter<T>) (com.alibaba.fastjson3.ObjectWriter<Object>)
                (generator, object, fieldName, fieldType, features) -> {
                    try {
                        Object val = valueMethod.invoke(object);
                        generator.writeAny(val);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw new com.alibaba.fastjson3.JSONException(
                                "error invoking value method: " + valueMethod.getName(), e.getTargetException());
                    } catch (Exception e) {
                        throw new com.alibaba.fastjson3.JSONException(
                                "error invoking value method: " + valueMethod.getName(), e);
                    }
                };
    }

    // ==================== @JSONField(value=true) / format / serializeUsing ====================

    /**
     * Scan for @JSONField(value = true) on getter methods. If found, return a single-value writer.
     * Also checks Jackson @JsonValue when useJacksonAnnotation is true.
     * Returns null if no value method is found.
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectWriter<T> findValueWriter(Class<T> type, Class<?> mixIn, boolean useJacksonAnnotation) {
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class || method.getParameterCount() != 0
                    || java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            JSONField jsonField = method.getAnnotation(JSONField.class);
            if (jsonField == null && mixIn != null) {
                String propName = extractPropertyName(method.getName(), method.getReturnType());
                if (propName != null) {
                    jsonField = findMixInAnnotation(mixIn, method, propName, JSONField.class);
                }
            }
            if (jsonField != null && jsonField.value()) {
                Method valueMethod = method;
                valueMethod.setAccessible(true);
                return createSingleValueWriter(valueMethod);
            }
            // Also check Jackson @JsonValue via JacksonAnnotationSupport
            if (useJacksonAnnotation && jsonField == null) {
                JacksonAnnotationSupport.FieldInfo jacksonInfo = JacksonAnnotationSupport.getFieldInfo(method.getAnnotations());
                if (jacksonInfo != null && jacksonInfo.isValue()) {
                    Method valueMethod = method;
                    valueMethod.setAccessible(true);
                    return createSingleValueWriter(valueMethod);
                }
            }
        }
        return null;
    }

    /**
     * Resolve format string from @JSONField or Jackson @JsonFormat, checking method and backing field.
     */
    private static String resolveFormat(JSONField jsonField, JacksonAnnotationSupport.FieldInfo jacksonField,
                                        Field backingField, Class<?> mixIn, boolean useJacksonAnnotation) {
        if (jsonField != null && !jsonField.format().isEmpty()) {
            return jsonField.format();
        }
        if (jacksonField != null && jacksonField.format() != null && !jacksonField.format().isEmpty()) {
            return jacksonField.format();
        }
        // Check backing field annotation
        if (backingField != null) {
            JSONField fAnn = backingField.getAnnotation(JSONField.class);
            if (fAnn == null && mixIn != null) {
                fAnn = findMixInFieldAnnotation(mixIn, backingField.getName(), JSONField.class);
            }
            if (fAnn != null && !fAnn.format().isEmpty()) {
                return fAnn.format();
            }
        }
        return null;
    }

    /**
     * Resolve serializeUsing from @JSONField, instantiating the custom ObjectWriter.
     */
    @SuppressWarnings("unchecked")
    private static ObjectWriter<?> resolveSerializeUsing(
            JSONField jsonField, Field backingField, Class<?> mixIn,
            JacksonAnnotationSupport.FieldInfo jacksonField) {
        Class<?> usingClass = null;
        if (jsonField != null && jsonField.serializeUsing() != Void.class) {
            usingClass = jsonField.serializeUsing();
        }
        if (usingClass == null && backingField != null) {
            JSONField fAnn = backingField.getAnnotation(JSONField.class);
            if (fAnn == null && mixIn != null) {
                fAnn = findMixInFieldAnnotation(mixIn, backingField.getName(), JSONField.class);
            }
            if (fAnn != null && fAnn.serializeUsing() != Void.class) {
                usingClass = fAnn.serializeUsing();
            }
        }
        if (usingClass == null && jacksonField != null && jacksonField.serializeUsing() != null) {
            usingClass = jacksonField.serializeUsing();
        }
        if (usingClass != null) {
            if (!com.alibaba.fastjson3.ObjectWriter.class.isAssignableFrom(usingClass)) {
                throw new com.alibaba.fastjson3.JSONException(
                        "serializeUsing class must implement ObjectWriter: " + usingClass.getName());
            }
            try {
                return (ObjectWriter<?>) usingClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new com.alibaba.fastjson3.JSONException(
                        "cannot instantiate serializeUsing class: " + usingClass.getName(), e);
            }
        }
        return null;
    }

    /**
     * Resolve the Jackson subtype name for a concrete class by looking up
     * the parent's {@code @JsonSubTypes} mapping.
     */
    private static String resolveJacksonTypeName(Class<?> type, JacksonAnnotationSupport.BeanInfo parentBean) {
        if (parentBean.subTypes() != null) {
            for (var entry : parentBean.subTypes().entrySet()) {
                if (entry.getValue() == type) {
                    return entry.getKey();
                }
            }
        }
        return type.getSimpleName();
    }

    /**
     * Walk the full class + interface hierarchy of {@code type} looking for an
     * {@code @JSONType(seeAlso=...)} declaration (either directly on the ancestor
     * or on a mix-in registered for that ancestor) that lists {@code type} as a
     * polymorphic subtype. Returns the discovered parent annotation so the
     * subtype writer can inherit {@code typeKey}/{@code typeName} defaults —
     * otherwise round-trip with a parent-only discriminator config breaks because
     * the subtype emits "@type" while the parent reader expects the parent's
     * custom {@code typeKey}.
     *
     * @param ancestorMixIns map used to resolve mix-in classes by ancestor type.
     *     {@code null} is treated as empty — ancestor mix-in resolution is then
     *     skipped and only direct annotations participate. {@link ObjectMapper}
     *     passes its full mix-in cache so {@code addMixIn(AncestorInterface.class,
     *     AncestorMixIn.class)}-style registrations work end-to-end.
     */
    private static JSONType findSeeAlsoAncestor(Class<?> type,
                                                 java.util.Map<Class<?>, Class<?>> ancestorMixIns) {
        java.util.Set<Class<?>> visited = new java.util.HashSet<>();
        return findSeeAlsoAncestorRecursive(type, type, ancestorMixIns, visited);
    }

    /**
     * @param target  the class we're asking "who declares me in seeAlso?" — never changes during recursion
     * @param current the node being inspected on this step
     */
    private static JSONType findSeeAlsoAncestorRecursive(
            Class<?> target,
            Class<?> current,
            java.util.Map<Class<?>, Class<?>> ancestorMixIns,
            java.util.Set<Class<?>> visited
    ) {
        if (current == null || current == Object.class || !visited.add(current)) {
            return null;
        }
        // Don't treat the target itself as its own ancestor.
        if (current != target) {
            JSONType direct = current.getAnnotation(JSONType.class);
            if (direct != null && containsType(direct.seeAlso(), target)) {
                return direct;
            }
            if (ancestorMixIns != null) {
                Class<?> mix = ancestorMixIns.get(current);
                if (mix != null) {
                    JSONType mxAnn = mix.getAnnotation(JSONType.class);
                    if (mxAnn != null && containsType(mxAnn.seeAlso(), target)) {
                        return mxAnn;
                    }
                }
            }
        }
        // Recurse into superclass first, then each implemented interface.
        JSONType f = findSeeAlsoAncestorRecursive(target, current.getSuperclass(), ancestorMixIns, visited);
        if (f != null) {
            return f;
        }
        for (Class<?> iface : current.getInterfaces()) {
            f = findSeeAlsoAncestorRecursive(target, iface, ancestorMixIns, visited);
            if (f != null) {
                return f;
            }
        }
        return null;
    }

    private static boolean containsType(Class<?>[] array, Class<?> target) {
        for (Class<?> c : array) {
            if (c == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * Walk the class hierarchy (type → superclass → interfaces) to find Jackson
     * {@code @JsonTypeInfo} with a typeKey. Needed because {@code @JsonTypeInfo} is not {@code @Inherited}.
     */
    private static JacksonAnnotationSupport.BeanInfo findJacksonBeanInfoWithTypeKey(Class<?> type) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            JacksonAnnotationSupport.BeanInfo info = JacksonAnnotationSupport.getBeanInfo(c);
            if (info != null && info.typeKey() != null) {
                return info;
            }
        }
        for (Class<?> iface : type.getInterfaces()) {
            JacksonAnnotationSupport.BeanInfo info = JacksonAnnotationSupport.getBeanInfo(iface);
            if (info != null && info.typeKey() != null) {
                return info;
            }
        }
        return null;
    }

    private static boolean isJacksonAnyGetter(Method method) {
        for (java.lang.annotation.Annotation ann : method.getAnnotations()) {
            if ("com.fasterxml.jackson.annotation.JsonAnyGetter".equals(ann.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Scan all methods for {@code @JSONField(anyGetter = true)} or Jackson {@code @JsonAnyGetter}.
     * When the annotation isn't on the target method directly, consult the
     * registered mix-in — a common Jackson pattern is to apply anyGetter
     * through a mix-in rather than edit the target class.
     *
     * <p>{@code getMethods()} order is JVM-unspecified, so first-wins would
     * be non-deterministic when two methods are annotated. Throw at
     * construction with both method names — matches Jackson's behaviour
     * and the sibling @JSONCreator ambiguity detector in PR #123.
     */
    private static Method findAnyGetterMethod(Class<?> type, Class<?> mixIn, boolean useJacksonAnnotation) {
        Method chosen = null;
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class || method.getParameterCount() != 0
                    || java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (!java.util.Map.class.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            boolean match = false;
            JSONField jsonField = method.getAnnotation(JSONField.class);
            if (jsonField == null && mixIn != null) {
                // Mix-in JSONField(anyGetter=true): a method declared on the
                // target picks up the annotation from a matching mix-in method.
                jsonField = findMixInAnnotation(mixIn, method,
                        extractPropertyName(method.getName(), method.getReturnType()),
                        JSONField.class);
            }
            if (jsonField != null && jsonField.anyGetter()) {
                match = true;
            } else if (useJacksonAnnotation) {
                if (isJacksonAnyGetter(method)
                        || (mixIn != null && findMixInJacksonAnyGetter(mixIn, method))) {
                    match = true;
                }
            }
            if (!match) {
                continue;
            }
            if (chosen != null && !chosen.equals(method)) {
                throw new com.alibaba.fastjson3.JSONException(
                        "multiple anyGetter methods on " + type.getName()
                                + ": " + chosen.getName() + " and " + method.getName()
                                + " — mark only one");
            }
            chosen = method;
        }
        if (chosen != null) {
            chosen.setAccessible(true);
        }
        return chosen;
    }

    private static boolean findMixInJacksonAnyGetter(Class<?> mixIn, Method targetMethod) {
        // Use getMethods() for the full transitive closure — inherited
        // public methods from all superclasses AND all (transitively) declared
        // interfaces are included. Covers:
        //   - interface A extends B (annotation on B), mixIn implements A
        //   - ChildMixIn extends ParentMixIn implements IBase (annotation on IBase)
        // Then fall back to a superclass chain walk that hits DECLARED methods
        // including package-private / protected ones which getMethods() skips.
        String name = targetMethod.getName();
        Class<?>[] pts = targetMethod.getParameterTypes();
        for (Method m : mixIn.getMethods()) {
            if (m.getName().equals(name)
                    && java.util.Arrays.equals(m.getParameterTypes(), pts)
                    && isJacksonAnyGetter(m)) {
                return true;
            }
        }
        // Non-public method declarations — getMethods() only returns public.
        // Walk the class chain + all interface layers via getDeclaredMethods.
        java.util.Set<Class<?>> visited = new java.util.HashSet<>();
        java.util.Deque<Class<?>> stack = new java.util.ArrayDeque<>();
        stack.push(mixIn);
        while (!stack.isEmpty()) {
            Class<?> host = stack.pop();
            if (host == null || host == Object.class || !visited.add(host)) {
                continue;
            }
            try {
                Method m = host.getDeclaredMethod(name, pts);
                if (isJacksonAnyGetter(m)) {
                    return true;
                }
            } catch (NoSuchMethodException ignored) {
                // not declared on this host
            }
            if (host.getSuperclass() != null) {
                stack.push(host.getSuperclass());
            }
            for (Class<?> iface : host.getInterfaces()) {
                stack.push(iface);
            }
        }
        return false;
    }

    private static String resolveLabel(JSONField jsonField, Field backingField, Class<?> mixIn) {
        if (jsonField != null && !jsonField.label().isEmpty()) {
            return jsonField.label();
        }
        if (backingField != null) {
            JSONField fAnn = backingField.getAnnotation(JSONField.class);
            if (fAnn == null && mixIn != null) {
                fAnn = findMixInFieldAnnotation(mixIn, backingField.getName(), JSONField.class);
            }
            if (fAnn != null && !fAnn.label().isEmpty()) {
                return fAnn.label();
            }
        }
        return null;
    }

    private static long resolveFieldFeatures(JSONField jsonField) {
        if (jsonField == null) {
            return 0;
        }
        WriteFeature[] sf = jsonField.serializeFeatures();
        if (sf.length == 0) {
            return 0;
        }
        return WriteFeature.of(sf);
    }

    // ==================== Mixin support ====================

    /**
     * Find an annotation on the corresponding method in the mixin class.
     * Matches by method name and parameter types.
     */
    static <A extends java.lang.annotation.Annotation> A findMixInAnnotation(
            Class<?> mixIn, Method targetMethod, String propertyName, Class<A> annotationType) {
        // Try matching method by name and signature
        for (Method m : mixIn.getMethods()) {
            if (m.getName().equals(targetMethod.getName())
                    && m.getParameterCount() == targetMethod.getParameterCount()
                    && java.util.Arrays.equals(m.getParameterTypes(), targetMethod.getParameterTypes())) {
                A ann = m.getAnnotation(annotationType);
                if (ann != null) {
                    return ann;
                }
            }
        }
        // Try matching by getter/setter naming convention for the property
        String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        String isName = "is" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        for (Method m : mixIn.getMethods()) {
            String mn = m.getName();
            if ((mn.equals(getterName) || mn.equals(isName)) && m.getParameterCount() == 0) {
                A ann = m.getAnnotation(annotationType);
                if (ann != null) {
                    return ann;
                }
            }
        }
        // Also check declared methods (including non-public in interfaces/abstract classes)
        for (Method m : mixIn.getDeclaredMethods()) {
            String mn = m.getName();
            if ((mn.equals(getterName) || mn.equals(isName)) && m.getParameterCount() == 0) {
                A ann = m.getAnnotation(annotationType);
                if (ann != null) {
                    return ann;
                }
            }
        }
        return null;
    }

    /**
     * Find an annotation on the corresponding field in the mixin class.
     */
    static <A extends java.lang.annotation.Annotation> A findMixInFieldAnnotation(
            Class<?> mixIn, String fieldName, Class<A> annotationType) {
        try {
            Field f = mixIn.getDeclaredField(fieldName);
            return f.getAnnotation(annotationType);
        } catch (NoSuchFieldException ignored) {
        }
        return null;
    }
}

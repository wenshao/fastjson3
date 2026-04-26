package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.JSONParser;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.ReadFeature;
import com.alibaba.fastjson3.annotation.*;
import com.alibaba.fastjson3.schema.JSONSchema;
import com.alibaba.fastjson3.util.JDKUtils;
import com.alibaba.fastjson3.util.TypeUtils;

import java.lang.reflect.*;
import java.util.*;

/**
 * Creates {@link ObjectReader} instances for arbitrary POJO classes via reflection.
 */
public final class ObjectReaderCreator {
    private ObjectReaderCreator() {
    }

    /**
     * Per-call mix-in cache, scoped by {@link ObjectMapper#getObjectReader}. Allows
     * helpers deep in the creation pipeline (e.g. inner-POJO expansion for
     * {@code @JSONField(unwrapped=true)}) to resolve a mix-in for a type other
     * than the top-level target without threading the entire mixInCache through
     * every overload. {@code null} outside a mapper-driven creation call.
     */
    public static final ThreadLocal<java.util.Map<Class<?>, Class<?>>> MIXIN_CONTEXT = new ThreadLocal<>();

    static Class<?> resolveMixIn(Class<?> type) {
        java.util.Map<Class<?>, Class<?>> ctx = MIXIN_CONTEXT.get();
        return ctx != null ? ctx.get(type) : null;
    }

    /**
     * Tracks inner classes currently being walked by {@code expandUnwrappedField}
     * / {@code expandRecordUnwrapped}. A cyclic {@code @JSONField(unwrapped=true)}
     * chain (A → B → A, or any longer loop) would otherwise recurse into
     * {@code collectFieldReaders} forever and blow the stack — we reject it at
     * construction time with a clear message instead. Cleared on the outer-most
     * exit so the top-level caller doesn't leak thread-local state.
     */
    private static final ThreadLocal<Set<Class<?>>> UNWRAP_VISITED = new ThreadLocal<>();

    /**
     * Field / element types whose reader wiring must defer to the parser's
     * native {@code readObject} / {@code readArray} / {@code readAny} path
     * instead of the auto-built reflection POJO reader. Auto-build silently
     * loses data on {@code JSONObject} (no field readers found), rejects
     * arrays outright on {@code JSONArray}, and produces a bare
     * {@code new Object()} for {@code Object} fields (which then also
     * rejects non-object JSON literals). Covered as a field type, element
     * type, and component type of an array field.
     */
    /**
     * Types whose deserialization is handled natively by the parser
     * ({@link com.alibaba.fastjson3.JSONParser#read(Class)}'s
     * {@code readObject} / {@code readArray} / {@code readAny} branches and
     * the raw-container fallback) and must NOT have an auto-built reflection
     * POJO reader synthesized for them. Without this gate, providers such as
     * {@link AbstractObjectReaderProvider} subclasses (ASM / Reflect / Auto)
     * would produce a broken reader that silently empties or throws on
     * non-object literals — defeating the parser fallback in
     * {@link com.alibaba.fastjson3.ObjectMapper#readValue(byte[], Class)}.
     *
     * <p>Keep in sync with {@code ObjectMapper.isParserShortCircuitClass}
     * — these two cover the same set from different layers: the
     * {@link com.alibaba.fastjson3.ObjectMapper} version short-circuits its
     * own auto-create branch (after modules / BuiltinCodecs / readerCreator),
     * while this one short-circuits provider-level reader creation so that
     * even {@code .readerProvider(ASM)} / {@code .readerCreatorType(...)}
     * mappers respect the parser fallback for these tree-shape types.
     */
    static boolean isParserHandled(Class<?> target) {
        return target == com.alibaba.fastjson3.JSONObject.class
                || target == com.alibaba.fastjson3.JSONArray.class
                || target == Object.class
                || target == java.util.Map.class
                || target == java.util.AbstractMap.class
                || target == java.util.List.class
                || target == java.util.Collection.class
                || target == Iterable.class
                || target == java.util.ArrayList.class
                || target == java.util.AbstractCollection.class
                || target == java.util.AbstractList.class
                || target == java.util.Set.class
                || target == java.util.AbstractSet.class
                || target == java.util.HashSet.class
                || target == java.util.LinkedHashSet.class;
    }

    private static boolean isJsonNodeOrJsonNodeArray(Class<?> target) {
        if (target == com.alibaba.fastjson3.JSONObject.class
                || target == com.alibaba.fastjson3.JSONArray.class
                || target == Object.class) {
            return true;
        }
        if (target.isArray()) {
            Class<?> ct = target.getComponentType();
            return ct == com.alibaba.fastjson3.JSONObject.class
                    || ct == com.alibaba.fastjson3.JSONArray.class
                    || ct == Object.class;
        }
        return false;
    }

    public static <T> ObjectReader<T> createObjectReader(Class<T> type) {
        return createObjectReader(type, null, false);
    }

    public static <T> ObjectReader<T> createObjectReader(Class<T> type, Class<?> mixIn) {
        return createObjectReader(type, mixIn, false);
    }

    public static <T> ObjectReader<T> createObjectReader(Class<T> type, Class<?> mixIn, boolean useJacksonAnnotation) {
        // Tree-shape / raw-container types are handled by the parser's native
        // routing; auto-building a reflection POJO reader for them produces
        // broken results (silent empty / "expected '{'" on non-objects). See
        // isParserHandled — this guard makes provider-level entry points
        // (ASM / Reflect / Auto via AbstractObjectReaderProvider#createReader)
        // safe to call without each provider repeating the check.
        if (isParserHandled(type)) {
            return null;
        }
        return createObjectReader(type, type, mixIn, useJacksonAnnotation);
    }

    /**
     * Create an {@link ObjectReader} for a parameterized POJO type such as
     * {@code Parent<Bean>}. Field resolution uses {@code pt}'s actual type
     * arguments so {@code T} fields read as their concrete type rather than
     * erased {@link Object}. The returned reader is specific to this
     * parameterization and should be cached by the {@link ParameterizedType}
     * key, not the raw class.
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectReader<T> createObjectReader(ParameterizedType pt, Class<?> mixIn, boolean useJacksonAnnotation) {
        if (!(pt.getRawType() instanceof Class<?> raw)) {
            return null;
        }
        return (ObjectReader<T>) createObjectReader((Class<Object>) raw, (java.lang.reflect.Type) pt,
                mixIn, useJacksonAnnotation);
    }

    /**
     * Internal entry used by both the {@link Class}-typed and
     * {@link ParameterizedType}-typed public entries. {@code contextType}
     * governs field-type resolution — for a parameterized context, its actual
     * type arguments bind the raw class's {@link TypeVariable}s.
     */
    private static <T> ObjectReader<T> createObjectReader(
            Class<T> type, java.lang.reflect.Type contextType, Class<?> mixIn, boolean useJacksonAnnotation) {
        // java.time types (LocalDate, LocalDateTime, etc.) are records in Java 17+
        // but should be handled by built-in date codecs, not RecordObjectReader
        if (type.getPackage() != null && type.getPackage().getName().equals("java.time")) {
            return null;
        }

        // Enum types don't have accessible constructors - they are deserialized
        // inline by FieldReader via name/ordinal lookup
        if (type.isEnum()) {
            return null;
        }

        // Static @JSONCreator / Jackson @JsonCreator factory method takes
        // precedence even over record's canonical-ctor dispatch. Record
        // authors may expose a validating factory (`@JSONCreator static R of
        // (int v) { if (v < 0) throw ...; return new R(v); }`) that the
        // record path would otherwise bypass, silently skipping the
        // validation. Checked BEFORE the record short-circuit AND before
        // the interface/abstract guard so factories on sealed / interface
        // / record targets all resolve through this single entry.
        Method staticFactory = resolveStaticFactoryMethod(type, mixIn, useJacksonAnnotation);
        if (staticFactory != null) {
            return createStaticFactoryReader(type, contextType, staticFactory, mixIn, useJacksonAnnotation);
        }

        if (JDKUtils.isRecord(type)) {
            return createRecordReader(type, contextType, mixIn, useJacksonAnnotation);
        }

        // Resolve type-level @JSONType once, consulting the mix-in when the target
        // class isn't annotated directly. Used by both the sealed path and the
        // explicit @JSONType(seeAlso) path below so mix-in-only registrations work.
        JSONType typeAnnotation = type.getAnnotation(JSONType.class);
        if (typeAnnotation == null && mixIn != null) {
            typeAnnotation = mixIn.getAnnotation(JSONType.class);
        }

        // Sealed class/interface: auto-discover subtypes for polymorphic deserialization
        if (type.isSealed()) {
            return createSealedReader(type, typeAnnotation, mixIn);
        }

        // @JSONType(seeAlso=...) on abstract class or interface: manual polymorphic
        // registration for non-sealed hierarchies. Reuses createSealedReader's logic
        // since that method already honours seeAlso; we just need to route here.
        if (typeAnnotation != null && typeAnnotation.seeAlso().length > 0
                && (type.isInterface() || Modifier.isAbstract(type.getModifiers()))) {
            return createSealedReader(type, typeAnnotation, mixIn);
        }

        // Jackson @JsonTypeInfo + @JsonSubTypes: polymorphic deserialization for non-sealed types
        if (useJacksonAnnotation) {
            JacksonAnnotationSupport.BeanInfo jacksonBean = JacksonAnnotationSupport.getBeanInfo(type);
            if (jacksonBean != null && jacksonBean.typeKey() != null && jacksonBean.subTypes() != null) {
                return createJacksonPolymorphicReader(type, mixIn, jacksonBean.typeKey(), jacksonBean.subTypes());
            }
        }

        // Abstract class or interface without polymorphic info: no way to construct.
        // Return null so the caller (JSONParser.read) can raise a targeted error
        // that distinguishes "no registered reader (maybe handled by a parser
        // special case)" from "abstract type with no discriminator registered".
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            return null;
        }

        Constructor<T> constructor = resolveConstructor(type, mixIn, useJacksonAnnotation);
        constructor.setAccessible(true);

        // If constructor has parameters, use constructor-based reader (like Records)
        // This handles: Kotlin data classes, Java immutable classes, @JSONCreator with params
        if (constructor.getParameterCount() > 0) {
            return createConstructorReader(type, contextType, constructor, mixIn, useJacksonAnnotation);
        }

        FieldReaderCollection collection = collectFieldReaders(type, contextType, mixIn, useJacksonAnnotation);

        // Scan for @JSONField(anySetter=true) or Jackson @JsonAnySetter
        Method anySetter = findAnySetterMethod(type, mixIn, useJacksonAnnotation);

        // Prefer Unsafe allocation for the common POJO case — bean fields get overwritten by
        // the parse path anyway, so skipping the constructor is safe and faster. Exception:
        // when the class declares an anySetter, its backing storage is typically an instance
        // field with an initializer (private Map<String,Object> extra = new LinkedHashMap<>()),
        // and Unsafe.allocateInstance skips initializers. Fall back to the no-arg constructor
        // so the map is non-null before the first anySetter.put() call.
        boolean useUnsafeAlloc = JDKUtils.UNSAFE_AVAILABLE && anySetter == null;

        // Parse class-level @JSONType(schema=)
        JSONSchema typeSchema = parseTypeSchema(type);

        // Type-discriminator key to exclude from anySetter routing. Only set
        // when the class has annotation-driven evidence that the writer WILL
        // emit a discriminator — mirrors ObjectWriterCreator's typeKey
        // resolution so a non-polymorphic class with @JSONType(typeName=) or
        // an ancestor with @JSONType(seeAlso=) doesn't round-trip the
        // discriminator through the anySetter map (which causes re-write to
        // emit it twice). Null for plain POJOs — "@type" stays a valid
        // business field for users who want to capture it via anySetter.
        //
        // Runtime WriteFeature.WriteClassName can't be mirrored here: the
        // reader has no way to know at construction whether the writer
        // enabled it, so the round-trip duplication on WriteClassName +
        // anySetter + no-@JSONType is a documented gap, not a silent bug.
        String anySetterTypeKey = (anySetter != null)
                ? resolveAnySetterTypeKey(type, mixIn, useJacksonAnnotation)
                : null;

        return new ReflectionObjectReader<>(type, constructor, collection.fieldReaders,
                collection.fieldReaderMap, collection.matcher, useUnsafeAlloc, anySetter,
                anySetterTypeKey, typeSchema,
                collection.unwrappedEntries, collection.requiredUnwrappedHolders);
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectReader<T> createRecordReader(Class<T> type, java.lang.reflect.Type contextType, Class<?> mixIn, boolean useJacksonAnnotation) {
        String[] componentNames = JDKUtils.getRecordComponentNames(type);
        Class<?>[] componentTypes = JDKUtils.getRecordComponentTypes(type);
        java.lang.reflect.Type[] genericTypes = JDKUtils.getRecordComponentGenericTypes(type);

        // Find canonical constructor
        Constructor<T> constructor;
        try {
            constructor = type.getDeclaredConstructor(componentTypes);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new JSONException("no canonical constructor found for record " + type.getName(), e);
        }

        // Build FieldReaders from record components
        JSONType jsonType = type.getAnnotation(JSONType.class);
        if (jsonType == null && mixIn != null) {
            jsonType = mixIn.getAnnotation(JSONType.class);
        }
        NamingStrategy naming = jsonType != null ? jsonType.naming() : com.alibaba.fastjson3.annotation.NamingStrategy.NoneStrategy;

        // Jackson class-level fallback
        if (useJacksonAnnotation) {
            JacksonAnnotationSupport.BeanInfo jackson = JacksonAnnotationSupport.getBeanInfo(type);
            if (jackson != null) {
                if (naming == NamingStrategy.NoneStrategy && jackson.naming() != null) {
                    naming = jackson.naming();
                }
            }
        }

        List<FieldReader> fieldReaderList = new ArrayList<>();
        List<RecordUnwrappedEntry> recordUnwrapped = null;
        List<Integer> recordRequiredIndices = null;
        for (int i = 0; i < componentNames.length; i++) {
            String rawName = componentNames[i];
            Field field = null;
            try {
                field = type.getDeclaredField(rawName);
                field.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }

            // Check mixIn class for annotation on matching field
            JSONField annotation = field != null ? field.getAnnotation(JSONField.class) : null;
            if (annotation == null && mixIn != null) {
                try {
                    Field mixInField = mixIn.getDeclaredField(rawName);
                    annotation = mixInField.getAnnotation(JSONField.class);
                } catch (NoSuchFieldException ignored) {
                }
            }

            // @JSONField(unwrapped=true) on a record component: don't register a
            // regular FieldReader — collect the inner's FieldReaders in a parallel
            // side table keyed by (component index, inner reader) so the record
            // reader can stage a scratch inner into the canonical constructor slot.
            if (annotation != null && annotation.unwrapped()) {
                if (componentTypes[i].isPrimitive()) {
                    throw new JSONException("@JSONField(unwrapped=true) on record component '"
                            + rawName + "': primitive component cannot be an unwrapped holder");
                }
                if (!annotation.defaultValue().isEmpty()) {
                    throw new JSONException("@JSONField(unwrapped=true) on record component '"
                            + rawName + "' does not support defaultValue — the holder is a POJO");
                }
                if (recordUnwrapped == null) {
                    recordUnwrapped = new ArrayList<>();
                }
                expandRecordUnwrapped(i, componentTypes[i], genericTypes[i], useJacksonAnnotation, recordUnwrapped);
                if (annotation.required()) {
                    if (recordRequiredIndices == null) {
                        recordRequiredIndices = new ArrayList<>();
                    }
                    recordRequiredIndices.add(i);
                }
                continue;
            }

            // Jackson field annotations as fallback
            JacksonAnnotationSupport.FieldInfo jacksonField = null;
            if (annotation == null && useJacksonAnnotation && field != null) {
                jacksonField = JacksonAnnotationSupport.getFieldInfo(field.getAnnotations());
            }

            String jsonName = resolveFieldName(rawName, annotation, naming);
            String[] alternateNames = annotation != null ? annotation.alternateNames() : new String[0];
            int ordinal = annotation != null ? annotation.ordinal() : 0;
            String defaultValue = annotation != null ? annotation.defaultValue() : "";
            boolean required = annotation != null && annotation.required();
            String schema = (annotation != null && !annotation.schema().isEmpty()) ? annotation.schema() : null;

            // Apply Jackson fallbacks
            if (jacksonField != null) {
                if (jsonName.equals(applyNamingStrategy(rawName, naming)) && !jacksonField.name().isEmpty()) {
                    jsonName = jacksonField.name();
                }
                if (alternateNames.length == 0 && jacksonField.alternateNames().length > 0) {
                    alternateNames = jacksonField.alternateNames();
                }
                if (ordinal == 0) {
                    ordinal = jacksonField.ordinal();
                }
                if (!required) {
                    required = jacksonField.required();
                }
            }

            fieldReaderList.add(new FieldReader(
                    jsonName, alternateNames,
                    TypeUtils.resolve(genericTypes[i], contextType), componentTypes[i],
                    ordinal, defaultValue, required,
                    field, null, null, null, schema, 0, useJacksonAnnotation
            ));
        }

        Collections.sort(fieldReaderList);

        FieldReader[] fieldReaders = fieldReaderList.toArray(new FieldReader[0]);
        FieldNameMatcher matcher = FieldNameMatcher.build(fieldReaders);

        Map<String, FieldReader> fieldReaderMap = HashMap.newHashMap(fieldReaders.length * 2);
        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fr = fieldReaders[i];
            fr.index = i;
            fieldReaderMap.put(fr.fieldName, fr);
            for (String alt : fr.alternateNames) {
                fieldReaderMap.putIfAbsent(alt, fr);
            }
        }

        FieldReaderCollection collection = new FieldReaderCollection(fieldReaders, matcher, fieldReaderMap, null, null);

        // Build component index mapping: fieldReader index → constructor param index
        // After sorting, fieldReader order may differ from constructor param order
        int[] paramMapping = new int[fieldReaders.length];
        for (int i = 0; i < fieldReaders.length; i++) {
            String name = fieldReaders[i].fieldName;
            // Find original component index
            for (int j = 0; j < componentNames.length; j++) {
                String resolvedName = resolveFieldName(componentNames[j],
                        null, naming);
                if (resolvedName.equals(name) || componentNames[j].equals(name)) {
                    paramMapping[i] = j;
                    break;
                }
            }
        }

        int[] requiredIdx = recordRequiredIndices == null ? null
                : recordRequiredIndices.stream().mapToInt(Integer::intValue).toArray();
        String[] requiredNames = recordRequiredIndices == null ? null
                : recordRequiredIndices.stream().map(ii -> componentNames[ii]).toArray(String[]::new);
        Constructor<T> ctor = constructor;
        InstanceInvoker<T> invoker = values -> ctor.newInstance(values);
        return new RecordObjectReader<>(type, invoker, ctor.getParameterTypes(), collection.fieldReaders,
                collection.fieldReaderMap, collection.matcher, componentTypes.length, paramMapping,
                recordUnwrapped, requiredIdx, requiredNames);
    }

    /**
     * Resolve the inner POJO's FieldReaders for a record component / constructor
     * parameter annotated with @JSONField(unwrapped=true), and stage them in the
     * record-side unwrapped list keyed by component index.
     */
    private static void expandRecordUnwrapped(int componentIdx, Class<?> innerClass,
                                               java.lang.reflect.Type genericInner,
                                               boolean useJacksonAnnotation,
                                               List<RecordUnwrappedEntry> sink) {
        rejectNonPojoUnwrapHolder(innerClass, "@JSONField(unwrapped=true) at component #" + componentIdx);

        Set<Class<?>> visited = UNWRAP_VISITED.get();
        boolean topEntry = (visited == null);
        if (topEntry) {
            visited = new HashSet<>();
            UNWRAP_VISITED.set(visited);
        }
        if (!visited.add(innerClass)) {
            if (topEntry) {
                UNWRAP_VISITED.remove();
            }
            throw new JSONException("Cyclic @JSONField(unwrapped=true) chain involving "
                    + innerClass.getName());
        }
        try {
            // Honour a user-registered mix-in for the inner type so renamed properties,
            // alternate names, ignores/includes rules etc. apply to flattened inner keys
            // the same way they would when reading the inner as a top-level target.
            // Use the generic form (e.g. Wrapper<Address>) as contextType so inner
            // field types parameterised on the outer's type argument (T value) resolve
            // to Address instead of erasing to Object.
            Class<?> innerMixIn = resolveMixIn(innerClass);
            java.lang.reflect.Type contextType = genericInner != null ? genericInner : innerClass;
            FieldReaderCollection innerCollection = collectFieldReaders(innerClass, contextType, innerMixIn, useJacksonAnnotation);
            for (FieldReader innerFr : innerCollection.fieldReaders) {
                sink.add(new RecordUnwrappedEntry(componentIdx, innerClass, innerFr));
            }
            // Nested unwrapped inside the component: the inner class itself declares
            // @JSONField(unwrapped=true), producing a deeper holder chain. Carry each
            // leaf through so double-flattened JSON emitted by the writer round-trips.
            // For a leaf inside `NestedName(@unwrapped Handle handle, String first)`
            // referenced from `record Person(@unwrapped NestedName name, int age)`,
            // the entry ends up keyed by "nick" with intermediateChain=[name.handle]
            // so ensureRecordScratch can construct NestedName, then Handle, before
            // writing the leaf field.
            if (innerCollection.unwrappedEntries != null) {
                for (UnwrappedEntry deep : innerCollection.unwrappedEntries) {
                    sink.add(new RecordUnwrappedEntry(componentIdx, innerClass,
                            deep.holderChain, deep.holderClasses, deep.innerReader));
                }
            }
        } finally {
            visited.remove(innerClass);
            if (topEntry) {
                UNWRAP_VISITED.remove();
            }
        }
    }

    /**
     * Create a constructor-based reader for non-record types with all-args constructors.
     * Handles Kotlin data classes, Java immutable classes, and @JSONCreator with parameters.
     * Reuses RecordObjectReader logic: collect values → invoke constructor.
     */
    @SuppressWarnings("unchecked")
    private static <T> ObjectReader<T> createConstructorReader(
            Class<T> type, java.lang.reflect.Type contextType, Constructor<T> constructor, Class<?> mixIn, boolean useJacksonAnnotation
    ) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        java.lang.reflect.Type[] genericParamTypes = constructor.getGenericParameterTypes();

        // Match constructor parameters to declared fields by position.
        // Parent-first order: superclass fields come before subclass fields,
        // matching typical constructor parameter order (super(...) params first).
        List<Field> instanceFields = getInstanceFieldsParentFirst(type);

        // Resolve naming strategy
        JSONType jsonType = type.getAnnotation(JSONType.class);
        if (jsonType == null && mixIn != null) {
            jsonType = mixIn.getAnnotation(JSONType.class);
        }
        NamingStrategy naming = jsonType != null ? jsonType.naming() : NamingStrategy.NoneStrategy;
        if (useJacksonAnnotation && naming == NamingStrategy.NoneStrategy) {
            JacksonAnnotationSupport.BeanInfo jackson = JacksonAnnotationSupport.getBeanInfo(type);
            if (jackson != null && jackson.naming() != null) {
                naming = jackson.naming();
            }
        }

        // Check for @JSONCreator(parameterNames=) override
        JSONCreator creatorAnn = constructor.getAnnotation(JSONCreator.class);
        String[] paramNames = (creatorAnn != null) ? creatorAnn.parameterNames() : new String[0];

        // Build FieldReaders from fields (in declaration order = constructor parameter order)
        List<FieldReader> fieldReaderList = new ArrayList<>();
        List<RecordUnwrappedEntry> ctorUnwrapped = null;
        List<Integer> ctorRequiredIndices = null;
        List<String> ctorRequiredNames = null;
        for (int i = 0; i < instanceFields.size() && i < paramTypes.length; i++) {
            Field field = instanceFields.get(i);
            field.setAccessible(true);
            String rawName = field.getName();

            // parameterNames override field name
            if (paramNames.length > i && !paramNames[i].isEmpty()) {
                rawName = paramNames[i];
            }

            JSONField annotation = field.getAnnotation(JSONField.class);
            if (annotation == null && mixIn != null) {
                annotation = findMixInFieldAnnotation(mixIn, rawName, JSONField.class);
            }
            // Constructor parameter annotations — an all-args / @JSONCreator style
            // class often carries @JSONField on the constructor parameter rather
            // than on the matching private field, so fall back to the parameter.
            if (annotation == null) {
                Parameter[] ctorParams = constructor.getParameters();
                if (i < ctorParams.length) {
                    annotation = ctorParams[i].getAnnotation(JSONField.class);
                }
            }

            // @JSONField(unwrapped=true) on a constructor parameter: same treatment
            // as record components — skip the regular FieldReader and collect the
            // inner's FieldReaders into a side table keyed by parameter index.
            if (annotation != null && annotation.unwrapped()) {
                if (paramTypes[i].isPrimitive()) {
                    throw new JSONException("@JSONField(unwrapped=true) on constructor param '"
                            + rawName + "': primitive parameter cannot be an unwrapped holder");
                }
                if (!annotation.defaultValue().isEmpty()) {
                    throw new JSONException("@JSONField(unwrapped=true) on constructor param '"
                            + rawName + "' does not support defaultValue — the holder is a POJO");
                }
                if (ctorUnwrapped == null) {
                    ctorUnwrapped = new ArrayList<>();
                }
                expandRecordUnwrapped(i, paramTypes[i], genericParamTypes[i], useJacksonAnnotation, ctorUnwrapped);
                if (annotation.required()) {
                    if (ctorRequiredIndices == null) {
                        ctorRequiredIndices = new ArrayList<>();
                        ctorRequiredNames = new ArrayList<>();
                    }
                    ctorRequiredIndices.add(i);
                    ctorRequiredNames.add(rawName);
                }
                continue;
            }

            JacksonAnnotationSupport.FieldInfo jacksonField = null;
            if (annotation == null && useJacksonAnnotation) {
                jacksonField = JacksonAnnotationSupport.getFieldInfo(field.getAnnotations());
            }

            String jsonName = resolveFieldName(rawName, annotation, naming);
            String[] alternateNames = annotation != null ? annotation.alternateNames() : new String[0];
            int ordinal = annotation != null ? annotation.ordinal() : 0;
            String defaultValue = annotation != null ? annotation.defaultValue() : "";
            boolean required = annotation != null && annotation.required();
            String format = (annotation != null && !annotation.format().isEmpty()) ? annotation.format() : null;
            Class<?> deserializeUsingClass = (annotation != null && annotation.deserializeUsing() != Void.class)
                    ? annotation.deserializeUsing() : null;
            String schema = (annotation != null && !annotation.schema().isEmpty()) ? annotation.schema() : null;

            if (jacksonField != null) {
                if (jsonName.equals(applyNamingStrategy(rawName, naming)) && !jacksonField.name().isEmpty()) {
                    jsonName = jacksonField.name();
                }
                if (alternateNames.length == 0 && jacksonField.alternateNames().length > 0) {
                    alternateNames = jacksonField.alternateNames();
                }
                if (ordinal == 0) {
                    ordinal = jacksonField.ordinal();
                }
                if (!required) {
                    required = jacksonField.required();
                }
                if (format == null && jacksonField.format() != null && !jacksonField.format().isEmpty()) {
                    format = jacksonField.format();
                }
                if (deserializeUsingClass == null && jacksonField.deserializeUsing() != null) {
                    deserializeUsingClass = jacksonField.deserializeUsing();
                }
            }

            fieldReaderList.add(new FieldReader(
                    jsonName, alternateNames,
                    TypeUtils.resolve(genericParamTypes[i], contextType), paramTypes[i],
                    ordinal, defaultValue, required,
                    field, null, format, deserializeUsingClass, schema, 0, useJacksonAnnotation
            ));
        }

        Collections.sort(fieldReaderList);

        FieldReader[] fieldReaders = fieldReaderList.toArray(new FieldReader[0]);
        FieldNameMatcher matcher = FieldNameMatcher.build(fieldReaders);

        Map<String, FieldReader> fieldReaderMap = HashMap.newHashMap(fieldReaders.length * 2);
        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fr = fieldReaders[i];
            fr.index = i;
            fieldReaderMap.put(fr.fieldName, fr);
            for (String alt : fr.alternateNames) {
                fieldReaderMap.putIfAbsent(alt, fr);
            }
        }

        FieldReaderCollection collection = new FieldReaderCollection(fieldReaders, matcher, fieldReaderMap, null, null);

        // Build parameter mapping: fieldReader index → constructor parameter index
        int[] paramMapping = new int[fieldReaders.length];
        for (int i = 0; i < fieldReaders.length; i++) {
            String name = fieldReaders[i].fieldName;
            for (int j = 0; j < instanceFields.size(); j++) {
                String resolvedName = applyNamingStrategy(instanceFields.get(j).getName(), naming);
                String rawFieldName = instanceFields.get(j).getName();
                if (resolvedName.equals(name) || rawFieldName.equals(name)) {
                    paramMapping[i] = j;
                    break;
                }
            }
        }

        int[] ctorRequiredIdx = ctorRequiredIndices == null ? null
                : ctorRequiredIndices.stream().mapToInt(Integer::intValue).toArray();
        String[] ctorRequiredNameArr = ctorRequiredNames == null ? null
                : ctorRequiredNames.toArray(new String[0]);
        Constructor<T> ctor = constructor;
        InstanceInvoker<T> invoker = values -> ctor.newInstance(values);
        return new RecordObjectReader<>(type, invoker, paramTypes, collection.fieldReaders,
                collection.fieldReaderMap, collection.matcher, paramTypes.length, paramMapping,
                ctorUnwrapped, ctorRequiredIdx, ctorRequiredNameArr);
    }

    /**
     * Holds the collected field reader metadata for a given type.
     * Used by both reflection-based and ASM-based ObjectReader creation.
     */
    static final class FieldReaderCollection {
        final FieldReader[] fieldReaders;
        final FieldNameMatcher matcher;
        final Map<String, FieldReader> fieldReaderMap;
        // One entry per JSON field name coming from an @JSONField(unwrapped=true)
        // nested POJO; holds the outer accessor + the inner FieldReader to delegate
        // into. Null when no unwrapped fields are declared on the type.
        final List<UnwrappedEntry> unwrappedEntries;
        // Holder chains marked @JSONField(unwrapped=true, required=true). Each chain
        // walks from the outer instance down to the required holder: a single-element
        // array for a direct holder (outer.field required), a multi-element array for
        // a nested required holder (outer.mid.leaf required — only enforced when the
        // earlier links are non-null, i.e. that mid was materialized by an incoming
        // key). Post-parse check runs in applyDefaults. Null when no such holders.
        final List<Field[]> requiredUnwrappedHolders;

        FieldReaderCollection(FieldReader[] fieldReaders, FieldNameMatcher matcher,
                              Map<String, FieldReader> fieldReaderMap,
                              List<UnwrappedEntry> unwrappedEntries,
                              List<Field[]> requiredUnwrappedHolders) {
            this.fieldReaders = fieldReaders;
            this.matcher = matcher;
            this.fieldReaderMap = fieldReaderMap;
            this.unwrappedEntries = unwrappedEntries;
            this.requiredUnwrappedHolders = requiredUnwrappedHolders;
        }
    }

    /**
     * Record / constructor-based equivalent of {@link UnwrappedEntry}. Because the
     * outer is immutable there's no holder field to write into — the flattened inner
     * keys accumulate into a scratch instance stored by the parser and then pass to
     * the canonical constructor at the correct parameter index.
     */
    static final class RecordUnwrappedEntry {
        static final Field[] EMPTY_CHAIN = new Field[0];
        static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

        final int componentIdx;
        final Class<?> innerClass;
        final Constructor<?> innerCtor; // pre-resolved no-arg ctor for innerClass; null if absent (reported at first hit)
        // Chain of intermediate holder Fields inside the component type when the
        // inner class itself has @JSONField(unwrapped=true). Empty for the direct
        // case. For `record Person(@unwrapped NestedName name, int age)` where
        // NestedName has `@unwrapped Handle handle`, a leaf entry for Handle.nick
        // carries intermediateChain=[NestedName.handle], classes=[Handle].
        final Field[] intermediateChain;
        final Class<?>[] intermediateClasses;
        final Constructor<?>[] intermediateCtors; // pre-resolved, setAccessible done at build
        final FieldReader innerReader;
        volatile ObjectReader<?> customReader;

        RecordUnwrappedEntry(int componentIdx, Class<?> innerClass, FieldReader innerReader) {
            this(componentIdx, innerClass, EMPTY_CHAIN, EMPTY_CLASSES, innerReader);
        }

        RecordUnwrappedEntry(int componentIdx, Class<?> innerClass,
                             Field[] intermediateChain, Class<?>[] intermediateClasses,
                             FieldReader innerReader) {
            this.componentIdx = componentIdx;
            this.innerClass = innerClass;
            this.intermediateChain = intermediateChain;
            this.intermediateClasses = intermediateClasses;
            this.innerReader = innerReader;
            // Pre-resolve constructors + setAccessible once so each parse-time hit
            // skips the reflective lookup and access-check overhead.
            Constructor<?> ic = null;
            try {
                ic = innerClass.getDeclaredConstructor();
                ic.setAccessible(true);
            } catch (NoSuchMethodException ignored) {
            }
            this.innerCtor = ic;
            Constructor<?>[] ctors = new Constructor<?>[intermediateClasses.length];
            for (int i = 0; i < intermediateClasses.length; i++) {
                try {
                    ctors[i] = intermediateClasses[i].getDeclaredConstructor();
                    ctors[i].setAccessible(true);
                } catch (NoSuchMethodException ignored) {
                }
                if (intermediateChain[i] != null) {
                    intermediateChain[i].setAccessible(true);
                }
            }
            this.intermediateCtors = ctors;
        }

        ObjectReader<?> resolveCustomReader() {
            if (innerReader.deserializeUsingClass == null) {
                return null;
            }
            ObjectReader<?> r = customReader;
            if (r != null) {
                return r;
            }
            try {
                r = (ObjectReader<?>) innerReader.deserializeUsingClass.getDeclaredConstructor().newInstance();
                customReader = r;
                return r;
            } catch (Exception e) {
                throw new JSONException("cannot instantiate deserializeUsing: "
                        + innerReader.deserializeUsingClass.getName(), e);
            }
        }
    }

    /**
     * Routing info for one JSON field name that maps into a (possibly nested) unwrapped
     * POJO. The holder chain preserves the intermediate @JSONField(unwrapped=true)
     * levels so a reader encountering a key like {@code "nick"} can walk
     * {@code Person.name → Name.handle → Handle.nick} in one step, constructing
     * missing intermediates on demand.
     */
    static final class UnwrappedEntry {
        final Field[] holderChain;       // [outerHolder, midHolder, ...] — index 0 is the direct parent field
        final Class<?>[] holderClasses;  // erased type of each holder, for no-arg construction
        final Constructor<?>[] holderCtors; // pre-resolved no-arg constructor per holder level; may contain nulls when resolution fails (reported at first parse hit)
        final FieldReader innerReader;   // FieldReader for the deepest target class's field
        volatile ObjectReader<?> customReader; // lazily-instantiated @JSONField(deserializeUsing=) instance

        UnwrappedEntry(Field[] holderChain, Class<?>[] holderClasses, FieldReader innerReader) {
            this.holderChain = holderChain;
            this.holderClasses = holderClasses;
            this.innerReader = innerReader;
            // Pre-resolve Constructor and setAccessible once so every parse-time
            // hit only pays the newInstance() invocation cost, not the per-call
            // access-check overhead of getDeclaredConstructor/setAccessible.
            Constructor<?>[] ctors = new Constructor<?>[holderClasses.length];
            for (int i = 0; i < holderClasses.length; i++) {
                try {
                    ctors[i] = holderClasses[i].getDeclaredConstructor();
                    ctors[i].setAccessible(true);
                } catch (NoSuchMethodException ignored) {
                    // Defer the no-arg-ctor requirement to the first actual hit so the
                    // error carries the field name context.
                }
                if (holderChain[i] != null) {
                    holderChain[i].setAccessible(true);
                }
            }
            this.holderCtors = ctors;
        }

        /**
         * Return the custom {@code @JSONField(deserializeUsing=)} ObjectReader for this
         * route's target field, lazily instantiated via the annotation's declared class.
         * Returns null if the field has no custom deserialiser. Cached on the entry so
         * subsequent writes through the same name reuse the instance.
         */
        ObjectReader<?> resolveCustomReader() {
            if (innerReader.deserializeUsingClass == null) {
                return null;
            }
            ObjectReader<?> r = customReader;
            if (r != null) {
                return r;
            }
            try {
                r = (ObjectReader<?>) innerReader.deserializeUsingClass.getDeclaredConstructor().newInstance();
                customReader = r;
                return r;
            } catch (Exception e) {
                throw new JSONException("cannot instantiate deserializeUsing: "
                        + innerReader.deserializeUsingClass.getName(), e);
            }
        }
    }

    /**
     * Collect field readers, build matcher, and assign indices for a given type.
     * Package-private so ObjectReaderCreatorASM can reuse this logic.
     */
    static FieldReaderCollection collectFieldReaders(Class<?> type) {
        return collectFieldReaders(type, type, null, false);
    }

    static FieldReaderCollection collectFieldReaders(Class<?> type, Class<?> mixIn) {
        return collectFieldReaders(type, type, mixIn, false);
    }

    static FieldReaderCollection collectFieldReaders(Class<?> type, Class<?> mixIn, boolean useJacksonAnnotation) {
        return collectFieldReaders(type, type, mixIn, useJacksonAnnotation);
    }

    static FieldReaderCollection collectFieldReaders(Class<?> type, java.lang.reflect.Type contextType,
                                                     Class<?> mixIn, boolean useJacksonAnnotation) {
        JSONType jsonType = type.getAnnotation(JSONType.class);
        NamingStrategy naming = jsonType != null ? jsonType.naming() : NamingStrategy.NoneStrategy;
        Set<String> includes = jsonType != null && jsonType.includes().length > 0
                ? Set.of(jsonType.includes()) : Set.of();
        Set<String> ignores = jsonType != null && jsonType.ignores().length > 0
                ? Set.of(jsonType.ignores()) : Set.of();

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
            }
        }

        List<FieldReader> fieldReaderList = new ArrayList<>();
        Set<String> processedNames = new HashSet<>();
        List<UnwrappedEntry> unwrappedEntries = null;
        // Holders annotated @JSONField(unwrapped=true, required=true). Tracked
        // separately because the unwrapped expansion skips creating a regular
        // FieldReader for these, so the normal applyDefaults required-field
        // enforcement won't see them. Null when no such holders declared.
        List<Field[]> requiredUnwrappedHolders = null;

        // Collect from fields
        for (Field field : getDeclaredFields(type)) {
            JSONField annotation = field.getAnnotation(JSONField.class);
            // Check mixin for field annotation
            if (annotation == null && mixIn != null) {
                annotation = findMixInFieldAnnotation(mixIn, field.getName(), JSONField.class);
            }
            if (annotation != null && !annotation.deserialize()) {
                continue;
            }

            // Jackson field annotations as fallback
            JacksonAnnotationSupport.FieldInfo jacksonField = null;
            if (annotation == null && useJacksonAnnotation) {
                jacksonField = JacksonAnnotationSupport.getFieldInfo(field.getAnnotations());
                if (jacksonField != null && jacksonField.noDeserialize()) {
                    continue;
                }
            }

            String rawName = field.getName();
            if (ignores.contains(rawName)) {
                continue;
            }
            if (!includes.isEmpty() && !includes.contains(rawName)) {
                continue;
            }

            boolean isPublic = Modifier.isPublic(field.getModifiers());
            boolean hasAnnotation = annotation != null || jacksonField != null;
            if (!isPublic && !hasAnnotation) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            // @JSONField(unwrapped=true) on deserialization: don't register this field
            // as a top-level reader. Instead, expand the inner POJO's FieldReaders and
            // route by their JSON names via the unwrappedEntries side table so the read
            // loop can lazily construct the inner and delegate field writes.
            // Placed AFTER the eligibility filters (ignores/includes/transient/visibility)
            // so an excluded holder field never contributes routes.
            if (annotation != null && annotation.unwrapped()) {
                // Primitive holder makes no sense — a primitive has no inner
                // bean layout to flatten. Silently falling through to a normal
                // FieldReader would leave the user believing their annotation
                // is honoured. Fail loud at construction time instead.
                if (field.getType().isPrimitive()) {
                    throw new JSONException("@JSONField(unwrapped=true) on " + type.getName() + "."
                            + field.getName() + ": primitive field cannot be an unwrapped holder");
                }
                if (!annotation.defaultValue().isEmpty()) {
                    // The holder is a POJO; a single-string defaultValue can't stand
                    // in for an arbitrary inner object. Reject explicitly so the user
                    // hears the constraint at construction time rather than the
                    // metadata being silently dropped.
                    throw new JSONException("@JSONField(unwrapped=true) on " + type.getName() + "."
                            + field.getName() + " does not support defaultValue — the holder is a POJO");
                }
                if (unwrappedEntries == null) {
                    unwrappedEntries = new ArrayList<>();
                }
                if (requiredUnwrappedHolders == null) {
                    requiredUnwrappedHolders = new ArrayList<>();
                }
                expandUnwrappedField(field, contextType, mixIn, useJacksonAnnotation, unwrappedEntries, processedNames,
                        requiredUnwrappedHolders);
                processedNames.add(field.getName());
                if (annotation.required()) {
                    field.setAccessible(true);
                    requiredUnwrappedHolders.add(new Field[]{field});
                }
                continue;
            }

            String jsonName = resolveFieldName(rawName, annotation, naming);
            String[] alternateNames = annotation != null ? annotation.alternateNames() : new String[0];
            int ordinal = annotation != null ? annotation.ordinal() : 0;
            String defaultValue = annotation != null ? annotation.defaultValue() : "";
            boolean required = annotation != null && annotation.required();
            String format = (annotation != null && !annotation.format().isEmpty()) ? annotation.format() : null;
            Class<?> deserializeUsingClass = (annotation != null && annotation.deserializeUsing() != Void.class)
                    ? annotation.deserializeUsing() : null;
            String schema = (annotation != null && !annotation.schema().isEmpty()) ? annotation.schema() : null;

            // Apply Jackson fallbacks
            if (jacksonField != null) {
                if (jsonName.equals(applyNamingStrategy(rawName, naming)) && !jacksonField.name().isEmpty()) {
                    jsonName = jacksonField.name();
                }
                if (alternateNames.length == 0 && jacksonField.alternateNames().length > 0) {
                    alternateNames = jacksonField.alternateNames();
                }
                if (ordinal == 0) {
                    ordinal = jacksonField.ordinal();
                }
                if (!required) {
                    required = jacksonField.required();
                }
                if (format == null && jacksonField.format() != null && !jacksonField.format().isEmpty()) {
                    format = jacksonField.format();
                }
                if (deserializeUsingClass == null && jacksonField.deserializeUsing() != null) {
                    deserializeUsingClass = jacksonField.deserializeUsing();
                }
            }

            fieldReaderList.add(new FieldReader(
                    jsonName, alternateNames,
                    TypeUtils.resolve(field.getGenericType(), contextType), field.getType(),
                    ordinal, defaultValue, required,
                    field, null, format, deserializeUsingClass, schema, 0, useJacksonAnnotation
            ));
            processedNames.add(rawName);
        }

        // Collect from setter methods
        for (Method method : type.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }

            JSONField annotation = method.getAnnotation(JSONField.class);
            // Check mixin for setter annotation
            String rawName = extractPropertyName(method);
            if (annotation == null && mixIn != null && rawName != null) {
                annotation = findMixInSetterAnnotation(mixIn, method, rawName, JSONField.class);
            }
            if (annotation != null && !annotation.deserialize()) {
                continue;
            }

            // Jackson setter annotations as fallback
            JacksonAnnotationSupport.FieldInfo jacksonField = null;
            if (annotation == null && useJacksonAnnotation) {
                jacksonField = JacksonAnnotationSupport.getFieldInfo(method.getAnnotations());
                if (jacksonField != null && jacksonField.noDeserialize()) {
                    continue;
                }
            }

            if (rawName == null && annotation == null && jacksonField == null) {
                continue;
            }
            if (rawName == null) {
                if (annotation != null) {
                    rawName = annotation.name();
                } else if (jacksonField != null && !jacksonField.name().isEmpty()) {
                    rawName = jacksonField.name();
                }
                if (rawName == null || rawName.isEmpty()) {
                    continue;
                }
            }

            if (processedNames.contains(rawName)) {
                continue;
            }
            if (ignores.contains(rawName)) {
                continue;
            }
            if (!includes.isEmpty() && !includes.contains(rawName)) {
                continue;
            }

            // Setter-backed @JSONField(unwrapped=true): expand into the side table
            // using the corresponding backing field as the holder. Without a backing
            // field we can't lazily read/write the inner POJO (a setter-only API
            // doesn't give us a getter path). Fall through to a normal field reader
            // when no such field exists — the user gets a regular setter and can
            // see the flattened keys land on the holder field directly at best.
            if (annotation != null && annotation.unwrapped()) {
                Class<?> paramType = method.getParameterTypes()[0];
                if (paramType.isPrimitive()) {
                    throw new JSONException("@JSONField(unwrapped=true) on setter "
                            + type.getName() + "." + method.getName()
                            + ": primitive parameter cannot be an unwrapped holder");
                }
                if (!annotation.defaultValue().isEmpty()) {
                    throw new JSONException("@JSONField(unwrapped=true) on setter "
                            + type.getName() + "." + method.getName()
                            + " does not support defaultValue — the holder is a POJO");
                }
                // Walk the class hierarchy — the holder field may live on a
                // superclass (common JavaBean shape: `class Child extends
                // Base { ... }` where Base declares the holder). Rejecting
                // inherited fields narrows the feature unnecessarily.
                Field holderField = findInstanceFieldInHierarchy(type, rawName);
                if (holderField != null) {
                    if (unwrappedEntries == null) {
                        unwrappedEntries = new ArrayList<>();
                    }
                    if (requiredUnwrappedHolders == null) {
                        requiredUnwrappedHolders = new ArrayList<>();
                    }
                    expandUnwrappedField(holderField, contextType, mixIn, useJacksonAnnotation, unwrappedEntries,
                            processedNames, requiredUnwrappedHolders);
                    processedNames.add(rawName);
                    if (annotation.required()) {
                        // Mirror the field-backed branch: register the backing
                        // field so applyDefaults validates it post-parse. Without
                        // this, @JSONField(unwrapped=true, required=true) on a
                        // setter silently became a no-op.
                        holderField.setAccessible(true);
                        requiredUnwrappedHolders.add(new Field[]{holderField});
                    }
                    continue;
                }
                // No backing field — the unwrapped semantics can't be lazily
                // implemented. Reject fast with a clear message rather than
                // silently falling back to a whole-object setter.
                throw new JSONException("@JSONField(unwrapped=true) on setter "
                        + type.getName() + "." + method.getName()
                        + " requires a backing field named '" + rawName + "' to be lazily constructed");
            }

            String jsonName = resolveFieldName(rawName, annotation, naming);
            String[] alternateNames = annotation != null ? annotation.alternateNames() : new String[0];
            int ordinal = annotation != null ? annotation.ordinal() : 0;
            String defaultValue = annotation != null ? annotation.defaultValue() : "";
            boolean required = annotation != null && annotation.required();
            String schema = (annotation != null && !annotation.schema().isEmpty()) ? annotation.schema() : null;

            // Apply Jackson fallbacks
            if (jacksonField != null) {
                if (jsonName.equals(applyNamingStrategy(rawName, naming)) && !jacksonField.name().isEmpty()) {
                    jsonName = jacksonField.name();
                }
                if (alternateNames.length == 0 && jacksonField.alternateNames().length > 0) {
                    alternateNames = jacksonField.alternateNames();
                }
                if (ordinal == 0) {
                    ordinal = jacksonField.ordinal();
                }
                if (!required) {
                    required = jacksonField.required();
                }
            }

            Parameter param = method.getParameters()[0];
            fieldReaderList.add(new FieldReader(
                    jsonName, alternateNames,
                    TypeUtils.resolve(param.getParameterizedType(), contextType), param.getType(),
                    ordinal, defaultValue, required,
                    null, method, null, null, schema, 0, useJacksonAnnotation
            ));
            processedNames.add(rawName);
        }

        Collections.sort(fieldReaderList);

        FieldReader[] fieldReaders = fieldReaderList.toArray(new FieldReader[0]);
        FieldNameMatcher matcher = FieldNameMatcher.build(fieldReaders);

        Map<String, FieldReader> fieldReaderMap = HashMap.newHashMap(fieldReaders.length * 2);
        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fr = fieldReaders[i];
            fr.index = i;
            fieldReaderMap.put(fr.fieldName, fr);
            for (String alt : fr.alternateNames) {
                fieldReaderMap.putIfAbsent(alt, fr);
            }
        }

        return new FieldReaderCollection(fieldReaders, matcher, fieldReaderMap, unwrappedEntries, requiredUnwrappedHolders);
    }

    /**
     * For an outer field annotated {@code @JSONField(unwrapped=true)}, resolve the
     * inner POJO's FieldReaders and register each under its JSON name in the
     * unwrapped side table. The outer field itself becomes the holder slot; the
     * inner FieldReaders target the inner's layout so {@code setObjectValue(inner, …)}
     * works unchanged.
     */
    private static void expandUnwrappedField(Field outerField, java.lang.reflect.Type outerContext,
                                             Class<?> mixIn, boolean useJacksonAnnotation,
                                             List<UnwrappedEntry> entries, Set<String> processedNames,
                                             List<Field[]> requiredUnwrappedHolders) {
        Class<?> innerClass = outerField.getType();
        // Guard rails: unwrapping only makes sense for regular bean POJOs. Reject
        // interfaces, abstracts, records, and structural types (Collection / Map /
        // array / enum) — their field layout isn't a set of flattenable properties,
        // so letting them through produces opaque downstream NPEs at the
        // collectFieldReaders step instead of a useful construction-time error.
        rejectNonPojoUnwrapHolder(innerClass, "@JSONField(unwrapped=true) on " + outerField.getName());
        outerField.setAccessible(true);

        Set<Class<?>> visited = UNWRAP_VISITED.get();
        boolean topEntry = (visited == null);
        if (topEntry) {
            visited = new HashSet<>();
            UNWRAP_VISITED.set(visited);
        }
        if (!visited.add(innerClass)) {
            if (topEntry) {
                UNWRAP_VISITED.remove();
            }
            throw new JSONException("Cyclic @JSONField(unwrapped=true) chain involving "
                    + innerClass.getName());
        }
        try {
            Field[] directChain = {outerField};
            Class<?>[] directClasses = {innerClass};

            // Honour a user-registered mix-in for the inner type so renamed properties,
            // alternate names, ignores/includes rules etc. apply to flattened inner
            // keys the same way they would when reading the inner as a top-level target.
            // Use the generic form of the outer field (e.g. Wrapper<Address>) as
            // contextType so inner fields declared with an outer-site type variable
            // (T value) resolve against the outer's type argument rather than erasing
            // to Object.
            Class<?> innerMixIn = resolveMixIn(innerClass);
            // Resolve the declared generic form (e.g. Wrapper<T>) against the
            // outer's own contextType so an inherited holder through a
            // parameterized parent — class Child extends Parent<Address>, where
            // Parent declares @JSONField(unwrapped=true) Wrapper<T> holder; —
            // still binds T to Address. Passing outerField.getGenericType()
            // alone would leave T unbound against the parent's TypeVariable.
            java.lang.reflect.Type contextType = outerContext != null
                    ? TypeUtils.resolve(outerField.getGenericType(), outerContext)
                    : outerField.getGenericType();
            FieldReaderCollection innerCollection = collectFieldReaders(innerClass, contextType, innerMixIn, useJacksonAnnotation);
            for (FieldReader innerFr : innerCollection.fieldReaders) {
                // Outer fields take precedence on name collision — skip any inner name
                // that has already been processed at the outer level.
                if (processedNames.contains(innerFr.fieldName)) {
                    continue;
                }
                entries.add(new UnwrappedEntry(directChain, directClasses, innerFr));
            }
            // Double-unwrap: if the inner POJO itself has @JSONField(unwrapped=true) fields,
            // its deep entries carry a holder chain rooted at the inner. Prepend the outer's
            // holder so the read loop can walk Person.name → Name.handle → Handle.nick in
            // one pass, lazily constructing each intermediate POJO on demand.
            if (innerCollection.unwrappedEntries != null) {
                for (UnwrappedEntry nested : innerCollection.unwrappedEntries) {
                    if (processedNames.contains(nested.innerReader.fieldName)) {
                        continue;
                    }
                    Field[] prependedChain = new Field[nested.holderChain.length + 1];
                    prependedChain[0] = outerField;
                    System.arraycopy(nested.holderChain, 0, prependedChain, 1, nested.holderChain.length);

                    Class<?>[] prependedClasses = new Class<?>[nested.holderClasses.length + 1];
                    prependedClasses[0] = innerClass;
                    System.arraycopy(nested.holderClasses, 0, prependedClasses, 1, nested.holderClasses.length);

                    entries.add(new UnwrappedEntry(prependedChain, prependedClasses, nested.innerReader));
                }
            }
            // Double-unwrap required enforcement: if the inner carried its own
            // required holders (e.g. @JSONField(unwrapped=true, required=true)
            // on a Mid→Leaf component), prepend the outer's holder so applyDefaults
            // walks outer→mid→leaf. The leaf is only required when every ancestor
            // materialised (at least one flattened ancestor key was present) — the
            // chain walk short-circuits on the first null ancestor.
            if (innerCollection.requiredUnwrappedHolders != null) {
                for (Field[] nestedChain : innerCollection.requiredUnwrappedHolders) {
                    Field[] prepended = new Field[nestedChain.length + 1];
                    prepended[0] = outerField;
                    System.arraycopy(nestedChain, 0, prepended, 1, nestedChain.length);
                    requiredUnwrappedHolders.add(prepended);
                }
            }
        } finally {
            visited.remove(innerClass);
            if (topEntry) {
                UNWRAP_VISITED.remove();
            }
        }
    }

    /**
     * Reject inner types that don't make sense as unwrap holders. An unwrapped
     * holder flattens the inner's bean fields into the outer's JSON shape — that
     * contract only applies to concrete POJO classes (records handled separately).
     * Collections, maps, enums, arrays, interfaces and abstracts aren't field
     * bags; letting them reach {@code collectFieldReaders} produces cryptic
     * downstream failures rather than a useful diagnostic.
     */
    private static void rejectNonPojoUnwrapHolder(Class<?> innerClass, String site) {
        // Check the more specific classifications first — an array has
        // Modifier.ABSTRACT, and a Collection-typed field is always an
        // interface, so a plain interface/abstract check would mis-label both.
        // Round-5 audit: extended to mirror the writer-side guard
        // (rejectNonPojoUnwrappedInner in ObjectWriterCreator). Previously a
        // holder typed as Object / String / boxed numeric / Optional would
        // pass the reader guard while the writer guard rejected it —
        // asymmetric, and the reader's "silent null" worse than the writer's
        // explicit rejection.
        String reason = null;
        if (innerClass.isArray()) {
            reason = "array";
        } else if (innerClass.isEnum()) {
            reason = "enum";
        } else if (Collection.class.isAssignableFrom(innerClass)) {
            reason = "Collection";
        } else if (Map.class.isAssignableFrom(innerClass)) {
            reason = "Map";
        } else if (innerClass.isInterface()) {
            reason = "interface";
        } else if (Modifier.isAbstract(innerClass.getModifiers())) {
            reason = "abstract class";
        } else if (JDKUtils.isRecord(innerClass)) {
            reason = "record";
        } else if (innerClass == Object.class) {
            reason = "Object";
        } else if (innerClass == String.class
                || innerClass == Integer.class || innerClass == Long.class
                || innerClass == Short.class || innerClass == Byte.class
                || innerClass == Double.class || innerClass == Float.class
                || innerClass == Boolean.class || innerClass == Character.class
                || innerClass == java.math.BigInteger.class
                || innerClass == java.math.BigDecimal.class
                || Number.class.isAssignableFrom(innerClass)) {
            reason = "scalar wrapper";
        } else if (java.util.Optional.class.isAssignableFrom(innerClass)
                || java.util.OptionalInt.class == innerClass
                || java.util.OptionalLong.class == innerClass
                || java.util.OptionalDouble.class == innerClass) {
            reason = "Optional wrapper";
        } else if (java.util.concurrent.atomic.AtomicReference.class.isAssignableFrom(innerClass)) {
            reason = "AtomicReference wrapper";
        }
        if (reason != null) {
            throw new JSONException(site + ": inner type " + innerClass.getName()
                    + " (" + reason + ") is not a concrete POJO and cannot be unwrapped");
        }
    }

    // ==================== Unwrapped map-fallback helpers ====================

    /**
     * Convert an already-parsed value (Map / Number / String / etc. from a JSONObject or
     * record-generic map-fallback path) to the inner field's target type, honouring any
     * {@code @JSONField(deserializeUsing=)} declared on the inner field. When a custom
     * reader is present, serialise the value back to JSON and feed it to the custom
     * reader's streaming entry so it sees the same shape it would in the live-parser path.
     * Without this detour the JSONObject / record-map paths silently fall through to
     * {@code convertValue}, producing divergent values depending on entry point.
     */
    static Object applyUnwrappedValueFromMap(UnwrappedEntry entry, Object rawValue, long features) {
        try {
            ObjectReader<?> custom = entry.resolveCustomReader();
            if (custom == null) {
                return entry.innerReader.convertValue(rawValue);
            }
            // Do NOT short-circuit on rawValue==null. The parser path
            // (writeUnwrappedValue) always hands the custom reader the JSON token
            // stream even when the literal is null, so a deserializeUsing that
            // maps null to a sentinel (Optional.empty, a NOT_SET singleton, etc.)
            // must get the same chance here. Serialise null → "null" so the
            // custom reader sees a well-formed JSON token either way.
            String json = com.alibaba.fastjson3.JSON.toJSONString(rawValue);
            try (JSONParser p = JSONParser.of(json)) {
                return custom.readObject(p, entry.innerReader.fieldType, entry.innerReader.fieldName, features);
            }
        } catch (RuntimeException e) {
            // Wrap to match the live-parser writeUnwrappedValue wrap pattern.
            throw com.alibaba.fastjson3.JSONException.wrapWithPath(e, entry.innerReader.fieldName);
        }
    }

    /** Record-side variant of {@link #applyUnwrappedValueFromMap}. */
    static Object applyRecordUnwrappedValueFromMap(RecordUnwrappedEntry entry, Object rawValue, long features) {
        try {
            ObjectReader<?> custom = entry.resolveCustomReader();
            if (custom == null) {
                return entry.innerReader.convertValue(rawValue);
            }
            String json = com.alibaba.fastjson3.JSON.toJSONString(rawValue);
            try (JSONParser p = JSONParser.of(json)) {
                return custom.readObject(p, entry.innerReader.fieldType, entry.innerReader.fieldName, features);
            }
        } catch (RuntimeException e) {
            throw com.alibaba.fastjson3.JSONException.wrapWithPath(e, entry.innerReader.fieldName);
        }
    }

    // ==================== Internal ObjectReader implementation ====================

    /**
     * Create an ObjectReader for a sealed class/interface (or a non-sealed abstract
     * parent / interface with {@code @JSONType(seeAlso=...)}). Uses the typeKey from
     * the supplied {@code jsonType} (resolved from the target class or its mix-in)
     * — {@code "@type"} by default — and the typeName from each subtype's
     * {@code @JSONType}, falling back to simple class name. Built once at creation
     * time so the hot path is zero-overhead dispatch.
     */
    @SuppressWarnings("unchecked")
    private static <T> ObjectReader<T> createSealedReader(Class<T> sealedType, JSONType jsonType, Class<?> mixIn) {
        String typeKey = (jsonType != null && !jsonType.typeKey().isEmpty()) ? jsonType.typeKey() : "@type";

        // Auto-discover permitted subtypes (includes seeAlso for manual overrides).
        // getPermittedSubclasses() returns null for non-sealed types (this path is
        // also used for @JSONType(seeAlso) on abstract/interface types without `sealed`).
        Class<?>[] permitted = sealedType.isSealed()
                ? sealedType.getPermittedSubclasses()
                : new Class<?>[0];
        Class<?>[] seeAlso = (jsonType != null) ? jsonType.seeAlso() : new Class<?>[0];

        // Build typeName → subtype map, eagerly create ObjectReaders
        Map<String, ObjectReader<?>> readerMap = new LinkedHashMap<>();
        for (Class<?> sub : permitted) {
            registerSubtype(sub, mixIn, readerMap);
        }
        for (Class<?> sub : seeAlso) {
            registerSubtype(sub, mixIn, readerMap);
        }

        // Single-subtype shortcut: reader used when typeKey is absent
        ObjectReader<?> singleReader = (permitted.length == 1 && seeAlso.length == 0)
                ? readerMap.values().iterator().next() : null;

        return (ObjectReader<T>) (JSONParser parser, java.lang.reflect.Type fieldType, Object fieldName, long features) -> {
            // Handle null literal
            if (parser.readNull()) {
                return null;
            }

            // Parse as JSONObject to inspect the type discriminator
            com.alibaba.fastjson3.JSONObject jsonObj = parser.readObject();

            String typeName = jsonObj.getString(typeKey);
            ObjectReader<?> subReader = (typeName != null) ? readerMap.get(typeName) : null;

            if (subReader == null) {
                subReader = singleReader;
            }

            if (subReader == null) {
                throw new com.alibaba.fastjson3.JSONException(
                        "cannot determine subtype, unknown type discriminator value '"
                                + typeName + "'");
            }

            // Fast path: read directly from JSONObject (no byte[] round-trip).
            // Pass the typeKey so the sub-reader's strict-unknown check skips it —
            // the discriminator is not a "real" property of the subtype.
            if (subReader instanceof ReflectionObjectReader<?> reflectReader) {
                return (T) reflectReader.readFromJSONObject(jsonObj, features, typeKey);
            }
            // Fallback for ASM-generated readers
            byte[] bytes = com.alibaba.fastjson3.JSON.toJSONBytes(jsonObj);
            try (JSONParser sub = JSONParser.of(bytes)) {
                return (T) subReader.readObject(sub, sealedType, null, features);
            }
        };
    }

    /**
     * Create an ObjectReader for Jackson @JsonTypeInfo + @JsonSubTypes polymorphic deserialization.
     * Same strategy as createSealedReader but uses Jackson-provided subtype mappings.
     */
    @SuppressWarnings("unchecked")
    private static <T> ObjectReader<T> createJacksonPolymorphicReader(
            Class<T> baseType, Class<?> mixIn,
            String typeKey, Map<String, Class<?>> subTypes) {
        Map<String, ObjectReader<?>> readerMap = new LinkedHashMap<>();
        for (Map.Entry<String, Class<?>> entry : subTypes.entrySet()) {
            // Pass useJacksonAnnotation=true so subtypes also get Jackson annotation processing
            ObjectReader<?> reader = createObjectReader(entry.getValue(), mixIn, true);
            if (reader != null) {
                readerMap.put(entry.getKey(), reader);
            }
        }

        return (ObjectReader<T>) (JSONParser parser, java.lang.reflect.Type fieldType, Object fieldName, long features) -> {
            if (parser.readNull()) {
                return null;
            }
            com.alibaba.fastjson3.JSONObject jsonObj = parser.readObject();
            String typeName = jsonObj.getString(typeKey);
            ObjectReader<?> subReader = (typeName != null) ? readerMap.get(typeName) : null;
            if (subReader == null) {
                throw new com.alibaba.fastjson3.JSONException(
                        "cannot determine subtype, unknown type discriminator value '"
                                + typeName + "'");
            }
            // Fast path: read directly from JSONObject (no byte[] round-trip).
            // Pass the typeKey so the sub-reader's strict-unknown check skips it.
            if (subReader instanceof ReflectionObjectReader<?> reflectReader) {
                return (T) reflectReader.readFromJSONObject(jsonObj, features, typeKey);
            }
            // Fallback for ASM-generated readers
            byte[] bytes = com.alibaba.fastjson3.JSON.toJSONBytes(jsonObj);
            try (JSONParser sub = JSONParser.of(bytes)) {
                return (T) subReader.readObject(sub, baseType, null, features);
            }
        };
    }

    private static void registerSubtype(Class<?> sub, Class<?> mixIn, Map<String, ObjectReader<?>> readerMap) {
        JSONType subJsonType = sub.getAnnotation(JSONType.class);
        String typeName = (subJsonType != null && !subJsonType.typeName().isEmpty())
                ? subJsonType.typeName() : sub.getSimpleName();
        if (!readerMap.containsKey(typeName)) {
            readerMap.put(typeName, createObjectReader(sub, mixIn));
        }
    }

    private static final class ReflectionObjectReader<T> implements ObjectReader<T> {
        private final Class<T> objectClass;
        private final Constructor<T> constructor;
        private final FieldReader[] fieldReaders;
        private final Map<String, FieldReader> fieldReaderMap;
        private final FieldNameMatcher matcher;
        private final boolean useUnsafeAlloc;
        private final Method anySetterMethod; // nullable
        private final String anySetterTypeKey; // nullable — type-discriminator key to exclude from anySetter routing
        private final JSONSchema typeSchema; // nullable, from @JSONType(schema=)
        private final boolean hasDefaultsOrRequired; // skip applyDefaults when false

        // @JSONField(unwrapped=true) routing: inner-POJO field names → (outer holder, inner reader).
        // Null when the type declares no unwrapped fields (the common case — zero overhead).
        private final java.util.Map<String, UnwrappedEntry> unwrappedByName;

        // Required unwrapped holder chains. Each chain walks from the outer instance
        // to a holder that must be non-null after parse — single-element for a direct
        // required holder, multi-element when a nested inner declared the requirement
        // (outer.mid.leaf required → chain = [mid, leaf]). Nested chains only fire
        // when every ancestor materialised. Null when no such holders — hot path empty.
        private final Field[][] requiredUnwrappedHolders;

        // Pre-resolved ObjectReaders for POJO and List element types
        // Lazily initialized on first use
        private volatile boolean fieldReadersResolved;
        private ObjectReader<?>[] fieldObjectReaders;
        private ObjectReader<?>[] fieldElementReaders;

        // Pre-encoded field headers for fast matching: '"fieldName":' as bytes
        private byte[][] fieldHeaders;

        ReflectionObjectReader(
                Class<T> objectClass,
                Constructor<T> constructor,
                FieldReader[] fieldReaders,
                Map<String, FieldReader> fieldReaderMap,
                FieldNameMatcher matcher,
                boolean useUnsafeAlloc,
                Method anySetterMethod,
                String anySetterTypeKey,
                JSONSchema typeSchema,
                List<UnwrappedEntry> unwrappedEntries,
                List<Field[]> requiredUnwrappedHolders
        ) {
            this.objectClass = objectClass;
            this.constructor = constructor;
            this.fieldReaders = fieldReaders;
            this.fieldReaderMap = fieldReaderMap;
            this.matcher = matcher;
            this.useUnsafeAlloc = useUnsafeAlloc;
            this.anySetterMethod = anySetterMethod;
            this.anySetterTypeKey = anySetterTypeKey;
            this.typeSchema = typeSchema;
            if (unwrappedEntries != null && !unwrappedEntries.isEmpty()) {
                java.util.Map<String, UnwrappedEntry> map = HashMap.newHashMap(unwrappedEntries.size() * 2);
                for (UnwrappedEntry ue : unwrappedEntries) {
                    UnwrappedEntry prev = map.putIfAbsent(ue.innerReader.fieldName, ue);
                    if (prev != null && prev != ue) {
                        throw new JSONException("ambiguous unwrapped field name '" + ue.innerReader.fieldName
                                + "' in " + objectClass.getName()
                                + " — multiple unwrapped holders register the same inner field");
                    }
                    for (String alt : ue.innerReader.alternateNames) {
                        UnwrappedEntry prevAlt = map.putIfAbsent(alt, ue);
                        if (prevAlt != null && prevAlt != ue) {
                            throw new JSONException("ambiguous unwrapped alternate name '" + alt
                                    + "' in " + objectClass.getName());
                        }
                    }
                }
                this.unwrappedByName = map;
            } else {
                this.unwrappedByName = null;
            }
            this.requiredUnwrappedHolders = (requiredUnwrappedHolders == null || requiredUnwrappedHolders.isEmpty())
                    ? null : requiredUnwrappedHolders.toArray(new Field[0][]);
            // Pre-compute: skip applyDefaults if no fields have required or defaultValue
            // AND no unwrapped holder is required-flagged.
            boolean hasDR = this.requiredUnwrappedHolders != null;
            if (!hasDR) {
                for (FieldReader fr : fieldReaders) {
                    if (fr.required || (fr.defaultValue != null && !fr.defaultValue.isEmpty())) {
                        hasDR = true;
                        break;
                    }
                }
            }
            this.hasDefaultsOrRequired = hasDR;
        }

        @Override
        public ObjectReader<?> getItemReader(int fieldIndex) {
            ensureFieldReaders();
            ObjectReader<?>[] e = this.fieldElementReaders;
            if (e == null || fieldIndex < 0 || fieldIndex >= e.length) {
                return null;
            }
            return e[fieldIndex];
        }

        private void ensureFieldReaders() {
            if (fieldReadersResolved) {
                return;
            }
            synchronized (this) {
                if (fieldReadersResolved) {
                    return;
                }
                int len = fieldReaders.length;
                ObjectReader<?>[] objReaders = new ObjectReader<?>[len];
                ObjectReader<?>[] elemReaders = new ObjectReader<?>[len];
                for (int i = 0; i < len; i++) {
                    FieldReader fr = fieldReaders[i];
                    Class<?> fc = fr.fieldClass;
                    // Custom deserializeUsing takes priority
                    if (fr.deserializeUsingClass != null) {
                        try {
                            objReaders[i] = (ObjectReader<?>) fr.deserializeUsingClass
                                    .getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new JSONException("cannot instantiate deserializeUsing: "
                                    + fr.deserializeUsingClass.getName(), e);
                        }
                    } else if (fr.formatter != null) {
                        // Field has custom @JSONField(format=...) annotation
                        // Skip BuiltinCodecs so FieldReader.convertValue() uses the formatter
                        objReaders[i] = null;
                    } else if (fr.typeTag == FieldReader.TAG_GENERIC) {
                        // For POJO fields (not basic types), get ObjectReader.
                        // Prefer the resolved fieldType over the erased fieldClass so
                        // inherited TypeVariables read as their concrete type — e.g.,
                        // `class Child extends Parent<Bean>` with `T value` field has
                        // fieldClass=Object but fieldType=Bean after resolution.
                        Class<?> target = (fr.fieldType instanceof Class<?> resolved)
                                ? resolved : fc;
                        // Skip JSONObject / JSONArray fields (and arrays of them): leave
                        // objReaders[i] null so the default-branch fallback uses
                        // parser.readAny() + convertValue, which yields a populated
                        // JSONObject / JSONArray. The auto-built reflection POJO reader
                        // assumes `{` and either rejects array input outright or silently
                        // returns an empty container.
                        if (isJsonNodeOrJsonNodeArray(target)) {
                            continue;
                        }
                        ObjectReader<?> r = com.alibaba.fastjson3.BuiltinCodecs.getReader(target);
                        if (r == null) {
                            // Skip interfaces, abstract classes, and enums - they don't have constructors
                            if (!target.isInterface() && !target.isEnum()
                                    && !java.lang.reflect.Modifier.isAbstract(target.getModifiers())) {
                                r = createObjectReader(target);
                            }
                        }
                        if (r != null) {
                            objReaders[i] = r;
                        }
                    }
                    // For Collection<E> fields where E is a plain Class: cache an
                    // element ObjectReader for the fast path. Nested generics
                    // (List<List<X>>, List<Map<K,V>>, List<? extends X>) go through
                    // parser.read(fieldType) at parse time instead — caching a
                    // class-keyed reader for a non-Class elementType wouldn't make
                    // sense. Skip Object.class (from raw List / List<?>) because
                    // auto-creating a reader for Object produces a useless empty
                    // reader that fails on non-object elements.
                    if (fr.elementType instanceof Class<?> elemC
                            && elemC != String.class && elemC != Object.class
                            && !isJsonNodeOrJsonNodeArray(elemC)) {
                        ObjectReader<?> r = com.alibaba.fastjson3.BuiltinCodecs.getReader(elemC);
                        if (r == null) {
                            if (!elemC.isInterface() && !java.lang.reflect.Modifier.isAbstract(elemC.getModifiers())) {
                                r = createObjectReader(elemC);
                            }
                        }
                        if (r != null) {
                            elemReaders[i] = r;
                        }
                    }
                }
                this.fieldElementReaders = elemReaders;
                this.fieldObjectReaders = objReaders;
                this.fieldReadersResolved = true;
            }
        }

        @Override
        public T readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
            ensureFieldReaders();

            if (parser instanceof JSONParser.UTF8 utf8) {
                return readObjectUTF8Impl(utf8, features);
            }
            return readObjectGeneric(parser, features);
        }

        @Override
        public T readObjectUTF8(JSONParser.UTF8 utf8, long features) {
            ensureFieldReaders();
            return readObjectUTF8Impl(utf8, features);
        }

        private T readObjectUTF8Impl(JSONParser.UTF8 utf8, long features) {
            int peek = utf8.skipWSAndPeek();
            if (peek == 'n') {
                if (utf8.readNull()) {
                    return null;
                }
            }
            if (peek != '{') {
                throw new JSONException("expected '{' at offset " + utf8.getOffset());
            }
            utf8.advance(1);

            T instance = createInstance(features);
            long fieldSetMask = 0;

            peek = utf8.skipWSAndPeek();
            if (peek == '}') {
                utf8.advance(1);
                applyDefaults(instance, fieldSetMask);
                validateTypeSchema(instance);
                return instance;
            }

            // Cache field readers in locals
            final FieldNameMatcher m = this.matcher;
            final ObjectReader<?>[] objReaders = this.fieldObjectReaders;
            final ObjectReader<?>[] elemReaders = this.fieldElementReaders;
            final boolean errorOnUnknown = (features & ReadFeature.ErrorOnUnknownProperties.mask) != 0;
            final boolean usePLHV = m.strategy == FieldNameMatcher.STRATEGY_PLHV;

            fieldSetMask = readFieldsLoop(utf8, instance, m, objReaders, elemReaders,
                    errorOnUnknown, usePLHV, features, fieldSetMask);

            applyDefaults(instance, fieldSetMask);
            validateTypeSchema(instance);
            return instance;
        }

        /**
         * Core field-reading loop, extracted for JIT compilation as a standalone hot method.
         *
         * <p>This method manages {@code off} as a local variable (CPU register) and only
         * syncs to {@code utf8.offset} when calling non-inlined sub-methods (POJO, List, etc.).
         * Field header matching and separator checking are fully inlined to avoid heap
         * access for {@code this.offset} on the hot path.
         *
         * <p>WARNING: 不可将此循环合并回 readObjectUTF8Impl。
         * 合并后方法体过大，JIT 无法内联子方法，实测导致性能大幅下降。
         */
        private long readFieldsLoop(JSONParser.UTF8 utf8, Object instance,
                                    FieldNameMatcher m, ObjectReader<?>[] objReaders,
                                    ObjectReader<?>[] elemReaders, boolean errorOnUnknown,
                                    boolean usePLHV, long features, long fieldSetMask) {
            final FieldReader[] frs = this.fieldReaders;
            final int frLen = frs.length;
            final byte[] b = utf8.getBytes();
            final int end = utf8.getEnd();
            int off = utf8.getOffset();
            int nextExpected = 0;

            for (;;) {
                FieldReader reader = null;
                int fieldStartOff = off; // save for potential anySetter re-read

                // Fast path: ordered field speculation — long-word header matching
                if (nextExpected < frLen) {
                    FieldReader candidate = frs[nextExpected];
                    // Skip whitespace
                    while (off < end && b[off] <= ' ') {
                        off++;
                    }
                    int hdrLen = candidate.fieldNameHeader.length;
                    if (off + hdrLen <= end) {
                        // Compare using pre-computed long words (8 bytes at a time).
                        // Bound on `end` not `b.length`: the parser may operate on a
                        // wrapped sub-range of bytes (UTF8(bytes, offset, length)),
                        // so reading past `end` would compare against arbitrary
                        // garbage and can spuriously match.
                        boolean match;
                        if (hdrLen <= 8 && off + 8 <= end) {
                            long w = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(b, off);
                            match = (w & candidate.hdrMask0) == candidate.hdrWord0;
                        } else if (hdrLen <= 16 && off + 16 <= end) {
                            long w0 = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(b, off);
                            long w1 = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(b, off + 8);
                            match = w0 == candidate.hdrWord0
                                    && (w1 & candidate.hdrMask1) == candidate.hdrWord1;
                        } else {
                            // Fallback for very long field names (> 13 chars)
                            byte[] hdr = candidate.fieldNameHeader;
                            match = true;
                            for (int i = 0; i < hdrLen; i++) {
                                if (b[off + i] != hdr[i]) {
                                    match = false;
                                    break;
                                }
                            }
                        }
                        if (match) {
                            off += hdrLen;
                            while (off < end && b[off] <= ' ') {
                                off++;
                            }
                            reader = candidate;
                            nextExpected++;
                        }
                    }
                }

                // Slow path: hash-based matching (sync offset for method calls)
                if (reader == null) {
                    utf8.setOffset(off);
                    long hash = usePLHV ? utf8.readFieldNameHashPLHV() : utf8.readFieldNameHash(m);
                    off = utf8.getOffset();
                    reader = m.matchFlat(hash);
                    if (reader != null) {
                        nextExpected = reader.index + 1;
                    } else {
                        nextExpected = 0;
                    }
                }

                if (reader != null) {
                    int fi = reader.index;
                    // Switch dispatch: compiled to tableswitch (O(1) jump)
                    // vs the previous if/else chain (O(N) worst case).
                    // For 22-field POJOs with 8+ type tags, the tableswitch
                    // is faster on average and more JIT-friendly.
                    if (off >= end) {
                        // Truncated input: header speculation matched `"name":` and
                        // ws-skipped to end with no value byte. The case branches
                        // below read b[off] / b[off+3] without per-branch guards.
                        throw new JSONException("unexpected end of input in " + objectClass.getName());
                    }
                    try {
                    switch (reader.typeTag) {
                    case FieldReader.TAG_STRING:
                        if (fi >= 0 && objReaders != null && objReaders[fi] != null) {
                            // custom deserializeUsing
                            utf8.setOffset(off);
                            readAndSetFieldUTF8Inline(utf8, instance, reader, fi, features, objReaders, elemReaders);
                            off = utf8.getOffset();
                        } else if (b[off] == '"') {
                            off = utf8.readStringOff(off, instance, reader);
                        } else if (off + 3 < end && b[off] == 'n' && b[off + 1] == 'u' && b[off + 2] == 'l' && b[off + 3] == 'l') {
                            off += 4;
                        } else if (b[off] == '-' || (b[off] >= '0' && b[off] <= '9')) {
                            utf8.setOffset(off);
                            reader.setObjectValue(instance, readNumberString(utf8));
                            off = utf8.getOffset();
                        } else if (b[off] == 't' || b[off] == 'f') {
                            utf8.setOffset(off);
                            reader.setObjectValue(instance, Boolean.toString(utf8.readBoolean()));
                            off = utf8.getOffset();
                        } else {
                            utf8.setOffset(off);
                            reader.setObjectValue(instance, utf8.readStringDirect());
                            off = utf8.getOffset();
                        }
                        break;
                    case FieldReader.TAG_INT:
                        off = utf8.readIntOff(off, instance, reader);
                        break;
                    case FieldReader.TAG_LONG:
                        off = utf8.readLongOff(off, instance, reader);
                        break;
                    case FieldReader.TAG_DOUBLE:
                        off = utf8.readDoubleOff(off, instance, reader);
                        break;
                    case FieldReader.TAG_BOOLEAN:
                        off = utf8.readBooleanOff(off, instance, reader);
                        break;
                    case FieldReader.TAG_STRING_ARRAY:
                        if (b[off] == '[') {
                            utf8.setOffset(off);
                            reader.setObjectValue(instance, utf8.readStringArrayInline());
                            off = utf8.getOffset();
                            break;
                        }
                        // fall through to default
                    case FieldReader.TAG_LONG_ARRAY:
                        if (reader.typeTag == FieldReader.TAG_LONG_ARRAY && b[off] == '[') {
                            utf8.setOffset(off);
                            reader.setObjectValue(instance, utf8.readLongArrayInline());
                            off = utf8.getOffset();
                            break;
                        }
                        // fall through to default
                    case FieldReader.TAG_ENUM:
                        if (reader.typeTag == FieldReader.TAG_ENUM && b[off] == '"'
                                && (fi < 0 || objReaders == null || objReaders[fi] == null)) {
                            off = utf8.readStringOff(off, instance, reader);
                            break;
                        }
                        // fall through to default
                    default:
                        utf8.setOffset(off);
                        readAndSetFieldUTF8Inline(utf8, instance, reader, fi, features, objReaders, elemReaders);
                        off = utf8.getOffset();
                        break;
                    }
                    } catch (RuntimeException e) {
                        // Tag the in-flight field so a nested parse failure reports
                        // "expected '{' at offset N (path: outer.address.city)".
                        throw JSONException.wrapWithPath(e, reader.fieldName);
                    }
                    if (fi >= 0 && fi < 64) {
                        fieldSetMask |= (1L << fi);
                    }
                } else {
                    boolean handled = false;
                    if (unwrappedByName != null) {
                        // Re-read field name to look up in the @JSONField(unwrapped=true)
                        // side table. A hit delegates the value write to the inner POJO
                        // (lazily constructed on first hit and stashed on the outer holder).
                        utf8.setOffset(fieldStartOff);
                        String fname = utf8.readFieldName();
                        UnwrappedEntry entry = unwrappedByName.get(fname);
                        if (entry != null) {
                            Object inner = ensureUnwrappedTarget(instance, entry);
                            writeUnwrappedValue(utf8, inner, entry, features);
                            off = utf8.getOffset();
                            handled = true;
                        } else {
                            // Miss — reset so the unknown-property branches below re-read
                            // from fieldStartOff.
                            utf8.setOffset(fieldStartOff);
                        }
                    }
                    if (!handled) {
                        if (anySetterMethod != null) {
                            // Re-read field name from saved offset (miss path is inherently slow)
                            utf8.setOffset(fieldStartOff);
                            String fname = utf8.readFieldName();
                            Object fvalue = utf8.readAny();
                            off = utf8.getOffset();
                            // Skip the type-discriminator key: when the writer
                            // emits "@type" (via @JSONType(typeName=) or
                            // WriteClassName), routing it to anySetter causes
                            // re-write to duplicate the key.
                            if (anySetterTypeKey != null && anySetterTypeKey.equals(fname)) {
                                // value already consumed above; drop it
                            } else {
                                try {
                                    anySetterMethod.invoke(instance, fname, fvalue);
                                } catch (Exception e) {
                                    throw new JSONException("anySetter error for '" + fname + "'", e);
                                }
                            }
                        } else if (errorOnUnknown) {
                            throw new JSONException("unknown property in " + objectClass.getName());
                        } else {
                            utf8.setOffset(off);
                            utf8.skipValue();
                            off = utf8.getOffset();
                        }
                    }
                }

                // Inline separator check — avoid readFieldSeparator heap access
                while (off < end && b[off] <= ' ') {
                    off++;
                }
                if (off >= end) {
                    throw new JSONException("unexpected end of input in " + objectClass.getName());
                }
                if (b[off] == ',') {
                    off++;
                    continue;
                }
                if (b[off] == '}') {
                    off++;
                    break;
                }
                throw new JSONException("expected ',' or '}' in " + objectClass.getName());
            }
            utf8.setOffset(off);
            return fieldSetMask;
        }

        /**
         * Read and assign a value for an @JSONField(unwrapped=true) inner field. Honours
         * @JSONField(deserializeUsing=...) declared on the inner field so custom per-field
         * deserialisers participate even when the field surfaces as a flattened key at the
         * outer level. Fields without a custom deserialiser go through
         * {@code readAny} + {@code convertValue} — sufficient for primitives, enums, and
         * nested POJOs (convertValue handles Map→POJO via JSON round-trip).
         */
        private static void writeUnwrappedValue(JSONParser parser, Object inner, UnwrappedEntry entry, long features) {
            FieldReader innerFr = entry.innerReader;
            try {
                ObjectReader<?> custom = entry.resolveCustomReader();
                if (custom != null) {
                    Object v = custom.readObject(parser, innerFr.fieldType, innerFr.fieldName, features);
                    innerFr.setFieldValue(inner, v);
                    return;
                }
                Object fvalue = parser.readAny();
                Object converted = innerFr.convertValue(fvalue);
                innerFr.setFieldValue(inner, converted);
            } catch (RuntimeException e) {
                // Mirror the regular-field-path wrapWithPath: the unwrapped
                // reader was previously letting CCE / NFE propagate raw,
                // which the fuzz target {@code fuzzParseUnwrapped} flagged
                // as an unexpected exception type leaking from a typed
                // parse call.
                throw com.alibaba.fastjson3.JSONException.wrapWithPath(e, innerFr.fieldName);
            }
        }

        /**
         * Walk the holder chain, lazily constructing each intermediate POJO as needed.
         * Subsequent hits on fields routed through the same chain return the cached
         * instance so multiple JSON keys collapse into a single inner-type graph.
         */
        private Object ensureUnwrappedTarget(Object outer, UnwrappedEntry entry) {
            Object target = outer;
            Field[] chain = entry.holderChain;
            Class<?>[] classes = entry.holderClasses;
            Constructor<?>[] ctors = entry.holderCtors;
            for (int i = 0; i < chain.length; i++) {
                Field holder = chain[i];
                try {
                    Object next = holder.get(target);
                    if (next == null) {
                        Constructor<?> ctor = ctors[i];
                        if (ctor == null) {
                            // Entry build detected no no-arg ctor; surface the real shape
                            // of the user error here so the exception carries the field name.
                            throw new JSONException("@JSONField(unwrapped=true) requires "
                                    + classes[i].getName() + " to declare a no-arg constructor"
                                    + " (cannot lazily construct via the unwrapped reader)");
                        }
                        next = ctor.newInstance();
                        holder.set(target, next);
                    }
                    target = next;
                } catch (ReflectiveOperationException e) {
                    throw new JSONException("cannot construct unwrapped inner " + classes[i].getName()
                            + " for field '" + holder.getName() + "': " + e.getMessage(), e);
                }
            }
            return target;
        }

        private T readObjectGeneric(JSONParser parser, long features) {
            parser.skipWS();
            if (parser.readNull()) {
                return null;
            }
            if (parser.charAt(parser.getOffset()) != '{') {
                throw new JSONException("expected '{' at offset " + parser.getOffset());
            }
            parser.advance(1);

            T instance = createInstance(features);
            boolean errorOnUnknown = (features & ReadFeature.ErrorOnUnknownProperties.mask) != 0;
            long fieldSetMask = 0;

            parser.skipWS();
            if (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) == '}') {
                parser.advance(1);
                applyDefaults(instance, fieldSetMask);
                validateTypeSchema(instance);
                return instance;
            }

            for (;;) {
                int fieldStart = parser.getOffset();
                long hash = parser.readFieldNameHash(matcher);
                FieldReader reader = matcher.match(hash);

                if (reader != null) {
                    int fi = reader.index;
                    try {
                        readAndSetFieldGeneric(parser, instance, reader, features);
                    } catch (RuntimeException e) {
                        throw JSONException.wrapWithPath(e, reader.fieldName);
                    }
                    if (fi >= 0 && fi < 64) {
                        fieldSetMask |= (1L << fi);
                    }
                } else {
                    boolean handled = false;
                    if (unwrappedByName != null) {
                        // Re-read the field name and consult the unwrapped side table.
                        // On hit, lazily walk the holder chain and route the value to
                        // the deepest inner's FieldReader. On miss, fall through to the
                        // pre-existing unknown-property handling.
                        parser.setOffset(fieldStart);
                        String fname = parser.readFieldName();
                        UnwrappedEntry entry = unwrappedByName.get(fname);
                        if (entry != null) {
                            Object inner = ensureUnwrappedTarget(instance, entry);
                            writeUnwrappedValue(parser, inner, entry, features);
                            handled = true;
                        } else {
                            parser.setOffset(fieldStart);
                        }
                    }
                    if (!handled) {
                        if (anySetterMethod != null) {
                            parser.setOffset(fieldStart);
                            String fname = parser.readFieldName();
                            Object fvalue = parser.readAny();
                            // Skip the type-discriminator key (see UTF8 path).
                            if (anySetterTypeKey != null && anySetterTypeKey.equals(fname)) {
                                // value consumed, drop
                            } else {
                                try {
                                    anySetterMethod.invoke(instance, fname, fvalue);
                                } catch (Exception e) {
                                    throw new JSONException("anySetter error for '" + fname + "'", e);
                                }
                            }
                        } else if (errorOnUnknown) {
                            throw new JSONException("unknown property in " + objectClass.getName());
                        } else {
                            parser.skipValue();
                        }
                    }
                }

                parser.skipWS();
                int off = parser.getOffset();
                if (off >= parser.getEnd()) {
                    throw new JSONException("unterminated object");
                }
                int c = parser.charAt(off);
                if (c == ',') {
                    parser.advance(1);
                    continue;
                }
                if (c == '}') {
                    parser.advance(1);
                    break;
                }
                throw new JSONException("expected ',' or '}' at offset " + off);
            }

            applyDefaults(instance, fieldSetMask);
            validateTypeSchema(instance);
            return instance;
        }

        /**
         * UTF-8 fast path with cached reader arrays passed as parameters.
         */
        /**
         * Handle non-STRING/non-LONG field types.
         * readFieldNameHash already skips WS after ':', so peekByte() is used instead of skipWSAndPeek().
         */
        private void readAndSetFieldUTF8Inline(JSONParser.UTF8 utf8, Object instance,
                                                FieldReader reader, int fieldIndex, long features,
                                                ObjectReader<?>[] objReaders, ObjectReader<?>[] elemReaders) {
            // Custom deserializeUsing takes priority over type-specific parsing
            if (reader.deserializeUsingClass != null && fieldIndex >= 0 && objReaders != null
                    && objReaders[fieldIndex] != null) {
                reader.setObjectValue(instance,
                        objReaders[fieldIndex].readObject(utf8, reader.fieldType, null, features));
                return;
            }

            int peek = utf8.peekByte();

            switch (reader.typeTag) {
                case FieldReader.TAG_STRING -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    if (peek == '"') {
                        reader.setObjectValue(instance, utf8.readStringDirect());
                    } else if (peek == '-' || (peek >= '0' && peek <= '9')) {
                        // Numeric value - read as String to preserve precision
                        String numStr = readNumberString(utf8);
                        reader.setObjectValue(instance, numStr);
                    } else if (peek == 't' || peek == 'f') {
                        // Boolean value - convert to String
                        boolean val = utf8.readBoolean();
                        reader.setObjectValue(instance, Boolean.toString(val));
                    } else {
                        // Fallback to readStringDirect for any other type
                        reader.setObjectValue(instance, utf8.readStringDirect());
                    }
                }
                case FieldReader.TAG_INT -> reader.setIntValue(instance, utf8.readIntDirect());
                case FieldReader.TAG_LONG -> reader.setLongValue(instance, utf8.readLongDirect());
                case FieldReader.TAG_DOUBLE -> reader.setDoubleValue(instance, utf8.readDoubleDirect());
                case FieldReader.TAG_BOOLEAN -> reader.setBooleanValue(instance, utf8.readBooleanDirect());
                case FieldReader.TAG_FLOAT -> {
                    float v = (float) utf8.readDoubleDirect();
                    if (reader.fieldOffset >= 0) {
                        JDKUtils.putFloat(instance, reader.fieldOffset, v);
                    } else {
                        reader.setFieldValue(instance, v);
                    }
                }
                case FieldReader.TAG_LIST -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    // Nested generic element (List<List<X>>, List<Map<K,V>>, List<? extends X>):
                    // the inline fast path only handles Class element types, so delegate to
                    // the generic-aware JSONParser entry for anything else.
                    if (reader.elementType != null && !(reader.elementType instanceof Class<?>)) {
                        reader.setObjectValue(instance, utf8.read(reader.fieldType));
                    } else {
                        reader.setObjectValue(instance, readListUTF8Inline(utf8, reader, fieldIndex, features, elemReaders));
                    }
                }
                case FieldReader.TAG_SET -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    reader.setObjectValue(instance, utf8.read(reader.fieldType));
                }
                case FieldReader.TAG_MAP -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    reader.setObjectValue(instance, utf8.read(reader.fieldType));
                }
                case FieldReader.TAG_STRING_ARRAY -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    reader.setObjectValue(instance, utf8.readStringArrayInline());
                }
                case FieldReader.TAG_LONG_ARRAY -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    reader.setObjectValue(instance, utf8.readLongArrayInline());
                }
                case FieldReader.TAG_ENUM -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    if (peek == '"') {
                        // Read string and convert to enum constant via setObjectValue
                        reader.setObjectValue(instance, utf8.readStringDirect());
                    } else if (peek >= '0' && peek <= '9' || peek == '-') {
                        // Numeric enum input. When the enum declares a value-method that
                        // returns a fractional type (double/float/BigDecimal), a 1-char
                        // readInt would truncate "1.1" to 1 and misroute through the
                        // ordinal fallback, so consult readAny to preserve precision when
                        // the enum has a value-map. Plain ordinal enums keep the original
                        // fast path (readInt) to avoid allocation.
                        if (reader.enumValueMap != null) {
                            reader.setFieldValue(instance, reader.convertValue(utf8.readAny()));
                        } else {
                            reader.setFieldValue(instance, reader.convertValue(utf8.readInt()));
                        }
                    } else {
                        Object val = utf8.readAny();
                        reader.setFieldValue(instance, reader.convertValue(val));
                    }
                }
                default -> {
                    if (peek == 'n' && utf8.readNull()) {
                        return;
                    }
                    // Check if we have a pre-resolved ObjectReader for this field (POJO type)
                    // Use readObject (not readObjectUTF8) to pass fieldType for generic-aware readers
                    // (e.g., Guava ImmutableList<User>, Optional<User>). For POJO readers, readObject
                    // internally dispatches to the UTF-8 fast path via instanceof check.
                    ObjectReader<?> objReader = (fieldIndex >= 0 && objReaders != null)
                            ? objReaders[fieldIndex] : null;
                    if (objReader != null) {
                        reader.setObjectValue(instance, objReader.readObject(utf8, reader.fieldType, null, features));
                    } else {
                        readAndSetFieldGenericValue(utf8, instance, reader, features);
                    }
                }
            }
        }

        // WARNING: 此方法虽然当前未被调用，但不可删除。
        // 删除会改变内部类的方法数量/结构，导致 JIT 编译决策变化，
        // 实测删除后性能从 718K 降至 651K 且方差大幅增加。
        private void readAndSetFieldUTF8(JSONParser.UTF8 utf8, Object instance, FieldReader reader, int fieldIndex, long features) {
            readAndSetFieldUTF8Inline(utf8, instance, reader, fieldIndex, features, fieldObjectReaders, fieldElementReaders);
        }

        private void readAndSetFieldGeneric(JSONParser parser, Object instance, FieldReader reader, long features) {
            parser.skipWS();
            if (parser.charAt(parser.getOffset()) == 'n') {
                if (parser.readNull()) {
                    return;
                }
            }
            readAndSetFieldGenericValue(parser, instance, reader, features);
        }

        private void readAndSetFieldGenericValue(JSONParser parser, Object instance, FieldReader reader, long features) {
            // Custom deserializeUsing takes priority over type-specific parsing
            int fi = reader.index;
            ObjectReader<?> customReader = (fi >= 0 && fieldObjectReaders != null)
                    ? fieldObjectReaders[fi] : null;
            if (customReader != null && reader.deserializeUsingClass != null) {
                reader.setObjectValue(instance, customReader.readObject(parser, reader.fieldType, null, features));
                return;
            }
            switch (reader.typeTag) {
                case FieldReader.TAG_STRING -> {
                    // Handle string values, including numeric values that should be converted to strings
                    parser.skipWS();
                    int c = parser.charAt(parser.getOffset());
                    if (c == '"') {
                        reader.setFieldValue(instance, parser.readString());
                    } else if (c == '\'' && parser.isEnabled(ReadFeature.AllowSingleQuotes)) {
                        // Single-quoted string (non-standard but supported)
                        Object value = parser.readAny();
                        reader.setFieldValue(instance, value != null ? value.toString() : null);
                    } else if (c == 'n' && parser.readNull()) {
                        // null value, leave field as default
                    } else if (c == '-' || (c >= '0' && c <= '9')) {
                        // Numeric value, read as string with precise conversion
                        Object value = parser.readAny();
                        if (value instanceof Double || value instanceof Float) {
                            // Use BigDecimal for precise floating-point to string conversion
                            reader.setFieldValue(instance, java.math.BigDecimal.valueOf(((Number) value).doubleValue()).toPlainString());
                        } else if (value instanceof Number) {
                            reader.setFieldValue(instance, value.toString());
                        } else {
                            reader.setFieldValue(instance, value != null ? value.toString() : null);
                        }
                    } else if (c == 't' || c == 'f') {
                        // Boolean value, read as string
                        boolean bool = parser.readBoolean();
                        reader.setFieldValue(instance, Boolean.toString(bool));
                    } else {
                        // Fallback to readAny for any other type (e.g., single-quoted strings)
                        Object value = parser.readAny();
                        reader.setFieldValue(instance, value != null ? value.toString() : null);
                    }
                }
                case FieldReader.TAG_INT -> reader.setIntValue(instance, parser.readInt());
                case FieldReader.TAG_LONG -> reader.setLongValue(instance, parser.readLong());
                case FieldReader.TAG_DOUBLE -> reader.setDoubleValue(instance, parser.readDouble());
                case FieldReader.TAG_BOOLEAN -> reader.setBooleanValue(instance, parser.readBoolean());
                case FieldReader.TAG_INT_OBJ -> reader.setFieldValue(instance, parser.readInt());
                case FieldReader.TAG_LONG_OBJ -> reader.setFieldValue(instance, parser.readLong());
                case FieldReader.TAG_DOUBLE_OBJ -> reader.setFieldValue(instance, parser.readDouble());
                case FieldReader.TAG_BOOLEAN_OBJ -> reader.setFieldValue(instance, parser.readBoolean());
                case FieldReader.TAG_ENUM -> {
                    Object value = parser.readAny();
                    reader.setFieldValue(instance, reader.convertValue(value));
                }
                case FieldReader.TAG_LIST -> {
                    if (reader.elementType != null && !(reader.elementType instanceof Class<?>)) {
                        reader.setObjectValue(instance, parser.read(reader.fieldType));
                    } else {
                        Object value = readListGeneric(parser, reader, fi, features);
                        reader.setObjectValue(instance, value);
                    }
                }
                case FieldReader.TAG_SET -> {
                    reader.setObjectValue(instance, parser.read(reader.fieldType));
                }
                case FieldReader.TAG_MAP -> {
                    reader.setObjectValue(instance, parser.read(reader.fieldType));
                }
                default -> {
                    // Check for registered ObjectReader (e.g., Guava ImmutableList, custom POJO)
                    ObjectReader<?> objReader = (fi >= 0 && fieldObjectReaders != null)
                            ? fieldObjectReaders[fi] : null;
                    if (objReader != null) {
                        reader.setObjectValue(instance, objReader.readObject(parser, reader.fieldType, null, features));
                    } else {
                        Object value = parser.readAny();
                        reader.setFieldValue(instance, reader.convertValue(value));
                    }
                }
            }
        }

        private Object readListUTF8Inline(JSONParser.UTF8 utf8, FieldReader reader, int fieldIndex, long features, ObjectReader<?>[] elemReaders) {
            Class<?> elemClass = reader.elementClass;
            // Fast path: use inline offset-managed readers for String lists
            if (elemClass == String.class) {
                return utf8.readStringListInline();
            }

            int peek = utf8.skipWSAndPeek();
            if (peek != '[') {
                throw new JSONException("expected '[' at offset " + utf8.getOffset());
            }
            utf8.advance(1);

            peek = utf8.skipWSAndPeek();
            if (peek == ']') {
                utf8.advance(1);
                return new ArrayList<>(0);
            }

            ObjectReader<?> elemReader = (fieldIndex >= 0 && elemReaders != null)
                    ? elemReaders[fieldIndex] : null;

            if (elemReader != null) {
                return readListPojoUTF8(utf8, elemReader, elemClass, features);
            }
            return readListGenericUTF8(utf8);
        }

        // WARNING: 此方法虽然当前未被调用，但不可删除。原因同 readAndSetFieldUTF8。
        private Object readListUTF8(JSONParser.UTF8 utf8, FieldReader reader, int fieldIndex, long features) {
            int peek = utf8.skipWSAndPeek();
            if (peek != '[') {
                throw new JSONException("expected '[' at offset " + utf8.getOffset());
            }
            utf8.advance(1);

            peek = utf8.skipWSAndPeek();
            if (peek == ']') {
                utf8.advance(1);
                return new ArrayList<>(0);
            }

            Class<?> elemClass = reader.elementClass;
            ObjectReader<?> elemReader = (fieldIndex >= 0 && fieldElementReaders != null)
                    ? fieldElementReaders[fieldIndex] : null;

            if (elemClass == String.class) {
                return readListStringUTF8(utf8);
            }
            if (elemReader != null) {
                return readListPojoUTF8(utf8, elemReader, elemClass, features);
            }
            return readListGenericUTF8(utf8);
        }

        private ArrayList<Object> readListStringUTF8(JSONParser.UTF8 utf8) {
            ArrayList<Object> list = new ArrayList<>(16);
            for (;;) {
                int peek = utf8.skipWSAndPeek();
                if (peek == 'n' && utf8.readNull()) {
                    list.add(null);
                } else {
                    list.add(utf8.readStringDirect());
                }
                int sep = utf8.readArraySeparator();
                if (sep == 0) {
                    continue;
                }
                if (sep == 1) {
                    return list;
                }
                throw new JSONException("expected ',' or ']'");
            }
        }

        private ArrayList<Object> readListPojoUTF8(JSONParser.UTF8 utf8, ObjectReader<?> elemReader, Class<?> elemClass, long features) {
            ArrayList<Object> list = new ArrayList<>(16);
            int index = 0;
            for (;;) {
                int peek = utf8.skipWSAndPeek();
                if (peek == 'n' && utf8.readNull()) {
                    list.add(null);
                } else {
                    try {
                        list.add(elemReader.readObjectUTF8(utf8, features));
                    } catch (RuntimeException e) {
                        throw JSONException.wrapWithPath(e, index);
                    }
                }
                int sep = utf8.readArraySeparator();
                if (sep == 0) {
                    index++;
                    continue;
                }
                if (sep == 1) {
                    return list;
                }
                throw new JSONException("expected ',' or ']'");
            }
        }

        private ArrayList<Object> readListGenericUTF8(JSONParser.UTF8 utf8) {
            ArrayList<Object> list = new ArrayList<>();
            for (;;) {
                list.add(utf8.readAny());
                int sep = utf8.readArraySeparator();
                if (sep == 0) {
                    continue;
                }
                if (sep == 1) {
                    return list;
                }
                throw new JSONException("expected ',' or ']'");
            }
        }

        private ArrayList<Object> readListGeneric(JSONParser parser, FieldReader reader, int fieldIndex, long features) {
            Class<?> elemClass = reader.elementClass;

            parser.skipWS();
            if (parser.charAt(parser.getOffset()) != '[') {
                throw new JSONException("expected '[' at offset " + parser.getOffset());
            }
            parser.advance(1);

            parser.skipWS();
            if (parser.charAt(parser.getOffset()) == ']') {
                parser.advance(1);
                return new ArrayList<>(0);
            }

            // Get element reader for POJO lists
            ObjectReader<?> elemReader = (fieldIndex >= 0 && fieldElementReaders != null)
                    ? fieldElementReaders[fieldIndex] : null;

            if (elemClass == String.class) {
                return readListStringGeneric(parser);
            }

            if (elemReader != null) {
                return readListPojoGeneric(parser, elemReader, elemClass, features);
            }

            // Fallback to generic list
            ArrayList<Object> list = new ArrayList<>();
            for (;;) {
                list.add(parser.readAny());
                parser.skipWS();
                int c = parser.charAt(parser.getOffset());
                if (c == ']') {
                    parser.advance(1);
                    return list;
                }
                if (c == ',') {
                    parser.advance(1);
                    continue;
                }
                throw new JSONException("expected ',' or ']'");
            }
        }

        private ArrayList<Object> readListStringGeneric(JSONParser parser) {
            ArrayList<Object> list = new ArrayList<>();
            for (;;) {
                parser.skipWS();
                int c = parser.charAt(parser.getOffset());
                if (c == 'n' && parser.readNull()) {
                    list.add(null);
                } else if (c == '"') {
                    list.add(parser.readString());
                } else {
                    throw new JSONException("expected string or null at offset " + parser.getOffset());
                }
                parser.skipWS();
                c = parser.charAt(parser.getOffset());
                if (c == ']') {
                    parser.advance(1);
                    return list;
                }
                if (c == ',') {
                    parser.advance(1);
                    continue;
                }
                throw new JSONException("expected ',' or ']'");
            }
        }

        private ArrayList<Object> readListPojoGeneric(JSONParser parser, ObjectReader<?> elemReader, Class<?> elemClass, long features) {
            ArrayList<Object> list = new ArrayList<>();
            int index = 0;
            for (;;) {
                parser.skipWS();
                if (parser.charAt(parser.getOffset()) == 'n' && parser.readNull()) {
                    list.add(null);
                } else {
                    try {
                        list.add(elemReader.readObject(parser, elemClass, null, features));
                    } catch (RuntimeException e) {
                        throw JSONException.wrapWithPath(e, index);
                    }
                }
                parser.skipWS();
                if (parser.charAt(parser.getOffset()) == ']') {
                    parser.advance(1);
                    return list;
                }
                if (parser.charAt(parser.getOffset()) == ',') {
                    parser.advance(1);
                    index++;
                    continue;
                }
                throw new JSONException("expected ',' or ']'");
            }
        }

        private static String[] readStringArrayUTF8(JSONParser.UTF8 utf8) {
            int peek = utf8.skipWSAndPeek();
            if (peek != '[') {
                throw new JSONException("expected '[' at offset " + utf8.getOffset());
            }
            utf8.advance(1);
            peek = utf8.skipWSAndPeek();
            if (peek == ']') {
                utf8.advance(1);
                return new String[0];
            }
            String[] arr = new String[8];
            int size = 0;
            for (;;) {
                if (size == arr.length) {
                    arr = java.util.Arrays.copyOf(arr, size + (size >> 1));
                }
                peek = utf8.skipWSAndPeek();
                if (peek == 'n' && utf8.readNull()) {
                    arr[size++] = null;
                } else {
                    arr[size++] = utf8.readStringDirect();
                }
                int sep = utf8.readArraySeparator();
                if (sep == 1) {
                    break;
                }
                if (sep != 0) {
                    throw new JSONException("expected ',' or ']'");
                }
            }
            return size == arr.length ? arr : java.util.Arrays.copyOf(arr, size);
        }

        private static long[] readLongArrayUTF8(JSONParser.UTF8 utf8) {
            int peek = utf8.skipWSAndPeek();
            if (peek != '[') {
                throw new JSONException("expected '[' at offset " + utf8.getOffset());
            }
            utf8.advance(1);
            peek = utf8.skipWSAndPeek();
            if (peek == ']') {
                utf8.advance(1);
                return new long[0];
            }
            long[] arr = new long[2];
            int size = 0;
            for (;;) {
                if (size == arr.length) {
                    arr = java.util.Arrays.copyOf(arr, size + (size >> 1));
                }
                utf8.skipWSAndPeek();
                arr[size++] = utf8.readLongDirect();
                int sep = utf8.readArraySeparator();
                if (sep == 1) {
                    break;
                }
                if (sep != 0) {
                    throw new JSONException("expected ',' or ']'");
                }
            }
            return size == arr.length ? arr : java.util.Arrays.copyOf(arr, size);
        }

        private Object readList(JSONParser parser, FieldReader reader, int fieldIndex, long features) {
            parser.skipWS();
            if (parser.charAt(parser.getOffset()) != '[') {
                throw new JSONException("expected '[' at offset " + parser.getOffset());
            }
            parser.advance(1);

            parser.skipWS();
            if (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) == ']') {
                parser.advance(1);
                return new ArrayList<>(0);
            }

            Class<?> elemClass = reader.elementClass;
            ObjectReader<?> elemReader = (fieldIndex >= 0 && fieldElementReaders != null)
                    ? fieldElementReaders[fieldIndex] : null;

            if (elemClass == String.class) {
                return readListString(parser);
            }
            if (elemReader != null) {
                return readListPojo(parser, elemReader, elemClass, features);
            }
            return readListGeneric(parser);
        }

        private ArrayList<Object> readListString(JSONParser parser) {
            ArrayList<Object> list = new ArrayList<>();
            for (;;) {
                parser.skipWS();
                if (parser.charAt(parser.getOffset()) == 'n' && parser.readNull()) {
                    list.add(null);
                } else {
                    list.add(parser.readString());
                }
                parser.skipWS();
                int c = parser.charAt(parser.getOffset());
                if (c == ',') {
                    parser.advance(1);
                    continue;
                }
                if (c == ']') {
                    parser.advance(1);
                    return list;
                }
                throw new JSONException("expected ',' or ']' at offset " + parser.getOffset());
            }
        }

        private ArrayList<Object> readListPojo(JSONParser parser, ObjectReader<?> elemReader, Class<?> elemClass, long features) {
            ArrayList<Object> list = new ArrayList<>();
            int index = 0;
            for (;;) {
                parser.skipWS();
                if (parser.charAt(parser.getOffset()) == 'n' && parser.readNull()) {
                    list.add(null);
                } else {
                    try {
                        list.add(elemReader.readObject(parser, elemClass, null, features));
                    } catch (RuntimeException e) {
                        throw JSONException.wrapWithPath(e, index);
                    }
                }
                parser.skipWS();
                int c = parser.charAt(parser.getOffset());
                if (c == ',') {
                    parser.advance(1);
                    index++;
                    continue;
                }
                if (c == ']') {
                    parser.advance(1);
                    return list;
                }
                throw new JSONException("expected ',' or ']' at offset " + parser.getOffset());
            }
        }

        private ArrayList<Object> readListGeneric(JSONParser parser) {
            ArrayList<Object> list = new ArrayList<>();
            for (;;) {
                list.add(parser.readAny());
                parser.skipWS();
                int c = parser.charAt(parser.getOffset());
                if (c == ',') {
                    parser.advance(1);
                    continue;
                }
                if (c == ']') {
                    parser.advance(1);
                    return list;
                }
                throw new JSONException("expected ',' or ']' at offset " + parser.getOffset());
            }
        }

        private void validateTypeSchema(T instance) {
            if (typeSchema != null && instance != null) {
                typeSchema.assertValidate(instance);
            }
        }

        /**
         * Read directly from a JSONObject without byte[] round-trip.
         * Used by polymorphic readers (sealed class, Jackson @JsonTypeInfo) to
         * avoid the serialize→parse cycle after type discriminator inspection.
         * Back-compat overload — equivalent to passing {@code discriminatorKey=null}.
         */
        T readFromJSONObject(com.alibaba.fastjson3.JSONObject jsonObj, long features) {
            return readFromJSONObject(jsonObj, features, null);
        }

        /**
         * Variant used by polymorphic dispatch that passes the resolved discriminator
         * key so the strict-unknown check can skip it (the sealed / Jackson polymorphic
         * readers call this after extracting the subtype — the discriminator key is
         * not a "real" property of the sub-reader's type).
         */
        T readFromJSONObject(com.alibaba.fastjson3.JSONObject jsonObj, long features, String discriminatorKey) {
            ensureFieldReaders();
            T instance = createInstance(features);
            long fieldSetMask = 0;

            for (int i = 0; i < fieldReaders.length; i++) {
                FieldReader fr = fieldReaders[i];
                Object value = jsonObj.get(fr.fieldName);

                // Check alternate names if primary not found
                if (value == null && !jsonObj.containsKey(fr.fieldName)) {
                    for (String alt : fr.alternateNames) {
                        value = jsonObj.get(alt);
                        if (value != null || jsonObj.containsKey(alt)) {
                            break;
                        }
                    }
                }

                if (value != null) {
                    Object converted = fr.convertValue(value);
                    fr.setFieldValue(instance, converted);
                    if (i < 64) {
                        fieldSetMask |= (1L << i);
                    }
                }
            }

            // Route unmapped keys to the @JSONField(unwrapped=true) side table before
            // falling through to anySetter — mirrors the parser-based loops so behavior
            // is consistent regardless of which entry point converts the JSONObject.
            boolean errorOnUnknown = (features & ReadFeature.ErrorOnUnknownProperties.mask) != 0;
            if (unwrappedByName != null || anySetterMethod != null || errorOnUnknown) {
                java.util.Set<String> knownNames = fieldReaderMap.keySet();
                for (var entry : jsonObj.entrySet()) {
                    String key = entry.getKey();
                    if (knownNames.contains(key)) {
                        continue;
                    }
                    // Polymorphic discriminator belongs to the parent reader, not this
                    // sub-reader's type. Skip it so ErrorOnUnknownProperties + anySetter
                    // don't mistakenly treat the type-key as a real user property.
                    if (discriminatorKey != null && discriminatorKey.equals(key)) {
                        continue;
                    }
                    // Same reasoning for non-polymorphic classes whose writer emits
                    // "@type" (via @JSONType(typeName=) or WriteClassName) — routing
                    // it to the anySetter map would duplicate the discriminator on
                    // the next write.
                    if (anySetterTypeKey != null && anySetterTypeKey.equals(key)) {
                        continue;
                    }
                    if (unwrappedByName != null) {
                        UnwrappedEntry ue = unwrappedByName.get(key);
                        if (ue != null) {
                            Object target = ensureUnwrappedTarget(instance, ue);
                            Object assigned = applyUnwrappedValueFromMap(ue, entry.getValue(), features);
                            ue.innerReader.setFieldValue(target, assigned);
                            continue;
                        }
                    }
                    if (anySetterMethod != null) {
                        try {
                            anySetterMethod.invoke(instance, key, entry.getValue());
                        } catch (Exception e) {
                            throw new JSONException("anySetter error: " + e.getMessage(), e);
                        }
                        continue;
                    }
                    if (errorOnUnknown) {
                        // Mirror the parser-based loops. Sealed / Jackson polymorphic flows
                        // materialize a JSONObject and call readFromJSONObject directly, so
                        // without this check ErrorOnUnknownProperties was silently ignored
                        // on those entry points.
                        throw new JSONException("unknown property '" + key
                                + "' in " + objectClass.getName());
                    }
                }
            }

            applyDefaults(instance, fieldSetMask);
            // Honour @JSONType(schema=...) on the polymorphic / JSONObject entry
            // point too — otherwise a sealed-class or Jackson-subtype instance
            // built via readFromJSONObject would skip the schema check that the
            // parser-based readers apply, producing inconsistent behaviour across
            // entry points for the same class.
            validateTypeSchema(instance);
            return instance;
        }

        private void applyDefaults(T instance, long fieldSetMask) {
            if (!hasDefaultsOrRequired) {
                return;
            }
            for (int i = 0; i < fieldReaders.length; i++) {
                if (i < 64 && (fieldSetMask & (1L << i)) != 0) {
                    continue; // field was set
                }
                FieldReader fr = fieldReaders[i];
                if (fr.required) {
                    throw new JSONException(
                            "required field '" + fr.fieldName + "' is missing in " + objectClass.getName()
                    );
                }
                if (fr.defaultValue != null && !fr.defaultValue.isEmpty()) {
                    Object defaultVal = parseDefault(fr.defaultValue, fr.fieldClass);
                    fr.setFieldValue(instance, defaultVal);
                }
            }
            // An @JSONField(unwrapped=true, required=true) holder is "present" only if
            // at least one flattened inner key populated it — otherwise the holder
            // remains null. The normal fieldReader loop above skips it because the
            // holder never got a FieldReader; check holders directly here.
            if (requiredUnwrappedHolders != null) {
                for (Field[] chain : requiredUnwrappedHolders) {
                    try {
                        Object current = instance;
                        int lastIdx = chain.length - 1;
                        for (int i = 0; i < chain.length; i++) {
                            Object next = chain[i].get(current);
                            if (next == null) {
                                // Direct required (chain.length == 1): the outer holder
                                // itself is null — always a violation. Nested required:
                                // if an intermediate is null the inner wasn't materialised,
                                // so the requirement simply doesn't apply (early exit).
                                // Only the final link is an enforced violation.
                                if (i == lastIdx) {
                                    throw new JSONException("required field '" + chain[i].getName()
                                            + "' is missing in " + objectClass.getName()
                                            + " (unwrapped holder — at least one flattened inner key must appear)");
                                }
                                break;
                            }
                            current = next;
                        }
                    } catch (IllegalAccessException e) {
                        throw new JSONException("cannot read unwrapped holder '"
                                + chain[chain.length - 1].getName() + "'", e);
                    }
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public T createInstance(long features) {
            if (useUnsafeAlloc) {
                return (T) com.alibaba.fastjson3.util.UnsafeAllocator.allocateInstanceUnchecked(objectClass);
            }
            try {
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new JSONException(
                        "cannot create instance of " + objectClass.getName() + ": " + e.getMessage(), e
                );
            }
        }

        @Override
        public void readFieldUTF8(JSONParser.UTF8 utf8, Object instance, int fieldIndex, long features) {
            ensureFieldReaders();
            FieldReader reader = fieldReaders[fieldIndex];
            readAndSetFieldUTF8Inline(utf8, instance, reader, fieldIndex, features, fieldObjectReaders, fieldElementReaders);
        }

        @Override
        public Class<T> getObjectClass() {
            return objectClass;
        }

        private static Object parseDefault(String defaultValue, Class<?> type) {
            if (type == String.class) {
                return defaultValue;
            }
            if (type == int.class || type == Integer.class) {
                return Integer.parseInt(defaultValue);
            }
            if (type == long.class || type == Long.class) {
                return Long.parseLong(defaultValue);
            }
            if (type == double.class || type == Double.class) {
                return Double.parseDouble(defaultValue);
            }
            if (type == float.class || type == Float.class) {
                return Float.parseFloat(defaultValue);
            }
            if (type == boolean.class || type == Boolean.class) {
                return Boolean.parseBoolean(defaultValue);
            }
            return defaultValue;
        }

        /**
         * Read a numeric value directly as a String, preserving precision.
         * This method reads the number characters from the byte array without
         * parsing to double/float, avoiding precision loss.
         */
        private String readNumberString(JSONParser.UTF8 utf8) {
            final byte[] b = utf8.getBytes();
            int off = utf8.getOffset();
            final int end = utf8.getEnd();
            int start = off;

            // Skip sign
            if (off < end && b[off] == '-') {
                off++;
            }

            // Integer part
            while (off < end && b[off] >= '0' && b[off] <= '9') {
                off++;
            }

            // Fraction part
            if (off < end && b[off] == '.') {
                off++;
                while (off < end && b[off] >= '0' && b[off] <= '9') {
                    off++;
                }
            }

            // Exponent part
            if (off < end && (b[off] == 'e' || b[off] == 'E')) {
                off++;
                if (off < end && (b[off] == '+' || b[off] == '-')) {
                    off++;
                }
                while (off < end && b[off] >= '0' && b[off] <= '9') {
                    off++;
                }
            }

            utf8.setOffset(off);
            return new String(b, start, off - start, java.nio.charset.StandardCharsets.ISO_8859_1);
        }
    }

    // ==================== Record ObjectReader ====================

    /**
     * ObjectReader for Java Record types. Records require all component values
     * to be provided to the canonical constructor — individual field setting is not possible.
     * This reader uses Unsafe to allocate the instance and set fields directly,
     * then validates that all required fields were provided.
     */
    /**
     * Invokes either a constructor or a static factory method to materialise
     * the record / constructor-based POJO instance from the filled values[]
     * array. Abstracted so a static `@JSONCreator` / `@JsonCreator`-annotated
     * factory method (e.g. {@code static F of(int v) { … }}) can slot into the
     * same parsing machinery as a canonical constructor without duplicating
     * the read loops.
     */
    @FunctionalInterface
    interface InstanceInvoker<T> {
        T invoke(Object[] values) throws Exception;
    }

    private static final class RecordObjectReader<T> implements ObjectReader<T> {
        private final Class<T> objectClass;
        private final InstanceInvoker<T> instanceInvoker;
        private final Class<?>[] parameterTypes;
        private final FieldReader[] fieldReaders;
        private final Map<String, FieldReader> fieldReaderMap;
        private final FieldNameMatcher matcher;
        private final int componentCount;
        private final int[] paramMapping; // fieldReader index → constructor param index
        private final Map<String, RecordUnwrappedEntry> unwrappedByName; // null when no unwrapped components
        // Component indices whose @JSONField(unwrapped=true, required=true) holder must
        // be non-null after parse (at least one flattened inner key populated it).
        // Null when no such components — post-parse check skipped.
        private final int[] requiredUnwrappedIndices;
        private final String[] requiredUnwrappedNames;

        private volatile boolean fieldReadersResolved;
        private ObjectReader<?>[] fieldObjectReaders;
        private ObjectReader<?>[] fieldElementReaders;

        RecordObjectReader(
                Class<T> objectClass,
                InstanceInvoker<T> instanceInvoker,
                Class<?>[] parameterTypes,
                FieldReader[] fieldReaders,
                Map<String, FieldReader> fieldReaderMap,
                FieldNameMatcher matcher,
                int componentCount,
                int[] paramMapping,
                List<RecordUnwrappedEntry> unwrappedEntries,
                int[] requiredUnwrappedIndices,
                String[] requiredUnwrappedNames
        ) {
            this.objectClass = objectClass;
            this.instanceInvoker = instanceInvoker;
            this.parameterTypes = parameterTypes;
            this.fieldReaders = fieldReaders;
            this.fieldReaderMap = fieldReaderMap;
            this.matcher = matcher;
            this.componentCount = componentCount;
            this.paramMapping = paramMapping;
            if (unwrappedEntries == null || unwrappedEntries.isEmpty()) {
                this.unwrappedByName = null;
            } else {
                Map<String, RecordUnwrappedEntry> map = HashMap.newHashMap(unwrappedEntries.size() * 2);
                for (RecordUnwrappedEntry ue : unwrappedEntries) {
                    RecordUnwrappedEntry prev = map.putIfAbsent(ue.innerReader.fieldName, ue);
                    if (prev != null && prev != ue) {
                        throw new JSONException("ambiguous unwrapped field name '" + ue.innerReader.fieldName
                                + "' in record " + objectClass.getName()
                                + " — multiple unwrapped components register the same inner field");
                    }
                    for (String alt : ue.innerReader.alternateNames) {
                        RecordUnwrappedEntry prevAlt = map.putIfAbsent(alt, ue);
                        if (prevAlt != null && prevAlt != ue) {
                            throw new JSONException("ambiguous unwrapped alternate name '" + alt
                                    + "' in record " + objectClass.getName());
                        }
                    }
                }
                this.unwrappedByName = map;
            }
            this.requiredUnwrappedIndices = requiredUnwrappedIndices;
            this.requiredUnwrappedNames = requiredUnwrappedNames;
        }

        /**
         * Lazily create the scratch inner instance for a given record-unwrapped
         * component and cache it in the component-values array slot. Subsequent hits
         * on the same componentIdx return the cached instance so sibling flattened
         * keys share one inner POJO.
         */
        private Object ensureRecordScratch(Object[] values, RecordUnwrappedEntry entry) {
            Object scratch = values[entry.componentIdx];
            if (scratch == null) {
                Constructor<?> ctor = entry.innerCtor;
                if (ctor == null) {
                    throw new JSONException("@JSONField(unwrapped=true) requires "
                            + entry.innerClass.getName() + " to declare a no-arg constructor"
                            + " (record/constructor-side unwrap stages flattened values before"
                            + " invoking the canonical constructor)");
                }
                try {
                    scratch = ctor.newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new JSONException("cannot construct unwrapped inner "
                            + entry.innerClass.getName() + ": " + e.getMessage(), e);
                }
                values[entry.componentIdx] = scratch;
            }
            // Walk intermediate holder chain inside the scratch when the entry
            // represents a nested unwrapped hop (inner class itself declares
            // @JSONField(unwrapped=true)). Each hop lazily constructs + attaches
            // the intermediate POJO on demand, mirroring ensureUnwrappedTarget
            // for mutable outer classes. Constructors are pre-resolved in the
            // RecordUnwrappedEntry constructor so the hot path skips reflection
            // lookup + setAccessible overhead.
            Object target = scratch;
            for (int i = 0; i < entry.intermediateChain.length; i++) {
                Field holder = entry.intermediateChain[i];
                try {
                    Object next = holder.get(target);
                    if (next == null) {
                        Constructor<?> ctor = entry.intermediateCtors[i];
                        if (ctor == null) {
                            throw new JSONException("@JSONField(unwrapped=true) requires "
                                    + entry.intermediateClasses[i].getName()
                                    + " to declare a no-arg constructor");
                        }
                        next = ctor.newInstance();
                        holder.set(target, next);
                    }
                    target = next;
                } catch (ReflectiveOperationException e) {
                    throw new JSONException("cannot construct unwrapped intermediate "
                            + entry.intermediateClasses[i].getName() + " for field '" + holder.getName()
                            + "': " + e.getMessage(), e);
                }
            }
            return target;
        }

        private boolean writeRecordUnwrapped(JSONParser parser, Object[] values, String fname, long features) {
            if (unwrappedByName == null) {
                return false;
            }
            RecordUnwrappedEntry entry = unwrappedByName.get(fname);
            if (entry == null) {
                return false;
            }
            Object scratch = ensureRecordScratch(values, entry);
            FieldReader innerFr = entry.innerReader;
            try {
                ObjectReader<?> custom = entry.resolveCustomReader();
                if (custom != null) {
                    Object v = custom.readObject(parser, innerFr.fieldType, innerFr.fieldName, features);
                    innerFr.setFieldValue(scratch, v);
                } else {
                    Object fvalue = parser.readAny();
                    Object converted = innerFr.convertValue(fvalue);
                    innerFr.setFieldValue(scratch, converted);
                }
            } catch (RuntimeException e) {
                // Mirror the POJO-side writeUnwrappedValue wrap (line 1907) so
                // record-side type mismatches surface as JSONException with
                // field path rather than raw NFE/CCE.
                throw com.alibaba.fastjson3.JSONException.wrapWithPath(e, innerFr.fieldName);
            }
            return true;
        }

        private void ensureFieldReaders() {
            if (fieldReadersResolved) {
                return;
            }
            // Use double-checked locking to avoid unnecessary array allocation
            synchronized (this) {
                if (fieldReadersResolved) {
                    return;
                }
                int len = fieldReaders.length;
                ObjectReader<?>[] objReaders = new ObjectReader<?>[len];
                ObjectReader<?>[] elemReaders = new ObjectReader<?>[len];
                for (int i = 0; i < len; i++) {
                    FieldReader fr = fieldReaders[i];
                    // Skip BuiltinCodecs for fields with custom @JSONField(format=...) annotation
                    if (fr.formatter != null) {
                        // objReaders[i] remains null, will fall through to convertValue path
                    } else if (fr.typeTag == FieldReader.TAG_GENERIC) {
                        // For POJO fields (not basic types), get ObjectReader.
                        // Prefer resolved fieldType (see non-record branch above).
                        Class<?> target = (fr.fieldType instanceof Class<?> resolved)
                                ? resolved : fr.fieldClass;
                        // See non-record branch: JSONObject / JSONArray fields (and arrays
                        // of them) fall through to readAny() + convertValue rather than
                        // the broken POJO reader.
                        if (isJsonNodeOrJsonNodeArray(target)) {
                            continue;
                        }
                        ObjectReader<?> r = com.alibaba.fastjson3.BuiltinCodecs.getReader(target);
                        if (r == null) {
                            // Skip interfaces and abstract classes - they don't have constructors
                            if (!target.isInterface() && !java.lang.reflect.Modifier.isAbstract(target.getModifiers())) {
                                r = createObjectReader(target);
                            }
                        }
                        if (r != null) {
                            objReaders[i] = r;
                            // Don't modify fr.typeTag - it may be shared across readers
                        }
                    }
                    if (fr.elementType instanceof Class<?> elemC
                            && elemC != String.class && elemC != Object.class
                            && !isJsonNodeOrJsonNodeArray(elemC)) {
                        ObjectReader<?> r = com.alibaba.fastjson3.BuiltinCodecs.getReader(elemC);
                        if (r == null) {
                            if (!elemC.isInterface() && !java.lang.reflect.Modifier.isAbstract(elemC.getModifiers())) {
                                r = createObjectReader(elemC);
                            }
                        }
                        if (r != null) {
                            elemReaders[i] = r;
                        }
                    }
                }
                this.fieldElementReaders = elemReaders;
                this.fieldObjectReaders = objReaders;
                this.fieldReadersResolved = true;
            }
        }

        @Override
        public T readObject(JSONParser parser, java.lang.reflect.Type fieldType, Object fieldName, long features) {
            ensureFieldReaders();
            // Handle null
            parser.skipWS();
            if (parser.readNull()) {
                return null;
            }
            if (parser instanceof JSONParser.UTF8 utf8) {
                return readRecordUTF8(utf8, features);
            }
            return readRecordGeneric(parser, features);
        }

        @Override
        public T readObjectUTF8(JSONParser.UTF8 utf8, long features) {
            ensureFieldReaders();
            return readRecordUTF8(utf8, features);
        }

        @SuppressWarnings("unchecked")
        private T readRecordUTF8(JSONParser.UTF8 utf8, long features) {
            int peek = utf8.skipWSAndPeek();
            if (peek == 'n' && utf8.readNull()) {
                return null;
            }
            if (peek != '{') {
                throw new JSONException("expected '{' at offset " + utf8.getOffset());
            }
            utf8.advance(1);

            peek = utf8.skipWSAndPeek();
            if (peek == '}') {
                utf8.advance(1);
                return constructRecord(new Object[componentCount]);
            }

            // Collect values into array, then call canonical constructor
            Object[] values = new Object[componentCount];
            final FieldNameMatcher m = this.matcher;
            final boolean errorOnUnknown = (features & ReadFeature.ErrorOnUnknownProperties.mask) != 0;
            final boolean usePLHV = m.strategy == FieldNameMatcher.STRATEGY_PLHV;

            final byte[] b = utf8.getBytes();
            final int end = utf8.getEnd();
            int off = utf8.getOffset();
            int nextExpected = 0;
            final int frLen = fieldReaders.length;

            for (;;) {
                FieldReader reader = null;
                int fieldStart = off; // saved for potential re-read in the unwrapped miss branch

                // Ordered field speculation. Bounds-guard every b[off] read against
                // the parser's `end` (not b.length — bytes can be a wrapped sub-range).
                // Truncated input falls through to the slow path below, which surfaces
                // a JSONException instead of an AIOOBE.
                if (nextExpected < frLen) {
                    FieldReader candidate = fieldReaders[nextExpected];
                    byte[] hdr = candidate.fieldNameHeader;
                    while (off < end && b[off] <= ' ') {
                        off++;
                    }
                    fieldStart = off;
                    int hdrLen = hdr.length;
                    if (off + hdrLen <= end && b[off] == '"') {
                        boolean match = true;
                        for (int i = 1; i < hdrLen; i++) {
                            if (b[off + i] != hdr[i]) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            off += hdrLen;
                            while (off < end && b[off] <= ' ') {
                                off++;
                            }
                            reader = candidate;
                            nextExpected++;
                        }
                    }
                }

                if (reader == null) {
                    utf8.setOffset(off);
                    long hash = usePLHV ? utf8.readFieldNameHashPLHV() : utf8.readFieldNameHash(m);
                    off = utf8.getOffset();
                    reader = m.matchFlat(hash);
                    if (reader != null) {
                        nextExpected = reader.index + 1;
                    } else {
                        nextExpected = 0;
                    }
                }

                if (reader != null) {
                    // Read value and store in values array at the correct constructor param index.
                    // Fields with resolved generic collection/map types route through parser.read
                    // so nested generics (List<List<X>>, Map<K, List<V>>) and plain POJO element
                    // types are fully resolved — readAny() alone would leave them as raw
                    // ArrayList/LinkedHashMap.
                    utf8.setOffset(off);
                    Object value;
                    try {
                        int tag = reader.typeTag;
                        if (tag == FieldReader.TAG_LIST || tag == FieldReader.TAG_SET
                                || tag == FieldReader.TAG_MAP) {
                            value = utf8.read(reader.fieldType);
                        } else {
                            value = utf8.readAny();
                        }
                        off = utf8.getOffset();
                        int paramIdx = paramMapping[reader.index];
                        values[paramIdx] = reader.convertValue(value);
                    } catch (RuntimeException e) {
                        throw JSONException.wrapWithPath(e, reader.fieldName);
                    }
                } else if (unwrappedByName != null) {
                    // Re-read the field name and consult the record-side unwrapped table.
                    // On hit, lazily construct the scratch inner at values[componentIdx]
                    // and stage the inner field value there; the canonical constructor
                    // will consume it alongside normally-matched components.
                    utf8.setOffset(fieldStart);
                    String fname = utf8.readFieldName();
                    if (!writeRecordUnwrapped(utf8, values, fname, features)) {
                        if (errorOnUnknown) {
                            throw new JSONException("unknown property '" + fname + "' in " + objectClass.getName());
                        }
                        utf8.skipValue();
                    }
                    off = utf8.getOffset();
                } else {
                    if (errorOnUnknown) {
                        throw new JSONException("unknown property in " + objectClass.getName());
                    }
                    utf8.setOffset(off);
                    utf8.skipValue();
                    off = utf8.getOffset();
                }

                while (off < end && b[off] <= ' ') {
                    off++;
                }
                if (off >= end) {
                    throw new JSONException("unexpected end of input in " + objectClass.getName());
                }
                if (b[off] == ',') {
                    off++;
                    continue;
                }
                if (b[off] == '}') {
                    off++;
                    break;
                }
                throw new JSONException("expected ',' or '}' in " + objectClass.getName());
            }
            utf8.setOffset(off);
            return constructRecord(values);
        }

        @SuppressWarnings("unchecked")
        private T readRecordGeneric(JSONParser parser, long features) {
            Object obj = parser.readObject();
            if (obj == null) {
                return null;
            }
            if (obj instanceof Map<?, ?> map) {
                Object[] values = new Object[componentCount];
                boolean errorOnUnknown = (features & ReadFeature.ErrorOnUnknownProperties.mask) != 0;
                // Track consumed keys when either unwrapped routing needs a second pass
                // or ErrorOnUnknownProperties needs to diff normal fields from unknowns.
                java.util.Set<String> consumed = (unwrappedByName != null || errorOnUnknown)
                        ? new java.util.HashSet<>() : null;
                for (FieldReader fr : fieldReaders) {
                    Object value = map.get(fr.fieldName);
                    // Mark consumed by PRESENCE (containsKey), not by non-null value.
                    // A key with an explicit `null` value must still count as consumed;
                    // otherwise the second pass misroutes it through unwrappedByName or
                    // wrongly flags it under ErrorOnUnknownProperties.
                    if (consumed != null && map.containsKey(fr.fieldName)) {
                        consumed.add(fr.fieldName);
                    }
                    if (value != null) {
                        try {
                            // Re-parse the raw value through the generic-aware path when
                            // the record component is a resolved collection/map type so
                            // elements arrive as the declared type, not raw LinkedHashMap.
                            int tag = fr.typeTag;
                            if (tag == FieldReader.TAG_LIST || tag == FieldReader.TAG_SET
                                    || tag == FieldReader.TAG_MAP) {
                                value = com.alibaba.fastjson3.JSON.parseObject(
                                        com.alibaba.fastjson3.JSON.toJSONString(value), fr.fieldType);
                            }
                            values[paramMapping[fr.index]] = fr.convertValue(value);
                        } catch (RuntimeException e) {
                            throw JSONException.wrapWithPath(e, fr.fieldName);
                        }
                    }
                }
                // Second pass — staged flattened keys go into scratch inners then feed the
                // canonical constructor at their component index. When errorOnUnknown is
                // enabled, any key that matches neither a normal field nor an unwrapped
                // entry raises the same error as the streaming path.
                if (unwrappedByName != null || errorOnUnknown) {
                    for (Map.Entry<?, ?> e : map.entrySet()) {
                        String key = String.valueOf(e.getKey());
                        if (consumed.contains(key)) {
                            continue;
                        }
                        RecordUnwrappedEntry ue = unwrappedByName == null ? null : unwrappedByName.get(key);
                        if (ue != null) {
                            Object scratch = ensureRecordScratch(values, ue);
                            Object assigned = applyRecordUnwrappedValueFromMap(ue, e.getValue(), features);
                            ue.innerReader.setFieldValue(scratch, assigned);
                            continue;
                        }
                        if (errorOnUnknown) {
                            throw new JSONException("unknown property '" + key
                                    + "' in " + objectClass.getName());
                        }
                    }
                }
                return constructRecord(values);
            }
            throw new JSONException("expected object for " + objectClass.getName());
        }

        @SuppressWarnings("unchecked")
        private T constructRecord(Object[] values) {
            // Canonical-constructor invocation unboxes primitive parameters via
            // the JDK's MethodHandle adapter chain. If a slot is null for a
            // primitive component, that adapter throws a NullPointerException
            // from sun.invoke.util.ValueConversions.primitiveConversion with
            // zero user-facing context. Catch it at the source and report the
            // missing field name.
            for (int slot = 0; slot < values.length; slot++) {
                if (values[slot] == null && parameterTypes[slot].isPrimitive()) {
                    String fieldName = findFieldNameForSlot(slot);
                    throw new JSONException(
                            "cannot construct record " + objectClass.getName()
                                    + ": required " + parameterTypes[slot].getName()
                                    + " field '" + fieldName + "' is missing or null"
                    );
                }
            }
            // @JSONField(unwrapped=true, required=true) on a component: the slot
            // stays null when NO flattened inner key appears in the JSON. Enforce
            // here because the unwrapped expansion skipped creating a regular
            // FieldReader, so the normal per-field required check can't see it.
            if (requiredUnwrappedIndices != null) {
                for (int i = 0; i < requiredUnwrappedIndices.length; i++) {
                    int slot = requiredUnwrappedIndices[i];
                    if (values[slot] == null) {
                        throw new JSONException("required field '" + requiredUnwrappedNames[i]
                                + "' is missing in " + objectClass.getName()
                                + " (unwrapped component — at least one flattened inner key must appear)");
                    }
                }
            }
            try {
                return instanceInvoker.invoke(values);
            } catch (Exception e) {
                // Unwrap reflection wrappers — InvocationTargetException /
                // UndeclaredThrowableException obscure the actual user-thrown
                // cause so the message reader sees comes from the creator
                // itself, not from the reflective-call scaffolding.
                Throwable root = e;
                if (e instanceof java.lang.reflect.InvocationTargetException ite && ite.getCause() != null) {
                    root = ite.getCause();
                }
                throw new JSONException(
                        "cannot construct " + objectClass.getName() + ": " + root.getMessage(), root
                );
            }
        }

        /** Reverse the paramMapping[fieldReaderIndex] → slot map to recover the field name. */
        private String findFieldNameForSlot(int slot) {
            for (int i = 0; i < fieldReaders.length; i++) {
                if (paramMapping[i] == slot) {
                    return fieldReaders[i].fieldName;
                }
            }
            return "component[" + slot + "]";
        }

        @Override
        public Class<T> getObjectClass() {
            return objectClass;
        }

        @Override
        public T createInstance(long features) {
            throw new UnsupportedOperationException("Record instances require all component values");
        }

        @Override
        public void readFieldUTF8(JSONParser.UTF8 utf8, Object instance, int fieldIndex, long features) {
            ensureFieldReaders();
            FieldReader reader = fieldReaders[fieldIndex];
            Object value = utf8.readAny();
            reader.setFieldValue(instance, reader.convertValue(value));
        }
    }

    // ==================== Helper methods ====================

    /**
     * Locate a {@code static} factory method annotated with {@code @JSONCreator}
     * (or Jackson {@code @JsonCreator} when {@code useJacksonAnnotation} is on)
     * that returns an instance of {@code type}. Mirrors Jackson's behaviour
     * where a class like {@code Money { static Money of(BigDecimal v) {...} }}
     * is deserialised through the factory rather than through a constructor.
     *
     * <p>Returns {@code null} when no eligible factory is declared — the caller
     * falls through to constructor resolution.</p>
     */
    private static Method resolveStaticFactoryMethod(Class<?> type, Class<?> mixIn, boolean useJacksonAnnotation) {
        // Ambiguity must be detected ACROSS the class + mix-in boundary too —
        // a factory declared on the class and another on the mix-in is just as
        // non-deterministic as two on the same class. Thread the first hit
        // through so the second scan throws if it finds a competitor.
        Method chosen = scanStaticFactory(type, type, useJacksonAnnotation, null);
        if (mixIn != null) {
            chosen = scanStaticFactory(type, mixIn, useJacksonAnnotation, chosen);
        }
        return chosen;
    }

    /**
     * Resolve the actual invocation target for a factory method chosen by
     * {@link #resolveStaticFactoryMethod}. If the chosen method comes from
     * the mix-in class, mix-in semantics say the body is METADATA and the
     * target class's same-signature method supplies the body. Mirrors
     * {@code resolveConstructor}'s constructor-side behaviour.
     *
     * @param factory the chosen factory (may be declared on target or mix-in)
     * @param type    the target class
     * @return the method to invoke — same as {@code factory} when it's declared
     *         on the target, otherwise the target's matching static method
     */
    private static Method resolveFactoryInvocationTarget(Method factory, Class<?> type) {
        Class<?> declaringClass = factory.getDeclaringClass();
        if (declaringClass == type) {
            return factory;
        }
        // Mix-in path: look up the same-signature static method on the target.
        try {
            Method onTarget = type.getDeclaredMethod(factory.getName(), factory.getParameterTypes());
            if (!Modifier.isStatic(onTarget.getModifiers())
                    || !type.isAssignableFrom(onTarget.getReturnType())) {
                throw new JSONException("@JSONCreator mix-in factory "
                        + declaringClass.getSimpleName() + "." + factory.getName()
                        + " has no matching static method on target " + type.getName());
            }
            onTarget.setAccessible(true);
            return onTarget;
        } catch (NoSuchMethodException e) {
            throw new JSONException("@JSONCreator mix-in factory "
                    + declaringClass.getSimpleName() + "." + factory.getName()
                    + " has no matching static method on target " + type.getName(), e);
        }
    }

    /**
     * Bind a factory method's method-scoped {@link java.lang.reflect.TypeVariable}s
     * to concrete {@link java.lang.reflect.Type}s by unifying the factory's
     * generic return type against the target {@code contextType}. For
     * `<T> Box<T> of(T v)` with contextType `Box<Address>` this produces
     * {@code T → Address}; without this step T collapses to its upper bound
     * (usually Object) at {@link TypeUtils#resolve} time.
     *
     * <p>Returns an empty map when the factory has no method type parameters
     * or the unification isn't possible (e.g., contextType is the raw class).
     */
    private static java.util.Map<java.lang.reflect.TypeVariable<?>, java.lang.reflect.Type> resolveMethodTypeVariables(
            Method factory, java.lang.reflect.Type contextType) {
        java.lang.reflect.TypeVariable<?>[] methodTypeParams = factory.getTypeParameters();
        if (methodTypeParams.length == 0 || !(contextType instanceof ParameterizedType target)) {
            return java.util.Map.of();
        }
        java.lang.reflect.Type genericReturn = factory.getGenericReturnType();
        if (!(genericReturn instanceof ParameterizedType retPT)) {
            return java.util.Map.of();
        }
        // Both sides must share the same raw class for positional arg unification to make sense.
        if (retPT.getRawType() != target.getRawType()) {
            return java.util.Map.of();
        }
        java.lang.reflect.Type[] retArgs = retPT.getActualTypeArguments();
        java.lang.reflect.Type[] targetArgs = target.getActualTypeArguments();
        int n = Math.min(retArgs.length, targetArgs.length);
        java.util.Map<java.lang.reflect.TypeVariable<?>, java.lang.reflect.Type> bindings = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) {
            if (retArgs[i] instanceof java.lang.reflect.TypeVariable<?> tv) {
                // Only bind variables declared on this method — class-level
                // TypeVariables are handled by TypeUtils.resolve against contextType.
                // .equals() not == because Method instances returned by
                // getDeclaredMethods() are freshly allocated per call.
                if (factory.equals(tv.getGenericDeclaration())) {
                    bindings.put(tv, targetArgs[i]);
                }
            }
        }
        return bindings;
    }

    /**
     * Narrow {@code T}-shaped factory parameters: if the generic parameter
     * type is itself a method-scoped TypeVariable present in {@code bindings},
     * return the bound type. Otherwise return the parameter type unchanged and
     * let {@link TypeUtils#resolve} handle class-level TypeVariables against
     * the caller's contextType.
     *
     * <p>Scope is intentionally limited to the direct-TypeVariable case
     * (`T v`) — the common Jackson-migration shape. Nested method-scoped
     * TypeVariables (`List&lt;T&gt; v`) would require synthesising a
     * ParameterizedType, which fj3 doesn't expose publicly. Those fall
     * through to TypeUtils.resolve with class-scope bindings only, which
     * collapses them to the bound (usually Object) — matching the
     * pre-PR-123 behaviour.
     */
    private static java.lang.reflect.Type bindMethodScopedTypeVariable(
            java.lang.reflect.Type paramGeneric,
            java.util.Map<java.lang.reflect.TypeVariable<?>, java.lang.reflect.Type> bindings) {
        if (paramGeneric instanceof java.lang.reflect.TypeVariable<?> tv) {
            java.lang.reflect.Type bound = bindings.get(tv);
            if (bound != null) {
                return bound;
            }
        }
        return paramGeneric;
    }

    private static Method scanStaticFactory(Class<?> owner, Class<?> annotationHost,
                                            boolean useJacksonAnnotation, Method chosen) {
        for (Method m : annotationHost.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) {
                continue;
            }
            if (!owner.isAssignableFrom(m.getReturnType())) {
                continue;
            }
            boolean marked = m.isAnnotationPresent(JSONCreator.class)
                    || (useJacksonAnnotation && JacksonAnnotationSupport.hasJsonCreator(m));
            if (!marked) {
                continue;
            }
            if (chosen != null) {
                // Declared-method order is JVM-unspecified, so silently picking
                // "first found" would be non-deterministic. Match Jackson's
                // behaviour (InvalidDefinitionException on conflicting creators)
                // — with both names and their declaring class so a class-vs-mix-in
                // clash is attributable.
                throw new JSONException("multiple @JSONCreator factory methods for "
                        + owner.getName() + ": "
                        + chosen.getDeclaringClass().getSimpleName() + "." + chosen.getName()
                        + " and "
                        + m.getDeclaringClass().getSimpleName() + "." + m.getName()
                        + " — mark only one");
            }
            chosen = m;
        }
        return chosen;
    }

    /**
     * Build a reader that invokes a {@code static} factory method to materialise
     * the instance. Structurally identical to {@link #createConstructorReader}
     * except the creator lambda calls {@code factory.invoke(null, values)}
     * instead of {@code constructor.newInstance(values)}. All parameter-matching,
     * unwrapped-expansion, and required-field machinery is shared through the
     * single {@link RecordObjectReader} implementation.
     */
    @SuppressWarnings("unchecked")
    private static <T> ObjectReader<T> createStaticFactoryReader(
            Class<T> type, java.lang.reflect.Type contextType, Method factory, Class<?> mixIn, boolean useJacksonAnnotation
    ) {
        factory.setAccessible(true);
        // Mix-in methods carry the @JSONCreator metadata and parameter
        // annotations, but their body is stub code — resolve the same-signature
        // method on the target class for the actual invocation. When `factory`
        // is already declared on `type`, `invocationTarget == factory`.
        Method invocationTarget = resolveFactoryInvocationTarget(factory, type);
        Class<?>[] paramTypes = factory.getParameterTypes();
        java.lang.reflect.Type[] genericParamTypes = factory.getGenericParameterTypes();

        // Method-scoped TypeVariable binding. For `<T> Box<T> of(T v)` with a
        // target of `TypeReference<Box<Address>>`, we want parameter `v` to
        // resolve to Address, not to T's erasure (Object). Class-level
        // TypeUtils.resolve walks only class TypeVariables; method-scoped ones
        // need to be bound from the unification of the factory's return type
        // against the target contextType.
        java.util.Map<java.lang.reflect.TypeVariable<?>, java.lang.reflect.Type> methodBindings
                = resolveMethodTypeVariables(factory, contextType);

        List<Field> instanceFields = getInstanceFieldsParentFirst(type);

        JSONType jsonType = type.getAnnotation(JSONType.class);
        if (jsonType == null && mixIn != null) {
            jsonType = mixIn.getAnnotation(JSONType.class);
        }
        NamingStrategy naming = jsonType != null ? jsonType.naming() : NamingStrategy.NoneStrategy;
        if (useJacksonAnnotation && naming == NamingStrategy.NoneStrategy) {
            JacksonAnnotationSupport.BeanInfo jackson = JacksonAnnotationSupport.getBeanInfo(type);
            if (jackson != null && jackson.naming() != null) {
                naming = jackson.naming();
            }
        }

        // Parameter-name source: @JSONCreator(parameterNames=) override first,
        // then @JSONField(name=…) on each parameter, then the parameter's
        // compiled name (requires -parameters on javac; falls back to arg0/arg1
        // otherwise, which still works if every parameter carries @JSONField).
        JSONCreator creatorAnn = factory.getAnnotation(JSONCreator.class);
        String[] overrideNames = (creatorAnn != null) ? creatorAnn.parameterNames() : new String[0];
        Parameter[] parameters = factory.getParameters();

        List<FieldReader> fieldReaderList = new ArrayList<>();
        for (int i = 0; i < paramTypes.length; i++) {
            Parameter p = parameters[i];
            JSONField annotation = p.getAnnotation(JSONField.class);

            // @JSONField(unwrapped=true) would need the record/ctor-path's
            // scratch-inner machinery (paramMapping + expandRecordUnwrapped),
            // which this reader doesn't wire up. Silently dropping the
            // annotation would lose inner-field routing; reject loudly so the
            // user knows to use a constructor or direct record instead.
            if (annotation != null && annotation.unwrapped()) {
                throw new JSONException("@JSONField(unwrapped=true) on factory parameter '"
                        + p.getName() + "' of " + factory.getDeclaringClass().getName()
                        + "." + factory.getName()
                        + " is not supported — use a constructor or record instead");
            }

            String rawName;
            if (overrideNames.length > i && !overrideNames[i].isEmpty()) {
                rawName = overrideNames[i];
            } else if (annotation != null && !annotation.name().isEmpty()) {
                rawName = annotation.name();
            } else {
                rawName = p.getName();
            }

            String jsonName = resolveFieldName(rawName, annotation, naming);
            String[] alternateNames = annotation != null ? annotation.alternateNames() : new String[0];
            int ordinal = annotation != null ? annotation.ordinal() : 0;
            String defaultValue = annotation != null ? annotation.defaultValue() : "";
            boolean required = annotation != null && annotation.required();
            String format = (annotation != null && !annotation.format().isEmpty()) ? annotation.format() : null;
            Class<?> deserializeUsingClass = (annotation != null && annotation.deserializeUsing() != Void.class)
                    ? annotation.deserializeUsing() : null;
            String schema = (annotation != null && !annotation.schema().isEmpty()) ? annotation.schema() : null;

            // Two-step resolution: first bind any method-scoped TypeVariable
            // direct hit from the unified bindings, then run the standard
            // context-type resolve for class-level TypeVariables. When the
            // binding substitutes `T` for a concrete class, the `fieldClass`
            // must also follow — paramTypes[i] is already erased to Object
            // by the compiler, so without this narrowing the Map→POJO
            // conversion won't know what type to build.
            java.lang.reflect.Type boundGeneric = methodBindings.isEmpty()
                    ? genericParamTypes[i]
                    : bindMethodScopedTypeVariable(genericParamTypes[i], methodBindings);
            java.lang.reflect.Type resolvedType = TypeUtils.resolve(boundGeneric, contextType);
            // Narrow `fieldClass` whenever the binding gives us a strictly
            // more specific type than the erased paramTypes[i]. Round 4 only
            // narrowed when paramTypes[i] was literally Object.class — but a
            // BOUNDED TypeVariable like `<T extends Bean>` erases to Bean,
            // not Object, so the narrow skipped and resolvedClass stayed at
            // Bean, silently dropping SubBean-only fields for a target of
            // Box<SubBean>. The widened check (isAssignableFrom) covers both
            // cases without regressing the Object→Anything path.
            Class<?> resolvedClass = paramTypes[i];
            if (resolvedType instanceof Class<?> rc && paramTypes[i].isAssignableFrom(rc)) {
                resolvedClass = rc;
            } else if (resolvedType instanceof ParameterizedType rpt
                    && rpt.getRawType() instanceof Class<?> rawRc
                    && paramTypes[i].isAssignableFrom(rawRc)) {
                resolvedClass = rawRc;
            }
            fieldReaderList.add(new FieldReader(
                    jsonName, alternateNames,
                    resolvedType, resolvedClass,
                    ordinal, defaultValue, required,
                    null, null, format, deserializeUsingClass, schema, 0, useJacksonAnnotation
            ));
        }

        Collections.sort(fieldReaderList);
        FieldReader[] fieldReaders = fieldReaderList.toArray(new FieldReader[0]);
        FieldNameMatcher matcher = FieldNameMatcher.build(fieldReaders);

        Map<String, FieldReader> fieldReaderMap = HashMap.newHashMap(fieldReaders.length * 2);
        for (int i = 0; i < fieldReaders.length; i++) {
            fieldReaders[i].index = i;
            fieldReaderMap.put(fieldReaders[i].fieldName, fieldReaders[i]);
            for (String alt : fieldReaders[i].alternateNames) {
                fieldReaderMap.put(alt, fieldReaders[i]);
            }
        }

        // paramMapping: fieldReader index → factory parameter slot. Parameters
        // are filled positionally, so the fieldReader's *parameter index* (its
        // declared order in the factory signature) is the slot.
        int[] paramMapping = new int[fieldReaders.length];
        for (int i = 0; i < fieldReaders.length; i++) {
            String name = fieldReaders[i].fieldName;
            for (int j = 0; j < paramTypes.length; j++) {
                String pName;
                if (overrideNames.length > j && !overrideNames[j].isEmpty()) {
                    pName = overrideNames[j];
                } else {
                    JSONField pAnn = parameters[j].getAnnotation(JSONField.class);
                    pName = (pAnn != null && !pAnn.name().isEmpty())
                            ? pAnn.name()
                            : applyNamingStrategy(parameters[j].getName(), naming);
                }
                if (pName.equals(name) || parameters[j].getName().equals(name)) {
                    paramMapping[i] = j;
                    break;
                }
            }
        }

        InstanceInvoker<T> invoker = values -> (T) invocationTarget.invoke(null, values);
        return new RecordObjectReader<>(type, invoker, paramTypes, fieldReaders,
                fieldReaderMap, matcher, paramTypes.length, paramMapping,
                null, null, null);
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> resolveConstructor(Class<T> type, Class<?> mixIn, boolean useJacksonAnnotation) {
        // 1. Check for @JSONCreator annotated constructor
        for (Constructor<?> ctor : type.getDeclaredConstructors()) {
            if (ctor.isAnnotationPresent(JSONCreator.class)) {
                return (Constructor<T>) ctor; // supports both no-arg and with-params
            }
        }
        // 2. Check Jackson @JsonCreator
        if (useJacksonAnnotation) {
            for (Constructor<?> ctor : type.getDeclaredConstructors()) {
                if (JacksonAnnotationSupport.hasJsonCreator(ctor)) {
                    return (Constructor<T>) ctor;
                }
            }
        }
        // 3. Check mixin constructors for @JSONCreator
        if (mixIn != null) {
            for (Constructor<?> mixCtor : mixIn.getDeclaredConstructors()) {
                if (mixCtor.isAnnotationPresent(JSONCreator.class)) {
                    if (mixCtor.getParameterCount() == 0) {
                        try {
                            return type.getDeclaredConstructor();
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                }
            }
        }
        // 4. Prefer no-arg constructor
        try {
            return type.getDeclaredConstructor();
        } catch (NoSuchMethodException ignored) {
        }
        // 5. Fall back to single all-args constructor (Kotlin data classes, Java immutable classes)
        Constructor<?> candidate = findAllArgsConstructor(type);
        if (candidate != null) {
            return (Constructor<T>) candidate;
        }
        throw new JSONException("no suitable constructor found for " + type.getName());
    }

    /**
     * Find a single non-synthetic constructor whose parameter count matches the instance field count.
     * This handles Kotlin data classes and Java immutable classes with all-args constructors.
     */
    private static Constructor<?> findAllArgsConstructor(Class<?> type) {
        // Skip non-static inner classes (have synthetic this$0 field + outer class constructor param)
        if (type.getEnclosingClass() != null && !Modifier.isStatic(type.getModifiers())) {
            return null;
        }

        // Count instance fields (includes superclass, excludes synthetic)
        int fieldCount = getInstanceFieldsParentFirst(type).size();
        if (fieldCount == 0) {
            return null;
        }

        // Find non-synthetic constructors
        Constructor<?>[] ctors = type.getDeclaredConstructors();
        Constructor<?> candidate = null;
        int candidateCount = 0;
        for (Constructor<?> ctor : ctors) {
            if (ctor.isSynthetic()) {
                continue;
            }
            // Skip Kotlin default-value synthetic constructors (have DefaultConstructorMarker param)
            Class<?>[] paramTypes = ctor.getParameterTypes();
            if (paramTypes.length > 0) {
                String lastParam = paramTypes[paramTypes.length - 1].getName();
                if (lastParam.contains("DefaultConstructorMarker")) {
                    continue;
                }
            }
            if (ctor.getParameterCount() == fieldCount) {
                candidate = ctor;
                candidateCount++;
            }
        }

        // Only use if exactly one matching constructor (unambiguous)
        return candidateCount == 1 ? candidate : null;
    }

    private static List<Field> getDeclaredFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * Collect instance fields in parent-first order (superclass fields before subclass fields).
     * Excludes static, transient, and synthetic fields.
     * Used by constructor-based readers where parameter order matches parent-first field order.
     */
    /**
     * Find an instance (non-static) field by name in the class hierarchy. Walks
     * {@code type} then each superclass, returning the first match. Used by the
     * setter-backed {@code @JSONField(unwrapped=true)} branch so an inherited
     * holder field satisfies the backing-field requirement the same way
     * declared-on-current-class fields do.
     */
    private static Field findInstanceFieldInHierarchy(Class<?> type, String name) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                if (!Modifier.isStatic(f.getModifiers())) {
                    return f;
                }
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private static List<Field> getInstanceFieldsParentFirst(Class<?> type) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }
        Collections.reverse(hierarchy);

        List<Field> fields = new ArrayList<>();
        for (Class<?> cls : hierarchy) {
            for (Field f : cls.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())
                        && !f.isSynthetic()) {
                    fields.add(f);
                }
            }
        }
        return fields;
    }

    /**
     * Scan for @JSONField(anySetter = true) or Jackson @JsonAnySetter on 2-param methods.
     * Method signature must be void xxx(String key, Object value).
     */
    private static JSONSchema parseTypeSchema(Class<?> type) {
        JSONType jsonType = type.getAnnotation(JSONType.class);
        if (jsonType != null && !jsonType.schema().isEmpty()) {
            JSONObject schemaObj = com.alibaba.fastjson3.JSON.parseObject(jsonType.schema());
            if (schemaObj != null && !schemaObj.isEmpty()) {
                return JSONSchema.of(schemaObj);
            }
        }
        return null;
    }

    /**
     * Mirror {@link com.alibaba.fastjson3.writer.ObjectWriterCreator}'s typeKey
     * resolution so anySetter routing can skip the same discriminator key the
     * writer will emit. Returns null when no annotation source dictates a
     * discriminator (the class serializes without @type and the user may use
     * "@type" as a legitimate business field).
     *
     * <p>Resolution order matches the writer:
     * <ol>
     *   <li>@JSONType(typeKey=) on class or mix-in</li>
     *   <li>@JSONType(typeName=) present (defaults to "@type")</li>
     *   <li>Jackson @JsonTypeInfo via bean info, when useJacksonAnnotation</li>
     *   <li>Inherited @JSONType(seeAlso=) ancestor (defaults to "@type")</li>
     * </ol>
     */
    private static String resolveAnySetterTypeKey(Class<?> type, Class<?> mixIn, boolean useJacksonAnnotation) {
        JSONType jsonType = type.getAnnotation(JSONType.class);
        if (jsonType == null && mixIn != null) {
            jsonType = mixIn.getAnnotation(JSONType.class);
        }
        if (jsonType != null) {
            if (!jsonType.typeKey().isEmpty()) {
                return jsonType.typeKey();
            }
            if (!jsonType.typeName().isEmpty()) {
                return "@type";
            }
        }
        if (useJacksonAnnotation) {
            JacksonAnnotationSupport.BeanInfo bean = JacksonAnnotationSupport.getBeanInfo(type);
            if (bean != null && bean.typeKey() != null && !bean.typeKey().isEmpty()) {
                return bean.typeKey();
            }
        }
        // Inherited @JSONType(seeAlso=) ancestor — if any ancestor lists this
        // type as a sub-type, the writer will emit the ancestor's discriminator.
        JSONType ancestor = findSeeAlsoAncestorForTypeKey(type);
        if (ancestor != null) {
            return !ancestor.typeKey().isEmpty() ? ancestor.typeKey() : "@type";
        }
        return null;
    }

    /**
     * Walk the superclass + interface closure looking for an ancestor whose
     * {@code @JSONType(seeAlso)} lists {@code type}. Returns the ancestor's
     * JSONType annotation or null. Minimal mirror of
     * {@code ObjectWriterCreator.findSeeAlsoAncestor} scoped to the fields
     * {@code resolveAnySetterTypeKey} needs.
     */
    private static JSONType findSeeAlsoAncestorForTypeKey(Class<?> type) {
        java.util.Set<Class<?>> visited = new java.util.HashSet<>();
        java.util.Deque<Class<?>> stack = new java.util.ArrayDeque<>();
        Class<?> superCls = type.getSuperclass();
        if (superCls != null && superCls != Object.class) {
            stack.push(superCls);
        }
        for (Class<?> i : type.getInterfaces()) {
            stack.push(i);
        }
        while (!stack.isEmpty()) {
            Class<?> current = stack.pop();
            if (!visited.add(current) || current == Object.class) {
                continue;
            }
            JSONType jt = current.getAnnotation(JSONType.class);
            if (jt != null) {
                for (Class<?> sub : jt.seeAlso()) {
                    if (sub == type) {
                        return jt;
                    }
                }
            }
            Class<?> sup = current.getSuperclass();
            if (sup != null && sup != Object.class) {
                stack.push(sup);
            }
            for (Class<?> i : current.getInterfaces()) {
                stack.push(i);
            }
        }
        return null;
    }

    private static Method findAnySetterMethod(Class<?> type, Class<?> mixIn, boolean useJacksonAnnotation) {
        for (Method method : type.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 2) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params[0] != String.class) {
                continue;
            }
            JSONField jsonField = method.getAnnotation(JSONField.class);
            if (jsonField == null && mixIn != null) {
                // Mix-in can declare @JSONField(anySetter=true) on a method with the same
                // name + parameter types as the target's setter. Without this lookup, a
                // user can't adapt a third-party bean's catch-all method from a mix-in.
                jsonField = findMixInMethodAnnotation(mixIn, method, JSONField.class);
            }
            if (jsonField != null && jsonField.anySetter()) {
                method.setAccessible(true);
                return method;
            }
            if (useJacksonAnnotation) {
                if (hasJsonAnySetter(method.getAnnotations())) {
                    method.setAccessible(true);
                    return method;
                }
                if (mixIn != null) {
                    Method mixInMethod = findMixInMethod(mixIn, method);
                    if (mixInMethod != null && hasJsonAnySetter(mixInMethod.getAnnotations())) {
                        method.setAccessible(true);
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private static boolean hasJsonAnySetter(java.lang.annotation.Annotation[] annotations) {
        for (java.lang.annotation.Annotation ann : annotations) {
            if ("com.fasterxml.jackson.annotation.JsonAnySetter".equals(ann.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Locate the mix-in method whose signature matches {@code targetMethod}. Matches by
     * exact name + parameter types — used for lookup of method-level annotations that
     * don't follow the JavaBean setter naming convention (like anySetter / anyGetter).
     *
     * Walks {@code getMethods()} first so inherited annotations from a parent mix-in
     * interface (e.g. {@code ChildMixIn extends BaseMixIn}) are visible, then falls
     * back to {@code getDeclaredMethods()} so package-private mix-in methods are
     * still discoverable.
     */
    private static Method findMixInMethod(Class<?> mixIn, Method targetMethod) {
        String name = targetMethod.getName();
        Class<?>[] params = targetMethod.getParameterTypes();
        for (Method m : mixIn.getMethods()) {
            if (m.getName().equals(name) && java.util.Arrays.equals(m.getParameterTypes(), params)) {
                return m;
            }
        }
        for (Class<?> c = mixIn; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name) && java.util.Arrays.equals(m.getParameterTypes(), params)) {
                    return m;
                }
            }
        }
        for (Class<?> iface : collectAllInterfaces(mixIn)) {
            for (Method m : iface.getDeclaredMethods()) {
                if (m.getName().equals(name) && java.util.Arrays.equals(m.getParameterTypes(), params)) {
                    return m;
                }
            }
        }
        return null;
    }

    private static java.util.Set<Class<?>> collectAllInterfaces(Class<?> start) {
        java.util.Set<Class<?>> out = new java.util.LinkedHashSet<>();
        java.util.Deque<Class<?>> stack = new java.util.ArrayDeque<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            Class<?> c = stack.pop();
            for (Class<?> iface : c.getInterfaces()) {
                if (out.add(iface)) {
                    stack.push(iface);
                }
            }
            Class<?> sup = c.getSuperclass();
            if (sup != null && sup != Object.class) {
                stack.push(sup);
            }
        }
        return out;
    }

    private static <A extends java.lang.annotation.Annotation> A findMixInMethodAnnotation(
            Class<?> mixIn, Method targetMethod, Class<A> annotationType) {
        Method m = findMixInMethod(mixIn, targetMethod);
        return m != null ? m.getAnnotation(annotationType) : null;
    }

    private static String extractPropertyName(Method method) {
        String name = method.getName();
        if (name.length() > 3 && name.startsWith("set") && Character.isUpperCase(name.charAt(3))) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        return null;
    }

    private static String resolveFieldName(String rawName, JSONField annotation, NamingStrategy naming) {
        if (annotation != null && !annotation.name().isEmpty()) {
            return annotation.name();
        }
        return applyNamingStrategy(rawName, naming);
    }

    static String applyNamingStrategy(String name, NamingStrategy strategy) {
        return switch (strategy) {
            case NoneStrategy, CamelCase -> name;
            case PascalCase -> Character.toUpperCase(name.charAt(0)) + name.substring(1);
            case SnakeCase -> camelToSeparator(name, '_', false);
            case UpperSnakeCase -> camelToSeparator(name, '_', true);
            case KebabCase -> camelToSeparator(name, '-', false);
            case UpperKebabCase -> camelToSeparator(name, '-', true);
        };
    }

    private static String camelToSeparator(String name, char separator, boolean uppercase) {
        StringBuilder sb = new StringBuilder(name.length() + 4);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append(separator);
                }
                sb.append(uppercase ? c : Character.toLowerCase(c));
            } else {
                sb.append(uppercase ? Character.toUpperCase(c) : c);
            }
        }
        return sb.toString();
    }

    // ==================== Mixin support ====================

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

    /**
     * Find annotation on the corresponding setter method in the mixin class.
     * Walks the mix-in's inheritance chain (superclass + full interface closure) so
     * a {@code ChildMixIn extends BaseMixIn} pattern surfaces annotations declared
     * on BaseMixIn. Without this, any-setter / unwrapped / etc. annotations coming
     * from a parent mix-in are silently lost.
     */
    static <A extends java.lang.annotation.Annotation> A findMixInSetterAnnotation(
            Class<?> mixIn, Method targetMethod, String propertyName, Class<A> annotationType) {
        String setterName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        Class<?>[] targetParams = targetMethod.getParameterTypes();

        // Primary search: exact signature match on any class / interface in the mix-in
        // inheritance closure. getMethods() covers public inherited methods first.
        for (Method m : mixIn.getMethods()) {
            if (m.getName().equals(targetMethod.getName())
                    && java.util.Arrays.equals(m.getParameterTypes(), targetParams)) {
                A ann = m.getAnnotation(annotationType);
                if (ann != null) {
                    return ann;
                }
            }
        }
        // Non-public inherited methods: walk the mix-in's superclasses + interfaces.
        for (Class<?> c = mixIn; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(targetMethod.getName())
                        && java.util.Arrays.equals(m.getParameterTypes(), targetParams)) {
                    A ann = m.getAnnotation(annotationType);
                    if (ann != null) {
                        return ann;
                    }
                }
            }
        }
        for (Class<?> iface : collectAllInterfaces(mixIn)) {
            for (Method m : iface.getDeclaredMethods()) {
                if (m.getName().equals(targetMethod.getName())
                        && java.util.Arrays.equals(m.getParameterTypes(), targetParams)) {
                    A ann = m.getAnnotation(annotationType);
                    if (ann != null) {
                        return ann;
                    }
                }
            }
        }

        // Secondary: setter-naming fallback — mix-in may name its setter differently
        // from the target (e.g. via @JSONField(name=...)). Matches by name alone,
        // 1-arg only, walking the same inheritance closure.
        for (Method m : mixIn.getMethods()) {
            if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                A ann = m.getAnnotation(annotationType);
                if (ann != null) {
                    return ann;
                }
            }
        }
        for (Class<?> c = mixIn; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                    A ann = m.getAnnotation(annotationType);
                    if (ann != null) {
                        return ann;
                    }
                }
            }
        }
        for (Class<?> iface : collectAllInterfaces(mixIn)) {
            for (Method m : iface.getDeclaredMethods()) {
                if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                    A ann = m.getAnnotation(annotationType);
                    if (ann != null) {
                        return ann;
                    }
                }
            }
        }
        return null;
    }
}

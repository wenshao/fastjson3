package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectWriter;
import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;
import com.alibaba.fastjson3.annotation.NamingStrategy;
import com.alibaba.fastjson3.internal.asm.ClassWriter;
import com.alibaba.fastjson3.internal.asm.Label;
import com.alibaba.fastjson3.internal.asm.MethodWriter;
import com.alibaba.fastjson3.internal.asm.Opcodes;
import com.alibaba.fastjson3.util.DynamicClassLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.fastjson3.internal.asm.ASMUtils.DESC_JSON_GENERATOR;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JSON_GENERATOR;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JDK_UTILS;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_OBJECT_WRITER;

/**
 * Creates {@link ObjectWriter} instances via ASM bytecode generation.
 * Generated writers use Unsafe to read bean fields (via JDKUtils) and call
 * JSONGenerator methods, avoiding FieldWriter switch dispatch and reflection.
 *
 * <p>Using Unsafe instead of getfield allows the generated code to work
 * across ClassLoader boundaries, enabling ASM optimization for user classes
 * loaded by different classloaders.</p>
 *
 * <p>Fallback: if ASM generation fails for a type, delegates to
 * {@link ObjectWriterCreator#createObjectWriter(Class)} (reflection).</p>
 */
@com.alibaba.fastjson3.annotation.JVMOnly
public final class ObjectWriterCreatorASM {
    private static final AtomicLong SEED = new AtomicLong();
    private static final DynamicClassLoader CLASS_LOADER = DynamicClassLoader.getInstance();

    // Generated class implements ObjectWriter interface
    private static final String[] INTERFACES = {TYPE_OBJECT_WRITER};

    // ObjectWriter.write method descriptor
    private static final String METHOD_DESC_WRITE =
            "(" + DESC_JSON_GENERATOR + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";

    private ObjectWriterCreatorASM() {
    }

    /**
     * Per-thread guard against infinite recursion when the ASM creator
     * eagerly resolves nested POJO / list-element writers. Each
     * {@link #generateWriter} call adds its bean type before recursing; the
     * recursive call sees the type in the set and falls back to a runtime
     * lookup rather than looping. Cleared on outermost return.
     */
    private static final ThreadLocal<java.util.Set<Class<?>>> CREATING =
            ThreadLocal.withInitial(java.util.HashSet::new);

    /**
     * Returns true if {@code fc} is a "nested POJO" the writer generator
     * can pre-cache an ASM child writer for. Excludes primitives/String/
     * boxed wrappers/enums/Maps/Collections/arrays/Date-time/UUID — those
     * have specialized fast paths or tight built-in codecs.
     */
    private static boolean isNestedPojo(Class<?> fc) {
        if (fc == null || fc.isPrimitive() || fc.isArray() || fc.isInterface()
                || fc.isEnum() || fc == String.class || fc == Object.class) {
            return false;
        }
        if (Modifier.isAbstract(fc.getModifiers())) {
            return false;
        }
        if (fc == Integer.class || fc == Long.class || fc == Double.class
                || fc == Float.class || fc == Boolean.class || fc == Byte.class
                || fc == Short.class || fc == Character.class
                || fc == java.util.UUID.class || fc == java.util.Date.class
                || fc == java.math.BigDecimal.class || fc == java.math.BigInteger.class
                || java.time.temporal.TemporalAccessor.class.isAssignableFrom(fc)
                || java.util.Map.class.isAssignableFrom(fc)
                || java.util.Collection.class.isAssignableFrom(fc)) {
            return false;
        }
        return Modifier.isPublic(fc.getModifiers());
    }

    /**
     * Create an ObjectWriter for the given type via ASM bytecode generation.
     * Generated writers use direct getfield access and invoke JSONGenerator's
     * writeNameXxx methods for each field, with upfront ensureCapacity.
     * Falls back to reflection if ASM generation fails.
     *
     * <p>Note: ASM writers skip null fields by default (standard JSON behavior).
     * For {@code WriteNulls} support, use the reflection-based writer path.</p>
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectWriter<T> createObjectWriter(Class<T> type) {
        // native-image: runtime bytecode generation is not supported, fall back to reflection
        if (com.alibaba.fastjson3.util.JDKUtils.NATIVE_IMAGE) {
            return ObjectWriterCreator.createObjectWriter(type);
        }

        if (!canGenerate(type)) {
            return ObjectWriterCreator.createObjectWriter(type);
        }

        try {
            return (ObjectWriter<T>) generateWriter(type);
        } catch (Throwable e) {
            // Log fallback for debugging
            com.alibaba.fastjson3.util.Logger.warn("ASM generation failed for " + type.getName() + ", using reflection: " + e.getMessage());
            return ObjectWriterCreator.createObjectWriter(type);
        }
    }

    private static boolean canGenerate(Class<?> type) {
        if (type.isInterface() || type.isArray() || type.isEnum()
                || type.isPrimitive() || Modifier.isAbstract(type.getModifiers())) {
            return false;
        }

        // ClassLoader compatibility check:
        // Using Unsafe instead of getfield allows cross-ClassLoader access
        // Named modules are still blocked (strong encapsulation).
        Module targetModule = type.getModule();
        if (targetModule.isNamed()) {
            return false;
        }

        // Check if class is accessible for ASM field access operations
        // 1. Public classes are always accessible
        if (Modifier.isPublic(type.getModifiers())) {
            // continue to other checks
        } else if (type.isMemberClass() && Modifier.isStatic(type.getModifiers())) {
            // 2. Static inner classes of public classes are also accessible
            Class<?> enclosing = type.getEnclosingClass();
            if (enclosing == null || !Modifier.isPublic(enclosing.getModifiers())) {
                // 3. Non-public static member classes can be accessed
                // via reflection from ASM code - allow them
            }
        } else {
            // Non-public non-static class - not accessible
            return false;
        }

        // @JSONType(schema=) requires reflection path for special handling
        com.alibaba.fastjson3.annotation.JSONType jsonType = type.getAnnotation(
                com.alibaba.fastjson3.annotation.JSONType.class);
        if (jsonType != null && !jsonType.schema().isEmpty()) {
            return false;
        }

        // @JSONField(format=) requires reflection path for custom formatting
        // @JSONField(inclusion=) requires reflection path for custom inclusion logic
        for (java.lang.reflect.Field field : type.getDeclaredFields()) {
            com.alibaba.fastjson3.annotation.JSONField jsonField = field.getAnnotation(
                    com.alibaba.fastjson3.annotation.JSONField.class);
            if (jsonField != null) {
                if (!jsonField.format().isEmpty()) {
                    return false;
                }
                if (jsonField.inclusion() != com.alibaba.fastjson3.annotation.Inclusion.ALWAYS) {
                    return false;
                }
            }
        }
        // Also check methods for @JSONField(format=) and @JSONField(inclusion=)
        for (java.lang.reflect.Method method : type.getMethods()) {
            com.alibaba.fastjson3.annotation.JSONField jsonField = method.getAnnotation(
                    com.alibaba.fastjson3.annotation.JSONField.class);
            if (jsonField != null) {
                if (!jsonField.format().isEmpty()) {
                    return false;
                }
                if (jsonField.inclusion() != com.alibaba.fastjson3.annotation.Inclusion.ALWAYS) {
                    return false;
                }
            }
        }

        // @JSONType(inclusion=) requires reflection path for custom inclusion logic
        if (jsonType != null && jsonType.inclusion() != com.alibaba.fastjson3.annotation.Inclusion.ALWAYS) {
            return false;
        }

        return true;
    }

    private static ObjectWriter<?> generateWriter(Class<?> beanType) {
        // 1. Collect field info (same logic as ObjectWriterCreator)
        List<FieldWriterInfo> fields = collectFields(beanType);
        if (fields.isEmpty()) {
            return ObjectWriterCreator.createObjectWriter(beanType);
        }

        // 2. Check for boxed primitive fields - these need null handling logic
        // which is complex in ASM. Fall back to reflection for such types.
        for (FieldWriterInfo fi : fields) {
            if (fi.fieldClass == Integer.class || fi.fieldClass == Long.class ||
                fi.fieldClass == Double.class || fi.fieldClass == Float.class ||
                fi.fieldClass == Boolean.class || fi.fieldClass == Byte.class ||
                fi.fieldClass == Short.class || fi.fieldClass == Character.class) {
                // Has boxed primitive field - use reflection for proper null handling
                return ObjectWriterCreator.createObjectWriter(beanType);
            }
            // Check that all fields have direct field access (no getter-only properties)
            // This avoids cross-ClassLoader issues with invokevirtual calls
            if (fi.field == null) {
                // No field access available - would need to call getter method
                // which requires class reference that may fail across ClassLoaders
                return ObjectWriterCreator.createObjectWriter(beanType);
            }
        }

        // 3. Generate bytecode
        String beanInternalName = beanType.getName().replace('.', '/');
        String className = "com.alibaba.fastjson3.writer.gen.OW_"
                + beanType.getSimpleName() + "_" + SEED.getAndIncrement();
        String classInternalName = className.replace('.', '/');

        ClassWriter cw = new ClassWriter(null);
        cw.visit(Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                classInternalName, "java/lang/Object", INTERFACES);

        // Generate static fields for field offsets (for Unsafe access)
        for (int i = 0; i < fields.size(); i++) {
            cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "fo" + i, "J");  // fieldOffset
        }

        // Generate static fields for pre-encoded name data
        for (int i = 0; i < fields.size(); i++) {
            cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "nc" + i, "[C");  // nameChars
            cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "nb" + i, "[B");  // nameBytes
            cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "nl" + i, "[J");  // nameByteLongs
            cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "nn" + i, "I");   // nameBytesLen
        }

        // Per-class instance field holding pre-resolved nested writers.
        // Slot i holds an ObjectWriter for the i-th field if it's a nested
        // POJO (or List<POJO> element), null otherwise. Populated by the
        // generated constructor from the array passed in by generateWriter.
        cw.visitField(Opcodes.ACC_FINAL, "ow", "[Lcom/alibaba/fastjson3/ObjectWriter;");

        // Pre-encoded enum-value token cache. Slot i is non-null only for
        // TYPE_ENUM fields; slot i's inner byte[][] is indexed by enum ordinal
        // and holds the full `"fieldName":"ENUM_VALUE",` blob. One System.arraycopy
        // per enum field write, no instanceof ladder through writeAny.
        cw.visitField(Opcodes.ACC_FINAL, "eb", "[[[B");

        // Generate <clinit> to initialize name data
        generateClinit(cw, classInternalName, fields);

        // Generate constructor (now takes ObjectWriter[] for nested writers)
        generateInit(cw, classInternalName);

        // Generate write method
        generateWriteMethod(cw, classInternalName, beanType, beanInternalName, fields);

        // Load class
        byte[] bytecode = cw.toByteArray();
        Class<?> writerClass = CLASS_LOADER.loadClass(className, bytecode, 0, bytecode.length);

        // Eagerly resolve per-field nested writers via recursive ASM creation.
        // Mirrors PR #74 on the read side. Slot i holds an OW_<X> for either:
        //   - a nested POJO field's type (consumed by generateWriteGeneric),
        //   - a List<E> element type (consumed by W#4's inline list loop in
        //     generateWriteListPojo).
        // The dispatch is picked at gen time based on the field type so the
        // call site always passes the right value (field value vs each
        // element). Slots stay null for fields with no cacheable child writer.
        ObjectWriter<?>[] nestedWriters = new ObjectWriter<?>[fields.size()];
        byte[][][] enumBytes = new byte[fields.size()][][];
        java.util.Set<Class<?>> creating = CREATING.get();
        boolean ownGuard = creating.add(beanType);
        try {
            for (int i = 0; i < fields.size(); i++) {
                FieldWriterInfo fi = fields.get(i);
                Class<?> fc = fi.fieldClass;

                // TYPE_ENUM blob precompute: bake the whole
                // `"fieldName":"ENUM_VALUE",` token per ordinal. Keeps the
                // write-time path to one System.arraycopy, matches fj2's
                // FieldWriterEnum cache shape.
                if (fc != null && fc.isEnum()) {
                    try {
                        Object[] constants = fc.getEnumConstants();
                        if (constants != null && constants.length > 0) {
                            byte[][] blobs = new byte[constants.length][];
                            byte[] namePrefix = ("\"" + fi.jsonName + "\":\"")
                                    .getBytes(java.nio.charset.StandardCharsets.UTF_8);
                            byte[] suffix = new byte[] {'"', ','};
                            for (int j = 0; j < constants.length; j++) {
                                byte[] enumNameBytes = ((Enum<?>) constants[j])
                                        .name().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                                byte[] blob = new byte[namePrefix.length + enumNameBytes.length + suffix.length];
                                System.arraycopy(namePrefix, 0, blob, 0, namePrefix.length);
                                System.arraycopy(enumNameBytes, 0, blob, namePrefix.length, enumNameBytes.length);
                                System.arraycopy(suffix, 0, blob, namePrefix.length + enumNameBytes.length, suffix.length);
                                blobs[j] = blob;
                            }
                            enumBytes[i] = blobs;
                        }
                    } catch (Throwable ignored) {
                        // leave null → falls back to writeAny at runtime
                    }
                }

                Class<?> target = null;
                if (isNestedPojo(fc)) {
                    target = fc;
                } else if (fc != null && java.util.List.class.isAssignableFrom(fc)) {
                    Class<?> elem = resolveListElementType(beanType, fi);
                    if (elem != null && isNestedPojo(elem)) {
                        target = elem;
                    }
                }
                if (target == null || creating.contains(target)) {
                    continue;
                }
                try {
                    // Check BuiltinCodecs first — types like Optional, URI,
                    // Path, Year, Duration, Period, etc. live in named JDK
                    // modules so canGenerate() rejects them, and a generic
                    // REFLECT writer would mis-serialize them. The codec
                    // returns the special writer that matches what the old
                    // writeAny path would have called, preserving correctness.
                    ObjectWriter<?> child = com.alibaba.fastjson3.BuiltinCodecs.getWriter(target);
                    if (child == null) {
                        child = createObjectWriter(target);
                    }
                    nestedWriters[i] = child;
                } catch (Throwable ignored) {
                    // leave null → falls back to writeAny at runtime
                }
            }
        } finally {
            if (ownGuard) {
                creating.remove(beanType);
            }
        }

        try {
            return (ObjectWriter<?>) writerClass
                    .getConstructor(ObjectWriter[].class, byte[][][].class)
                    .newInstance(nestedWriters, enumBytes);
        } catch (Exception e) {
            throw new JSONException("Failed to instantiate generated writer for " + beanType.getName(), e);
        }
    }

    /**
     * Best-effort resolution of the element class for a {@code List<E>}
     * field. Reads the generic type signature from the backing field; if
     * there's only a getter, reads the generic return type. Returns null
     * for raw {@code List} or wildcards (which can't be cached statically).
     */
    private static Class<?> resolveListElementType(Class<?> beanType, FieldWriterInfo fi) {
        java.lang.reflect.Type genericType = null;
        if (fi.field != null) {
            genericType = fi.field.getGenericType();
        } else if (fi.getter != null) {
            genericType = fi.getter.getGenericReturnType();
        }
        if (genericType instanceof java.lang.reflect.ParameterizedType pt) {
            java.lang.reflect.Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?> c) {
                return c;
            }
        }
        return null;
    }

    // ==================== Bytecode generation methods ====================

    private static void generateClinit(ClassWriter cw, String classInternalName, List<FieldWriterInfo> fields) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", 32);

        for (int i = 0; i < fields.size(); i++) {
            FieldWriterInfo fi = fields.get(i);
            String encodedName = "\"" + fi.jsonName + "\":";

            // fieldOffset: foN = <constant offset>
            mw.visitLdcInsn(fi.fieldOffset)
                    .putstatic(classInternalName, "fo" + i, "J");

            // nameChars: char[]
            pushString(mw, encodedName);
            mw.invokevirtual("java/lang/String", "toCharArray", "()[C")
                    .putstatic(classInternalName, "nc" + i, "[C");

            // nameBytes: byte[] (UTF-8)
            pushString(mw, encodedName);
            mw.getstatic("java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;");
            mw.invokevirtual("java/lang/String", "getBytes",
                    "(Ljava/nio/charset/Charset;)[B");
            mw.putstatic(classInternalName, "nb" + i, "[B");

            // nameByteLongs: call FieldWriter.encodeByteLongs(nameBytes)
            mw.getstatic(classInternalName, "nb" + i, "[B");
            mw.invokestatic("com/alibaba/fastjson3/writer/FieldWriter",
                    "encodeByteLongs", "([B)[J");
            mw.putstatic(classInternalName, "nl" + i, "[J");

            // nameBytesLen: nameBytes.length
            mw.getstatic(classInternalName, "nb" + i, "[B")
                    .arraylength()
                    .putstatic(classInternalName, "nn" + i, "I");
        }

        mw.return_();
        mw.visitMaxs(3, 0);
    }

    private static void generateInit(ClassWriter cw, String classInternalName) {
        // Constructor takes the per-field nested-writer array (slot i holds an
        // ObjectWriter for POJO / List<POJO> element types) AND the per-field
        // enum-blob array (slot i holds a byte[][] indexed by ordinal for
        // TYPE_ENUM fields). Both arrays are populated by generateWriter.
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                "([Lcom/alibaba/fastjson3/ObjectWriter;[[[B)V", 16);
        mw.aload(0)
                .invokespecial("java/lang/Object", "<init>", "()V")
                .aload(0)
                .aload(1)
                .putfield(classInternalName, "ow", "[Lcom/alibaba/fastjson3/ObjectWriter;")
                .aload(0)
                .aload(2)
                .putfield(classInternalName, "eb", "[[[B")
                .return_();
        mw.visitMaxs(2, 3);
    }

    private static void generateWriteMethod(
            ClassWriter cw,
            String classInternalName,
            Class<?> beanType,
            String beanInternalName,
            List<FieldWriterInfo> fields
    ) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "write", METHOD_DESC_WRITE, 64);
        // Locals: 0=this, 1=generator, 2=object, 3=fieldName, 4=fieldType, 5-6=features(long)
        // 7=bean (object reference, no cast needed for Unsafe access)

        // Store object reference directly (no cast - Unsafe accepts Object)
        mw.aload(2)
                .astore(7);

        // Pre-compute total estimated capacity: sum of all name bytes + 48 per field + 2 for {}
        // This allows Compact methods to skip per-field ensureCapacity
        int totalEstimated = 2; // for { and }
        for (FieldWriterInfo fi : fields) {
            int nameEncodedLen = ("\"" + fi.jsonName + "\":").getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            totalEstimated += nameEncodedLen + 48; // name + max value size
        }
        mw.aload(1)
                .visitLdcInsn(totalEstimated)
                .invokevirtual(TYPE_JSON_GENERATOR, "ensureCapacityPublic", "(I)V");

        // generator.startObject()
        mw.aload(1)
                .invokevirtual(TYPE_JSON_GENERATOR, "startObject", "()V");

        // Write each field
        for (int i = 0; i < fields.size(); i++) {
            FieldWriterInfo fi = fields.get(i);
            generateWriteField(mw, classInternalName, beanInternalName, fi, i);
        }

        // generator.endObject()
        mw.aload(1)
                .invokevirtual(TYPE_JSON_GENERATOR, "endObject", "()V");

        mw.return_();
        // Locals 9 = elementWriter, 10 = list iterator, 11 = list item
        // (used by generateWriteListPojo, W#4).
        mw.visitMaxs(10, 12);
    }

    private static void generateWriteField(
            MethodWriter mw,
            String classInternalName,
            String beanInternalName,
            FieldWriterInfo fi,
            int fieldIndex
    ) {
        String namePrefix = "" + fieldIndex;

        switch (fi.typeTag) {
            case FieldWriter.TYPE_INT -> generateWriteInt(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_LONG -> generateWriteLong(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_DOUBLE -> generateWriteDouble(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_FLOAT -> generateWriteFloat(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_BOOL -> generateWriteBool(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_STRING -> generateWriteString(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_ENUM -> {
                if (fi.fieldClass != null && fi.fieldClass.isEnum()) {
                    generateWriteEnum(mw, classInternalName, beanInternalName, fi, namePrefix, fieldIndex);
                } else {
                    generateWriteGeneric(mw, classInternalName, beanInternalName, fi, namePrefix, fieldIndex);
                }
            }
            case FieldWriter.TYPE_LONG_ARRAY -> generateWriteTypedArray(mw, classInternalName, beanInternalName, fi, namePrefix, "[J", "writeLongArray", "([J)V");
            case FieldWriter.TYPE_INT_ARRAY -> generateWriteTypedArray(mw, classInternalName, beanInternalName, fi, namePrefix, "[I", "writeIntArray", "([I)V");
            case FieldWriter.TYPE_STRING_ARRAY -> generateWriteTypedArray(mw, classInternalName, beanInternalName, fi, namePrefix, "[Ljava/lang/String;", "writeStringArray", "([Ljava/lang/String;)V");
            case FieldWriter.TYPE_DOUBLE_ARRAY -> generateWriteTypedArray(mw, classInternalName, beanInternalName, fi, namePrefix, "[D", "writeDoubleArray", "([D)V");
            default -> {
                // Phase W#4: List<E> field with a cached element writer or
                // String element type → emit an inline write loop. Otherwise
                // fall through to the generic writer-or-writeAny path.
                if (fi.fieldClass != null
                        && java.util.List.class.isAssignableFrom(fi.fieldClass)) {
                    Class<?> elemClass = resolveListElementType(null, fi);
                    if (elemClass == String.class) {
                        generateWriteListString(mw, classInternalName, beanInternalName, fi, namePrefix);
                    } else {
                        generateWriteListPojo(mw, classInternalName, beanInternalName, fi, namePrefix, fieldIndex);
                    }
                } else {
                    generateWriteGeneric(mw, classInternalName, beanInternalName, fi, namePrefix, fieldIndex);
                }
            }
        }
    }

    private static void generateWriteInt(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        if (emitNameWrite(mw, encodedNameBytes(fi.jsonName))) {
            // W#5 fast path: writeName1L/2L + writeInt32 (trailing comma in writeInt32)
            mw.aload(1); // generator
            if (fi.field != null && fi.fieldClass == int.class) {
                mw.aload(7)
                        .getstatic(classInternalName, "fo" + namePrefix, "J")
                        .invokestatic(TYPE_JDK_UTILS, "getInt", "(Ljava/lang/Object;J)I");
            } else if (fi.getter != null) {
                mw.aload(7);
                mw.invokevirtual(beanInternalName, fi.getter.getName(),
                        "()" + getDescriptor(fi.fieldClass));
            } else {
                throw new JSONException("no field or getter for int property: " + fi.jsonName);
            }
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writeInt32", "(I)V");
            return;
        }
        // Legacy fallback for > 16-byte or non-ASCII encoded names (rare).
        mw.aload(1); // generator
        loadNameFields(mw, classInternalName, namePrefix);
        mw.aload(7); // bean
        if (fi.field != null && fi.fieldClass == int.class) {
            mw.getstatic(classInternalName, "fo" + namePrefix, "J")
                    .invokestatic(TYPE_JDK_UTILS, "getInt", "(Ljava/lang/Object;J)I");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
        } else {
            throw new JSONException("no field or getter for int property: " + fi.jsonName);
        }
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeNameInt32Compact",
                "([JI[B[CI)V");
    }

    private static void generateWriteLong(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        // Boxed Long fields are rejected upstream in generateWriter — only the
        // primitive long path reaches here.
        if (emitNameWrite(mw, encodedNameBytes(fi.jsonName))) {
            // W#5 fast path: writeName1L/2L + writeInt64
            mw.aload(1); // generator
            if (fi.field != null && fi.fieldClass == long.class) {
                mw.aload(7)
                        .getstatic(classInternalName, "fo" + namePrefix, "J")
                        .invokestatic(TYPE_JDK_UTILS, "getLongField", "(Ljava/lang/Object;J)J");
            } else if (fi.getter != null) {
                mw.aload(7);
                mw.invokevirtual(beanInternalName, fi.getter.getName(),
                        "()" + getDescriptor(fi.fieldClass));
            } else {
                throw new JSONException("no field or getter for long property: " + fi.jsonName);
            }
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writeInt64", "(J)V");
            return;
        }
        // Legacy fallback for > 16-byte or non-ASCII encoded names.
        mw.aload(1);
        loadNameFields(mw, classInternalName, namePrefix);
        mw.aload(7);
        if (fi.field != null && fi.fieldClass == long.class) {
            mw.getstatic(classInternalName, "fo" + namePrefix, "J")
                    .invokestatic(TYPE_JDK_UTILS, "getLongField", "(Ljava/lang/Object;J)J");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
        } else {
            throw new JSONException("no field or getter for long property: " + fi.jsonName);
        }
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeNameInt64Compact",
                "([JI[B[CJ)V");
    }

    private static void generateWriteDouble(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        if (emitNameWrite(mw, encodedNameBytes(fi.jsonName))) {
            // W#5 fast path: writeName1L/2L + writeDouble
            mw.aload(1); // generator
            if (fi.field != null && fi.fieldClass == double.class) {
                mw.aload(7)
                        .getstatic(classInternalName, "fo" + namePrefix, "J")
                        .invokestatic(TYPE_JDK_UTILS, "getDouble", "(Ljava/lang/Object;J)D");
            } else if (fi.getter != null) {
                mw.aload(7);
                mw.invokevirtual(beanInternalName, fi.getter.getName(),
                        "()" + getDescriptor(fi.fieldClass));
            } else {
                throw new JSONException("no field or getter for double property: " + fi.jsonName);
            }
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writeDouble", "(D)V");
            return;
        }
        mw.aload(1);
        loadNameFields(mw, classInternalName, namePrefix);
        mw.aload(7);
        if (fi.field != null && fi.fieldClass == double.class) {
            mw.getstatic(classInternalName, "fo" + namePrefix, "J")
                    .invokestatic(TYPE_JDK_UTILS, "getDouble", "(Ljava/lang/Object;J)D");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
        } else {
            throw new JSONException("no field or getter for double property: " + fi.jsonName);
        }
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeNameDoubleCompact",
                "([JI[B[CD)V");
    }

    private static void generateWriteFloat(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        if (emitNameWrite(mw, encodedNameBytes(fi.jsonName))) {
            // W#5 fast path: writeName1L/2L + writeFloat
            mw.aload(1); // generator
            if (fi.field != null && fi.fieldClass == float.class) {
                mw.aload(7)
                        .getstatic(classInternalName, "fo" + namePrefix, "J")
                        .invokestatic(TYPE_JDK_UTILS, "getFloat", "(Ljava/lang/Object;J)F");
            } else if (fi.getter != null) {
                mw.aload(7);
                mw.invokevirtual(beanInternalName, fi.getter.getName(),
                        "()" + getDescriptor(fi.fieldClass));
            } else {
                throw new JSONException("no field or getter for float property: " + fi.jsonName);
            }
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writeFloat", "(F)V");
            return;
        }
        // Legacy fallback.
        mw.aload(1);
        loadNameFieldsForPreEncoded(mw, classInternalName, namePrefix);
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writePreEncodedNameLongs",
                "([JI[C[B)V");
        mw.aload(1)
                .aload(7);
        if (fi.field != null && fi.fieldClass == float.class) {
            mw.getstatic(classInternalName, "fo" + namePrefix, "J")
                    .invokestatic(TYPE_JDK_UTILS, "getFloat", "(Ljava/lang/Object;J)F");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
        } else {
            throw new JSONException("no field or getter for float property: " + fi.jsonName);
        }
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeFloat", "(F)V");
    }

    private static void generateWriteBool(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        if (emitNameWrite(mw, encodedNameBytes(fi.jsonName))) {
            // W#5 fast path: writeName1L/2L + writeBool
            mw.aload(1); // generator
            if (fi.field != null && fi.fieldClass == boolean.class) {
                mw.aload(7)
                        .getstatic(classInternalName, "fo" + namePrefix, "J")
                        .invokestatic(TYPE_JDK_UTILS, "getBoolean", "(Ljava/lang/Object;J)Z");
            } else if (fi.getter != null) {
                mw.aload(7);
                mw.invokevirtual(beanInternalName, fi.getter.getName(),
                        "()" + getDescriptor(fi.fieldClass));
            } else {
                throw new JSONException("no field or getter for boolean property: " + fi.jsonName);
            }
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writeBool", "(Z)V");
            return;
        }
        mw.aload(1);
        loadNameFields(mw, classInternalName, namePrefix);
        mw.aload(7);
        if (fi.field != null && fi.fieldClass == boolean.class) {
            mw.getstatic(classInternalName, "fo" + namePrefix, "J")
                    .invokestatic(TYPE_JDK_UTILS, "getBoolean", "(Ljava/lang/Object;J)Z");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
        } else {
            throw new JSONException("no field or getter for boolean property: " + fi.jsonName);
        }
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeNameBoolCompact", "([JI[B[CZ)V");
    }

    private static void generateWriteString(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        byte[] encodedName = encodedNameBytes(fi.jsonName);

        // String value = JDKUtils.getObject(bean, fieldOffset);
        mw.aload(7);
        if (fi.field != null) {
            mw.getstatic(classInternalName, "fo" + namePrefix, "J")
                    .invokestatic(TYPE_JDK_UTILS, "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;")
                    .checkcast("java/lang/String");
        } else {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()Ljava/lang/String;");
        }
        mw.astore(8); // local 8 = string value

        Label notNull = new Label();
        mw.aload(8)
                .ifnonnull(notNull);
        Label end = new Label();
        mw.goto_(end);

        mw.visitLabel(notNull);
        if (emitNameWrite(mw, encodedName)) {
            // W#5 fast path: writeName1L/2L + writeString
            mw.aload(1)
                    .aload(8)
                    .invokevirtual(TYPE_JSON_GENERATOR, "writeString", "(Ljava/lang/String;)V");
        } else {
            // Legacy fallback for > 16-byte or non-ASCII encoded names.
            mw.aload(1);
            loadNameFields(mw, classInternalName, namePrefix);
            mw.aload(8);
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writeNameStringCompact",
                    "([JI[B[CLjava/lang/String;)V");
        }

        mw.visitLabel(end);
    }

    /**
     * Emit an inline write for a TYPE_ENUM field. Loads the enum value,
     * reads its ordinal, then copies the precomputed
     * {@code "fieldName":"ENUM_VALUE",} blob from {@code this.eb[fieldIndex][ordinal]}
     * into the output buffer via {@link com.alibaba.fastjson3.JSONGenerator#writeRawBytes}.
     *
     * <p>If the precompute slot is null at runtime (e.g., the enum class
     * failed static init and {@code generateWriter} left the slot empty),
     * falls back to {@code generator.writeAny(value)} — identical behaviour
     * to the previous {@code generateWriteGeneric} path.
     */
    private static void generateWriteEnum(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix, int fieldIndex
    ) {
        // Object value = Unsafe.getObject(bean, fo_<i>);
        mw.aload(7)
                .getstatic(classInternalName, "fo" + namePrefix, "J")
                .invokestatic(TYPE_JDK_UTILS, "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;")
                .astore(8);

        Label end = new Label();
        mw.aload(8)
                .ifnull(end);

        // byte[][] slot = this.eb[fieldIndex];
        mw.aload(0)
                .getfield(classInternalName, "eb", "[[[B")
                .bipush(fieldIndex)
                .aaload()
                .astore(9);

        // if (slot == null) goto fallback;
        Label fallback = new Label();
        mw.aload(9)
                .ifnull(fallback);

        // int ord = ((Enum) value).ordinal();
        mw.aload(8)
                .checkcast("java/lang/Enum")
                .invokevirtual("java/lang/Enum", "ordinal", "()I");

        // byte[] blob = slot[ord];
        mw.aload(9)
                .swap()
                .aaload();

        // generator.writeRawBytes(blob);
        mw.aload(1)
                .swap()
                .invokevirtual(TYPE_JSON_GENERATOR, "writeRawBytes", "([B)V")
                .goto_(end);

        mw.visitLabel(fallback);
        // Slot was not precomputed (raw Enum<?> or static init failure).
        // Write the name token then hand the value to writeAny.
        if (!emitNameWrite(mw, encodedNameBytes(fi.jsonName))) {
            mw.aload(1);
            loadNameFieldsForPreEncoded(mw, classInternalName, namePrefix);
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writePreEncodedNameLongs",
                    "([JI[C[B)V");
        }
        mw.aload(1)
                .aload(8);
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeAny",
                "(Ljava/lang/Object;)V");

        mw.visitLabel(end);
    }

    private static void generateWriteGeneric(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix, int fieldIndex
    ) {
        // Prefer Unsafe field-offset read over getter (Unsafe bypasses access
        // checks AND avoids cross-classloader invokevirtual problems). Falls
        // back to invokevirtual on the getter only when there's no backing
        // field at all (calculated property).
        mw.aload(7);
        if (fi.field != null) {
            mw.getstatic(classInternalName, "fo" + namePrefix, "J");  // field offset
            mw.invokestatic(TYPE_JDK_UTILS, "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;");
        } else if (fi.getter != null) {
            String retDesc = getDescriptor(fi.fieldClass);
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + retDesc);
            // Box if primitive
            if (fi.fieldClass.isPrimitive()) {
                boxPrimitive(mw, fi.fieldClass);
            }
        } else {
            // Should be unreachable — generateWriter rejects FieldWriterInfo
            // with neither field nor getter via the early null check.
            mw.aconst_null();
        }
        mw.astore(8);

        // if (value == null) skip
        Label notNull = new Label();
        mw.aload(8)
                .ifnonnull(notNull);
        Label end = new Label();
        mw.goto_(end);

        mw.visitLabel(notNull);
        // W#5: prefer per-length writeName1L/2L over writePreEncodedNameLongs
        // so the emitted bytecode for the name token is 7–12 bytes instead of
        // 15 (4 × getstatic + invokevirtual). Falls back for > 16-byte or
        // non-ASCII names.
        if (!emitNameWrite(mw, encodedNameBytes(fi.jsonName))) {
            mw.aload(1);
            loadNameFieldsForPreEncoded(mw, classInternalName, namePrefix);
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writePreEncodedNameLongs",
                    "([JI[C[B)V");
        }

        // Pre-cached nested writer fast path: if this.ow[fieldIndex] != null,
        // call nestedWriter.write(generator, value, null, null, features) —
        // mirrors the read-side PR #74. The cache slot is monomorphic per
        // field, so C2's inline cache devirtualizes the invokeinterface to
        // a direct call into the child OW_<X>.write. No ObjectMapper lookup.
        mw.aload(0)
                .getfield(classInternalName, "ow", "[Lcom/alibaba/fastjson3/ObjectWriter;")
                .bipush(fieldIndex)
                .aaload()
                .astore(9);

        mw.aload(9);
        Label noCachedWriter = new Label();
        mw.ifnull(noCachedWriter);

        // nestedWriter.write(generator, value, null, null, features)
        mw.aload(9);
        mw.aload(1);  // generator
        mw.aload(8);  // value
        mw.aconst_null()
                .aconst_null();
        mw.lload(5);  // features (slots 5+6, long)
        mw.invokeinterface(TYPE_OBJECT_WRITER, "write",
                "(Lcom/alibaba/fastjson3/JSONGenerator;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V");
        mw.goto_(end);

        mw.visitLabel(noCachedWriter);
        // Fallback: generator.writeAny(value)
        mw.aload(1)
                .aload(8);
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeAny",
                "(Ljava/lang/Object;)V");

        mw.visitLabel(end);
    }

    /**
     * Phase W#4 — inline {@code List<E>} write loop where {@code E} is a
     * nested POJO with a pre-resolved ASM child writer cached in
     * {@code this.ow[fieldIndex]}.
     *
     * <p>Generated bytecode:</p>
     * <pre>
     *   list = bean.field (via Unsafe)
     *   if (list == null) goto end;
     *   writePreEncodedNameLongs(...);
     *   elementWriter = this.ow[fieldIndex];
     *   if (elementWriter == null) {
     *       generator.writeAny(list);  // fallback
     *       goto end;
     *   }
     *   generator.startArray();
     *   for (Object item : list) {
     *       generator.beforeArrayValue();
     *       if (item == null) {
     *           generator.writeNull();
     *       } else {
     *           elementWriter.write(generator, item, null, null, features);
     *       }
     *   }
     *   generator.endArray();
     * end:
     * </pre>
     *
     * <p>Key win: the {@code elementWriter.write} invokeinterface is
     * monomorphic per call site, so C2 devirtualizes it and (often) inlines
     * the child OW_E.write body. Compared with the previous
     * {@code writeAny(item)} per element, this eliminates per-element
     * {@code ObjectMapper.shared().getObjectWriter(item.getClass())} lookups
     * — the dominant cost on aarch64 Eishay write profiles.</p>
     */
    private static void generateWriteListPojo(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix, int fieldIndex
    ) {
        // list = bean.<field> (Unsafe-preferred)
        mw.aload(7);
        if (fi.field != null) {
            mw.getstatic(classInternalName, "fo" + namePrefix, "J")
                    .invokestatic(TYPE_JDK_UTILS, "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
        } else {
            mw.aconst_null();
        }
        mw.astore(8); // list

        Label end = new Label();
        mw.aload(8)
                .ifnull(end);

        // W#5: prefer per-length writeName1L/2L over writePreEncodedNameLongs.
        if (!emitNameWrite(mw, encodedNameBytes(fi.jsonName))) {
            mw.aload(1);
            loadNameFieldsForPreEncoded(mw, classInternalName, namePrefix);
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writePreEncodedNameLongs",
                    "([JI[C[B)V");
        }

        // elementWriter = this.ow[fieldIndex]
        mw.aload(0)
                .getfield(classInternalName, "ow", "[Lcom/alibaba/fastjson3/ObjectWriter;")
                .bipush(fieldIndex)
                .aaload();
        mw.astore(9); // elementWriter

        // If no cached writer, fall back to writeAny(list)
        Label haveWriter = new Label();
        mw.aload(9)
                .ifnonnull(haveWriter)
                .aload(1)
                .aload(8);
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeAny",
                "(Ljava/lang/Object;)V");
        mw.goto_(end);

        mw.visitLabel(haveWriter);
        // generator.startArray()
        mw.aload(1)
                .invokevirtual(TYPE_JSON_GENERATOR, "startArray", "()V");

        // Iterator-based loop (works for any List subtype, no random-access
        // assumption). The JIT recognizes the pattern and unrolls for
        // ArrayList in particular.
        mw.aload(8)
                .invokeinterface("java/util/List", "iterator", "()Ljava/util/Iterator;");
        mw.astore(10); // iterator

        Label loopTop = new Label();
        Label loopEnd = new Label();
        mw.visitLabel(loopTop);
        mw.aload(10)
                .invokeinterface("java/util/Iterator", "hasNext", "()Z")
                .ifeq(loopEnd);

        // generator.beforeArrayValue()
        mw.aload(1)
                .invokevirtual(TYPE_JSON_GENERATOR, "beforeArrayValue", "()V");

        // Object item = iterator.next()
        mw.aload(10)
                .invokeinterface("java/util/Iterator", "next", "()Ljava/lang/Object;");
        mw.astore(11); // item

        // if (item == null) generator.writeNull()
        mw.aload(11);
        Label notNullItem = new Label();
        mw.ifnonnull(notNullItem)
                .aload(1)
                .invokevirtual(TYPE_JSON_GENERATOR, "writeNull", "()V")
                .goto_(loopTop);

        mw.visitLabel(notNullItem);
        // elementWriter.write(generator, item, null, null, features)
        mw.aload(9)
                .aload(1)
                .aload(11)
                .aconst_null()
                .aconst_null()
                .lload(5);
        mw.invokeinterface(TYPE_OBJECT_WRITER, "write",
                "(Lcom/alibaba/fastjson3/JSONGenerator;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V");
        mw.goto_(loopTop);

        mw.visitLabel(loopEnd);
        // generator.endArray()
        mw.aload(1)
                .invokevirtual(TYPE_JSON_GENERATOR, "endArray", "()V");

        mw.visitLabel(end);
    }

    /**
     * Phase W#4 — inline {@code List<String>} write loop. Bypasses
     * {@code writeAny} for String elements; uses {@code generator.writeString}
     * directly. Mirrors {@link #generateWriteListPojo} but with no element
     * writer cache (the JSONGenerator already has a tight String emit path).
     */
    private static void generateWriteListString(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        // list = bean.<field> (Unsafe-preferred)
        mw.aload(7);
        if (fi.field != null) {
            mw.getstatic(classInternalName, "fo" + namePrefix, "J")
                    .invokestatic(TYPE_JDK_UTILS, "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
        } else {
            mw.aconst_null();
        }
        mw.astore(8);

        Label end = new Label();
        mw.aload(8)
                .ifnull(end);

        // W#5: prefer per-length writeName1L/2L over writePreEncodedNameLongs.
        if (!emitNameWrite(mw, encodedNameBytes(fi.jsonName))) {
            mw.aload(1);
            loadNameFieldsForPreEncoded(mw, classInternalName, namePrefix);
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writePreEncodedNameLongs",
                    "([JI[C[B)V");
        }

        // generator.startArray()
        mw.aload(1)
                .invokevirtual(TYPE_JSON_GENERATOR, "startArray", "()V");

        // Iterator-based loop
        mw.aload(8)
                .invokeinterface("java/util/List", "iterator", "()Ljava/util/Iterator;")
                .astore(10);

        Label loopTop = new Label();
        Label loopEnd = new Label();
        mw.visitLabel(loopTop);
        mw.aload(10)
                .invokeinterface("java/util/Iterator", "hasNext", "()Z")
                .ifeq(loopEnd);

        mw.aload(1)
                .invokevirtual(TYPE_JSON_GENERATOR, "beforeArrayValue", "()V");

        mw.aload(10)
                .invokeinterface("java/util/Iterator", "next", "()Ljava/lang/Object;")
                .astore(11);

        mw.aload(11);
        Label notNullItem = new Label();
        mw.ifnonnull(notNullItem)
                .aload(1)
                .invokevirtual(TYPE_JSON_GENERATOR, "writeNull", "()V")
                .goto_(loopTop);

        mw.visitLabel(notNullItem);
        // generator.writeString((String) item)
        mw.aload(1)
                .aload(11)
                .checkcast("java/lang/String")
                .invokevirtual(TYPE_JSON_GENERATOR, "writeString", "(Ljava/lang/String;)V")
                .goto_(loopTop);

        mw.visitLabel(loopEnd);
        mw.aload(1)
                .invokevirtual(TYPE_JSON_GENERATOR, "endArray", "()V");

        mw.visitLabel(end);
    }

    private static void generateWriteTypedArray(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix,
            String arrayDescriptor, String writeMethodName, String writeMethodDesc
    ) {
        mw.aload(7);
        if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(), "()" + arrayDescriptor);
        } else {
            // Use Unsafe to read field
            mw.getstatic(classInternalName, "fo" + namePrefix, "J");  // field offset
            mw.invokestatic(TYPE_JDK_UTILS, "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;");
            // For array types, descriptor is the same as internal name for checkcast
            mw.checkcast(arrayDescriptor);
        }
        mw.astore(8);

        Label notNull = new Label();
        mw.aload(8)
                .ifnonnull(notNull);
        Label end = new Label();
        mw.goto_(end);

        mw.visitLabel(notNull);
        // W#5: prefer per-length writeName1L/2L over writePreEncodedNameLongs.
        if (!emitNameWrite(mw, encodedNameBytes(fi.jsonName))) {
            mw.aload(1);
            loadNameFieldsForPreEncoded(mw, classInternalName, namePrefix);
            mw.invokevirtual(TYPE_JSON_GENERATOR, "writePreEncodedNameLongs", "([JI[C[B)V");
        }

        mw.aload(1)
                .aload(8)
                .invokevirtual(TYPE_JSON_GENERATOR, writeMethodName, writeMethodDesc);

        mw.visitLabel(end);
    }

    // ==================== Helper methods ====================

    /**
     * Load name fields in the order expected by writeNameXxx: nl, nn, nb, nc
     */
    private static void loadNameFields(MethodWriter mw, String classInternalName, String namePrefix) {
        mw.getstatic(classInternalName, "nl" + namePrefix, "[J")
                .getstatic(classInternalName, "nn" + namePrefix, "I")
                .getstatic(classInternalName, "nb" + namePrefix, "[B")
                .getstatic(classInternalName, "nc" + namePrefix, "[C");
    }

    /**
     * Load name fields in the order expected by writePreEncodedNameLongs: nl, nn, nc, nb
     */
    private static void loadNameFieldsForPreEncoded(MethodWriter mw, String classInternalName, String namePrefix) {
        mw.getstatic(classInternalName, "nl" + namePrefix, "[J")
                .getstatic(classInternalName, "nn" + namePrefix, "I")
                .getstatic(classInternalName, "nc" + namePrefix, "[C")
                .getstatic(classInternalName, "nb" + namePrefix, "[B");
    }

    /** W#5 — compute the full {@code "jsonName":} UTF-8 bytes. */
    private static byte[] encodedNameBytes(String jsonName) {
        return ("\"" + jsonName + "\":").getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * W#5 — emit a call to {@code writeName1L} or {@code writeName2L} on
     * the generator, writing the pre-encoded name token {@code "jsonName":}
     * directly into the output buffer via {@code Unsafe.putLong}. Names
     * with encoded length &gt; 16 or containing any non-ASCII byte fall
     * back to the legacy {@code loadNameFields + writeNameStringCompact}
     * path — the caller takes the {@code else} branch.
     *
     * <p>{@link #packNameLong0}/{@link #packNameLong1} pack using
     * {@link com.alibaba.fastjson3.util.JDKUtils#getLongDirect}, so the
     * long constants embedded in the generated bytecode are already in
     * <em>native byte order</em>. {@code putLongDirect} at runtime writes
     * the original byte sequence back regardless of host endianness —
     * crucial for portability beyond x86 / aarch64 LE. Using inline
     * helpers (rather than routing through {@code FieldWriter.encodeByteLongs})
     * matters at JIT-time on aarch64: during the multi-round audit we
     * found the extra cross-class call from {@code emitNameWrite}
     * perturbed ARM C2's tiered compilation of {@code OW_*.write} in a
     * way that cost ~25% throughput — the generated class files were
     * byte-identical, but the standalone compile of the generator
     * triggered different downstream JIT decisions. Keeping the packing
     * inline sidesteps that.
     *
     * <p>The non-ASCII rejection matters for the base-class default
     * implementation of {@code writeName1L/2L} on {@code JSONGenerator},
     * which reconstructs {@code char[]} by sign-extending each byte. That
     * is only correct for pure-ASCII field names — a UTF-8 multi-byte
     * sequence would produce garbled characters. UTF8 subclass doesn't
     * care (it writes bytes directly), but since we can't tell the
     * runtime target at ASM generate time, it's safer to restrict the
     * compact path to ASCII names and keep non-ASCII names on the slow
     * path that uses pre-computed {@code char[]} / {@code byte[]} statics.
     *
     * @return {@code true} if compact emit was used; {@code false} if the
     *         caller must use the legacy fallback.
     */
    private static boolean emitNameWrite(MethodWriter mw, byte[] encodedName) {
        int len = encodedName.length;
        if (len == 0 || len > 16) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (encodedName[i] < 0) {
                return false; // non-ASCII byte — fall back
            }
        }
        mw.aload(1); // generator
        mw.visitLdcInsn(packNameLong0(encodedName));
        if (len <= 8) {
            mw.visitLdcInsn(len)
                    .invokevirtual(TYPE_JSON_GENERATOR, "writeName1L", "(JI)V");
        } else {
            mw.visitLdcInsn(packNameLong1(encodedName))
                    .visitLdcInsn(len)
                    .invokevirtual(TYPE_JSON_GENERATOR, "writeName2L", "(JJI)V");
        }
        return true;
    }

    /**
     * Pack the first 8 bytes of {@code encodedName} into a long using native
     * byte order (matches {@link com.alibaba.fastjson3.util.JDKUtils#putLongDirect}).
     * Bytes past {@code encodedName.length} are zero-padded.
     */
    private static long packNameLong0(byte[] encodedName) {
        byte[] padded = new byte[8];
        System.arraycopy(encodedName, 0, padded, 0, Math.min(8, encodedName.length));
        return com.alibaba.fastjson3.util.JDKUtils.getLongDirect(padded, 0);
    }

    /** Pack bytes 8..15 of {@code encodedName} into a long (native byte order). */
    private static long packNameLong1(byte[] encodedName) {
        byte[] padded = new byte[8];
        if (encodedName.length > 8) {
            System.arraycopy(encodedName, 8, padded, 0, Math.min(8, encodedName.length - 8));
        }
        return com.alibaba.fastjson3.util.JDKUtils.getLongDirect(padded, 0);
    }

    private static void pushString(MethodWriter mw, String value) {
        mw.visitLdcInsn(value);
    }

    private static String getDescriptor(Class<?> type) {
        if (type == void.class) {
            return "V";
        }
        if (type == boolean.class) {
            return "Z";
        }
        if (type == byte.class) {
            return "B";
        }
        if (type == char.class) {
            return "C";
        }
        if (type == short.class) {
            return "S";
        }
        if (type == int.class) {
            return "I";
        }
        if (type == long.class) {
            return "J";
        }
        if (type == float.class) {
            return "F";
        }
        if (type == double.class) {
            return "D";
        }
        if (type.isArray()) {
            return "[" + getDescriptor(type.getComponentType());
        }
        return "L" + type.getName().replace('.', '/') + ";";
    }

    private static void boxPrimitive(MethodWriter mw, Class<?> type) {
        if (type == int.class) {
            mw.invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        } else if (type == long.class) {
            mw.invokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        } else if (type == double.class) {
            mw.invokestatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        } else if (type == float.class) {
            mw.invokestatic("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
        } else if (type == boolean.class) {
            mw.invokestatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        } else if (type == byte.class) {
            mw.invokestatic("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
        } else if (type == short.class) {
            mw.invokestatic("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
        } else if (type == char.class) {
            mw.invokestatic("java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
        }
    }

    // ==================== Field collection (reuses ObjectWriterCreator logic) ====================

    private static List<FieldWriterInfo> collectFields(Class<?> beanType) {
        JSONType jsonType = beanType.getAnnotation(JSONType.class);
        NamingStrategy naming = jsonType != null ? jsonType.naming() : NamingStrategy.NoneStrategy;
        Set<String> includes = jsonType != null && jsonType.includes().length > 0
                ? Set.of(jsonType.includes()) : Set.of();
        Set<String> ignores = jsonType != null && jsonType.ignores().length > 0
                ? Set.of(jsonType.ignores()) : Set.of();
        boolean alphabetic = jsonType == null || jsonType.alphabetic();

        Map<String, FieldWriterInfo> writerMap = new LinkedHashMap<>();

        // 1. Getter methods
        for (Method method : beanType.getMethods()) {
            if (method.getDeclaringClass() == Object.class
                    || method.getParameterCount() != 0
                    || Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            String propertyName = extractPropertyName(method.getName(), method.getReturnType());
            if (propertyName == null) {
                continue;
            }

            JSONField jsonField = method.getAnnotation(JSONField.class);
            if (jsonField != null && !jsonField.serialize()) {
                continue;
            }

            // Check backing field annotation
            if (jsonField == null) {
                Field f = findDeclaredField(beanType, propertyName);
                if (f != null) {
                    jsonField = f.getAnnotation(JSONField.class);
                    if (jsonField != null && !jsonField.serialize()) {
                        continue;
                    }
                }
            }

            String jsonName = resolveJsonName(propertyName, jsonField, naming);
            int ordinal = jsonField != null ? jsonField.ordinal() : 0;

            // Prefer backing field for Unsafe-based access. Visibility doesn't
            // matter — Unsafe.getXxx with a field offset bypasses access checks,
            // so private fields work just as well as public ones, AND avoid
            // the cross-classloader `invokevirtual getter` problem entirely.
            // Falling back to the getter is only needed when no backing field
            // exists (calculated property), or when the field is marked
            // static / transient (which should be skipped per the public-field
            // path's existing convention).
            //
            // The previous gate of `Modifier.isPublic(backingField)` silently
            // rejected every Eishay POJO and routed `EishayWriteUTF8Bytes.
            // fastjson3_asm` through ReflectObjectWriter — JFR profiling on
            // the post-Path-B run exposed it.
            Field backingField = findDeclaredField(beanType, propertyName);
            boolean useField = backingField != null
                    && !Modifier.isStatic(backingField.getModifiers())
                    && !Modifier.isTransient(backingField.getModifiers());
            if (useField) {
                writerMap.put(propertyName, new FieldWriterInfo(
                        jsonName, ordinal, method.getReturnType(), backingField, null));
            } else {
                writerMap.put(propertyName, new FieldWriterInfo(
                        jsonName, ordinal, method.getReturnType(), null, method));
            }
        }

        // 2. Public fields (fallback)
        for (Field field : beanType.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            String propertyName = field.getName();
            if (writerMap.containsKey(propertyName)) {
                continue;
            }

            JSONField jsonField = field.getAnnotation(JSONField.class);
            if (jsonField != null && !jsonField.serialize()) {
                continue;
            }

            String jsonName = resolveJsonName(propertyName, jsonField, naming);
            int ordinal = jsonField != null ? jsonField.ordinal() : 0;

            writerMap.put(propertyName, new FieldWriterInfo(
                    jsonName, ordinal, field.getType(), field, null));
        }

        // 3. Filter
        if (!includes.isEmpty()) {
            writerMap.keySet().retainAll(includes);
        }
        if (!ignores.isEmpty()) {
            writerMap.keySet().removeAll(ignores);
        }

        // 4. Sort
        List<FieldWriterInfo> result = new ArrayList<>(writerMap.values());
        if (alphabetic) {
            Collections.sort(result);
        }

        return result;
    }

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

    private static String decapitalize(String name) {
        if (name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static String resolveJsonName(String propertyName, JSONField jsonField, NamingStrategy naming) {
        if (jsonField != null && !jsonField.name().isEmpty()) {
            return jsonField.name();
        }
        return ObjectWriterCreator.applyNamingStrategy(propertyName, naming);
    }

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

    // ==================== Field info holder ====================

    static final class FieldWriterInfo implements Comparable<FieldWriterInfo> {
        final String jsonName;
        final int ordinal;
        final Class<?> fieldClass;
        final Field field;
        final Method getter;
        final int typeTag;
        final long fieldOffset;  // Unsafe field offset

        FieldWriterInfo(String jsonName, int ordinal, Class<?> fieldClass, Field field, Method getter) {
            this.jsonName = jsonName;
            this.ordinal = ordinal;
            this.fieldClass = fieldClass;
            this.field = field;
            this.getter = getter;
            this.typeTag = resolveTypeTag(fieldClass);
            // Calculate field offset for Unsafe access
            if (field != null) {
                this.fieldOffset = com.alibaba.fastjson3.util.JDKUtils.objectFieldOffset(field);
            } else {
                this.fieldOffset = -1;
            }
        }

        private static int resolveTypeTag(Class<?> type) {
            if (type == String.class) {
                return FieldWriter.TYPE_STRING;
            }
            if (type == int.class || type == Integer.class) {
                return FieldWriter.TYPE_INT;
            }
            if (type == long.class || type == Long.class) {
                return FieldWriter.TYPE_LONG;
            }
            if (type == double.class || type == Double.class) {
                return FieldWriter.TYPE_DOUBLE;
            }
            if (type == float.class || type == Float.class) {
                return FieldWriter.TYPE_FLOAT;
            }
            if (type == boolean.class || type == Boolean.class) {
                return FieldWriter.TYPE_BOOL;
            }
            if (type == long[].class) {
                return FieldWriter.TYPE_LONG_ARRAY;
            }
            if (type == int[].class) {
                return FieldWriter.TYPE_INT_ARRAY;
            }
            if (type == String[].class) {
                return FieldWriter.TYPE_STRING_ARRAY;
            }
            if (type == double[].class) {
                return FieldWriter.TYPE_DOUBLE_ARRAY;
            }
            if (type != null && type.isEnum()) {
                return FieldWriter.TYPE_ENUM;
            }
            return FieldWriter.TYPE_GENERIC;
        }

        @Override
        public int compareTo(FieldWriterInfo other) {
            int cmp = Integer.compare(this.ordinal, other.ordinal);
            if (cmp != 0) {
                return cmp;
            }
            return this.jsonName.compareTo(other.jsonName);
        }
    }
}

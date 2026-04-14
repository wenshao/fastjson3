package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.internal.asm.ClassWriter;
import com.alibaba.fastjson3.internal.asm.Label;
import com.alibaba.fastjson3.internal.asm.MethodWriter;
import com.alibaba.fastjson3.internal.asm.Opcodes;
import com.alibaba.fastjson3.util.DynamicClassLoader;

import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.fastjson3.internal.asm.ASMUtils.DESC_FIELD_NAME_MATCHER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.DESC_FIELD_READER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.DESC_OBJECT_READER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_FIELD_NAME_MATCHER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_FIELD_READER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JDK_UTILS;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JSON_EXCEPTION;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JSON_PARSER;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JSON_PARSER_UTF8;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_OBJECT_READER;

/**
 * Creates {@link ObjectReader} instances via ASM bytecode generation.
 * The generated readObjectUTF8 method inlines type-specific read+set
 * for each field using direct Unsafe puts, eliminating FieldReader
 * dispatch overhead in the hot parsing loop.
 *
 * <p>Fallback: if ASM generation fails or the type is not suitable,
 * delegates to {@link ObjectReaderCreator#createObjectReader(Class)}.</p>
 */
@com.alibaba.fastjson3.annotation.JVMOnly
public final class ObjectReaderCreatorASM {
    private static final AtomicLong SEED = new AtomicLong();
    private static final DynamicClassLoader CLASS_LOADER = DynamicClassLoader.getSharedInstance();

    private static final String[] INTERFACES = {TYPE_OBJECT_READER};

    // UnsafeAllocator type
    private static final String TYPE_UNSAFE_ALLOC = "com/alibaba/fastjson3/util/UnsafeAllocator";

    private ObjectReaderCreatorASM() {
    }

    /**
     * Create an ObjectReader for the given type via ASM bytecode generation.
     * Falls back to reflection if ASM generation fails or the type is not suitable.
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectReader<T> createObjectReader(Class<T> type) {
        ObjectReader<T> reflectionReader = ObjectReaderCreator.createObjectReader(type);

        // native-image: runtime bytecode generation is not supported, fall back to reflection
        if (com.alibaba.fastjson3.util.JDKUtils.NATIVE_IMAGE) {
            return reflectionReader;
        }

        ObjectReaderCreator.FieldReaderCollection collection = ObjectReaderCreator.collectFieldReaders(type);
        if (!canGenerate(type, collection.fieldReaders)) {
            return reflectionReader;
        }

        try {
            return (ObjectReader<T>) generateReader(type, collection, reflectionReader);
        } catch (Throwable e) {
            return reflectionReader;
        }
    }

    // ===================== Fast-path helpers (PR #2 of parse-stage1) =====================
    //
    // When every field in the bean has an ASCII name of length 2..9 and the
    // per-field 4-byte prefixes are mutually unique, we can emit a switch-based
    // speculation phase that uses {@code nextIfName4Match<N>} intrinsics for
    // constant-time long/int comparisons instead of the byte-array matcher.
    // Beans that don't qualify keep using the existing {@code tryMatchFieldHeaderOff}
    // speculation phase — no regression on them.

    private static boolean canUseFastPath(FieldReader[] fieldReaders) {
        if (fieldReaders.length == 0) {
            return false;
        }
        java.util.HashSet<Integer> prefixes = new java.util.HashSet<>();
        for (FieldReader fr : fieldReaders) {
            String name = fr.fieldName;
            int L = name.length();
            if (L < 2 || L > 9) {
                return false;
            }
            for (int i = 0; i < L; i++) {
                if (name.charAt(i) > 127) {
                    return false;   // non-ASCII: byte[] != char[]
                }
            }
            if (!prefixes.add(computePrefix(name))) {
                return false;       // collision on the 4-byte dispatch key
            }
        }
        return true;
    }

    /**
     * Compute the 4-byte native-order int that the runtime will see at
     * {@code this.offset} after {@code advanceAfterNameOpeningQuote()}.
     * For names of length &ge;4 this is the first 4 name bytes; for shorter
     * names the prefix includes the closing quote (and colon for length 2)
     * so the single dispatch key uniquely identifies short field names too.
     */
    private static int computePrefix(String name) {
        int L = name.length();
        byte[] buf = new byte[4];
        buf[0] = (byte) name.charAt(0);
        buf[1] = (byte) name.charAt(1);
        if (L >= 3) {
            buf[2] = (byte) name.charAt(2);
        } else {
            buf[2] = '"';
        }
        if (L >= 4) {
            buf[3] = (byte) name.charAt(3);
        } else if (L == 3) {
            buf[3] = '"';
        } else {
            buf[3] = ':';
        }
        return com.alibaba.fastjson3.util.JDKUtils.getIntDirect(buf, 0);
    }

    /** Encode 4 bytes as a native-order int using the same primitive as the runtime. */
    private static int encodeInt(byte b0, byte b1, byte b2, byte b3) {
        byte[] buf = {b0, b1, b2, b3};
        return com.alibaba.fastjson3.util.JDKUtils.getIntDirect(buf, 0);
    }

    /** Encode 8 bytes as a native-order long using the same primitive as the runtime. */
    private static long encodeLong(byte b0, byte b1, byte b2, byte b3,
                                   byte b4, byte b5, byte b6, byte b7) {
        byte[] buf = {b0, b1, b2, b3, b4, b5, b6, b7};
        return com.alibaba.fastjson3.util.JDKUtils.getLongDirect(buf, 0);
    }

    private static boolean canGenerate(Class<?> type, FieldReader[] fields) {
        if (type.isInterface() || type.isArray() || type.isEnum()
                || type.isPrimitive() || Modifier.isAbstract(type.getModifiers())) {
            return false;
        }
        if (fields.length == 0) {
            return false;
        }

        // Check if class is accessible for ASM field access
        // 1. Public classes are always accessible
        if (Modifier.isPublic(type.getModifiers())) {
            // continue to other checks
        } else if (type.isMemberClass() && Modifier.isStatic(type.getModifiers())) {
            // 2. Static inner classes of public classes are also accessible
            Class<?> enclosing = type.getEnclosingClass();
            if (enclosing == null || !Modifier.isPublic(enclosing.getModifiers())) {
                // 3. Non-public static member classes (like benchmark classes) can be accessed
                // via reflection from ASM code - allow them
            }
        } else {
            // Non-public non-static class - not accessible
            return false;
        }
        // @JSONType(schema=) requires reflection path for post-construction validation
        com.alibaba.fastjson3.annotation.JSONType jsonType = type.getAnnotation(
                com.alibaba.fastjson3.annotation.JSONType.class);
        if (jsonType != null && !jsonType.schema().isEmpty()) {
            return false;
        }

        for (FieldReader fr : fields) {
            if (fr.fieldOffset < 0) {
                return false;
            }
            if (fr.required) {
                return false;
            }
            if (fr.defaultValue != null && !fr.defaultValue.isEmpty()) {
                return false;
            }
            if (fr.jsonSchema != null) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static ObjectReader<?> generateReader(
            Class<?> beanType,
            ObjectReaderCreator.FieldReaderCollection collection,
            ObjectReader<?> fallbackReader
    ) {
        FieldReader[] fieldReaders = collection.fieldReaders;
        FieldNameMatcher matcher = collection.matcher;

        String beanInternalName = beanType.getName().replace('.', '/');
        String className = "com.alibaba.fastjson3.reader.gen.OR_"
                + beanType.getSimpleName() + "_" + SEED.getAndIncrement();
        String classInternalName = className.replace('.', '/');

        ClassWriter cw = new ClassWriter(null);
        cw.visit(Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                classInternalName, "java/lang/Object", INTERFACES);

        // Instance fields
        cw.visitField(Opcodes.ACC_FINAL, "matcher", DESC_FIELD_NAME_MATCHER);
        cw.visitField(Opcodes.ACC_FINAL, "fieldReaders", "[" + DESC_FIELD_READER);
        cw.visitField(Opcodes.ACC_FINAL, "fallback", DESC_OBJECT_READER);
        cw.visitField(Opcodes.ACC_FINAL, "usePLHV", "Z");

        // Static fields for field offsets and bean class
        cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "bc", "Ljava/lang/Class;");
        for (int i = 0; i < fieldReaders.length; i++) {
            cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "fo" + i, "J");
        }

        generateClinit(cw, classInternalName, beanType, fieldReaders);
        generateInit(cw, classInternalName);
        generateCreateInstance(cw, classInternalName);
        generateGetObjectClass(cw, classInternalName);
        generateReadObject(cw, classInternalName);
        generateReadObjectUTF8(cw, classInternalName, beanInternalName, fieldReaders);

        byte[] bytecode = cw.toByteArray();
        Class<?> readerClass = CLASS_LOADER.loadClass(className, bytecode, 0, bytecode.length);

        try {
            return (ObjectReader<?>) readerClass
                    .getConstructor(FieldNameMatcher.class, FieldReader[].class, ObjectReader.class)
                    .newInstance(matcher, fieldReaders, fallbackReader);
        } catch (Exception e) {
            throw new JSONException("Failed to instantiate generated reader for " + beanType.getName(), e);
        }
    }

    // ==================== <clinit> ====================

    private static void generateClinit(
            ClassWriter cw, String classInternalName,
            Class<?> beanType, FieldReader[] fieldReaders
    ) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", 32);

        // bc = BeanType.class
        mw.visitLdcInsn(beanType);
        mw.putstatic(classInternalName, "bc", "Ljava/lang/Class;");

        // fo0 = <constant offset>, fo1 = ..., etc.
        for (int i = 0; i < fieldReaders.length; i++) {
            mw.visitLdcInsn(fieldReaders[i].fieldOffset);
            mw.putstatic(classInternalName, "fo" + i, "J");
        }

        mw.return_();
        mw.visitMaxs(2, 0);
    }

    // ==================== <init> ====================

    private static void generateInit(ClassWriter cw, String classInternalName) {
        String desc = "(" + DESC_FIELD_NAME_MATCHER + "[" + DESC_FIELD_READER + DESC_OBJECT_READER + ")V";
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", desc, 32);

        // super()
        mw.aload(0);
        mw.invokespecial("java/lang/Object", "<init>", "()V");

        // this.matcher = arg1
        mw.aload(0);
        mw.aload(1);
        mw.putfield(classInternalName, "matcher", DESC_FIELD_NAME_MATCHER);

        // this.fieldReaders = arg2
        mw.aload(0);
        mw.aload(2);
        mw.putfield(classInternalName, "fieldReaders", "[" + DESC_FIELD_READER);

        // this.fallback = arg3
        mw.aload(0);
        mw.aload(3);
        mw.putfield(classInternalName, "fallback", DESC_OBJECT_READER);

        // this.usePLHV = (arg1.strategy == STRATEGY_PLHV)
        mw.aload(0);
        mw.aload(1);
        mw.getfield(TYPE_FIELD_NAME_MATCHER, "strategy", "I");
        // STRATEGY_PLHV == 0, so usePLHV = (strategy == 0)
        Label notPLHV = new Label();
        Label afterPLHV = new Label();
        mw.ifne(notPLHV);
        mw.iconst_1();
        mw.goto_(afterPLHV);
        mw.visitLabel(notPLHV);
        mw.iconst_0();
        mw.visitLabel(afterPLHV);
        mw.putfield(classInternalName, "usePLHV", "Z");

        mw.return_();
        mw.visitMaxs(3, 4);
    }

    // ==================== createInstance ====================

    private static void generateCreateInstance(ClassWriter cw, String classInternalName) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "createInstance", "(J)Ljava/lang/Object;", 16);
        // return UnsafeAllocator.allocateInstanceUnchecked(bc)
        mw.getstatic(classInternalName, "bc", "Ljava/lang/Class;");
        mw.invokestatic(TYPE_UNSAFE_ALLOC, "allocateInstanceUnchecked",
                "(Ljava/lang/Class;)Ljava/lang/Object;");
        mw.areturn();
        mw.visitMaxs(1, 3);
    }

    // ==================== getObjectClass ====================

    private static void generateGetObjectClass(ClassWriter cw, String classInternalName) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "getObjectClass", "()Ljava/lang/Class;", 16);
        mw.getstatic(classInternalName, "bc", "Ljava/lang/Class;");
        mw.areturn();
        mw.visitMaxs(1, 1);
    }

    // ==================== readObject (dispatch) ====================

    private static void generateReadObject(ClassWriter cw, String classInternalName) {
        // readObject(JSONParser parser, Type fieldType, Object fieldName, long features)
        String desc = "(Lcom/alibaba/fastjson3/JSONParser;Ljava/lang/reflect/Type;Ljava/lang/Object;J)"
                + "Ljava/lang/Object;";
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "readObject", desc, 32);
        // Locals: 0=this, 1=parser, 2=fieldType, 3=fieldName, 4-5=features

        // if (parser instanceof JSONParser.UTF8)
        mw.aload(1);
        mw.instanceOf(TYPE_JSON_PARSER_UTF8);
        Label fallbackLabel = new Label();
        mw.ifeq(fallbackLabel);

        // return this.readObjectUTF8((JSONParser.UTF8)parser, features)
        mw.aload(0);
        mw.aload(1);
        mw.checkcast(TYPE_JSON_PARSER_UTF8);
        mw.lload(4);
        mw.invokevirtual(classInternalName, "readObjectUTF8",
                "(Lcom/alibaba/fastjson3/JSONParser$UTF8;J)Ljava/lang/Object;");
        mw.areturn();

        // fallback: return fallback.readObject(parser, fieldType, fieldName, features)
        mw.visitLabel(fallbackLabel);
        mw.aload(0);
        mw.getfield(classInternalName, "fallback", DESC_OBJECT_READER);
        mw.aload(1);
        mw.aload(2);
        mw.aload(3);
        mw.lload(4);
        mw.invokeinterface(TYPE_OBJECT_READER, "readObject", desc);
        mw.areturn();

        mw.visitMaxs(6, 6);
    }

    // ==================== readObjectUTF8 (hot path) ====================

    private static void generateReadObjectUTF8(
            ClassWriter cw,
            String classInternalName,
            String beanInternalName,
            FieldReader[] fieldReaders
    ) {
        // readObjectUTF8(JSONParser.UTF8 utf8, long features) -> Object
        String desc = "(Lcom/alibaba/fastjson3/JSONParser$UTF8;J)Ljava/lang/Object;";
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "readObjectUTF8", desc, 64);
        // Locals: 0=this, 1=utf8, 2-3=features, 4=instance, 5=peek, 6-7=hash, 8=reader, 9=fi/sep
        //         10=matcher

        // --- Null check ---
        // peek = utf8.skipWSAndPeek()
        mw.aload(1);
        mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "skipWSAndPeek", "()I");
        mw.istore(5);

        // if (peek == 'n' && utf8.readNull()) return null
        mw.iload(5);
        mw.bipush('n');
        Label notNull = new Label();
        mw.if_icmpne(notNull);
        mw.aload(1);
        mw.invokevirtual(TYPE_JSON_PARSER, "readNull", "()Z");
        Label notNull2 = new Label();
        mw.ifeq(notNull2);
        mw.aconst_null();
        mw.areturn();
        mw.visitLabel(notNull2);
        mw.visitLabel(notNull);

        // --- Check '{' ---
        Label errorOpen = new Label();
        mw.iload(5);
        mw.bipush('{');
        mw.if_icmpne(errorOpen);
        mw.aload(1);
        mw.iconst_1();
        mw.invokevirtual(TYPE_JSON_PARSER, "advance", "(I)V");

        // --- Create instance ---
        // instance = createInstance(features)
        mw.aload(0);
        mw.lload(2);
        mw.invokevirtual(classInternalName, "createInstance", "(J)Ljava/lang/Object;");
        mw.astore(4);

        // --- Check for empty object ---
        mw.aload(1);
        mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "skipWSAndPeek", "()I");
        mw.istore(5);
        mw.iload(5);
        mw.bipush('}');
        Label startLoop = new Label();
        mw.if_icmpne(startLoop);
        mw.aload(1);
        mw.iconst_1();
        mw.invokevirtual(TYPE_JSON_PARSER, "advance", "(I)V");
        mw.aload(4);
        mw.areturn();

        // --- Load matcher, fieldReaders, and initial offset into locals ---
        // Locals: 0=this, 1=utf8, 2-3=features, 4=instance, 5=peek/temp,
        //         6-7=hash, 8=reader, 9=sep/fi, 10=matcher, 11=frArray, 12=off
        mw.visitLabel(startLoop);
        mw.aload(0);
        mw.getfield(classInternalName, "matcher", DESC_FIELD_NAME_MATCHER);
        mw.astore(10);
        mw.aload(0);
        mw.getfield(classInternalName, "fieldReaders", "[" + DESC_FIELD_READER);
        mw.astore(11);

        String readOffDesc = "(ILjava/lang/Object;" + DESC_FIELD_READER + ")I";

        Label genericLoopTop = new Label();
        Label returnInstance = new Label();

        boolean fastPath = canUseFastPath(fieldReaders);

        if (fastPath) {
            // ==================== Fast Path Speculation (PR #2 of parse-stage1) ====================
            // Uses getRawInt switch + nextIfName4Match<N> intrinsic (long/int compare)
            // instead of the byte[] tryMatchFieldHeaderOff.
            //
            // On any prefix or tail mismatch, restore this.offset to the pre-quote
            // position and fall through to the hash-based generic loop.
            Label fastLoopTop = new Label();
            Label fastMiss = new Label();
            Label afterFastField = new Label();

            mw.visitLabel(fastLoopTop);

            // 1. Skip ws + check opening quote + advance past it.
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "advanceAfterNameOpeningQuote", "()Z");
            mw.ifeq(genericLoopTop);

            // 2. Read first 4 bytes of the name and dispatch on them.
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "getRawInt", "()I");

            int n = fieldReaders.length;
            // lookupswitch requires keys sorted in ascending int order.
            int[] prefixes = new int[n];
            int[] fieldIdxByPrefix = new int[n];
            for (int i = 0; i < n; i++) {
                prefixes[i] = computePrefix(fieldReaders[i].fieldName);
                fieldIdxByPrefix[i] = i;
            }
            // Simple insertion sort — n is typically small.
            for (int i = 1; i < n; i++) {
                int kp = prefixes[i];
                int kf = fieldIdxByPrefix[i];
                int j = i - 1;
                while (j >= 0 && prefixes[j] > kp) {
                    prefixes[j + 1] = prefixes[j];
                    fieldIdxByPrefix[j + 1] = fieldIdxByPrefix[j];
                    j--;
                }
                prefixes[j + 1] = kp;
                fieldIdxByPrefix[j + 1] = kf;
            }

            Label[] fastCaseLabels = new Label[n];
            for (int c = 0; c < n; c++) {
                fastCaseLabels[c] = new Label();
            }
            Label fastDefault = new Label();
            mw.visitLookupSwitchInsn(fastDefault, prefixes, fastCaseLabels);

            for (int c = 0; c < n; c++) {
                mw.visitLabel(fastCaseLabels[c]);
                int fieldIdx = fieldIdxByPrefix[c];
                emitMatchCall(mw, fieldReaders[fieldIdx]);
                mw.ifeq(fastMiss);

                // Success: read field value using the non-off generateFieldCase
                // (uses this.offset, which Match<N> has advanced past ':' and trailing ws).
                generateFieldCase(mw, classInternalName, beanInternalName,
                        fieldReaders[fieldIdx], fieldIdx);
                mw.goto_(afterFastField);
            }

            // Default case: no prefix match -> same as a Match<N> miss.
            mw.visitLabel(fastDefault);
            // Fall through to fastMiss. (Labelled goto for clarity.)
            mw.goto_(fastMiss);

            // Fast miss: this.offset is at the first name char (post opening quote).
            // Back it up by 1 so the hash path can re-read the quote + field name.
            mw.visitLabel(fastMiss);
            mw.aload(1);
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER, "getOffset", "()I");
            mw.iconst_1();
            mw.isub();
            mw.invokevirtual(TYPE_JSON_PARSER, "setOffset", "(I)V");
            mw.goto_(genericLoopTop);

            // Separator after a successful fast-path field read.
            mw.visitLabel(afterFastField);
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readFieldSeparator", "()I");
            mw.istore(9);
            mw.iload(9);
            mw.ifeq(fastLoopTop);        // 0 = comma -> next iteration
            mw.iload(9);
            mw.iconst_1();
            Label fastErrorSep = new Label();
            mw.if_icmpne(fastErrorSep);
            mw.aload(4);
            mw.areturn();
            mw.visitLabel(fastErrorSep);
            mw.new_(TYPE_JSON_EXCEPTION);
            mw.dup();
            mw.visitLdcInsn("expected ',' or '}'");
            mw.invokespecial(TYPE_JSON_EXCEPTION, "<init>", "(Ljava/lang/String;)V");
            mw.athrow();
        } else {
            // off = utf8.getOffset() — initialised only for the off-as-local path
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER, "getOffset", "()I");
            mw.istore(12);

            // ==================== Ordered Speculation (byte[] header) ====================
            for (int i = 0; i < fieldReaders.length; i++) {
                // off = utf8.tryMatchFieldHeaderOff(off, fieldReaders[i].fieldNameHeader)
                mw.aload(1);  // utf8
                mw.iload(12); // off
                mw.aload(11); // frArray
                mw.bipush(i);
                mw.aaload();   // frArray[i]
                mw.getfield(TYPE_FIELD_READER, "fieldNameHeader", "[B");
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "tryMatchFieldHeaderOff", "(I[B)I");
                mw.istore(12);
                mw.iload(12);
                mw.iconst_m1();
                mw.if_icmpeq(genericLoopTop); // -1 = mismatch -> fallback

                generateFieldCaseOff(mw, classInternalName, fieldReaders[i], i, readOffDesc);

                // sep = utf8.readFieldSepOff(off) — positive=comma, negative='}'
                mw.aload(1);
                mw.iload(12);
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readFieldSepOff", "(I)I");
                mw.istore(9);
                mw.iload(9);
                mw.iconst_m1();
                mw.if_icmple(returnInstance); // result <= -1 means '}', recover offset
                // comma: off = result
                mw.iload(9);
                mw.istore(12);
                // fall through to next field
            }

            // All fields matched but JSON has more fields
            mw.aload(1);
            mw.iload(12);
            mw.invokevirtual(TYPE_JSON_PARSER, "setOffset", "(I)V");
            mw.goto_(genericLoopTop);

            // --- Return instance (off-as-local path only) ---
            mw.visitLabel(returnInstance);
            mw.iload(9);
            mw.ineg();
            mw.istore(12);
            mw.aload(1);
            mw.iload(12);
            mw.invokevirtual(TYPE_JSON_PARSER, "setOffset", "(I)V");
            mw.aload(4);
            mw.areturn();
        }

        // ==================== Generic Fallback Loop ====================
        // this.offset was set by tryMatchFieldHeaderOff on mismatch
        mw.visitLabel(genericLoopTop);

        // Read field name hash
        mw.aload(0);
        mw.getfield(classInternalName, "usePLHV", "Z");
        Label useNormalHash = new Label();
        mw.ifeq(useNormalHash);
        mw.aload(1);
        mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readFieldNameHashPLHV", "()J");
        Label afterHash = new Label();
        mw.goto_(afterHash);
        mw.visitLabel(useNormalHash);
        mw.aload(1);
        mw.aload(10);
        mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readFieldNameHash",
                "(" + DESC_FIELD_NAME_MATCHER + ")J");
        mw.visitLabel(afterHash);
        mw.lstore(6);

        // reader = matcher.match(hash)
        mw.aload(10);
        mw.lload(6);
        mw.invokevirtual(TYPE_FIELD_NAME_MATCHER, "match",
                "(J)" + DESC_FIELD_READER);
        mw.astore(8);

        mw.aload(8);
        Label skipValue = new Label();
        mw.ifnull(skipValue);

        // fi = reader.index
        mw.aload(8);
        mw.getfield(TYPE_FIELD_READER, "index", "I");
        mw.istore(9);

        // lookupswitch on fi
        Label afterField = new Label();
        Label defaultCase = new Label();
        int[] keys = new int[fieldReaders.length];
        Label[] labels = new Label[fieldReaders.length];
        for (int i = 0; i < fieldReaders.length; i++) {
            keys[i] = i;
            labels[i] = new Label();
        }
        mw.iload(9);
        mw.visitLookupSwitchInsn(defaultCase, keys, labels);

        for (int i = 0; i < fieldReaders.length; i++) {
            mw.visitLabel(labels[i]);
            generateFieldCase(mw, classInternalName, beanInternalName, fieldReaders[i], i);
            mw.goto_(afterField);
        }

        mw.visitLabel(defaultCase);
        mw.aload(8);
        mw.aload(4);
        mw.aload(1);
        mw.invokevirtual(TYPE_JSON_PARSER, "readAny", "()Ljava/lang/Object;");
        mw.invokevirtual(TYPE_FIELD_READER, "setFieldValue",
                "(Ljava/lang/Object;Ljava/lang/Object;)V");
        mw.goto_(afterField);

        mw.visitLabel(skipValue);
        mw.aload(1);
        mw.invokevirtual(TYPE_JSON_PARSER, "skipValue", "()V");

        mw.visitLabel(afterField);
        mw.aload(1);
        mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readFieldSeparator", "()I");
        mw.istore(9);
        mw.iload(9);
        mw.ifeq(genericLoopTop);
        mw.iload(9);
        mw.iconst_1();
        Label errorSep = new Label();
        mw.if_icmpne(errorSep);
        mw.aload(4);
        mw.areturn();

        // Error: expected ',' or '}'
        mw.visitLabel(errorSep);
        mw.new_(TYPE_JSON_EXCEPTION);
        mw.dup();
        mw.visitLdcInsn("expected ',' or '}'");
        mw.invokespecial(TYPE_JSON_EXCEPTION, "<init>", "(Ljava/lang/String;)V");
        mw.athrow();

        // Error: expected '{'
        mw.visitLabel(errorOpen);
        mw.new_(TYPE_JSON_EXCEPTION);
        mw.dup();
        mw.visitLdcInsn("expected '{'");
        mw.invokespecial(TYPE_JSON_EXCEPTION, "<init>", "(Ljava/lang/String;)V");
        mw.athrow();

        mw.visitMaxs(12, 13);
    }

    /**
     * Emit the call to {@code nextIfName4Match<N>} for a single field. Precomputes
     * the tail constants from the field name bytes and pushes them on the stack.
     * Leaves a boolean result on the stack.
     */
    private static void emitMatchCall(MethodWriter mw, FieldReader fr) {
        String name = fr.fieldName;
        int L = name.length();
        mw.aload(1);
        switch (L) {
            case 2:
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "nextIfName4Match2", "()Z");
                break;
            case 3:
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "nextIfName4Match3", "()Z");
                break;
            case 4: {
                mw.bipush((byte) name.charAt(3));
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "nextIfName4Match4", "(B)Z");
                break;
            }
            case 5: {
                int name1 = encodeInt(
                        (byte) name.charAt(3),
                        (byte) name.charAt(4),
                        (byte) '"',
                        (byte) ':');
                mw.visitLdcInsn(name1);
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "nextIfName4Match5", "(I)Z");
                break;
            }
            case 6: {
                int name1 = encodeInt(
                        (byte) name.charAt(3),
                        (byte) name.charAt(4),
                        (byte) name.charAt(5),
                        (byte) '"');
                mw.visitLdcInsn(name1);
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "nextIfName4Match6", "(I)Z");
                break;
            }
            case 7: {
                int name1 = encodeInt(
                        (byte) name.charAt(3),
                        (byte) name.charAt(4),
                        (byte) name.charAt(5),
                        (byte) name.charAt(6));
                mw.visitLdcInsn(name1);
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "nextIfName4Match7", "(I)Z");
                break;
            }
            case 8: {
                int name1 = encodeInt(
                        (byte) name.charAt(3),
                        (byte) name.charAt(4),
                        (byte) name.charAt(5),
                        (byte) name.charAt(6));
                mw.visitLdcInsn(name1);
                mw.bipush((byte) name.charAt(7));
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "nextIfName4Match8", "(IB)Z");
                break;
            }
            case 9: {
                long name1 = encodeLong(
                        (byte) name.charAt(3),
                        (byte) name.charAt(4),
                        (byte) name.charAt(5),
                        (byte) name.charAt(6),
                        (byte) name.charAt(7),
                        (byte) name.charAt(8),
                        (byte) '"',
                        (byte) ':');
                mw.visitLdcInsn(name1);
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "nextIfName4Match9", "(J)Z");
                break;
            }
            default:
                throw new IllegalStateException(
                        "emitMatchCall: unsupported field name length " + L + " for " + name);
        }
    }

    private static void generateFieldCase(
            MethodWriter mw,
            String classInternalName,
            String beanInternalName,
            FieldReader fr,
            int fieldIndex
    ) {
        Class<?> fc = fr.fieldClass;

        if (fc == int.class) {
            // JDKUtils.putInt(instance, foN, utf8.readIntDirect())
            mw.aload(4); // instance
            mw.getstatic(classInternalName, "fo" + fieldIndex, "J");
            mw.aload(1); // utf8
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readIntDirect", "()I");
            mw.invokestatic(TYPE_JDK_UTILS, "putInt", "(Ljava/lang/Object;JI)V");
        } else if (fc == long.class) {
            // JDKUtils.putLongField(instance, foN, utf8.readLongDirect())
            mw.aload(4);
            mw.getstatic(classInternalName, "fo" + fieldIndex, "J");
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readLongDirect", "()J");
            mw.invokestatic(TYPE_JDK_UTILS, "putLongField", "(Ljava/lang/Object;JJ)V");
        } else if (fc == double.class) {
            // JDKUtils.putDouble(instance, foN, utf8.readDoubleDirect())
            mw.aload(4);
            mw.getstatic(classInternalName, "fo" + fieldIndex, "J");
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readDoubleDirect", "()D");
            mw.invokestatic(TYPE_JDK_UTILS, "putDouble", "(Ljava/lang/Object;JD)V");
        } else if (fc == float.class) {
            // JDKUtils.putFloat(instance, foN, (float)utf8.readDoubleDirect())
            mw.aload(4);
            mw.getstatic(classInternalName, "fo" + fieldIndex, "J");
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readDoubleDirect", "()D");
            mw.d2f();
            mw.invokestatic(TYPE_JDK_UTILS, "putFloat", "(Ljava/lang/Object;JF)V");
        } else if (fc == boolean.class) {
            // JDKUtils.putBoolean(instance, foN, utf8.readBooleanDirect())
            mw.aload(4);
            mw.getstatic(classInternalName, "fo" + fieldIndex, "J");
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readBooleanDirect", "()Z");
            mw.invokestatic(TYPE_JDK_UTILS, "putBoolean", "(Ljava/lang/Object;JZ)V");
        } else if (fc == String.class) {
            // peek = utf8.skipWSAndPeek()
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "skipWSAndPeek", "()I");
            mw.istore(5);

            // if (peek == 'n' && utf8.readNull()) skip
            Label readStr = new Label();
            mw.iload(5);
            mw.bipush('n');
            mw.if_icmpne(readStr);
            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER, "readNull", "()Z");
            Label strEnd = new Label();
            mw.ifne(strEnd);

            // JDKUtils.putObject(instance, foN, utf8.readStringDirect())
            mw.visitLabel(readStr);
            mw.aload(4); // instance
            mw.getstatic(classInternalName, "fo" + fieldIndex, "J");
            mw.aload(1); // utf8
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readStringDirect", "()Ljava/lang/String;");
            mw.invokestatic(TYPE_JDK_UTILS, "putObject", "(Ljava/lang/Object;JLjava/lang/Object;)V");

            mw.visitLabel(strEnd);
        } else {
            // Complex type (List, String[], long[], POJO, etc.):
            // delegate to fallback.readFieldUTF8(utf8, instance, fieldIndex, features)
            mw.aload(0); // this
            mw.getfield(classInternalName, "fallback", DESC_OBJECT_READER);
            mw.aload(1); // utf8
            mw.aload(4); // instance
            mw.bipush(fieldIndex);
            mw.lload(2); // features
            mw.invokeinterface(TYPE_OBJECT_READER, "readFieldUTF8",
                    "(Lcom/alibaba/fastjson3/JSONParser$UTF8;Ljava/lang/Object;IJ)V");
        }
    }

    /**
     * Generate offset-based field value reading for the speculation phase.
     * Uses readStringOff/readIntOff/etc. that take offset as parameter and return new offset.
     * For types without offset-based methods, syncs this.offset and delegates.
     * Local 12 = off (int), local 11 = frArray (FieldReader[]).
     */
    private static void generateFieldCaseOff(
            MethodWriter mw,
            String classInternalName,
            FieldReader fr,
            int fieldIndex,
            String readOffDesc
    ) {
        Class<?> fc = fr.fieldClass;

        if (fc == String.class || fc == int.class || fc == long.class
                || fc == double.class || fc == boolean.class) {
            // off = utf8.readXxxOff(off, instance, reader)
            String methodName;
            if (fc == String.class) {
                methodName = "readStringOff";
            } else if (fc == int.class) {
                methodName = "readIntOff";
            } else if (fc == long.class) {
                methodName = "readLongOff";
            } else if (fc == double.class) {
                methodName = "readDoubleOff";
            } else {
                methodName = "readBooleanOff";
            }
            mw.aload(1);  // utf8
            mw.iload(12); // off
            mw.aload(4);  // instance
            mw.aload(11); // frArray
            mw.bipush(fieldIndex);
            mw.aaload();   // reader
            mw.invokevirtual(TYPE_JSON_PARSER_UTF8, methodName, readOffDesc);
            mw.istore(12); // off = result
        } else {
            // Complex type or float: sync offset, delegate, get offset back
            mw.aload(1);
            mw.iload(12);
            mw.invokevirtual(TYPE_JSON_PARSER, "setOffset", "(I)V");

            if (fc == float.class) {
                mw.aload(4);
                mw.getstatic(classInternalName, "fo" + fieldIndex, "J");
                mw.aload(1);
                mw.invokevirtual(TYPE_JSON_PARSER_UTF8, "readDoubleDirect", "()D");
                mw.d2f();
                mw.invokestatic(TYPE_JDK_UTILS, "putFloat", "(Ljava/lang/Object;JF)V");
            } else {
                mw.aload(0);
                mw.getfield(classInternalName, "fallback", DESC_OBJECT_READER);
                mw.aload(1);
                mw.aload(4);
                mw.bipush(fieldIndex);
                mw.lload(2);
                mw.invokeinterface(TYPE_OBJECT_READER, "readFieldUTF8",
                        "(Lcom/alibaba/fastjson3/JSONParser$UTF8;Ljava/lang/Object;IJ)V");
            }

            mw.aload(1);
            mw.invokevirtual(TYPE_JSON_PARSER, "getOffset", "()I");
            mw.istore(12);
        }
    }
}

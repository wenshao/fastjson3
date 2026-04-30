package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONGenerator;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ObjectWriter;
import com.alibaba.fastjson3.WriteFeature;
import com.alibaba.fastjson3.annotation.Inclusion;
import com.alibaba.fastjson3.filter.NameFilter;
import com.alibaba.fastjson3.filter.PropertyFilter;
import com.alibaba.fastjson3.filter.ValueFilter;
import com.alibaba.fastjson3.util.JDKUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Represents a single field to be written during JSON serialization.
 * Pre-encodes the field name token ({@code "fieldName":}) as both char[] and byte[]
 * for direct bulk copy during writing.
 *
 * <p>Uses a type tag + switch dispatch instead of polymorphic subclasses to keep
 * the call site monomorphic (avoids megamorphic vtable dispatch in the hot loop).</p>
 */
public final class FieldWriter implements Comparable<FieldWriter> {
    // Type tags
    public static final int TYPE_GENERIC = 0;
    public static final int TYPE_STRING = 1;
    public static final int TYPE_INT = 2;
    public static final int TYPE_LONG = 3;
    public static final int TYPE_DOUBLE = 4;
    public static final int TYPE_FLOAT = 5;
    public static final int TYPE_BOOL = 6;
    public static final int TYPE_OBJECT = 7;
    public static final int TYPE_LIST_STRING = 8;
    public static final int TYPE_LIST_OBJECT = 9;
    public static final int TYPE_LONG_ARRAY = 10;
    public static final int TYPE_INT_ARRAY = 11;
    public static final int TYPE_STRING_ARRAY = 12;
    public static final int TYPE_DOUBLE_ARRAY = 13;
    public static final int TYPE_ENUM = 14;

    final String fieldName;
    final int ordinal;
    final Type fieldType;
    public final Class<?> fieldClass;
    final Method getter;
    final Field field;
    public final long fieldOffset; // Unsafe field offset, -1 if unavailable
    public final int typeTag;
    public final Inclusion inclusion; // resolved at creation time, DEFAULT = no extra check

    // Pre-encoded field name token: "fieldName":
    public final char[] nameChars;
    public final byte[] nameBytes;
    // Pre-encoded as long[] for bulk Unsafe.putLong writes (8 bytes at a time)
    public final long[] nameByteLongs;
    public final int nameBytesLen; // actual byte count (not padded)

    // For TYPE_OBJECT / TYPE_LIST_OBJECT: cached ObjectWriter + class guard
    private volatile ObjectWriter<Object> cachedWriter;
    private volatile Class<?> cachedWriterClass;
    // Separate cache for @JSONField(unwrapped=true): we need a FieldWriter[] to
    // iterate, and ASM writers don't expose theirs. Populated from a dedicated
    // ReflectObjectWriter so the unwrap path works regardless of what kind of
    // writer the global mapper cached for the inner type.
    private volatile FieldWriter[] cachedUnwrapFieldWriters;
    private volatile Class<?> cachedUnwrapFieldWritersClass;

    // For TYPE_LIST_STRING / TYPE_LIST_OBJECT: element class
    public final Class<?> elementClass;

    // Optional: date/time format pattern + classified strategy. The strategy
    // recognizes special tokens ("millis", "unixtime", "iso8601") and the
    // five fastjson2-equivalent fast-path patterns; anything else falls
    // through to a cached DateTimeFormatter.
    public final String format;
    final com.alibaba.fastjson3.util.DateFormatPattern datePattern;

    // Optional: custom ObjectWriter for this field (from @JSONField(serializeUsing=))
    public final ObjectWriter<Object> customWriter;

    // Optional: label for view-based filtering (from @JSONField(label=))
    public final String label;

    // Optional: field-level serialization features (from @JSONField(serializeFeatures=))
    public final long fieldFeatures;

    // Optional: unwrap nested object properties into parent (from @JSONField(unwrapped=true))
    public final boolean unwrapped;

    /**
     * For {@link #TYPE_ENUM} fields: pre-encoded UTF-8 bytes per enum ordinal, computed
     * once at FieldWriter construction. Skips the per-write {@code name().getBytes(UTF_8)}
     * allocation and {@code StringCoding.hasNegatives} scan that async-profiler showed
     * consumed ~4% of EishayWriteUTF8Bytes CPU. {@code null} when {@code fieldClass} is
     * not a concrete enum type (e.g., declared as raw {@code Enum<?>} or {@code Object}).
     */
    public final byte[][] enumNameBytesUtf8;

    /**
     * Per-ordinal non-ASCII flag. {@code null} when every constant is pure ASCII — the
     * common case for Java identifiers. Only allocated when at least one constant has a
     * byte &lt; 0, in which case the full {@code boolean[]} is used for O(1) per-write lookup.
     */
    public final boolean[] enumConstantHasNonAscii;

    // Private constructor — use factory methods
    @SuppressWarnings("unchecked")
    private FieldWriter(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass,
            Method getter, Field field, int typeTag, Class<?> elementClass,
            Inclusion inclusion, String format, ObjectWriter<?> customWriter, String label,
            long fieldFeatures, boolean unwrapped
    ) {
        this.fieldName = fieldName;
        this.ordinal = ordinal;
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.getter = getter;
        this.field = field;
        this.fieldOffset = (field != null && JDKUtils.UNSAFE_AVAILABLE)
                ? JDKUtils.objectFieldOffset(field) : -1;
        this.typeTag = typeTag;
        this.elementClass = elementClass;
        this.inclusion = inclusion;
        this.format = format;
        this.datePattern = com.alibaba.fastjson3.util.DateFormatPattern.of(format);
        this.customWriter = (ObjectWriter<Object>) customWriter;
        this.label = (label != null && !label.isEmpty()) ? label : null;
        this.fieldFeatures = fieldFeatures;
        this.unwrapped = unwrapped;
        this.nameChars = encodeNameChars(fieldName);
        this.nameBytes = encodeNameBytes(fieldName);
        this.nameBytesLen = nameBytes.length;
        this.nameByteLongs = encodeByteLongs(nameBytes);

        // Pre-encode enum constant names once per FieldWriter. Only populated for concrete
        // enum field types — generic Enum<?> or Object declarations fall back to the per-call
        // encoding path at serialization time.
        //
        // Note on enum static initialization: calling getEnumConstants() triggers static
        // initialization of the enum class. If the class has a throwing static initializer
        // or a missing class dependency, it would abort FieldWriter construction — a
        // functional regression vs the pre-PR behavior where writer construction succeeds
        // and the failure only surfaces when a non-null enum value is actually written. The
        // try/catch below preserves the pre-PR semantics: any Throwable from the init path
        // (ExceptionInInitializerError, NoClassDefFoundError, etc.) falls back to the
        // per-call encoding path in JSONGenerator, where the same failure will surface at
        // the first actual write of a non-null enum value. Fields that are always null in
        // practice never trigger init at all.
        byte[][] enumBytes = null;
        boolean[] nonAscii = null;
        if (typeTag == TYPE_ENUM && fieldClass != null && fieldClass.isEnum()) {
            try {
                Object[] constants = fieldClass.getEnumConstants();
                if (constants != null && constants.length > 0) {
                    enumBytes = new byte[constants.length][];
                    for (int i = 0; i < constants.length; i++) {
                        byte[] b = ((Enum<?>) constants[i]).name().getBytes(StandardCharsets.UTF_8);
                        enumBytes[i] = b;
                        for (byte v : b) {
                            if (v < 0) {
                                if (nonAscii == null) {
                                    nonAscii = new boolean[constants.length];
                                }
                                nonAscii[i] = true;
                                break;
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
                // Enum class static init failed or is unavailable — leave cache null so
                // the runtime path in JSONGenerator falls back to per-call encoding,
                // preserving the pre-PR "fail lazily on actual write" semantics.
                enumBytes = null;
                nonAscii = null;
            }
        }
        this.enumNameBytesUtf8 = enumBytes;
        this.enumConstantHasNonAscii = nonAscii;
    }

    // ==================== Factory methods ====================

    public static FieldWriter ofGetter(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass, Method getter
    ) {
        return ofGetter(fieldName, ordinal, fieldType, fieldClass, getter, Inclusion.DEFAULT);
    }

    public static FieldWriter ofGetter(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass, Method getter,
            Inclusion inclusion
    ) {
        return new FieldWriter(fieldName, ordinal, fieldType, fieldClass, getter, null,
                typeTagFor(fieldClass), null, inclusion, null, null, null, 0, false);
    }

    public static FieldWriter ofGetter(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass, Method getter,
            Inclusion inclusion, String format, ObjectWriter<?> customWriter, String label
    ) {
        return ofGetter(fieldName, ordinal, fieldType, fieldClass, getter,
                inclusion, format, customWriter, label, 0, false);
    }

    public static FieldWriter ofGetter(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass, Method getter,
            Inclusion inclusion, String format, ObjectWriter<?> customWriter, String label,
            long fieldFeatures, boolean unwrapped
    ) {
        return new FieldWriter(fieldName, ordinal, fieldType, fieldClass, getter, null,
                typeTagFor(fieldClass), null, inclusion, format, customWriter, label,
                fieldFeatures, unwrapped);
    }

    public static FieldWriter ofField(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass, Field field
    ) {
        return ofField(fieldName, ordinal, fieldType, fieldClass, field, Inclusion.DEFAULT);
    }

    public static FieldWriter ofField(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass, Field field,
            Inclusion inclusion
    ) {
        return new FieldWriter(fieldName, ordinal, fieldType, fieldClass, null, field,
                typeTagFor(fieldClass), null, inclusion, null, null, null, 0, false);
    }

    public static FieldWriter ofField(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass, Field field,
            Inclusion inclusion, String format, ObjectWriter<?> customWriter, String label
    ) {
        return ofField(fieldName, ordinal, fieldType, fieldClass, field,
                inclusion, format, customWriter, label, 0, false);
    }

    public static FieldWriter ofField(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass, Field field,
            Inclusion inclusion, String format, ObjectWriter<?> customWriter, String label,
            long fieldFeatures, boolean unwrapped
    ) {
        return new FieldWriter(fieldName, ordinal, fieldType, fieldClass, null, field,
                typeTagFor(fieldClass), null, inclusion, format, customWriter, label,
                fieldFeatures, unwrapped);
    }

    public static FieldWriter ofList(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass,
            Class<?> elementClass, Method getter
    ) {
        return ofList(fieldName, ordinal, fieldType, fieldClass, elementClass, getter, Inclusion.DEFAULT);
    }

    public static FieldWriter ofList(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass,
            Class<?> elementClass, Method getter, Inclusion inclusion
    ) {
        int tag = elementClass == String.class ? TYPE_LIST_STRING : TYPE_LIST_OBJECT;
        return new FieldWriter(fieldName, ordinal, fieldType, fieldClass, getter, null,
                tag, elementClass, inclusion, null, null, null, 0, false);
    }

    public static FieldWriter ofList(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass,
            Class<?> elementClass, Field field
    ) {
        return ofList(fieldName, ordinal, fieldType, fieldClass, elementClass, field, Inclusion.DEFAULT);
    }

    public static FieldWriter ofList(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass,
            Class<?> elementClass, Field field, Inclusion inclusion
    ) {
        int tag = elementClass == String.class ? TYPE_LIST_STRING : TYPE_LIST_OBJECT;
        return new FieldWriter(fieldName, ordinal, fieldType, fieldClass, null, field,
                tag, elementClass, inclusion, null, null, null, 0, false);
    }

    private static int typeTagFor(Class<?> fieldClass) {
        if (fieldClass == String.class) {
            return TYPE_STRING;
        }
        if (fieldClass == int.class || fieldClass == Integer.class) {
            return TYPE_INT;
        }
        if (fieldClass == long.class || fieldClass == Long.class) {
            return TYPE_LONG;
        }
        if (fieldClass == double.class || fieldClass == Double.class) {
            return TYPE_DOUBLE;
        }
        if (fieldClass == float.class || fieldClass == Float.class) {
            return TYPE_FLOAT;
        }
        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            return TYPE_BOOL;
        }
        if (fieldClass == long[].class) {
            return TYPE_LONG_ARRAY;
        }
        if (fieldClass == int[].class) {
            return TYPE_INT_ARRAY;
        }
        if (fieldClass == String[].class) {
            return TYPE_STRING_ARRAY;
        }
        if (fieldClass == double[].class) {
            return TYPE_DOUBLE_ARRAY;
        }
        if (fieldClass.isEnum()) {
            return TYPE_ENUM;
        }
        if (!fieldClass.isPrimitive() && !fieldClass.isArray()
                && fieldClass != Object.class
                && !java.util.Collection.class.isAssignableFrom(fieldClass)
                && !java.util.Map.class.isAssignableFrom(fieldClass)) {
            return TYPE_OBJECT;
        }
        return TYPE_GENERIC;
    }

    // ==================== Name encoding ====================

    /**
     * Pre-encode {@code "name":} as a char[]. Bytes between the quotes are
     * the JSON-escaped form (per RFC 8259 §7) so the output is valid JSON
     * and the byte-prefix matcher used by {@code readFieldsLoop} compares
     * against the same wire form a conformant parser would consume.
     *
     * <p>Pre-fix this method emitted {@code name} verbatim; for a field
     * declared with {@code @JSONField(name = "a\\b")} (backslash + b)
     * that produced {@code "a\b":} on the wire — which any JSON consumer
     * (including fastjson when reading externally-produced correct JSON)
     * interprets as backspace, silently losing the field.
     */
    static char[] encodeNameChars(String name) {
        int len = name.length();
        if (!nameNeedsEscape(name, len)) {
            char[] chars = new char[len + 3];
            chars[0] = '"';
            name.getChars(0, len, chars, 1);
            chars[len + 1] = '"';
            chars[len + 2] = ':';
            return chars;
        }
        // Escape pass — char-for-char, never invoked at write time so cost
        // here is one allocation per FieldWriter init not per serialisation.
        StringBuilder sb = new StringBuilder(len + 8);
        sb.append('"');
        for (int i = 0; i < len; i++) {
            char ch = name.charAt(i);
            if (ch == '\\' || ch == '"') {
                sb.append('\\').append(ch);
            } else if (ch == '\b') {
                sb.append("\\b");
            } else if (ch == '\f') {
                sb.append("\\f");
            } else if (ch == '\n') {
                sb.append("\\n");
            } else if (ch == '\r') {
                sb.append("\\r");
            } else if (ch == '\t') {
                sb.append("\\t");
            } else if (ch < 0x20) {
                sb.append("\\u00")
                  .append(HEX[(ch >> 4) & 0xF])
                  .append(HEX[ch & 0xF]);
            } else {
                sb.append(ch);
            }
        }
        sb.append('"').append(':');
        char[] out = new char[sb.length()];
        sb.getChars(0, sb.length(), out, 0);
        return out;
    }

    static byte[] encodeNameBytes(String name) {
        // Reuse char-side escape, then UTF-8 encode the result. Single
        // allocation cost is negligible (FieldWriter init only).
        // Use an explicit encoder configured to substitute lone surrogates
        // with U+FFFD (EF BF BD), matching JSONGenerator's value-side
        // surrogate handling. Java's default String.getBytes(UTF_8) uses
        // `?` (0x3F) as the substitution byte, which would diverge from
        // the value path's behaviour on the same input.
        char[] encoded = encodeNameChars(name);
        java.nio.charset.CharsetEncoder enc = StandardCharsets.UTF_8.newEncoder()
                .onMalformedInput(java.nio.charset.CodingErrorAction.REPLACE)
                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPLACE)
                .replaceWith(new byte[]{(byte) 0xEF, (byte) 0xBF, (byte) 0xBD});
        try {
            java.nio.ByteBuffer bb = enc.encode(java.nio.CharBuffer.wrap(encoded));
            byte[] out = new byte[bb.remaining()];
            bb.get(out);
            return out;
        } catch (java.nio.charset.CharacterCodingException e) {
            // REPLACE actions cannot raise CharacterCodingException —
            // re-throw as unchecked just so the signature stays clean.
            throw new IllegalStateException(e);
        }
    }

    private static boolean nameNeedsEscape(String name, int len) {
        for (int i = 0; i < len; i++) {
            char ch = name.charAt(i);
            if (ch < 0x20 || ch == '"' || ch == '\\') {
                return true;
            }
        }
        return false;
    }

    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /** Check if putLong is faster than arraycopy (x86 only). Android-safe. */
    private static final boolean PUTLONG_FAST = detectPutLongFast();
    private static boolean detectPutLongFast() {
        try {
            java.lang.reflect.Field f = JDKUtils.class.getField("PUTLONG_FAST");
            return (Boolean) f.get(null);
        } catch (Throwable e) {
            // Android JDKUtils doesn't have PUTLONG_FAST
            return false;
        }
    }

    /**
     * Encode byte[] as long[] for bulk Unsafe.putLong writes.
     * Pads to 8-byte boundary so the last long can safely overwrite trailing bytes.
     */
    public static long[] encodeByteLongs(byte[] bytes) {
        if (!JDKUtils.UNSAFE_AVAILABLE || !PUTLONG_FAST) {
            return null;
        }
        int len = bytes.length;
        int longCount = (len + 7) >>> 3; // ceil(len / 8)
        byte[] padded = new byte[longCount << 3]; // pad to 8-byte boundary
        System.arraycopy(bytes, 0, padded, 0, len);
        long[] result = new long[longCount];
        for (int i = 0; i < longCount; i++) {
            result[i] = JDKUtils.getLongDirect(padded, i << 3);
        }
        return result;
    }

    // ==================== Accessors ====================

    public String getFieldName() {
        return fieldName;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public Type getFieldType() {
        return fieldType;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }

    // ==================== Field value reading ====================

    private Object getFieldValue(Object bean) {
        try {
            if (getter != null) {
                return getter.invoke(bean);
            } else {
                return field.get(bean);
            }
        } catch (InvocationTargetException e) {
            throw new JSONException("Error reading field '" + fieldName + "'", e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new JSONException("Error reading field '" + fieldName + "'", e);
        }
    }


    private Object getObjectValue(Object bean) {
        if (fieldOffset >= 0) {
            if (fieldClass == int.class) {
                return JDKUtils.getInt(bean, fieldOffset);
            } else if (fieldClass == long.class) {
                return JDKUtils.getLongField(bean, fieldOffset);
            } else if (fieldClass == boolean.class) {
                return JDKUtils.getBoolean(bean, fieldOffset);
            } else if (fieldClass == double.class) {
                return JDKUtils.getDouble(bean, fieldOffset);
            } else if (fieldClass == float.class) {
                return JDKUtils.getFloat(bean, fieldOffset);
            } else {
                return JDKUtils.getObject(bean, fieldOffset);
            }
        }
        return getFieldValue(bean);
    }

    private void writeName(JSONGenerator generator) {
        if (generator.bypassStaticPath) {
            generator.writeName(fieldName);
        } else {
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        }
    }

    private void writeNull(JSONGenerator generator, long features) {
        // Merge with generator features to pick up NullAsDefaultValue expansion
        features |= generator.getFeatures();
        if (inclusion == Inclusion.ALWAYS
                || (features & WriteFeature.WriteNulls.mask) != 0) {
            writeName(generator);
            generator.writeNull();
            return;
        }

        // Type-specific null defaults — check typeTag first, then fieldClass for collections
        if (typeTag == TYPE_STRING
                && (features & WriteFeature.WriteNullStringAsEmpty.mask) != 0) {
            writeName(generator);
            generator.writeString("");
        } else if ((typeTag == TYPE_LIST_STRING || typeTag == TYPE_LIST_OBJECT
                || java.util.Collection.class.isAssignableFrom(fieldClass))
                && (features & WriteFeature.WriteNullListAsEmpty.mask) != 0) {
            writeName(generator);
            generator.startArray();
            generator.endArray();
        } else if ((typeTag == TYPE_INT || typeTag == TYPE_LONG
                || typeTag == TYPE_DOUBLE || typeTag == TYPE_FLOAT
                || Number.class.isAssignableFrom(fieldClass))
                && (features & WriteFeature.WriteNullNumberAsZero.mask) != 0) {
            writeName(generator);
            if (typeTag == TYPE_LONG) {
                generator.writeInt64(0L);
            } else if (typeTag == TYPE_DOUBLE) {
                generator.writeDouble(0D);
            } else if (typeTag == TYPE_FLOAT) {
                generator.writeFloat(0F);
            } else {
                generator.writeInt32(0);
            }
        } else if ((typeTag == TYPE_BOOL || fieldClass == Boolean.class)
                && (features & WriteFeature.WriteNullBooleanAsFalse.mask) != 0) {
            writeName(generator);
            generator.writeBool(false);
        }
    }

    /**
     * Check if a value is "empty" for NON_EMPTY inclusion.
     * Only called when inclusion == NON_EMPTY (resolved at creation time).
     */
    /**
     * Check if a value is a container type whose reference tracking is handled
     * internally by JSONGenerator.writeAny(). Avoids double pushReference.
     */
    private static boolean isContainerType(Object value) {
        return value instanceof java.util.Map
                || value instanceof java.util.Collection
                || value instanceof Object[]
                || value instanceof com.alibaba.fastjson3.JSONObject
                || value instanceof com.alibaba.fastjson3.JSONArray;
    }

    private static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String s) {
            return s.isEmpty();
        }
        if (value instanceof java.util.Collection<?> c) {
            return c.isEmpty();
        }
        if (value instanceof java.util.Map<?, ?> m) {
            return m.isEmpty();
        }
        if (value.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(value) == 0;
        }
        return false;
    }

    // ==================== Main dispatch ====================

    /**
     * Write this field (name + value) to the generator.
     * Single monomorphic method with switch dispatch on type tag.
     */
    public void writeField(JSONGenerator generator, Object bean, long features) {
        try {
            writeFieldInternal(generator, bean, features);
        } catch (JSONException e) {
            if (generator.ignoreErrorGetter) {
                return; // skip this field silently
            }
            throw e;
        }
    }

    private void writeFieldInternal(JSONGenerator generator, Object bean, long features) {
        // Merge field-level features
        if (fieldFeatures != 0) {
            features |= fieldFeatures;
        }
        // Custom writer takes full control
        if (customWriter != null) {
            writeFieldCustom(generator, bean, features);
            return;
        }
        switch (typeTag) {
            case TYPE_STRING -> writeString(generator, bean, features);
            case TYPE_INT -> writeInt(generator, bean, features);
            case TYPE_LONG -> writeLong(generator, bean, features);
            case TYPE_DOUBLE -> writeDouble(generator, bean, features);
            case TYPE_FLOAT -> writeFloat(generator, bean, features);
            case TYPE_BOOL -> writeBool(generator, bean, features);
            case TYPE_OBJECT -> writeObject(generator, bean, features);
            case TYPE_LIST_STRING -> writeListString(generator, bean, features);
            case TYPE_LIST_OBJECT -> writeListObject(generator, bean, features);
            case TYPE_LONG_ARRAY -> writeLongArray(generator, bean, features);
            case TYPE_INT_ARRAY -> writeIntArray(generator, bean, features);
            case TYPE_STRING_ARRAY -> writeStringArray(generator, bean, features);
            case TYPE_DOUBLE_ARRAY -> writeDoubleArray(generator, bean, features);
            default -> writeGeneric(generator, bean, features);
        }
    }

    // ==================== Type-specific write methods ====================

    private void writeString(JSONGenerator generator, Object bean, long features) {
        String value = (String) getObjectValue(bean);
        if (value == null) {
            writeNull(generator, features);
            return;
        }
        if (inclusion == Inclusion.NON_EMPTY && value.isEmpty()) {
            return;
        }
        generator.writeNameString(nameByteLongs, nameBytesLen, nameBytes, nameChars, value);
    }

    private void writeInt(JSONGenerator generator, Object bean, long features) {
        if (fieldOffset >= 0 && fieldClass == int.class) {
            int val = JDKUtils.getInt(bean, fieldOffset);
            if (generator.notWriteDefaultValue && val == 0) {
                return;
            }
            generator.writeNameInt32(nameByteLongs, nameBytesLen, nameBytes, nameChars, val);
        } else {
            Object value = getObjectValue(bean);
            if (value == null) {
                writeNull(generator, features);
                return;
            }
            int val = (Integer) value;
            if (generator.notWriteDefaultValue && val == 0) {
                return;
            }
            generator.writeNameInt32(nameByteLongs, nameBytesLen, nameBytes, nameChars, val);
        }
    }

    private void writeLong(JSONGenerator generator, Object bean, long features) {
        if (fieldOffset >= 0 && fieldClass == long.class) {
            long val = JDKUtils.getLongField(bean, fieldOffset);
            if (generator.notWriteDefaultValue && val == 0L) {
                return;
            }
            generator.writeNameInt64(nameByteLongs, nameBytesLen, nameBytes, nameChars, val);
        } else {
            Object value = getObjectValue(bean);
            if (value == null) {
                writeNull(generator, features);
                return;
            }
            long val = (Long) value;
            if (generator.notWriteDefaultValue && val == 0L) {
                return;
            }
            generator.writeNameInt64(nameByteLongs, nameBytesLen, nameBytes, nameChars, val);
        }
    }

    private void writeDouble(JSONGenerator generator, Object bean, long features) {
        if (fieldOffset >= 0 && fieldClass == double.class) {
            double val = JDKUtils.getDouble(bean, fieldOffset);
            if (generator.notWriteDefaultValue && Double.doubleToRawLongBits(val) == 0L) {
                return;
            }
            generator.writeNameDouble(nameByteLongs, nameBytesLen, nameBytes, nameChars, val);
        } else {
            Object value = getObjectValue(bean);
            if (value == null) {
                writeNull(generator, features);
                return;
            }
            double val = (Double) value;
            if (generator.notWriteDefaultValue && Double.doubleToRawLongBits(val) == 0L) {
                return;
            }
            generator.writeNameDouble(nameByteLongs, nameBytesLen, nameBytes, nameChars, val);
        }
    }

    private void writeFloat(JSONGenerator generator, Object bean, long features) {
        if (fieldOffset >= 0 && fieldClass == float.class) {
            float val = JDKUtils.getFloat(bean, fieldOffset);
            if (generator.notWriteDefaultValue && Float.floatToRawIntBits(val) == 0) {
                return;
            }
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
            generator.writeFloat(val);
        } else {
            Object value = getObjectValue(bean);
            if (value == null) {
                writeNull(generator, features);
                return;
            }
            float val = (Float) value;
            if (generator.notWriteDefaultValue && Float.floatToRawIntBits(val) == 0) {
                return;
            }
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
            generator.writeFloat(val);
        }
    }

    private void writeBool(JSONGenerator generator, Object bean, long features) {
        if (fieldOffset >= 0 && fieldClass == boolean.class) {
            boolean val = JDKUtils.getBoolean(bean, fieldOffset);
            if (generator.notWriteDefaultValue && !val) {
                return;
            }
            generator.writeNameBool(nameByteLongs, nameBytesLen, nameBytes, nameChars, val);
        } else {
            Object value = getObjectValue(bean);
            if (value == null) {
                writeNull(generator, features);
                return;
            }
            boolean val = (Boolean) value;
            if (generator.notWriteDefaultValue && !val) {
                return;
            }
            generator.writeNameBool(nameByteLongs, nameBytesLen, nameBytes, nameChars, val);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeObject(JSONGenerator generator, Object bean, long features) {
        Object value = getObjectValue(bean);
        if (value == null) {
            if (!unwrapped) {
                writeNull(generator, features);
            }
            return;
        }
        if (inclusion == Inclusion.NON_EMPTY && isEmpty(value)) {
            return;
        }
        // Unwrapped: write nested object's fields directly into parent (no field name, no braces).
        // We need a concrete FieldWriter[] to iterate; ASM writers don't publish theirs, so
        // we prefer the mapper's cached writer when it's a ReflectObjectWriter (already
        // configured with the owning mapper's mix-in + useJacksonAnnotation), and fall back
        // to a fresh ReflectObjectWriter built with the mapper's config when the cached
        // writer is an ASM class.
        if (unwrapped) {
            // Depth guard catches circular unwrap chains (A.b @unwrapped B,
            // B.a @unwrapped A) that would otherwise recurse until the JVM
            // stack overflows. The reader-side UNWRAP_VISITED mechanism only
            // detects cycles at field-list expansion; on the writer, the cycle
            // only manifests with actual object graphs, so we rely on the
            // generator's existing depth counter to bound recursion to
            // MAX_WRITE_DEPTH and surface a clean JSONException.
            generator.incrementDepth();
            try {
                FieldWriter[] innerFieldWriters = resolveUnwrapFieldWriters(generator, value.getClass());
                ObjectWriterCreator.writeFields(generator, innerFieldWriters, value, features);
            } finally {
                generator.decrementDepth();
            }
            return;
        }
        if (generator.notWriteEmptyArray) {
            if (value instanceof java.util.Collection && ((java.util.Collection<?>) value).isEmpty()) {
                return;
            }
            if (value.getClass().isArray() && java.lang.reflect.Array.getLength(value) == 0) {
                return;
            }
        }
        // Field-level @JSONField(format=...) handled here (datePattern is
        // pre-classified at FieldWriter construction). Mapper-level default
        // dateFormat is honored further down inside the per-type
        // ObjectWriter (see BuiltinCodecs date/temporal writers), which
        // works for both the reflection path AND the ASM-generated path.
        if (datePattern != null
                && (value instanceof java.time.temporal.TemporalAccessor
                        || value instanceof java.util.Date)) {
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
            datePattern.write(generator, value);
            return;
        }
        // Reference tracking lives on the writer side: bean writers (ASM
        // and ReflectObjectWriter) push their own bean before emitting
        // its body. Pushing the value here (the FieldWriter / value side)
        // would double-push when the resolved writer is also a bean writer
        // — surfacing as a false circular reference for an acyclic graph.
        // Container writers (Map/List/Object[]/JSONObject/JSONArray) push
        // themselves at writeAny time (lines 911/1064 below + JSONGenerator's
        // own writeAny container branches).
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);

        Class<?> valueClass = value.getClass();
        ObjectWriter<Object> writer = cachedWriter;
        if (writer == null || cachedWriterClass != valueClass) {
            writer = (ObjectWriter<Object>) generator.effectiveMapper().getObjectWriter(valueClass);
            if (writer != null) {
                // Write class guard BEFORE writer data to prevent another thread
                // from seeing new writer with stale class guard
                cachedWriterClass = valueClass;
                cachedWriter = writer;
            }
        }
        if (writer != null) {
            writer.write(generator, value, fieldName, fieldType, features);
        } else {
            // Fallback: inline common types instead of writeAny
            if (value instanceof String s) {
                generator.writeString(s);
            } else if (value instanceof Integer i) {
                generator.writeInt32(i);
            } else if (value instanceof Long l) {
                generator.writeInt64(l);
            } else if (value instanceof Boolean b) {
                generator.writeBool(b);
            } else if (value instanceof Double d) {
                generator.writeDouble(d);
            } else {
                generator.writeAny(value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void writeListString(JSONGenerator generator, Object bean, long features) {
        java.util.List<?> list = (java.util.List<?>) getObjectValue(bean);
        if (list == null) {
            writeNull(generator, features);
            return;
        }
        if (inclusion == Inclusion.NON_EMPTY && list.isEmpty()) {
            return;
        }
        if (generator.notWriteEmptyArray && list.isEmpty()) {
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        generator.startArray();

        int size = list.size();
        // Capacity pre-calculation for List<String>
        if (size > 0) {
            // Estimate: average 16 bytes per string element
            generator.ensureCapacityPublic(size * 16 + 16);
        }
        
        for (int i = 0; i < size; i++) {
            generator.beforeArrayValue();
            String s = (String) list.get(i);
            if (s == null) {
                generator.writeNull();
            } else {
                generator.writeString(s);
            }
        }
        generator.endArray();
    }

    @SuppressWarnings("unchecked")
    private void writeListObject(JSONGenerator generator, Object bean, long features) {
        java.util.List<?> list = (java.util.List<?>) getObjectValue(bean);
        if (list == null) {
            writeNull(generator, features);
            return;
        }
        if (inclusion == Inclusion.NON_EMPTY && list.isEmpty()) {
            return;
        }
        if (generator.notWriteEmptyArray && list.isEmpty()) {
            return;
        }
        generator.incrementDepth();
        generator.pushReference(list);
        try {
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
            generator.startArray();

            ObjectWriter<Object> elemWriter = cachedWriter;
            if (elemWriter == null && elementClass != null) {
                elemWriter = (ObjectWriter<Object>) generator.effectiveMapper().getObjectWriter(elementClass);
                if (elemWriter != null) {
                    cachedWriter = elemWriter;
                }
            }
            
            int size = list.size();
            // Optimized: capacity pre-calculation for List<POJO>
            if (size > 0 && elemWriter instanceof com.alibaba.fastjson3.writer.ObjectWriterCreator.ReflectObjectWriter) {
                // Estimate capacity: roughly 64 bytes per POJO element
                generator.ensureCapacityPublic(size * 64 + 16);
            }
            
            for (int i = 0; i < size; i++) {
                generator.beforeArrayValue();
                Object item = list.get(i);
                if (item == null) {
                    generator.writeNull();
                } else if (elemWriter != null) {
                    elemWriter.write(generator, item, null, null, features);
                } else {
                    writeAnyItem(generator, item, features);
                }
            }
            generator.endArray();
        } finally {
            generator.popReference(list);
            generator.decrementDepth();
        }
    }

    private void writeLongArray(JSONGenerator generator, Object bean, long features) {
        long[] value = (long[]) getObjectValue(bean);
        if (value == null) {
            writeNull(generator, features);
            return;
        }
        if (inclusion == Inclusion.NON_EMPTY && value.length == 0) {
            return;
        }
        if (generator.notWriteEmptyArray && value.length == 0) {
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        generator.writeLongArray(value);
    }

    private void writeIntArray(JSONGenerator generator, Object bean, long features) {
        int[] value = (int[]) getObjectValue(bean);
        if (value == null) {
            writeNull(generator, features);
            return;
        }
        if (inclusion == Inclusion.NON_EMPTY && value.length == 0) {
            return;
        }
        if (generator.notWriteEmptyArray && value.length == 0) {
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        generator.writeIntArray(value);
    }

    private void writeStringArray(JSONGenerator generator, Object bean, long features) {
        String[] value = (String[]) getObjectValue(bean);
        if (value == null) {
            writeNull(generator, features);
            return;
        }
        if (inclusion == Inclusion.NON_EMPTY && value.length == 0) {
            return;
        }
        if (generator.notWriteEmptyArray && value.length == 0) {
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        generator.writeStringArray(value);
    }

    private void writeDoubleArray(JSONGenerator generator, Object bean, long features) {
        double[] value = (double[]) getObjectValue(bean);
        if (value == null) {
            writeNull(generator, features);
            return;
        }
        if (inclusion == Inclusion.NON_EMPTY && value.length == 0) {
            return;
        }
        if (generator.notWriteEmptyArray && value.length == 0) {
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        generator.writeDoubleArray(value);
    }

    private void writeGeneric(JSONGenerator generator, Object bean, long features) {
        Object value = getObjectValue(bean);
        if (value == null) {
            writeNull(generator, features);
            return;
        }
        if (inclusion == Inclusion.NON_EMPTY && isEmpty(value)) {
            return;
        }
        if (generator.notWriteEmptyArray) {
            if (value instanceof java.util.Collection && ((java.util.Collection<?>) value).isEmpty()) {
                return;
            }
            if (value.getClass().isArray() && java.lang.reflect.Array.getLength(value) == 0) {
                return;
            }
        }
        // Field-level @JSONField(format=) on a generic-typed field (declared
        // type Object / T) — runtime value may still be a Temporal or Date.
        // Mirror the writeObject branch at line ~810: pin field-level format
        // ahead of the type dispatch so the format string isn't silently
        // dropped just because the field's declared type is Object.
        if (datePattern != null
                && (value instanceof java.time.temporal.TemporalAccessor
                        || value instanceof java.util.Date)) {
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
            datePattern.write(generator, value);
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        if (value instanceof String s) {
            generator.writeString(s);
        } else if (value instanceof Integer i) {
            generator.writeInt32(i);
        } else if (value instanceof Long l) {
            generator.writeInt64(l);
        } else if (value instanceof Boolean b) {
            generator.writeBool(b);
        } else if (value instanceof Double d) {
            generator.writeDouble(d);
        } else if (value instanceof Float f) {
            generator.writeFloat(f);
        } else if (value instanceof BigDecimal bd) {
            generator.writeDecimal(bd);
        } else if (isContainerType(value)) {
            // Container types: delegate to writeAny which handles sortMapKeys,
            // reference detection, and depth limits
            if (value instanceof java.util.Map<?, ?>) {
                generator.writeAny(value);
            } else if (value instanceof java.util.List<?> list) {
                generator.incrementDepth();
                generator.pushReference(list);
                try {
                    generator.startArray();
                    Class<?> previousClass = null;
                    ObjectWriter<Object> previousWriter = null;
                    for (int i = 0, size = list.size(); i < size; i++) {
                        generator.beforeArrayValue();
                        Object item = list.get(i);
                        if (item == null) {
                            generator.writeNull();
                        } else if (item instanceof String s) {
                            generator.writeString(s);
                        } else if (item instanceof Integer in) {
                            generator.writeInt32(in);
                        } else if (item instanceof Long lo) {
                            generator.writeInt64(lo);
                        } else if (item instanceof Boolean bo) {
                            generator.writeBool(bo);
                        } else if (item instanceof Double dou) {
                            generator.writeDouble(dou);
                        } else {
                            Class<?> itemClass = item.getClass();
                            ObjectWriter<Object> writer;
                            if (itemClass == previousClass) {
                                writer = previousWriter;
                            } else {
                                writer = (ObjectWriter<Object>) generator.effectiveMapper().getObjectWriter(itemClass);
                                previousClass = itemClass;
                                previousWriter = writer;
                            }
                            if (writer != null) {
                                writer.write(generator, item, null, null, features);
                            } else {
                                generator.writeAny(item);
                            }
                        }
                    }
                    generator.endArray();
                } finally {
                    generator.popReference(list);
                    generator.decrementDepth();
                }
            } else {
                generator.writeAny(value);
            }
        } else {
            // POJO types: use cached ObjectWriter instead of writeAny
            writeObjectWithWriter(generator, value, features);
        }
    }

    private void writeAnyItem(JSONGenerator generator, Object item, long features) {
        if (item == null) {
            generator.writeNull();
            return;
        }
        Class<?> itemClass = item.getClass();
        ObjectWriter<Object> writer = (ObjectWriter<Object>) generator.effectiveMapper().getObjectWriter(itemClass);
        if (writer != null) {
            writer.write(generator, item, null, null, features);
        } else {
            generator.writeAny(item);
        }
    }

    private void writeObjectWithWriter(JSONGenerator generator, Object value, long features) {
        // Reference tracking moved to writer side (ReflectObjectWriter / ASM
        // generated writers). Pushing here would double-push when the
        // resolved writer is itself a bean writer; see comment in
        // writeObject above.
        Class<?> valueClass = value.getClass();
        ObjectWriter<Object> writer = (ObjectWriter<Object>) generator.effectiveMapper().getObjectWriter(valueClass);
        if (writer != null) {
            writer.write(generator, value, fieldName, fieldType, features);
        } else {
            generator.writeAny(value);
        }
    }

    @SuppressWarnings("unchecked")
    private ObjectWriter<Object> resolveObjectWriter(JSONGenerator generator, Class<?> valueClass) {
        ObjectWriter<Object> writer = cachedWriter;
        if (writer == null || cachedWriterClass != valueClass) {
            writer = (ObjectWriter<Object>) generator.effectiveMapper().getObjectWriter(valueClass);
            if (writer != null) {
                cachedWriterClass = valueClass;
                cachedWriter = writer;
            }
        }
        return writer;
    }

    /**
     * Resolve the inner type's {@link FieldWriter} array for unwrap-time
     * emission. Prefers the mapper's cached writer when it's a
     * {@link ObjectWriterCreator.ReflectObjectWriter} — that writer was
     * already built with the mapper's own mix-in + useJacksonAnnotation
     * config, so field renames from a mix-in or Jackson's {@code @JsonProperty}
     * are honoured. Only when the cached writer is an ASM class (which
     * conceals its internal FieldWriter[]) do we fall back to building a
     * fresh ReflectObjectWriter — and then we rebuild it with the mapper's
     * mix-in lookup and useJacksonAnnotation flag so the inner's rename
     * rules still apply on the flattened keys.
     */
    private FieldWriter[] resolveUnwrapFieldWriters(JSONGenerator generator, Class<?> valueClass) {
        FieldWriter[] writers = cachedUnwrapFieldWriters;
        if (writers != null && cachedUnwrapFieldWritersClass == valueClass) {
            return writers;
        }
        // Mapper's cached writer may already be a ReflectObjectWriter with the
        // correct config — take its writers array directly.
        ObjectMapper mapper = generator.effectiveMapper();
        ObjectWriter<?> cached = mapper.getObjectWriter(valueClass);
        if (cached instanceof ObjectWriterCreator.ReflectObjectWriter cachedRow) {
            writers = cachedRow.writers;
        } else {
            // ASM or custom — build a reflect writer with the SAME config the
            // mapper uses (mix-in lookup + useJacksonAnnotation) so rename
            // rules stay consistent between nested and flattened shapes.
            Class<?> innerMixIn = mapper.getMixIn(valueClass);
            boolean jackson = mapper.useJacksonAnnotation();
            ObjectWriter<?> reflect = ObjectWriterCreator.createObjectWriter(
                    valueClass, innerMixIn, jackson);
            if (reflect instanceof ObjectWriterCreator.ReflectObjectWriter row) {
                writers = row.writers;
            } else {
                // Inner type uses a custom @JSONType(serializer=…) / @JsonAnyGetter
                // lambda path — no accessible FieldWriter[] to flatten.
                throw new JSONException("@JSONField(unwrapped=true) on " + fieldName
                        + ": inner type " + valueClass.getName()
                        + " uses a custom serializer or anyGetter and cannot be flattened");
            }
        }
        cachedUnwrapFieldWriters = writers;
        cachedUnwrapFieldWritersClass = valueClass;
        return writers;
    }

    // ==================== Filter-aware write ====================

    /**
     * Write this field with filter support. Only called when filters are configured.
     * Zero overhead when not used — callers check filter array length before calling.
     */
    public void writeFieldFiltered(JSONGenerator generator, Object bean, long features,
                                   com.alibaba.fastjson3.filter.PropertyPreFilter[] propertyPreFilters,
                                   PropertyFilter[] propertyFilters,
                                   ValueFilter[] valueFilters,
                                   NameFilter[] nameFilters) {
        // Merge field-level features (same as writeFieldInternal)
        if (fieldFeatures != 0) {
            features |= fieldFeatures;
        }

        // PropertyPreFilter: check before reading value (more efficient)
        if (propertyPreFilters != null) {
            for (var ppf : propertyPreFilters) {
                if (!ppf.apply(bean, fieldName)) {
                    return;
                }
            }
        }

        Object value;
        try {
            value = getObjectValue(bean);
        } catch (JSONException e) {
            if (generator.ignoreErrorGetter) {
                return;
            }
            throw e;
        }

        // PropertyFilter: check if field should be included
        if (propertyFilters != null) {
            for (PropertyFilter pf : propertyFilters) {
                if (!pf.apply(bean, fieldName, value)) {
                    return;
                }
            }
        }

        // ValueFilter: transform value
        if (valueFilters != null) {
            for (ValueFilter vf : valueFilters) {
                value = vf.apply(bean, fieldName, value);
            }
        }

        // NameFilter: transform name
        String name = fieldName;
        if (nameFilters != null) {
            for (NameFilter nf : nameFilters) {
                name = nf.apply(bean, name, value);
            }
        }

        // Write name + value (use generic path since name/value may be transformed)
        if (value == null) {
            if (!unwrapped) {
                // Inline writeNull logic using the NameFilter-transformed name
                long mergedFeatures = features | generator.getFeatures();
                if ((mergedFeatures & WriteFeature.WriteNulls.mask) != 0
                        || inclusion == Inclusion.ALWAYS) {
                    generator.writeName(name);
                    generator.writeNull();
                } else if (typeTag == TYPE_STRING
                        && (mergedFeatures & WriteFeature.WriteNullStringAsEmpty.mask) != 0) {
                    generator.writeName(name);
                    generator.writeString("");
                } else if ((typeTag == TYPE_LIST_STRING || typeTag == TYPE_LIST_OBJECT
                        || java.util.Collection.class.isAssignableFrom(fieldClass))
                        && (mergedFeatures & WriteFeature.WriteNullListAsEmpty.mask) != 0) {
                    generator.writeName(name);
                    generator.startArray();
                    generator.endArray();
                } else if ((typeTag == TYPE_INT || typeTag == TYPE_LONG || typeTag == TYPE_DOUBLE || typeTag == TYPE_FLOAT
                        || Number.class.isAssignableFrom(fieldClass))
                        && (mergedFeatures & WriteFeature.WriteNullNumberAsZero.mask) != 0) {
                    generator.writeName(name);
                    if (typeTag == TYPE_LONG) {
                        generator.writeInt64(0L);
                    } else if (typeTag == TYPE_DOUBLE) {
                        generator.writeDouble(0D);
                    } else if (typeTag == TYPE_FLOAT) {
                        generator.writeFloat(0F);
                    } else {
                        generator.writeInt32(0);
                    }
                } else if ((typeTag == TYPE_BOOL || fieldClass == Boolean.class)
                        && (mergedFeatures & WriteFeature.WriteNullBooleanAsFalse.mask) != 0) {
                    generator.writeName(name);
                    generator.writeBool(false);
                }
            }
            return;
        }
        // Unwrapped: write nested fields directly (no name, no braces)
        if (unwrapped) {
            // Depth guard catches circular unwrap chains (A.b @unwrapped B,
            // B.a @unwrapped A) that would otherwise recurse until the JVM
            // stack overflows. The reader-side UNWRAP_VISITED mechanism only
            // detects cycles at field-list expansion; on the writer, the cycle
            // only manifests with actual object graphs, so we rely on the
            // generator's existing depth counter to bound recursion to
            // MAX_WRITE_DEPTH and surface a clean JSONException.
            generator.incrementDepth();
            try {
                FieldWriter[] innerFieldWriters = resolveUnwrapFieldWriters(generator, value.getClass());
                ObjectWriterCreator.writeFields(generator, innerFieldWriters, value, features);
            } finally {
                generator.decrementDepth();
            }
            return;
        }
        generator.writeName(name);
        generator.writeAny(value);
    }

    // ==================== Custom writer ====================

    private void writeFieldCustom(JSONGenerator generator, Object bean, long features) {
        Object value = getObjectValue(bean);
        if (value == null) {
            writeNull(generator, features);
            return;
        }
        if (inclusion == Inclusion.NON_EMPTY && isEmpty(value)) {
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        customWriter.write(generator, value, fieldName, fieldType, features);
    }

    // ==================== Comparable ====================

    @Override
    public int compareTo(FieldWriter other) {
        int cmp = Integer.compare(this.ordinal, other.ordinal);
        if (cmp != 0) {
            return cmp;
        }
        return this.fieldName.compareTo(other.fieldName);
    }

    @Override
    public String toString() {
        return "FieldWriter{name='" + fieldName + "', ordinal=" + ordinal
                + ", type=" + fieldClass.getSimpleName() + "}";
    }
}

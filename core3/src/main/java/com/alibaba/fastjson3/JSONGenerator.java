package com.alibaba.fastjson3;

import com.alibaba.fastjson3.filter.NameFilter;
import com.alibaba.fastjson3.filter.PropertyFilter;
import com.alibaba.fastjson3.filter.ValueFilter;
import com.alibaba.fastjson3.util.BufferPool;
import com.alibaba.fastjson3.util.JDKUtils;

import java.io.Closeable;
import java.io.Flushable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;

/**
 * High-performance JSON generator.
 * Uses pre-computed digit lookup tables and bulk memory operations.
 *
 * <pre>
 * try (JSONGenerator generator = JSONGenerator.ofUTF8()) {
 *     generator.startObject();
 *     generator.writeName("key");
 *     generator.writeString("value");
 *     generator.endObject();
 *     String json = generator.toString();
 * }
 * </pre>
 */
public abstract sealed class JSONGenerator implements Closeable, Flushable
        permits JSONGenerator.Char, JSONGenerator.UTF8 {
    // Pre-computed lookup tables for fast number writing (wast-style optimization)
    static final int[] TWO_DIGITS_32 = new int[100];
    static final long[] FOUR_DIGITS_64 = new long[10000];
    static final char[] DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f'
    };
    // Byte-oriented 2-digit lookup for UTF-8 int writing
    static final byte[] DIGIT_TENS = new byte[100];
    static final byte[] DIGIT_ONES = new byte[100];
    // Packed 2-digit lookup: short[i] = tens_byte | (ones_byte << 8) for Unsafe.putShort
    static final short[] PACKED_DIGITS = new short[100];
    // Pre-computed escape table
    static final char[] ESCAPE_CHARS = new char[128];
    static final char[] ESCAPE_CHARS_BROWSER = new char[128];
    static final char[] ESCAPE_CHARS_SECURE = new char[128];

    static {
        // Initialize 2-digit lookup table (char-based, for Char)
        for (int i = 0; i < 100; i++) {
            int d0 = i / 10 + '0';
            int d1 = i % 10 + '0';
            TWO_DIGITS_32[i] = (d0 << 16) | d1;
            DIGIT_TENS[i] = (byte) d0;
            DIGIT_ONES[i] = (byte) d1;
            // Little-endian: low byte = tens, high byte = ones
            PACKED_DIGITS[i] = (short) (d0 | (d1 << 8));
        }

        // Initialize 4-digit lookup table
        for (int i = 0; i < 10000; i++) {
            int d0 = i / 1000 + '0';
            int d1 = (i / 100) % 10 + '0';
            int d2 = (i / 10) % 10 + '0';
            int d3 = i % 10 + '0';
            FOUR_DIGITS_64[i] = ((long) d0 << 48) | ((long) d1 << 32) | ((long) d2 << 16) | d3;
        }

        // Initialize escape table
        ESCAPE_CHARS['"'] = '"';
        ESCAPE_CHARS['\\'] = '\\';
        ESCAPE_CHARS['/'] = '/';
        ESCAPE_CHARS['\b'] = 'b';
        ESCAPE_CHARS['\f'] = 'f';
        ESCAPE_CHARS['\n'] = 'n';
        ESCAPE_CHARS['\r'] = 'r';
        ESCAPE_CHARS['\t'] = 't';

        // Browser-compatible: standard + < > ( )
        System.arraycopy(ESCAPE_CHARS, 0, ESCAPE_CHARS_BROWSER, 0, 128);
        ESCAPE_CHARS_BROWSER['<'] = '\1'; // marker for \u003C
        ESCAPE_CHARS_BROWSER['>'] = '\2'; // marker for \u003E
        ESCAPE_CHARS_BROWSER['('] = '\3'; // marker for \u0028
        ESCAPE_CHARS_BROWSER[')'] = '\4'; // marker for \u0029

        // Secure: browser + & '
        System.arraycopy(ESCAPE_CHARS_BROWSER, 0, ESCAPE_CHARS_SECURE, 0, 128);
        ESCAPE_CHARS_SECURE['&'] = '\5'; // marker for \u0026
        ESCAPE_CHARS_SECURE['\''] = '\6'; // marker for \u0027
    }

    static final int MAX_WRITE_DEPTH = 512;

    // Pre-computed indent tables for pretty format (2 and 4 spaces per level)
    static final int MAX_INDENT_LEVEL = 32;
    static final byte[][] INDENT_BYTES_2 = new byte[MAX_INDENT_LEVEL][];
    static final char[][] INDENT_CHARS_2 = new char[MAX_INDENT_LEVEL][];
    static final byte[][] INDENT_BYTES_4 = new byte[MAX_INDENT_LEVEL][];
    static final char[][] INDENT_CHARS_4 = new char[MAX_INDENT_LEVEL][];
    // Aliases for default (2-space) tables
    static final byte[][] INDENT_BYTES_TABLE = INDENT_BYTES_2;
    static final char[][] INDENT_CHARS_TABLE = INDENT_CHARS_2;

    static {
        for (int i = 0; i < MAX_INDENT_LEVEL; i++) {
            // 2-space indent
            int len2 = 1 + 2 * i;
            byte[] b2 = new byte[len2];
            char[] c2 = new char[len2];
            b2[0] = '\n';
            c2[0] = '\n';
            for (int j = 1; j < len2; j++) {
                b2[j] = ' ';
                c2[j] = ' ';
            }
            INDENT_BYTES_2[i] = b2;
            INDENT_CHARS_2[i] = c2;

            // 4-space indent
            int len4 = 1 + 4 * i;
            byte[] b4 = new byte[len4];
            char[] c4 = new char[len4];
            b4[0] = '\n';
            c4[0] = '\n';
            for (int j = 1; j < len4; j++) {
                b4[j] = ' ';
                c4[j] = ' ';
            }
            INDENT_BYTES_4[i] = b4;
            INDENT_CHARS_4[i] = c4;
        }
    }

    protected final long features;
    protected int count;
    protected int writeDepth;
    protected final boolean pretty;
    public boolean isPretty() { return pretty; }

    /**
     * Owning {@link ObjectMapper} — null when the generator was created outside a
     * mapper context (e.g. direct construction by test code). Set by the mapper's
     * {@code writeValueAsString}/{@code writeValueAsBytes} entries so nested writer
     * lookups honour mapper-specific mix-ins and registered writers instead of
     * falling through to {@link ObjectMapper#shared()}.
     */
    public ObjectMapper owner;

    /**
     * Return the mapper whose configuration should drive nested writer lookups
     * during this serialisation. Defaults to {@link ObjectMapper#shared()} when
     * no owner was attached.
     */
    public ObjectMapper effectiveMapper() {
        return owner != null ? owner : ObjectMapper.shared();
    }
    protected final boolean bigDecimalPlain;
    protected final boolean longAsString;
    protected final boolean nonStringAsString;
    protected final boolean boolAsNumber;
    protected final boolean enumOrdinal;
    protected final boolean escapeNoneAscii;
    protected final boolean sortMapKeys;
    public final boolean notWriteDefaultValue;
    public final boolean notWriteEmptyArray;
    protected final char[] escapeChars; // instance escape table
    protected final boolean extendedEscape; // browser or secure escaping active
    protected final boolean floatSpecialAsString; // NaN/Infinity → "NaN"/"Infinity"
    public final boolean ignoreErrorGetter; // suppress getter exceptions
    public final boolean writeClassName; // write @type for polymorphic types
    public final boolean notWriteRootClassName; // skip @type at depth 0
    public final boolean notWriteHashMapArrayListClassName; // skip @type for common collections
    public final boolean dateAsMillis; // Date → milliseconds timestamp
    public final boolean writeMapNullValue; // write null values in Map entries
    public final boolean notWriteSetClassName; // skip @type for Set types
    public final boolean notWriteNumberClassName; // skip @type for Number types
    public final boolean writeThrowableClassName; // write @type for Throwable
    public final boolean unquoteFieldName; // field names without quotes
    protected final byte quoteChar; // '"' or '\'' — pre-computed at construction
    protected final int indentStep; // 2 or 4 — pre-computed at construction
    // True when any feature requires bypassing the static UTF8 fast path
    public final boolean bypassStaticPath;
    protected int indentLevel;

    // Filters — null when no filters configured (zero overhead)
    public com.alibaba.fastjson3.filter.BeforeFilter[] beforeFilters;
    public com.alibaba.fastjson3.filter.AfterFilter[] afterFilters;
    public com.alibaba.fastjson3.filter.PropertyPreFilter[] propertyPreFilters;
    public PropertyFilter[] propertyFilters;
    public ValueFilter[] valueFilters;
    public NameFilter[] nameFilters;
    public com.alibaba.fastjson3.filter.LabelFilter labelFilter;

    // Circular reference detection — null when disabled (zero overhead)
    private java.util.IdentityHashMap<Object, Object> references;
    private boolean referenceDetection;

    protected JSONGenerator(long features) {
        // Expand NullAsDefaultValue into individual null-handling flags
        if ((features & WriteFeature.NullAsDefaultValue.mask) != 0) {
            features |= WriteFeature.WriteNullStringAsEmpty.mask
                    | WriteFeature.WriteNullListAsEmpty.mask
                    | WriteFeature.WriteNullNumberAsZero.mask
                    | WriteFeature.WriteNullBooleanAsFalse.mask;
        }
        // PrettyFormatWith2Space / PrettyFormatWith4Space imply PrettyFormat
        if ((features & (WriteFeature.PrettyFormatWith2Space.mask | WriteFeature.PrettyFormatWith4Space.mask)) != 0) {
            features |= WriteFeature.PrettyFormat.mask;
        }
        this.features = features;
        this.pretty = (features & WriteFeature.PrettyFormat.mask) != 0;
        this.bigDecimalPlain = (features & WriteFeature.WriteBigDecimalAsPlain.mask) != 0;
        this.longAsString = (features & WriteFeature.WriteLongAsString.mask) != 0;
        this.nonStringAsString = (features & WriteFeature.WriteNonStringValueAsString.mask) != 0;
        this.boolAsNumber = (features & WriteFeature.WriteBooleanAsNumber.mask) != 0;
        this.enumOrdinal = (features & WriteFeature.WriteEnumUsingOrdinal.mask) != 0;
        this.escapeNoneAscii = (features & WriteFeature.EscapeNoneAscii.mask) != 0;
        this.sortMapKeys = (features & WriteFeature.SortMapEntriesByKeys.mask) != 0;
        this.notWriteDefaultValue = (features & WriteFeature.NotWriteDefaultValue.mask) != 0;
        this.notWriteEmptyArray = (features & WriteFeature.NotWriteEmptyArray.mask) != 0;

        boolean browserSecure = (features & WriteFeature.BrowserSecure.mask) != 0;
        boolean browserCompatible = (features & WriteFeature.BrowserCompatible.mask) != 0;
        this.escapeChars = browserSecure ? ESCAPE_CHARS_SECURE
                : browserCompatible ? ESCAPE_CHARS_BROWSER
                : ESCAPE_CHARS;
        this.extendedEscape = (escapeChars != ESCAPE_CHARS);
        this.floatSpecialAsString = (features & WriteFeature.WriteFloatSpecialAsString.mask) != 0;
        this.ignoreErrorGetter = (features & WriteFeature.IgnoreErrorGetter.mask) != 0;
        this.writeClassName = (features & WriteFeature.WriteClassName.mask) != 0;
        this.notWriteRootClassName = (features & WriteFeature.NotWriteRootClassName.mask) != 0;
        this.notWriteHashMapArrayListClassName = (features & WriteFeature.NotWriteHashMapArrayListClassName.mask) != 0;
        this.dateAsMillis = (features & WriteFeature.WriteDateAsMillis.mask) != 0;
        this.writeMapNullValue = (features & WriteFeature.WriteMapNullValue.mask) != 0;
        this.notWriteSetClassName = (features & WriteFeature.NotWriteSetClassName.mask) != 0;
        this.notWriteNumberClassName = (features & WriteFeature.NotWriteNumberClassName.mask) != 0;
        this.writeThrowableClassName = (features & WriteFeature.WriteThrowableClassName.mask) != 0;
        this.unquoteFieldName = (features & WriteFeature.UnquoteFieldName.mask) != 0;
        this.quoteChar = (features & WriteFeature.UseSingleQuotes.mask) != 0 ? (byte) '\'' : (byte) '"';
        this.indentStep = (features & WriteFeature.PrettyFormatWith4Space.mask) != 0 ? 4 : 2;
        boolean writeNulls = (features & WriteFeature.WriteNulls.mask) != 0
                || (features & WriteFeature.WriteNullStringAsEmpty.mask) != 0
                || (features & WriteFeature.WriteNullListAsEmpty.mask) != 0
                || (features & WriteFeature.WriteNullNumberAsZero.mask) != 0
                || (features & WriteFeature.WriteNullBooleanAsFalse.mask) != 0;
        this.bypassStaticPath = longAsString || nonStringAsString || boolAsNumber
                || notWriteDefaultValue || notWriteEmptyArray || extendedEscape || escapeNoneAscii
                || writeNulls || unquoteFieldName || quoteChar != '"';

        if ((features & WriteFeature.ReferenceDetection.mask) != 0) {
            this.referenceDetection = true;
            this.references = new java.util.IdentityHashMap<>();
        }
    }

    /**
     * Enable circular reference detection. When enabled, serializing an object
     * that has already been seen in the current object graph throws JSONException.
     */
    public void setReferenceDetection(boolean enabled) {
        this.referenceDetection = enabled;
        if (enabled && references == null) {
            references = new java.util.IdentityHashMap<>();
        }
    }

    /**
     * Check and register an object for circular reference detection.
     * Throws JSONException if a circular reference is detected.
     * No-op when reference detection is disabled.
     */
    public void pushReference(Object obj) {
        if (!referenceDetection || obj == null) {
            return;
        }
        if (references.put(obj, Boolean.TRUE) != null) {
            throw new JSONException("circular reference detected: " + obj.getClass().getName());
        }
    }

    /**
     * Remove an object from the reference tracking set after serialization completes.
     */
    public void popReference(Object obj) {
        if (referenceDetection && obj != null) {
            references.remove(obj);
        }
    }

    /**
     * Increment write depth and check against MAX_WRITE_DEPTH.
     * Call before writing a nested container; call {@link #decrementDepth()} after.
     */
    public void incrementDepth() {
        if (++writeDepth > MAX_WRITE_DEPTH) {
            throw new JSONException("serialization depth " + writeDepth + " exceeds maximum " + MAX_WRITE_DEPTH);
        }
    }

    /**
     * Get current write depth (0 = root level).
     */
    public int getWriteDepth() {
        return writeDepth;
    }

    /**
     * Decrement write depth after writing a nested container.
     */
    public void decrementDepth() {
        if (writeDepth > 0) {
            writeDepth--;
        }
    }

    /**
     * Called before each element in an array for pretty-format indentation.
     * No-op when PrettyFormat is not enabled (zero overhead).
     */
    public void beforeArrayValue() {
        // overridden by subclasses
    }

    public void setFilters(PropertyFilter[] pf, ValueFilter[] vf, NameFilter[] nf) {
        this.propertyFilters = pf;
        this.valueFilters = vf;
        this.nameFilters = nf;
    }

    /**
     * Check if per-field filters are configured (Property/Value/Name/PreFilter).
     * Before/After filters are handled at the object level, not per-field.
     */
    public boolean hasFilters() {
        return propertyFilters != null || propertyPreFilters != null
                || valueFilters != null || nameFilters != null;
    }

    /**
     * Public ensureCapacity for ASM-generated code to call once per object.
     * Subclasses that support capacity management should override.
     */
    public void ensureCapacityPublic(int needed) {
        // default no-op; overridden by UTF8
    }

    /**
     * Create a UTF-16 writer (char-based, optimal for String output).
     */
    public static JSONGenerator of() {
        return new Char(0);
    }

    /**
     * Get the feature bitmask (includes NullAsDefaultValue expansion).
     */
    public long getFeatures() {
        return features;
    }

    /**
     * Create a writer with features.
     */
    public static JSONGenerator of(WriteFeature... features) {
        return new Char(WriteFeature.of(features));
    }

    /**
     * Create a writer with feature mask (avoid array allocation).
     * @param features the feature mask
     * @return a new Char writer
     */
    public static JSONGenerator of(long features) {
        return new Char(features);
    }

    /**
     * Create a UTF-8 writer (byte-based, optimal for OutputStream/byte[] output).
     */
    public static JSONGenerator ofUTF8() {
        return new UTF8(0);
    }

    /**
     * Create a UTF-8 writer with features.
     */
    public static JSONGenerator ofUTF8(WriteFeature... features) {
        return new UTF8(WriteFeature.of(features));
    }

    /**
     * Create a UTF-8 writer with feature mask (avoid array allocation).
     * @param features the feature mask
     * @return a new UTF8 writer
     */
    public static JSONGenerator ofUTF8(long features) {
        return new UTF8(features);
    }

    /**
     * Check if feature is enabled.
     */
    public boolean isEnabled(WriteFeature feature) {
        return (features & feature.mask) != 0;
    }

    // ---- Structure tokens ----

    public abstract void startObject();
    public abstract void endObject();
    public abstract void startArray();
    public abstract void endArray();

    // ---- Field name ----

    public abstract void writeName(String name);

    /**
     * Write a pre-encoded field name token ("name":) using bulk copy.
     * This is faster than writeName() as the encoding is pre-computed.
     *
     * @param nameChars pre-encoded as char[] for char-based generators
     * @param nameBytes pre-encoded as byte[] (UTF-8) for byte-based generators
     */
    public abstract void writePreEncodedName(char[] nameChars, byte[] nameBytes);

    /**
     * Write a pre-encoded field name using long[] bulk writes (8 bytes at a time via Unsafe).
     * Default delegates to writePreEncodedName; UTF8 subclass overrides.
     */
    public void writePreEncodedNameLongs(long[] nameByteLongs, int nameBytesLen, char[] nameChars, byte[] nameBytes) {
        writePreEncodedName(nameChars, nameBytes);
    }

    // ---- Fused name+value writers (single ensureCapacity) ----

    /**
     * Write pre-encoded field name + string value in one fused operation.
     * Default delegates to separate calls; UTF8 subclass overrides for performance.
     */
    public void writeNameString(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, String value) {
        writePreEncodedName(nameChars, nameBytes);
        writeString(value);
    }

    /**
     * Write pre-encoded field name + int value in one fused operation.
     */
    public void writeNameInt32(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, int value) {
        writePreEncodedName(nameChars, nameBytes);
        writeInt32(value);
    }

    public void writeNameDouble(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, double value) {
        writePreEncodedName(nameChars, nameBytes);
        writeDouble(value);
    }

    public void writeNameInt64(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, long value) {
        writePreEncodedName(nameChars, nameBytes);
        writeInt64(value);
    }

    public void writeNameBool(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, boolean value) {
        writePreEncodedName(nameChars, nameBytes);
        writeBool(value);
    }

    // ---- Compact (non-pretty) variants — called by ASM-generated code. No pretty checks. ----
    // Default implementations delegate to the pretty-aware versions.
    // UTF8 subclass overrides with implementations that skip the pretty check.

    public void writePreEncodedNameLongsCompact(long[] nameByteLongs, int nameBytesLen, char[] nameChars, byte[] nameBytes) {
        writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
    }

    public void writeNameStringCompact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, String value) {
        writeNameString(nameByteLongs, nameBytesLen, nameBytes, nameChars, value);
    }

    public void writeNameInt32Compact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, int value) {
        writeNameInt32(nameByteLongs, nameBytesLen, nameBytes, nameChars, value);
    }

    public void writeNameInt64Compact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, long value) {
        writeNameInt64(nameByteLongs, nameBytesLen, nameBytes, nameChars, value);
    }

    public void writeNameDoubleCompact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, double value) {
        writeNameDouble(nameByteLongs, nameBytesLen, nameBytes, nameChars, value);
    }

    public void writeNameBoolCompact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, boolean value) {
        writeNameBool(nameByteLongs, nameBytesLen, nameBytes, nameChars, value);
    }

    // ---- Per-length raw name writers (Phase W#5 — ported from fj2 JSONWriterUTF8.writeNameXRaw) ----
    //
    // These take the pre-packed UTF-8 name bytes `"name":` as 1 or 2 longs and
    // write them directly via Unsafe.putLong. No length switch inside the
    // method — the caller (ASM-generated code) picks the right overload based
    // on the encoded name byte length. Each is tiny (~20 bytes bytecode) so
    // the JIT reliably inlines them into the generated writer.
    //
    // Caller contract: the top-level write() has already called
    // ensureCapacityPublic for the full object, so writeName1L / writeName2L
    // skip the per-call capacity check. If the name bytes are <= 5, the
    // full `"name":` token fits in the first long; 6-13 char bean fields
    // need two longs. The trailing comma is written by the subsequent
    // writeInt32 / writeString / etc. call, matching fj3's existing
    // trailing-comma model (endObject strips the tail comma).

    public void writeName1L(long nameLong0, int nameBytesLen) {
        // Default: reconstruct {nameBytes, nameChars} from the long in
        // native byte order (matching ObjectWriterCreatorASM's use of
        // JDKUtils.getLongDirect when packing) and delegate to
        // writePreEncodedName. Only hit by non-UTF8 subclasses (Char);
        // the UTF8 subclass overrides with the fast putLongDirect path.
        // ASM-generated classes only use writeName1L/2L for names whose
        // pre-encoded `"jsonName":` UTF-8 bytes are pure ASCII (otherwise
        // emitNameWrite returns false and the legacy fallback is taken),
        // so sign-extending each byte to a char yields the correct
        // JSON-identifier chars here.
        byte[] raw = new byte[8];
        JDKUtils.putLongDirect(raw, 0, nameLong0);
        byte[] b = new byte[nameBytesLen];
        char[] c = new char[nameBytesLen];
        for (int i = 0; i < nameBytesLen; i++) {
            b[i] = raw[i];
            c[i] = (char) (raw[i] & 0xFF);
        }
        writePreEncodedName(c, b);
    }

    public void writeName2L(long nameLong0, long nameLong1, int nameBytesLen) {
        byte[] raw = new byte[16];
        JDKUtils.putLongDirect(raw, 0, nameLong0);
        JDKUtils.putLongDirect(raw, 8, nameLong1);
        byte[] b = new byte[nameBytesLen];
        char[] c = new char[nameBytesLen];
        for (int i = 0; i < nameBytesLen; i++) {
            b[i] = raw[i];
            c[i] = (char) (raw[i] & 0xFF);
        }
        writePreEncodedName(c, b);
    }

    // ---- Array writers (avoid writeAny dispatch per element) ----

    public void writeLongArray(long[] arr) {
        startArray();
        for (int i = 0, len = arr.length; i < len; i++) {
            beforeArrayValue();
            writeInt64(arr[i]);
        }
        endArray();
    }

    public void writeIntArray(int[] arr) {
        startArray();
        for (int i = 0, len = arr.length; i < len; i++) {
            beforeArrayValue();
            writeInt32(arr[i]);
        }
        endArray();
    }

    public void writeDoubleArray(double[] arr) {
        startArray();
        for (int i = 0, len = arr.length; i < len; i++) {
            beforeArrayValue();
            writeDouble(arr[i]);
        }
        endArray();
    }

    public void writeStringArray(String[] arr) {
        startArray();
        for (int i = 0, len = arr.length; i < len; i++) {
            beforeArrayValue();
            String s = arr[i];
            if (s == null) {
                writeNull();
            } else {
                writeString(s);
            }
        }
        endArray();
    }

    // ---- Value writers ----

    public abstract void writeNull();
    public abstract void writeTrue();
    public abstract void writeFalse();
    public abstract void writeBool(boolean val);
    public abstract void writeInt32(int val);
    public abstract void writeInt64(long val);
    public abstract void writeFloat(float val);
    public abstract void writeDouble(double val);
    public abstract void writeDecimal(BigDecimal val);
    public abstract void writeString(String val);
    public abstract void writeRaw(String raw);

    /**
     * Write a pre-encoded byte[] blob directly into the output buffer.
     * Used by ASM-generated writers to emit precomputed enum field tokens
     * (the form {@code "fieldName":"ENUM_VALUE",}) in a single memcpy.
     *
     * <p>The generator's trailing-comma model requires the caller-supplied
     * bytes to already include the {@code ,} separator when appropriate —
     * {@code endObject} strips the tail comma.</p>
     */
    public abstract void writeRawBytes(byte[] bytes);

    /**
     * Write a LocalDate value as a JSON string in yyyy-MM-dd format.
     * Subclasses may override for zero-alloc direct writing.
     */
    public void writeLocalDate(LocalDate val) {
        writeString(val.toString());
    }

    /**
     * Write a LocalDateTime value as a JSON string in yyyy-MM-ddTHH:mm:ss format.
     */
    public void writeLocalDateTime(LocalDateTime val) {
        writeString(val.toString());
    }

    /**
     * Write a LocalTime value as a JSON string in HH:mm:ss format.
     */
    public void writeLocalTime(LocalTime val) {
        writeString(val.toString());
    }

    /**
     * Write an Instant value as a JSON string in yyyy-MM-ddTHH:mm:ssZ format.
     */
    public void writeInstant(Instant val) {
        writeString(val.toString());
    }

    // ---- Name-Value convenience ----

    /**
     * Write name:value pair for string value.
     */
    public void writeNameValue(String name, String value) {
        writeName(name);
        writeString(value);
    }

    /**
     * Write name:value pair for int value.
     */
    public void writeNameValue(String name, int value) {
        writeName(name);
        writeInt32(value);
    }

    /**
     * Write name:value pair for long value.
     */
    public void writeNameValue(String name, long value) {
        writeName(name);
        writeInt64(value);
    }

    /**
     * Write name:value pair for boolean value.
     */
    public void writeNameValue(String name, boolean value) {
        writeName(name);
        writeBool(value);
    }

    // ---- Object serialization ----

    /**
     * Write any Java object as JSON.
     */
    public void writeAny(Object value) {
        if (value == null) {
            writeNull();
            return;
        }
        if (value instanceof String) {
            writeString((String) value);
        } else if (value instanceof Integer) {
            writeInt32((Integer) value);
        } else if (value instanceof Long) {
            writeInt64((Long) value);
        } else if (value instanceof Boolean) {
            writeBool((Boolean) value);
        } else if (value instanceof Double) {
            writeDouble((Double) value);
        } else if (value instanceof Float) {
            writeFloat((Float) value);
        } else if (value instanceof BigDecimal) {
            writeDecimal((BigDecimal) value);
        } else if (value instanceof BigInteger bi) {
            writeDecimal(new BigDecimal(bi));
        } else if (value instanceof Short) {
            writeInt32((Short) value);
        } else if (value instanceof Byte) {
            writeInt32((Byte) value);
        } else if (value instanceof AtomicInteger ai) {
            writeInt32(ai.intValue());
        } else if (value instanceof AtomicLong al) {
            writeInt64(al.longValue());
        } else if (value instanceof AtomicBoolean ab) {
            writeBool(ab.get());
        } else if (value instanceof AtomicReference<?> ar) {
            writeAny(ar.get());
        } else if (value instanceof AtomicIntegerArray aia) {
            startArray();
            for (int i = 0, len = aia.length(); i < len; i++) {
                beforeArrayValue();
                writeInt32(aia.get(i));
            }
            endArray();
        } else if (value instanceof AtomicLongArray ala) {
            startArray();
            for (int i = 0, len = ala.length(); i < len; i++) {
                beforeArrayValue();
                writeInt64(ala.get(i));
            }
            endArray();
        } else if (value instanceof long[] la) {
            writeLongArray(la);
        } else if (value instanceof int[] ia) {
            writeIntArray(ia);
        } else if (value instanceof double[] da) {
            writeDoubleArray(da);
        } else if (value instanceof boolean[] ba) {
            startArray();
            for (int i = 0, len = ba.length; i < len; i++) {
                beforeArrayValue();
                writeBool(ba[i]);
            }
            endArray();
        } else if (value instanceof float[] fa) {
            startArray();
            for (int i = 0, len = fa.length; i < len; i++) {
                beforeArrayValue();
                writeFloat(fa[i]);
            }
            endArray();
        } else if (value instanceof short[] sa) {
            startArray();
            for (int i = 0, len = sa.length; i < len; i++) {
                beforeArrayValue();
                writeInt32(sa[i]);
            }
            endArray();
        } else if (value instanceof byte[] ba) {
            // byte[] as JSON array of ints
            startArray();
            for (int i = 0, len = ba.length; i < len; i++) {
                beforeArrayValue();
                writeInt32(ba[i]);
            }
            endArray();
        } else if (value instanceof JSONObject) {
            writeJSONObject((JSONObject) value);
        } else if (value instanceof JSONArray) {
            writeJSONArray((JSONArray) value);
        } else if (value instanceof Map.Entry<?, ?> entry
                && (features & WriteFeature.WritePairAsJavaBean.mask) != 0) {
            if (++writeDepth > MAX_WRITE_DEPTH) {
                throw new JSONException("serialization depth " + writeDepth + " exceeds maximum " + MAX_WRITE_DEPTH);
            }
            try {
                startObject();
                writeName("key");
                writeAny(entry.getKey());
                writeName("value");
                writeAny(entry.getValue());
                endObject();
            } finally {
                writeDepth--;
            }
        } else if (value instanceof Map<?, ?> map) {
            if (++writeDepth > MAX_WRITE_DEPTH) {
                throw new JSONException("serialization depth " + writeDepth + " exceeds maximum " + MAX_WRITE_DEPTH);
            }
            try {
                pushReference(map);
                startObject();
                Iterable<? extends Map.Entry<?, ?>> entries;
                if (sortMapKeys) {
                    if (map instanceof java.util.SortedMap) {
                        entries = map.entrySet();
                    } else {
                        java.util.List<? extends Map.Entry<?, ?>> list = new java.util.ArrayList<>(map.entrySet());
                        list.sort(java.util.Comparator.comparing(e -> String.valueOf(e.getKey())));
                        entries = list;
                    }
                } else {
                    entries = map.entrySet();
                }
                for (Map.Entry<?, ?> entry : entries) {
                    writeName(String.valueOf(entry.getKey()));
                    writeAny(entry.getValue());
                }
                endObject();
            } finally {
                popReference(map);
                writeDepth--;
            }
        } else if (value instanceof Collection<?> coll) {
            if (++writeDepth > MAX_WRITE_DEPTH) {
                throw new JSONException("serialization depth " + writeDepth + " exceeds maximum " + MAX_WRITE_DEPTH);
            }
            try {
                pushReference(coll);
                startArray();
                for (Object item : coll) {
                    beforeArrayValue();
                    writeAny(item);
                }
                endArray();
            } finally {
                popReference(coll);
                writeDepth--;
            }
        } else if (value instanceof Object[] arr) {
            if (++writeDepth > MAX_WRITE_DEPTH) {
                throw new JSONException("serialization depth " + writeDepth + " exceeds maximum " + MAX_WRITE_DEPTH);
            }
            try {
                pushReference(arr);
                startArray();
                for (Object item : arr) {
                    beforeArrayValue();
                    writeAny(item);
                }
                endArray();
            } finally {
                popReference(arr);
                writeDepth--;
            }
        } else if (value instanceof Enum<?> e) {
            if (enumOrdinal) {
                writeInt32(e.ordinal());
            } else {
                writeString(e.name());
            }
        } else if (value instanceof LocalDateTime ldt) {
            writeLocalDateTime(ldt);
        } else if (value instanceof LocalDate ld) {
            writeLocalDate(ld);
        } else if (value instanceof LocalTime lt) {
            writeLocalTime(lt);
        } else if (value instanceof Instant inst) {
            writeInstant(inst);
        } else if (value instanceof ZonedDateTime zdt) {
            writeString(zdt.toString());
        } else if (value instanceof OffsetDateTime odt) {
            writeString(odt.toString());
        } else if (value instanceof Date date) {
            if (dateAsMillis) {
                writeInt64(date.getTime());
            } else {
                writeInstant(date.toInstant());
            }
        } else {
            // Try ObjectWriter-based serialization via the owning mapper so mix-in /
            // registerWriter configurations attached to a custom mapper drive nested
            // polymorphic discriminator emission.
            @SuppressWarnings("unchecked")
            ObjectWriter<Object> objectWriter = (ObjectWriter<Object>) effectiveMapper().getObjectWriter(value.getClass());
            if (objectWriter != null) {
                objectWriter.write(this, value, null, null, features);
            } else {
                writeString(value.toString());
            }
        }
    }

    private void writeJSONObject(JSONObject obj) {
        if (++writeDepth > MAX_WRITE_DEPTH) {
            throw new JSONException("serialization depth " + writeDepth + " exceeds maximum " + MAX_WRITE_DEPTH);
        }
        try {
            pushReference(obj);
            startObject();
            Iterable<Map.Entry<String, Object>> entries;
            if (sortMapKeys) {
                java.util.List<Map.Entry<String, Object>> list = new java.util.ArrayList<>(obj.entrySet());
                list.sort(java.util.Comparator.comparing(e -> String.valueOf(e.getKey())));
                entries = list;
            } else {
                entries = obj.entrySet();
            }
            for (Map.Entry<String, Object> entry : entries) {
                writeName(entry.getKey());
                writeAny(entry.getValue());
            }
            endObject();
        } finally {
            popReference(obj);
            writeDepth--;
        }
    }

    private void writeJSONArray(JSONArray arr) {
        if (++writeDepth > MAX_WRITE_DEPTH) {
            throw new JSONException("serialization depth " + writeDepth + " exceeds maximum " + MAX_WRITE_DEPTH);
        }
        try {
            pushReference(arr);
            startArray();
            for (int i = 0, size = arr.size(); i < size; i++) {
                beforeArrayValue();
                writeAny(arr.get(i));
            }
            endArray();
        } finally {
            popReference(arr);
            writeDepth--;
        }
    }

    /**
     * Write all fields of an object using static methods (JSONB.IO pattern).
     * Entire loop uses local pos -- only reads gen.count once at entry, writes once at exit.
     * Zero virtual dispatch for primitive and Latin-1 string fields.
     */
    public static void writeObjectStaticUTF8(UTF8 gen, com.alibaba.fastjson3.writer.FieldWriter[] writers, Object bean, long features) {
        gen.ensureCapacity(writers.length * 48);
        byte[] buf = gen.buf;
        int pos = gen.count;
        // Use pos-passing helper for the actual work (enables recursive calls for nested objects)
        pos = writeFieldsStaticUTF8(gen, buf, pos, writers, bean, features);
        gen.count = pos;
    }

    /**
     * Write object fields: tiny loop that calls writeOneFieldStatic per field.
     * Keeping this method small (~15 lines) ensures JIT can fully inline it.
     */
    private static int writeFieldsStaticUTF8(UTF8 gen, byte[] buf, int pos,
                                              com.alibaba.fastjson3.writer.FieldWriter[] writers,
                                              Object bean, long features) {
        buf[pos++] = '{';
        for (com.alibaba.fastjson3.writer.FieldWriter fw : writers) {
            pos = writeOneFieldStatic(gen, buf, pos, fw, bean, features);
            buf = gen.buf; // re-read in case ensureCapacity reallocated
        }
        if (pos > 0 && buf[pos - 1] == ',') pos--;
        buf[pos++] = '}';
        buf[pos++] = ',';
        return pos;
    }

    /**
     * Write a single field (name + value) using static methods. Returns new pos.
     * The switch dispatch is compiled as a tableswitch by the JVM — fast ~2 cycles.
     */
    private static int writeOneFieldStatic(UTF8 gen, byte[] buf, int pos,
                                            com.alibaba.fastjson3.writer.FieldWriter fw,
                                            Object bean, long features) {
        if (fw.customWriter != null || fw.format != null || fw.label != null || gen.bypassStaticPath
                || fw.inclusion != com.alibaba.fastjson3.annotation.Inclusion.DEFAULT
                || fw.fieldFeatures != 0) {
            gen.count = pos; fw.writeField(gen, bean, features); return gen.count;
        }
        switch (fw.typeTag) {
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_INT: {
                if (fw.fieldOffset >= 0 && fw.fieldClass == int.class) {
                    pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                    pos += UTF8.writeIntToBytes(com.alibaba.fastjson3.util.JDKUtils.getInt(bean, fw.fieldOffset), buf, pos);
                    buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_LONG: {
                if (fw.fieldOffset >= 0 && fw.fieldClass == long.class) {
                    pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                    pos += UTF8.writeLongToBytes(com.alibaba.fastjson3.util.JDKUtils.getLongField(bean, fw.fieldOffset), buf, pos);
                    buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_DOUBLE: {
                if (fw.fieldOffset >= 0 && fw.fieldClass == double.class) {
                    pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                    pos = com.alibaba.fastjson3.util.NumberUtils.writeDouble(buf, pos, com.alibaba.fastjson3.util.JDKUtils.getDouble(bean, fw.fieldOffset), true, false);
                    buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_BOOL: {
                if (fw.fieldOffset >= 0 && fw.fieldClass == boolean.class) {
                    pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                    if (com.alibaba.fastjson3.util.JDKUtils.getBoolean(bean, fw.fieldOffset)) {
                        com.alibaba.fastjson3.util.JDKUtils.putIntDirect(buf, pos, UTF8.TRUE_INT);
                        pos += 4;
                    } else {
                        com.alibaba.fastjson3.util.JDKUtils.putIntDirect(buf, pos, UTF8.FALS_INT);
                        pos += 4;
                        buf[pos++] = 'e';
                    }
                    buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_STRING: {
                if (fw.fieldOffset >= 0) {
                    String s = (String) com.alibaba.fastjson3.util.JDKUtils.getObject(bean, fw.fieldOffset);
                    if (s == null) return pos;
                    Object sv = com.alibaba.fastjson3.util.JDKUtils.getStringValue(s);
                    if (sv instanceof byte[] strBytes && strBytes.length == s.length()) {
                        // Worst-case escape inflation is 6x (each control
                        // byte emits a 6-char unicode escape). Pre-fix
                        // this allocated only 1x, so a long string of
                        // control chars walked off the end of the buffer
                        // inside writeLatinStringStatic — fuzz target
                        // fuzzAnySetterWithType found AIOOBE on a 1.4KB
                        // control-char label with the default 8KB pool.
                        int needed = fw.nameBytesLen + strBytes.length * 6 + 3;
                        if (pos + needed + UTF8.SAFE_MARGIN > buf.length) {
                            gen.count = pos; gen.ensureCapacity(needed); buf = gen.buf; pos = gen.count;
                        }
                        pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                        return UTF8.writeLatinStringStatic(buf, pos, strBytes, strBytes.length);
                    }
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_ENUM: {
                if (fw.fieldOffset >= 0) {
                    Object enumVal = com.alibaba.fastjson3.util.JDKUtils.getObject(bean, fw.fieldOffset);
                    if (enumVal == null) return pos;
                    // Fast path: FieldWriter precomputed UTF-8 bytes per ordinal at
                    // construction. Skips per-call name().getBytes(UTF_8) allocation +
                    // StringCoding.hasNegatives scan (~4% of EishayWriteUTF8Bytes CPU).
                    Enum<?> e = (Enum<?>) enumVal;
                    byte[] nameBytes;
                    if (fw.enumNameBytesUtf8 != null) {
                        int ord = e.ordinal();
                        nameBytes = fw.enumNameBytesUtf8[ord];
                        if (fw.enumConstantHasNonAscii != null && fw.enumConstantHasNonAscii[ord]) {
                            gen.hasNonAscii = true;
                        }
                    } else {
                        // Fallback: raw Enum<?> or Object-typed field (rare).
                        nameBytes = e.name().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        for (byte b : nameBytes) {
                            if (b < 0) { gen.hasNonAscii = true; break; }
                        }
                    }
                    int needed = fw.nameBytesLen + nameBytes.length + 3;
                    if (pos + needed + UTF8.SAFE_MARGIN > buf.length) {
                        gen.count = pos; gen.ensureCapacity(needed); buf = gen.buf; pos = gen.count;
                    }
                    pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                    buf[pos++] = '"';
                    System.arraycopy(nameBytes, 0, buf, pos, nameBytes.length);
                    pos += nameBytes.length;
                    buf[pos++] = '"';
                    buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            default:
                // Complex types: LIST_OBJECT, LIST_STRING, arrays, generic → separate method
                return writeOneFieldStaticComplex(gen, buf, pos, fw, bean, features);
        }
        // Fallback for boxed/nullable primitive types
        gen.count = pos; fw.writeField(gen, bean, features); return gen.count;
    }

    /** Handle complex field types in a separate method to keep writeOneFieldStatic small for JIT. */
    private static int writeOneFieldStaticComplex(UTF8 gen, byte[] buf, int pos,
                                                   com.alibaba.fastjson3.writer.FieldWriter fw,
                                                   Object bean, long features) {
        switch (fw.typeTag) {
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_LIST_OBJECT: {
                if (fw.fieldOffset >= 0 && fw.elementClass != null) {
                    @SuppressWarnings("unchecked")
                    java.util.List<?> list = (java.util.List<?>) com.alibaba.fastjson3.util.JDKUtils.getObject(bean, fw.fieldOffset);
                    if (list == null) return pos;
                    if (list.isEmpty()) {
                        pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                        buf[pos++] = '['; buf[pos++] = ']'; buf[pos++] = ',';
                        return pos;
                    }
                    com.alibaba.fastjson3.writer.FieldWriter[] elemFws = getElementWriters(gen, fw.elementClass);
                    if (elemFws != null) {
                        pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                        buf[pos++] = '[';
                        for (int j = 0, size = list.size(); j < size; j++) {
                            Object item = list.get(j);
                            if (item == null) {
                                com.alibaba.fastjson3.util.JDKUtils.putIntDirect(buf, pos, UTF8.NULL_INT);
                                pos += 4; buf[pos++] = ','; continue;
                            }
                            if (pos + elemFws.length * 48 + UTF8.SAFE_MARGIN > buf.length) {
                                gen.count = pos; gen.ensureCapacity(elemFws.length * 48);
                                buf = gen.buf; pos = gen.count;
                            }
                            pos = writeFieldsStaticUTF8(gen, buf, pos, elemFws, item, features);
                            buf = gen.buf;
                        }
                        if (pos > 0 && buf[pos - 1] == ',') pos--;
                        buf[pos++] = ']'; buf[pos++] = ',';
                        return pos;
                    }
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_LIST_STRING: {
                if (fw.fieldOffset >= 0) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> list = (java.util.List<String>) com.alibaba.fastjson3.util.JDKUtils.getObject(bean, fw.fieldOffset);
                    if (list == null) return pos;
                    pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                    if (list.isEmpty()) { buf[pos++] = '['; buf[pos++] = ']'; buf[pos++] = ','; return pos; }
                    buf[pos++] = '[';
                    for (int j = 0, size = list.size(); j < size; j++) {
                        String s = list.get(j);
                        if (s == null) {
                            com.alibaba.fastjson3.util.JDKUtils.putIntDirect(buf, pos, UTF8.NULL_INT);
                            pos += 4; buf[pos++] = ','; continue;
                        }
                        int sNeeded = s.length() + 3;
                        if (pos + sNeeded + UTF8.SAFE_MARGIN > buf.length) {
                            gen.count = pos; gen.ensureCapacity(sNeeded); buf = gen.buf; pos = gen.count;
                        }
                        int r = UTF8.writeStringStatic(buf, pos, s);
                        if (r >= 0) { pos = r; }
                        else { gen.count = pos; gen.writeString(s); pos = gen.count; buf = gen.buf; }
                    }
                    if (pos > 0 && buf[pos - 1] == ',') pos--;
                    buf[pos++] = ']'; buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_STRING_ARRAY: {
                if (fw.fieldOffset >= 0) {
                    String[] arr = (String[]) com.alibaba.fastjson3.util.JDKUtils.getObject(bean, fw.fieldOffset);
                    if (arr == null) return pos;
                    pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                    buf[pos++] = '[';
                    for (String s : arr) {
                        if (s == null) {
                            com.alibaba.fastjson3.util.JDKUtils.putIntDirect(buf, pos, UTF8.NULL_INT);
                            pos += 4; buf[pos++] = ','; continue;
                        }
                        int sn = s.length() + 3;
                        if (pos + sn + UTF8.SAFE_MARGIN > buf.length) {
                            gen.count = pos; gen.ensureCapacity(sn); buf = gen.buf; pos = gen.count;
                        }
                        int r = UTF8.writeStringStatic(buf, pos, s);
                        if (r >= 0) { pos = r; }
                        else { gen.count = pos; gen.writeString(s); pos = gen.count; buf = gen.buf; }
                    }
                    if (pos > 0 && buf[pos - 1] == ',') pos--;
                    buf[pos++] = ']'; buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_LONG_ARRAY: {
                if (fw.fieldOffset >= 0) {
                    long[] arr = (long[]) com.alibaba.fastjson3.util.JDKUtils.getObject(bean, fw.fieldOffset);
                    if (arr == null) return pos;
                    pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                    buf[pos++] = '[';
                    for (long v : arr) {
                        pos += UTF8.writeLongToBytes(v, buf, pos);
                        buf[pos++] = ',';
                    }
                    if (pos > 0 && buf[pos - 1] == ',') pos--;
                    buf[pos++] = ']'; buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_INT_ARRAY: {
                if (fw.fieldOffset >= 0) {
                    int[] arr = (int[]) com.alibaba.fastjson3.util.JDKUtils.getObject(bean, fw.fieldOffset);
                    if (arr == null) return pos;
                    pos = UTF8.writeNameStatic(buf, pos, fw.nameByteLongs, fw.nameBytes, fw.nameBytesLen);
                    buf[pos++] = '[';
                    for (int v : arr) {
                        pos += UTF8.writeIntToBytes(v, buf, pos);
                        buf[pos++] = ',';
                    }
                    if (pos > 0 && buf[pos - 1] == ',') pos--;
                    buf[pos++] = ']'; buf[pos++] = ',';
                    return pos;
                }
                break;
            }
        }
        // Fallback
        gen.count = pos; fw.writeField(gen, bean, features); return gen.count;
    }

    /**
     * Resolve the inline FieldWriter[] for a TYPE_LIST_OBJECT element type via the
     * owning mapper's per-type writer cache, so mapper-specific mix-ins / registered
     * writers / polymorphic discriminators applied to the element type are honoured
     * when serialising nested list fields. Falling back to ObjectMapper.shared() when
     * owner is null preserves the pre-existing behaviour for generators constructed
     * outside a mapper context.
     */
    private static com.alibaba.fastjson3.writer.FieldWriter[] getElementWriters(UTF8 gen, Class<?> elementClass) {
        try {
            com.alibaba.fastjson3.ObjectWriter<?> w = gen.effectiveMapper().getObjectWriter(elementClass);
            if (w instanceof com.alibaba.fastjson3.writer.ObjectWriterCreator.ReflectObjectWriter rw) {
                return rw.writers;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // ---- Static field-writing paths (no braces, called between startObject/endObject) ----

    /**
     * Write all fields using static dispatch for UTF8 generator.
     * Called from ObjectWriterCreator.writeFields — no {/} framing.
     */
    public static void writeFieldsStaticUTF8NoFrame(UTF8 gen, com.alibaba.fastjson3.writer.FieldWriter[] writers,
                                                     Object bean, long features) {
        gen.ensureCapacity(writers.length * 48);
        byte[] buf = gen.buf;
        int pos = gen.count;
        for (com.alibaba.fastjson3.writer.FieldWriter fw : writers) {
            pos = writeOneFieldStatic(gen, buf, pos, fw, bean, features);
            buf = gen.buf;
        }
        gen.count = pos;
    }

    /**
     * Write all fields using static dispatch for Char generator.
     * Uses char[] nameChars directly — no virtual dispatch per field.
     */
    public static void writeFieldsStaticChar(Char gen, com.alibaba.fastjson3.writer.FieldWriter[] writers,
                                              Object bean, long features) {
        gen.ensureCapacity(gen.count + writers.length * 48);
        char[] buf = gen.buf;
        int pos = gen.count;
        for (com.alibaba.fastjson3.writer.FieldWriter fw : writers) {
            pos = writeOneFieldStaticChar(gen, buf, pos, fw, bean, features);
            buf = gen.buf;
        }
        gen.count = pos;
    }

    /**
     * Write a single field for Char generator. Returns new pos.
     */
    private static int writeOneFieldStaticChar(Char gen, char[] buf, int pos,
                                                com.alibaba.fastjson3.writer.FieldWriter fw,
                                                Object bean, long features) {
        if (fw.customWriter != null || fw.format != null || fw.label != null || gen.bypassStaticPath
                || fw.inclusion != com.alibaba.fastjson3.annotation.Inclusion.DEFAULT
                || fw.fieldFeatures != 0) {
            gen.count = pos; fw.writeField(gen, bean, features); return gen.count;
        }
        char[] nameChars = fw.nameChars;
        int nameLen = nameChars.length;
        switch (fw.typeTag) {
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_INT: {
                if (fw.fieldOffset >= 0 && fw.fieldClass == int.class) {
                    int needed = nameLen + 12;
                    if (pos + needed > buf.length) { gen.count = pos; gen.ensureCapacity(pos + needed); buf = gen.buf; pos = gen.count; }
                    System.arraycopy(nameChars, 0, buf, pos, nameLen);
                    pos += nameLen;
                    pos += Char.writeIntToChars(com.alibaba.fastjson3.util.JDKUtils.getInt(bean, fw.fieldOffset), buf, pos);
                    buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_LONG: {
                if (fw.fieldOffset >= 0 && fw.fieldClass == long.class) {
                    int needed = nameLen + 21;
                    if (pos + needed > buf.length) { gen.count = pos; gen.ensureCapacity(pos + needed); buf = gen.buf; pos = gen.count; }
                    System.arraycopy(nameChars, 0, buf, pos, nameLen);
                    pos += nameLen;
                    pos += Char.writeLongToChars(com.alibaba.fastjson3.util.JDKUtils.getLongField(bean, fw.fieldOffset), buf, pos);
                    buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_DOUBLE: {
                if (fw.fieldOffset >= 0 && fw.fieldClass == double.class) {
                    double val = com.alibaba.fastjson3.util.JDKUtils.getDouble(bean, fw.fieldOffset);
                    if (!Double.isNaN(val) && !Double.isInfinite(val)) {
                        String s = Double.toString(val);
                        int sLen = s.length();
                        int needed = nameLen + sLen + 1;
                        if (pos + needed > buf.length) { gen.count = pos; gen.ensureCapacity(pos + needed); buf = gen.buf; pos = gen.count; }
                        System.arraycopy(nameChars, 0, buf, pos, nameLen);
                        pos += nameLen;
                        s.getChars(0, sLen, buf, pos);
                        pos += sLen;
                        buf[pos++] = ',';
                        return pos;
                    }
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_BOOL: {
                if (fw.fieldOffset >= 0 && fw.fieldClass == boolean.class) {
                    int needed = nameLen + 6;
                    if (pos + needed > buf.length) { gen.count = pos; gen.ensureCapacity(pos + needed); buf = gen.buf; pos = gen.count; }
                    System.arraycopy(nameChars, 0, buf, pos, nameLen);
                    pos += nameLen;
                    if (com.alibaba.fastjson3.util.JDKUtils.getBoolean(bean, fw.fieldOffset)) {
                        buf[pos] = 't'; buf[pos+1] = 'r'; buf[pos+2] = 'u'; buf[pos+3] = 'e';
                        pos += 4;
                    } else {
                        buf[pos] = 'f'; buf[pos+1] = 'a'; buf[pos+2] = 'l'; buf[pos+3] = 's'; buf[pos+4] = 'e';
                        pos += 5;
                    }
                    buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_STRING: {
                if (fw.fieldOffset >= 0) {
                    String s = (String) com.alibaba.fastjson3.util.JDKUtils.getObject(bean, fw.fieldOffset);
                    if (s == null) return pos;
                    int sLen = s.length();
                    int needed = nameLen + sLen + 4;
                    if (pos + needed > buf.length) { gen.count = pos; gen.ensureCapacity(pos + needed); buf = gen.buf; pos = gen.count; }
                    System.arraycopy(nameChars, 0, buf, pos, nameLen);
                    pos += nameLen;
                    buf[pos++] = '"';
                    s.getChars(0, sLen, buf, pos);
                    // Check for escape chars
                    boolean needsEscape = false;
                    for (int i = 0; i < sLen; i++) {
                        char ch = buf[pos + i];
                        if (ch < 0x20 || ch == '"' || ch == '\\') { needsEscape = true; break; }
                    }
                    if (!needsEscape) {
                        pos += sLen;
                        buf[pos] = '"'; buf[pos+1] = ',';
                        pos += 2;
                        return pos;
                    }
                    // Fall back to gen.writeString for escape handling
                    pos -= (nameLen + 1); // undo name + opening quote
                }
                break;
            }
            case com.alibaba.fastjson3.writer.FieldWriter.TYPE_ENUM: {
                if (fw.fieldOffset >= 0) {
                    Object enumVal = com.alibaba.fastjson3.util.JDKUtils.getObject(bean, fw.fieldOffset);
                    if (enumVal == null) return pos;
                    String name = ((Enum<?>) enumVal).name();
                    int sLen = name.length();
                    int needed = nameLen + sLen + 4;
                    if (pos + needed > buf.length) { gen.count = pos; gen.ensureCapacity(pos + needed); buf = gen.buf; pos = gen.count; }
                    System.arraycopy(nameChars, 0, buf, pos, nameLen);
                    pos += nameLen;
                    buf[pos++] = '"';
                    name.getChars(0, sLen, buf, pos);
                    pos += sLen;
                    buf[pos++] = '"';
                    buf[pos++] = ',';
                    return pos;
                }
                break;
            }
            default:
                break;
        }
        // Fallback: use virtual dispatch
        gen.count = pos; fw.writeField(gen, bean, features); return gen.count;
    }

    // ---- Output ----

    /**
     * Get result as byte array (UTF-8).
     */
    public abstract byte[] toByteArray();

    /**
     * Get the number of bytes/chars written.
     */
    /** Reset count for buffer reuse (diagnostic only). */
    public void resetForReuse() { count = 0; }

    public int size() {
        return count;
    }

    @Override
    public void flush() {
        // default no-op
    }

    @Override
    public void close() {
        // default no-op; subclasses may return buffers to pool
    }

    // ---- Public implementations ----

    public static final class Char extends JSONGenerator {
        private char[] buf;
        private boolean pooled;

        Char(long features) {
            super(features);
            this.buf = BufferPool.borrowCharBuffer();
            this.pooled = true;
        }

        private void ensureCapacity(int minCap) {
            if (minCap > buf.length) {
                int newCap = Math.max(buf.length + (buf.length >> 1), minCap);
                char[] newBuf = new char[newCap];
                System.arraycopy(buf, 0, newBuf, 0, count);
                buf = newBuf;
            }
        }

        @Override
        public void ensureCapacityPublic(int needed) {
            ensureCapacity(count + needed);
        }

        private void writeNewlineIndent() {
            int level = indentLevel;
            char[][] table = indentStep == 4 ? INDENT_CHARS_4 : INDENT_CHARS_2;
            if (level < MAX_INDENT_LEVEL) {
                char[] indent = table[level];
                ensureCapacity(count + indent.length);
                System.arraycopy(indent, 0, buf, count, indent.length);
                count += indent.length;
            } else {
                int spaces = indentStep * level;
                ensureCapacity(count + 1 + spaces);
                buf[count++] = '\n';
                for (int i = 0; i < spaces; i++) {
                    buf[count++] = ' ';
                }
            }
        }

        @Override
        public void beforeArrayValue() {
            if (pretty) {
                writeNewlineIndent();
            }
        }

        @Override
        public void startObject() {
            ensureCapacity(count + 1);
            buf[count++] = '{';
            if (pretty) {
                indentLevel++;
            }
        }

        @Override
        public void endObject() {
            if (pretty) {
                indentLevel--;
                if (count > 0 && buf[count - 1] == '{') {
                    ensureCapacity(count + 2);
                    buf[count] = '}'; buf[count + 1] = ',';
                    count += 2;
                    return;
                }
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                writeNewlineIndent();
                ensureCapacity(count + 2);
                buf[count] = '}'; buf[count + 1] = ',';
                count += 2;
            } else {
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                ensureCapacity(count + 2);
                buf[count] = '}'; buf[count + 1] = ',';
                count += 2;
            }
        }

        @Override
        public void startArray() {
            ensureCapacity(count + 1);
            buf[count++] = '[';
            if (pretty) {
                indentLevel++;
            }
        }

        @Override
        public void endArray() {
            if (pretty) {
                indentLevel--;
                if (count > 0 && buf[count - 1] == '[') {
                    // empty array: []
                    ensureCapacity(count + 2);
                    buf[count++] = ']';
                    buf[count++] = ',';
                    return;
                }
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                writeNewlineIndent();
                ensureCapacity(count + 2);
                buf[count++] = ']';
                buf[count++] = ',';
            } else {
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                ensureCapacity(count + 2);
                buf[count++] = ']';
                buf[count++] = ',';
            }
        }

        @Override
        public void writeName(String name) {
            int len = name.length();
            if (pretty) {
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                if (count > 0 && buf[count - 1] != '{') {
                    buf[count++] = ',';
                }
                writeNewlineIndent();
                ensureCapacity(count + len + 4); // after indent, ensure space for name
            } else {
                ensureCapacity(count + len + 4);
            }
            if (unquoteFieldName) {
                name.getChars(0, len, buf, count);
                count += len;
            } else {
                // Scan for chars that need JSON escaping. Mirrors writeString
                // so name escape stays in lockstep with value escape across
                // all feature flags. Categories that trigger the slow path:
                //   1. ch in active escapeChars table (named escape +
                //      BrowserCompatible/Secure markers) excluding '/'
                //      which has a table entry but is never actually escaped.
                //   2. ch under 0x20 (JSON spec RFC 8259 section 7 requires
                //      escape for every control char, even those without a
                //      named escape: 0x00-0x07, 0x0B, 0x0E-0x1F).
                //   3. ch == quoteChar when quoteChar != '"' (single-quote
                //      mode must escape its own quote).
                //   4. non-ASCII when escapeNoneAscii is on.
                boolean needsEscape = false;
                for (int i = 0; i < len; i++) {
                    char ch = name.charAt(i);
                    if (ch < 128) {
                        if (ch < 0x20 || (escapeChars[ch] != 0 && ch != '/')) {
                            needsEscape = true;
                            break;
                        }
                        if (quoteChar != '"' && ch == quoteChar) {
                            needsEscape = true;
                            break;
                        }
                    } else if (escapeNoneAscii) {
                        needsEscape = true;
                        break;
                    }
                }
                if (needsEscape) {
                    // Delegate to writeString — handles every escape form
                    // already exercised by value-side serialisation. It
                    // emits trailing `,`; we patch that to `:` below.
                    writeString(name);
                    buf[count - 1] = ':';
                    if (pretty) {
                        buf[count++] = ' ';
                    }
                    return;
                }
                buf[count++] = (char) quoteChar;
                name.getChars(0, len, buf, count);
                count += len;
                buf[count++] = (char) quoteChar;
            }
            buf[count++] = ':';
            if (pretty) {
                buf[count++] = ' ';
            }
        }

        @Override
        public void writePreEncodedName(char[] nameChars, byte[] nameBytes) {
            int len = nameChars.length;
            if (pretty) {
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                if (count > 0 && buf[count - 1] != '{') {
                    buf[count++] = ',';
                }
                writeNewlineIndent();
                ensureCapacity(count + len + 1); // after indent, ensure space for name
            } else {
                ensureCapacity(count + len + 1);
            }
            System.arraycopy(nameChars, 0, buf, count, len);
            count += len;
            if (pretty) {
                buf[count++] = ' ';
            }
        }

        // ---- Fused name+value writers for Char (single ensureCapacity) ----

        @Override
        public void writeNameString(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, String value) {
            if (pretty || extendedEscape || escapeNoneAscii) {
                writePreEncodedName(nameChars, nameBytes);
                writeString(value);
                return;
            }
            int nameLen = nameChars.length;
            if (value == null) {
                ensureCapacity(count + nameLen + 5);
                System.arraycopy(nameChars, 0, buf, count, nameLen);
                count += nameLen;
                buf[count++] = 'n'; buf[count++] = 'u'; buf[count++] = 'l'; buf[count++] = 'l'; buf[count++] = ',';
                return;
            }
            int valLen = value.length();
            ensureCapacity(count + nameLen + valLen + 4);
            int pos = count;
            System.arraycopy(nameChars, 0, buf, pos, nameLen);
            pos += nameLen;
            buf[pos++] = '"';
            // Fast path: check-and-copy without escape for common ASCII strings
            value.getChars(0, valLen, buf, pos);
            // Scan for chars needing escape
            boolean needsEscape = false;
            for (int i = 0; i < valLen; i++) {
                char ch = buf[pos + i];
                if (ch < 0x20 || ch == '"' || ch == '\\') {
                    needsEscape = true;
                    break;
                }
            }
            if (!needsEscape) {
                pos += valLen;
                buf[pos++] = '"';
                buf[pos++] = ',';
                count = pos;
            } else {
                // Fallback: reset and use writeString which handles escaping
                count = pos - 1; // undo the '"'
                count -= nameLen; // undo name copy
                writePreEncodedName(nameChars, nameBytes);
                writeString(value);
            }
        }

        @Override
        public void writeNameInt32(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, int value) {
            if (pretty || nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeInt32(value);
                return;
            }
            int nameLen = nameChars.length;
            ensureCapacity(count + nameLen + 12);
            int pos = count;
            System.arraycopy(nameChars, 0, buf, pos, nameLen);
            pos += nameLen;
            pos += writeIntToChars(value, buf, pos);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameInt64(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, long value) {
            if (pretty || longAsString || nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeInt64(value);
                return;
            }
            int nameLen = nameChars.length;
            ensureCapacity(count + nameLen + 21);
            int pos = count;
            System.arraycopy(nameChars, 0, buf, pos, nameLen);
            pos += nameLen;
            pos += writeLongToChars(value, buf, pos);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameBool(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, boolean value) {
            if (pretty || boolAsNumber || nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeBool(value);
                return;
            }
            int nameLen = nameChars.length;
            ensureCapacity(count + nameLen + 6);
            int pos = count;
            System.arraycopy(nameChars, 0, buf, pos, nameLen);
            pos += nameLen;
            if (value) {
                buf[pos++] = 't'; buf[pos++] = 'r'; buf[pos++] = 'u'; buf[pos++] = 'e';
            } else {
                buf[pos++] = 'f'; buf[pos++] = 'a'; buf[pos++] = 'l'; buf[pos++] = 's'; buf[pos++] = 'e';
            }
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameDouble(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, double value) {
            if (pretty || nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeDouble(value);
                return;
            }
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                writePreEncodedName(nameChars, nameBytes);
                writeNull();
                return;
            }
            String s = Double.toString(value);
            int nameLen = nameChars.length;
            int valLen = s.length();
            ensureCapacity(count + nameLen + valLen + 1);
            int pos = count;
            System.arraycopy(nameChars, 0, buf, pos, nameLen);
            pos += nameLen;
            s.getChars(0, valLen, buf, pos);
            pos += valLen;
            buf[pos++] = ',';
            count = pos;
        }

        // ---- Compact variants for ASM-generated writers ----
        // Guard: if pretty or bypass features active, delegate to non-compact methods.

        @Override
        public void writeNameInt32Compact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, int value) {
            if (pretty || bypassStaticPath) { writeNameInt32(nameByteLongs, nameBytesLen, nameBytes, nameChars, value); return; }
            int nameLen = nameChars.length;
            ensureCapacity(count + nameLen + 12);
            int pos = count;
            System.arraycopy(nameChars, 0, buf, pos, nameLen);
            pos += nameLen;
            pos += writeIntToChars(value, buf, pos);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameInt64Compact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, long value) {
            if (pretty || bypassStaticPath) { writeNameInt64(nameByteLongs, nameBytesLen, nameBytes, nameChars, value); return; }
            int nameLen = nameChars.length;
            ensureCapacity(count + nameLen + 21);
            int pos = count;
            System.arraycopy(nameChars, 0, buf, pos, nameLen);
            pos += nameLen;
            pos += writeLongToChars(value, buf, pos);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameStringCompact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, String value) {
            if (pretty || bypassStaticPath) { writeNameString(nameByteLongs, nameBytesLen, nameBytes, nameChars, value); return; }
            int nameLen = nameChars.length;
            int valLen = (value != null) ? value.length() : 4; // "null".length()
            ensureCapacity(count + nameLen + valLen + 4); // quotes, comma, margin
            if (value == null) {
                int pos = count;
                System.arraycopy(nameChars, 0, buf, pos, nameLen);
                pos += nameLen;
                buf[pos] = 'n'; buf[pos+1] = 'u'; buf[pos+2] = 'l'; buf[pos+3] = 'l'; buf[pos+4] = ',';
                count = pos + 5;
                return;
            }
            int pos = count;
            System.arraycopy(nameChars, 0, buf, pos, nameLen);
            pos += nameLen;
            buf[pos++] = '"';
            value.getChars(0, valLen, buf, pos);
            boolean needsEscape = false;
            for (int i = 0; i < valLen; i++) {
                char ch = buf[pos + i];
                if (ch < 0x20 || ch == '"' || ch == '\\') { needsEscape = true; break; }
            }
            if (!needsEscape) {
                pos += valLen;
                buf[pos] = '"'; buf[pos + 1] = ',';
                count = pos + 2;
            } else {
                // Fallback for escaped strings
                count = pos - 1 - nameLen;
                writeNameString(nameByteLongs, nameBytesLen, nameBytes, nameChars, value);
            }
        }

        @Override
        public void writeNameBoolCompact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, boolean value) {
            if (pretty || bypassStaticPath) { writeNameBool(nameByteLongs, nameBytesLen, nameBytes, nameChars, value); return; }
            int nameLen = nameChars.length;
            ensureCapacity(count + nameLen + 6);
            int pos = count;
            System.arraycopy(nameChars, 0, buf, pos, nameLen);
            pos += nameLen;
            if (value) {
                buf[pos] = 't'; buf[pos+1] = 'r'; buf[pos+2] = 'u'; buf[pos+3] = 'e';
                pos += 4;
            } else {
                buf[pos] = 'f'; buf[pos+1] = 'a'; buf[pos+2] = 'l'; buf[pos+3] = 's'; buf[pos+4] = 'e';
                pos += 5;
            }
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameDoubleCompact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, double value) {
            if (pretty || bypassStaticPath || Double.isNaN(value) || Double.isInfinite(value)) {
                writeNameDouble(nameByteLongs, nameBytesLen, nameBytes, nameChars, value);
                return;
            }
            String s = Double.toString(value);
            int nameLen = nameChars.length;
            int sLen = s.length();
            ensureCapacity(count + nameLen + sLen + 1);
            int pos = count;
            System.arraycopy(nameChars, 0, buf, pos, nameLen);
            pos += nameLen;
            s.getChars(0, sLen, buf, pos);
            pos += sLen;
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNull() {
            ensureCapacity(count + 5);
            int pos = count;
            buf[pos] = 'n'; buf[pos + 1] = 'u'; buf[pos + 2] = 'l'; buf[pos + 3] = 'l'; buf[pos + 4] = ',';
            count = pos + 5;
        }

        @Override
        public void writeTrue() {
            ensureCapacity(count + 5);
            int pos = count;
            buf[pos] = 't'; buf[pos + 1] = 'r'; buf[pos + 2] = 'u'; buf[pos + 3] = 'e'; buf[pos + 4] = ',';
            count = pos + 5;
        }

        @Override
        public void writeFalse() {
            ensureCapacity(count + 6);
            int pos = count;
            buf[pos] = 'f'; buf[pos + 1] = 'a'; buf[pos + 2] = 'l'; buf[pos + 3] = 's'; buf[pos + 4] = 'e'; buf[pos + 5] = ',';
            count = pos + 6;
        }

        @Override
        public void writeBool(boolean val) {
            if (boolAsNumber) {
                writeInt32(val ? 1 : 0);
                return;
            }
            if (nonStringAsString) {
                writeString(Boolean.toString(val));
                return;
            }
            if (val) {
                writeTrue();
            } else {
                writeFalse();
            }
        }

        @Override
        public void writeInt32(int val) {
            if (nonStringAsString) {
                writeString(Integer.toString(val));
                return;
            }
            ensureCapacity(count + 12);
            count += writeIntToChars(val, buf, count);
            buf[count++] = ',';
        }

        @Override
        public void writeInt64(long val) {
            if (longAsString || nonStringAsString) {
                writeString(Long.toString(val));
                return;
            }
            ensureCapacity(count + 21);
            count += writeLongToChars(val, buf, count);
            buf[count++] = ',';
        }

        @Override
        public void writeFloat(float val) {
            if (nonStringAsString) {
                writeString(Float.toString(val));
                return;
            }
            if (Float.isNaN(val) || Float.isInfinite(val)) {
                if (floatSpecialAsString) {
                    writeString(Float.toString(val));
                } else {
                    writeNull();
                }
                return;
            }
            String s = Float.toString(val);
            int len = s.length();
            ensureCapacity(count + len + 1);
            s.getChars(0, len, buf, count);
            count += len;
            buf[count++] = ',';
        }

        @Override
        public void writeDouble(double val) {
            if (nonStringAsString) {
                writeString(Double.toString(val));
                return;
            }
            if (Double.isNaN(val) || Double.isInfinite(val)) {
                if (floatSpecialAsString) {
                    writeString(Double.toString(val));
                } else {
                    writeNull();
                }
                return;
            }
            String s = Double.toString(val);
            int len = s.length();
            ensureCapacity(count + len + 1);
            s.getChars(0, len, buf, count);
            count += len;
            buf[count++] = ',';
        }

        @Override
        public void writeDecimal(BigDecimal val) {
            if (val == null) {
                writeNull();
                return;
            }
            String s = bigDecimalPlain ? val.toPlainString() : val.toString();
            if (nonStringAsString) {
                writeString(s);
                return;
            }
            int len = s.length();
            ensureCapacity(count + len + 1);
            s.getChars(0, len, buf, count);
            count += len;
            buf[count++] = ',';
        }

        @Override
        public void writeString(String val) {
            if (val == null) {
                writeNull();
                return;
            }
            int len = val.length();
            char q = (char) quoteChar;
            // Conservative initial estimate: most chars are not escaped
            ensureCapacity(count + len + 16);
            buf[count++] = q;
            for (int i = 0; i < len; i++) {
                char ch = val.charAt(i);
                if (ch < 128) {
                    char escaped = escapeChars[ch];
                    if (escaped != 0 && ch != '/') {
                        ensureCapacity(count + 6 + (len - i) + 3);
                        if (escaped == '"' || escaped == '\\' || escaped == 'b' || escaped == 'f'
                                || escaped == 'n' || escaped == 'r' || escaped == 't') {
                            if (q != '"' && ch == '"') {
                                // UseSingleQuotes: don't escape double quotes
                                buf[count++] = ch;
                            } else {
                                buf[count] = '\\';
                                buf[count + 1] = escaped;
                                count += 2;
                            }
                        } else {
                            buf[count] = '\\'; buf[count + 1] = 'u';
                            buf[count + 2] = '0'; buf[count + 3] = '0';
                            buf[count + 4] = DIGITS[ch >> 4]; buf[count + 5] = DIGITS[ch & 0xF];
                            count += 6;
                        }
                    } else if (q != '"' && ch == q) {
                        // UseSingleQuotes: escape the single quote character
                        ensureCapacity(count + 2 + (len - i) + 3);
                        buf[count++] = '\\';
                        buf[count++] = q;
                    } else if (ch < 0x20) {
                        ensureCapacity(count + 6 + (len - i) + 3);
                        buf[count] = '\\'; buf[count + 1] = 'u';
                        buf[count + 2] = '0'; buf[count + 3] = '0';
                        buf[count + 4] = DIGITS[ch >> 4]; buf[count + 5] = DIGITS[ch & 0xF];
                        count += 6;
                    } else {
                        buf[count++] = ch;
                    }
                } else {
                    if (escapeNoneAscii) {
                        ensureCapacity(count + 6 + (len - i) + 3);
                        buf[count++] = '\\';
                        buf[count++] = 'u';
                        buf[count++] = DIGITS[(ch >> 12) & 0xF];
                        buf[count++] = DIGITS[(ch >> 8) & 0xF];
                        buf[count++] = DIGITS[(ch >> 4) & 0xF];
                        buf[count++] = DIGITS[ch & 0xF];
                    } else {
                        buf[count++] = ch;
                    }
                }
            }
            buf[count++] = q;
            buf[count++] = ',';
        }

        @Override
        public void writeRaw(String raw) {
            int len = raw.length();
            ensureCapacity(count + len);
            raw.getChars(0, len, buf, count);
            count += len;
        }

        @Override
        public void writeRawBytes(byte[] bytes) {
            int len = bytes.length;
            ensureCapacity(count + len);
            for (int i = 0; i < len; i++) {
                buf[count + i] = (char) (bytes[i] & 0xFF);
            }
            count += len;
        }

        private int outputCount() {
            int c = count;
            if (c > 0 && buf[c - 1] == ',') {
                c--;
            }
            return c;
        }

        @Override
        public byte[] toByteArray() {
            int c = outputCount();
            // Use an explicit encoder configured to substitute lone
            // surrogates with U+FFFD, matching the UTF-8 generator's
            // value-side surrogate handling. JDK default
            // `String.getBytes(UTF_8)` falls back to `?` (0x3F) — the
            // same inconsistency the round-2 audit flagged on
            // FieldWriter.encodeNameBytes (now fixed in lockstep).
            java.nio.charset.CharsetEncoder enc = StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPLACE)
                    .replaceWith(new byte[]{(byte) 0xEF, (byte) 0xBF, (byte) 0xBD});
            try {
                java.nio.ByteBuffer bb = enc.encode(java.nio.CharBuffer.wrap(buf, 0, c));
                byte[] out = new byte[bb.remaining()];
                bb.get(out);
                return out;
            } catch (java.nio.charset.CharacterCodingException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public String toString() {
            return new String(buf, 0, outputCount());
        }

        @Override
        public void close() {
            if (pooled) {
                BufferPool.returnCharBuffer(buf);
                buf = null;
                pooled = false;
            }
        }

        // Fast int-to-chars using lookup table
        private static int writeIntToChars(int val, char[] buf, int pos) {
            if (val == Integer.MIN_VALUE) {
                String s = "-2147483648";
                s.getChars(0, 11, buf, pos);
                return 11;
            }
            int start = pos;
            if (val < 0) {
                buf[pos++] = '-';
                val = -val;
            }
            if (val < 10) {
                buf[pos++] = (char) ('0' + val);
            } else if (val < 100) {
                buf[pos++] = (char) ('0' + val / 10);
                buf[pos++] = (char) ('0' + val % 10);
            } else {
                // General case: write digits in reverse then flip
                int end = pos;
                while (val > 0) {
                    buf[end++] = (char) ('0' + val % 10);
                    val /= 10;
                }
                // Reverse
                int left = pos, right = end - 1;
                while (left < right) {
                    char tmp = buf[left];
                    buf[left] = buf[right];
                    buf[right] = tmp;
                    left++;
                    right--;
                }
                pos = end;
            }
            return pos - start;
        }

        private static int writeLongToChars(long val, char[] buf, int pos) {
            if (val == Long.MIN_VALUE) {
                String s = "-9223372036854775808";
                s.getChars(0, 20, buf, pos);
                return 20;
            }
            int start = pos;
            if (val < 0) {
                buf[pos++] = '-';
                val = -val;
            }
            if (val < 10) {
                buf[pos++] = (char) ('0' + val);
            } else {
                int end = pos;
                while (val > 0) {
                    buf[end++] = (char) ('0' + (int) (val % 10));
                    val /= 10;
                }
                int left = pos, right = end - 1;
                while (left < right) {
                    char tmp = buf[left];
                    buf[left] = buf[right];
                    buf[right] = tmp;
                    left++;
                    right--;
                }
                pos = end;
            }
            return pos - start;
        }
    }

    public static final class UTF8 extends JSONGenerator {
        static final int SAFE_MARGIN = 512;

        private byte[] buf;
        private boolean pooled;

        UTF8(long features) {
            super(features);
            this.buf = BufferPool.borrowByteBuffer();
            this.pooled = true;
        }

        private void ensureCapacity(int needed) {
            int minCap = count + needed + SAFE_MARGIN;
            if (minCap > buf.length) {
                int newCap = Math.max(buf.length + (buf.length >> 1), minCap);
                byte[] newBuf = new byte[newCap];
                System.arraycopy(buf, 0, newBuf, 0, count);
                buf = newBuf;
            }
        }

        @Override
        public void ensureCapacityPublic(int needed) {
            ensureCapacity(needed);
        }

        private void writeNewlineIndent() {
            int level = indentLevel;
            byte[][] table = indentStep == 4 ? INDENT_BYTES_4 : INDENT_BYTES_2;
            if (level < MAX_INDENT_LEVEL) {
                byte[] indent = table[level];
                ensureCapacity(indent.length);
                System.arraycopy(indent, 0, buf, count, indent.length);
                count += indent.length;
            } else {
                int spaces = indentStep * level;
                ensureCapacity(1 + spaces);
                buf[count++] = '\n';
                for (int i = 0; i < spaces; i++) {
                    buf[count++] = ' ';
                }
            }
        }

        @Override
        public void beforeArrayValue() {
            if (pretty) {
                writeNewlineIndent();
            }
        }

        // Small token write — no capacity check (covered by SAFE_MARGIN)
        @Override
        public void startObject() {
            buf[count++] = '{';
            if (pretty) {
                indentLevel++;
            }
        }

        @Override
        public void endObject() {
            if (pretty) {
                indentLevel--;
                if (count > 0 && buf[count - 1] == '{') {
                    buf[count++] = '}';
                    buf[count++] = ',';
                    return;
                }
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                writeNewlineIndent();
                buf[count++] = '}';
                buf[count++] = ',';
            } else {
                // Strip trailing comma if present; handle empty object safely
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                buf[count++] = '}';
                buf[count++] = ',';
            }
        }

        @Override
        public void startArray() {
            buf[count++] = '[';
            if (pretty) {
                indentLevel++;
            }
        }

        @Override
        public void endArray() {
            if (pretty) {
                indentLevel--;
                if (count > 0 && buf[count - 1] == '[') {
                    buf[count++] = ']';
                    buf[count++] = ',';
                    return;
                }
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                writeNewlineIndent();
                buf[count++] = ']';
                buf[count++] = ',';
            } else {
                // Strip trailing comma if present; handle empty array safely
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                buf[count++] = ']';
                buf[count++] = ',';
            }
        }

        @Override
        public void writeName(String name) {
            int len = name.length();
            if (pretty) {
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                if (count > 0 && buf[count - 1] != '{') {
                    ensureCapacity(len * 4 + 5);
                    buf[count++] = ',';
                } else {
                    ensureCapacity(len * 4 + 4);
                }
                writeNewlineIndent();
            } else {
                ensureCapacity(len * 4 + 4);
            }
            if (unquoteFieldName) {
                for (int i = 0; i < len; i++) {
                    char ch = name.charAt(i);
                    if (ch < 0x80) {
                        buf[count++] = (byte) ch;
                    } else if (ch < 0x800) {
                        buf[count++] = (byte) (0xC0 | (ch >> 6));
                        buf[count++] = (byte) (0x80 | (ch & 0x3F));
                    } else if (Character.isHighSurrogate(ch)) {
                        if (i + 1 < len && Character.isLowSurrogate(name.charAt(i + 1))) {
                            int cp = Character.toCodePoint(ch, name.charAt(++i));
                            buf[count++] = (byte) (0xF0 | (cp >> 18));
                            buf[count++] = (byte) (0x80 | ((cp >> 12) & 0x3F));
                            buf[count++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
                            buf[count++] = (byte) (0x80 | (cp & 0x3F));
                        } else {
                            buf[count++] = (byte) 0xEF;
                            buf[count++] = (byte) 0xBF;
                            buf[count++] = (byte) 0xBD;
                        }
                    } else if (Character.isLowSurrogate(ch)) {
                        // Lone low surrogate — emit U+FFFD (matches the
                        // lone-high branch above; the general 3-byte else
                        // would produce CESU-8 bytes that are invalid UTF-8
                        // per RFC 3629 §3).
                        buf[count++] = (byte) 0xEF;
                        buf[count++] = (byte) 0xBF;
                        buf[count++] = (byte) 0xBD;
                    } else {
                        buf[count++] = (byte) (0xE0 | (ch >> 12));
                        buf[count++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                        buf[count++] = (byte) (0x80 | (ch & 0x3F));
                    }
                }
            } else {
                // Scan for chars that need JSON escaping. Mirrors writeString
                // so name escape stays in lockstep with value escape across
                // all feature flags. Same categories as the Char gen sibling
                // above (escape table, ch < 0x20, alt quote, escapeNoneAscii).
                boolean needsEscape = false;
                for (int i = 0; i < len; i++) {
                    char ch = name.charAt(i);
                    if (ch < 128) {
                        if (ch < 0x20 || (escapeChars[ch] != 0 && ch != '/')) {
                            needsEscape = true;
                            break;
                        }
                        if (quoteChar != '"' && ch == quoteChar) {
                            needsEscape = true;
                            break;
                        }
                    } else if (escapeNoneAscii) {
                        needsEscape = true;
                        break;
                    }
                }
                if (needsEscape) {
                    // Delegate to writeString — already handles every
                    // escape form including escapeNoneAscii / surrogate
                    // pairs / Latin1 fast path. It emits trailing `,`;
                    // we patch to `:` below.
                    writeString(name);
                    buf[count - 1] = ':';
                    if (pretty) {
                        buf[count++] = ' ';
                    }
                    return;
                }
                buf[count++] = quoteChar;
                for (int i = 0; i < len; i++) {
                    char ch = name.charAt(i);
                    if (ch < 0x80) {
                        buf[count++] = (byte) ch;
                    } else if (ch < 0x800) {
                        buf[count++] = (byte) (0xC0 | (ch >> 6));
                        buf[count++] = (byte) (0x80 | (ch & 0x3F));
                    } else if (Character.isHighSurrogate(ch)) {
                        if (i + 1 < len && Character.isLowSurrogate(name.charAt(i + 1))) {
                            int cp = Character.toCodePoint(ch, name.charAt(++i));
                            buf[count++] = (byte) (0xF0 | (cp >> 18));
                            buf[count++] = (byte) (0x80 | ((cp >> 12) & 0x3F));
                            buf[count++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
                            buf[count++] = (byte) (0x80 | (cp & 0x3F));
                        } else {
                            buf[count++] = (byte) 0xEF;
                            buf[count++] = (byte) 0xBF;
                            buf[count++] = (byte) 0xBD;
                        }
                    } else if (Character.isLowSurrogate(ch)) {
                        // Lone low surrogate — emit U+FFFD (matches the
                        // lone-high branch above; the general 3-byte else
                        // would produce CESU-8 bytes that are invalid UTF-8
                        // per RFC 3629 §3).
                        buf[count++] = (byte) 0xEF;
                        buf[count++] = (byte) 0xBF;
                        buf[count++] = (byte) 0xBD;
                    } else {
                        buf[count++] = (byte) (0xE0 | (ch >> 12));
                        buf[count++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                        buf[count++] = (byte) (0x80 | (ch & 0x3F));
                    }
                }
                buf[count++] = quoteChar;
            }
            buf[count++] = ':';
            if (pretty) {
                buf[count++] = ' ';
            }
        }

        @Override
        public void writePreEncodedName(char[] nameChars, byte[] nameBytes) {
            int len = nameBytes.length;
            if (pretty) {
                if (count > 0 && buf[count - 1] == ',') {
                    count--;
                }
                if (count > 0 && buf[count - 1] != '{') {
                    ensureCapacity(len + 2);
                    buf[count++] = ',';
                } else {
                    ensureCapacity(len + 2);
                }
                writeNewlineIndent();
            } else {
                ensureCapacity(len + 1);
            }
            System.arraycopy(nameBytes, 0, buf, count, len);
            count += len;
            if (pretty) {
                buf[count++] = ' ';
            }
        }

        @Override
        public void writePreEncodedNameLongs(long[] nameByteLongs, int nameBytesLen, char[] nameChars, byte[] nameBytes) {
            if (pretty) {
                writePreEncodedName(nameChars, nameBytes);
                return;
            }
            if (nameByteLongs != null) {
                ensureCapacity(nameBytesLen);
                int pos = count;
                for (long v : nameByteLongs) {
                    JDKUtils.putLongDirect(buf, pos, v);
                    pos += 8;
                }
                count += nameBytesLen;
            } else {
                writePreEncodedName(nameChars, nameBytes);
            }
        }

        /**
         * Write long[] name bytes without ensureCapacity (caller must guarantee space).
         * Unrolled for common field name lengths (1-4 longs = up to 32 bytes).
         * Updates count field directly.
         */
        private void writeNameLongsNoCheck(long[] nameByteLongs, int nameBytesLen) {
            writeName0(nameByteLongs, nameBytesLen);
            count += nameBytesLen;
        }

        /**
         * Write name longs to buf at current count position without updating count.
         * Used by fused methods that manage count as a local variable.
         */
        private void writeName0(long[] nameByteLongs, int nameBytesLen) {
            int pos = count;
            switch (nameByteLongs.length) {
                case 1:
                    JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    break;
                case 2:
                    JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]);
                    break;
                case 3:
                    JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]);
                    JDKUtils.putLongDirect(buf, pos + 16, nameByteLongs[2]);
                    break;
                default:
                    for (int i = 0; i < nameByteLongs.length; i++) {
                        JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]);
                    }
                    break;
            }
        }

        // Small token writes — no capacity check (within SAFE_MARGIN)
        // Use Unsafe putInt/putLong for fewer store instructions

        // Pre-packed constants (little-endian x86): "null" = 0x6C6C756E, "true" = 0x65757274, etc.
        private static final int NULL_INT = 'n' | ('u' << 8) | ('l' << 16) | ('l' << 24);
        private static final int TRUE_INT = 't' | ('r' << 8) | ('u' << 16) | ('e' << 24);
        // "false" = 5 bytes: int "fals" + byte 'e'
        private static final int FALS_INT = 'f' | ('a' << 8) | ('l' << 16) | ('s' << 24);

        @Override
        public void writeNull() {
            int pos = count;
            if (JDKUtils.UNSAFE_AVAILABLE) {
                JDKUtils.putIntDirect(buf, pos, NULL_INT);
                pos += 4;
            } else {
                buf[pos++] = 'n';
                buf[pos++] = 'u';
                buf[pos++] = 'l';
                buf[pos++] = 'l';
            }
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeTrue() {
            int pos = count;
            if (JDKUtils.UNSAFE_AVAILABLE) {
                JDKUtils.putIntDirect(buf, pos, TRUE_INT);
                pos += 4;
            } else {
                buf[pos++] = 't';
                buf[pos++] = 'r';
                buf[pos++] = 'u';
                buf[pos++] = 'e';
            }
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeFalse() {
            int pos = count;
            if (JDKUtils.UNSAFE_AVAILABLE) {
                JDKUtils.putIntDirect(buf, pos, FALS_INT);
                pos += 4;
            } else {
                buf[pos++] = 'f';
                buf[pos++] = 'a';
                buf[pos++] = 'l';
                buf[pos++] = 's';
            }
            buf[pos++] = 'e';
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeBool(boolean val) {
            if (boolAsNumber) {
                writeInt32(val ? 1 : 0);
                return;
            }
            if (nonStringAsString) {
                writeString(Boolean.toString(val));
                return;
            }
            if (val) {
                writeTrue();
            } else {
                writeFalse();
            }
        }

        @Override
        public void writeInt32(int val) {
            if (nonStringAsString) {
                writeString(Integer.toString(val));
                return;
            }
            // max 11 digits, within SAFE_MARGIN
            count += writeIntToBytes(val, buf, count);
            buf[count++] = ',';
        }

        @Override
        public void writeInt64(long val) {
            if (longAsString || nonStringAsString) {
                writeString(Long.toString(val));
                return;
            }
            // max 20 digits, within SAFE_MARGIN
            count += writeLongToBytes(val, buf, count);
            buf[count++] = ',';
        }

        @Override
        public void writeFloat(float val) {
            if (nonStringAsString) {
                writeString(Float.toString(val));
                return;
            }
            if (Float.isNaN(val) || Float.isInfinite(val)) {
                if (floatSpecialAsString) {
                    writeString(Float.toString(val));
                } else {
                    writeNull();
                }
                return;
            }
            String s = Float.toString(val);
            writeNumericString(s);
        }

        @Override
        public void writeDouble(double val) {
            if (nonStringAsString) {
                writeString(Double.toString(val));
                return;
            }
            if (Double.isNaN(val) || Double.isInfinite(val)) {
                if (floatSpecialAsString) {
                    writeString(Double.toString(val));
                } else {
                    writeNull();
                }
                return;
            }
            count = com.alibaba.fastjson3.util.NumberUtils.writeDouble(buf, count, val, true, false);
            buf[count++] = ',';
        }

        private void writeNumericString(String s) {
            // Numeric strings are always Latin-1 and short (< 25 chars), within SAFE_MARGIN
            int coder = JDKUtils.getStringCoder(s);
            if (coder == 0) {
                byte[] value = (byte[]) JDKUtils.getStringValue(s);
                if (value != null) {
                    int len = value.length;
                    System.arraycopy(value, 0, buf, count, len);
                    count += len;
                    buf[count++] = ',';
                    return;
                }
            }
            int len = s.length();
            for (int i = 0; i < len; i++) {
                buf[count++] = (byte) s.charAt(i);
            }
            buf[count++] = ',';
        }

        @Override
        public void writeDecimal(BigDecimal val) {
            if (val == null) {
                writeNull();
                return;
            }
            String s = bigDecimalPlain ? val.toPlainString() : val.toString();
            if (nonStringAsString) {
                writeString(s);
                return;
            }
            writeNumericString(s);
        }

        // ---- Escape check using bit manipulation (wast-style) ----

        /**
         * Check if 8 bytes (as a long) contain no chars that need JSON escaping.
         * Checks: no byte < 0x20 (control), no '"' (0x22), no '\\' (0x5C).
         *
         * <p>The body is split into a tiny fast path (~6 ops, well below the JIT
         * {@code MaxInlineSize=35} threshold so it inlines into hot loops like
         * {@link #writeLatinStringStatic}) and a separate slow path for the rare
         * case where the fast check fails. Profile of EishayWriteUTF8Bytes shows
         * the fast path covers >99% of ASCII text — keeping it small lets JIT
         * inline it cleanly into the per-8-byte string-write loop.
         */
        public static boolean noEscape8(long v) {
            long hiMask = 0x8080808080808080L;
            // Check '\\' (0x5C): XOR with 0xA3 (complement) + add 0x01, high bit set if NOT '\\'
            long notBackslash = (v ^ 0xA3A3A3A3A3A3A3A3L) + 0x0101010101010101L & hiMask;
            // Fast path: all bytes > '"' (0x22) and not '\\' — covers >99% of ASCII text
            if ((notBackslash & v + 0x5D5D5D5D5D5D5D5DL) == hiMask) {
                return true;
            }
            return noEscape8Full(v, notBackslash, hiMask);
        }

        private static boolean noEscape8Full(long v, long notBackslash, long hiMask) {
            // Full check: control chars < 0x20, '"', '\\'
            long lo = 0x0101010101010101L;
            long ctrl = (v - 0x2020202020202020L) & ~v & hiMask;
            long xq = v ^ 0x2222222222222222L;
            long quote = (xq - lo) & ~xq & hiMask;
            return (ctrl | quote | (notBackslash ^ hiMask)) == 0;
        }

        /**
         * Write a Latin-1 string value with progressive-width escape checking.
         */
        private void writeLatinString(byte[] value, int len) {
            ensureCapacity(len + 3);
            writeLatinStringNoCapCheck(value, len);
        }

        /**
         * Check 4 bytes (as an int) for JSON escape characters.
         * Also rejects bytes >= 0x80 (Latin-1 high chars that need 2-byte UTF-8 output).
         */
        public static boolean noEscape4(int v) {
            int hiMask = 0x80808080;
            int lo = 0x01010101;
            int ctrl = (v - 0x20202020) & ~v & hiMask;
            int xq = v ^ 0x22222222;
            int quote = (xq - lo) & ~xq & hiMask;
            int xb = v ^ 0x5C5C5C5C;
            int bslash = (xb - lo) & ~xb & hiMask;
            int hi = v & hiMask; // bytes >= 0x80 require 2-byte UTF-8
            return (ctrl | quote | bslash | hi) == 0;
        }

        /**
         * Write a Latin-1 string value WITHOUT ensureCapacity (caller must guarantee space).
         * Check-and-copy in 8-byte chunks using putLong (single pass, no separate scan).
         */
        private void writeLatinStringNoCapCheck(byte[] value, int len) {
            int pos = count;
            buf[pos++] = '"';

            if (JDKUtils.UNSAFE_AVAILABLE && len > 0) {
                int i = 0;
                // Check-and-copy 8 bytes at a time
                for (; i + 7 < len; i += 8) {
                    long v = JDKUtils.getLongDirect(value, i);
                    if (noEscape8(v)) {
                        JDKUtils.putLongDirect(buf, pos, v);
                        pos += 8;
                    } else {
                        pos = writeEscapedBytes(value, i, len, pos);
                        buf[pos++] = '"';
                        buf[pos++] = ',';
                        count = pos;
                        return;
                    }
                }
                // 4-byte tail acceleration
                if (i + 3 < len) {
                    int iv = JDKUtils.getIntDirect(value, i);
                    if (noEscape4(iv)) {
                        JDKUtils.putIntDirect(buf, pos, iv);
                        pos += 4; i += 4;
                    }
                }
                // Tail loop: 0-7 bytes remain after 8-byte chunks (further reduced to 0-3 if 4-byte loop runs)
                for (; i < len; i++) {
                    byte b = value[i];
                    if (b >= 0x20 && b != '"' && b != '\\') {
                        buf[pos++] = b;
                    } else {
                        // Max 6 bytes output per char: control chars use \\uXXXX (6 bytes),
                        // Latin-1 high bytes (>= 0x80) use 2-byte UTF-8.
                        if (pos + 6 > buf.length) {
                            count = pos;
                            ensureCapacity((len - i) * 2 + 6);
                            pos = count;
                        }
                        pos = writeEscapedByte(b, pos);
                    }
                }
            } else {
                pos = writeEscapedBytes(value, 0, len, pos);
            }

            buf[pos++] = '"';
            buf[pos++] = ',';
            count = pos;
        }

        private int writeEscapedBytes(byte[] src, int from, int end, int pos) {
            for (int i = from; i < end; i++) {
                byte b = src[i];
                if (b >= 0x20 && b != '"' && b != '\\') {
                    buf[pos++] = b;
                } else {
                    // Escape expansion may need more space (up to 6 bytes per char)
                    if (pos + 6 > buf.length) {
                        count = pos;
                        ensureCapacity((end - i) * 6 + 6);
                        pos = count;
                    }
                    pos = writeEscapedByte(b, pos);
                }
            }
            return pos;
        }

        private int writeEscapedByte(byte b, int pos) {
            int ch = b & 0xFF;
            if (ch >= 128) {
                // Latin-1 char 0x80-0xFF: encode as 2-byte UTF-8
                hasNonAscii = true;
                buf[pos++] = (byte) (0xC0 | (ch >> 6));
                buf[pos++] = (byte) (0x80 | (ch & 0x3F));
                return pos;
            }
            char escaped = ESCAPE_CHARS[ch];
            if (escaped != 0 && ch != '/') {
                buf[pos++] = '\\';
                buf[pos++] = (byte) escaped;
            } else if (ch < 0x20) {
                buf[pos++] = '\\';
                buf[pos++] = 'u';
                buf[pos++] = '0';
                buf[pos++] = '0';
                buf[pos++] = (byte) DIGITS[ch >> 4];
                buf[pos++] = (byte) DIGITS[ch & 0xF];
            } else {
                buf[pos++] = b;
            }
            return pos;
        }

        @Override
        public void writeString(String val) {
            if (val == null) {
                writeNull();
                return;
            }

            if (JDKUtils.getStringCoder(val) == 0 && escapeChars == ESCAPE_CHARS && !escapeNoneAscii) {
                byte[] value = (byte[]) JDKUtils.getStringValue(val);
                // Inline writeLatinString: eliminates 1 method call level for list string elements
                ensureCapacity(value.length + 3);
                writeLatinStringNoCapCheck(value, value.length);
                return;
            }

            // Non-Latin1 string — mark as having non-ASCII output
            hasNonAscii = true;
            // General path: char-by-char with escaping and UTF-8 encoding
            int len = val.length();
            // Initial estimate: 4 bytes per char covers most CJK + surrogate pairs.
            // Escape sequences (6 bytes) are rare; handled by inline capacity checks.
            ensureCapacity(len * 4 + 6);
            int pos = count;
            buf[pos++] = '"';
            for (int i = 0; i < len; i++) {
                // Ensure at least 6 bytes available for worst-case single char expansion
                if (pos + 6 >= buf.length) {
                    count = pos;
                    ensureCapacity(6 + (len - i) * 4);
                    buf = this.buf;
                    pos = count;
                }
                char ch = val.charAt(i);
                if (ch < 128) {
                    char escaped = escapeChars[ch];
                    if (escaped != 0 && ch != '/') {
                        if (escaped == '"' || escaped == '\\' || escaped == 'b' || escaped == 'f'
                                || escaped == 'n' || escaped == 'r' || escaped == 't') {
                            buf[pos++] = '\\';
                            buf[pos++] = (byte) escaped;
                        } else {
                            // Browser/secure unicode escape
                            buf[pos++] = '\\';
                            buf[pos++] = 'u';
                            buf[pos++] = '0';
                            buf[pos++] = '0';
                            buf[pos++] = (byte) DIGITS[ch >> 4];
                            buf[pos++] = (byte) DIGITS[ch & 0xF];
                        }
                    } else if (ch < 0x20) {
                        buf[pos++] = '\\';
                        buf[pos++] = 'u';
                        buf[pos++] = '0';
                        buf[pos++] = '0';
                        buf[pos++] = (byte) DIGITS[ch >> 4];
                        buf[pos++] = (byte) DIGITS[ch & 0xF];
                    } else {
                        buf[pos++] = (byte) ch;
                    }
                } else if (escapeNoneAscii) {
                    // Escape non-ASCII as unicode sequence
                    buf[pos++] = '\\';
                    buf[pos++] = 'u';
                    buf[pos++] = (byte) DIGITS[(ch >> 12) & 0xF];
                    buf[pos++] = (byte) DIGITS[(ch >> 8) & 0xF];
                    buf[pos++] = (byte) DIGITS[(ch >> 4) & 0xF];
                    buf[pos++] = (byte) DIGITS[ch & 0xF];
                } else if (ch < 0x800) {
                    buf[pos++] = (byte) (0xC0 | (ch >> 6));
                    buf[pos++] = (byte) (0x80 | (ch & 0x3F));
                } else if (Character.isHighSurrogate(ch)) {
                    // UTF-16 surrogate pair → 4-byte UTF-8
                    if (i + 1 < len && Character.isLowSurrogate(val.charAt(i + 1))) {
                        int cp = Character.toCodePoint(ch, val.charAt(++i));
                        buf[pos++] = (byte) (0xF0 | (cp >> 18));
                        buf[pos++] = (byte) (0x80 | ((cp >> 12) & 0x3F));
                        buf[pos++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
                        buf[pos++] = (byte) (0x80 | (cp & 0x3F));
                    } else {
                        // Lone high surrogate — write replacement char U+FFFD
                        buf[pos++] = (byte) 0xEF;
                        buf[pos++] = (byte) 0xBF;
                        buf[pos++] = (byte) 0xBD;
                    }
                } else if (Character.isLowSurrogate(ch)) {
                    // Lone low surrogate (any preceding high would have
                    // consumed it via i++). Pre-fix the general 3-byte
                    // else branch emitted CESU-8 bytes (ed bX XX) which
                    // are invalid UTF-8 per RFC 3629 §3. Match the lone-
                    // high branch's U+FFFD replacement.
                    buf[pos++] = (byte) 0xEF;
                    buf[pos++] = (byte) 0xBF;
                    buf[pos++] = (byte) 0xBD;
                } else {
                    buf[pos++] = (byte) (0xE0 | (ch >> 12));
                    buf[pos++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                    buf[pos++] = (byte) (0x80 | (ch & 0x3F));
                }
            }
            buf[pos++] = '"';
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeLocalDate(LocalDate val) {
            // "yyyy-MM-dd" = 12 bytes + comma
            ensureCapacity(13);
            int pos = count;
            buf[pos++] = '"';
            pos += com.alibaba.fastjson3.util.DateUtils.writeLocalDate(buf, pos, val);
            buf[pos++] = '"';
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeLocalDateTime(LocalDateTime val) {
            // "yyyy-MM-ddTHH:mm:ss.SSSSSSSSS" = max 32 bytes + comma
            ensureCapacity(33);
            int pos = count;
            buf[pos++] = '"';
            pos += com.alibaba.fastjson3.util.DateUtils.writeLocalDateTime(buf, pos, val);
            buf[pos++] = '"';
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeLocalTime(LocalTime val) {
            // "HH:mm:ss.SSSSSSSSS" = max 21 bytes + comma
            ensureCapacity(22);
            int pos = count;
            buf[pos++] = '"';
            pos += com.alibaba.fastjson3.util.DateUtils.writeLocalTime(buf, pos, val);
            int nanos = val.getNano();
            if (nanos != 0) {
                pos += com.alibaba.fastjson3.util.DateUtils.writeFractionNanos(buf, pos, nanos);
            }
            buf[pos++] = '"';
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeInstant(Instant val) {
            // "yyyy-MM-ddTHH:mm:ss.SSSSSSSSSZ" = max 34 bytes + comma
            ensureCapacity(35);
            int pos = count;
            buf[pos++] = '"';
            LocalDateTime ldt = LocalDateTime.ofInstant(val, java.time.ZoneOffset.UTC);
            pos += com.alibaba.fastjson3.util.DateUtils.writeLocalDateTime(buf, pos, ldt);
            buf[pos++] = 'Z';
            buf[pos++] = '"';
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameString(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, String value) {
            if (pretty || extendedEscape || escapeNoneAscii) {
                writePreEncodedName(nameChars, nameBytes);
                writeString(value);
                return;
            }
            // Fast path: Latin-1 string (covers >99% of cases on JDK 9+)
            if (JDKUtils.getStringCoder(value) == 0) {
                byte[] valBytes = (byte[]) JDKUtils.getStringValue(value);
                int valLen = valBytes.length;
                // Like wast: use actual length + safety margin, not worst-case len*6.
                ensureCapacity(nameBytesLen + valLen + 3);
                int pos = count;

                // Inline writeName0 for better performance
                if (nameByteLongs != null) {
                    switch (nameByteLongs.length) {
                        case 1 -> JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                        case 2 -> { JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]); JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]); }
                        default -> { for (int i = 0; i < nameByteLongs.length; i++) JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]); }
                    }
                    pos += nameBytesLen;
                } else {
                    System.arraycopy(nameBytes, 0, buf, pos, nameBytesLen);
                    pos += nameBytesLen;
                }

                // Inline writeLatinStringNoCapCheck for Latin-1 strings
                buf[pos++] = '"';
                if (JDKUtils.UNSAFE_AVAILABLE && valLen > 0) {
                    int i = 0;
                    for (; i + 7 < valLen; i += 8) {
                        long v = JDKUtils.getLongDirect(valBytes, i);
                        if (noEscape8(v)) {
                            JDKUtils.putLongDirect(buf, pos, v);
                            pos += 8;
                        } else {
                            pos = writeEscapedBytes(valBytes, i, valLen, pos);
                            buf[pos++] = '"';
                            buf[pos++] = ',';
                            count = pos;
                            return;
                        }
                    }
                    // 4-byte tail acceleration: handle 4+ remaining bytes with int ops
                    if (i + 3 < valLen) {
                        int iv = JDKUtils.getIntDirect(valBytes, i);
                        if (noEscape4(iv)) {
                            JDKUtils.putIntDirect(buf, pos, iv);
                            pos += 4; i += 4;
                        }
                    }
                    for (; i < valLen; i++) {
                        byte b = valBytes[i];
                        if (b >= 0x20 && b != '"' && b != '\\') {
                            buf[pos++] = b;
                        } else {
                            pos = writeEscapedByte(b, pos);
                        }
                    }
                } else if (valLen > 0) {
                    for (int i = 0; i < valLen; i++) {
                        byte b = valBytes[i];
                        if (b >= 0x20 && b != '"' && b != '\\') {
                            buf[pos++] = b;
                        } else {
                            pos = writeEscapedByte(b, pos);
                        }
                    }
                }
                buf[pos++] = '"';
                buf[pos++] = ',';
                count = pos;
                return;
            }

            // Fallback for UTF-16 strings
            if (nameByteLongs != null) {
                ensureCapacity(nameBytesLen);
                writeName0(nameByteLongs, nameBytesLen);
                count += nameBytesLen;
            } else {
                ensureCapacity(nameBytesLen);
                System.arraycopy(nameBytes, 0, buf, count, nameBytesLen);
                count += nameBytesLen;
            }
            writeString(value);
        }

        @Override
        public void writeNameInt32(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, int value) {
            if (pretty || nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeInt32(value);
                return;
            }
            ensureCapacity(nameBytesLen + 25);
            int pos = count;
            // Inline writeName0 for better performance
            if (nameByteLongs != null) {
                switch (nameByteLongs.length) {
                    case 1 -> JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    case 2 -> { JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]); JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]); }
                    default -> { for (int i = 0; i < nameByteLongs.length; i++) JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]); }
                }
                pos += nameBytesLen;
            } else {
                System.arraycopy(nameBytes, 0, buf, pos, nameBytesLen);
                pos += nameBytesLen;
            }
            pos += writeIntToBytes(value, buf, pos);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameInt64(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, long value) {
            if (pretty || longAsString || nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeInt64(value);
                return;
            }
            ensureCapacity(nameBytesLen + 25);
            int pos = count;
            // Inline writeName0 for better performance
            if (nameByteLongs != null) {
                switch (nameByteLongs.length) {
                    case 1 -> JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    case 2 -> { JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]); JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]); }
                    default -> { for (int i = 0; i < nameByteLongs.length; i++) JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]); }
                }
                pos += nameBytesLen;
            } else {
                System.arraycopy(nameBytes, 0, buf, pos, nameBytesLen);
                pos += nameBytesLen;
            }
            pos += writeLongToBytes(value, buf, pos);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameDouble(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, double value) {
            if (pretty || nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeDouble(value);
                return;
            }
            ensureCapacity(nameBytesLen + 25);
            int pos = count;
            // Inline writeName0 for better performance
            if (nameByteLongs != null) {
                switch (nameByteLongs.length) {
                    case 1 -> JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    case 2 -> { JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]); JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]); }
                    default -> { for (int i = 0; i < nameByteLongs.length; i++) JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]); }
                }
                pos += nameBytesLen;
            } else {
                System.arraycopy(nameBytes, 0, buf, pos, nameBytesLen);
                pos += nameBytesLen;
            }
            pos = com.alibaba.fastjson3.util.NumberUtils.writeDouble(buf, pos, value, true, false);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameBool(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, boolean value) {
            if (pretty || boolAsNumber || nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeBool(value);
                return;
            }
            ensureCapacity(nameBytesLen + 25);
            int pos = count;
            // Inline writeName0 for better performance
            if (nameByteLongs != null) {
                switch (nameByteLongs.length) {
                    case 1 -> JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    case 2 -> { JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]); JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]); }
                    default -> { for (int i = 0; i < nameByteLongs.length; i++) JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]); }
                }
                pos += nameBytesLen;
            } else {
                System.arraycopy(nameBytes, 0, buf, pos, nameBytesLen);
                pos += nameBytesLen;
            }
            if (JDKUtils.UNSAFE_AVAILABLE) {
                if (value) {
                    JDKUtils.putIntDirect(buf, pos, TRUE_INT);
                    pos += 4;
                } else {
                    JDKUtils.putIntDirect(buf, pos, FALS_INT);
                    pos += 4;
                    buf[pos++] = 'e';
                }
            } else {
                if (value) {
                    buf[pos++] = 't'; buf[pos++] = 'r'; buf[pos++] = 'u'; buf[pos++] = 'e';
                } else {
                    buf[pos++] = 'f'; buf[pos++] = 'a'; buf[pos++] = 'l'; buf[pos++] = 's'; buf[pos++] = 'e';
                }
            }
            buf[pos++] = ',';
            count = pos;
        }

        // ---- Compact (non-pretty) variants — called by ASM-generated code ----

        @Override
        public void writePreEncodedNameLongsCompact(long[] nameByteLongs, int nameBytesLen, char[] nameChars, byte[] nameBytes) {
            if (nameByteLongs != null) {
                int pos = count;
                for (long v : nameByteLongs) {
                    JDKUtils.putLongDirect(buf, pos, v);
                    pos += 8;
                }
                count += nameBytesLen;
            } else {
                int len = nameBytes.length;
                System.arraycopy(nameBytes, 0, buf, count, len);
                count += len;
            }
        }

        @Override
        public void writeNameStringCompact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, String value) {
            if (extendedEscape || escapeNoneAscii) {
                writePreEncodedName(nameChars, nameBytes);
                writeString(value);
                return;
            }
            if (JDKUtils.getStringCoder(value) == 0) {
                byte[] valBytes = (byte[]) JDKUtils.getStringValue(value);
                int valLen = valBytes.length;
                ensureCapacity(valLen + 3);
                if (nameByteLongs != null) {
                    writeName0(nameByteLongs, nameBytesLen);
                    count += nameBytesLen;
                } else {
                    System.arraycopy(nameBytes, 0, buf, count, nameBytesLen);
                    count += nameBytesLen;
                }
                writeLatinStringNoCapCheck(valBytes, valLen);
                return;
            }
            // Fallback for UTF-16 strings
            if (nameByteLongs != null) {
                writeName0(nameByteLongs, nameBytesLen);
                count += nameBytesLen;
            } else {
                System.arraycopy(nameBytes, 0, buf, count, nameBytesLen);
                count += nameBytesLen;
            }
            writeString(value);
        }

        // Per-length raw name writers (W#5) — see abstract declarations on
        // JSONGenerator base for the design rationale. These UTF8 overrides
        // are the fast path: one putLong per long arg, no branches. Callers
        // (ASM-generated writer code) are expected to have pre-reserved
        // capacity and to write the trailing comma via the subsequent value
        // method (writeInt32 / writeString / ...).

        @Override
        public void writeName1L(long nameLong0, int nameBytesLen) {
            int pos = count;
            JDKUtils.putLongDirect(buf, pos, nameLong0);
            count = pos + nameBytesLen;
        }

        @Override
        public void writeName2L(long nameLong0, long nameLong1, int nameBytesLen) {
            int pos = count;
            JDKUtils.putLongDirect(buf, pos, nameLong0);
            JDKUtils.putLongDirect(buf, pos + 8, nameLong1);
            count = pos + nameBytesLen;
        }

        @Override
        public void writeNameInt32Compact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, int value) {
            if (nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeInt32(value);
                return;
            }
            // Optimized: remove ensureCapacity (caller ensures capacity), inline writeName0
            int pos = count;
            if (nameByteLongs != null) {
                switch (nameByteLongs.length) {
                    case 1 -> JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    case 2 -> { JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]); JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]); }
                    default -> { for (int i = 0; i < nameByteLongs.length; i++) JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]); }
                }
                pos += nameBytesLen;
            } else {
                System.arraycopy(nameBytes, 0, buf, pos, nameBytesLen);
                pos += nameBytesLen;
            }
            pos += writeIntToBytes(value, buf, pos);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameInt64Compact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, long value) {
            if (longAsString || nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeInt64(value);
                return;
            }
            // Optimized: inline writeName0 for better performance
            int pos = count;
            if (nameByteLongs != null) {
                switch (nameByteLongs.length) {
                    case 1 -> JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    case 2 -> { JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]); JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]); }
                    default -> { for (int i = 0; i < nameByteLongs.length; i++) JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]); }
                }
                pos += nameBytesLen;
            } else {
                System.arraycopy(nameBytes, 0, buf, pos, nameBytesLen);
                pos += nameBytesLen;
            }
            pos += writeLongToBytes(value, buf, pos);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameDoubleCompact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, double value) {
            if (nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeDouble(value);
                return;
            }
            // Optimized: inline writeName0 for better performance
            int pos = count;
            if (nameByteLongs != null) {
                switch (nameByteLongs.length) {
                    case 1 -> JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    case 2 -> { JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]); JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]); }
                    default -> { for (int i = 0; i < nameByteLongs.length; i++) JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]); }
                }
                pos += nameBytesLen;
            } else {
                System.arraycopy(nameBytes, 0, buf, pos, nameBytesLen);
                pos += nameBytesLen;
            }
            pos = com.alibaba.fastjson3.util.NumberUtils.writeDouble(buf, pos, value, true, false);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameBoolCompact(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, boolean value) {
            if (boolAsNumber || nonStringAsString) {
                writePreEncodedName(nameChars, nameBytes);
                writeBool(value);
                return;
            }
            int pos;
            if (nameByteLongs != null) {
                writeName0(nameByteLongs, nameBytesLen);
                pos = count + nameBytesLen;
            } else {
                pos = count;
                System.arraycopy(nameBytes, 0, buf, pos, nameBytesLen);
                pos += nameBytesLen;
            }
            if (JDKUtils.UNSAFE_AVAILABLE) {
                if (value) {
                    JDKUtils.putIntDirect(buf, pos, TRUE_INT);
                    pos += 4;
                } else {
                    JDKUtils.putIntDirect(buf, pos, FALS_INT);
                    pos += 4;
                    buf[pos++] = 'e';
                }
            } else {
                if (value) {
                    buf[pos++] = 't'; buf[pos++] = 'r'; buf[pos++] = 'u'; buf[pos++] = 'e';
                } else {
                    buf[pos++] = 'f'; buf[pos++] = 'a'; buf[pos++] = 'l'; buf[pos++] = 's'; buf[pos++] = 'e';
                }
            }
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeRaw(String raw) {
            byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
            ensureCapacity(bytes.length);
            System.arraycopy(bytes, 0, buf, count, bytes.length);
            count += bytes.length;
        }

        @Override
        public void writeRawBytes(byte[] bytes) {
            ensureCapacity(count + bytes.length);
            System.arraycopy(bytes, 0, buf, count, bytes.length);
            count += bytes.length;
        }

        private int outputCount() {
            int c = count;
            if (c > 0 && buf[c - 1] == ',') {
                c--;
            }
            return c;
        }

        @Override
        public byte[] toByteArray() {
            int c = outputCount();
            byte[] result = new byte[c];
            System.arraycopy(buf, 0, result, 0, c);
            return result;
        }

        /** Tracks if any non-ASCII byte was written */
        boolean hasNonAscii;

        @Override
        public void resetForReuse() {
            super.resetForReuse();
            hasNonAscii = false;
        }

        /**
         * Create a Latin-1 String from the internal byte[] buffer (copies buffer,
         * since it is pooled and will be reused after close).
         * Only works if output is all ASCII (common case for JSON).
         * Returns null if non-ASCII bytes were written (caller should fall back).
         */
        public String toStringLatin1() {
            if (hasNonAscii) return null;
            if (!JDKUtils.FAST_STRING_CREATION) return null;
            int c = outputCount();
            return JDKUtils.createLatin1String(buf, 0, c);
        }

        @Override
        public String toString() {
            return new String(buf, 0, outputCount(), StandardCharsets.UTF_8);
        }

        @Override
        public void close() {
            if (pooled) {
                BufferPool.returnByteBuffer(buf);
                buf = null;
                pooled = false;
            }
        }

        // ---- Static write methods (JSONB.IO pattern): take byte[] buf + int pos, return new pos ----

        /**
         * Write pre-encoded field name bytes directly into buf at pos.
         * Uses nameByteLongs for bulk 8-byte writes when available.
         */
        /**
         * Write a pre-encoded field name token (e.g. {@code "id":}) into {@code buf} at
         * {@code pos}. The vast majority of MediaContent / JJB field names are 5-8 byte
         * tokens (length-1 in the long-encoded array), so the fast path is a tiny
         * single-{@code putLong} body that JIT inlines into every per-field write call
         * site. Longer names and the no-Unsafe fallback go through the out-of-line
         * {@link #writeNameStaticSlow} helper.
         *
         * <p>This mirrors the per-length specialization fastjson2 uses with
         * {@code writeName3Raw}/{@code writeName5Raw}/etc. — same idea (single {@code
         * putLong} for the common case) packed into one entry point so the
         * {@code writeOneFieldStatic} call sites don't need a per-typeTag dispatch.
         */
        public static int writeNameStatic(byte[] buf, int pos, long[] nameByteLongs, byte[] nameBytes, int nameBytesLen) {
            if (nameByteLongs != null && nameByteLongs.length == 1) {
                JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                return pos + nameBytesLen;
            }
            return writeNameStaticSlow(buf, pos, nameByteLongs, nameBytes, nameBytesLen);
        }

        private static int writeNameStaticSlow(byte[] buf, int pos, long[] nameByteLongs, byte[] nameBytes, int nameBytesLen) {
            if (nameByteLongs != null) {
                int len = nameByteLongs.length;
                if (len == 2) {
                    JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]);
                } else {
                    for (int i = 0; i < len; i++) {
                        JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]);
                    }
                }
                return pos + nameBytesLen;
            }
            System.arraycopy(nameBytes, 0, buf, pos, nameBytesLen);
            return pos + nameBytesLen;
        }

        /**
         * Write a string value with quotes and trailing comma.
         * Returns new pos, or -1 if non-Latin (caller should fall back to virtual path).
         */
        public static int writeStringStatic(byte[] buf, int pos, String value) {
            Object sv = com.alibaba.fastjson3.util.JDKUtils.getStringValue(value);
            if (sv instanceof byte[] bytes && bytes.length == value.length()) {
                return writeLatinStringStatic(buf, pos, bytes, bytes.length);
            }
            return -1; // non-Latin: signal fallback
        }

        /**
         * Write a Latin-1 string value with quotes and trailing comma, using static methods only.
         * Returns new pos after writing "value",
         */
        public static int writeLatinStringStatic(byte[] buf, int pos, byte[] value, int len) {
            buf[pos++] = '"';
            int i = 0;
            for (; i + 7 < len; i += 8) {
                long v = JDKUtils.getLongDirect(value, i);
                if (noEscape8(v)) {
                    JDKUtils.putLongDirect(buf, pos, v);
                    pos += 8;
                } else {
                    pos = writeEscapedBytesDirect(value, i, len, buf, pos);
                    buf[pos++] = '"';
                    buf[pos++] = ',';
                    return pos;
                }
            }
            // 4-byte tail acceleration
            if (i + 3 < len) {
                int iv = JDKUtils.getIntDirect(value, i);
                if (noEscape4(iv)) {
                    JDKUtils.putIntDirect(buf, pos, iv);
                    pos += 4; i += 4;
                }
            }
            for (; i < len; i++) {
                byte b = value[i];
                if (b >= 0x20 && b != '"' && b != '\\') {
                    buf[pos++] = b;
                } else {
                    pos = writeEscapedByteDirect(b, buf, pos);
                }
            }
            buf[pos++] = '"';
            buf[pos++] = ',';
            return pos;
        }

        /**
         * Write bytes with JSON escaping, using only static access to buf parameter.
         */
        public static int writeEscapedBytesDirect(byte[] src, int from, int end, byte[] buf, int pos) {
            for (int i = from; i < end; i++) {
                byte b = src[i];
                if (b >= 0x20 && b != '"' && b != '\\') {
                    buf[pos++] = b;
                } else {
                    pos = writeEscapedByteDirect(b, buf, pos);
                }
            }
            return pos;
        }

        /**
         * Write a single escaped byte, using only static access to buf parameter.
         */
        public static int writeEscapedByteDirect(byte b, byte[] buf, int pos) {
            int ch = b & 0xFF;
            if (ch >= 128) {
                // Latin-1 char 0x80-0xFF: encode as 2-byte UTF-8
                // Note: hasNonAscii is set by the caller (writeLatinStringStatic) if needed
                buf[pos++] = (byte) (0xC0 | (ch >> 6));
                buf[pos++] = (byte) (0x80 | (ch & 0x3F));
                return pos;
            }
            char escaped = ESCAPE_CHARS[ch];
            if (escaped != 0 && ch != '/') {
                buf[pos++] = '\\';
                buf[pos++] = (byte) escaped;
            } else if (ch < 0x20) {
                buf[pos++] = '\\';
                buf[pos++] = 'u';
                buf[pos++] = '0';
                buf[pos++] = '0';
                buf[pos++] = (byte) DIGITS[ch >> 4];
                buf[pos++] = (byte) DIGITS[ch & 0xF];
            } else {
                buf[pos++] = b;
            }
            return pos;
        }

        /**
         * Delegates to {@link com.alibaba.fastjson3.util.NumberUtils#writeInt32(byte[], int, long)}
         * which uses magic-multiplier division ({@code val * 1759218605L >> 44} for /10000) and
         * range-dispatched 3/4/8-byte-at-a-time helpers, avoiding the per-pair IDIV cost of the
         * previous {@code /100} loop. Returns the number of bytes written so callers can keep
         * their {@code pos += writeIntToBytes(...)} idiom.
         *
         * <p><b>Buffer slack contract</b>: the underlying {@code writeInt3} helper uses a 4-byte
         * {@code putInt} write for 1-3 digit values, which touches up to 3 bytes past the logical
         * end of the digit run (filling them with NUL bytes that are immediately overwritten by
         * subsequent writes or remain in buffer slack). Callers must therefore ensure {@code buf}
         * has at least <b>14 bytes</b> available from {@code pos} (max 11 digits for
         * {@code Integer.MIN_VALUE} + up to 3 trailing slack bytes). All internal callers in
         * {@link JSONGenerator} satisfy this via the {@code SAFE_MARGIN} guarantee in
         * {@code ensureCapacity}.
         */
        public static int writeIntToBytes(int val, byte[] buf, int pos) {
            assert pos >= 0 && buf.length - pos >= 14
                    : "writeIntToBytes requires >=14 bytes slack at pos " + pos
                            + " but buf.length=" + buf.length;
            return com.alibaba.fastjson3.util.NumberUtils.writeInt32(buf, pos, val) - pos;
        }

        /**
         * Delegates to {@link com.alibaba.fastjson3.util.NumberUtils#writeInt64(byte[], int, long)}
         * which uses {@code Math.multiplyHigh} for 64-bit /10000 and range-dispatched
         * 3/4/8-byte-at-a-time helpers. Returns the number of bytes written.
         *
         * <p><b>Buffer slack contract</b>: same as {@link #writeIntToBytes(int, byte[], int)} —
         * the underlying {@code writeInt3} helper may write up to 3 bytes past the digit run for
         * short tail values. Callers must ensure {@code buf} has at least <b>23 bytes</b>
         * available from {@code pos} (max 20 digits for {@code Long.MIN_VALUE} + 3 trailing slack
         * bytes).
         */
        public static int writeLongToBytes(long val, byte[] buf, int pos) {
            assert pos >= 0 && buf.length - pos >= 23
                    : "writeLongToBytes requires >=23 bytes slack at pos " + pos
                            + " but buf.length=" + buf.length;
            return com.alibaba.fastjson3.util.NumberUtils.writeInt64(buf, pos, val) - pos;
        }
    }
}

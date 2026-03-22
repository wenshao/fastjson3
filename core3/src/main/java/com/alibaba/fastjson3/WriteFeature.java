package com.alibaba.fastjson3;

/**
 * Features controlling JSON serialization behavior.
 * Each feature is a bit in a long bitmask for O(1) checking.
 */
public enum WriteFeature {
    /**
     * Use field-based access instead of getter/setter
     */
    FieldBased,

    /**
     * Pretty print JSON output
     */
    PrettyFormat,

    /**
     * Write null fields (by default nulls are omitted)
     */
    WriteNulls,

    /**
     * Write null list as empty array []
     */
    WriteNullListAsEmpty,

    /**
     * Write null string as empty string ""
     */
    WriteNullStringAsEmpty,

    /**
     * Write null number as 0
     */
    WriteNullNumberAsZero,

    /**
     * Write null boolean as false
     */
    WriteNullBooleanAsFalse,

    /**
     * Write enum using name() instead of ordinal
     */
    WriteEnumsUsingName,

    /**
     * Write enum using toString()
     */
    WriteEnumUsingToString,

    /**
     * Write class name for polymorphic types
     */
    WriteClassName,

    /**
     * Write Map keys sorted
     */
    SortMapEntriesByKeys,

    /**
     * Escape non-ASCII characters
     */
    EscapeNoneAscii,

    /**
     * Write BigDecimal as plain string (no scientific notation)
     */
    WriteBigDecimalAsPlain,

    /**
     * Write long as string (for JavaScript compatibility)
     */
    WriteLongAsString,

    /**
     * Write byte[] as Base64 encoded string
     */
    WriteByteArrayAsBase64,

    /**
     * Write bean as array (ordered fields)
     */
    BeanToArray,

    /**
     * Detect circular references
     */
    ReferenceDetection,

    /**
     * Browser compatible mode: escape &lt;, &gt;, (, )
     */
    BrowserCompatible,

    /**
     * Write non-string values as string
     */
    WriteNonStringValueAsString,

    /**
     * Optimize output for ASCII content
     */
    OptimizedForAscii,

    /** Skip fields with default values: 0/0L/0.0/0.0f/false for both primitive and boxed types */
    NotWriteDefaultValue,

    /** Skip empty collections/arrays */
    NotWriteEmptyArray,

    /** Write enum using ordinal() instead of name() */
    WriteEnumUsingOrdinal,

    /** Write boolean as 0/1 */
    WriteBooleanAsNumber,

    /** Strict browser escaping: escape &lt;, &gt;, (, ), &amp;, ' (combine with EscapeNoneAscii for U+2028/U+2029) */
    BrowserSecure,

    /** Shorthand: null numbers→0, null strings→"", null booleans→false, null lists→[] */
    NullAsDefaultValue,

    /** Pretty print with 2-space indent (default when PrettyFormat is set) */
    PrettyFormatWith2Space,

    /** Pretty print with 4-space indent (overrides 2-space) */
    PrettyFormatWith4Space,

    /** Use single quotes instead of double quotes for strings and names */
    UseSingleQuotes,

    /** Skip fields whose declared type does not implement Serializable (checked at writer creation time) */
    IgnoreNoneSerializable,

    /** Throw JSONException if any field type does not implement Serializable (checked at writer creation time) */
    ErrorOnNoneSerializable,

    /** Suppress exceptions thrown by getter methods — treat as null instead */
    IgnoreErrorGetter,

    /** Serialize Map.Entry / Pair types as {"key":..., "value":...} instead of nested structure */
    WritePairAsJavaBean,

    /** Convert non-String Map keys to String via toString() (default behavior, explicit opt-in for clarity) */
    WriteNonStringKeyAsString,

    /** Write float/double NaN and Infinity as string literals ("NaN", "Infinity") instead of null */
    WriteFloatSpecialAsString,

    /** When WriteClassName is on, skip @type for the root object (depth 0) */
    NotWriteRootClassName,

    /** When WriteClassName is on, skip @type for HashMap/ArrayList/LinkedHashMap */
    NotWriteHashMapArrayListClassName,

    /** Ignore getter methods that don't correspond to a declared field (e.g. getClass()) */
    IgnoreNonFieldGetter,

    /** Write java.util.Date as milliseconds timestamp instead of formatted string */
    WriteDateAsMillis,

    /** Write null values in Map entries (independent of WriteNulls which controls POJO fields) */
    WriteMapNullValue,

    /** When WriteClassName is on, skip @type for Set/HashSet/LinkedHashSet */
    NotWriteSetClassName,

    /** When WriteClassName is on, skip @type for Number subclasses (Integer, Long, etc.) */
    NotWriteNumberClassName,

    /** Write Throwable class name in serialization output (adds @type to exception JSON) */
    WriteThrowableClassName,

    /** Write field names without quotes (non-standard JSON, for JS eval compatibility) */
    UnquoteFieldName,

    /** Allow large object serialization (raises buffer limit from default to 1GB) */
    LargeObject;

    public final long mask;

    WriteFeature() {
        this.mask = 1L << ordinal();
    }

    public static long of(WriteFeature... features) {
        long flags = 0;
        for (WriteFeature f : features) {
            flags |= f.mask;
        }
        return flags;
    }

    /**
     * Convert a bitmask back to a WriteFeature array.
     * Used to pass features to JSONGenerator factory methods.
     */
    public static WriteFeature[] valuesFrom(long mask) {
        if (mask == 0) {
            return EMPTY;
        }
        WriteFeature[] all = values();
        int count = 0;
        for (WriteFeature f : all) {
            if ((mask & f.mask) != 0) {
                count++;
            }
        }
        WriteFeature[] result = new WriteFeature[count];
        int idx = 0;
        for (WriteFeature f : all) {
            if ((mask & f.mask) != 0) {
                result[idx++] = f;
            }
        }
        return result;
    }

    private static final WriteFeature[] EMPTY = {};
}

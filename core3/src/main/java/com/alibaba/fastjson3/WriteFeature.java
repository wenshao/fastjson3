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
     * Browser compatible mode (escape special chars)
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

    /** Skip fields with primitive default values (0, false, 0.0) */
    NotWriteDefaultValue,

    /** Skip empty collections/arrays */
    NotWriteEmptyArray,

    /** Write enum using ordinal() instead of name() */
    WriteEnumUsingOrdinal,

    /** Write boolean as 0/1 */
    WriteBooleanAsNumber,

    /** Strict browser escaping: escape &lt;, &gt;, &amp;, ' (combine with EscapeNoneAscii for \u2028/\u2029) */
    BrowserSecure,

    /** Shorthand: null numbers→0, null strings→"", null booleans→false, null lists→[] */
    NullAsDefaultValue,

    /** Swallow getter exceptions instead of throwing (not yet implemented) */
    IgnoreErrorGetter,

    /** Skip getters without backing field (not yet implemented) */
    IgnoreNonFieldGetter,

    /** Map non-string keys → String.valueOf() (not yet implemented) */
    WriteNonStringKeyAsString;

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

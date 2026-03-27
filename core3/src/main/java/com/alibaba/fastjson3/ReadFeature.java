package com.alibaba.fastjson3;

/**
 * Features controlling JSON deserialization behavior.
 * Each feature is a bit in a long bitmask for O(1) checking.
 */
public enum ReadFeature {
    /**
     * Use field-based access instead of getter/setter
     */
    FieldBased,

    /**
     * Allow single quotes in JSON strings
     */
    AllowSingleQuotes,

    /**
     * Allow unquoted field names
     */
    AllowUnquotedFieldNames,

    /**
     * Allow JSON comments (// and /* *​/)
     */
    AllowComments,

    /**
     * Use BigDecimal for floating point numbers
     */
    UseBigDecimalForFloats,

    /**
     * Use BigDecimal for double values
     */
    UseBigDecimalForDoubles,

    /**
     * Trim whitespace from string values
     */
    TrimString,

    /**
     * Throw on unknown properties during deserialization
     */
    ErrorOnUnknownProperties,

    /**
     * Throw when null value for primitive type
     */
    ErrorOnNullForPrimitives,

    /**
     * Support smart match (case-insensitive, underscore-insensitive)
     */
    SupportSmartMatch,

    /**
     * Support auto type detection for polymorphism.
     *
     * <p><strong>Security warning:</strong> This feature is a placeholder and is NOT implemented
     * in fastjson3. Enabling it has no effect. Use sealed classes with {@code @JSONType(seeAlso=...)}
     * or Jackson {@code @JsonTypeInfo/@JsonSubTypes} for safe polymorphic deserialization instead.
     * Do NOT implement arbitrary {@code @type} class loading — it enables Remote Code Execution.</p>
     *
     * @deprecated Not implemented. Use sealed classes or {@code @JSONType(seeAlso)} instead.
     */
    @Deprecated
    SupportAutoType,

    /**
     * Initialize string fields as empty string instead of null
     */
    InitStringFieldAsEmpty,

    /**
     * Return null on parsing error instead of throwing
     */
    NullOnError,

    /**
     * Support mapping JSON array to Java bean
     */
    SupportArrayToBean,

    /**
     * Treat empty string as null
     */
    EmptyStringAsNull,

    /**
     * Treat duplicate keys as array values
     */
    DuplicateKeyValueAsArray,

    /**
     * Base64 encoded string as byte array
     */
    Base64StringAsByteArray,

    /** Throw when enum value doesn't match any constant */
    ErrorOnEnumNotMatch,

    /** Ignore null values — don't set property when JSON value is null */
    IgnoreSetNullValue,

    /** Use HashMap/ArrayList instead of JSONObject/JSONArray for untyped parsing */
    UseNativeObject,

    /** Cast non-zero numbers to boolean true (0→false, others→true) */
    NonZeroNumberCastToBooleanAsTrue,

    /** Disable reference detection ($ref handling) */
    DisableReferenceDetect,

    /** Use Long for integer values (instead of Integer) */
    UseLongForInts,

    /** Don't throw on number overflow, truncate silently */
    NonErrorOnNumberOverflow,

    /**
     * Support Class.forName() for type resolution.
     *
     * <p><strong>Security warning:</strong> This feature is a placeholder and is NOT implemented
     * in fastjson3. Enabling it has no effect. Arbitrary Class.forName() from JSON input
     * enables Remote Code Execution via gadget chains.</p>
     *
     * @deprecated Not implemented. Do not use.
     */
    @Deprecated
    SupportClassForName,

    /** Prefer no-arg constructor when available */
    UseDefaultConstructorAsPossible,

    /** Throw when AutoType encounters an unsupported type */
    ErrorOnNotSupportAutoType,

    /** Silently ignore AutoType type mismatch instead of throwing */
    IgnoreAutoTypeNotMatch,

    /** Ignore null property values — don't call setter for null */
    IgnoreNullPropertyValue,

    /** Skip resource close check (performance optimization) */
    IgnoreCheckClose,

    /** Use BigInteger for integer values */
    UseBigIntegerForInts,

    /** Use double for decimal values (instead of BigDecimal) */
    UseDoubleForDecimals,

    /** Disable single-quote string support */
    DisableSingleQuote,

    /** Disable unwrapping single-element string arrays */
    DisableStringArrayUnwrapping,

    /** Ignore non-Serializable types during deserialization */
    IgnoreNoneSerializable,

    /** Throw on non-Serializable types during deserialization */
    ErrorOnNoneSerializable,

    /** Convert non-String Map keys to String */
    NonStringKeyAsString;

    public final long mask;

    ReadFeature() {
        this.mask = 1L << ordinal();
    }

    public static long of(ReadFeature... features) {
        long flags = 0;
        for (ReadFeature f : features) {
            flags |= f.mask;
        }
        return flags;
    }

    /**
     * Convert a bitmask back to a ReadFeature array.
     * Used to pass features to JSONParser factory methods.
     *
     * @param mask the feature mask
     * @return array of enabled features
     */
    public static ReadFeature[] valuesFrom(long mask) {
        if (mask == 0) {
            return EMPTY;
        }
        ReadFeature[] all = values();
        // Count only valid features (ignore invalid mask bits to avoid null elements)
        int count = 0;
        for (ReadFeature f : all) {
            if ((mask & f.mask) != 0) {
                count++;
            }
        }
        ReadFeature[] result = new ReadFeature[count];
        int idx = 0;
        for (ReadFeature f : all) {
            if ((mask & f.mask) != 0) {
                result[idx++] = f;
            }
        }
        return result;
    }

    private static final ReadFeature[] EMPTY = {};
}

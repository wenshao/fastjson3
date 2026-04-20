package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.schema.JSONSchema;
import com.alibaba.fastjson3.util.JDKUtils;
import com.alibaba.fastjson3.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a single field to read during JSON deserialization.
 * Holds metadata (name, type, ordinal) and a reference to the underlying
 * {@link Field} or setter {@link Method} used to inject the value.
 */
public final class FieldReader implements Comparable<FieldReader> {
    public final String fieldName;
    public final String[] alternateNames;
    public final Type fieldType;
    public final Class<?> fieldClass;
    public final int ordinal;
    public final String defaultValue;
    public final boolean required;

    // Element class for List<X>/Set<X> fields (erasure of elementType).
    // Kept for fast-path dispatch (e.g., elementClass == String.class).
    public final Class<?> elementClass;

    // Full element type for Collection<E> fields. May be a Class, ParameterizedType,
    // or (normalized) WildcardType upper bound. Null for non-collection fields.
    public final Type elementType;

    // Map<K, V> field key/value types. Null for non-Map fields.
    public final Type keyType;
    public final Type valueType;
    public final Class<?> valueClass;

    // Exactly one of these is non-null
    private final Field field;
    private final Method setter;

    // Unsafe field offset for direct field injection (-1 if unavailable)
    public final long fieldOffset;

    // Pre-encoded '"fieldName":' as bytes for fast ordered matching
    public final byte[] fieldNameHeader;

    // Pre-computed long words for fast header comparison (avoids byte-by-byte loop)
    public final long hdrWord0;     // first 8 bytes as long (masked for short headers)
    public final long hdrMask0;     // mask for first word comparison
    public final long hdrWord1;     // bytes 8-15 as long (for headers > 8 bytes)
    public final long hdrMask1;     // mask for second word comparison

    // Optional: date/time format pattern + cached formatter
    public final String format;
    public final java.time.format.DateTimeFormatter formatter;

    // Optional: custom ObjectReader class (from @JSONField(deserializeUsing=))
    public final Class<?> deserializeUsingClass;

    // Optional: JSON Schema for validation during deserialization (from @JSONField(schema=))
    public final JSONSchema jsonSchema;

    // Cached enum constants for enum fields (null for non-enum fields)
    public final Object[] enumConstants;

    // Inverse map for enums whose declaring class has a @JSONField(value=true) / @JsonValue
    // accessor. Maps the value-method's return (String/Integer/...) back to the enum constant.
    // null when the enum has no value method or the field is not an enum.
    public final java.util.Map<Object, Object> enumValueMap;

    // Optional: field-level deserialization features (from @JSONField(deserializeFeatures=))
    public final long fieldFeatures;

    // Index in the fieldReaders array (set after construction)
    public int index = -1;

    // Type tag for fast dispatch
    public static final int TAG_STRING = 1;
    public static final int TAG_INT = 2;
    public static final int TAG_LONG = 3;
    public static final int TAG_DOUBLE = 4;
    public static final int TAG_BOOLEAN = 5;
    public static final int TAG_FLOAT = 6;
    public static final int TAG_INT_OBJ = 7;
    public static final int TAG_LONG_OBJ = 8;
    public static final int TAG_DOUBLE_OBJ = 9;
    public static final int TAG_BOOLEAN_OBJ = 10;
    public static final int TAG_LIST = 11;
    public static final int TAG_POJO = 12;
    public static final int TAG_STRING_ARRAY = 13;
    public static final int TAG_LONG_ARRAY = 14;
    public static final int TAG_ENUM = 15;
    public static final int TAG_MAP = 16;
    public static final int TAG_SET = 17;
    public static final int TAG_GENERIC = 0;

    public int typeTag;

    public FieldReader(
            String fieldName,
            String[] alternateNames,
            Type fieldType,
            Class<?> fieldClass,
            int ordinal,
            String defaultValue,
            boolean required,
            Field field,
            Method setter
    ) {
        this(fieldName, alternateNames, fieldType, fieldClass, ordinal, defaultValue,
                required, field, setter, null, null, null);
    }

    public FieldReader(
            String fieldName,
            String[] alternateNames,
            Type fieldType,
            Class<?> fieldClass,
            int ordinal,
            String defaultValue,
            boolean required,
            Field field,
            Method setter,
            String format,
            Class<?> deserializeUsingClass
    ) {
        this(fieldName, alternateNames, fieldType, fieldClass, ordinal, defaultValue,
                required, field, setter, format, deserializeUsingClass, null);
    }

    public FieldReader(
            String fieldName,
            String[] alternateNames,
            Type fieldType,
            Class<?> fieldClass,
            int ordinal,
            String defaultValue,
            boolean required,
            Field field,
            Method setter,
            String format,
            Class<?> deserializeUsingClass,
            String schema
    ) {
        this(fieldName, alternateNames, fieldType, fieldClass, ordinal, defaultValue,
                required, field, setter, format, deserializeUsingClass, schema, 0);
    }

    public FieldReader(
            String fieldName,
            String[] alternateNames,
            Type fieldType,
            Class<?> fieldClass,
            int ordinal,
            String defaultValue,
            boolean required,
            Field field,
            Method setter,
            String format,
            Class<?> deserializeUsingClass,
            String schema,
            long fieldFeatures
    ) {
        this(fieldName, alternateNames, fieldType, fieldClass, ordinal, defaultValue,
                required, field, setter, format, deserializeUsingClass, schema, fieldFeatures, false);
    }

    public FieldReader(
            String fieldName,
            String[] alternateNames,
            Type fieldType,
            Class<?> fieldClass,
            int ordinal,
            String defaultValue,
            boolean required,
            Field field,
            Method setter,
            String format,
            Class<?> deserializeUsingClass,
            String schema,
            long fieldFeatures,
            boolean useJacksonAnnotation
    ) {
        this.fieldName = fieldName;
        this.alternateNames = alternateNames != null ? alternateNames : new String[0];
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.ordinal = ordinal;
        this.defaultValue = defaultValue;
        this.required = required;
        this.field = field;
        this.setter = setter;
        this.format = format;
        this.formatter = (format != null && !format.isEmpty())
                ? java.time.format.DateTimeFormatter.ofPattern(format) : null;
        this.deserializeUsingClass = deserializeUsingClass;

        // Parse JSON Schema for validation
        JSONSchema parsedSchema = null;
        if (schema != null && !schema.isEmpty()) {
            try {
                JSONObject schemaObj = JSON.parseObject(schema);
                if (schemaObj != null && !schemaObj.isEmpty()) {
                    parsedSchema = JSONSchema.of(schemaObj, fieldClass);
                }
            } catch (Exception e) {
                // Invalid schema, disable validation
                com.alibaba.fastjson3.util.Logger.warn("Invalid JSON schema for field '" + fieldName + "': " + e.getMessage());
            }
        }
        this.jsonSchema = parsedSchema;

        if (field != null) {
            field.setAccessible(true);
        }
        if (setter != null) {
            setter.setAccessible(true);
        }

        // Extract generic parameters for collection / map fields. A length-1
        // array carries {E} for List<E>/Set<E>; length-2 carries {K, V} for Map.
        Type[] genericParams = resolveGenericParams(fieldType, fieldClass);
        if (genericParams != null && genericParams.length == 2) {
            this.elementType = null;
            this.elementClass = null;
            this.keyType = genericParams[0];
            this.valueType = genericParams[1];
            this.valueClass = TypeUtils.getRawClass(genericParams[1]);
        } else if (genericParams != null && genericParams.length == 1) {
            Type et = genericParams[0];
            this.elementType = et;
            this.elementClass = (et == Object.class) ? null : TypeUtils.getRawClass(et);
            this.keyType = null;
            this.valueType = null;
            this.valueClass = null;
        } else {
            this.elementType = null;
            this.elementClass = null;
            this.keyType = null;
            this.valueType = null;
            this.valueClass = null;
        }

        // Cache enum constants for enum fields
        this.enumConstants = fieldClass.isEnum() ? fieldClass.getEnumConstants() : null;
        this.enumValueMap = fieldClass.isEnum() ? buildEnumValueMap(fieldClass, useJacksonAnnotation) : null;
        this.fieldFeatures = fieldFeatures;

        // Resolve Unsafe field offset (look up corresponding field if we only have a setter)
        this.fieldOffset = resolveFieldOffset(field, setter, fieldName, fieldClass);

        // Pre-compute type tag. Resolves against generic info so that fields
        // like Set<E> / Map<K,V> take the dedicated fast path when the element
        // or value type is known.
        this.typeTag = resolveTypeTag(fieldClass, this.elementType, this.valueType);

        // Pre-encode '"fieldName":' as UTF-8 bytes for fast ordered matching
        {
            byte[] nameBytes = fieldName.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] h = new byte[nameBytes.length + 3];
            h[0] = '"';
            System.arraycopy(nameBytes, 0, h, 1, nameBytes.length);
            h[nameBytes.length + 1] = '"';
            h[nameBytes.length + 2] = ':';
            this.fieldNameHeader = h;
        }
        byte[] hdr = this.fieldNameHeader;

        // Pre-compute long words for fast header comparison
        int hdrLen = hdr.length;
        byte[] padded = new byte[16];
        System.arraycopy(hdr, 0, padded, 0, Math.min(hdrLen, 16));
        this.hdrWord0 = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(padded, 0);
        this.hdrMask0 = (hdrLen >= 8) ? -1L : (1L << (hdrLen * 8)) - 1;
        if (hdrLen > 8) {
            this.hdrWord1 = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(padded, 8);
            this.hdrMask1 = (hdrLen - 8 >= 8) ? -1L : (1L << ((hdrLen - 8) * 8)) - 1;
        } else {
            this.hdrWord1 = 0;
            this.hdrMask1 = 0;
        }
    }

    private static int resolveTypeTag(Class<?> fc, Type elementType, Type valueType) {
        if (fc == String.class) {
            return TAG_STRING;
        } else if (fc == int.class) {
            return TAG_INT;
        } else if (fc == long.class) {
            return TAG_LONG;
        } else if (fc == double.class) {
            return TAG_DOUBLE;
        } else if (fc == boolean.class) {
            return TAG_BOOLEAN;
        } else if (fc == float.class) {
            return TAG_FLOAT;
        } else if (fc == Integer.class) {
            return TAG_INT_OBJ;
        } else if (fc == Long.class) {
            return TAG_LONG_OBJ;
        } else if (fc == Double.class) {
            return TAG_DOUBLE_OBJ;
        } else if (fc == Boolean.class) {
            return TAG_BOOLEAN_OBJ;
        } else if (List.class.isAssignableFrom(fc) && fc.isAssignableFrom(java.util.ArrayList.class)) {
            return TAG_LIST;
        } else if (fc == String[].class) {
            return TAG_STRING_ARRAY;
        } else if (fc == long[].class) {
            return TAG_LONG_ARRAY;
        } else if (fc.isEnum()) {
            return TAG_ENUM;
        } else if (elementType != null && Set.class.isAssignableFrom(fc)
                && fc.isAssignableFrom(java.util.LinkedHashSet.class)) {
            return TAG_SET;
        } else if (valueType != null && Map.class.isAssignableFrom(fc)
                && fc.isAssignableFrom(java.util.LinkedHashMap.class)) {
            return TAG_MAP;
        }
        return TAG_GENERIC;
    }

    /**
     * Extract generic type parameters for Collection/Map field types.
     * Returns {@code null} for non-parameterized or unsupported fields, a
     * single-element {@code [E]} for {@code List<E>}/{@code Set<E>}, or a
     * two-element {@code [K, V]} for {@code Map<K, V>}. Callers branch on the
     * array length to distinguish.
     */
    private static Type[] resolveGenericParams(Type fieldType, Class<?> fieldClass) {
        if (!(fieldType instanceof ParameterizedType pt)) {
            return null;
        }
        Type[] args = pt.getActualTypeArguments();
        if (args.length == 1 && Collection.class.isAssignableFrom(fieldClass)) {
            return new Type[] {normalizeGenericArg(args[0])};
        }
        if (args.length == 2 && Map.class.isAssignableFrom(fieldClass)) {
            return new Type[] {normalizeGenericArg(args[0]), normalizeGenericArg(args[1])};
        }
        return null;
    }

    /**
     * Normalize a generic type argument for use as an element/key/value type:
     * collapse {@code ? extends T} to its upper bound, {@code ?} to {@link Object},
     * and unresolved {@link java.lang.reflect.TypeVariable} to {@link Object}
     * (the declaring class will have been supplied upstream; an unresolved
     * variable here means the field was created without a binding context).
     */
    private static Type normalizeGenericArg(Type t) {
        if (t instanceof java.lang.reflect.WildcardType wt) {
            Type[] upper = wt.getUpperBounds();
            return upper.length > 0 ? normalizeGenericArg(upper[0]) : Object.class;
        }
        if (t instanceof java.lang.reflect.TypeVariable<?>) {
            return Object.class;
        }
        return t;
    }

    private static long resolveFieldOffset(Field field, Method setter, String fieldName, Class<?> declaringClassUnused) {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return -1;
        }
        if (field != null) {
            return JDKUtils.objectFieldOffset(field);
        }
        // For setter-based fields, find the corresponding instance field
        if (setter != null) {
            Class<?> declaringClass = setter.getDeclaringClass();
            Class<?> current = declaringClass;
            while (current != null && current != Object.class) {
                try {
                    Field f = current.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    return JDKUtils.objectFieldOffset(f);
                } catch (NoSuchFieldException ignored) {
                }
                current = current.getSuperclass();
            }
        }
        return -1;
    }

    /**
     * Set the deserialized value onto the target bean instance.
     */
    public void setFieldValue(Object bean, Object value) {
        if (jsonSchema != null && value != null) {
            jsonSchema.assertValidate(value);
        }
        if (fieldOffset >= 0) {
            if (fieldClass == int.class) {
                JDKUtils.putInt(bean, fieldOffset, ((Number) value).intValue());
                return;
            } else if (fieldClass == long.class) {
                JDKUtils.putLongField(bean, fieldOffset, ((Number) value).longValue());
                return;
            } else if (fieldClass == boolean.class) {
                JDKUtils.putBoolean(bean, fieldOffset, (Boolean) value);
                return;
            } else if (fieldClass == double.class) {
                JDKUtils.putDouble(bean, fieldOffset, ((Number) value).doubleValue());
                return;
            } else if (fieldClass == float.class) {
                JDKUtils.putFloat(bean, fieldOffset, ((Number) value).floatValue());
                return;
            } else if (!fieldClass.isPrimitive()) {
                JDKUtils.putObject(bean, fieldOffset, value);
                return;
            }
            // short, byte primitives: fall through to reflection
        }
        try {
            if (setter != null) {
                setter.invoke(bean, value);
            } else {
                field.set(bean, value);
            }
        } catch (Exception e) {
            throw new JSONException("error setting field '" + fieldName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Set a reference-type value directly via Unsafe, skipping primitive type checks.
     */
    public void setObjectValue(Object bean, Object value) {
        if (enumConstants != null && value != null && !fieldClass.isInstance(value)) {
            // String name from JSON → enum constant (or, when the enum has a
            // @JSONField(value=true) method, a String/Number produced by that
            // method → enum constant via the inverse map).
            value = resolveEnumValue(value);
        }
        if (jsonSchema != null && value != null) {
            jsonSchema.assertValidate(value);
        }
        if (fieldOffset >= 0) {
            // Handle primitive types that don't have dedicated tags
            if (fieldClass == short.class) {
                // Fall through to reflection for short
            } else if (fieldClass == byte.class) {
                // Fall through to reflection for byte
            } else if (fieldClass == char.class) {
                // Fall through to reflection for char
            } else {
                JDKUtils.putObject(bean, fieldOffset, value);
                return;
            }
        }
        setFieldValue(bean, value);
    }

    public void setIntValue(Object bean, int value) {
        if (jsonSchema != null) {
            jsonSchema.assertValidate((long) value);
        }
        if (fieldOffset >= 0) {
            JDKUtils.putInt(bean, fieldOffset, value);
            return;
        }
        setFieldValue(bean, value);
    }

    public void setLongValue(Object bean, long value) {
        if (jsonSchema != null) {
            jsonSchema.assertValidate(value);
        }
        if (fieldOffset >= 0) {
            JDKUtils.putLongField(bean, fieldOffset, value);
            return;
        }
        setFieldValue(bean, value);
    }

    public void setDoubleValue(Object bean, double value) {
        if (jsonSchema != null) {
            jsonSchema.assertValidate(value);
        }
        if (fieldOffset >= 0) {
            JDKUtils.putDouble(bean, fieldOffset, value);
            return;
        }
        setFieldValue(bean, value);
    }

    public void setBooleanValue(Object bean, boolean value) {
        if (jsonSchema != null) {
            jsonSchema.assertValidate((Object) value);
        }
        if (fieldOffset >= 0) {
            JDKUtils.putBoolean(bean, fieldOffset, value);
            return;
        }
        setFieldValue(bean, value);
    }

    /**
     * Convert a raw JSON-parsed value to the type expected by this field.
     */
    public Object convertValue(Object value) {
        if (value == null) {
            return null;
        }

        // Use the resolved fieldType for the fast-path / Map→POJO check when it
        // is a concrete Class more specific than the erased fieldClass. This
        // happens for records / setters whose declared type is a TypeVariable
        // bound by a parameterized parent (e.g., {@code T payload} with
        // {@code RecordBox<Bean>}) — without this, an {@code Object.isInstance}
        // check would short-circuit and leave the value as a raw Map.
        Class<?> targetClass = (fieldType instanceof Class<?> fc && fc != fieldClass && fc != Object.class)
                ? fc : fieldClass;

        // Fast path: already the right type
        if (targetClass.isInstance(value)) {
            return value;
        }

        // Numeric narrowing / widening / temporal conversion
        if (value instanceof Number number) {
            if (fieldClass == int.class || fieldClass == Integer.class) {
                return number.intValue();
            }
            if (fieldClass == long.class || fieldClass == Long.class) {
                return number.longValue();
            }
            if (fieldClass == double.class || fieldClass == Double.class) {
                return number.doubleValue();
            }
            if (fieldClass == float.class || fieldClass == Float.class) {
                return number.floatValue();
            }
            if (fieldClass == short.class || fieldClass == Short.class) {
                return number.shortValue();
            }
            if (fieldClass == byte.class || fieldClass == Byte.class) {
                return number.byteValue();
            }
            if (fieldClass == AtomicInteger.class) {
                return new AtomicInteger(number.intValue());
            }
            if (fieldClass == AtomicLong.class) {
                return new AtomicLong(number.longValue());
            }
            if (fieldClass == java.math.BigDecimal.class) {
                return toBigDecimal(number);
            }
            if (fieldClass == java.math.BigInteger.class) {
                // Accept fractional sources (3.14 → 3) by routing through
                // BigDecimal, matching the typed-target parse path. An integer
                // Number is returned directly without lossy string conversion.
                if (number instanceof java.math.BigInteger bi) {
                    return bi;
                }
                if (number instanceof Integer || number instanceof Long
                        || number instanceof Short || number instanceof Byte
                        || number instanceof AtomicInteger || number instanceof AtomicLong) {
                    return java.math.BigInteger.valueOf(number.longValue());
                }
                return toBigDecimal(number).toBigInteger();
            }
            // Number → temporal type conversion (millis timestamp)
            long millis = number.longValue();
            if (fieldClass == Date.class) {
                return new Date(millis);
            }
            if (fieldClass == Instant.class) {
                return Instant.ofEpochMilli(millis);
            }
            if (fieldClass == LocalDateTime.class) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID);
            }
            if (fieldClass == LocalDate.class) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID).toLocalDate();
            }
            if (fieldClass == ZonedDateTime.class) {
                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID);
            }
            if (fieldClass == OffsetDateTime.class) {
                return OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID);
            }
        }

        // Boolean → AtomicBoolean conversion
        if (value instanceof Boolean b && fieldClass == AtomicBoolean.class) {
            return new AtomicBoolean(b);
        }

        // Any value → AtomicReference conversion
        if (fieldClass == AtomicReference.class) {
            return new AtomicReference<>(value);
        }

        // String → temporal type conversion
        // If format is specified, use DateTimeFormatter; otherwise use high-performance manual parsing
        if (value instanceof String str && !str.isEmpty()) {
            if (formatter != null) {
                return parseWithFormatter(str);
            }
            if (fieldClass == LocalDateTime.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseLocalDateTime(str);
            }
            if (fieldClass == LocalDate.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseLocalDate(str);
            }
            if (fieldClass == LocalTime.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseLocalTime(str);
            }
            if (fieldClass == Instant.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseInstant(str);
            }
            if (fieldClass == ZonedDateTime.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseZonedDateTime(str);
            }
            if (fieldClass == OffsetDateTime.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseOffsetDateTime(str);
            }
            if (fieldClass == Date.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseDate(str);
            }
            // String → JDK types (builtin codecs)
            if (fieldClass == UUID.class) {
                return UUID.fromString(str);
            }
            if (fieldClass == java.math.BigDecimal.class) {
                // Apply the same magnitude cap the parser uses on in-stream
                // literals — otherwise a 1 MB quoted-integer payload on a
                // BigDecimal field would produce a 1M-digit bignum (round 4's
                // cap was only on the scientific-notation branch).
                com.alibaba.fastjson3.JSONParser.checkBigNumberMagnitude(str);
                try {
                    return new java.math.BigDecimal(str);
                } catch (NumberFormatException e) {
                    throw new JSONException("invalid BigDecimal literal '" + str
                            + "' for field " + fieldName, e);
                }
            }
            if (fieldClass == java.math.BigInteger.class) {
                // Same cap as above applied BEFORE either branch — pure-digit
                // strings were unbounded in round 4.
                com.alibaba.fastjson3.JSONParser.checkBigNumberMagnitude(str);
                try {
                    if (str.indexOf('.') >= 0 || str.indexOf('e') >= 0 || str.indexOf('E') >= 0) {
                        return new java.math.BigDecimal(str).toBigInteger();
                    }
                    return new java.math.BigInteger(str);
                } catch (NumberFormatException | ArithmeticException e) {
                    throw new JSONException("invalid BigInteger literal '" + str
                            + "' for field " + fieldName, e);
                }
            }
            if (fieldClass == Duration.class) {
                return Duration.parse(str);
            }
            if (fieldClass == Period.class) {
                return Period.parse(str);
            }
            if (fieldClass == YearMonth.class) {
                return YearMonth.parse(str);
            }
            if (fieldClass == MonthDay.class) {
                return MonthDay.parse(str);
            }
            if (fieldClass == URI.class) {
                return URI.create(str);
            }
            if (Path.class.isAssignableFrom(fieldClass)) {
                return Path.of(str);
            }
        }

        // String/Number → Enum conversion
        if (enumConstants != null) {
            if (value instanceof String name) {
                if (enumValueMap != null) {
                    Object mapped = enumValueMap.get(name);
                    if (mapped != null) {
                        return mapped;
                    }
                }
                return resolveEnumValue(name);
            }
            if (value instanceof Number number) {
                // When the enum has a numeric @JSONField(value=true) accessor, try value-map first
                // (e.g. Priority.HIGH has getCode() == 1, so JSON 1 must map back to HIGH, not ordinal(1)).
                // Use a non-throwing probe so a value-map miss still falls through to the
                // ordinal fallback below — callers that historically sent enum ordinals
                // must keep working even when the enum grows a value-method.
                if (enumValueMap != null) {
                    Object mapped = enumValueMap.get(number);
                    if (mapped == null) {
                        // Cross-compat for mismatched numeric boxings (e.g. JSON arrives
                        // as Long, map key is Integer). Compare by exact numeric value via
                        // BigDecimal.compareTo so 1.1 and 1.9 do not both collapse to 1.
                        java.math.BigDecimal probe = toBigDecimal(number);
                        for (java.util.Map.Entry<Object, Object> e : enumValueMap.entrySet()) {
                            Object k = e.getKey();
                            if (k instanceof Number kn) {
                                java.math.BigDecimal kd = toBigDecimal(kn);
                                if (kd != null && probe != null && kd.compareTo(probe) == 0) {
                                    mapped = e.getValue();
                                    break;
                                }
                            }
                        }
                    }
                    if (mapped != null) {
                        return mapped;
                    }
                }
                int ordinal = number.intValue();
                if (ordinal >= 0 && ordinal < enumConstants.length) {
                    return enumConstants[ordinal];
                }
                throw new JSONException("no enum ordinal " + ordinal + " for " + fieldClass.getName());
            }
        }

        // Number → Year conversion
        if (value instanceof Number number && fieldClass == Year.class) {
            return Year.of(number.intValue());
        }

        // Wrap into Optional types
        if (fieldClass == Optional.class) {
            return Optional.ofNullable(value);
        }
        if (fieldClass == OptionalInt.class && value instanceof Number n) {
            return OptionalInt.of(n.intValue());
        }
        if (fieldClass == OptionalLong.class && value instanceof Number n) {
            return OptionalLong.of(n.longValue());
        }
        if (fieldClass == OptionalDouble.class && value instanceof Number n) {
            return OptionalDouble.of(n.doubleValue());
        }

        // Map → POJO/Record conversion (for nested objects parsed via readAny()).
        if (value instanceof java.util.Map<?, ?> map
                && !targetClass.isInterface()
                && !java.util.Map.class.isAssignableFrom(targetClass)
                && targetClass != Object.class) {
            String json = com.alibaba.fastjson3.JSON.toJSONString(value);
            return com.alibaba.fastjson3.JSON.parseObject(json, targetClass);
        }

        // No conversion found; return as-is
        return value;
    }

    /**
     * Resolve an enum constant from a JSON value. When the enum declares a
     * {@code @JSONField(value=true)} accessor, the value-map built at construction
     * is consulted first; otherwise falls back to {@code Enum.valueOf} (by name).
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object resolveEnumValue(Object value) {
        if (enumValueMap != null) {
            Object found = enumValueMap.get(value);
            if (found != null) {
                return found;
            }
            // Numeric cross-compat: JSON number arrives as Long/Integer/BigDecimal;
            // map keys may have been stored as the value-method's declared return type.
            // Compare by exact numeric value so 1.1 and 1.9 don't both match 1.
            if (value instanceof Number n) {
                java.math.BigDecimal probe = toBigDecimal(n);
                for (java.util.Map.Entry<Object, Object> e : enumValueMap.entrySet()) {
                    Object k = e.getKey();
                    if (k instanceof Number kn) {
                        java.math.BigDecimal kd = toBigDecimal(kn);
                        if (kd != null && probe != null && kd.compareTo(probe) == 0) {
                            return e.getValue();
                        }
                    }
                }
            }
        }
        if (value instanceof String name) {
            try {
                return Enum.valueOf((Class) fieldClass, name);
            } catch (IllegalArgumentException e) {
                throw new JSONException("no enum constant " + fieldClass.getName() + "." + name, e);
            }
        }
        throw new JSONException("cannot resolve enum " + fieldClass.getName() + " from value: " + value);
    }

    /**
     * Normalise any {@link Number} to a {@link java.math.BigDecimal} for exact value
     * comparison in the enum value-map cross-compat branches. Integer / Long / Short /
     * Byte keep integer precision; Double / Float convert through {@code toString()}
     * to avoid the binary-fraction drift {@code new BigDecimal(double)} introduces.
     */
    private static java.math.BigDecimal toBigDecimal(Number n) {
        if (n == null) {
            return null;
        }
        if (n instanceof java.math.BigDecimal bd) {
            return bd;
        }
        if (n instanceof java.math.BigInteger bi) {
            return new java.math.BigDecimal(bi);
        }
        if (n instanceof Integer || n instanceof Long || n instanceof Short || n instanceof Byte
                || n instanceof java.util.concurrent.atomic.AtomicInteger
                || n instanceof java.util.concurrent.atomic.AtomicLong) {
            return java.math.BigDecimal.valueOf(n.longValue());
        }
        if (n instanceof Double || n instanceof Float) {
            return new java.math.BigDecimal(n.toString());
        }
        // Fallback for uncommon Number subclasses — toString keeps textual precision.
        try {
            return new java.math.BigDecimal(n.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Inspect the enum class for a {@code @JSONField(value=true)} or Jackson {@code @JsonValue}
     * accessor. If present, invoke it on each enum constant and build the inverse lookup map.
     * Returns null when the enum has no value method.
     */
    private static java.util.Map<Object, Object> buildEnumValueMap(Class<?> enumClass, boolean useJacksonAnnotation) {
        java.lang.reflect.Method valueMethod = null;
        for (java.lang.reflect.Method m : enumClass.getMethods()) {
            if (m.getParameterCount() != 0 || java.lang.reflect.Modifier.isStatic(m.getModifiers())
                    || m.getDeclaringClass() == Object.class) {
                continue;
            }
            com.alibaba.fastjson3.annotation.JSONField jf = m.getAnnotation(
                    com.alibaba.fastjson3.annotation.JSONField.class);
            if (jf != null && jf.value()) {
                valueMethod = m;
                break;
            }
            // Jackson @JsonValue is respected only when the owning ObjectMapper has
            // Jackson-annotation support enabled, mirroring the writer's
            // findValueWriter(useJacksonAnnotation=true) opt-in so reader and writer
            // stay consistent on the same flag.
            if (useJacksonAnnotation) {
                try {
                    com.alibaba.fastjson3.annotation.JacksonAnnotationSupport.FieldInfo ji
                            = com.alibaba.fastjson3.annotation.JacksonAnnotationSupport.getFieldInfo(m.getAnnotations());
                    if (ji != null && ji.isValue()) {
                        valueMethod = m;
                        break;
                    }
                } catch (Throwable ignored) {
                    // Jackson annotations off the classpath — skip silently.
                }
            }
        }
        if (valueMethod == null) {
            return null;
        }
        try {
            valueMethod.setAccessible(true);
        } catch (Exception ignored) {
        }
        Object[] constants = enumClass.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return null;
        }
        java.util.Map<Object, Object> map = new java.util.HashMap<>(constants.length * 2);
        for (Object c : constants) {
            try {
                Object v = valueMethod.invoke(c);
                if (v != null) {
                    map.put(v, c);
                }
            } catch (Exception ignored) {
            }
        }
        return map.isEmpty() ? null : map;
    }

    private Object parseWithFormatter(String str) {
        if (fieldClass == LocalDateTime.class) {
            return LocalDateTime.parse(str, formatter);
        }
        if (fieldClass == LocalDate.class) {
            return LocalDate.parse(str, formatter);
        }
        if (fieldClass == LocalTime.class) {
            return LocalTime.parse(str, formatter);
        }
        if (fieldClass == ZonedDateTime.class) {
            return ZonedDateTime.parse(str, formatter);
        }
        if (fieldClass == OffsetDateTime.class) {
            return OffsetDateTime.parse(str, formatter);
        }
        if (fieldClass == Date.class) {
            var zdt = ZonedDateTime.parse(str, formatter.withZone(
                    com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID));
            return Date.from(zdt.toInstant());
        }
        if (fieldClass == Instant.class) {
            return Instant.from(formatter.withZone(
                    com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID).parse(str));
        }
        // Unsupported type with format — fall through to default parsing
        return str;
    }

    public int compareTo(FieldReader other) {
        int cmp = Integer.compare(this.ordinal, other.ordinal);
        if (cmp != 0) {
            return cmp;
        }
        return this.fieldName.compareTo(other.fieldName);
    }

    @Override
    public String toString() {
        return "FieldReader{" + fieldName + ", type=" + fieldClass.getSimpleName() + "}";
    }
}

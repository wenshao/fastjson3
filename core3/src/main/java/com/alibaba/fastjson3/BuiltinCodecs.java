package com.alibaba.fastjson3;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;

/**
 * Built-in ObjectReader/ObjectWriter implementations for JDK types
 * that are not primitive but commonly used.
 *
 * <p>These are resolved once per type on first access and cached in ObjectMapper's
 * ConcurrentHashMap. Zero hot-path overhead for unrelated types.</p>
 */
public final class BuiltinCodecs {
    private BuiltinCodecs() {
    }

    // ==================== ObjectReader factories ====================

    @SuppressWarnings("unchecked")
    public static <T> ObjectReader<T> getReader(Class<T> type) {
        if (type == String.class) {
            return (ObjectReader<T>) STRING_READER;
        }
        if (type == Optional.class) {
            return (ObjectReader<T>) OPTIONAL_READER;
        }
        if (type == OptionalInt.class) {
            return (ObjectReader<T>) OPTIONAL_INT_READER;
        }
        if (type == OptionalLong.class) {
            return (ObjectReader<T>) OPTIONAL_LONG_READER;
        }
        if (type == OptionalDouble.class) {
            return (ObjectReader<T>) OPTIONAL_DOUBLE_READER;
        }
        if (type == UUID.class) {
            return (ObjectReader<T>) UUID_READER;
        }
        if (type == Duration.class) {
            return (ObjectReader<T>) DURATION_READER;
        }
        if (type == Period.class) {
            return (ObjectReader<T>) PERIOD_READER;
        }
        if (type == Year.class) {
            return (ObjectReader<T>) YEAR_READER;
        }
        if (type == YearMonth.class) {
            return (ObjectReader<T>) YEAR_MONTH_READER;
        }
        if (type == MonthDay.class) {
            return (ObjectReader<T>) MONTH_DAY_READER;
        }
        if (type == URI.class) {
            return (ObjectReader<T>) URI_READER;
        }
        if (Path.class.isAssignableFrom(type)) {
            return (ObjectReader<T>) PATH_READER;
        }
        if (type == LocalDate.class) {
            return (ObjectReader<T>) LOCAL_DATE_READER;
        }
        if (type == LocalDateTime.class) {
            return (ObjectReader<T>) LOCAL_DATE_TIME_READER;
        }
        if (type == LocalTime.class) {
            return (ObjectReader<T>) LOCAL_TIME_READER;
        }
        if (type == Instant.class) {
            return (ObjectReader<T>) INSTANT_READER;
        }
        if (type == ZonedDateTime.class) {
            return (ObjectReader<T>) ZONED_DATE_TIME_READER;
        }
        if (type == OffsetDateTime.class) {
            return (ObjectReader<T>) OFFSET_DATE_TIME_READER;
        }
        if (type == OffsetTime.class) {
            return (ObjectReader<T>) OFFSET_TIME_READER;
        }
        if (type == Date.class) {
            return (ObjectReader<T>) DATE_READER;
        }
        if (type == AtomicInteger.class) {
            return (ObjectReader<T>) ATOMIC_INTEGER_READER;
        }
        if (type == AtomicLong.class) {
            return (ObjectReader<T>) ATOMIC_LONG_READER;
        }
        if (type == AtomicBoolean.class) {
            return (ObjectReader<T>) ATOMIC_BOOLEAN_READER;
        }
        if (type == Byte.class || type == byte.class) {
            return (ObjectReader<T>) BYTE_READER;
        }
        if (type == Short.class || type == short.class) {
            return (ObjectReader<T>) SHORT_READER;
        }
        if (type == Character.class || type == char.class) {
            return (ObjectReader<T>) CHARACTER_READER;
        }
        if (type == Integer.class || type == int.class) {
            return (ObjectReader<T>) INTEGER_READER;
        }
        if (type == Long.class || type == long.class) {
            return (ObjectReader<T>) LONG_READER;
        }
        if (type == Float.class || type == float.class) {
            return (ObjectReader<T>) FLOAT_READER;
        }
        if (type == Double.class || type == double.class) {
            return (ObjectReader<T>) DOUBLE_READER;
        }
        if (type == Boolean.class || type == boolean.class) {
            return (ObjectReader<T>) BOOLEAN_READER;
        }
        if (type == java.math.BigDecimal.class) {
            return (ObjectReader<T>) BIG_DECIMAL_READER;
        }
        if (type == java.math.BigInteger.class) {
            return (ObjectReader<T>) BIG_INTEGER_READER;
        }

        // Guava immutable collections (zero-dependency, reflection-based)
        ObjectReader<?> guavaReader = GuavaSupport.getReader(type);
        if (guavaReader != null) {
            return (ObjectReader<T>) guavaReader;
        }

        return null;
    }

    // ==================== ObjectWriter factories ====================

    @SuppressWarnings("unchecked")
    public static <T> ObjectWriter<T> getWriter(Class<T> type) {
        if (Optional.class.isAssignableFrom(type)) {
            return (ObjectWriter<T>) OPTIONAL_WRITER;
        }
        if (type == OptionalInt.class) {
            return (ObjectWriter<T>) OPTIONAL_INT_WRITER;
        }
        if (type == OptionalLong.class) {
            return (ObjectWriter<T>) OPTIONAL_LONG_WRITER;
        }
        if (type == OptionalDouble.class) {
            return (ObjectWriter<T>) OPTIONAL_DOUBLE_WRITER;
        }
        if (type == UUID.class) {
            return (ObjectWriter<T>) UUID_WRITER;
        }
        if (type == Duration.class) {
            return (ObjectWriter<T>) DURATION_WRITER;
        }
        if (type == Period.class) {
            return (ObjectWriter<T>) PERIOD_WRITER;
        }
        if (type == Year.class) {
            return (ObjectWriter<T>) YEAR_WRITER;
        }
        if (type == YearMonth.class) {
            return (ObjectWriter<T>) YEAR_MONTH_WRITER;
        }
        if (type == MonthDay.class) {
            return (ObjectWriter<T>) MONTH_DAY_WRITER;
        }
        if (type == URI.class) {
            return (ObjectWriter<T>) URI_WRITER;
        }
        if (Path.class.isAssignableFrom(type)) {
            return (ObjectWriter<T>) PATH_WRITER;
        }
        if (type == LocalDate.class) {
            return (ObjectWriter<T>) LOCAL_DATE_WRITER;
        }
        if (type == LocalDateTime.class) {
            return (ObjectWriter<T>) LOCAL_DATE_TIME_WRITER;
        }
        if (type == LocalTime.class) {
            return (ObjectWriter<T>) LOCAL_TIME_WRITER;
        }
        if (type == Instant.class) {
            return (ObjectWriter<T>) INSTANT_WRITER;
        }
        if (type == ZonedDateTime.class) {
            return (ObjectWriter<T>) ZONED_DATE_TIME_WRITER;
        }
        if (type == OffsetDateTime.class) {
            return (ObjectWriter<T>) OFFSET_DATE_TIME_WRITER;
        }
        if (type == OffsetTime.class) {
            return (ObjectWriter<T>) OFFSET_TIME_WRITER;
        }
        if (type == Date.class) {
            return (ObjectWriter<T>) DATE_WRITER;
        }
        if (type == AtomicInteger.class) {
            return (ObjectWriter<T>) ATOMIC_INTEGER_WRITER;
        }
        if (type == AtomicLong.class) {
            return (ObjectWriter<T>) ATOMIC_LONG_WRITER;
        }
        if (type == AtomicBoolean.class) {
            return (ObjectWriter<T>) ATOMIC_BOOLEAN_WRITER;
        }
        if (Throwable.class.isAssignableFrom(type)) {
            return (ObjectWriter<T>) THROWABLE_WRITER;
        }
        if (type == java.util.Currency.class) {
            return (ObjectWriter<T>) CURRENCY_WRITER;
        }
        if (type == java.util.Locale.class) {
            return (ObjectWriter<T>) LOCALE_WRITER;
        }
        if (type == java.util.TimeZone.class || java.util.TimeZone.class.isAssignableFrom(type)) {
            return (ObjectWriter<T>) TIMEZONE_WRITER;
        }
        return null;
    }

    // ==================== Optional ====================

    private static final ObjectReader<Optional<?>> OPTIONAL_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return Optional.empty();
                }
                // Resolve the element type T from Optional<T>
                Class<?> elemClass = null;
                if (fieldType instanceof ParameterizedType pt) {
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length == 1 && args[0] instanceof Class<?> c) {
                        elemClass = c;
                    }
                }
                if (elemClass != null && elemClass != Object.class) {
                    // Use ObjectReader for T if available (e.g., Optional<User>)
                    ObjectReader<?> elemReader = ObjectMapper.shared().getObjectReader(elemClass);
                    if (elemReader != null) {
                        return Optional.of(elemReader.readObject(parser, elemClass, fieldName, features));
                    }
                }
                // Fallback: auto-detect type
                Object value = parser.readAny();
                return Optional.ofNullable(value);
            };

    private static final ObjectWriter<Optional<?>> OPTIONAL_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                Optional<?> opt = (Optional<?>) object;
                if (opt == null || opt.isEmpty()) {
                    generator.writeNull();
                } else {
                    generator.writeAny(opt.get());
                }
            };

    private static final ObjectReader<OptionalInt> OPTIONAL_INT_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return OptionalInt.empty();
                }
                return OptionalInt.of(parser.readInt());
            };

    private static final ObjectWriter<OptionalInt> OPTIONAL_INT_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                OptionalInt opt = (OptionalInt) object;
                if (opt == null || opt.isEmpty()) {
                    generator.writeNull();
                } else {
                    generator.writeInt32(opt.getAsInt());
                }
            };

    private static final ObjectReader<OptionalLong> OPTIONAL_LONG_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return OptionalLong.empty();
                }
                return OptionalLong.of(parser.readLong());
            };

    private static final ObjectWriter<OptionalLong> OPTIONAL_LONG_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                OptionalLong opt = (OptionalLong) object;
                if (opt == null || opt.isEmpty()) {
                    generator.writeNull();
                } else {
                    generator.writeInt64(opt.getAsLong());
                }
            };

    private static final ObjectReader<OptionalDouble> OPTIONAL_DOUBLE_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return OptionalDouble.empty();
                }
                return OptionalDouble.of(parser.readDouble());
            };

    private static final ObjectWriter<OptionalDouble> OPTIONAL_DOUBLE_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                OptionalDouble opt = (OptionalDouble) object;
                if (opt == null || opt.isEmpty()) {
                    generator.writeNull();
                } else {
                    generator.writeDouble(opt.getAsDouble());
                }
            };

    // ==================== UUID ====================

    private static final ObjectReader<UUID> UUID_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : UUID.fromString(str);
            };

    private static final ObjectWriter<UUID> UUID_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((UUID) object).toString());
            };

    // ==================== Duration / Period ====================

    private static final ObjectReader<Duration> DURATION_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : Duration.parse(str);
            };

    private static final ObjectWriter<Duration> DURATION_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((Duration) object).toString());
            };

    private static final ObjectReader<Period> PERIOD_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : Period.parse(str);
            };

    private static final ObjectWriter<Period> PERIOD_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((Period) object).toString());
            };

    // ==================== Year / YearMonth / MonthDay ====================

    private static final ObjectReader<Year> YEAR_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return Year.of(parser.readInt());
            };

    private static final ObjectWriter<Year> YEAR_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeInt32(((Year) object).getValue());
            };

    private static final ObjectReader<YearMonth> YEAR_MONTH_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : YearMonth.parse(str);
            };

    private static final ObjectWriter<YearMonth> YEAR_MONTH_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((YearMonth) object).toString());
            };

    private static final ObjectReader<MonthDay> MONTH_DAY_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : MonthDay.parse(str);
            };

    private static final ObjectWriter<MonthDay> MONTH_DAY_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((MonthDay) object).toString());
            };

    // ==================== URI / Path ====================

    private static final ObjectReader<URI> URI_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : URI.create(str);
            };

    private static final ObjectWriter<URI> URI_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((URI) object).toString());
            };

    private static final ObjectReader<Path> PATH_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : Path.of(str);
            };

    private static final ObjectWriter<Path> PATH_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((Path) object).toString());
            };

    // ==================== java.time types ====================

    private static final ObjectReader<LocalDate> LOCAL_DATE_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : com.alibaba.fastjson3.util.DateUtils.parseLocalDate(str);
            };

    private static final ObjectWriter<LocalDate> LOCAL_DATE_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                com.alibaba.fastjson3.util.DateFormatPattern p =
                        generator.effectiveMapper().getDateFormatPattern();
                if (p != null) {
                    p.write(generator, object);
                } else {
                    generator.writeString(((LocalDate) object).toString());
                }
            };

    private static final ObjectReader<LocalDateTime> LOCAL_DATE_TIME_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : com.alibaba.fastjson3.util.DateUtils.parseLocalDateTime(str);
            };

    private static final ObjectWriter<LocalDateTime> LOCAL_DATE_TIME_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                com.alibaba.fastjson3.util.DateFormatPattern p =
                        generator.effectiveMapper().getDateFormatPattern();
                if (p != null) {
                    p.write(generator, object);
                } else {
                    generator.writeString(((LocalDateTime) object).toString());
                }
            };

    private static final ObjectReader<LocalTime> LOCAL_TIME_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : LocalTime.parse(str);
            };

    private static final ObjectWriter<LocalTime> LOCAL_TIME_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                com.alibaba.fastjson3.util.DateFormatPattern p =
                        generator.effectiveMapper().getDateFormatPattern();
                if (p != null) {
                    p.write(generator, object);
                } else {
                    generator.writeString(((LocalTime) object).toString());
                }
            };

    private static final ObjectReader<Instant> INSTANT_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : Instant.parse(str);
            };

    private static final ObjectWriter<Instant> INSTANT_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                com.alibaba.fastjson3.util.DateFormatPattern p =
                        generator.effectiveMapper().getDateFormatPattern();
                if (p != null) {
                    p.write(generator, object);
                } else {
                    generator.writeString(((Instant) object).toString());
                }
            };

    private static final ObjectReader<ZonedDateTime> ZONED_DATE_TIME_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : ZonedDateTime.parse(str);
            };

    private static final ObjectWriter<ZonedDateTime> ZONED_DATE_TIME_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                com.alibaba.fastjson3.util.DateFormatPattern p =
                        generator.effectiveMapper().getDateFormatPattern();
                if (p != null) {
                    p.write(generator, object);
                } else {
                    generator.writeString(((ZonedDateTime) object).toString());
                }
            };

    private static final ObjectReader<OffsetDateTime> OFFSET_DATE_TIME_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : OffsetDateTime.parse(str);
            };

    private static final ObjectWriter<OffsetDateTime> OFFSET_DATE_TIME_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                com.alibaba.fastjson3.util.DateFormatPattern p =
                        generator.effectiveMapper().getDateFormatPattern();
                if (p != null) {
                    p.write(generator, object);
                } else {
                    generator.writeString(((OffsetDateTime) object).toString());
                }
            };

    private static final ObjectReader<OffsetTime> OFFSET_TIME_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : OffsetTime.parse(str);
            };

    private static final ObjectWriter<OffsetTime> OFFSET_TIME_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                com.alibaba.fastjson3.util.DateFormatPattern p =
                        generator.effectiveMapper().getDateFormatPattern();
                if (p != null) {
                    p.write(generator, object);
                } else {
                    generator.writeString(((OffsetTime) object).toString());
                }
            };

    private static final ObjectReader<Date> DATE_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null ? null : Date.from(Instant.parse(str));
            };

    private static final ObjectWriter<Date> DATE_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                com.alibaba.fastjson3.util.DateFormatPattern p =
                        generator.effectiveMapper().getDateFormatPattern();
                if (p != null) {
                    p.write(generator, object);
                } else {
                    generator.writeString(((Date) object).toInstant().toString());
                }
            };

    // ==================== java.util.concurrent.atomic types ====================

    private static final ObjectReader<AtomicInteger> ATOMIC_INTEGER_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return new AtomicInteger(parser.readInt());
            };

    private static final ObjectWriter<AtomicInteger> ATOMIC_INTEGER_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeInt32(((AtomicInteger) object).get());
            };

    private static final ObjectReader<AtomicLong> ATOMIC_LONG_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return new AtomicLong(parser.readLong());
            };

    private static final ObjectWriter<AtomicLong> ATOMIC_LONG_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeInt64(((AtomicLong) object).get());
            };

    private static final ObjectReader<AtomicBoolean> ATOMIC_BOOLEAN_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return new AtomicBoolean(parser.readBoolean());
            };

    private static final ObjectWriter<AtomicBoolean> ATOMIC_BOOLEAN_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeBool(((AtomicBoolean) object).get());
            };

    // ==================== Throwable ====================

    private static final ObjectWriter<Throwable> THROWABLE_WRITER = new ObjectWriter<>() {
        @Override
        public void write(JSONGenerator generator, Object object,
                          Object fieldName, java.lang.reflect.Type fieldType, long features) {
            Throwable t = (Throwable) object;
            generator.startObject();
            boolean writeType = generator.writeThrowableClassName
                    || (generator.writeClassName
                        && !(generator.notWriteRootClassName && generator.getWriteDepth() == 0));
            if (writeType) {
                generator.writeName("@type");
                generator.writeString(t.getClass().getName());
            }
            if (t.getMessage() != null) {
                generator.writeName("message");
                generator.writeString(t.getMessage());
            }
            StackTraceElement[] stack = t.getStackTrace();
            if (stack != null && stack.length > 0) {
                generator.writeName("stackTrace");
                generator.startArray();
                for (StackTraceElement ste : stack) {
                    generator.beforeArrayValue();
                    generator.writeString(ste.toString());
                }
                generator.endArray();
            }
            Throwable cause = t.getCause();
            if (cause != null && cause != t) {
                generator.writeName("cause");
                generator.incrementDepth();
                try {
                    this.write(generator, cause, null, null, features);
                } finally {
                    generator.decrementDepth();
                }
            }
            generator.endObject();
        }
    };

    // ==================== Currency / Locale / TimeZone ====================

    private static final ObjectWriter<java.util.Currency> CURRENCY_WRITER =
            (generator, object, fieldName, fieldType, features) ->
                    generator.writeString(((java.util.Currency) object).getCurrencyCode());

    private static final ObjectWriter<java.util.Locale> LOCALE_WRITER =
            (generator, object, fieldName, fieldType, features) ->
                    generator.writeString(object.toString());

    private static final ObjectWriter<java.util.TimeZone> TIMEZONE_WRITER =
            (generator, object, fieldName, fieldType, features) ->
                    generator.writeString(((java.util.TimeZone) object).getID());

    // ==================== String ====================

    private static final ObjectReader<String> STRING_READER =
            (parser, fieldType, fieldName, features) -> parser.readNullableString();

    // ==================== Primitive wrapper types ====================

    private static final ObjectReader<Byte> BYTE_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return (byte) parser.readInt();
            };

    private static final ObjectReader<Short> SHORT_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return (short) parser.readInt();
            };

    private static final ObjectReader<Character> CHARACTER_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readNullableString();
                return str == null || str.isEmpty() ? null : str.charAt(0);
            };

    private static final ObjectReader<Integer> INTEGER_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return parser.readInt();
            };

    private static final ObjectReader<Long> LONG_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return parser.readLong();
            };

    private static final ObjectReader<Float> FLOAT_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return (float) parser.readDouble();
            };

    private static final ObjectReader<Double> DOUBLE_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return parser.readDouble();
            };

    private static final ObjectReader<Boolean> BOOLEAN_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return parser.readBoolean();
            };

    private static final ObjectReader<java.math.BigDecimal> BIG_DECIMAL_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                // readBigDecimalLiteral itself handles both the bare-literal
                // and the quoted-string shapes (see JSONParser round-1 fix).
                // Earlier this ObjectReader duplicated the quoted-string peek
                // inline with an unguarded charAt — whitespace-only input blew
                // past end() and threw AIOOBE; single-quote bypassed the
                // AllowSingleQuotes feature check. Delegating cleanly fixes
                // both without re-implementing the quote-peek protocol.
                return parser.readBigDecimalLiteral();
            };

    private static final ObjectReader<java.math.BigInteger> BIG_INTEGER_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return parser.readBigIntegerLiteral();
            };
}

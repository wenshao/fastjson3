package com.alibaba.fastjson3.util;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Classified view of a date/time format string. Constructed once at
 * {@code @JSONField(format=...)} parse time or {@code ObjectMapper.Builder
 * .dateFormat(...)} build time, then reused per write.
 *
 * <p>Recognizes three special tokens that emit JSON numbers / default ISO
 * shape, plus five common patterns hand-rolled at the byte level via
 * {@link DateUtils} (bypassing {@link DateTimeFormatter}'s allocation
 * profile, which the fastjson2 author measured at roughly an order of
 * magnitude slower than direct byte writing). Anything else falls through
 * to a cached {@link DateTimeFormatter}.</p>
 *
 * <table>
 *   <caption>Recognized format strings</caption>
 *   <tr><th>format</th><th>kind</th><th>output shape</th></tr>
 *   <tr><td>{@code "millis"}</td><td>{@link Kind#MILLIS}</td><td>{@code 1714521600000} (epoch ms as JSON number)</td></tr>
 *   <tr><td>{@code "unixtime"}</td><td>{@link Kind#UNIXTIME}</td><td>{@code 1714521600} (epoch s as JSON number)</td></tr>
 *   <tr><td>{@code "iso8601"}</td><td>{@link Kind#ISO8601}</td><td>{@code "2024-04-30T12:00:00"} (default ISO emit)</td></tr>
 *   <tr><td>{@code "yyyy-MM-dd"}</td><td>{@link Kind#YYYY_MM_DD}</td><td>{@code "2024-04-30"} (10 chars)</td></tr>
 *   <tr><td>{@code "yyyyMMdd"}</td><td>{@link Kind#YYYYMMDD}</td><td>{@code "20240430"} (8 chars)</td></tr>
 *   <tr><td>{@code "yyyy-MM-dd HH:mm"}</td><td>{@link Kind#YYYY_MM_DD_HH_MM}</td><td>{@code "2024-04-30 12:00"} (16 chars)</td></tr>
 *   <tr><td>{@code "yyyy-MM-dd HH:mm:ss"}</td><td>{@link Kind#YYYY_MM_DD_HH_MM_SS}</td><td>{@code "2024-04-30 12:00:00"} (19 chars)</td></tr>
 *   <tr><td>{@code "yyyyMMddHHmmss"}</td><td>{@link Kind#YYYYMMDDHHMMSS}</td><td>{@code "20240430120000"} (14 chars)</td></tr>
 *   <tr><td>any other</td><td>{@link Kind#PATTERN}</td><td>via {@link DateTimeFormatter}</td></tr>
 * </table>
 */
public final class DateFormatPattern {
    public enum Kind {
        MILLIS,
        UNIXTIME,
        ISO8601,
        YYYY_MM_DD,
        YYYYMMDD,
        YYYY_MM_DD_HH_MM,
        YYYY_MM_DD_HH_MM_SS,
        YYYYMMDDHHMMSS,
        PATTERN
    }

    public final String format;
    public final Kind kind;
    /** Non-null only when {@link #kind} == {@link Kind#PATTERN}. */
    public final DateTimeFormatter formatter;

    private DateFormatPattern(String format, Kind kind, DateTimeFormatter formatter) {
        this.format = format;
        this.kind = kind;
        this.formatter = formatter;
    }

    /**
     * Classify a format string. Returns {@code null} for null / empty input
     * (treat as "no format set"). Never throws — even unrecognized patterns
     * are accepted and pre-compile a {@link DateTimeFormatter}.
     *
     * @throws java.time.format.DateTimeParseException if {@code format} is
     *     not a valid {@link DateTimeFormatter} pattern (and not one of the
     *     recognized special tokens / fast paths)
     */
    public static DateFormatPattern of(String format) {
        if (format == null || format.isEmpty()) {
            return null;
        }
        switch (format) {
            case "millis":
                return new DateFormatPattern(format, Kind.MILLIS, null);
            case "unixtime":
                return new DateFormatPattern(format, Kind.UNIXTIME, null);
            case "iso8601":
                return new DateFormatPattern(format, Kind.ISO8601, null);
            // Fast-path patterns also pre-compile a DateTimeFormatter as
            // the out-of-range-year fallback. The hand-rolled byte writers
            // in DateUtils (writeLocalDate / writeYyyyMMdd8 / etc.) assume
            // year ∈ [0, 9999]; for year < 0 or year > 9999 we route
            // through the formatter, which signs years correctly per JDK
            // conventions. Pre-compiling here keeps the fallback alloc-free
            // at write time.
            case "yyyy-MM-dd":
                return new DateFormatPattern(format, Kind.YYYY_MM_DD, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case "yyyyMMdd":
                return new DateFormatPattern(format, Kind.YYYYMMDD, DateTimeFormatter.ofPattern("yyyyMMdd"));
            case "yyyy-MM-dd HH:mm":
                return new DateFormatPattern(format, Kind.YYYY_MM_DD_HH_MM, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            case "yyyy-MM-dd HH:mm:ss":
                return new DateFormatPattern(format, Kind.YYYY_MM_DD_HH_MM_SS, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            case "yyyyMMddHHmmss":
                return new DateFormatPattern(format, Kind.YYYYMMDDHHMMSS, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            default:
                return new DateFormatPattern(format, Kind.PATTERN, DateTimeFormatter.ofPattern(format));
        }
    }

    /**
     * Range used by the fast-path byte writers. Year outside this range
     * routes through the pre-compiled {@link #formatter} fallback.
     *
     * <p>The lower bound is {@code 1} (year 1 CE), not {@code 0}, because
     * {@link DateTimeFormatter}'s {@code yyyy} pattern letter is
     * <em>year-of-era</em>, not proleptic ISO year. ISO year {@code 0}
     * is BCE year-of-era {@code 1}, so a formatter emits {@code "0001"}
     * for {@code LocalDate.of(0, ...)}, while the hand-rolled writers
     * would emit raw digits {@code "0000"}. Tightening to
     * {@code [1, 9999]} routes year {@code 0} (and any negative /
     * &gt; 9999 year) through the formatter, ensuring byte-equivalent
     * output. Years 1–9999 are byte-identical between fast path and
     * formatter.</p>
     */
    private static final int FAST_PATH_YEAR_MIN = 1;
    private static final int FAST_PATH_YEAR_MAX = 9999;

    /**
     * Dispatch a write of {@code value} according to {@link #kind}. Caller
     * must have already emitted the field name (this only writes the value).
     */
    public void write(JSONGenerator g, Object value) {
        switch (kind) {
            case MILLIS:
                g.writeInt64(toEpochMillis(value));
                return;
            case UNIXTIME:
                g.writeInt64(toEpochMillis(value) / 1000);
                return;
            case ISO8601:
                writeIso8601(g, value);
                return;
            case YYYY_MM_DD: {
                LocalDate ld = toLocalDate(value);
                if (yearInFastPathRange(ld.getYear())) {
                    g.writeLocalDate(ld);
                } else {
                    g.writeString(formatter.format(ld));
                }
                return;
            }
            case YYYYMMDD: {
                LocalDate ld = toLocalDate(value);
                if (yearInFastPathRange(ld.getYear())) {
                    g.writeYyyyMMdd8(ld);
                } else {
                    g.writeString(formatter.format(ld));
                }
                return;
            }
            case YYYY_MM_DD_HH_MM: {
                LocalDateTime ldt = toLocalDateTime(value);
                if (yearInFastPathRange(ldt.getYear())) {
                    g.writeYyyyMMddhhmm16(ldt);
                } else {
                    g.writeString(formatter.format(ldt));
                }
                return;
            }
            case YYYY_MM_DD_HH_MM_SS: {
                LocalDateTime ldt = toLocalDateTime(value);
                if (yearInFastPathRange(ldt.getYear())) {
                    g.writeYyyyMMddhhmmss19(ldt);
                } else {
                    g.writeString(formatter.format(ldt));
                }
                return;
            }
            case YYYYMMDDHHMMSS: {
                LocalDateTime ldt = toLocalDateTime(value);
                if (yearInFastPathRange(ldt.getYear())) {
                    g.writeYyyyMMddhhmmss14(ldt);
                } else {
                    g.writeString(formatter.format(ldt));
                }
                return;
            }
            case PATTERN:
            default:
                writePattern(g, value);
                return;
        }
    }

    private static boolean yearInFastPathRange(int year) {
        return year >= FAST_PATH_YEAR_MIN && year <= FAST_PATH_YEAR_MAX;
    }

    /**
     * Format a Date / Temporal value to a String using this pattern.
     * Used by code paths that need the formatted shape but cannot write
     * directly to a {@link JSONGenerator} — most notably JSON object keys
     * (a Map with a {@link Date} or {@link java.time.temporal.TemporalAccessor}
     * key needs the formatted key to land in the {@code writeName(String)} call).
     *
     * <p>For {@link Kind#MILLIS} / {@link Kind#UNIXTIME}, returns the
     * numeric representation as a decimal {@link String} (still wraps in
     * quotes when used as a JSON object key — JSON spec mandates string
     * keys, so the numeric form is preserved as a quoted decimal).</p>
     *
     * <p>For value types this strategy can't handle (e.g., {@link java.time.LocalTime}
     * with a date-shaped Kind), throws {@link com.alibaba.fastjson3.JSONException}
     * the same way {@link #write} does.</p>
     */
    public String formatToString(Object value) {
        switch (kind) {
            case MILLIS:
                return Long.toString(toEpochMillis(value));
            case UNIXTIME:
                return Long.toString(toEpochMillis(value) / 1000);
            case ISO8601:
                return iso8601String(value);
            case YYYY_MM_DD: {
                LocalDate ld = toLocalDate(value);
                return yearInFastPathRange(ld.getYear()) ? ld.toString() : formatter.format(ld);
            }
            case YYYYMMDD: {
                LocalDate ld = toLocalDate(value);
                if (yearInFastPathRange(ld.getYear())) {
                    byte[] buf = new byte[8];
                    DateUtils.writeYyyyMMdd8(buf, 0, ld);
                    return new String(buf, 0, 8, java.nio.charset.StandardCharsets.US_ASCII);
                }
                return formatter.format(ld);
            }
            case YYYY_MM_DD_HH_MM: {
                LocalDateTime ldt = toLocalDateTime(value);
                if (yearInFastPathRange(ldt.getYear())) {
                    byte[] buf = new byte[16];
                    DateUtils.writeYyyyMMddhhmm16(buf, 0, ldt);
                    return new String(buf, 0, 16, java.nio.charset.StandardCharsets.US_ASCII);
                }
                return formatter.format(ldt);
            }
            case YYYY_MM_DD_HH_MM_SS: {
                LocalDateTime ldt = toLocalDateTime(value);
                if (yearInFastPathRange(ldt.getYear())) {
                    byte[] buf = new byte[19];
                    DateUtils.writeYyyyMMddhhmmss19(buf, 0, ldt);
                    return new String(buf, 0, 19, java.nio.charset.StandardCharsets.US_ASCII);
                }
                return formatter.format(ldt);
            }
            case YYYYMMDDHHMMSS: {
                LocalDateTime ldt = toLocalDateTime(value);
                if (yearInFastPathRange(ldt.getYear())) {
                    byte[] buf = new byte[14];
                    DateUtils.writeYyyyMMddhhmmss14(buf, 0, ldt);
                    return new String(buf, 0, 14, java.nio.charset.StandardCharsets.US_ASCII);
                }
                return formatter.format(ldt);
            }
            case PATTERN:
            default:
                if (value instanceof Instant in) {
                    return formatter.format(in.atZone(DateUtils.DEFAULT_ZONE_ID));
                }
                if (value instanceof LocalDate ld) {
                    return formatter.format(ld.atStartOfDay());
                }
                if (value instanceof java.time.temporal.TemporalAccessor ta) {
                    return formatter.format(ta);
                }
                if (value instanceof Date d) {
                    return formatter.format(utilOrSqlDateToInstant(d).atZone(DateUtils.DEFAULT_ZONE_ID));
                }
                throw new JSONException(
                        "Cannot format value of type " + value.getClass().getName()
                                + " with date pattern \"" + format + "\"");
        }
    }

    /**
     * Same shape as {@link #writeIso8601(JSONGenerator, Object)} but
     * returns a String. Mirrors the per-type ISO emit fastjson3 uses by
     * default.
     */
    private static String iso8601String(Object value) {
        if (value instanceof Instant in) {
            return in.toString();
        } else if (value instanceof LocalDateTime ldt) {
            return ldt.toString();
        } else if (value instanceof LocalDate ld) {
            return ld.toString();
        } else if (value instanceof ZonedDateTime zdt) {
            return zdt.toString();
        } else if (value instanceof OffsetDateTime odt) {
            return odt.toString();
        } else if (value instanceof Date d) {
            return utilOrSqlDateToInstant(d).toString();
        }
        throw new JSONException(
                "Cannot emit value of type " + value.getClass().getName() + " as iso8601");
    }

    private void writePattern(JSONGenerator g, Object value) {
        // Instant has no calendar fields (year/month/day/hour/...) so a
        // custom pattern like "yyyy/MM/dd" would throw
        // UnsupportedTemporalTypeException at formatter.format(instant).
        // Project the Instant into a ZonedDateTime at the default zone
        // before formatting — same convention as the Date branch.
        if (value instanceof Instant in) {
            g.writeString(formatter.format(in.atZone(DateUtils.DEFAULT_ZONE_ID)));
        } else if (value instanceof LocalDate ld) {
            // LocalDate has no time fields, so a pattern containing
            // {@code HH/mm/ss} (e.g., the common ISO "yyyy-MM-dd'T'HH:mm:ss")
            // throws UnsupportedTemporalTypeException at format time.
            // Promote to LocalDateTime via atStartOfDay() — same upgrade
            // the fast-path date-time Kinds already perform via
            // toLocalDateTime, so behavior is consistent across Kinds.
            g.writeString(formatter.format(ld.atStartOfDay()));
        } else if (value instanceof java.time.temporal.TemporalAccessor ta) {
            g.writeString(formatter.format(ta));
        } else if (value instanceof Date d) {
            // java.sql.Date / java.sql.Time override toInstant() to throw
            // UnsupportedOperationException — reroute through epoch ms +
            // a fresh Instant.
            Instant in = utilOrSqlDateToInstant(d);
            g.writeString(formatter.format(in.atZone(DateUtils.DEFAULT_ZONE_ID)));
        } else {
            throw new JSONException(
                    "Cannot format value of type " + value.getClass().getName()
                            + " with date pattern \"" + format + "\"");
        }
    }

    private static void writeIso8601(JSONGenerator g, Object value) {
        // Equivalent to fj3's default ISO-8601 emit. Routes each Temporal
        // type to its dedicated JSONGenerator method; java.util.Date goes
        // through Instant. ZonedDateTime / OffsetDateTime preserve their
        // offset/zone information via toString() — calling
        // writeLocalDateTime(zdt.toLocalDateTime()) here would silently
        // drop the offset, which is data loss.
        if (value instanceof Instant in) {
            g.writeInstant(in);
        } else if (value instanceof LocalDateTime ldt) {
            g.writeLocalDateTime(ldt);
        } else if (value instanceof LocalDate ld) {
            g.writeLocalDate(ld);
        } else if (value instanceof ZonedDateTime zdt) {
            g.writeString(zdt.toString());
        } else if (value instanceof OffsetDateTime odt) {
            g.writeString(odt.toString());
        } else if (value instanceof Date d) {
            g.writeInstant(utilOrSqlDateToInstant(d));
        } else {
            throw new JSONException(
                    "Cannot emit value of type " + value.getClass().getName()
                            + " as iso8601");
        }
    }

    /**
     * Convert a {@link Date} (including {@code java.sql.Date} /
     * {@code java.sql.Time} which throw on {@code toInstant()}) to a
     * fresh {@link Instant} via epoch millis. The JDK's
     * {@code java.sql.Date.toInstant} contract is "always throws
     * UnsupportedOperationException because date does not have a time
     * component" — going through {@code getTime()} sidesteps that.
     */
    private static Instant utilOrSqlDateToInstant(Date d) {
        return Instant.ofEpochMilli(d.getTime());
    }

    private static long toEpochMillis(Object value) {
        if (value instanceof Date d) {
            return d.getTime();
        }
        if (value instanceof Instant in) {
            return in.toEpochMilli();
        }
        if (value instanceof ZonedDateTime zdt) {
            return zdt.toInstant().toEpochMilli();
        }
        if (value instanceof OffsetDateTime odt) {
            return odt.toInstant().toEpochMilli();
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.atZone(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        }
        if (value instanceof LocalDate ld) {
            return ld.atStartOfDay(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        }
        throw new JSONException(
                "Cannot convert value of type " + value.getClass().getName() + " to epoch millis");
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) {
            return ld;
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.toLocalDate();
        }
        if (value instanceof ZonedDateTime zdt) {
            return zdt.toLocalDate();
        }
        if (value instanceof OffsetDateTime odt) {
            return odt.toLocalDate();
        }
        if (value instanceof Instant in) {
            return LocalDateTime.ofInstant(in, DateUtils.DEFAULT_ZONE_ID).toLocalDate();
        }
        if (value instanceof Date d) {
            return utilOrSqlDateToInstant(d).atZone(DateUtils.DEFAULT_ZONE_ID).toLocalDate();
        }
        throw new JSONException(
                "Cannot convert value of type " + value.getClass().getName() + " to LocalDate");
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof LocalDate ld) {
            return ld.atStartOfDay();
        }
        if (value instanceof ZonedDateTime zdt) {
            return zdt.toLocalDateTime();
        }
        if (value instanceof OffsetDateTime odt) {
            return odt.toLocalDateTime();
        }
        if (value instanceof Instant in) {
            return LocalDateTime.ofInstant(in, DateUtils.DEFAULT_ZONE_ID);
        }
        if (value instanceof Date d) {
            return utilOrSqlDateToInstant(d).atZone(DateUtils.DEFAULT_ZONE_ID).toLocalDateTime();
        }
        throw new JSONException(
                "Cannot convert value of type " + value.getClass().getName() + " to LocalDateTime");
    }
}

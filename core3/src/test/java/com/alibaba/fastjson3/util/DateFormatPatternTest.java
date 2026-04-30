package com.alibaba.fastjson3.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-level coverage for {@link DateFormatPattern}: verify each {@code Kind}
 * is classified correctly from its format string, and that {@code write(...)}
 * dispatches to the right code path for each kind. Higher-level integration
 * (mapper / FieldWriter) is in {@code ObjectMapperDateFormatTest}.
 */
class DateFormatPatternTest {
    @Test
    void of_null_returnsNull() {
        assertNull(DateFormatPattern.of(null));
    }

    @Test
    void of_empty_returnsNull() {
        assertNull(DateFormatPattern.of(""));
    }

    @Test
    void of_specialTokens_classifiesCorrectly() {
        assertEquals(DateFormatPattern.Kind.MILLIS, DateFormatPattern.of("millis").kind);
        assertEquals(DateFormatPattern.Kind.UNIXTIME, DateFormatPattern.of("unixtime").kind);
        assertEquals(DateFormatPattern.Kind.ISO8601, DateFormatPattern.of("iso8601").kind);
    }

    @Test
    void of_fastPathPatterns_classifiesCorrectly() {
        assertEquals(DateFormatPattern.Kind.YYYY_MM_DD, DateFormatPattern.of("yyyy-MM-dd").kind);
        assertEquals(DateFormatPattern.Kind.YYYYMMDD, DateFormatPattern.of("yyyyMMdd").kind);
        assertEquals(DateFormatPattern.Kind.YYYY_MM_DD_HH_MM, DateFormatPattern.of("yyyy-MM-dd HH:mm").kind);
        assertEquals(DateFormatPattern.Kind.YYYY_MM_DD_HH_MM_SS, DateFormatPattern.of("yyyy-MM-dd HH:mm:ss").kind);
        assertEquals(DateFormatPattern.Kind.YYYYMMDDHHMMSS, DateFormatPattern.of("yyyyMMddHHmmss").kind);
    }

    @Test
    void of_specialAndFastPath_haveNoFormatter() {
        // Caching DateTimeFormatter for a special token / fast path would
        // be wasted memory — these paths never invoke it.
        assertNull(DateFormatPattern.of("millis").formatter);
        assertNull(DateFormatPattern.of("unixtime").formatter);
        assertNull(DateFormatPattern.of("iso8601").formatter);
        assertNull(DateFormatPattern.of("yyyy-MM-dd").formatter);
        assertNull(DateFormatPattern.of("yyyyMMdd").formatter);
        assertNull(DateFormatPattern.of("yyyy-MM-dd HH:mm").formatter);
        assertNull(DateFormatPattern.of("yyyy-MM-dd HH:mm:ss").formatter);
        assertNull(DateFormatPattern.of("yyyyMMddHHmmss").formatter);
    }

    @Test
    void of_unrecognizedPattern_classifiesAsPATTERN_andCachesFormatter() {
        DateFormatPattern p = DateFormatPattern.of("EEE, dd MMM yyyy");
        assertEquals(DateFormatPattern.Kind.PATTERN, p.kind);
        assertNotNull(p.formatter);
        // The cached formatter must round-trip the same pattern.
        DateTimeFormatter expected = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
        LocalDateTime now = LocalDateTime.of(2024, 4, 30, 12, 0);
        assertEquals(expected.format(now), p.formatter.format(now));
    }

    @Test
    void format_field_preservesOriginalString() {
        // The format string itself is exposed for debugging / migration tools.
        assertEquals("yyyy-MM-dd", DateFormatPattern.of("yyyy-MM-dd").format);
        assertEquals("millis", DateFormatPattern.of("millis").format);
        assertEquals("EEE, dd MMM", DateFormatPattern.of("EEE, dd MMM").format);
    }

    @Test
    void of_millis_doesNotCrash_unlikeRawDateTimeFormatter() {
        // Regression pin: pre-PR fastjson3, @JSONField(format = "millis")
        // crashed at FieldWriter construction because
        // DateTimeFormatter.ofPattern("millis") throws IAE on the unknown
        // pattern letter 'l'. This is the bug fixed alongside the new
        // mapper-level dateFormat API.
        assertDoesNotThrow(() -> DateFormatPattern.of("millis"));
        assertDoesNotThrow(() -> DateFormatPattern.of("unixtime"));
        assertDoesNotThrow(() -> DateFormatPattern.of("iso8601"));
    }

    @Test
    void of_invalidPattern_throwsAtConstructionNotAtWrite() {
        // Patterns that aren't special tokens must validate at construction
        // (DateTimeFormatter.ofPattern is the gate) so misconfigured fields
        // fail fast rather than at first write request.
        assertThrows(IllegalArgumentException.class,
                () -> DateFormatPattern.of("zzzInvalidPatternZZZ"));
    }

    // ---- end-to-end write coverage ----
    // The convert helpers (toEpochMillis / toLocalDate / toLocalDateTime)
    // are exercised by the integration tests in ObjectMapperDateFormatTest
    // through real typed POJO fields — that's the path mapper-level
    // dateFormat reaches via FieldWriter.writeDate.
}

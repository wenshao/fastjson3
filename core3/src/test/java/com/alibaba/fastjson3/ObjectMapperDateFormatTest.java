package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.util.DateFormatPattern;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link ObjectMapper.Builder#dateFormat(String)}.
 * Mapper-level default + field-level @JSONField(format=...) precedence,
 * round-trip across the major Temporal types, fj2-parity special tokens.
 */
class ObjectMapperDateFormatTest {
    /**
     * Container for typed Temporal / Date fields. Mapper-level
     * {@code dateFormat} reaches the date-write path through
     * {@code FieldWriter}, which gates on a typed POJO field. Untyped Map
     * values / List elements bypass this path and emit through the default
     * {@code ObjectWriter} chain — same scope limitation as
     * {@code @JSONField(format=...)}.
     */
    public static class Bean {
        public LocalDateTime ts;
        public LocalDate date;
        public Date legacyDate;
    }

    public static class FieldOverrideBean {
        @JSONField(format = "yyyy-MM-dd")
        public LocalDateTime ts;
    }

    public static class MillisFieldBean {
        // Pre-PR this would crash at FieldWriter construction; the new
        // DateFormatPattern recognizer accepts "millis" without invoking
        // DateTimeFormatter.ofPattern.
        @JSONField(format = "millis")
        public Date d;
    }

    // ---- Builder API ----

    @Test
    void builder_dateFormat_null_isAccepted() {
        // null clears any preset; same as not calling the builder method.
        ObjectMapper m = ObjectMapper.builder().dateFormat(null).build();
        assertNull(m.getDateFormat());
        assertNull(m.getDateFormatPattern());
    }

    @Test
    void builder_dateFormat_empty_isAccepted() {
        ObjectMapper m = ObjectMapper.builder().dateFormat("").build();
        assertNull(m.getDateFormatPattern(),
                "empty string should be treated the same as null");
    }

    @Test
    void builder_dateFormat_pattern_storedAndClassified() {
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        assertEquals("yyyy-MM-dd", m.getDateFormat());
        assertNotNull(m.getDateFormatPattern());
        assertEquals(DateFormatPattern.Kind.YYYY_MM_DD, m.getDateFormatPattern().kind);
    }

    @Test
    void builder_dateFormat_carriesAcrossRebuild() {
        ObjectMapper m1 = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        ObjectMapper m2 = m1.rebuild().build();
        assertEquals("yyyy-MM-dd", m2.getDateFormat());
    }

    // ---- Mapper-level default applies ----

    @Test
    void mapperLevel_yyyy_MM_dd_appliedToLocalDateTime() {
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        Bean b = new Bean();
        b.ts = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        assertEquals("{\"ts\":\"2024-04-30\"}", m.writeValueAsString(b));
    }

    @Test
    void mapperLevel_yyyyMMdd_appliedToLocalDate() {
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyyMMdd").build();
        Bean b = new Bean();
        b.date = LocalDate.of(2024, 4, 30);
        String out = m.writeValueAsString(b);
        assertTrue(out.contains("\"date\":\"20240430\""),
                "expected yyyyMMdd-formatted date in: " + out);
    }

    @Test
    void mapperLevel_yyyy_MM_dd_HH_mm_ss_appliedToLocalDateTime() {
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd HH:mm:ss").build();
        Bean b = new Bean();
        b.ts = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        assertEquals("{\"ts\":\"2024-04-30 12:34:56\"}", m.writeValueAsString(b));
    }

    @Test
    void mapperLevel_yyyy_MM_dd_HH_mm_appliedToLocalDateTime() {
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd HH:mm").build();
        Bean b = new Bean();
        b.ts = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        assertEquals("{\"ts\":\"2024-04-30 12:34\"}", m.writeValueAsString(b));
    }

    @Test
    void mapperLevel_yyyyMMddHHmmss_appliedToLocalDateTime() {
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyyMMddHHmmss").build();
        Bean b = new Bean();
        b.ts = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        assertEquals("{\"ts\":\"20240430123456\"}", m.writeValueAsString(b));
    }

    // ---- Special tokens ----

    @Test
    void mapperLevel_millis_emitsEpochMillisAsNumber() {
        ObjectMapper m = ObjectMapper.builder().dateFormat("millis").build();
        long epochMs = 1714521600000L;
        Bean b = new Bean();
        b.legacyDate = new Date(epochMs);
        String out = m.writeValueAsString(b);
        // No quotes around the number; raw integer literal.
        assertTrue(out.contains("\"legacyDate\":" + epochMs),
                "expected epoch millis literal in: " + out);
    }

    @Test
    void mapperLevel_unixtime_emitsEpochSecondsAsNumber() {
        ObjectMapper m = ObjectMapper.builder().dateFormat("unixtime").build();
        long epochMs = 1714521600000L;
        Bean b = new Bean();
        b.legacyDate = new Date(epochMs);
        String out = m.writeValueAsString(b);
        assertTrue(out.contains("\"legacyDate\":" + (epochMs / 1000)),
                "expected epoch seconds literal in: " + out);
    }

    @Test
    void mapperLevel_iso8601_emitsDefaultIso() {
        // iso8601 token is equivalent to no-format (default ISO emit).
        ObjectMapper m = ObjectMapper.builder().dateFormat("iso8601").build();
        Bean b = new Bean();
        b.ts = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        // fj3's default LocalDateTime ISO shape is yyyy-MM-ddTHH:mm:ss[.nanos].
        assertEquals("{\"ts\":\"2024-04-30T12:34:56\"}", m.writeValueAsString(b));
    }

    // ---- Field-level overrides mapper-level ----

    @Test
    void fieldLevel_overrides_mapperLevel() {
        // Mapper says yyyyMMdd; field says yyyy-MM-dd. Field wins.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyyMMdd").build();
        FieldOverrideBean b = new FieldOverrideBean();
        b.ts = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        assertEquals("{\"ts\":\"2024-04-30\"}", m.writeValueAsString(b),
                "field-level @JSONField(format) must beat mapper-level dateFormat");
    }

    @Test
    void fieldLevel_format_millis_nowWorks_regression() {
        // Pre-PR: @JSONField(format = "millis") would crash at FieldWriter
        // construction because DateTimeFormatter.ofPattern("millis") throws
        // (unknown pattern letter 'l'). Post-PR: DateFormatPattern recognizes
        // "millis" as a special token and never invokes DateTimeFormatter.
        ObjectMapper m = ObjectMapper.builder().build();
        MillisFieldBean b = new MillisFieldBean();
        long epochMs = 1714521600000L;
        b.d = new Date(epochMs);
        assertEquals("{\"d\":" + epochMs + "}", m.writeValueAsString(b));
    }

    // ---- Default fallback ----

    @Test
    void noMapperFormat_emitsDefaultIso() {
        // Sanity check: a mapper without dateFormat keeps the existing
        // ISO-8601 default behavior.
        ObjectMapper m = ObjectMapper.builder().build();
        Bean b = new Bean();
        b.ts = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        // No format: writeAny path goes through default LocalDateTime writer.
        String out = m.writeValueAsString(b);
        assertTrue(out.contains("2024-04-30") && out.contains("12:34"),
                "expected default ISO-ish output, got: " + out);
    }

    // ---- Custom DateTimeFormatter pattern ----

    @Test
    void mapperLevel_customPattern_fallsThroughToDateTimeFormatter() {
        // Patterns outside the recognized list still work via the cached
        // DateTimeFormatter — slower path, correctness preserved.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy/MM/dd").build();
        Bean b = new Bean();
        b.ts = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        String out = m.writeValueAsString(b);
        assertTrue(out.contains("2024/04/30"),
                "expected slash-separated date in: " + out);
    }
}

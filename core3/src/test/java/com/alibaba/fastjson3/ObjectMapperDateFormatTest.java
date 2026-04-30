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

    public static class TimeBean {
        // Time-only types — date-shaped mapper formats must not apply to
        // these (would crash in DateFormatPattern's convert helpers).
        public java.time.LocalTime t;
        public java.time.OffsetTime ot;
    }

    public record DateRecord(Long id,
                             @JSONField(format = "yyyy-MM-dd") LocalDateTime ts,
                             LocalDateTime tsDefault) {
    }

    public static class ObjectFieldBean {
        // Object-typed field: runtime value may be any type. Field-level
        // @JSONField(format=) must apply when the runtime value is a date.
        @JSONField(format = "yyyy-MM-dd")
        public Object payload;
    }

    public static class ObjectFieldBeanNoAnnotation {
        // No field-level annotation — mapper-level dateFormat applies via
        // writeAny (already covered in earlier tests, but pinning here too).
        public Object payload;
    }

    public static class LocalTimeFieldBean {
        // Field-level @JSONField(format) on a LocalTime: time-only patterns
        // are not in the recognized fast paths, so this falls through to
        // PATTERN kind, which delegates to DateTimeFormatter.format and
        // accepts any TemporalAccessor including LocalTime.
        @JSONField(format = "HH:mm:ss")
        public java.time.LocalTime t;
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

    public static class ZonedBean {
        public java.time.ZonedDateTime zdt;
        public java.time.OffsetDateTime odt;
    }

    @Test
    void mapperLevel_iso8601_preservesZonedDateTimeOffset() {
        // qwen P-Critical regression: pre-fix, iso8601 routed
        // ZonedDateTime through writeLocalDateTime(zdt.toLocalDateTime())
        // which silently dropped the zone — data loss. Post-fix, emit via
        // value.toString() which includes the offset.
        ObjectMapper m = ObjectMapper.builder().dateFormat("iso8601").build();
        ZonedBean b = new ZonedBean();
        b.zdt = java.time.ZonedDateTime.of(
                LocalDateTime.of(2024, 4, 30, 12, 34, 56),
                java.time.ZoneId.of("Asia/Shanghai"));
        String out = m.writeValueAsString(b);
        assertTrue(out.contains("+08:00") || out.contains("Asia/Shanghai"),
                "iso8601 must preserve ZonedDateTime offset/zone, got: " + out);
    }

    @Test
    void mapperLevel_iso8601_preservesOffsetDateTimeOffset() {
        // Same data-loss bug for OffsetDateTime — preserve the offset.
        ObjectMapper m = ObjectMapper.builder().dateFormat("iso8601").build();
        ZonedBean b = new ZonedBean();
        b.odt = java.time.OffsetDateTime.of(
                LocalDateTime.of(2024, 4, 30, 12, 34, 56),
                java.time.ZoneOffset.ofHours(8));
        String out = m.writeValueAsString(b);
        assertTrue(out.contains("+08:00"),
                "iso8601 must preserve OffsetDateTime offset, got: " + out);
    }

    @Test
    void mapperLevel_customPattern_localDate_handlesTimePattern() {
        // R8 audit P1 regression: pre-fix, a LocalDate field + custom
        // pattern that includes time fields (e.g. "yyyy-MM-dd'T'HH:mm:ss"
        // — the most common ISO pattern users reach for) crashed with
        // UnsupportedTemporalTypeException because LocalDate has no
        // hour/minute/second accessors. Fast-path Kinds (YYYY_MM_DD_HH_MM_SS)
        // already promote LocalDate to LocalDateTime via toLocalDateTime;
        // PATTERN kind now matches.
        ObjectMapper m = ObjectMapper.builder()
                .dateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .build();
        Bean b = new Bean();
        b.date = LocalDate.of(2024, 4, 30);
        // Must not throw — atStartOfDay() upgrade fills in 00:00:00.
        String out = m.writeValueAsString(b);
        assertTrue(out.contains("\"2024-04-30T00:00:00\""),
                "LocalDate + time-bearing pattern must promote to LocalDateTime, got: " + out);
    }

    @Test
    void mapperLevel_customPattern_handlesInstant() {
        // qwen P-Critical regression: pre-fix, writePattern called
        // formatter.format(instant) directly. Custom patterns like
        // "yyyy/MM/dd" require calendar fields that Instant lacks, so
        // this threw UnsupportedTemporalTypeException. Post-fix, project
        // Instant onto ZonedDateTime at the default zone before formatting
        // (matches the Date branch convention).
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy/MM/dd").build();
        Map<String, Object> wrap = new LinkedHashMap<>();
        wrap.put("ts", java.time.Instant.parse("2024-04-30T12:34:56Z"));
        // Must not throw — output uses the system default zone, so we don't
        // pin the exact date; just confirm it parses and contains a slash.
        String out = m.writeValueAsString(wrap);
        assertTrue(out.contains("\"ts\":\"") && out.contains("/"),
                "custom pattern on Instant must format without crashing, got: " + out);
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

    // ---- Path coverage: ASM and Map<String, Date> ----

    @Test
    void mapperLevel_explicitAsmWriter_appliesFormat() {
        // ASM-generated POJO writers cache child writers via
        // BuiltinCodecs.getWriter, but only for "nested POJOs" — Date /
        // Temporal types miss the cache and fall through to
        // generator.writeAny(value). The fix is at writeAny: it consults
        // mapper.getDateFormatPattern() at the top of the date-dispatch
        // block. Without that, ASM-emitted Bean writers silently emit
        // default ISO regardless of the configured dateFormat.
        ObjectMapper m = ObjectMapper.builder()
                .writerCreatorType(com.alibaba.fastjson3.writer.WriterCreatorType.ASM)
                .dateFormat("yyyy-MM-dd")
                .build();
        Bean b = new Bean();
        b.ts = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        assertEquals("{\"ts\":\"2024-04-30\"}", m.writeValueAsString(b),
                "ASM writer must honor mapper-level dateFormat (writeAny path)");
    }

    @Test
    void mapperLevel_explicitReflectWriter_appliesFormat() {
        // Companion to the ASM coverage test — reflection path goes through
        // FieldWriter then BuiltinCodecs LOCAL_DATE_TIME_WRITER, both of
        // which now consult mapper format. Pin the same expected output.
        ObjectMapper m = ObjectMapper.builder()
                .writerCreatorType(com.alibaba.fastjson3.writer.WriterCreatorType.REFLECT)
                .dateFormat("yyyy-MM-dd")
                .build();
        Bean b = new Bean();
        b.ts = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        assertEquals("{\"ts\":\"2024-04-30\"}", m.writeValueAsString(b));
    }

    @Test
    void mapperLevel_appliesToMapValues() {
        // Map<String, Date> values flow through writeAny; mapper-level
        // dateFormat should now reach them too (was a documented scope
        // limitation in v1; the writeAny fix lifts it).
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        Map<String, LocalDate> wrap = new LinkedHashMap<>();
        wrap.put("d", LocalDate.of(2024, 4, 30));
        assertEquals("{\"d\":\"2024-04-30\"}", m.writeValueAsString(wrap));
    }

    @Test
    void mapperLevel_yyyy_MM_dd_outOfRangeYear_fallsThroughToFormatter() {
        // Round 3 audit P1: hand-rolled byte writers in DateUtils assume
        // year ∈ [0, 9999]. For year < 0 (BCE) or year > 9999, the byte
        // writers produce garbled output ("000/-..." for -1, "<345-..."
        // for 12345). DateFormatPattern.write must detect out-of-range
        // and route through the pre-compiled DateTimeFormatter fallback
        // which signs years correctly per JDK conventions.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        Bean b = new Bean();

        // Far-future year — JDK formats as "+12345-01-01" (5-digit signed)
        b.date = LocalDate.of(12345, 1, 1);
        String farFuture = m.writeValueAsString(b);
        assertTrue(farFuture.contains("+12345-01-01"),
                "year > 9999 must route through formatter fallback (signed year), got: " + farFuture);
        assertFalse(farFuture.contains("<345"),
                "garbled output indicates byte writer was used for out-of-range year, got: " + farFuture);

        // Year 9999 — upper boundary, fast path applies (max in range)
        b.date = LocalDate.of(9999, 12, 31);
        String boundary = m.writeValueAsString(b);
        assertTrue(boundary.contains("\"9999-12-31\""),
                "year 9999 (max in range) must use fast path, got: " + boundary);

        // Year 1 — lower boundary, fast path applies
        b.date = LocalDate.of(1, 1, 1);
        String lowerBoundary = m.writeValueAsString(b);
        assertTrue(lowerBoundary.contains("\"0001-01-01\""),
                "year 1 (min in range) must use fast path, got: " + lowerBoundary);

        // Year 0 — out of fast-path range. DateTimeFormatter's "yyyy"
        // pattern letter is year-of-era (not proleptic), so year 0 emits
        // "0001" via the formatter. Hand-rolled would emit "0000" — that
        // divergence is exactly why FAST_PATH_YEAR_MIN = 1 (year 0 routes
        // through the formatter for byte-equivalent output).
        b.date = LocalDate.of(0, 1, 1);
        String yearZero = m.writeValueAsString(b);
        assertTrue(yearZero.contains("\"0001-01-01\""),
                "year 0 (out of fast path) must route through formatter, got: " + yearZero);
    }

    @Test
    void mapperLevel_yyyy_MM_dd_year0_byteEquivalentToFormatter() {
        // Round 6 audit P0 regression: year 0 used to emit "0000-01-01"
        // via the hand-rolled byte writer (raw proleptic digits). The
        // DateTimeFormatter ("yyyy" = year-of-era) emits "0001-01-01"
        // for the same input. Tightened FAST_PATH_YEAR_MIN to 1 so year
        // 0 routes through the formatter — byte-equivalent output.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        Bean b = new Bean();
        b.date = LocalDate.of(0, 6, 15);
        String out = m.writeValueAsString(b);
        // DateTimeFormatter renders ISO year 0 as year-of-era 1 BCE → "0001"
        String expected = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd").format(LocalDate.of(0, 6, 15));
        assertTrue(out.contains("\"" + expected + "\""),
                "year 0 must produce formatter-equivalent output (" + expected + "), got: " + out);
    }

    @Test
    void mapperLevel_yyyyMMdd_outOfRangeYear_fallsThroughToFormatter() {
        // Same regression boundary on the no-separator pattern.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyyMMdd").build();
        Bean b = new Bean();
        b.date = LocalDate.of(12345, 1, 1);
        String out = m.writeValueAsString(b);
        // JDK formatter for "yyyyMMdd" with year=12345 emits "+123450101"
        // (signed, adaptive width). Specific shape: not the corrupt
        // "<3450101" the byte writer would emit.
        assertFalse(out.contains("<345"),
                "garbled byte writer output for year=12345, got: " + out);
        assertTrue(out.contains("12345"),
                "expected year digits in formatter fallback, got: " + out);
    }

    @Test
    void mapperLevel_yyyy_MM_dd_HH_mm_ss_outOfRangeYear_fallsThroughToFormatter() {
        // Datetime fast path for far-future year.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd HH:mm:ss").build();
        Bean b = new Bean();
        b.ts = LocalDateTime.of(12345, 1, 1, 0, 0, 0);
        String out = m.writeValueAsString(b);
        assertFalse(out.contains("<345"),
                "garbled byte writer output for year=12345, got: " + out);
        assertTrue(out.contains("12345"));
    }

    @Test
    void mapperLevel_dateFormat_doesNotCrashOnLocalTimeField() {
        // Round 2 audit P1 regression boundary: pre-PR, a LocalTime field
        // emitted value.toString() unconditionally. The Round 1 fix routed
        // BuiltinCodecs through DateFormatPattern.write, which crashes for
        // LocalTime because there's no meaningful LocalTime → LocalDate
        // conversion. Pin: time-only types bypass the mapper format and
        // emit their natural ISO shape.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        TimeBean b = new TimeBean();
        b.t = java.time.LocalTime.of(12, 34, 56);
        b.ot = java.time.OffsetTime.of(12, 34, 56, 0, java.time.ZoneOffset.UTC);
        String out = m.writeValueAsString(b);
        assertTrue(out.contains("12:34:56"),
                "LocalTime/OffsetTime fields must emit their natural ISO shape, got: " + out);
    }

    @Test
    void mapperLevel_millis_doesNotCrashOnLocalTimeField() {
        // millis token is even more nonsensical for time-only values.
        // Same bypass.
        ObjectMapper m = ObjectMapper.builder().dateFormat("millis").build();
        TimeBean b = new TimeBean();
        b.t = java.time.LocalTime.of(12, 34, 56);
        b.ot = java.time.OffsetTime.of(12, 34, 56, 0, java.time.ZoneOffset.UTC);
        String out = m.writeValueAsString(b);
        assertTrue(out.contains("12:34:56"),
                "LocalTime/OffsetTime fields must emit ISO shape under millis token, got: " + out);
    }

    @Test
    void fieldLevel_HHmmss_onLocalTime_stillWorks() {
        // Pin: field-level @JSONField(format="HH:mm:ss") on a LocalTime
        // routes through FieldWriter.datePattern → PATTERN kind, which
        // delegates to DateTimeFormatter.format(TemporalAccessor) and
        // produces "HH:mm:ss". Mapper-level dateFormat (date-shaped) is
        // bypassed for time-only types, but field-level pattern always
        // applies regardless.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        LocalTimeFieldBean b = new LocalTimeFieldBean();
        b.t = java.time.LocalTime.of(12, 34, 56);
        String out = m.writeValueAsString(b);
        assertEquals("{\"t\":\"12:34:56\"}", out,
                "field-level @JSONField(format=HH:mm:ss) on LocalTime must format via PATTERN kind");
    }

    @Test
    void fieldLevel_format_objectTypedField_appliesToDateValue() {
        // R9 audit P1 regression: a field declared as `Object` holding a
        // LocalDateTime / Date runtime value silently dropped the
        // field-level @JSONField(format=). FieldWriter dispatched through
        // writeGeneric (typeTag=GENERIC for Object) which had no
        // datePattern guard. Mapper-level dateFormat applied via writeAny
        // → BuiltinCodecs lambdas, but field-level was silently masked.
        // Post-fix: writeGeneric mirrors writeObject's datePattern guard.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyyMMdd").build();
        ObjectFieldBean b = new ObjectFieldBean();
        b.payload = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        // Field-level "yyyy-MM-dd" must beat mapper-level "yyyyMMdd".
        assertEquals("{\"payload\":\"2024-04-30\"}", m.writeValueAsString(b));
    }

    @Test
    void fieldLevel_format_objectTypedField_appliesToDate() {
        ObjectMapper m = ObjectMapper.builder().build();
        ObjectFieldBean b = new ObjectFieldBean();
        b.payload = new Date(LocalDate.of(2024, 4, 30)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli());
        String out = m.writeValueAsString(b);
        assertTrue(out.contains("\"2024-04-30\""),
                "Object-typed field with @JSONField(format) on java.util.Date should format, got: " + out);
    }

    @Test
    void mapperLevel_appliesToObjectField_withoutAnnotation() {
        // Sanity: no field-level annotation, mapper-level applies via
        // writeAny path (already covered, pinning here for symmetry).
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        ObjectFieldBeanNoAnnotation b = new ObjectFieldBeanNoAnnotation();
        b.payload = LocalDateTime.of(2024, 4, 30, 12, 34, 56);
        assertEquals("{\"payload\":\"2024-04-30\"}", m.writeValueAsString(b));
    }

    @Test
    void timestamp_routesAsDate_withMapperFormat() {
        // R9 audit P2 pin: java.sql.Timestamp routing through writeAny's
        // `instanceof Date` whitelist is currently accidental — relies on
        // BuiltinCodecs.getWriter(Timestamp.class) returning null (which
        // happens because core3's module-info doesn't `requires java.sql`,
        // so reflection-based POJO creation fails and the codec falls
        // through). Pin the current behavior with a regression test —
        // if module-info ever changes, this test fails loudly so we
        // explicitly handle Timestamp at that point.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        java.util.Map<String, Object> wrap = new LinkedHashMap<>();
        // Use reflection to construct a Timestamp without compile-time dep
        // on java.sql (this test module doesn't `requires java.sql`).
        try {
            Class<?> tsClass = Class.forName("java.sql.Timestamp");
            Object ts = tsClass.getConstructor(long.class)
                    .newInstance(LocalDate.of(2024, 4, 30)
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant().toEpochMilli());
            wrap.put("ts", ts);
            String out = m.writeValueAsString(wrap);
            assertTrue(out.contains("\"2024-04-30\""),
                    "java.sql.Timestamp must route as Date with mapper format, got: " + out);
        } catch (ClassNotFoundException unavailable) {
            // java.sql not on this test runtime — skip silently.
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not construct java.sql.Timestamp", e);
        }
    }

    @Test
    void records_honorFieldLevelFormat() {
        // R8 audit P1 regression: createRecordWriter previously passed
        // null for the format slot, so @JSONField(format=...) on a record
        // component was silently dropped. mapper-level dateFormat then
        // "won" over a field-level annotation that should have taken
        // precedence — inconsistent vs the POJO writer.
        // Post-fix: createRecordWriter calls resolveFormat and threads
        // the format string through to FieldWriter.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd HH:mm:ss").build();
        DateRecord r = new DateRecord(
                1L,
                LocalDateTime.of(2024, 4, 30, 12, 34, 56),
                LocalDateTime.of(2024, 4, 30, 12, 34, 56));
        String out = m.writeValueAsString(r);
        assertTrue(out.contains("\"ts\":\"2024-04-30\""),
                "record component @JSONField(format) must win over mapper.dateFormat, got: " + out);
        assertTrue(out.contains("\"tsDefault\":\"2024-04-30 12:34:56\""),
                "record component without @JSONField must use mapper.dateFormat, got: " + out);
    }

    @Test
    void mapperLevel_dateFormat_doesNotCrashOnYearYearMonthMonthDay() {
        // Round 4 audit P1: Year / YearMonth / MonthDay are TemporalAccessor
        // instances but have no date-of-the-day semantics. Pre-PR, writeAny
        // dispatched them through the ObjectWriter fallback (Year emits as
        // int, YearMonth/MonthDay as ISO string). PR #153's initial merged
        // guard captured them under "instanceof TemporalAccessor" and tried
        // to convert via toLocalDate / toLocalDateTime, which throws.
        //
        // Fix: outer guard switched to whitelist of date-shaped types
        // (LocalDate, LocalDateTime, Instant, ZonedDateTime, OffsetDateTime,
        // Date). Year/YearMonth/MonthDay fall through to the existing
        // ObjectWriter dispatch — preserved pre-PR behavior.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        Map<String, Object> wrap = new LinkedHashMap<>();
        wrap.put("y", java.time.Year.of(2024));
        wrap.put("ym", java.time.YearMonth.of(2024, 4));
        wrap.put("md", java.time.MonthDay.of(4, 30));
        // Must not crash — the date-shaped mapper format does not apply
        // to these partial-date types, so they emit their natural shape.
        String out = m.writeValueAsString(wrap);
        assertTrue(out.contains("2024"), "expected Year value preserved, got: " + out);
        assertTrue(out.contains("04") || out.contains("4"), "expected month digit, got: " + out);
        assertTrue(out.contains("30"), "expected day digit, got: " + out);
    }

    @Test
    void noMapperFormat_yearYearMonthMonthDay_preservedFromPrePR() {
        // Sanity that the whitelist switch didn't break the no-format
        // path either — Year/YearMonth/MonthDay should round-trip
        // through the ObjectWriter fallback exactly like pre-PR.
        ObjectMapper m = ObjectMapper.builder().build();
        Map<String, Object> wrap = new LinkedHashMap<>();
        wrap.put("y", java.time.Year.of(2024));
        wrap.put("ym", java.time.YearMonth.of(2024, 4));
        wrap.put("md", java.time.MonthDay.of(4, 30));
        String out = m.writeValueAsString(wrap);
        // Don't pin exact shapes (those are fj3 defaults outside this PR's
        // scope); just confirm no crash and values appear.
        assertNotNull(out);
        assertFalse(out.isEmpty());
    }

    @Test
    void mapperLevel_dateFormat_appliesToMapKeys() {
        // R10 audit P1 regression: Map keys that are Date / Temporal
        // values were emitted via String.valueOf(key) — Date.toString()
        // produces "Tue Apr 30 16:00:00 CST 2024" (locale-dependent, ambiguous
        // tz abbreviation, non-round-trippable). LocalDateTime.toString()
        // emits ISO without seconds when seconds==0. Both diverged from the
        // value-side mapper.dateFormat application.
        // Post-fix: keys go through the same DateFormatPattern.formatToString
        // hook the value side uses.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        java.util.Map<LocalDateTime, String> m1 = new LinkedHashMap<>();
        m1.put(LocalDateTime.of(2024, 4, 30, 12, 34, 56), "x");
        String out1 = m.writeValueAsString(m1);
        assertEquals("{\"2024-04-30\":\"x\"}", out1);

        java.util.Map<Date, String> m2 = new LinkedHashMap<>();
        long epochMs = LocalDate.of(2024, 4, 30)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        m2.put(new Date(epochMs), "y");
        String out2 = m.writeValueAsString(m2);
        assertEquals("{\"2024-04-30\":\"y\"}", out2,
                "Date map key must format via mapper.dateFormat, not Date.toString()");
    }

    @Test
    void mapperLevel_millis_appliesToMapKeys() {
        // millis token emits epoch ms. As a JSON object key, the numeric
        // form is preserved as a quoted decimal string (JSON spec mandates
        // string keys; we don't emit unquoted numbers as keys).
        ObjectMapper m = ObjectMapper.builder().dateFormat("millis").build();
        java.util.Map<Date, String> wrap = new LinkedHashMap<>();
        long epochMs = 1714521600000L;
        wrap.put(new Date(epochMs), "x");
        String out = m.writeValueAsString(wrap);
        assertEquals("{\"" + epochMs + "\":\"x\"}", out);
    }

    @Test
    void mapperLevel_dateFormat_skipsMapKey_forChronologyDates() {
        // R13 audit P2: HijrahDate / JapaneseDate / ThaiBuddhistDate /
        // MinguoDate are TemporalAccessor instances but NOT in the
        // value-side date-shape whitelist. Pre-fix, R10's blacklist
        // approach passed them through to fmt.formatToString → toLocalDate
        // → throws JSONException. Post-fix, key-side whitelist matches
        // value-side exactly: only LocalDateTime/LocalDate/Instant/
        // ZonedDateTime/OffsetDateTime/Date land in the format hook;
        // chronology subtypes fall through to String.valueOf.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        java.util.Map<java.time.chrono.HijrahDate, String> wrap = new LinkedHashMap<>();
        java.time.chrono.HijrahDate h = java.time.chrono.HijrahDate.from(LocalDate.of(2024, 4, 30));
        wrap.put(h, "x");
        // Must not throw — chronology dates emit via toString, the same as
        // they would without mapper.dateFormat.
        String out = m.writeValueAsString(wrap);
        assertTrue(out.contains("\"x\""),
                "HijrahDate Map key must not crash, got: " + out);
        assertTrue(out.contains(h.toString()),
                "expected HijrahDate.toString() in output, got: " + out);
    }

    @Test
    void mapperLevel_dateFormat_skipsMapKey_forTimeOnlyAndYearLikeTypes() {
        // LocalTime / OffsetTime / Year / YearMonth / MonthDay map keys
        // bypass mapper format (same scope as the writeAny outer guard).
        // They emit via toString — partial-date types have no
        // date-shaped projection.
        ObjectMapper m = ObjectMapper.builder().dateFormat("yyyy-MM-dd").build();
        java.util.Map<java.time.LocalTime, String> wrap = new LinkedHashMap<>();
        wrap.put(java.time.LocalTime.of(12, 34, 56), "x");
        String out = m.writeValueAsString(wrap);
        // LocalTime.toString = "12:34:56"
        assertEquals("{\"12:34:56\":\"x\"}", out,
                "LocalTime map key must NOT be re-routed through date-shaped mapper format, got: " + out);
    }

    @Test
    void mapperLevel_millis_appliesToMapValues() {
        // Map<String, Date> with "millis" emits epoch ms as JSON number,
        // not the ISO string fallback.
        ObjectMapper m = ObjectMapper.builder().dateFormat("millis").build();
        long epochMs = 1714521600000L;
        Map<String, Date> wrap = new LinkedHashMap<>();
        wrap.put("d", new Date(epochMs));
        assertEquals("{\"d\":" + epochMs + "}", m.writeValueAsString(wrap));
    }
}

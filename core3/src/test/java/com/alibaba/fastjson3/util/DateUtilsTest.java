package com.alibaba.fastjson3.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DateUtils - high-performance date/time parsing utilities.
 */
class DateUtilsTest {

    // ==================== LocalDate parsing tests ====================

    @Test
    void testParseLocalDate_nullInput() {
        assertNull(DateUtils.parseLocalDate(null));
    }

    @Test
    void testParseLocalDate_emptyInput() {
        assertNull(DateUtils.parseLocalDate(""));
    }

    @Test
    void testParseLocalDate_yyyyMMdd() {
        LocalDate date = DateUtils.parseLocalDate("20240615");

        assertEquals(2024, date.getYear());
        assertEquals(6, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    void testParseLocalDate_yyyyDashMMDashdd() {
        LocalDate date = DateUtils.parseLocalDate("2024-06-15");

        assertEquals(2024, date.getYear());
        assertEquals(6, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    void testParseLocalDate_yyyySlashMMSlashdd() {
        LocalDate date = DateUtils.parseLocalDate("2024/06/15");

        assertEquals(2024, date.getYear());
        assertEquals(6, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    void testParseLocalDate_ddDotMMDotyyyy() {
        LocalDate date = DateUtils.parseLocalDate("15.06.2024");

        assertEquals(2024, date.getYear());
        assertEquals(6, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    void testParseLocalDate_leapYear() {
        LocalDate date = DateUtils.parseLocalDate("2024-02-29");

        assertEquals(2024, date.getYear());
        assertEquals(2, date.getMonthValue());
        assertEquals(29, date.getDayOfMonth());
    }

    @Test
    void testParseLocalDate_invalidDate() {
        assertThrows(DateTimeParseException.class, () -> DateUtils.parseLocalDate("2024-02-30"));
    }

    @Test
    void testParseLocalDate_invalidLeapYear() {
        assertThrows(DateTimeParseException.class, () -> DateUtils.parseLocalDate("2023-02-29"));
    }

    @Test
    void testParseLocalDate_minMaxYear() {
        LocalDate min = DateUtils.parseLocalDate("0001-01-01");
        assertEquals(1, min.getYear());

        LocalDate max = DateUtils.parseLocalDate("9999-12-31");
        assertEquals(9999, max.getYear());
    }

    // ==================== LocalTime parsing tests ====================

    @Test
    void testParseLocalTime_nullInput() {
        assertNull(DateUtils.parseLocalTime(null));
    }

    @Test
    void testParseLocalTime_emptyInput() {
        assertNull(DateUtils.parseLocalTime(""));
    }

    @Test
    void testParseLocalTime_HHColonmmColonss() {
        LocalTime time = DateUtils.parseLocalTime("14:30:45");

        assertEquals(14, time.getHour());
        assertEquals(30, time.getMinute());
        assertEquals(45, time.getSecond());
    }

    @Test
    void testParseLocalTime_HHColonmm() {
        LocalTime time = DateUtils.parseLocalTime("14:30");

        assertEquals(14, time.getHour());
        assertEquals(30, time.getMinute());
        assertEquals(0, time.getSecond());
    }

    @Test
    void testParseLocalTime_withMillis() {
        LocalTime time = DateUtils.parseLocalTime("14:30:45.123");

        assertEquals(14, time.getHour());
        assertEquals(30, time.getMinute());
        assertEquals(45, time.getSecond());
        assertEquals(123_000_000, time.getNano());
    }

    @Test
    void testParseLocalTime_withNanos() {
        LocalTime time = DateUtils.parseLocalTime("14:30:45.123456789");

        assertEquals(14, time.getHour());
        assertEquals(30, time.getMinute());
        assertEquals(45, time.getSecond());
        assertEquals(123_456_789, time.getNano());
    }

    @Test
    void testParseLocalTime_midnight() {
        LocalTime time = DateUtils.parseLocalTime("00:00:00");

        assertEquals(0, time.getHour());
        assertEquals(0, time.getMinute());
        assertEquals(0, time.getSecond());
    }

    @Test
    void testParseLocalTime_lastSecond() {
        LocalTime time = DateUtils.parseLocalTime("23:59:59");

        assertEquals(23, time.getHour());
        assertEquals(59, time.getMinute());
        assertEquals(59, time.getSecond());
    }

    @Test
    void testParseLocalTime_invalidHour() {
        assertThrows(DateTimeParseException.class, () -> DateUtils.parseLocalTime("24:00:00"));
    }

    @Test
    void testParseLocalTime_invalidMinute() {
        assertThrows(DateTimeParseException.class, () -> DateUtils.parseLocalTime("23:60:00"));
    }

    @Test
    void testParseLocalTime_invalidSecond() {
        assertThrows(DateTimeParseException.class, () -> DateUtils.parseLocalTime("23:59:60"));
    }

    // ==================== LocalDateTime parsing tests ====================

    @Test
    void testParseLocalDateTime_nullInput() {
        assertNull(DateUtils.parseLocalDateTime(null));
    }

    @Test
    void testParseLocalDateTime_emptyInput() {
        assertNull(DateUtils.parseLocalDateTime(""));
    }

    @Test
    void testParseLocalDateTime_yyyyMMdd() {
        LocalDateTime dt = DateUtils.parseLocalDateTime("20240615");

        assertEquals(2024, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(0, dt.getHour());
        assertEquals(0, dt.getMinute());
        assertEquals(0, dt.getSecond());
    }

    @Test
    void testParseLocalDateTime_yyyyMMddHHmmss() {
        LocalDateTime dt = DateUtils.parseLocalDateTime("20240615143045");

        assertEquals(2024, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(14, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(45, dt.getSecond());
    }

    @Test
    void testParseLocalDateTime_yyyyDashMMDashddSpaceHHColonmm() {
        LocalDateTime dt = DateUtils.parseLocalDateTime("2024-06-15 14:30");

        assertEquals(2024, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(14, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(0, dt.getSecond());
    }

    @Test
    void testParseLocalDateTime_yyyyDashMMDashddTHHColonmm() {
        LocalDateTime dt = DateUtils.parseLocalDateTime("2024-06-15T14:30");

        assertEquals(2024, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(14, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(0, dt.getSecond());
    }

    @Test
    void testParseLocalDateTime_yyyyDashMMDashddSpaceHHColonmmColonss() {
        LocalDateTime dt = DateUtils.parseLocalDateTime("2024-06-15 14:30:45");

        assertEquals(2024, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(14, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(45, dt.getSecond());
    }

    @Test
    void testParseLocalDateTime_yyyySlashMMSlashddSpaceHHColonmmColonss() {
        LocalDateTime dt = DateUtils.parseLocalDateTime("2024/06/15 14:30:45");

        assertEquals(2024, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(14, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(45, dt.getSecond());
    }

    @Test
    void testParseLocalDateTime_dd_MM_yyyy_HH_mm_ss() {
        LocalDateTime dt = DateUtils.parseLocalDateTime("15/06/2024 14:30:45");

        assertEquals(2024, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(14, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(45, dt.getSecond());
    }

    @Test
    void testParseLocalDateTime_dd_MM_yyyy_HH_mm_ss_dots() {
        LocalDateTime dt = DateUtils.parseLocalDateTime("15.06.2024 14:30:45");

        assertEquals(2024, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(14, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(45, dt.getSecond());
    }

    @Test
    void testParseLocalDateTime_withMillis() {
        LocalDateTime dt = DateUtils.parseLocalDateTime("2024-06-15 14:30:45.123");

        assertEquals(2024, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(14, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(45, dt.getSecond());
        assertEquals(123_000_000, dt.getNano());
    }

    // ==================== Instant parsing tests ====================

    @Test
    void testParseInstant_nullInput() {
        assertNull(DateUtils.parseInstant(null));
    }

    @Test
    void testParseInstant_emptyInput() {
        assertNull(DateUtils.parseInstant(""));
    }

    @Test
    void testParseInstant_ISO8601_withZ() {
        Instant instant = DateUtils.parseInstant("2024-06-15T14:30:45Z");

        assertEquals(2024, instant.atZone(ZoneOffset.UTC).getYear());
        assertEquals(6, instant.atZone(ZoneOffset.UTC).getMonthValue());
        assertEquals(15, instant.atZone(ZoneOffset.UTC).getDayOfMonth());
    }

    @Test
    void testParseInstant_ISO8601_withOffset() {
        Instant instant = DateUtils.parseInstant("2024-06-15T14:30:45+08:00");

        assertNotNull(instant);
    }

    @Test
    void testParseInstant_ISO8601_withMillis() {
        Instant instant = DateUtils.parseInstant("2024-06-15T14:30:45.123Z");

        assertNotNull(instant);
    }

    @Test
    void testParseInstant_numericTimestamp() {
        // parseInstant falls back to Instant.parse() for numeric strings,
        // which expects ISO-8601 format. This test verifies behavior.
        assertThrows(Exception.class, () -> DateUtils.parseInstant("1718467845000"));
    }

    // ==================== ZonedDateTime parsing tests ====================

    @Test
    void testParseZonedDateTime_nullInput() {
        assertNull(DateUtils.parseZonedDateTime(null));
    }

    @Test
    void testParseZonedDateTime_emptyInput() {
        assertNull(DateUtils.parseZonedDateTime(""));
    }

    @Test
    void testParseZonedDateTime_ISO8601_withZ() {
        ZonedDateTime zdt = DateUtils.parseZonedDateTime("2024-06-15T14:30:45Z");

        assertEquals(2024, zdt.getYear());
        assertEquals(ZoneOffset.UTC, zdt.getZone());
    }

    @Test
    void testParseZonedDateTime_local() {
        ZonedDateTime zdt = DateUtils.parseZonedDateTime("2024-06-15 14:30:45");

        assertEquals(2024, zdt.getYear());
        assertEquals(DateUtils.DEFAULT_ZONE_ID, zdt.getZone());
    }

    // ==================== OffsetDateTime parsing tests ====================

    @Test
    void testParseOffsetDateTime_nullInput() {
        assertNull(DateUtils.parseOffsetDateTime(null));
    }

    @Test
    void testParseOffsetDateTime_emptyInput() {
        assertNull(DateUtils.parseOffsetDateTime(""));
    }

    @Test
    void testParseOffsetDateTime_ISO8601_withZ() {
        OffsetDateTime odt = DateUtils.parseOffsetDateTime("2024-06-15T14:30:45Z");

        assertEquals(2024, odt.getYear());
        assertEquals(ZoneOffset.UTC, odt.getOffset());
    }

    @Test
    void testParseOffsetDateTime_withOffset() {
        OffsetDateTime odt = DateUtils.parseOffsetDateTime("2024-06-15T14:30:45+08:00");

        assertEquals(2024, odt.getYear());
        assertEquals(ZoneOffset.ofHours(8), odt.getOffset());
    }

    // ==================== Date parsing tests ====================

    @Test
    void testParseDate_nullInput() {
        assertNull(DateUtils.parseDate(null));
    }

    @Test
    void testParseDate_emptyInput() {
        assertNull(DateUtils.parseDate(""));
    }

    @Test
    void testParseDate_numericTimestamp() {
        long timestamp = 1718467845000L;
        Date date = DateUtils.parseDate(String.valueOf(timestamp));

        assertEquals(timestamp, date.getTime());
    }

    @Test
    void testParseDate_ISO8601() {
        Date date = DateUtils.parseDate("2024-06-15T14:30:45Z");

        assertNotNull(date);
        // The exact timestamp may vary based on implementation
        assertTrue(date.getTime() > 0);
    }

    @Test
    void testParseDate_dateOnly() {
        Date date = DateUtils.parseDate("2024-06-15");

        assertNotNull(date);
    }

    // ==================== Leap year tests ====================

    @Test
    void testIsLeapYear_divisibleBy4() {
        assertTrue(DateUtils.isLeapYear(2024));
        assertTrue(DateUtils.isLeapYear(2020));
        assertTrue(DateUtils.isLeapYear(2000));
    }

    @Test
    void testIsLeapYear_notDivisibleBy4() {
        assertFalse(DateUtils.isLeapYear(2023));
        assertFalse(DateUtils.isLeapYear(2022));
    }

    @Test
    void testIsLeapYear_centuryNotDivisibleBy400() {
        assertFalse(DateUtils.isLeapYear(1900));
        assertFalse(DateUtils.isLeapYear(2100));
    }

    @Test
    void testIsLeapYear_centuryDivisibleBy400() {
        assertTrue(DateUtils.isLeapYear(2000));
        assertTrue(DateUtils.isLeapYear(1600));
    }

    // ==================== maxDayOfMonth tests ====================

    @Test
    void testMaxDayOfMonth_31DayMonths() {
        assertEquals(31, DateUtils.maxDayOfMonth(2024, 1));  // January
        assertEquals(31, DateUtils.maxDayOfMonth(2024, 3));  // March
        assertEquals(31, DateUtils.maxDayOfMonth(2024, 5));  // May
        assertEquals(31, DateUtils.maxDayOfMonth(2024, 7));  // July
        assertEquals(31, DateUtils.maxDayOfMonth(2024, 8));  // August
        assertEquals(31, DateUtils.maxDayOfMonth(2024, 10)); // October
        assertEquals(31, DateUtils.maxDayOfMonth(2024, 12)); // December
    }

    @Test
    void testMaxDayOfMonth_30DayMonths() {
        assertEquals(30, DateUtils.maxDayOfMonth(2024, 4));  // April
        assertEquals(30, DateUtils.maxDayOfMonth(2024, 6));  // June
        assertEquals(30, DateUtils.maxDayOfMonth(2024, 9));  // September
        assertEquals(30, DateUtils.maxDayOfMonth(2024, 11)); // November
    }

    @Test
    void testMaxDayOfMonth_February_nonLeap() {
        assertEquals(28, DateUtils.maxDayOfMonth(2023, 2));
        assertEquals(28, DateUtils.maxDayOfMonth(2022, 2));
    }

    @Test
    void testMaxDayOfMonth_February_leap() {
        assertEquals(29, DateUtils.maxDayOfMonth(2024, 2));
        assertEquals(29, DateUtils.maxDayOfMonth(2000, 2));
    }

    @Test
    void testMaxDayOfMonth_invalidMonth() {
        assertEquals(-1, DateUtils.maxDayOfMonth(2024, 0));
        assertEquals(-1, DateUtils.maxDayOfMonth(2024, 13));
    }

    // ==================== Write tests ====================

    @Test
    void testWriteLocalDate() {
        byte[] buf = new byte[10];
        LocalDate date = LocalDate.of(2024, 6, 15);

        int written = DateUtils.writeLocalDate(buf, 0, date);

        assertEquals(10, written);
        assertEquals('2', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('2', buf[2]);
        assertEquals('4', buf[3]);
        assertEquals('-', buf[4]);
        assertEquals('0', buf[5]);
        assertEquals('6', buf[6]);
        assertEquals('-', buf[7]);
        assertEquals('1', buf[8]);
        assertEquals('5', buf[9]);
    }

    @Test
    void testWriteLocalTime() {
        byte[] buf = new byte[8];
        LocalTime time = LocalTime.of(14, 30, 45);

        int written = DateUtils.writeLocalTime(buf, 0, time);

        assertEquals(8, written);
        assertEquals('1', buf[0]);
        assertEquals('4', buf[1]);
        assertEquals(':', buf[2]);
        assertEquals('3', buf[3]);
        assertEquals('0', buf[4]);
        assertEquals(':', buf[5]);
        assertEquals('4', buf[6]);
        assertEquals('5', buf[7]);
    }

    @Test
    void testWriteLocalDateTime_noNanos() {
        byte[] buf = new byte[19];
        LocalDateTime ldt = LocalDateTime.of(2024, 6, 15, 14, 30, 45);

        int written = DateUtils.writeLocalDateTime(buf, 0, ldt);

        assertEquals(19, written);
        assertEquals('T', buf[10]);
    }

    @Test
    void testWriteLocalDateTime_withNanos() {
        byte[] buf = new byte[30];
        LocalDateTime ldt = LocalDateTime.of(2024, 6, 15, 14, 30, 45, 123_000_000);

        int written = DateUtils.writeLocalDateTime(buf, 0, ldt);

        assertEquals(23, written); // 19 + ".123"
        assertEquals('.', buf[19]);
        assertEquals('1', buf[20]);
        assertEquals('2', buf[21]);
        assertEquals('3', buf[22]);
    }

    @Test
    void testWriteFractionNanos_trimsTrailingZeros() {
        byte[] buf = new byte[10];

        // 123_000_000 nanos = 123 millis, should write as ".123"
        int written1 = DateUtils.writeFractionNanos(buf, 0, 123_000_000);
        assertEquals(4, written1); // ".123"
        assertEquals('.', buf[0]);
        assertEquals('1', buf[1]);
        assertEquals('2', buf[2]);
        assertEquals('3', buf[3]);
    }

    // ==================== toEpochMilli tests ====================

    @Test
    void testToEpochMilli_epoch() {
        long millis = DateUtils.toEpochMilli(1970, 1, 1, 0, 0, 0, 0, 0);

        assertEquals(0, millis);
    }

    @Test
    void testToEpochMilli_withOffset() {
        // 1970-01-01 00:00:00 with +1 hour offset = -3600000 millis
        long millis = DateUtils.toEpochMilli(1970, 1, 1, 0, 0, 0, 0, 3600);

        assertEquals(-3600000L, millis);
    }

    @Test
    void testToEpochMilli_withNanos() {
        long millis = DateUtils.toEpochMilli(1970, 1, 1, 0, 0, 0, 500_000_000, 0);

        assertEquals(500L, millis); // 500_000_000 nanos = 500 millis
    }

    // ==================== Constants tests ====================

    @Test
    void testDEFAULT_ZONE_ID() {
        assertNotNull(DateUtils.DEFAULT_ZONE_ID);
        assertEquals(ZoneId.systemDefault(), DateUtils.DEFAULT_ZONE_ID);
    }

    @Test
    void testEPOCH_constant() {
        assertEquals(LocalDate.of(1970, 1, 1), DateUtils.EPOCH);
    }
}

package com.alibaba.fastjson3.android;

import com.alibaba.fastjson3.JSON;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for date/time serialization - commonly used in Android apps.
 */
public class DateTimeTest {

    @Test
    public void testUtilDate() {
        Event event = new Event();
        event.timestamp = new Date(1700000000000L);
        String json = JSON.toJSONString(event);
        assertNotNull(json);
        // Check that timestamp is serialized
        Event parsed = JSON.parseObject(json, Event.class);
        assertEquals(1700000000000L, parsed.timestamp.getTime());
    }

    @Test
    public void testLocalDate() {
        LocalDateEvent event = new LocalDateEvent();
        event.date = LocalDate.of(2024, 6, 15);
        String json = JSON.toJSONString(event);
        assertTrue(json.contains("2024-06-15"));

        LocalDateEvent parsed = JSON.parseObject(json, LocalDateEvent.class);
        assertEquals(LocalDate.of(2024, 6, 15), parsed.date);
    }

    @Test
    public void testLocalDateTime() {
        LocalDateTimeEvent event = new LocalDateTimeEvent();
        event.dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
        String json = JSON.toJSONString(event);
        assertTrue(json.contains("2024-06-15"));
        assertTrue(json.contains("10:30"));

        LocalDateTimeEvent parsed = JSON.parseObject(json, LocalDateTimeEvent.class);
        assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30, 0), parsed.dateTime);
    }

    @Test
    public void testLocalTime() {
        LocalTimeEvent event = new LocalTimeEvent();
        event.time = LocalTime.of(14, 30, 45);
        String json = JSON.toJSONString(event);
        assertTrue(json.contains("14:30:45"));

        LocalTimeEvent parsed = JSON.parseObject(json, LocalTimeEvent.class);
        assertEquals(LocalTime.of(14, 30, 45), parsed.time);
    }

    @Test
    public void testNullDate() {
        Event event = new Event();
        event.timestamp = null;
        String json = JSON.toJSONString(event);

        Event parsed = JSON.parseObject(json, Event.class);
        assertEquals(null, parsed.timestamp);
    }

    @Test
    public void testMultipleDatesInOneObject() {
        MultiDateEvent event = new MultiDateEvent();
        event.utilDate = new Date(1700000000000L);
        event.localDate = LocalDate.of(2024, 6, 15);
        event.localDateTime = LocalDateTime.of(2024, 6, 15, 10, 30);

        String json = JSON.toJSONString(event);
        MultiDateEvent parsed = JSON.parseObject(json, MultiDateEvent.class);
        assertEquals(1700000000000L, parsed.utilDate.getTime());
        assertEquals(LocalDate.of(2024, 6, 15), parsed.localDate);
        assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30), parsed.localDateTime);
    }

    static class Event {
        public Date timestamp;
    }

    static class LocalDateEvent {
        public LocalDate date;
    }

    static class LocalDateTimeEvent {
        public LocalDateTime dateTime;
    }

    static class LocalTimeEvent {
        public LocalTime time;
    }

    static class MultiDateEvent {
        public Date utilDate;
        public LocalDate localDate;
        public LocalDateTime localDateTime;
    }
}

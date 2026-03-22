package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for API compatibility methods (JSONObject/JSONArray/JSON) for fastjson2 migration.
 */
class APICompatibilityTest {

    // ==================== JSONObject.getObject ====================

    public static class Address {
        public String city;
        public String street;
    }

    @Test
    void jsonObject_getObject() {
        JSONObject obj = JSON.parseObject("{\"addr\":{\"city\":\"Shanghai\",\"street\":\"Nanjing Rd\"}}");
        Address addr = obj.getObject("addr", Address.class);
        assertNotNull(addr);
        assertEquals("Shanghai", addr.city);
    }

    @Test
    void jsonObject_getObject_null() {
        JSONObject obj = new JSONObject();
        assertNull(obj.getObject("missing", Address.class));
    }

    // ==================== JSONObject.getList ====================

    @Test
    void jsonObject_getList() {
        JSONObject obj = JSON.parseObject("{\"items\":[{\"city\":\"A\"},{\"city\":\"B\"}]}");
        List<Address> items = obj.getList("items", Address.class);
        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("A", items.get(0).city);
    }

    // ==================== JSONObject.getDate/Instant/LocalDate ====================

    @Test
    void jsonObject_getDate_fromMillis() {
        JSONObject obj = new JSONObject();
        obj.put("time", 1700000000000L);
        Date date = obj.getDate("time");
        assertNotNull(date);
        assertEquals(1700000000000L, date.getTime());
    }

    @Test
    void jsonObject_getDate_fromString() {
        JSONObject obj = new JSONObject();
        obj.put("time", "2023-11-14");
        Date date = obj.getDate("time");
        assertNotNull(date);
    }

    @Test
    void jsonObject_getInstant() {
        JSONObject obj = new JSONObject();
        obj.put("time", 1700000000000L);
        Instant instant = obj.getInstant("time");
        assertNotNull(instant);
        assertEquals(1700000000000L, instant.toEpochMilli());
    }

    @Test
    void jsonObject_getLocalDate() {
        JSONObject obj = new JSONObject();
        obj.put("date", "2023-11-14");
        LocalDate date = obj.getLocalDate("date");
        assertEquals(LocalDate.of(2023, 11, 14), date);
    }

    @Test
    void jsonObject_getLocalDateTime() {
        JSONObject obj = new JSONObject();
        obj.put("dt", "2023-11-14T10:30:00");
        LocalDateTime dt = obj.getLocalDateTime("dt");
        assertEquals(LocalDateTime.of(2023, 11, 14, 10, 30, 0), dt);
    }

    @Test
    void jsonObject_getLocalTime() {
        JSONObject obj = new JSONObject();
        obj.put("t", "10:30:00");
        LocalTime t = obj.getLocalTime("t");
        assertEquals(LocalTime.of(10, 30, 0), t);
    }

    // ==================== JSONObject.getByte/Short ====================

    @Test
    void jsonObject_getByte() {
        JSONObject obj = new JSONObject();
        obj.put("b", 42);
        assertEquals((byte) 42, obj.getByteValue("b"));
    }

    @Test
    void jsonObject_getShort() {
        JSONObject obj = new JSONObject();
        obj.put("s", 1000);
        assertEquals((short) 1000, obj.getShortValue("s"));
    }

    // ==================== JSONObject.putObject/putArray ====================

    @Test
    void jsonObject_putObject() {
        JSONObject obj = new JSONObject();
        JSONObject child = obj.putObject("child");
        child.put("name", "test");
        assertEquals("{\"child\":{\"name\":\"test\"}}", obj.toString());
    }

    @Test
    void jsonObject_putArray() {
        JSONObject obj = new JSONObject();
        JSONArray arr = obj.putArray("items");
        arr.add(1);
        arr.add(2);
        assertTrue(obj.toString().contains("\"items\":[1,2]"));
    }

    // ==================== JSONObject.getByPath ====================

    @Test
    void jsonObject_getByPath() {
        JSONObject obj = JSON.parseObject("{\"a\":{\"b\":{\"c\":42}}}");
        Object val = obj.getByPath("$.a.b.c");
        assertEquals(42, ((Number) val).intValue());
    }

    // ==================== JSONObject.toJavaObject(Type) ====================

    @Test
    void jsonObject_toJavaObject_type() {
        JSONObject obj = JSON.parseObject("{\"city\":\"Shanghai\",\"street\":\"Nanjing Rd\"}");
        Address addr = obj.toJavaObject(Address.class);
        assertEquals("Shanghai", addr.city);
    }

    // ==================== JSON.parseObject(InputStream) ====================

    @Test
    void json_parseObject_inputStream() {
        byte[] bytes = "{\"city\":\"Shanghai\"}".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        Address addr = JSON.parseObject(in, Address.class);
        assertEquals("Shanghai", addr.city);
    }

    @Test
    void json_parseObject_inputStream_typed() {
        byte[] bytes = "{\"city\":\"Shanghai\"}".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        Address addr = JSON.parseObject(in, Address.class);
        assertEquals("Shanghai", addr.city);
    }

    // ==================== ReadFeature new values exist ====================

    @Test
    void readFeature_newValues() {
        assertNotNull(ReadFeature.ErrorOnEnumNotMatch);
        assertNotNull(ReadFeature.IgnoreSetNullValue);
        assertNotNull(ReadFeature.UseNativeObject);
        assertNotNull(ReadFeature.NonZeroNumberCastToBooleanAsTrue);
        assertNotNull(ReadFeature.DisableReferenceDetect);
        assertNotNull(ReadFeature.UseLongForInts);
        assertNotNull(ReadFeature.NonErrorOnNumberOverflow);
        // All should have unique masks
        long combined = ReadFeature.of(
                ReadFeature.ErrorOnEnumNotMatch,
                ReadFeature.IgnoreSetNullValue,
                ReadFeature.UseNativeObject,
                ReadFeature.NonZeroNumberCastToBooleanAsTrue,
                ReadFeature.DisableReferenceDetect,
                ReadFeature.UseLongForInts,
                ReadFeature.NonErrorOnNumberOverflow
        );
        assertEquals(7, Long.bitCount(combined));
    }
}

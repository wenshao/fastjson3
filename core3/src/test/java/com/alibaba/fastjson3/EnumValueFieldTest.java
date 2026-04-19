package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code @JSONField(value=true)} declared on an enum accessor must drive both
 * serialization and deserialization when that enum is held as a field of
 * another class — not just when it is the root object. Prior to the fix, the
 * field-writer fast path used {@code enum.name()} bytes pre-cached per ordinal,
 * so the documented pattern in the {@code JSONField.value} javadoc silently
 * emitted the enum's {@code name()} instead of the value-method output.
 */
public class EnumValueFieldTest {
    static final ObjectMapper MAPPER = ObjectMapper.shared();

    // ==================== String-valued enum ====================

    public enum Status {
        ACTIVE("active"),
        INACTIVE("inactive");

        private final String code;

        Status(String c) {
            this.code = c;
        }

        @JSONField(value = true)
        public String getCode() {
            return code;
        }
    }

    public static class Account {
        public String user;
        public Status status;
    }

    @Test
    public void stringValuedEnumSerializesViaValueMethod() {
        Account a = new Account();
        a.user = "alice";
        a.status = Status.ACTIVE;

        String json = MAPPER.writeValueAsString(a);
        assertTrue(json.contains("\"status\":\"active\""), json);
        assertFalse(json.contains("\"ACTIVE\""), "must not emit enum.name(): " + json);
    }

    @Test
    public void stringValuedEnumDeserializesViaValueMap() {
        Account back = JSON.parse("{\"user\":\"alice\",\"status\":\"active\"}", Account.class);
        assertEquals(Status.ACTIVE, back.status);
    }

    @Test
    public void stringValuedEnumDeserializeNameStillWorks() {
        // Lenient: accept enum.name() as well, for cross-compat with callers
        // that don't know about the value method.
        Account back = JSON.parse("{\"user\":\"alice\",\"status\":\"ACTIVE\"}", Account.class);
        assertEquals(Status.ACTIVE, back.status);
    }

    @Test
    public void stringValuedEnumRoundTrip() {
        Account a = new Account();
        a.user = "bob";
        a.status = Status.INACTIVE;
        String json = MAPPER.writeValueAsString(a);
        Account back = JSON.parse(json, Account.class);
        assertEquals("bob", back.user);
        assertEquals(Status.INACTIVE, back.status);
    }

    // ==================== Integer-valued enum ====================

    public enum Priority {
        LOW(1), HIGH(2), CRITICAL(3);

        private final int code;

        Priority(int c) {
            this.code = c;
        }

        @JSONField(value = true)
        public int getCode() {
            return code;
        }
    }

    public static class Task {
        public String name;
        public Priority priority;
    }

    @Test
    public void intValuedEnumSerializesAsNumber() {
        Task t = new Task();
        t.name = "build";
        t.priority = Priority.HIGH;

        String json = MAPPER.writeValueAsString(t);
        assertTrue(json.contains("\"priority\":2"), json);
    }

    @Test
    public void intValuedEnumDeserializesFromNumber() {
        Task back = JSON.parse("{\"name\":\"build\",\"priority\":2}", Task.class);
        assertEquals(Priority.HIGH, back.priority);
    }

    @Test
    public void intValuedEnumRoundTrip() {
        Task t = new Task();
        t.name = "ship";
        t.priority = Priority.CRITICAL;
        String json = MAPPER.writeValueAsString(t);
        Task back = JSON.parse(json, Task.class);
        assertEquals(Priority.CRITICAL, back.priority);
    }

    // ==================== Enum as root (pre-existing behavior must still work) ====================

    @Test
    public void rootEnumValueSerializationStillWorks() {
        String json = MAPPER.writeValueAsString(Status.ACTIVE);
        assertEquals("\"active\"", json);
    }

    // ==================== Enum without @JSONField(value=true) — unchanged ====================

    public enum PlainEnum {
        A, B, C
    }

    public static class PlainBean {
        public PlainEnum e;
    }

    @Test
    public void plainEnumFieldStillUsesName() {
        PlainBean p = new PlainBean();
        p.e = PlainEnum.B;
        String json = MAPPER.writeValueAsString(p);
        assertTrue(json.contains("\"e\":\"B\""), json);

        PlainBean back = JSON.parse(json, PlainBean.class);
        assertEquals(PlainEnum.B, back.e);
    }
}

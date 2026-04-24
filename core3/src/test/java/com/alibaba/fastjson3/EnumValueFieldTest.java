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

    // ==================== Ordinal fallback preserved for numeric value enums ====================
    //
    // A user whose enum has @JSONField(value=true) returning a code may still receive JSON
    // that sends the enum's ordinal (legacy client, mixed ecosystem). The Number path must
    // probe the value-map WITHOUT throwing on miss so the ordinal fallback stays reachable.

    public enum Channel {
        SMS(100), EMAIL(101);

        private final int code;

        Channel(int c) {
            this.code = c;
        }

        @JSONField(value = true)
        public int getCode() {
            return code;
        }
    }

    public static class Notification {
        public String subject;
        public Channel channel;
    }

    @Test
    public void numericEnumValueCodeHits() {
        // JSON uses the value-method code 101 → EMAIL.
        Notification back = JSON.parse("{\"subject\":\"hi\",\"channel\":101}", Notification.class);
        assertEquals(Channel.EMAIL, back.channel);
    }

    @Test
    public void numericEnumOrdinalFallbackStillWorks() {
        // JSON uses ordinal 0 (SMS) — must still resolve after the value-map probe misses.
        Notification back = JSON.parse("{\"subject\":\"hi\",\"channel\":0}", Notification.class);
        assertEquals(Channel.SMS, back.channel);
    }

    // ==================== Jackson @JsonValue symmetric with writer ====================

    public enum Tier {
        GOLD("gold"), SILVER("silver");

        private final String label;

        Tier(String l) {
            this.label = l;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String getLabel() {
            return label;
        }
    }

    public static class Membership {
        public String user;
        public Tier tier;
    }

    @Test
    public void jacksonJsonValueEnumReadBack() {
        ObjectMapper m = ObjectMapper.builder().useJacksonAnnotation(true).build();
        Membership src = new Membership();
        src.user = "alice";
        src.tier = Tier.GOLD;
        String json = m.writeValueAsString(src);
        assertTrue(json.contains("\"tier\":\"gold\""), json);

        Membership back = m.readValue(json, Membership.class);
        assertEquals(Tier.GOLD, back.tier);

        // Lenient name-based read still works for callers that emit enum.name().
        Membership lenient = m.readValue("{\"user\":\"bob\",\"tier\":\"SILVER\"}", Membership.class);
        assertEquals(Tier.SILVER, lenient.tier);
    }

    @Test
    public void jacksonJsonValueRespectsUseJacksonAnnotationOptIn() {
        // Default mapper (useJacksonAnnotation=false): writer emits enum.name(),
        // reader's value-map does NOT include @JsonValue entries, so round-trip stays
        // name-based. Asserting that the 'gold' label DOES NOT deserialize when the
        // flag is off locks the opt-in contract.
        ObjectMapper offMapper = ObjectMapper.builder().build();
        Membership onName = offMapper.readValue("{\"tier\":\"SILVER\"}", Membership.class);
        assertEquals(Tier.SILVER, onName.tier);

        JSONException ex = assertThrows(JSONException.class,
                () -> offMapper.readValue("{\"tier\":\"gold\"}", Membership.class));
        assertTrue(ex.getMessage().contains("no enum constant"), ex.getMessage());
    }

    // ==================== Precise numeric match (no longValue() truncation) ====================

    public enum Rate {
        LOW(1.1),
        HIGH(1.9);

        private final double code;

        Rate(double c) {
            this.code = c;
        }

        @JSONField(value = true)
        public double getCode() {
            return code;
        }
    }

    public static class Score {
        public Rate rate;
    }

    @Test
    public void fractionalEnumValuesDoNotCollapse() {
        Score low = JSON.parse("{\"rate\":1.1}", Score.class);
        assertEquals(Rate.LOW, low.rate);

        Score high = JSON.parse("{\"rate\":1.9}", Score.class);
        assertEquals(Rate.HIGH, high.rate);
    }

    // AutoObjectWriterProvider fix: classes that hold an enum field whose
    // enum declares @JSONField(value=true) must be routed away from the
    // ASM TYPE_ENUM fast path (which bakes enum.name() per ordinal) into
    // the reflection writer (which honours findValueWriter). Uses fresh
    // fixture classes — Grade / ReportCard — so no prior test warms up
    // the shared-provider cache for these types.

    public enum Grade {
        A("A+"), B("B"), C("C-");
        private final String code;
        Grade(String c) { this.code = c; }
        @JSONField(value = true)
        public String getCode() { return code; }
    }

    public static class ReportCard {
        public String student;
        public Grade grade;
    }

    @Test
    public void autoProviderRoutesEnumValueClassToReflection() {
        ReportCard rc = new ReportCard();
        rc.student = "carol";
        rc.grade = Grade.A;
        String json = MAPPER.writeValueAsString(rc);
        assertTrue(json.contains("\"grade\":\"A+\""),
                "AUTO provider must honour @JSONField(value=true) on enum field: " + json);
        assertFalse(json.contains("\"A\","), "must not emit enum.name(): " + json);

        // Round-trip: the reflect path's inverse value map accepts both the
        // code ("A+") and the enum name ("A") — see stringValuedEnumRoundTrip.
        ReportCard back = MAPPER.readValue(json, ReportCard.class);
        assertEquals("carol", back.student);
        assertEquals(Grade.A, back.grade);
    }

    // ==================== canGenerate gate coverage (qwen review) ====================
    //
    // Below cover the additional paths the first qwen review surfaced:
    //   (r3) getter-only enum properties (no declared field)
    //   (r3) explicit WriterCreatorType.ASM (not just AUTO)
    //
    // Jackson @JsonValue isn't included in the gate — when
    // useJacksonAnnotation=true, ObjectMapper routes around the writer
    // provider entirely (ObjectMapper:1648), so the gate can't be
    // reached in Jackson-annotation mode.

    @Test
    public void asmExplicitModeAlsoRoutesEnumValueClassToReflection() {
        // Same fixture as above but via the explicit ASM provider.
        // canGenerate's hasEnumWithValueAccessor check now rejects, so
        // ObjectWriterCreatorASM falls back to the reflect writer.
        // Using Grade.C whose code ("C-") is unambiguously distinct
        // from enum.name() ("C").
        ObjectMapper asmMapper = ObjectMapper.builder()
                .writerCreatorType(com.alibaba.fastjson3.writer.WriterCreatorType.ASM)
                .build();
        ReportCard rc = new ReportCard();
        rc.student = "dave";
        rc.grade = Grade.C;
        String json = asmMapper.writeValueAsString(rc);
        assertTrue(json.contains("\"grade\":\"C-\""),
                "ASM provider must honour @JSONField(value=true) too: " + json);
        assertFalse(json.contains("\"grade\":\"C\""), json); // would be enum.name()
    }

    // Getter-only enum property: no declared field, only a calculated
    // getter returning the enum type. canGenerate must detect this shape.
    public static class GetterOnlyReport {
        private int internal;

        public GetterOnlyReport(int internal) {
            this.internal = internal;
        }

        public String getStudent() {
            return "eve";
        }

        public Grade getGrade() {
            return internal >= 90 ? Grade.A : Grade.B;
        }
    }

    @Test
    public void asmGateDetectsGetterOnlyEnumProperty() {
        GetterOnlyReport r = new GetterOnlyReport(95);
        String json = MAPPER.writeValueAsString(r);
        assertTrue(json.contains("\"grade\":\"A+\""),
                "calculated getter returning enum must also honour value-method: " + json);
        assertFalse(json.contains("\"A\","), json);
    }

}

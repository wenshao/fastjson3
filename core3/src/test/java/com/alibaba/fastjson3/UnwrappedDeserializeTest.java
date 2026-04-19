package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code @JSONField(unwrapped=true)} — write side flattens a nested POJO's
 * fields into the parent, read side must reverse it: inner-type field names
 * appearing at the parent level route into a lazily-constructed inner instance.
 */
public class UnwrappedDeserializeTest {
    static final ObjectMapper MAPPER = ObjectMapper.shared();

    // ==================== Simple name unwrap ====================

    public static class Name {
        public String first;
        public String last;
    }

    public static class Person {
        @JSONField(unwrapped = true)
        public Name name;
        public int age;
    }

    @Test
    public void unwrappedRoundTrip() {
        Person p = new Person();
        p.name = new Name();
        p.name.first = "John";
        p.name.last = "Doe";
        p.age = 30;

        String json = MAPPER.writeValueAsString(p);
        assertTrue(json.contains("\"first\":\"John\""), json);
        assertTrue(json.contains("\"last\":\"Doe\""), json);
        assertTrue(json.contains("\"age\":30"), json);

        Person back = JSON.parse(json, Person.class);
        assertEquals(30, back.age);
        assertNotNull(back.name);
        assertEquals("John", back.name.first);
        assertEquals("Doe", back.name.last);
    }

    @Test
    public void unwrappedOrderingIndependent() {
        // Inner field first, then outer
        Person back1 = JSON.parse("{\"first\":\"A\",\"age\":10,\"last\":\"B\"}", Person.class);
        assertEquals(10, back1.age);
        assertNotNull(back1.name);
        assertEquals("A", back1.name.first);
        assertEquals("B", back1.name.last);

        // Outer first, then inner
        Person back2 = JSON.parse("{\"age\":20,\"first\":\"C\",\"last\":\"D\"}", Person.class);
        assertEquals(20, back2.age);
        assertEquals("C", back2.name.first);
        assertEquals("D", back2.name.last);
    }

    @Test
    public void unwrappedAbsentKeepsInnerNull() {
        Person back = JSON.parse("{\"age\":5}", Person.class);
        assertEquals(5, back.age);
        assertNull(back.name);
    }

    // ==================== Primitive and mixed types ====================

    public static class Dimensions {
        public int width;
        public int height;
        public double weight;
    }

    public static class Package {
        public String id;
        @JSONField(unwrapped = true)
        public Dimensions dims;
    }

    @Test
    public void unwrappedPrimitives() {
        Package pkg = JSON.parse("{\"id\":\"pk1\",\"width\":10,\"height\":20,\"weight\":3.5}", Package.class);
        assertEquals("pk1", pkg.id);
        assertNotNull(pkg.dims);
        assertEquals(10, pkg.dims.width);
        assertEquals(20, pkg.dims.height);
        assertEquals(3.5, pkg.dims.weight);
    }

    // ==================== Name collision: outer wins ====================

    public static class InnerId {
        public String id;
        public String label;
    }

    public static class OuterId {
        public String id;  // collides with inner
        @JSONField(unwrapped = true)
        public InnerId inner;
    }

    @Test
    public void unwrappedOuterWinsOnCollision() {
        // Outer 'id' captures "outer-id"; 'label' routes into inner
        OuterId back = JSON.parse("{\"id\":\"outer-id\",\"label\":\"l1\"}", OuterId.class);
        assertEquals("outer-id", back.id);
        assertNotNull(back.inner);
        assertEquals("l1", back.inner.label);
        assertNull(back.inner.id); // inner.id never populated because outer consumed "id"
    }

    // ==================== Unwrapped hit short-circuits unknown-property handling ====================
    //
    // When unwrapped matches a name, that name must not also be reported as unknown —
    // asserted here via ErrorOnUnknownProperties: "first" and "last" (inner names)
    // must not trigger the error, only a genuinely unknown "spice" should.

    @Test
    public void unwrappedMatchDoesNotCountAsUnknown() {
        // With only inner names + age, must succeed even under ErrorOnUnknown.
        Person p = JSON.parseObject("{\"first\":\"A\",\"last\":\"B\",\"age\":1}",
                Person.class, ReadFeature.ErrorOnUnknownProperties);
        assertEquals("A", p.name.first);
        assertEquals("B", p.name.last);
        assertEquals(1, p.age);
    }

    @Test
    public void genuineUnknownStillErrorsUnderFeature() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"first\":\"A\",\"spice\":\"cinnamon\"}",
                        Person.class, ReadFeature.ErrorOnUnknownProperties));
        assertTrue(ex.getMessage().contains("unknown"), ex.getMessage());
    }
}

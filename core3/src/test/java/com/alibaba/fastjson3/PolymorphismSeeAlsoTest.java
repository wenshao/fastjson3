package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Polymorphic deserialization for abstract / interface types declared with
 * {@code @JSONType(seeAlso = ..., typeKey = ...)} — the shape shown in the
 * {@code JSONType} javadoc example. Prior to the fix, this path fell through
 * to {@code Unsafe.allocateInstance} and surfaced a raw {@code InstantiationException}.
 */
public class PolymorphismSeeAlsoTest {
    static final ObjectMapper MAPPER = ObjectMapper.shared();

    // ==================== Abstract parent (non-sealed) ====================

    @JSONType(seeAlso = {Cat.class, Dog.class}, typeKey = "kind")
    public static abstract class Animal {
        public String name;
    }

    @JSONType(typeName = "cat")
    public static class Cat extends Animal {
        public boolean indoor;
    }

    @JSONType(typeName = "dog")
    public static class Dog extends Animal {
        public String breed;
    }

    @Test
    public void abstractWriteEmitsDiscriminator() {
        Cat c = new Cat();
        c.name = "Whiskers";
        c.indoor = true;

        String json = MAPPER.writeValueAsString(c);
        assertTrue(json.contains("\"kind\":\"cat\""), "subtype must inherit typeKey from parent: " + json);
    }

    @Test
    public void abstractReadDispatchesByTypeKey() {
        Cat parsed = (Cat) JSON.parse("{\"kind\":\"cat\",\"name\":\"Whiskers\",\"indoor\":true}", Animal.class);
        assertNotNull(parsed);
        assertEquals("Whiskers", parsed.name);
        assertTrue(parsed.indoor);
    }

    @Test
    public void abstractRoundTripList() {
        Cat c = new Cat(); c.name = "Whiskers"; c.indoor = true;
        Dog d = new Dog(); d.name = "Rex"; d.breed = "Husky";

        String json = MAPPER.writeValueAsString(Arrays.asList(c, d));
        List<Animal> back = JSON.parseList(json, Animal.class);

        assertEquals(2, back.size());
        assertInstanceOf(Cat.class, back.get(0));
        assertInstanceOf(Dog.class, back.get(1));
        assertEquals("Whiskers", back.get(0).name);
        assertEquals("Rex", back.get(1).name);
        assertEquals("Husky", ((Dog) back.get(1)).breed);
        assertTrue(((Cat) back.get(0)).indoor);
    }

    @Test
    public void abstractUnknownTypeNameYieldsClearError() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("{\"kind\":\"fish\",\"name\":\"Nemo\"}", Animal.class));
        assertTrue(ex.getMessage().contains("fish"),
                "error must name the unknown discriminator value: " + ex.getMessage());
    }

    @Test
    public void abstractMissingTypeKeyYieldsClearError() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("{\"name\":\"Nemo\"}", Animal.class));
        assertTrue(ex.getMessage().toLowerCase().contains("subtype")
                        || ex.getMessage().toLowerCase().contains("discriminator"),
                "error should mention missing subtype discriminator: " + ex.getMessage());
    }

    // ==================== Interface parent ====================

    @JSONType(seeAlso = {Square.class, Circle.class}, typeKey = "shape")
    public interface Shape {
    }

    @JSONType(typeName = "square")
    public static class Square implements Shape {
        public int side;
    }

    @JSONType(typeName = "circle")
    public static class Circle implements Shape {
        public double radius;
    }

    @Test
    public void interfaceRoundTrip() {
        Square s = new Square(); s.side = 5;
        String json = MAPPER.writeValueAsString(s);
        assertTrue(json.contains("\"shape\":\"square\""), json);

        Shape back = JSON.parse(json, Shape.class);
        assertInstanceOf(Square.class, back);
        assertEquals(5, ((Square) back).side);
    }

    // ==================== Abstract without polymorphic info: clear error ====================

    public static abstract class NonPolymorphicBase {
        public String name;
    }

    @Test
    public void abstractWithoutSeeAlsoThrowsHelpfully() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("{\"name\":\"x\"}", NonPolymorphicBase.class));
        String msg = ex.getMessage();
        assertTrue(msg.contains("abstract") || msg.contains("interface"), msg);
        assertTrue(msg.contains("seeAlso") || msg.contains("JsonTypeInfo") || msg.contains("sealed"),
                "message should point at the registration remedies: " + msg);
    }

    @Test
    public void interfaceWithoutSeeAlsoThrowsHelpfully() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("{\"name\":\"x\"}", Runnable.class));
        assertTrue(ex.getMessage().contains("interface"), ex.getMessage());
    }

    // ==================== Subtype without typeName → falls back to simple class name ====================

    @JSONType(seeAlso = {FallbackA.class, FallbackB.class}, typeKey = "t")
    public static abstract class FallbackBase {
        public int x;
    }

    public static class FallbackA extends FallbackBase {
    }

    public static class FallbackB extends FallbackBase {
    }

    @Test
    public void simpleClassNameFallbackRoundTrip() {
        FallbackA a = new FallbackA(); a.x = 42;
        String json = MAPPER.writeValueAsString(a);
        assertTrue(json.contains("\"t\":\"FallbackA\""), json);

        FallbackBase back = JSON.parse(json, FallbackBase.class);
        assertInstanceOf(FallbackA.class, back);
        assertEquals(42, back.x);
    }
}

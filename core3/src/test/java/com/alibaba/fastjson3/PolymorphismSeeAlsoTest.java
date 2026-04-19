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

    // ==================== Mix-in-only polymorphic metadata ====================
    //
    // The @JSONType(seeAlso=...) registration lives only on the mix-in class, not
    // on the third-party base class the user can't modify. The reader must consult
    // the mix-in's @JSONType when resolving seeAlso + typeKey — otherwise valid
    // payloads fail with "unknown type discriminator" because the resolved seeAlso
    // list is empty.

    public static abstract class ThirdPartyShape {
        public int area;
    }

    public static class ThirdPartyCircle extends ThirdPartyShape {
        public double radius;
    }

    public static class ThirdPartySquare extends ThirdPartyShape {
        public int side;
    }

    @JSONType(seeAlso = {ThirdPartyCircle.class, ThirdPartySquare.class}, typeKey = "shape")
    public interface ShapeMixIn {
    }

    @Test
    public void mixInOnlySeeAlsoPolymorphism() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(ThirdPartyShape.class, ShapeMixIn.class)
                .build();

        ThirdPartyShape parsed = mapper.readValue(
                "{\"shape\":\"ThirdPartyCircle\",\"area\":78,\"radius\":5.0}",
                ThirdPartyShape.class);
        assertInstanceOf(ThirdPartyCircle.class, parsed);
        assertEquals(78, parsed.area);
        assertEquals(5.0, ((ThirdPartyCircle) parsed).radius);
    }

    @Test
    public void mixInOnlySeeAlsoRoundTripViaWriter() {
        // Writer must honour the mix-in's @JSONType so the emitted discriminator
        // matches what the reader reads.
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(ThirdPartyShape.class, ShapeMixIn.class)
                .build();

        ThirdPartyCircle c = new ThirdPartyCircle();
        c.area = 78;
        c.radius = 5.0;

        String json = mapper.writeValueAsString(c);
        assertTrue(json.contains("\"shape\":\"ThirdPartyCircle\""),
                "writer must emit mix-in-derived typeKey: " + json);

        ThirdPartyShape back = mapper.readValue(json, ThirdPartyShape.class);
        assertInstanceOf(ThirdPartyCircle.class, back);
        assertEquals(78, back.area);
    }

    // ==================== Deep interface hierarchy — ancestor recursion ====================
    //
    // Impl → MidInterface → BaseInterface(@JSONType). Prior to the fix the writer
    // only looked at direct interfaces, so Impl emitted "@type" instead of
    // BaseInterface's custom typeKey, breaking round-trip.

    @JSONType(seeAlso = {Impl.class}, typeKey = "level")
    public interface BaseInterface {
    }

    public interface MidInterface extends BaseInterface {
    }

    @JSONType(typeName = "impl")
    public static class Impl implements MidInterface {
        public String label;
    }

    @Test
    public void deepInterfaceHierarchyInheritsTypeKey() {
        Impl i = new Impl();
        i.label = "hi";
        String json = MAPPER.writeValueAsString(i);
        assertTrue(json.contains("\"level\":\"impl\""),
                "subtype must inherit typeKey from BaseInterface across MidInterface: " + json);

        BaseInterface back = JSON.parse(json, BaseInterface.class);
        assertInstanceOf(Impl.class, back);
        assertEquals("hi", ((Impl) back).label);
    }

    // ==================== List of abstract types without registration ====================
    //
    // Prior to the fix, readList on an abstract element type silently cast raw
    // JSONObject elements to AbstractBase, leading to later ClassCastException.
    // Now it raises the targeted registration error at the list level.

    public static abstract class NoRegBase {
        public int x;
    }

    @Test
    public void parseListOfAbstractWithoutRegistrationErrorsEarly() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parseList("[{\"x\":1},{\"x\":2}]", NoRegBase.class));
        String msg = ex.getMessage();
        assertTrue(msg.contains("abstract") || msg.contains("interface"), msg);
        assertTrue(msg.contains("seeAlso") || msg.contains("sealed") || msg.contains("JsonTypeInfo"),
                "must point at remedies: " + msg);
    }

    // ==================== Custom mapper context propagates to nested writers ====================
    //
    // Polymorphic metadata attached to a non-shared mapper via addMixIn must reach
    // nested field / collection element serialisation. Prior to the fix, nested
    // writer lookups went through ObjectMapper.shared(), silently dropping the
    // custom mapper's mix-in so a bean field typed Animal emitted no discriminator.

    public static abstract class Pet {
        public String name;
    }

    public static class PetCat extends Pet {
    }

    public static class PetDog extends Pet {
    }

    @JSONType(seeAlso = {PetCat.class, PetDog.class}, typeKey = "petKind")
    public interface PetMixIn {
    }

    public static class PetOwner {
        public String id;
        public Pet pet;
    }

    @Test
    public void customMapperMixInReachesNestedFieldWriter() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(Pet.class, PetMixIn.class)
                .build();

        PetOwner owner = new PetOwner();
        owner.id = "o1";
        PetCat c = new PetCat();
        c.name = "Whiskers";
        owner.pet = c;

        String json = mapper.writeValueAsString(owner);
        assertTrue(json.contains("\"petKind\":\"PetCat\""),
                "nested polymorphic field must see the mapper's mix-in: " + json);
    }

    @Test
    public void customMapperMixInReachesCollectionElementWriter() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(Pet.class, PetMixIn.class)
                .build();

        PetCat c = new PetCat();
        c.name = "W";
        PetDog d = new PetDog();
        d.name = "R";

        String json = mapper.writeValueAsString(java.util.List.of(c, d));
        assertTrue(json.contains("\"petKind\":\"PetCat\""), json);
        assertTrue(json.contains("\"petKind\":\"PetDog\""), json);
    }
}

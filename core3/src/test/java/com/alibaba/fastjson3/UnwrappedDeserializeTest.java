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

    // ==================== Double-unwrap (nested @JSONField(unwrapped=true)) ====================

    public static class Handle {
        public String nick;
    }

    public static class NestedName {
        @JSONField(unwrapped = true)
        public Handle handle;
        public String first;
    }

    public static class Celebrity {
        @JSONField(unwrapped = true)
        public NestedName name;
        public int fame;
    }

    @Test
    public void doubleUnwrapRoundTrip() {
        Celebrity c = new Celebrity();
        c.name = new NestedName();
        c.name.first = "Mononymous";
        c.name.handle = new Handle();
        c.name.handle.nick = "Prince";
        c.fame = 11;

        String json = MAPPER.writeValueAsString(c);
        assertTrue(json.contains("\"nick\":\"Prince\""), json);
        assertTrue(json.contains("\"first\":\"Mononymous\""), json);
        assertTrue(json.contains("\"fame\":11"), json);

        Celebrity back = JSON.parse(json, Celebrity.class);
        assertEquals(11, back.fame);
        assertNotNull(back.name);
        assertEquals("Mononymous", back.name.first);
        assertNotNull(back.name.handle);
        assertEquals("Prince", back.name.handle.nick);
    }

    // ==================== Non-UTF8 parser path (non-ASCII input) ====================

    @Test
    public void unwrappedWorksOnStringPathWithNonAscii() {
        // Non-ASCII characters route parsing through the generic Str path in some
        // entry points; unwrapped routing must be available in readObjectGeneric,
        // not only in the UTF8 fast loop.
        Person back = JSON.parseObject("{\"first\":\"张\",\"last\":\"伟\",\"age\":7}", Person.class);
        assertEquals(7, back.age);
        assertNotNull(back.name);
        assertEquals("张", back.name.first);
        assertEquals("伟", back.name.last);
    }

    // ==================== Custom @JSONField(deserializeUsing=) on unwrapped inner field ====================

    public static class MoneyInner {
        @JSONField(deserializeUsing = MoneyReader.class)
        public java.math.BigDecimal amount;
    }

    public static class InvoicePayload {
        @JSONField(unwrapped = true)
        public MoneyInner money;
        public String id;
    }

    public static class MoneyReader implements com.alibaba.fastjson3.ObjectReader<java.math.BigDecimal> {
        @Override
        public java.math.BigDecimal readObject(JSONParser p, java.lang.reflect.Type type, Object name, long features) {
            String s = p.readString();
            if (s == null) {
                return null;
            }
            if (s.startsWith("$")) {
                s = s.substring(1);
            }
            return new java.math.BigDecimal(s);
        }
    }

    @Test
    public void unwrappedHonoursCustomDeserializeUsing() {
        InvoicePayload back = JSON.parse("{\"id\":\"inv1\",\"amount\":\"$42.50\"}", InvoicePayload.class);
        assertEquals("inv1", back.id);
        assertNotNull(back.money);
        assertEquals(new java.math.BigDecimal("42.50"), back.money.amount);
    }

    // ==================== Holder eligibility — ignored holder is skipped ====================

    @com.alibaba.fastjson3.annotation.JSONType(ignores = {"name"})
    public static class IgnoredHolder {
        @JSONField(unwrapped = true)
        public Name name;
        public int age;
    }

    @Test
    public void ignoredHolderSkipped() {
        IgnoredHolder back = JSON.parse("{\"first\":\"X\",\"last\":\"Y\",\"age\":3}", IgnoredHolder.class);
        assertEquals(3, back.age);
        assertNull(back.name, "ignored holder must not be hydrated from inner keys");
    }

    // ==================== No-arg constructor validated up front ====================

    public static class ParamOnlyInner {
        public final String v;
        public ParamOnlyInner(String v) {
            this.v = v;
        }
    }

    public static class ParamOnlyHolder {
        @JSONField(unwrapped = true)
        public ParamOnlyInner inner;
    }

    @Test
    public void noArgCtorValidationFailsWithClearMessage() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("{\"v\":\"hi\"}", ParamOnlyHolder.class));
        assertTrue(ex.getMessage().contains("no-arg constructor"), ex.getMessage());
    }

    // ==================== Setter-backed unwrapped ====================

    public static class SetterHolder {
        private Name name;
        public int age;

        @JSONField(unwrapped = true)
        public void setName(Name n) {
            this.name = n;
        }

        public Name getName() {
            return name;
        }
    }

    @Test
    public void setterBackedUnwrapped() {
        SetterHolder back = JSON.parse("{\"first\":\"S1\",\"last\":\"S2\",\"age\":9}", SetterHolder.class);
        assertEquals(9, back.age);
        assertNotNull(back.getName());
        assertEquals("S1", back.getName().first);
        assertEquals("S2", back.getName().last);
    }

    // ==================== Record component unwrapped ====================

    public record RecordPerson(
            @JSONField(unwrapped = true) Name name,
            int age
    ) {
    }

    @Test
    public void recordComponentUnwrapped() {
        RecordPerson back = JSON.parse("{\"first\":\"R1\",\"last\":\"R2\",\"age\":11}", RecordPerson.class);
        assertEquals(11, back.age());
        assertNotNull(back.name());
        assertEquals("R1", back.name().first);
        assertEquals("R2", back.name().last);
    }

    @Test
    public void recordComponentUnwrappedAbsent() {
        // Flattened keys missing → scratch inner never built → component stays null.
        RecordPerson back = JSON.parse("{\"age\":12}", RecordPerson.class);
        assertEquals(12, back.age());
        assertNull(back.name());
    }

    // ==================== Constructor-based unwrapped ====================

    public static final class ImmutablePerson {
        private final Name name;
        private final int age;

        @com.alibaba.fastjson3.annotation.JSONCreator(parameterNames = {"name", "age"})
        public ImmutablePerson(
                @JSONField(unwrapped = true) Name name,
                int age) {
            this.name = name;
            this.age = age;
        }

        public Name getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    @Test
    public void constructorComponentUnwrapped() {
        // Note: @JSONCreator with unwrapped constructor param. The constructor
        // reader paths the param type as the inner POJO and stages flattened keys
        // into a scratch inner before invoking the constructor.
        ImmutablePerson back = JSON.parse(
                "{\"first\":\"C1\",\"last\":\"C2\",\"age\":13}", ImmutablePerson.class);
        assertEquals(13, back.getAge());
        assertNotNull(back.getName());
        assertEquals("C1", back.getName().first);
        assertEquals("C2", back.getName().last);
    }

    // ==================== JSONObject / map-fallback paths honour deserializeUsing ====================
    //
    // Polymorphic readFromJSONObject and record readRecordGeneric's map branch
    // used to fall through to convertValue, silently skipping custom codecs on
    // unwrapped inner fields. Verify @JSONField(deserializeUsing=) fires on these
    // non-streaming paths.

    public static class SealedMoneyInner {
        @JSONField(deserializeUsing = MoneyReader.class)
        public java.math.BigDecimal amount;
    }

    // Sealed hierarchy triggers createSealedReader which uses readFromJSONObject.
    public sealed interface SealedPayload permits SealedInvoice {
    }

    @com.alibaba.fastjson3.annotation.JSONType(typeName = "invoice")
    public static final class SealedInvoice implements SealedPayload {
        public String id;

        @JSONField(unwrapped = true)
        public SealedMoneyInner money;
    }

    @Test
    public void unwrappedDeserializeUsingAcrossJsonObjectFastPath() {
        // Sealed interface routes through createSealedReader → readFromJSONObject.
        // The unwrapped inner's deserializeUsing must still fire through that path.
        SealedPayload back = JSON.parse(
                "{\"@type\":\"invoice\",\"id\":\"i1\",\"amount\":\"$100.50\"}", SealedPayload.class);
        assertInstanceOf(SealedInvoice.class, back);
        SealedInvoice parsed = (SealedInvoice) back;
        assertEquals("i1", parsed.id);
        assertNotNull(parsed.money);
        assertEquals(new java.math.BigDecimal("100.50"), parsed.money.amount);
    }

    public static class RecordMoneyInner {
        @JSONField(deserializeUsing = MoneyReader.class)
        public java.math.BigDecimal amount;
    }

    public record RecordInvoice(
            String id,
            @JSONField(unwrapped = true) RecordMoneyInner money
    ) {
    }

    @Test
    public void recordUnwrappedDeserializeUsingOnMapFallback() {
        // The record generic path goes through readRecordGeneric's map branch when
        // invoked via parser.readObject() first. The '$'-prefix value must still be
        // routed through MoneyReader rather than converted blindly via convertValue.
        RecordInvoice direct = JSON.parse("{\"id\":\"r1\",\"amount\":\"$42.50\"}", RecordInvoice.class);
        assertEquals("r1", direct.id());
        assertNotNull(direct.money());
        assertEquals(new java.math.BigDecimal("42.50"), direct.money().amount);
    }

    // ==================== Inner type's mix-in honoured by unwrapped expansion ====================
    //
    // Mix-in on the inner type must survive through the unwrapped expansion. A
    // user who registers `addMixIn(Address.class, AddressMixIn.class)` and has
    // @JSONField(unwrapped=true) on a field of type Address should see the
    // mix-in's renamed properties fire for the flattened inner keys.

    public static class AddressUnwrapped {
        public String street;
    }

    public static abstract class AddressMixIn {
        @JSONField(name = "street_name")
        public String street;
    }

    public static class PersonWithAddress {
        @JSONField(unwrapped = true)
        public AddressUnwrapped address;
        public int age;
    }

    @Test
    public void innerMixInHonouredDuringUnwrappedExpansion() {
        ObjectMapper m = ObjectMapper.builder()
                .addMixIn(AddressUnwrapped.class, AddressMixIn.class)
                .build();

        PersonWithAddress back = m.readValue(
                "{\"street_name\":\"Main\",\"age\":5}", PersonWithAddress.class);
        assertEquals(5, back.age);
        assertNotNull(back.address);
        assertEquals("Main", back.address.street);
    }

    // ==================== Setter-backed unwrapped with inherited holder field ====================

    public static class BaseHolder {
        protected Name name;

        public Name getName() {
            return name;
        }
    }

    public static class ChildHolder extends BaseHolder {
        public int age;

        @JSONField(unwrapped = true)
        public void setName(Name n) {
            this.name = n;
        }
    }

    @Test
    public void setterBackedUnwrappedFindsInheritedField() {
        ChildHolder back = JSON.parse("{\"first\":\"I\",\"last\":\"F\",\"age\":8}", ChildHolder.class);
        assertEquals(8, back.age);
        assertNotNull(back.getName());
        assertEquals("I", back.getName().first);
        assertEquals("F", back.getName().last);
    }

    // ==================== ASM-provider AUTO mapper falls back to reflection ====================

    @Test
    public void autoModeFallsBackToReflectionForUnwrappedField() {
        // The AUTO provider must not pick the ASM reader for a type that declares
        // an unwrapped field — the generated reader has no routing side table and
        // would silently drop the flattened inner keys.
        ObjectMapper autoMapper = ObjectMapper.builder()
                .readerProvider(com.alibaba.fastjson3.reader.AutoObjectReaderProvider.INSTANCE)
                .build();

        Person back = autoMapper.readValue(
                "{\"first\":\"A\",\"last\":\"Z\",\"age\":13}", Person.class);
        assertEquals(13, back.age);
        assertNotNull(back.name);
        assertEquals("A", back.name.first);
        assertEquals("Z", back.name.last);
    }

    // ==================== required=true on unwrapped holder ====================

    public static class ContactRequired {
        public String user;

        @JSONField(unwrapped = true, required = true)
        public Name name;
    }

    @Test
    public void requiredUnwrappedHolderWithFlattenedKeysPasses() {
        ContactRequired back = JSON.parse(
                "{\"user\":\"u1\",\"first\":\"A\",\"last\":\"Z\"}", ContactRequired.class);
        assertEquals("u1", back.user);
        assertNotNull(back.name);
        assertEquals("A", back.name.first);
    }

    @Test
    public void requiredUnwrappedHolderWithoutFlattenedKeysFails() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("{\"user\":\"u1\"}", ContactRequired.class));
        assertTrue(ex.getMessage().contains("required"), ex.getMessage());
        assertTrue(ex.getMessage().contains("name"), ex.getMessage());
    }

    public record ContactRequiredRecord(
            String user,
            @JSONField(unwrapped = true, required = true) Name name
    ) {
    }

    @Test
    public void requiredUnwrappedRecordComponentWithoutFlattenedKeysFails() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("{\"user\":\"u\"}", ContactRequiredRecord.class));
        assertTrue(ex.getMessage().contains("required"), ex.getMessage());
        assertTrue(ex.getMessage().contains("name"), ex.getMessage());
    }

    // ==================== defaultValue on unwrapped holder rejected ====================

    public static class DefaultOnUnwrapped {
        @JSONField(unwrapped = true, defaultValue = "x")
        public Name name;
    }

    @Test
    public void defaultValueOnUnwrappedHolderRejectedAtConstruction() {
        // The creator raises "does not support defaultValue" when constructing the
        // reader. ObjectMapper's auto-create path currently swallows that specific
        // exception, surfacing it at parse time as the generic
        // "no ObjectReader registered" fallback. Call the reader creator directly
        // so the targeted error surfaces unchanged.
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(DefaultOnUnwrapped.class));
        assertTrue(ex.getMessage().contains("defaultValue"), ex.getMessage());
    }

    // ==================== Setter-backed required unwrapped ====================

    public static class SetterRequiredHolder {
        private Name name;
        public int age;

        @JSONField(unwrapped = true, required = true)
        public void setName(Name n) {
            this.name = n;
        }

        public Name getName() {
            return name;
        }
    }

    @Test
    public void setterBackedRequiredUnwrappedWithFlattenedKeysPasses() {
        SetterRequiredHolder back = JSON.parse(
                "{\"first\":\"a\",\"last\":\"b\",\"age\":7}", SetterRequiredHolder.class);
        assertEquals(7, back.age);
        assertNotNull(back.getName());
    }

    @Test
    public void setterBackedRequiredUnwrappedWithoutFlattenedKeysFails() {
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parse("{\"age\":8}", SetterRequiredHolder.class));
        assertTrue(ex.getMessage().contains("required"), ex.getMessage());
        assertTrue(ex.getMessage().contains("name"), ex.getMessage());
    }

    // ==================== ErrorOnUnknownProperties honoured in record map-fallback ====================

    @Test
    public void recordMapFallbackHonoursErrorOnUnknownProperties() {
        // Keys that match neither a real component nor an unwrapped entry must
        // raise under ErrorOnUnknownProperties even when the parser took the
        // record map-fallback path. Previously silently ignored.
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parseObject(
                        "{\"age\":7,\"first\":\"a\",\"last\":\"b\",\"unexpected\":\"oops\"}",
                        RecordPerson.class, ReadFeature.ErrorOnUnknownProperties));
        assertTrue(ex.getMessage().contains("unknown"), ex.getMessage());
        assertTrue(ex.getMessage().contains("unexpected"), ex.getMessage());
    }

    @Test
    public void recordMapFallbackAcceptsUnwrappedInnerUnderErrorOnUnknown() {
        // Positive case: flattened inner keys shouldn't trigger the unknown error.
        RecordPerson back = JSON.parseObject(
                "{\"age\":9,\"first\":\"a\",\"last\":\"b\"}",
                RecordPerson.class, ReadFeature.ErrorOnUnknownProperties);
        assertEquals(9, back.age());
        assertEquals("a", back.name().first);
    }

    // ==================== AUTO provider: mix-in-provided unwrapped blocks ASM ====================

    public static class AsmEligibleTarget {
        public String id;
        public Name name;
        public int age;
    }

    public interface UnwrappedNameMixIn {
        @JSONField(unwrapped = true)
        Name getName();

        @JSONField(unwrapped = true)
        void setName(Name n);
    }

    @Test
    public void autoProviderRejectsAsmWhenMixInProvidesUnwrapped() {
        // The AUTO provider consults MIXIN_CONTEXT when deciding ASM eligibility.
        // Simulate the mapper-set context and ask the provider directly for a reader;
        // it must return a reflection reader (not ASM) because the mix-in marks a
        // method unwrapped. Note: for ObjectMapper.readValue the builder shortcuts
        // mix-in-bearing mappers past the provider to ObjectReaderCreator directly
        // (line 255 in ObjectMapper), so this defensive check guards the direct
        // provider path and any custom harness wiring that consults it.
        java.util.Map<Class<?>, Class<?>> ctx = new java.util.HashMap<>();
        ctx.put(AsmEligibleTarget.class, UnwrappedNameMixIn.class);
        com.alibaba.fastjson3.reader.ObjectReaderCreator.MIXIN_CONTEXT.set(ctx);
        try {
            // Direct provider call — the provider path is mix-in-aware after the fix.
            com.alibaba.fastjson3.ObjectReader<?> reader =
                    com.alibaba.fastjson3.reader.AutoObjectReaderProvider.INSTANCE
                            .getObjectReader(AsmEligibleTarget.class);
            assertNotNull(reader);
            // The ASM reader class names include "gen." prefix; reflection readers don't.
            // Asserting the class name stays free of the gen prefix pins "ASM bypassed".
            assertFalse(reader.getClass().getName().contains(".gen."),
                    "ASM provider should fall back to reflection for mix-in-provided unwrapped, got "
                            + reader.getClass().getName());
        } finally {
            com.alibaba.fastjson3.reader.ObjectReaderCreator.MIXIN_CONTEXT.remove();
        }
    }

    // ==================== Nested unwrapped inside record / constructor ====================
    //
    // The writer already double-flattens a record that wraps an unwrapped POJO
    // which itself wraps another unwrapped POJO. The reader must round-trip that
    // shape — prior to the fix, expandRecordUnwrapped refused to walk the chain.

    public static class DeepHandle {
        public String nick;
    }

    public static class DeepName {
        @JSONField(unwrapped = true)
        public DeepHandle handle;
        public String first;
    }

    public record DeepCelebrityRecord(
            @JSONField(unwrapped = true) DeepName name,
            int fame
    ) {
    }

    @Test
    public void recordDoubleUnwrappedParse() {
        // Hand-craft the double-flattened shape (matches what the writer emits
        // for mutable double-unwrap). The reader must walk the intermediate
        // holder chain: component `name` = new DeepName, then DeepName.handle
        // = new DeepHandle, then set nick on Handle.
        DeepCelebrityRecord back = JSON.parse(
                "{\"nick\":\"Prince\",\"first\":\"Mononymous\",\"fame\":11}",
                DeepCelebrityRecord.class);
        assertEquals(11, back.fame());
        assertNotNull(back.name());
        assertEquals("Mononymous", back.name().first);
        assertNotNull(back.name().handle);
        assertEquals("Prince", back.name().handle.nick);
    }

    public static final class DeepCelebrityCtor {
        private final DeepName name;
        private final int fame;

        @com.alibaba.fastjson3.annotation.JSONCreator(parameterNames = {"name", "fame"})
        public DeepCelebrityCtor(
                @JSONField(unwrapped = true) DeepName name,
                int fame) {
            this.name = name;
            this.fame = fame;
        }

        public DeepName getName() {
            return name;
        }

        public int getFame() {
            return fame;
        }
    }

    @Test
    public void constructorDoubleUnwrappedRoundTrip() {
        DeepCelebrityCtor back = JSON.parse(
                "{\"nick\":\"P\",\"first\":\"M\",\"fame\":9}", DeepCelebrityCtor.class);
        assertEquals(9, back.getFame());
        assertNotNull(back.getName());
        assertEquals("M", back.getName().first);
        assertNotNull(back.getName().handle);
        assertEquals("P", back.getName().handle.nick);
    }

    // ==================== Inherited mix-in setter annotation ====================

    public static class InheritedSetterTarget {
        private Name name;
        public int age;

        public void setName(Name n) {
            this.name = n;
        }

        public Name getName() {
            return name;
        }
    }

    public interface BaseSetterMixIn {
        @JSONField(unwrapped = true)
        void setName(Name n);
    }

    // Empty child — the annotation is inherited from BaseSetterMixIn.
    public interface ChildSetterMixIn extends BaseSetterMixIn {
    }

    @Test
    public void inheritedSetterMixInUnwrapped() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(InheritedSetterTarget.class, ChildSetterMixIn.class)
                .build();

        InheritedSetterTarget back = mapper.readValue(
                "{\"first\":\"A\",\"last\":\"B\",\"age\":3}", InheritedSetterTarget.class);
        assertEquals(3, back.age);
        assertNotNull(back.getName());
        assertEquals("A", back.getName().first);
        assertEquals("B", back.getName().last);
    }

    // ==================== Inherited mix-in interface blocks ASM selection ====================

    public static class SimpleInheritTarget {
        public String id;
        public Name name;
        public int age;
    }

    public interface BaseUnwrappedMixIn {
        @JSONField(unwrapped = true)
        Name getName();

        @JSONField(unwrapped = true)
        void setName(Name n);
    }

    public interface ChildUnwrappedMixIn extends BaseUnwrappedMixIn {
    }

    // ==================== readFromJSONObject honours ErrorOnUnknownProperties ====================
    //
    // The JSONObject fast path used by sealed / Jackson polymorphic readers used
    // to silently ignore unknown keys when no anySetter was registered. It must
    // mirror the parser-based loops: throw under ErrorOnUnknownProperties.

    public sealed interface JsonObjectStrictBase permits JsonObjectStrictImpl {
    }

    @com.alibaba.fastjson3.annotation.JSONType(typeName = "strict")
    public static final class JsonObjectStrictImpl implements JsonObjectStrictBase {
        public int x;
    }

    @Test
    public void readFromJsonObjectHonoursErrorOnUnknown() {
        // Sealed-interface dispatch materialises a JSONObject and calls
        // readFromJSONObject. The unknown "junk" key must surface under
        // ErrorOnUnknownProperties, same as the streaming paths.
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.parseObject(
                        "{\"@type\":\"strict\",\"x\":5,\"junk\":\"nope\"}",
                        JsonObjectStrictBase.class,
                        ReadFeature.ErrorOnUnknownProperties));
        assertTrue(ex.getMessage().contains("unknown"), ex.getMessage());
        assertTrue(ex.getMessage().contains("junk"), ex.getMessage());
    }

    @Test
    public void readFromJsonObjectAcceptsKnownKeysUnderErrorOnUnknown() {
        JsonObjectStrictBase back = JSON.parseObject(
                "{\"@type\":\"strict\",\"x\":7}",
                JsonObjectStrictBase.class,
                ReadFeature.ErrorOnUnknownProperties);
        assertInstanceOf(JsonObjectStrictImpl.class, back);
        assertEquals(7, ((JsonObjectStrictImpl) back).x);
    }

    // ==================== Record map path — null-valued field is consumed ====================
    //
    // A JSON key like `{"name": null}` is presence-but-null. The record map
    // fallback must mark the key as consumed by PRESENCE (containsKey) so the
    // second pass neither re-routes it through unwrappedByName nor flags it
    // under ErrorOnUnknownProperties. Bug pattern from wenshao's review.

    public record NullableAware(String name, int age) {
    }

    @Test
    public void recordMapFallbackConsumesExplicitNullKey() {
        // Plain record (no unwrapped) — ErrorOnUnknownProperties must not fire
        // on a declared component that just happens to be present with a null
        // value. This pins the "consumed by presence" contract.
        NullableAware back = JSON.parseObject(
                "{\"name\":null,\"age\":5}", NullableAware.class,
                ReadFeature.ErrorOnUnknownProperties);
        assertNull(back.name());
        assertEquals(5, back.age());
    }

    // ==================== Ambiguous unwrapped field names rejected at build ====================

    public static class AmbiguousInnerA {
        public String tag;
    }

    public static class AmbiguousInnerB {
        public String tag;
    }

    public static class AmbiguousHolder {
        @JSONField(unwrapped = true)
        public AmbiguousInnerA a;

        @JSONField(unwrapped = true)
        public AmbiguousInnerB b;
    }

    @Test
    public void ambiguousUnwrappedFieldNameRejected() {
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(AmbiguousHolder.class));
        assertTrue(ex.getMessage().contains("ambiguous"), ex.getMessage());
        assertTrue(ex.getMessage().contains("tag"), ex.getMessage());
    }

    @Test
    public void autoProviderRejectsAsmForInheritedMixInUnwrapped() {
        // hasUnwrappedField must recurse through interface inheritance so a
        // mix-in interface that inherits @JSONField(unwrapped=true) from a
        // parent still flips the ASM gate.
        java.util.Map<Class<?>, Class<?>> ctx = new java.util.HashMap<>();
        ctx.put(SimpleInheritTarget.class, ChildUnwrappedMixIn.class);
        com.alibaba.fastjson3.reader.ObjectReaderCreator.MIXIN_CONTEXT.set(ctx);
        try {
            com.alibaba.fastjson3.ObjectReader<?> reader =
                    com.alibaba.fastjson3.reader.AutoObjectReaderProvider.INSTANCE
                            .getObjectReader(SimpleInheritTarget.class);
            assertNotNull(reader);
            assertFalse(reader.getClass().getName().contains(".gen."),
                    "ASM provider must fall back to reflection for inherited mix-in unwrapped, got "
                            + reader.getClass().getName());
        } finally {
            com.alibaba.fastjson3.reader.ObjectReaderCreator.MIXIN_CONTEXT.remove();
        }
    }
}

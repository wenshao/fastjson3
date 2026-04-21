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

    // ==================== Cyclic unwrap rejected ====================

    public static class CycleA {
        @JSONField(unwrapped = true)
        public CycleB b;
    }

    public static class CycleB {
        @JSONField(unwrapped = true)
        public CycleA a;
    }

    @Test
    public void directCyclicUnwrapRejected() {
        // A → B → A: without cycle detection this recurses until StackOverflowError.
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(CycleA.class));
        assertTrue(ex.getMessage().toLowerCase().contains("cyclic"), ex.getMessage());
    }

    public static class CycleX {
        @JSONField(unwrapped = true)
        public CycleY y;
    }

    public static class CycleY {
        @JSONField(unwrapped = true)
        public CycleZ z;
    }

    public static class CycleZ {
        @JSONField(unwrapped = true)
        public CycleX x;
    }

    @Test
    public void indirectCyclicUnwrapRejected() {
        // X → Y → Z → X — three-hop cycle still caught.
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(CycleX.class));
        assertTrue(ex.getMessage().toLowerCase().contains("cyclic"), ex.getMessage());
    }

    @Test
    public void cycleDetectionDoesNotPoisonSubsequentConstruction() {
        // A cyclic failure must clean the thread-local visited set so later
        // (unrelated) construction of a legitimate unwrapped POJO still works.
        try {
            com.alibaba.fastjson3.reader.ObjectReaderCreator.createObjectReader(CycleA.class);
            fail("expected cyclic rejection");
        } catch (JSONException expected) {
            // consume
        }
        // Previously healthy unwrapped bean still constructs.
        Person p = JSON.parse("{\"first\":\"a\",\"last\":\"b\",\"age\":1}", Person.class);
        assertEquals("a", p.name.first);
    }

    // ==================== Non-POJO unwrap holder rejected ====================

    public static class CollectionHolder {
        @JSONField(unwrapped = true)
        public java.util.List<String> items;
    }

    @Test
    public void collectionUnwrapHolderRejected() {
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(CollectionHolder.class));
        assertTrue(ex.getMessage().contains("Collection"), ex.getMessage());
    }

    public static class MapHolder {
        @JSONField(unwrapped = true)
        public java.util.Map<String, Object> props;
    }

    @Test
    public void mapUnwrapHolderRejected() {
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(MapHolder.class));
        assertTrue(ex.getMessage().contains("Map"), ex.getMessage());
    }

    public enum Role { ADMIN, USER }

    public static class EnumHolder {
        @JSONField(unwrapped = true)
        public Role role;
    }

    @Test
    public void enumUnwrapHolderRejected() {
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(EnumHolder.class));
        assertTrue(ex.getMessage().contains("enum"), ex.getMessage());
    }

    public static class ArrayHolder {
        @JSONField(unwrapped = true)
        public String[] tags;
    }

    @Test
    public void arrayUnwrapHolderRejected() {
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(ArrayHolder.class));
        assertTrue(ex.getMessage().contains("array"), ex.getMessage());
    }

    // ==================== readFromJSONObject honours @JSONType(schema=) ====================

    public sealed interface SchemaAnimal permits SchemaAdultCat {
    }

    @com.alibaba.fastjson3.annotation.JSONType(typeName = "Cat", schema =
            "{\"type\":\"object\",\"properties\":{\"age\":{\"type\":\"integer\",\"minimum\":1}}}")
    public static final class SchemaAdultCat implements SchemaAnimal {
        public int age;
        public String name;
    }

    // ==================== Generic inner resolves via outer type argument ====================

    public static class Wrapper<T> {
        public T value;
    }

    public static class Address {
        public String city;
    }

    public static class GenericUnwrapHost {
        @JSONField(unwrapped = true)
        public Wrapper<Address> box;
    }

    @Test
    public void genericInnerResolvesAgainstOuterTypeArgument() {
        // Inner field `T value` previously erased to Object, landing a LinkedHashMap
        // in box.value instead of a real Address. The contextType handed to
        // collectFieldReaders must be `Wrapper<Address>`, not `Wrapper.class`.
        GenericUnwrapHost host = JSON.parse("{\"value\":{\"city\":\"SF\"}}", GenericUnwrapHost.class);
        assertNotNull(host.box);
        assertNotNull(host.box.value);
        assertInstanceOf(Address.class, host.box.value,
                "expected Address, got " + host.box.value.getClass().getName());
        assertEquals("SF", host.box.value.city);
    }

    public record GenericRecordHost(@JSONField(unwrapped = true) Wrapper<Address> box) {
    }

    public static class InheritParent<T> {
        @JSONField(unwrapped = true)
        public Wrapper<T> holder;
    }

    public static class InheritChild extends InheritParent<Address> {
    }

    @Test
    public void inheritedGenericUnwrapBindsAgainstChildTypeArgument() {
        // outerField.getGenericType() returns Wrapper<T> where T is the parent's
        // unbound type variable. Resolving it against the parent alone erases
        // back to Object. Must resolve against Child's binding of Parent<Address>
        // so Wrapper<T>.value → Address.
        InheritChild c = JSON.parse("{\"value\":{\"city\":\"Paris\"}}", InheritChild.class);
        assertNotNull(c.holder);
        assertInstanceOf(Address.class, c.holder.value,
                "expected Address, got " + (c.holder.value == null ? "null" : c.holder.value.getClass().getName()));
        assertEquals("Paris", ((Address) c.holder.value).city);
    }

    @Test
    public void genericInnerRecordResolvesAgainstOuterTypeArgument() {
        GenericRecordHost host = JSON.parse("{\"value\":{\"city\":\"LA\"}}", GenericRecordHost.class);
        assertNotNull(host.box());
        assertInstanceOf(Address.class, host.box().value);
        assertEquals("LA", host.box().value.city);
    }

    // ==================== Primitive unwrap holder rejected ====================

    public static class PrimitiveFieldHolder {
        @JSONField(unwrapped = true)
        public int counter;
    }

    @Test
    public void primitiveFieldUnwrapHolderRejected() {
        // Primitive can't have an inner bean layout to flatten. A silent
        // fall-through to the regular FieldReader path would mislead users
        // into thinking the annotation took effect.
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(PrimitiveFieldHolder.class));
        assertTrue(ex.getMessage().contains("primitive"), ex.getMessage());
    }

    public static class PrimitiveSetterHolder {
        private int counter;

        @JSONField(unwrapped = true)
        public void setCounter(int c) {
            this.counter = c;
        }

        public int getCounter() {
            return counter;
        }
    }

    @Test
    public void primitiveSetterUnwrapRejected() {
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(PrimitiveSetterHolder.class));
        assertTrue(ex.getMessage().contains("primitive"), ex.getMessage());
    }

    public record PrimitiveCtorHolder(@JSONField(unwrapped = true) int counter) {
    }

    @Test
    public void primitiveCtorParamUnwrapRejected() {
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(PrimitiveCtorHolder.class));
        assertTrue(ex.getMessage().contains("primitive"), ex.getMessage());
    }

    // ==================== Custom reader sees null on map-fallback path ====================

    public static class NullSentinelReader implements com.alibaba.fastjson3.ObjectReader<String> {
        @Override
        public String readObject(com.alibaba.fastjson3.JSONParser parser,
                                 java.lang.reflect.Type fieldType, Object fieldName, long features) {
            if (parser.readNull()) {
                return "<NULL-HANDLED>";
            }
            return parser.readString();
        }
    }

    public static class CustomNullInner {
        @JSONField(deserializeUsing = NullSentinelReader.class)
        public String token;
    }

    public static class CustomNullHost {
        @JSONField(unwrapped = true)
        public CustomNullInner inner;
    }

    public sealed interface CustomNullPolymorphic permits CustomNullChild {
    }

    @com.alibaba.fastjson3.annotation.JSONType(typeName = "child")
    public static final class CustomNullChild implements CustomNullPolymorphic {
        @JSONField(unwrapped = true)
        public CustomNullInner inner;
    }

    @Test
    public void customReaderSeesNullOnMapFallbackPath() {
        // Polymorphic dispatch materializes a JSONObject and flows through
        // applyUnwrappedValueFromMap. A deserializeUsing that maps null to a
        // sentinel (parser.nextIfNull() → "<NULL-HANDLED>") must get the same
        // chance the parser path gives it, not be short-circuited to a plain
        // null conversion.
        String json = "{\"@type\":\"child\",\"token\":null}";
        CustomNullPolymorphic parsed = JSON.parse(json, CustomNullPolymorphic.class);
        assertInstanceOf(CustomNullChild.class, parsed);
        CustomNullChild c = (CustomNullChild) parsed;
        assertNotNull(c.inner);
        assertEquals("<NULL-HANDLED>", c.inner.token);
    }

    @Test
    public void sealedReadFromJSONObjectHonoursSubtypeSchema() {
        // Sealed-class polymorphic dispatch materializes the subtype payload as
        // a JSONObject and then routes through readFromJSONObject. The subtype's
        // @JSONType(schema=) must still fire — parser-based readers enforce it,
        // and this entry point used to silently skip the check.
        String bad = "{\"@type\":\"Cat\",\"age\":0,\"name\":\"k\"}";
        assertThrows(Exception.class, () -> JSON.parse(bad, SchemaAnimal.class));

        String good = "{\"@type\":\"Cat\",\"age\":3,\"name\":\"whiskers\"}";
        SchemaAnimal parsed = JSON.parse(good, SchemaAnimal.class);
        assertInstanceOf(SchemaAdultCat.class, parsed);
        assertEquals(3, ((SchemaAdultCat) parsed).age);
    }

    // ==================== Writer round-trip when inner uses ASM writer ====================
    // Round-3 usability audit F6: the writer's @JSONField(unwrapped=true) path
    // used to accept only ReflectObjectWriter, crashing with "requires a POJO
    // type; InnerC is not supported" whenever the inner class was ASM-compiled
    // (which is the majority of POJOs). After the fix, the writer resolves a
    // dedicated ReflectObjectWriter for the inner type regardless of what the
    // global mapper cached, making round-trip symmetric with the reader.

    public static class InnerAddr {
        public String city;
        public String zip;
    }

    public static class OuterWithAddr {
        @JSONField(unwrapped = true)
        public InnerAddr addr;
        public String name;
    }

    @Test
    public void writerUnwrapsAsmInnerRoundTrip() {
        OuterWithAddr o = new OuterWithAddr();
        o.addr = new InnerAddr();
        o.addr.city = "SF";
        o.addr.zip = "94103";
        o.name = "alice";

        // Write via JSON.toJSONString — the global mapper will typically
        // produce ASM writers for the inner type. Must NOT crash.
        String json = JSON.toJSONString(o);
        assertTrue(json.contains("\"city\":\"SF\""), json);
        assertTrue(json.contains("\"zip\":\"94103\""), json);
        assertTrue(json.contains("\"name\":\"alice\""), json);
        assertFalse(json.contains("\"addr\":"), "inner should be flattened, not wrapped: " + json);

        OuterWithAddr back = JSON.parse(json, OuterWithAddr.class);
        assertEquals("alice", back.name);
        assertNotNull(back.addr);
        assertEquals("SF", back.addr.city);
        assertEquals("94103", back.addr.zip);
    }

    @Test
    public void writerRejectsRecordInnerAtConstruction() {
        // Writer-side mirror of the reader's guard: emitting flattened keys
        // from a record inner would produce a payload the reader rejects,
        // so the writer rejects at construction time instead of silently
        // producing un-round-trippable JSON.
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.toJSONString(new RecordInnerHost(new RecordInner("a"))));
        assertTrue(ex.getMessage().toLowerCase().contains("record"), ex.getMessage());
    }

    public record RecordInner(String tag) {}

    public static class RecordInnerHost {
        @JSONField(unwrapped = true)
        public RecordInner leaf;

        public RecordInnerHost() {}
        public RecordInnerHost(RecordInner leaf) { this.leaf = leaf; }
    }

    // ==================== Mix-in / Jackson config preserved on unwrap ====================
    // Round-1 audit on PR #125 found that building a fresh ReflectObjectWriter
    // via the 1-arg factory dropped the mapper's mix-in map + useJacksonAnnotation
    // flag, so a Jackson-renamed inner field emitted its raw Java name instead.

    public static class RenamedInner {
        @com.fasterxml.jackson.annotation.JsonProperty("renamed")
        public String a;
    }

    public static class JacksonRenameHost {
        @JSONField(unwrapped = true)
        public RenamedInner inner;
        public String name;
    }

    @Test
    public void unwrapHonoursJacksonRenamingOnInnerField() {
        JacksonRenameHost h = new JacksonRenameHost();
        h.inner = new RenamedInner();
        h.inner.a = "hello";
        h.name = "bob";

        ObjectMapper jackson = ObjectMapper.builder().useJacksonAnnotation(true).build();
        String json = jackson.writeValueAsString(h);
        assertTrue(json.contains("\"renamed\":\"hello\""),
                "inner field should emit as 'renamed' not raw 'a': " + json);
        assertFalse(json.contains("\"a\":\"hello\""), json);
        assertTrue(json.contains("\"name\":\"bob\""), json);
    }

    public static class MixInRenamedInner {
        public String a;
    }

    public interface InnerMixIn {
        @JSONField(name = "renamed")
        String a = null;
    }

    public static class MixInRenameHost {
        @JSONField(unwrapped = true)
        public MixInRenamedInner inner;
        public String name;
    }

    @Test
    public void unwrapHonoursMixInRenamingOnInnerField() {
        MixInRenameHost h = new MixInRenameHost();
        h.inner = new MixInRenamedInner();
        h.inner.a = "hello";
        h.name = "bob";

        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(MixInRenamedInner.class, InnerMixIn.class)
                .build();
        String json = mapper.writeValueAsString(h);
        assertTrue(json.contains("\"renamed\":\"hello\""),
                "mix-in-renamed inner should emit as 'renamed': " + json);
        assertFalse(json.contains("\"a\":\"hello\""), json);
    }

    // Round-4: rejectNonPojoUnwrappedInner previously only caught structural
    // non-POJOs (Collection / Map / enum / array / abstract / record). Scalar
    // wrappers (String, Integer, Long, BigDecimal, Object, …) slipped through
    // and produced `"field":value` output that the reader couldn't recombine.

    public static class ScalarStringUnwrapHolder {
        @JSONField(unwrapped = true)
        public String payload;
    }

    public static class ScalarIntegerUnwrapHolder {
        @JSONField(unwrapped = true)
        public Integer counter;
    }

    public static class ScalarObjectUnwrapHolder {
        @JSONField(unwrapped = true)
        public Object payload;
    }

    @Test
    public void writerRejectsScalarStringInnerAtConstruction() {
        ScalarStringUnwrapHolder h = new ScalarStringUnwrapHolder();
        h.payload = "hi";
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.toJSONString(h));
        assertTrue(ex.getMessage().contains("scalar")
                        || ex.getMessage().toLowerCase().contains("string"),
                ex.getMessage());
    }

    @Test
    public void writerRejectsScalarIntegerInnerAtConstruction() {
        ScalarIntegerUnwrapHolder h = new ScalarIntegerUnwrapHolder();
        h.counter = 42;
        assertThrows(JSONException.class, () -> JSON.toJSONString(h));
    }

    @Test
    public void writerRejectsObjectInnerAtConstruction() {
        ScalarObjectUnwrapHolder h = new ScalarObjectUnwrapHolder();
        h.payload = java.util.Map.of("k", "v");
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.toJSONString(h));
        assertTrue(ex.getMessage().contains("Object"), ex.getMessage());
    }

    // Round-5: Optional and AtomicReference slipped through the R4 guard.

    public static class OptionalHolder {
        @JSONField(unwrapped = true)
        public java.util.Optional<String> payload;
    }

    @Test
    public void writerRejectsOptionalInnerAtConstruction() {
        OptionalHolder h = new OptionalHolder();
        h.payload = java.util.Optional.of("hi");
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.toJSONString(h));
        assertTrue(ex.getMessage().contains("Optional"), ex.getMessage());
    }

    @Test
    public void readerRejectsOptionalInnerAtConstruction() {
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(OptionalHolder.class));
        assertTrue(ex.getMessage().contains("Optional"), ex.getMessage());
    }

    public static class AtomicReferenceHolder {
        @JSONField(unwrapped = true)
        public java.util.concurrent.atomic.AtomicReference<String> payload;
    }

    @Test
    public void writerRejectsAtomicReferenceInnerAtConstruction() {
        AtomicReferenceHolder h = new AtomicReferenceHolder();
        h.payload = new java.util.concurrent.atomic.AtomicReference<>("hi");
        JSONException ex = assertThrows(JSONException.class,
                () -> JSON.toJSONString(h));
        assertTrue(ex.getMessage().contains("AtomicReference"), ex.getMessage());
    }

    // Round-5 reader symmetry with writer's R4 scalar/Object rejection.

    @Test
    public void readerRejectsScalarStringInnerAtConstruction() {
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(ScalarStringUnwrapHolder.class));
        assertTrue(ex.getMessage().toLowerCase().contains("scalar")
                        || ex.getMessage().toLowerCase().contains("string"),
                ex.getMessage());
    }

    @Test
    public void readerRejectsObjectInnerAtConstruction() {
        JSONException ex = assertThrows(JSONException.class,
                () -> com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(ScalarObjectUnwrapHolder.class));
        assertTrue(ex.getMessage().contains("Object"), ex.getMessage());
    }

    // Round-5 ASM bypass: without unwrap in canGenerate, ASM writer got
    // chosen for unwrap fields with inclusion=ALWAYS and emitted nested
    // shape instead of flattened.

    public static class AsmBypassInner {
        public String city;
    }

    public static class AsmBypassOuter {
        @JSONField(unwrapped = true, serialize = true)
        public AsmBypassInner addr;
        public String name;
    }

    @Test
    public void asmWriterRejectsUnwrappedForReflectPath() {
        AsmBypassOuter o = new AsmBypassOuter();
        o.addr = new AsmBypassInner();
        o.addr.city = "SF";
        o.name = "alice";

        String json = JSON.toJSONString(o);
        assertTrue(json.contains("\"city\":\"SF\""),
                "unwrap must flatten — nested means ASM bypass re-opened: " + json);
        assertFalse(json.contains("\"addr\":"), json);

        AsmBypassOuter back = JSON.parse(json, AsmBypassOuter.class);
        assertEquals("alice", back.name);
        assertEquals("SF", back.addr.city);
    }

    // Round-6: ASM canGenerate checked only `type.getDeclaredFields()`, so
    // an INHERITED public field with @JSONField(unwrapped=true) passed the
    // gate and ASM emitted nested JSON. Scan superclass chain.

    public static class ParentWithUnwrap {
        @JSONField(unwrapped = true, serialize = true)
        public AsmBypassInner addr;
    }

    public static class InheritedUnwrap extends ParentWithUnwrap {
        public String name;
    }

    @Test
    public void asmWriterRejectsInheritedUnwrappedField() {
        InheritedUnwrap o = new InheritedUnwrap();
        o.addr = new AsmBypassInner();
        o.addr.city = "LA";
        o.name = "bob";

        String json = JSON.toJSONString(o);
        assertTrue(json.contains("\"city\":\"LA\""),
                "inherited unwrap must flatten — if nested the ASM gate still misses inherited: " + json);
        assertFalse(json.contains("\"addr\":"), json);

        InheritedUnwrap back = JSON.parse(json, InheritedUnwrap.class);
        assertEquals("bob", back.name);
        assertEquals("LA", back.addr.city);
    }

    // Round-8 F1: cyclic @JSONField(unwrapped=true) chain on the writer side
    // recursed until the JVM stack overflowed. Reader had class-level cycle
    // detection but writer had no equivalent. Depth guard via generator's
    // existing MAX_WRITE_DEPTH=512 now surfaces a clean JSONException.
    public static class CircA {
        @JSONField(unwrapped = true)
        public CircB b;
    }

    public static class CircB {
        @JSONField(unwrapped = true)
        public CircA a;
    }

    @Test
    public void circularUnwrapThrowsJSONExceptionNotStackOverflow() {
        CircA a = new CircA();
        a.b = new CircB();
        a.b.a = a;
        // Pre-fix: StackOverflowError. Post-fix: JSONException from depth cap.
        JSONException ex = assertThrows(JSONException.class, () -> JSON.toJSONString(a));
        assertTrue(ex.getMessage().contains("depth"), ex.getMessage());
    }

    // Round-8 F2: record components with @JSONField(unwrapped=true) silently
    // emitted nested JSON because createRecordWriter never read jsonField.unwrapped().
    // Reader-side expandRecordUnwrapped already flattens record components,
    // producing a reader/writer asymmetry.
    public record RecordHolder(@JSONField(unwrapped = true) Name inner, int extra) {
    }

    @Test
    public void recordComponentUnwrapFlattens() {
        Name n = new Name();
        n.first = "ada";
        n.last = "lovelace";
        RecordHolder h = new RecordHolder(n, 42);

        String json = JSON.toJSONString(h);
        assertFalse(json.contains("\"inner\":"),
                "record component @unwrapped must flatten: " + json);
        assertTrue(json.contains("\"first\":\"ada\""), json);
        assertTrue(json.contains("\"last\":\"lovelace\""), json);
        assertTrue(json.contains("\"extra\":42"), json);

        // Round-trip: reader-side already supports record-component unwrap, so
        // the flattened payload must reconstruct to the same shape.
        RecordHolder back = JSON.parse(json, RecordHolder.class);
        assertNotNull(back.inner());
        assertEquals("ada", back.inner().first);
        assertEquals("lovelace", back.inner().last);
        assertEquals(42, back.extra());
    }
}

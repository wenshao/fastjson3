package com.alibaba.fastjson3;

import com.alibaba.fastjson3.reader.ObjectReaderCreatorASM;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for PR #114's large-bean ASM path
 * (commit {@code f616a98}). The {@code AutoObjectReaderProvider} gate at
 * {@code countSerializableFields(type) <= 15} routes 22-field POJOs to
 * REFLECT under {@code JSON.parseObject}, so a real-world bench that goes
 * through AUTO never exercises the new {@code useFastPath} demotion or the
 * extracted {@code readFieldsFallback} trampoline. This test forces the ASM
 * generator directly via {@link ObjectReaderCreatorASM#createObjectReader}
 * and asserts both the speculation hot path and the fallback path produce
 * the same value as REFLECT.
 */
public class AsmLargeBeanTest {

    public static class LargeBean {
        public String f01, f02, f03, f04, f05, f06, f07, f08, f09, f10;
        public String f11, f12, f13, f14, f15, f16, f17, f18, f19, f20;
        public int n01, n02;
    }

    private static final String JSON_IN_DECL_ORDER =
            "{\"f01\":\"a\",\"f02\":\"b\",\"f03\":\"c\",\"f04\":\"d\",\"f05\":\"e\","
                    + "\"f06\":\"f\",\"f07\":\"g\",\"f08\":\"h\",\"f09\":\"i\",\"f10\":\"j\","
                    + "\"f11\":\"k\",\"f12\":\"l\",\"f13\":\"m\",\"f14\":\"n\",\"f15\":\"o\","
                    + "\"f16\":\"p\",\"f17\":\"q\",\"f18\":\"r\",\"f19\":\"s\",\"f20\":\"t\","
                    + "\"n01\":1,\"n02\":2}";

    private static final String JSON_IN_REVERSE_ORDER =
            "{\"n02\":2,\"n01\":1,"
                    + "\"f20\":\"t\",\"f19\":\"s\",\"f18\":\"r\",\"f17\":\"q\",\"f16\":\"p\","
                    + "\"f15\":\"o\",\"f14\":\"n\",\"f13\":\"m\",\"f12\":\"l\",\"f11\":\"k\","
                    + "\"f10\":\"j\",\"f09\":\"i\",\"f08\":\"h\",\"f07\":\"g\",\"f06\":\"f\","
                    + "\"f05\":\"e\",\"f04\":\"d\",\"f03\":\"c\",\"f02\":\"b\",\"f01\":\"a\"}";

    private static final String JSON_WITH_UNKNOWN_FIELD =
            "{\"f01\":\"a\",\"unknown\":\"skip\",\"f02\":\"b\",\"f03\":\"c\",\"f04\":\"d\","
                    + "\"f05\":\"e\",\"f06\":\"f\",\"f07\":\"g\",\"f08\":\"h\",\"f09\":\"i\","
                    + "\"f10\":\"j\",\"f11\":\"k\",\"f12\":\"l\",\"f13\":\"m\",\"f14\":\"n\","
                    + "\"f15\":\"o\",\"f16\":\"p\",\"f17\":\"q\",\"f18\":\"r\",\"f19\":\"s\","
                    + "\"f20\":\"t\",\"n01\":1,\"n02\":2}";

    @Test
    public void asmReaderInDeclOrder() {
        ObjectReader<LargeBean> r = ObjectReaderCreatorASM.createObjectReader(LargeBean.class);
        assertGeneratedAsmReader(r);
        try (JSONParser p = JSONParser.of(JSON_IN_DECL_ORDER.getBytes())) {
            LargeBean b = r.readObject(p, null, null, 0);
            assertEverythingPopulated(b);
        }
    }

    @Test
    public void asmReaderInReverseOrderHitsFallback() {
        // JSON arrives in reverse field order — every batch's ordered speculation
        // misses on the first field, so the very first iteration falls into
        // readFieldsFallback for the rest of the parse. Verifies the trampoline
        // wiring + extracted fallback method correctness end-to-end.
        ObjectReader<LargeBean> r = ObjectReaderCreatorASM.createObjectReader(LargeBean.class);
        try (JSONParser p = JSONParser.of(JSON_IN_REVERSE_ORDER.getBytes())) {
            LargeBean b = r.readObject(p, null, null, 0);
            assertEverythingPopulated(b);
        }
    }

    @Test
    public void asmReaderSkipsUnknownField() {
        // Unknown field "unknown":"skip" appears between f01 and f02 — the
        // speculation matches on f01, then misses on f02 (since "unknown" is
        // there instead). Forces the fallback to take over mid-parse and
        // continue past the unknown into the remaining fields.
        ObjectReader<LargeBean> r = ObjectReaderCreatorASM.createObjectReader(LargeBean.class);
        try (JSONParser p = JSONParser.of(JSON_WITH_UNKNOWN_FIELD.getBytes())) {
            LargeBean b = r.readObject(p, null, null, 0);
            assertEverythingPopulated(b);
        }
    }

    @Test
    public void generatedClassExposesReadFieldsFallback() {
        // Reflect on the generated class to lock in the structural change of
        // PR #114: a private readFieldsFallback method must exist alongside
        // readObjectUTF8 (and readFieldsBatchN for >8 fields). If the
        // generator regresses to inline-everything, this fails fast.
        ObjectReader<LargeBean> r = ObjectReaderCreatorASM.createObjectReader(LargeBean.class);
        Class<?> generated = r.getClass();
        assertTrue(generated.getName().startsWith("com.alibaba.fastjson3.reader.gen."),
                "expected ASM-generated class, got " + generated.getName());

        Method fallback = null;
        Method batch0 = null;
        for (Method m : generated.getDeclaredMethods()) {
            if (m.getName().equals("readFieldsFallback")) {
                fallback = m;
            } else if (m.getName().equals("readFieldsBatch0")) {
                batch0 = m;
            }
        }
        assertNotNull(fallback,
                "PR #114's readFieldsFallback method must be emitted on " + generated.getName());
        assertNotNull(batch0,
                "non-fast-path batch method readFieldsBatch0 must be emitted for 22-field bean");
    }

    private static void assertGeneratedAsmReader(ObjectReader<?> r) {
        assertTrue(r.getClass().getName().startsWith("com.alibaba.fastjson3.reader.gen."),
                "expected ASM-generated reader, got " + r.getClass().getName());
    }

    private static void assertEverythingPopulated(LargeBean b) {
        assertEquals("a", b.f01);
        assertEquals("b", b.f02);
        assertEquals("c", b.f03);
        assertEquals("d", b.f04);
        assertEquals("e", b.f05);
        assertEquals("f", b.f06);
        assertEquals("g", b.f07);
        assertEquals("h", b.f08);
        assertEquals("i", b.f09);
        assertEquals("j", b.f10);
        assertEquals("k", b.f11);
        assertEquals("l", b.f12);
        assertEquals("m", b.f13);
        assertEquals("n", b.f14);
        assertEquals("o", b.f15);
        assertEquals("p", b.f16);
        assertEquals("q", b.f17);
        assertEquals("r", b.f18);
        assertEquals("s", b.f19);
        assertEquals("t", b.f20);
        assertEquals(1, b.n01);
        assertEquals(2, b.n02);
    }
}

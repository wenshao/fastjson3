package com.alibaba.fastjson3;

import com.alibaba.fastjson3.reader.ReaderCreatorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link com.alibaba.fastjson3.reader.ObjectReaderCreatorASM}'s
 * inline enum value matching (Phase B2 of Path B). The generator emits a
 * {@code getRawInt}-based lookupswitch followed by
 * {@code nextIfValue4Match<N>} intrinsics to read enum field values without
 * allocating a String or consulting a HashMap.
 *
 * <p>All bean types in this file are public with public enum types so the ASM
 * reader generated in the {@code ...reader.gen} package can resolve them via
 * standard classloader resolution.</p>
 */
public class ObjectReaderCreatorASMEnumTest {
    public static final class BeanLen3 {
        public enum E { RED, AMT, XYZ }
        public E color;
        public int value;
    }

    public static final class BeanLen4 {
        public enum E { JAVA, RUST, PERL }
        public E lang;
        public int year;
    }

    public static final class BeanLen5 {
        public enum E { FLASH, MYSQL, REDIS }
        public E platform;
        public int rev;
    }

    public static final class BeanLen6 {
        public enum E { ORANGE, BANANA, APPLES }
        public E fruit;
        public int count;
    }

    public static final class BeanLen7 {
        public enum E { BARBARA, CHARLES, DENNISK }
        public E name;
        public int ord;
    }

    public static final class BeanLen8 {
        public enum E { LARGE123, SMALL456, MEDIUM99 }
        public E code;
        public int val;
    }

    public static final class BeanLen9 {
        public enum E { ALPHABETA, BRAVOBETA, CHARLIE42 }
        public E tag;
        public int n;
    }

    public static final class BeanLen10 {
        public enum E { HORIZONTAL, VERTICAL14, DIAGONAL22 }
        public E align;
        public int px;
    }

    public static final class BeanLen11 {
        public enum E { DIAGNOSTIC1, MAINFRAME99, FIREWALLS1 }
        public E typ;
        public int cd;
    }

    public static final class BeanMultiEnum {
        public enum Player { JAVA, FLASH }
        public enum Size { SMALL, LARGE }
        public Player player;
        public Size size;
        public int count;
    }

    public static final class BeanNullable {
        public enum E { ALPHA, BETA }
        public E value;
        public String name;
    }

    private static ObjectMapper asmMapper() {
        return ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.ASM)
                .build();
    }

    @Test
    void len3_successAllConstants() {
        ObjectMapper m = asmMapper();
        BeanLen3 r = m.readValue("{\"color\":\"RED\",\"value\":1}", BeanLen3.class);
        assertEquals(BeanLen3.E.RED, r.color);
        assertEquals(1, r.value);

        r = m.readValue("{\"color\":\"AMT\",\"value\":2}", BeanLen3.class);
        assertEquals(BeanLen3.E.AMT, r.color);

        r = m.readValue("{\"color\":\"XYZ\",\"value\":3}", BeanLen3.class);
        assertEquals(BeanLen3.E.XYZ, r.color);
    }

    @Test
    void len4_successAllConstants() {
        ObjectMapper m = asmMapper();
        BeanLen4 r = m.readValue("{\"lang\":\"JAVA\",\"year\":1995}", BeanLen4.class);
        assertEquals(BeanLen4.E.JAVA, r.lang);
        assertEquals(1995, r.year);

        r = m.readValue("{\"lang\":\"RUST\",\"year\":2010}", BeanLen4.class);
        assertEquals(BeanLen4.E.RUST, r.lang);

        r = m.readValue("{\"lang\":\"PERL\",\"year\":1987}", BeanLen4.class);
        assertEquals(BeanLen4.E.PERL, r.lang);
    }

    @Test
    void len5_successAllConstants() {
        ObjectMapper m = asmMapper();
        BeanLen5 r = m.readValue("{\"platform\":\"FLASH\",\"rev\":10}", BeanLen5.class);
        assertEquals(BeanLen5.E.FLASH, r.platform);

        r = m.readValue("{\"platform\":\"MYSQL\",\"rev\":8}", BeanLen5.class);
        assertEquals(BeanLen5.E.MYSQL, r.platform);

        r = m.readValue("{\"platform\":\"REDIS\",\"rev\":7}", BeanLen5.class);
        assertEquals(BeanLen5.E.REDIS, r.platform);
    }

    @Test
    void len6_successAllConstants() {
        ObjectMapper m = asmMapper();
        BeanLen6 r = m.readValue("{\"fruit\":\"ORANGE\",\"count\":5}", BeanLen6.class);
        assertEquals(BeanLen6.E.ORANGE, r.fruit);

        r = m.readValue("{\"fruit\":\"BANANA\",\"count\":3}", BeanLen6.class);
        assertEquals(BeanLen6.E.BANANA, r.fruit);

        r = m.readValue("{\"fruit\":\"APPLES\",\"count\":7}", BeanLen6.class);
        assertEquals(BeanLen6.E.APPLES, r.fruit);
    }

    @Test
    void len7_successAllConstants() {
        ObjectMapper m = asmMapper();
        BeanLen7 r = m.readValue("{\"name\":\"BARBARA\",\"ord\":1}", BeanLen7.class);
        assertEquals(BeanLen7.E.BARBARA, r.name);

        r = m.readValue("{\"name\":\"CHARLES\",\"ord\":2}", BeanLen7.class);
        assertEquals(BeanLen7.E.CHARLES, r.name);

        r = m.readValue("{\"name\":\"DENNISK\",\"ord\":3}", BeanLen7.class);
        assertEquals(BeanLen7.E.DENNISK, r.name);
    }

    @Test
    void len8_successAllConstants() {
        ObjectMapper m = asmMapper();
        BeanLen8 r = m.readValue("{\"code\":\"LARGE123\",\"val\":1}", BeanLen8.class);
        assertEquals(BeanLen8.E.LARGE123, r.code);

        r = m.readValue("{\"code\":\"SMALL456\",\"val\":2}", BeanLen8.class);
        assertEquals(BeanLen8.E.SMALL456, r.code);

        r = m.readValue("{\"code\":\"MEDIUM99\",\"val\":3}", BeanLen8.class);
        assertEquals(BeanLen8.E.MEDIUM99, r.code);
    }

    @Test
    void len9_successAllConstants() {
        ObjectMapper m = asmMapper();
        BeanLen9 r = m.readValue("{\"tag\":\"ALPHABETA\",\"n\":1}", BeanLen9.class);
        assertEquals(BeanLen9.E.ALPHABETA, r.tag);

        r = m.readValue("{\"tag\":\"BRAVOBETA\",\"n\":2}", BeanLen9.class);
        assertEquals(BeanLen9.E.BRAVOBETA, r.tag);

        r = m.readValue("{\"tag\":\"CHARLIE42\",\"n\":3}", BeanLen9.class);
        assertEquals(BeanLen9.E.CHARLIE42, r.tag);
    }

    @Test
    void len10_successAllConstants() {
        ObjectMapper m = asmMapper();
        BeanLen10 r = m.readValue("{\"align\":\"HORIZONTAL\",\"px\":10}", BeanLen10.class);
        assertEquals(BeanLen10.E.HORIZONTAL, r.align);

        r = m.readValue("{\"align\":\"VERTICAL14\",\"px\":5}", BeanLen10.class);
        assertEquals(BeanLen10.E.VERTICAL14, r.align);

        r = m.readValue("{\"align\":\"DIAGONAL22\",\"px\":7}", BeanLen10.class);
        assertEquals(BeanLen10.E.DIAGONAL22, r.align);
    }

    @Test
    void len11_successAllConstants() {
        ObjectMapper m = asmMapper();
        BeanLen11 r = m.readValue("{\"typ\":\"DIAGNOSTIC1\",\"cd\":10}", BeanLen11.class);
        assertEquals(BeanLen11.E.DIAGNOSTIC1, r.typ);

        r = m.readValue("{\"typ\":\"MAINFRAME99\",\"cd\":20}", BeanLen11.class);
        assertEquals(BeanLen11.E.MAINFRAME99, r.typ);

        r = m.readValue("{\"typ\":\"FIREWALLS1\",\"cd\":30}", BeanLen11.class);
        assertEquals(BeanLen11.E.FIREWALLS1, r.typ);
    }

    @Test
    void multipleEnums_inOneBean() {
        ObjectMapper m = asmMapper();
        BeanMultiEnum r = m.readValue(
                "{\"player\":\"JAVA\",\"size\":\"LARGE\",\"count\":42}",
                BeanMultiEnum.class);
        assertEquals(BeanMultiEnum.Player.JAVA, r.player);
        assertEquals(BeanMultiEnum.Size.LARGE, r.size);
        assertEquals(42, r.count);

        r = m.readValue(
                "{\"player\":\"FLASH\",\"size\":\"SMALL\",\"count\":99}",
                BeanMultiEnum.class);
        assertEquals(BeanMultiEnum.Player.FLASH, r.player);
        assertEquals(BeanMultiEnum.Size.SMALL, r.size);
    }

    @Test
    void nullValue_fallsThroughToReflect() {
        ObjectMapper m = asmMapper();
        BeanNullable r = m.readValue("{\"value\":null,\"name\":\"x\"}", BeanNullable.class);
        assertNull(r.value);
        assertEquals("x", r.name);
    }

    @Test
    void lastFieldWithCloseBraceTerminator() {
        ObjectMapper m = asmMapper();
        // Enum is the LAST field of the object — post-value separator is `}`
        BeanLen4 r = m.readValue("{\"year\":1995,\"lang\":\"JAVA\"}", BeanLen4.class);
        assertEquals(BeanLen4.E.JAVA, r.lang);
        assertEquals(1995, r.year);
    }

    @Test
    void unknownEnumValue_fallsBackToReflect() {
        ObjectMapper m = asmMapper();
        // "SCALA" isn't in BeanLen5.E.values — inline match fails, falls back
        // to reflection which throws JSONException.
        assertThrows(Exception.class, () ->
                m.readValue("{\"platform\":\"SCALA\",\"rev\":1}", BeanLen5.class));
    }

    @Test
    void eishayStylePlayer() {
        // Simulates the exact hot path from Eishay bench:
        //   {"player":"JAVA",...} within Media.
        // This is what Phase B2 targets.
        ObjectMapper m = asmMapper();
        BeanMultiEnum r = m.readValue(
                "{\"player\":\"JAVA\",\"size\":\"SMALL\",\"count\":1}",
                BeanMultiEnum.class);
        assertEquals(BeanMultiEnum.Player.JAVA, r.player);
    }
}

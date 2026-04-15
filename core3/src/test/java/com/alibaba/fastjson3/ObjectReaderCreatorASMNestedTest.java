package com.alibaba.fastjson3;

import com.alibaba.fastjson3.reader.ReaderCreatorType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link com.alibaba.fastjson3.reader.ObjectReaderCreatorASM}'s
 * nested POJO inline case (Phase B5) and List&lt;String&gt; inline case
 * (Phase B5.5). The generator eagerly resolves an ASM child reader for
 * each nested POJO field at construction time and stores it in the
 * {@code itemReaders} slot; the generated {@code readObjectUTF8} loads
 * the child reader directly and calls its {@code readObjectUTF8}, avoiding
 * the {@code fallback.readFieldUTF8} reflection detour.
 */
public class ObjectReaderCreatorASMNestedTest {
    public static final class Inner {
        public int height;
        public String title;
    }

    public static final class Outer {
        public String name;
        public Inner inner;
        public int count;
    }

    public static final class Deep {
        public Outer top;
        public int tag;
    }

    public static final class WithStringList {
        public String name;
        public List<String> tags;
        public int count;
    }

    public static final class Combined {
        public String label;
        public Inner inner;
        public List<String> tags;
        public int n;
    }

    private static ObjectMapper asmMapper() {
        return ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.ASM)
                .build();
    }

    // ---------- Nested POJO (B5) ----------

    @Test
    void nestedPojo_success() {
        ObjectMapper m = asmMapper();
        Outer o = m.readValue(
                "{\"name\":\"x\",\"inner\":{\"height\":42,\"title\":\"t\"},\"count\":7}",
                Outer.class);
        assertEquals("x", o.name);
        assertNotNull(o.inner);
        assertEquals(42, o.inner.height);
        assertEquals("t", o.inner.title);
        assertEquals(7, o.count);
    }

    @Test
    void nestedPojo_nullLeavesDefault() {
        ObjectMapper m = asmMapper();
        Outer o = m.readValue(
                "{\"name\":\"x\",\"inner\":null,\"count\":5}",
                Outer.class);
        assertNull(o.inner);
        assertEquals("x", o.name);
        assertEquals(5, o.count);
    }

    @Test
    void nestedPojo_leadingWhitespace() {
        ObjectMapper m = asmMapper();
        Outer o = m.readValue(
                "{\"name\":\"x\",\"inner\":   {\"height\":1,\"title\":\"t\"}   ,\"count\":1}",
                Outer.class);
        assertEquals(1, o.inner.height);
        assertEquals("t", o.inner.title);
    }

    @Test
    void nestedPojo_twoLevelsDeep() {
        ObjectMapper m = asmMapper();
        Deep d = m.readValue(
                "{\"top\":{\"name\":\"a\",\"inner\":{\"height\":9,\"title\":\"b\"},\"count\":3},\"tag\":99}",
                Deep.class);
        assertEquals(99, d.tag);
        assertEquals("a", d.top.name);
        assertEquals(9, d.top.inner.height);
        assertEquals("b", d.top.inner.title);
    }

    @Test
    void nestedPojo_nestedFieldFirstPosition() {
        ObjectMapper m = asmMapper();
        Outer o = m.readValue(
                "{\"inner\":{\"height\":10,\"title\":\"t\"},\"name\":\"n\",\"count\":1}",
                Outer.class);
        assertEquals(10, o.inner.height);
        assertEquals("n", o.name);
    }

    // ---------- List<String> (B5.5) ----------

    @Test
    void stringList_empty() {
        ObjectMapper m = asmMapper();
        WithStringList w = m.readValue(
                "{\"name\":\"x\",\"tags\":[],\"count\":0}",
                WithStringList.class);
        assertNotNull(w.tags);
        assertEquals(0, w.tags.size());
    }

    @Test
    void stringList_single() {
        ObjectMapper m = asmMapper();
        WithStringList w = m.readValue(
                "{\"name\":\"x\",\"tags\":[\"a\"],\"count\":1}",
                WithStringList.class);
        assertEquals(List.of("a"), w.tags);
    }

    @Test
    void stringList_multiple() {
        ObjectMapper m = asmMapper();
        WithStringList w = m.readValue(
                "{\"name\":\"x\",\"tags\":[\"alpha\",\"beta\",\"gamma\"],\"count\":3}",
                WithStringList.class);
        assertEquals(List.of("alpha", "beta", "gamma"), w.tags);
    }

    @Test
    void stringList_null() {
        ObjectMapper m = asmMapper();
        WithStringList w = m.readValue(
                "{\"name\":\"x\",\"tags\":null,\"count\":0}",
                WithStringList.class);
        assertNull(w.tags);
    }

    @Test
    void stringList_leadingWhitespace() {
        ObjectMapper m = asmMapper();
        WithStringList w = m.readValue(
                "{\"name\":\"x\",\"tags\":  [ \"a\" , \"b\" ]  ,\"count\":2}",
                WithStringList.class);
        assertEquals(List.of("a", "b"), w.tags);
    }

    @Test
    void stringList_lastField() {
        ObjectMapper m = asmMapper();
        WithStringList w = m.readValue(
                "{\"name\":\"x\",\"count\":2,\"tags\":[\"p\",\"q\"]}",
                WithStringList.class);
        assertEquals(List.of("p", "q"), w.tags);
    }

    // ---------- Nested + StringList combined (Eishay Media shape) ----------

    @Test
    void combinedNestedAndStringList() {
        ObjectMapper m = asmMapper();
        Combined c = m.readValue(
                "{\"label\":\"L\",\"inner\":{\"height\":1,\"title\":\"t\"},\"tags\":[\"x\",\"y\"],\"n\":5}",
                Combined.class);
        assertEquals("L", c.label);
        assertEquals(1, c.inner.height);
        assertEquals("t", c.inner.title);
        assertEquals(List.of("x", "y"), c.tags);
        assertEquals(5, c.n);
    }
}
